import { describe, it, expect } from 'vitest';
import { simulateStampede, summary } from '../../src/scripts/sims/cache-stampede.ts';
import { simulateRead } from '../../src/scripts/sims/replication-lag.ts';
import { simulateRollout } from '../../src/scripts/sims/rollout.ts';
import { simulateBucket, burstTraffic } from '../../src/scripts/sims/token-bucket.ts';
import { modN, ring, keys, nodeNames, movedShare, loadShare } from '../../src/scripts/sims/hash-ring.ts';
import { simulateRaft } from '../../src/scripts/sims/raft-election.ts';

describe('cache stampede (SPEC § cache-stampede)', () => {
  const base = { ticks: 30, requestsPerTick: 50, expireAt: 10, recomputeTicks: 5 };

  it('serves hits before the key expires', () => {
    const t = simulateStampede({ ...base, coalesce: false });
    expect(t.slice(0, 10).every((x) => x.hits === 50 && x.dbQueries === 0)).toBe(true);
  });

  it('without coalescing every miss queries the database until the first query finishes', () => {
    const t = simulateStampede({ ...base, coalesce: false });
    expect(t.slice(10, 15).every((x) => x.dbQueries === 50)).toBe(true);
    expect(t[15].hits).toBe(50);
    expect(summary(t).totalQueries).toBe(50 * 5);
    expect(summary(t).peakInFlight).toBe(250);
  });

  it('with coalescing exactly one query runs and the others wait', () => {
    const t = simulateStampede({ ...base, coalesce: true });
    expect(summary(t).totalQueries).toBe(1);
    expect(t[10].dbQueries).toBe(1);
    expect(t[12].waiting).toBeGreaterThan(0);
    expect(t[15].hits).toBe(50);
    expect(summary(t).peakInFlight).toBe(1);
  });
});

describe('replication lag (SPEC § replication-lag)', () => {
  it('a follower read before the lag is stale, after is fresh', () => {
    expect(simulateRead({ lagMs: 500, readAfterMs: 100, readYourWrites: false, rywWindowMs: 0 })).toEqual({ readFrom: 'follower', value: 'old', stale: true });
    expect(simulateRead({ lagMs: 500, readAfterMs: 500, readYourWrites: false, rywWindowMs: 0 }).stale).toBe(false);
  });

  it('read-your-writes sends the writer to the leader inside the window', () => {
    expect(simulateRead({ lagMs: 500, readAfterMs: 100, readYourWrites: true, rywWindowMs: 1000 })).toEqual({ readFrom: 'leader', value: 'new', stale: false });
  });
});

describe('rollout (SPEC § rollout)', () => {
  const base = { stages: [1, 10, 50, 100], bakeMinutes: 10, detectMinutes: 5, rollbackMinutes: 5, minutes: 60 };

  it('a global push hits everyone until the rollback finishes', () => {
    const r = simulateRollout({ ...base, strategy: 'global' });
    expect(r.peakPct).toBe(100);
    expect(r.timeline[9].affectedPct).toBe(100);
    expect(r.timeline[10].affectedPct).toBe(0);
    expect(r.userMinutes).toBe(1000);
  });

  it('a staged rollout caught in the first stage only hurts that stage', () => {
    const r = simulateRollout({ ...base, strategy: 'staged' });
    expect(r.peakPct).toBe(1);
    expect(r.userMinutes).toBe(10);
  });

  it('slow detection lets the rollout reach later stages, but no new stage starts after detection', () => {
    const r = simulateRollout({ ...base, strategy: 'staged', detectMinutes: 15, rollbackMinutes: 5 });
    expect(r.timeline[12].affectedPct).toBe(10);
    expect(r.timeline[19].affectedPct).toBe(10);
    expect(r.timeline[20].affectedPct).toBe(0);
    expect(r.peakPct).toBe(10);
  });
});


describe('token bucket (SPEC § token-bucket)', () => {
  it('allows a burst up to capacity, then rejects until tokens refill', () => {
    const t = simulateBucket({ capacity: 10, refillPerSec: 10, arrivals: [0, 0, 25, 0, 0, 0, 0, 0, 0, 0, 0, 5] });
    expect(t[2].allowed).toBe(10);
    expect(t[2].rejected).toBe(15);
    expect(t[11].allowed).toBe(5);
    t.forEach((x, i) => expect(x.allowed + x.rejected).toBe([0, 0, 25, 0, 0, 0, 0, 0, 0, 0, 0, 5][i]));
  });

  it('long-run allowed rate never exceeds the refill rate (plus the first full bucket)', () => {
    const arrivals = burstTraffic(100, 5, 50, 100); // 50 req/s offered, 10 s
    const t = simulateBucket({ capacity: 20, refillPerSec: 30, arrivals });
    const allowed = t.reduce((n, x) => n + x.allowed, 0);
    expect(allowed).toBeLessThanOrEqual(20 + 30 * 10);
    expect(t.every((x) => x.tokens >= 0 && x.tokens <= 20)).toBe(true);
  });
});

describe('hash ring (SPEC § hash-ring)', () => {
  const ks = keys(5000);
  it('hash mod N moves most keys when a server is added', () => {
    const moved = movedShare(modN(ks, nodeNames(4)), modN(ks, nodeNames(5)));
    expect(moved).toBeGreaterThan(0.7);
  });

  it('a ring moves only about 1/(N+1) of the keys, and only to the new server', () => {
    const before = ring(ks, nodeNames(4), 100);
    const after = ring(ks, nodeNames(5), 100);
    const moved = movedShare(before, after);
    expect(moved).toBeGreaterThan(0.1);
    expect(moved).toBeLessThan(0.3);
    for (const [k, n] of before) if (after.get(k) !== n) expect(after.get(k)).toBe('server-5');
  });

  it('more virtual nodes give a more even load', () => {
    const spread = (v: number) => {
      const s = loadShare(ring(ks, nodeNames(5), v), nodeNames(5)).map((x) => x.share);
      return Math.max(...s) - Math.min(...s);
    };
    expect(spread(200)).toBeLessThan(spread(1));
  });
});


describe('raft election (SPEC § raft-election)', () => {
  const base = { stopAt: 100, untilMs: 3000 };

  it('elects a new leader after the leader stops, with a majority of the whole cluster', () => {
    for (let seed = 1; seed <= 50; seed++) {
      const r = simulateRaft({ ...base, nodes: 5, stopped: [0], seed });
      expect(r.leader).not.toBeNull();
      expect(r.leader).not.toBe(0);
      const win = r.events.find((e) => e.kind === 'leader')!;
      expect(win.votes).toBeGreaterThanOrEqual(3);
      expect(win.t).toBeGreaterThanOrEqual(base.stopAt + 150 - 50); // no sooner than min timeout after the last heartbeat
    }
  });

  it('never elects two leaders in the same term, and nobody votes twice in a term', () => {
    for (let seed = 1; seed <= 200; seed++) {
      for (const [nodes, stopped] of [[5, [0]], [5, [0, 1]], [3, [0]]] as const) {
        const r = simulateRaft({ ...base, nodes, stopped: [...stopped], seed });
        const leadersPerTerm = new Map<number, number>();
        for (const e of r.events.filter((x) => x.kind === 'leader')) leadersPerTerm.set(e.term, (leadersPerTerm.get(e.term) ?? 0) + 1);
        expect([...leadersPerTerm.values()].every((n) => n === 1)).toBe(true);
        const votes = new Set<string>();
        for (const e of r.events.filter((x) => x.kind === 'vote')) {
          const k = `${e.node}:${e.term}`;
          expect(votes.has(k)).toBe(false);
          votes.add(k);
        }
      }
    }
  });

  it('survives a minority of failures but not a majority', () => {
    for (let seed = 1; seed <= 30; seed++) {
      expect(simulateRaft({ ...base, nodes: 5, stopped: [0, 1], seed }).leader).not.toBeNull();
      expect(simulateRaft({ ...base, nodes: 5, stopped: [0, 1, 2], seed }).leader).toBeNull();
      expect(simulateRaft({ ...base, nodes: 3, stopped: [0], seed }).leader).not.toBeNull();
      expect(simulateRaft({ ...base, nodes: 3, stopped: [0, 1], seed }).leader).toBeNull();
    }
  });

  it('keeps the same leader while it is healthy', () => {
    const r = simulateRaft({ ...base, nodes: 5, stopped: [], seed: 7 });
    expect(r.leader).toBe(0);
    expect(r.events.filter((e) => e.kind === 'timeout')).toEqual([]);
  });
});
