---
topic: tail-latency-queueing
subtitle: The average hides your slowest users. Learn why the slow tail grows with fan-out and with busy queues, and what seniors do about it.
status: published
reviewed_by: ai-factcheck
reviewed_on: 2026-10-21
illustrative_story: true
sources:
  - id: tail
    title: "Jeffrey Dean and Luiz André Barroso, “The Tail at Scale”, Communications of the ACM 56(2), February 2013"
    url: https://www.barroso.org/publications/TheTailAtScale.pdf
    note: Why rare slowness becomes common with fan-out, Google's measurements, and hedged and tied requests.
  - id: sre-slo
    title: "Chris Jones, John Wilkes and Niall Murphy, with Cody Smith, “Service Level Objectives”, Site Reliability Engineering (Google, O'Reilly 2016), chapter 4"
    url: https://sre.google/sre-book/service-level-objectives/
    note: Why to think of latency as a distribution, and why an average hides the tail.
  - id: little
    title: "John D. C. Little, “Little's Law as Viewed on Its 50th Anniversary”, Operations Research 59(3), 2011"
    url: https://people.cs.umass.edu/~emery/classes/cmpsci691st/readings/OS/Littles-Law-50-Years-Later.pdf
    note: L = λW explained by its author, with a server load test where queues explode.
  - id: mm1
    title: "MIT OpenCourseWare, “M/M/1 Queue”, course 2.854 Introduction to Manufacturing Systems, Fall 2016"
    url: https://ocw.mit.edu/courses/2-854-introduction-to-manufacturing-systems-fall-2016/927056a1af54772a587fd84ad4951e71_MIT2_854F16_Mm1Queue.pdf
    note: The two-page formula sheet for the simplest queue, with utilization and time in system.
  - id: sre-cascade
    title: "Mike Ulrich, “Addressing Cascading Failures”, Site Reliability Engineering (Google, O'Reilly 2016), chapter 22"
    url: https://sre.google/sre-book/addressing-cascading-failures/
    note: Queue sizes, load shedding, deadlines and their propagation, and retry budgets.
  - id: sre-overload
    title: "Alejandro Forero Cuervo, “Handling Overload”, Site Reliability Engineering (Google, O'Reilly 2016), chapter 21"
    url: https://sre.google/sre-book/handling-overload/
    note: Per-customer limits, request criticality and client-side throttling.
  - id: aws-shed
    title: "Amazon Builders' Library, “Using load shedding to avoid overload”, 2019"
    url: https://builder.aws.com/content/3Eun1EEyX6p2e3VYNyRLSJzLuMV/using-load-shedding-to-avoid-overload
    note: Goodput, timeouts that meet rising latency, and dropping work that nobody waits for.
claims_checked:
  - claim: "With 10 ms typical latency and a 1 s p99 per server, fanning out to 100 servers makes 63% of user requests take over 1 s; at 1 in 10,000 and 2,000 servers, almost 1 in 5."
    source: tail
    note: "Matches the paper's hypothetical example and figure (0.63, 0.18). Recomputed: 1 − 0.99^100 = 0.634; 1 − 0.9999^2000 = 0.181."
  - claim: "Measured p99: one random leaf 10 ms, 95% of leaves 70 ms, all leaves 140 ms; the slowest 5% cause half the p99."
    source: tail
    note: Matches Table 1 and the text exactly.
  - claim: "Hedging after the p95 limits extra load to about 5%; a BigTable test (1,000 keys, 100 servers) with a 10 ms hedge cut p99.9 from 1,800 ms to 74 ms with 2% more requests."
    source: tail
    note: Matches the 'Hedged requests' section word for word in numbers.
  - claim: "Little's law L = λW holds independent of queue discipline (FIFO, LIFO); in a load test the queue grew about linearly, then steeply, and past about 18 requests a second further requests were dumped."
    source: little
    note: "Matches sections 2 and 4.1. The quiz example 2,000 × 0.05 s = 100 is correct."
  - claim: "M/M/1 time in system W = 1/(μ − λ), so with a 10 ms service time: 20 ms at 50%, 200 ms at 95% utilization."
    source: mm1
    note: "W = 1/(μ − λ) = (1/μ)/(1 − ρ). Recomputed the full table: 20, 50, 100, 200, 1,000 ms."
  - claim: "Small queues (50% or less of thread pool size), deadline propagation, randomized exponential backoff and a retry budget such as 60 retries a minute per process."
    source: sre-cascade
    note: All present in chapter 22.
  - claim: "Goodput is throughput handled without errors and fast enough to use; if median latency equals the client timeout, half of requests time out."
    source: aws-shed
    note: "The article defines goodput; it does not say Amazon tracks it as a metric, so the wording was changed."
quiz:
  - q: A request calls 100 servers in parallel and waits for all of them. Each server is slow on 1% of calls. About how often is the whole request slow?
    options:
      - About 1% of the time.
      - About 10% of the time.
      - About 63% of the time.
    answer: 2
    why: The chance that all 100 are fast is 0.99^100 ≈ 0.366. So about 1 − 0.366 ≈ 63% of requests wait for at least one slow server.
  - q: A service handles 2,000 requests a second. Each request spends 50 ms inside it on average. On average, how many requests are inside at once?
    options:
      - "40"
      - "100"
      - "2,000"
    answer: 1
    why: "Little's law: L = λW = 2,000 × 0.05 s = 100."
  - q: In a simple queue model, a server with a 10 ms service time goes from 50% to 95% busy. What happens to the average time per request?
    options:
      - It almost doubles, from 10 ms to 19 ms.
      - It grows about 10 times, from 20 ms to 200 ms.
      - It stays the same until 100%.
    answer: 1
    why: Time in system is service time / (1 − utilization). That is 10 / 0.5 = 20 ms at 50%, and 10 / 0.05 = 200 ms at 95%.
  - q: What is a hedged request?
    options:
      - Sending the same request again after it fails.
      - Sending a backup copy to another replica if the first has not answered after a short delay, and using the first answer.
      - Sending every request to two replicas at the same time, always.
    answer: 1
    why: Waiting for a short delay first, such as the 95th percentile, keeps the extra load small.
  - q: Your dashboard shows an average latency of 40 ms. Why is that not enough?
    options:
      - Averages are always wrong.
      - A few very slow requests can hide inside a low average. You need p99 or p99.9 to see the users who wait.
      - Latency should be measured in seconds.
    answer: 1
    why: Most requests can be fast while a long tail is much slower. A high percentile shows how slow the slowest requests are. For example, only 1 in 100 requests is slower than p99.
incidents: []
cases: [google-tail-at-scale, amazon-queue-backlogs, netflix-adaptive-concurrency]
related: [latency-estimation, load-shedding, backpressure, backoff-jitter, fan-out, load-testing, observability-slos, gc-tuning]
---

## What broke

This story is made up, but it is common. A shop's search page sends each query to 100 index servers at once. It merges their answers and shows the results. Each server answers in about 10 ms.

The team is proud of the dashboard. Every server shows an average latency near 20 ms. But users say search "often hangs for a second". The team cannot find a slow server, because no single server looks slow.

The cause is small. Every few seconds, each server pauses for garbage collection. About 1 call in 100 to any server takes a full second. That looks rare on one server.

But every search waits for all 100 servers. So most searches hit at least one slow server. The rare problem on each server became the normal case for users.

## The idea in simple words

**Latency** is how long a request takes. It is never one number. Some requests are fast and a few are very slow. The slow end of this spread is called the **tail**.

A **percentile** describes the spread. The p50 (median) is the typical request. The p99 is the time that 99% of requests are faster than. The p999 (p99.9) is the time that 999 in 1,000 requests are faster than.

Google's SRE book says latency is better seen as a distribution than as an average. A simple average "can obscure these tail latencies". In one of its examples, a typical request takes about 50 ms, but 5% of requests are 20 times slower. [@sre-slo]

Two forces make the tail worse as you grow:

1. **Fan-out.** One user request calls many servers and waits for all of them.
2. **Queueing.** When servers get busy, requests wait in line, and waiting time grows very fast near full load.

> [!key] **The key idea.** Users feel your tail, not your average. Fan-out makes rare slowness common, and a busy queue makes waits explode. Measure p99, keep spare capacity, and refuse work early.

## How it works

### Fan-out makes the tail common

Dean and Barroso give the classic example. Each server usually answers in 10 ms, but its p99 is one second. If a user request uses one server, 1 request in 100 is slow. If it must collect answers from 100 servers, 63% of user requests take more than one second. [@tail]

The maths: one server is fast 99% of the time. All 100 are fast with chance 0.99^100 ≈ 0.366. So the chance that at least one is slow is 1 − 0.366 ≈ 0.63.

Rarer slowness does not save you. If only 1 call in 10,000 is slow, a service with 2,000 servers still sees almost 1 user request in 5 take over a second. [@tail] Check it: 1 − 0.9999^2000 ≈ 0.18.

Step through it. Use Next and Back, or the ← and → keys.

[[diagram:fan-out-tail]]

Google measured this on a real service. The p99 for one random leaf server to answer was 10 ms. The p99 for all leaf servers to answer was 140 ms. The p99 for 95% of them was 70 ms. So waiting for the slowest 5% caused half of the total p99. [@tail]

Where does the slowness come from? The paper lists shared machines, background daemons, garbage collection, log compaction, and queueing at many layers. Most of these are not bugs, and removing them all is not practical. [@tail]

### Little's law

Little's law links three long-run averages for any stable queue or system: **L = λW**. L is the number of items inside, λ (lambda) is the arrival rate, and W is the time each item spends inside. [@little] It does not depend on the order of service, such as first in, first out (FIFO) or last in, first out (LIFO). [@little]

For a server, it means: requests in flight = requests per second × seconds per request. At 2,000 requests a second and 50 ms each, about 100 requests are in flight at any moment.

Use it both ways. If latency doubles at the same traffic, the number of requests in flight doubles too. That means twice the threads, connections and memory. This is how a slow database can use up an app server's connection pool.

### Busy servers make long queues

Utilization is how busy a server is: arrival rate divided by service rate. In the simplest queue model (M/M/1), the average time in the system is 1 / (service rate − arrival rate). [@mm1] Put another way, it is service time / (1 − utilization).

Take a service time of 10 ms:

| Utilization | Average time in system |
|---|---|
| 50% | 20 ms |
| 80% | 50 ms |
| 90% | 100 ms |
| 95% | 200 ms |
| 99% | 1,000 ms |

Going from 50% to 95% busy makes each request about 10 times slower. Real systems are not this simple model, but the shape holds. Little writes that as arrivals approach the service rate, the queue and the wait "increase, slowly at first and then rapidly". [@little] In one load test he reports, the queue grew about linearly with load and then climbed steeply. Past about 18 requests a second, further requests were mostly dropped. [@little]

Our advice: do not plan to run latency-sensitive services near 100% busy. In the last 10% of capacity, the slowest requests get much slower.

### Backpressure and load shedding

When work arrives faster than you can finish it, the queue grows without end. You have two honest choices:

- **Backpressure.** Tell the sender to slow down. A bounded queue does this: when it is full, the sender must wait or get an error.
- **Load shedding.** Reject the extra work quickly, so the work you accept still finishes on time.

Google's SRE book says queued requests use memory and add latency. It suggests small queues, such as 50% or less of the thread pool size, so the server rejects early. [@sre-cascade] It also suggests per-task throttling on CPU, memory or queue length. [@sre-cascade]

Amazon explains why a long queue is worse than a rejection. When latency climbs past the client's timeout, requests fail even though the server did the work. If median latency equals the client timeout, half the requests time out. [@aws-shed] Amazon calls the useful part **goodput**: throughput handled without errors and fast enough for the client to use. [@aws-shed] It suggests a limit on how long a request may sit in a queue. Under overload, it suggests a LIFO queue where the protocol allows it. [@aws-shed]

Not all requests are equal. Google tags each request with a criticality, from `CRITICAL_PLUS` down to `SHEDDABLE`, and rejects the least critical requests first. [@sre-overload]

### Hedged requests

You cannot remove every slow moment, so hide them. A **hedged request** sends a request to one replica. If no answer comes after a short delay, it sends a backup copy to another replica and uses whichever answers first. [@tail]

The trick is the delay. Wait until the request is older than the p95 for its kind, and the extra load is only about 5%. [@tail] In a Google test, reading 1,000 keys spread over 100 BigTable servers, a hedge after 10 ms cut the p99.9 from 1,800 ms to 74 ms. It sent just 2% more requests. [@tail]

**Tied requests** go further: both servers get the request, and the first one to start it tells the other to cancel. [@tail] Our advice: hedge only safe, repeatable reads, or writes that are [idempotent](/learn/idempotency-retries-timeouts).

### Timeouts and deadlines

Every call needs a timeout. Google's SRE book says it is usually wise to set a deadline. A missing or very high deadline can keep resources busy long after the problem has passed. [@sre-cascade]

Pass the remaining time down the call chain. Each server should check it before doing more work, and drop requests that have already run out of time. [@sre-cascade] Limit retries too. The book advises randomized exponential backoff and a retry budget per process, such as 60 retries a minute. [@sre-cascade]

## Trade-offs

| Tool | Cuts the tail? | Cost | Use it when |
|---|---|---|---|
| More spare capacity | Yes. Lower utilization means shorter queues. | Money for idle servers. | Latency-sensitive paths. |
| Hedged requests | Yes, often by a lot. | A few % extra load. Needs safe repeats. | Reads with many replicas, large fan-out. |
| Smaller fan-out | Yes. Fewer servers to wait for. | Bigger shards, or less parallel work. | You can group data so one request hits fewer servers. |
| Load shedding | Yes, for accepted work. | Some users get a fast error. | Traffic can exceed capacity. |
| Backpressure | Yes, by keeping queues short. | Senders must slow down or buffer. | Pipelines and queues between your own services. |
| Bigger queues | No. It hides overload and adds wait. | Memory, and stale work. | Short, rare bursts only. |

**Our simple rule:** measure p99 per call, keep queues short, set deadlines, and hedge only safe reads.

## At work: how a senior uses this

- Ask for p50, p99 and p99.9, never only the average. Ask: **"p99 of what, over what time window?"**
- Draw the fan-out of a request. Count how many calls it waits for, and work out how often at least one of them is slow.
- Use Little's law to size pools: requests per second × latency = requests in flight. Size threads and connections from it.
- Set alerts on queue length and the age of the oldest item, not only on CPU.
- Set a timeout on every outbound call, below the caller's own deadline.
- In load tests, push on purpose past the point where latency starts to rise steeply. Write down the load where latency jumps.
- Read [latency numbers and estimation](/learn/latency-numbers-and-estimation) to get the base numbers right first.

## With AI

### What to ask the agent

> "Here are latency histograms for our 12 downstream calls. Which call drives our p99? Show the maths."

> "Our service gets 3,000 requests a second at 40 ms p50 and 400 ms p99. Use Little's law to size the thread pool and the database connection pool. Explain each number."

> "Add a hedged read to this client: send a backup to a second replica after the p95 latency, cancel the loser, and add a metric for how often the hedge fires."

> "Find every outbound HTTP or gRPC call in this repo that has no timeout or no deadline propagation."

### What to check yourself

- The agent did not average percentiles. A p99 across servers cannot be built by averaging each server's p99.
- Hedging is used only for reads or idempotent writes, and the backup really cancels the first call.
- The hedge delay comes from measured latency, not a guess.
- Queues have a size limit, and there is a rule for what happens when they are full.
- Timeouts shrink down the call chain, so an inner call never outlives its caller.

## Check yourself

## Go deeper

Related lessons: [Latency numbers and estimation](/learn/latency-numbers-and-estimation), [Load shedding](/learn/load-shedding-graceful-degradation), [Backpressure](/learn/backpressure-and-flow-control) and [Backoff and jitter](/learn/backoff-jitter-retry-budgets).
