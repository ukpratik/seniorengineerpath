---
topic: idempotency-retries
subtitle: Why one retry can charge a customer twice, and the small key that stops it.
status: published
illustrative_story: true
sources:
  - id: stripe
    title: "Stripe API reference: Idempotent requests"
    url: https://docs.stripe.com/api/idempotent_requests
    note: How a real payment API stores keys, replays saved results and expires keys.
  - id: brooker
    title: "Marc Brooker, “Timeouts, retries and backoff with jitter”, Amazon Builders' Library"
    url: https://builder.aws.com/content/3EumjoZascWd1oZiEgL8ORlv3qE/timeouts-retries-and-backoff-with-jitter
    note: Why retries multiply across layers, and how jitter spreads them out.
  - id: ddia
    title: "Martin Kleppmann, Designing Data-Intensive Applications (O'Reilly)"
    where: "1st edition (2017): chapter 8 on unreliable networks and timeouts; chapter 12 on end-to-end request IDs."
reviewed_by: ai-factcheck
reviewed_on: 2026-10-21
claims_checked:
  - claim: "Stripe keeps idempotency keys for at least 24 hours."
    source: stripe
    note: "Idempotent requests page: for API v1, keys can be removed once they are at least 24 hours old; a reused key after pruning makes a new request. Wording kept as 'at least 24 hours'."
  - claim: "The server saves the result for a key and returns it to retries; a reused key with different parameters is rejected; a request that clashes with one still running is not run twice."
    source: stripe
    note: "Idempotent requests page: v1 saves status code and body once execution begins and replays it (even 500s); compares parameters and errors if different; a request conflicting with a concurrent one gets no saved result and can be retried. Added that the app can retry later."
  - claim: "Retries multiply across layers, so retry in one layer only; one tap can become 3 × 3 × 3 = 27 calls."
    source: brooker
    note: "Section 'Retries and backoff': five layers with three retries each give 243x database load; best practice is to retry at a single point in the stack. The 27 is our own arithmetic on the same rule."
  - claim: "Jitter spreads retries out so clients don't all retry at once; every remote call needs a timeout because a stuck call holds resources."
    source: brooker
    note: "Sections 'Jitter' (randomness spreads retries in time) and 'Timeouts' (set a timeout on any remote call; waiting holds memory, threads, connections)."
  - claim: "A timeout does not tell you whether the work happened (DDIA ch 8); end-to-end request IDs stop duplicates (ch 12)."
    source: ddia
    note: "Checked via secondary sources (chapter lists and reader notes): ch 8 'The Trouble with Distributed Systems' covers unreliable networks and timeouts; ch 12 'The Future of Data Systems' covers end-to-end operation IDs. O'Reilly TOC page returned 403."

quiz:
  - q: The app's Pay request times out. What do you know for sure?
    options:
      - The payment failed.
      - The payment worked.
      - Nothing. The server may or may not have charged the card.
    answer: 2
    why: A timeout only means no answer came back in time. The work may have happened.
  - q: When should the app create the idempotency key?
    options:
      - Once per payment, before the first try.
      - Again for each retry.
      - The server creates it after charging.
    answer: 0
    why: Every retry must carry the same key, or the server can't tell it is a repeat.
  - q: The app, the gateway and the payment service each try up to 3 times. How many charge calls can reach the bank from one tap?
    options: ["3", "9", "27"]
    answer: 2
    why: 3 × 3 × 3 = 27. Retries multiply across layers, so retry in one layer only.
incidents: [aws-2021-12-us-east-1]
related: [backoff-jitter]
---

## What broke

Priya is on a train with weak mobile data. She taps **Pay ₹1,499** in a shopping app. The request reaches the payment server, and the server charges her card.

The answer back to her phone is lost. After 10 seconds the app hits its timeout. It keeps the spinner on and sends the same request again: an automatic retry.

The server sees a new request and charges the card again. Priya now has two charges of ₹1,499. Support spends a day finding and refunding the double charges.

## The idea in simple words

A retry is safe only if doing something twice has the same effect as doing it once. That property has a name: idempotent.

Reading a balance is idempotent. "Set the order status to shipped" is idempotent. "Charge ₹1,499" is *not*: each call takes more money.

> [!key] **The key idea.** A timeout tells you that you did not get an answer. It does not tell you that the work did not happen. [@ddia]

So we make the charge idempotent. The app creates one unique idempotency key for the payment and sends it with every attempt. The server remembers each key and its result. If the same key comes again, the server sends back the saved result instead of charging again. [@stripe]

## How it works

Step through what happens when the answer is lost. Use Next and Back, or the ← and → keys.

[[diagram:idempotency-retry]]

Two details matter in real systems. If a second request with the same key arrives while the first is still running, the server must not start a second charge. Stripe returns an error, and the app can retry later. And if the same key arrives with a different amount, the server should reject it: that is a bug in the app. [@stripe]

## Trade-offs

| Approach | Safe to retry? | Cost | Use it when |
|---|---|---|---|
| Never retry | Yes. Nothing runs twice. | Users see more failed payments. | A person can easily check and try again. |
| Retry, no key | No. It can charge twice. | Cheap to build. | The call is already idempotent, like a read or "set status to shipped". |
| Retry with an idempotency key | Yes. Repeats get the saved result. | You store keys, handle "still running", and delete old keys. | Payments, orders, anything that creates something. |
| Key + backoff with jitter | Yes. And retries are spread out. | One user may wait a little longer. | Many clients may retry at the same moment, for example after an outage. |

**Rule of thumb:** add a key to every call that creates or moves something. Add backoff with jitter to every retry loop. [@brooker]

## At work: how a senior uses this

- In design reviews, ask: **"What happens if this call runs twice?"** Ask it for every call that creates something or moves money.
- Put a timeout on every network call. A call without one can wait forever and hold a thread or a connection. [@brooker]
- Retry in one layer only. If the app, the gateway and the service each try 3 times, one tap can become 3 × 3 × 3 = 27 calls. [@brooker]
- Wait longer between tries with exponential backoff and jitter, so clients don't all retry in the same second.
- Keep keys long enough to cover every retry. Stripe's API, for example, keeps them for at least 24 hours. [@stripe]

## With AI

### What to ask the agent

> "Add an Idempotency-Key header to the create-payment call. Create the key once per payment, before the retry loop."

> "Write a test where the first response is lost and the client retries with the same key. Assert that the card is charged exactly once."

### What to check yourself

- The key is made **outside** the retry loop, not inside it.
- The server saves the key and the payment in the same database transaction.
- A second request with the same key, while the first still runs, does not charge again.
- Retries happen in one layer, with a limit and backoff.
- Keys belong to one customer, so nobody can read another person's result.

## Check yourself

## Go deeper

Coming next on this line: [Backoff, jitter and retry budgets](/learn/backoff-jitter-retry-budgets).
