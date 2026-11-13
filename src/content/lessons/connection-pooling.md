---
topic: connection-pooling
subtitle: Learn why databases run out of connections, how to size and guard a pool, and what breaks when you put PgBouncer in front of Postgres.
status: published
illustrative_story: false
glossary_optout: [parameterized-query]
sources:
  - id: gh-oct-2026
    title: "GitHub availability report: October 2026"
    url: https://github.blog/news-insights/company-news/github-availability-report-october-2026/
    note: The October 30 entry, where a shared database connection pool ran out and requests failed across github.com.
  - id: pg-connect
    title: "PostgreSQL documentation: 51.2. How Connections Are Established"
    url: https://www.postgresql.org/docs/current/connect-estab.html
    note: The process-per-user model, where the postmaster starts one backend process per connection.
  - id: pg-conn-config
    title: "PostgreSQL documentation: 19.3. Connections and Authentication"
    url: https://www.postgresql.org/docs/current/runtime-config-connection.html
    note: max_connections, its default, and the slots reserved for superusers.
  - id: pg-scalability
    title: "Andres Freund, Analyzing the Limits of Connection Scalability in Postgres"
    url: https://techcommunity.microsoft.com/blog/adforpostgresql/analyzing-the-limits-of-connection-scalability-in-postgres/1757266
    note: Measurements showing that memory is not the main limit, and that thousands of idle connections slow down active ones.
  - id: hikari-sizing
    title: "About Pool Sizing · brettwooldridge/HikariCP Wiki"
    url: https://github.com/brettwooldridge/HikariCP/wiki/About-Pool-Sizing
    note: Why a small pool is faster, the starting formula, and the minimum size that avoids pool deadlock.
  - id: hikari
    title: "HikariCP README (brettwooldridge/HikariCP on GitHub)"
    url: https://github.com/brettwooldridge/HikariCP
    note: Every pool setting with its default, including connectionTimeout, maxLifetime and leakDetectionThreshold.
  - id: psycopg-pool
    title: "psycopg_pool – Connection pool implementations"
    url: https://www.psycopg.org/psycopg3/docs/api/pool.html
    note: The Python pool API, its wait timeout and the warning that a missing putconn() depletes the pool.
  - id: pgb-features
    title: "PgBouncer features"
    url: https://www.pgbouncer.org/features.html
    note: The three pooling modes and a table of which Postgres features work in each.
  - id: pgb-config
    title: "PgBouncer config"
    url: https://www.pgbouncer.org/config.html
    note: pool_mode, pool sizes, max_prepared_statements, and the note that PgBouncer is single-threaded.
  - id: pgb-changelog
    title: "PgBouncer changelog"
    url: https://www.pgbouncer.org/changelog.html
    note: Version 1.21.0 adds protocol-level prepared statements; 1.24.0 turns them on by default.
  - id: pg-client
    title: "PostgreSQL documentation: 19.11. Client Connection Defaults"
    url: https://www.postgresql.org/docs/current/runtime-config-client.html
    note: idle_in_transaction_session_timeout, and a warning about idle timeouts behind poolers.
  - id: pg-stats
    title: "PostgreSQL documentation: 27.2. The Cumulative Statistics System"
    url: https://www.postgresql.org/docs/current/monitoring-stats.html
    note: The pg_stat_activity view and its state values, such as "idle in transaction".
  - id: rds-proxy
    title: "Amazon RDS Proxy - Amazon Relational Database Service"
    url: https://docs.aws.amazon.com/AmazonRDS/latest/UserGuide/rds-proxy.html
    note: A managed pooler that queues, throttles and finally rejects connections it cannot serve.
  - id: rds-pinning
    title: "Avoiding pinning an RDS Proxy"
    url: https://docs.aws.amazon.com/AmazonRDS/latest/UserGuide/rds-proxy-pinning.html
    note: Which Postgres statements tie a client to one database connection and stop reuse.
claims_checked:
  - claim: "On 30 October 2026 a database connection pool shared by many github.com services ran out. Update requests held connections \"reserved but unused\" during a slow lookup after starting a write. About 43% of pull request merges and about 3.5% of API calls failed."
    source: gh-oct-2026
    note: "Checked against the October 30 entry. No duration is stated (the header says 31 minutes, the body about 9). \"Several seconds\" is kept as GitHub's wording; the 3-second figure and the diagram are marked as a model."
  - claim: "Postgres max_connections defaults to typically 100, and 3 slots are reserved for superusers by default."
    source: pg-conn-config
    note: "Both defaults match the documentation, including the note that Postgres sizes shared memory from max_connections."
  - claim: "In PgBouncer transaction pooling, SET/RESET, LISTEN, session-level advisory locks, SQL PREPARE/DEALLOCATE, WITH HOLD cursors, temporary tables that outlive a transaction, and LOAD are not supported."
    source: pgb-features
    note: "Each item is marked \"Never\" in the feature table. The SET LOCAL and pg_advisory_xact_lock workarounds are kept apart from the docs' list."
  - claim: "PgBouncer 1.21.0 (October 2023) added protocol-level named prepared statements; 1.24.0 turned them on by default with max_prepared_statements at 200."
    source: pgb-changelog
    note: "1.21.0 is dated 2023-10-16; the 1.24.0 entry states the new default of 200."
  - claim: "HikariCP defaults: maximumPoolSize 10, connectionTimeout 30 seconds, leakDetectionThreshold off (0)."
    source: hikari
    note: "Checked against the README. The quiz wording was changed to \"logs a warning\", because the README does not mention a stack trace."
  - claim: "With 5,000 mostly idle connections, a single active connection ran less than half as fast, and each idle connection used below 2 MiB with huge_pages."
    source: pg-scalability
    note: "33,773 vs 9,807 TPS (about 29%). Dated to 2020 in the text."
reviewed_by: ai-factcheck
reviewed_on: 2026-11-13
quiz:
  - q: Your service has 25 pods, each with a fixed-size pool of 10 (HikariCP's default). Postgres has max_connections = 300. What happens during a deploy that briefly runs 40 pods?
    options:
      - Nothing. Pools only open connections when they need them.
      - The pods need 400 connections, more than the server allows, so some fail to connect.
      - Postgres queues the extra connections until others close.
    answer: 1
    why: Each pod has its own pool. 25 × 10 = 250 fits, but 40 × 10 = 400 is over the limit, and 3 slots are reserved for superusers.
  - q: A request starts a transaction, calls a payment API for 4 seconds, then commits. What is the main pool problem?
    options:
      - It holds a connection for 4 seconds while doing no database work.
      - The payment API call uses up the database's CPU.
      - Nothing, as long as the pool is large.
    answer: 0
    why: The connection is borrowed but idle. A few of these requests can take the whole pool, as in GitHub's October 2026 incident.
  - q: You move from direct connections to PgBouncer in transaction mode. Which code is most likely to break?
    options:
      - A plain SELECT inside BEGIN and COMMIT.
      - A job that takes a session-level advisory lock, then runs several transactions.
      - An INSERT with a RETURNING clause.
    answer: 1
    why: Session-level advisory locks are not supported in transaction pooling. The next transaction may run on a different server connection.
  - q: Which pool setting logs a warning when a connection may have leaked?
    options:
      - maxLifetime
      - minimumIdle
      - leakDetectionThreshold
    answer: 2
    why: HikariCP logs a possible leak when a connection has been out of the pool longer than this threshold. It is off by default.
incidents: [github-2026-10-connection-pool-exhaustion]
related: [postgres, tail-latency-queueing, backpressure, load-shedding, resilience-patterns]
---

## What broke

On 30 October 2026, some requests to github.com hung for up to about 10 seconds. Then they failed with a server error. At the worst point, about 43% of pull request merge attempts failed. About 3.5% of API calls failed too. [@gh-oct-2026]

GitHub's report blames a connection pool, not the database itself. A pool used by many github.com services had reached its limit. For several minutes, no connections were available. [@gh-oct-2026]

The pool was already near its limit. Then some update requests began to hold their connections far longer than expected. Each one started writing a change, then ran a separate lookup that could take several seconds. During that lookup, the connection was "reserved but unused". [@gh-oct-2026]

New requests waited for a free connection. When the connection timeout expired, they failed. [@gh-oct-2026]

GitHub changed the order. The slow lookup now runs before the write starts, so it holds no connection. GitHub is also looking at limits on how long a pooled connection can be held. [@gh-oct-2026]

## The idea in simple words

A database connection is expensive to open and expensive to keep. So apps open a few, keep them, and lend them out. That set of reusable connections is a **connection pool**.

A pool is a tiny resource with a queue in front of it. A request borrows a connection, uses it and gives it back. If all connections are out, the next request waits. If it waits too long, it fails.

So a pool works only if two things are true. The pool is the right size. And every borrower gives the connection back quickly.

> [!key] **The key idea.** The database can serve only a few connections well. Keep pools small, count them across every app instance, and hold a connection only while you are actually talking to the database.

## How it works

### Why connections are scarce

The [Postgres lesson](/learn/postgresql-in-depth) showed the process-per-user model. For every new connection, the postmaster starts a new backend process. [@pg-connect] Before the first query, that backend must also check the client's credentials. [@pg-stats] AWS describes this as "the memory and CPU overhead" of opening a connection. [@rds-proxy]

The default `max_connections` is typically 100. Postgres sizes some resources, including shared memory, from it. [@pg-conn-config] Three slots are reserved for superusers by default. [@pg-conn-config] Your app gets the rest.

Why not just raise it to 5,000? Memory is not the main problem. Andres Freund measured idle connections at below 2 MiB each with a good setup. [@pg-scalability] The real cost is that every open connection makes the others slower. In tests he published in 2020, a single active connection ran less than half as fast with 5,000 mostly idle connections open. [@pg-scalability] Few connections, kept busy, beat many idle ones.

### Sizing an app-side pool

A pool like HikariCP for Java has four settings that matter most: [@hikari]

- **Maximum size** (`maximumPoolSize`). Idle plus in-use connections. Default 10.
- **Minimum idle** (`minimumIdle`). The authors recommend leaving it unset, so the pool stays a fixed size.
- **Acquire timeout** (`connectionTimeout`). How long a caller waits for a connection before an error. Default 30 seconds.
- **Max lifetime** (`maxLifetime`). Retire a connection after this age. Set it "several seconds shorter" than any limit imposed by the database or network.

How big? Smaller than you think. HikariCP's "About Pool Sizing" page describes an Oracle demo. Cutting the pool from 2,048 connections to 96 cut response times from about 100 ms to about 2 ms. [@hikari-sizing] A database server has a few CPU cores. Beyond some point, more connections only add switching and waiting.

The page gives a formula from the PostgreSQL project as a **starting point**: [@hikari-sizing]

```text
connections = ((core_count * 2) + effective_spindle_count)
```

`core_count` excludes hyper-threads. `effective_spindle_count` is zero if your active data is fully cached. It grows toward the number of disks as more reads miss the cache. The page says there has been no analysis yet of how well it works with SSDs. [@hikari-sizing] So treat it as a first guess. Load test around it and keep the size that gives the best latency.

There is also a floor. If one request needs several connections at once, a small pool can deadlock. The minimum is `threads × (connections per thread − 1) + 1`. [@hikari-sizing] Our advice: better still, design each request to need only one connection.

To check the app side, use Little's law from the [tail latency lesson](/learn/tail-latency-and-queueing). Connections in use = requests per second × seconds each request holds one. At 500 requests a second and 10 ms per hold, about 5 are in use. GitHub's lookups took several seconds. At 3 seconds you would need 1,500.

### The multiplication problem

Every app instance has its own pool. So the database sees the sum.

Take 30 pods with a pool of 20 each. That is 600 connections against a limit near 100. Autoscaling and rolling deploys make it worse, because old and new pods run at the same time. Our advice: treat serverless functions as the extreme case. Each running copy may open its own connection, and many copies can start at once.

Write the sum down. `pods at peak × pool size + admin tools + cron jobs` must stay below the database limit. When it cannot, put a shared pooler in the middle.

### External poolers: PgBouncer and RDS Proxy

PgBouncer can accept many client connections and share a few server connections among them. It has three pooling modes: [@pgb-features]

- **Session pooling.** A client keeps one server connection until it disconnects. Every Postgres feature works. It is the default. [@pgb-config]
- **Transaction pooling.** A client gets a server connection only for one transaction. This is where the big savings are.
- **Statement pooling.** Transaction pooling that forbids multi-statement transactions.

Transaction pooling breaks anything that lives in the session, because your next transaction may run on another connection. PgBouncer's docs list these as unsupported: [@pgb-features]

- `SET` and `RESET`.
- `LISTEN`.
- Session-level advisory locks.
- SQL `PREPARE` and `DEALLOCATE`, and `WITH HOLD` cursors.
- Temporary tables that live past one transaction, and `LOAD`.

Two common workarounds: `SET LOCAL`, which lasts only for the transaction, and `pg_advisory_xact_lock`, which is released at commit. RDS Proxy, for example, does not pin on transaction-level advisory locks. [@rds-pinning]

Prepared statements used to be the big pain. PgBouncer 1.21.0 added support for protocol-level named prepared statements in October 2023. [@pgb-changelog] Since 1.24.0, it is on by default, with `max_prepared_statements` at 200. [@pgb-changelog] [@pgb-config] SQL-level `PREPARE` still does not work. [@pgb-features] Also note that PgBouncer is single-threaded and uses one CPU core per instance. [@pgb-config]

On AWS, **RDS Proxy** is a managed pooler. It queues or throttles connections it cannot serve at once. Past your limits, it rejects them. [@rds-proxy] When a Postgres session uses `SET`, `LISTEN`, SQL `PREPARE`, session advisory locks, temporary tables or even `nextval`, among others, the proxy "pins" that client to one database connection. [@rds-pinning] Pinned connections cannot be shared, so watch the pinning metric. [@rds-pinning]

### Exhaustion and leaks

Here is a simplified model of what happened at GitHub. The pool size and timings are made up:

[[diagram:pool-held-connections]]

Pool exhaustion looks strange from the outside. The app is slow and returns errors. But the database CPU is low and its queries are fast. The time is spent waiting for a connection, not running a query.

The acquire timeout is your safety valve. A 30-second default means callers hang for 30 seconds first. [@hikari] Set it below your request deadline, so a full pool fails fast. Then [load shedding](/learn/load-shedding-graceful-degradation) and [backpressure](/learn/backpressure-and-flow-control) can do their jobs.

A **connection leak** is a borrow without a return. Each leak shrinks the pool by one, until nothing is left. The psycopg docs warn that forgetting `putconn()` "will deplete the pool". [@psycopg-pool]

```python
# Leaks: if execute() raises, the connection never goes back.
conn = pool.getconn()
conn.execute(sql)
pool.putconn(conn)

# Safe: the block always returns it.
with pool.connection() as conn:
    conn.execute(sql)
```

To find leaks, turn on HikariCP's `leakDetectionThreshold`. It logs a message when a connection has been out of the pool longer than that. It is off by default. [@hikari] On the server, look in `pg_stat_activity` for sessions in the state `idle in transaction`. [@pg-stats] Set `idle_in_transaction_session_timeout` to end them. [@pg-client]

## Trade-offs

| Option | Gain | Cost | Use when |
|---|---|---|---|
| No pool | Simple. | A new backend process per request. [@pg-connect] | Scripts and one-off jobs. |
| App-side pool | No extra hop. Every feature works. | Total = pods × size. | A fixed, small number of instances. |
| PgBouncer, session mode | Every feature works. [@pgb-features] | Saves little. One client still holds one connection. | Many short-lived client connections. |
| PgBouncer, transaction mode | Many clients share a few connections. | No session state, `LISTEN` or session locks. [@pgb-features] | Many pods or serverless functions. |
| RDS Proxy | Managed. Queues, then rejects. [@rds-proxy] | Pinning stops sharing. [@rds-pinning] | You run on RDS and want no pooler to operate. |
| Raise `max_connections` | One config line. | More shared memory, and slower active connections. [@pg-conn-config] [@pg-scalability] | Small steps only, after measuring. |

**Simple rule:** small app pools, a sum you have written down, and a transaction-mode pooler once instances multiply.

## At work: how a senior uses this

- Keep a table of every client of the database: service, peak instances, pool size. Check the sum against `max_connections` before each scale-up.
- Ask in review: **"Does this code hold a connection while it waits for something else?"** HTTP calls, queue publishes and file uploads go before or after the transaction.
- Set the acquire timeout below the request deadline. Alert on time waiting for a connection, not only query time.
- Graph pool metrics: active, idle, waiting and time to acquire.
- Turn on leak detection in staging and production, set to a few times your longest normal transaction.
- Before moving to transaction pooling, search the code for `SET `, `LISTEN`, `pg_advisory_lock`, `PREPARE` and temporary tables.
- Keep `maxLifetime` below any timeout in PgBouncer, proxies or load balancers.

## With AI

### What to ask the agent

> "List every place in this service that borrows a database connection. For each, say what else happens while it is held: HTTP calls, sleeps, retries or queue sends."

> "We have 24 pods, a HikariCP pool of 30 and Postgres with 4 cores and max_connections 200. Work out the total connections at peak and during a rolling deploy, and propose pool sizes."

> "We plan to put PgBouncer in transaction mode in front of this app. Search the code for session state that will break: SET, LISTEN, advisory locks, SQL PREPARE, WITH HOLD cursors and temporary tables."

### What to check yourself

- Every `getconn` or manual borrow has a matching return in a `finally` block or a `with` block.
- The proposed pool size came from a load test, not just from the formula.
- The agent counted all instances during a deploy, not only at normal load.
- Lock code uses `pg_advisory_xact_lock`, not session-level advisory locks, if you use transaction pooling.
- Your PgBouncer version is 1.21 or later before you rely on prepared statements, and `max_prepared_statements` is not 0.

## Check yourself

## Go deeper

Before this: [Postgres](/learn/postgresql-in-depth) and [Tail latency and queueing](/learn/tail-latency-and-queueing). Next to it: [Backpressure](/learn/backpressure-and-flow-control), [Load shedding](/learn/load-shedding-graceful-degradation) and [Circuit breakers and bulkheads](/learn/circuit-breakers-and-bulkheads).
