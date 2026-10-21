// Replication lag model (lesson replication). Pure and deterministic.
// SPEC: see SPEC.md § replication-lag.
export interface LagParams {
  lagMs: number; // how long the follower takes to apply the write
  readAfterMs: number; // when the client reads, after its write
  readYourWrites: boolean; // route the writer's reads to the leader for a short window
  rywWindowMs: number; // that window
}

export interface LagResult {
  readFrom: 'leader' | 'follower';
  value: 'old' | 'new';
  stale: boolean;
}

export function simulateRead(p: LagParams): LagResult {
  const toLeader = p.readYourWrites && p.readAfterMs < p.rywWindowMs;
  if (toLeader) return { readFrom: 'leader', value: 'new', stale: false };
  const applied = p.readAfterMs >= p.lagMs;
  return { readFrom: 'follower', value: applied ? 'new' : 'old', stale: !applied };
}
