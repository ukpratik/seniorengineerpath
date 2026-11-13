---
topic: postgres
subtitle: Learn how Postgres stores, cleans, indexes and copies your rows, so it does not stop taking writes on a Monday morning.
status: published
illustrative_story: false
glossary_optout: [at-least-once, rollback]
sources:
  - id: mandrill
    title: "Mailchimp, What We Learned from the Recent Mandrill Outage"
    url: https://mailchimp.com/what-we-learned-from-the-recent-mandrill-outage/
    note: A full timeline of a 2019 outage caused by transaction ID wraparound on one Postgres shard.
  - id: sentry
    title: "David Cramer (Sentry), Transaction ID Wraparound in Postgres"
    url: https://blog.sentry.io/transaction-id-wraparound-in-postgres/
    note: The same failure at Sentry in 2015, and the autovacuum settings they changed afterwards.
  - id: pg-vacuum
    title: "PostgreSQL documentation: 24.1. Routine Vacuuming"
    url: https://www.postgresql.org/docs/18/routine-vacuuming.html
    note: Version 18 page. Dead row versions, freezing, the wraparound limits and warnings, and how to recover.
  - id: pg-vacuum-19
    title: "PostgreSQL documentation: 24.1. Routine Vacuuming"
    url: https://www.postgresql.org/docs/19/routine-vacuuming.html
    note: Version 19 page. The wraparound warning now starts at 100 million transactions.
  - id: pg19-notes
    title: "PostgreSQL documentation: E.1. Release 19"
    url: https://www.postgresql.org/docs/19/release-19.html
    note: Release notes for version 19, including the new wraparound warning and logical replication of sequence values.
  - id: pg18-notes
    title: "PostgreSQL documentation: E.7. Release 18"
    url: https://www.postgresql.org/docs/18/release-18.html
    note: Release notes for version 18, where EXPLAIN ANALYZE started to show buffers by default.
  - id: pg-vacuum-config
    title: "PostgreSQL documentation: 19.10. Vacuuming"
    url: https://www.postgresql.org/docs/current/runtime-config-vacuum.html
    note: Autovacuum settings and their defaults.
  - id: pg-hot
    title: "PostgreSQL documentation: 66.7. Heap-Only Tuples (HOT)"
    url: https://www.postgresql.org/docs/current/storage-hot.html
    note: When an update can skip the indexes, and how fillfactor helps.
  - id: pg-stats
    title: "PostgreSQL documentation: 27.2. The Cumulative Statistics System"
    url: https://www.postgresql.org/docs/current/monitoring-stats.html
    note: The pg_stat_all_tables view, with dead rows, HOT updates and autovacuum times per table.
  - id: pg-index-types
    title: "PostgreSQL documentation: 11.2. Index Types"
    url: https://www.postgresql.org/docs/current/indexes-types.html
    note: What B-tree, Hash, GiST, SP-GiST, GIN and BRIN indexes are each good at.
  - id: pg-json
    title: "PostgreSQL documentation: 8.14. JSON Types"
    url: https://www.postgresql.org/docs/current/datatype-json.html
    note: Section 8.14.4 on GIN indexes for jsonb and the two operator classes.
  - id: pg-textsearch-idx
    title: "PostgreSQL documentation: 12.9. Preferred Index Types for Text Search"
    url: https://www.postgresql.org/docs/current/textsearch-indexes.html
    note: Why GIN is the usual index for full text search.
  - id: pg-explain
    title: "PostgreSQL documentation: 14.1. Using EXPLAIN"
    url: https://www.postgresql.org/docs/current/using-explain.html
    note: Costs, actual times, loops and the BUFFERS option.
  - id: pg-explain-ref
    title: "PostgreSQL documentation: EXPLAIN"
    url: https://www.postgresql.org/docs/current/sql-explain.html
    note: "The BUFFERS option: what shared hit, read, dirtied and written mean."
  - id: pg-alter
    title: "PostgreSQL documentation: ALTER TABLE"
    url: https://www.postgresql.org/docs/current/sql-altertable.html
    note: Changing a storage parameter such as fillfactor does not rewrite existing pages.
  - id: pg-gin
    title: "PostgreSQL documentation: 65.4. GIN Indexes"
    url: https://www.postgresql.org/docs/current/gin.html
    note: How GIN works, and why updating it tends to be slow.
  - id: pg-brin
    title: "PostgreSQL documentation: 65.5. BRIN Indexes"
    url: https://www.postgresql.org/docs/current/brin.html
    note: Block ranges, why a BRIN index is very small, and when it helps.
  - id: pg-wal-runtime
    title: "PostgreSQL documentation: 19.5. Write Ahead Log"
    url: https://www.postgresql.org/docs/current/runtime-config-wal.html
    note: checkpoint_warning and its 30-second default.
  - id: pg-wal-config
    title: "PostgreSQL documentation: 28.5. WAL Configuration"
    url: https://www.postgresql.org/docs/current/wal-configuration.html
    note: What a checkpoint does, its default timing, and full page writes.
  - id: pg-repl
    title: "PostgreSQL documentation: 26.2. Log-Shipping Standby Servers"
    url: https://www.postgresql.org/docs/current/warm-standby.html
    note: Streaming replication, version limits, and replication slots.
  - id: pg-logical
    title: "PostgreSQL documentation: Chapter 29. Logical Replication"
    url: https://www.postgresql.org/docs/current/logical-replication.html
    note: How logical replication differs from physical, and what it is used for.
  - id: pg-logical-limits
    title: "PostgreSQL documentation: 29.8. Restrictions"
    url: https://www.postgresql.org/docs/current/logical-replication-restrictions.html
    note: What logical replication does not copy, such as schema changes and sequences.
  - id: pg-connect
    title: "PostgreSQL documentation: 51.2. How Connections Are Established"
    url: https://www.postgresql.org/docs/current/connect-estab.html
    note: The process-per-user model and the postmaster.
  - id: pg-conn-config
    title: "PostgreSQL documentation: 19.3. Connections and Authentication"
    url: https://www.postgresql.org/docs/current/runtime-config-connection.html
    note: max_connections, its default, and why raising it costs shared memory.
  - id: pg-client
    title: "PostgreSQL documentation: 19.11. Client Connection Defaults"
    url: https://www.postgresql.org/docs/current/runtime-config-client.html
    note: statement_timeout, lock_timeout and idle_in_transaction_session_timeout.
  - id: pg-stat-statements
    title: "PostgreSQL documentation: F.32. pg_stat_statements — track statistics of SQL planning and execution"
    url: https://www.postgresql.org/docs/current/pgstatstatements.html
    note: How to turn on the extension and what its columns mean.
claims_checked:
  - claim: "Mandrill's shard4 stopped taking writes at 05:35 UTC on 4 February 2019 because of transaction ID wraparound. A vacuum was estimated at up to 40 days; the team truncated terabyte-scale tables at 19:12 UTC on 5 February, the vacuum finished about an hour later, and wraparound alerting was in place by 22:40 UTC."
    source: mandrill
    note: "Sections 'A Potential Problem', 'Wraparound Protection Triggered', 'Worst Case Scenario' and 'Getting Creative'."
  - claim: "Sentry's July 2015 wraparound outage ended by truncating one large table; afterwards it ran 6 autovacuum workers and set autovacuum_vacuum_cost_delay to 0."
    source: sentry
    note: "Truncation paragraph and the configuration list near the end of the post."
  - claim: "XIDs are 32 bits and compared modulo 2^32, so every table must be vacuumed at least once every two billion transactions. In version 18 the warning starts 40 million transactions before wraparound, and below 3 million Postgres refuses to assign new XIDs; only read-only transactions can start. Single-user mode is needed only to TRUNCATE or DROP tables."
    source: pg-vacuum
    note: "Section 24.1.5 and its Note (version 18 page). Version 19 moves the warning to 100 million (pg-vacuum-19, pg19-notes)."
  - claim: "autovacuum_freeze_max_age defaults to 200 million, autovacuum_max_workers to 3, and the vacuum trigger is 50 tuples plus 20% of the table."
    source: pg-vacuum-config
    note: "Parameter entries; defaults also confirmed on PostgreSQL 17.12 and 18.7 in Docker."
  - claim: "A HOT update needs no change to columns used by non-summarizing indexes (BRIN is the only core summarizing method) and free space on the same page; then no new index entries are needed."
    source: pg-hot
    note: "Section 66.7 conditions list; BRIN case confirmed on 18.7 via n_tup_hot_upd."
  - claim: "Streaming replication cannot run between different major versions; logical replication lists replicating between major versions as a typical use case."
    source: pg-repl
    note: "26.2 'different major PostgreSQL release levels is not possible'; Chapter 29 use-case list (pg-logical)."
quiz:
  - q: You run `UPDATE orders SET status = 'paid' WHERE id = 7`. What happens to the old row in Postgres?
    options:
      - It is changed in place on disk.
      - It stays as an old row version until no transaction can see it, and VACUUM frees its space.
      - It is moved to an undo log at once.
    answer: 1
    why: Postgres writes a new row version and keeps the old one for older snapshots. VACUUM removes it later.
  - q: A database logs "must be vacuumed within 39985967 transactions". What does that mean?
    options:
      - Autovacuum is keeping up. Nothing to do.
      - Old transaction IDs are near the wraparound limit. Find what blocks VACUUM and fix it now.
      - You must restart Postgres in single-user mode at once.
    answer: 1
    why: In PostgreSQL 18 and earlier, the warning starts 40 million transactions before wraparound (100 million from version 19). Below 3 million, Postgres refuses new writes. The docs say single-user mode is usually not needed.
  - q: Which update is most likely to be a HOT update?
    options:
      - Changing an indexed email column.
      - Changing a non-indexed counter when the page has free space.
      - Any update on a table with a GIN index.
    answer: 1
    why: HOT needs no change to indexed columns and room on the same page.
  - q: You want to move to a new major Postgres version with little downtime. Which tool fits?
    options:
      - Physical streaming replication to a standby on the new version.
      - Logical replication to a server on the new version.
      - A replication slot alone.
    answer: 1
    why: Physical streaming does not work across major versions. Logical replication does, but it does not copy schema changes, and before version 19 it does not copy sequence values.
  - q: Why can 2,000 direct connections hurt a Postgres server even when most are idle?
    options:
      - Each connection is its own backend process, and Postgres sizes shared memory by max_connections.
      - Postgres has a hard limit of 100 connections.
      - Idle connections hold row locks.
    answer: 0
    why: Postgres starts one process per connection. The default limit is about 100, and raising it costs memory.
incidents: [mailchimp-2019-02-mandrill-xid-wraparound]
related: [concurrency-control, secondary-indexes, replication, schema-migrations, transactions-isolation]
reviewed_by: ai-factcheck
reviewed_on: 2026-11-13
---

## What broke

At 05:35 UTC on 4 February 2019, one database stopped taking writes at Mandrill, Mailchimp's email service for apps. The database was a Postgres shard called shard4. It went into "safety shutdown mode". [@mandrill]

The cause was transaction ID wraparound. Postgres gives each writing transaction a 32-bit ID. Autovacuum must clean up old IDs in time. On shard4 it had fallen behind or failed, and the IDs hit their limit. [@mandrill]

The team had seen it coming. In November 2018 they found the IDs at about half their limit during peak load. They added a ticket to set up more monitoring. [@mandrill]

The fix was slow. Vacuuming the whole shard was estimated to take up to 40 days. Two tables were terabytes in size. Mandrill deletes queued mail after 48 hours, so time mattered. [@mandrill]

At 19:12 UTC on 5 February, the team truncated those large tables. The vacuum finished about an hour later. Service came back after about 40 hours. By 22:40 they had alerts for wraparound. [@mandrill] Sentry had the same outage in 2015. It too ended by truncating one large table. [@sentry]

## The idea in simple words

Postgres does not change a row in place. An `UPDATE` writes a new row version and leaves the old one behind. [@pg-vacuum] The [concurrency control lesson](/learn/locking-and-mvcc-in-practice) showed why: readers with an older snapshot still need it.

So every busy table creates garbage all the time. An old version that no transaction can see is a dead tuple. VACUUM finds dead tuples and frees their space. [@pg-vacuum]

VACUUM has a second job that people forget. It marks old rows as "frozen", so their transaction IDs can be reused safely. [@pg-vacuum] Skip the first job and you get bloat. Skip the second and Postgres stops writes.

> [!key] **The key idea.** In Postgres, every update and delete leaves work for VACUUM. Your job is to keep autovacuum fast enough, and to make sure nothing stops it.

## How it works

Step through one update, from new row version to cleanup.

[[diagram:heap-update-hot]]

### Autovacuum and when it falls behind

Autovacuum vacuums a table after enough rows have changed. By default the trigger is 50 rows plus 20% of the table. [@pg-vacuum-config] On a table with 100 million rows, that means about 20 million dead tuples before it starts. By default, 3 workers share the whole server. [@pg-vacuum-config]

When it falls behind, dead tuples pile up and the table bloats. Scans read more pages. A long open transaction or a forgotten replication slot can also stop cleanup. [@pg-client] [@pg-vacuum] Watch `n_dead_tup` and `last_autovacuum` in `pg_stat_all_tables`. [@pg-stats]

### Transaction ID wraparound, precisely

Transaction IDs (XIDs) are 32 bits. Postgres compares them in a circle: for each ID, two billion are "older" and two billion are "newer". [@pg-vacuum] An unfrozen row older than that would suddenly look like it is from the future.

So every table must be vacuumed at least once every two billion transactions. [@pg-vacuum] Here are the steps, from the current docs: [@pg-vacuum]

1. A table with unfrozen rows older than `autovacuum_freeze_max_age` gets an anti-wraparound autovacuum. The default is 200 million. [@pg-vacuum-config] This runs even if autovacuum is off.
2. When the oldest XIDs are 40 million transactions from wraparound, PostgreSQL 18 logs `WARNING: database "mydb" must be vacuumed within ... transactions`. From version 19 this warning starts at 100 million. [@pg-vacuum-19] [@pg19-notes]
3. At 3 million left, it refuses to start new transactions that need an XID. Reads still work. Writes fail.

To recover, the docs say: end old transactions, resolve prepared ones, drop stale slots, then run a plain `VACUUM` as a superuser. Single-user mode is usually not needed any more. The one exception: you want to `TRUNCATE` or `DROP` a large table instead of vacuuming it. [@pg-vacuum]

Check how close each database is: [@pg-vacuum]

```sql
SELECT datname, age(datfrozenxid) FROM pg_database
ORDER BY 2 DESC;
```

On a healthy server the age stays below about 200 million, because the anti-wraparound autovacuum starts there. Alert when it stays well above that, for example at 500 million. That is long before the warning. Mandrill's monitoring ticket sat in the backlog. [@mandrill]

### HOT updates

In the diagram, the update was a HOT update (heap-only tuple). It needs two things. No column used by an index changes (BRIN indexes do not count), and the new version fits on the same page. [@pg-hot] Then no index needs a new entry, though a BRIN summary may be updated. [@pg-hot]

Every B-tree, GIN or GiST index you add makes HOT less likely. To leave room on pages, lower the table's `fillfactor`: [@pg-hot]

```sql
ALTER TABLE orders SET (fillfactor = 80);
```

This applies to pages written from now on. Existing full pages get no room until the table is rewritten. [@pg-alter]

Compare `n_tup_hot_upd` with `n_tup_upd` to see your HOT rate. [@pg-stats]

### Picking an index type

The [indexes lesson](/learn/secondary-and-composite-indexes) covers composite, partial and expression indexes. Here is when to leave the B-tree.

- **B-tree**: the default. Equality and ranges on sortable values. [@pg-index-types]
- **GIN**: an inverted index for values with many parts, like arrays. [@pg-index-types] It also serves `jsonb` containment (`@>`) and key checks. [@pg-json] It is the preferred index for full text search. [@pg-textsearch-idx]
- **GiST**: a framework for geometric data and "nearest-neighbor" queries. [@pg-index-types]
- **BRIN**: small summaries of block ranges. Best when a column follows the physical row order, like `created_at` in an append-only log. [@pg-index-types]

```sql
CREATE INDEX events_payload_gin
  ON events USING GIN (payload jsonb_path_ops);
CREATE INDEX events_created_brin
  ON events USING BRIN (created_at);
```

`jsonb_path_ops` is usually much smaller than the default class. But it does not support the key-exists operators like `?`. [@pg-json]

### Reading EXPLAIN (ANALYZE, BUFFERS)

`EXPLAIN ANALYZE` runs the query and shows real times and row counts next to the estimates. [@pg-explain] Three checks catch most problems:

- **Estimated vs actual rows.** The docs call this the most important thing to look for. A big gap means the planner guessed wrong. [@pg-explain]
- **Loops.** Times and rows are per loop. Multiply by `loops` for the total. [@pg-explain]
- **Buffers.** From PostgreSQL 18, ANALYZE turns on buffer counts too. [@pg18-notes] On older versions, add BUFFERS. A "hit" means a read was avoided because the page was already in the cache. [@pg-explain-ref] A "read" had to ask the operating system, from disk or its cache. Our advice: many buffers (hit plus read) for a few rows often point to bloat or a missing index. Many reads but few hits just mean the data was not in cache.

`ANALYZE` really runs the statement. Wrap an `UPDATE` in `BEGIN` and `ROLLBACK`. [@pg-explain]

### WAL and checkpoints

You met the [write-ahead log](/learn/btree-vs-lsm-tree) before. Postgres also runs checkpoints. At a checkpoint it flushes all changed pages and records that point in the WAL. After a crash, it replays only from there. [@pg-wal-config]

A checkpoint starts every 5 minutes, or sooner when the WAL nears `max_wal_size` (default 1 GB). [@pg-wal-config] After each checkpoint, the first change to a page writes the whole page to the WAL. [@pg-wal-config] So frequent checkpoints mean more WAL. By default, Postgres logs a hint when checkpoints caused by WAL volume come less than 30 seconds apart (`checkpoint_warning`). [@pg-wal-runtime]

### Physical vs logical replication

**Physical streaming replication** sends WAL records to a standby as they are made. [@pg-repl] It copies exact disk blocks, so the standby is a copy of the whole server. [@pg-logical] It cannot run a different major version. [@pg-repl]

**Logical replication** sends row changes by table and key, through publications and subscriptions. It can copy a subset of the data, and works across major versions. [@pg-logical] But it does not copy schema changes. [@pg-logical-limits] It also does not keep sequence values in step, though version 19 can sync them on request. [@pg-logical-limits] [@pg19-notes]

Both can use a replication slot. A slot that nobody reads can keep WAL until the disk fills. [@pg-repl] It also holds back VACUUM. [@pg-vacuum]

### One process per connection

Postgres starts a new backend process for every connection. [@pg-connect] The default `max_connections` is about 100. Raising it allocates more shared memory. [@pg-conn-config] So thousands of app connections usually go through a pooler like PgBouncer. The connection pooling lesson, coming later, covers this.

## Trade-offs

| Choice | Gain | Cost | Use when |
|---|---|---|---|
| Default autovacuum | No tuning work. | Starts late on huge tables. 3 workers in total. [@pg-vacuum-config] | Small and medium tables. |
| Per-table autovacuum settings | Vacuums big, busy tables early. | More vacuum I/O, and settings to maintain. | Tables with many millions of rows and frequent updates. |
| Lower `fillfactor` | More HOT updates, fewer index writes. [@pg-hot] | Bigger table on disk. | Rows updated often on non-indexed columns. |
| GIN on `jsonb` | Fast containment and key search. [@pg-json] | Updating it tends to be slow. [@pg-gin] | You filter on fields inside documents. |
| BRIN | Very small index. [@pg-brin] | Poor when values do not follow row order. [@pg-index-types] | Append-only time data. |
| Physical replica | Exact copy, simple failover. [@pg-repl] | Same major version, whole server only. | High availability and read scaling. |
| Logical replication | Chosen tables, across versions. [@pg-logical] | No schema changes. Sequences only from version 19. [@pg-logical-limits] [@pg19-notes] | Upgrades and feeding other databases. |

**Simple rule:** keep the defaults, but watch dead tuples and XID age from day one.

## At work: how a senior uses this

- Alert on `age(datfrozenxid)` per database. [@pg-vacuum] Treat a rising number as a ticket for this week, not the backlog. Mandrill's monitoring ticket waited there. [@mandrill]
- Set `idle_in_transaction_session_timeout`. An idle open transaction holds back VACUUM and causes bloat. [@pg-client]
- Set `statement_timeout` per app role or session, not in `postgresql.conf`, which affects every session. [@pg-client]
- Set `lock_timeout` in every migration, as in the [schema migrations lesson](/learn/schema-migrations-zero-downtime). [@pg-client]
- Turn on `pg_stat_statements`. It needs `shared_preload_libraries` and a restart, so do it early. [@pg-stat-statements] Sort by `total_exec_time` to find what costs most.
- Check `pg_replication_slots` for inactive slots after every replica or CDC change. [@pg-vacuum]
- For big, busy tables, tune autovacuum per table. Sentry raised workers and removed the cost delay after its outage. [@sentry]
- Know which large tables you could truncate in an emergency. Both Mandrill and Sentry recovered that way. [@mandrill] [@sentry] Once writes have stopped, truncating needs single-user mode. [@pg-vacuum]

## With AI

### What to ask the agent

> "Write SQL that lists, for each table, dead tuples, the last autovacuum time, the HOT update rate and the age of relfrozenxid. Sort by the worst."

> "Here is our EXPLAIN (ANALYZE, BUFFERS) output. Find the node where estimated and actual rows differ most, and explain why."

> "We query `payload->>'country'` and `payload @> '{\"tier\":\"gold\"}'` on a jsonb column. Propose indexes for each and explain the difference."

### What to check yourself

- Every number the agent gives for wraparound matches the docs for your version: 200 million, 40 million (100 million from version 19) and 3 million. Old blog posts give older advice.
- The agent did not suggest `VACUUM FULL` as a fix for wraparound. The docs say not to use it there. [@pg-vacuum]
- A proposed GIN index actually supports the operator in the query.
- Timeouts are set per role or session, with a longer limit for migrations and reports.
- `EXPLAIN ANALYZE` on a write runs inside a transaction that rolls back.

## Check yourself

## Go deeper

Before this: [Transactions and isolation levels](/learn/transactions-and-isolation-levels) and [Replication](/learn/database-replication). Related: [Concurrency control in practice](/learn/locking-and-mvcc-in-practice), [Secondary and composite indexes](/learn/secondary-and-composite-indexes) and [B-trees and LSM-trees](/learn/btree-vs-lsm-tree). Coming later: connection pooling.
