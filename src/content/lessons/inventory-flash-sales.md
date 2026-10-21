---
topic: inventory-flash-sales
subtitle: Sell the last item exactly once, even when thousands of people tap "Buy" in the same second, and tell them the truth while they wait.
status: published
reviewed_by: ai-factcheck
reviewed_on: 2026-10-21
illustrative_story: true
sources:
  - id: pg-iso
    title: "PostgreSQL documentation, “Transaction Isolation” (Read Committed)"
    url: https://www.postgresql.org/docs/current/transaction-iso.html
    note: What an UPDATE does when another transaction changed the same row first. It waits, then checks the WHERE clause again.
  - id: pg-update
    title: "PostgreSQL documentation, “UPDATE”"
    url: https://www.postgresql.org/docs/current/sql-update.html
    note: The RETURNING clause, and the "UPDATE count" result, where a count of 0 is not an error.
  - id: pg-lock
    title: "PostgreSQL documentation, “Explicit Locking”"
    url: https://www.postgresql.org/docs/current/explicit-locking.html
    note: Row-level locks with SELECT FOR UPDATE, who they block, and how to avoid deadlocks.
  - id: pg-select
    title: "PostgreSQL documentation, “SELECT”: the locking clause"
    url: https://www.postgresql.org/docs/current/sql-select.html
    note: NOWAIT and SKIP LOCKED, and when skipping locked rows is safe to use.
  - id: shopify-states
    title: "Shopify developer docs, “Manage inventory quantities and states”"
    url: https://shopify.dev/docs/apps/build/orders-fulfillment/inventory-management-apps/manage-quantities-states
    note: A real inventory model with on_hand, available, committed, reserved and safety_stock.
  - id: polardb-hot
    title: "Alibaba Cloud documentation, “Hot row optimization” (PolarDB for MySQL)"
    url: https://www.alibabacloud.com/help/en/polardb/polardb-for-mysql/user-guide/hot-row-optimization
    note: Why updates to one hot row run one after another, and how the database groups them.
  - id: alibaba-decade
    title: "Alibaba Cloud blog, “A Decade of Evolution of Alibaba's Databases - Part 1”"
    url: https://www.alibabacloud.com/blog/a-decade-of-evolution-of-alibabas-databases---part-1_594444
    note: Alibaba's work on the "inventory hotspot" problem for its Double Eleven sales.
  - id: shopify-flash-1
    title: "Emil Stolarsky, “Surviving Flashes of High-Write Traffic Using Scriptable Load Balancers (Part I)”, Shopify Engineering (2017)"
    url: https://shopify.engineering/surviving-flashes-of-high-write-traffic-using-scriptable-load-balancers-part-i
    note: How checkout writes overloaded the database during flash sales, and the first checkout queue.
  - id: shopify-flash-2
    title: "Emil Stolarsky, “Surviving Flashes of High-Write Traffic Using Scriptable Load Balancers (Part II)”, Shopify Engineering (2017)"
    url: https://shopify.engineering/surviving-flashes-of-high-write-traffic-using-scriptable-load-balancers-part-ii
    note: Making the queue fair, with a first-attempt timestamp in a signed cookie.
  - id: cf-waiting-room
    title: "Cloudflare documentation, “About Waiting Room”"
    url: https://developers.cloudflare.com/waiting-room/about/
    note: Total active users, new users per minute, first-in-first-out order and estimated wait times.
claims_checked:
  - claim: "Under Read Committed, a second UPDATE on a row changed by a concurrent transaction waits for it to commit, then re-checks the WHERE clause on the new row version."
    source: pg-iso
    note: "Read Committed section: 'the would-be updater will wait ... The search condition of the command (the WHERE clause) is re-evaluated'. Read Committed is the default. Supports the stock > 0 fix and quiz 2."
  - claim: "UPDATE returns 'UPDATE count'; a count of 0 is not an error."
    source: pg-update
    note: "Outputs section: 'If count is 0, no rows were updated by the query (this is not considered an error).'"
  - claim: "SKIP LOCKED gives an inconsistent view, not for general work, but fits queue-like tables."
    source: pg-select
    note: "Locking clause section, matches. Bucket splitting itself is labelled as the lesson's advice; no source covers it."
  - claim: "Shopify: checkout writes took down segments of the platform; first queue was a lottery, some waited up to 40 minutes for a 40-minute sale; fix used a first-attempt timestamp in a signed cookie."
    source: shopify-flash-1
    note: "Part I (Feb 2017) for the problem, cached queue page, lottery and 40 minutes. Part II (shopify-flash-2) for the signed-cookie timestamp. Part II says earlier arrivals 'wouldn't lose out', not strict first-come order; wording softened."
  - claim: "Hot-row updates are serialized by the row lock; sharding gives limited gains; Alibaba solved the inventory hotspot in time for Double Eleven 2013."
    source: polardb-hot
    note: "PolarDB doc: lock held until commit or rollback; 'database and table sharding strategies provide limited performance improvements'. 2013 quote from alibaba-decade (Zhang Rui, Database Division)."
  - claim: "Cloudflare Waiting Room uses total active users and new users per minute, a FIFO queue and an estimated wait time."
    source: cf-waiting-room
    note: "About Waiting Room page, matches."
quiz:
  - q: Your code reads stock, checks it is above 0 in the app, then writes stock − 1. What happens when two buyers try for the last item at once?
    options:
      - The database stops the second buyer automatically.
      - Both can read 1, both write 0, and you sell two items you do not have.
      - The second write fails with an error.
    answer: 1
    why: The check and the write are separate steps. Both requests pass the check before either one writes.
  - q: "In Postgres (Read Committed), two requests run UPDATE items SET stock = stock - 1 WHERE id = 7 AND stock > 0 when stock is 1. What happens?"
    options:
      - Both succeed and stock becomes −1.
      - One changes 1 row. The other waits, checks the WHERE again on the new value, and changes 0 rows.
      - Both fail with a deadlock.
    answer: 1
    why: Postgres makes the second updater wait, then re-checks the WHERE clause on the updated row. 0 rows changed means sold out.
  - q: A flash sale has 1,000 units of one product in one row. Checkout is slow even though the database CPU is low. What is the likely cause?
    options:
      - The cache is too small.
      - Every purchase waits for the lock on the same hot row, so updates run one at a time.
      - The network is slow.
    answer: 1
    why: Updates to one row are serialized by its lock. Splitting stock into several bucket rows lets purchases run in parallel.
  - q: Why put a time limit on a stock reservation?
    options:
      - So stock held by people who leave without paying goes back on sale.
      - To make the database faster.
      - Because the law requires it.
    answer: 0
    why: Without expiry, abandoned checkouts keep items held forever, and the site shows sold out while the shelf is full.
incidents: []
related: [transactions-isolation, concurrency-control, caching-stampedes, rate-limiting, load-shedding, cart, checkout-orders, sagas-outbox-cdc]
---

## What broke

A shop in Bengaluru runs a midnight sale: 500 pairs of a limited sneaker at ₹4,999. Marketing has been posting about it for two weeks.

At 00:00, about 40,000 people tap "Buy" within the first minute. The checkout code reads the stock, checks that it is above zero, and then writes the new number.

The sale page says "sold out" after 90 seconds. Everyone celebrates. The next morning, the warehouse counts 523 paid orders for 500 pairs. Twenty-three customers get a refund and an apology email. Several post screenshots online.

There was a second problem. During the first minute, checkout pages took 20 seconds to load. The stock row was the bottleneck. Every purchase had to wait its turn for that one row.

## The idea in simple words

**Overselling** happens when two requests check the stock at the same time. Both see "1 left". Both decide to sell. Both write. This is a race between a read and a write. It is the same bug as the lost update in [transactions and isolation levels](/learn/transactions-and-isolation-levels).

The fix is simple: do not check in your app code. Ask the database to check and change the number in one step. If the number is already zero, the database changes nothing, and you show "sold out".

A flash sale adds a second problem: everyone wants the same row. The row lock that keeps you correct also makes buyers wait one at a time. So you also need ways to spread the work, and to let fewer people in at once.

> [!key] **The key idea.** Correct stock is a single number that only the database may change, in one atomic step. Speed comes from splitting that number, and from controlling who reaches checkout. It never comes from skipping the check.

## How it works

Step through two buyers racing for the last item, and the one-line fix. Use Next and Back, or the ← and → keys.

[[diagram:last-item-race]]

### Fix 1: an atomic conditional decrement

Write the check into the UPDATE itself:

```sql
UPDATE items SET stock = stock - 1
WHERE id = 7 AND stock > 0
RETURNING stock;
```

In Postgres, under the default Read Committed level, a second UPDATE on the same row waits for the first one to commit. Then Postgres checks the WHERE clause again on the new version of the row. [@pg-iso] So when stock reaches 0, the next UPDATE matches no row.

Postgres reports this as `UPDATE 0`. That is not an error. [@pg-update] Your code must read the count, or the RETURNING result, and treat 0 rows as "sold out". To sell three items, use `stock >= 3`.

### Fix 2: lock the row first

Sometimes you must read before you decide, for example to check a per-customer limit. Then use `SELECT ... FOR UPDATE`. It locks the row until your transaction ends. Other transactions that try to update or lock it wait. [@pg-lock]

Keep these transactions short. Never call a payment API while you hold the lock. If you lock more than one row, always lock them in the same order, such as by ID. Postgres advises this to avoid deadlocks. [@pg-lock]

### Fix 3: reservations with expiry

You can hold stock when the buyer starts checkout, not only at payment time. Shopify's inventory model has states for held stock. It splits the units on hand into states such as available, committed and reserved. [@shopify-states]

A reservation is a row like "1 unit for cart 88, until 00:10". Your conditional UPDATE moves one unit from available to reserved. Payment turns the reservation into a sale. A small job, or a check at read time, releases reservations that expire. Without expiry, people who leave without paying keep stock held forever.

Pick the expiry from real data, like how long most people take to pay. A short expiry frees stock fast. Too short, and buyers lose the item while typing a card number.

### The hot row

In a flash sale, every buyer updates the same row. A transaction holds the row lock until it commits. So, as Alibaba's PolarDB docs explain, updates to one hot row run one at a time. The docs say sharding the database or tables gives limited gains here. [@polardb-hot] Alibaba calls this the "inventory hotspot" problem. A researcher from its database team says it solved the problem in time for Double Eleven in 2013. [@alibaba-decade]

You cannot remove this waiting, but you can make each turn shorter. Do only the stock UPDATE inside the transaction. Do the rest of the order, like the address and the email, outside it.

### Splitting stock into buckets

This section is our advice, not a method from the sources above. Split one stock number into several rows. For example, store 500 sneakers as 10 bucket rows of 50. Each request picks a bucket at random and runs the conditional UPDATE on it. Now 10 purchases can run at the same time, not one.

When a bucket is empty, try another. Near the end, a buyer may see "sold out" while one other bucket still has a pair. You can handle this with a final pass over all buckets, or a small job that moves leftover units. In Postgres, `FOR UPDATE SKIP LOCKED` skips rows that others hold. The docs say it gives an inconsistent view, but it suits queue-like tables. [@pg-select] That makes it useful to pick any free bucket.

### Queues and virtual waiting rooms

Even a fast database has a limit. So control how many people reach checkout. Shopify learned this in its flash sales. Checkout writes were taking down parts of the platform. [@shopify-flash-1] It added a throttle, a limit on how many buyers get through, in its load balancers. Buyers over the limit saw a cached queue page that checked again from time to time. [@shopify-flash-1]

The first version picked people at random. Some customers waited up to 40 minutes for a sale that lasted 40 minutes. [@shopify-flash-1] The fix stored each buyer's first attempt time in a signed cookie, so earlier buyers would not lose out to later ones. [@shopify-flash-2]

You can also buy this as a product. Cloudflare's Waiting Room admits visitors based on two limits: total active users and new users per minute. Others wait in a first-in-first-out queue and see an estimated wait time. [@cf-waiting-room] Rate-limit checkout, not browsing. See [rate limiting](/learn/rate-limiting).

### Caching availability vs truth

The product page might get 100 times more views than purchases. Do not hit the stock row for each view. Cache "in stock" or "few left" for a few seconds, as in [caching and cache stampedes](/learn/caching-and-cache-stampedes).

But the cache is only a hint. The conditional UPDATE at checkout is the truth. A stale cache may show "in stock" for a few seconds after the last sale. That is fine, as long as checkout says "sold out" clearly.

### Being honest with users

Tell people where they stand. Show their place in the queue or an estimated wait. When stock runs out, tell everyone in the queue at once, so they stop waiting. When a buyer reaches checkout, say how long their item is held. If you ever do oversell, refund fast and say sorry plainly.

## Trade-offs

| Option | Prevents overselling? | Speed on one hot item | Use when |
|---|---|---|---|
| Read, check in app, write | No. | Fast, but wrong. | Never for stock. |
| Atomic conditional UPDATE | Yes. | Limited by one row lock. | The default for most shops. |
| SELECT FOR UPDATE, then write | Yes. | Slower. The lock is held longer. | You need other checks before the write. |
| Reservations with expiry | Yes, if made with a conditional UPDATE. | Same as the UPDATE. | Checkout takes minutes, such as card payments. |
| Stock split into buckets | Yes. | Much better. | Flash sales of one product. |
| Waiting room in front | Indirectly. It limits load. | Protects everything behind it. | Planned big sales and ticket drops. |
| Cached availability | No. It is only a hint. | Very fast reads. | Product and list pages. |

**Simple rule:** make the conditional UPDATE the only way stock goes down. Add buckets and a waiting room when one product gets a planned rush.

## At work: how a senior uses this

- Search the code for "read stock, then write stock". Replace each case with a conditional UPDATE, and check the rows-changed count.
- Ask product: "How long do we hold an item in checkout? What do we tell a buyer when it expires?"
- Before a big sale, load-test checkout for the one hot product, not a spread of products. See [load shedding](/learn/load-shedding-graceful-degradation).
- Add a check that runs every few minutes: units sold plus units available must equal the starting stock. Alert if they differ.
- Plan for the user, not just the database: a queue page, a clear "sold out" for everyone, and a refund plan.
- Keep the stock transaction tiny. No payment calls, emails or HTTP calls while a row lock is held.

## With AI

### What to ask the agent

> "Find every place in this repo where we read stock and then update it in separate statements. Rewrite each one as a single conditional UPDATE, and handle 0 rows as sold out."

> "Write a test that starts 50 parallel purchases against a product with stock 10, using a real Postgres database. Assert that exactly 10 succeed and stock ends at 0."

> "Design a stock-bucket scheme for one product with 500 units in 10 rows. Show how a buyer picks a bucket, and what happens when the chosen bucket is empty."

### What to check yourself

- The test really runs purchases in parallel, against a real database. A loop that buys one at a time proves nothing.
- The code checks the rows-changed count. An UPDATE that changes 0 rows does not raise an error.
- No network call happens inside the stock transaction.
- Expired reservations go back to available stock, and this has its own test.
- The agent did not "fix" the race with an app-level lock or a mutex. That breaks as soon as you run two servers.

## Check yourself

## Go deeper

Before this: [transactions and isolation levels](/learn/transactions-and-isolation-levels) and [locking and MVCC in practice](/learn/locking-and-mvcc-in-practice). Next: [checkout and the order state machine](/learn/checkout-and-order-state-machine), and [sagas, outbox and CDC](/learn/sagas-outbox-and-cdc) for releasing stock when payment fails.
