---
topic: data-models
subtitle: Relational, document and graph models, and how the shape you pick makes some queries easy and others painful.
status: published
reviewed_by: ai-factcheck
reviewed_on: 2026-11-07
illustrative_story: true
sources:
  - id: ddia
    title: "Martin Kleppmann, Designing Data-Intensive Applications, O'Reilly, 2017 (1st edition)"
    where: "Chapter 2, “Data Models and Query Languages”"
    note: The classic book-length comparison of relational, document and graph models, for further reading.
  - id: kleppmann-db
    title: "Martin Kleppmann, Databases (University of Cambridge lecture notes, 2026/27)"
    url: https://github.com/ept/databases
    note: Kleppmann's own notes on relational, document and graph models, schema-on-read and query optimisers.
  - id: pg-planner
    title: "PostgreSQL documentation, section 51.5: Planner/Optimizer"
    url: https://www.postgresql.org/docs/current/planner-optimizer.html
    note: How the planner compares possible plans and join orders and picks the cheapest.
  - id: pg-fk
    title: "PostgreSQL documentation, section 5.5: Constraints (Foreign Keys)"
    url: https://www.postgresql.org/docs/current/ddl-constraints.html
    note: Foreign keys, referential integrity, the products and orders many-to-many example, and ON DELETE rules.
  - id: pg-json
    title: "PostgreSQL documentation, section 8.14: JSON Types"
    url: https://www.postgresql.org/docs/current/datatype-json.html
    note: json versus jsonb, containment queries, GIN indexes, and advice on document size and structure.
  - id: pg-with
    title: "PostgreSQL documentation, section 7.8: WITH Queries (Common Table Expressions)"
    url: https://www.postgresql.org/docs/current/queries-with.html
    note: WITH RECURSIVE, how it runs step by step, and how to stop it looping on cycles in a graph.
  - id: mongo-model
    title: "MongoDB Manual: Data Modeling"
    url: https://www.mongodb.com/docs/manual/data-modeling/
    note: Flexible schema, and the rule that data accessed together should be stored together.
  - id: mongo-embed
    title: "MongoDB Manual: Embedded Data Versus References"
    url: https://www.mongodb.com/docs/manual/data-modeling/concepts/embedding-vs-references/
    note: When to embed and when to reference, the cost of duplicated data, and single-document atomic writes.
  - id: mongo-lookup
    title: "MongoDB Manual: $lookup (aggregation stage)"
    url: https://www.mongodb.com/docs/manual/reference/operator/aggregation/lookup/
    note: The left outer join stage, and why it needs an index on the joined field.
  - id: mongo-versioning
    title: "MongoDB Manual: Schema Versioning Pattern"
    url: https://www.mongodb.com/docs/manual/data-modeling/design-patterns/data-versioning/schema-versioning/
    note: Old and new document shapes in one collection, and queries that must handle both.
  - id: mongo-validate
    title: "MongoDB Manual: Schema Validation"
    url: https://www.mongodb.com/docs/manual/core/schema-validation/
    note: Rules for field types and ranges on a flexible-schema collection, and what happens to invalid writes.
  - id: cypher-intro
    title: "Neo4j Cypher Manual: Overview"
    url: https://neo4j.com/docs/cypher-manual/current/introduction/cypher-overview/
    note: Cypher as a declarative graph query language, and its ASCII-art pattern syntax.
  - id: cypher-paths
    title: "Neo4j Cypher Manual: Variable-length patterns"
    url: https://neo4j.com/docs/cypher-manual/current/patterns/variable-length-patterns/
    note: Quantified relationships such as -[:NEXT]->{1,3} for paths of one to three hops.
claims_checked:
  - claim: MongoDB advises embedding for "contains" relationships read together, and references when embedded data grows without bounds or the child can exist without a parent; writes are atomic per single document.
    source: mongo-embed
    note: Matches the embedding/referencing scenario table and the atomicity section.
  - claim: $lookup performs a left outer join, and unindexed $lookup operations on large collections can be slow.
    source: mongo-lookup
    note: '"Performs a left outer join to a collection in the same database"; "Unindexed ... $lookup operations on large collections can slow down query performance."'
  - claim: A foreign key requires values to match a row in another table; order_items links products and orders; ON DELETE CASCADE deletes referencing rows; Postgres does not index referencing columns automatically.
    source: pg-fk
    note: Matches section 5.5.5, including the order_items example and the indexing note.
  - claim: WITH RECURSIVE repeats until the working table is empty, can loop forever on cycles, and is stopped by tracking the path or the CYCLE clause.
    source: pg-with
    note: Matches 7.8.2 and 7.8.2.2 ("This query will loop if the link relationships contain cycles").
  - claim: Schema-on-write means the database checks every write; "schemaless" documents still have an implicit schema checked on read; declarative queries let the optimiser choose plans, which can also change for the worse as data shifts.
    source: kleppmann-db
    note: databases.tex lines 595-598, 624-636 and 2730-2735 (repo checked 2026-11-07). DDIA 1st ed. ch. 2 title corroborated only by the author's ddia-references repo, not a published TOC.
  - claim: MongoDB schema validation rejects invalid inserts and updates by default and is meant for an established schema.
    source: mongo-validate
    note: '"By default, MongoDB rejects any insert or update operation that would produce an invalid document."'
quiz:
  - q: Your app shows a blog post with its 20 comments on one screen. Comments are never shown anywhere else. Which model fits this best?
    options: ["Embed the comments inside the post document", "A graph with a node per comment", "Store each comment in its own collection and join on every read"]
    answer: 0
    why: The post "contains" the comments and they are read together, which is the case MongoDB's docs give for embedding.
  - q: An event document embeds the name of every attendee. A user with tickets to 3 events changes their name. What is the main risk?
    options: ["The document becomes too small", "One of the 3 copies is not updated, so the data disagrees", "The database rejects the rename"]
    answer: 1
    why: Denormalised copies must all be updated. If one write fails, the copies disagree.
  - q: What does "schema-on-read" mean?
    options: ["The database checks every write against a fixed schema", "The code that reads the data decides its structure, and must handle old shapes", "There is no structure in the data at all"]
    answer: 1
    why: The structure is still there, but it is implied. The reader interprets it, so old and new shapes must both be handled.
  - q: You need "all people within 4 hops of this account" for a fraud check. Which query is the most natural fit?
    options: ["A Cypher pattern with a quantified relationship like {1,4}", "One SQL join with no recursion", "A single document lookup by id"]
    answer: 0
    why: A graph query names a variable number of hops directly. In SQL you need WITH RECURSIVE or several self-joins.
  - q: Why is a declarative query like SQL or Cypher easier for the database to speed up than a hand-written loop?
    options: ["It always uses less memory", "It says what result you want, so the database can choose how to get it", "It cannot use indexes"]
    answer: 1
    why: A declarative query states the result, not the steps. The planner is free to pick indexes and join order.
incidents: []
glossary_optout: [embedding, cell]
related: [choosing-database, nosql-modelling, catalog-modelling, schema-migrations, transactions-isolation]
---

## What broke

This story is made up, but it is a common one.

A small team builds an events app on MongoDB. Each event is one document. Inside it is an array of attendees: user id, name and photo URL. The event page loads with one read. It is fast and the code is simple.

Six months later, three requests arrive in the same sprint.

First, a user changes her name. Her old name still shows on 40 past events. Someone has to write a script that finds and updates every copy.

Second, a big conference sells 30,000 tickets. Its document keeps growing. Every read of the event page now loads all 30,000 attendees.

Third, product wants a page: "events your friends are going to". Users and events now link both ways, and the app has no easy way to ask that question. The team writes code that loads friends, then events, then filters in memory.

Nothing was wrong with MongoDB. The data had changed shape. It started as a tree, one event with its attendees. It became a web of users, events and friendships. The model had not changed with it.

## The idea in simple words

A **data model** is the shape you give your data: tables, documents or a graph. A **query language** is how you ask the data questions.

Each model makes some questions cheap and others expensive. [@kleppmann-db]

- The **relational model** stores rows in tables. Tables point to each other with ids. You combine them with a **join** when you read.
- The **document model** stores a whole record, with its nested parts, as one JSON-like document. MongoDB is a common example. [@mongo-model]
- The **graph model** stores nodes and the links between them. Neo4j is a common example, and Cypher is its query language. [@cypher-intro]

> [!key] **The key idea.** Look at the relationships in your data. If records mostly contain their parts, a document fits. If many records point to many others, tables or a graph fit better. The model decides which queries are easy.

To pick a product, read [Choosing a database](/learn/choosing-a-database). This lesson is about the shape of the data, which comes first.

## How it works

Step through the diagram. It shows one rename in two models. Use Next and Back, or the ← and → keys.

[[diagram:data-models-rename]]

### Normalise or embed

**Normalisation** means each fact is stored in one place. Other rows point to it by id. A rename is one write. The cost moves to reads: you join tables to show a page.

**Embedding** puts related data inside the parent document. MongoDB's guide says to embed when one thing "contains" another and when you read them together. [@mongo-embed] It says to use references when the child list can grow without limit, or when the child can exist on its own. [@mongo-embed]

Copying data to avoid joins is called **denormalisation**. MongoDB says duplicates can speed up reads. It also warns that if a copied field is updated often, a reference may be the better choice. [@mongo-embed]

### Many-to-many links

One user goes to many events. One event has many users. This is a **many-to-many** relationship.

In SQL you store it as a third table. The PostgreSQL docs show `order_items`, which links products and orders. It has two **foreign keys**, one to each table. [@pg-fk] A foreign key means a value must match a row in the other table. The database rejects a ticket for a user that does not exist. [@pg-fk] You can also choose what happens on delete. `CASCADE` removes the linked rows too. [@pg-fk]

In MongoDB you store ids and join with `$lookup`, a left outer join. [@mongo-lookup] Its docs warn that it can be slow on large collections without an index on the joined field. [@mongo-lookup]

### Schema-on-write and schema-on-read

In Postgres, a table has a fixed schema. The database checks every write against it. This is **schema-on-write**.

A MongoDB collection does not require all documents to have the same fields. [@mongo-model] The structure still exists, but your reading code decides what it means. This is **schema-on-read**. [@kleppmann-db] You can add a field without a migration. But your code must handle old documents that lack it. [@mongo-versioning]

The line is not sharp. MongoDB has schema validation: rules for field types and ranges. By default, it rejects writes that break them. [@mongo-validate] Postgres has `jsonb` columns for flexible parts. Its docs still advise a "somewhat fixed structure" for JSON documents. [@pg-json]

### Declarative and imperative queries

An **imperative** query is a loop you write: fetch users, then for each one fetch events. You choose the order and the method.

A **declarative query** says what result you want. SQL and Cypher both work this way. [@cypher-intro] [@kleppmann-db] The database's planner compares possible plans and join orders, and picks the cheapest. [@pg-planner] When the data changes, it can pick a new plan with no change to your code. [@kleppmann-db]

Usually the new plan is better, but not always. A planner can suddenly switch to a slower plan as the data shifts. [@kleppmann-db] So watch the plans of your most important queries.

### Graphs and many hops

Cypher draws the pattern you want. `(a:User)-[:FRIEND]->(b:User)` reads like a picture of two nodes and an arrow. [@cypher-intro] A quantified relationship like `-[:FRIEND]->{1,3}` matches paths of one to three hops. [@cypher-paths]

SQL can do this too, with `WITH RECURSIVE`. The query repeats a step until it finds no new rows. [@pg-with] The PostgreSQL docs warn that it can loop forever if the links contain a cycle. They show how to track the path, or use the `CYCLE` clause, to stop it. [@pg-with] So deep, variable hops are possible in SQL, but they are easier to write in a graph language.

## Trade-offs

The cells sum up the sources. Some cells are this lesson's own advice, so test them on your own data.

| Option | Rename or update shared data | Read one record with its parts | Many-to-many and many hops | Schema |
|---|---|---|---|---|
| Relational, normalised | Yes. One write. | No. Needs joins. | Yes. Join tables; `WITH RECURSIVE` for hops. [@pg-with] | On write, checked by the database. |
| Document, embedded | No. Every copy must change. [@mongo-embed] | Yes. One read, one atomic write. [@mongo-embed] | No. `$lookup` or code; slow without an index. [@mongo-lookup] | On read by default; validation is optional. [@mongo-validate] |
| Document, with references | Yes. One write. | No. Needs `$lookup` or several reads. | Partly. You search by ids in a way that is close to relational. [@kleppmann-db] | Same as above. |
| Graph | Yes. One node. | Partly. Fine for small shapes. | Yes. Hops are part of the language. [@cypher-paths] | Usually flexible. |
| Postgres with a `jsonb` column | Yes, for the table parts. | Yes, for the JSON parts. | Yes, for the table parts. | Mixed. Keep JSON documents small. [@pg-json] |

**Simple rule:** embed what is read together and owned by one parent. [@mongo-model] Put anything shared by many parents in its own table or collection.

## At work: how a senior uses this

- Start from the access patterns you listed in [Choosing a database](/learn/choosing-a-database). For each one, ask: **"Which data is read together?"**
- For each relationship, write "one-to-one", "one-to-many" or "many-to-many". Many-to-many is the warning sign for heavy embedding.
- Ask: **"Can this list grow without a limit?"** If yes, do not embed it. MongoDB says the same. [@mongo-embed]
- When you copy data for speed, write down who updates each copy, and how you find copies that disagree.
- Use a `jsonb` column for truly varied fields, like product attributes per category. Keep ids, money and status as real columns.
- Add schema validation to a document collection once its shape settles. [@mongo-validate]
- Add a graph database only when hop queries are a core feature. Try `WITH RECURSIVE` first.
- Read the query plan. A declarative query is fast only if the planner finds a good plan, which usually needs the right index. [@pg-planner]

## With AI

### What to ask the agent

> "Here are our entities and the screens that read them. Draw a relational schema and a MongoDB document schema for them. For each query, say how many reads or joins it needs in each design."

> "List every place this MongoDB schema copies the same data. For each copy, show the code that must change when the source changes."

> "Rewrite this loop that fetches users, then their orders, as one SQL query. Then run EXPLAIN and tell me which indexes it uses."

### What to check yourself

- The agent marked each relationship correctly. Many-to-many is easy to miss.
- Embedded arrays have a clear upper limit, such as "at most 50 addresses".
- Foreign key columns you join or delete on have an index. Postgres does not create it for you. [@pg-fk]
- The agent did not put money, ids or status inside a JSON blob without a reason.
- A recursive query has a stop rule, so a cycle cannot loop forever. [@pg-with]
- Each copied field has a named owner and an update path.

## Check yourself

## Go deeper

- [Choosing a database](/learn/choosing-a-database): pick a product from your access patterns, once you know your data's shape.
- [Data modelling for NoSQL](/learn/data-modelling-for-nosql): design tables around queries in DynamoDB- and Cassandra-style stores.
- [Product catalog modelling](/learn/product-catalog-modelling): attributes that differ by category, a common use for `jsonb`.
- [Schema migrations](/learn/schema-migrations-zero-downtime): how to change a schema-on-write table without downtime.
- For a longer comparison of the three models, read chapter 2 of *Designing Data-Intensive Applications* (1st edition). [@ddia]
