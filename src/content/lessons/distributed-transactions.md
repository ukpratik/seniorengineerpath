---
topic: distributed-transactions
subtitle: Two-phase commit makes several databases commit as one. Learn how it works, why it can freeze when the coordinator dies, and what to use across services instead.
status: published
illustrative_story: false
glossary_optout: [span, thread, rollback, mitigation]
sources:
  - id: pg-list-wrap
    title: "pgsql-admin mailing list, “postgres 8.2.9 transaction id wraparound failure” (April 2009)"
    url: https://www.postgresql.org/message-id/49ECBBD3.5060200%40nasa.gov
    note: The first report. A test server with 315 databases refuses commands to avoid wraparound, and vacuuming one database does not help.
  - id: pg-list-prepared
    title: "pgsql-admin mailing list, “postgres 8.2.9 can't drop database in single user mode” (April 2009)"
    url: https://www.postgresql.org/message-id/flat/49EF0315.4010905%40nasa.gov
    note: The follow-up thread. Tom Lane finds old prepared transactions, ROLLBACK PREPARED clears them, and VACUUM works again.
  - id: pg-hackers
    title: "Tom Lane, “Prepared transactions vs novice DBAs, again,” pgsql-hackers (22 April 2009)"
    url: https://www.postgresql.org/message-id/7105.1240422511%40sss.pgh.pa.us
    note: Tom Lane tells the developers' list about the pgsql-admin case and proposes turning prepared transactions off by default.
  - id: pg-commit
    title: "Tom Lane, “pgsql: Change the default value of max_prepared_transactions to zero,” pgsql-committers (23 April 2009)"
    url: https://www.postgresql.org/message-id/20090423002346.5D61F7540E2@cvs.postgresql.org
    note: The commit that turned prepared transactions off by default, and why.
  - id: pg-prepare
    title: "PostgreSQL documentation: PREPARE TRANSACTION"
    url: https://www.postgresql.org/docs/current/sql-prepare-transaction.html
    note: What a prepared transaction keeps, and the warning about leaving one open.
  - id: gray-lamport
    title: "Jim Gray and Leslie Lamport, “Consensus on Transaction Commit”, Microsoft Research MSR-TR-2003-96 (2004, revised 2005)"
    url: https://www.microsoft.com/en-us/research/wp-content/uploads/2016/02/tr-2003-96.pdf
    note: Section 3 explains two-phase commit, its cost and why it blocks. Section 4 runs the decision through Paxos instead.
  - id: mysql-xa
    title: "MySQL 8.4 Reference Manual: XA Transactions"
    url: https://dev.mysql.com/doc/refman/8.4/en/xa.html
    note: The XA roles (transaction manager and resource managers) and the two phases, in plain words.
  - id: spanner
    title: "James C. Corbett et al. (Google), “Spanner: Google's Globally-Distributed Database”, OSDI 2012"
    url: https://static.googleusercontent.com/media/research.google.com/en//archive/spanner-osdi2012.pdf
    note: Section 2.1 and 4.2.1 show two-phase commit across Paxos groups. Table 4 shows how latency grows with participants.
  - id: crdb-pc
    title: "Nathan VanBenschoten, “Parallel Commits: An atomic commit protocol for globally distributed transactions”, Cockroach Labs blog (November 2019)"
    url: https://www.cockroachlabs.com/blog/parallel-commits/
    note: Why 2PC blocks, how CockroachDB runs it on consensus, and how it cut commit time from two consensus rounds to one.
  - id: crdb-txn
    title: "CockroachDB documentation: Transaction Layer"
    url: https://docs.cockroachlabs.com/docs/stable/architecture/transaction-layer
    note: Write intents, transaction records, heartbeats, and what happens when a coordinator dies.
claims_checked:
  - claim: "A PostgreSQL 8.2.9 test server with 315 databases refused commands to avoid wraparound. Three forgotten prepared transactions, the oldest from July 2008, blocked VACUUM, and ROLLBACK PREPARED fixed it."
    source: pg-list-prepared
    note: "Messages of 22 April 2009 (pg_prepared_xacts output; 'these uncommitted transactions blocked the vacuuming'); first report in pg-list-wrap."
  - claim: "Tom Lane's commit changed the default max_prepared_transactions to zero and added documentation warnings, citing a scenario 'seen several times now'."
    source: pg-commit
    note: "Log message; shipped in PostgreSQL 8.4 (release notes: previously 5)."
  - claim: "If the coordinator fails right after every participant has voted yes, the others cannot know the outcome and 2PC blocks until it is repaired. Paxos Commit uses 2F+1 coordinators, and classic 2PC is the F = 0 case."
    source: gray-lamport
    note: "Abstract and Section 3.3."
  - claim: "Spanner runs 2PC across Paxos groups with prepare and commit records logged through Paxos; mean commit latency was 17.0 ms with 1 participant, 42.7 ms with 50 and 150.5 ms with 200."
    source: spanner
    note: "Sections 2.1 and 4.2.1; Table 4."
  - claim: "CockroachDB parallel commits writes a STAGING record in parallel with the writes; the transaction is implicitly committed once all listed writes reach consensus, committing cross-range transactions in half the time."
    source: crdb-pc
    note: "Sections on the STAGING status, implicit commit and benchmarks."
reviewed_by: ai-factcheck
reviewed_on: 2026-11-13
quiz:
  - q: In two-phase commit, a database has voted YES. Then the coordinator stops answering. What may the database do?
    options:
      - Abort after a short timeout, because no decision arrived.
      - Commit, because it voted YES.
      - Neither. It must wait for the decision and keep its locks.
    answer: 2
    why: The coordinator may have decided either way before it died. A YES voter that guessed could disagree with the others.
  - q: Your Postgres server has `max_prepared_transactions = 10`, and nobody knows why. What is the safest first step?
    options:
      - Check pg_prepared_xacts for old prepared transactions, then plan to set the value to 0.
      - Raise it to 100 so more transactions fit.
      - Restart Postgres, which rolls back all prepared transactions.
    answer: 0
    why: Prepared transactions survive restarts. The Postgres docs say to keep the feature off unless a transaction manager closes them out.
  - q: How do Spanner and CockroachDB stop 2PC from blocking when a coordinator machine dies?
    options:
      - They skip the prepare phase.
      - They keep the transaction state in a Paxos or Raft group, so another replica can finish the commit.
      - They use a timeout and abort every slow transaction.
    answer: 1
    why: The decision is replicated like the data. Losing one machine does not lose the decision.
  - q: Orders, Stock and Payments each own a database, and one checkout touches all three. Which design avoids holding locks across the three services while a checkout runs?
    options:
      - XA two-phase commit across the three databases.
      - A saga with an outbox and idempotent steps.
      - One shared table that all three services write.
    answer: 1
    why: Each step commits locally, and compensations undo earlier steps. No service holds locks while it waits for another.
incidents: [postgres-2009-04-forgotten-prepared-transactions]
related: [transactions-isolation, raft, sagas-outbox-cdc, clocks-ordering, leader-election, exactly-once]
---

## What broke

In April 2009, an administrator posted to the PostgreSQL admin mailing list. She had inherited a test server running PostgreSQL 8.2.9. It had 315 databases. [@pg-list-wrap]

The server refused to work. Every connection got "database is not accepting commands to avoid wraparound data loss". She followed the hint and ran VACUUM in single-user mode. The server still would not start normally. [@pg-list-wrap]

She started a new thread when she could not drop a database. Tom Lane, a core Postgres developer, asked one question: "Maybe you've got some prepared transactions laying about?" She checked. There were three. The oldest had been waiting since July 2008. She did not know what had created them. [@pg-list-prepared]

She ran ROLLBACK PREPARED on each one. VACUUM worked again, and she was "back in business". Most of their code was Java on JBoss. Another list member suggested a likely cause: XA two-phase commit code that assumed a rollback on disconnect. Nobody confirmed it. [@pg-list-prepared]

The same day, Tom Lane told the developers' list that this was "another instance" of the problem. He proposed turning the feature off by default. [@pg-hackers] That evening, he committed the change. The commit message said the project had seen this "several times now". [@pg-commit]

## The idea in simple words

A transaction in one database is all or nothing. You saw this in [transactions and isolation levels](/learn/transactions-and-isolation-levels). Now one action changes two databases. You want both changes, or neither. This is the atomic commit problem.

You cannot just commit one, then the other. If the second fails, the first is already committed.

Two-phase commit (2PC) solves this with a coordinator. In phase one, it asks every database, "Can you commit?" In phase two, it tells all of them the same answer. [@gray-lamport]

The catch is in the middle. A database that says "yes" gives up its right to decide. It must wait for the coordinator, however long that takes.

> [!key] **The key idea.** 2PC makes many machines agree, but one machine holds the decision. If that machine dies at the wrong moment, the others wait with their locks held. Modern databases avoid most of this by replicating the decision with consensus.

## How it works

### The two phases

XA, a standard that many databases support, calls the coordinator a transaction manager. Each database is a resource manager. [@mysql-xa]

1. **Prepare.** The coordinator asks each database to prepare. Each one writes its changes to stable storage and votes yes or no. [@mysql-xa]
2. **Commit or abort.** If every vote is yes, the coordinator tells all of them to commit. If any vote is no, it tells all of them to roll back. [@mysql-xa]

Every machine writes its state to disk before it sends a message. After a crash, it reads that state and carries on. [@gray-lamport] The coordinator's log entry is the real moment of commit. Once it says COMMIT, the transaction is committed, even if no database has heard yet. [@gray-lamport]

### Why it blocks

Step through one order that changes two databases.

[[diagram:two-phase-commit-blocking]]

If a database does not answer phase one, the coordinator simply aborts. [@gray-lamport] The bad case comes later. If the coordinator fails right after every database has voted yes, the databases cannot know whether it committed or aborted. The protocol blocks until the coordinator is repaired. [@gray-lamport]

A transaction in this state is an in-doubt transaction. It keeps every row lock it holds. Other transactions that need those rows wait too.

The cost is also visible when nothing fails. With the coordinator on the same node as one database, a commit takes three message delays. Add disk writes for each prepare and for the decision. [@gray-lamport]

### Postgres: PREPARE TRANSACTION

Postgres gives you the database side of 2PC:

```sql
-- needs max_prepared_transactions > 0
BEGIN;
UPDATE stock SET qty = qty - 1 WHERE sku = 'tee-m';
PREPARE TRANSACTION 'order-42';
-- later, from any session:
COMMIT PREPARED 'order-42';  -- or ROLLBACK PREPARED
```

After PREPARE, the transaction no longer belongs to your session. Its state is on disk, and it can almost always still commit after a crash. Any session can finish it. [@pg-prepare]

The docs say this command is not meant for applications. It is for an external transaction manager. [@pg-prepare] They also warn that leaving a transaction prepared for a long time is unwise. It keeps its locks, it stops VACUUM from reclaiming space, and in extreme cases it can force a shutdown to prevent transaction ID wraparound. [@pg-prepare] That is what happened to the test server above.

The fix since 2009: `max_prepared_transactions` defaults to 0, which turns the feature off. [@pg-commit] Keep it at 0 unless a transaction manager tracks every prepared transaction. [@pg-prepare] You can list open ones in the `pg_prepared_xacts` view. [@pg-prepare]

### Making 2PC fault-tolerant

The weak point is one coordinator on one machine. So replicate it, the way [Raft](/learn/consensus-and-raft) replicates a log.

Gray and Lamport's Paxos Commit goes a step further. It runs Paxos on each database's vote, uses 2F+1 coordinators, and makes progress while F+1 of them work. Classic 2PC is the case F = 0. [@gray-lamport]

**Spanner.** Google's Spanner splits data into Paxos groups. A transaction inside one group does not need 2PC. [@spanner] If it spans several groups, their leaders run 2PC, and one group acts as coordinator. Each prepare record and the commit record go through Paxos. So the coordinator's state is replicated. [@spanner] The paper says "Running two-phase commit over Paxos mitigates the availability problems." [@spanner]

**CockroachDB.** CockroachDB also runs 2PC on top of consensus. A transaction's record is replicated the same way as its writes. [@crdb-pc] Each write is a write intent: "a replicated lock and a replicated provisional value". [@crdb-txn] The coordinator heartbeats the transaction record. If the heartbeats stop, the record is moved to ABORTED, so others stop waiting. [@crdb-txn] A STAGING transaction is the exception, as below.

Its older protocol needed two consensus rounds in a row: the writes, then the commit record. Parallel commits writes a STAGING record and the writes at the same time. Once every listed write has reached consensus, the transaction counts as committed. Cockroach Labs says this commits cross-range transactions in half the time. [@crdb-pc] If the coordinator dies, another transaction checks the listed writes. If all are there, the transaction is committed. If any is missing, it first stops that write from ever landing, then marks the transaction aborted. [@crdb-pc]

### What it still costs

Consensus removes the single point of failure. It does not remove the waiting. In Spanner's tests, a commit with 1 participant took 17 ms on average. With 50 it took 42.7 ms, and with 200 it took 150.5 ms. The paper calls up to 50 participants reasonable. [@spanner] Participants release their locks only after the commit is applied. [@spanner] So locks are held longer as participants grow.

## Trade-offs

| Option | Atomic across all parts? | Blocks if the coordinator dies? | Use it when |
|---|---|---|---|
| One database transaction | Yes. | No coordinator. | The data fits in one database. Prefer this. |
| Classic 2PC or XA | Yes. | Yes. Locks stay until it returns. [@gray-lamport] | Rarely. A tested transaction manager owns recovery. |
| 2PC over consensus (Spanner, CockroachDB) | Yes. | Not when one machine dies. It waits only if most replicas of a group are down. [@spanner] [@gray-lamport] | You already run such a database. It handles 2PC for you. |
| Saga, outbox and idempotent steps | No. Each step commits alone and compensations undo. | No global locks. | Several services, each with its own database. |

**Simple rule:** inside one distributed database, let the database run 2PC. Across services, use a saga.

## At work: how a senior uses this

- When a design says "update both in one transaction", ask: **"Is this one database, or two?"** If it is two, the answer is 2PC or a saga, never two plain commits.
- Across services, use a [saga with an outbox](/learn/sagas-outbox-and-cdc). Make every step idempotent, as in [idempotency, retries and timeouts](/learn/idempotency-retries-timeouts).
- Inside one Spanner or CockroachDB cluster, trust its transactions. Keep them short and touch few ranges.
- Check `max_prepared_transactions` on every Postgres server. If nothing uses it, set it to 0.
- If you do use XA, alert on prepared transactions older than a few minutes. In Postgres, query `pg_prepared_xacts`.
- Write a runbook for in-doubt transactions. Say who decides commit or rollback, and how to check the coordinator's log first.

## With AI

### What to ask the agent

> "Here is our checkout code. List every place where one request writes to two databases or to a database and a broker. For each, say whether a crash between the writes leaves them out of step."

> "Write a monitoring query for Postgres that alerts when any row in pg_prepared_xacts is older than 5 minutes. Include the gid, owner and database."

> "Explain what our XA transaction manager does on restart with in-doubt transactions. Point me to its recovery log settings."

### What to check yourself

- The agent did not "fix" a cross-service write by adding PREPARE TRANSACTION in app code. The Postgres docs say not to. [@pg-prepare]
- Any ROLLBACK PREPARED in a runbook first checks the coordinator's decision. Rolling back a transaction that the coordinator committed breaks atomicity.
- Timeouts do not abort a transaction that already voted yes. That is the coordinator's job.
- Claims about Spanner or CockroachDB match their own docs, not a blog summary.

## Check yourself

## Go deeper

Before this: [transactions and isolation levels](/learn/transactions-and-isolation-levels) and [consensus and Raft](/learn/consensus-and-raft). Related: [time, clocks and ordering](/learn/time-clocks-and-ordering), for Spanner's commit wait, and [leader election and leases](/learn/leader-election-and-leases). Next: [sagas, outbox and CDC](/learn/sagas-outbox-and-cdc), and exactly-once delivery.
