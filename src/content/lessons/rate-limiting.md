---
topic: rate-limiting
subtitle: A rate limit says no early and cheaply, so one noisy client cannot take your service down for everyone else.
status: published
reviewed_by: ai-factcheck
reviewed_on: 2026-10-21
illustrative_story: true
sources:
  - id: stripe
    title: "Paul Tarjan, “Scaling your API with rate limiters”, Stripe blog, 2017"
    url: https://stripe.com/blog/rate-limiters
    note: Stripe's four limiters, the token bucket in Redis, and how to roll out limits safely.
  - id: cf-sliding
    title: "Cloudflare, “How we built rate limiting capable of scaling to millions of domains”, Cloudflare blog, 2017"
    url: https://blog.cloudflare.com/counting-things-a-lot-of-different-things/
    note: The sliding window counter, with a worked example and accuracy measured on real traffic.
  - id: rfc6585
    title: "Nottingham and Fielding, RFC 6585: Additional HTTP Status Codes (IETF, 2012), section 4"
    url: https://www.rfc-editor.org/rfc/rfc6585#section-4
    note: The definition of 429 Too Many Requests.
  - id: rfc9110
    title: "Fielding, Nottingham and Reschke, RFC 9110: HTTP Semantics (IETF, 2022), section 10.2.3"
    url: https://www.rfc-editor.org/rfc/rfc9110#field.retry-after
    note: The Retry-After header and its two formats.
  - id: nginx
    title: "NGINX documentation, “Module ngx_http_limit_req_module”"
    url: https://nginx.org/en/docs/http/ngx_http_limit_req_module.html
    note: A real leaky bucket limiter, with burst, nodelay and the status code it returns.
  - id: redis-incr
    title: "Redis documentation, “INCR”, section “Pattern: rate limiter”"
    url: https://redis.io/docs/latest/commands/incr/
    note: Two counter-based limiters in Redis, and the race condition to avoid.
  - id: envoy-global
    title: "Envoy proxy documentation, “Global rate limiting”"
    url: https://www.envoyproxy.io/docs/envoy/latest/intro/arch_overview/other_features/global_rate_limiting
    note: A shared rate limit service backed by Redis, and why to add a local limit in front of it.
  - id: github
    title: "GitHub Docs, “Rate limits for the REST API”"
    url: https://docs.github.com/en/rest/using-the-rest-api/rate-limits-for-the-rest-api
    note: A public API's limits, headers and advice for clients that hit them.
  - id: cf-throttle
    title: "Cloudflare, “How Cloudflare erroneously throttled a customer’s web traffic”, Cloudflare blog, 2023"
    url: https://blog.cloudflare.com/how-cloudflare-erroneously-throttled-a-customers-web-traffic/
    note: A manual throttle that made one customer's site unusable, and the process fixes that followed.
claims_checked:
  - claim: "Stripe uses a token bucket in Redis, runs 4 kinds of limiters (request rate, concurrent requests with an example of 20, two load shedders), dark launches limits, keeps a kill switch and fails open if Redis goes down."
    source: stripe
    note: "Paul Tarjan, 30 Mar 2017. Post says 'catching exceptions at all levels so that any coding or operational errors would fail open'. Load shedder wording changed from 'whole fleet' to 'the whole system'."
  - claim: "Sliding window counter: limit 50/min, 42 last minute, 18 this minute 15 s in, estimate 42 x 0.75 + 18 = 49.5; on 400 million requests only 0.003% were wrongly allowed or limited."
    source: cf-sliding
    note: "Matches the worked example and the measurement (400M requests from 270,000 sources). Arithmetic re-done: 31.5 + 18 = 49.5."
  - claim: "NGINX limit_req uses the leaky bucket method; rate=1r/s burst=5 allows 1 r/s on average with bursts up to 5; nodelay stops delaying; rejected requests get 503 by default, set by limit_req_status."
    source: nginx
    note: "All match the module docs: 'leaky bucket' method, 'Excessive requests are delayed until their number exceeds the maximum burst size', limit_req_status default 503."
  - claim: "429 means too many requests in a given time and may carry Retry-After; Retry-After is an HTTP-date or delay-seconds (example Retry-After: 120)."
    source: rfc9110
    note: "RFC 6585 section 4 and RFC 9110 section 10.2.3, both read in full."
  - claim: "GitHub REST API: 5,000 requests an hour for a user; headers x-ratelimit-remaining and x-ratelimit-reset; for secondary limits wait for retry-after, else the reset time, else at least one minute."
    source: github
    note: "Current page checked 2026-10-21. Primary limit: wait until x-ratelimit-reset. Wording now ties retry-after advice to secondary limits."
  - claim: "Cloudflare, Feb 2023: an unapproved manual throttle made one customer's website unusable; it was lifted after 12 h 53 min; future actions need multiple levels of approval."
    source: cf-throttle
    note: "Post published 7 Feb 2023, incident 2 Feb. 'Almost 13 hours' is how long the throttle was on, so the lesson now says that."
quiz:
  - q: A token bucket holds 10 tokens and adds 2 tokens a second. The bucket is full. A client sends 15 requests at once. How many go through right away?
    options:
      - "2"
      - "10"
      - "15"
    answer: 1
    why: The bucket holds 10 tokens, so 10 requests go through. The other 5 are rejected. New tokens then arrive at 2 a second.
  - q: Your limit is 100 requests per minute with a fixed window. What is the worst case?
    options:
      - 100 requests in any minute.
      - About 200 requests in two seconds, around the edge of two windows.
      - 50 requests per minute.
    answer: 1
    why: A client can send 100 at the end of one window and 100 at the start of the next.
  - q: You run 20 servers. Each keeps its own in-memory limit of 100 requests per minute per user. What is the real limit for one user?
    options:
      - 100 per minute.
      - Up to 2,000 per minute.
      - 5 per minute.
    answer: 1
    why: Each server counts alone. A user whose requests spread over all 20 servers can make 20 × 100.
  - q: 'What should a well-behaved client do when it gets 429 with "Retry-After: 30"?'
    options:
      - Retry at once, in a loop.
      - Wait at least 30 seconds before it tries again.
      - Treat it as a bug in the server and give up forever.
    answer: 1
    why: Retry-After tells the client how long to wait. Retrying sooner only adds load.
  - q: Your shared Redis for rate limits goes down. What does Stripe advise?
    options:
      - Block all requests until Redis is back.
      - Fail open, so a limiter problem does not break real requests.
      - Restart every API server.
    answer: 1
    why: The limiter protects the service. It should not become a new way for the service to fail.
incidents: [cloudflare-2023-02-customer-throttle]
related: [api-gateway, load-shedding, backoff-jitter, redis, abuse-prevention, resilience-patterns]
---

## What broke

This story is made up, but it is common. A small company sells a public API for sending text messages. Most customers send a few requests a second.

One Monday, a new customer starts a data import. Their script has a bug: when a request is slow, it retries at once, in a loop, with no wait. Soon this one customer sends 3,000 requests a second.

The API servers pass every request to the database. The database slows down, so requests get slower. Slower requests make the script retry even more.

Within ten minutes, every customer sees timeouts. The team blocks the customer by hand, and things recover. Then the next question comes: "Why could one customer do this at all?"

There was no rate limit. The service said yes to every request until it could not say anything.

## The idea in simple words

A rate limit is a rule like "each API key may make 100 requests a minute". The service counts requests. When a client goes over the limit, the service rejects the extra requests at once, before doing any real work.

A rate limit protects you in three ways:

1. **Safety.** One client, a bug or an attack cannot use all your capacity.
2. **Fairness.** Every customer gets their share.
3. **Cost.** You do not pay for work that nobody planned for.

> [!key] **The key idea.** A fast, cheap "no" is better than a slow "yes" that ends in a timeout. Rejecting early keeps the service healthy for everyone else.

## How it works

### Token bucket

Picture a bucket of tokens for each client. Each request takes one token. Tokens drip back in at a fixed rate, up to the size of the bucket. If the bucket is empty, the request is rejected.

Two numbers set the rule. The bucket size sets the biggest burst. The refill rate sets the average. Stripe uses a token bucket for its main limiter, with the buckets stored in Redis. [@stripe]

Step through a burst. Use Next and Back, or the ← and → keys.

[[diagram:token-bucket-burst]]

Now try it yourself. Change the bucket size and refill rate, then send a burst.

[[anim:token-bucket]]

### Leaky bucket

A leaky bucket smooths traffic instead of allowing bursts. Requests join a queue, and the queue drains at a fixed rate. When the queue is full, new requests are rejected.

NGINX's `limit_req` module uses this method. With `rate=1r/s burst=5`, NGINX lets through 1 request a second on average. Up to 5 extra requests wait in line, and more than that get an error. With `nodelay`, the queued requests are not delayed. By default the error is 503, but you can change it with `limit_req_status`. [@nginx] Set it to 429, so clients know they hit a limit.

### Fixed window

Count requests per client per minute. The key could be `user42:12:01`. When the minute ends, start a new counter.

In Redis this is a few lines: `INCR` the key and set it to expire. [@redis-incr] It is cheap and easy to explain to customers.

But the edges are weak. With a limit of 100 a minute, a client can send 100 requests at 12:00:59 and 100 more at 12:01:00. That is 200 requests in two seconds.

### Sliding window

A **sliding window log** stores the time of every request. To decide, it counts the times in the last 60 seconds. It is exact, but it stores one entry per request, which uses a lot of memory for busy clients.

A **sliding window counter** keeps only two counters: this minute and last minute. It guesses the rate by weighting last minute's count by how much of it is still inside the window.

Cloudflare gives an example. The limit is 50 a minute. Last minute had 42 requests, and this minute has 18 so far, 15 seconds in. The estimate is 42 × 0.75 + 18 = 49.5, just under the limit. [@cf-sliding] It is only an estimate. But on 400 million requests, Cloudflare found just 0.003% were wrongly allowed or wrongly limited. [@cf-sliding]

### Limits across many servers

If each server counts in its own memory, the limit multiplies. With 20 servers and a limit of 100, a user can make up to 2,000 requests.

So most teams keep counters in a shared store like Redis. Be careful with races. If you `INCR` a key and set its expiry in a separate step, a crash between them can leave the key with no expiry. Redis suggests `MULTI`/`EXEC` or a small Lua script, so both happen together. [@redis-incr]

A shared store adds a network call to every request, so it can become a bottleneck. Envoy's answer is a separate rate limit service, plus a local token bucket on each proxy. Its reference service uses Redis. The local limit absorbs large bursts and cuts the load on the shared service. [@envoy-global]

What if Redis goes down? Stripe's advice is to fail open: catch the error and let the request through. [@stripe] The limiter should never become the outage.

### Where to enforce it

- **At the edge or API gateway.** Reject by IP address or API key before any real work. This is cheap and stops floods early.
- **In the service.** The service knows the user, their plan and how costly each call is. A search can cost much more than a read by ID.

Our advice is to use both: a coarse limit at the gateway and finer limits in the service.

### Per-user and global limits

A per-user limit keeps things fair. A global limit protects a shared resource, such as the database, whoever the traffic comes from.

Stripe runs four kinds. A request rate limiter caps each user's requests per second. A concurrent requests limiter caps how many requests can run at once, such as 20. Two load shedders protect the whole system: one keeps capacity for critical requests, and the other sheds traffic when workers are overloaded. [@stripe]

### Telling the client

The HTTP status for this is 429 Too Many Requests. The response may include a `Retry-After` header that says how long to wait. [@rfc6585] `Retry-After` can be a number of seconds, like `Retry-After: 120`, or a date. [@rfc9110]

Tell clients their budget too. GitHub's REST API sends headers like `x-ratelimit-remaining` and `x-ratelimit-reset`. A logged-in user gets 5,000 requests an hour. [@github] After a primary limit, GitHub says to wait until the reset time. After a secondary limit, wait for `retry-after` if sent. Otherwise wait for the reset time, or at least one minute. [@github]

## Trade-offs

| Algorithm | Allows bursts? | Memory per client | Accuracy | Use it when |
|---|---|---|---|---|
| Token bucket | Yes, up to the bucket size. | Two numbers. | Good. | Most APIs. Clients are bursty but you want a fair average. |
| Leaky bucket | No. Output is smooth. | A queue. | Good. | You must protect a backend that needs a steady rate. |
| Fixed window | Yes, up to 2x at window edges. | One counter. | Weak at the edges. | Simple limits, like daily quotas. |
| Sliding window log | Up to the limit, never more in any window. | One entry per request. | Exact. | Low limits where exactness matters, like login attempts. |
| Sliding window counter | Up to about the limit. | Two counters. | Close estimate. | High traffic, many clients. |

**Our rule of thumb:** use a token bucket per API key in a shared store, plus a coarse limit at the gateway.

## At work: how a senior uses this

- Ask about every public endpoint: **"What stops one client from using all of it?"**
- Choose the key with care: API key, user, account or IP address. Many users can share one office IP address.
- Dark launch a new limit first. Log who would be blocked, then turn it on. Stripe does this, and adds a kill switch. [@stripe]
- Put limits in your API docs. Send 429 with `Retry-After`, and headers that show what is left.
- Alert when limits fire a lot. It can mean an attack, a client bug or a limit that is too low.
- Treat limiting a customer as a product decision, with an owner and a clear process. In 2023, an unapproved throttle applied by hand made one Cloudflare customer's site unusable. It stayed on for almost 13 hours. Afterwards, Cloudflare said such actions would need several levels of approval. [@cf-throttle] See the [incident](/incidents#cloudflare-2023-02-customer-throttle).
- Check your own clients: they should back off with jitter, not retry in a loop.

## With AI

### What to ask the agent

> "Add a token bucket rate limiter to this API, per API key, stored in Redis. Use a Lua script so the check and the update happen together. Return 429 with Retry-After."

> "Write tests for the limiter: a burst up to the bucket size passes, the next request gets 429, and one request passes after one refill interval."

> "Make the limiter fail open if Redis is down or slow for more than 20 ms. Log and count every fail-open."

### What to check yourself

- The read and the update of the counter happen in one atomic step, not two.
- Every Redis key has an expiry, so idle clients do not fill memory.
- The limit key is right: per API key, not per server, and not shared by accident.
- Retry-After is in seconds and matches when a token will really be free.
- The agent did not use the server's local clock in a way that breaks across many servers.

## Check yourself

## Go deeper

Related lessons: [API gateways](/learn/api-gateway-and-edge), [Load shedding](/learn/load-shedding-graceful-degradation), [Backoff and jitter](/learn/backoff-jitter-retry-budgets) and [Redis in depth](/learn/redis-in-depth).
