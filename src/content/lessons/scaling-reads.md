---
topic: scaling-reads
subtitle: Most apps read far more than they write. Learn the order of steps that serves many more reads without breaking what users see.
status: published
reviewed_by: ai-factcheck
reviewed_on: 2026-10-22
illustrative_story: false
sources:
  - id: fb-memcache
    title: "Rajesh Nishtala and others, “Scaling Memcache at Facebook”, NSDI 2013 (USENIX)"
    url: https://www.usenix.org/conference/nsdi13/technical-sessions/presentation/nishtala
    note: How Facebook served a read-heavy workload with memcache, replicas in other regions, hot key replication and invalidations from the database log.
  - id: fb-tao
    title: "Nathan Bronson and others, “TAO: Facebook's Distributed Data Store for the Social Graph”, USENIX ATC 2013"
    url: https://www.usenix.org/conference/atc13/technical-sessions/presentation/bronson
    note: A read-optimized graph store with leader and follower cache tiers. Shows the read/write mix and measured replication lag.
  - id: pg-explain
    title: "PostgreSQL documentation, “Using EXPLAIN”"
    url: https://www.postgresql.org/docs/current/using-explain.html
    note: How to read a query plan, and what EXPLAIN ANALYZE really runs.
  - id: pg-stat
    title: "PostgreSQL documentation, “pg_stat_statements”"
    url: https://www.postgresql.org/docs/current/pgstatstatements.html
    note: A module that counts calls and time for every SQL statement, so you can find your top reads.
  - id: pg-matview
    title: "PostgreSQL documentation, “Materialized Views”"
    url: https://www.postgresql.org/docs/current/rules-materializedviews.html
    note: What a materialized view is, why it can be much faster, and why its data is not always current.
  - id: pg-refresh
    title: "PostgreSQL documentation, “REFRESH MATERIALIZED VIEW”"
    url: https://www.postgresql.org/docs/current/sql-refreshmaterializedview.html
    note: Refreshing a view, and the CONCURRENTLY option that keeps readers unblocked.
  - id: fowler-cqrs
    title: "Martin Fowler, “CQRS”, martinfowler.com, 2011"
    url: https://martinfowler.com/bliki/CQRS.html
    note: Separate models for reading and writing, and a clear warning that most systems do not need it.
  - id: slack-flannel
    title: "Bing Wei, “Flannel: An Application-Level Edge Cache to Make Slack Scale”, Slack Engineering, 2017"
    url: https://slack.engineering/flannel-an-application-level-edge-cache-to-make-slack-scale/
    note: A cache near users that cut what clients of large teams had to download at startup.
claims_checked:
  - claim: "Facebook users consume an order of magnitude more content than they create; one popular page fetches an average of 521 distinct items from memcache; hundreds of memcached servers per cluster; all-to-all traffic causes incast congestion or a single-server bottleneck."
    source: fb-memcache
    note: "Sections 2 and 3. Incast: responses arriving all at once can overwhelm rack and cluster switches (3.1). 'What broke' reworded to say the cache sits in front of MySQL and other stores, not that it 'took most' reads (the paper gives no such figure)."
  - claim: "Popular items only become more popular as web servers are added; a single key can account for 20% of a server's requests."
    source: fb-memcache
    note: "Section 4 intro and section 3.3. The 20% figure comes from the Gutter (failure) section, so the lesson now presents it as a separate note, not a result of adding servers."
  - claim: "Remote markers: a non-master-region writer sets a marker; on a cache miss, a present marker sends the query to the master region. Lag can make a user's own change appear missing."
    source: fb-memcache
    note: "Section 5. Added 'on a cache miss' and 'non-master region', which the paper states."
  - claim: "Hot key replication: 100 items, 500k req/s per server, 1M req/s; splitting keys leaves each server at 1M, replicating cuts it to 500k; invalidations must reach all replicas."
    source: fb-memcache
    note: "Section 3.2.3 example, matched number for number. The paper applies it to a small key set that fits on one or two servers, so the lesson now says 'a small, very busy set of keys'."
  - claim: "mcsqueal daemons read committed SQL, broadcast deletes to every frontend cluster in the region, and can replay lost invalidations from reliable logs; deletes are used instead of updates because deletes are idempotent."
    source: fb-memcache
    note: "Sections 2 and 4.1. Lesson does not repeat the lease/thundering-herd material, which the caching-stampedes lesson covers; it links there instead."
  - claim: "TAO sample of 6.5 million requests: 99.8% reads, 0.2% writes. Replica lag under 1 s 85% of the time, under 3 s 99%, under 10 s 99.8%. Reads may be marked critical and proxied to the master region, e.g. during authentication."
    source: fb-tao
    note: "Figure 3, section 7, section 8 (replication lag) and section 6.1. Added the 6.5 million sample size and the 3 s / 99% point; lag is between regions."
  - claim: "EXPLAIN ANALYZE actually executes the query; to analyze a data-modifying query without changing tables, roll it back."
    source: pg-explain
    note: Section 14.1.2 and its caveats; the docs show BEGIN; EXPLAIN ANALYZE ...; ROLLBACK.
  - claim: "pg_stat_statements tracks planning and execution statistics (calls, total time) of all SQL statements."
    source: pg-stat
    note: "Module must be loaded (shared_preload_libraries) and created per database; lesson now says 'once enabled'."
  - claim: "Materialized views persist results in a table-like form; access is often much faster, but data is not always current. A plain refresh can block readers; CONCURRENTLY avoids that but needs a UNIQUE index on plain column names covering all rows."
    source: pg-refresh
    note: "Matview wording from pg-matview; refresh rules from pg-refresh. Added the plain-columns/all-rows condition."
  - claim: "CQRS uses a different model to update than to read; for most systems it adds risky complexity; use it only on specific parts (bounded contexts); separate models make eventual consistency likely; a ReportingDatabase is a simpler option for demanding queries."
    source: fowler-cqrs
    note: Fowler's bliki, 14 July 2011. All points stated on the page.
  - claim: "Flannel caches users, channels, bots and more at edge points of presence; startup payload on a 32K-user team fell 44 times."
    source: slack-flannel
    note: "Bing Wei, 31 May 2017. Also: 7 times smaller on a 1.5K-user team."
quiz:
  - q: Your database CPU is at 90%. What should you do first?
    options:
      - Add three read replicas.
      - Find the top queries by total time, and check their query plans.
      - Put Redis in front of every table.
    answer: 1
    why: A missing index or a bad query is the cheapest fix. Replicas and caches can come later, if the numbers still need them.
  - q: A user saves their new display name, refreshes, and sees the old one. Reads go to a replica. What is a common fix?
    options:
      - Read this user's own data from the primary for a short time after they write.
      - Make the cache TTL longer.
      - Send all users' reads to the primary forever.
    answer: 0
    why: This is a read-your-writes problem. Only the writer needs the fresh copy, so only their reads go to the primary.
  - q: One cache server is overloaded because of one very popular key. Why does adding more cache servers with the same hashing not help?
    options:
      - The key still maps to one server, so that server still gets all its reads.
      - New servers start with no memory.
      - Hashing does not work with more than ten servers.
    answer: 0
    why: Hashing spreads keys, not reads of one key. Copy the hot key to several servers, or cache it inside each app server.
  - q: What is the main cost of a materialized view?
    options:
      - It cannot use indexes.
      - Its data is only as fresh as its last refresh.
      - It slows down every write to the base tables at once.
    answer: 1
    why: PostgreSQL stores the result and updates it only when you refresh it, so reads are fast but can be out of date.
  - q: Facebook's TAO paper reports reads as 99.8% of requests. What does a number like that tell you?
    options:
      - Caching and read copies will pay off, because almost all the work is reads.
      - You should shard by write volume first.
      - You do not need a database.
    answer: 0
    why: Measure the read/write ratio first. When reads are almost all the work, read-side tools give the most benefit.
incidents: []
related: [caching-stampedes, replication, distributed-cache, sagas-outbox-cdc, search-systems, cdn-http-caching, scaling-writes]
---

## What broke

This is a real case, from a paper Facebook published in 2013. Facebook's users read an order of magnitude more content than they create. So the work was mostly reads, and Facebook put a large cache, memcache, in front of MySQL and other data stores. [@fb-memcache]

Loading one popular page fetched an average of 521 different items from that cache. Facebook ran hundreds of cache servers in one cluster. Every web server talked to every cache server, and that caused two problems. Replies that arrived all at once could overload the network switches. And one busy cache server could slow down many web servers. [@fb-memcache]

Simply buying more servers did not fix everything. The paper says popular items only become more popular as you add web servers. It also notes that a single key can be 20% of one cache server's requests. [@fb-memcache]

Then Facebook added data centres in other regions. One master region kept the other regions up to date. Those regions read from local copies of the database, which can lag behind. When lag was large, a user's next page could be missing the change they had just made. [@fb-memcache]

There was no single outage. The read path worked at one size and needed new fixes at the next.

## The idea in simple words

A read path is the route a read takes, from the user's phone to the data. Scaling reads means answering each read as close to the user and as cheaply as you can. Keep the primary database for work only it can do.

Think of it as a ladder. Climb one step at a time, only when your numbers say so. Each step adds a new way for data to be stale.

> [!key] **The key idea.** Every read you answer higher up is a read your database never sees. But every copy of data can be out of date. Choose, for each kind of read, how stale it may be.

## How it works

### Step 0: measure the read/write ratio

First, count reads and writes per second, and find the queries that use the most total time. In PostgreSQL, the `pg_stat_statements` module, once enabled, tracks planning and execution statistics for every SQL statement. These include how often each one ran and its total time. [@pg-stat]

Facebook's numbers made the choice clear. In a sample of 6.5 million requests to TAO, its graph store, 99.8% were reads and 0.2% were writes. [@fb-tao] If your mix is closer to half and half, read-side tools will help much less. See [Scaling writes](/learn/scaling-writes-and-hot-keys) instead.

### Step 1: fix the query and the index

Many "we need replicas" problems are one slow query. `EXPLAIN` shows the query plan the database chose. `EXPLAIN ANALYZE` really runs the query and shows true row counts and times. So for an `UPDATE` or `DELETE`, wrap it in a transaction and roll it back. [@pg-explain] Look for a full table scan where you expected an index.

This step is cheap, and it never shows stale data. Do it first.

### Step 2: cache

Keep hot results in memory. The common pattern is cache-aside: read the cache, and on a miss read the database and fill the cache. On a write, Facebook deletes the cached key instead of updating it, because deletes are idempotent. [@fb-memcache] The [caching lesson](/learn/caching-and-cache-stampedes) covers TTLs, invalidation and stampedes.

### Step 3: read replicas

Send reads to copies of the database, and writes to the primary. This adds read capacity, but replicas lag. TAO measured the lag of its replica databases in other regions. It was under 1 second 85% of the time, under 3 seconds 99% of the time, and under 10 seconds 99.8% of the time. [@fb-tao] So plan for seconds, not zero. The [replication lesson](/learn/database-replication) explains lag in depth.

The bug users notice is read-your-writes: "I saved it, and it is gone." Facebook's fix was a "remote marker". When a web server in a non-master region writes, it first sets a marker for that key. On a cache miss, if the marker exists, the read skips the local replica and goes to the master region. [@fb-memcache] TAO has a similar idea: a read can be marked "critical" and sent to the master region. The paper's example is a login check, so old credentials are not used. [@fb-tao]

Our advice: most reads go to replicas. A user's own data, just after they write it, goes to the primary.

Step through one read falling through the layers. Use Next and Back, or the ← and → keys.

[[diagram:scaling-reads-layers]]

### Step 4: denormalise and precompute

**Denormalise.** Store a copy of data where it is read. For example, keep `comment_count` on the post row, instead of counting comments on every page view. You now have two places to update.

**Materialized views.** PostgreSQL can store the result of a query in a table-like form. Reads are often much faster than querying the base tables. The cost: the data is not always current. [@pg-matview] You update it with `REFRESH MATERIALIZED VIEW`. A plain refresh can block readers. With `CONCURRENTLY`, readers are not blocked. But the view needs a unique index on plain columns that covers all rows. [@pg-refresh]

### Step 5: read models fed by change data capture

When one database cannot serve every kind of read, build separate read models. This is the idea behind CQRS: one model for updates and a different model for reads, maybe in a different database. [@fowler-cqrs] In this lesson, a feed table, a search index and a cache can all count as read models.

Feed them from the database's change log, with change data capture (CDC). The [sagas, outbox and CDC lesson](/learn/sagas-outbox-and-cdc) shows how. Facebook used a similar idea to keep its caches correct. Daemons read the SQL that each database committed and sent the cache deletes to every frontend cluster in the region. Lost deletes could be replayed from that reliable log. [@fb-memcache]

Be careful. Fowler warns that for most systems CQRS adds risky complexity, and should be used only on parts of a system. Separate models also make eventual consistency likely. [@fowler-cqrs] For a few heavy queries, he suggests a simpler option: a separate reporting database. [@fowler-cqrs]

### Step 6: search indexes for complex reads

Send full-text search and complex filters to a search index: another read model, with its own lag. See [Search systems](/learn/search-and-inverted-indexes).

### Step 7: CDN and edge caches for public reads

Reads that are the same for everyone, like product pages or images, can be answered by a CDN near the user. See [CDNs and HTTP caching](/learn/cdn-and-http-caching).

Slack's Flannel caches data about users, channels and more at its edge locations, so clients load it only when needed. For a team of 32,000 users, the startup data clients needed became 44 times smaller. [@slack-flannel]

### Hot keys

A hot key is one key that gets a huge share of reads. Hashing spreads keys, not reads of one key, so more servers do not help.

Facebook copies a small, very busy set of keys to several servers in a pool. Its example: one server holds 100 keys and answers 500,000 requests a second. Each request needs all 100 keys, and 1 million requests arrive each second. Split the keys over two servers, and each server still gets 1 million requests. Copy all keys to both, and each gets 500,000. The cost: every delete must reach every copy. [@fb-memcache]

Other ways to spread a hot key, from our experience:

- Cache it inside each app server for a few seconds.
- Split it into copies like `post:42#1` … `post:42#8`, and read one at random.
- Collapse many misses for one key into one database read.

## Trade-offs

| Step | Read capacity gained | Staleness | Cost and new risks |
|---|---|---|---|
| Query and index tuning | Often large, for the queries you fix. | None. | Indexes slow writes a little and use disk. |
| Cache | Very large for hot data. | Until the TTL or delete arrives. | Invalidation bugs, stampedes, hot keys. |
| Read replicas | Roughly one database per replica. | Replication lag, sometimes several seconds. | Routing logic for read-your-writes. |
| Denormalise or materialized view | Large for joins and counts. | Until you update or refresh. | Two copies to keep in step. |
| Read model fed by CDC | Large, and each read shape gets its own store. | Pipeline lag. | A new pipeline to run, watch and replay. |
| Search index | Makes complex queries possible. | Indexing lag. | Another system and its own operations. |
| CDN | Huge for public reads. | Until the cache rules say it expires. | Only safe for data that is the same for many users. |

**Our simple rule:** climb the ladder in order, and stop at the first step that meets your target.

## At work: how a senior uses this

- Start with numbers: reads per second, writes per second, and the top ten queries by total time.
- For each kind of read, ask: **"How stale may this be?"** Write the answer in the design doc. A bank balance and a like count get different answers.
- Decide who reads from the primary. Make it a rule in one place in the code, not a choice in each query.
- Watch replica lag and alert on it. Reads that "sometimes show old data" are often lag.
- Before a launch or a sale, ask which item everyone will open at once.

## With AI

### What to ask the agent

> "Here is the output of pg_stat_statements for our top 20 queries, and our schema. For each query, say if an index would help, and write the CREATE INDEX statement. Do not suggest caching yet."

> "Add read routing to this data layer. Reads go to a replica by default. For 10 seconds after a user writes, that user's reads go to the primary. Show where the 10 seconds is set."

> "Design a materialized view for this dashboard query. Add the unique index it needs for REFRESH CONCURRENTLY, and a job that refreshes it every 5 minutes."

### What to check yourself

- The new index matches the real WHERE and ORDER BY, and `EXPLAIN` shows the plan uses it.
- The agent did not run `EXPLAIN ANALYZE` on an UPDATE or DELETE outside a rolled-back transaction.
- The read-your-writes window is longer than your real replica lag at p99, not your average.
- Every cache or read model has a way to be rebuilt, and someone owns it.
- Hot keys are spread, and the agent did not add a cache that all servers miss at once.

## Check yourself

## Go deeper

Read the [TAO case](/cases#meta-tao) and the [Slack Flannel case](/cases#slack-flannel-edge-cache). Related lessons: [Distributed caches](/learn/distributed-caches), [Replication](/learn/database-replication) and [Scaling from zero to millions](/learn/scaling-from-zero-to-millions).
