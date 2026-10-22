---
topic: choosing-database
subtitle: Pick a database from how you read and write your data, not from what is popular. Then count what it costs to run.
status: published
reviewed_by: ai-factcheck
reviewed_on: 2026-10-22
illustrative_story: false
sources:
  - id: discord-2017
    title: "Stanislav Vishnevskiy, “How Discord Stores Billions of Messages”, Discord blog (2017)"
    url: https://discord.com/blog/how-discord-stores-billions-of-messages
    note: Why one MongoDB replica set stopped fitting, the access patterns Discord measured, its list of requirements, and why it chose Cassandra.
  - id: discord-2023
    title: "Discord Engineering, “How Discord Stores Trillions of Messages” (2023)"
    url: https://discord.com/blog/how-discord-stores-trillions-of-messages
    note: What the Cassandra choice cost five years later, and the move to 72 ScyllaDB nodes.
  - id: aws-choose
    title: "AWS, “Choosing an AWS database service” (decision guide)"
    url: https://docs.aws.amazon.com/decision-guides/latest/databases-on-aws-how-to-choose/databases-on-aws-how-to-choose.html
    note: Database families with use cases, the criteria to compare, self-hosted versus managed, and a mix of databases for one shop.
  - id: db-engines
    title: "DB-Engines, “Ranking per database model category”"
    url: https://db-engines.com/en/ranking_categories
    note: The list of database models that DB-Engines uses to classify hundreds of systems.
  - id: pg-about
    title: "PostgreSQL: About"
    url: https://www.postgresql.org/about/
    note: What Postgres is, and its long feature list (JSON, constraints, all isolation levels, many index types).
  - id: mongo-intro
    title: "MongoDB Manual: What is MongoDB?"
    url: https://www.mongodb.com/docs/manual/introduction/
    note: Documents, collections without a fixed schema, multi-document transactions, sharding.
  - id: mongo-txn
    title: "MongoDB Manual: Transactions"
    url: https://www.mongodb.com/docs/manual/core/transactions/
    note: The warning that distributed transactions cost more than single-document writes and do not replace good schema design.
  - id: cassandra
    title: "Apache Cassandra documentation: Overview"
    url: https://cassandra.apache.org/doc/latest/cassandra/architecture/overview.html
    note: Partitioned wide-column storage, queries that need the partition key, and what Cassandra does not support.
  - id: dynamodb
    title: "Amazon DynamoDB Developer Guide: What is Amazon DynamoDB?"
    url: https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/Introduction.html
    note: A fully managed NoSQL database, its use cases, and ACID transactions across tables.
  - id: redis
    title: "Redis docs: Get started with Redis Open Source"
    url: https://redis.io/docs/latest/get-started/
    note: An in-memory data store used as a cache, document and vector database, streaming engine and message broker.
  - id: redis-persist
    title: "Redis docs: Redis persistence"
    url: https://redis.io/docs/latest/operate/oss_and_stack/management/persistence/
    note: RDB snapshots and the append-only file, and how much data each can lose.
  - id: es-store
    title: "Elastic docs: The Elasticsearch data store"
    url: https://www.elastic.co/docs/manage-data/data-store
    note: A distributed search and analytics engine built on Apache Lucene that stores JSON documents in indices.
  - id: es-nrt
    title: "Elastic docs: Near real-time search"
    url: https://www.elastic.co/docs/manage-data/data-store/near-real-time-search
    note: A new document becomes searchable within about 1 second.
  - id: neo4j
    title: "Neo4j docs: What is a graph database"
    url: https://neo4j.com/docs/getting-started/graph-database/
    note: Nodes, relationships and properties, and why graph queries do not need joins.
  - id: timescale
    title: "Tiger Data (TimescaleDB) docs: Understand hypertables"
    url: https://www.tigerdata.com/docs/use-timescale/latest/hypertables
    note: Postgres tables that are split into chunks by time automatically.
  - id: influx
    title: "InfluxDB 3 Core documentation"
    url: https://docs.influxdata.com/influxdb3/core/
    note: A database built for event and time-series data, with its common use cases.
  - id: pgvector
    title: "pgvector (GitHub README)"
    url: https://github.com/pgvector/pgvector
    note: Vector similarity search inside Postgres, next to the rest of your data.
  - id: zerodha
    title: "Kailash Nadh, “Scaling with common sense”, Zerodha tech blog"
    url: https://zerodha.tech/blog/scaling-with-common-sense/
    note: Zerodha's CTO (2020) on why a relational database works, almost always.
  - id: mckinley
    title: "Dan McKinley, “Choose Boring Technology”"
    url: https://boringtechnology.club/
    note: Innovation tokens, unknown unknowns, and the hidden running cost of each new database.
  - id: fowler
    title: "Martin Fowler, “Polyglot Persistence” (2011)"
    url: https://martinfowler.com/bliki/PolyglotPersistence.html
    note: The idea of several data stores per system, and its cost in complexity.
claims_checked:
  - claim: Discord's first version (early 2015) stored all messages in one MongoDB replica set on purpose; around November 2015, at 100 million messages, data and index no longer fit in RAM and latency became unpredictable.
    source: discord-2017
    note: Matches the 2017 post ("this was intentional", "could no longer fit in RAM and latencies started to become unpredictable").
  - claim: Discord measured very random reads and a read/write ratio of about 50/50, listed its requirements, and found Cassandra was the only database that met them all; Cassandra trades strong consistency for availability.
    source: discord-2017
    note: Quoted closely. The post says the trade "is something we wanted", so the lesson no longer calls it a cost.
  - claim: 12 Cassandra nodes in 2017, 177 by early 2022; hot partitions, compactions and GC pauses caused latency spikes; moved in May 2022 to 72 ScyllaDB nodes; p99 historical read latency fell from 40-125 ms to 15 ms.
    source: discord-2023
    note: All numbers match the 2023 post. Compactions and the 12-node figure added; latency figure added from the post.
  - claim: DB-Engines classifies hundreds of systems by model (relational, key-value, document, wide column, search engines, time series, graph, vector and more), and some systems belong to more than one category.
    source: db-engines
    note: Page lists 447 systems (October 2026) and says "Some of the systems belong to more than one category". AWS comparison corrected - AWS has in-memory but no search engine group.
  - claim: Cassandra does not support cross-partition transactions, distributed joins or foreign keys, and all performant queries supply the partition key; it uses eventually consistent semantics.
    source: cassandra
    note: Matches the architecture overview word for word.
  - claim: MongoDB warns that a distributed transaction costs more than single-document writes and does not replace effective schema design.
    source: mongo-txn
    note: Matches the callout on the Transactions page.
  - claim: With AOF on, the default fsync every second can lose about 1 second of writes; RDB snapshots alone can lose minutes.
    source: redis-persist
    note: Matches the docs. AOF is not on by default, so the lesson now says "if you turn on".
  - claim: Elasticsearch makes a new document searchable within about 1 second.
    source: es-nrt
    note: Docs say "within 1 second" (near real-time, default refresh interval).
  - claim: AWS suggests key-value for session stores and shopping carts, says self-hosting means doing maintenance, monitoring and patching, advises Postgres teams to first evaluate pgvector, and gives the DocumentDB + DynamoDB + Aurora ecommerce mix.
    source: aws-choose
    note: All four found in the decision guide (updated June 2026).
  - claim: A Postgres node with a replica, partitions and indexes can hold hundreds of billions of records and terabytes of data.
    source: zerodha
    note: Quote from section 6 of the 2020 post by Kailash Nadh. The post does not call Zerodha "a large stock broker", so the lesson now says "trading platform".
  - claim: New technology brings many more unknown unknowns; a company gets maybe three innovation tokens. Polyglot persistence costs complexity and each store is a new interface to learn.
    source: mckinley
    note: Both checked; Fowler's points checked against fowler (16 November 2011).
quiz:
  - q: You are starting a new shop app with users, orders and payments. Nobody has measured any load yet. What is the safest first choice?
    options:
      - A key-value store, so it can grow to any size.
      - One relational database such as Postgres.
      - Five different databases, one for each kind of data.
    answer: 1
    why: Orders and payments link to each other and need transactions. A relational database handles both, and you can add a store later for a proven need.
  - q: What made Discord leave its single MongoDB replica set?
    options:
      - MongoDB had no way to store text.
      - Around 100 million messages, the data and index no longer fit in RAM, and latency became unpredictable.
      - The team wanted SQL joins.
    answer: 1
    why: Discord's 2017 post says exactly this, around November 2015.
  - q: In Cassandra, which query is fast?
    options:
      - One that gives the partition key.
      - A join across two tables.
      - A transaction across many partitions.
    answer: 0
    why: The Cassandra docs say all performant queries supply the partition key. Cross-partition transactions and distributed joins are not supported.
  - q: Your Postgres app needs "find similar products" with vector embeddings. What should you check first?
    options:
      - Whether the pgvector extension in your current Postgres is enough.
      - Which new vector database is the most popular this year.
      - Whether you can move all data to a graph database.
    answer: 0
    why: AWS suggests teams that already run Postgres start by trying pgvector. It keeps vectors next to the rest of the data.
incidents: []
cases: [discord-trillions-messages]
related: [data-models, nosql-modelling, postgres, btree-lsm, transactions-isolation, search-systems, time-series-stores, vector-search]
---

## What broke

Discord built the first version of its app in early 2015. Every chat message lived in one MongoDB replica set. This was on purpose, and the team planned a path to move later. [@discord-2017]

Around November 2015, they had 100 million stored messages. Then the data and the index no longer fit in RAM. Latency became unpredictable. [@discord-2017]

Before they picked a new database, they studied how the app really used messages. Reads were very random, and reads and writes were about 50/50. They also wrote down what they needed: linear scaling, automatic failover, low maintenance, predictable speed, a proven product, and open source. [@discord-2017]

Cassandra was the only database that met all of these needs. It also came with a trade-off. It gives up strong consistency to stay available. Discord wanted that, and accepted eventual consistency. [@discord-2017]

In 2017 the cluster had 12 Cassandra nodes. By early 2022 it had 177. Hot partitions, compaction backlogs and garbage collection pauses caused latency spikes. [@discord-2023]

In May 2022, Discord moved to 72 nodes of ScyllaDB, a Cassandra-compatible database. The p99 latency to read old messages fell from 40–125 ms to 15 ms. [@discord-2023]

So a choice is not "right forever". It fits a set of access patterns at one size. In this lesson you will learn to ask the questions Discord asked, before your data outgrows your database.

## The idea in simple words

There is no best database. There are databases that fit some ways of reading and writing data, and fit others badly.

So start from your **access patterns**: which questions the app asks, how often, and how fast the answer must be. Then add your requirements: transactions, consistency, size, and who will run it at 3 a.m.

> [!key] **The key idea.** First list how you will read and write the data. Then pick the simplest database that serves those patterns, and that your team can run. Popularity is not a requirement.

DB-Engines sorts hundreds of systems by data model. Its main groups include relational, key-value, document, wide column, search engines, time series, graph and vector. Some systems belong to more than one group. [@db-engines] The AWS decision guide uses a similar list. It adds in-memory as a group and has no search engine group. [@aws-choose]

## How it works

Step through the questions. Each "yes" points to a family. Use Next and Back, or the ← and → keys.

[[diagram:db-choice-path]]

### The main families, as their own docs describe them

- **Relational** (Postgres, MySQL). Tables, joins, constraints and ACID transactions. Postgres also stores JSON and supports every SQL isolation level. [@pg-about]
- **Key-value** (DynamoDB, Redis). Get and put by key. AWS suggests it for session stores and shopping carts. [@aws-choose] DynamoDB is fully managed and also has ACID transactions across tables. [@dynamodb] Redis keeps data in memory. [@redis]
- **Document** (MongoDB). JSON-like documents with nested fields, in collections without a fixed schema. [@mongo-intro]
- **Wide-column** (Cassandra). Tables split into partitions across many nodes, with eventual consistency. [@cassandra]
- **Search engine** (Elasticsearch). Built on Apache Lucene, it stores JSON documents in indices for search and analytics. [@es-store]
- **Time-series** (TimescaleDB, InfluxDB). TimescaleDB splits Postgres tables into chunks by time. [@timescale] InfluxDB is built for event and time-series data, like server and sensor monitoring. [@influx]
- **Graph** (Neo4j). Nodes and relationships. The links are stored with the data, so queries follow them without joins. [@neo4j]
- **Vector** (pgvector). Finds the nearest vectors, for example similar texts. pgvector does this inside Postgres, next to your other data. [@pgvector]

### What each one is bad at

Each family pays for its strength somewhere.

Cassandra chose not to support transactions across partitions, distributed joins or foreign keys. Fast queries must give the partition key. [@cassandra] So you design one table per query, and changing the questions later is hard.

MongoDB has multi-document transactions. Its docs warn that a distributed transaction costs more than a single-document write, and that it does not replace a good schema. [@mongo-txn]

Redis keeps data in memory. If you turn on its append-only file, the default policy syncs to disk once per second. A crash can then lose about 1 second of writes. With snapshots only, you can lose minutes. [@redis-persist] That may be fine for a cache, but usually not for money.

Elasticsearch makes a new document searchable within about 1 second, not at once. [@es-nrt] So keep the real data in your main database, and copy it into the search index.

### Transactions and consistency

Ask: must several records change together, or not at all? Money, stock and bookings usually need this. Relational databases are built for it: Postgres has been ACID-compliant since 2001. [@pg-about] If you pick a store with weaker transactions, your app code must handle the gaps. The [transactions lesson](/learn/transactions-and-isolation-levels) and the [CAP lesson](/learn/cap-pacelc-consistency-models) explain what you give up.

### Scale

Write numbers, not adjectives. How many rows in two years? How many reads and writes per second at peak?

Kailash Nadh, the CTO of the Indian trading platform Zerodha, makes this point. A Postgres node with a replica, good partitions and indexes can hold hundreds of billions of records and terabytes of data. [@zerodha] He argues that you often do not need a distributed database. [@zerodha] This lesson agrees: most apps fit one Postgres for a long time. When they do not, [replication](/learn/database-replication) and [sharding](/learn/sharding-and-consistent-hashing) come next.

### Running cost and team skills

Someone must back it up, upgrade it, watch it and fix it at night. If you host it yourself, your team does the maintenance, monitoring and patching. A managed service takes most of that work. [@aws-choose] It usually costs more per hour, so compare that with the engineer time it saves.

Skills matter too. AWS suggests that a team already running Postgres should first try pgvector before a new vector store. [@aws-choose] Dan McKinley says a new technology brings more "unknown unknowns". He suggests a young company gets only a few "innovation tokens", maybe three, to spend on new tools. [@mckinley]

### Polyglot persistence

Using several stores in one system is called **polyglot persistence**. AWS gives a shop example: a document store for the catalogue, DynamoDB for fast browsing, and Aurora for orders and stock. [@aws-choose]

Martin Fowler wrote about it in 2011. He warns that it costs complexity. Each new store is a new interface to learn and to tune. [@fowler] You also have to keep the copies in sync.

## Trade-offs

The table sums up the sources above. The "Bad at" column is partly this lesson's own advice, so test it against your own case.

| Family | Good at | Bad at | Typical use |
|---|---|---|---|
| Relational | Joins, constraints, ACID transactions, new queries later. | Writes beyond one big node need sharding. | Users, orders, payments. Most apps. |
| Key-value | Very fast get and put by key at huge scale. | Queries not by key need extra indexes. No joins. | Sessions, carts, cache. |
| Document | Nested records that match your code. | Data with many links between records. Multi-document transactions cost more. | Catalogues, profiles. |
| Wide-column | Heavy writes spread over many nodes. | Joins, cross-partition transactions, new query shapes. | Messages, events at very large scale. |
| Search engine | Ranked full-text search, typos, filters. | Being the main copy of the data. New data is not searchable at once. | Product and log search. |
| Time-series | Cheap writes and time-range queries. | Data that is not mostly timestamped points. | Metrics, sensors, prices. |
| Graph | Following many hops of links. | Simple lookups any database can do. One more system to run. | Fraud rings, recommendations. |
| Vector | "Find similar" by embedding. | Being the only store. You still need the source data. | Semantic search, RAG. |

## At work: how a senior uses this

- Write the access patterns first: the top 10 queries, read/write ratio, peak rate, and data size in two years.
- Mark which data needs transactions. Money and stock usually do.
- Start with the database the team already runs well. Ask "what can't it do?" and prove it with a test.
- Prefer a feature of the current database (JSON column, pgvector, TimescaleDB) before a new system.
- For each new store, name the owner, the backup plan, the on-call runbook and the monthly cost.
- Choose managed unless you have a clear reason and the people to self-host.
- Plan how data gets into a second store, for example with change data capture, and how you rebuild it.
- Write the decision down in a short record with the patterns, options, and the reason.

**Checklist before you pick:**

1. Access patterns listed?
2. Transactions needed where?
3. Size and rate in two years, as numbers?
4. Can the current database do it?
5. Who runs it, and is it managed?
6. How do you leave it later?

## With AI

### What to ask the agent

> "Here are our 10 most common queries, our read/write ratio and our data size. For each database family, say which queries it serves well and which it serves badly. Do not recommend a product until the end."

> "We run Postgres. We need vector search for 2 million documents. Compare pgvector with a separate vector store, including backups, sync and on-call work."

> "List the operational tasks our team takes on if we self-host Cassandra instead of using a managed service. Link each task to the official docs."

### What to check yourself

- The agent's family descriptions match the official docs, not a blog post or its memory.
- Every claim like "scales to millions" names a source or comes from your own load test.
- The comparison includes running cost: backups, upgrades, monitoring and on-call.
- The answer says how data stays in sync between stores.
- The agent did not pick the newest tool just because it is new.

## Check yourself

## Go deeper

Next: [Data modelling for NoSQL](/learn/data-modelling-for-nosql) shows how to design tables around access patterns. For how storage engines pay for writes, see [B-tree vs LSM-tree](/learn/btree-vs-lsm-tree). For search, see [Search and inverted indexes](/learn/search-and-inverted-indexes).
