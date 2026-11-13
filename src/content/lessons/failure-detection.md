---
topic: failure-detection
subtitle: A cluster must decide when a node is dead, but it can never be sure. Learn how heartbeats, phi accrual and SWIM gossip make that guess, and what goes wrong after it.
status: published
illustrative_story: false
sources:
  - id: github-2012
    title: "Jesse Newland, “GitHub availability this week”, The GitHub Blog (September 2012)"
    url: https://github.blog/2012-09-14-github-availability-this-week/
    note: The official write-up. Health checks failed on a busy MySQL primary, the cluster failed over to a cold server, and then failed back.
  - id: phi-paper
    title: "Naohiro Hayashibara, Xavier Défago, Rami Yared, Takuya Katayama, “The ϕ Accrual Failure Detector”, JAIST Research Report IS-RR-2004-010 (May 2004)"
    url: https://dspace.jaist.ac.jp/dspace/bitstream/10119/4784/1/IS-RR-2004-010.pdf
    note: The original paper. A suspicion level on a scale instead of yes or no, and what each value of phi means.
  - id: akka-fd
    title: "Akka documentation, “Phi Accrual Failure Detector”"
    url: https://doc.akka.io/libraries/akka-core/current/typed/failure-detector.html
    note: A short, practical explanation of phi with its formula, the default threshold and advice for cloud networks.
  - id: cassandra-yaml
    title: "Apache Cassandra documentation, “cassandra.yaml file configuration”"
    url: https://cassandra.apache.org/doc/latest/cassandra/managing/configuration/cass_yaml_file.html
    note: The phi_convict_threshold setting and its default.
  - id: swim-paper
    title: "Abhinandan Das, Indranil Gupta, Ashish Motivala, “SWIM: Scalable Weakly-consistent Infection-style Process Group Membership Protocol”, Proc. International Conference on Dependable Systems and Networks (DSN 2002)"
    url: https://www.cs.cornell.edu/projects/Quicksilver/public_pdfs/SWIM.pdf
    note: The SWIM paper. Random probes, indirect probes, suspicion, incarnation numbers and gossip on top of pings.
  - id: lifeguard-paper
    title: "Armon Dadgar, James Phillips, Jon Currey, “Lifeguard: Local Health Awareness for More Accurate Failure Detection”, arXiv:1707.00788 (2017)"
    url: https://arxiv.org/abs/1707.00788
    note: Why slow message processing makes SWIM mark healthy members as failed, and the idea of local health.
  - id: lifeguard-blog
    title: "Jon Currey, “Making Gossip More Robust with Lifeguard”, HashiCorp blog (July 2017)"
    url: https://www.hashicorp.com/en/blog/making-gossip-more-robust-with-lifeguard
    note: A plain summary of the three Lifeguard changes in memberlist, with results.
  - id: aws-2011
    title: "Amazon Web Services, “Summary of the Amazon EC2 and Amazon RDS Service Disruption in the US East Region” (April 2011)"
    url: https://aws.amazon.com/message/65648/
    note: The postmortem of the EBS "re-mirroring storm", where nodes that lost contact assumed their peers had failed.
claims_checked:
  - claim: "On Monday, September 10, 2012, migration load made Percona Replication Manager's health checks fail on GitHub's MySQL primary. The active role moved to a server with a cold cache, then failed back. Afterwards, only the operations team starts a failover."
    source: github-2012
    note: "Matches the post: \"higher load on the database than our operations team has previously seen\", \"cold InnoDB buffer pool and performed rather poorly\", \"failed back\", maintenance-mode, and \"only occur when initiated by a member of our operations team\"."
  - claim: "A phi threshold of 1 means about a 10% chance of a mistake, 2 about 1%, and 3 about 0.1%. The paper's master/worker example stops sending jobs at a low level and removes the worker at a high one."
    source: phi-paper
    note: "Section IV-A after Eq. 2, and the Example in Section I. Eq. 2-4 also match the lesson's code (phi = -log10(1 - F), normal distribution). Worked values recomputed with the code: about 9.0 (spread 0.1 s) and about 1.6 (spread 0.3 s)."
  - claim: "Akka and Cassandra both default to a phi threshold of 8. Akka suggests 12 on Amazon EC2."
    source: akka-fd
    note: "Akka: \"The default threshold is 8\", \"could be increased to 12\" on EC2. Cassandra's phi_convict_threshold default value is 8 (cassandra-yaml)."
  - claim: "In SWIM, an unanswered ping leads to ping-req through k other members, to avoid congestion on one path. A suspected member refutes with an Alive message carrying a higher incarnation number, and a timed-out suspicion leads to Confirm."
    source: swim-paper
    note: "Sections 3 and 4.2. The paper also says all-to-all heartbeat load grows quadratically, and expected detection time and per-member load do not vary with group size. Published at DSN 2002, pp. 303-312."
  - claim: "Lifeguard cut false positives from CPU exhaustion by more than 50x. With the new memberlist defaults, failures were detected 20% faster with 20x fewer false positives."
    source: lifeguard-blog
    note: "Matches the post (July 6, 2017); the new default is alpha=4. Also lists Self-Awareness, Dogpile and Buddy System."
  - claim: "In the 2011 EBS event, nodes that lost contact assumed their peers had failed, and the re-mirroring storm left about 13% of volumes in the affected zone stuck."
    source: aws-2011
    note: "\"it assumes the other node failed\", \"re-mirroring storm\", \"about 13% of the volumes in the affected Availability Zone\"."
reviewed_by: ai-factcheck
reviewed_on: 2026-11-10
quiz:
  - q: Your failure detector uses a 2-second timeout. A healthy node has a 3-second garbage collection pause. What happens?
    options:
      - Nothing. The detector knows the node is only paused.
      - The node is marked dead, even though it is healthy. This is a false positive.
      - The node is marked dead only if it also stops answering health checks.
    answer: 1
    why: From the outside, a pause looks the same as a crash. Any silence longer than the timeout is treated as a failure.
  - q: What does a phi accrual failure detector give you instead of "alive" or "dead"?
    options:
      - A suspicion level that grows the longer a heartbeat is late, compared with past heartbeat gaps.
      - The exact time when the node crashed.
      - The number of nodes that agree the node is dead.
    answer: 0
    why: Phi is a score on a scale. Each application picks its own threshold, for example 8 in Akka and Cassandra by default.
  - q: In SWIM, node A gets no ack from node B. What does A do next?
    options:
      - It marks B as dead at once and tells everyone.
      - It sends a heartbeat to every member of the group.
      - It asks a few other members to ping B for it.
    answer: 2
    why: The indirect ping-req goes over other network paths. It stops one congested link from getting a healthy node removed.
  - q: Lifeguard adds "local health" to SWIM. What problem does that solve?
    options:
      - A slow or overloaded node wrongly accuses healthy nodes of being dead.
      - Gossip messages are too large for one network packet.
      - New nodes cannot find the cluster when they start.
    answer: 0
    why: If a node is slow itself, its missed acks are probably its own fault. Lifeguard makes such a node wait longer before it accuses others.
incidents: [github-2012-09-mysql-failover]
glossary_optout: [ack, round-trip-time]
related: [why-distributed-hard, load-balancing, raft, leader-election, sharding, resilience-patterns]
---

## What broke

In September 2012, GitHub ran its main MySQL database on a 3-node cluster. A tool called Percona Replication Manager, running on Pacemaker, ran health checks on the primary. If the checks failed, it moved the "active" role to another server. [@github-2012]

On Monday, a database migration put more load on the primary than the team had seen before. The primary was busy, not broken. But the load made its health checks fail. [@github-2012]

So the cluster did what it was told. It moved the active role and the primary database to another server. [@github-2012]

The new server had a cold cache, so it "performed rather poorly". Under real traffic, its health checks failed too. The active role moved back to the first server. [@github-2012]

A GitHub engineer turned on Pacemaker's maintenance mode, which turns off all health checks and automatic failover. The site recovered slowly as the cache filled. After the incident, GitHub changed its setup so that only a person from operations starts a failover of the primary database. [@github-2012]

The health check could not tell "busy" from "dead". Each wrong decision made the next server weaker.

## The idea in simple words

A cluster needs to know which nodes are alive. It uses that list to route requests, place data and pick leaders.

But you saw in [Why distributed systems are hard](/learn/why-distributed-systems-are-hard) that silence has many causes. A node can be dead, paused, overloaded, or behind a bad link. The SWIM paper puts it plainly: a process that loses messages looks the same as one that has failed. [@swim-paper]

So a failure detector can only guess. Every design picks a point between two mistakes:

- **Too fast:** you mark healthy nodes dead. This is a false positive.
- **Too slow:** a dead node keeps getting traffic for a long time.

> [!key] **The key idea.** A failure detector does not find dead nodes. It finds nodes that are late. Choose how late counts as dead, and design what happens next so that a wrong guess is cheap.

## How it works

### Heartbeats and a fixed timeout

The simplest design: each node sends a heartbeat every second. If you hear nothing for, say, 5 seconds, you call the node dead. Raft uses this idea for leaders. A follower that hears no heartbeat for one election timeout starts an election. You can read more in [Consensus and Raft](/learn/consensus-and-raft).

A load balancer health check is the same idea, but it asks instead of waiting. [Load balancing](/learn/load-balancing) covers those settings.

A fixed timeout has a problem. A good value on a quiet network is too short on a busy one. The phi paper calls this the trade-off between conservative and aggressive detection. [@phi-paper]

### Phi accrual: a score, not a yes or no

Hayashibara and others proposed the phi accrual failure detector in 2004. It does not answer "trust" or "suspect". It gives a suspicion level on a continuous scale. The level keeps rising while no heartbeat arrives. [@phi-paper]

The phi detector keeps a window of recent gaps between heartbeats. From these, it learns what "normal" looks like. Then it asks: how unlikely is it that the next heartbeat is still on its way? [@phi-paper]

```python
import math, statistics

def phi(now, last_beat, gaps):
    mean = statistics.mean(gaps)
    std = max(statistics.stdev(gaps), 0.1)  # avoid a zero spread
    late = now - last_beat
    # chance a heartbeat still arrives after this long
    p_later = 0.5 * math.erfc((late - mean) / (std * math.sqrt(2)))
    return -math.log10(max(p_later, 1e-300))
```

This is the formula Akka documents, using a normal curve. [@akka-fd] Each step of 1 in phi means about ten times fewer mistakes. With a threshold of 1, about 10% of suspicions turn out wrong. With 2 it is about 1%, and with 3 about 0.1%. [@phi-paper]

Say heartbeats come every 1 second. With a spread of 0.1 seconds (the smallest the code allows), 1.6 seconds of silence gives phi of about 9. With a spread of 0.3 seconds, the same silence gives phi of only about 1.6. With a threshold of 8, the first node is suspected and the second is not. The detector adapts to the network you really have.

Akka and Cassandra both use a default threshold of 8. [@akka-fd] [@cassandra-yaml] Akka suggests 12 on cloud networks such as Amazon EC2. [@akka-fd] A low threshold gives many wrong suspicions but finds crashes fast. A high one makes fewer mistakes but is slower. [@akka-fd]

### SWIM: membership by gossip

Heartbeats raise a second problem: who talks to whom? If every node sends heartbeats to every other node, the network load grows with the square of the group size. [@swim-paper]

SWIM splits the job into two parts: failure detection and spreading the news. [@swim-paper] Step through one probe round.

[[diagram:swim-probe-cycle]]

In each protocol period, a node pings one random member. If no ack comes in time, it sends a ping-req to k other members. They ping the target and relay any ack. This avoids blaming a node for congestion on one path. [@swim-paper]

If nobody gets an ack, the target is marked "suspect", not dead. A suspected node can refute this by sending an Alive message with a higher incarnation number. If the suspicion times out first, a Confirm message removes the node. [@swim-paper]

The news does not need its own messages. It rides on the pings and acks that are already flowing. The paper calls this infection-style dissemination. [@swim-paper] The expected detection time and the load on each member do not grow with the size of the group. [@swim-paper] SWIM is a gossip protocol: news spreads from node to node, with no central server.

### Lifeguard: when the accuser is the slow one

The SWIM paper itself notes a weak spot. A slow process, for example one that drops packets from full buffers, can declare several healthy processes faulty. [@swim-paper]

HashiCorp hit this in memberlist, the library inside Serf, Consul and Nomad. CPU exhaustion, network delay or loss made members wrongly declare others faulty. [@lifeguard-blog] Their fix, Lifeguard, assumes the local failure detector may be the problem. [@lifeguard-paper] It has three parts: [@lifeguard-blog]

- **Self-awareness.** A node keeps a score of signs that it is unhealthy itself. While the score is high, it is slower to accuse others.
- **Dogpile.** The suspicion timeout starts long and gets shorter as more members confirm the suspicion. A lone, slow accuser gets no support, so the target has time to refute.
- **Buddy system.** A suspected node is told directly in the probe it receives, so it can refute at once.

HashiCorp reports more than 50 times fewer false positives under CPU exhaustion. With the new memberlist defaults, failures were found 20% faster with 20 times fewer false positives. [@lifeguard-blog]

### After detection

Detection is only half of the story. The action you take next often does more damage.

- **Rebalancing.** Data and traffic move off the "dead" node. HashiCorp notes that false accusations move traffic away from healthy members and cause needless data rebalancing. [@lifeguard-blog]
- **Flapping.** The node comes back, then is removed again. GitHub's role moved away and then back on one day. [@github-2012]
- **Cascading failure.** Moving work to the rest of the cluster adds load. In 2011, AWS EBS nodes that lost contact assumed their peers had failed. [@aws-2011] They all searched for space for new copies at once. This "re-mirroring storm" left about 13% of volumes in one zone stuck. [@aws-2011]

Removing a slow node is not free. The work it held lands on the nodes that are left.

## Trade-offs

| Option | Detects a crash fast? | Few false positives? | Use it when |
|---|---|---|---|
| Fixed timeout, short | Yes. | No. Pauses and slow links trip it. | Cheap actions, such as skipping a server for a few seconds. |
| Fixed timeout, long | No. | Yes. | Expensive actions, such as moving data. |
| Phi accrual | Yes, on a steady network. | Yes. It adapts to jitter. | Peer-to-peer databases and actor systems such as Cassandra and Akka. |
| SWIM gossip with Lifeguard | Yes. Time does not grow with cluster size. | Yes. Indirect probes and suspicion help. | Large clusters that need a shared member list, such as Consul. |
| A human decides | No. | Yes. | Rare, risky moves, such as failing over a primary database. |

## At work: how a senior uses this

- Ask: **"What does this cluster do when it marks a node dead, and what does a wrong guess cost?"** Size the timeout to the cost of the action.
- Use different thresholds for different actions. The phi paper's example: stop sending new work at a low level, and remove the worker only at a high one. [@phi-paper]
- Know your longest normal pause: garbage collection, a big compaction, a slow disk. Your timeout must be longer.
- Rate-limit removals. If many nodes "die" at once, suspect the network or the detector first.
- When you see false positives, find the cause before you raise the threshold. Akka lists long GC pauses, overload and tight CPU limits. [@akka-fd]
- Plan the rebalance. Check that the remaining nodes can take the extra load.
- Make removal and rejoin cheap and gradual, so flapping does not move all the data each time.

## With AI

### What to ask the agent

> "Read our cluster config. List every failure detection setting: heartbeat interval, timeouts, phi threshold, suspicion multiplier. For each, explain what action it triggers."

> "Our Cassandra nodes are marked down during GC pauses. Show me how to confirm this from the logs, and compare raising the phi threshold with fixing the pauses."

> "Write a test that pauses one node for 10 seconds in our 5-node cluster. Show what moves, how long it takes, and what happens when the node returns."

### What to check yourself

- The agent did not raise every timeout without saying what gets slower. Longer detection means a dead node gets traffic for longer.
- The proposed timeout is longer than your real worst pauses, taken from metrics, not guesses.
- Automatic actions after detection have a limit, such as "remove at most one node per 10 minutes".
- Any code that counts members handles a node that is "suspect", not only "alive" or "dead".

## Check yourself

## Go deeper

Next: [leader election and leases](/learn/leader-election-and-leases), which use failure detection to pick one leader. Related: [Consensus and Raft](/learn/consensus-and-raft), [Sharding and consistent hashing](/learn/sharding-and-consistent-hashing), and [Circuit breakers and bulkheads](/learn/circuit-breakers-and-bulkheads). See [the GitHub incident page](/incidents#github-2012-09-mysql-failover).
