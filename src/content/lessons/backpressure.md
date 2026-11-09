---
topic: backpressure
subtitle: When the consumer is slower than the producer, you must buffer, drop or slow the sender. Learn to slow the sender, so your queues stay small.
status: published
illustrative_story: false
glossary_optout: [join, process, rfc]
sources:
  - id: discord-2026
    title: "Bo Ingram and Stephen Birarda, “You’ve Got (Too Much) Mail: Behind the Scenes of the 3/25/26 Voice Outage”, Discord Blog, 29 April 2026"
    url: https://discord.com/blog/behind-the-scenes-of-the-3-25-26-voice-outage
    note: The official postmortem. Read how a reconnect flood filled process mailboxes, and how the team got the queues to drain again.
  - id: java-tpe
    title: "Java SE 21 API documentation: ThreadPoolExecutor"
    url: https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/ThreadPoolExecutor.html
    note: The "Queuing" and "Rejected tasks" sections compare unbounded and bounded work queues, and list what happens when a pool is full.
  - id: java-executors
    title: "Java SE 21 API documentation: Executors"
    url: https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/Executors.html
    note: newFixedThreadPool uses a shared unbounded queue. Worth knowing before you use it in a server.
  - id: node-bp
    title: "Node.js Learn: Backpressuring in Streams"
    url: https://nodejs.org/en/learn/modules/backpressuring-in-streams
    note: What write() returning false means, the 'drain' event, and a measured memory test with and without backpressure.
  - id: py-queue
    title: "Python documentation: asyncio Queues"
    url: https://docs.python.org/3/library/asyncio-queue.html
    note: maxsize, put() that waits, put_nowait() that raises QueueFull, and task_done().
  - id: rs-spec
    title: "Reactive Streams: reactive-streams-jvm README (specification for the JVM)"
    url: https://github.com/reactive-streams/reactive-streams-jvm/blob/master/README.md
    note: The goals of the spec, and the rules for Subscription.request(n) and how many onNext signals a Publisher may send.
  - id: rfc9113
    title: "Thomson and Benfield (eds.), RFC 9113: HTTP/2 (IETF, 2022), section 5.2"
    url: https://www.rfc-editor.org/rfc/rfc9113#section-5.2
    note: HTTP/2 flow control per stream and per connection, the credit-based scheme, and the 65,535-octet starting window.
  - id: rfc9293
    title: "Eddy (ed.), RFC 9293: Transmission Control Protocol (TCP) (IETF, 2022)"
    url: https://www.rfc-editor.org/rfc/rfc9293
    note: The window field in section 3.1, and zero-window probing in section 3.8.6.1.
  - id: kafka-design
    title: "Apache Kafka documentation: Design (Kafka 4.3)"
    url: https://kafka.apache.org/43/design/design/
    note: The "Push vs. pull" section explains why Kafka consumers pull, and what a slow consumer does in each model.
  - id: kafka-consumer
    title: "Apache Kafka 4.3 API documentation: KafkaConsumer"
    url: https://kafka.apache.org/43/javadoc/org/apache/kafka/clients/consumer/KafkaConsumer.html
    note: The "Consumption Flow Control" section on pause() and resume(), and max.poll.records and max.poll.interval.ms.
claims_checked:
  - claim: "Discord's start-session rate limit \"provides backpressure when we have a flood of requests\" but had not been re-tuned after the migration; about 17% of sessions stopped without a clean handoff."
    source: discord-2026
    note: "Post: \"hadn't yet been re-tuned after the migration\"; \"17% of sessions across Discord were ungracefully stopped\"."
  - claim: "A ~100k-message mailbox on a single-process supervisor adds about 1 ms to each process spawn; the etcd connection also goes through those supervisors, so instances dropped out after the 60s TTL. Nearly 1M is the total across 15 voice syncers."
    source: discord-2026
    note: "Post: \"adds ~1ms to process spawn time\", \"the etcd connection ... also goes through the bottlenecking supervisors\". The ~1M figure is from the aggregate mailbox chart (alt text), not one mailbox."
  - claim: "Executors.newFixedThreadPool uses a shared unbounded queue; a running ThreadPoolExecutor rejects only when both threads and queue are bounded and saturated; CallerRunsPolicy is a simple feedback control mechanism."
    source: java-tpe
    note: "Executors Javadoc: \"a shared unbounded queue\". ThreadPoolExecutor \"Rejected tasks\" and CallerRunsPolicy text match."
  - claim: "asyncio.Queue with maxsize <= 0 is infinite; put() waits for a free slot; put_nowait() raises QueueFull."
    source: py-queue
    note: "Matches the class description and method docs. Code sketch run on Python 3.13: 100 x 202 then 503s, producer waits, join() returns."
  - claim: "Reactive Streams: total onNext signals must be <= total requested via request(n)."
    source: rs-spec
    note: "Rule 1.1 and rule 3.8."
  - claim: "HTTP/2 flow control is a credit-based scheme per stream and per connection using WINDOW_UPDATE; only DATA frames count; both windows start at 65,535 octets."
    source: rfc9113
    note: "RFC 9113 section 5.2 and 5.2.1."
reviewed_by: ai-factcheck
reviewed_on: 2026-11-09
quiz:
  - q: A service creates its worker pool with Java's Executors.newFixedThreadPool(8). Jobs arrive faster than 8 threads can finish them, for an hour. What happens?
    options:
      - New jobs are rejected with RejectedExecutionException once all 8 threads are busy.
      - The pool starts more threads to keep up.
      - New jobs wait in a queue with no size limit, which keeps growing and uses more and more memory.
    answer: 2
    why: newFixedThreadPool uses a shared unbounded queue. A running pool rejects jobs only when both its threads and its queue have finite limits and are full.
  - q: In Reactive Streams, a subscriber has called request(10) and has received 10 items. What may the publisher do next?
    options:
      - Send more items, as long as it sends them slowly.
      - Wait. It may not send more items until the subscriber requests more.
      - Drop the subscription, because the demand is used up.
    answer: 1
    why: The total number of items sent must never be more than the total requested. Demand is a credit, and the subscriber gives more credit with another request(n).
  - q: An API puts each request on a queue and returns 202 Accepted at once. A worker reads the queue. The worker slows down. Which statement is true?
    options:
      - The client slows down too, because backpressure always travels back to the caller.
      - The client does not slow down. The queue absorbs the difference, so you must give it a limit and decide what happens when it is full.
      - The worker's TCP window will make the client wait.
    answer: 1
    why: The 202 ends the client's wait before the work is done. That async boundary stops backpressure. Only a limit on the queue, with a wait or a reject, brings it back.
  - q: Your request handler must not wait. Which asyncio call fits a bounded queue there?
    options:
      - "await queue.put(job), and let it wait for a free slot."
      - "queue.put_nowait(job), and turn QueueFull into 503 with Retry-After."
      - "asyncio.Queue() with the default maxsize, so it never fills."
    answer: 1
    why: put_nowait raises QueueFull at once when the queue is at maxsize. The default maxsize of 0 means the queue has no limit.
incidents: [discord-2026-03-voice-outage]
related: [queues-vs-logs, tail-latency-queueing, load-shedding, rate-limiting, tcp-ip, http-versions, stream-processing, resilience-patterns]
---

## What broke

On 25 March 2026, voice and video on Discord were badly degraded from 12:13 to 15:30 PDT. Most users could not start or join calls. They saw "Awaiting Endpoint". [@discord-2026]

It began with a routine config change. Kubernetes stopped pods before their sessions could hand off cleanly. About 17% of sessions across Discord stopped without warning. [@discord-2026]

Those users all tried to reconnect. Discord has a start-session rate limit that "provides backpressure when we have a flood of requests". But that limit had not been re-tuned after a recent migration. Too many session starts got through, and gateway nodes in one zone began to run out of memory. [@discord-2026]

The voice service had a second problem. Its Elixir processes read one message at a time from a mailbox, "a queue of incoming messages". [@discord-2026] Each new outbound connection went through a single supervisor process. To start a process, that supervisor scanned its mailbox for one message. With about 100,000 messages waiting, each start took about 1 ms longer. [@discord-2026]

So the longer the queue got, the slower it drained. In total, the mailboxes on 15 voice syncers grew to nearly 1 million messages and kept rising. The connection that sends health reports to etcd also went through those supervisors. After the 60-second TTL, instances dropped out of service discovery. [@discord-2026]

Early restarts helped only briefly, because the queues filled again. Recovery came after the team set lower spawn limits and doubled the instances. [@discord-2026]

## The idea in simple words

A **producer** sends work. A **consumer** does it. Sometimes the producer is faster for a while. What then?

You have three choices, and only three:

1. **Buffer.** Store the extra work and do it later.
2. **Drop.** Throw some work away, on purpose.
3. **Slow the producer.** Make the sender wait until the consumer is ready.

The third one is **backpressure**. The slow part pushes back on the fast part. A buffer only buys time. If the producer stays faster on average, the buffer fills, and then you are back to choosing between dropping and slowing.

[Load shedding](/learn/load-shedding-graceful-degradation) is choice 2: drop work early and cheaply. Backpressure is choice 3. You often need both. You can slow your own services, but you cannot slow a user who is tapping a button.

> [!key] **The key idea.** A queue does not add capacity. It only moves the wait. Give every queue a limit, and decide what happens when it is full: the sender waits, or the sender gets an error.

## How it works

### Unbounded queues hide the problem

Many queues have no limit by default. In Python, `asyncio.Queue()` has `maxsize=0`, and that means "the queue size is infinite". [@py-queue] Java's `Executors.newFixedThreadPool` uses "a shared unbounded queue". [@java-executors]

The Java docs say what this means. An unbounded queue smooths out short bursts. But it allows "unbounded work queue growth" when tasks keep arriving faster than they finish. [@java-tpe] As the [Tail latency and queueing](/learn/tail-latency-and-queueing) lesson showed, a long queue also means a long wait for every item in it.

Node.js measured the cost. It compressed a file of about 9 GB with streams, once with backpressure and once without. With backpressure, the process used about 87.81 MB of memory. Without it, about 1.52 GB. [@node-bp]

A **bounded queue** has a fixed size. Java's docs say it "helps prevent resource exhaustion". [@java-tpe] The hard part is the next question: what happens when it is full?

### Wait or reject

When a bounded queue is full, the producer either waits or gets an error. Pick one on purpose.

In asyncio, `await queue.put(item)` waits for a free slot. `put_nowait(item)` raises `QueueFull` at once. [@py-queue] Here is both, in a small sketch:

```python
import asyncio

queue = asyncio.Queue(maxsize=100)  # at most 100 jobs wait (Python 3.10+)

async def producer(jobs):
    for job in jobs:
        await queue.put(job)  # waits while the queue is full

async def worker():
    while True:
        job = await queue.get()
        try:
            await handle(job)
        finally:
            queue.task_done()

def enqueue_from_request(job):  # a handler that must not wait
    try:
        queue.put_nowait(job)
        return 202
    except asyncio.QueueFull:
        return 503  # caller should back off and retry later
```

Waiting is real backpressure: the producer slows to the consumer's speed. Use it inside your own system, such as a batch job.

Rejecting turns the decision into load shedding. Use it at the edge, where callers are users or other teams. Return 503 with `Retry-After`, as in the load shedding lesson.

Java gives you both choices too. The default policy, `AbortPolicy`, throws `RejectedExecutionException`. `CallerRunsPolicy` runs the task on the thread that submitted it. The docs call this "a simple feedback control mechanism" that slows down new submissions. [@java-tpe]

### Pull and credits instead of push

In a push system, the sender decides the rate. Kafka's docs say a push consumer "tends to be overwhelmed" when it is slower than the producer. [@kafka-design] So Kafka consumers pull. A slow consumer "simply falls behind and catches up when it can". [@kafka-design]

A more general form is **credit-based** flow control. The receiver says how much it can take. The sender sends no more than that.

- **Reactive Streams.** The subscriber calls `request(n)` to ask for n more items. [@rs-spec] The publisher must never send more items in total than were requested. [@rs-spec] The goal is that the receiver "is not forced to buffer arbitrary amounts of data". [@rs-spec]
- **HTTP/2.** Each stream and each connection has a **flow-control window**. The receiver opens it with `WINDOW_UPDATE` frames. The RFC calls it "a credit-based scheme". [@rfc9113] Both windows start at 65,535 octets, and only DATA frames count. [@rfc9113]
- **TCP.** Every segment carries a window: how many more bytes the receiver will accept. [@rfc9293] If the window drops to zero, the sender stops and sends small probes, with growing gaps, until it opens. [@rfc9293]

This is **flow control**: one receiver protecting itself from one sender. It is different from congestion control, which protects the network. The [TCP/IP](/learn/tcp-ip-network-stack) lesson covers both.

### How backpressure travels, and where it stops

Backpressure works best when it travels back, hop by hop, to whoever started the work. Step through it. Use Next and Back, or the ← and → keys.

[[diagram:backpressure-hops]]

The chain is only as good as its weakest link. Three things break it:

- **An unbounded buffer anywhere.** The signal stops there. That part just grows. In Discord's outage, the mailboxes kept growing until the team lowered rate limits upstream. [@discord-2026]
- **An async boundary that answers early.** "202 Accepted" ends the caller's wait before the work is done. From then on, only the queue's limit protects you. Reactive Streams exists to carry the signal across exactly these boundaries. [@rs-spec]
- **Fire-and-forget calls.** A call nobody waits for can never slow anybody down.

A durable log is a special case. A Kafka producer does not slow down when a consumer falls behind. The log on disk is the buffer, and the cost shows up as consumer lag. [@kafka-design] Inside the consumer, you can still apply backpressure. `max.poll.records` limits how many records one poll returns. `pause()` stops fetching from chosen partitions until you call `resume()`. [@kafka-consumer] But keep calling `poll()`. If you wait longer than `max.poll.interval.ms`, the consumer leaves its group. [@kafka-consumer]

At the very front, users will not slow down. So the last hop of backpressure becomes a choice: make them wait a little, or reject with a clear error.

## Trade-offs

| Option | Memory safe? | What the producer sees | Use it when |
|---|---|---|---|
| Unbounded buffer | No. It grows until something crashes. | Nothing. Every send succeeds. | Never on purpose. Find these and add limits. |
| Bounded queue, producer waits | Yes. | Slower sends. | Inside your own system: pipelines, batch jobs, workers. |
| Bounded queue, reject when full | Yes. | Fast errors, such as 503. | At the edge, where callers can retry later. |
| Drop oldest or newest item | Yes. | Nothing. Data is lost quietly. | Only the latest value matters, like metrics or presence. |
| Pull or credit-based flow | Yes. | The receiver sets the pace. | Streams, brokers, protocols you control on both sides. |
| Durable log (Kafka) | Yes, on disk, until retention. | Nothing. Lag grows instead. | Consumers may fall behind and catch up later. |

**Simple rule:** every queue gets a limit. Inside your system, let the producer wait. At the edge, reject fast.

## At work: how a senior uses this

- Draw the path of one request and mark every buffer: thread pools, channels, mailboxes, connection pools, broker queues. Ask: **"What is its limit, and what happens when it is full?"**
- Search for default constructors that hide unbounded queues, such as `newFixedThreadPool` or `asyncio.Queue()`. Give each a size from Little's law and your latency target.
- Size a queue from time, not memory. If you can wait 2 seconds and do 100 jobs a second, 200 is the limit.
- Alert on queue length and the age of the oldest item. Also track time spent blocked in `put()`.
- After a migration or a resize, retune every limit that provides backpressure. Discord's limit had not been re-tuned after a migration. [@discord-2026]
- Never let health checks or heartbeats wait behind normal work. Give them their own path.
- When you add an async API that returns 202, write down who protects the queue behind it.

## With AI

### What to ask the agent

> "List every queue, channel, thread pool and buffer in this service. For each, give its size limit, or say 'unbounded', and what happens when it is full."

> "Replace this unbounded asyncio.Queue with a bounded one. Producers inside the batch job should wait. The HTTP handler should return 503 with Retry-After when the queue is full."

> "Write a load test where the consumer is 50% slower than the producer for 10 minutes. Record queue length, memory and p99 latency every second."

### What to check yourself

- No queue uses a default that means "no limit". Read the docs of each constructor the agent used.
- The handler that must stay fast uses a non-waiting call, like `put_nowait` or `offer`, not a blocking `put`.
- A blocking `put` never runs on an event loop thread or while holding a lock.
- The load test runs long enough for the queue to fill. Memory flattens out instead of climbing.
- Rejected work is counted in a metric, so you can see shedding happen.

## Check yourself

## Go deeper

Before this: [Queues vs logs](/learn/queues-vs-logs-delivery-guarantees) and [Tail latency and queueing](/learn/tail-latency-and-queueing). Next to it: [Load shedding](/learn/load-shedding-graceful-degradation) and [Rate limiting](/learn/rate-limiting). For flow control in protocols, see [TCP/IP](/learn/tcp-ip-network-stack) and [HTTP/1.1, HTTP/2, HTTP/3 and QUIC](/learn/http-1-2-3-and-quic).
