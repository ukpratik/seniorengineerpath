---
topic: concurrency-control
subtitle: Pick the right tool when two requests change the same row, and learn how your database keeps reads fast while rows change.
status: published
reviewed_by: ai-factcheck
reviewed_on: 2026-11-09
illustrative_story: false
glossary_optout: [rollback]
sources:
  - id: flexcoin
    title: "Flexcoin, “Flexcoin is shutting down” (notice on flexcoin.com, 3–4 March 2014, Wayback Machine copy)"
    url: http://web.archive.org/web/20140305154222/http://www.flexcoin.com/
    note: The exchange's own notice. It explains how thousands of simultaneous transfers overdrew an account.
  - id: pg-lock
    title: "PostgreSQL documentation: 13.3. Explicit Locking"
    url: https://www.postgresql.org/docs/current/explicit-locking.html
    note: Row-level lock modes and what each one blocks, deadlocks, and advisory locks.
  - id: pg-select
    title: "PostgreSQL documentation: SELECT"
    url: https://www.postgresql.org/docs/current/sql-select.html
    note: "The Locking Clause: NOWAIT, SKIP LOCKED, and why skipping locked rows suits only queue-like tables."
  - id: pg-iso
    title: "PostgreSQL documentation: 13.2. Transaction Isolation"
    url: https://www.postgresql.org/docs/current/transaction-iso.html
    note: In Read Committed, how a second UPDATE waits and then checks its WHERE clause again.
  - id: pg-update
    title: "PostgreSQL documentation: UPDATE"
    url: https://www.postgresql.org/docs/current/sql-update.html
    note: The "UPDATE count" result, where 0 rows is not an error.
  - id: pg-mvcc
    title: "PostgreSQL documentation: 13.1. Introduction (Concurrency Control)"
    url: https://www.postgresql.org/docs/current/mvcc-intro.html
    note: What MVCC is, and why reading never blocks writing in Postgres.
  - id: pg-syscols
    title: "PostgreSQL documentation: 5.6. System Columns"
    url: https://www.postgresql.org/docs/current/ddl-system-columns.html
    note: The hidden xmin and xmax columns on every row version.
  - id: pg-vacuum
    title: "PostgreSQL documentation: 24.1. Routine Vacuuming"
    url: https://www.postgresql.org/docs/current/routine-vacuuming.html
    note: Why old row versions stay, what VACUUM and autovacuum do, and when VACUUM FULL is needed.
  - id: pg-lock-config
    title: "PostgreSQL documentation: 19.12. Lock Management"
    url: https://www.postgresql.org/docs/current/runtime-config-locks.html
    note: deadlock_timeout, how long Postgres waits before it looks for a deadlock.
  - id: pg-errcodes
    title: "PostgreSQL documentation: Appendix A. PostgreSQL Error Codes"
    url: https://www.postgresql.org/docs/current/errcodes-appendix.html
    note: The codes your retry logic should catch, such as 40P01 for a deadlock.
  - id: pg-advisory
    title: "PostgreSQL documentation: 9.28. System Administration Functions (Advisory Lock Functions)"
    url: https://www.postgresql.org/docs/current/functions-admin.html
    note: pg_advisory_xact_lock, pg_try_advisory_xact_lock and their session-level versions.
  - id: pg-95
    title: "PostgreSQL documentation: Release 9.5"
    url: https://www.postgresql.org/docs/9.5/release-9-5.html
    note: The release that added SKIP LOCKED.
  - id: mysql-mvcc
    title: "MySQL 8.4 Reference Manual: 17.3 InnoDB Multi-Versioning"
    url: https://dev.mysql.com/doc/refman/8.4/en/innodb-multi-versioning.html
    note: Undo logs, the hidden row fields, purge, and why you should commit read-only transactions too.
  - id: mysql-locking-reads
    title: "MySQL 8.4 Reference Manual: 17.7.2.4 Locking Reads"
    url: https://dev.mysql.com/doc/refman/8.4/en/innodb-locking-reads.html
    note: FOR UPDATE, FOR SHARE, NOWAIT and SKIP LOCKED in InnoDB.
  - id: mysql-deadlocks
    title: "MySQL 8.4 Reference Manual: 17.7.5 Deadlocks in InnoDB"
    url: https://dev.mysql.com/doc/refman/8.4/en/innodb-deadlocks.html
    note: How InnoDB deadlocks happen, how it picks a victim, and how to reduce them.
  - id: fowler-ool
    title: "Martin Fowler (pattern by David Rice), “Optimistic Offline Lock”, Catalog of Patterns of Enterprise Application Architecture"
    url: https://martinfowler.com/eaaCatalog/optimisticOfflineLock.html
    note: The classic name for a version check that spans more than one database transaction.
claims_checked:
  - claim: "Flexcoin lost 896 BTC, all the coins in its hot wallet, on 2 March 2014 and closed. Thousands of simultaneous transfer requests moved coins until the sending account was overdrawn, before balances were updated."
    source: flexcoin
    note: "Notice of 3 March 2014 and update of 4 March 2014 (Wayback copy). The check-then-write mechanism is labelled in the lesson as our reading."
  - claim: "In Read Committed, an UPDATE that waits on a row another transaction changed re-evaluates its WHERE clause on the updated row; UPDATE reports a count, and 0 is not an error."
    source: pg-iso
    note: "Read Committed section: 'The search condition of the command (the WHERE clause) is re-evaluated'. pg-update: 'If count is 0, no rows were updated by the query (this is not considered an error)'."
  - claim: "SKIP LOCKED skips rows that cannot be locked at once, gives an inconsistent view, suits queue-like tables, and was added in PostgreSQL 9.5."
    source: pg-select
    note: "The Locking Clause; Release 9.5 notes: 'Add SELECT option SKIP LOCKED to skip locked rows'. MySQL 8.4 Locking Reads has the same warning."
  - claim: "Postgres waits deadlock_timeout (default 1s) before checking for a deadlock, aborts one transaction (which one is 'difficult to predict'), and reports 40P01."
    source: pg-lock-config
    note: "Confirmed with pg-lock (13.3.4 Deadlocks) and pg-errcodes (40P01 deadlock_detected, 40001 serialization_failure)."
  - claim: "A row version must not be removed while it is still potentially visible to other transactions; plain VACUUM usually does not return space to the OS; VACUUM FULL needs ACCESS EXCLUSIVE."
    source: pg-vacuum
    note: "24.1.2 Recovering Disk Space. The lesson marks 'a long open transaction holds back cleanup' as an inference from this rule."
  - claim: "InnoDB updates rows in place, keeps old versions in undo logs reached through DB_ROLL_PTR, purges them only when no snapshot needs them, and recommends committing even read-only transactions regularly."
    source: mysql-mvcc
    note: "17.3 InnoDB Multi-Versioning: quote 'including transactions that issue only consistent reads' is exact."
quiz:
  - q: "You run UPDATE tickets SET body = $1, version = version + 1 WHERE id = 7 AND version = 3. Postgres answers UPDATE 0. What does it mean?"
    options:
      - The database had an error. Log it and move on.
      - Someone changed the row after you read it. Reload it, then retry or show a conflict.
      - The update worked, but Postgres did not count it.
    answer: 1
    why: The version is no longer 3, so no row matched. Postgres does not treat 0 rows as an error, so your code must check the count.
  - q: Ten workers take jobs from one jobs table. Each job must run once, and workers should not wait for each other. Which read fits best?
    options:
      - "SELECT ... FOR UPDATE"
      - "SELECT ... FOR UPDATE NOWAIT"
      - "SELECT ... FOR UPDATE SKIP LOCKED"
    answer: 2
    why: SKIP LOCKED passes over rows another worker has locked, so each worker gets a different job at once. The docs name queue-like tables as the use case.
  - q: Someone leaves a Postgres transaction open for six hours. A busy table keeps growing on disk. Why?
    options:
      - VACUUM cannot remove old row versions that the open transaction might still need to see.
      - The open transaction holds a lock that blocks all writes.
      - Autovacuum runs only once a day.
    answer: 0
    why: An old row version must stay while it is still potentially visible to an open transaction. So one old transaction can hold back cleanup of dead versions.
  - q: Two money transfers sometimes fail with "deadlock detected". One locks account 17 then 42, the other 42 then 17. What is the best fix?
    options:
      - Raise deadlock_timeout to 60 seconds.
      - Lock rows in one fixed order, such as lowest ID first, and retry the transaction on a deadlock error.
      - Remove the transaction so each UPDATE commits alone.
    answer: 1
    why: A consistent lock order removes the wait cycle. A retry handles the rare cases that are left. Removing the transaction breaks atomicity.
  - q: Users edit a product description in a form for several minutes before they click Save. Conflicts are rare. Which approach fits?
    options:
      - SELECT ... FOR UPDATE when the form opens, and commit on Save.
      - Optimistic locking with a version column, checked on Save.
      - An advisory lock held for the whole session.
    answer: 1
    why: You must never hold a database lock while a person thinks. A version check needs no lock until the write.
incidents: [flexcoin-2014-03-transfer-race]
related: [transactions-isolation, inventory-flash-sales, concurrency-primitives, idempotency-retries, money-ledgers, schema-migrations, secondary-indexes]
---

## What broke

On 2 March 2014, a small Bitcoin exchange called Flexcoin lost every coin in its hot wallet. The attacker took 896 BTC. The next day, Flexcoin closed for good. [@flexcoin]

The attacker did not break in. They created a normal account and deposited some coins. Then they used Flexcoin's feature for transfers between users. [@flexcoin]

They sent thousands of transfer requests at the same moment. Coins moved out of the sending account "until the sending account was overdrawn, before balances were updated". They repeated this across several accounts to grow the amount, then withdrew it. [@flexcoin]

Flexcoin did not publish its code. Our reading of the notice: each request checked the balance, then wrote a new balance a moment later. Many requests passed the check before the first writes landed.

You have seen this bug before as the lost update. This lesson is about the tools that stop it, and what each one costs.

## The idea in simple words

The [transactions lesson](/learn/transactions-and-isolation-levels) showed that a transaction alone is not a lock. Here you choose how to protect the rows you change. There are three main tools.

1. **One atomic statement.** The database checks and changes the row in a single `UPDATE`. No read in your app code, so no gap.
2. **Pessimistic locking.** You lock the row first, then read and change it. Others who want to change it wait.
3. **Optimistic locking.** You read with no lock. When you write, you check that nobody changed the row in between. If someone did, you retry.

All three protect writers from each other. None of them should slow down plain reads. That is the job of MVCC, the way the database keeps old row versions so readers do not wait.

> [!key] **The key idea.** First try to make the change one statement. If you must read first, lock when conflicts are common and the lock is short. Check a version when conflicts are rare or a person is thinking.

## How it works

### Pessimistic: lock the rows you will change

For a transfer, lock both accounts in one statement, in ID order:

```sql
BEGIN;
SELECT id, balance FROM accounts
WHERE id IN (17, 42)
ORDER BY id
FOR UPDATE;
-- app checks: balance of 17 >= 50
UPDATE accounts SET balance = balance - 50 WHERE id = 17;
UPDATE accounts SET balance = balance + 50 WHERE id = 42;
COMMIT;
```

`FOR UPDATE` stops other transactions from updating, deleting or locking these rows until you commit. Plain `SELECT`s are not blocked. [@pg-lock] In Read Committed (the default), a second Flexcoin-style request would wait here, then read the new balance. [@pg-iso]

There are softer and faster versions:

- **`FOR SHARE`** blocks updates and deletes, but other `FOR SHARE` readers can lock the row too. [@pg-lock] Use it when the row must not change while you work, but you will not change it yourself.
- **`NOWAIT`** returns an error at once if a row is already locked. [@pg-select] Use it to tell a user "someone else is editing this" instead of making them wait.
- **`SKIP LOCKED`** skips rows that are locked. [@pg-select] Postgres added it in 9.5. [@pg-95] MySQL 8.4 has `NOWAIT` and `SKIP LOCKED` too. [@mysql-locking-reads]

`SKIP LOCKED` is built for job queues:

```sql
SELECT id, payload FROM jobs
WHERE status = 'ready'
ORDER BY id
LIMIT 1
FOR UPDATE SKIP LOCKED;
```

Each worker gets a different job, and nobody waits. Both manuals warn that skipping rows gives an inconsistent view. So use it for queue-like tables, not for general work. [@pg-select] [@mysql-locking-reads]

### Optimistic: check a version when you write

Add a `version` column. Read it with the row. When you save, require that it has not changed:

```sql
UPDATE tickets
SET body = $1, version = version + 1
WHERE id = 7 AND version = 3;
```

Step through two agents saving the same ticket.

[[diagram:optimistic-version-race]]

Postgres answers `UPDATE 1` or `UPDATE 0`. A count of 0 is not an error. [@pg-update] So your code must read the count. On 0, reload the row and retry, or show the user a conflict.

What if the other writer has not committed yet? In Read Committed, your `UPDATE` waits for it. Then Postgres checks your `WHERE` again on the new row. [@pg-iso] The version no longer matches, so you get 0 rows.

The big win is time. The read and the write can be minutes apart, in two HTTP requests. No lock is held in between. Martin Fowler's catalog calls this pattern Optimistic Offline Lock. It "assumes that the chance of conflict is low". [@fowler-ool]

### The atomic statement, again

For counters and balances you often need no version at all:

```sql
UPDATE accounts SET balance = balance - 50
WHERE id = 17 AND balance >= 50;
```

In Read Committed, 0 rows means "not enough money" (or no such account). A waiting update checks its WHERE again on the new row. [@pg-iso] In Repeatable Read, you get error 40001 instead. [@pg-iso] The check and the change are one step. That is the shape Flexcoin's transfers needed. The [flash sales lesson](/learn/inventory-overselling-flash-sales) uses the same idea for stock.

### MVCC: why readers do not wait

The Postgres docs say it plainly: "reading never blocks writing and writing never blocks reading". [@pg-mvcc] The trick is that a write keeps the old version of the row for readers who still need it.

**In Postgres,** each `UPDATE` makes a new row version. Each version has hidden columns. `xmin` is the ID of the transaction that created it. `xmax` is the ID of the transaction that deleted or replaced it. [@pg-syscols] It is 0 for an undeleted row. A visible row can still show a number, usually because the deleting transaction has not committed or was rolled back. [@pg-syscols] Try `SELECT xmin, xmax, * FROM accounts;` on a test database.

The old version stays on disk. It must not be removed "while it is still potentially visible to other transactions". VACUUM later frees that space for reuse. Autovacuum runs it for you. [@pg-vacuum]

Plain VACUUM does not usually give space back to the operating system. After a big spike of updates, a table can bloat until you need `VACUUM FULL`. That takes an `ACCESS EXCLUSIVE` lock, so the table is unusable while it runs. [@pg-vacuum]

Here is the trap. A row version stays while it is "still potentially visible to other transactions". [@pg-vacuum] So, we infer, one transaction left open for hours can stop VACUUM from removing dead versions. This applies to a transaction that holds a snapshot or has written something. Table bloat grows. Find such transactions in `pg_stat_activity`, where the docs check `backend_xid` and `backend_xmin`. [@pg-vacuum]

**In MySQL InnoDB,** the row is changed in place. The old content goes to an undo log. Each row has a hidden transaction ID and a pointer to its undo record. Readers rebuild older versions from there. [@mysql-mvcc]

A background purge removes old undo records. It can only discard them when no open snapshot needs them. The manual asks you to commit regularly, "including transactions that issue only consistent reads". [@mysql-mvcc] Same lesson, different engine.

### Deadlocks: two locks, opposite order

Transfer A locks account 17, then wants 42. Transfer B locks 42, then wants 17. Each waits for the other forever.

Postgres waits `deadlock_timeout` (default 1 second) before it checks for this. [@pg-lock-config] Then it aborts one transaction. Which one "is difficult to predict and should not be relied upon". [@pg-lock] The error code is `40P01`. [@pg-errcodes] InnoDB also detects deadlocks by default and rolls back a victim. [@mysql-deadlocks]

The fix has two parts. Lock rows in a consistent order, like the `ORDER BY id` above. [@pg-lock] Then retry the whole transaction on a deadlock, because the InnoDB manual says you must handle retries "even if your application logic is correct". [@mysql-deadlocks]

### Advisory locks, and beyond one database

Sometimes you want to lock an idea, not a row. For example: "only one nightly billing job runs". Postgres advisory locks take a number you choose. The database does not enforce their meaning; your code must ask for them. [@pg-lock]

```sql
BEGIN;
SELECT pg_try_advisory_xact_lock(4242);  -- true or false, no wait
-- if true: do the work
COMMIT;  -- releases the lock
```

The `xact` versions take the lock without waiting or with a wait, as you choose. [@pg-advisory] They are released at the end of the transaction. [@pg-lock] Session-level ones stay until you unlock them or the session ends, even after a rollback. [@pg-lock] Our advice: with a connection pool, prefer the transaction-level versions. For a long job, take a session-level lock on one connection that you keep for the whole job, and unlock it at the end. Or use a lock row with an expiry.

Locks across several databases or services are a different problem. They need a [distributed lock](/glossary#distributed-lock) with expiry and a [fencing token](/glossary#fencing-token). Don't reach for one if a single database can do the job.

## Trade-offs

| Option | Best when | Waits? | Watch out for |
|---|---|---|---|
| Atomic `UPDATE ... WHERE` | The rule fits in one statement: counters, balances, stock. | Briefly, only on the same row. | Read the row count. 0 means the rule said no. |
| Optimistic (version column) | Conflicts are rare, or a person edits between read and write. | Briefly, at write time, only if another write to the row is in progress. | Every caller must check the count and retry. Many retries under heavy contention. |
| `SELECT ... FOR UPDATE` | Conflicts are common and the logic needs a read first. | Yes. | Hold time. Never call a slow API or wait for a user inside it. Lock order. |
| `FOR UPDATE NOWAIT` | A user should hear "busy" now, not wait. | No. It fails fast. | You must handle the error. |
| `FOR UPDATE SKIP LOCKED` | Many workers pull from a queue table. | No. | Inconsistent view. Queues only. |
| Advisory lock | You lock a task, not a row. | Your choice. | Not enforced. Session locks can leak through a pool. |

## At work: how a senior uses this

- In code review, find every read-then-write on a shared row. Ask: **"Can this be one `UPDATE ... WHERE`?"** Often it can.
- Check that every optimistic or atomic update reads the row count. An ignored `UPDATE 0` is a silent bug.
- Keep lock hold time to milliseconds. No HTTP calls, no queue publishes, no user input between `FOR UPDATE` and `COMMIT`.
- Sort IDs before you lock several rows. Write the rule in a comment so the next person keeps it.
- Put the retry in one helper. Catch deadlocks (`40P01`) and serialization failures (`40001`), back off, and retry the whole transaction. [@pg-errcodes] Make it safe to repeat, as in [idempotency and retries](/learn/idempotency-retries-timeouts).
- Add an index for the `WHERE` of every locking query. InnoDB's deadlock advice includes this. [@mysql-deadlocks]
- Alert on old open transactions. A query on `pg_stat_activity` ordered by `xact_start` finds them.
- Remember that schema changes queue behind long transactions too. See [schema migrations](/learn/schema-migrations-zero-downtime).

## With AI

### What to ask the agent

> "List every place in this repo that reads a row and later updates it in the same request. For each, say if it can become one `UPDATE ... WHERE`, needs `FOR UPDATE`, or needs a version check."

> "Add optimistic locking to the `tickets` table: a version column, a migration, and an update that returns a conflict error when 0 rows change. Include a test with two concurrent saves."

> "Write a Postgres query that lists transactions open longer than 5 minutes, with their state and query text."

### What to check yourself

- The code checks rows affected after every guarded `UPDATE`. Agents often drop this.
- Locks are taken in a fixed order, and nothing slow runs while they are held.
- The retry repeats the whole transaction, with a limit and backoff, not only the failed statement.
- `SKIP LOCKED` is used only on queue tables, never on balances or stock.
- The test uses real parallel connections to a real database. A mock cannot show a race.
- The agent did not add a lock in app memory. It breaks as soon as two servers run.

## Check yourself

## Go deeper

Before this: [Transactions and isolation levels](/learn/transactions-and-isolation-levels). The same ideas in app code: [Locks, atomics and deadlocks](/learn/locks-atomics-and-deadlocks). Next: [Inventory and flash sales](/learn/inventory-overselling-flash-sales), where one hot row meets thousands of buyers. Related: [Money and double-entry ledgers](/learn/money-and-double-entry-ledgers) and [Secondary and composite indexes](/learn/secondary-and-composite-indexes), which shows how VACUUM affects index-only scans.
