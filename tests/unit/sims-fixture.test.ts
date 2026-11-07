// Golden outputs of the six simulation models, shared with the Android app (US-007, architecture.md §5).
// The app's Kotlin port (android/content/.../sims) must give exactly these results; SimsFixtureTest.kt checks that.
// After a deliberate model change, regenerate with UPDATE_SIM_FIXTURES=1 npx vitest run tests/unit/sims-fixture.test.ts
import { describe, it, expect } from 'vitest';
import { existsSync, readFileSync, writeFileSync } from 'node:fs';
import { simulateStampede, summary } from '../../src/scripts/sims/cache-stampede.ts';
import { simulateRead } from '../../src/scripts/sims/replication-lag.ts';
import { simulateRollout } from '../../src/scripts/sims/rollout.ts';
import { simulateBucket, burstTraffic } from '../../src/scripts/sims/token-bucket.ts';
import { hash, modN, ring, keys, nodeNames, movedShare, loadShare } from '../../src/scripts/sims/hash-ring.ts';
import { simulateRaft } from '../../src/scripts/sims/raft-election.ts';

const FILE = new URL('../fixtures/sims/cases.json', import.meta.url);

function cases() {
  const stampede = [];
  for (const requestsPerTick of [5, 40, 100]) for (const coalesce of [false, true]) {
    const params = { ticks: 30, requestsPerTick, expireAt: 8, recomputeTicks: 6, coalesce };
    const ticks = simulateStampede(params);
    stampede.push({ params, ticks, summary: summary(ticks) });
  }
  const lag = [];
  for (const lagMs of [0, 500, 800, 2000]) for (const readAfterMs of [0, 300, 800, 1500]) for (const readYourWrites of [false, true]) {
    const params = { lagMs, readAfterMs, readYourWrites, rywWindowMs: 1000 };
    lag.push({ params, result: simulateRead(params) });
  }
  const rollout = [];
  for (const strategy of ['global', 'staged'] as const) for (const detectMinutes of [1, 5, 15, 30]) {
    const params = { strategy, stages: [1, 10, 50, 100], bakeMinutes: 10, detectMinutes, rollbackMinutes: 5, minutes: 60 };
    rollout.push({ params, result: simulateRollout(params) });
  }
  const bucket = [];
  for (const capacity of [5, 20, 100]) for (const refillPerSec of [10, 50, 200]) for (const burst of [0, 80, 200]) {
    const arrivals = burstTraffic(30, 3, 10, burst);
    bucket.push({ params: { capacity, refillPerSec, arrivals }, ticks: simulateBucket({ capacity, refillPerSec, arrivals }) });
  }
  const ks = keys(4000);
  const hashes = ['', 'a', 'user:0', 'server-1#0', 'server-12#199', 'naïve ✓'].map((s) => ({ s, h: hash(s) }));
  const ringCases = [];
  for (const n of [2, 4, 12]) for (const v of [1, 10, 100]) {
    const before = nodeNames(n);
    const after = nodeNames(n + 1);
    const count = (m: Map<string, string>, nodes: string[]) => loadShare(m, nodes).map((x) => Math.round(x.share * ks.length));
    ringCases.push({
      params: { servers: n, vnodes: v, keys: ks.length },
      modMoved: Math.round(movedShare(modN(ks, before), modN(ks, after)) * ks.length),
      ringMoved: Math.round(movedShare(ring(ks, before, v), ring(ks, after, v)) * ks.length),
      ringCounts: count(ring(ks, after, v), after),
    });
  }
  const raft = [];
  for (const nodes of [3, 5]) for (const stopCount of [0, 1, 2, 3]) {
    if (stopCount >= nodes) continue;
    for (const seed of [1, 2, 3, 7]) {
      const params = { nodes, stopped: Array.from({ length: stopCount }, (_, i) => i), stopAt: 100, untilMs: 2500, seed };
      raft.push({ params, result: simulateRaft(params) });
    }
  }
  return { stampede, lag, rollout, bucket, hashes, ring: ringCases, raft };
}

describe('simulation fixture shared with the Android app', () => {
  it('matches the models', () => {
    const now = JSON.stringify(cases(), null, 1);
    if (process.env.UPDATE_SIM_FIXTURES || !existsSync(FILE)) writeFileSync(FILE, now + '\n');
    expect(readFileSync(FILE, 'utf8')).toBe(now + '\n');
  });
});
