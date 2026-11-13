---
topic: exactly-once
subtitle: No network can deliver a message exactly once. You can still make its effect happen once, if the check lives where the effect is saved.
status: published
reviewed_by: ai-factcheck
reviewed_on: 2026-11-13
illustrative_story: false
sources:
  - id: ezypay
    title: "Ezypay Status, “Duplicated Billing in AU Direct Debit/Bank Transactions” (postmortem, May 2023)"
    url: https://status.ezypay.com/incidents/pk2dkcrr0m8c
    note: The official postmortem. A lock failure during a rolling deploy let two batch instances run, and some customers were debited two or four times.
  - id: kleppmann-notes
    title: "Martin Kleppmann, “Distributed Systems” lecture notes, University of Cambridge, Michaelmas term 2021/22"
    url: https://www.cl.cam.ac.uk/teaching/2122/ConcDisSys/dist-sys-notes.pdf
    note: Section 2.1 on the two generals problem, with an online shop example. Section 5.1, slide 90 defines at-most-once, at-least-once and exactly-once as retry plus idempotence or deduplication.
  - id: e2e
    title: "J.H. Saltzer, D.P. Reed and D.D. Clark, “End-to-End Arguments in System Design”, ACM Transactions on Computer Systems 2(4), November 1984, pages 277–288"
    url: https://web.mit.edu/Saltzer/www/publications/endtoend/endtoend.pdf
    note: The original paper. See the sections "Delivery guarantees" and "Duplicate message suppression".
  - id: kafka-design
    title: "Apache Kafka documentation: Design (Kafka 4.3)"
    url: https://kafka.apache.org/43/design/design/
    note: The section on message delivery semantics. The idempotent producer, transactions, read_committed, and what exactly-once needs from outside systems.
  - id: kip-98
    title: "KIP-98 - Exactly Once Delivery and Transactional Messaging"
    url: https://cwiki.apache.org/confluence/display/KAFKA/KIP-98+-+Exactly+Once+Delivery+and+Transactional+Messaging
    note: The design proposal behind Kafka transactions. Producer IDs and sequence numbers, fencing by transactional ID, and committing consumer offsets inside a transaction.
  - id: kafka-producer
    title: "Apache Kafka documentation: Producer Configs (Kafka 4.3)"
    url: https://kafka.apache.org/43/configuration/producer-configs/
    note: enable.idempotence (on by default), transactional.id and transaction.timeout.ms.
  - id: kafka-consumer
    title: "Apache Kafka documentation: Consumer Configs (Kafka 4.3)"
    url: https://kafka.apache.org/43/configuration/consumer-configs/
    note: isolation.level, and why the default read_uncommitted also returns aborted messages.
  - id: flink-2pc
    title: "Piotr Nowojski and Mike Winters, “An Overview of End-to-End Exactly-Once Processing in Apache Flink (with Apache Kafka, too!)”, Apache Flink blog (February 2018)"
    url: https://flink.apache.org/2018/02/28/an-overview-of-end-to-end-exactly-once-processing-in-apache-flink-with-apache-kafka-too/
    note: How TwoPhaseCommitSinkFunction ties a sink's transaction to Flink's checkpoints.
  - id: flip-453
    title: "FLIP-453: Promote Unified Sink API V2 to Public and Deprecate SinkFunction"
    url: https://cwiki.apache.org/confluence/display/FLINK/FLIP-453:+Promote+Unified+Sink+API+V2+to+Public+and+Deprecate+SinkFunction
    note: Flink 1.20 marks TwoPhaseCommitSinkFunction as deprecated and makes the Sink V2 interfaces public.
  - id: pg-insert
    title: "PostgreSQL documentation: INSERT"
    url: https://www.postgresql.org/docs/current/sql-insert.html
    note: ON CONFLICT DO NOTHING, and which rows RETURNING gives back.
  - id: sqs-fifo
    title: "Amazon SQS Developer Guide: Exactly-once processing in Amazon SQS"
    url: https://docs.aws.amazon.com/AWSSimpleQueueService/latest/SQSDeveloperGuide/FIFO-queues-exactly-once-processing.html
    note: FIFO queues drop a retried send within a 5-minute deduplication interval.
claims_checked:
  - claim: "Ezypay: during a rolling deploy, a mishandled exception while taking the distributed lock let two instances of a single-instance batch billing service run. A batch file with duplicated AU direct debits skipped its pre-bank reconciliation; some customers were debited twice, some 4 times; each duplicate was reversed within 24 hours."
    source: ezypay
    note: "Postmortem text checked line by line. Status-page timestamps (first update 16 May 2023, 20:51 AEST, 'processed today, 16th May') support 16 May; the postmortem's 17 May conflicts with its own timeline, so the incident is dated 16 May."
  - claim: "The idempotent producer: the broker assigns each producer an ID and deduplicates by sequence number; on by default unless conflicting settings such as acks=1 are set; holds only within one producer session without a transactional ID."
    source: kafka-design
    note: "Also checked Producer Configs (enable.idempotence default true, disabled by conflicting configs) and KIP-98 ('only guarantee idempotent production within a single producer session')."
  - claim: "Consumers default to isolation.level=read_uncommitted, which returns messages from aborted transactions; exactly-once needs read_committed and enable.auto.commit=false, and exactly-once to other systems 'generally requires cooperation with such systems'."
    source: kafka-consumer
    note: "Default and aborted-message wording from Consumer Configs; the read_committed/auto-commit requirement and the 'cooperation' quote are verbatim in the Design page."
  - claim: "INSERT ... ON CONFLICT DO NOTHING RETURNING returns only rows actually inserted, so a dedupe insert in the same transaction as the effect turns a redelivery into a no-op."
    source: pg-insert
    note: "Run on PostgreSQL 17.12 in Docker: redelivery returns 0 rows, one charge; a concurrent copy blocks until the first commits, then gets 0 rows (1 row if the first rolls back)."
  - claim: "End-to-end argument: duplicates made by the application's own retries look like different messages to the network, so only the application can suppress them; the useful ack is the end-to-end 'I did it'."
    source: e2e
    note: "Sections 'Delivery guarantees' and 'Duplicate message suppression'. Paper is a revised version of a 1981 conference paper, published in ACM TOCS 2(4), Nov 1984, pp. 277-288."
  - claim: "Two generals: no finite number of messages gives either side certainty; exactly-once semantics is retry plus idempotence or deduplication."
    source: kleppmann-notes
    note: "Section 2.1 (slides 23-25) and section 5.1, slide 90."
quiz:
  - q: A consumer saves a charge, then its ack to the broker is lost. What does the broker know?
    options:
      - The charge failed, so it must redeliver.
      - The charge worked, so it must not redeliver.
      - Nothing for sure. It cannot tell a lost ack from a message that was never handled.
    answer: 2
    why: This is the two generals problem. With no ack, the broker sees the same thing either way, so it has to choose between redelivering and risking a loss.
  - q: Your consumer reads from Kafka and calls a card network's API. You turn on Kafka transactions and read_committed. Can the card still be charged twice?
    options:
      - No. Kafka transactions make the whole flow exactly-once.
      - Yes. The transaction covers Kafka writes and offsets, not the API call. A rerun calls the API again.
      - No, as long as enable.idempotence is true.
    answer: 1
    why: Kafka's exactly-once covers reading from and writing to Kafka. An outside system must take part itself, for example with an idempotency key.
  - q: Where should the processed-message-ID insert go?
    options:
      - In the same database transaction as the effect it protects.
      - In Redis, checked before the work starts.
      - In a separate transaction that commits after the work.
    answer: 0
    why: Only one transaction makes "seen" and "done" true together. Any gap between them lets a crash skip the work or repeat it.
  - q: You delete processed message IDs after 7 days. What risk does that create?
    options:
      - None. Old messages are never delivered again.
      - A message redelivered or replayed after 7 days is not recognised and runs again.
      - The table grows without limit.
    answer: 1
    why: A dedupe store only protects you for as long as it remembers. Keep IDs longer than any retry or replay can reach.
incidents: [ezypay-2023-05-duplicate-direct-debits]
related: [queues-vs-logs, idempotency-retries, sagas-outbox-cdc, stream-processing, webhooks, why-distributed-hard, distributed-transactions]
---

## What broke

In May 2023, the payments company Ezypay processed some customers' regular payments twice. A few were debited four times. Only Australian bank direct debits were affected. [@ezypay]

Ezypay's batch billing services must run as a single instance. Before each batch, the service takes a lock, so no other copy can work on the same batch. [@ezypay]

A scheduled deploy started a new instance next to the old one. The lock code hit a particular exception and handled it wrongly. For a short time, two instances ran. [@ezypay]

One batch failed, because both instances tried to write its file at once. The team fixed that one. But an earlier batch had already written a complete file, with duplicated debits in it. Its usual check before going to the bank was missed. A later reconciliation found the problem. [@ezypay]

Ezypay reversed every duplicate within 24 hours. Its action items include locks that survive every failure and alerts for duplicate payments. [@ezypay]

Notice where the protection was. A lock tried to make sure the work *started* once. The one check at the end, a reconciliation of the file, was skipped that day. Nothing in the data itself stopped a second debit for the same customer and period.

## The idea in simple words

You already know the three delivery promises from [queues vs logs](/learn/queues-vs-logs-delivery-guarantees). This lesson is about why the third one, exactly-once delivery, cannot be built from messages alone.

The reason is old. In the two generals problem, two sides talk over a link that can lose messages. Neither can ever be certain the other got the last message, however many acknowledgements they send. [@kleppmann-notes]

So a sender that hears nothing has two choices. It can stop, and risk losing the message: at-most-once. Or it can retry, and risk doing the work twice: at-least-once. [@kleppmann-notes]

What you can build is **effectively-once processing**. You accept at-least-once delivery. Then you make the effect idempotent, or you deduplicate it. The work may run twice, but the effect is the same as if it ran once. Kleppmann's notes call this exactly-once semantics. [@kleppmann-notes]

> [!key] **The key idea.** Delivery is at-least-once. Processing becomes effectively-once when the place that saves the effect also rejects the repeat.

## How it works

Step through a lost ack. Use Next and Back, or the ← and → keys.

[[diagram:ack-lost-dedupe]]

### The ack lost after the work

Look at frame 3. The consumer did the work and committed. Then the ack was lost, or the consumer crashed before it sent it.

The broker now sees nothing. That looks the same as "the consumer died before it started". No timeout can tell the two apart. If the broker wants no message lost, it must redeliver. A Kafka producer that hits a network error has the same problem: it cannot tell if the write succeeded. [@kafka-design]

### A dedupe table in the same transaction

The fix is a table of message IDs you have handled. Insert the ID in the *same* transaction as the effect. Here it is in PostgreSQL:

```sql
CREATE TABLE processed_messages (
  consumer     text        NOT NULL,
  message_id   text        NOT NULL,
  processed_at timestamptz NOT NULL DEFAULT now(),
  PRIMARY KEY (consumer, message_id)
);

BEGIN;
INSERT INTO processed_messages (consumer, message_id)
VALUES ('billing', 'm42')
ON CONFLICT DO NOTHING
RETURNING message_id;
-- 0 rows back: a copy already ran. ROLLBACK, then ack.
-- 1 row back: do the work in this same transaction.
INSERT INTO charges (order_id, amount_paise) VALUES (7, 149900);
COMMIT;
-- Only now ack the message or commit the offset.
```

`ON CONFLICT DO NOTHING` skips a row that would break the primary key, with no error. `RETURNING` gives back only rows that were really inserted. [@pg-insert] So your code knows at once if this is a repeat. If two copies arrive at the same moment, the second insert waits for the first transaction, then gets 0 rows.

Order matters. The dedupe row and the charge commit together, or neither does. The ack comes last. A crash at any point leaves you either "not done, will retry" or "done, will skip".

Two common mistakes break this. One is checking a separate store, like Redis, before the work. A crash between the work and the "seen" write lets the next copy run again. The other is writing "seen" first, in its own transaction. A crash after it means the work is skipped forever. Kleppmann's notes add that dedupe records must be in stable storage, so they survive a crash. [@kleppmann-notes]

The effect may live in another company's system, like a card network. Then you cannot share a transaction with it. Pass the message ID as an [idempotency key](/learn/idempotency-retries-timeouts), so their side does the dedupe.

### How long to keep the IDs

The table grows forever unless you delete old rows. Most teams add a TTL, such as "delete IDs older than 30 days".

That TTL is a risk. Any copy that arrives after its ID is gone will run again. Copies can come late: a Kafka consumer rewound to an old offset, a replayed dead-letter queue, a partner who resends last month's file. Set the TTL longer than the longest replay you allow. Write that number down.

Short windows exist elsewhere too. SQS FIFO queues drop a retried send only within a 5-minute deduplication interval. [@sqs-fifo] That stops producer retries, not a consumer that runs a message twice.

### What Kafka's exactly-once covers

Kafka gives you two tools. [@kafka-design]

The **idempotent producer** stops duplicates from producer retries. The broker gives each producer an ID and drops repeats by sequence number. [@kafka-design] It is on by default (`enable.idempotence=true`). But it turns itself off if you set a conflicting option, such as `acks=1`, unless you set it to true yourself. [@kafka-producer] But without a transactional ID, this holds only within one producer session. [@kip-98]

**Transactions** let one producer write to many partitions as one unit. For a read-process-write job, the producer also sends the consumer's offsets inside the transaction. The input then counts as consumed only if the transaction commits. [@kip-98] You set `transactional.id` on the producer. When a restarted instance starts with the same ID, Kafka fences off the old one, so a "zombie" cannot finish its transaction. [@kip-98]

Readers must opt in. Set `isolation.level=read_committed` and turn off auto-commit. [@kafka-design] The default, `read_uncommitted`, returns every message, even from aborted transactions. [@kafka-consumer]

Now the limit. All of this covers Kafka in, Kafka out. If your code also sends an email or calls a payment API, a retried transaction calls it again. Kafka's docs say exactly-once delivery to other systems "generally requires cooperation with such systems". [@kafka-design]

### Flink: checkpoints plus two-phase commit

You met Flink's checkpoints in [stream processing](/learn/stream-processing). They make Flink's own state exactly-once. Since Flink 1.4.0, `TwoPhaseCommitSinkFunction` stretches this to some outside systems. [@flink-2pc] Flink 1.20 deprecates that class in favour of the Sink V2 API. [@flip-453] The two-phase idea stays the same.

When a checkpoint starts, the sink pre-commits its open transaction. When the checkpoint completes, Flink tells the sink to commit. On a failure, the job goes back to the last good checkpoint. [@flink-2pc] The sink must support real transactions, as Kafka does. After a restore, Flink may commit the same transaction again, so the commit must be idempotent. [@flink-2pc]

### The end-to-end argument

Why can't the broker just do the dedupe for you? Saltzer, Reed and Clark answered this in 1981. The journal version of their paper is from 1984. [@e2e]

Their end-to-end argument says some functions can be done "completely and correctly" only with the help of the application at the end points. Lower layers can offer a partial version as a speed-up. [@e2e]

Duplicate suppression is one of their examples. Even if the network removes its own duplicates, the application makes new ones in its own retries. The network sees those as different messages. Only the application can spot them. [@e2e]

They make the same point about acks. A delivery ack says the message arrived. What you want to know is whether the target "did it". [@e2e] Ezypay relied on a lock at the start of the job. By the end-to-end argument, the check that matters belongs where the debit is saved.

## Trade-offs

| Option | Duplicate effects? | Lost messages? | Cost | Use it when |
|---|---|---|---|---|
| At-most-once | No. | Yes. | Cheapest. | Metrics or logs, where losing a few is fine. |
| At-least-once, no dedupe | Yes, unless the write is idempotent. | No. | Cheap. | The write is idempotent by nature, like "set status to shipped". |
| At-least-once + dedupe table in the same transaction | No. | No. | One table, a TTL, and the effect must be in that database. | Most consumers that write to a database. |
| Idempotency key to an outside API | No, if the API keeps keys long enough. | No. | You depend on the other side's rules. | Payments, emails and other calls you cannot put in your transaction. |
| Kafka transactions | No, for Kafka outputs. | No. | Readers see output only after the commit, and must use read_committed. | Kafka in, Kafka out, for example Kafka Streams. |
| Flink two-phase commit sink | No, for that sink. | No. | Output is visible only after each checkpoint. | The sink supports transactions, and you need Flink's state. |

**Simple rule:** assume at-least-once everywhere. Put the dedupe where the effect is saved, and nowhere else is enough.

## At work: how a senior uses this

- When someone says "exactly-once", ask: **"Exactly-once of what, between which two points?"** Delivery, Kafka writes and a card charge are different answers.
- Give every message a stable ID at the source. A new ID on each retry makes dedupe impossible.
- Read the transaction boundary in code review. The dedupe insert and the effect must be in one `BEGIN … COMMIT`, and the ack must come after it.
- Write the dedupe TTL next to the longest replay you allow. Then check that the TTL is longer.
- For batch jobs, add a unique key on the business fact, such as one debit per customer per period. A lock is a helper, not the guarantee.
- Keep [reconciliation](/learn/reconciliation) as the last line. Ezypay's later reconciliation is what caught its duplicates. [@ezypay]
- Alert on dedupe hits. A sudden rise means a retry storm, a replay, or two instances running.

## With AI

### What to ask the agent

> "Here is our Kafka consumer that writes to Postgres. Add a processed_messages table, and insert the message ID in the same transaction as the write, with ON CONFLICT DO NOTHING. Commit the offset only after the transaction commits."

> "List every side effect in this consumer that is outside our database: emails, HTTP calls, other queues. For each one, say how a redelivered message would repeat it, and propose an idempotency key."

> "Write a test that kills the consumer after the database commit but before the offset commit. Restart it and assert there is exactly one charge row."

### What to check yourself

- The dedupe insert and the effect share one transaction. A "check, then do" in two steps lets two copies through.
- The message ID comes from the producer, not from the consumer's clock or a random value made on receipt.
- The agent did not claim Kafka transactions cover an HTTP call. They don't.
- Consumers of transactional topics set `read_committed`. The default reads aborted data. [@kafka-consumer]
- The cleanup job for old IDs uses a TTL longer than your Kafka retention or replay window.

## Check yourself

## Go deeper

Before this: [Queues vs logs](/learn/queues-vs-logs-delivery-guarantees) and [Idempotency, retries and timeouts](/learn/idempotency-retries-timeouts). Related: [Sagas, the outbox pattern and CDC](/learn/sagas-outbox-and-cdc) for the inbox on the consumer side, [Stream processing](/learn/stream-processing) for checkpoints, and [Webhooks and async APIs](/learn/webhooks-and-async-apis) for dedupe across companies. [Why distributed systems are hard](/learn/why-distributed-systems-are-hard) covers lost replies in general. [Distributed transactions and two-phase commit](/learn/distributed-transactions-two-phase-commit) shows the other way to get all-or-nothing across systems. Coming later: Kafka internals.
