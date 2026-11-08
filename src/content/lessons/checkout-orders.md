---
topic: checkout-orders
subtitle: Give every order a clear list of states and allowed moves, so checkout stays correct when payments time out, webhooks arrive late and stock runs out.
status: published
reviewed_by: ai-factcheck
reviewed_on: 2026-11-08
illustrative_story: true
glossary_optout: [authorization]
sources:
  - id: stripe-pi
    title: "Stripe documentation, How Payment Intents and Setup Intents work"
    url: https://docs.stripe.com/payments/paymentintents/lifecycle
    note: The statuses of a Stripe payment, from requires_payment_method to succeeded or canceled, and what each one means.
  - id: stripe-idem
    title: "Stripe API reference, Idempotent requests"
    url: https://docs.stripe.com/api/idempotent_requests
    note: How Stripe saves the first result for an idempotency key, how long keys live, and why parameters must match.
  - id: stripe-webhooks
    title: "Stripe documentation, Receive Stripe events in your webhook endpoint"
    url: https://docs.stripe.com/webhooks
    note: Event ordering is not guaranteed, duplicate events happen, and you can fetch missing objects from the API.
  - id: stripe-fulfil
    title: "Stripe documentation, Fulfil orders"
    url: https://docs.stripe.com/checkout/fulfillment
    note: Why fulfilment must run from webhooks, and why your fulfilment function must be safe to call many times at once.
  - id: stripe-inventory
    title: "Stripe documentation, Manage limited inventory"
    url: https://docs.stripe.com/payments/checkout/managing-limited-inventory
    note: Expiring Checkout Sessions after 30 minutes to 24 hours, and returning held items when a session expires.
  - id: stripe-hold
    title: "Stripe documentation, Place a hold on a payment method"
    url: https://docs.stripe.com/payments/place-a-hold-on-a-payment-method
    note: Authorize now and capture later, hold windows, partial capture, and the one-capture limit for most payments.
  - id: stripe-refunds
    title: "Stripe documentation, Refund and cancel payments"
    url: https://docs.stripe.com/refunds
    note: Cancel before capture, partial and multiple refunds, and which payment statuses you can still cancel.
  - id: airbnb-orpheus
    title: "Jon Chew and Ninad Khisti, Avoiding Double Payments in a Distributed Payments System, The Airbnb Tech Blog (2019)"
    url: https://web.archive.org/web/20250105181155/https://medium.com/airbnb-engineering/avoiding-double-payments-in-a-distributed-payments-system-2981f6b070bb
    note: Airbnb's idempotency library, its pre-RPC, RPC and post-RPC phases, and how it sorts errors into retryable and non-retryable.
  - id: shopify-resilient
    title: "Bart de Water, 10 Tips for Building Resilient Payment Systems, Shopify Engineering (2022)"
    url: https://shopify.engineering/building-resilient-payment-systems
    note: Short timeouts, idempotency keys and reconciliation, from Shopify's payments team.
claims_checked:
  - claim: "On Stripe, a declined attempt returns the PaymentIntent to requires_payment_method, and after a manual-capture authorization the status is requires_capture."
    source: stripe-pi
    note: Lifecycle table says a failed attempt returns to requires_payment_method; stripe-hold says the status transitions to requires_capture after authorization.
  - claim: "Stripe saves the status code and body of the first request for an idempotency key and replays them, including 500 errors; keys may be removed after at least 24 hours; differing parameters return an error."
    source: stripe-idem
    note: Matches the API v1 wording on the Idempotent requests page.
  - claim: "Stripe does not guarantee event order, says not to use created to order events, and says to track event IDs to find duplicates."
    source: stripe-webhooks
    note: Matches the Event ordering and Handle duplicate events sections.
  - claim: "An online card hold is usually valid for 7 days (about 5 for Visa merchant-initiated), most payments allow one capture, and a partial capture releases the rest."
    source: stripe-hold
    note: Matches the validity windows table, the Extended authorisations note and the Capture the funds section.
  - claim: "A Checkout Session's expires_at must be 30 minutes to 24 hours ahead (default 24 hours), and checkout.session.expired lets you return held items."
    source: stripe-inventory
    note: Read from the full-page variant of the page.
  - claim: "Airbnb's Orpheus treats network and 5XX errors as retryable and 4XX validation errors as non-retryable, and can query the transaction status first on a retry."
    source: airbnb-orpheus
    note: Checked against the archived Medium article (web.archive.org snapshot 20250105181155).
quiz:
  - q: "A buyer taps Place order. The app's request times out, and the app retries. How do you stop a second order and a second charge?"
    options:
      - Ask the buyer to wait 30 seconds before tapping again.
      - The app creates one idempotency key per checkout and sends the same key on every retry. The server returns the order it already made.
      - The server creates a new order and refunds the old one later.
    answer: 1
    why: The key lets the server spot the repeat. Stripe works the same way and returns the saved result for a repeated key.
  - q: "Your call to authorize a payment times out. What should your code do next?"
    options:
      - Mark the order failed and release the stock at once.
      - Send a new payment request with a new idempotency key.
      - Treat the result as unknown. Ask the provider about this order, or retry with the same key, before you decide.
    answer: 2
    why: A timeout does not mean the charge failed. A new key could charge the card twice. Airbnb's library can ask for the status first when it retries.
  - q: "A payment_intent.succeeded webhook arrives for an order that is already cancelled. What should happen?"
    options:
      - Move the order back to confirmed and ship it.
      - The transition guard refuses the move. You refund the payment and alert someone.
      - Ignore the webhook and keep the money.
    answer: 1
    why: Cancelled is a final state, so no rule allows cancelled to confirmed. A succeeded payment can't be cancelled, so you refund it.
  - q: "An order has two items. One ships today, one ships in 10 days. You authorized the card at checkout. On Stripe, what is the risk of capturing per shipment?"
    options:
      - None. You can capture as many times as you like.
      - For most payments you can capture only once. A partial capture releases the rest, and a card hold usually lasts about 7 days.
      - Stripe captures both parts for you.
    answer: 1
    why: Stripe says most payments allow one capture, and a partial capture releases the remaining amount. Capture once, then refund what never ships.
  - q: "Why do you copy the price and tax onto the order line when the order is placed?"
    options:
      - So a later price change does not change what the buyer agreed to pay, refunds or receipts.
      - To save database space.
      - Because the product table can't store prices.
    answer: 0
    why: The catalogue changes over time. The order is a record of one deal at one moment, so it keeps its own copy.
incidents: []
cases: [airbnb-double-payments, shopify-resilient-payments]
related: [inventory-flash-sales, payment-lifecycle, idempotency-retries, sagas-outbox-cdc, money-ledgers, transactions-isolation, webhooks, reconciliation]
---

## What broke

This is a made-up story, but each failure in it is common.

Ravi's team runs a shoe shop. On a sale day, the payment provider was slow. The checkout service called "authorize payment" with a 3-second timeout. Many calls timed out.

The code treated a timeout as a decline. It marked those orders "failed" and released the stock. The app showed "Payment failed, please try again". Many buyers tapped Pay again.

But many first payments had worked. The provider just answered late. Its webhooks arrived a minute later, saying the payments succeeded. The webhook handler set those orders to "paid", even though they were "failed" and their stock was gone.

So some buyers paid twice for one pair of shoes. Some "paid" orders had no stock, so the warehouse could not ship them. Support spent two weeks on refunds.

The root cause was simple. Any code could write any value to `orders.status`. Nobody had written down which moves were allowed, and "we do not know yet" was not a state at all.

## The idea in simple words

An order is a [state machine](/glossary#state-machine). At any moment it is in exactly one state. Only a short list of moves can change that state.

A common set of states looks like this:

- **pending_payment**: the order exists, the stock is held, and the money is not yet secured.
- **authorized**: the provider holds the money on the card.
- **confirmed**: you have committed the stock and accepted the order.
- **shipped**, then **delivered**.
- **failed**, **cancelled** and **refunded**: end states. Nothing moves out of them.

Write the allowed moves in one table, in one place in the code. Every change goes through one function that checks the table. A webhook, an admin tool and a cron job all use that same function.

> [!key] **The key idea.** Make illegal moves impossible, not just unlikely. One transition table, one function that changes state, and one history row for every change.

## How it works

Step through one order, including the two cases that break most shops.

[[diagram:order-state-timeout]]

### The checkout sequence

1. **Hold the stock with a TTL.** Reserve the items for a short time, for example 30 minutes, as in [the flash-sale lesson](/learn/inventory-overselling-flash-sales). If you use Stripe Checkout, a session can expire between 30 minutes and 24 hours after you create it. The default is 24 hours. Stripe sends `checkout.session.expired` so you can return the held items. [@stripe-inventory] Make the two times match. Set `expires_at` no later than the end of your stock hold, or call Stripe's expire endpoint when the hold ends. [@stripe-inventory] Otherwise a buyer can pay for stock you already released.
2. **Create the order** in `pending_payment`, in the same database transaction as the hold.
3. **Authorize the payment.** On Stripe, after the card is approved with manual capture, the payment's status is `requires_capture`. [@stripe-hold] If the card is declined, the payment goes back to `requires_payment_method`, so the buyer can try again. [@stripe-pi] The order stays in `pending_payment` while they do.
4. **Confirm.** Commit the stock and move the order to `confirmed`.
5. **Capture** when you ship, or at once for digital goods. An online card hold is usually valid for 7 days. Some are shorter. For example, a Visa payment that you start without the customer is held for about 5 days. The charge's `capture_before` field shows the exact expiry. [@stripe-hold] If the hold expires, the funds are released and the payment's status becomes `canceled`. [@stripe-hold]

Do not hold a database transaction open while you call the provider. Airbnb's library splits each request into three parts. It saves the request, makes the network call, then saves the result. [@airbnb-orpheus] The two database parts never make network calls, and the network call never touches the database. Airbnb learned that network calls inside those database parts can quickly use up the connection pool. [@airbnb-orpheus]

### The transition guard

Here is a small guard in TypeScript. The `WHERE state = $2` part makes it safe when two workers race.

```ts
type State = "pending_payment" | "authorized" | "confirmed"
  | "shipped" | "delivered" | "failed" | "cancelled" | "refunded";

const ALLOWED: Record<State, State[]> = {
  pending_payment: ["authorized", "failed"],
  authorized: ["confirmed", "cancelled"],
  confirmed: ["shipped", "cancelled"],
  shipped: ["delivered"],
  delivered: ["refunded"],
  failed: [], cancelled: [], refunded: [],
};

interface Tx { // your database client
  update(sql: string, args: unknown[]): Promise<number>;
  insert(table: string, row: object): Promise<void>;
}

async function move(tx: Tx, orderId: string, from: State, to: State, why: string) {
  if (!ALLOWED[from].includes(to)) throw new Error(`illegal ${from} -> ${to}`);
  const n = await tx.update(
    "UPDATE orders SET state = $3 WHERE id = $1 AND state = $2",
    [orderId, from, to]);
  if (n === 0) throw new Error("state changed, reload and decide again");
  await tx.insert("order_events", { orderId, from, to, why });
  await tx.insert("outbox", { type: `order.${to}`, orderId });
}
```

If the row count is 0, another worker moved the order first. This is a [compare-and-swap](/glossary#compare-and-swap); see [Transactions and isolation levels](/learn/transactions-and-isolation-levels).

The same transaction writes a row to `order_events` and a row to the transactional outbox. The history row gives you an audit trail: who changed what, when, and why. The outbox row lets a relay publish `order.confirmed` to email and the warehouse without a dual write. [Sagas, outbox and CDC](/learn/sagas-outbox-and-cdc) explains the relay.

### One checkout, one key per attempt

The app creates one idempotency key when the checkout page opens. It sends that key with every "Place order" retry. The server saves the key with a unique index, so a repeat returns the same order.

Pass a key to the provider too. Stripe saves the status code and body of the first request for a key and returns them on retries, including `500` errors. [@stripe-idem] It may remove keys after they are at least 24 hours old. It also returns an error if a retry sends different parameters. [@stripe-idem] Use one provider key per payment attempt, for example `order-8812-attempt-1`. Send the same key on every retry of that attempt. If the card is declined and the buyer tries again, that is a new attempt with a new key. Airbnb also builds keys from the entity. Its example `payment-1234-refund` makes sure payment 1234 is refunded only once. [@airbnb-orpheus]

### Unknown is a real result

A timeout does not mean "declined". Airbnb's library sorts errors into retryable and non-retryable. Network and server errors (5XX) are retryable. Invalid requests (4XX) are not. [@airbnb-orpheus] On a retry, it can first ask the downstream service for the transaction's status. [@airbnb-orpheus]

So keep the order in `pending_payment`, and ask the provider about this order. Never start a new attempt with a new key while the old one is still unknown.

Keep timeouts short, too. Shopify suggests starting with a 1-second connect timeout and a 5-second read timeout, then tuning from your own data. [@shopify-resilient]

### Webhooks: late, repeated and out of order

Stripe does not promise to send events in the order they happened. [@stripe-webhooks] It also says not to use an event's `created` time to order events. Track event IDs to find duplicates, and fetch the latest object from the API when you need it. [@stripe-webhooks]

So the webhook handler should not set a state directly. It reads the payment's current status and asks the guard for a move. A late success for a `cancelled` order is refused. If the payment is only authorized (`requires_capture`), cancel it, which costs nothing. If it has already `succeeded`, refund it, because a succeeded payment can't be cancelled. Log both cases. [@stripe-refunds]

Stripe also says your fulfilment code may be called many times, even at the same moment, for one Checkout Session. [@stripe-fulfil] The `WHERE state = $2` check handles that.

### Snapshot prices, then handle partial work

When you create the order, copy the unit price, discount, tax, currency and shipping fee onto each order line. Store money as integers in [minor units](/glossary#minor-unit). Later price changes must not change old orders, receipts or refunds. [Money and double-entry ledgers](/learn/money-and-double-entry-ledgers) shows how to record the amounts.

Partial shipments need care. On Stripe, most payments allow only one capture, and a partial capture releases the rest. [@stripe-hold] So capture once, then refund items that never ship. Stripe allows several refunds on one charge, but never more than the original amount in total. [@stripe-refunds] Keep shipments and refunds as their own rows that point to order lines, instead of adding more order states.

## Trade-offs

| Option | Good | Bad | Use it when |
|---|---|---|---|
| Free-form `status` column | Yes. Fast to start. | Any code can write any value. Bugs show up as money problems. | Never for real money. |
| Transition table plus one `move()` function | Yes. Illegal moves fail loudly, and all writers follow one rule. | You must update the table when the business adds a step. | The default. |
| Workflow engine (Temporal, AWS Step Functions) | Yes. Retries, timers and history come built in. | A new system to run and learn. | Long flows with many timers and services. |
| Capture at checkout | Yes. One step, no hold expiry. | Refunds for items you can't ship. | Digital goods, and stock that ships the same day. |
| Authorize now, capture on ship | Yes. You can cancel at no cost before capture. [@stripe-refunds] | The hold can expire if shipping is late. [@stripe-hold] | Physical goods that ship within a few days. |

## At work: how a senior uses this

- Draw the order states on one page before you write code. Mark every end state, and every state that needs a timer.
- Ask: **"If this call times out, what state is the order in? Who finds out the real answer, and when?"**
- Keep one `move()` function. Search the code for any other write to the state column, and remove it.
- Run a job every few minutes for orders stuck in `pending_payment` past their TTL. It asks the provider, then moves each order or releases the stock.
- Alert on refused transitions. Each one is a race or a bug that someone should look at.
- Reconcile your orders with the provider's records. Shopify checks single records, such as charges and refunds. It also checks totals, such as the balance it has not yet paid out to a merchant. [@shopify-resilient]

## With AI

### What to ask the agent

> "List every place in this repo that writes to orders.state. For each one, show the old state it expects and the new state it sets."

> "Draw our order state machine as a Mermaid diagram from the code. Mark any state with no way out, and any move the business rules don't allow."

> "Write tests for checkout where the payment call times out, the webhook arrives twice, and the webhook arrives after the order was cancelled."

### What to check yourself

- The idempotency key is created once per checkout, not once per request. A common bug is calling `uuid()` inside the retry loop.
- No network call runs inside a database transaction.
- The webhook handler verifies the signature and fetches the current payment before it moves the order.
- Prices and tax on order lines are copied values, not joins to the product table.
- Hold windows and session limits match your provider's docs, not the model's memory.

## Check yourself

## Go deeper

- [Payment lifecycle](/learn/payment-lifecycle): every state of the payment itself, from authorization to payout.
- [Idempotency, retries and timeouts](/learn/idempotency-retries-timeouts): keys, backoff and safe retries in depth.
