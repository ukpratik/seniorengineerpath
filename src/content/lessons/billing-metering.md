---
topic: billing-metering
subtitle: Count every unit of usage once, put it in the right billing period, and turn it into an invoice you can prove.
status: published
illustrative_story: true
sources:
  - id: stripe-meter-event
    title: "Stripe API reference, “Create a billing meter event”"
    url: https://docs.stripe.com/api/billing/meter-event/create
    note: The fields of one usage event, the identifier Stripe uses to drop duplicates, and the allowed timestamp range.
  - id: stripe-record-usage
    title: "Stripe documentation, “Record usage for billing with the API”"
    url: https://docs.stripe.com/billing/subscriptions/usage-based/recording-usage-api
    note: How to send usage, why totals lag behind events, and why an accepted event can still miss its invoice.
  - id: stripe-meters
    title: "Stripe documentation, “Create and configure a meter”"
    url: https://docs.stripe.com/billing/subscriptions/usage-based/meters/configure
    note: Sum, count and last aggregation, pre-aggregated ingestion by UTC hour or day, and how to cancel wrong events.
  - id: stripe-grace
    title: "Stripe documentation, “Configure an invoice finalization grace period”"
    url: https://docs.stripe.com/billing/subscriptions/usage-based/configure-grace-period
    note: The window after a period ends in which late usage still reaches the invoice, and its limits.
  - id: stripe-prorations
    title: "Stripe documentation, “Prorations”"
    url: https://docs.stripe.com/billing/subscriptions/prorations
    note: How a mid-period plan change becomes a credit and a charge, to the second, and why usage is not prorated.
  - id: openmeter-events
    title: "OpenMeter documentation, “Usage Events”"
    url: https://openmeter.io/docs/metering/events/usage-events
    note: Usage events in the CloudEvents format, and deduplication by event id and source.
  - id: lago-ingest
    title: "Lago documentation, “Ingest usage”"
    url: https://getlago.com/docs/guide/events/ingesting-usage
    note: The transaction_id dedupe key, a trap where a changed timestamp bills a retry twice, and late events.
  - id: lago-aggregation
    title: "Lago documentation, “Aggregation types: how usage is measured and counted”"
    url: https://getlago.com/docs/guide/billable-metrics/aggregation-types/overview
    note: Count, count unique, sum, max, latest and weighted sum, each in one line.
  - id: lago-graduated
    title: "Lago documentation, “Graduated”"
    url: https://getlago.com/docs/guide/plans/charges/charge-models/graduated
    note: Price tiers with a short worked example.
  - id: lago-charges
    title: "Lago documentation, “Usage based charge”"
    url: https://getlago.com/docs/guide/plans/charges/usage-based-charges
    note: Charges with up to 15 decimal places, and rounding to the currency's smallest unit on the invoice.
  - id: lago-wallets
    title: "Lago documentation, “Prepaid credits and wallets: bill usage against a balance”"
    url: https://getlago.com/docs/guide/wallet-and-prepaid-credits/overview
    note: Prepaid credits, when they come off an invoice, the live balance and expiry.
  - id: lago-usage
    title: "Lago documentation, “Retrieve usage”"
    url: https://getlago.com/docs/guide/events/retrieve-usage
    note: Current, projected and past usage, and why closed periods never change.
claims_checked:
  - claim: Stripe enforces a unique meter-event identifier within a rolling period of at least 24 hours, and accepts event timestamps up to 35 calendar days in the past (or 5 minutes in the future).
    source: stripe-meter-event
    note: Both checked against the identifier and timestamp parameter descriptions on the API reference page.
  - claim: Stripe's invoice finalization grace period defaults to 1 hour and can be set up to 72 hours; usage reported after it is not added to the finalized invoice or the next one; after a mid-cycle price change, grace-period usage reaches no invoice.
    source: stripe-grace
    note: Default, maximum and both limitations checked on the page.
  - claim: On Lago's ClickHouse event store the dedupe key includes the timestamp, so a retry with the same transaction_id but a different timestamp is billed again.
    source: lago-ingest
    note: Stated on the Ingest usage page, with the advice to resend the original payload.
  - claim: OpenMeter deduplicates CloudEvents usage events by id and source.
    source: openmeter-events
    note: No dedupe time window is given on the page.
  - claim: Stripe prorates to the second by default (a £10 to £20 change at half-period gives a £5 credit and £10 charge), and usage-based billing is not prorated.
    source: stripe-prorations
    note: Example, granularity and the usage-based exception checked on the page.
reviewed_by: ai-factcheck
reviewed_on: 2026-11-08
quiz:
  - q: Your service sends a usage event. The request times out, so it retries. Each retry builds the event again with a fresh UUID. What happens?
    options:
      - Nothing bad. Dedupe catches the retries.
      - The customer can be billed for the same usage more than once, because each copy has a new id.
      - The billing system rejects the retries.
    answer: 1
    why: Dedupe only works if a copy has the same id as the original. Create the id once, when the usage happens, and resend the same payload on every retry.
  - q: "Graduated price tiers: the first 10,000 calls are free, the next 90,000 cost ₹0.10 each, and every call after 100,000 costs ₹0.05. A customer makes 250,000 calls. What is the charge?"
    options:
      - ₹12,500
      - ₹16,500
      - ₹25,000
    answer: 1
    why: "90,000 × ₹0.10 = ₹9,000, plus 150,000 × ₹0.05 = ₹7,500. The first 10,000 calls cost nothing. Total ₹16,500."
  - q: A customer moves from a ₹3,000 plan to a ₹6,000 plan after 10 days of a 30-day month. With proration, what extra amount do they owe for the rest of the month?
    options:
      - ₹2,000
      - ₹3,000
      - ₹4,000
    answer: 0
    why: "They get a credit of ₹3,000 × 20/30 = ₹2,000 for unused time. They pay ₹6,000 × 20/30 = ₹4,000 for the new plan. The difference is ₹2,000."
  - q: With Stripe Billing meters, a usage event for 30 November arrives after the November invoice has been finalized. What happens to it?
    options:
      - Stripe updates the finalized invoice.
      - Stripe adds it to the December invoice.
      - Stripe can accept it into meter summaries, but it is not added to the finalized invoice or the next one.
    answer: 2
    why: Stripe's docs say usage reported after the grace period is not added to the finalized invoice or to the next invoice. You must handle it yourself.
  - q: You bill for the number of different users who used the product in a month. Which aggregation fits?
    options:
      - Sum
      - Count unique
      - Latest
    answer: 1
    why: Count unique counts distinct values of a property, such as user id. Sum would count a user once for every event.
incidents: []
related: [idempotency-retries, queues-vs-logs, money-ledgers, reconciliation, stream-processing, multi-tenancy, unique-ids]
---

## What broke

This story is made up, but each part of it happens. A small company sells an SMS API and charges per message sent.

In March, a network problem slowed the link to the billing service. The API servers retried their usage reports. The retry code built each report again, with a new random id each time.

The billing service saw many "new" events. It added them all. Forty customers got invoices for about twice the messages they sent.

A large customer noticed first, because their own logs showed half the number. The team could not show the real count. The monthly job had stored only running totals, not the events behind them.

It took two weeks to rebuild the numbers from server logs. The company refunded everyone. Then it had to answer a harder question: were earlier months wrong too?

## The idea in simple words

With **usage-based pricing**, the bill depends on what the customer did: API calls, messages, gigabytes or tokens. So billing is now a counting problem. If you count wrong, you charge wrong.

Counting looks easy. But events get repeated and arrive late. Months start at midnight, but whose midnight? Prices have tiers, and customers change plans mid-month.

You need four things:

1. Count each piece of usage **exactly once**.
2. Put it in the right **billing period**.
3. Turn the count into money with **exact arithmetic**.
4. Keep the raw events, so you can **prove** every number later.

> [!key] **The key idea.** An invoice is a calculation over raw usage events. Keep the events, give each one a stable id, and you can always recompute and explain the bill.

## How it works

### The pipeline

Most metering systems have the same steps:

1. **Emit.** Your service writes a usage event: id, customer, meter name, value and timestamp.
2. **Log.** The event goes to a durable, append-only log, such as a Kafka topic or an events table. See [queues and logs](/learn/queues-vs-logs-delivery-guarantees).
3. **Dedupe.** Copies of the same event are dropped.
4. **Aggregate.** Combine events per customer, meter and billing period.
5. **Rate.** The price plan turns the quantity into money.
6. **Invoice.** At period end, create the invoice, then make it final.
7. **Collect.** You charge the card or wait for a transfer.

Stripe meter events carry an event name, a customer id, a value and an optional timestamp. [@stripe-meter-event] Stripe aggregates them asynchronously, so totals can lag behind the latest events. [@stripe-record-usage]

[[diagram:usage-dedupe-late]]

### Dedupe: exactly-once in effect

Networks lose messages, so senders retry. That gives you at-least-once delivery: the same event can arrive twice. The fix is the same as for any [idempotent](/learn/idempotency-retries-timeouts) write: a stable id per event.

- Stripe enforces a unique `identifier` per meter event within a rolling period of at least 24 hours. [@stripe-meter-event]
- OpenMeter handles events that share the same `id` and `source` only once. [@openmeter-events]
- Lago uses a `transaction_id`. [@lago-ingest]

The id must be created **once**, when the usage happens. It must not be created when you send. Use a [UUID or ULID](/learn/unique-id-generation) stored with the event.

Watch the whole dedupe key, not just the id. On Lago's ClickHouse event store, the timestamp is part of the key. A retry with the same `transaction_id` but a different timestamp is billed again. [@lago-ingest] So resend the original payload, byte for byte.

Stripe only promises its check for at least 24 hours. So run your own dedupe too, on your log, across the whole billing period.

### Late events and closing a period

An event belongs to the period of its **event time**, not its arrival time. Lago assigns a late event to the right past period by its timestamp. If that period's invoice is already final, the event does not change it. [@lago-ingest]

A **grace period** is the time after a period ends in which late events still count. Stripe's default is 1 hour, and you can set up to 72 hours. It applies to the end-of-cycle invoice, not to threshold invoices. [@stripe-grace] Stripe accepts event timestamps up to 35 days in the past. [@stripe-meter-event] But an accepted event can still miss its invoice. Usage reported after the grace period is not added to the finalized invoice or to the next one. [@stripe-grace]

So decide in advance what happens to very late events. Common choices: bill them as a separate line on the next invoice, or write them off below a set amount. Either way, keep them and count them in [reconciliation](/learn/reconciliation).

### Timezones and period boundaries

Store every timestamp in UTC. Then define each period boundary as an exact instant.

A customer in India expects "November" to start at 00:00 IST on 1 November. That is 18:30 UTC on 31 October. If your job uses UTC days, five and a half hours of usage land in the wrong month, at both ends of the month. Tools often mean a UTC day by "a day". Stripe's pre-aggregated ingestion keeps only the latest event per UTC hour or day. [@stripe-meters] Choose the billing timezone per customer, store it, and test the hours around midnight.

### Aggregation types

A meter says how to combine events. Lago lists count, count unique, sum, max, latest and weighted sum. [@lago-aggregation] Stripe meters offer sum, count and last. [@stripe-meters]

| Type | Bills for | Example |
|---|---|---|
| Sum | The total of a value | Tokens, gigabytes sent |
| Count | The number of events | API calls |
| Max | The peak value | Most parallel jobs at once |
| Latest | The last reported value | Storage used at period end |
| Count unique | Distinct values of a property | Active users this month |

### Rating, rounding and minor units

Price tiers are common. In graduated pricing, each tier's units get that tier's price. [@lago-graduated] Take this plan: the first 10,000 calls are free, the next 90,000 cost ₹0.10 each, and calls above 100,000 cost ₹0.05. For 250,000 calls:

- 90,000 × 10 paise = 900,000 paise
- 150,000 × 5 paise = 750,000 paise
- Total: 1,650,000 paise, which is ₹16,500

Never use floats for money. Unit prices can be smaller than one paise, so store them as exact decimals: a decimal type, or an integer count of a tiny unit. Store line totals and invoice amounts as integers in [minor units](/learn/money-and-double-entry-ledgers). Lago allows charges with up to 15 decimal places. It rounds to the currency's smallest unit, such as cents or paise, on the invoice. [@lago-charges]

Multiply exactly, then round **once**, on the line total. Suppose one request costs 0.555 paise. If you round each event up to 1 paise, you charge about 80% too much.

### Plan changes and proration

A plan change in mid-period splits the fixed fee by time. This is **proration**. Stripe's example: a move from a £10 plan to a £20 plan halfway through gives a £5 credit and a £10 charge, so the customer owes £5 more. [@stripe-prorations] Stripe prorates to the second by default. [@stripe-prorations]

Usage-based charges are not prorated. [@stripe-prorations] Instead, split the usage at the moment of the change. Rate each part with the plan that was active then. Test this path well. Stripe warns that after a mid-cycle price change, usage reported in the grace period does not reach any invoice. [@stripe-grace]

### Credits and prepaid balances

Many customers prepay. Lago calls this a wallet of credits, for example 1 credit = $5. It takes credits off the invoice after tax and after any credit notes. [@lago-wallets] Treat a wallet like an account in your [ledger](/learn/money-and-double-entry-ledgers): every top-up and every use is its own entry.

### Showing usage, and proving it

Customers want to see their usage before the invoice. Lago's current-usage endpoint shows the open period and warns that amounts can still change. [@lago-usage] Label the number "estimated" until the period closes.

Once a period is closed, Lago treats its usage as final. A later price change does not touch past invoices. [@lago-usage] Do the same. Corrections are new entries, never edits. Stripe lets you cancel a meter event only within 24 hours of sending it. It does not change a finalized invoice. [@stripe-meters]

Finally, reconcile. Every day, sum the raw events per customer, meter and period. Compare that with your running totals and with the invoice lines. Any gap is a break with an owner.

## Trade-offs

| Option | Accuracy | Cost | Use it when |
|---|---|---|---|
| Send every raw event to the billing provider | Yes. The provider sees everything. | High. Rate limits apply (Stripe: 1,000 calls a second). [@stripe-record-usage] | Low volume, simple pricing. |
| Pre-aggregate, then send totals | Yes, if your own dedupe is correct. | Lower. You run the counting. | High volume. Keep raw events anyway. |
| Short grace period (1 hour) | Lower. More late events miss the invoice. | Invoices go out on time. | Your senders are fast and reliable. |
| Long grace period (up to 3 days) | Higher. Fewer late events missed. | Invoices and cash arrive later. | Senders batch usage or run on devices. |
| Keep only running totals | No. You cannot recompute or prove a bill. | Cheapest storage. | Never for billing. |

**Simple rule:** stable ids at the source, raw events kept, one place that rounds, and a daily check that raw events equal invoice lines.

## At work: how a senior uses this

- In design review, ask: **"Where is the event id created?"** If the answer is "in the sender, on each send", that is a double-billing bug.
- Ask: **"Can we rebuild last month's invoice from raw events alone?"** If not, keep the events before you launch.
- Write the billing timezone and grace period into the plan data, not into a cron job.
- Put a test on every tier boundary: 9,999, 10,000, 10,001 and so on.
- Alert when the daily recon finds a gap, and when late events after close go above a set share.
- Give each [tenant](/learn/multi-tenancy-noisy-neighbours) an "estimated" usage page before the first invoice. It catches bugs early.

## With AI

### What to ask the agent

> "Write a usage event producer. Create the event id once, when usage is recorded, and resend the same bytes on every retry. Test that three retries carry one id."

> "Write SQL that sums raw usage events per customer, meter and billing period, using each customer's billing timezone. Compare it with invoice line quantities and list every mismatch."

> "Write a function that rates graduated price tiers. Store unit prices as exact decimals, multiply exactly, and round once to an integer number of paise per line. Add tests at every tier boundary and for a unit price below one paise."

### What to check yourself

- No floats anywhere in rating. Unit prices are exact decimals; line totals and invoices are integers in minor units.
- Rounding happens once per line, not per event.
- Period boundaries come from the customer's timezone. Test 23:59 and 00:01 in that zone.
- The event id is not generated inside the retry loop.
- A late event after close is kept and flagged. It is not silently dropped.
- Corrections add new rows. No `UPDATE` on raw events or final invoices.

## Check yourself

## Go deeper

- [Stream processing](/learn/stream-processing): windows, watermarks and late events in depth.
