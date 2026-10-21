---
topic: queues-vs-logs
subtitle: A queue forgets a message once it is done. A log keeps it. Know which one you have, and what "delivered once" really means.
status: published
illustrative_story: true
sources:
  - id: ddia
    title: "Martin Kleppmann, Designing Data-Intensive Applications (O'Reilly)"
    where: "1st edition (2017): chapter 11, Stream Processing (message brokers, acknowledgements and redelivery, log-based message brokers, consumer offsets, replaying old messages)."
  - id: kafka-design
    title: "Apache Kafka documentation: Design (Kafka 4.3)"
    url: https://kafka.apache.org/43/design/design/
    note: Sections on the consumer position, push vs pull, and message delivery semantics. Defines at-most-once, at-least-once and exactly-once.
  - id: kafka-intro
    title: "Apache Kafka documentation: Introduction (Kafka 4.3)"
    url: https://kafka.apache.org/43/getting-started/introduction/
    note: Topics, partitions, keys, ordering within a partition, and retention.
  - id: sqs-vis
    title: "Amazon SQS Developer Guide: Visibility timeout"
    url: https://docs.aws.amazon.com/AWSSimpleQueueService/latest/SQSDeveloperGuide/sqs-visibility-timeout.html
    note: What happens to a message after a worker receives it, the 30-second default, and the 12-hour limit.
  - id: sqs-std
    title: "Amazon SQS Developer Guide: Standard queues"
    url: https://docs.aws.amazon.com/AWSSimpleQueueService/latest/SQSDeveloperGuide/standard-queues.html
    note: At-least-once delivery, possible duplicates and best-effort order.
  - id: sqs-fifo
    title: "Amazon SQS Developer Guide: Exactly-once processing in FIFO queues"
    url: https://docs.aws.amazon.com/AWSSimpleQueueService/latest/SQSDeveloperGuide/FIFO-queues-exactly-once-processing.html
    note: What FIFO deduplication covers, and the 5-minute window.
  - id: sqs-dlq
    title: "Amazon SQS Developer Guide: Using dead-letter queues"
    url: https://docs.aws.amazon.com/AWSSimpleQueueService/latest/SQSDeveloperGuide/sqs-dead-letter-queues.html
    note: maxReceiveCount, retention advice, and a warning about FIFO order.
  - id: rabbit-ack
    title: "RabbitMQ documentation: Consumer Acknowledgements and Publisher Confirms"
    url: https://www.rabbitmq.com/docs/confirms
    note: Automatic vs manual acks, redelivery after a crash, and prefetch.
reviewed_by: ai-factcheck
reviewed_on: 2026-10-21
claims_checked:
  - claim: "An SQS message that is received stays in the queue, hidden for the visibility timeout (30 seconds by default). If not deleted in time, it becomes visible again. Extensions are capped at 12 hours from the first receive."
    source: sqs-vis
    note: "Visibility timeout page: default 30 seconds; message becomes visible again and can be retrieved by another consumer; heartbeat with ChangeMessageVisibility recommended; 12-hour maximum from first receive, not reset by extending."
  - claim: "Saving the position before processing gives at-most-once; processing then saving gives at-least-once; Kafka gives at-least-once by default."
    source: kafka-design
    note: "Message delivery semantics section: definitions match; 'Kafka guarantees at-least-once delivery by default.' Quiz 2 answer (at-most-once) checked against this."
  - claim: "Kafka exactly-once covers reading from and writing to Kafka topics with transactions; for outside systems it 'generally requires cooperation', e.g. storing the offset with the output."
    source: kafka-design
    note: "Design page: transactional producer + read_committed consumer, offsets committed in the same transaction; quote 'generally requires cooperation' is verbatim. Reworded to Kafka's own scope (Kafka topics in and out, e.g. Kafka Streams)."
  - claim: "Events with the same key go to the same partition, and consumers read a partition in the order it was written; each partition is read by one consumer per group at a time."
    source: kafka-intro
    note: "Introduction page confirms key-to-partition and per-partition order; design page confirms one consumer per group per partition. Neither states cross-partition order explicitly, so that line was reworded as advice."
  - claim: "SQS FIFO 'exactly-once processing' means no duplicates are added to the queue for SendMessage retries within the 5-minute deduplication interval."
    source: sqs-fifo
    note: "FIFO exactly-once page: scope is send-side deduplication only; nothing about consumer crashes, so the lesson's caveat is correct."
  - claim: "maxReceiveCount is how many times a message can be received before moving to the dead-letter queue; DLQ retention should exceed the source queue's; a DLQ breaks strict FIFO order."
    source: sqs-dlq
    note: "DLQ page: all three confirmed. Retention advice applies to standard queues (FIFO resets the enqueue time), so the lesson now says 'On a standard queue'."
  - claim: "RabbitMQ requeues unacked deliveries when a channel or connection closes and sets redeliver=true; automatic acks are 'unsafe'; prefetch caps unacked deliveries."
    source: rabbit-ack
    note: "Confirms page: all confirmed. Wording changed from 'connection' to 'channel or connection' and 'a crash loses' to 'a crash can lose'."
quiz:
  - q: An SQS worker needs 50 seconds for a job. The visibility timeout is 30 seconds. What happens?
    options:
      - SQS waits for the worker, because it is still busy.
      - The message becomes visible again after 30 seconds, and another worker can run the same job.
      - SQS moves the message to the dead-letter queue.
    answer: 1
    why: SQS does not know the worker is busy. If the message is not deleted before the timeout, it comes back. Extend the timeout while you work.
  - q: A Kafka consumer saves its offset first, then processes the messages. It crashes in between. What delivery do you get?
    options:
      - At-most-once. The messages it did not finish are skipped.
      - At-least-once. The messages run again.
      - Exactly-once.
    answer: 0
    why: The saved offset is already past those messages, so the next consumer starts after them. The Kafka docs call this at-most-once.
  - q: Your consumer reads from Kafka and charges a card with an outside payment API. How do you get "exactly once" in practice?
    options:
      - Turn on Kafka transactions. They cover the payment API too.
      - Use at-least-once delivery and make the charge idempotent, for example with an idempotency key.
      - Commit the offset before calling the API.
    answer: 1
    why: Kafka transactions cover writes inside Kafka. An outside system sees retries, so it must ignore duplicates itself.
  - q: Events for one bank account must be handled in order. You use Kafka. What do you do?
    options:
      - Use the account ID as the message key, so all its events go to one partition.
      - Use one consumer group per account.
      - Nothing. Kafka keeps order across the whole topic.
    answer: 0
    why: Kafka keeps order only inside a partition. Events with the same key go to the same partition.
  - q: What does maxReceiveCount do on an SQS queue with a dead-letter queue?
    options:
      - It limits how many workers can read the queue.
      - It sets how many times a message can be received before SQS moves it to the dead-letter queue.
      - It sets how many messages one receive call returns.
    answer: 1
    why: After that many receives without a delete, SQS gives up on the message and moves it aside, so it stops coming back forever.
incidents: []
related: [idempotency-retries, backpressure, kafka-internals, stream-processing, sagas-outbox-cdc]
---

## What broke

A billing service puts one message on an Amazon SQS queue for each invoice. A worker takes the message, builds a PDF, emails it to the customer, and deletes the message.

For most customers this takes 5 seconds. For a few large customers, the PDF has thousands of lines and takes 50 seconds. The queue uses the default visibility timeout of 30 seconds.

At second 30, the message for a large customer becomes visible again. A second worker takes it and starts the same invoice. At second 50, the first worker sends the email and deletes the message. At second 80, the second worker sends the same email again.

The biggest customers, the ones the company cares about most, get every invoice two or three times. Their finance teams start asking if they were charged twice. Nothing in the logs shows an error. Each worker did exactly what it was told.

## The idea in simple words

Both queues and logs let one service hand work to another without waiting. The sender is called the producer. The service that does the work is the consumer. A server in the middle, the broker, holds the messages.

A **queue**, like SQS or RabbitMQ, hands each message to one consumer. When the consumer says "done", the broker deletes the message. Many workers can share the load. [@ddia] [@rabbit-ack]

A **log**, like Kafka, is a file you only add to. Each message gets a number, its offset. Consumers do not delete anything. Each consumer just remembers how far it has read. Messages stay until a time or size limit you set, so you can read them again later. [@kafka-intro] [@kafka-design]

> [!key] **The key idea.** In a queue, "done" deletes the message. In a log, "done" moves a bookmark. Either way, a crash between doing the work and saying "done" means the work runs again.

## How it works

Step through a worker crash in both systems. Use Next and Back, or the ← and → keys.

[[diagram:queue-vs-log-crash]]

### The queue: acks and visibility timeout

In SQS, a received message is not removed. It is hidden from other consumers for the visibility timeout, 30 seconds by default. If you do not delete it in time, it becomes visible again, and any worker can receive it. [@sqs-vis]

That is the invoice bug. The fix is to set the timeout above your slowest normal job. For long jobs, call `ChangeMessageVisibility` from time to time, like a heartbeat, to keep the message hidden. The total is capped at 12 hours from the first receive. [@sqs-vis]

RabbitMQ works in a similar way with acks. With manual acks, if a consumer's channel or connection closes before it acks, RabbitMQ puts the message back in the queue. It marks it as redelivered. [@rabbit-ack] With automatic acks, RabbitMQ counts a message as delivered as soon as it sends it. The docs call this mode unsafe, because a crash can lose the message. [@rabbit-ack]

### The log: partitions, offsets and consumer groups

A Kafka topic is split into partitions. Messages with the same key, such as a customer ID, always go to the same partition. Inside one partition, every consumer reads messages in the order they were written. [@kafka-intro] Do not count on any order across partitions.

A consumer's position in a partition is a single number: the offset of the next message to read. The consumer commits this number to save its progress. It can also rewind to an old offset and read again, for example after you fix a bug. [@kafka-design]

Consumers that share the work join a consumer group. Each partition is read by exactly one consumer in the group at a time. [@kafka-design] So a topic with 6 partitions can keep at most 6 consumers busy in one group. Other groups, like an analytics job, read the same messages with their own offsets.

### Three delivery guarantees

The Kafka docs give three meanings. [@kafka-design]

- **At-most-once.** Messages may be lost, but never run twice. You get this if you save your position *before* you do the work.
- **At-least-once.** Messages are never lost, but may run twice. You get this if you do the work, *then* save your position. Kafka gives this by default.
- **Exactly-once.** Each message is processed once and only once.

SQS standard queues are at-least-once too. AWS says more than one copy of a message might be delivered, and order is only best effort. [@sqs-std]

### Why "exactly once" usually means "at least once, plus idempotency"

Your work and your "done" are two separate steps. A crash can always fall between them. So the broker must either skip the message or send it again.

Kafka gives exactly-once when you read from Kafka topics and write to Kafka topics, for example in Kafka Streams. It uses transactions, so the output and the new offset are saved together. But for an outside system, like a database or an email API, the docs say this "generally requires cooperation" from that system. One way is to store the offset in the same database as the result, in one transaction. [@kafka-design]

SQS FIFO queues promise "exactly-once processing". Read the details. It means SQS will not add a duplicate if you retry a send within a 5-minute window. [@sqs-fifo] It cannot stop your worker from crashing after it emails and before it deletes.

The practical answer: accept at-least-once, and make the handler idempotent. Save a message ID or an idempotency key with the result. Skip any message you have already done. The lesson on [idempotency, retries and timeouts](/learn/idempotency-retries-timeouts) shows how.

### Dead-letter queues

Some messages fail every time, for example because of bad data. Without a limit, they come back forever. In SQS you set `maxReceiveCount`. After that many receives without a delete, the message moves to a dead-letter queue, where you can inspect it. [@sqs-dlq]

On a standard queue, a moved message keeps its first enqueue time. So set the dead-letter queue's retention longer than the main queue's, or messages may expire before you look. And on a FIFO queue, moving one message aside breaks the strict order of the rest. [@sqs-dlq]

### Backpressure

If producers are faster than consumers, the backlog grows. Kafka consumers pull messages, so a slow consumer just falls behind and catches up later. It is not flooded. [@kafka-design] RabbitMQ's prefetch setting limits how many unacked messages one consumer can hold. [@rabbit-ack] Watch the backlog, or consumer lag in Kafka, and alert when it keeps growing. See [backpressure and flow control](/learn/backpressure-and-flow-control).

## Trade-offs

| Option | Replay old messages? | Order | Best for |
|---|---|---|---|
| Queue (SQS standard, RabbitMQ) | No. A message is deleted after the ack. | Best effort only. | Jobs: send email, resize an image. Add workers to go faster. |
| SQS FIFO queue | No. | Yes, inside a message group. | Jobs where order matters per customer or per order, at lower throughput. |
| Log (Kafka) | Yes, until retention ends. | Yes, inside a partition. | Events that many teams read, rebuilding data, audit trails, stream processing. |
| At-most-once | No retry. | n/a | Metrics or logs where losing a few is fine. |
| At-least-once + idempotent handler | Yes, it runs again. | n/a | Almost everything else, including payments. |

**Rule of thumb:** use a queue for "please do this job". Use a log for "this happened", when more than one reader needs it or you may want to replay. Either way, design for at-least-once.

## At work: how a senior uses this

- For every consumer, ask: **"What happens if this message runs twice?"** If the answer is "a customer is charged twice", fix the handler before launch.
- Set the visibility timeout from the slowest normal job, not the average. Add a heartbeat for long jobs.
- Pick the Kafka key on purpose. It decides order and load. A hot key fills one partition while the others sit idle.
- Count partitions before you scale consumers. More consumers than partitions in one group means some sit idle.
- Give every queue a dead-letter queue and an alert on it. A dead-letter queue nobody watches is lost data.
- Write down the guarantee for each topic in its README: at-least-once, the key, and where order holds.

## With AI

### What to ask the agent

> "Here is our SQS consumer. Find every point where a crash would cause a message to run twice or be lost. Then make the handler idempotent using the message ID."

> "Our longest job takes up to 3 minutes. Add a heartbeat that extends the SQS visibility timeout every 60 seconds while the job runs, and stops when it ends."

> "Write a test that kills our Kafka consumer after it processes a message but before it commits the offset. Assert that the restarted consumer does not create a duplicate row."

### What to check yourself

- Offsets are committed, or messages deleted, only after the work is saved. Look for auto-commit settings that commit on a timer.
- The idempotency check and the result are saved in one transaction. A check in one step and a write in another can still race.
- The test really crashes the process between the work and the ack. A clean shutdown proves nothing.
- The agent did not "fix" duplicates by turning off retries. That only swaps duplicates for lost messages.
- Ordering code does not assume order across partitions or message groups.

## Check yourself

## Go deeper

Before this: [Idempotency, retries and timeouts](/learn/idempotency-retries-timeouts). Next: [Kafka internals](/learn/kafka-internals), and [sagas, the outbox pattern and CDC](/learn/sagas-outbox-and-cdc) for writing to a database and a queue together.
