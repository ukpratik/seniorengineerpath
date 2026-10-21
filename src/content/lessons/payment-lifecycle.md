---
topic: payment-lifecycle
subtitle: What really happens after the customer taps "Pay", and the states your code must track until the money is in your bank.
status: published
reviewed_by: ai-factcheck
reviewed_on: 2026-10-21
illustrative_story: true
sources:
  - id: stripe-cards
    title: "Stripe documentation, How cards work"
    url: https://docs.stripe.com/payments/cards/overview
    note: The steps of an online card payment, and plain definitions of card issuer and card network.
  - id: adyen-acquirer
    title: "Adyen Knowledge Hub, What's an acquiring bank and why you need one"
    url: https://www.adyen.com/knowledge-hub/acquiring-bank
    note: What an acquirer does and how it sends authorization requests through the card schemes.
  - id: stripe-hold
    title: "Stripe documentation, Place a hold on a payment method"
    url: https://docs.stripe.com/payments/place-a-hold-on-a-payment-method
    note: Separate authorization and capture, partial capture, and how long a hold stays valid for each card brand.
  - id: stripe-pi
    title: "Stripe documentation, How Payment Intents and Setup Intents work"
    url: https://docs.stripe.com/payments/paymentintents/lifecycle
    note: The status of a payment, from requires_payment_method to succeeded or canceled.
  - id: adyen-lifecycle
    title: "Adyen documentation, Payments lifecycle"
    url: https://docs.adyen.com/essentials/payments-lifecycle
    note: Another processor's statuses, from Received and Authorised to SentForSettle, Settled and Refunded.
  - id: stripe-payouts
    title: "Stripe documentation, Payouts"
    url: https://docs.stripe.com/payouts
    note: Settlement timing as T+X days per country, and payout schedules.
  - id: stripe-refunds
    title: "Stripe documentation, Refund and cancel payments"
    url: https://docs.stripe.com/refunds
    note: Cancel versus refund, partial refunds, refund timing and failed refunds.
  - id: stripe-disputes
    title: "Stripe documentation, Disputes"
    url: https://docs.stripe.com/disputes
    note: What a chargeback is and how the money is pulled back from you.
  - id: stripe-respond
    title: "Stripe documentation, Respond to disputes"
    url: https://docs.stripe.com/disputes/responding
    note: Response deadlines, evidence, and who decides the outcome.
  - id: stripe-3ds
    title: "Stripe documentation, 3D Secure authentication"
    url: https://docs.stripe.com/payments/3d-secure
    note: What 3D Secure is and where regulation may require it.
  - id: stripe-webhooks
    title: "Stripe documentation, Receive Stripe events in your webhook endpoint"
    url: https://docs.stripe.com/webhooks
    note: Signature checks, replay protection, duplicates, ordering and retries.
  - id: stripe-events
    title: "Stripe API reference, Types of events"
    url: https://docs.stripe.com/api/events/types
    note: What each event means, such as payment_intent.succeeded and payout.paid.
  - id: stripe-recon
    title: "Stripe documentation, Payout reconciliation report"
    url: https://docs.stripe.com/reports/payout-reconciliation
    note: How to match each payout in your bank with the payments, refunds, disputes and fees inside it.
  - id: stripe-security
    title: "Stripe documentation, Integration security guide"
    url: https://docs.stripe.com/security/guide
    note: PCI DSS in practice, and how hosted payment fields keep card data off your servers.
  - id: pci-dss
    title: "PCI Security Standards Council, PCI DSS"
    url: https://www.pcisecuritystandards.org/standards/pci-dss/
    note: The official home of the card data security standard and who it applies to.
claims_checked:
  - claim: "At Stripe, an online card authorization is usually valid for 7 days; for Visa merchant-initiated card-not-present payments the table says 5 days, with an exact window of 4 days and 18 hours. If it expires before capture, funds are released and the status becomes canceled."
    source: stripe-hold
    note: Matches the authorization validity windows table and the "Extended authorizations" note. Scoped to Stripe and card payments.
  - claim: "Stripe's 'Initial settlement timing' column, which applies to the first payout, lists 2 business days for the United States, and for India 2 business days for domestic and 5 for international charges."
    source: stripe-payouts
    note: The column is "Initial settlement timing", not a default; the default column shows "—" for both. Lesson now says it applies to the first payout.
  - claim: "Stripe retries webhook deliveries for up to three days in live mode; its libraries use a default 5-minute timestamp tolerance; endpoints may receive the same event more than once, so log processed event IDs."
    source: stripe-webhooks
    note: All three match the webhooks page (automatic retries, replay attacks, handle duplicate events).
  - claim: "On Stripe you usually have 7 to 21 days to respond to a dispute, depending on the card network; if countered, the cardholder's bank decides, which can take up to 3 months."
    source: stripe-respond
    note: Exact figures from Respond to disputes. The 3 months applies after you counter.
  - claim: "Refunds appear to the customer about 5 to 10 business days later, depending on the bank; cancel before capture costs nothing, a refund might incur a fee."
    source: stripe-refunds
    note: Matches "Trace a refund" and the intro of Refund and cancel payments.
  - claim: "Handling raw card data can mean more than 300 PCI DSS controls; low-risk integrations send card data directly to Stripe without it passing through your servers; you must attest to PCI compliance annually."
    source: stripe-security
    note: Matches the Integration security guide.
  - claim: "payout.paid means the payout is expected to be available; a payout.failed event can follow later."
    source: stripe-events
    note: Matches the event type description.
quiz:
  - q: "Your shop authorizes a card payment at checkout and captures it when the item ships. What does authorization do?"
    options:
      - It moves the money to your bank account.
      - It holds the amount on the customer's card, so you can capture it later.
      - It sends a payout to the merchant.
    answer: 1
    why: The bank checks for funds and holds the amount. The money moves only when you capture, and reaches your bank later, as a payout.
  - q: "An item ships 10 days after a Visa online checkout, where the customer paid themselves. You authorized at checkout. What is the risk?"
    options:
      - None. Authorizations never expire.
      - The hold may have expired, so the capture fails and the payment is canceled.
      - The customer is charged twice.
    answer: 1
    why: For a Visa online payment that the customer starts, Stripe lists a 7-day hold. After that the funds are released and the payment becomes canceled.
  - q: "Your webhook handler gets the same payment_intent.succeeded event twice. What should it do?"
    options:
      - Ship the order twice.
      - Save processed event IDs and skip an event it has already handled.
      - Reject the second one with a 500 error.
    answer: 1
    why: Stripe says endpoints may receive the same event more than once, and recommends logging processed event IDs.
  - q: "Why do many shops use hosted payment fields, like Stripe Elements, instead of their own card form?"
    options:
      - They are faster to load.
      - Card data goes straight to the processor, so it does not pass through the shop's servers. This reduces PCI DSS work.
      - They remove the need for 3D Secure.
    answer: 1
    why: Stripe says handling raw card data can mean more than 300 PCI DSS controls. Low-risk integrations send card data straight to Stripe.
  - q: "What is the difference between a refund and a chargeback?"
    options:
      - There is none.
      - You start a refund. A chargeback starts when the cardholder disputes the payment with their card issuer.
      - A chargeback is a refund made in under a day.
    answer: 1
    why: A dispute starts with the cardholder and their issuer. At Stripe, the payment amount and a dispute fee leave your balance before the bank decides who is right.
incidents: []
cases: [shopify-resilient-payments, stripe-idempotency-keys]
related: [money-ledgers, idempotency-retries, webhooks, reconciliation, pci-tokenisation, payment-rails, fraud-risk]
glossary_optout: [authorization, authentication]
---

## What broke

This is a made-up example, but each rule in it comes from processor docs.

Meera's team runs an online furniture shop in India. To avoid refunds for items that are out of stock, the shop takes card payments in two steps. At checkout it only places a hold on the card. When a sofa ships, a job captures the money.

In one busy month, some sofas shipped late, 9 or 10 days after checkout. The capture job logged "error" for those orders, and nobody read the log. The sofas went out anyway.

Their processor's rule was simple. For many online card payments, a hold lasts about 7 days. If you do not capture before it expires, the funds are released and the payment becomes canceled. [@stripe-hold] The shop gave away the sofas for free.

Nobody saw it for weeks. The order table said "paid" because the hold had worked. The team found it only at month end. The money in the bank was lower than the sum of "paid" orders, and finance asked why.

Three bugs, one root cause. The team treated a payment as one event, "paid". It is really a series of states over many days, and each state can fail.

## The idea in simple words

A card payment has several parties.

- **Customer**: the person who pays with a card.
- **Merchant**: your business.
- **Payment processor** or gateway: a company such as Stripe or Adyen that your code talks to.
- **Acquiring bank**: processes card payments for merchants. It sends the authorization request to the customer's bank through the card networks. [@adyen-acquirer]
- **Card network**: the network of a card brand, such as Visa or Mastercard. [@stripe-cards]
- **Issuing bank**: the bank that gave the customer the card. [@stripe-cards]

The payment then moves through states. Your code must track each one.

1. **Authorization**: the issuing bank checks that funds are there and holds the amount. [@stripe-cards]
2. **Capture**: the money moves from the issuing bank toward you. [@stripe-cards]
3. **Settlement**: the customer's bank transfers the funds to the processor. Adyen marks a payment Settled at this point. [@adyen-lifecycle]
4. **Payout**: the processor sends money from your available balance to your bank account. [@stripe-payouts]

> [!key] **The key idea.** "Paid" is not one moment. A payment is a state machine that runs for days. Store each state, learn about changes from verified webhooks, and check the final money against the processor's report.

## How it works

Step through one ₹1,000 card payment.

[[diagram:payment-auth-capture-payout]]

### Authorize, then capture

You can capture right away. You can also authorize and capture in two steps, with the capture later. Hotels, for example, often authorize the full amount before a guest arrives and capture at checkout. [@stripe-hold]

With Stripe, you set `capture_method=manual`. After authorization, the payment's status becomes `requires_capture`. [@stripe-hold] You can capture less than you held, and Stripe releases the rest automatically. [@stripe-hold]

The hold does not last forever. Stripe says an online card authorization is usually valid for 7 days. For Visa payments that you start without the customer, the table says 5 days, and the exact window is 4 days and 18 hours. [@stripe-hold] Other payment methods have their own windows. [@stripe-hold] If the hold expires before capture, the funds are released and the payment is canceled. [@stripe-hold] On Stripe, the charge's `capture_before` field shows when the hold expires. [@stripe-hold] Build an alert for holds that are about to expire.

If you no longer want the money, cancel before capture. At Stripe, canceling costs nothing, while a refund after success might cost a fee. [@stripe-refunds]

### 3-D Secure

Sometimes the issuing bank wants proof that the real cardholder is paying. With 3-D Secure (3DS), the bank may ask for a password, a one-time code sent to the phone, or a biometric check. [@stripe-3ds] Rules in the EEA, the UK, India, Japan and Australia may require it for card payments. [@stripe-3ds] On Stripe, the payment waits in the `requires_action` status until the customer finishes this step. [@stripe-pi]

### Settlement takes days

Capture is not cash. The money still has to travel between banks. Stripe gives settlement timing per country as "T+X" days. "T" is when the payment was confirmed or captured. [@stripe-payouts]

Stripe's table has an "initial settlement timing" column, which applies to your first payout. There it lists 2 business days for the United States. For India, it lists 2 business days for domestic charges and 5 for international ones. [@stripe-payouts] Later payouts follow a default timing, which can differ. [@stripe-payouts] Your processor and contract may differ too, so read your own terms.

Then a payout sends the money to your bank. Stripe's `payout.paid` event means the payout is *expected* to arrive. If it then fails, a `payout.failed` event comes later. [@stripe-events]

### Refunds and chargebacks

A **refund** is started by you. At Stripe, you can refund part or all of a payment, but never more than the original charge. [@stripe-refunds] It is not instant. Stripe says the customer sees the credit about 5 to 10 business days later, depending on the bank. [@stripe-refunds] A refund can also fail, for example on a closed bank account. [@stripe-refunds]

A **chargeback**, also called a dispute, is started by the cardholder with their issuer. The network reverses the payment at once. Stripe then takes the amount and a dispute fee from your balance. [@stripe-disputes] On Stripe, you usually have 7 to 21 days to respond, depending on the card network. If you miss the deadline, you lose. [@stripe-respond] If you counter the dispute, the cardholder's bank decides the outcome. Stripe says this can take up to 3 months. [@stripe-respond]

So money you counted can leave your account weeks after a "successful" sale. Your [ledger](/learn/money-and-double-entry-ledgers) needs entries for refunds, disputes and fees, not only sales.

### Webhooks: verify, then dedupe

Many results arrive later, for example when a bank confirms a payment or a customer disputes a charge. The processor tells you with a webhook. [@stripe-webhooks] Treat each one with care.

- **Verify the signature.** Without it, an attacker could send a fake "payment succeeded" event and get free goods. [@stripe-webhooks] Stripe signs each event with HMAC-SHA256 in the `Stripe-Signature` header. Verify it on the raw request body. [@stripe-webhooks]
- **Reject old events.** The signed timestamp stops replay attacks. Stripe's libraries allow 5 minutes by default. [@stripe-webhooks]
- **Dedupe.** The same event can arrive more than once. Save the event IDs you have processed and skip repeats. [@stripe-webhooks] This is the same idea as in [Idempotency, retries and timeouts](/learn/idempotency-retries-timeouts).
- **Do not trust order.** Events can arrive out of order. If one is missing, fetch the object from the API. [@stripe-webhooks]
- **Answer fast.** Return 2xx before slow work, and process from a queue. Stripe retries failed deliveries for up to three days in live mode. [@stripe-webhooks]

### Reconcile with the settlement report

Webhooks can be late or lost, and your code has bugs. So once a day, compare your records with the processor's. For automatic payouts, Stripe's payout reconciliation report lists every payment, refund, dispute and fee inside each payout. [@stripe-recon] Match each line to your ledger and explain every difference. Meera's team would have seen the missing money in the first daily check.

### PCI DSS scope

PCI DSS is the security standard for anyone who stores, processes or transmits card data. [@pci-dss] If your servers handle raw card numbers, you may face more than 300 security controls. [@stripe-security]

Most teams avoid that. They use hosted fields or a hosted checkout page, so the card number goes straight from the browser to the processor. It never passes through your servers. [@stripe-security] You may still store the card brand, the last four digits and the expiry date. [@stripe-security] Your scope shrinks, but you still must attest to PCI compliance every year. [@stripe-security]

## Trade-offs

| Option | Customer sees | Risk | Use it when |
|---|---|---|---|
| Capture at once | One charge. | Refunds for items you cannot deliver. Refunds can cost fees. [@stripe-refunds] | Digital goods and in-stock items that ship the same day. |
| Authorize now, capture later | A hold, then a charge. Some statements do not show the difference. [@stripe-hold] | The hold expires before capture, and the payment is canceled. [@stripe-hold] | Stock checks, hotels, rentals, or a final amount that may drop. |
| Trust webhooks only | Nothing. | Missed or late events leave orders in the wrong state. | Never alone. |
| Webhooks plus daily reconciliation | Nothing. | More work for your team. | Always, for real money. |
| Your own card form | Your design. | Raw card data on your servers, and a much larger PCI scope. [@stripe-security] | Rarely. Only with a strong reason and a security team. |
| Hosted fields or checkout | The processor's fields in your page. | Less control over the look. | The default for most teams. |

**Simple rule:** store every payment state, verify and dedupe every webhook, and reconcile every payout.

## At work: how a senior uses this

- Draw the payment state machine for your product, with every state and every exit, before you write code.
- Ask: **"What happens if this webhook never arrives? If it arrives twice? If it arrives before the one before it?"**
- Store the processor's payment ID and status next to your order. Never mark an order "paid" from the browser's redirect alone.
- Alert on holds close to expiry, captures that fail, refunds that fail and new disputes.
- Run reconciliation daily, and give someone the job of explaining each difference.
- Keep card numbers out of your systems, including logs and support tools. Use the processor's hosted fields.

## With AI

### What to ask the agent

> "Draw the state machine of a payment in our code: every status, every event that changes it, and every place we write it. Mark any state with no exit."

> "Review our webhook handler. Does it verify the signature on the raw body, reject old timestamps, dedupe by event ID and return 2xx before slow work? Show the line for each."

> "Write a daily job that matches each payout line in the processor's report to our ledger. List rows found on only one side."

### What to check yourself

- The handler uses the processor's official library to verify signatures. AI agents sometimes parse the JSON first, which breaks the raw-body check.
- The dedupe check and the business update happen in one database transaction.
- No settlement days, hold windows or fees are hard-coded from memory. They must match your processor's docs and contract.
- Amounts are integers in minor units, as in [Money and double-entry ledgers](/learn/money-and-double-entry-ledgers).
- Test cards and test webhooks run in CI for capture failure, refund failure and a dispute.
- No card number or CVC appears in logs, prompts or test data.

## Check yourself

## Go deeper

- [Money and double-entry ledgers](/learn/money-and-double-entry-ledgers): how to record each state as balanced entries.
- [Idempotency, retries and timeouts](/learn/idempotency-retries-timeouts): safe retries for capture and refund calls.
- Coming next on this line: [Reconciliation](/learn/reconciliation), [PCI-DSS and tokenisation](/learn/pci-dss-and-tokenisation) and [Payment rails: UPI and card networks](/learn/payment-rails-upi-and-cards).
