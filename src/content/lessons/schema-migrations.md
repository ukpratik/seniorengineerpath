---
topic: schema-migrations
subtitle: Change a live database schema in small steps that old and new code can both survive, with a way back at every step.
status: published
illustrative_story: false
sources:
  - id: gocardless
    title: "Chris Sinjakli, “Zero-downtime Postgres migrations: the hard parts”, GoCardless blog"
    url: https://gocardless.com/blog/zero-downtime-postgres-migrations-the-hard-parts/
    note: A real 15-second API outage caused by a fast ALTER TABLE that waited for a lock, and the fixes.
  - id: pg-alter
    title: "PostgreSQL documentation, ALTER TABLE"
    url: https://www.postgresql.org/docs/current/sql-altertable.html
    note: The lock each form takes, which changes rewrite the table, and NOT VALID constraints.
  - id: pg-locks
    title: "PostgreSQL documentation, Explicit Locking (section 13.3)"
    url: https://www.postgresql.org/docs/current/explicit-locking.html
    note: The table lock modes, and why ACCESS EXCLUSIVE blocks even plain reads.
  - id: pg-index
    title: "PostgreSQL documentation, CREATE INDEX"
    url: https://www.postgresql.org/docs/current/sql-createindex.html
    note: How CREATE INDEX CONCURRENTLY avoids blocking writes, what it costs, and how it can fail.
  - id: pg-timeout
    title: "PostgreSQL documentation, Client Connection Defaults: lock_timeout"
    url: https://www.postgresql.org/docs/current/runtime-config-client.html
    note: The setting that makes a statement give up if it waits too long for a lock.
  - id: mysql-ddl
    title: "MySQL 8.4 Reference Manual, Online DDL Operations"
    url: https://dev.mysql.com/doc/refman/8.4/en/innodb-online-ddl-operations.html
    note: Which ALTER TABLE operations in InnoDB are instant, in place or a full copy.
  - id: gh-ost
    title: "GitHub, gh-ost: triggerless online schema migration for MySQL"
    url: https://github.com/github/gh-ost
    note: A tool that changes a big MySQL table by copying it to a ghost table while traffic runs.
  - id: fowler
    title: "Danilo Sato, “ParallelChange”, martinfowler.com, 2014"
    url: https://martinfowler.com/bliki/ParallelChange.html
    note: The expand, migrate and contract pattern, and why most database refactorings use it.
  - id: stripe
    title: "Jacqueline Xu, “Online migrations at scale”, Stripe blog, 2017"
    url: https://stripe.com/blog/online-migrations
    note: How Stripe moved hundreds of millions of objects with dual writes, backfills and checks.
  - id: k8s
    title: "Kubernetes documentation, Deployments"
    url: https://kubernetes.io/docs/concepts/workloads/controllers/deployment/
    note: How a rolling update replaces old pods with new ones step by step.
  - id: gitlab
    title: "GitLab, “Postmortem of database outage of January 31” (2017)"
    url: https://about.gitlab.com/blog/2017/02/10/postmortem-of-database-outage-of-january-31/
    note: How every backup method failed silently, and what it cost.
claims_checked:
  - claim: GoCardless had about 15 seconds of API downtime because adding a foreign key waited for an AccessExclusive lock behind a long read query, and later queries queued behind it.
    source: gocardless
    note: Matches the post. It gives no year. The tables were empty and unused. The post advises lock_timeout and quotes "It's better to abort a deploy than take your application down."
  - claim: ALTER TABLE takes ACCESS EXCLUSIVE unless noted; VALIDATE CONSTRAINT takes SHARE UPDATE EXCLUSIVE; ADD FOREIGN KEY takes SHARE ROW EXCLUSIVE on both tables.
    source: pg-alter
    note: Checked against the PostgreSQL 18 docs. The lesson notes that GoCardless saw a stronger lock on their older version.
  - claim: Only ACCESS EXCLUSIVE blocks a plain SELECT, and a lock request waits indefinitely unless a deadlock is found.
    source: pg-locks
    note: Both stated in section 13.3. lock_timeout defaults to 0, which disables it (pg-timeout).
  - claim: A non-volatile default does not rewrite the table; a volatile default such as clock_timestamp() rewrites the table and its indexes.
    source: pg-alter
    note: Wording changed from "constant" to "non-volatile" to match the docs.
  - claim: A normal CREATE INDEX blocks writes; CONCURRENTLY does two scans, cannot run in a transaction block, and can leave an INVALID index that still costs on updates.
    source: pg-index
    note: The docs advise dropping it and retrying, or REINDEX INDEX CONCURRENTLY.
  - claim: In MySQL 8.4, ADD COLUMN defaults to INSTANT, a same-type rename can be instant, a type change needs ALGORITHM=COPY with no concurrent DML, and a secondary index is built in place with reads and writes allowed.
    source: mysql-ddl
    note: Matches the online DDL tables, with the listed limits.
  - claim: GitLab's pg_dump backups failed from a 9.2 vs 9.6 version mismatch, failure emails were rejected, about six hours of data was lost, and the site was down about 18 hours.
    source: gitlab
    note: Matches the postmortem.
reviewed_by: ai-factcheck
reviewed_on: 2026-10-21
quiz:
  - q: A migration runs `ALTER TABLE orders ADD CONSTRAINT ...`. It takes 5 ms, yet the API is down for 15 seconds. What is the most likely cause?
    options:
      - The disk was full.
      - The ALTER waited for a lock behind a long query, and every new query on the table queued behind the ALTER.
      - Postgres restarts after every ALTER TABLE.
    answer: 1
    why: Most ALTER TABLE forms need an ACCESS EXCLUSIVE lock, which blocks even plain reads. GoCardless had about 15 seconds of API downtime this way. Set lock_timeout.
  - q: You need to rename `users.name` to `full_name` in a service that runs many pods. What is the safe plan?
    options:
      - One migration with RENAME COLUMN, deployed with the new code.
      - Add full_name, write both, backfill, switch reads, stop writing name, then drop it later.
      - Take the site down for maintenance.
    answer: 1
    why: During a rolling deploy, old and new code run at once. Expand and contract keeps both versions working at every step.
  - q: Why use `CREATE INDEX CONCURRENTLY` in Postgres on a busy table?
    options:
      - It is faster than a normal index build.
      - A normal build blocks inserts, updates and deletes until it finishes. The concurrent build does not.
      - It can run inside a transaction with other changes.
    answer: 1
    why: The concurrent build does not block writes, but it is slower, cannot run in a transaction block and can leave an invalid index if it fails.
  - q: How do you add NOT NULL to a large Postgres column without a long lock?
    options:
      - Add a CHECK (col IS NOT NULL) constraint as NOT VALID, validate it, then SET NOT NULL.
      - Run SET NOT NULL directly. It is always instant.
      - Copy the table by hand.
    answer: 0
    why: VALIDATE CONSTRAINT takes a weaker SHARE UPDATE EXCLUSIVE lock, and a valid CHECK lets SET NOT NULL skip its full table scan.
  - q: What lesson did GitLab's 2017 database outage teach about backups?
    options:
      - Backups are only needed for very large databases.
      - Backup jobs can fail silently, so alert on failures and test restores often.
      - Snapshots replace the need for backups.
    answer: 1
    why: GitLab's pg_dump backups had been failing, and the failure emails were rejected. About six hours of data was lost.
incidents: [gitlab-2017-01-database]
cases: [stripe-online-migrations]
related: [transactions-isolation, api-versioning, safe-deploys, migrations, feature-flags, disaster-recovery, sagas-outbox-cdc]
---

## What broke

GoCardless is a payments company. Its team wrote about a schema migration that went wrong. It renamed some invoicing tables and dropped and recreated their foreign keys. The tables were empty, and no production code used them yet. [@gocardless]

Just after the migration started, alerts fired: API requests were timing out. This lasted about 15 seconds. Then the migration finished and the API came back. [@gocardless]

The strange part: the same migration, run on a copy of the database, took a few hundred milliseconds. The `ALTER TABLE` itself was fast. [@gocardless]

The cause was a lock. On their Postgres version, adding the foreign key needed an `AccessExclusive` lock on both tables. That included the busy `parent` table. A long-running read query was already using it. So the `ALTER` waited. And every normal query that arrived after it waited behind the `ALTER`. [@gocardless] Current Postgres docs list a weaker lock for this, `SHARE ROW EXCLUSIVE`. It still blocks writes. [@pg-alter] [@pg-locks]

For 15 seconds, the API's own reads and writes piled up, and clients timed out. The fix they suggest is short: set `lock_timeout` in migration scripts. "It's better to abort a deploy than take your application down." [@gocardless]

## The idea in simple words

A schema change on a live database can hurt you in three ways.

1. **Locks.** Many changes need a strong lock on the table. While they wait for it or hold it, other queries stop.
2. **Table rewrites.** Some changes copy the whole table. On a big table that can take a long time and double the disk space. [@pg-alter]
3. **Two versions of code.** During a rolling deploy, new pods start while old ones still run. [@k8s] Both versions must work with the schema at every moment.

The fix is to never make a big change in one step. Split it into small steps that each keep old and new code working. This is called **expand and contract**, or parallel change: expand to support old and new, migrate the users, then contract to remove the old. Sato writes that most database refactorings follow this pattern. [@fowler]

> [!key] **The key idea.** Never change the schema and the code in a way that needs both to switch at the same moment. Add first, move slowly, remove last.

## How it works

Step through a column rename done safely across several deploys. Use Next and Back, or the ← and → keys.

[[diagram:rename-column-steps]]

### Know your locks

In Postgres, `ALTER TABLE` takes an `ACCESS EXCLUSIVE` lock unless the docs say otherwise. [@pg-alter] That lock conflicts with every other lock mode. It is the only one that blocks a plain `SELECT`. [@pg-locks] By default, a statement waits for a lock with no time limit. [@pg-locks]

So set `lock_timeout` in your migration session. It aborts a statement that waits too long for a lock. Its default is 0, which means no timeout. [@pg-timeout] If the migration times out, retry it later. A failed deploy is better than a down API. [@gocardless]

Also find long-running queries and transactions before you migrate. GoCardless advises removing them, and splitting schema changes into smaller pieces. [@gocardless]

### Adding a column

In Postgres, adding a column with no default, or with a non-volatile default such as a constant, does not rewrite the table. A volatile default, such as `clock_timestamp()`, rewrites the whole table and its indexes. [@pg-alter] Even the fast form takes an `ACCESS EXCLUSIVE` lock for a moment, so set `lock_timeout`. [@pg-alter]

In MySQL 8.4, `ADD COLUMN` uses the `INSTANT` algorithm by default, with some limits. [@mysql-ddl]

Our advice: add new columns as nullable or with a constant default. Old code that names its columns ignores them, so this step is safe on its own.

### Renaming and changing types

Renaming a column in Postgres does not touch stored data. [@pg-alter] MySQL 8.4 can often rename a column instantly, if the type stays the same. [@mysql-ddl] But the fast rename is still a trap. Old pods ask for `name` the moment it becomes `full_name`, and they fail.

So rename with the five steps in the diagram. The same goes for changing a type. In Postgres, most type changes rewrite the table and its indexes. [@pg-alter] In MySQL, a type change uses a full table copy with no writes allowed during it. [@mysql-ddl] Add a new column of the new type instead, then move the data.

### Adding NOT NULL and constraints

`SET NOT NULL` normally scans the whole table while it holds its lock. But if a valid `CHECK` constraint already proves there are no nulls, Postgres skips the scan. [@pg-alter]

So do it in three steps:

1. Add `CHECK (full_name IS NOT NULL) NOT VALID`. This skips the check of old rows. It still needs an `ACCESS EXCLUSIVE` lock for a moment. [@pg-alter]
2. Run `VALIDATE CONSTRAINT`. It scans the table, but takes only a `SHARE UPDATE EXCLUSIVE` lock, so normal reads and writes continue. [@pg-alter]
3. Run `SET NOT NULL`, which now skips the scan. [@pg-alter] You can then drop the `CHECK`.

The current docs also allow `NOT VALID` on a not-null constraint itself. [@pg-alter] Check your version before you rely on it.

Foreign keys work the same way: add them `NOT VALID`, then validate. Adding a foreign key locks both tables, so set `lock_timeout` here too. [@pg-alter]

### Adding indexes

A normal `CREATE INDEX` in Postgres lets reads continue but blocks inserts, updates and deletes until it is done. [@pg-index] On a busy table, that is an outage.

Use `CREATE INDEX CONCURRENTLY`. It does not block writes. The cost: it scans the table twice, waits for older transactions, and takes much longer. It cannot run inside a transaction block. If it fails, it leaves an "invalid" index that still slows writes. Drop it and try again. [@pg-index]

In MySQL 8.4, adding a secondary index runs in place and allows reads and writes. [@mysql-ddl] For changes that MySQL cannot do online, tools like GitHub's **gh-ost** build a "ghost" copy of the table, copy rows over in small pieces and follow new changes from the binary log. It can pause, and you choose when it swaps the tables. [@gh-ost]

### Backfills and dual writes

A **backfill** copies or fixes data in old rows. Our advice: do not run it as one giant `UPDATE`, which can hold row locks for a long time and make replicas fall behind. Instead, update a small batch of rows by primary key, commit, pause, and repeat. Make the job safe to stop and restart. Watch replica lag and slow it down if lag grows.

While the backfill runs, the app writes both the old and new columns, so new rows stay correct. Stripe used this for hundreds of millions of objects: dual write, move reads, move writes, then remove the old data. [@stripe] It ran old and new reads side by side with GitHub's Scientist library and alerted when results did not match. [@stripe]

### Rollback plans and backups

Our advice: give each step a way back. Until the contract step, rollback is easy: deploy the previous app version. The old column is still complete. After you drop a column, the only way back is a restore.

So before the contract step, take a backup and know how long a restore takes. GitLab's 2017 outage shows why. Its `pg_dump` backups had been failing because of a version mismatch, and the failure emails were rejected. [@gitlab] After an engineer deleted data on the primary by mistake, about six hours of data was lost, and the site was down for about 18 hours. [@gitlab] A backup you have never restored is only a hope.

## Trade-offs

| Approach | Downtime? | Old and new code both work? | Effort | Use it when |
|---|---|---|---|---|
| One big migration in a maintenance window | Yes. Planned. | No. Everything switches at once. | Low. | Small internal tools where a short outage is fine. |
| Direct change with `lock_timeout` | No, if it is metadata only and gets its lock fast. [@pg-timeout] | Yes, for adds. No, for renames and drops. | Low. | Adding a nullable column, or a concurrent index. |
| Expand and contract | No. | Yes, at every step. [@fowler] | High. Several deploys over days. | Renames, type changes, splitting tables, anything a running app reads. |
| Online copy tool (gh-ost, similar) | Mostly no. You choose when the final swap happens. [@gh-ost] | Yes, if the change itself is compatible. | Medium. Needs disk space and care. | Big MySQL tables where the change would rebuild the table. |

**Simple rule:** if a change removes or renames something that running code reads, it needs expand and contract. Otherwise, a single small migration with a lock timeout is usually fine.

## At work: how a senior uses this

- For every migration, ask: **"What lock does this take, and for how long?"** Check the docs for that exact statement.
- Ask: **"Does the code running right now still work after this migration? Does the next version work before it?"**
- Keep schema changes and code changes in separate deploys. For an add, run the migration first. For a drop, deploy the code that stops using the column first.
- Set `lock_timeout` in every migration session. Also set `statement_timeout`, but raise it for slow steps like `VALIDATE CONSTRAINT`.
- Run big backfills as a job, not inside the migration. Batch, pause, and make it restartable.
- Test the migration on a copy of production data size, not an empty test database.
- Write the rollback plan in the pull request. Say when the point of no return is.
- Test a restore from backup on a schedule, and alert when a backup job fails.

## With AI

### What to ask the agent

> "Here is a Postgres migration. For each statement, tell me the lock it takes and whether it rewrites or scans the table. Quote the PostgreSQL docs."

> "Split this column rename into expand and contract steps. Give me the migrations and code changes for each deploy, and the rollback for each step."

> "Write a backfill job that copies `name` to `full_name` in batches of 1,000 rows by id, with a pause, a resume point and a progress log."

### What to check yourself

- No migration renames or drops a column that the deployed code still reads.
- Index builds on big Postgres tables use `CONCURRENTLY`, outside a transaction. Some migration tools wrap each file in a transaction, so check yours.
- `NOT NULL` and foreign keys are added `NOT VALID` first, then validated.
- No volatile default on a new column, and no type change on a big table.
- `lock_timeout` is set. AI agents rarely add it unless asked.
- The backfill is batched, restartable and does not run inside the schema migration.
- There is a tested backup before any step that deletes data.

## Check yourself

## Go deeper

- [Safe deploys](/learn/safe-deploys-config-changes): how to roll out the code side of each step.
- [Transactions and isolation levels](/learn/transactions-and-isolation-levels): what locks are and why they wait.
- Coming next on this line: [Running large migrations](/learn/running-large-migrations), [API versioning and lifecycle](/learn/api-versioning-and-lifecycle) and [Disaster recovery and backups](/learn/disaster-recovery-and-backups).
