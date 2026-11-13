---
topic: stream-processing
subtitle: Count, sum and join events as they arrive. Learn event time, windows, watermarks and checkpoints, and what "exactly-once" really promises.
status: published
reviewed_by: ai-factcheck
reviewed_on: 2026-11-08
illustrative_story: true
glossary_optout: [process, join, sliding-window, fixed-window]
sources:
  - id: flink-time
    title: "Apache Flink documentation (2.3): Timely Stream Processing"
    url: https://nightlies.apache.org/flink/flink-docs-stable/docs/concepts/time/
    note: Event time vs processing time, what a watermark means, late elements, and how an operator with many inputs takes the minimum watermark.
  - id: flink-watermarks
    title: "Apache Flink documentation (2.3): Generating Watermarks"
    url: https://nightlies.apache.org/flink/flink-docs-stable/docs/dev/datastream/event-time/generating_watermarks/
    note: Bounded out-of-orderness, idle inputs that hold the watermark back, and watermarks per Kafka partition.
  - id: flink-windows
    title: "Apache Flink documentation (2.3): Windows"
    url: https://nightlies.apache.org/flink/flink-docs-stable/docs/dev/datastream/operators/windows/
    note: Tumbling, sliding and session windows, allowed lateness (0 by default), and the side output for late data.
  - id: flink-stateful
    title: "Apache Flink documentation (2.3): Stateful Stream Processing"
    url: https://nightlies.apache.org/flink/flink-docs-stable/docs/concepts/stateful-stream-processing/
    note: Keyed state, checkpoint barriers, barrier alignment, recovery, and savepoints compared with checkpoints.
  - id: flink-backends
    title: "Apache Flink documentation (2.3): State Backends"
    url: https://nightlies.apache.org/flink/flink-docs-stable/docs/ops/state/state_backends/
    note: The heap backend vs the RocksDB backend, the default, incremental checkpoints, and size limits.
  - id: flink-savepoints
    title: "Apache Flink documentation (2.3): Savepoints"
    url: https://nightlies.apache.org/flink/flink-docs-stable/docs/ops/state/savepoints/
    note: How to stop, upgrade and rescale a job from a savepoint, and why you set operator IDs with uid().
  - id: flink-connectors
    title: "Apache Flink documentation (2.3): DataStream Connectors, Fault Tolerance Guarantees"
    url: https://nightlies.apache.org/flink/flink-docs-stable/docs/connectors/datastream/overview/
    note: What a source and a sink must do for exactly-once results. Checkpointing is off by default.
  - id: flink-ckpt-bp
    title: "Apache Flink documentation (2.3): Checkpointing under backpressure"
    url: https://nightlies.apache.org/flink/flink-docs-stable/docs/ops/state/checkpointing_under_backpressure/
    note: Why slow operators make checkpoints slow, and the two fixes, buffer debloating and unaligned checkpoints.
  - id: abs-paper
    title: "Paris Carbone, Gyula Fóra, Stephan Ewen, Seif Haridi and Kostas Tzoumas, “Lightweight Asynchronous Snapshots for Distributed Dataflows” (2015)"
    url: https://arxiv.org/abs/1506.08603
    note: The paper behind Flink's checkpoints (Asynchronous Barrier Snapshotting).
  - id: streaming-101
    title: "Tyler Akidau, “Streaming 101: The world beyond batch”, O'Reilly Radar (2015)"
    url: https://www.oreilly.com/radar/the-world-beyond-batch-streaming-101/
    note: Event time vs processing time, the skew between them, and fixed, sliding and session windows.
  - id: streaming-102
    title: "Tyler Akidau, “Streaming 102: The world beyond batch”, O'Reilly Radar (2016)"
    url: https://www.oreilly.com/radar/the-world-beyond-batch-streaming-102/
    note: Perfect vs heuristic watermarks, watermarks that are too slow or too fast, triggers and allowed lateness.
  - id: kafka-streams
    title: "Apache Kafka documentation (4.3): Kafka Streams Core Concepts"
    url: https://kafka.apache.org/43/streams/core-concepts/
    note: Local state stores, the grace period for out-of-order records, and exactly_once_v2.
  - id: spark-ss
    title: "Apache Spark documentation (4.3): Structured Streaming, Getting Started"
    url: https://spark.apache.org/docs/latest/streaming/getting-started.html
    note: The trigger interval, watermarks for late data, and end-to-end exactly-once with replayable sources and idempotent sinks.
  - id: uber-ads
    title: "Uber Engineering, “Real-Time Exactly-Once Ad Event Processing with Apache Flink, Kafka, and Pinot” (2021)"
    url: https://www.uber.com/blog/real-time-exactly-once-ad-event-processing/
    note: A real pipeline with one-minute tumbling windows, Kafka transactions, a 2-minute checkpoint interval and record IDs for upserts.
claims_checked:
  - claim: "An operator with many inputs uses the smallest of their watermarks, so one idle input holds the watermark back; withIdleness marks such an input as idle."
    source: flink-time
    note: "Concepts page: an operator's current event time is the minimum of its input streams' event times. Idleness and withIdleness are on the Generating Watermarks page (flink-watermarks)."
  - claim: "Allowed lateness is 0 by default. Flink drops an event only when the watermark has passed the end of its window plus the allowed lateness; later events can go to a side output."
    source: flink-windows
    note: "Windows page: 'By default, the allowed lateness is set to 0'; late elements are dropped when the watermark is past the end of the window; sideOutputLateData(OutputTag)."
  - claim: "A savepoint is a checkpoint you trigger by hand, and it does not expire when newer checkpoints complete."
    source: flink-stateful
    note: "Savepoints are 'manually triggered checkpoints' that do not automatically expire when newer checkpoints are completed."
  - claim: "For exactly-once results the source must rewind to the checkpoint position and the sink must commit transactionally or write idempotently; checkpointing is disabled by default."
    source: flink-connectors
    note: "Fault Tolerance Guarantees section of the DataStream Connectors page, Flink 2.3."
  - claim: "The default state backend keeps objects on the Java heap; RocksDB stores serialized data on local disk, is on average an order of magnitude slower, and supports incremental checkpoints."
    source: flink-backends
    note: "'If nothing else is configured, the system will use the HashMapStateBackend'; 'average performance that is an order of magnitude slower than the memory state backends'."
  - claim: "Uber's ad pipeline uses one-minute tumbling windows, Kafka transactions read with read_committed, a 2-minute checkpoint interval, and unique record IDs for Pinot upserts and Hive dedup."
    source: uber-ads
    note: "Uber Engineering blog, 23 Sep 2021. Says the checkpoint interval is the main delay, so the pipeline is not exactly real-time."
quiz:
  - q: A job counts orders per minute using processing time. It was down for 10 minutes and then catches up in 30 seconds. What does the chart show?
    options:
      - The right count for every minute, because Kafka kept the events.
      - A gap of 10 minutes, then one minute with about 10 minutes of orders in it.
      - Nothing changes, because processing time ignores downtime.
    answer: 1
    why: Processing-time windows use the clock of the machine. All the old events are read in one minute, so they are all counted in that minute.
  - q: In Flink, what does a watermark with time 12:01:00 tell an operator?
    options:
      - That every event with a time up to 12:01:00 has been written to the sink.
      - That the machine clock is 12:01:00.
      - That event time has reached 12:01:00, so no more events at or before that time are expected.
    answer: 2
    why: A watermark is a promise about event time, not about the clock. It can be wrong, which is why late events exist.
  - q: Your Flink job has exactly-once checkpointing on. It calls a payment API inside a map function. The job fails and restarts. What can happen?
    options:
      - Nothing. Exactly-once means each record runs exactly once.
      - Some payments are sent twice, because records after the last checkpoint are processed again.
      - Flink rolls back the payment API calls.
    answer: 1
    why: Flink restores state and replays input from the last checkpoint. Its state ends up as if each event counted once, but outside calls repeat.
  - q: What is the main reason to choose the RocksDB state backend over the heap backend?
    options:
      - It is faster for every state access.
      - Your state is too large for memory, for example long windows or many keys.
      - It is the only backend that supports checkpoints.
    answer: 1
    why: RocksDB keeps state on local disk, so it can hold much more. Each access must be serialized, so it is slower than the heap.
  - q: You need a Kafka consumer that writes each order to Postgres, keyed by order ID. There are no windows or joins. What do you use?
    options:
      - A plain consumer with idempotent upserts. A stream processor adds little here.
      - Flink with RocksDB and exactly-once checkpoints.
      - Spark Structured Streaming with a one-second trigger.
    answer: 0
    why: There is no state across events and no time logic. An upsert by order ID is already safe to repeat.
incidents: []
related: [queues-vs-logs, sagas-outbox-cdc, clocks-ordering, idempotency-retries, backpressure]
---

## What broke

A ride app shows a live chart: rides started per minute, per city. A small job reads ride events from Kafka and counts them. Each count goes into the minute when the job reads the event.

One evening the job is down for 10 minutes during a deploy. Kafka keeps the events, so nothing is lost. When the job comes back, it reads the 10-minute backlog in about 30 seconds.

The chart shows 10 empty minutes, then one minute with about ten times the normal rides. A pricing rule watches that chart. It sees "demand spike" and raises prices across the city.

There was no spike. The rides happened at a normal rate. The job counted them by when it *read* them, not by when they *happened*. The team switches to the time inside each event. Then phones that were offline in a tunnel send their events 3 minutes late, and the team learns about late data.

## The idea in simple words

A stream processor is a program that runs forever. It reads an endless flow of events, keeps some memory between them, and sends out results. Apache Flink, Kafka Streams and Spark Structured Streaming are the common tools.

Every event has two times. **Event time** is when it happened on the device. **Processing time** is when your machine sees it. [@flink-time] The gap between them changes all the time. Network delays, backlogs and offline phones can make it minutes, hours or days. [@streaming-101]

You cannot sum an endless stream, so you cut it into windows, like "each minute". [@flink-time] If you group by processing time, events land in the wrong window whenever the gap changes. [@streaming-101] That is the ride chart bug.

Grouping by event time is correct, but raises a new question. When is the 12:00 minute *finished*? A late event could still arrive. A **watermark** is the engine's answer: a marker that says "event time has now reached t". [@flink-time]

> [!key] **The key idea.** Group by when things happened, not when you saw them. A watermark decides when a window is done. Some events will still come late, so decide on purpose what to do with them.

## How it works

Step through one window. Use Next and Back, or the ← and → keys.

[[diagram:stream-watermark-window]]

### Windows

A **tumbling window** has a fixed size and never overlaps. Each event belongs to exactly one, like "12:00 to 12:01". A **sliding window** has a size and a slide. A 10-minute window that slides every 5 minutes overlaps, so one event can count in two windows. A **session window** closes after a gap with no events, for example 30 minutes without a click. [@flink-windows]

### Watermarks and late data

A common rule is "newest event time seen, minus a fixed delay". Flink's built-in bounded out-of-orderness strategy follows this rule. [@flink-watermarks] With a 5-second delay, a window ending at 12:01 fires soon after an event from 12:01:05 or later arrives.

An operator with many inputs uses the *smallest* watermark of them. [@flink-time] So one quiet Kafka partition can hold back the whole job. Flink's `withIdleness` setting marks such an input as idle so the rest can move on. [@flink-watermarks]

Pick the delay with care. A watermark that is too slow makes every result wait. One that is too fast closes windows early and turns normal events into late ones. [@streaming-102]

An event is late when it arrives after the watermark has passed its time. [@flink-time] For windows, what matters is the end of the window. Flink drops an event only if the watermark has already passed the end of its window plus the allowed lateness. The allowed lateness is 0 by default. [@flink-windows] If you set it, an event that arrives within that time still joins its window, and the window can fire again with a new result. Flink keeps the window's state until the lateness runs out. After that, you can route even later events to a **side output** instead of losing them silently. [@flink-windows]

Kafka Streams calls the same idea a grace period. A record that arrives after it is discarded for that window. [@kafka-streams] Spark uses a watermark to know how late data can be, and to clean up old state. [@spark-ss]

### Keyed state and where it lives

A count per city must be remembered between events. Flink keeps this keyed state next to the operator, split by key, so each update is local. [@flink-stateful] Kafka Streams also keeps local state stores, with automatic recovery. [@kafka-streams]

Flink's default state backend keeps state as Java objects on the heap. It is fast, but limited by memory. The RocksDB backend keeps state on local disk, so it suits long windows and very large state. Each access must be serialized, and the docs call it "an order of magnitude slower" on average. RocksDB also supports incremental checkpoints. [@flink-backends]

### Checkpoints: barriers in the stream

If a machine dies, the counts in memory are gone. So Flink takes regular snapshots, called checkpoints. The design is "inspired by" the Chandy-Lamport algorithm for distributed snapshots. [@flink-stateful] The 2015 paper behind it describes how, for a dataflow with no cycles, the snapshot needs only operator state, not the messages in flight. [@abs-paper]

It works like this. [@flink-stateful]

1. The sources insert a **checkpoint barrier** into the stream and note their position, such as a Kafka offset.
2. Barriers never overtake records. Everything before barrier n belongs to checkpoint n.
3. An operator with two inputs waits until barrier n has come from both. This is called alignment. Then it passes the barrier on and saves its state.
4. When every sink has received barrier n and told the coordinator, checkpoint n is complete.

On a failure, Flink restores all state from the last complete checkpoint. It rewinds the sources to the saved offsets and reads again. [@flink-stateful] Note that checkpointing is off until you turn it on. [@flink-connectors]

A **savepoint** is a checkpoint you trigger by hand. It does not expire when newer checkpoints finish. [@flink-stateful] You use it to stop a job, deploy new code, or change parallelism, then resume. Give each operator a stable ID with `uid()`, or the new version may not find its old state. [@flink-savepoints]

### What "exactly-once" means here

Flink's exactly-once is about state. After a restore, records processed since the checkpoint have not touched the saved state. [@flink-stateful] But those records *are* processed again. A call to an outside API inside your code can run twice.

End-to-end exactly-once needs two more things. The source must rewind to the checkpoint position. The sink must take part, by committing writes in a transaction or by writing idempotently. [@flink-connectors] Spark says the same: replayable sources plus idempotent sinks. [@spark-ss] Kafka Streams with `exactly_once_v2` covers reading from Kafka and writing results back to Kafka topics. [@kafka-streams]

Uber's ad pipeline shows the cost. Flink writes results to Kafka in transactions that commit only at the next checkpoint, every 2 minutes. Readers use `read_committed`, so they see results only after the next checkpoint, which can be about 2 minutes later. Uber says the checkpoint interval is the main delay, so the pipeline is "not exactly 'real-time'". Uber also gives each record a unique ID, so its databases can upsert or deduplicate. [@uber-ads]

### Backpressure

When a sink is slow, buffers fill and the slowness spreads back to the source. Barriers travel with the data, so they wait in the same queues. [@flink-stateful] Checkpoints then take much longer. [@flink-ckpt-bp] Flink offers two fixes. Buffer debloating keeps less data in flight. Unaligned checkpoints let barriers pass buffered data, but save that data in the checkpoint, which adds I/O. [@flink-ckpt-bp]

## Trade-offs

| Option | State across events? | Event-time windows? | Good for |
|---|---|---|---|
| Plain consumer + database | No. Each event is handled alone. | No. You write it yourself. | Copying, enriching or upserting each event. Simple to run. |
| Kafka Streams | Yes. Local state stores. | Yes, with a grace period. | Kafka in, Kafka out, with exactly-once inside Kafka. |
| Apache Flink | Yes. Heap or RocksDB, with checkpoints. | Yes, with watermarks, lateness and side outputs. | Large state and complex time logic. More moving parts to run. |
| Spark Structured Streaming | Yes. | Yes, with watermarks. | Teams already on Spark. Results come in small batches, for example every second. [@spark-ss] |
| Nightly batch job | n/a | n/a | Reports where a day of delay is fine. Easiest to fix and rerun. |

**Simple rule:** if each event can be handled alone, a consumer plus an idempotent write is enough. Reach for a stream processor when you need windows, joins over time, or large state per key.

## At work: how a senior uses this

- Ask first: **"Which time are we grouping by?"** Business questions almost always mean event time.
- Write down the watermark delay and the allowed lateness, and why. Then measure how late events really are.
- Never drop late events silently. Send them to a side output with a counter and an alert.
- Watch for a stuck watermark. One idle partition can stop every window from firing.
- Turn checkpointing on, store checkpoints in durable storage, and set `uid()` on every stateful operator before the first deploy.
- For every sink, ask: **"What happens when the last 2 minutes are replayed?"** Use a transactional or idempotent sink.
- Choose the checkpoint interval as a latency budget, since transactional sinks only commit at checkpoints.
- Use RocksDB when state may outgrow memory. Size the local disks for it.

## With AI

### What to ask the agent

> "Here is our Flink job. List every operator that uses processing time. For each, tell me if the business question needs event time instead."

> "Add a bounded out-of-orderness watermark of 10 seconds, idleness detection of 1 minute, allowed lateness of 2 minutes, and a side output for late events with a metric."

> "Write a test that feeds events out of order into this windowed count, including one late and one very late event. Assert each window's output."

> "Review this sink. If the job restarts from the last checkpoint, could any row be written twice? Show me how to make it idempotent."

### What to check yourself

- The timestamp comes from a field in the event, not from `System.currentTimeMillis()`.
- Late data goes somewhere you can see. The default drops it.
- Side effects inside operators, such as HTTP calls, are safe to repeat.
- Every stateful operator has a stable `uid()`, so a savepoint restore works after a code change.
- The test really sends events out of order. A test with sorted input proves nothing about watermarks.
- The agent did not "fix" late data by making the watermark delay huge. That only delays every result.

## Check yourself

## Go deeper

Before this: [Queues vs logs](/learn/queues-vs-logs-delivery-guarantees). Related: [Time, clocks and ordering](/learn/time-clocks-and-ordering), [Idempotency, retries and timeouts](/learn/idempotency-retries-timeouts), [Sagas, the outbox pattern and CDC](/learn/sagas-outbox-and-cdc), [Exactly-once and effectively-once](/learn/exactly-once-and-effectively-once), and [LinkedIn's case on the log](/cases#linkedin-the-log). Coming later: counting and top-k at scale, fraud and risk systems, and ML platforms with feature stores.
