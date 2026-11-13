---
topic: kafka-internals
subtitle: Kafka is a set of logs on disk, copied between brokers. Learn which settings decide what is safe, what is ordered, and what pauses.
status: published
illustrative_story: false
glossary_optout: [ack, compaction, watermark, join]
sources:
  - id: jepsen-kafka
    title: "Kyle Kingsbury, “Jepsen: Kafka” (September 2013)"
    url: https://aphyr.com/posts/293-call-me-maybe-kafka
    note: A test of Kafka 0.8's replication before its release. A leader alone in the ISR acks writes, then a lagging follower is elected and 520 acked writes are lost.
  - id: kip-106
    title: "KIP-106 - Change Default unclean.leader.election.enabled from True to False"
    url: https://cwiki.apache.org/confluence/display/KAFKA/KIP-106+-+Change+Default+unclean.leader.election.enabled+from+True+to+False
    note: The proposal to protect data by default instead of electing a replica that is behind.
  - id: kafka-design
    title: "Apache Kafka documentation: Design (Kafka 4.3)"
    url: https://kafka.apache.org/43/design/design/
    note: The Replication section (ISR, committed messages, unclean leader election, durability settings) and the Log Compaction section.
  - id: kafka-log
    title: "Apache Kafka documentation: Log (Kafka 4.3)"
    url: https://kafka.apache.org/43/implementation/log/
    note: How a partition is stored as segment files named by offset, how reads find an offset, and why deletion works one segment at a time.
  - id: kafka-topic-config
    title: "Apache Kafka documentation: Topic Configs (Kafka 4.3)"
    url: https://kafka.apache.org/43/configuration/topic-configs/
    note: segment.bytes, retention.ms, cleanup.policy, min.insync.replicas and unclean.leader.election.enable, with their defaults.
  - id: kafka-producer
    title: "Apache Kafka documentation: Producer Configs (Kafka 4.3)"
    url: https://kafka.apache.org/43/configuration/producer-configs/
    note: acks, linger.ms, batch.size and how the default partitioner picks a partition.
  - id: kip-966
    title: "KIP-966: Eligible Leader Replicas"
    url: https://cwiki.apache.org/confluence/display/KAFKA/KIP-966%3A+Eligible+Leader+Replicas
    note: Defines the high watermark (one line on consumer visibility is inverted; see the design doc) and describes how the last replica in the ISR can still lose committed data.
  - id: kafka-elr
    title: "Apache Kafka documentation: Eligible Leader Replicas (Kafka 4.3)"
    url: https://kafka.apache.org/43/operations/eligible-leader-replicas/
    note: Which replicas outside the ISR may safely become leader, the election order, and when the feature is on by default.
  - id: kafka-consumer-configs
    title: "Apache Kafka documentation: Consumer Configs (Kafka 4.3)"
    url: https://kafka.apache.org/43/configuration/consumer-configs/
    note: group.protocol, partition.assignment.strategy, session.timeout.ms, max.poll.interval.ms and auto-commit.
  - id: kafka-distribution
    title: "Apache Kafka documentation: Distribution (Kafka 4.3)"
    url: https://kafka.apache.org/43/implementation/distribution/
    note: The group coordinator and the compacted __consumer_offsets topic.
  - id: kip-429
    title: "KIP-429: Kafka Consumer Incremental Rebalance Protocol"
    url: https://cwiki.apache.org/confluence/display/KAFKA/KIP-429%3A+Kafka+Consumer+Incremental+Rebalance+Protocol
    note: Why the old protocol makes every member give up all its partitions, and how cooperative rebalancing moves only the ones that change owner.
  - id: kafka-rebalance
    title: "Apache Kafka documentation: Consumer Rebalance Protocol (Kafka 4.3)"
    url: https://kafka.apache.org/43/operations/consumer-rebalance-protocol/
    note: The KIP-848 protocol, how to turn it on, and the plan to make it the default.
  - id: kafka-ops
    title: "Apache Kafka documentation: Basic Kafka Operations (Kafka 4.3)"
    url: https://kafka.apache.org/43/operations/basic-kafka-operations/
    note: What adding partitions does to keyed data, and how to check consumer lag with kafka-consumer-groups.sh.
  - id: kafka-kraft
    title: "Apache Kafka documentation: KRaft (Kafka 4.3)"
    url: https://kafka.apache.org/43/operations/kraft/
    note: Controller and broker roles, how many controllers to run, and the metadata log.
claims_checked:
  - claim: "In Jepsen's September 2013 test of the pre-release Kafka 0.8, a leader alone in the ISR acked writes, a lagging follower was then elected, and 520 of 987 acknowledged writes were lost."
    source: jepsen-kafka
    note: "Partition scenario and the results table (987 acknowledged, 520 acknowledged writes lost)."
  - claim: "Since 0.11.0.0, Kafka by default waits for an in-sync replica instead of electing one that is behind; unclean leader election can lose committed messages."
    source: kafka-design
    note: "Replication section: 'By default from version 0.11.0.0, Kafka chooses the first strategy and favor waiting for a consistent replica.'"
  - claim: "min.insync.replicas defaults to 1; with acks=all, too few in-sync replicas makes the write fail with NotEnoughReplicas; the typical setup is replication factor 3, min.insync.replicas=2 and acks=all."
    source: kafka-topic-config
    note: "min.insync.replicas entry."
  - claim: "acks defaults to all, and Kafka 4.0 changed the linger.ms default from 0 to 5 ms."
    source: kafka-producer
    note: "acks and linger.ms entries."
  - claim: "Eligible Leader Replicas (KIP-966) are on by default in new clusters starting with Kafka 4.1; in 4.0 they had to be turned on."
    source: kafka-elr
    note: "Default status paragraph of the ELR page."
  - claim: "After adding partitions, messages with the same key may be routed to different partitions; Kafka does not redistribute existing data and cannot reduce the partition count."
    source: kafka-ops
    note: "Expanding your cluster / modifying topics section."
quiz:
  - q: A topic has 3 replicas and min.insync.replicas=2. Two brokers are down. A producer with acks=all sends a write. What happens?
    options:
      - The leader stores it and acks, and copies it later.
      - The write fails with NotEnoughReplicas, so the producer knows it was not stored safely.
      - Kafka elects a new leader that is behind.
    answer: 1
    why: With acks=all, a write needs at least min.insync.replicas in-sync copies. One is not enough, so Kafka returns an error instead of a weak ack.
  - q: Events for one order must be read in the order they were sent. What do you do?
    options:
      - Use the order ID as the key, so all its events go to one partition.
      - Add more partitions, so the events move faster.
      - Use acks=all.
    answer: 0
    why: Kafka keeps order only inside a partition. The key hash sends every event of one order to the same partition.
  - q: You grow a keyed topic from 6 to 12 partitions. What changes?
    options:
      - Nothing. Kafka moves old messages so each key stays in one place.
      - New messages for a key may go to a different partition than its old messages, so order per key can break.
      - Kafka shrinks it back once the load drops.
    answer: 1
    why: Kafka does not move existing data. The key-to-partition mapping changes with the count, and Kafka cannot reduce partitions either.
  - q: What does a tombstone do on a compacted topic?
    options:
      - It deletes the whole topic.
      - It marks a key as deleted. After compaction and a delay, the key's older values and then the tombstone itself go away.
      - It pauses the log cleaner.
    answer: 1
    why: A tombstone is a message with a key and a null value. Compaction keeps the latest value per key, so the delete wins, and the marker is removed after delete.retention.ms.
incidents: []
related: [queues-vs-logs, replication, exactly-once, backpressure, schema-evolution, coordination-services, stream-processing, sharding]
reviewed_by: ai-factcheck
reviewed_on: 2026-11-13
---

## What broke

In September 2013, Kyle Kingsbury tested a pre-release of Kafka 0.8 with his tool, Jepsen. His question was simple: when Kafka says a write is safe, is it? [@jepsen-kafka]

Kafka keeps each partition on several servers, called brokers. One copy is the leader. The leader tracks which followers are up to date: the in-sync replicas, or ISR. A write counted as committed once every member of the ISR had it. [@jepsen-kafka]

He cut the leader off from its followers. They timed out and dropped out of the ISR, so the leader was alone in it. It kept acking writes that existed only on its own disk. The producer was waiting for every in-sync copy, and it still got "ok". [@jepsen-kafka]

Then the leader lost its claim, and Kafka elected a follower that was far behind. When the old leader came back, its writes conflicted with the new leader's log and were thrown away. Of 987 acked writes, 520 were lost. [@jepsen-kafka]

He suggested that Kafka should "block and sound an alarm" instead of silently dropping data. [@jepsen-kafka] Kafka later changed its default. Since version 0.11.0.0, it waits for an in-sync replica rather than elect one that is behind. [@kafka-design] [@kip-106] But two settings still decide whether your writes are safe. Most teams never look at them.

## The idea in simple words

You know from [queues vs logs](/learn/queues-vs-logs-delivery-guarantees) that a Kafka topic is split into partitions. Each partition is a log: messages are only added at the end, and each gets an offset.

This lesson shows what is inside. A partition is a set of files on a broker's disk. It is copied to other brokers. A producer picks the partition for each message. A consumer group splits the partitions among its members, and each member saves its offset.

> [!key] **The key idea.** Kafka gives every guarantee per partition: the order, the leader, the copies and the consumer's offset. Once you see that, most Kafka behaviour makes sense.

## How it works

### Segments on disk

A partition is not one big file. It is a series of log segments. Each segment file is named after the offset of its first message, like `00000000000000000000.log`. New messages go to the newest segment. When it reaches a size limit, Kafka starts a new one. [@kafka-log] That limit is `segment.bytes`, 1 GiB by default. Kafka also rolls a segment after `segment.ms`, 7 days by default. [@kafka-topic-config]

To read from an offset, Kafka first finds the segment that holds it, then the position inside the file. [@kafka-log] Next to each segment, an index maps offsets to file positions. Kafka adds an entry about every 4 KiB of data (`index.interval.bytes`). A second index, the time index, gets entries on the same schedule when the timestamp has moved forward. It lets Kafka find messages by time. [@kafka-topic-config]

Kafka writes to the file system and leans on the OS page cache. It writes to the log without necessarily flushing each write to disk. [@kafka-design] Its safety comes from copies on other brokers, not from one disk.

### Retention and log compaction

With the default `cleanup.policy=delete`, Kafka keeps data for `retention.ms`, 7 days by default. A size limit, `retention.bytes`, is off by default. [@kafka-topic-config] Kafka deletes old data one whole segment at a time. [@kafka-log] So data can live a bit longer than the limit, until its segment is old enough.

With `cleanup.policy=compact`, Kafka keeps at least the last value for each key. Older values for the same key are removed in the background by the log cleaner. Offsets never change. [@kafka-design] This suits a topic of "current state per key", such as customer addresses.

To delete a key, you send a tombstone: a message with that key and a null value. [@kafka-design] Kafka keeps the tombstone for `delete.retention.ms`, one day by default. [@kafka-topic-config] A consumer that falls further behind than that can miss the delete. [@kafka-design]

### Producers: partitioner, batching and acks

A producer sends each message straight to the leader of its partition. [@kafka-design] If the message has a key, the default partitioner hashes the key to pick the partition. With no key, it sticks to one partition until at least `batch.size` bytes have gone there, then moves on. [@kafka-producer]

The producer groups messages for the same partition into batches. `batch.size` caps a batch at 16,384 bytes by default. `linger.ms` lets the producer wait a little for more messages. Kafka 4.0 changed its default from 0 to 5 ms. [@kafka-producer] Bigger batches mean fewer requests and more throughput, for a few milliseconds of extra delay.

The `acks` setting decides when the producer hears "ok". [@kafka-producer]

- `acks=0`: the producer does not wait at all. Retries do nothing.
- `acks=1`: the leader writes to its own log and answers. If it dies before followers copy the message, the message is lost.
- `acks=all`: the leader waits for every in-sync replica. This is the default.

The [idempotent producer](/learn/exactly-once-and-effectively-once) needs `acks=all`, and stops retries from writing duplicates.

### Replication: the ISR and the high watermark

Each partition has one leader and zero or more followers. Followers fetch from the leader, much like a consumer. [@kafka-design] A follower stays in the ISR while it keeps its session with the controller and keeps up. If it lags longer than `replica.lag.time.max.ms`, the leader removes it. [@kafka-design]

A message is committed when all in-sync replicas have it. Consumers see only committed messages. [@kafka-design] The high watermark marks the end of that committed part of the log. [@kip-966] Kafka promises that a committed message is not lost "as long as there is at least one in sync replica alive". [@kafka-design]

Now the trap from Jepsen. "All in-sync replicas" can mean just the leader. `acks=all` then means one copy. The fix is `min.insync.replicas`. With `acks=all`, a write fails with `NotEnoughReplicas` when the ISR is smaller than this number. [@kafka-topic-config] Its default is 1. The docs suggest 3 replicas with `min.insync.replicas=2`. [@kafka-topic-config]

Step through a partition losing replicas. Use Next and Back, or the ← and → keys.

[[diagram:kafka-isr-min-insync]]

If every in-sync replica dies, Kafka must choose. It can wait for one to come back, or elect any replica that returns first, even one that is behind. The second choice keeps the partition up but may lose committed messages. [@kafka-design] That is unclean leader election. `unclean.leader.election.enable` is `false` by default. [@kafka-topic-config] Even then, a lone last replica that crashes uncleanly can lose committed data. [@kip-966] KIP-966 adds Eligible Leader Replicas to close this gap. They are on by default in new clusters since Kafka 4.1, but not in 4.0. [@kafka-elr]

### Consumer groups, offsets and rebalances

Each consumer group has a group coordinator on one broker. When a consumer commits offsets, the coordinator writes them to `__consumer_offsets`, a compacted topic that keeps the latest commit per partition. [@kafka-distribution] By default, the consumer commits offsets every 5 seconds in the background. [@kafka-consumer-configs]

When a member joins, leaves or stops answering, the group rebalances: partitions move between members. A member that is silent for `session.timeout.ms` (45 seconds by default) is removed in the classic protocol. With KIP-848, the broker sets this timeout. A member is also removed if it does not call `poll()` within `max.poll.interval.ms` (5 minutes). [@kafka-consumer-configs]

In the old "eager" protocol, every member gives up all its partitions, then waits for a new assignment. Nobody reads during that pause. [@kip-429] Cooperative rebalancing, from Kafka 2.4, revokes only the partitions that change owner. Everyone else keeps reading. [@kip-429] For the classic protocol, choose `CooperativeStickyAssignor`. [@kafka-consumer-configs]

The newer protocol from KIP-848 is generally available since Kafka 4.0. It is "fully incremental" and the broker computes the assignment. [@kafka-rebalance] On the client it is still off by default: set `group.protocol=consumer`. The docs expect it to become the client default in Kafka 5.0. [@kafka-rebalance]

Consumer lag is how far a group is behind the end of each partition. `kafka-consumer-groups.sh --describe` shows the current offset, the log end offset and the lag for each partition. [@kafka-ops] Watch it. Lag that keeps growing is your [backpressure](/learn/backpressure-and-flow-control) signal.

### Ordering, and the partition count

Kafka keeps order only inside a partition. Same key, same partition, same order. Across partitions there is no order.

That is why the partition count is hard to change for keyed data. Roughly, the default partitioner picks `hash(key) % number_of_partitions`. When you add partitions, messages for a key "may be routed to different partitions". Kafka does not move the old data. [@kafka-ops] For a while, one key's events sit in two partitions, and a consumer can read them out of order. Kafka also cannot reduce the number of partitions. [@kafka-ops] So pick the count for your peak load, with room to grow.

### KRaft controllers

Kafka now keeps cluster metadata in its own log, run by controllers. This is KRaft mode, covered in [ZooKeeper, etcd and coordination](/learn/zookeeper-etcd-coordination). A server can be a broker, a controller, or both. Most clusters run 3 or 5 controllers. A majority must stay alive, so 3 survive one failure and 5 survive two. [@kafka-kraft] Don't run combined mode in critical production. [@kafka-kraft]

## Trade-offs

| Option | Lose acked writes? | Cost | Use it when |
|---|---|---|---|
| `acks=0` | Yes. You never know if it arrived. | Fastest. | Metrics where a gap is fine. |
| `acks=1` | Yes, if the leader dies before followers copy it. | One round trip to the leader. | Logs you can lose a little of. |
| `acks=all`, `min.insync.replicas=1` | Yes, when the ISR shrinks to the leader. | Waits for the ISR. | Rarely. This is the default pair, so check it. |
| `acks=all`, 3 replicas, `min.insync.replicas=2` | No, while one in-sync copy survives. | Writes fail when 2 of 3 brokers are down. | Orders, payments, anything you must keep. |
| Unclean leader election on | Yes. | The partition comes back sooner. | Almost never. |
| Cooperative or KIP-848 rebalancing | n/a | Needs a client upgrade or config change. | Large groups, frequent deploys. |

**Simple rule:** for data you must not lose, use 3 replicas, `acks=all` and `min.insync.replicas=2`. Leave unclean election off.

## At work: how a senior uses this

- Read the topic config before you trust a topic. Ask: **"What is `min.insync.replicas`, and what happens if two brokers die?"**
- Pick the key and the partition count on day one. Write both in the topic's README, with where order holds.
- Alert on consumer lag per partition, not just per group. One hot partition can hide in an average.
- Find out why a group rebalances. Slow handlers that miss `max.poll.interval.ms` cause rebalances that cause more lag.
- Move consumers to cooperative or KIP-848 rebalancing before scaling a group up.
- For compacted topics, set `delete.retention.ms` longer than your slowest consumer can fall behind.

## With AI

### What to ask the agent

> "List every Kafka topic in this repo's config with its replication factor, min.insync.replicas, cleanup.policy and retention.ms. Flag any topic where acks=all gives only one copy."

> "Our consumer group rebalances every few minutes. Read this consumer code and config, and find what could make a member miss max.poll.interval.ms."

> "We want to grow this keyed topic from 12 to 24 partitions. Write a plan that keeps per-key order, for example a new topic and a cut-over."

### What to check yourself

- The agent did not claim `acks=all` alone means "on 3 brokers". It means "on every replica in the ISR right now".
- Any partition change for a keyed topic has a plan for old and new messages of the same key.
- Offsets are committed after the work, not by a timer in the middle of it.
- Config changes name the Kafka version. Defaults like `linger.ms` changed in 4.0.
- Nobody turned on unclean leader election to "fix" an offline partition without saying what data it loses.

## Check yourself

## Go deeper

Before this: [Queues vs logs](/learn/queues-vs-logs-delivery-guarantees) and [database replication](/learn/database-replication). Related: [exactly-once and effectively-once](/learn/exactly-once-and-effectively-once) for the idempotent producer and transactions, [schema evolution and encoding](/learn/schema-evolution-and-encoding) for message formats, [stream processing](/learn/stream-processing), and [sharding and consistent hashing](/learn/sharding-and-consistent-hashing) for why changing a partition count moves keys.
