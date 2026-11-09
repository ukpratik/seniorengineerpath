---
topic: backoff-jitter
subtitle: Retries rescue one request but can sink a whole system. Here is how to retry without making an outage worse.
status: published
reviewed_by: ai-factcheck
reviewed_on: 2026-11-08
illustrative_story: false
sources:
  - id: gcp2025
    title: "Google Cloud Service Health, “Multiple GCP products are experiencing Service issues” (incident of 12 June 2025; report of 13 June 2025)"
    url: https://status.cloud.google.com/incidents/ow5i3PPK96RduMcb1SsW
    note: The official report. Read the recovery part, where restarting tasks overloaded a Spanner table.
  - id: brooker2015
    title: "Marc Brooker, “Exponential Backoff And Jitter”, AWS Architecture Blog (4 March 2015)"
    url: https://aws.amazon.com/blogs/architecture/exponential-backoff-and-jitter/
    note: The simulation that compares no jitter, full, equal and decorrelated jitter, with charts.
  - id: backoff-sim
    title: "awslabs/aws-arch-backoff-simulator, backoff_simulator.py (GitHub)"
    url: https://github.com/awslabs/aws-arch-backoff-simulator/blob/master/src/backoff_simulator.py
    note: The short Python code behind the 2015 post, with each jitter rule in a few lines.
  - id: brooker-bl
    title: "Marc Brooker, “Timeouts, retries, and backoff with jitter”, Amazon Builders' Library"
    url: https://builder.aws.com/content/3EumjoZascWd1oZiEgL8ORlv3qE/timeouts-retries-and-backoff-with-jitter
    note: How Amazon caps backoff, limits retries with a token bucket and retries at one point in the stack.
  - id: sre-overload
    title: "Alejandro Forero Cuervo, “Handling Overload”, Site Reliability Engineering (Google, O'Reilly 2016), chapter 21"
    url: https://sre.google/sre-book/handling-overload/
    note: Google's per-request and per-client retry budgets, and the "overloaded; don't retry" error.
  - id: sre-cascade
    title: "Mike Ulrich, “Addressing Cascading Failures”, Site Reliability Engineering (Google, O'Reilly 2016), chapter 22"
    url: https://sre.google/sre-book/addressing-cascading-failures/
    note: The 64-attempt example, retry rules and deadline propagation.
  - id: aws-sdk
    title: "AWS SDKs and Tools Reference Guide, “Retry behavior”"
    url: https://docs.aws.amazon.com/sdkref/latest/guide/feature-retry-behavior.html
    note: Standard and adaptive retry modes, the backoff formula and the retry quota, with worked numbers.
  - id: grpc-a6
    title: "gRPC proposal A6, “gRPC Retry Design” (GitHub)"
    url: https://github.com/grpc/proposal/blob/master/A6-client-retries.md
    note: How gRPC clients throttle retries with a token count, and how servers push back.
  - id: rfc9110
    title: "IETF RFC 9110, “HTTP Semantics” (June 2022), section 10.2.3 Retry-After"
    url: https://www.rfc-editor.org/rfc/rfc9110#field.retry-after
    note: The Retry-After header, its two formats and its meaning with 503.
claims_checked:
  - claim: "Service Control's root cause was found within 10 minutes and the red button rolled out within 40; restarting tasks then caused a herd effect on a Spanner table because there was no randomized exponential backoff; us-central1 took up to about 2 h 40 min to fully recover."
    source: gcp2025
    note: "13 Jun 2025 incident report: 'Within 10 minutes, the root cause was identified'; 'Within 40 minutes of the incident, the red-button rollout was completed'; 'did not have the appropriate randomized exponential backoff implemented'; 'up to ~2h 40 mins to fully resolve in us-central-1'."
  - claim: "Three layers that each make 4 attempts send 64 attempts to the database for one user action."
    source: sre-cascade
    note: "Ch. 22: 'the backend, frontend, and JavaScript layers all issue 3 retries (4 attempts)... 64 attempts (4^3) on the database'."
  - claim: "Google caps each request at 3 attempts and retries per client only while retries are below 10% of requests; load grows to just under 3x without the client budget and about 1.1x with it; only the layer just above the failing one retries."
    source: sre-overload
    note: "Ch. 21: 'per-request retry budget of up to three attempts'; 'retried as long as this ratio is below 10%'; 'just below 3X'; 'just 1.1x in the general case'; 'the layer immediately above the layer that is rejecting them'."
  - claim: "AWS SDKs' updated retry behavior (opt-in via AWS_NEW_RETRIES_2026=true) uses full jitter, random(0,1) x min(20,000 ms, base_delay x 2^retry), and a 500-token retry quota where a transient retry costs 14 tokens and a first-try success restores 1."
    source: aws-sdk
    note: "Formula, 20 s cap, 500/14/1 and the opt-in notice are on the Retry behavior page as read 2026-11-08."
  - claim: "Full, equal and decorrelated jitter formulas in the snippet match the simulator code (constants and indexing adapted)."
    source: backoff-sim
    note: "ExpoBackoffFullJitter uniform(0, v); ExpoBackoffEqualJitter v/2 + uniform(0, v/2); ExpoBackoffDecorr min(cap, uniform(base, sleep*3)) starting at base. Simulator uses base 5, cap 2000, first retry n=1; lesson uses AWS 50 ms / 20 s and n from 0, and says so."
  - claim: "gRPC retry throttling: each failed call with a retryable code takes 1 token per server, each success adds tokenRatio, and retries stop at or below maxTokens/2."
    source: grpc-a6
    note: "gRFC A6 'Throttling Retry Attempts and Hedged RPCs' section."
quiz:
  - q: The service calls the database, and the database rejects the call as overloaded. Under Google's rule, which layer retries?
    options:
      - Every layer, so each one gets another chance.
      - Only the service, the layer just above the database.
      - Only the app at the top, because it knows the user.
    answer: 1
    why: Google retries only at the layer just above the one that failed. If that fails, it returns "overloaded; don't retry" so no one higher retries.
  - q: With full jitter, how long does a client wait before retry number n?
    options:
      - Exactly base × 2^n, so every client waits the same time.
      - A random time between 0 and min(cap, base × 2^n).
      - Half of min(cap, base × 2^n), plus a small random part.
    answer: 1
    why: Full jitter picks the whole wait at random, up to the capped backoff. The third option is equal jitter.
  - q: A client sends 1,000 requests a second and keeps retries under 10% of its requests. The backend fails every call. About how many retries a second can the client send?
    options: ["About 100", "About 1,000", "About 3,000"]
    answer: 0
    why: A 10% retry budget caps retries at about 100 a second, so total load stays near 1.1×.
  - q: Which response is worth retrying, after a wait?
    options:
      - 400 Bad Request, because a field is missing.
      - 503 Service Unavailable with Retry-After of 30 seconds.
      - 403 Forbidden, because the token has no access.
    answer: 1
    why: A 503 may pass, and Retry-After says how long to wait. The other two will fail the same way again.
incidents: [google-cloud-2025-06-service-control]
glossary_optout: [cap-theorem, process, quota]
related: [idempotency-retries, tail-latency-queueing, rate-limiting, caching-stampedes, error-handling, resilience-patterns]
---

## What broke

On 12 June 2025, many Google Cloud APIs returned 503 errors around the world. A bad policy record made Service Control, the system that checks API requests, crash again and again. [@gcp2025]

Google's engineers found the cause within 10 minutes. Within 40 minutes, they had rolled out a switch that turned the failing code path off. [@gcp2025]

That should have been the end. But in large regions like us-central1, the restarting Service Control tasks all hit the Spanner table they depend on. Google calls this a "herd effect". [@gcp2025]

The report says Service Control "did not have the appropriate randomized exponential backoff implemented to avoid this." Engineers throttled task creation to protect that database. Full recovery in us-central1 took up to about 2 hours 40 minutes. [@gcp2025]

So the bug caused the outage, but the missing backoff stretched the recovery. One of Google's fixes: "audit and ensure our systems employ randomized exponential backoff." [@gcp2025]

## The idea in simple words

You learned in [Idempotency and retries](/learn/idempotency-retries-timeouts) that retries need a key, a limit and backoff with jitter. This lesson is about the system as a whole. One retry helps one request. Millions of retries can keep a failing service down.

Brooker calls retries "selfish": "when a client retries, it spends more of the server's time to get a higher chance of success". [@brooker-bl] That is fine when failures are rare. It is dangerous when the server fails because it is overloaded.

> [!key] **The key idea.** Spread retries out in time (backoff with jitter), keep them in one layer, and give them a budget. When the budget runs out, fail fast instead of retrying.

## How it works

### Retries multiply across layers

You saw in the last lesson that retries multiply when several layers retry. Here are the numbers when each layer makes 1 try plus 3 retries. Step through it with Next and Back, or the ← and → keys.

[[diagram:retry-amplification]]

Google's SRE book gives the same example: 3 layers that each make 4 attempts send 64 attempts to the database for one user action. [@sre-cascade] Brooker's example has five layers with 3 tries each: 3^5 = 243 times the database load. [@brooker-bl] This is retry amplification.

The fix is to retry at one point in the stack. That is Amazon's best practice for low-cost operations. [@brooker-bl] Google adds a rule: only the layer just above the one that failed retries. If its retries fail, it returns an "overloaded; don't retry" error, so callers higher up do not retry again. [@sre-overload] Amazon does not say which layer. Google picks the layer just above the failure. The diagram lets the app retry. Either way, only one layer retries.

### Exponential backoff, with a cap

Exponential backoff doubles the wait after each failure. Without a limit, the wait grows without end, so implementations set a maximum wait. [@brooker-bl] The AWS SDKs, for example, cap each backoff at 20 seconds. [@aws-sdk]

Backoff alone is not enough. In Brooker's 2015 simulation, clients that failed together backed off together. They still arrived in clusters, so contention "hasn't been reduced much". [@brooker2015]

### The three jitter rules

Jitter adds randomness to each wait. Here are the three rules from the simulator code behind the 2015 post, rewritten as small functions. [@backoff-sim] The base and cap are the AWS SDK values for transient errors. [@aws-sdk] `n` is the retry number, starting at 0.

```python
import random

BASE, CAP = 0.05, 20.0   # seconds

def expo(n):
    return min(CAP, BASE * 2 ** n)

def full_jitter(n):
    return random.uniform(0, expo(n))

def equal_jitter(n):
    v = expo(n)
    return v / 2 + random.uniform(0, v / 2)

def decorrelated_jitter(prev_sleep):   # start with prev_sleep = BASE
    return min(CAP, random.uniform(BASE, prev_sleep * 3))
```

- **Full jitter** picks the whole wait at random, from 0 up to the capped backoff.
- **Equal jitter** keeps half the backoff and randomizes the other half. No wait is ever very short.
- **Decorrelated jitter** grows from the last random wait, not from the retry number.

In the simulation, with 100 clients competing for the same resource, jitter cut the number of calls by more than half compared with plain backoff. [@brooker2015] Full jitter did the least work. Decorrelated jitter finished slightly sooner but made more calls. Equal jitter was the weakest of the three. [@brooker2015]

In their updated retry behavior, the AWS SDKs use full jitter: `random(0, 1) × min(20,000 ms, base_delay × 2^retry)`. [@aws-sdk] Jitter helps outside retries too. Amazon considers adding jitter to "all timers, periodic jobs, and other delayed work". [@brooker-bl] Restarting tasks, like Google's, need it as well.

### Retry budgets

Backoff spreads retries out. It does not limit their total. When a backend is down for everyone, every request fails, and every request retries. You need a retry budget.

Google uses two limits. Each request gets at most three attempts. Each client also retries only while retries stay below 10% of its requests. [@sre-overload] Without the client budget, load can grow to just under 3 times. With it, load grows about 1.1 times. [@sre-overload] The SRE book also gives a per-process example: 60 retries a minute, then fail instead of retrying. [@sre-cascade]

A token bucket is a common way to build this. [@brooker-bl] Real libraries do it:

- **AWS SDKs, standard mode:** a retry quota of 500 tokens. A retry after a transient error costs 14 tokens. A success with no retry gives 1 back. When the retry quota is empty, the SDK returns the error at once. [@aws-sdk] These numbers are for AWS's updated retry behavior. You opt in with `AWS_NEW_RETRIES_2026=true` until it becomes the default. [@aws-sdk]
- **AWS SDKs, adaptive mode:** adds a client-side rate limiter. It can delay even the first request when the service throttles. [@aws-sdk]
- **gRPC:** for each server, each failed call with a retryable code takes 1 token, and each success adds back `tokenRatio`. Retries stop while the count is at or below half of `maxTokens`. [@grpc-a6]

### Which errors to retry

Retry only errors that may pass. Do not retry permanent errors or malformed requests, because they will never succeed. [@sre-cascade] gRPC goes further: in general, retry only codes that show the server did not handle the request. [@grpc-a6]

Listen to the server. A 503 with `Retry-After` says how long the service expects to be unavailable. The value is seconds, like `Retry-After: 120`, or a date. [@rfc9110] gRPC servers can send the `grpc-retry-pushback-ms` metadata key: a delay in milliseconds, or a negative value for "don't retry". [@grpc-a6]

### Deadlines

Retries must fit inside the caller's time limit. Pass the time left down the chain. If a server has a 30-second deadline and spends 7 seconds, it gives the next server 23 seconds. [@sre-cascade] Do not start a retry that cannot finish in time.

## Trade-offs

| Option | Protects the backend? | Cost | Use it when |
|---|---|---|---|
| Retry at once, no backoff | No. Retries pile onto a sick service. | Nothing to build. | Never, for remote calls. |
| Exponential backoff, no jitter | Partly. Clients still retry in clusters. | A little code. | Rarely. Add jitter. |
| Backoff with full jitter | Yes. Retries spread out evenly. | Some calls wait longer than needed. | Your default for remote calls. |
| Decorrelated jitter | Yes. Similar to full jitter. | Slightly more calls in the 2015 simulation. | You want slightly faster completion. |
| Retry budget (token bucket) | Yes. Total retries stay small. | Some requests fail that one retry could save. | Many clients call a shared service. |
| Retries at every layer | No. Retries multiply. | Hidden until an outage. | Never. Pick one layer. |

**Simple rule:** retry in one layer, with capped backoff and full jitter, under a retry budget. [@brooker-bl] [@sre-overload]

## At work: how a senior uses this

- Draw the call chain in design reviews. Ask: **"Which layer retries? How many attempts in total, top to bottom?"**
- Read your library's defaults. HTTP clients, SDKs, service meshes and queues often retry on their own.
- Ask what happens when the backend is down for everyone, not just for one request. A budget should cap the total.
- Tell callers how to behave when you are overloaded: a 503 with `Retry-After` to say when to come back, or a "don't retry" error so upper layers do not retry.
- Add jitter to cron jobs, cache TTLs, reconnect loops and restarts, not just to request retries.
- Watch the retry rate on a dashboard. A rising retry rate is an early warning of overload.
- Pair retries with circuit breakers, which stop calls to a failing service for a while. See [circuit breakers, bulkheads and fallbacks](/learn/circuit-breakers-and-bulkheads).

## With AI

### What to ask the agent

> "Find every place in this repo that retries a network call: our code, HTTP client settings, SDK configs and service mesh rules. List the layer, max attempts and backoff for each."

> "Wrap this client in a retry policy: at most 3 attempts, full jitter with a 50 ms base and a 20 s cap, and a token-bucket retry budget. Only retry 503, 504 and connection errors. Respect Retry-After."

> "Write a test where the backend fails every call for 60 seconds. Assert that retries stay under 10% of requests."

### What to check yourself

- The wait is random from 0 to the capped backoff. It is not a fixed wait plus a tiny random part.
- The cap is applied before the random pick, not after.
- Only one layer retries. Other layers pass the error up.
- Client errors like 400 and 403 are not retried. A 429 is retried only after a wait: the `Retry-After` value if there is one, or your backoff if not.
- Retries stop when the deadline has passed or the budget is empty.
- Calls that change data are idempotent before they are retried.

## Check yourself

## Go deeper

Related lessons: [Idempotency and retries](/learn/idempotency-retries-timeouts), [Tail latency and queueing](/learn/tail-latency-and-queueing), [Rate limiting](/learn/rate-limiting), [Caching and cache stampedes](/learn/caching-and-cache-stampedes) and [Error handling](/learn/error-handling-defensive-design).

Read the [Google Cloud incident](/incidents#google-cloud-2025-06-service-control) for the rest of the story.
