---
topic: batching-pipelining
subtitle: Many small calls waste time on fixed costs. Learn how batching and pipelining share those costs, and how much waiting time you pay for it.
status: published
illustrative_story: true
sources:
  - id: redis-pipe
    title: "Redis, “Redis pipelining”, Redis documentation"
    url: https://redis.io/docs/latest/develop/using-commands/pipelining/
    note: Round-trip time, how pipelining sends many commands before reading replies, the cost of read() and write() system calls, and the advice to send batches of about 10k commands.
  - id: kafka-producer
    title: "Apache Kafka, “Producer Configs”, Apache Kafka 4.4 documentation"
    url: https://kafka.apache.org/44/configuration/producer-configs/
    note: The batch.size and linger.ms settings, their defaults, and why the linger.ms default changed from 0 to 5 in Kafka 4.0.
  - id: pg-wal
    title: "PostgreSQL Global Development Group, “28.5. WAL Configuration”, PostgreSQL 18 documentation"
    url: https://www.postgresql.org/docs/current/wal-configuration.html
    note: Group commit, commit_delay and commit_siblings, and how to pick a starting value with pg_test_fsync.
  - id: rfc896
    title: "John Nagle, “Congestion Control in IP/TCP Internetworks”, RFC 896, January 1984"
    url: https://www.rfc-editor.org/rfc/rfc896
    note: The small-packet problem (41-byte packets for 1 byte of data) and the rule now called Nagle's algorithm.
  - id: rfc9293
    title: "Wesley Eddy (ed.), “Transmission Control Protocol (TCP)”, RFC 9293, August 2022"
    url: https://www.rfc-editor.org/rfc/rfc9293.html
    note: Section 3.7.4 on Nagle's algorithm, section 3.8.6.3 on delayed acknowledgements, and Appendix A.3 on how the two interact badly.
  - id: linux-tcp
    title: "Linux man-pages, “tcp(7) - Linux manual page”"
    url: https://man7.org/linux/man-pages/man7/tcp.7.html
    note: The TCP_NODELAY socket option, which turns off Nagle's algorithm.
  - id: rfc9112
    title: "Roy Fielding, Mark Nottingham and Julian Reschke (eds.), “HTTP/1.1”, RFC 9112, June 2022"
    url: https://www.rfc-editor.org/rfc/rfc9112#section-9.3.2
    note: Section 9.3.2 on pipelining, the rule that responses come back in request order, and retries.
  - id: rfc9113
    title: "Martin Thomson and Cory Benfield (eds.), “HTTP/2”, RFC 9113, June 2022"
    url: https://datatracker.ietf.org/doc/html/rfc9113
    note: Why HTTP/1.1 pipelining only partly solved concurrency, and how HTTP/2 streams avoid head-of-line blocking at the HTTP layer.
  - id: mdn-conn
    title: "MDN Web Docs, “Connection management in HTTP/1.x”"
    url: https://developer.mozilla.org/en-US/docs/Web/HTTP/Guides/Connection_management_in_HTTP_1.x
    note: Why browsers do not turn on HTTP pipelining, and how HTTP/2 multiplexing replaced it.
  - id: smart-batching
    title: "Martin Thompson, “Smart Batching”, Mechanical Sympathy blog, 19 October 2011"
    url: https://mechanical-sympathy.blogspot.com/2011/10/smart-batching.html
    note: Batching whatever arrived while the last send was in progress, with no timer, and a worked example with 100 µs writes.
claims_checked:
  - claim: "Kafka producer batch.size defaults to 16384 bytes; linger.ms defaults to 5 ms (changed from 0 in Apache Kafka 4.0); the producer sends when the batch is full or the linger time ends, whichever happens first; linger.ms = 50 would add up to 50 ms of latency without load."
    source: kafka-producer
    note: "Checked on the Kafka 4.4 Producer Configs page, batch.size and linger.ms entries. Quotes are exact. The page also warns broker backpressure can raise the effective linger; the lesson says so."
  - claim: "PostgreSQL groups commits that arrive during a flush even with commit_delay = 0 (the default, in microseconds); commit_delay sleeps so more commit records join one flush; no sleep if fewer than commit_siblings other sessions are in active transactions; start at half the pg_test_fsync time for one 8 kB write."
    source: pg-wal
    note: "Checked on 28.5 WAL Configuration (PostgreSQL 18). It also warns a too-high value can lower throughput."
  - claim: "Nagle's 1984 RFC describes 41-byte packets (1 byte of data, 40 of header) from keyboard traffic and the rule to hold new data while earlier data is unacknowledged."
    source: rfc896
    note: "RFC 896, 6 January 1984. The original rule is unconditional; the full-sized-segment version is RFC 9293 section 3.7.4, cited separately."
  - claim: "TCP SHOULD implement Nagle and MUST let an application disable it per connection; delayed ACKs MUST be under 0.5 s; together they can result in poor application performance for request-response traffic."
    source: rfc9293
    note: "Sections 3.7.4 (SHLD-7, MUST-17), 3.8.6.3 (MUST-40) and Appendix A.3. The header-then-body stall is the lesson's own example and is labelled as such."
  - claim: "At 250 ms RTT a non-pipelined client manages at most 4 requests a second even if the server can do 100k; pipelining pays the RTT once per group, cuts read()/write() calls, reaches about 10x throughput, and Redis queues replies in memory, so send about 10k commands at a time."
    source: redis-pipe
    note: "Checked on the Redis pipelining docs page."
  - claim: "With 100 µs per write and a burst of 10 messages, serial writes finish the last message at 1,000 µs; with smart batching no message takes more than 200 µs."
    source: smart-batching
    note: "Table worst-case column. The 190 µs in the post's text is the worst-case average, not a contradiction."
reviewed_by: ai-factcheck
reviewed_on: 2026-11-08
quiz:
  - q: "Each call to a service costs 1 ms of fixed overhead plus 0.01 ms per item. How long do 1,000 items take in batches of 100?"
    options:
      - "About 1,010 ms."
      - "About 20 ms."
      - "About 2 ms."
    answer: 1
    why: "There are 10 batches. Each costs 1 + 100 × 0.01 = 2 ms. So 10 × 2 = 20 ms. One at a time it would be 1,000 × 1.01 = 1,010 ms."
  - q: "A Kafka producer uses linger.ms = 50. Traffic is very low: one record every few seconds. What happens to each record?"
    options:
      - "It is sent at once, because the batch is never full."
      - "It can wait up to 50 ms before it is sent, with nothing to share the request with."
      - "It waits until batch.size bytes have arrived, which may take minutes."
    answer: 1
    why: "The producer sends when the batch is full or when the linger time ends, whichever comes first. At low load the time limit decides, so each record pays the wait for no gain."
  - q: "A client sends a small request in two writes: header, then body. Nagle's algorithm is on at the client. The server uses delayed acknowledgements. Why can each request stall?"
    options:
      - "Nagle holds the body until the header is acknowledged, and the server delays its acknowledgement, because it has no reply to send yet."
      - "The server's CPU is too slow to read two packets."
      - "TCP drops the second packet on purpose."
    answer: 0
    why: "Each side waits for the other. The stall ends only when the delayed-acknowledgement timer fires. Setting TCP_NODELAY, or sending the request in one write, avoids it."
  - q: "Why did browsers not turn on HTTP/1.1 pipelining, and what replaced it?"
    options:
      - "It was too fast for servers. HTTP/1.0 replaced it."
      - "Responses must come back in order, so one slow response blocks the rest; buggy proxies made it worse. HTTP/2 multiplexing replaced it."
      - "It needed UDP. QUIC replaced it."
    answer: 1
    why: "RFC 9112 makes the server answer in request order, so a slow first response blocks the others. MDN also names buggy proxies, and says HTTP/2 multiplexing superseded it."
  - q: "You pipeline 1,000,000 Redis commands in one go. What is the main risk?"
    options:
      - "Redis runs the commands in a random order."
      - "Redis must queue all the replies in memory until you read them."
      - "Each command needs its own round trip anyway."
    answer: 1
    why: "The Redis docs suggest batches of a reasonable size, such as 10k commands, then read the replies. The speed is nearly the same and the memory stays small."
incidents: []
related: [tail-latency-queueing, latency-estimation, mechanical-sympathy, queues-vs-logs, stream-processing, os-essentials, http-versions, tcp-ip]
---

## What broke

This story is made up, but the shape is common. A team runs a nightly job that copies 2,000,000 product prices into Redis. The job runs in one zone and Redis runs in another.

The code is a simple loop: send one `SET`, wait for the reply, send the next. Each round trip between the zones takes about 0.5 ms.

The job takes 2,000,000 × 0.5 ms = 1,000 seconds, about 17 minutes. Redis is almost idle. The job spends nearly all its time waiting for the network.

An engineer switches to pipelining. She sends all 2,000,000 commands first, then reads the replies. The job is fast now, but Redis memory jumps, because Redis must hold every reply until the client reads it. [@redis-pipe] The final fix sends 10,000 commands at a time, reads those replies, and repeats.

## The idea in simple words

Many operations have a **fixed cost** that does not depend on how much data they carry. The fixed part of a network round trip, a system call, a disk flush or a packet header is the same for 1 item or for 100.

**Batching** means grouping many items into one operation, so they share that fixed cost. **Pipelining** means sending the next request before the reply to the last one arrives, so the round trips overlap.

Both raise throughput: more items per second. Both have a price. With batching, an item may wait for more items to arrive and fill its batch. With pipelining, both sides buffer more data, and one slow item can hold up the rest.

> [!key] **The key idea.** Batching and pipelining spread a fixed cost over many items. You gain throughput and pay with waiting time and memory. Always put a limit on both the size of a batch and how long an item may wait.

## How it works

### The arithmetic of batching

Say each call to a service costs 1 ms of fixed overhead, plus 0.01 ms per item. You have 1,000 items.

- **One at a time:** 1,000 × (1 + 0.01) = 1,010 ms. That is about 990 items a second.
- **Batches of 100:** 10 × (1 + 100 × 0.01) = 10 × 2 = 20 ms. That is 50,000 items a second.

The work per item did not change. You paid the fixed cost 10 times, not 1,000 times.

Now look at the wait. Items arrive at 2,000 a second. A batch of 100 takes 100 / 2,000 = 0.05 s to fill. So the first item in each batch waits about 50 ms before anything is sent.

In [tail latency and queueing](/learn/tail-latency-and-queueing), you saw that a busy server builds long queues. Batching adds waiting time to each item. But it also raises capacity, so the server is less busy and its queue is shorter. Here, sending one at a time handles only about 990 items a second. At 2,000 arriving a second, its queue would grow without end.

### Two knobs: size and time

Every batcher needs two limits. A **size limit** sends the batch when it is full. A **time limit** sends it when the oldest item has waited long enough.

Kafka's producer shows both. It groups records for the same partition into batches. `batch.size` sets the batch size in bytes, with a default of 16384. [@kafka-producer] `linger.ms` is how long the producer waits for more records. It sends when the batch is full or the linger time ends, "whichever happens first". [@kafka-producer]

The default for `linger.ms` changed from 0 to 5 in Kafka 4.0. The docs say "the efficiency gains from larger batches typically result in similar or lower producer latency despite the increased linger." [@kafka-producer] But the docs also warn that a value of 50 "would add up to 50ms of latency to records sent in the absence of load". [@kafka-producer]

In the example above, a 5 ms time limit at 2,000 items a second gives batches of about 2,000 × 0.005 = 10 items. Each batch costs 1 + 10 × 0.01 = 1.1 ms, or 0.11 ms per item. That is about 9 times better than one at a time. At normal load, an item lingers at most about 5 ms. Kafka warns that broker backpressure can make the real wait longer. [@kafka-producer]

### Group commit in databases

A durable commit must reach the disk with [fsync](/glossary#fsync). That flush is a large fixed cost. If several transactions commit at the same moment, one flush can cover all of them. This is **group commit**.

With made-up numbers: one flush takes 2 ms. One commit per flush allows at most 1,000 / 2 = 500 commits a second. If 8 commits share each flush, the same disk allows up to 4,000.

PostgreSQL already groups sessions that need to flush while the previous flush runs. [@pg-wal] The setting `commit_delay` (in microseconds, 0 by default) adds a short sleep before the flush, so more commit records can be added to it. [@pg-wal] It skips the sleep if fewer than `commit_siblings` other sessions are in active transactions. [@pg-wal]

The docs suggest starting at half the flush time that `pg_test_fsync` reports for one 8 kB write. They also warn that a value that is too high can add so much latency that throughput falls. [@pg-wal]

### Nagle's algorithm and TCP_NODELAY

TCP batches too. In 1984, John Nagle described a problem with keyboard traffic. Each typed character went out alone, as a 41-byte packet: 1 byte of data and 40 bytes of headers. [@rfc896]

His fix, now called **Nagle's algorithm**: while earlier data on the connection is still unacknowledged, hold new data. Send it together when the acknowledgement comes. [@rfc896] Today's version holds data only until it can fill a full-sized packet. [@rfc9293] TCP should still implement this rule, and must let an application turn it off per connection. [@rfc9293]

The trouble is the receiver. It may hold back its acknowledgement for a short time, so it sends fewer of them. This is a **delayed acknowledgement**. The delay must be less than 0.5 seconds. [@rfc9293] RFC 9293 says the two features together "can result in poor application performance" for request-response traffic. [@rfc9293]

RFC 9293 gives no example, so here is a common one. Your client sends a request in two small writes: header, then body. The header goes out at once. Nagle holds the body until the header is acknowledged. The server's TCP delays its acknowledgement. The server app cannot reply yet, because it still waits for the body. Nothing moves until the delayed-acknowledgement timer fires.

The usual fix is the `TCP_NODELAY` socket option, which turns Nagle off for one connection. [@linux-tcp] [@rfc9293] A better habit is to build the whole request in memory and send it in one write. That is batching in your own code.

### Pipelining

Pipelining cuts the cost of round trips. CPUs use the same idea inside: an instruction pipeline starts the next instruction before the last one is done. If the round-trip time is 250 ms, a client that waits for each reply can do at most 4 requests a second, even if the server can handle 100,000. [@redis-pipe]

With pipelining, the client sends many commands, then reads all the replies. You pay the round trip once for the group. [@redis-pipe]

Step through it. Use Next and Back, or the ← and → keys.

[[diagram:pipelining-round-trips]]

It also cuts system calls. Without pipelining, Redis does a `read()` and a `write()` per command. With it, one `read()` takes in many commands and one `write()` sends many replies. Redis reports up to 10 times the throughput this way. [@redis-pipe]

Pipelining cannot help when the next command needs the last reply. A read, compute, write sequence must wait. Redis suggests a server-side script for that case. [@redis-pipe]

### HTTP/1.1 pipelining and why it faded

HTTP/1.1 allows pipelining too. But the server must send responses "in the same order that the requests were received". [@rfc9112] So a slow first response blocks every response behind it. This is head-of-line blocking.

Browsers do not turn HTTP pipelining on by default. MDN gives three reasons: buggy proxies, it is hard to implement correctly, and head-of-line blocking. [@mdn-conn] HTTP/2 replaced it with multiplexing. Each request gets its own stream, so a stalled response does not block the others. [@rfc9113] But TCP head-of-line blocking remains: a lost packet still delays every stream on the connection. [@rfc9113] HTTP/3 fixes that; see [HTTP/1.1, HTTP/2, HTTP/3 and QUIC](/learn/http-1-2-3-and-quic).

### Adaptive batching

A fixed time limit makes every item wait, even when the system is idle. Martin Thompson describes a better way, which he calls **smart batching**. [@smart-batching]

One thread sends data as soon as any is available. Items that arrive while a send is in progress form the next batch, up to the buffer size. A lone item goes out alone, with no timer. [@smart-batching]

In his example, writing one buffer to the network and getting it acknowledged takes 100 µs. Then 10 messages arrive at once. If each thread writes its own message, the writes queue up, and the last one finishes after 1,000 µs. With smart batching, no message takes more than 200 µs. [@smart-batching] Batches grow only when load grows. So at low load you get low latency, and at high load you get big batches. Bound the queue, though. Thompson warns that an unbounded queue "could grow out of control". [@smart-batching]

## Trade-offs

| Option | Throughput | Latency | Use it when |
|---|---|---|---|
| One at a time | No. Pays the full fixed cost per item. | Lowest at low load. | Traffic is light, or each step needs the last result. |
| Batch with size and time limits | Yes. Big gains under load. | Adds up to the time limit per item. | Producers, log shippers, bulk writes. |
| Adaptive (smart) batching | Yes. Batches grow with load. | Low at low load, no timer. | You own the sender loop and the downstream is serial. |
| Pipelining | Yes. Pays the round trip once per group. | Low, but one slow reply can block in-order replies. | Many independent commands on one connection. |
| Group commit | Yes. One flush for many commits. | Adds a short wait, if you set a delay. | Many concurrent writers, flush-bound disk. |

## At work: how a senior uses this

- Find the fixed cost first. Ask: **"What do we pay per call, whatever its size?"** Usually it is round trips, system calls, flushes or headers.
- Never batch without a time limit. Check the worst wait an item can have against your latency target.
- Measure at low load too. A time limit that helps at peak adds pure delay at 3 a.m.
- Decide what happens when one item in a batch fails. Return a result per item, and retry only the failed items.
- Plan for a bad item (sometimes called a poison message). One bad record should not block the whole batch forever. Move it to a [dead letter queue](/glossary#dead-letter-queue).
- For request-response protocols over TCP, send each request in one write, or set `TCP_NODELAY`.

## With AI

### What to ask the agent

> "This loop calls Redis once per item. Rewrite it to pipeline in chunks of 10,000 commands, and keep a result for each item."

> "Our Kafka producer sends 200 records a second at night and 20,000 at peak. Estimate the batch size and added latency for linger.ms = 5 and 50. Show the arithmetic."

### What to check yourself

- Every batch has both a size limit and a time limit, and the numbers came from your traffic, not a guess.
- A failure of one item does not lose or silently retry the other items in the batch.
- The new code does not depend on the reply to a command that is still in the same pipeline.

## Check yourself

## Go deeper

- [Tail latency and queueing](/learn/tail-latency-and-queueing)
- [Latency numbers and back-of-envelope maths](/learn/latency-numbers-and-estimation)
- [Mechanical sympathy: CPU caches and memory](/learn/mechanical-sympathy-cpu-memory)
- [OS essentials for backend engineers](/learn/os-essentials-for-backend)
- [HTTP/1.1, HTTP/2, HTTP/3 and QUIC](/learn/http-1-2-3-and-quic)
