---
topic: coordination-services
subtitle: ZooKeeper and etcd keep a small amount of critical data that every node must agree on. Learn what they give you, where they break, and what not to put in them.
status: published
illustrative_story: false
sources:
  - id: roblox-2021
    title: "Roblox, “Roblox Return to Service 10/28-10/31 2021”"
    url: https://about.roblox.com/newsroom/2022/01/roblox-return-to-service-10-28-10-31-2021/
    note: The official postmortem. One Consul cluster held discovery, health checks, locks and KV data, and its failure took Roblox down for 73 hours.
  - id: zk-overview
    title: "Apache ZooKeeper documentation, “ZooKeeper: A Distributed Coordination Service for Distributed Applications”"
    url: https://zookeeper.apache.org/doc/current/zookeeperOver.html
    note: What ZooKeeper is for, why reads are local and writes go through the leader, and the 10:1 read-to-write sweet spot.
  - id: zk-programmers
    title: "Apache ZooKeeper documentation, “ZooKeeper Programmer's Guide”"
    url: https://zookeeper.apache.org/doc/current/zookeeperProgrammers.html
    note: Znodes, ephemeral and sequential nodes, one-time watches, sessions, the 1 MB limit, and why a client may need sync().
  - id: zk-internals
    title: "Apache ZooKeeper documentation, “ZooKeeper Internals”"
    url: https://zookeeper.apache.org/doc/current/zookeeperInternals.html
    note: The atomic broadcast protocol, zxids, and the exact consistency model. Writes are linearizable; reads are not.
  - id: zk-admin
    title: "Apache ZooKeeper documentation, “ZooKeeper Administrator's Guide”"
    url: https://zookeeper.apache.org/doc/current/zookeeperAdmin.html
    note: Running a ZooKeeper ensemble. Names the server-to-server protocol ZAB, and explains why the transaction log needs its own fast disk.
  - id: zk-recipes
    title: "Apache ZooKeeper documentation, “ZooKeeper Recipes and Solutions”"
    url: https://zookeeper.apache.org/doc/current/recipes.html
    note: Locks and leader election built from sequential nodes, and how to avoid the herd effect.
  - id: etcd-api
    title: "etcd documentation (v3.5): etcd API"
    url: https://etcd.io/docs/v3.5/learning/api/
    note: Revisions, key fields, linearizable and serializable reads, Txn, Watch from a revision, and leases.
  - id: etcd-guarantees
    title: "etcd documentation (v3.5): etcd API guarantees"
    url: https://etcd.io/docs/v3.5/learning/api_guarantees/
    note: Linearizable by default, the serializable option, and the watch guarantees.
  - id: etcd-limits
    title: "etcd documentation (v3.5): System limits"
    url: https://etcd.io/docs/v3.5/dev-guide/limit/
    note: The 1.5 MiB request limit, the 2 GiB default storage limit and the 8 GiB suggested maximum.
  - id: etcd-maintenance
    title: "etcd documentation (v3.5): Maintenance"
    url: https://etcd.io/docs/v3.5/op-guide/maintenance/
    note: History compaction, defragmentation, and what happens when the space quota is full.
  - id: etcd-hardware
    title: "etcd documentation (v3.5): Hardware recommendations"
    url: https://etcd.io/docs/v3.5/op-guide/hardware/
    note: Why the disk is the most critical part of an etcd deployment.
  - id: etcd-grpc-proxy
    title: "etcd documentation (v3.5): gRPC proxy"
    url: https://etcd.io/docs/v3.5/op-guide/grpc_proxy/
    note: A proxy that merges many watchers on the same key into one, to protect the cluster.
  - id: kip-500
    title: "Apache Kafka, KIP-500: Replace ZooKeeper with a Self-Managed Metadata Quorum"
    url: https://cwiki.apache.org/confluence/display/KAFKA/KIP-500%3A+Replace+ZooKeeper+with+a+Self-Managed+Metadata+Quorum
    note: Why Kafka moved its metadata out of ZooKeeper and into its own log.
  - id: kafka-4
    title: "Apache Kafka, “Apache Kafka 4.0.0 Release Announcement” (March 2025)"
    url: https://kafka.apache.org/blog/2025/03/18/apache-kafka-4.0.0-release-announcement/
    note: The first major Kafka release that runs entirely without ZooKeeper.
  - id: pg-locks
    title: "PostgreSQL documentation: 13.3. Explicit Locking"
    url: https://www.postgresql.org/docs/current/explicit-locking.html
    note: Row-level locks with SELECT FOR UPDATE, and advisory locks held per session or per transaction.
claims_checked:
  - claim: "etcd rejects any request over 1.5 MiB by default; the default storage limit is 2 GiB and 8 GiB is the suggested maximum."
    source: etcd-limits
    note: "The v3.5 System limits page states all three values, in binary units."
  - claim: "ZooKeeper writes are linearizable but reads are not: a server answers reads from its local copy, and sync() is not a quorum operation."
    source: zk-internals
    note: "Consistency Guarantees section. The Programmer's Guide gives the client A / client B stale-read example and the sync() advice."
  - claim: "A standard ZooKeeper watch is a one-time trigger, so a client cannot reliably see every change; persistent watches arrived in 3.6.0."
    source: zk-programmers
    note: "Persistent watches set with addWatch() are not removed when triggered."
  - claim: "When the etcd space quota is exceeded, a cluster-wide alarm puts the cluster in a maintenance mode that only accepts key reads and deletes."
    source: etcd-maintenance
    note: "Recovery is compact, defragment, then etcdctl alarm disarm. Defragmenting a live member blocks its reads and writes."
  - claim: "Roblox's 73-hour outage came from Consul streaming under very high read and write load and a BoltDB slow path; a 4.2 GB log store held 489 MB of data."
    source: roblox-2021
    note: "Consul served discovery, health checks, session locking and KV. Some critical services were later split into dedicated clusters."
  - claim: "Kafka 4.0 (March 2025) is the first major release to run entirely without ZooKeeper."
    source: kafka-4
    note: "Release announcement of 18 March 2025; KRaft mode is the default."
quiz:
  - q: A ZooKeeper client sets a standard watch on /config. The node changes three times quickly. What does the client learn?
    options:
      - It gets three events, one per change, each with the new data.
      - It may get only one event. When it reads again, it sees the latest value and may have missed the changes in between.
      - It gets no event, because watches only fire when a node is deleted.
    answer: 1
    why: A standard watch is a one-time trigger. Between the event and the next read with a new watch, more changes can happen unseen.
  - q: Client A writes /flag in ZooKeeper, then tells client B to read it. B is connected to a different server and reads the old value. Is this a bug?
    options:
      - Yes. ZooKeeper writes are linearizable, so every read must see them.
      - No. Reads are served by the local server and can be stale. B should call sync() before reading.
      - No. ZooKeeper only promises that writes are stored on one server.
    answer: 1
    why: Writes are linearizable, but reads are answered locally without a quorum. The Programmer's Guide gives exactly this case.
  - q: Your team wants to store 3 MB build artifacts in etcd "because it is already there". What happens?
    options:
      - It works, but each write is slower.
      - It is rejected. By default, etcd refuses any request larger than 1.5 MiB.
      - etcd splits the value across members automatically.
    answer: 1
    why: The default request size limit is 1.5 MiB. Even smaller blobs would fill the 2 GiB default storage limit quickly.
  - q: An etcd cluster raises the space quota alarm. What still works until you fix it?
    options:
      - Only key reads and deletes.
      - Everything. The alarm is only a warning.
      - Nothing. All members shut down.
    answer: 0
    why: The cluster goes into maintenance mode. You compact, defragment and disarm the alarm to accept writes again.
  - q: Two app servers must not process the same order at the same time. The orders live in Postgres. What is the simplest safe choice?
    options:
      - Add an etcd cluster and take a lock per order.
      - Use SELECT ... FOR UPDATE on the order row inside the transaction that changes it.
      - Store a "locked" flag in a local file on each server.
    answer: 1
    why: The lock lives in the same database and transaction as the data, so there is no second system to fail or to fall out of step.
incidents: [roblox-2021-10-consul]
glossary_optout: [compaction, serializable, quota, keep-alive]
reviewed_by: ai-factcheck
reviewed_on: 2026-11-13
related: [raft, leader-election, failure-detection, dns-discovery, kafka-internals, cap-consistency, postgres]
---

## What broke

On 28 October 2021, Roblox engineers saw that Vault was slow and one Consul server had high CPU load. Players were still fine. [@roblox-2021]

Roblox runs over 18,000 servers. Consul gave them service discovery, health checks, session locking and a key-value store. Nomad and Vault both depended on it. [@roblox-2021] So when Consul was sick, services could not find each other. No new containers could start, and no secrets could be read. Roblox wrote: "the system failed because Consul was a single point of failure, and Consul was not healthy." [@roblox-2021]

Writes to Consul's key-value store usually took under 300 ms at the median. Now they took 2 seconds. [@roblox-2021] The team replaced hardware. Later they shut down the whole cluster and restored a snapshot from before the outage. Neither fixed it. Latency stayed at about 2 seconds, or came back to it once load returned. [@roblox-2021]

There were two causes. A new Consul streaming feature caused contention under very high read and write load. And a usage pattern that arose during the incident hit a slow path in BoltDB, the store under Consul's Raft log. One log store was 4.2 GB but held only 489 MB of real data. [@roblox-2021]

The outage lasted 73 hours. [@roblox-2021] Monitoring also depended on Consul, so the team was partly blind while they debugged. Afterwards they split some critical services into their own Consul clusters. They also began moving data out of the key-value store. Some services had used it "as a convenient place to store data". [@roblox-2021]

## The idea in simple words

You met consensus in [Consensus and Raft](/learn/consensus-and-raft). Few teams run Raft themselves. They use a coordination service such as ZooKeeper, etcd or Consul, which runs consensus for you and offers a small, strongly consistent store.

ZooKeeper lists what this store is for: configuration, naming, synchronization and group membership. [@zk-overview] In practice, that means:

- **Config** that every node must see the same way.
- **Membership:** which workers are alive right now.
- **Service discovery:** where each service runs.
- **Locks and leader election.** [Leader election and leases](/learn/leader-election-and-leases) covers leases, fencing tokens and ephemeral nodes in depth. This lesson does not repeat them.

It is not a database. ZooKeeper "was not designed to be a general database or large object store". Its data is usually measured in kilobytes. [@zk-programmers] It works best when reads outnumber writes by about 10 to 1. [@zk-overview]

> [!key] **The key idea.** A coordination service holds the few facts your whole fleet must agree on. Keep that data small and slow-changing. Every extra use you add is one more thing that breaks when it breaks.

## How it works

### ZooKeeper: a tree of znodes

ZooKeeper stores data in a tree of paths, like a file system. Each node in the tree is a znode. A znode holds data and can have children. Reads and writes of a znode's data are atomic. [@zk-programmers]

Two flags make it useful for coordination. An ephemeral znode lives only as long as the session that created it. A sequential znode gets a 10-digit counter added to its name, such as `lock-0000000001`. [@zk-programmers] Locks and elections combine both. [@zk-recipes]

A session is the client's link to the cluster. If the cluster hears nothing from the client within the session timeout, it expires the session and deletes its ephemeral znodes. [@zk-programmers]

All writes go to one leader. The servers agree on them with an atomic broadcast protocol. [@zk-overview] [@zk-internals] The admin guide calls it ZAB. [@zk-admin] A write is committed once a quorum of servers acknowledges it. Each change gets a zxid, a number that shows its place in the total order. [@zk-internals]

Writes are linearizable. Reads are not. Any server answers a read from its local copy, without asking a quorum. [@zk-internals] So client A can write a value, tell client B, and B can still read the old one. The guide says B should call `sync()` first. [@zk-programmers] Even `sync()` is not a strict guarantee, because it is not a quorum operation. [@zk-internals]

### Standard watches fire once

A client can set a watch when it reads. A standard watch is a one-time trigger. After it fires, you must set a new one. Between the event and your next read, the znode can change again. So you cannot reliably see every change. [@zk-programmers] ZooKeeper 3.6 added persistent watches that stay after they fire. [@zk-programmers]

Step through how this differs from etcd.

[[diagram:zk-etcd-watch-gap]]

### etcd: flat keys and revisions

etcd has no tree. Keys sit in one flat, sorted key space. You read ranges of keys, so a prefix like `/services/` works like a folder. [@etcd-api]

etcd keeps old versions of every key, which is MVCC. A store revision counts every change to the whole key space. Each key records the revision that created it, the revision that last changed it, and a version count. [@etcd-api] The revision only goes up. [@etcd-api]

Revisions make watches stronger. You can watch a key or range from any revision still in history. Events arrive ordered by revision, and none are dropped inside the history window. A broken watch can resume just after the last revision you saw, if that revision is still in history. [@etcd-guarantees]

A lease has a TTL and is kept alive by keepalives. When it expires, every key attached to it is deleted. [@etcd-api] This plays the role of ZooKeeper's ephemeral znodes.

A Txn is an atomic if/then/else. If every compare is true, the success operations run; otherwise the failure ones do. [@etcd-api] This is compare-and-swap across keys. Here is "create the key only if nobody has":

```go
key := "/jobs/nightly/owner"
resp, err := cli.Txn(ctx).
    If(clientv3.Compare(clientv3.CreateRevision(key), "=", 0)).
    Then(clientv3.OpPut(key, "worker-7", clientv3.WithLease(leaseID))).
    Commit()
// resp.Succeeded is false if another worker created it first.
```

Reads are linearizable by default. You can ask for a serializable read instead. One member answers it from its local copy, which is faster but may be stale. [@etcd-api] [@etcd-guarantees]

### Limits and failure modes

- **Small values only.** etcd rejects any request over 1.5 MiB by default. ZooKeeper checks that a znode holds less than 1 MB. [@etcd-limits] [@zk-programmers]
- **The size quota.** etcd's default storage limit is 2 GiB, and 8 GiB is the suggested maximum. [@etcd-limits] Past the quota, the cluster raises an alarm and accepts only reads and deletes. [@etcd-maintenance]
- **History grows.** Old revisions stay until you compact them. Compaction leaves gaps inside the database file, so deleting data does not shrink it on disk. Defragmentation gives the space back, but it blocks reads and writes on that member while it runs. [@etcd-maintenance] Roblox's BoltDB log store shows how far a file can drift from its data: 4.2 GB on disk for 489 MB of data. [@roblox-2021]
- **Slow disks.** A majority must write every request to disk. If those writes take too long, heartbeats can time out and start an election. [@etcd-hardware] ZooKeeper also syncs each write to its log on disk before it answers, and wants a dedicated log device. [@zk-admin]
- **The herd effect.** If many clients watch the same znode, one change wakes all of them, but only one can act. The recipes avoid this by having each client watch only the node just before its own. [@zk-recipes] etcd's gRPC proxy merges many watchers of one key into a single watcher on the server. [@etcd-grpc-proxy]

### Why Kafka left ZooKeeper

Kafka used to keep its cluster metadata in ZooKeeper. KIP-500 listed the costs. Operators had to run a second system with its own config and tools. After an election, a new controller had to load all state from ZooKeeper, which took a long time. KIP-500 also wanted Kafka to support more partitions. [@kip-500] The fix stores metadata as a log inside Kafka. [@kip-500] Kafka calls this KRaft mode. Kafka 4.0, from March 2025, is the first major release that runs entirely without ZooKeeper. [@kafka-4]

## Trade-offs

| Option | Strong consistency? | Extra system to run? | Use it when |
|---|---|---|---|
| Row lock in your database (`SELECT ... FOR UPDATE`) | Yes, inside one database. [@pg-locks] | No. | The data you protect lives in that database. |
| Postgres advisory lock | Yes, but the app must use it correctly. [@pg-locks] | No. | Many processes share one Postgres and need a named lock. |
| ZooKeeper | Writes yes; reads can be stale. [@zk-internals] | Yes. | You already run it, or a tool you use needs it. |
| etcd | Yes by default; serializable reads are optional. [@etcd-guarantees] | Yes. | You need watches that can resume from a past revision. |
| Your own Raft inside the product | Yes. | No, but you own the code. | The metadata is core to your product, like KRaft. [@kip-500] |

**Simple rule:** if the thing you protect lives in one database, lock it there. Reach for a coordination service when many systems must agree and there is no single database between them.

## At work: how a senior uses this

- Ask of every key: **"Is this coordination data, or just data?"** If it is large, changes often, or only one service reads it, move it to a database.
- Count who depends on the cluster. Split critical users into their own clusters before an outage forces you to, as Roblox did afterwards. [@roblox-2021]
- Keep monitoring off the cluster it watches.
- Alert on etcd database size against the quota, and on leader changes. Set up auto-compaction and plan defragmentation one member at a time. [@etcd-maintenance]
- Put the members on fast SSDs that other processes do not share. [@etcd-hardware]
- In ZooKeeper, re-read after each watch event, and design for missed middle states.
- In etcd, use a linearizable read when a decision depends on the latest value. In ZooKeeper, call `sync()` first, and remember it narrows the gap but does not close it.

## With AI

### What to ask the agent

> "List every key our services write to etcd. For each, give the size, writes per minute and readers. Flag anything that looks like application data, not coordination data."

> "Here is our ZooKeeper watch code. Check that it sets a new watch after each event and reads the current value, and show what happens if the node changes twice quickly."

> "We use an etcd lock to stop two workers handling the same order. The orders live in Postgres. Rewrite it with a row lock and explain what we lose and gain."

### What to check yourself

- The agent did not store blobs, logs or per-request data in the coordination service.
- Watch code handles a compacted revision by reading the current state, not by crashing.
- Lock code that needs fresh data does not use serializable reads.
- The agent did not add a coordination service where a database row lock would do.

## Check yourself

## Go deeper

Before this: [Consensus and Raft](/learn/consensus-and-raft), [leader election and leases](/learn/leader-election-and-leases) and [failure detection and membership](/learn/failure-detection-and-membership). Related: [DNS and service discovery](/learn/dns-and-service-discovery), [CAP and consistency models](/learn/cap-pacelc-consistency-models) and [locks, atomics and deadlocks](/learn/locks-atomics-and-deadlocks). For row locks and advisory locks, see [PostgreSQL in depth](/learn/postgresql-in-depth). Kafka internals is coming. See [the Roblox incident page](/incidents#roblox-2021-10-consul).
