---
topic: cap-consistency
subtitle: When the network splits, every copy of your data must choose between answering and being right. The rest of the time, it chooses between fast and right.
status: published
reviewed_by: ai-factcheck
reviewed_on: 2026-10-21
illustrative_story: true
sources:
  - id: gilbert-lynch
    title: "Seth Gilbert and Nancy Lynch, “Brewer's Conjecture and the Feasibility of Consistent, Available, Partition-Tolerant Web Services”, ACM SIGACT News, 2002"
    url: https://users.ece.cmu.edu/~adrian/731-sp04/readings/GL-cap.pdf
    note: The proof of CAP. Section 2 defines consistency, availability and partition tolerance exactly. Theorem 1 is the result.
  - id: brewer-2012
    title: "Eric Brewer, “CAP Twelve Years Later: How the ‘Rules’ Have Changed”, IEEE Computer, 2012"
    url: https://www.infoq.com/articles/cap-twelve-years-later-how-the-rules-have-changed/
    note: Brewer explains why "2 of 3" is misleading, and how to detect a partition, limit some operations, and recover after it.
  - id: abadi
    title: "Daniel J. Abadi, “Consistency Tradeoffs in Modern Distributed Database System Design”, IEEE Computer, February 2012"
    url: https://www.cs.umd.edu/~abadi/papers/abadi-pacelc.pdf
    note: The PACELC paper. Explains the latency versus consistency trade-off and classifies Dynamo, Cassandra, PNUTS, MongoDB and others.
  - id: kleppmann-cap
    title: "Martin Kleppmann, “Please stop calling databases CP or AP” (2015)"
    url: https://martin.kleppmann.com/2015/05/11/please-stop-calling-databases-cp-or-ap.html
    note: Why most real databases are neither CP nor AP in CAP's strict sense, and what to say instead.
  - id: jepsen-lin
    title: "Jepsen, Consistency models: Linearizability"
    url: https://jepsen.io/consistency/models/linearizable
    note: A short, exact definition, and why it cannot stay available during a partition.
  - id: jepsen-seq
    title: "Jepsen, Consistency models: Sequential consistency"
    url: https://jepsen.io/consistency/models/sequential
    note: Like linearizability, but without the real-time rule.
  - id: jepsen-causal
    title: "Jepsen, Consistency models: Causal consistency"
    url: https://jepsen.io/consistency/models/causal
    note: Related operations appear in the same order everywhere. Available during a partition if clients stay on one server.
  - id: jepsen-ryw
    title: "Jepsen, Consistency models: Read Your Writes"
    url: https://jepsen.io/consistency/models/read-your-writes
    note: A short, exact definition of the read-your-writes guarantee.
  - id: vogels
    title: "Werner Vogels, “Eventually Consistent – Revisited” (2008)"
    url: https://www.allthingsdistributed.com/2008/12/eventually_consistent.html
    note: Defines eventual consistency, read-your-writes and other client-side guarantees.
  - id: github-2018
    title: "GitHub, “October 21 post-incident analysis”"
    url: https://github.blog/2018-10-30-oct21-post-incident-analysis/
    note: A 43-second network cut left writes in two data centres. In recovery, GitHub chose data integrity over a fast recovery.
claims_checked:
  - claim: "CAP (Gilbert and Lynch 2002): during a partition, a system cannot guarantee both linearizable consistency and availability, where availability means every request to a non-failing node gets a response."
    source: gilbert-lynch
    note: "The 2002 PDF opens but its text layer is not machine-readable here. Definitions checked through exact quotes in Kleppmann (availability) and Abadi p. 38 (atomic/linearizable consistency). Added those citations."
  - claim: "Brewer: '2 of 3' is misleading; partitions are rare, so C and A need not be given up without a partition; CAP's C is not ACID's C; detect partitions, limit operations, then recover."
    source: brewer-2012
    note: "Quotes match. Brewer says the idea first appeared in 1998 and was presented at PODC 2000. The '2000 talk' sentence now cites Brewer, not Gilbert and Lynch."
  - claim: "PACELC definition; Dynamo, Cassandra, Riak PA/EL; VoltDB/H-Store, Megastore, BigTable, HBase PC/EC; PNUTS PC/EL; MongoDB PA/EC; Cassandra quorum vs weak reads differ by a factor of four or more."
    source: abadi
    note: "All match pp. 40–42. Classification is of default/original versions. Abadi notes that even R + W > N does not give full Gilbert–Lynch consistency, so the lesson says 'more consistency', not 'consistent'."
  - claim: "Single leader with async followers is neither CAP-consistent nor CAP-available."
    source: kleppmann-cap
    note: "Matches. Quiz 'why' changed from 'this exact setup' (Postgres is not named by Kleppmann) to the general setup."
  - claim: "Linearizable and sequential cannot be totally or sticky available; causal and read-your-writes are sticky available (clients stay on one server)."
    source: jepsen-lin
    note: "Checked on all four Jepsen pages. Jepsen calls linearizability 'one of the strongest single-object' models, so the glossary no longer says 'the strongest'."
  - claim: "GitHub 2018: 43-second cut, writes in both data centres, chose 'to prioritize data integrity over site usability and time to recovery', 24 hours 11 minutes of degradation."
    source: github-2018
    note: "Quotes match. The choice was made during recovery, after the network was back. 'Made the CP choice by hand' was reworded because CAP's choice is about behaviour during the partition."
  - claim: "Eventual consistency: if no new updates are made, all reads eventually return the last value."
    source: vogels
    note: "Was cited to DDIA, which could not be opened. Replaced with Vogels 2008."

quiz:
  - q: According to CAP, when must a system choose between consistency and availability?
    options:
      - All the time. You can only ever have 2 of the 3 properties.
      - Only while there is a network partition between its nodes.
      - Only when a disk fails.
    answer: 1
    why: CAP is about the moment the network loses messages. With no partition, a system can be both consistent and available.
  - q: A replica is cut off from the others. It refuses a write and returns an error. What did it choose?
    options:
      - Consistency over availability (CP).
      - Availability over consistency (AP).
      - Low latency over consistency (EL).
    answer: 0
    why: It stayed correct by not answering. In CAP, a node that is up but does not answer is not available.
  - q: What does the "ELC" part of PACELC describe?
    options:
      - What the system does during a partition.
      - The trade between latency and consistency when there is no partition.
      - The trade between cost and latency.
    answer: 1
    why: Else (E), in normal running, the system trades latency (L) against consistency (C). Waiting for other replicas makes a read or write slower.
  - q: Ana posts a comment. Ben replies to it. Under causal consistency, what can Chen never see?
    options:
      - Ana's comment without Ben's reply.
      - Ben's reply without Ana's comment.
      - Both of them, a few seconds late.
    answer: 1
    why: The reply depends on the comment. Causal consistency shows related operations in the same order to everyone.
  - q: Your service uses one Postgres leader with async read replicas. Is it CP or AP in the strict CAP sense?
    options:
      - CP, because there is one leader.
      - AP, because replicas answer reads.
      - Neither. Clients cut off from the leader cannot write, and replica reads can be stale.
    answer: 2
    why: Kleppmann uses this setup, one leader with async followers, to show why the CP/AP labels often do not fit real systems.
incidents: [github-2018-10-split-brain]
cases: [amazon-dynamo, google-spanner]
related: [replication, quorums-leaderless, clocks-ordering, raft, multi-region, crdts]
glossary_optout: [partition]
---

## What broke

A shoe brand runs a flash sale in two regions, Mumbai and Singapore. There are 500 pairs. The stock count lives in a database that copies data between the regions. Both regions accept orders.

Ten minutes into the sale, the link between the regions breaks for four minutes. Both sides keep selling. Each side thinks it has the full count. When the link comes back, the team finds 620 orders for 500 pairs. Support spends a week on refunds and apologies.

In the same company, the payments team uses a different database. During the same four minutes, it refused every write in Singapore. Those users saw "Payment failed, try again".

Both teams wrote the same thing in the incident channel: "the database broke". It did not. Each database did exactly what it was built to do. Nobody had written down which choice the business wanted. This story is made up, but each part of it happens in real systems.

## The idea in simple words

You keep copies of your data on several machines, called replicas. Sometimes the network between them fails, but the machines keep running. This is a network partition.

During a partition, a replica that gets a request has only two options:

- **Answer now.** It may return old data, or accept a write that conflicts with the other side. The system stays available.
- **Wait or refuse.** It does not answer until it can check with the other side. The data stays consistent.

That is the CAP theorem. Gilbert and Lynch proved it in 2002. No system can promise both consistency and availability when messages between nodes can be lost. [@gilbert-lynch]

People often say CAP means "pick 2 of 3". Eric Brewer presented the idea in a keynote talk in 2000. [@brewer-2012] In 2012 he wrote that "2 of 3" is misleading. [@brewer-2012] You cannot simply pick "no partitions", because networks can fail. [@brewer-2012] But partitions are rare. When there is no partition, a system can be both consistent and available. [@brewer-2012] [@abadi]

> [!key] **The key idea.** CAP is a rule for one bad moment: the network is split. In that moment, each cut-off replica chooses between answering and being right. The rest of the time, the real choice is between fast and right.

## How it works

Step through the shoe sale with one item left. Use Next and Back, or the ← and → keys.

[[diagram:cap-partition]]

### What C and A mean in CAP

The words in CAP are narrow. Consistency means linearizability: the system must act as if there is one copy and each operation happens at one instant. [@gilbert-lynch] [@abadi] It is not the C in ACID. [@brewer-2012]

Availability means every request that reaches a working node gets an answer. Not "most requests", and not "some node answers". [@gilbert-lynch] [@kleppmann-cap]

With these strict meanings, many real databases are neither CP nor AP. Kleppmann gives an example: one leader with async followers. A client cut off from the leader cannot write, so it is not available. Reads from a lagging follower can be stale, so it is not linearizable either. [@kleppmann-cap] So do not label a whole database. Ask what each operation does during a partition.

### PACELC: the trade you make every day

Abadi pointed out a second trade-off that matters more day to day. [@abadi] He called the full picture PACELC:

- If there is a **P**artition, choose **A**vailability or **C**onsistency.
- **E**lse, in normal running, choose **L**atency or **C**onsistency.

Why is there a trade when the network is fine? As soon as you copy data, a consistent read or write must coordinate with other replicas or a leader. That takes time. Over long distances, you cannot avoid this wait. [@abadi]

Abadi gives an example from a study of Cassandra. Reads that check several replicas were compared with reads from any one replica. The latency difference could be a factor of four or more. [@abadi]

### Who chooses what

Abadi classified well-known systems by their original default settings: [@abadi]

- **PA/EL:** Dynamo, Cassandra and Riak. They give up consistency during a partition, and for lower latency in normal running.
- **PC/EC:** VoltDB/H-Store, Megastore, BigTable and HBase. They pay in availability and latency to stay consistent.
- **PC/EL:** Yahoo's PNUTS. Fast and less consistent normally, but it refuses updates during a partition.
- **PA/EC:** MongoDB, as it worked in 2012.

Many of these systems let you change the trade. In Cassandra-style stores, you can ask more replicas to answer each read and write. You get more consistency, though still not linearizability, and pay with latency. [@abadi]

GitHub saw the cost of a short partition in 2018. A 43-second network cut caused a failover. Both data centres then had writes the other did not have. [@github-2018] During recovery, they chose "to prioritize data integrity over site usability and time to recovery". The site was degraded for 24 hours and 11 minutes. [@github-2018]

### The consistency ladder

"Consistent" and "eventually consistent" are not the only options. There is a ladder of consistency models in between. Each step down promises less, and is easier to keep available and fast.

1. **Linearizable.** Once a write is done, every later read, by anyone, sees it. The system acts like one copy. [@jepsen-lin] Use it for a lock, a unique username, or the last item in stock. It cannot stay available during a partition. [@jepsen-lin]
2. **Sequential.** Everyone sees all operations in the same order, and each client's own order is kept. But that order does not have to match real time. [@jepsen-seq] Like linearizability, it cannot stay available during a partition. [@jepsen-seq]
3. **Causal.** If one operation may have caused another, everyone sees them in that order. A reply never appears before the comment it answers. Unrelated operations may appear in different orders to different people. [@jepsen-causal]
4. **Read-your-writes.** After you save something, your own later reads show it. Other users may see it later. [@jepsen-ryw] [@vogels]
5. **Eventual.** If writes stop for a while, all replicas end up the same. The basic promise says nothing about when, or what you see before that. [@vogels]

Causal consistency and read-your-writes can stay available during a partition, if each client keeps talking to the same server. [@jepsen-causal] [@jepsen-ryw] That makes them a useful middle ground for feeds, comments and profiles.

## Trade-offs

| Model | During a partition | Normal latency | Good for |
|---|---|---|---|
| Linearizable | No. Cut-off nodes must wait or refuse. | Highest. Operations must coordinate with other replicas or a leader. | Locks, unique names, stock counts, money moves. |
| Sequential | No. Same as linearizable. | High. | Cases where all must agree on one order, and a short delay is fine. |
| Causal | Yes, if each client stays on one server. | Low to medium. | Comments and replies, chat, feeds. |
| Read-your-writes | Yes, if each client stays on one server. | Low. | Profiles and settings a user just changed. |
| Eventual | Yes. | Lowest. | Like counts, view counts, caches, search indexes. |

**Rule of thumb:** decide per operation, not per database. Ask what a wrong answer costs and what an error costs, then pick the weakest model the business can live with.

## At work: how a senior uses this

- For each write path, ask: **"If the network splits right now, should this fail or should it accept and fix later?"** Write the answer in the design doc.
- Treat "CP" and "AP" in vendor docs as a hint, not a fact. Read which operations wait, which refuse, and what the default settings are. [@kleppmann-cap]
- Price the latency. A consistent read in another region costs a round trip on every request, partition or not. [@abadi]
- If you choose AP, design the repair before launch: how to detect conflicts, which side wins, and who tells the customer. Brewer calls this partition recovery. [@brewer-2012]
- Use stronger models only where they pay. A stock count may need linearizable writes. The product description next to it does not.
- Test with a real partition. Cut the network between replicas in a test environment, and watch what your app does.

## With AI

### What to ask the agent

> "Here is our order service. List every database write. For each, tell me what happens if the replicas in two regions cannot talk for 5 minutes, with our current settings."

> "We use Cassandra with replication factor 3. Explain which read and write consistency levels give us read-your-writes for one user, and what each choice costs in latency."

> "Write a test that starts two replicas, cuts the network between them, writes on both sides, heals the network, and checks that our conflict rule keeps both orders."

### What to check yourself

- The agent's claims about a database's behaviour match that database's own docs and version. CAP labels on blogs are often wrong.
- The settings it suggests are the ones your code really sends. Many clients have their own default consistency level.
- The partition test cuts the network between replicas. Killing a node is a different failure.
- The conflict rule never silently drops a paid order or a payment.
- Every operation that needs linearizable behaviour is listed, and a person agreed to its latency cost.

## Check yourself

## Go deeper

Before this: [Replication](/learn/database-replication) explains replication lag and read-your-writes in detail. Next: [Quorums and leaderless replication](/learn/quorums-and-leaderless-replication).
