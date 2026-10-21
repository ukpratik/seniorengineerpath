---
topic: caching-stampedes
subtitle: A cache makes reads fast and cheap, until one popular key expires and every request rushes to the database at once.
status: published
reviewed_by: ai-factcheck
reviewed_on: 2026-10-21
illustrative_story: true
sources:
  - id: fb-memcache
    title: "Nishtala et al., “Scaling Memcache at Facebook”, NSDI 2013 (USENIX)"
    url: https://www.usenix.org/conference/nsdi13/technical-sessions/presentation/nishtala
    note: Facebook's look-aside cache, why it deletes instead of updates, and leases (section 3.2.1).
  - id: aws-caching
    title: "Jas Chhabra, “Caching challenges and strategies”, Amazon Builders' Library"
    url: https://builder.aws.com/content/3K9TeXp6strMiJ4vhakpRD7BQWO/caching-challenges-and-strategies
    note: Side caches vs inline caches, TTLs, request coalescing, and the risk of depending on a cache.
  - id: rfc5861
    title: "Mark Nottingham, RFC 5861: HTTP Cache-Control Extensions for Stale Content (IETF, 2010)"
    url: https://www.rfc-editor.org/rfc/rfc5861
    note: The stale-while-revalidate and stale-if-error directives, with examples.
  - id: xfetch
    title: "Vattani, Chierichetti and Lowenstein, “Optimal Probabilistic Cache Stampede Prevention”, PVLDB vol. 8, 2015"
    url: https://www.vldb.org/pvldb/vol8/p886-vattani.pdf
    note: Defines the cache stampede and gives a simple random rule for refreshing early.
  - id: redis-eviction
    title: "Redis documentation, “Key eviction”"
    url: https://redis.io/docs/latest/develop/reference/eviction/
    note: The maxmemory limit and every eviction policy, with advice on which to pick.
  - id: singleflight
    title: "Go package golang.org/x/sync/singleflight"
    url: https://pkg.go.dev/golang.org/x/sync/singleflight
    note: A tiny, real request-coalescing library you can read in a few minutes.
claims_checked:
  - claim: "Facebook uses memcache as a look-aside cache, and deletes keys on writes because deletes are idempotent."
    source: fb-memcache
    note: "Section 2 and Figure 1: 'demand-filled look-aside cache'; write path is UPDATE then delete k; 'deletes are idempotent'."
  - claim: "Leases: by default one token per key every 10 seconds; other clients are told to wait; peak DB rate fell from 17K/s to 1.3K/s."
    source: fb-memcache
    note: "Section 3.2.1. Paper's herd is caused by writes that repeatedly invalidate a hot key; measured over one week for keys susceptible to thundering herds. Added 'By default', the write cause and 'over one week'."
  - claim: "A cache stampede is when a popular item expires and many requests miss and regenerate it at once; early refresh is decided at random, with a chance that rises near expiry."
    source: xfetch
    note: "Abstract and section 2.2 (PVLDB Vol. 8, No. 8, 2015): probability of early expiration increases as access time approaches expiry. The 1,000-miss example (5,000/s x 200 ms) matches the paper's rate x recompute-time reasoning."
  - claim: "With max-age=600, stale-while-revalidate=30, a response is fresh for 600 s and may be served stale for up to 30 more seconds while it is revalidated."
    source: rfc5861
    note: "RFC 5861 section 3.1 example (M. Nottingham, May 2010)."
  - claim: "allkeys-lru evicts least recently used keys and is a good default when a subset of keys is read far more often."
    source: redis-eviction
    note: "Redis 'Key eviction' rule of thumb section."
  - claim: "Side vs inline caches, request coalescing, soft/hard TTL, separate TTL for errors, cold cache risk."
    source: aws-caching
    note: "Page lists Jas Chhabra as author. Reworded inline-cache failure ('cannot work around it'), hit-rate advice ('measure hits and misses') and hard TTL (old data used until hard TTL if downstream fails)."
  - claim: "Go singleflight runs only one execution per key; duplicate callers wait and get the same result."
    source: singleflight
    note: "Group.Do documentation."
quiz:
  - q: In cache-aside, what does the app do on a cache miss?
    options:
      - Returns an error to the user.
      - Reads from the database, then puts the value in the cache.
      - Waits until the cache fills itself.
    answer: 1
    why: In cache-aside, the app owns the cache. It reads the database on a miss and fills the cache itself.
  - q: A key read 5,000 times a second expires. Each database query takes 200 ms. Without protection, what happens?
    options:
      - One request reloads it, the rest wait.
      - Many requests miss at the same moment and all run the same query.
      - The cache returns the old value automatically.
    answer: 1
    why: That is a cache stampede. Every request that misses before the first reload finishes goes to the database.
  - q: What does request coalescing do?
    options:
      - Sends one database query per key and lets other callers wait for its answer.
      - Joins many keys into one big key.
      - Spreads TTLs out with random values.
    answer: 0
    why: Only one load runs per key. Duplicate callers wait and get the same result.
  - q: Why add jitter to TTLs?
    options:
      - So keys written at the same time do not all expire at the same time.
      - To make the cache use less memory.
      - So the cache never serves old data.
    answer: 0
    why: Keys loaded together, for example after a deploy, would expire together and cause a burst of misses.
incidents: []
related: [distributed-cache, cdn-http-caching, redis, load-shedding]
---

## What broke

This story is made up, but it is common. A shopping app runs a flash sale at 8 pm. The product page for one phone gets 5,000 requests a second.

The page data sits in Redis with a TTL of 60 seconds. The database is sized for the normal load: a few hundred queries a second. Life is fine while the cache answers almost every read.

At 8:01 pm the key expires. In the next 200 ms, about a thousand requests find nothing in the cache. Each one runs the same heavy query against the database.

The database slows down, so the queries take longer. More requests miss while they wait, and they pile on too. The product page times out, and the sale page shows errors for ten minutes.

Nothing was wrong with the code or the cache. One popular key expired at the worst moment.

## The idea in simple words

A cache is a fast copy of data that is slow or costly to get. You read the copy instead of asking the database every time. A cache hit is fast. A cache miss means you must go to the source.

Every cache has to answer three questions:

1. **How does data get in?** The app fills it, or the cache fills itself.
2. **When does data leave?** It expires (TTL), it is deleted on a write (invalidation), or it is pushed out when memory is full (eviction).
3. **What happens on a miss?** If a key is popular, a miss is not one query. It can be thousands.

> [!key] **The key idea.** A cache does not just make reads faster. It hides load. When a hot key expires, all that hidden load hits the database at once. Design for that moment.

That moment is called a cache stampede, or thundering herd. A popular item expires, and many requests miss and rebuild the same item at the same time. [@xfetch]

## How it works

### Ways to fill a cache

- **Cache-aside.** The app checks the cache. On a miss, the app reads the database and writes the value into the cache. Facebook uses its memcache this way. On a write, it updates the database and then deletes the cache key. It deletes instead of updating because deletes are idempotent. [@fb-memcache]
- **Read-through.** The app only talks to the cache. On a miss, the cache itself loads from the database.
- **Write-through.** Each write goes to the cache and the database together. The cache stays fresh, but every write is slower.
- **Write-behind.** Writes go to the cache first and reach the database later, in batches. Writes are fast, but you can lose data if the cache dies first.

AWS calls a cache that the app code manages a "side cache", like cache-aside. Read-through and write-through are "inline caches". With an inline cache, the app needs no cache code. But if the cache fails, the app cannot work around it. [@aws-caching]

### TTL, invalidation and eviction

A TTL sets how long a value lives. Pick it from two facts: how old the data may be, and how often the source changes. Then measure cache hits and misses, and adjust. [@aws-caching]

Invalidation removes a key when the data changes. It keeps data fresh, but you must find every place that writes the data.

Eviction removes keys when memory is full. In Redis you set a `maxmemory` limit and a policy. `allkeys-lru` removes the least recently used keys. Redis calls it a good default when some keys are read far more often than others. [@redis-eviction]

### The stampede, step by step

Step through what happens when a hot key expires. Use Next and Back, or the ← and → keys.

[[diagram:cache-stampede]]

Now try it yourself. Turn request coalescing on and off, and change the traffic.

[[anim:cache-stampede]]

### Four ways to stop a stampede

**1. Request coalescing (single-flight).** Only one load runs per key. Others wait for it and share the answer. Go's `singleflight` package does exactly this inside one server. [@singleflight] AWS uses coalescing so only one request is pending for an uncached item. [@aws-caching]

**2. Locks or leases in the cache.** Coalescing inside one server still leaves one query per server. With 200 servers, that is 200 queries. A shared lock in the cache fixes this. Facebook's memcache gives out a "lease" token on a miss. By default, it gives at most one token per key every 10 seconds. Other clients are told to wait a moment and try again. [@fb-memcache] In Facebook's case, the herd came from writes that kept deleting a hot key. Over one week, for keys prone to thundering herds, peak database load fell from 17,000 to 1,300 queries a second. [@fb-memcache]

**3. Refresh early, with randomness.** Refresh a hot key a little before it expires, so it never goes missing. If every server refreshes at the same moment, you get a smaller stampede. So each request decides at random, and the chance rises as expiry gets closer. [@xfetch] Also add jitter to TTLs, so keys loaded together do not all expire together.

**4. Serve stale while you refresh.** Keep the old value a little longer and return it while one request fetches the new value. HTTP has a header for this: `stale-while-revalidate`. With `max-age=600, stale-while-revalidate=30`, a response is fresh for 600 seconds. A cache may then serve it stale for 30 more seconds while it checks for a new copy. [@rfc5861] AWS describes a similar idea: refresh after a "soft" TTL, and keep using the old value until a "hard" TTL if the source fails. [@aws-caching]

## Trade-offs

| Fix | Stops the stampede? | Cost | Use it when |
|---|---|---|---|
| Request coalescing in each server | Partly. One query per server, not one in total. | Small code change. | Always, as a first step. |
| Lock or lease in the shared cache | Yes. One query in total. | Waiting clients add latency. Lost locks need a timeout. | Many servers and very hot keys. |
| Early refresh with jitter | Yes, for keys that are read often. | Some extra loads before they are needed. | Hot keys you can predict. |
| Stale-while-revalidate | Yes. Users get the old value fast. | Users may see data a few seconds old. | Data where a little staleness is fine, like product pages. |
| Longer TTL only | No. It only makes stampedes rarer. | More stale data. | Data that rarely changes. |

**Rule of thumb:** add coalescing everywhere. For your top keys, add stale-while-revalidate or a lease too.

## At work: how a senior uses this

- Ask in design reviews: **"What happens to the database if the cache is empty?"** If the answer is "it falls over", you depend on the cache. Plan for a cold start. [@aws-caching]
- Know your hot keys. Track the top keys by reads. A sale, a viral post or a homepage banner creates new ones.
- Decide how stale each kind of data may be. Write it down: "prices: 5 seconds, product text: 10 minutes".
- Watch hit rate, evictions and database load on the same dashboard. A falling hit rate is an early warning.
- Never flush the whole cache in production without a plan. It turns every key into a miss at once.
- Cache "not found" and error results too, with a short TTL. Otherwise a missing item sends every request to the database. AWS suggests a separate TTL for error results. [@aws-caching]

## With AI

### What to ask the agent

> "Wrap this getProduct function with cache-aside in Redis. Use request coalescing so only one database call runs per key at a time."

> "Add a soft TTL and a hard TTL. After the soft TTL, return the cached value and refresh it in the background. Add 10% random jitter to both."

> "Write a load test where 1,000 requests ask for the same key just after it expires. Assert that the database gets at most 1 query."

### What to check yourself

- The coalescing key is the cache key, not something that differs per user.
- A failed or slow load does not leave other callers waiting forever. There is a timeout.
- A lock in Redis has an expiry, so a crashed server does not hold it forever.
- On a write, the code deletes or updates the cache **after** the database commit.
- Error results are not cached for as long as good results.

## Check yourself

## Go deeper

Related lessons: [Distributed caches](/learn/distributed-caches), [CDN and HTTP caching](/learn/cdn-and-http-caching) and [Redis in depth](/learn/redis-in-depth).
