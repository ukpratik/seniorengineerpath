---
topic: cassandra
subtitle: Cassandra makes writes cheap and pays later, in compaction, tombstones and repair. Learn where each cost lands, so deleted data stays deleted and reads stay fast.
status: published
illustrative_story: false
sources:
  - id: uber-ops
    title: "Runtian Liu, Gopal Mor and Jaydeepkumar Chovatia, “How Uber Optimized Cassandra Operations At Scale”, Uber blog (20 July 2023)"
    url: https://www.uber.com/en-US/blog/how-uber-optimized-cassandra-operations-at-scale/
    note: Uber's Cassandra service at tens of thousands of nodes. See the sections on lightweight transaction errors and on building repair into Cassandra after deleted data came back.
  - id: cassandra-storage
    title: "Apache Cassandra documentation: Storage Engine"
    url: https://cassandra.apache.org/doc/latest/cassandra/architecture/storage-engine.html
    note: The commit log and its sync modes, memtable flushes, and the files that make up one SSTable.
  - id: cassandra-bloom
    title: "Apache Cassandra documentation: Bloom Filters"
    url: https://cassandra.apache.org/doc/latest/cassandra/managing/operating/bloom_filters.html
    note: How a read skips SSTables that cannot hold the partition, and the default false-positive chance per compaction strategy.
  - id: cassandra-compaction
    title: "Apache Cassandra documentation: Compaction overview"
    url: https://cassandra.apache.org/doc/latest/cassandra/managing/operating/compaction/overview.html
    note: The four strategies and when the docs suggest each one, plus how gc_grace_seconds and TTLs turn into tombstones.
  - id: cassandra-twcs
    title: "Apache Cassandra documentation: Time Window Compaction Strategy"
    url: https://cassandra.apache.org/doc/latest/cassandra/managing/operating/compaction/twcs.html
    note: Time windows, dropping whole expired SSTables, and the writes that break it.
  - id: cassandra-ucs
    title: "Apache Cassandra documentation: Unified Compaction Strategy (UCS)"
    url: https://cassandra.apache.org/doc/latest/cassandra/managing/operating/compaction/ucs.html
    note: One strategy with a scaling setting that can act tiered (like STCS) or leveled (like LCS).
  - id: cassandra-tombstones
    title: "Apache Cassandra documentation: Tombstones"
    url: https://cassandra.apache.org/doc/latest/cassandra/managing/operating/compaction/tombstones.html
    note: Why a delete is a write, the 10-day grace period, zombies, and when compaction may drop a tombstone.
  - id: cassandra-repair
    title: "Apache Cassandra documentation: Repair"
    url: https://cassandra.apache.org/doc/latest/cassandra/managing/operating/repair.html
    note: Repair is run by you, not by Cassandra. How often to run it so deleted data does not come back.
  - id: cassandra-yaml
    title: "Apache Cassandra, conf/cassandra.yaml (cassandra-5.0 branch, GitHub)"
    url: https://github.com/apache/cassandra/blob/cassandra-5.0/conf/cassandra.yaml
    note: The default config with comments. See num_tokens, the tombstone safety thresholds and paxos_variant.
  - id: cassandra-news
    title: "Apache Cassandra, NEWS.txt (cassandra-5.0 branch, GitHub)"
    url: https://github.com/apache/cassandra/blob/cassandra-5.0/NEWS.txt
    note: Release notes. The 4.0 change of num_tokens from 256 to 16, Paxos v2 in 4.1, UCS in 5.0, and Auto Repair in 5.0.8.
  - id: cassandra-dynamo
    title: "Apache Cassandra documentation: Dynamo"
    url: https://cassandra.apache.org/doc/latest/cassandra/architecture/dynamo.html
    note: The token ring, virtual nodes, and why many tokens per node raise the risk of an outage.
  - id: cassandra-dml
    title: "Apache Cassandra documentation: Data Manipulation"
    url: https://cassandra.apache.org/doc/latest/cassandra/developing/cql/dml.html
    note: INSERT, UPDATE and DELETE, the cost of IF conditions, and counter batches.
  - id: cassandra-types
    title: "Apache Cassandra documentation: Data Types"
    url: https://cassandra.apache.org/doc/latest/cassandra/developing/cql/types.html
    note: See "Counters" for what counters cannot do and why a retried update can over count.
  - id: scylla-compat
    title: "ScyllaDB documentation: ScyllaDB and Apache Cassandra Compatibility"
    url: https://docs.scylladb.com/manual/stable/using-scylla/cassandra-compatibility.html
    note: A feature-by-feature table of what ScyllaDB supports from Cassandra.
claims_checked:
  - claim: "Uber's p99 repair time was 'tens of days'. After Uber moved repair orchestration into Cassandra, with no control plane, it fell to 'just under a single-digit number of days'."
    source: uber-ops
    note: "Repair section of the 20 July 2023 post."
  - claim: "gc_grace_seconds defaults to 864000 seconds (ten days). A node down longer than that can have its deleted data repaired back to the other nodes as a zombie."
    source: cassandra-tombstones
    note: "Sections 'Zombies' and 'Deletes with tombstones'."
  - claim: "With the default 10-day grace period, repairing every node at least once every 7 days stops deleted data from coming back. Cassandra does not run repair by default; Auto Repair (CEP-37) arrived in 5.0.8 and must be enabled."
    source: cassandra-repair
    note: "'Usage and Best Practices'; Auto Repair from NEWS.txt 5.0.8 (cassandra-news)."
  - claim: "cassandra.yaml (5.0) sets tombstone_warn_threshold: 1000 and tombstone_failure_threshold: 100000, because a scan keeps the tombstones it sees in memory."
    source: cassandra-yaml
    note: "SAFETY THRESHOLDS section."
  - claim: "Paxos v1, the default in 5.0, expects 4 round trips for a write; v2, added in 4.1, expects 2."
    source: cassandra-yaml
    note: "paxos_variant comment; 4.1 section of NEWS.txt (cassandra-news)."
  - claim: "Cassandra 4.0 changed the default num_tokens from 256 to 16; many tokens couple many nodes together and raise the risk of unavailability."
    source: cassandra-news
    note: "CASSANDRA-13701 in the 4.0 upgrade notes; Dynamo docs (cassandra-dynamo) explain why."
quiz:
  - q: "You delete a row at QUORUM while one replica is down for two days. Nobody runs repair. Twelve days later, the row appears again. The table uses the default gc_grace_seconds. What happened?"
    options:
      - "Compaction on the other replicas dropped the tombstone after 10 days. Then the down replica's old copy looked like live data and was copied back."
      - "The QUORUM delete was rolled back because one replica failed."
      - "The row's TTL reset itself when the replica came back."
    answer: 0
    why: "A tombstone can be purged after gc_grace_seconds, 10 days by default. A replica that never got it still has the row, and nothing remains to say it was deleted."
  - q: "Your table stores sensor readings with a 30-day TTL. Rows are never updated or deleted by hand. Which compaction strategy do the Cassandra docs design for this?"
    options:
      - "Leveled (LCS)"
      - "Time window (TWCS)"
      - "Size-tiered (STCS)"
    answer: 1
    why: "The docs say TWCS is designed for TTL'ed, mostly immutable time-series data. When a whole window expires, Cassandra can drop the SSTable instead of merging it."
  - q: "A jobs table works like a queue. Workers read the oldest rows in one partition and delete them when done. After a few weeks, reads slow down and logs warn about tombstones. Why?"
    options:
      - "Deleted rows are removed at once, so the partition gets too small."
      - "Each delete leaves a tombstone. A read for the oldest live rows must scan past every tombstone still in the partition."
      - "Queues need the counter type."
    answer: 1
    why: "Deletes are writes. Tombstones stay for at least gc_grace_seconds, and a scan keeps the ones it sees in memory. That is why cassandra.yaml has warn and failure thresholds."
  - q: "A counter update times out. Your code retries it. What can happen?"
    options:
      - "Nothing bad. Counter updates are idempotent."
      - "The counter may count the update twice, because the first try may have been applied."
      - "Cassandra rejects the retry with a duplicate error."
    answer: 1
    why: "The docs say counter updates are not idempotent. After a timeout the client cannot know if the update was applied, so replaying it may over count."
  - q: "Why did Cassandra 4.0 lower the default num_tokens from 256 to 16?"
    options:
      - "Fewer tokens means less memory for Bloom filters."
      - "With a smarter token allocator, a few tokens per node spread data well. Many tokens tie many nodes together, which raises the risk of unavailability."
      - "Because 16 is the largest value the Murmur3 partitioner accepts."
    answer: 1
    why: "The docs say a high token count couples many nodes together. The deterministic allocator added in 3.x lets nodes use far fewer tokens."
incidents: []
glossary_optout: [at-least-once, cell, join]
related: [quorums-leaderless, btree-lsm, nosql-modelling, sharding, raft]
reviewed_by: ai-factcheck
reviewed_on: 2026-11-13
---

## What broke

Uber runs Cassandra as an internal service. In 2023 it had tens of thousands of nodes and tens of millions of queries per second. Clusters ranged from 6 to 450 nodes. [@uber-ops]

Teams that used it reported a strange problem: "data inconsistency due to data resurrection". Data they had deleted came back. Uber's write-up names the usual causes: "Frequent data deletions and downed nodes". [@uber-ops]

The fix was repair, the background job that makes replicas agree. Uber's repairs were slow. Its p99 repair time was "tens of days". [@uber-ops] With Cassandra's default grace period of 10 days, a repair that takes tens of days cannot keep up. [@cassandra-tombstones] [@cassandra-repair] Uber's post does not say which grace period it used.

So Uber made repair part of Cassandra itself, like compaction. It works through the ring node by node and needs no outside control plane. The p99 repair time fell to "just under a single-digit number of days". [@uber-ops]

Some use cases that relied heavily on lightweight transactions saw higher error rates "every other week". The cause was in node replacement. The new node kept looking up the old node's hostname. When that lookup failed, its view of pending ring changes no longer matched the other nodes. [@uber-ops]

None of this is a bug in your queries. It is how Cassandra stores and deletes data. This lesson shows the parts you must run yourself.

## The idea in simple words

You already know two halves of Cassandra. [Quorums and leaderless replication](/learn/quorums-and-leaderless-replication) covers consistency levels, hints, read repair and Merkle-tree repair. [B-tree vs LSM-tree](/learn/btree-vs-lsm-tree) covers the memtable, SSTables and compaction. [Data modelling for NoSQL](/learn/data-modelling-for-nosql) covers partition keys and time buckets.

This lesson joins them. Cassandra never changes a file on disk. So a delete cannot remove data. It writes a marker instead, a tombstone. [@cassandra-tombstones] Every replica must see that marker before any replica throws it away.

> [!key] **The key idea.** In Cassandra, a delete is a write that must reach every replica before its grace period ends. Compaction cleans up, and repair makes sure there is nothing left to bring back. You must run both well.

## How it works

### The write path

A write first goes to a commit log on disk, added at the end. Then it goes into the memtable in memory. [@cassandra-storage] By default the commit log uses "periodic" sync. The write is acknowledged at once, and the log is synced every 10 seconds. A crash can lose up to that period, or more if a sync is late. [@cassandra-storage]

When the memtable is full, Cassandra flushes it to a new SSTable. That file is never written again. So one partition is usually spread over several SSTables. [@cassandra-storage]

### The read path

A read must merge the memtable and every SSTable that may hold the partition. [@cassandra-bloom] Each SSTable has a Bloom filter. It says "surely not here" or "maybe here", so most files are skipped. [@cassandra-bloom] For a file that may have it, a partition index points to the data's place in the file. [@cassandra-storage]

The read keeps the newest value for each cell, by timestamp. A tombstone hides every older value. [@cassandra-tombstones] So a read for live rows must still step over tombstones.

The default false-positive chance is 0.01 for most tables and 0.1 for leveled ones. Going from 0.1 to 0.01 roughly triples the filter's memory. [@cassandra-bloom]

### Tombstones and the grace period

Step through one delete. Use Next and Back, or the ← and → keys.

[[diagram:cassandra-zombie-delete]]

The grace period is the table setting gc_grace_seconds. Its default is 864000 seconds, which is 10 days. [@cassandra-tombstones] After that, compaction may drop the tombstone. It needs every older SSTable for that partition in the same compaction. [@cassandra-tombstones] An expired [TTL](/glossary#ttl) also becomes a tombstone. [@cassandra-compaction]

By default, Cassandra does not run repair for you. [@cassandra-repair] Since 5.0.8 it has a built-in scheduler, Auto Repair, but you must turn it on. [@cassandra-news] With the default grace period, the docs say to repair every node at least once every 7 days. [@cassandra-repair] If you lower `gc_grace_seconds`, repair must run more often. You can also set `only_purge_repaired_tombstones`, so only repaired tombstones are dropped. [@cassandra-tombstones]

### Too many tombstones

A scan keeps the tombstones it sees in memory. That can hurt performance and even exhaust the heap. [@cassandra-yaml] So `cassandra.yaml` sets two limits. By default, `tombstone_warn_threshold` is 1,000 and `tombstone_failure_threshold` is 100,000. A read that scans more than 1,000 tombstones logs a warning. More than 100,000 and the read fails. [@cassandra-yaml]

The classic way to reach them is a queue in a table. Workers read the oldest rows in a partition and delete them. Every read for "the next job" now walks past all recent deletes. The yaml file links to a DataStax post about this anti-pattern. [@cassandra-yaml] Use a real queue or log instead. See [queues vs logs](/learn/queues-vs-logs-delivery-guarantees).

### Large partitions

A partition lives on one set of replicas. A huge one cannot be spread across the cluster. Cassandra 5.0 has guardrails, `partition_size_warn_threshold` and `partition_size_fail_threshold`. They are off by default. [@cassandra-yaml] The fix is in the key, for example a time bucket. The NoSQL modelling lesson shows how.

### Compaction strategies

| Strategy | Best for | Watch out for |
|---|---|---|
| Size-tiered (STCS) | The default, a fallback. Data that is not strict time series, or when LCS costs too much I/O. [@cassandra-compaction] | Reads touch more SSTables. |
| Leveled (LCS) | Read-heavy tables, or many updates and deletes. [@cassandra-compaction] | Not for immutable time series. [@cassandra-compaction] |
| Time window (TWCS) | TTL'ed, mostly immutable time series. [@cassandra-compaction] | Out-of-order writes and `USING TIMESTAMP` mix old and new data. [@cassandra-twcs] |
| Unified (UCS) | Recommended for new workloads. [@cassandra-compaction] | New in 5.0. [@cassandra-news] |

TWCS merges each closed window into one SSTable, then leaves it alone. When everything in it expires, Cassandra can drop the whole file. [@cassandra-twcs] Aim for about 20 to 30 windows. With a 90-day TTL, 3-day windows fit. [@cassandra-twcs]

UCS has one scaling setting. Tiered values act like STCS, leveled values like LCS. Its default, T4, is STCS with a threshold of 4. [@cassandra-ucs]

### Lightweight transactions and counters

`INSERT ... IF NOT EXISTS` and `UPDATE ... IF` use Paxos. The docs warn of "a non-negligible performance cost". [@cassandra-dml] The legacy Paxos (v1), still the default, needs about 4 round trips for a write. Paxos v2 needs about 2. [@cassandra-yaml] Cassandra 4.1 added v2. [@cassandra-news]

```sql
INSERT INTO usernames (name, user_id)
VALUES ('asha', 42)
IF NOT EXISTS;   -- check [applied] in the result
```

Counters are a special column type. Apart from its primary key, a table with a counter can hold only counters. You cannot set a value or give it a TTL. [@cassandra-types] Counter updates are not idempotent. After a timeout, a retry "may or may not lead to an over count". [@cassandra-types]

### The token ring

Each node owns tokens on a hash ring. With [virtual nodes](/learn/sharding-and-consistent-hashing), a node owns many small ranges. [@cassandra-dynamo] Cassandra 4.0 cut the default `num_tokens` from 256 to 16. [@cassandra-news] A smarter token allocator now spreads data well with few tokens. Many tokens tie many nodes together and raise the risk of an outage. [@cassandra-dynamo]

ScyllaDB calls itself "a drop-in replacement for Apache Cassandra 3.11", with some 4.0 features. [@scylla-compat] Check its table for the features you use.

## Trade-offs

| Choice | Good for | Cost |
|---|---|---|
| Lower `gc_grace_seconds` | Yes. Fewer tombstones to scan. | Repair must finish inside the shorter window. Otherwise zombies. [@cassandra-repair] |
| TTL instead of deletes, with TWCS | Yes. Whole files expire. [@cassandra-twcs] | Breaks with out-of-order writes. [@cassandra-twcs] |
| Lightweight transactions | Yes. Check-and-set in one partition. | More round trips than a plain write. [@cassandra-yaml] |
| Counters | Yes. Fast increments. | Retries can over count. [@cassandra-types] |
| Commit log in batch mode | Yes. A write is acknowledged only after the log is synced to disk. [@cassandra-storage] | Each write waits for that sync. |

**Simple rule:** if a table has deletes, schedule repair inside `gc_grace_seconds` before you go live. If a table works like a queue, move it out of Cassandra.

## At work: how a senior uses this

- For each table, write down how rows die: delete, TTL, or never. Pick the compaction strategy from that answer.
- Alert when repair has not finished on every node within the grace period. Missing it is silent until data comes back.
- Watch tombstone warnings in the logs. Each one names a query that scans too much.
- Before a design review, ask, "Does any read step over many deleted rows?" Queues, inboxes and "latest status" tables are the usual suspects.
- Use lightweight transactions only where a race costs money or trust, such as unique usernames. Measure their latency.
- Keep the same `num_tokens` on every node in a data centre. A new node with a different value owns a different share of data. [@cassandra-news]

## With AI

### What to ask the agent

> "Here is our Cassandra schema and every query. For each table, tell me how rows are removed, the compaction strategy you would pick, and why. Flag any table that behaves like a queue."

> "Write a test on a 3-node cluster. Stop one node, delete a row, set gc_grace_seconds to 60, compact the others, start the node and run repair. Show whether the row comes back."

> "Find every counter update and IF condition in this code. For each, say what happens on a timeout and a retry."

### What to check yourself

- Repair is scheduled, finishes, and alerts on failure, at least once per `gc_grace_seconds`.
- The agent's compaction advice matches your Cassandra version. UCS needs 5.0.
- No code retries a counter update blindly.
- Version-specific defaults the agent quotes, like `num_tokens` or `paxos_variant`, match your `cassandra.yaml`.
- A test that "proves" deletes work also includes a node that was down during the delete.

## Check yourself

## Go deeper

Before this: [Quorums and leaderless replication](/learn/quorums-and-leaderless-replication), [B-tree vs LSM-tree](/learn/btree-vs-lsm-tree) and [Data modelling for NoSQL](/learn/data-modelling-for-nosql). Related: [Sharding and consistent hashing](/learn/sharding-and-consistent-hashing) and [Consensus and Raft](/learn/consensus-and-raft), for what Paxos-style agreement costs.
