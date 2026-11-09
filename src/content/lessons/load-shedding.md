---
topic: load-shedding
subtitle: When more work arrives than you can finish, drop the least important part early and cheaply, so the important part still works.
status: published
reviewed_by: ai-factcheck
reviewed_on: 2026-11-09
illustrative_story: false
sources:
  - id: netflix-2020
    title: "Manuel Correa, Arthur Gonigberg and Daniel West, “Keeping Netflix Reliable Using Prioritized Load Shedding”, Netflix TechBlog, 2 November 2020"
    url: https://netflixtechblog.com/keeping-netflix-reliable-using-prioritized-load-shedding-6cc827b02f94
    note: Netflix's three request classes, the priority score in the Zuul gateway, the cubic shedding curve, and how they test it in production.
  - id: aws-shed
    title: "David Yanacek, “Using load shedding to avoid overload”, Amazon Builders' Library (now AWS Builder Center), 2019"
    url: https://builder.aws.com/content/3Eun1EEyX6p2e3VYNyRLSJzLuMV/using-load-shedding-to-avoid-overload
    note: Goodput, why a timed-out request is wasted work, timeout hints, and how Amazon load tests past the breaking point.
  - id: sre-overload
    title: "Alejandro Forero Cuervo, “Handling Overload”, Site Reliability Engineering (Google, O'Reilly 2016), chapter 21"
    url: https://sre.google/sre-book/handling-overload/
    note: Criticality classes, client-side adaptive throttling with its formula, and when not to retry.
  - id: sre-cascade
    title: "Mike Ulrich, “Addressing Cascading Failures”, Site Reliability Engineering (Google, O'Reilly 2016), chapter 22"
    url: https://sre.google/sre-book/addressing-cascading-failures/
    note: Load shedding versus graceful degradation, queue sizes, deadlines, and load testing until things break.
  - id: fail-at-scale
    title: "Ben Maurer, “Fail at Scale”, ACM Queue, volume 13, issue 8, 2015"
    url: https://queue.acm.org/detail.cfm?id=2839461
    note: Facebook's controlled delay queue, adaptive LIFO and client-side concurrency limits, each in a few paragraphs.
  - id: rfc6585
    title: "Nottingham and Fielding, RFC 6585: Additional HTTP Status Codes (IETF, 2012), section 4"
    url: https://www.rfc-editor.org/rfc/rfc6585#section-4
    note: The definition of 429 Too Many Requests.
  - id: rfc9110
    title: "Fielding, Nottingham and Reschke, RFC 9110: HTTP Semantics (IETF, 2022), section 15.6.4"
    url: https://www.rfc-editor.org/rfc/rfc9110#name-503-service-unavailable
    note: The definition of 503 Service Unavailable and its link to Retry-After.
claims_checked:
  - claim: "Zuul sorts requests into NON_CRITICAL, DEGRADED_EXPERIENCE and CRITICAL, scores each from 1 to 100 as a first step, and uses a cubic curve: mid-90s at 35% overload, priority 50 at 80%."
    source: netflix-2020
    note: "Post: \"computes a priority score between 1 to 100\", \"The computation is done as a first step\", \"at 35%, it's still in the mid-90s ... we hit priority 50 at 80% exceeded\". Read via Wayback snapshot 20261028155512."
  - claim: "In 2019 a sizable share of members could not play; in 2020, days after deployment, a similar issue with the same potential impact was shed without impacting playback. ChAP experiments throttle a range of priorities for 45 minutes and first found an Android and iOS race condition."
    source: netflix-2020
    note: "Matches the \"Reaping the benefits\" and ChAP sections. The post does not say the 2020 traffic was as heavy; the opener now says the problem was as serious."
  - claim: "Adaptive throttling rejects locally with probability max(0, (requests − K×accepts)/(requests+1)) over the last two minutes; Google generally uses K = 2 and a lower K is more aggressive."
    source: sre-overload
    note: "Formula is an image on the page; read from the image. Text: \"We generally prefer the 2x multiplier\", \"Reducing the multiplier will make adaptive throttling behave more aggressively\"."
  - claim: "Facebook's CoDel variant: if the queue was not empty in the last 100 ms, wait is capped at 5 ms, otherwise 100 ms; adaptive LIFO switches to LIFO when a queue starts to form."
    source: fail-at-scale
    note: "The pseudo-code says \"N seconds\" but the prose says N milliseconds and \"5 milliseconds for M and 100 ms for N\"; the lesson follows the prose. Read via Wayback snapshot 20251212061629."
  - claim: "Goodput is throughput handled without errors and fast enough to use; if median latency equals the client timeout, availability is 50%; the load balancer ping is the most important request."
    source: aws-shed
    note: "All three match the article text (David Yanacek, published 5 Dec 2019, exact title \"Using load shedding to avoid overload\")."
  - claim: "Return HTTP 503 when more than a given number of requests are in flight; keep queues at 50% of the thread pool size or less for steady traffic."
    source: sre-cascade
quiz:
  - q: A server can finish 1,000 requests a second. 1,500 a second arrive, and it queues the extra in FIFO order with no limit. Clients give up after 1 second. What happens after a few seconds?
    options:
      - The server finishes 1,000 a second, and nearly all of them reach a client that is still waiting.
      - The server finishes about 1,000 a second, but almost every answer comes after its caller gave up.
      - The server finishes fewer requests, but each one is fast.
    answer: 1
    why: The queue grows by 500 a second, so the wait soon passes 1 second. The server stays busy, but goodput falls close to zero.
  - q: What is the main difference between a rate limit and load shedding?
    options:
      - A rate limit is usually per client and fixed ahead of time. Load shedding reacts to the server's own state, such as CPU or requests in flight.
      - A rate limit returns 503, and load shedding returns 429.
      - Load shedding only runs on the client.
    answer: 0
    why: A rate limit stops one client using more than its share. Load shedding protects the server when total load is too high, whoever sends it.
  - q: In Netflix's taxonomy, which traffic is shed first?
    options:
      - CRITICAL, such as the request to start playback.
      - DEGRADED_EXPERIENCE, such as viewing history.
      - NON_CRITICAL, such as logs and background requests.
    answer: 2
    why: Shedding starts with the lowest priority. Logs and background requests do not affect playback, and they are a large share of the load.
  - q: Under Facebook's adaptive LIFO, when does the server switch from FIFO to LIFO?
    options:
      - Always. LIFO is faster in general.
      - When a queue starts to form, because the oldest requests are the most likely to be abandoned.
      - Only for requests marked as critical.
    answer: 1
    why: In normal times the queue is short and FIFO is fair. Under queuing, the newest request is the one most likely to still have a waiting user.
  - q: "With Google's adaptive throttling (K = 2), a client made 100 requests in the last 2 minutes and the backend accepted 30. About what share of new requests does the client reject locally?"
    options:
      - "About 40%: (100 − 2 × 30) / 101."
      - "About 70%: the backend rejected 70 of 100."
      - "0%, because the backend still accepts some requests."
    answer: 0
    why: "The rejection chance is max(0, (requests − K × accepts) / (requests + 1)). That is 40 / 101, about 40%."
incidents: []
glossary_optout: [cap-theorem]
related: [rate-limiting, resilience-patterns, backoff-jitter, tail-latency-queueing, caching-stampedes, queues-vs-logs, observability-slos]
---

## What broke

In 2019, Netflix had an outage. A sizable share of members could not play for a period of time. [@netflix-2020]

Netflix lists many things that can overload its systems. A client bug can trigger a retry storm. A backend can be under-scaled. A deploy can be bad, or the network or cloud provider can have problems. Each of these had stopped members from playing at some point. [@netflix-2020]

At the time, Netflix had on/off circuit breakers. It had "no progressive way to shed load". [@netflix-2020] So the team built one into Zuul, its API gateway. Each request gets a priority. When things go wrong, low-priority requests are dropped first. [@netflix-2020]

In 2020, days after the change went live, a similar problem hit. It had the same potential impact as the 2019 outage. This time Zuul shed traffic until the service was healthy again, "without impacting members' ability to play at all". [@netflix-2020]

The problem was just as serious. The difference was that the system chose what to drop.

## The idea in simple words

Every server has a limit. When more work arrives than it can finish, some work will fail. You cannot avoid that. You can only choose which work fails, and how.

Without a plan, the server tries to do everything. Then nearly everything fails. With load shedding, the server rejects some requests early and finishes the rest in time.

You already know [rate limiting](/learn/rate-limiting) and [circuit breakers and bulkheads](/learn/circuit-breakers-and-bulkheads). The difference matters:

- A **rate limit** is usually a fixed quota per client. It stops one client from taking more than its share.
- **Load shedding** is the server protecting itself. It looks at its own state, such as CPU or requests in flight, whoever the traffic comes from.

**Graceful degradation** goes one step further. It cuts the work per request, for example by serving a slightly stale local copy of the data. [@sre-overload]

> [!key] **The key idea.** Overload means some work will fail. Decide in advance which work that is. Drop it early, cheaply and on purpose, so the important work still finishes in time.

## How it works

### What overload does

Step through a server under rising load. Use Next and Back, or the ← and → keys.

[[diagram:overload-goodput-shedding]]

The trap is that the server looks busy and healthy. It finishes as many requests as ever. But the client has already timed out, so the work is wasted. Amazon calls the useful part **goodput**: requests handled without errors and fast enough to use. [@aws-shed] If median latency equals the client timeout, half the requests time out, so availability is 50%. [@aws-shed]

The [Tail latency and queueing](/learn/tail-latency-and-queueing) lesson showed why queues explode near 100% busy. Load shedding is what you do about it. It lets a server "maintain its goodput". [@aws-shed]

### Shed early and cheaply

A rejection must cost much less than real work. Amazon says early rejection "is the cheapest place to drop excess traffic". [@aws-shed] So check before you parse the body, open a transaction or call another service.

Google warns that rejecting is not always cheap. For a simple memory lookup, rejecting costs almost as much as serving. [@sre-overload] Shedding has limits, so you still need capacity.

A simple server-side signal is the number of requests in flight. Over a limit, return HTTP 503 to new requests. [@sre-cascade] Google also uses CPU and rejects by criticality as use nears a threshold. [@sre-overload]

### Decide what to drop

Not all requests are equal. Netflix sorts its traffic into three classes: [@netflix-2020]

- **NON_CRITICAL**: logs and background requests. They do not affect playback, but they are a large share of load.
- **DEGRADED_EXPERIENCE**: features like viewing history and pause markers. Users notice, but can still play.
- **CRITICAL**: if this fails, the user sees an error when they press play.

Zuul gives each request a score from 1 to 100 as its first step. [@netflix-2020] Under overload it drops requests below a moving threshold. A cubic curve moves the threshold slowly at first, then sharply. At 35% overload the threshold is still in the mid-90s. At 80% it reaches 50. [@netflix-2020]

Google uses four levels: `CRITICAL_PLUS`, `CRITICAL`, `SHEDDABLE_PLUS` and `SHEDDABLE`. [@sre-overload] Amazon adds useful hints. The load balancer's health check is "the most important request" a server gets. Requests that finish work already started beat requests that start new work. [@aws-shed]

In a shop, that could mean: payment and checkout first, product pages next, then prefetch, recommendations and batch jobs. Move batch work to a [queue](/learn/queues-vs-logs-delivery-guarantees), so it can wait.

Here is a sketch of a server-side check. Lower classes get lower limits, so they are shed first:

```python
LIMITS = {"critical": 200, "degraded": 150, "sheddable": 100}

def admit(req, in_flight):
    if req.deadline_left_ms() <= 0:
        return reject(503)  # the caller has given up
    if in_flight >= LIMITS[req.priority]:
        return reject(503, retry_after_s=5)
    return None  # accept the request
```

### Drop work nobody waits for

A request whose caller has gone is pure waste. Amazon suggests clients send a timeout hint, so the server can "drop doomed requests at little cost". [@aws-shed] Google says to check the time left at each stage, before more work. [@sre-cascade] You met deadline propagation in [Backoff, jitter and retry budgets](/learn/backoff-jitter-retry-budgets).

Keep queues small. For steady traffic, Google suggests 50% of the thread pool size or less. [@sre-cascade] Amazon also caps how long a request may wait in a queue. [@aws-shed]

### Queue order: controlled delay and adaptive LIFO

Facebook found that "many of our worst incidents" had large numbers of requests waiting in queues. [@fail-at-scale] In 2015, Facebook described using two ideas together.

**Controlled delay**, a variant of the CoDel algorithm from network research. If the queue has not been empty in the last 100 ms, a new request may wait only 5 ms. Otherwise it may wait up to 100 ms. [@fail-at-scale] Short bursts still queue, but a standing queue cannot form. Facebook found these values work well for most services, without tuning for each one. [@fail-at-scale]

**Adaptive LIFO.** In a long queue, the oldest request's user may have already left. So the server uses FIFO normally, then switches to LIFO when a queue starts to form. [@fail-at-scale] The newest requests then have the best chance to finish in time.

### Clients can help: adaptive throttling

A rejected request still costs the server something. So Google's clients throttle themselves. Google calls this adaptive throttling. Each client counts `requests` (attempts) and `accepts` (accepted by the backend) over the last 2 minutes. [@sre-overload] Once `requests` passes K times `accepts`, the client starts rejecting some new requests locally, without sending them. The chance grows as the backend accepts less:

```python
K = 2.0  # Google's usual choice

def reject_chance(requests, accepts):
    # counts from the last 2 minutes; requests includes local rejects
    return max(0.0, (requests - K * accepts) / (requests + 1))
```

A lower K throttles harder. [@sre-overload] Facebook's clients also cap requests in flight to each service and fail extra ones at once. [@fail-at-scale]

### Send the right signal

Use 503 for "I am overloaded". It means a temporary overload, and the server may add `Retry-After`. [@rfc9110] Keep 429 for a client that sent too many requests, as in the rate limiting lesson. [@rfc6585]

The signal must calm clients down, not cause a retry storm. When Zuul sheds, it tells devices how many retries they may make, and in what time window. [@netflix-2020] Google advises not to retry when many backend tasks are overloaded. Let the error go back to the caller. [@sre-overload]

### Degrade gracefully

Shedding drops whole requests. Degradation does less work per request. Google's examples: search a smaller in-memory set instead of the disk, or use a cheaper ranking. [@sre-cascade] Its example service stops returning pictures and small maps when capacity runs short. [@sre-cascade] Other choices: serve stale cached data, as in [Caching and cache stampedes](/learn/caching-and-cache-stampedes). Or turn off optional features with a kill switch.

Degraded modes should rarely run. Exercise them anyway, and alert when many servers use them. [@sre-cascade]

### Test it

Amazon's rule is direct. If you have not load tested "far beyond the point where it breaks", expect the worst way of failing. [@aws-shed] The ideal result: goodput levels off near full use, instead of falling. [@aws-shed]

Netflix also tests what users see when traffic is shed. In production experiments, a small group of users has a range of priorities throttled for 45 minutes. [@netflix-2020] The first run found a race condition on Android and iOS, in a low-priority request that caused playback errors. [@netflix-2020]

## Trade-offs

| Option | Protects goodput? | Cost | Use it when |
|---|---|---|---|
| No limit, unbounded FIFO queue | No. Goodput falls close to zero. | Nothing until the spike. | Never for user-facing services. |
| Bounded queue, reject when full | Yes, partly. | Drops at random, not by importance. | A first, simple step. |
| In-flight limit per priority class | Yes. | You must label every request and keep labels right. | Mixed traffic, like checkout and prefetch. |
| Deadline checks | Yes. Removes wasted work. | Clients must send a deadline or timeout hint. | Long call chains. |
| Controlled delay + adaptive LIFO | Yes. | Newer requests overtake older ones. | Server queues under bursty load. |
| Client adaptive throttling | Yes. Saves the cost of rejecting. | Some requests fail without trying. | Many clients call one backend. |
| Graceful degradation | Yes. Less work per request. | Users get less. Rare code paths can break. | Optional parts of a response. |

**Simple rule:** a small bounded queue, an in-flight limit per priority class, and deadline checks. Add degradation for optional features, and test it all with load tests.

## At work: how a senior uses this

- Ask of every service: **"At twice our peak, what fails first? Did we choose that?"**
- Write down the priority class of each endpoint with the product owner. Payment is not the same as a recommendations widget.
- Watch goodput, not only throughput. Count requests that finished in time and without error, and tie it to your [SLOs](/learn/observability-slos-error-budgets).
- Count shed requests by class and alert when critical traffic is shed.
- Load test past the breaking point before big events. Check that goodput levels off.
- Review client retry rules. They should honor `Retry-After` and stop when most calls fail.

## With AI

### What to ask the agent

> "Add an admission check to this service. Count requests in flight. Read a priority from the `X-Priority` header (critical, degraded, sheddable). Reject over a per-class limit with 503 and Retry-After, before parsing the body."

> "Propagate the client deadline through every call in this handler. Before each downstream call, drop the request if less than 50 ms is left."

> "Write a load test that ramps from 50% to 300% of measured capacity. Plot throughput, goodput and p99 latency per priority class."

### What to check yourself

- The rejection happens before any expensive work: no body parsing, database call or downstream call.
- Shed requests get 503, and the client code honors `Retry-After`.
- Health checks are never shed.
- Every endpoint has a priority, and unknown traffic gets a low class, not critical.
- The load test shows checkout latency staying flat while low-priority traffic is shed.

## Check yourself

## Go deeper

Related lessons: [Rate limiting](/learn/rate-limiting), [Circuit breakers and bulkheads](/learn/circuit-breakers-and-bulkheads), [Tail latency and queueing](/learn/tail-latency-and-queueing), [Backoff, jitter and retry budgets](/learn/backoff-jitter-retry-budgets) and [Caching and cache stampedes](/learn/caching-and-cache-stampedes).
