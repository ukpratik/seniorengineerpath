---
topic: quorums-leaderless
subtitle: In a leaderless store, you choose how many copies must answer each read and write. Learn what that number buys you, and the cases where it still returns the wrong data.
status: published
illustrative_story: false
sources:
  - id: monzo-2019
    title: "Monzo, “We had issues with Monzo on 29th July. Here's what happened, and what we did to fix it.”"
    url: https://monzo.com/blog/2019/09/08/why-monzo-wasnt-working-on-july-29th/
    note: A bank's postmortem. Six new Cassandra servers took over data they did not have, and data appeared to be missing.
  - id: dynamo
    title: "Giuseppe DeCandia et al., “Dynamo: Amazon's Highly Available Key-value Store”, SOSP 2007 (full text on All Things Distributed)"
    url: https://www.allthingsdistributed.com/2007/10/amazons_dynamo.html
    note: The paper behind leaderless stores. Sections 4.5 to 4.7 cover R, W and N, sloppy quorums, hinted handoff and Merkle trees. Section 6 gives real (N, R, W) settings.
  - id: cassandra-dynamo
    title: "Apache Cassandra documentation: Dynamo"
    url: https://cassandra.apache.org/doc/stable/cassandra/architecture/dynamo.html
    note: How Cassandra follows and changes Dynamo. See "Tunable Consistency" for every consistency level and "Data Versioning" for last write wins.
  - id: cassandra-hints
    title: "Apache Cassandra documentation: Hints"
    url: https://cassandra.apache.org/doc/stable/cassandra/managing/operating/hints.html
    note: How hinted handoff works in Cassandra, the 3-hour default window, and why hints are only best effort.
  - id: cassandra-read-repair
    title: "Apache Cassandra documentation: Read repair"
    url: https://cassandra.apache.org/doc/stable/cassandra/managing/operating/read_repair.html
    note: Blocking read repair, monotonic quorum reads, and how a failed write can appear and then disappear.
  - id: cassandra-repair
    title: "Apache Cassandra documentation: Repair"
    url: https://cassandra.apache.org/doc/stable/cassandra/managing/operating/repair.html
    note: Full and incremental repair with Merkle trees, and how often to run it so deleted data does not come back.
  - id: scylla-cl
    title: "ScyllaDB documentation: Consistency Levels"
    url: https://docs.scylladb.com/manual/stable/cql/consistency.html
    note: A table of each consistency level with its consistency and availability.
  - id: scylla-ft
    title: "ScyllaDB documentation: ScyllaDB Architecture - Fault Tolerance"
    url: https://docs.scylladb.com/manual/stable/architecture/architecture-fault-tolerance.html
    note: Writes go to every replica; the consistency level only sets when the client hears back. Lower levels answer faster, but fewer replicas are known to hold the write when the client is told it succeeded.
  - id: jepsen-cassandra
    title: "Kyle Kingsbury, “Jepsen: Cassandra” (2013)"
    url: https://aphyr.com/posts/294-call-me-maybe-cassandra
    note: Why last write wins can silently drop a write, and why Cassandra chose it over vector clocks.
claims_checked:
  - claim: "Monzo ran 21 Cassandra servers with 3 replicas and quorum reads and writes. Six new servers with auto_bootstrap false took ownership of data they did not hold. A one-server test missed it because the other two replicas still agreed."
    source: monzo-2019
    note: "'All reads and writes from our services happen with quorum'; new servers 'assumed responsibility for some parts of the data' 'despite not actually holding any data'; with one server 'we'd have agreement from the other two servers'; six gave 'two or three members reallocated to the new nodes'. Majority working at 15:08, all by 23:00."
  - claim: "W + R > N makes read and write sets overlap. The common Dynamo setting is (3,2,2), and a sloppy quorum uses the first N healthy nodes, with hinted handoff."
    source: dynamo
    note: "'The common (N,R,W) configuration used by several instances of Dynamo is (3,2,2)'; 'all read and write operations are performed on the first N healthy nodes from the preference list'."
  - claim: "Writes go to every replica at any consistency level. QUORUM is n/2 + 1, and LOCAL_QUORUM only promises reads see writes from the same data centre."
    source: cassandra-dynamo
    note: "'Write operations are always sent to all replicas, regardless of consistency level'; QUORUM 'A majority (n/2 + 1) of the replicas must respond'; LOCAL_QUORUM is a 'weaker but still useful guarantee'."
  - claim: "Blocking read repair is the default and gives monotonic quorum reads. Without it, a write that failed to reach a quorum may be visible in one read and then disappear."
    source: cassandra-read-repair
    note: "Options are 'blocking (default), and none'; background read repair was removed in 4.0."
  - claim: "Repair is run by the operator. With the default 10-day grace period, repair every node at least once every 7 days, or deleted data can come back."
    source: cassandra-repair
    note: "'it's not run automatically by Cassandra. It is run by the operator via nodetool'; 'repairing every node in your cluster at least once every 7 days will prevent this'."
  - claim: "Early on, Cassandra chose last write wins over vector clocks for performance, because vector clocks typically need a read before each write."
    source: jepsen-cassandra
    note: "Jepsen: Cassandra (2013-09-24): 'Cassandra chose not to implement vector clocks for performance reasons'; 'Vclocks (typically) require a read before each write'."
quiz:
  - q: "A key has N = 3 replicas. You write with W = 1 and read with R = 1. Does every read see the latest acknowledged write?"
    options:
      - "Yes. Every write reaches every replica in the end."
      - "No. 1 + 1 is not more than 3, so the read may ask a replica the write has not reached yet."
      - "Yes, as long as the clocks are in sync."
    answer: 1
    why: "Overlap needs W + R > N. With 1 + 1 = 2, the copy you read and the copy you wrote can be different nodes."
  - q: "A Cassandra write at QUORUM returns a timeout error to your code. What can be true?"
    options:
      - "The write was rolled back on every replica."
      - "It is certain that no replica stored it."
      - "One replica may have stored it, and later reads may return it."
    answer: 2
    why: "Nothing rolls back a partly applied write. Cassandra's docs say such a write can show up in a read, and with non-blocking read repair it can then disappear."
  - q: "Your Cassandra cluster spans two data centres. You read and write at LOCAL_QUORUM. What does the Cassandra documentation promise?"
    options:
      - "Reads see the latest write made from within the same data centre."
      - "Reads see the latest write from any data centre."
      - "Reads are linearizable across the whole cluster."
    answer: 0
    why: "LOCAL_QUORUM is a majority in the local data centre only. The docs call it a weaker but useful guarantee for clients that stay in one data centre."
  - q: "Why do Dynamo and Cassandra use Merkle trees for anti-entropy repair?"
    options:
      - "To pick which write wins a conflict."
      - "To find the ranges that differ by comparing hashes, so replicas copy only those ranges."
      - "To decide which node is the leader."
    answer: 1
    why: "Equal roots mean equal data. If roots differ, the nodes walk down only the branches that differ, which cuts the data sent and the disk reads."
  - q: "Monzo tested adding one Cassandra server and saw no problem. Adding six broke reads. Why?"
    options:
      - "Six servers made the cluster too slow."
      - "With one new empty server, the other two replicas of each key still formed a quorum. With six, some keys had two or three empty replicas."
      - "The test cluster used a different consistency level."
    answer: 1
    why: "Monzo's postmortem says it failed to account for quorum. With one empty replica, every quorum of two still includes a replica that has the data. With two empty replicas, a quorum can be both empty ones."
incidents: [monzo-2019-07-cassandra]
cases: [amazon-dynamo]
glossary_optout: [at-least-once, deadline, offset, ownership, transaction]
related: [replication, cap-consistency, clocks-ordering, sharding, raft, cassandra, crdts]
reviewed_by: ai-factcheck
reviewed_on: 2026-11-10
---

## What broke

On 29 July 2019, at about 13:10, some Monzo customers could not log in. Some could not send or receive payments. Some saw wrong balances. [@monzo-2019]

Monzo kept its data in one Cassandra cluster of 21 servers. Every piece of data had 3 copies. Every read and write used quorum, so at least 2 of the 3 copies had to answer. [@monzo-2019]

That day, the team added 6 servers for more capacity. A setting, `auto_bootstrap`, was false. The team believed the new servers would wait, inactive, until data was copied to them. Instead, they took ownership of some keys at once and started answering queries. They held none of the data. [@monzo-2019]

For some keys, two or three of the three copies now lived on empty servers. A quorum read could then get both of its answers from empty servers. To the app, the data looked missing. [@monzo-2019]

Monzo had tested the change, but with one new server, not six. With one empty server, the other two copies still formed a quorum. In their words, "What we failed to account for was quorum". [@monzo-2019] Most customers were working again by 15:08, after the team removed the new servers one by one. All were fixed by 23:00. [@monzo-2019]

## The idea in simple words

In [replication](/learn/database-replication) you met the leader model, and leaderless replication in one paragraph. Here is the full picture.

In a leaderless store, there is no single node that orders writes. Each key lives on N nodes. A coordinator node sends every write to all N copies and waits for W of them to say yes. A read waits for R answers. Cassandra keeps the one with the newest timestamp. Dynamo can return several versions (more below). [@dynamo] [@cassandra-dynamo]

If W + R > N, the copies you read and the copies you wrote must share at least one node. So the read sees the write. [@dynamo] [@cassandra-dynamo] Amazon's common setting was N = 3, R = 2, W = 2. [@dynamo]

> [!key] **The key idea.** W + R > N only says the two sets of nodes overlap. It does not say those nodes hold the right data, or that everyone agrees on what "newest" means. A quorum is a counting rule, not a promise of [linearizability](/learn/cap-pacelc-consistency-models).

## How it works

Step through a write, a read, and four ways a quorum still goes wrong. Use Next and Back, or the ← and → keys.

[[diagram:quorum-overlap-gaps]]

### Why not a strict quorum?

Dynamo was built so that a shopping cart could always take writes. [@dynamo] A strict quorum would fail writes when replicas are down or cut off. So Dynamo uses a **sloppy quorum**. It writes to the first N healthy nodes it can reach, even if they are not the key's usual home. [@dynamo]

A stand-in node keeps the copy with a hint that names the real owner. When the owner comes back, the stand-in sends the copy over. This is **hinted handoff**. [@dynamo] The cost is clear from the diagram: a reader that reaches only the usual owners can miss the write.

Cassandra also stores hints for a replica that is down, for up to 3 hours by default. Its docs say hints are best effort, not a guarantee. [@cassandra-hints] Its weakest write level, ANY, accepts a stored hint in place of a replica. At every other level, only real replicas count toward the quorum. Hints just help a replica catch up later. [@cassandra-dynamo]

### Where else quorums leak

- **A failed write may still be there.** A write that reaches only one replica returns an error, but nothing rolls it back. Cassandra's docs say such a write "may be visible in one read, and then disappear" if read repair is off. [@cassandra-read-repair]
- **Concurrent writes.** Two clients write the same key at once. Each write reaches a quorum. Someone must now pick a winner, and that rule decides what you lose.
- **Clocks.** Cassandra picks the winner by timestamp: last write wins. The timestamp comes from a client clock or the coordinator's clock, so correctness depends on synced clocks. [@cassandra-dynamo] [Time, clocks and ordering](/learn/time-clocks-and-ordering) shows how skew lets an older write win.
- **Membership changes.** At Monzo, some keys had two of their three replicas on new, empty servers. The count was right, but the data looked missing. [@monzo-2019]

### Read repair

When a read finds that replicas disagree, the coordinator writes the newest value back to the stale ones. Dynamo calls this read repair. [@dynamo] Cassandra does it in the foreground by default. The client waits until the repair is done. [@cassandra-read-repair]

The timing matters. Blocking repair gives "monotonic quorum reads": a second quorum read will not return something older than the first. [@cassandra-read-repair] But read repair only fixes keys that someone reads. Cold keys stay out of sync.

### Anti-entropy with Merkle trees

For everything else, replicas compare their data in the background. This is anti-entropy repair. Comparing every key would be slow, so Dynamo uses Merkle trees. [@dynamo]

A Merkle tree is a tree of hashes. Each leaf is the hash of one key's value. Each parent is the hash of its children. Two nodes swap the root hashes for a key range. If the roots match, the range is in sync. If not, they compare children and walk down only the branches that differ. [@dynamo]

Cassandra does not run repair for you. You run `nodetool repair`. [@cassandra-repair] Its docs suggest incremental repair every 1–3 days and a full repair every 1–3 weeks as a starting point. [@cassandra-repair] Deletes add a hard deadline. With the default 10-day grace period, repair every node at least once every 7 days. Otherwise deleted data can come back. [@cassandra-repair]

### Tunable consistency in Cassandra

Cassandra turns R and W into named consistency levels that you set per query. [@cassandra-dynamo] With a replication factor of 3:

```sql
-- in cqlsh
CONSISTENCY QUORUM;   -- 2 of 3 replicas must answer
SELECT balance FROM accounts WHERE id = 42;
```

- **ONE**: one replica answers. Fastest, and it works if any single replica is up. [@cassandra-dynamo]
- **QUORUM**: a majority of all replicas, n/2 + 1. QUORUM for both reads and writes gives overlap. [@cassandra-dynamo]
- **LOCAL_QUORUM**: a majority in the coordinator's data centre. Reads see the latest write made from the same data centre. [@cassandra-dynamo]
- **ALL**: every replica must answer. ScyllaDB rates it highest in consistency and lowest in availability. [@scylla-cl]

The write is sent to every replica at any level. The level only sets how many answers the coordinator waits for. [@cassandra-dynamo] [@scylla-ft] Waiting for more answers means waiting for a slower node. Dynamo's paper says latency is set by "the slowest of the R (or W) replicas". [@dynamo] Lower levels are faster and keep working with more nodes down. But fewer copies have confirmed the write when you hear yes, so you get weaker consistency and durability. [@scylla-ft]

### Last write wins or versions?

Last write wins keeps one value and drops the others, with no error. If two clients each read a value, change it and write it back, the write with the lower timestamp is lost. Jepsen showed acknowledged writes being lost this way. [@jepsen-cassandra] Early on, Cassandra chose this for speed. Vector clocks usually need a read before each write. [@jepsen-cassandra]

Dynamo kept vector clocks instead. When versions are concurrent, it returns all of them and the app merges. A cart then never loses an added item, but deleted items can come back. [@dynamo] In practice this was rare. Over 24 hours, 99.94% of requests to the cart service saw exactly one version. [@dynamo]

## Trade-offs

With N = 3 replicas. The failure counts are our arithmetic.

| Choice | Survives replicas down | Latency | What you get |
|---|---|---|---|
| W = 1, R = 1 (ONE/ONE) | Yes. Up to 2. | Lowest. One answer each. | No overlap. Reads can be stale. |
| W = 2, R = 2 (QUORUM/QUORUM) | Yes. 1. | Waits for the slower of two replicas. | Overlap. Still not linearizable. |
| W = 3, R = 1 (ALL/ONE) | No for writes. A single node down blocks writes. | Writes wait for the slowest replica. | Overlap with fast reads. |
| LOCAL_QUORUM, two data centres | Yes. A whole remote data centre. | No cross-region wait. | Overlap inside one data centre only. [@cassandra-dynamo] |
| Sloppy quorum | Yes. Writes rarely fail. [@dynamo] | Low. | Reads can miss writes held by stand-in nodes. |

**Simple rule:** start with QUORUM or LOCAL_QUORUM for both reads and writes. Drop to ONE only for data you can afford to see stale. For a true check-and-set, use something built for consensus, like [Raft](/learn/consensus-and-raft).

## At work: how a senior uses this

- Write down N, W and R for every table, and the reason. Check what your driver actually sends. Defaults differ; cqlsh uses ONE. [@scylla-ft]
- Treat a timeout as "unknown", not "failed". Make writes [idempotent](/glossary#idempotent) so a retry is safe.
- Ask, "What happens if two clients write this key at once?" If the answer is "one is lost", use a version check, a merge, or a new key per change.
- Check that repair runs, and finishes, inside the tombstone grace period. Alert when it does not. [@cassandra-repair]
- Treat adding or removing nodes as a risky change. Rehearse it at production scale, and watch "row not found" rates while it runs. After its outage, Monzo planned alerts on sharp shifts in "row not found". [@monzo-2019]
- Monitor clock offset on every node. Last write wins trusts those clocks. [@cassandra-dynamo]

## With AI

### What to ask the agent

> "Here is our Cassandra data access code. For each query, list the consistency level it really uses, including driver defaults. Flag any read and write pair where W + R is not more than the replication factor."

> "Write an integration test with a 3-node Cassandra cluster. Stop one node, write at QUORUM, start the node, and check that a QUORUM read returns the new value."

> "This code does read-modify-write on a Cassandra column. Show me how concurrent requests can lose an update, and rewrite it to use a lightweight transaction or a new row per change."

### What to check yourself

- The consistency levels the agent quotes match your Cassandra or ScyllaDB version's docs, and your multi-region layout.
- Retries after a timeout are safe to repeat. A timed-out write may already be stored. [@cassandra-read-repair]
- A test that "proves" consistency also covers a stopped node, a slow node and two concurrent writers. One happy run proves nothing.
- Repair is scheduled, and someone gets alerted if it fails.
- Any claim that quorums make reads linearizable is removed. They do not.

## Check yourself

## Go deeper

Before this: [Replication](/learn/database-replication) and [Sharding and consistent hashing](/learn/sharding-and-consistent-hashing), which explains the ring that decides where each key lives. Related: [CAP, PACELC and consistency models](/learn/cap-pacelc-consistency-models) and [Time, clocks and ordering](/learn/time-clocks-and-ordering). Next: [Cassandra in depth](/learn/cassandra-in-depth), and CRDTs and coordination avoidance.
