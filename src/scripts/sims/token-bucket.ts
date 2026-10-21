// Token bucket rate limiter model (lesson rate-limiting). Pure and deterministic.
// SPEC: see SPEC.md § token-bucket.
export interface BucketParams {
  capacity: number; // most tokens the bucket can hold (the allowed burst)
  refillPerSec: number; // tokens added per second
  arrivals: number[]; // requests arriving in each 100 ms tick
}

export interface BucketTick {
  t: number;
  allowed: number;
  rejected: number;
  tokens: number; // tokens left at the end of the tick
}

export function simulateBucket(p: BucketParams): BucketTick[] {
  let tokens = p.capacity; // starts full
  return p.arrivals.map((n, t) => {
    if (t > 0) tokens = Math.min(p.capacity, tokens + p.refillPerSec / 10);
    const allowed = Math.min(n, Math.floor(tokens));
    tokens -= allowed;
    return { t, allowed, rejected: n - allowed, tokens: Math.round(tokens * 100) / 100 };
  });
}

// Steady traffic with one burst in the middle.
export function burstTraffic(ticks: number, steadyPerTick: number, burstAt: number, burstSize: number): number[] {
  return Array.from({ length: ticks }, (_, t) => steadyPerTick + (t === burstAt ? burstSize : 0));
}
