---
topic: scaling-writes
subtitle: Reads can be copied, but every write must land on the one place that owns its key. Learn to spread writes, split hot keys and smooth bursts, and know what each fix costs.
status: published
reviewed_by: ai-factcheck
reviewed_on: 2026-11-13
illustrative_story: false
glossary_optout: [process]
sources:
  - id: ddb-paper
    title: "Mostafa Elhemali et al., “Amazon DynamoDB: A Scalable, Predictably Performant, and Fully Managed NoSQL Database Service”, USENIX ATC 2022"
    url: https://www.usenix.org/system/files/atc22-elhemali.pdf
    note: Section 4 tells how hot partitions and throughput dilution caused throttling, and how bursting, adaptive capacity, global admission control and split for consumption fixed most of it.
  - id: ddb-partition
    title: "Amazon DynamoDB Developer Guide, “Best practices for designing and using partition keys effectively in DynamoDB”"
    url: https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/bp-partition-key-design.html
    note: The read and write limits of one partition, and what one write unit is.
  - id: ddb-sharding
    title: "Amazon DynamoDB Developer Guide, “Using write sharding to distribute workloads evenly in your DynamoDB table”"
    url: https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/bp-partition-key-sharding.html
    note: Random and calculated suffixes on a partition key, and the extra queries that reads need.
  - id: firestore-counters
    title: "Firebase documentation, “Distributed counters” (Cloud Firestore)"
    url: https://firebase.google.com/docs/firestore/solutions/counters
    note: A counter split into shards, how write throughput and read cost both grow with the number of shards, and the roll-up option.
  - id: mt-hot
    title: "Matt McNierney, “Behind the Scenes: How We Built Ledgers for High Throughput”, Modern Treasury (2024)"
    url: https://www.moderntreasury.com/journal/behind-the-scenes-how-we-built-ledgers-for-high-throughput
    note: A real hot account in a ledger, and how a queue plus batch processing took the pressure off one balance row.
  - id: ddb-gsi
    title: "Amazon DynamoDB Developer Guide: Using Global Secondary Indexes in DynamoDB"
    url: https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/GSI.html
    note: Every table write that changes data an index stores also writes to the index, and an index without enough write capacity can throttle writes to the table.
  - id: uber-pg
    title: "Evan Klitzke, “Why Uber Engineering Switched from Postgres to MySQL”, Uber Engineering (2016)"
    url: https://www.uber.com/blog/postgres-to-mysql-migration/
    note: How, in the Postgres of 2016, a change to one row could mean an update to every index on the table, and more data to replicate.
claims_checked:
  - claim: "In early DynamoDB, a table's throughput was split evenly over its partitions (example: 3,200 WCU under a 1,000 WCU cap gave 4 partitions of 800), so skewed access caused throttling even when the table had enough total capacity."
    source: ddb-paper
    note: "Section 4: \"application workloads frequently have non-uniform access patterns\"; throttling happened \"even though the total provisioned throughput of the table was sufficient\"."
  - claim: "Adaptive capacity removed over 99.99% of throttling caused by skew, and split for consumption does not help a partition whose traffic goes to a single item."
    source: ddb-paper
    note: "\"eliminated over 99.99% of the throttling due to skewed access pattern\"; \"a partition receiving high traffic to a single item ... will not benefit from split\" (4.1.2, 4.4)."
  - claim: "Each DynamoDB partition takes at most 3,000 read units and 1,000 write units a second; one write unit is one write a second for an item up to 1 KB."
    source: ddb-partition
    note: Exact figures on the current best-practices page.
  - claim: "DynamoDB write sharding adds a random suffix 1-200 to a date key; reading the day means querying all 200 keys and merging; a calculated suffix from the order ID allows single-item reads."
    source: ddb-sharding
    note: Example keys 2014-07-09.1 to 2014-07-09.200; calculated suffix from the OrderId.
  - claim: "A table write is throttled if an affected GSI lacks write capacity, and changing an indexed key attribute costs two index writes (delete and put)."
    source: ddb-gsi
    note: "\"Otherwise, the write to the table is throttled.\"; \"two writes are required, one to delete ... and another write to put\"."
  - claim: "Modern Treasury's hot-account fix sends entries that can wait to an SQS queue that a scheduled job processes in batches; the target is 60 s and the p90 is 1 s."
    source: mt-hot
    note: "\"We target processing all Entries within 60s, and in practice, our p90 time to process is 1s.\""
quiz:
  - q: A DynamoDB table has plenty of total write capacity, but writes to one item are throttled. Amazon's split for consumption is on. Why does the split not help?
    options:
      - A split only happens once a day.
      - All writes go to one item, and one item cannot be split across partitions.
      - Splits only help reads, never writes.
    answer: 1
    why: The paper says a partition with high traffic to a single item will not benefit from a split. One key always lives on one partition.
  - q: A like counter gets 20,000 writes a second at peak. Each key takes at most 1,000 writes a second. How many sub-keys should you start with?
    options:
      - About 5, because most likes are spread over the day.
      - At least 20, plus headroom, so about 40.
      - 20,000, one per expected writer.
    answer: 1
    why: 20,000 ÷ 1,000 = 20 is the bare minimum. Random choice is not perfectly even, so add headroom. Many more sub-keys only make reads cost more.
  - q: Each app server adds likes up in memory and writes the total once a second. What is the main new risk?
    options:
      - A server crash loses the likes it counted but has not written yet.
      - The database must handle more writes than before.
      - Reads must now fetch one row per server.
    answer: 0
    why: The pending total lives only in memory until the flush. A crash in between loses it.
  - q: You add three global secondary indexes to a write-heavy DynamoDB table. What can happen to writes on the table?
    options:
      - Nothing. Index updates happen in the background and are free.
      - Each write that changes an attribute the index stores also uses index write capacity, and an index without enough write capacity can throttle the table's writes.
      - Writes get faster, because reads move to the indexes.
    answer: 1
    why: The DynamoDB docs say index updates consume the index's write units, and a table write is throttled if any affected index lacks capacity.
incidents: []
related: [sharding, queues-vs-logs, batching-pipelining, backpressure, btree-lsm, distributed-cache, scaling-reads, design-deep-dives, inventory-flash-sales, money-ledgers]
---

## What broke

Amazon DynamoDB lets you buy write throughput for a table. In its early years, DynamoDB split that throughput evenly over the table's partitions. [@ddb-paper]

The paper's example: one partition can take at most 1,000 write units. A table bought with 3,200 got four partitions of 800 each. [@ddb-paper] This works only if your app writes to all keys evenly.

Real apps do not. Amazon found that workloads "frequently have non-uniform access patterns", over time and over keys. [@ddb-paper] A few busy items sat on one partition. That partition hit its 800, and DynamoDB rejected the extra writes. This is called throttling. It happened even when the whole table had enough capacity. [@ddb-paper]

To customers, it looked like downtime, "even though the service was behaving as expected". Customers worked around it by buying more capacity than they used, just to give the hot partition enough. [@ddb-paper]

Amazon added fixes over time. Partitions could borrow unused capacity for short bursts. Adaptive capacity raised the limit on hot partitions and removed over 99.99% of the throttling caused by skew. Later, busy partitions were split automatically. [@ddb-paper]

But one case was left. A partition "receiving high traffic to a single item" does not gain from a split. [@ddb-paper] Today each partition still takes at most 1,000 write units a second. [@ddb-partition] The database can spread many keys. Only you can spread one key.

## The idea in simple words

Reads are easy to copy. You can add [replicas and caches](/learn/scaling-reads), and each copy serves its own share of reads.

Writes cannot be copied away. Each write must reach the one place that owns its key. Every replica must then apply that write too. So adding replicas does not add write capacity.

When writes are the bottleneck, you have five moves:

1. **A bigger machine.** Simple, and it buys time.
2. **Spread keys over shards,** so each machine owns part of the writes.
3. **Split a hot key** into many sub-keys.
4. **Write less often,** by combining many small writes into one.
5. **Smooth bursts** with a queue, so the database sees a steady rate.

> [!key] **The key idea.** One key lives in one place, so its write rate is capped by that place. To go faster, you must change the key, or send fewer writes to it. Every fix moves cost somewhere else: to reads, to freshness, or to correctness.

## How it works

### First, check that writes are the problem

Look at the primary database, not the replicas. Signs of a write bottleneck are a busy disk, a long write queue, and transactions waiting on row locks. Replicas that lag behind are another sign, because each replica must apply every write.

Then try the cheap fix: a bigger machine with faster disks. It needs no code change. Do this before you shard, as in [Sharding](/learn/sharding-and-consistent-hashing) and [From zero to millions](/learn/scaling-from-zero-to-millions).

### Shard by a key that spreads writes

If one machine is not enough, shard. For writes, the shard key must have many values, and new writes must spread over all of them. A user ID or tenant ID usually works.

A date is a bad write key. All of today's writes go to the shard that owns today. The DynamoDB docs use exactly this example of a hot key. [@ddb-sharding]

### Split a hot key: write sharding

Some keys are hot by nature: a celebrity's post, a flash-sale product, a global counter. No shard key fixes them. So you split the key itself. Step through it. Use Next and Back, or the ← and → keys.

[[diagram:write-sharding-hot-key]]

The DynamoDB docs call this write sharding. Their example adds a random number from 1 to 200 to a date key, such as `2014-07-09.1` to `2014-07-09.200`. Writes spread across partitions. To read the whole day, you query all 200 keys and merge the results. [@ddb-sharding]

If you need to find one item again, use a calculated suffix instead. Compute the suffix from something you know, such as a hash of the order ID. [@ddb-sharding]

For a counter, this is called a sharded counter. Firestore's docs describe a pattern you build yourself: each counter gets a set of shard documents. A write adds 1 to a random shard. A read sums all the shards. [@firestore-counters] Here it is in Python with Postgres:

```python
import random

N = 40  # sub-keys per hot counter

def add_like(conn, post_id):
    # conn: a psycopg 3 connection with autocommit=True
    conn.execute(
        """INSERT INTO like_shards (post_id, shard, n)
           VALUES (%s, %s, 1)
           ON CONFLICT (post_id, shard)
           DO UPDATE SET n = like_shards.n + 1""",
        (post_id, random.randrange(N)))

def like_count(conn, post_id):
    return conn.execute(
        "SELECT COALESCE(SUM(n), 0) FROM like_shards WHERE post_id = %s",
        (post_id,)).fetchone()[0]
```

The table has the primary key `(post_id, shard)`. In one Postgres, the 40 rows let 40 writers hold row locks at once, not one. In a sharded store, if the shard key includes the sub-key, the rows also land on different machines. The read sums whatever rows exist, so you can raise N later without moving data.

Choose N from numbers. Firestore says write throughput grows linearly with the number of shards. So does the cost of each read. [@firestore-counters] For 20,000 writes a second and a limit of 1,000 per key, you need at least 20. Add headroom, and stop there. Only split the keys you have measured as hot.

For busy pages, add a roll-up. A job sums the shards every few seconds and stores one total. Firestore suggests this too, at the price of a total that lags. [@firestore-counters] The [deep dives lesson](/learn/design-deep-dives-trade-offs) walks through this choice for a like counter.

### Write less often: combine, then flush

You can also stop sending every write to the hot row. This part is our advice. Each app server keeps a running total per key in memory. Once a second, it writes the total as one `UPDATE ... SET n = n + 37`. With 50 servers, the row gets at most 50 writes a second, whatever the traffic. The risk: a crash loses the totals not yet written. And a retried flush can add the same total twice. So use it only for counts where a small loss is fine, such as views.

A safer form keeps every event in a queue and applies them in batches. Modern Treasury had a "hot account" in its ledger: a ledger account that was "present on most Transactions". Contention on its single balance cache row made write latency spike. [@mt-hot] Its fix lets a client mark an entry as safe to process later. Those entries go to an AWS SQS queue. A scheduled job takes them from the queue in batches. The target is 60 seconds; in practice, the p90 is 1 second. [@mt-hot]

Modern Treasury still keeps every entry, because all money movement must be recorded immutably. The balance still lives in one cached row. The batch job simply writes to that row far less often than once per entry. Wait too little between batches, and contention comes back. [@mt-hot] [Money and ledgers](/learn/money-and-double-entry-ledgers) shows why ledgers work this way.

### Smooth bursts with a queue

A database has a steady rate it can handle. Traffic arrives in spikes. Put a log or queue in front, and let consumers write at the rate the database can take. During a spike, you get lag, not errors. [Queues vs logs](/learn/queues-vs-logs-delivery-guarantees) covers delivery, and [backpressure](/learn/backpressure-and-flow-control) covers what to do when the queue fills.

A consumer can also write in batches, such as 500 rows per statement. That shares the fixed cost of each round trip and each disk flush. See [batching and group commit](/learn/batching-and-pipelining).

### Each write costs more than one write

Every index on a table is extra work for every write. In DynamoDB, a write that changes an attribute the index stores also uses the index's write units. Changing an indexed key costs two index writes: a delete and a put. If an index lacks capacity, writes to the table itself are throttled. [@ddb-gsi]

Postgres had its own form of this in 2016. Uber wrote that every index pointed to the row's location on disk. When that location changed, all indexes had to be updated. The write-ahead log grew too, so replication carried more data. [@uber-pg] Storage engines add more: an LSM-tree rewrites data during compaction. See [B-trees vs LSM-trees](/learn/btree-vs-lsm-tree). Before you scale writes, remove the indexes nobody uses.

## Trade-offs

| Option | Write gain | Cost on reads | Other costs |
|---|---|---|---|
| Bigger machine | Some. One step, then a ceiling. | None. | Price. Still one machine. |
| Shard by a spread-out key | Yes. Grows with shards. | Queries without the key hit every shard. | Cross-shard transactions get hard. |
| Split a hot key into N sub-keys | Yes. About N times for that key. | N reads, or a roll-up that lags. | A rule like "stock ≥ 0" no longer lives in one row. |
| Combine in memory, then flush | Yes. One write per server per interval. | Count lags by the interval. | A crash loses the pending totals. |
| Queue events, apply in batches | Yes. The shared total row is written once per batch. | Total lags by the batch interval. | A queue and a batch job to run and watch. |
| Queue in front of the database | Smooths peaks only. | Reads lag behind the queue. | The queue needs a limit and an alert. |

**Simple rule:** go bigger first, then shard by a key that spreads writes. Split or combine only the few keys you have measured as hot.

## At work: how a senior uses this

- Ask: **"What is our peak write rate per key, not per table?"** Averages hide the one hot key.
- Find the limit of one key in your store, and divide. The answer tells you if a key needs splitting.
- Log the top keys by write count on each partition. Alert when one key takes a big share.
- Decide how stale each count may be, and get product to agree. It picks between sub-keys, a roll-up and in-memory totals.
- Before splitting stock or balances, check the rules that need one row, such as "never below zero". See [inventory and flash sales](/learn/inventory-overselling-flash-sales).
- Count the indexes on write-heavy tables. Each one needs a reason.
- Load test with real skew: one key with a large share of the writes, not keys spread evenly.

## With AI

### What to ask the agent

> "Here is our schema and a sample of write logs. Find the 10 keys with the most writes per second at peak, and say which ones exceed the per-key limit of our database."

> "Convert this single-row counter to 40 sub-keys with a random suffix. Keep the read API the same, and add a job that stores the total every 5 seconds."

> "List every index on these tables, how often each is used by queries, and how many extra writes each adds per insert."

### What to check yourself

- The sub-key is picked at random, or from a hash, never from a value that is the same for every write.
- The read sums every sub-key that exists, not just 0 to N − 1 of today's N.
- In-memory totals are only used where losing a few seconds of writes is acceptable.
- The queue in front of the database has a limit and an alert on lag.
- The load test sends a large share of writes to one key.

## Check yourself

## Go deeper

Before this: [Sharding and consistent hashing](/learn/sharding-and-consistent-hashing) and [Queues vs logs](/learn/queues-vs-logs-delivery-guarantees). For the read side of hot keys, see [Distributed caches](/learn/distributed-caches). DynamoDB and Cassandra in depth are planned as their own lessons.
