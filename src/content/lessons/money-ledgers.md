---
topic: money-ledgers
subtitle: Why money must never be a float, and how a ledger where every entry sums to zero keeps every rupee accounted for.
status: published
reviewed_by: ai-factcheck
reviewed_on: 2026-10-21
illustrative_story: true
sources:
  - id: python-float
    title: "Python documentation, “Floating-point arithmetic: issues and limitations”"
    url: https://docs.python.org/3/tutorial/floatingpoint.html
    note: Why 0.1 cannot be stored exactly in binary, with small examples you can run yourself.
  - id: iso4217
    title: "SIX Group, ISO 4217 currency codes (the official maintenance agency and its code lists)"
    url: https://www.six-group.com/en/products-services/financial-information/data-standards.html
    note: The official currency list. Its "minor unit" column says how many decimal places each currency has.
  - id: stripe-currencies
    title: "Stripe documentation, “Supported currencies”: specify amounts in minor units"
    url: https://docs.stripe.com/currencies
    note: A real payment API that takes every amount as an integer in the currency's smallest unit.
  - id: mt
    title: "Lucas Rocha, “Accounting for Developers, Part I: The Fundamentals”, Modern Treasury"
    url: https://moderntreasury.com/journal/accounting-for-developers-part-i
    note: Debits, credits and double-entry, explained for engineers with small examples.
  - id: square-books
    title: "Square Engineering, “Books, an immutable double-entry accounting database service” (2019)"
    url: https://developer.squareup.com/blog/books-an-immutable-double-entry-accounting-database-service/
    note: How Square built an append-only ledger where every journal entry balances to zero.
  - id: stripe-ledger
    title: "Ilya Ganelin, “Ledger: Stripe's system for tracking and validating money movement”, Stripe (2024)"
    url: https://stripe.dev/blog/ledger-stripe-system-for-tracking-and-validating-money-movement
    note: An immutable ledger at large scale, and how clearing accounts and data checks find missing money.
  - id: tigerbeetle
    title: "TigerBeetle documentation, “Debit / Credit: the schema for OLTP”"
    url: https://docs.tigerbeetle.com/concepts/debit-credit/
    note: A ledger database built around double-entry. Transfers cannot be erased, only reversed.
  - id: fowler
    title: "Martin Fowler, “Accounting Entry” (analysis pattern, 2005)"
    url: https://martinfowler.com/eaaDev/AccountingEntry.html
    note: A classic pattern write-up on entries, accounts and when entries become immutable.
  - id: python-decimal
    title: "Python documentation, “decimal — Decimal fixed-point and floating-point arithmetic”"
    url: https://docs.python.org/3/library/decimal.html
    note: Exact decimal arithmetic and its rounding modes, including ROUND_HALF_EVEN, the default.
  - id: mt-hot
    title: "Matt McNierney, “Behind the Scenes: How We Built Ledgers for High Throughput”, Modern Treasury (2024)"
    url: https://www.moderntreasury.com/journal/behind-the-scenes-how-we-built-ledgers-for-high-throughput
    note: The "hot account" problem in a real ledger, and how batching entries reduces the contention.
claims_checked:
  - claim: "0.1 + 0.1 + 0.1 == 0.3 is false in Python; an exact decimal type is the alternative."
    source: python-float
    note: "Tutorial shows the expression returning False and recommends the decimal module for accounting. Ran it locally: 0.1 + 0.2 = 0.30000000000000004."
  - claim: "ISO 4217 minor units: INR 2, JPY 0, KWD 3."
    source: iso4217
    note: "Checked SIX List One XML (CcyMnrUnts): INR 2, JPY 0, KWD 3."
  - claim: "Stripe takes amounts in minor units: 1000 charges 10 USD; ¥500 is 500."
    source: stripe-currencies
    note: "Supported currencies page: '1000 to charge 10 USD'; zero-decimal example 'to charge 500 JPY, provide an amount value of 500'."
  - claim: "Square Books entries are append-only and immutable once stored, every journal entry balances to 0, and a balance sums all changes."
    source: square-books
    note: "Quotes match the 2019 post by Łukasz Strzałkowski."
  - claim: "Stripe Ledger is an immutable log; clearing accounts should return to zero; every producer id must have a matching Ledger event."
    source: stripe-ledger
    note: "Post (Ilya Ganelin, 2024-02-16) says 'an immutable log of events', not 'immutable and auditable log'. Quote corrected."
  - claim: "Debit/credit direction depends on account type; transactions have two or more entries."
    source: mt
    note: "Modern Treasury: debit normal accounts increase with debits, credit normal with credits; 'Transactions must have two or more entries.'"
  - claim: "TigerBeetle transfers cannot be erased; fixes are separate reversal transfers. Fowler: entries can change while open, not once booked."
    source: tigerbeetle
    note: "TigerBeetle page quote matches. Fowler's Accounting Entry (2005) says entries may change 'in an open state, but not ... when they are closed, or booked'."
  - claim: "A hot account appears in most transactions, so writes wait on one balance row; batching entries helps."
    source: mt-hot
    note: "Modern Treasury post (2024-01-25): contention on 'the single balance cache row' spikes write latency; fixed with async batching. It does not mention splitting accounts, so that was removed."
  - claim: "Round half to even sends ties to the nearest even digit and is Python Decimal's default."
    source: python-decimal
    note: "decimal docs: ROUND_HALF_EVEN 'Round to nearest with ties going to nearest even integer'; DefaultContext rounding = ROUND_HALF_EVEN."
  - claim: "Modern Treasury processes hot-account entries asynchronously in batches; the balance cache catches up later (target 60 s, p90 about 1 s)."
    source: mt-hot
    note: "QC1-19: 'can be asynchronously batch processed'; 'All Entries eventually make it to the balance caches'; 'We target processing all Entries within 60s, and in practice, our p90 time to process is 1s.' Lesson now says this breaks the same-transaction rule on purpose."
glossary_optout: [transaction]
quiz:
  - q: A wallet app stores balances as a float. Why is this a problem?
    options:
      - Floats are too slow for money.
      - Many decimal amounts, like 0.10, cannot be stored exactly, so small errors build up.
      - Floats cannot hold negative numbers.
    answer: 1
    why: Floats store binary fractions. 0.1 is only close, so sums can drift by tiny amounts that never go away.
  - q: You store amounts as integers in minor units. How do you store ₹10.50 and ¥500?
    options:
      - 1050 and 50000
      - 1050 and 500
      - 10.5 and 500
    answer: 1
    why: The rupee has 2 decimal places, so ₹10.50 is 1050 paise. The yen has 0, so ¥500 is just 500.
  - q: A new ledger transaction has entries −1,000, +980 and +15. What should the ledger do?
    options:
      - Post it. The amounts are close enough.
      - Post it, and add a +5 entry to a "difference" account.
      - Reject it. The entries sum to −5, not zero, so this is a bug.
    answer: 2
    why: Every transaction must sum to zero. If it does not, money was created or lost by mistake.
  - q: Yesterday a fee entry was posted with the wrong amount. How do you fix it?
    options:
      - Update the amount in the old entry.
      - Delete the old entry and insert a correct one.
      - Post a reversal of the wrong entry, then post a new correct entry.
    answer: 2
    why: Entries are never edited. A reversal keeps the full history, so anyone can see what happened and why.
  - q: All payments have settled, but your "holding" account still shows ₹20. What does this most likely mean?
    options:
      - Nothing. Small leftovers are normal.
      - An entry is missing, late or wrong.
      - The database needs a restart.
    answer: 1
    why: An account that should go back to zero tells you that some money did not finish its journey.
incidents: []
related: [idempotency-retries, reconciliation, payment-lifecycle]
---

## What broke

This story is made up, but each part of it happens in real companies. A small wallet app gives users cashback. Each user has one row in a table with a `balance` column of type `float`. Every payment runs `balance = balance + amount`.

For months it looks fine. Then the finance team adds up all user balances and compares the total with the money in the bank. The numbers do not match. They are off by ₹38,412.07.

The team looks for the cause and finds three problems. Small float errors on millions of cashback payments do not add up to clean paise. A retry bug added some cashback twice. And one engineer fixed a support ticket by changing a balance by hand.

Nobody can say which part of the ₹38,412.07 came from which problem. The table only holds the current balance. It has no history. The money is gone from the records, and so is the story of how it moved.

## The idea in simple words

Two rules prevent almost all of this.

**Rule one: never store money as a float.** A float stores numbers as binary fractions. Most decimal amounts, like 0.1, cannot be written exactly in binary. So `0.1 + 0.1 + 0.1 == 0.3` is false in Python and most other languages. [@python-float] The error is tiny, but money systems add millions of numbers, and the errors stay.

Instead, store money as an **integer in minor units**. A minor unit is the smallest unit of a currency: paise for rupees, cents for dollars. ₹10.50 becomes `1050`. Stripe's API works this way: you send `1000` to charge 10 US dollars. [@stripe-currencies] Another option is an exact decimal type, like `NUMERIC` in Postgres or `Decimal` in Python. Python's docs suggest its `decimal` module for accounting. [@python-float]

Always keep the currency next to the amount. `1050` means nothing without "INR". And currencies differ. The official ISO 4217 list says the Indian rupee has 2 decimal places, the Japanese yen has 0, and the Kuwaiti dinar has 3. [@iso4217]

**Rule two: record every movement of money as double-entry.** Money never appears or disappears. It always moves *from* somewhere *to* somewhere. So every movement is written as at least two entries: one that takes money out of an account and one that puts it into another. [@mt]

In this lesson we write money leaving an account as minus and money arriving as plus. Accountants call the two sides debit and credit. Which side means "more" depends on the type of account. [@mt] The rule that matters is the same in both styles: the entries of one transaction must sum to zero. [@square-books]

> [!key] **The key idea.** Never change a balance directly. Write balanced entries that never change, and calculate every balance from those entries.

## How it works

Step through a ₹1,000 card payment to a shop, with a ₹20 platform fee. Use Next and Back, or the ← and → keys.

[[diagram:ledger-payment]]

A ledger has three parts:

- **Accounts.** One for each place money can sit: a customer, a merchant, your fee income, money held for a short time.
- **Entries.** One line each: account, amount in minor units, currency, and a sign.
- **Transactions.** A group of entries that happen together and sum to zero. Many teams also call this a journal entry.

### Entries never change

The ledger is append-only. You add new entries. You never update or delete old ones. Square's ledger, Books, works this way: its entries are effectively append-only and immutable once stored. [@square-books] Stripe describes its Ledger as "an immutable log of events". [@stripe-ledger]

So how do you fix a mistake? You post a **reversal**: a new transaction with the same entries and opposite signs. Then you post the correct one. TigerBeetle, a ledger database, follows the same rule: once transfers are recorded, they cannot be erased. [@tigerbeetle] The wrong entry stays in the history, next to its fix. An auditor can see what happened, when, and why.

Martin Fowler notes that some systems let an entry change while it is still "open", then lock it once it is booked. [@fowler] Payment systems often use the same idea as **pending** and **posted** entries.

### Balances come from entries

A balance is the sum of all entries for one account. Square describes a balance as "essentially summing up all of the changes" to that account. [@square-books] Summing millions of rows on every read is slow, so real systems also store a cached balance. They update it in the same database transaction as the new entries. The entries stay the truth. If the cache is ever wrong, you rebuild it from the entries.

### Retries must not post twice

A ledger has the same retry problem as a payment API. If the "post transaction" call times out and the caller retries, you can post the same money twice. Give every ledger transaction a unique key from the event that caused it, such as the payment id. Make that key unique in the database, so a repeat is rejected. This is the idea from [Idempotency, retries and timeouts](/learn/idempotency-retries-timeouts), applied to money.

### Reconciliation: check against the outside world

Your ledger can be perfectly balanced and still be wrong. It may miss a payment that the bank processed. Reconciliation means you regularly compare your ledger with outside records, such as bank statements and payment provider reports. Every difference is a missing, late or wrong entry that someone must explain.

Stripe uses a neat trick inside its ledger. Money that is "on the way" sits in a clearing account. When all steps finish, that account should go back to zero. A balance that is not zero points to a missing, late or wrong transaction. [@stripe-ledger] Stripe also checks that every id in the source systems has a matching ledger event. [@stripe-ledger]

### Rounding

Integers do not remove rounding. They make it visible. Split ₹100.00 between three people and you get 3,333 paise each, with 1 paisa left over. Decide where the leftover goes, for example to the first person, and write it as an entry. Pick one rounding rule and use it everywhere. A common choice is "round half to even": a tie goes to the nearest even digit. It is the default in Python's `Decimal`. [@python-decimal] Write it down in the code, not in someone's head.

## Trade-offs

| Approach | Exact? | Cost | Use it when |
|---|---|---|---|
| Float for amounts | No. Small errors build up. | Easy at first, painful later. | Never for money. Fine for charts and estimates. |
| Integer minor units + currency | Yes. | You must know each currency's decimal places and convert for display. | Most payment and wallet systems. Simple and fast. |
| Decimal type (`NUMERIC`, `Decimal`) | Yes, if you create it from strings, not floats. | Slower, and every language handles it a bit differently. | You need fractions of a minor unit, like interest or FX rates. |
| One balance column, updated in place | No history, so you cannot prove it. | Cheap to build. | Toy projects only. |
| Double-entry ledger, balances derived | Yes, and every change is explained. | More rows, more design work, and busy accounts can slow writes. | Any system that holds or moves real money. |

**Simple rule:** integers in minor units with a currency code, in an append-only, double-entry ledger. Add a cached balance only when reads get slow.

One cost needs a warning. Some accounts, like the platform's own fee account, appear in almost every transaction. Many writes then wait on the same balance row. Modern Treasury calls this the "hot account" problem, and handles it by batching entries for that account. [@mt-hot] This breaks the same-transaction rule on purpose. The cached balance can be a little behind: Modern Treasury targets 60 seconds, and nine in ten entries take about 1 second. [@mt-hot]

## At work: how a senior uses this

- In design reviews, search for `float` and `double` near any money field. Ask: **"Where is the currency?"**
- Ask: **"Where does this money come from, and where does it go?"** If nobody can name both accounts, the design is not ready.
- Add a check in code that refuses any transaction whose entries do not sum to zero. Treat a failure as a bug, never as a warning.
- Ban `UPDATE` and `DELETE` on the entries table. Use database permissions, not only a team rule.
- Give every ledger transaction a unique key from the event that caused it, so retries cannot post twice.
- Run reconciliation every day. Give every difference an owner and a deadline.
- Keep accounts that should return to zero, like money that has been sent but has not arrived yet. Alert when they do not return to zero. [@stripe-ledger]
- Use the same pattern for things that are not money: reward points, credits, gift cards, even stock in a warehouse.

## With AI

### What to ask the agent

> "Search this codebase for money stored or calculated as float or double. List every place, with the file and line, and how you would change it to integer minor units."

> "Write a property-based test: generate random ledger transactions and assert that every posted transaction sums to zero, and that every account balance equals the sum of its entries."

> "Split ₹100.00 between 3, 7 and 9 people. Show the paise each person gets, and prove that nothing is lost or created."

### What to check yourself

- Amounts are integers or exact decimals in every layer: database, code, API and JSON. A float in the middle breaks the whole chain.
- Decimal values are created from strings, not from floats.
- The currency travels with every amount, and the code uses each currency's correct decimal places.
- The agent did not add an `UPDATE` to "fix" a balance. Fixes are new entries.
- The entries and the cached balance change in the same database transaction.
- Tests cover a retry of the same transaction and a split with a leftover paisa.

## Check yourself

## Go deeper

Next on this line: [Reconciliation](/learn/reconciliation) and [The payment lifecycle](/learn/payment-lifecycle).

If you have not read it yet: [Idempotency, retries and timeouts](/learn/idempotency-retries-timeouts).
