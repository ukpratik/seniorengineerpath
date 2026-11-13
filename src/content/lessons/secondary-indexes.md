---
topic: secondary-indexes
subtitle: Pick indexes that match your real queries, read the plan to prove it, and count what every extra index costs on writes.
status: published
reviewed_by: ai-factcheck
reviewed_on: 2026-11-09
illustrative_story: false
sources:
  - id: gitlab-6458
    title: "GitLab, “Incident Review: INC-6458: Sidekiq queueing SLO violation on urgent-cpu-bound shard” (2026)"
    url: https://gitlab.com/gitlab-com/gl-infra/production/-/issues/21049
    note: A dropped index on one table slowed queries, filled the connection pool and delayed GitLab.com for 2 hours 36 minutes.
  - id: pg-index-only
    title: "PostgreSQL documentation: Index-Only Scans and Covering Indexes"
    url: https://www.postgresql.org/docs/current/indexes-index-only-scans.html
    note: Why every Postgres index is a secondary index, why a normal index scan visits the heap, and how INCLUDE and the visibility map help.
  - id: innodb-clustered
    title: "MySQL 8.4 Reference Manual: Clustered and Secondary Indexes"
    url: https://dev.mysql.com/doc/refman/8.4/en/innodb-index-types.html
    note: The InnoDB clustered index holds the rows. Secondary index records hold the primary key, which is used for a second lookup.
  - id: pg-multicolumn
    title: "PostgreSQL documentation: Multicolumn Indexes"
    url: https://www.postgresql.org/docs/current/indexes-multicolumn.html
    note: The exact rule for which conditions narrow a multicolumn B-tree scan, skip scan, and advice to use these indexes sparingly.
  - id: pg18-notes
    title: "PostgreSQL 18 release notes"
    url: https://www.postgresql.org/docs/release/18.0/
    note: B-tree skip scans, and EXPLAIN ANALYZE changes (buffers by default, fractional row counts, index searches).
  - id: luke-ranges
    title: "Markus Winand, “Indexing SQL range conditions less than, greater than and between”, Use The Index, Luke!"
    url: https://use-the-index-luke.com/sql/where-clause/searching-for-ranges/greater-less-between-tuning-sql-access-filter-predicates
    note: A worked example of why equality columns go before range columns.
  - id: pg-partial
    title: "PostgreSQL documentation: Partial Indexes"
    url: https://www.postgresql.org/docs/current/indexes-partial.html
    note: Indexes over a subset of rows, when the planner can use them, and why they are not a substitute for partitioning.
  - id: pg-expression
    title: "PostgreSQL documentation: Indexes on Expressions"
    url: https://www.postgresql.org/docs/current/indexes-expressional.html
    note: The lower(col1) example, its cost on writes, and unique indexes on expressions.
  - id: pg-unique
    title: "PostgreSQL documentation: Unique Indexes"
    url: https://www.postgresql.org/docs/current/indexes-unique.html
    note: Unique constraints and primary keys create a unique index for you. How nulls are treated.
  - id: pg-explain
    title: "PostgreSQL documentation: Using EXPLAIN"
    url: https://www.postgresql.org/docs/current/using-explain.html
    note: How to read plan nodes and costs, what bitmap scans do, and why EXPLAIN ANALYZE really runs the query.
  - id: pg-index-intro
    title: "PostgreSQL documentation: Indexes, Introduction"
    url: https://www.postgresql.org/docs/current/indexes-intro.html
    note: Indexes add overhead to every write, and seldom-used indexes should be removed.
  - id: pg-stats
    title: "PostgreSQL documentation: The Cumulative Statistics System"
    url: https://www.postgresql.org/docs/current/monitoring-stats.html
    note: The pg_stat_user_indexes view and its idx_scan and last_idx_scan columns.
  - id: pg-vacuum
    title: "PostgreSQL documentation: Routine Vacuuming"
    url: https://www.postgresql.org/docs/current/routine-vacuuming.html
    note: VACUUM updates the visibility map, which speeds up index-only scans.
  - id: pg-create-index
    title: "PostgreSQL documentation: CREATE INDEX"
    url: https://www.postgresql.org/docs/current/sql-createindex.html
    note: A normal build blocks writes. CONCURRENTLY does not, but it is slower and can leave an invalid index.
  - id: pg-index-types
    title: "PostgreSQL documentation: Index Types"
    url: https://www.postgresql.org/docs/current/indexes-types.html
    note: One paragraph on each index type (B-tree, hash, GiST, SP-GiST, GIN, BRIN) and what it is good for.
  - id: pg-gin
    title: "PostgreSQL documentation: GIN Indexes"
    url: https://www.postgresql.org/docs/current/gin.html
    note: GIN is an inverted index for arrays, jsonb and text search, and it is slow to update.
  - id: pg-brin
    title: "PostgreSQL documentation: BRIN Indexes"
    url: https://www.postgresql.org/docs/current/brin.html
    note: A tiny, lossy index for very large tables where values follow the physical row order.
  - id: ddb-gsi
    title: "Amazon DynamoDB Developer Guide: Using Global Secondary Indexes in DynamoDB"
    url: https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/GSI.html
    note: Global indexes are updated asynchronously, and a global index without enough write capacity throttles writes to the table.
  - id: ddb-lsi
    title: "Amazon DynamoDB Developer Guide: Local secondary indexes"
    url: https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/LSI.html
    note: Local indexes share the table's partition key, allow strongly consistent reads, and limit each partition key value to 10 GB.
claims_checked:
  - claim: "On 6 January 2026 a post-deployment migration removed index_ssh_signatures_on_commit_sha (commit_sha). Slow queries saturated the PgBouncer pool from 07:12 UTC, the Sidekiq backlog peaked at 600,000-700,000 jobs, and impact lasted 2 hours 36 minutes until the index was recreated by hand."
    source: gitlab-6458
    note: "Public incident review (API: confidential false). Impact 06:45:00-09:21:27 UTC; RCA summary, 08:29 and 08:51 updates; key finding 2 on trailing columns of composite indexes."
  - claim: "Equality conditions on leading columns plus a range on the first column without equality limit the part of a multicolumn B-tree that is scanned; multicolumn indexes should be used sparingly."
    source: pg-multicolumn
    note: "Quotes the 'exact rule' paragraph; skip scan generally only with few distinct leading values."
  - claim: "PostgreSQL 18 added B-tree skip scan, BUFFERS by default in EXPLAIN ANALYZE, fractional row counts, and a count of index searches per node."
    source: pg18-notes
    note: "Release notes 18.0: 'Allow skip scans of btree indexes', 'Automatically include BUFFERS output in EXPLAIN ANALYZE', 'Modify EXPLAIN to output fractional row counts', 'report the number of index lookups used per index scan node'."
  - claim: "Every PostgreSQL index is secondary; an index-only scan skips the heap only for pages whose visibility-map all-visible bit is set; INCLUDE adds non-key payload columns."
    source: pg-index-only
    note: "Section 11.9, checked against the PG 18 docs. The illustrative plan's query uses only columns in the covering index."
  - claim: "Each InnoDB secondary index record holds the primary key columns, which are used to search the clustered index; a long primary key makes secondary indexes bigger."
    source: innodb-clustered
    note: "MySQL 8.4 manual 17.6.2.1, 'How Secondary Indexes Relate to the Clustered Index'."
  - claim: "DynamoDB global secondary indexes are updated asynchronously, usually within a fraction of a second, and too little GSI write capacity throttles table writes."
    source: ddb-gsi
    note: "Exact phrase 'within a fraction of a second, under normal conditions'."
quiz:
  - q: "You have an index on orders (customer_id, created_at). Which query can use it to read only a small, narrow part of the index?"
    options:
      - "WHERE created_at > '2026-05-01'"
      - "WHERE customer_id = 42 AND created_at > '2026-05-01'"
      - "WHERE status = 'paid'"
    answer: 1
    why: Equality on the leading column plus a range on the next column gives one narrow range. A condition on created_at alone has no leading column to start from.
  - q: In MySQL's InnoDB, what does an entry in a secondary index contain besides the indexed columns?
    options:
      - The full row.
      - The row's primary key, which is then used to find the row in the clustered index.
      - Nothing else. The index is only used to check uniqueness.
    answer: 1
    why: InnoDB stores the primary key columns in every secondary index record and uses them for a second search in the clustered index. A long primary key makes every secondary index bigger.
  - q: "EXPLAIN ANALYZE shows an Index Only Scan with Heap Fetches close to the number of rows returned. What is the most likely reason?"
    options:
      - The index is the wrong type.
      - Many table pages are not marked all-visible in the visibility map, so Postgres still visits the heap for those rows.
      - The query has no WHERE clause.
    answer: 1
    why: An index-only scan only skips the heap for pages whose all-visible bit is set. VACUUM sets those bits.
  - q: "You have a partial index ON orders (created_at) WHERE billed = false. Your app runs a prepared query with WHERE billed = $1 AND created_at > $2. Can Postgres use the partial index?"
    options:
      - Yes, always.
      - Not reliably. A plan made for any value of $1 cannot use it, because billed = $1 does not prove that billed = false.
      - Only if the table is small.
    answer: 1
    why: A partial index is used only when the query's WHERE condition clearly implies the index condition. Postgres docs say parameterized clauses do not work with a partial index. (Early runs of a prepared query may plan with the real value, but you cannot count on it.)
  - q: "pg_stat_user_indexes shows idx_scan = 0 for users_email_key, the index behind a UNIQUE constraint on email. Should you drop it to save write cost?"
    options:
      - Yes. Zero scans means nobody uses it.
      - No. It enforces the unique constraint on every insert and update, even if no query reads through it.
      - Yes, but only on replicas.
    answer: 1
    why: Postgres creates a unique index for each unique constraint, and that index is how the constraint is enforced. idx_scan counts only scans.
incidents: []
related: [btree-lsm, data-models, schema-migrations, pagination, sharding, search-systems, geo-proximity]
---

## What broke

On 6 January 2026, GitLab.com slowed down for 2 hours and 36 minutes. Merge requests were slow and CI pipelines started late. Customers opened emergency tickets. [@gitlab-6458]

A background job queue grew to between 600,000 and 700,000 jobs. The PgBouncer connection pool in front of the primary database was 100% full from 07:12 UTC. Database CPU stayed high. [@gitlab-6458]

The cause was one missing index. A migration after a deploy had removed `index_ssh_signatures_on_commit_sha`. That was a single-column index on `ssh_signatures (commit_sha)`. [@gitlab-6458]

Queries that looked up signatures by `commit_sha` became slow or timed out. They held connections for longer, so the pool filled up, and everything else waited. By 08:51 the team had found the cause and recreated the index by hand. Performance came back. [@gitlab-6458]

One follow-up is the lesson of this page. GitLab updated its docs on the column order of composite indexes. The point: a query that uses only the trailing columns of a composite index is not covered by it. [@gitlab-6458]

The review does not say why the index looked safe to drop. But here is the lesson for you. An index can look like a copy of another one. It can still be the only one that serves a query.

## The idea in simple words

A table stores its rows in one way. InnoDB keeps them sorted by primary key. Postgres keeps them in no particular order. A **secondary index** is an extra sorted copy of a few columns, with a pointer back to each row. It lets the database find rows by something other than the main key, like orders by customer.

In PostgreSQL, every index is a secondary index. The rows live in a separate area called the **heap**. A normal index scan reads the index, then fetches each row from the heap. [@pg-index-only]

MySQL's InnoDB works differently. The primary key index (the clustered index) holds the rows themselves. Each secondary index entry stores the row's primary key. So a lookup finds the primary key first, then searches the clustered index for the row. [@innodb-clustered] That is why InnoDB advises a short primary key: it is copied into every secondary index. [@innodb-clustered]

> [!key] **The key idea.** An index is a promise to answer one shape of query fast. You pay for that promise on every write. Choose indexes from your real queries, check the plan, and remove the ones nothing needs.

You already know how a B-tree stores sorted keys from [the B-tree and LSM lesson](/learn/btree-vs-lsm-tree). This lesson is about which keys to put in it.

## How it works

Step through one composite index and three queries. Use Next and Back, or the ← and → keys.

[[diagram:composite-index-scan]]

### Composite indexes and column order

A **composite index** sorts by its first column, then by the second inside each value of the first, and so on. Think of a phone book: sorted by last name, then first name.

PostgreSQL states the exact rule. Equality conditions on the leading columns, plus a range on the first column without equality, narrow the part of the index it reads. Conditions on later columns are only checked along the way. [@pg-multicolumn]

So put **equality columns first, then the range column**. Winand shows the same with a date range and a subsidiary ID: with the equality column first, the scan reads far fewer index pages. [@luke-ranges]

```sql
CREATE INDEX orders_customer_created_idx
  ON orders (customer_id, created_at);

-- Uses the index well: equality, then range
SELECT id FROM orders
WHERE customer_id = 42 AND created_at >= '2026-05-06';

-- Cannot narrow the scan: no condition on customer_id
SELECT id FROM orders WHERE created_at >= '2026-05-06';
```

PostgreSQL 18 can sometimes "skip scan" past a leading column with no equality condition. [@pg18-notes] It usually does this only when that column has few distinct values. With many values, the planner usually prefers a full table scan. [@pg-multicolumn] The docs also say to use multicolumn indexes sparingly. Indexes with more than three columns rarely help. [@pg-multicolumn]

### Covering indexes and index-only scans

If the index holds every column the query needs, Postgres can skip the heap. This is an **index-only scan**. [@pg-index-only] An index that holds every column a query needs is a **covering index**. You can add extra "payload" columns with `INCLUDE`. They are stored but are not part of the search key. [@pg-index-only]

```sql
CREATE INDEX orders_customer_created_cov_idx
  ON orders (customer_id, created_at) INCLUDE (status, total);
```

There is a catch. Postgres must still check that each row is visible to your transaction. It skips the heap only for pages marked all-visible in the table's visibility map. [@pg-index-only] VACUUM updates that map. [@pg-vacuum] On a table where many rows change, index-only scans may gain you little. Wide payload columns also make the index bigger. [@pg-index-only]

### Partial, expression and unique indexes

A **partial index** covers only rows that match a `WHERE` condition. [@pg-partial] It is small when most rows don't matter, like unbilled orders. The query's condition must clearly match the index condition. A parameter like `x < $1` does not count. [@pg-partial]

```sql
CREATE INDEX orders_unbilled_idx
  ON orders (created_at) WHERE billed = false;
```

An **expression index** stores the result of an expression. A search on `lower(email)` can use it. [@pg-expression] The expression runs on each insert and on many updates, so writes cost more. Use one when fast reads matter more than fast writes. [@pg-expression]

```sql
CREATE UNIQUE INDEX users_email_lower_idx ON users (lower(email));
```

Because it is `UNIQUE`, this index also blocks two emails that differ only in case. [@pg-expression] A unique constraint or primary key creates its unique index for you. Don't add a second one by hand. [@pg-unique] By default, a unique index allows many nulls. `NULLS NOT DISTINCT` changes that. [@pg-unique]

### Reading EXPLAIN

`EXPLAIN` shows the **query plan**. `EXPLAIN ANALYZE` also runs the query and shows real row counts and times. It really runs it, so wrap an `UPDATE` or `DELETE` in a transaction and end it with `ROLLBACK`. [@pg-explain]

The scan nodes you will see most:

- **Seq Scan**: reads the whole table.
- **Index Scan**: walks the index, then visits the heap for each row. [@pg-index-only]
- **Index Only Scan**: answers from the index, visiting the heap only for pages not marked all-visible. [@pg-index-only]
- **Bitmap Index Scan + Bitmap Heap Scan**: first collects matching row locations, sorts them into physical order, then reads the pages. It skips pages with no matches, so it can beat a full scan. [@pg-explain]

Here is a query that the covering index above can answer alone. Every column it uses is in the index.

```sql
EXPLAIN ANALYZE
SELECT status, total FROM orders
WHERE customer_id = 42 AND created_at >= '2026-05-06';
```

The plan below is illustrative: we made up the numbers, in the PostgreSQL 18 style. PostgreSQL 18 shows buffers by default and prints fractional row counts. [@pg18-notes] The long lines are wrapped to fit a phone.

```text
Index Only Scan using orders_customer_created_cov_idx on orders
    (cost=0.43..8.61 rows=12 width=12)
    (actual time=0.031..0.044 rows=11.00 loops=1)
  Index Cond: ((customer_id = 42) AND
    (created_at >= '2026-05-06 00:00:00+00'::timestamp with time zone))
  Heap Fetches: 0
  Index Searches: 1
  Buffers: shared hit=4
Planning Time: 0.112 ms
Execution Time: 0.061 ms
```

`Heap Fetches: 0` means no row needed a heap visit. A high number means many pages are not all-visible: rows changed recently, or VACUUM is behind. [@pg-index-only] Also look for `Rows Removed by Filter`: rows read and then thrown away. [@pg-explain]

### What each index costs

The database must keep every index in step with the table. That adds work to each insert, update and delete. [@pg-index-intro] Indexes also use disk and memory.

Postgres has a cheaper kind of update that skips index work. More indexes make it happen less often. [@pg-index-intro] The docs say plainly: remove indexes that are seldom or never used. [@pg-index-intro]

To find them, look at `pg_stat_user_indexes`. Its `idx_scan` column counts scans, and `last_idx_scan` (PostgreSQL 16 and later) gives the time of the last one. [@pg-stats]

```sql
SELECT indexrelname, idx_scan, last_idx_scan
FROM pg_stat_user_indexes
ORDER BY idx_scan;
```

A unique index can show zero scans and still be needed, because it enforces the constraint. [@pg-unique] Never drop one for a low `idx_scan`.

Counters reset after a crash or a restore from backup. Each server, including each replica, keeps its own counters. [@pg-stats] A replica that serves reports may use an index the primary never touches. Dropping it on the primary removes it from the replicas too. So check every server first.

### Building an index on a live table

A normal `CREATE INDEX` lets reads continue but blocks inserts, updates and deletes until it finishes. [@pg-create-index] On a large table, the build can take a long time. [@pg-index-intro]

`CREATE INDEX CONCURRENTLY` does not block writes, but it is slower and can leave an invalid index if it fails. [@pg-create-index] See [schema migrations](/learn/schema-migrations-zero-downtime) for how to ship this safely.

### Special index types in one line each

- **GIN**: an inverted index for values with many parts: arrays, `jsonb` and full text search. Fast to search, slow to update. [@pg-gin] See [search systems](/learn/search-and-inverted-indexes).
- **GiST**: a framework for many index types, including geometric data and nearest-neighbour search ("the 10 closest shops"). [@pg-index-types]
- **BRIN**: tiny summaries per block range. Use it for huge tables where a column follows the physical row order, like an order date in an append-only table. [@pg-brin]
- **Hash**: equality only. [@pg-index-types] A B-tree also handles equality, so start with a B-tree.

### At scale: local and global secondary indexes

When data is split across machines, an index can be local to each piece, or global. The [sharding lesson](/learn/sharding-and-consistent-hashing) explains both. DynamoDB makes the trade-off concrete.

A **local secondary index** (LSI) keeps the table's partition key with a different sort key. It allows strongly consistent reads. But it must be created with the table, and all items with one partition key value, in the table and its local indexes together, are limited to 10 GB. [@ddb-lsi]

A **global secondary index** (GSI) can use any key. It is updated asynchronously, usually "within a fraction of a second", so reads may be out of date. [@ddb-gsi] If a GSI lacks write capacity, writes to the table itself are throttled. [@ddb-gsi]

## Trade-offs

| Option | Fast reads | Write cost | Use when |
|---|---|---|---|
| No index | No. Full scan on each query. | Lowest. | Small tables, or queries that read most rows anyway. |
| Single-column index | Yes, for that column. | One more write per row change. | One column filters well on its own. |
| Composite index | Yes, if the query matches the leading columns. | One write, larger entries. | Queries filter on a few columns together: equality first, then range. |
| Covering (`INCLUDE`) | Yes. Can skip the heap. | Larger index. | Hot queries on tables that change slowly. [@pg-index-only] |
| Partial index | Yes, for matching queries only. | Low. Only matching rows. | Most rows are not interesting, like `billed = false`. |
| Global index (GSI) | Yes, by any key. | Extra write on another partition. Eventually consistent. [@ddb-gsi] | A distributed store needs a second access path. |

**Simple rule:** every index should name the query it serves. If you cannot name one, you probably don't need it.

## At work: how a senior uses this

- Start from the slow query log or `pg_stat_statements`, not from the schema. Index the queries that run most or cost most.
- Run `EXPLAIN (ANALYZE, BUFFERS)` on a copy of production data. Small test data gives different plans.
- Choose column order by the queries: equality columns first, the range or sort column last.
- Before dropping an index, list every query that might use it, including ones that use only its trailing columns. GitLab's outage came from one drop. [@gitlab-6458]
- On write-heavy tables, count the indexes. Before you call one unused, watch `pg_stat_user_indexes` on every server for a full business cycle.
- Build indexes on live tables with `CONCURRENTLY`, and check afterwards that no invalid index is left behind.

## With AI

### What to ask the agent

> "Here is the table definition, its indexes, and our 10 most frequent queries with their counts. For each query, say which index serves it, or propose one. Explain the column order. List indexes that no query uses."

> "Here is the EXPLAIN (ANALYZE, BUFFERS) output for a slow query. Explain each node in plain words. Say where the time goes, and whether a different index or query would help."

> "Write a migration that adds this index to a 200-million-row Postgres table without blocking writes. Include a check for an invalid index and a rollback step."

### What to check yourself

- Run every index the agent proposes through `EXPLAIN ANALYZE` on realistic data. A plan that looks good on 1,000 rows can change at 100 million.
- Check that the column order fits equality first, then range.
- Before dropping an index, check `pg_stat_user_indexes` on every server yourself.
- Make sure the migration uses `CONCURRENTLY` and is not wrapped in a transaction.
- For a partial index, check that the app's queries match its condition exactly, not through a parameter.

## Check yourself

## Go deeper

- [API pagination and filtering](/learn/api-pagination-and-filtering): keyset pagination needs a composite index that matches the sort.
- [Data models and query languages](/learn/data-models-and-query-languages): how the planner picks a plan.
- [Data modelling for NoSQL](/learn/data-modelling-for-nosql): global secondary indexes in DynamoDB-style stores.
