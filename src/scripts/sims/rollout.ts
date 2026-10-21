// Bad config rollout model (lesson safe-deploys). Pure and deterministic.
// SPEC: see SPEC.md § rollout.
export interface RolloutParams {
  strategy: 'global' | 'staged';
  stages: number[]; // share of users per stage, e.g. [1, 10, 50, 100] (staged only)
  bakeMinutes: number; // wait between stages while watching health checks
  detectMinutes: number; // how long until alerts notice errors after users are hit
  rollbackMinutes: number; // how long a rollback takes once started
  minutes: number; // length of the timeline
}

export interface RolloutMinute {
  m: number;
  affectedPct: number; // users getting errors in this minute
}

export interface RolloutResult {
  timeline: RolloutMinute[];
  peakPct: number;
  userMinutes: number; // sum of affectedPct over time (area under the curve)
  detectedAt: number;
  fixedAt: number;
}

export function simulateRollout(p: RolloutParams): RolloutResult {
  const detectedAt = p.detectMinutes; // errors start at minute 0 for the first users reached
  const fixedAt = detectedAt + p.rollbackMinutes;
  const timeline: RolloutMinute[] = [];
  for (let m = 0; m < p.minutes; m++) {
    let reach: number;
    if (p.strategy === 'global') reach = 100;
    else {
      // Stage k starts at k × bake, unless the rollout was stopped at detection.
      const k = Math.min(p.stages.length - 1, Math.floor(m / p.bakeMinutes));
      const lastStarted = Math.min(k, Math.floor(Math.max(0, detectedAt - 1) / p.bakeMinutes));
      reach = p.stages[m >= detectedAt ? lastStarted : k];
    }
    timeline.push({ m, affectedPct: m < fixedAt ? reach : 0 });
  }
  return {
    timeline,
    peakPct: Math.max(...timeline.map((x) => x.affectedPct)),
    userMinutes: timeline.reduce((n, x) => n + x.affectedPct, 0),
    detectedAt,
    fixedAt,
  };
}
