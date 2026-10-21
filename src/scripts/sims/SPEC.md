# Simulation specs

Each custom animation draws a pure model. These specs say what the model must do; `tests/unit/sims.test.ts` encodes them. Change the spec, the test and the model together.

## cache-stampede
Source: "Scaling Memcache at Facebook" (NSDI 2013, §3.2.1 leases: thundering herds); single-flight / request coalescing.
- Before `expireAt`, every request is a cache hit and no database query runs.
- Without coalescing: from `expireAt` until the first query finishes (`expireAt + recomputeTicks`), **every** request starts its own database query.
- With coalescing: exactly **one** query starts; other requests wait for it and start no query.
- When a query finishes, the value is cached again; later requests are hits.
- Total queries: without coalescing = `requestsPerTick × recomputeTicks`; with coalescing = 1.

## replication-lag
Source: DDIA ch. 5 "Problems with Replication Lag" (read-your-writes).
- A read from the follower before `lagMs` returns the old value (stale); at or after `lagMs` it returns the new value.
- With read-your-writes on, the writer's reads within `rywWindowMs` go to the leader and are never stale.

## rollout
Sources: the safe-deploys lesson's incident postmortems (global config pushes) and staged-rollout practice.
- Global: 100% of users get errors from minute 0 until `detectMinutes + rollbackMinutes`.
- Staged: stage k reaches `stages[k]`% of users at minute `k × bakeMinutes`. When errors are detected, no new stage starts. Affected users stay at the last started stage until the rollback finishes.
- After the fix, 0% are affected.
- If detection is faster than the bake time, the peak equals the first stage.

## token-bucket
Source: the rate-limiting lesson's sources (Stripe "Scaling your API with rate limiters"; token bucket definition).
- The bucket starts full (`capacity` tokens) and gains `refillPerSec / 10` tokens every 100 ms tick, never above `capacity`.
- A request is allowed if a whole token is available, and takes one token; otherwise it is rejected (HTTP 429).
- Allowed + rejected = arrivals in every tick.
- A burst up to `capacity` is allowed at once; over a long time, allowed requests average at most `refillPerSec`.

## hash-ring
Sources: Karger et al. 1997 (consistent hashing); Amazon Dynamo 2007 (virtual nodes).
- `hash mod N`: adding one server to N moves about N/(N+1) of the keys (most of them).
- Consistent hash ring: adding one server moves only the keys it takes over, about 1/(N+1) of them. Keys never move between two old servers.
- More virtual nodes per server make the share of keys per server more even.
