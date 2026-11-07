---
topic: reconciliation
subtitle: How to match your ledger against provider reports and bank statements, sort every difference, and fix it without rewriting history.
status: published
reviewed_by: ai-factcheck
reviewed_on: 2026-11-07
illustrative_story: false
glossary_optout: [process, near-real-time-search, transaction]
sources:
  - id: cfpb-synapse
    title: "Consumer Financial Protection Bureau, complaint against Synapse Financial Technologies, Inc. (U.S. Bankruptcy Court, C.D. Cal., filed 21 August 2025)"
    url: https://files.consumerfinance.gov/f/documents/cfpb_synapse-financial-technologies_complaint_2025-08.pdf
    note: The regulator's allegations against a fintech middle layer whose records, it says, did not match its partner banks', and the harm to consumers it describes. Settled in September 2025 without admission.
  - id: cfpb-synapse-order
    title: "Stipulated Final Judgment and Order, CFPB v. Synapse Financial Technologies, Inc. (U.S. Bankruptcy Court, C.D. Cal., entered 12 September 2025)"
    url: https://files.consumerfinance.gov/f/documents/cfpb_synapse-fin-tech_stipulated-final-judgment-order_2025-09.pdf
    note: The settlement order. It was agreed “without adjudication of any issue of fact or law”, and the trustee neither admits nor denies the allegations.
  - id: cfpb-action
    title: "Consumer Financial Protection Bureau, enforcement action page: Synapse Financial Technologies, Inc."
    url: https://www.consumerfinance.gov/enforcement/actions/synapse-financial-technologies-inc/
    note: The regulator's summary of the case, its allegations and the date the court entered the order.
  - id: stripe-recon
    title: "Stripe documentation, “Payout reconciliation report”"
    url: https://docs.stripe.com/reports/payout-reconciliation
    note: Match each bank deposit with the payments, refunds, disputes and fees inside it. Lists every report column.
  - id: stripe-recon-api
    title: "Stripe documentation, “Payout reconciliation” (API guide)"
    url: https://docs.stripe.com/payouts/reconciliation
    note: List the balance transactions of one payout with the API, and why manual payouts are your job to reconcile.
  - id: stripe-bt-types
    title: "Stripe documentation, “Balance transaction types”"
    url: https://docs.stripe.com/reports/balance-transaction-types
    note: Every kind of money movement in a Stripe balance, including disputes, payout failures and currency conversion.
  - id: adyen-sdr
    title: "Adyen documentation, “Settlement details report”"
    url: https://docs.adyen.com/reporting/settlement-reconciliation/transaction-level/settlement-details-report
    note: Adyen's per-batch report, with its references, record types, gross and net amounts, fee columns and exchange rate.
  - id: adyen-camt
    title: "Adyen documentation, “Business account statement” (CAMT.053)"
    url: https://docs.adyen.com/business-accounts/business-account-statement
    note: A real ISO 20022 camt.053 statement, field by field, with booked balances, booking dates and value dates.
  - id: circle-camt
    title: "Circle developer documentation, “CAMT.053 statements”"
    url: https://developers.circle.com/circle-mint/camt053-statements
    note: One statement file per UTC day, and the opening-plus-entries-equals-closing check.
  - id: mt-bai2
    title: "Modern Treasury, “What are BAI2 files?” (2021)"
    url: https://www.moderntreasury.com/learn/bai2
    note: The bank statement file format used by many US banks, and how often banks send it.
  - id: mt-expected
    title: "Modern Treasury API reference, “Expected Payments”"
    url: https://docs.moderntreasury.com/platform/reference/expected-payment-object
    note: Matching an incoming payment you expect against bank data, with amount and date ranges.
  - id: stripe-ledger
    title: "Ilya Ganelin, “Ledger: Stripe's system for tracking and validating money movement”, Stripe (2024)"
    url: https://stripe.dev/blog/ledger-stripe-system-for-tracking-and-validating-money-movement
    note: How Stripe finds money that did not finish its journey, tracks ageing balances and alerts the right owner.
claims_checked:
  - claim: "The CFPB complaint alleges the Chapter 11 trustee estimated the shortfall between the partner banks' holdings and Synapse's records at $60 to $90 million."
    source: cfpb-synapse
    note: "Complaint ¶50 (Doc 1, filed 08/21/25). An allegation in a complaint, not a court finding."
  - claim: "The court entered a stipulated order on 12 September 2025, without adjudication of any issue of fact or law; the trustee and debtor neither admit nor deny the allegations."
    source: cfpb-synapse-order
    note: "Read the order PDF: stamped 'FILED & ENTERED SEP 12 2025' (Doc 4); preamble and Finding 7. Date also on the CFPB action page."
  - claim: "Stripe's payout reconciliation report gives gross, fee and net in major units, and groups a payout by estimated arrival date, not the bank's posting date."
    source: stripe-recon
    note: "Column descriptions ('Expressed in major units of the currency') and the 'Payout timing and report availability' section."
  - claim: "For manual payouts, Stripe cannot identify which transactions a payout includes, so you must reconcile them yourself."
    source: stripe-recon-api
    note: "'Reconciliation for manual payouts' callout."
  - claim: "Adyen's PSP reference is a 16-character, globally unique id; the exchange rate column is 1 when you process and settle in the same currency."
    source: adyen-sdr
    note: "Settlement details report column descriptions."
  - claim: "Modern Treasury expected payments use amount and date bounds; the date range must not exceed 75 days (200 for check payments)."
    source: mt-expected
    note: "Expected Payment object reference."
quiz:
  - q: Your ledger shows a payment of ₹500. The provider's report shows the same payment id with ₹500 gross, ₹10 fee and ₹490 net. Is this a break?
    options:
      - Yes. The amounts differ by ₹10.
      - No, if your ledger also records the ₹10 fee. Compare gross with gross and fee with fee.
      - No. Small differences are always fine.
    answer: 1
    why: Providers pay out the net amount. A fee is not a break if you record it as its own entry and compare each part.
  - q: A payment succeeded at 23:50 on 31 March. It is missing from the provider's report for 31 March. What is the most likely first step?
    options:
      - Post a reversal at once.
      - Mark it as a timing break and check the next day's report before you act.
      - Delete the payment from the ledger.
    answer: 1
    why: Reports and statements have cut-off times. A late payment often appears in the next file. Escalate only if it is still missing after the expected window.
  - q: The bank deposit matches the payout total, but your ledger expected less. What does that tell you?
    options:
      - Everything is fine, because the bank agrees with the provider.
      - Your ledger is missing or misstating something the provider included.
      - The bank made a mistake.
    answer: 1
    why: Reconciliation compares every pair of sides. The provider and bank agree with each other, so the gap is on your side.
  - q: Last week a fee was booked as ₹20 instead of ₹26. How do you fix the ledger?
    options:
      - Update the old entry to ₹26.
      - Post a new adjusting entry for ₹6, linked to the break and the old entry.
      - Change the provider's report to say ₹20.
    answer: 1
    why: Ledger history is never edited. A new, linked entry fixes the balance and keeps the record of what happened.
  - q: Which number is the best early warning that reconciliation is failing?
    options:
      - The total number of payments today.
      - The count and value of open breaks older than their expected window.
      - The number of reconciliation jobs that ran.
    answer: 1
    why: New breaks are normal. Breaks that stay open past their window mean money is missing, late or wrong, and nobody has explained it.
incidents: []
cases: [stripe-ledger]
related: [money-ledgers, payment-lifecycle, idempotency-retries, audit-trails, payment-rails]
---

## What broke

In May 2024 thousands of people suddenly could not touch their money. According to a later complaint by the US Consumer Financial Protection Bureau (CFPB), some waited more than eight months. [@cfpb-synapse] They used apps built on Synapse, which connected fintech apps to the banks that held the money.

The complaint describes the setup. Synapse told several partner banks where to move customer money. According to the CFPB, Synapse was responsible for tracking where each customer's money was. It also had to make sure its records matched the banks' records. [@cfpb-synapse]

The complaint says they did not match. As early as September 2023, one bank held less money than Synapse's records said it should. The bank and Synapse blamed each other for the gap. [@cfpb-synapse] Synapse filed for bankruptcy in April 2024.

In May 2024, the complaint says, Synapse stopped sending one bank the daily ledger reports it used to send. The bank also lost access to a dashboard of customer balances. On 11 May that bank froze all activity for end users. [@cfpb-synapse]

According to the complaint, the banks together held less money than Synapse's records showed. The court-appointed trustee estimated the gap at $60 to $90 million. [@cfpb-synapse] Two banks hired outside help to reconcile and confirm each balance. They used their own records, Federal Reserve records and records from the trustee. [@cfpb-synapse]

The CFPB alleged that Synapse failed to keep adequate records of where consumers' money was. It also alleged that Synapse failed to make sure those records matched the banks' records. [@cfpb-synapse] The case was settled when the court entered an order on 12 September 2025. [@cfpb-action] Synapse's bankruptcy trustee agreed to it without admitting or denying the allegations. [@cfpb-synapse-order]

Records that nobody compares can drift apart for months. This lesson is about the daily check that catches the gap early.

## The idea in simple words

Your system keeps its own record of money: orders, payments and a [ledger](/learn/money-and-double-entry-ledgers). But you do not hold the money. A payment provider and a bank do. They keep their own records.

**Reconciliation** means comparing your records with theirs, line by line, and explaining every difference. A difference is called a **reconciliation break**.

Usually you compare three sides:

1. **Your ledger.** What you think happened.
2. **The provider's report.** What Stripe, Adyen or another provider says it processed, and what it kept as fees.
3. **The bank statement.** What actually arrived in your bank account.

Each pair must agree. If two agree and the third does not, you know where to look.

> [!key] **The key idea.** Your ledger is only a claim. Reconciliation proves the claim against outside records every day. Every break gets a type, an owner and an age, and you fix it with new entries, never by editing old ones.

## How it works

Step through one day of card payments. Use Next and Back, or the ← and → keys.

[[diagram:recon-payout-match]]

### Step 1: Load the outside files

Providers send settlement reports. Stripe's payout reconciliation report lists every payment, refund, dispute and fee inside each payout. [@stripe-recon] You can get the same list from the API: list balance transactions and filter by the payout id. [@stripe-recon-api] Adyen creates a settlement details report, by default one per merchant account per batch. [@adyen-sdr]

Banks send statements. Many US banks send BAI2 files. Banks usually send one every day with posted transactions, and more during the day with pending ones. [@mt-bai2] Another standard format is camt.053, the ISO 20022 XML bank-to-customer statement. Adyen and Circle, for example, provide statements in it. [@adyen-camt] [@circle-camt]

Turn every file into one shape: id, amount in minor units, currency, date, type. Watch the units. Stripe's report gives amounts in major units, like dollars, not cents. [@stripe-recon]

### Step 2: Match on keys

Match on exact ids first. Store the provider's id on your payment row when you create it. Adyen's PSP reference is a 16-character, globally unique id. Its report also carries your own merchant reference from the payment request. [@adyen-sdr] Stripe's itemised report has `balance_transaction_id` by default. You can add columns such as `payment_intent_id` and `charge_id`. [@stripe-recon]

The payout must then match the bank deposit. Stripe's report can include a `trace_id`, which banks use to find a transfer. It can also include a reference that appears on your bank statement. [@stripe-recon]

Some money arrives with no id you know, like a customer's bank transfer. Then you match on amount, currency and a date window. Modern Treasury's "expected payments" work this way. You give an amount range and a date range, and the date range can be at most 75 days (200 for cheques). [@mt-expected] Keep windows tight, or you get false matches.

### Step 3: Compare amounts, then sort the breaks

A matched pair can still disagree. Compare gross, fee and net separately. In Adyen's report, the commission should be the difference between the gross and net amounts. For some acquirers it is split into markup, scheme fee and interchange columns instead. [@adyen-sdr] Your ledger should record the fee as its own entry, so the net is never a surprise.

Common break types:

- **Missing on your side.** The provider has a payment your ledger never recorded. A lost webhook is a common cause.
- **Missing on their side.** You recorded a payment the provider does not show.
- **Amount or currency mismatch.** Same id, different numbers.
- **Duplicate.** The same id appears twice on one side. A retry without an [idempotency key](/learn/idempotency-retries-timeouts) is a common cause.
- **Timing.** The item will appear, but in the next file. Stripe groups a payout by its estimated arrival date, not by the date your bank posts it. Report data can also be ready after the money arrives. [@stripe-recon]
- **Fees and other provider entries.** Disputes and failed payouts show up as their own balance transactions. [@stripe-bt-types]
- **FX.** Adyen's report has an exchange rate column. It is 1 when you process and settle in the same currency. [@adyen-sdr]

Finally, check the statement against itself. The opening balance plus all entries must equal the closing balance. [@circle-camt] If it does not, something is wrong. The file may be incomplete, or your parser may have read it wrongly. Do not reconcile against it.

### Step 4: Work the break queue

Put every break in a queue with a type, an owner and an age. Many breaks follow known patterns, so write auto-fix rules for them. A timing break can wait one cycle. A missing fee can post a fee entry. Everything else goes to a person.

Stripe's Ledger team tracks "Aging Balances" for problems that persist over time. Its tooling lets people move a problem to the right owner. [@stripe-ledger] Stripe also checks that every id in a source system has a matching ledger event. [@stripe-ledger]

The fix is always a new ledger entry: an adjustment or a [reversal](/learn/money-and-double-entry-ledgers), linked to the break. You never edit or delete history. Money you cannot explain yet sits in a **suspense account** with an owner, not in revenue.

## Trade-offs

| Option | Catches problems | Cost | Use it when |
|---|---|---|---|
| Daily batch recon on final files | Yes, but up to a day late. | Low. Simple jobs on files that do not change. | Always. This is the base layer. |
| Near real-time recon on webhooks and intraday data | Yes. Within minutes. | Higher. Data is pending and may change, so you get more false breaks. | High volume, instant payouts, or tight cash. |
| Exact id matching | Yes. Very few false matches. | You must store provider ids on day one. | Card and provider payments. |
| Amount and date window matching | Partly. Can match the wrong item. | Needs tuning and manual review. | Bank transfers with no shared id. |
| Manual payouts | Only if you do the work. | Stripe cannot tell you which transactions a manual payout covers. [@stripe-recon-api] | Rarely. Prefer automatic payouts if you can. |

**Simple rule:** reconcile every day on final data, match on stored ids, and treat any break older than its window as an incident.

Real-time recon helps, but it does not replace the daily run, because final statements still arrive once a day. Circle, for example, delivers one camt.053 file per UTC day, by 06:00 UTC the next day. [@circle-camt] Stripe sends a `reporting.report_type.updated` webhook when new report data is ready. [@stripe-recon] Use that event to start the job, not a fixed clock.

## At work: how a senior uses this

- In a design review, ask: **"Which provider id do we store on this row, and when?"** If the answer is "we look it up later", ask for the id to be stored when the row is created.
- Ask: **"What are the three sides here, and who owns each one?"** A missing side is a missing control.
- Write the expected window for each break type in the code: timing breaks may wait one cycle, missing payments must not.
- Alert on the count and value of breaks older than their window, per currency. Also alert when an expected file does not arrive.
- Require a second person to approve manual adjustments above a set amount. Log who approved each one.
- After any bug fix that touched money, run the full recon again. Old breaks should close, and no new ones should open.

## With AI

### What to ask the agent

> "Write a parser for this Stripe itemised payout reconciliation CSV. Convert amounts from major units to integer minor units using each row's currency. Add tests for a zero-decimal currency."

> "Given these two tables, ledger_entries and provider_report_lines, write SQL that lists rows found on only one side, rows with the same id but different amounts, and ids that appear twice."

> "Design a break queue table with type, owner, age, status and a link to the fixing ledger entry. Explain how a break is closed."

### What to check yourself

- Amounts are integers in minor units after parsing. No floats.
- The matcher uses exact ids before any fuzzy rule. Fuzzy windows are small and logged.
- Duplicates are found, not silently merged.
- Fixes create new ledger entries. The agent did not write an `UPDATE` on old entries.
- Timezones and cut-off times match the provider's and bank's files.
- A missing or empty file raises an alert. It must not count as "zero breaks".

## Check yourself

## Go deeper

- [Money and double-entry ledgers](/learn/money-and-double-entry-ledgers): entries, reversals and clearing accounts that should return to zero.
- [The payment lifecycle](/learn/payment-lifecycle): authorization, capture, settlement and payout, the steps that create the files you reconcile.
- [Idempotency, retries and timeouts](/learn/idempotency-retries-timeouts): how to stop the duplicates before they become breaks.
