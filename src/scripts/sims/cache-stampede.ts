// Cache stampede model (lesson caching-stampedes). Pure and deterministic.
// SPEC: see SPEC.md § cache-stampede.
export interface StampedeParams {
  ticks: number; // simulated time steps (1 tick = 100 ms)
  requestsPerTick: number; // reads of the hot key per tick
  expireAt: number; // tick when the cached value expires
  recomputeTicks: number; // how long one database query takes
  coalesce: boolean; // single-flight: only one query per missing key
}

export interface StampedeTick {
  t: number;
  hits: number; // served from cache
  dbQueries: number; // new database queries started this tick
  waiting: number; // requests waiting for an in-flight query (coalescing only)
  inFlight: number; // database queries running at the end of the tick
}

export function simulateStampede(p: StampedeParams): StampedeTick[] {
  const out: StampedeTick[] = [];
  let cachedUntil = p.expireAt; // value is valid for t < cachedUntil
  const finishing: number[] = []; // end ticks of running queries
  let waiting = 0;
  for (let t = 0; t < p.ticks; t++) {
    // Queries that finish now put a fresh value in the cache.
    if (finishing.some((f) => f === t)) {
      cachedUntil = Number.POSITIVE_INFINITY;
      waiting = 0;
    }
    for (let i = finishing.length - 1; i >= 0; i--) if (finishing[i] <= t) finishing.splice(i, 1);

    let hits = 0;
    let dbQueries = 0;
    if (t < cachedUntil) {
      hits = p.requestsPerTick;
    } else if (p.coalesce) {
      if (finishing.length === 0) {
        dbQueries = 1;
        finishing.push(t + p.recomputeTicks);
      }
      waiting += p.requestsPerTick - dbQueries;
    } else {
      dbQueries = p.requestsPerTick;
      for (let i = 0; i < dbQueries; i++) finishing.push(t + p.recomputeTicks);
    }
    out.push({ t, hits, dbQueries, waiting: p.coalesce ? waiting : 0, inFlight: finishing.length });
  }
  return out;
}

export function summary(ticks: StampedeTick[]): { totalQueries: number; peakInFlight: number } {
  return {
    totalQueries: ticks.reduce((n, x) => n + x.dbQueries, 0),
    peakInFlight: Math.max(0, ...ticks.map((x) => x.inFlight)),
  };
}
