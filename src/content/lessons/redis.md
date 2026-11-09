---
topic: redis
subtitle: Redis runs your commands one at a time on one thread. Learn what that buys you, what it costs, and what Redis can lose.
status: published
illustrative_story: false
glossary_optout: [main-thread, rollback, atomic, leader, lease]
sources:
  - id: openai-outage
    title: "OpenAI, “March 20 ChatGPT outage: Here’s what happened”, 24 March 2023"
    url: https://openai.com/index/march-20-chatgpt-outage/
    note: OpenAI's own account of the redis-py bug, what users could see, and the fixes.
  - id: redis-py-2624
    title: "redis/redis-py issue #2624, “Off by 1 - Canceling async Redis command leaves connection open, in unsafe state for future commands”"
    url: https://github.com/redis/redis-py/issues/2624
    note: A short script that shows a cancelled command leaving its reply on the connection.
  - id: redis-latency
    title: "Redis documentation, “Diagnosing latency issues”"
    url: https://redis.io/docs/latest/operate/oss_and_stack/management/optimization/latency/
    note: The single-threaded design, slow commands, KEYS, fork and fsync as sources of latency.
  - id: redis-io-threads
    title: "redis/redis pull request #13695, “Async IO Threads”"
    url: https://github.com/redis/redis/pull/13695
    note: How I/O threads read, parse and write while the main thread still runs every command.
  - id: redis-conf
    title: "Redis source, redis.conf (sections “THREADED I/O” and “LAZY FREEING”)"
    url: https://raw.githubusercontent.com/redis/redis/unstable/redis.conf
    note: When to turn on io-threads, and why DEL of a huge key blocks while UNLINK does not.
  - id: redis-tx
    title: "Redis documentation, “Transactions”"
    url: https://redis.io/docs/latest/develop/using-commands/transactions/
    note: What MULTI/EXEC guarantees, why there is no rollback, and WATCH for check-and-set.
  - id: redis-prog
    title: "Redis documentation, “Redis programmability”"
    url: https://redis.io/docs/latest/develop/programmability/
    note: Scripts and functions run atomically and block the server; the five-second busy limit.
  - id: redis-eval
    title: "Redis documentation, “Scripting with Lua”"
    url: https://redis.io/docs/latest/develop/programmability/eval-intro/
    note: EVAL, the KEYS and ARGV arguments, and why every key a script touches must be passed in.
  - id: redis-zset
    title: "Redis documentation, “Redis sorted sets”"
    url: https://redis.io/docs/latest/develop/data-types/sorted-sets/
    note: Sorted sets for leaderboards and sliding-window rate limiters.
  - id: redis-types
    title: "Redis documentation, “Redis data types”"
    url: https://redis.io/docs/latest/develop/data-types/
    note: A one-line summary of every Redis data type, including streams.
  - id: redis-hll
    title: "Redis documentation, “HyperLogLog”"
    url: https://redis.io/docs/latest/develop/data-types/probabilistic/hyperloglogs/
    note: Counting unique items in at most 12 KB, with a 0.81% standard error.
  - id: redis-persistence
    title: "Redis documentation, “Redis persistence”"
    url: https://redis.io/docs/latest/operate/oss_and_stack/management/persistence/
    note: RDB snapshots, the append-only file, the three fsync policies and what each can lose.
  - id: redis-eviction
    title: "Redis documentation, “Key eviction”"
    url: https://redis.io/docs/latest/develop/reference/eviction/
    note: maxmemory, every eviction policy, and the buffers that maxmemory does not count.
  - id: redis-replication
    title: "Redis documentation, “Redis replication”"
    url: https://redis.io/docs/latest/operate/oss_and_stack/management/replication/
    note: Asynchronous replication, WAIT, min-replicas-to-write, and the empty-primary restart trap.
  - id: redis-wait
    title: "Redis documentation, “WAIT”"
    url: https://redis.io/docs/latest/commands/wait/
    note: Why WAIT does not make Redis strongly consistent.
  - id: redis-sentinel
    title: "Redis documentation, “High availability with Redis Sentinel”"
    url: https://redis.io/docs/latest/operate/oss_and_stack/management/sentinel/
    note: What Sentinel does, why you need three of them, and the window for lost writes.
  - id: redis-cluster
    title: "Redis documentation, “Scale with Redis Cluster”"
    url: https://redis.io/docs/latest/operate/oss_and_stack/management/scaling/
    note: Hash slots, hash tags, and two ways Redis Cluster can lose acknowledged writes.
  - id: redis-distlock
    title: "Redis documentation, “Distributed Locks with Redis”"
    url: https://redis.io/docs/latest/develop/clients/patterns/distributed-locks/
    note: The single-node lock, the Redlock algorithm, and the docs' own consistency warnings.
  - id: kleppmann
    title: "Martin Kleppmann, “How to do distributed locking”, 8 February 2016"
    url: https://martin.kleppmann.com/2016/02/08/how-to-do-distributed-locking.html
    note: The critique of Redlock's timing assumptions, and fencing tokens.
  - id: antirez
    title: "Salvatore Sanfilippo (antirez), “Is Redlock safe?”"
    url: http://antirez.com/news/101
    note: The Redis author's reply to Kleppmann, point by point.
claims_checked:
  - claim: "OpenAI took ChatGPT offline on 20 March 2023 because some users could see titles from another active user's chat history; later it found that payment details of up to 1.2% of Plus subscribers active in a nine-hour window may have been visible."
    source: openai-outage
    note: "Read in the user's browser (WebFetch gets 403). Post: \"We took ChatGPT offline ... to see titles from another active user's chat history\"; \"Upon deeper investigation ... 1.2% of the ChatGPT Plus subscribers who were active during a specific nine-hour window\"."
  - claim: "OpenAI cached user data in Redis Cluster through the asyncio redis-py client with a shared connection pool; a request cancelled after it was sent left its reply on the connection, and a server change at 1 a.m. Pacific spiked cancellations."
    source: openai-outage
    note: "Post, Technical details section."
  - claim: "In redis-py asyncio, cancelling a command after it was sent left its reply on the connection, and the next command read it (GET bar returned b'foo')."
    source: redis-py-2624
  - claim: "Redis runs every command on one main thread; I/O threads only read and parse requests and write replies, so a slow command blocks all clients."
    source: redis-io-threads
  - claim: "WAIT does not make Redis strongly consistent; a write acknowledged by replicas can still be lost in a failover."
    source: redis-wait
  - claim: "Redis Cluster has 16,384 hash slots; multi-key commands, transactions and scripts need all keys in one slot, and hash tags make that happen."
    source: redis-cluster
reviewed_by: ai-factcheck
reviewed_on: 2026-11-09
quiz:
  - q: An engineer runs KEYS user:* on a production Redis with 20 million keys. I/O threads are on. What happens to other clients?
    options:
      - Nothing. The I/O threads serve them while KEYS runs.
      - They wait, because one main thread runs every command, and KEYS must finish first.
      - Redis splits KEYS across all CPU cores, so it ends fast.
    answer: 1
    why: I/O threads only read, parse and write. Commands still run one at a time on the main thread, so a slow one blocks everyone.
  - q: "Inside MULTI/EXEC, the second of three commands fails with WRONGTYPE. What happens?"
    options:
      - Redis rolls back the first command.
      - The first and third commands still run. Redis has no rollback.
      - EXEC returns null and nothing runs.
    answer: 1
    why: Errors after EXEC are not handled in a special way. The other commands run. A null reply means a WATCHed key changed.
  - q: Redis uses AOF with the default appendfsync everysec. The machine loses power. What can you lose?
    options:
      - Nothing. Every write is on disk before Redis replies.
      - About the last second of writes.
      - Everything since the last daily backup.
    answer: 1
    why: Redis calls fsync about once a second in a background thread, so up to about one second of acknowledged writes can be lost.
  - q: You call SET, then WAIT 1 100, and WAIT returns 1. Is the write now safe from any failover?
    options:
      - Yes. WAIT makes Redis strongly consistent.
      - No. It is much less likely to be lost, but a failover can still promote a replica without it.
      - No. WAIT only checks that the write reached the disk.
    answer: 1
    why: The docs say WAIT does not make Redis strongly consistent. Failover is best effort, so a synchronously replicated write can still be lost.
  - q: Your job holds a Redis lock with a 30 s TTL, then pauses for 40 s in garbage collection. Another worker takes the lock. What protects the data?
    options:
      - A longer TTL, such as 60 s.
      - A fencing token, or a version check, that the storage verifies on every write.
      - Releasing the lock with plain DEL.
    answer: 1
    why: Any lock that expires can be lost during a pause. Only a check at the storage can reject the late write.
incidents: [openai-2023-03-chatgpt-redis-client]
related: [caching-stampedes, rate-limiting, concurrency-control, concurrency-primitives, batching-pipelining, replication, sharding, leaderboards, distributed-cache, leader-election]
---

## What broke

On 20 March 2023, OpenAI took ChatGPT offline. Some users could see titles from another active user's chat history. [@openai-outage]

Later it found something worse. In the hours before the shutdown, some users could see another active Plus user's name, email address, payment address and the last four digits of their card. OpenAI says this may have affected 1.2% of Plus subscribers active in a nine-hour window. [@openai-outage]

ChatGPT cached user data in Redis Cluster. Its Python servers used the asyncio version of the redis-py client, with a shared pool of connections. [@openai-outage]

If a request was cancelled after it was sent, but before its reply was read, the connection kept that reply. The next, unrelated request on the connection read it instead. Usually this caused an error. Sometimes the data looked valid, so one user got another user's cached data. [@openai-outage] The bug report shows the same thing: after a cancelled `GET foo`, the next `GET bar` returned `foo`'s value. [@redis-py-2624]

At 1 a.m. Pacific time that day, a server change caused a spike in cancelled Redis requests. One fix OpenAI added: check that data from the cache belongs to the user who asked. [@openai-outage]

The bug was in the client library, not in the Redis server. [@openai-outage] To use Redis well, you need to know exactly what it promises, and what it doesn't.

## The idea in simple words

Redis keeps your data in memory and runs **one command at a time**. That one rule explains most of its strengths and its traps.

- **Fast and simple.** Each command runs alone, so a single command is always atomic. `INCR` never loses an update. [@redis-tx]
- **Fragile under slow commands.** If one command takes two seconds, every client waits two seconds.
- **Memory first, disk second.** Data reaches disk later, by your choice of settings. A crash can lose recent writes.
- **Copies are late.** Replicas get writes after the primary replies. A failover can lose writes that clients saw succeed.

> [!key] **The key idea.** Redis is safe for one command and fast for small ones. Ask of every use: how long does each command take, and what happens to my data if this node dies right now?

## How it works

### One thread runs every command

The docs call Redis "*mostly* single threaded". One main thread serves all requests, one after another, using an event loop. [@redis-latency] Normal commands like `GET`, `SET` and `LPUSH` take a tiny, constant time. Commands that touch many elements do not. [@redis-latency]

Step through what one slow command does to everyone else.

[[diagram:redis-slow-command]]

The usual suspects:

- **`KEYS`.** The docs call it "a VERY common source of latency" in production. Use `SCAN`, which walks the keys in small steps. [@redis-latency]
- **Deleting a huge key.** `DEL` frees memory on the main thread. For a value with millions of elements, it "can block for a long time (even seconds)". `UNLINK` frees it in the background instead. [@redis-conf]
- **Long scripts.** A Lua script blocks every other client while it runs. [@redis-prog] More on that below.
- **Forking for persistence.** Saving a snapshot needs a `fork()`. On a big dataset, Redis may stop serving clients for milliseconds, or even a second. [@redis-persistence]

Find them with the slow log (`SLOWLOG GET`). [@redis-latency]

**What about I/O threads?** Since Redis 6.0, I/O threads can read requests, parse them and write replies. The main thread still runs every command. Redis kept this rule on purpose, to avoid races. [@redis-io-threads] They are off by default. The config file suggests them only on machines with 4 or more cores, and only if you have a real performance problem. [@redis-conf] I/O threads help with many busy connections. They do nothing for one slow command.

### Atomic groups: MULTI/EXEC, WATCH and scripts

**`MULTI`/`EXEC`** queues commands and runs them together. No other client's command runs in the middle. [@redis-tx] But there is no rollback. If one command fails at run time, the others still run. [@redis-tx]

In Redis, atomic means nothing else runs in the middle. It does not mean that failed work is undone.

**`WATCH`** adds a check-and-set. If a watched key changes before `EXEC`, the whole transaction aborts and `EXEC` returns null. You retry. [@redis-tx] This is the optimistic locking from the [concurrency control lesson](/learn/locking-and-mvcc-in-practice), in Redis form.

**Lua scripts** can read, decide and write in one atomic step. The docs say a script is usually "both simpler and faster" than a transaction. [@redis-tx] Here a script takes stock only if enough is left:

```lua
-- KEYS[1] = stock key, ARGV[1] = amount
local left = tonumber(redis.call('GET', KEYS[1]) or '0')
if left < tonumber(ARGV[1]) then return -1 end
return redis.call('DECRBY', KEYS[1], ARGV[1])
```

Pass every key name in `KEYS`. Never build key names inside the script. Redis needs this to run scripts correctly, and Redis Cluster needs it most. [@redis-eval] Since Redis 7.0, **functions** let you load scripts into the server as named, first-class code. [@redis-prog]

The price of atomic is blocking. A script past the busy limit (five seconds by default) is not stopped. Other clients get `BUSY` errors. If the script has already written, only `SHUTDOWN NOSAVE` ends it. [@redis-prog] So no loops over big data inside scripts.

### Pick the structure that fits

- **Sorted sets** keep members ordered by score. The docs list leaderboards and sliding-window rate limiters as uses. Adding or updating a member costs O(log N). [@redis-zset]
- **Streams** act like an append-only log, for events in order. [@redis-types] For when to pick a log over a queue, see [queues vs logs](/learn/queues-vs-logs-delivery-guarantees).
- **HyperLogLog** estimates unique counts, like unique visitors, in at most 12 KB, with a 0.81% standard error. [@redis-hll]

### What a crash can lose

Redis has two ways to reach disk. [@redis-persistence]

- An **RDB snapshot** is a point-in-time copy, written by a forked child. You usually save one every few minutes, so be ready to lose "the latest minutes of data".
- The **append-only file (AOF)** logs every write. Its `appendfsync` setting decides how often Redis calls fsync: `always`, `everysec` (the default) or `no`. With `everysec` you may lose 1 second of data. With `no`, Linux usually flushes every 30 seconds. If the disk is slow, `everysec` can lose up to about 2 seconds. [@redis-latency]

The docs suggest both together if you want safety close to PostgreSQL. [@redis-persistence]

### When memory is full

`maxmemory` caps the data size. On 64-bit systems the default is 0, which means no limit. [@redis-eviction] The default policy, `noeviction`, returns an error for writes that would add data when memory is full. Reads still work. [@redis-eviction] For a cache, `allkeys-lru` is a good default, as in [caching stampedes](/learn/caching-and-cache-stampedes). [@redis-eviction] The `volatile-*` policies only evict keys with a TTL, so with no TTLs they act like `noeviction`. Replication and AOF buffers are not counted, so leave some RAM free. [@redis-eviction]

### Copies, failover and shards

Replication is asynchronous. The primary replies to you, then sends the write to replicas. [@redis-replication]

`WAIT n timeout` blocks until n replicas acknowledge your earlier writes, or until the timeout. It returns how many did, so check that number. [@redis-wait] It "does not make Redis a strongly consistent store". A write that reached replicas can still be lost in a failover. [@redis-wait] `min-replicas-to-write`, with `min-replicas-max-lag`, limits the loss window to a few seconds. It does not remove it. [@redis-replication]

**Sentinel** watches a primary and its replicas. It promotes a replica when the primary fails, and tells clients the new address. Run at least three Sentinels. [@redis-sentinel] The docs describe the result as "last failover wins": there is always a window for losing acknowledged writes. [@redis-sentinel]

**Redis Cluster** also shards data. Each key maps to one of 16,384 hash slots: CRC16 of the key, modulo 16384. [@redis-cluster] Multi-key commands, transactions and scripts work only if all keys share a slot. A hash tag forces this: `user:{123}:profile` and `user:{123}:account` land together. [@redis-cluster] Cluster can also lose acknowledged writes, through async replication or a network partition. [@redis-cluster]

### Locks in Redis, and the Redlock debate

The simple lock is one command: `SET lock:job42 <random-value> NX PX 30000`. Release it with a script that deletes the key only if the value is still yours. [@redis-distlock]

On one primary with a replica, a failover can hand the same lock to two clients. The write may not have reached the replica yet. [@redis-distlock] **Redlock** answers this. A client takes the lock on a majority of, say, 5 independent primaries, within the lock's validity time. [@redis-distlock]

Martin Kleppmann argued that Redlock depends on timing assumptions: bounded network delay, process pauses and clock error. A clock jump can let two clients hold the lock. [@kleppmann] His advice: for efficiency, one Redis node is enough. For correctness, use fencing tokens and a consensus system such as ZooKeeper. [@kleppmann] The [locks lesson](/learn/locks-atomics-and-deadlocks) covers the pause problem.

Salvatore Sanfilippo (antirez), who wrote Redis, replied. If your storage can check tokens, he says, "you probably don’t need a distributed lock at all". A unique random token with check-and-set can work too. [@antirez] He pointed out that Redlock checks the time again after it gets the majority. He also said pauses after that point hurt "all the distributed locks implementations". On clocks, he replied that Redlock does not need synchronized clocks. Processes only need to count time at about the same speed. He said ops teams can avoid big clock jumps: don't set clocks by hand, and use an ntpd that spreads changes out. [@antirez] He did agree Redis should move to monotonic time. But he found no other point that broke Redlock's safety, and he rejected the advice to stop using it. [@antirez]

Today the Redis docs point readers to both posts. They tell you to use fencing tokens, and warn that TTLs do not use a monotonic clock. [@redis-distlock]

## Trade-offs

| Persistence setting | What a crash can lose | Cost | Use it when |
|---|---|---|---|
| None | Everything. | Fastest. A restarted empty primary can wipe its replicas. | A pure cache you can rebuild. |
| RDB snapshots only | Minutes of writes. | A fork per save, which can pause Redis. | Losing a few minutes is fine. |
| AOF, `everysec` | About 1 second of writes. | Bigger files, some disk I/O. | The usual choice for data you care about. |
| AOF, `always` | Very little. | "Very very slow." | Rarely. If every write must be on disk, a disk-first database may fit better. |
| RDB + AOF | About 1 second, plus easy backups. | Both costs. | You want safety close to PostgreSQL. |

Sources: [@redis-persistence] and, for the empty-primary trap, [@redis-replication].

## At work: how a senior uses this

- Ask of every Redis use: **"Is this a cache, or the only copy?"** Settings for the two are very different.
- Rename or block `KEYS` and `FLUSHALL` for app users with ACLs. Use `SCAN` and `UNLINK` in tools and scripts.
- Watch the slow log and the latency monitor. A CPU-busy Redis with low traffic usually means slow commands. [@redis-latency]
- Keep values and collections small. A 5-million-member set is a slow command waiting to happen.
- Set `maxmemory` and a policy on purpose. Alert on evictions and on rejected writes.
- Never turn off persistence on a primary that restarts by itself. It comes back empty, and replicas copy the empty set. [@redis-replication]
- For locks, ask: **efficiency or correctness?** For correctness, the database must check a token or a version.
- Treat client connections as stateful. On a cancel or timeout, drop the connection. Don't return it to the pool. [@redis-py-2624]

## With AI

### What to ask the agent

> "Find every Redis call in this repo that is O(N) on a large key or keyspace: KEYS, SMEMBERS, HGETALL, LRANGE 0 -1, DEL on big keys. Suggest SCAN, paging or UNLINK for each."

> "Rewrite this read-then-write Redis code as one Lua script. Pass all key names in KEYS. Add a test with 50 parallel clients."

> "Review our Redis config for a cache: maxmemory, maxmemory-policy, persistence and io-threads. Explain each choice for a 16 GB, 4-core machine."

### What to check yourself

- No script loops over a whole collection. Scripts must stay short.
- Every key the script uses is passed in `KEYS`, and multi-key operations use hash tags in Cluster.
- The code checks for a null `EXEC` and retries.
- Any lock has a TTL, a unique value, and a safe release. Correctness still rests on a fencing check.
- A cancelled or timed-out request closes its connection.
- Nobody assumes `WAIT` makes a write safe from failover.

## Check yourself

## Go deeper

Before this: [Caching stampedes](/learn/caching-and-cache-stampedes), [Rate limiting](/learn/rate-limiting) and [Locking and MVCC in practice](/learn/locking-and-mvcc-in-practice). Related: [Batching and pipelining](/learn/batching-and-pipelining), [Database replication](/learn/database-replication), [Sharding and consistent hashing](/learn/sharding-and-consistent-hashing) and [Consensus and Raft](/learn/consensus-and-raft). Coming later: Leaderboards and live ops, Leader election and leases.
