---
topic: raft
subtitle: Raft lets a small group of servers agree on one list of changes, even when some of them fail. Learn how it works and what it costs.
status: published
illustrative_story: true
sources:
  - id: raft-paper
    title: "Diego Ongaro and John Ousterhout, “In Search of an Understandable Consensus Algorithm (Extended Version)”, 2014 (USENIX ATC 2014)"
    url: https://raft.github.io/raft.pdf
    note: The Raft paper. Section 2 explains replicated state machines, section 5 the whole algorithm, and Figure 2 fits it on one page.
  - id: raft-site
    title: "The Raft Consensus Algorithm (raft.github.io)"
    url: https://raft.github.io/
    note: A short intro to consensus and Raft, with a visual demo you can click through.
  - id: etcd-faq
    title: "etcd documentation (v3.5): Frequently Asked Questions"
    url: https://etcd.io/docs/v3.5/faq/
    note: The cluster size and failure tolerance table, and why an odd number of members is best.
  - id: consul-consensus
    title: "HashiCorp Consul documentation: Consensus protocol"
    url: https://developer.hashicorp.com/consul/docs/architecture/consensus
    note: How Consul servers use Raft, what goes in the log, and a deployment table of servers, quorum size and failure tolerance.
  - id: kafka-kraft
    title: "Apache Kafka documentation (4.1): Operations, KRaft"
    url: https://kafka.apache.org/41/operations/kraft/
    note: How Kafka controllers form a metadata quorum, and why you run 3 or 5 of them.
  - id: kip-595
    title: "Apache Kafka, KIP-595: A Raft Protocol for the Metadata Quorum"
    url: https://cwiki.apache.org/confluence/display/KAFKA/KIP-595%3A+A+Raft+Protocol+for+the+Metadata+Quorum
    note: Kafka's design for a self-managed metadata quorum to replace ZooKeeper. It calls its protocol "a sort of Raft dialect".
  - id: crdb-design
    title: "CockroachDB design document (GitHub, cockroachdb/cockroach, docs/design.md)"
    url: https://github.com/cockroachdb/cockroach/blob/master/docs/design.md
    note: See "Raft - Consistency of Range Replicas". Each range of keys is its own Raft group.
  - id: tikv-readme
    title: "TiKV README (GitHub, tikv/tikv)"
    url: https://github.com/tikv/tikv
    note: TiKV splits data into Regions, and the copies of each Region form a Raft group.
  - id: paxos-simple
    title: "Leslie Lamport, “Paxos Made Simple”, 2001"
    url: https://lamport.azurewebsites.net/pubs/paxos-simple.pdf
    note: Paxos in plain words. Section 3 shows how to build a replicated state machine on top of it.
  - id: flp
    title: "Michael J. Fischer, Nancy A. Lynch and Michael S. Paterson, “Impossibility of Distributed Consensus with One Faulty Process”, Journal of the ACM 32(2), 1985"
    url: https://groups.csail.mit.edu/tds/papers/Lynch/jacm85.pdf
    note: The FLP result. The abstract states the main claim in two sentences.
  - id: pg-repl
    title: "PostgreSQL documentation: Log-Shipping Standby Servers"
    url: https://www.postgresql.org/docs/current/warm-standby.html
    note: Streaming replication is asynchronous by default, so a primary crash can lose committed transactions.
  - id: dynamo
    title: "Werner Vogels, “Amazon's Dynamo” (2007), with the SOSP paper"
    url: https://www.allthingsdistributed.com/2007/10/amazons_dynamo.html
    note: A leaderless, "always writeable" store. Shows R + W > N quorums and how apps merge conflicting versions.
  - id: github-2018
    title: "GitHub, “October 21 post-incident analysis”"
    url: https://github.blog/2018-10-30-oct21-post-incident-analysis/
    note: Orchestrator, a failover tool built on Raft, moved the MySQL primaries across the US after a 43-second network cut.
  - id: roblox-2021
    title: "Roblox, “Roblox Return to Service 10/28-10/31 2021”"
    url: https://about.roblox.com/newsroom/2022/01/roblox-return-to-service-10-28-10-31-2021/
    note: How Roblox runs Consul (5 voters, Raft), and how a 73-hour outage followed when it failed.
claims_checked:
  - claim: "A log entry is committed once the leader that created it has stored it on a majority of servers; a new leader counts majorities only for entries from its own term."
    source: raft-paper
    note: "§5.3 and §5.4.2 say exactly this. The leader sends its commit index in AppendEntries, including heartbeats (§5.3)."
  - claim: "Election timeouts are random from a fixed range, e.g. 150–300 ms; each server votes for at most one candidate per term, first come, first served; a candidate needs a majority of the full cluster."
    source: raft-paper
    note: "§5.2. Matches SPEC.md raft-election. The up-to-date rule (later last term, then longer log) is §5.4.1."
  - claim: "3 and 4 members tolerate 1 failure, 5 tolerate 2, 7 tolerate 3; an odd number is recommended, probably no more than seven."
    source: etcd-faq
    note: "Failure tolerance table and max cluster size answer. Consul's deployment table gives the same numbers and recommends 3 or 5 servers."
  - claim: "FLP: in an asynchronous system, every consensus protocol may never finish, even with only one faulty process."
    source: flp
    note: "Abstract (JACM 32(2), 374–382, 1985, read via Crossref). The lesson states only what the abstract says. Paxos Made Simple §2.4 links FLP to needing randomness or timeouts."
  - claim: "KIP-595 calls its protocol 'a sort of Raft dialect': Kafka log replication plus Raft leader election."
    source: kip-595
    note: "The KIP does not use the name KRaft; the Kafka 4.1 KRaft operations page does, and gives 3 controllers tolerating 1 failure and 5 tolerating 2."
  - claim: "GitHub 2018: a 43-second cut; Orchestrator, built on Raft, failed primaries over to US West, leaving writes on each coast the other lacked. Roblox 2021: 73-hour outage; Consul with 5 voters, used for discovery, health checks, session locking and KV."
    source: github-2018
    note: "Both postmortems read. Roblox details are from roblox-2021."
reviewed_by: ai-factcheck
reviewed_on: 2026-10-21
quiz:
  - q: Your etcd cluster has 5 members. How many can fail at the same time while the cluster still accepts writes?
    options:
      - "1"
      - "2"
      - "4"
    answer: 1
    why: A majority of 5 is 3. With 2 members down, the other 3 are still a majority.
  - q: A teammate wants to grow a 3-node Raft cluster to 4 nodes "for safety". What does the 4th node add?
    options:
      - It lets the cluster survive 2 failures.
      - Nothing for failures. A majority of 4 is 3, so it still survives only 1 failure.
      - It makes every write faster.
    answer: 1
    why: The etcd table shows 3 and 4 members both tolerate 1 failure. Use an odd number.
  - q: In Raft, when is a new log entry committed?
    options:
      - When the leader writes it to its own disk.
      - When every server has stored it.
      - When the leader has stored it on a majority of servers.
    answer: 2
    why: A majority is enough. Any future leader needs votes from a majority, so one voter has the entry and refuses a candidate without it.
  - q: Why does Raft pick each election timeout at random?
    options:
      - So that usually only one follower times out first, and votes are not split.
      - So that the leader can save network traffic.
      - So that the slowest server becomes leader.
    answer: 0
    why: If all followers timed out at once, they would all ask for votes and nobody might win. Random timeouts spread them out.
  - q: A follower was cut off and missed the last 50 entries. It times out and asks for votes. What happens?
    options:
      - It wins, because it asked first.
      - The servers with newer logs refuse to vote for it, so it cannot get a majority while they are a majority.
      - It deletes the other servers' entries.
    answer: 1
    why: A server only votes for a candidate whose log is at least as up to date as its own. This keeps committed entries safe.
incidents: [github-2018-10-split-brain, roblox-2021-10-consul]
related: [leader-election, replication, quorums-leaderless, coordination-services, cap-consistency]
---

## What broke

A payouts service runs on two servers. Only the active one may send money to sellers each night. Every second, it writes "I am alive" to a shared table. If the standby sees no update for 10 seconds, it takes over.

One night the active server freezes for 15 seconds in a long garbage collection pause. The standby takes over and starts sending payouts.

Then the old server wakes up. Nothing tells it that it lost its job, so it carries on. About 300 sellers get paid twice. Each server decided "I am in charge" on its own. This is a split-brain.

This story is made up, but the pattern is common. A better timeout will not fix it. The servers need to agree, by majority, on who is in charge. That is consensus. The old server must also check that it is still in charge before it acts.

## The idea in simple words

Many systems need one agreed answer: who is the leader now, or which config is current. A consensus algorithm gives it. It lets a group of machines act as one, even if some of them fail. [@raft-paper] Once the group decides something, the decision is final. [@raft-site]

The usual shape is a replicated state machine. Each server keeps a log of commands, and every log holds the same commands in the same order. Each server runs them on the same deterministic code, so all end up with the same state. [@raft-paper]

Raft is a consensus algorithm built to be easy to understand. It is equal to Paxos in fault tolerance and speed. [@raft-site]

> [!key] **The key idea.** One leader decides the order. A change counts only when a majority of servers has stored it. Any two majorities share at least one server, so the group can never decide two different things.

## How it works

### Roles and terms

Each Raft server is in one of three states: leader, follower or candidate. In normal work there is exactly one leader and the others are followers. Followers only answer the leader and candidates. The leader handles all client requests. [@raft-paper]

Raft splits time into numbered Raft terms. Each term starts with an election. If a candidate wins, it is the leader for the rest of that term. There is at most one leader per term. [@raft-paper]

Terms work like a clock that every server can read. Servers send their term in every message. A server that sees a bigger term takes that number, and a leader or candidate goes back to being a follower at once. A request with an old term is rejected. [@raft-paper] This is how an old leader that wakes up learns that it lost its job.

The basic algorithm needs only two kinds of messages. **RequestVote** is sent by candidates. **AppendEntries** is sent by the leader to copy log entries. An AppendEntries with no entries is a heartbeat. [@raft-paper]

### Elections

The leader sends heartbeats to every follower. If a follower hears nothing for one election timeout, it assumes there is no leader. It moves to the next term, becomes a candidate, votes for itself, and sends RequestVote to all other servers. [@raft-paper]

A candidate wins if a majority of the full cluster votes for it in that term. Each server votes for at most one candidate per term, first come, first served. So two candidates cannot both win the same term. The winner sends heartbeats at once to stop new elections. [@raft-paper]

What if two followers time out together and split the votes? Nobody wins, and they try again in a new term. To make this rare, Raft picks each election timeout at random from a fixed range. The paper uses 150–300 ms as an example. Usually one server times out first, wins and sends heartbeats before the others time out. [@raft-paper]

Step through an election.

[[diagram:raft-leader-election]]

Now stop the leader yourself and watch a follower take over.

[[anim:raft-election]]

### Copying the log

A client sends a command to the leader. The leader adds it to its log, then sends AppendEntries to all followers at the same time. Each log entry stores the command, its position (the index) and the term in which the leader got it. [@raft-paper]

An entry is **committed** once the leader that created it has stored it on a majority of servers. Then it is safe to run on the state machines. The leader sends its commit index, the highest committed index, in every AppendEntries, so followers learn which entries they may apply. [@raft-paper]

Each AppendEntries also carries the index and term of the entry just before the new ones. If the follower does not have that exact entry, it refuses. The leader then steps back one entry and tries again, until the two logs match. The follower deletes anything after that point and takes the leader's entries. [@raft-paper]

This check gives the **Log Matching** rule: if two logs share an entry with the same index and term, they match up to that point. [@raft-paper]

### The safety rule

What stops a server with an old log from becoming leader and deleting committed entries? The vote itself. RequestVote includes the candidate's last log index and term. A server refuses to vote for a candidate whose log is less up to date than its own. [@raft-paper]

"More up to date" means a later term on the last entry. If the last terms are equal, the longer log wins. A candidate needs a majority, and every committed entry lives on a majority. So at least one voter has every committed entry, and that voter blocks a candidate that lacks one. [@raft-paper]

A new leader cannot treat an old entry as committed just because a majority holds it. It counts majorities only for entries from its own term. [@raft-paper]

### 3 servers or 5

A Raft group works while a majority of its servers are up and can talk, so five servers survive any two failures. [@raft-paper] If more fail, the group stops making progress, but it never gives a wrong answer. [@raft-site]

| Servers | Majority | Failures you survive |
|---|---|---|
| 3 | 2 | 1 |
| 4 | 3 | 1 |
| 5 | 3 | 2 |
| 7 | 4 | 3 |

A 4th server adds no failure tolerance. [@etcd-faq] [@consul-consensus] etcd recommends an odd number of members, and probably no more than seven. [@etcd-faq] Consul recommends 3 or 5 servers. [@consul-consensus]

### What it costs

In the normal case, a command finishes after one round of messages to a majority. [@raft-paper] Each of those messages usually waits for a disk write. The paper says this round trip may take 0.5–20 ms, based on the storage. [@raft-paper] Servers in different data centres make each request slower. [@etcd-faq] More members also make writes slower, because data goes to more machines. [@etcd-faq]

When the leader crashes, the group is unavailable for about one election timeout. [@raft-paper] Reads need care too. A leader that answers from memory may already be replaced, so for fresh reads it first checks with a majority. [@raft-paper]

Our advice: put small, important data in Raft, like leaders, config, locks and metadata. Not every user event.

### Where it runs

- **etcd** is built on Raft. Its leader handles all requests that need consensus. [@etcd-faq]
- **Consul** servers use Raft. The log holds changes like new nodes, registered services and key-value updates. [@consul-consensus]
- **CockroachDB** splits data into ranges. Each range has three or more copies, and they run their own Raft group. [@crdb-design]
- **TiKV** splits data into Regions. The copies of each Region form a Raft group. [@tikv-readme]
- **Kafka** used to rely on ZooKeeper's consensus protocol. Its replacement, from KIP-595, is "a sort of Raft dialect": Kafka's own log copying plus Raft's leader election. [@kip-595] In KRaft mode, a quorum of controllers keeps the metadata. With 3 controllers it survives 1 failure, and with 5 it survives 2. [@kafka-kraft]

### Paxos and FLP, briefly

Paxos, by Leslie Lamport, is the older consensus algorithm. Proposers suggest values and acceptors accept them, in two phases that each need a majority. One run picks one value. For a log, you run one instance per log position, with a leader as the only proposer. [@paxos-simple] Raft gives the same result as this multi-Paxos, but is easier to learn. [@raft-paper]

In 1985, Fischer, Lynch and Paterson studied consensus in an asynchronous system, where messages can take any amount of time. They proved that every protocol for it might never finish, even if only one process is faulty. [@flp] This is called the FLP result. Because of it, a reliable way to elect a leader must use randomness or real time, such as timeouts. [@paxos-simple] In Raft, safety never depends on timing. Only availability does. [@raft-paper]

## Trade-offs

| Option | Lose committed writes? | Cost | Use it when |
|---|---|---|---|
| Single leader + async replication | Yes. A primary crash can lose recent commits. [@pg-repl] | Fast writes. Failover needs a safe way to choose one leader. | Most app databases. It is the Postgres default. [@pg-repl] |
| Raft group (3 or 5 servers) | No, while a majority survives. [@raft-paper] | Every write waits for a majority and a disk write. No writes without a majority. | Leaders, config, locks, metadata, and databases built on Raft. |
| Leaderless quorums | No single order. Apps may need to merge conflicting versions. [@dynamo] | Any node takes writes. You must design conflict rules. | You need writes to always work, like a shopping cart. [@dynamo] |
| External coordination service (etcd, Consul, ZooKeeper) | No, for data stored in it. Your own database is not protected. | One more system to run. Many services may depend on it. | You need leader election or locks but do not want to write Raft yourself. |

**Simple rule:** do not write your own consensus. Use a tested Raft library or a coordination service.

## At work: how a senior uses this

- When a design says "only one worker does X", ask: **"Who decides that, and what happens to the old one if it wakes up?"** If the answer is a timestamp in a table, you have the payouts bug.
- Run 3 or 5 servers, never 2 or 4. Place them in different failure zones so one outage cannot take a majority.
- Know what Raft protects. At GitHub in 2018, Orchestrator used Raft for its own decisions. After a 43-second network cut, it still failed MySQL primaries over to the US West Coast. Both coasts ended up with writes the other did not have. [@github-2018] Consensus in the tool did not make the database safe.
- Watch the blast radius of the coordination cluster. Roblox used one Consul cluster for service discovery, health checks, session locking and key-value data. When it failed, Roblox had a 73-hour outage. [@roblox-2021]
- Measure write latency on the Raft group, and alert on leader changes. If the leader changes often, check disk speed and the network first.

## With AI

### What to ask the agent

> "Our cron job runs on 3 pods, and only one may run each night. Show me how to use an etcd lease and election so only the leader runs it. Explain what happens if the leader pauses for 30 seconds."

> "Here is our Raft cluster config. List the election timeout and heartbeat interval, and check that the heartbeat is much shorter than the election timeout."

### What to check yourself

- The code stops doing leader work as soon as it loses the lease or sees a newer term. It must not finish the batch "just this once".
- The cluster has an odd number of nodes, spread across failure zones.
- Any Raft code comes from a tested library. A "simple Raft" from scratch will miss edge cases, like the rule on older-term entries.
- Reads that must be fresh go through the leader's majority check, not a local read from any member.
- Monitoring and alerts do not depend on the same coordination cluster they watch.

## Check yourself

## Go deeper

Before this: [database replication](/learn/database-replication) and [leader election and leases](/learn/leader-election-and-leases). Next: [ZooKeeper, etcd and coordination](/learn/zookeeper-etcd-coordination), and [CAP and consistency models](/learn/cap-pacelc-consistency-models).
