---
topic: transactions-isolation
subtitle: Your code is correct when it runs alone. Isolation levels decide what happens when it runs twice at the same moment.
status: published
reviewed_by: ai-factcheck
reviewed_on: 2026-10-21
illustrative_story: true
sources:
  - id: ddia
    title: "Martin Kleppmann, Designing Data-Intensive Applications (O'Reilly)"
    where: "1st edition (2017): chapter 7, Transactions (ACID, weak isolation levels, lost updates, write skew, serializability)."
  - id: pg-iso
    title: "PostgreSQL documentation: Transaction Isolation"
    url: https://www.postgresql.org/docs/current/transaction-iso.html
    note: The default level, what each level allows in Postgres, and the errors you must retry.
  - id: pg-lock
    title: "PostgreSQL documentation: Explicit Locking (Row-Level Locks)"
    url: https://www.postgresql.org/docs/current/explicit-locking.html
    note: What SELECT … FOR UPDATE locks, and who has to wait.
  - id: mysql-iso
    title: "MySQL 8.4 Reference Manual: Transaction Isolation Levels (InnoDB)"
    url: https://dev.mysql.com/doc/refman/8.4/en/innodb-transaction-isolation-levels.html
    note: The InnoDB default level, how its snapshots work, and how locking reads differ.
  - id: hermitage
    title: "Martin Kleppmann, Hermitage: testing transaction isolation levels (GitHub)"
    url: https://github.com/ept/hermitage
    note: A table of which anomalies each database's levels really prevent, tested by hand.
claims_checked:
  - claim: Read Committed is the default isolation level in PostgreSQL.
    source: pg-iso
    note: Quoted directly in the docs.
  - claim: In Postgres Repeatable Read, a write to a row that a concurrent transaction changed and committed fails with "could not serialize access due to concurrent update". The app must retry the whole transaction.
    source: pg-iso
    note: Docs quote the error and the retry advice. Serialization failures use SQLSTATE 40001.
  - claim: InnoDB defaults to REPEATABLE READ, and plain reads use the snapshot from the first read.
    source: mysql-iso
    note: Both quoted in the MySQL 8.4 manual.
  - claim: Postgres Read Committed and MySQL Repeatable Read do not prevent lost updates. Repeatable Read in both does not prevent write skew. Serializable in both prevents both.
    source: hermitage
    note: Hermitage table, columns P4 (lost update) and G2-item (write skew). MySQL test uses plain SELECTs; tests run by hand.
  - claim: SELECT … FOR UPDATE makes other writers and lockers of the row wait until the transaction ends. In Read Committed, the waiter then gets the updated row.
    source: pg-lock
    note: Explicit Locking 13.3.2, confirmed by the Read Committed section of pg-iso. Plain SELECTs are not blocked.
  - claim: ACID, lost update, write skew (on-call doctors) and FOR UPDATE as a write-skew fix are described in DDIA chapter 7.
    source: ddia
    note: Book not opened; matches widely cited summaries of chapter 7. Wording kept general.
  - claim: "Under MySQL SERIALIZABLE, plain SELECTs become SELECT ... FOR SHARE only when autocommit is disabled."
    source: mysql-iso
    note: "QC1-23: InnoDB isolation levels page: 'converts all plain SELECT statements to SELECT ... FOR SHARE if autocommit is disabled'. Citation and the autocommit condition added."
quiz:
  - q: What is the default isolation level in PostgreSQL?
    options: [Read uncommitted, Read committed, Repeatable read, Serializable]
    answer: 1
    why: The Postgres docs say Read Committed is the default.
  - q: Two requests run `SELECT balance`, add ₹100 in app code, then `UPDATE ... SET balance = <new value>`. What can go wrong under Read Committed?
    options:
      - Nothing. Read Committed prevents this.
      - A lost update. One of the two deposits disappears.
      - A dirty read.
    answer: 1
    why: Both read the same old value, and the second write overwrites the first. Read Committed does not stop this.
  - q: Two doctors are on call. Each checks "at least 2 on call", then removes only themselves. Both commit, and nobody is on call. What is this?
    options: [A dirty read, A lost update, Write skew]
    answer: 2
    why: They read the same data but wrote different rows. Only Serializable stops it in general.
  - q: Your service runs at Serializable in Postgres. What must the code do?
    options:
      - Nothing extra. Serializable never fails.
      - Catch serialization failures and retry the whole transaction.
      - Add SELECT … FOR UPDATE to every read.
    answer: 1
    why: Postgres aborts one transaction when it finds a conflict. The app must retry it from the start.
incidents: []
related: [concurrency-control, postgres, replication, money-ledgers]
---

## What broke

A small shop sells concert T-shirts. The stock for size M is 10. A popular singer posts a link, and orders arrive many per second.

The order code looks fine. It reads the stock, checks that it is above zero, works out stock minus one, and saves the new number. Each order runs inside a database transaction. Every test passes.

After the sale, the warehouse counts the shirts. The database says 4 left, but the shelf is empty. The shop has sold 6 shirts it does not have. Now someone must email 6 customers and refund them.

The code was correct for one order at a time. It was not correct for two orders at the same moment. Wrapping it in a transaction did not help, because of the isolation level.

## The idea in simple words

A transaction groups several reads and writes into one unit. The database makes four promises about it, called ACID. [@ddia]

- **Atomicity:** all or nothing. If anything fails halfway, the database undoes the whole group.
- **Consistency:** your rules, like "stock is never negative", still hold at the end. This one is mostly your job, not the database's.
- **Isolation:** transactions running at the same time should not get in each other's way.
- **Durability:** once the database says "committed", the data survives a crash.

The surprise is in the "I". Full isolation is slow, so databases offer weaker isolation levels. Most apps run at a weak level without knowing it.

> [!key] **The key idea.** A transaction is not a lock. At the default level in Postgres and MySQL, two transactions can both read a value, both change it, and one change can vanish. [@hermitage]

## How it works

Step through the T-shirt bug. Two orders, A and B, buy one shirt each.

[[diagram:lost-update]]

This is a lost update: two transactions read, change and write the same value, and the second write wipes out the first. [@ddia]

### The levels, from weak to strong

**Read committed.** You never see data that another transaction has not committed yet. That rules out the dirty read. But each statement sees the newest committed data. So two reads in one transaction can return different answers. This is the Postgres default. [@pg-iso]

**Snapshot isolation** (called Repeatable Read in Postgres). The transaction reads from one snapshot of the database, taken at its first statement. It does not see changes that others commit later. [@pg-iso]

How does the database keep old and new versions at once? With MVCC. It keeps several versions of a row. Readers see the version that fits their snapshot, and writers add a new version. Readers and writers do not block each other. [@ddia]

**Serializable.** The result must be the same as if the transactions had run one after another. Postgres builds this on top of snapshots. It watches for dangerous patterns and aborts one transaction when it finds one. [@pg-iso]

### What the defaults really do

Names differ between databases, so check what your database does, not what the level is called.

- **Postgres** defaults to Read Committed. A lost update can happen here. [@pg-iso] [@hermitage]
- In Postgres **Repeatable Read**, order B's write fails. It gets "could not serialize access due to concurrent update", and the app must retry the whole transaction. [@pg-iso]
- **MySQL InnoDB** defaults to REPEATABLE READ. Plain reads use a snapshot from the first read. [@mysql-iso] But Hermitage's tests found that this level does *not* prevent lost updates when you read with a plain SELECT. [@hermitage]
- At Serializable, both databases prevent lost updates and write skew. [@hermitage]

### Three ways to fix a lost update

**1. Atomic update.** Let the database do the math in one statement:

```sql
UPDATE products SET stock = stock - 1
WHERE id = 42 AND stock > 0;
```

If no row changed, the shirt is sold out. In Read Committed, Postgres makes the second update wait. Then it checks the WHERE clause again on the new row. [@pg-iso] This is the simplest fix, and you should use it whenever the change fits in one statement.

**2. Lock the row.** Read with `SELECT … FOR UPDATE`. Other transactions that try to lock or change the row wait until you commit or roll back. [@pg-lock] In Read Committed, the waiting transaction then gets the new value, 9, not 10. [@pg-iso] Use this when the logic is too complex for one statement.

**3. Stronger isolation.** Use Repeatable Read or Serializable in Postgres, and retry when the database aborts a transaction. Every caller must handle the retry.

### Write skew: the harder bug

Some bugs are not about one row. A hospital rule says at least one doctor must be on call. Two doctors are on call, and both feel ill at the same time. [@ddia]

Each doctor's transaction checks: "Are at least 2 on call? Yes." Then each removes only *their own* row. No row is written twice, so no lock and no snapshot check fires. Both commit. Now nobody is on call.

This is write skew. Repeatable Read does not stop it, in Postgres or in MySQL. Serializable does. [@hermitage] You can also lock the rows you read with `FOR UPDATE`, so the second doctor waits and then sees only one on call. [@ddia]

## Trade-offs

| Level | Stops lost update? | Stops write skew? | Cost |
|---|---|---|---|
| Read committed (Postgres default) | No. Use atomic updates or FOR UPDATE. | No. | Cheapest. Few aborts. |
| Repeatable read, Postgres (snapshot) | Yes. One transaction fails and must retry. | No. | The app must retry on errors. |
| Repeatable read, MySQL (default) | No, Hermitage says. | No. | Cheap. But it does not catch the conflict for you. |
| Serializable | Yes. | Yes. | More aborts and retries under contention. In MySQL, plain reads take shared locks when autocommit is off. [@mysql-iso] |

**Simple rule:** keep the default level. Make every read-change-write either one atomic statement or a `FOR UPDATE` read. Use Serializable for rules that span several rows, like the on-call check.

## At work: how a senior uses this

- In code review, look for this shape: read a value, change it in app code, write it back. Ask: **"What if two requests do this at once?"**
- Know your database's default level, and write it in the service README. Don't assume "repeatable read" means the same thing in MySQL and Postgres.
- Prefer `SET x = x + 1` over reading and writing the value in app code.
- Turn business rules into constraints when you can: `UNIQUE`, `CHECK (stock >= 0)`. The database then enforces them for every caller.
- If you use Repeatable Read or Serializable, wrap transactions in a retry helper. Postgres marks these failures with the error code (SQLSTATE) `40001`. [@pg-iso]
- Keep transactions short. Never wait for a user or a slow network call while holding a row lock.
- Write a concurrency test. Run the same request 50 times in parallel and check the final count.

## With AI

### What to ask the agent

> "Find every place in this service that reads a row, changes a value in code, and writes it back. For each, tell me if two requests at once can lose an update."

> "Rewrite this stock decrement as one atomic UPDATE with a `stock > 0` check. Return whether a row was updated."

> "Write a test that runs 20 parallel orders against a real Postgres with stock 10. Assert that exactly 10 succeed and stock ends at 0."

### What to check yourself

- The test uses a real database with real parallel connections. Mocks and an in-memory map cannot show a lost update.
- The fix runs inside the same transaction as the rest of the order. A lock taken in one transaction and used in another protects nothing.
- Any retry repeats the **whole** transaction, not just the last statement.
- The agent did not "fix" it with a lock in app memory. That breaks as soon as you run two servers.
- Locks are taken in the same order everywhere, so two transactions don't wait for each other forever.

## Check yourself

## Go deeper

Before this: [Replication](/learn/database-replication), which adds stale reads on top of these bugs. Next on this line: [Concurrency control in practice](/learn/locking-and-mvcc-in-practice).
