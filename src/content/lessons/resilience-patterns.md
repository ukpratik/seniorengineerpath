---
topic: resilience-patterns
subtitle: One slow service can take down every service that calls it. Here is how to keep the damage small.
status: published
reviewed_by: ai-factcheck
reviewed_on: 2026-11-09
illustrative_story: false
sources:
  - id: cf-kv
    title: "Cloudflare blog, “Cloudflare service outage June 12, 2025”"
    url: https://blog.cloudflare.com/cloudflare-service-outage-june-12-2025/
    note: The official report. See how one storage failure spread to Access, WARP, the dashboard and more, and how each product reacted.
  - id: fowler
    title: "Martin Fowler, “Circuit Breaker”, martinfowler.com bliki (6 March 2014)"
    url: https://martinfowler.com/bliki/CircuitBreaker.html
    note: The closed, open and half-open states, with short Ruby code and advice on monitoring.
  - id: hystrix-home
    title: "Netflix/Hystrix wiki, “Home” (GitHub)"
    url: https://github.com/Netflix/Hystrix/wiki
    note: Why one slow dependency can use up all resources on every server in seconds.
  - id: hystrix-how
    title: "Netflix/Hystrix wiki, “How it Works” (GitHub)"
    url: https://github.com/Netflix/Hystrix/wiki/How-it-Works
    note: Thread pools per dependency, semaphores, when the breaker trips, and rules for fallbacks.
  - id: hystrix-readme
    title: "Netflix/Hystrix, README (GitHub)"
    url: https://github.com/Netflix/Hystrix
    note: Hystrix is in maintenance mode, and Netflix points new projects to resilience4j.
  - id: r4j-cb
    title: "Resilience4j documentation, “CircuitBreaker”"
    url: https://resilience4j.readme.io/docs/circuitbreaker
    note: Sliding windows, failure and slow-call thresholds, every setting with its default, and a config example.
  - id: r4j-bh
    title: "Resilience4j documentation, “Bulkhead”"
    url: https://resilience4j.readme.io/docs/bulkhead
    note: Semaphore and thread-pool bulkheads, with their settings and defaults.
  - id: brooker-bl
    title: "Marc Brooker, “Timeouts, retries, and backoff with jitter”, Amazon Builders' Library"
    url: https://builder.aws.com/content/3EumjoZascWd1oZiEgL8ORlv3qE/timeouts-retries-and-backoff-with-jitter
    note: What a waiting call holds on to, how Amazon picks a timeout, and the cost of circuit breakers.
  - id: aws-fallback
    title: "Jacob Gabrielson, “Avoiding fallback in distributed systems”, Amazon Builders' Library"
    url: https://builder.aws.com/content/3EuS9Sakq7L3VLQIF3qzfMfke1Y/avoiding-fallback-in-distributed-systems
    note: Why Amazon usually avoids fallback, and what it does instead.
  - id: envoy-cb
    title: "Envoy documentation, “Circuit breaking” (architecture overview)"
    url: https://www.envoyproxy.io/docs/envoy/latest/intro/arch_overview/upstream/circuit_breaking
    note: The per-cluster limits a proxy can enforce for you, with no app code.
  - id: envoy-outlier
    title: "Envoy documentation, “Outlier detection” (architecture overview)"
    url: https://www.envoyproxy.io/docs/envoy/latest/intro/arch_overview/upstream/outlier
    note: How Envoy finds hosts that fail more than their peers and removes them from load balancing.
claims_checked:
  - claim: "About 90% of Workers KV requests failed; Access failed every identity-based login, dashboard logins failed and all Workers AI inference failed; impact ended at 20:28 UTC."
    source: cf-kv
    note: "Impact table: 'Workers KV saw 90.22% of requests failing'; 'Access failed 100% of identity based logins'; 'All inference requests to Workers AI failed'; timeline '2025-06-12 20:28 IMPACT END'. Start 17:52 to end 20:28 is 2 h 36 min vs the post's '2 hours and 28 minutes', so no duration is stated."
  - claim: "Access and Gateway fail closed by design; Turnstile's kill switches let users through but tokens could be redeemed more than once; at 19:32 UTC Access and Device Posture dropped requests to shed load on KV."
    source: cf-kv
    note: "'designed to fail closed'; 'could redeem valid tokens multiple times'; '19:32 Access and Device Posture force drop identity and device posture requests to shed load on Workers KV'."
  - claim: "A circuit breaker has closed, open and half-open states; when open, calls return an error without the protected call being made; Nygard popularized the pattern in Release It!."
    source: fowler
    note: "'all further calls to the circuit breaker return with an error, without the protected call being made at all'; 'Michael Nygard popularized the Circuit Breaker pattern'; half-open trial call resets or restarts the timeout."
  - claim: "Resilience4j defaults: sliding window 100, minimumNumberOfCalls 100, waitDurationInOpenState 60 s, 10 permitted half-open calls, slow call 60 s at 100%; open breaker throws CallNotPermittedException."
    source: r4j-cb
    note: "Config property table and Introduction section; all builder method names in the snippet match the docs."
  - claim: "Hystrix is in maintenance mode and Netflix points new projects to resilience4j."
    source: hystrix-readme
    note: "'Hystrix is no longer in active development, and is currently in maintenance mode'; 'leverage open and active projects like resilience4j for new internal projects'."
  - claim: "Amazon almost never uses fallback in the narrow sense of a different mechanism doing the same job, prefers a more reliable main path, pushing data ahead of time, or running both paths continuously."
    source: aws-fallback
    note: "'Fallback: Use a different mechanism to achieve the same result ... why we almost never use them at Amazon'; IAM role credentials 'proactively pushed'; 'run both the fallback and the non-fallback logic continuously'."
quiz:
  - q: Service A calls service B. Which version of B hurts service A more?
    options:
      - B is down and refuses every connection at once.
      - B is up but answers every call after 10 seconds.
      - Both hurt A the same.
    answer: 1
    why: A refused call fails in milliseconds. A slow call holds a thread and a connection while it waits, so waiting calls pile up and use up A's threads.
  - q: A circuit breaker is open. Your code makes a call through it. What happens?
    options:
      - The call goes to the service with a shorter timeout.
      - The call waits until the breaker closes.
      - The breaker returns an error at once, without calling the service.
    answer: 2
    why: An open breaker fails fast. That frees your threads and gives the failing service a rest.
  - q: The breaker is half-open, and its trial calls fail. What does it do next?
    options:
      - It goes back to open and waits again.
      - It closes, because it has tried enough.
      - It stays half-open and sends all traffic.
    answer: 0
    why: Failed trial calls mean the service is still sick. The breaker opens again and starts a new wait.
  - q: Your service has 200 worker threads. Why give the ratings call its own pool of 20 slots?
    options:
      - So ratings calls run 10 times faster.
      - So a slow ratings service can tie up at most 20 threads, and the other 180 keep serving.
      - So you no longer need a timeout on the ratings call.
    answer: 1
    why: That is a bulkhead. One bad dependency can fill only its own pool.
  - q: In the Builders' Library, what is Amazon's position on fallback, meaning a different mechanism that does the same job?
    options:
      - Always add a fallback to a second system.
      - Almost never use it. Make the main path more reliable, or run both paths all the time.
      - Use fallback only for reads.
    answer: 1
    why: A second mechanism that rarely runs is hard to test and can make an outage worse. Amazon prefers paths that run all the time.
incidents: [cloudflare-2025-06-workers-kv]
cases: [shopify-resilient-payments]
glossary_optout: [game-day]
related: [idempotency-retries, backoff-jitter, error-handling, tail-latency-queueing, api-gateway, why-distributed-hard]
---

## What broke

On 12 June 2025, Cloudflare's Workers KV started to fail. KV is a key-value store, and Cloudflare calls it "a critical dependency for many Cloudflare products". [@cf-kv]

Part of the storage behind KV ran on a third-party cloud, and that cloud had its own outage. About 90% of KV requests failed. Reads that hit a cache still worked. [@cf-kv]

The failure did not stay inside KV. Access, Cloudflare's login gate, failed every identity-based login. Dashboard logins failed, and Workers AI could not run models at all. [@cf-kv] Cloudflare wrote that the cascading impact from KV "significantly broadened the blast radius." [@cf-kv]

Each product's design decided what happened when KV failed. Access and Gateway are designed to fail closed, so they blocked users instead of skipping customer rules. Turnstile turned on kill switches that switch off those failing calls. Users could get through, but a token could be used more than once. [@cf-kv]

At 19:32 UTC, Access and Device Posture dropped requests on purpose to take load off KV. Impact ended at 20:28 UTC. One fix is a tool to bring KV back step by step, without "a denial-of-service against our own infrastructure as caches are repopulated." [@cf-kv]

## The idea in simple words

Every service you call can fail or get slow. If you do nothing, its problem becomes your problem, and then your callers' problem.

Four tools keep the damage small:

- A **timeout** limits how long one call waits.
- A **circuit breaker** stops calling a service that keeps failing, for a short time.
- A **bulkhead** limits how many of your threads one dependency can use.
- **Graceful degradation** decides what the user gets when the call fails: old data, a default, or a page without that feature.

> [!key] **The key idea.** A dead dependency fails fast. A slow one holds on to your threads until you have none left. Put a limit on every call: how long it waits, how many calls wait at once, and how long you keep calling a service that is failing.

## How it works

### Why slow is worse than dead

If a service is down and its machine refuses the connection, you get an error in milliseconds. (If the whole machine is gone, you wait for the connect timeout.) If it is slow, each call holds memory, a thread and a connection while it waits. [@brooker-bl]

New requests keep coming, so waiting calls pile up. Say you get 200 requests a second and each makes one call. At 50 ms per call, about 10 calls wait at any moment. At 5 seconds per call, about 1,000 wait. A pool of 200 threads is full in about one second.

The Hystrix wiki says that under high traffic, "a single backend dependency becoming latent can cause all resources to become saturated in seconds on all servers." [@hystrix-home] Now your service is slow too, and your callers start to pile up. Fowler calls the result "cascading failures across multiple systems." [@fowler]

### Timeouts come first

You met timeouts in [Idempotency and retries](/learn/idempotency-retries-timeouts). Every remote call needs one. Amazon picks an acceptable rate of false timeouts, such as 0.1%. It then uses that percentile of the downstream latency: the 99.9th percentile. [@brooker-bl]

A timeout caps one call. It does not stop you from sending the next call to the same slow service, and the one after that. That is the job of the circuit breaker.

### The circuit breaker

Michael Nygard made this pattern popular in his book *Release It!*. Martin Fowler's short article describes it well. [@fowler] You wrap the remote call in a breaker object that watches for failures. It has three states:

- **Closed.** Calls go through. The breaker counts failures, and a timeout counts as a failure. [@fowler]
- **Open.** When failures reach a threshold, the breaker trips. Every call now gets an error at once, and the service is not called at all. [@fowler]
- **Half-open.** After a wait, the breaker lets a trial call through. If it works, the breaker closes. If it fails, the breaker opens and waits again. [@fowler]

Step through a breaker in front of a ratings service. Use Next and Back, or the ← and → keys.

[[diagram:circuit-breaker-states]]

Real libraries judge a rate, not a simple count. Hystrix trips only when there are enough requests and the error percentage is over a threshold. After a sleep window, it lets one request through as a test. [@hystrix-how]

Resilience4j records the last N calls, or the calls in the last N seconds. It can also trip when too many calls are slow, not only when they fail. That matters, because slow is the dangerous case. An open breaker rejects calls with a `CallNotPermittedException`. [@r4j-cb] Here is the diagram's setup in Resilience4j. The slow-call setting does not stop a call, so you still need the timeout:

```java
// The 1 s timeout is set on the HTTP client (or a TimeLimiter).
// The breaker counts the timeout exception as a failure.
CircuitBreakerConfig config = CircuitBreakerConfig.custom()
    .slidingWindowType(SlidingWindowType.COUNT_BASED)
    .slidingWindowSize(20)              // judge the last 20 calls
    .minimumNumberOfCalls(10)           // but only after 10 calls
    .failureRateThreshold(50)           // open at 50% failures
    .slowCallDurationThreshold(Duration.ofSeconds(1))
    .slowCallRateThreshold(50)          // or at 50% slow calls
    .waitDurationInOpenState(Duration.ofSeconds(30))
    .permittedNumberOfCallsInHalfOpenState(3)
    .build();
```

The defaults are larger: a window of 100 calls, at least 100 calls before it judges, a 60-second wait and 10 trial calls. A call counts as slow only after 60 seconds, and the breaker trips on slow calls only if all of them are slow. [@r4j-cb] So set both slow-call values yourself. Check them against your traffic. A breaker that needs 100 calls may never trip on a quiet endpoint.

Log every state change, and show the breaker's state on your monitoring. [@fowler]

Breakers have a cost, as you saw in [Error handling](/learn/error-handling-defensive-design). They add a second mode of working, which "can be difficult to test", and they can slow recovery. [@brooker-bl] Amazon limits retries locally instead, as in [Backoff, jitter and retry budgets](/learn/backoff-jitter-retry-budgets). [@brooker-bl]

### Bulkheads

A ship's hull is split into sealed sections. If one section floods, the others keep the ship afloat. A bulkhead in software does the same for your threads.

Give each dependency its own small pool. Hystrix ran a separate thread pool per dependency, so a slow dependency filled only its own pool. [@hystrix-how] Each Netflix API server had more than 40 thread pools, with 5 to 20 threads each, and most had 10. [@hystrix-how]

A lighter option is a semaphore: a counter of calls in flight. It costs less than a thread pool. But the caller cannot time out and walk away from a call that hangs. [@hystrix-how] Resilience4j offers both kinds. Its semaphore bulkhead allows 25 calls at once by default, and it does not wait for a free slot. [@r4j-bh]

Here is the effect. Your service has 200 threads, and the ratings call gets a bulkhead of 20. When ratings gets slow, 20 calls wait and the 21st is rejected at once. The other 180 threads keep serving other pages, such as checkout.

### Fallbacks and graceful degradation

When a call fails, you still have to send the user something. Serving less instead of failing is graceful degradation. Common choices:

- **Stale data.** Show the last cached ratings.
- **A default.** Show a safe value from config, such as the standard delivery time.
- **Hide the feature.** Show the product page without ratings.
- **Fail.** Return a clear error. Cloudflare's Access chose this on purpose, to avoid letting in users it could not check. [@cf-kv]

Hystrix calls code like the first three a fallback. Its advice: a fallback should avoid network calls, and "It is a poor practice to implement a fallback implementation that can fail." [@hystrix-how]

Amazon uses the word fallback in a narrower sense: a different mechanism that does the same job. Its example is web servers that read from a database when their cache fails. Amazon almost never uses that kind, because it rarely runs, is hard to test and can make an outage worse. [@aws-fallback] [Error handling](/learn/error-handling-defensive-design) tells the story of the outage behind this view.

Amazon prefers to make the main path more reliable. Two of its other options: push data out ahead of time, as when EC2 instances get IAM role credentials that stay valid for hours. Or run both paths all the time, for example by picking at random between their answers, so neither path is rare. [@aws-fallback]

Amazon's warning is about a second mechanism for the same job. The same logic applies to any degraded mode, though: code that rarely runs needs tests.

The Turnstile kill switch shows the trade: users kept moving, but tokens could be reused. [@cf-kv] Decide each trade with your product owner before the outage.

### Where these live

- **In your code, as a library.** Resilience4j is one Java library for this. Hystrix is in maintenance mode, and Netflix suggests resilience4j for new projects. [@hystrix-readme]
- **In a proxy next to each service**, as in a [service mesh](/learn/service-mesh-and-sidecars). Envoy calls its per-cluster limits "circuit breaking": maximum connections, pending requests, requests and active retries. [@envoy-cb] These act like bulkheads, and you write no app code. Removing hosts that fail more than their peers is a separate Envoy feature, outlier detection. [@envoy-outlier]
- **At the [gateway](/learn/api-gateway-and-edge)**, to guard your services from outside traffic.

The server can protect itself too, by rejecting work it cannot finish in time. That is load shedding, the topic of [load shedding and graceful degradation](/learn/load-shedding-graceful-degradation).

### Test them

A breaker or fallback that never ran is like any rare code path: it may not work. Make a dependency slow in a test environment. Check that the breaker opens, the bulkhead rejects and the page still renders. [Chaos engineering and game days](/learn/chaos-engineering-game-days) shows how to do this safely in production.

## Trade-offs

| Option | Protects your threads? | Cost | Use it when |
|---|---|---|---|
| Timeout only | Partly. Each call is capped, but slow calls still pile up. | Almost nothing. | Always. It is the minimum. |
| Circuit breaker | Yes. An open breaker fails fast. | A new mode that is hard to test. It can slow recovery. | A dependency fails or slows for minutes, not milliseconds. |
| Thread-pool bulkhead | Yes. Each dependency has its own pool. | More threads and a thread switch on each call. | Blocking calls to several dependencies of different health. |
| Semaphore bulkhead | Yes, it caps calls in flight. | You cannot walk away from a hung call. | Non-blocking clients, or calls with their own timeout. |
| Stale data or a default | Yes, if it needs no network call. | Users may see old or wrong data. | Old data is clearly better than none. |
| Hide the feature | Yes. | Users lose that part of the page. | The feature is optional. |
| Fail closed with an error | Yes. | Users are blocked. | Security checks, payments, anything unsafe to guess. |

**Simple rule:** a timeout on every call, a bulkhead per dependency, a breaker where failures last, and a fallback only if you test it often.

## At work: how a senior uses this

- List every dependency of your service. For each one, ask: **"If this takes 10 seconds, what happens to our threads?"**
- Mark each dependency as critical or optional. Optional ones get a bulkhead and a planned degraded mode.
- Size bulkheads with numbers: calls a second × normal latency, plus room for spikes.
- Put breaker state on a dashboard and alert when a breaker stays open.
- Agree with the product owner on fail open or fail closed, feature by feature.

## With AI

### What to ask the agent

> "List every outgoing call in this service. For each, show the timeout, retry policy, circuit breaker and bulkhead settings, and what the caller gets on failure. Mark calls with none."

> "Wrap the ratings client in a Resilience4j circuit breaker and a semaphore bulkhead of 20 calls. On CallNotPermittedException or BulkheadFullException, return an empty ratings list. Do not make network calls in the fallback."

> "Write a test where the ratings service answers after 5 seconds. Assert that the breaker opens, that checkout still answers in under 300 ms, and that the page renders without ratings."

### What to check yourself

- Every remote call has a timeout, and the breaker counts a timeout as a failure.
- Thresholds fit your real traffic, not the library defaults.
- No fallback calls another remote service, especially a shared database.
- Fail open or fail closed was a decision, not an accident.
- A test makes the dependency slow, not just down.

## Check yourself

## Go deeper

Related lessons: [Backoff, jitter and retry budgets](/learn/backoff-jitter-retry-budgets), [Error handling](/learn/error-handling-defensive-design), [Tail latency and queueing](/learn/tail-latency-and-queueing) and [Why distributed systems are hard](/learn/why-distributed-systems-are-hard).

Read the [Cloudflare Workers KV incident](/incidents#cloudflare-2025-06-workers-kv) and the [Shopify payments case](/cases#shopify-resilient-payments).
