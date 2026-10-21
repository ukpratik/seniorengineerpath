import { describe, it, expect } from 'vitest';
import { simulateStampede, summary } from '../../src/scripts/sims/cache-stampede.ts';
import { simulateRead } from '../../src/scripts/sims/replication-lag.ts';
import { simulateRollout } from '../../src/scripts/sims/rollout.ts';

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
