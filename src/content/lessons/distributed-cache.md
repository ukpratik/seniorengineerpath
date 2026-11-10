---
topic: distributed-cache
subtitle: One cache server is not enough. Learn how to spread keys over many, what each lost node costs you, and how to keep the copies honest.
status: published
illustrative_story: false
glossary_optout: [lease]
sources:
  - id: slack-2222
    title: "Laura Nolan (Slack Engineering), “Slack’s Incident on 2-22-22” (2022)"
    url: https://slack.engineering/slacks-incident-on-2-22-22/
    note: A real cache cascade. Empty Memcached nodes, a query that hits every shard, and how Slack throttled its way back.
  - id: fb-memcache
    title: "Nishtala et al., “Scaling Memcache at Facebook”, NSDI 2013 (USENIX)"
    url: https://www.usenix.org/conference/nsdi13/technical-sessions/presentation/nishtala
    note: Look-aside caching, leases (3.2.1), replication in pools (3.2.3), Gutter (3.3), cross-region deletes (4.1, 5) and cold cluster warmup (4.3).
  - id: mcrouter
    title: "mcrouter README (GitHub, meta/mcrouter)"
    url: https://github.com/meta/mcrouter
    note: What the Memcached router does, with its feature list and scale.
  - id: mcrouter-routes
    title: "mcrouter wiki, “List of Route Handles”"
    url: https://github.com/meta/mcrouter/wiki/List-of-Route-Handles
    note: The building blocks of mcrouter's config, including HashRoute and WarmUpRoute.
  - id: twemproxy
    title: "twemproxy (nutcracker) README (GitHub, twitter/twemproxy)"
    url: https://github.com/twitter/twemproxy
    note: A small proxy for Memcached and Redis, with its key distribution modes.
  - id: redis-cluster
    title: "Redis documentation, “Redis cluster specification”"
    url: https://redis.io/docs/latest/operate/oss_and_stack/reference/cluster-spec/
    note: Hash slots, MOVED and ASK redirects, and how slots move between nodes.
  - id: redis-eviction
    title: "Redis documentation, “Key eviction”"
    url: https://redis.io/docs/latest/develop/reference/eviction/
    note: Every eviction policy, how LRU and LFU are approximated, and how to read your hit rate.
  - id: redis-csc
    title: "Redis documentation, “Client-side caching reference”"
    url: https://redis.io/docs/latest/develop/reference/client-side-caching/
    note: Server-assisted local caches, invalidation messages, and the race you must handle.
claims_checked:
  - claim: A lease is a 64-bit token given on a miss; a delete invalidates it, so a late stale set is rejected.
    source: fb-memcache
    note: Section 3.2.1, "The lease is a 64-bit token bound to the specific key"; verification fails if memcached "has invalidated the lease token due to receiving a delete request".
  - claim: Gutter is about 1% of a cluster's servers, used instead of rehashing because one key can be 20% of a server's requests; it cut client-visible failures by 99%.
    source: fb-memcache
    note: Section 3.3, all three numbers checked verbatim. Cold cluster warmup (4.3) "a few hours instead of a few days" and the two-second hold-off also checked.
  - claim: Memcached nodes left Consul service discovery during a 25% agent upgrade and Mcrib swapped in empty spares; GDM membership misses queried every shard, load grew "superlinearly", and timeouts stopped the refill.
    source: slack-2222
    note: All quotes verbatim from the post. Mcrib contributed "not by being incorrect or inefficient"; its efficiency "increased the churn in the cache tier".
  - claim: Twemproxy's default key distribution is ketama (consistent hashing), and it keeps backend connection counts low.
    source: twemproxy
    note: README, distribution setting - "ketama (default, recommended...)".
  - claim: Redis Cluster has 16384 hash slots, redirects with MOVED, uses no proxies, and moves keys between nodes with MIGRATE.
    source: redis-cluster
    note: Spec checked, including "There are no proxies".
reviewed_by: ai-factcheck
reviewed_on: 2026-11-10
quiz:
  - q: A Memcached pool uses consistent hashing over 10 nodes. One node dies and is replaced by an empty spare. What happens to the hit rate?
    options:
      - Nothing. The other nodes still have copies of its keys.
      - It drops by about one tenth, until the new node fills up from misses.
      - It drops to zero, because every key moves.
    answer: 1
    why: This pool keeps no copies. The keys that lived on the lost node now miss, and the other nine nodes keep theirs.
  - q: Why did Facebook send a failed server's traffic to a small Gutter pool, instead of rehashing it over the remaining servers?
    options:
      - Rehashing is too slow to compute.
      - A single hot key could overload whichever server took it over.
      - Gutter keeps data forever, so it never misses.
    answer: 1
    why: The paper says one key can be 20% of a server's requests. Moving it onto a busy server can start a chain of failures.
  - q: Reader A misses, reads v1 from the database, and is slow. Meanwhile writer B saves v2 and deletes the key. With leases, what happens when A tries to set v1?
    options:
      - The cache stores v1, and v2 is lost.
      - The cache rejects the set, because B's delete cancelled A's lease.
      - The cache waits for B to finish, then stores v1.
    answer: 1
    why: A delete cancels any lease on that key. A set with a cancelled lease is refused, so old data does not go back in.
  - q: Every request needs the same 100 small keys, and one server can hold them all but cannot serve the request rate. What helps most?
    options:
      - Split the 100 keys over more servers.
      - Copy all 100 keys to several servers, and send each request to one copy.
      - Switch the eviction policy from LRU to LFU.
    answer: 1
    why: Splitting means each request still visits every server. Copies let each server take a share of the requests.
  - q: You add a near cache in each app server. What must you also add?
    options:
      - A way to drop local copies when the data changes, plus a maximum TTL.
      - A second database.
      - Nothing. Local copies cannot go stale.
    answer: 0
    why: A local copy goes stale when the data changes. You need invalidation messages, and a TTL as a safety net if they are lost.
incidents: [slack-2022-02-cache-cascade]
related: [caching-stampedes, sharding, redis, cdn-http-caching, load-shedding]
---

## What broke

On 22 February 2022, just after 6 a.m. Pacific time, Slack users started to have trouble connecting. Responders found very high load on one part of the database. [@slack-2222]

Slack kept hot data in Memcached, in front of its sharded database. Mcrouter sent each key to a cache node by consistent hashing. A control plane called Mcrib replaced any missing cache node with an empty spare. A node that came back after some time away was flushed. [@slack-2222]

That morning, a routine upgrade restarted the Consul agents on 25% of the servers. Each time a cache node's agent restarted, the node left service discovery. Mcrib swapped in an empty spare, and the cache hit rate dropped. [@slack-2222]

One query made this much worse. When a client started, it loaded the members of every group direct message the user was in. That data was sharded by user, so "even one channel missing from cache" meant a query on every shard. Database load grew "superlinearly" as misses grew. Most of those queries timed out, so the cache could not fill again. [@slack-2222]

Pausing the upgrade early on did not help. Slack had to limit how many clients could start. It also changed the query to read only the missing data. [@slack-2222] Mcrib worked as designed. It was so fast at replacing nodes that the cache emptied faster than it could refill. [@slack-2222]

## The idea in simple words

A distributed cache is many cache servers that act like one big cache. You need one when a single server cannot hold the data your app reads, cannot serve the request rate, or would hurt too much if it died.

This lesson builds on [caching and stampedes](/learn/caching-and-cache-stampedes) and [sharding](/learn/sharding-and-consistent-hashing). Here you add four new questions:

1. **Where does each key live,** and what happens when nodes come and go?
2. **What gets thrown out** when memory is full?
3. **What about one key that is far too busy?**
4. **How do you keep the cache in step with the database?**

> [!key] **The key idea.** Your database is sized for the misses, not for the full traffic. The hit rate is a capacity number. Anything that empties part of the cache (a lost node, a deploy, a new region) sends a sudden wave of load to your database.

## How it works

### Why one node is not enough

In 2013, Facebook ran hundreds of Memcached servers in each cluster, to take load off its databases. One popular page fetched 521 different cached items on average. [@fb-memcache] So one request can touch many cache servers. Once you have many nodes, every app server needs a rule that maps a key to a node.

### Three ways to spread keys

**A library in each client.** The app hashes the key itself, usually with consistent hashing. There is no extra hop. But every client, in every language, must use the same hash and the same server list.

**A proxy.** The app talks to a router, and the router picks the node. Mcrouter is "a memcached protocol router". Its README says it handles "almost 5 billion requests per second at peak". [@mcrouter] Its HashRoute picks a node from the key hash, with `Ch3` as the default hash. [@mcrouter-routes] Twemproxy does the same job for Memcached and Redis. It keeps the number of connections to cache servers low. Its default `ketama` mode is consistent hashing. [@twemproxy]

**Redis Cluster.** The servers own the map. Each key goes to one of 16,384 hash slots, and each primary owns some slots. Clients cache the slot map. If a client asks the wrong node, it gets a `MOVED` reply with the right one. [@redis-cluster] There are no proxies. [@redis-cluster] For details, see [Redis in depth](/learn/redis-in-depth).

### Adding or losing a node

When Redis Cluster moves a slot, it moves the keys to the new node with `MIGRATE`. [@redis-cluster] The data stays warm.

A plain Memcached pool keeps no copies. A new node on the ring takes over about 1/(N + 1) of the keys, and those keys simply miss. A dead or replaced node loses all its keys. With 10 nodes, and keys and traffic spread evenly, losing one costs you about a tenth of your hits. That is what happened to Slack, one node at a time. [@slack-2222]

Facebook chose not to rehash a dead server's keys over the others. One key can be 20% of a server's requests, so the server that takes it over may fall over too. Instead, failed requests go to a small spare pool called Gutter, about 1% of the servers. Its entries expire quickly. [@fb-memcache] This cut client-visible failures by 99%. [@fb-memcache]

### Eviction and sizing

When memory is full, the cache must evict something. The main policies in Redis are: [@redis-eviction]

- **LRU** (`allkeys-lru`) drops the key used least recently. It is the suggested default when some keys are read far more than others.
- **LFU** (`allkeys-lfu`) drops the key used least often. Its counters decay, by default every minute, so old favourites can leave.
- **TTL-based** (`volatile-ttl`) drops keys with the shortest time left to live.

Redis does not track exact LRU. It samples a few keys at random and drops the ones unused the longest. `maxmemory-samples` sets how many. [@redis-eviction]

Size the cache from your working set: the data you read in a day or a week, not all the data you have. Facebook's paper shows the daily and weekly working sets of two families of keys. [@fb-memcache] Then check the result. Hit rate is `keyspace_hits / (keyspace_hits + keyspace_misses)`. If it is low and `evicted_keys` is high, the right keys are being thrown out too often. [@redis-eviction]

### Hot keys

One hot key can get more traffic than its node can serve, however many nodes you add. First, find it. Count requests per key in the client or proxy, on a sample of traffic.

Then pick a fix:

- **A near cache.** Keep the hottest keys in each app server's memory. Redis can help: with tracking on, the server remembers which keys a client read. It sends an invalidation message when one of them changes. If that message can beat a read's reply, store a "caching-in-progress" placeholder first. [@redis-csc] Flush the local cache if that connection drops. Put a maximum TTL on every local key. [@redis-csc]
- **Copies of the key.** Facebook copies some keys to several servers in a pool. In its example, each request needs all 100 keys. Splitting them over two servers still sends every request to both. Copying them halves the load on each. Every write must then delete every copy. [@fb-memcache]
- **Split the key.** For a hot counter, split it into many keys, as in the [sharding lesson](/learn/sharding-and-consistent-hashing).

### Keeping the cache in step with the database

Facebook updates the database, then deletes the key from the cache. It deletes, not updates, because a delete is idempotent. [@fb-memcache] Deleting still leaves a race. Step through it.

[[diagram:cache-lease-stale-set]]

Facebook calls this a "stale set". Its fix is a lease: a 64-bit token the cache gives a client on a miss. The client must show the token to set the value. A delete cancels the token, so a late set with an old value is rejected. [@fb-memcache]

**Across regions.** To reach other clusters, Facebook does not rely on web servers to send deletes. A daemon called mcsqueal reads the SQL each database commits and sends the deletes to the caches. [@fb-memcache] Because deletes live in the database log, lost ones can be replayed. Mcsqueal also runs on the replica databases in other regions. Each one sends a delete only when the change reaches its own database. So a reader there is less likely to put old data back in the cache. [@fb-memcache]

### Cold start and warm-up

A new or rebuilt cache cluster has a very poor hit rate. Facebook lets a cold cluster read misses from a warm cluster, not the database. That brings it back to full capacity "in a few hours instead of a few days". [@fb-memcache] To avoid a race, deletes in the cold cluster block new adds for two seconds. [@fb-memcache] Mcrouter offers this as WarmUpRoute. Gets try the cold node first and fall back to the warm one. [@mcrouter-routes]

If you have no warm copy, let traffic in slowly, as Slack did. [@slack-2222]

## Trade-offs

| Option | Extra hop? | When a node changes | Watch out for |
|---|---|---|---|
| Hashing in each client | No. | Keys on the moved arc miss. | Every client must agree on the hash and the server list. |
| Proxy (mcrouter, twemproxy) | Yes. | Keys on the moved arc miss, unless you add warm-up or copies. | The proxy is one more thing to run and scale. |
| Redis Cluster | No. Clients may get a redirect. | Keys move with their slot. | Multi-key commands need all keys in one slot. |
| Near cache | No network at all. | Not affected. | Stale local copies if invalidations are lost. |
| Copies of hot keys | Depends on the router. | Other copies keep serving. | Every write must delete every copy. |

**Simple rule:** start with a proxy or a cluster client and LRU. Add near caches and copies only for the keys you have measured as hot.

## At work: how a senior uses this

- Ask: **"If we lose one cache node, or a whole cluster, how much extra load hits the database?"** Then test it.
- Make misses cheap. A large part of Slack's problem was a miss that queried every shard. [@slack-2222]
- Limit how fast nodes are replaced or flushed. Fast churn empties the cache faster than misses refill it.
- Chart hit rate, evictions and the top keys per node, next to database load.
- Write down which data may be stale and for how long. That decides between delete, leases and near caches.
- Plan warm-up before a migration, a new region or a full restart.

## With AI

### What to ask the agent

> "Here is our cache-aside code. Find every place that sets a cache value after a database read. Show me the race with a concurrent write, and add a version check."

> "Write a script that replays 1 hour of our key access log against an LRU cache of 4, 8 and 16 GB. Print the hit rate for each size."

> "Add a near cache for these 20 keys with a 5-second maximum TTL, Redis client tracking, and a flush when the tracking connection drops."

### What to check yourself

- Writes delete the cache key **after** the database commit.
- Every local copy has a maximum TTL and a memory limit.
- The replay test uses real, skewed traffic. Uniform fake keys hide hot keys.
- There is a limit on how many clients can start at once, for the day the cache is empty.

## Check yourself

## Go deeper

Before this: [Caching and cache stampedes](/learn/caching-and-cache-stampedes) and [Sharding and consistent hashing](/learn/sharding-and-consistent-hashing). Next: [Redis in depth](/learn/redis-in-depth), [CDN and HTTP caching](/learn/cdn-and-http-caching) and [Load shedding](/learn/load-shedding-graceful-degradation).
