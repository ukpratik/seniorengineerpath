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

// Where an event is drawn on a lane that shows `spanMs` (BUG-051, QA2-02).
// The tick sits at the exact time: `pct` % of the lane. So two events at the same
// time line up, and a later event is always further right. The label starts at the
// tick and moves left by `pct` % of its own width: it stays inside the lane and
// still covers the tick, whatever its width.
export function lanePosition(ms: number, spanMs: number): { pct: number; tick: string; label: string } {
  const pct = Math.max(0, Math.min(100, (ms / spanMs) * 100));
  const style = `left:${pct}%;transform:translateX(-${pct}%)`;
  return { pct, tick: style, label: style };
}
