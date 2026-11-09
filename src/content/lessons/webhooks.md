---
topic: webhooks
subtitle: Webhooks arrive late, twice, out of order, or from someone pretending to be the sender. Here is how to build both ends so none of that hurts.
status: published
reviewed_by: ai-factcheck
reviewed_on: 2026-11-09
illustrative_story: false
sources:
  - id: gh-aug23
    title: "GitHub Availability Report: August 2023"
    url: https://github.blog/news-insights/company-news/github-availability-report-august-2023/
    note: The official report on the 15 August 2023 incident, when webhooks were delayed by up to 4.5 hours.
  - id: stripe-webhooks
    title: "Stripe documentation: Receive Stripe events in your webhook endpoint"
    url: https://docs.stripe.com/webhooks
    note: Signatures with a timestamp, the 5-minute tolerance, retries for up to three days, duplicates, ordering, thin events and secret rolling.
  - id: gh-validate
    title: "GitHub Docs: Validating webhook deliveries"
    url: https://docs.github.com/en/webhooks/using-webhooks/validating-webhook-deliveries
    note: The X-Hub-Signature-256 header, HMAC-SHA256 over the raw payload, and why you never compare with a plain ==.
  - id: gh-best
    title: "GitHub Docs: Best practices for using webhooks"
    url: https://docs.github.com/en/webhooks/using-webhooks/best-practices-for-using-webhooks
    note: Answer within 10 seconds, use a queue, and use X-GitHub-Delivery to spot repeats.
  - id: gh-failed
    title: "GitHub Docs: Handling failed webhook deliveries"
    url: https://docs.github.com/en/webhooks/using-webhooks/handling-failed-webhook-deliveries
    note: GitHub does not retry failed deliveries for you, and how to script redelivery with the REST API.
  - id: gh-redeliver
    title: "GitHub Docs: Redelivering webhooks"
    url: https://docs.github.com/en/webhooks/testing-and-troubleshooting-webhooks/redelivering-webhooks
    note: How to redeliver a failed delivery, and the 3-day limit.
  - id: shopify-about
    title: "Shopify Dev Docs: About webhooks"
    url: https://shopify.dev/docs/apps/build/webhooks
    note: No ordering promise, the X-Shopify-Webhook-Id header, and why you also need reconciliation jobs.
  - id: shopify-trouble
    title: "Shopify Dev Docs: Troubleshoot webhooks"
    url: https://shopify.dev/docs/apps/build/webhooks/troubleshoot
    note: The five-second response limit, eight retries in four hours, and removal of subscriptions that keep failing.
  - id: stdwh
    title: "Standard Webhooks: Open source tools and guidelines for sending webhooks easily, securely and reliably, version 1.0.0 (GitHub, standard-webhooks/standard-webhooks)"
    url: https://github.com/standard-webhooks/standard-webhooks/blob/main/spec/standard-webhooks.md
    note: A shared design for senders, with headers, signing, a multi-day retry schedule, status code handling, thin payloads and SSRF advice.
  - id: owasp-ssrf
    title: "OWASP Cheat Sheet Series: Server Side Request Forgery Prevention Cheat Sheet"
    url: https://cheatsheetseries.owasp.org/cheatsheets/Server_Side_Request_Forgery_Prevention_Cheat_Sheet.html
    note: How to defend a server that calls URLs its users give it, such as webhook URLs.
  - id: rfc9110
    title: "Fielding, Nottingham and Reschke, RFC 9110: HTTP Semantics (IETF, 2022), section 15.3.3"
    url: https://www.rfc-editor.org/rfc/rfc9110#name-202-accepted
    note: What 202 Accepted means, and why its body should point to a status monitor.
claims_checked:
  - claim: "On 15 August 2023 from 16:58 UTC, a sustained spike in webhook deliveries backed up GitHub's webhook job queue; the incident lasted 4 h 29 min, webhooks were delayed up to 4.5 hours, and GitHub mitigated it by blocking events from the sources of the increased load."
    source: gh-aug23
    note: Matches the report text. The per-endpoint isolation advice is labelled as ours and kept separate from GitHub's fix.
  - claim: "Stripe-Signature carries t= and v1=; the signed payload is the timestamp, a dot and the raw body (HMAC-SHA256); the libraries' default tolerance is 5 minutes; each retry gets a new timestamp and signature."
    source: stripe-webhooks
    note: Matches the manual verification and replay-attack sections. The lesson's Node code was run against an openssl-computed test vector and 13 edge cases.
  - claim: "Stripe retries for up to three days with exponential backoff in live mode, does not guarantee event order, and says not to use created to order events."
    source: stripe-webhooks
    note: Matches the Automatic retries and Event ordering sections.
  - claim: "GitHub expects a 2xx within 10 seconds and a redelivery keeps the same X-GitHub-Delivery; GitHub does not automatically redeliver failed deliveries; redelivery is possible only for the past 3 days."
    source: gh-best
    note: Second and third parts confirmed in gh-failed and gh-redeliver.
  - claim: "Shopify fails a delivery not answered within five seconds, retries up to eight times in four hours, and removes subscriptions that keep failing."
    source: shopify-trouble
    note: Matches the Troubleshoot webhooks page (URL updated to /troubleshoot after a 301).
  - claim: "The Standard Webhooks example schedule has ten attempts (waits 5 s to 24 h) ending at about 75.6 hours, recommends a 15 to 30 s timeout, and says to disable an endpoint on 410 Gone."
    source: stdwh
    note: Matches the spec's retry table, request timeouts and status code sections.
quiz:
  - q: Your framework parses the JSON body and you compute the HMAC over the re-serialised object. Real Stripe events now fail the signature check. Why?
    options:
      - The secret has expired.
      - The bytes changed. The signature covers the raw body, so you must check it before any parsing.
      - Stripe signs only the headers.
    answer: 1
    why: Stripe needs the raw request body for verification, and any change to it makes verification fail.
  - q: A request has a valid Stripe signature, but its timestamp is two hours old. What should your endpoint do?
    options:
      - Accept it. The signature is valid.
      - Reject it. It may be a replayed copy, and real retries come with a fresh timestamp.
      - Accept it, but answer 202.
    answer: 1
    why: The timestamp is signed, so an attacker cannot change it. Stripe makes a new timestamp and signature for each retry.
  - q: Your endpoint gets invoice.paid before invoice.created for the same invoice. What is the best response?
    options:
      - Return 500, so the sender retries until the order is right.
      - Fetch the current invoice from the provider's API and act on that.
      - Drop the event. It arrived in the wrong order.
    answer: 1
    why: Stripe does not promise order and suggests using the API to get missing objects.
  - q: Your handler needs 20 seconds to do its work. GitHub waits about 10. What should you change?
    options:
      - Verify, save the event to a queue, and answer 2xx at once. A worker does the rest.
      - Ask users to raise GitHub's timeout.
      - Answer 2xx before you check the signature.
    answer: 0
    why: GitHub expects a 2xx within 10 seconds and suggests a queue to process payloads in the background.
  - q: A customer sets their webhook URL to an address that resolves to 169.254.169.254. What should your sender do?
    options:
      - Send it. The customer chose the URL.
      - Block it. Webhook calls must not reach internal or metadata addresses.
      - Send it, but without a signature.
    answer: 1
    why: This is SSRF. OWASP says attackers use it to steal credentials from cloud metadata services.
incidents: [github-2023-08-webhook-delays]
glossary_optout: [rfc, process, cap-theorem]
related: [rest-api, idempotency-retries, payment-lifecycle, sagas-outbox-cdc, queues-vs-logs, backoff-jitter, owasp-secure-coding, rate-limiting, reconciliation]
---

## What broke

On 15 August 2023, at 16:58 UTC, an internal GitHub job queue started to fall behind. That queue processes webhooks. [@gh-aug23]

The cause was a large and lasting spike in webhook deliveries. Customers waited up to 4.5 hours for some webhooks. GitHub fixed it by blocking events from the sources of the extra load. Then the backlog drained slowly. [@gh-aug23]

Think about what that means for a receiver. Your CI bot or deploy tool waits for a push event. The event does not come for hours. Nothing is broken on your side, so no alert fires.

When the backlog drains, you could get hundreds of old events close together. Your handler must not run them all twice, run them in the wrong order, or fall over.

Webhooks fail in four ways: late, twice, out of order, and fake. This lesson shows how to build both ends so each failure is boring.

## The idea in simple words

A **webhook** is the reverse of an API call. You do not ask the provider "did anything change?" again and again. The provider calls your URL when something happens. You first met them in [REST API design](/learn/rest-api-design).

The provider's promise is weak. Most providers retry, so delivery is **at-least-once**: an event can come late, twice, or before an older event. Some, like GitHub, do not retry at all, so an event can also be lost. [@gh-failed] Anyone on the internet can also post to your URL.

> [!key] **The key idea.** Treat each webhook as a signed hint, not as the truth. Check who sent it, save it, answer fast, skip repeats, and read the current state before you act.

## How it works

Step through one delivery that times out and comes back. Use Next and Back, or the ← and → keys.

[[diagram:webhook-retry-dedupe]]

### The receiver

**1. Verify the signature.** The sender signs each request with an **HMAC**: a hash of the body made with a secret only you and the sender know. GitHub puts it in `X-Hub-Signature-256`, as `sha256=` plus a hex digest of the payload. [@gh-validate] Shopify uses `X-Shopify-Hmac-Sha256`. [@shopify-about]

Compute the HMAC over the **raw body**, byte for byte. If your framework parses and rewrites the JSON first, the check fails. [@stripe-webhooks] Compare in constant time. GitHub says: "Never use a plain `==` operator." [@gh-validate]

**2. Reject old timestamps.** A valid signature alone does not stop a **replay attack**. Someone can copy a real request and send it again. So Stripe signs a timestamp too. Its header, `Stripe-Signature`, holds `t=` and `v1=`. The signed text is the timestamp, a dot, then the body. [@stripe-webhooks]

Stripe's libraries reject anything older than 5 minutes by default. Each retry gets a new timestamp and signature, so real retries still pass. [@stripe-webhooks] Standard Webhooks also signs a timestamp. It uses three headers: `webhook-id`, `webhook-timestamp` and `webhook-signature`. It signs the id, timestamp and body together, and leaves the tolerance to you. [@stdwh]

Here is a manual Stripe-style check in Node. In real code, prefer the provider's library.

```ts
import { createHmac, timingSafeEqual } from "node:crypto";

const TOLERANCE_SEC = 300; // 5 minutes

export function verify(raw: Buffer, header: string, secret: string) {
  const parts = header.split(",").map((p) => p.split("="));
  const t = parts.find(([k]) => k === "t")?.[1];
  const sigs = parts.filter(([k]) => k === "v1").map(([, v]) => v);
  if (!t || sigs.length === 0) return false;

  const age = Math.abs(Date.now() / 1000 - Number(t));
  if (!(age <= TOLERANCE_SEC)) return false; // too old: maybe a replay

  const expected = createHmac("sha256", secret)
    .update(`${t}.`).update(raw).digest();
  return sigs.some((s) => {
    const got = Buffer.from(s, "hex");
    return got.length === expected.length && timingSafeEqual(got, expected);
  });
}
```

**3. Answer 2xx fast, then work.** GitHub wants a 2xx within 10 seconds, or it marks the delivery failed. [@gh-best] Shopify gives you five seconds. [@shopify-trouble] So verify, save the event to a queue, and answer. A worker does the real work. Stripe says a queue also protects you from spikes, like the start of the month when all subscriptions renew. [@stripe-webhooks]

**4. Skip repeats by event id.** Save each processed id and skip ids you have seen. [@stripe-webhooks] Use `X-GitHub-Delivery` on GitHub, `X-Shopify-Webhook-Id` on Shopify, and `webhook-id` in Standard Webhooks. [@gh-best] [@shopify-about] [@stdwh] Insert the id with a unique constraint, in the same transaction as the work. This is the [idempotency key](/learn/idempotency-retries-timeouts) idea, from the other side.

**5. Do not trust order.** Stripe does not promise order. It also says not to use the `created` time to order events. [@stripe-webhooks] Shopify says a `products/update` can come before `products/create`. [@shopify-about] So store a version or `updated_at` with your copy, and ignore anything older. Or fetch the current object from the API.

**6. Prefer thin events.** A **thin event** says only "object X changed". You then fetch the object. Stripe recommends thin events for new endpoints. Its SDK has a method, `fetchRelatedObject()`, that gets the latest version of the object. [@stripe-webhooks] Fetching also fixes ordering: you always act on the current state.

### The sender

**1. Never lose an event.** Write the event to a [transactional outbox](/learn/sagas-outbox-and-cdc) in the same transaction as the change. A delivery worker reads the outbox and sends.

**2. Retry for days, with backoff and jitter.** Standard Webhooks gives an example schedule of ten tries over about three days. The first is at once. The waits after that are 5 s, 5 min, 30 min, 2 h, 5 h, 10 h, 14 h, 20 h and 24 h. [@stdwh] Stripe retries for up to three days in live mode. [@stripe-webhooks] Shopify retries eight times in four hours. [@shopify-trouble] GitHub does not redeliver failed deliveries automatically. [@gh-failed] See [backoff and jitter](/learn/backoff-jitter-retry-budgets).

**3. Isolate each endpoint.** Our advice: give each endpoint its own queue, or a cap on in-flight requests. Then one customer's slow server cannot hold workers that others need. Use a timeout too. Standard Webhooks suggests 15 to 30 seconds. [@stdwh]

GitHub faced a related problem in 2023. A spike from some sources slowed everyone, and it blocked events from those sources. Afterwards, it improved its ability to manage load sources. [@gh-aug23]

**4. Disable dead endpoints.** After long, steady failure, tell the customer by email and stop sending. On `410 Gone`, disable the endpoint at once. [@stdwh] Shopify removes a subscription that keeps failing. [@shopify-trouble]

**5. Offer replay.** Customers recover from outages by asking for events again. Stripe lets you resend from the Dashboard for 15 days, and from the CLI for 30 days. [@stripe-webhooks] A GitHub redelivery keeps the same `X-GitHub-Delivery` value, so dedupe still works. [@gh-best]

**6. Rotate secrets with an overlap.** When Stripe rolls a secret, the old one can stay valid for up to 24 hours. Meanwhile it sends one signature per secret. [@stripe-webhooks] Standard Webhooks puts a space-separated list of signatures in one header for the same reason. [@stdwh]

**7. Block SSRF.** Customers type in URLs, and your servers call them. An attacker can use this for [server-side request forgery](/learn/owasp-top-10-secure-coding). [@stdwh] Check that the resolved IP is not private. Do not follow redirects. [@owasp-ssrf] Send through a proxy that filters internal IPs, from a subnet that cannot reach internal services. [@stdwh]

### Async APIs

Webhooks are one way to report a slow job. Another is **202 Accepted**: the job was accepted, but is not done. RFC 9110 says the body should point to a status monitor. [@rfc9110] The client polls that URL. A third way is streaming, such as Server-Sent Events, which keeps one connection open.

## Trade-offs

| Option | Who keeps the connection | Works if the client has no public URL? | Main cost | Use it when |
|---|---|---|---|---|
| 202 + status URL (polling) | The client asks again and again. | Yes. | Wasted calls, and a delay between checks. | Jobs of seconds or minutes, browser or mobile clients. |
| Webhooks | The sender calls you. | No. You need a public HTTPS endpoint. | Signatures, retries, dedupe, SSRF guards. | Server-to-server events between companies. |
| Streaming (SSE, WebSocket) | One long open connection. | Yes. | Reconnects, and missed events while offline. | Live screens that must update in a second. |

**Simple rule:** use webhooks for the push and a periodic [reconciliation](/learn/reconciliation) job for the truth. Shopify says your app should not rely only on webhooks. [@shopify-about]

## At work: how a senior uses this

- Ask of every handler: **"What happens if this event comes twice, late, or before the one it depends on?"**
- Ask: **"If we miss a whole day of events, how do we catch up?"** Have a backfill or reconcile job ready. On GitHub, which does not retry, you write a script that finds failed deliveries and redelivers them. [@gh-failed] You can only redeliver deliveries from the past 3 days. [@gh-redeliver]
- Log and alert on signature failures. A sudden rise means a broken deploy, a rotated secret, or an attack.
- Measure the age of events in your webhook queue, not only errors. Late events are silent.
- As a sender, add a delivery log per endpoint that customers can see and replay from. [@stdwh]
- In design reviews of a sender, ask: **"Can one customer's slow endpoint delay everyone else?"**
- For payments, read [the payment lifecycle](/learn/payment-lifecycle). The webhook is often the only place you learn a payment settled.

## With AI

### What to ask the agent

> "Write an Express route for Stripe webhooks. Read the raw body, verify the signature with the official library, insert the event id with a unique constraint, push the event to a queue, and return 200. Do no other work in the route."

> "Review this webhook sender. Find every place where one slow endpoint can block deliveries to other endpoints, and every way a customer URL can reach our internal network."

> "Write tests for this handler: duplicate event, old timestamp, wrong signature, and an update event that arrives before its create event."

### What to check yourself

- The signature is checked on the raw bytes, before any JSON parsing or body middleware.
- The compare is constant-time, such as `crypto.timingSafeEqual` or Python's `hmac.compare_digest`. [@gh-validate]
- The timestamp tolerance is not 0. Stripe warns that 0 turns the recency check off. [@stripe-webhooks]
- The dedupe insert and the work share one transaction. A check-then-insert in two steps lets two copies through.
- The handler does not set a state straight from the payload. It fetches the object or compares versions.
- The sender's HTTP client blocks private IPs on every request and does not follow redirects.

## Check yourself

## Go deeper

- [Idempotency, retries and timeouts](/learn/idempotency-retries-timeouts): the same dedupe idea for normal API calls.
- [Sagas, outbox and CDC](/learn/sagas-outbox-and-cdc): how to publish events without losing them.
- [Queues vs logs](/learn/queues-vs-logs-delivery-guarantees): at-least-once delivery inside your own system.
- [Rate limiting](/learn/rate-limiting): how to protect your receiver when a backlog drains.
