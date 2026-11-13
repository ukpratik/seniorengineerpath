---
topic: nosql-modelling
subtitle: In DynamoDB and Cassandra you design keys around the questions your app asks. List them first, then shape the data so each one is a single cheap read.
status: published
illustrative_story: false
sources:
  - id: discord-2017
    title: "Stanislav Vishnevskiy, “How Discord Stores Billions of Messages”, Discord blog (2017)"
    url: https://discord.com/blog/how-discord-stores-billions-of-messages
    note: The messages schema, the partitions over 100 MB, the 10-day buckets, and the tombstone problem in one channel.
  - id: ddb-nosql
    title: "Amazon DynamoDB Developer Guide: NoSQL design for DynamoDB"
    url: https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/bp-general-nosql-design.html
    note: Why you list the questions before the schema, and the principles of locality, sort order and spreading load.
  - id: ddb-foundations
    title: "Amazon DynamoDB Developer Guide: Data Modeling foundations in DynamoDB"
    url: https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/data-modeling-foundations.html
    note: AWS's own list of pros, cons and fit for single table and multiple table design.
  - id: ddb-partition-key
    title: "Amazon DynamoDB Developer Guide: Best practices for designing and using partition keys effectively in DynamoDB"
    url: https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/bp-partition-key-design.html
    note: The per-partition limit of 3,000 read units and 1,000 write units per second.
  - id: ddb-sort-keys
    title: "Amazon DynamoDB Developer Guide: Best practices for using sort keys to organize data in DynamoDB"
    url: https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/bp-sort-keys.html
    note: Range queries with begins_with and between, and composite sort keys for hierarchies.
  - id: ddb-adjacency
    title: "Amazon DynamoDB Developer Guide: Best practices for managing many-to-many relationships in DynamoDB tables"
    url: https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/bp-adjacency-graphs.html
    note: The adjacency list pattern with invoices and bills, and a GSI on the sort key for the reverse lookup.
  - id: ddb-gsi
    title: "Amazon DynamoDB Developer Guide: Using Global Secondary Indexes in DynamoDB"
    url: https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/GSI.html
    note: Why a query on a non-key attribute needs a Scan or an index, and how the index is kept in sync.
  - id: ddb-txn
    title: "Amazon DynamoDB Developer Guide: Amazon DynamoDB Transactions: How it works"
    url: https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/transaction-apis.html
    note: The limits of TransactWriteItems and what a transaction costs in capacity.
  - id: cass-intro
    title: "Apache Cassandra Documentation: Data Modeling, Introduction"
    url: https://cassandra.apache.org/doc/latest/cassandra/developing/data-modeling/intro.html
    note: Query-driven modelling, one table per query, partition and clustering keys, and partition size guidelines.
  - id: cass-refining
    title: "Apache Cassandra Documentation: Evaluating and refining data models"
    url: https://cassandra.apache.org/doc/latest/cassandra/developing/data-modeling/data-modeling_refining.html
    note: How to estimate a partition's size, and how to split a big one by adding a column to the partition key.
  - id: debrie
    title: "Alex DeBrie, “The What, Why, and When of Single-Table Design with DynamoDB” (2020)"
    url: https://www.alexdebrie.com/posts/dynamodb-single-table/
    note: The case for single-table design, its downsides, and when he would not use it.
  - id: naylor
    title: "Pete Naylor, “Single table design for DynamoDB: The reality”, Momento blog (2023)"
    url: https://www.gomomento.com/blog/single-table-design-for-dynamodb-the-reality
    note: Someone who worked with DynamoDB at Amazon argues that item collections matter, and the number of tables should be an outcome.
claims_checked:
  - claim: Discord added a time bucket to the partition key, ((channel_id, bucket), message_id), with about 10 days per bucket, expecting to keep partitions under 100 MB; its published schema uses CLUSTERING ORDER BY (message_id DESC).
    source: discord-2017
    note: Post text plus the embedded schema gist (vishnevskiy/1c82641e).
  - claim: After millions of messages were deleted in one channel, Cassandra scanned millions of tombstones with 10-second stop-the-world GC; Discord cut tombstone lifetime from 10 days to 2 (safe with nightly repairs) and skipped empty buckets.
    source: discord-2017
    note: '"The Big Surprise" section.'
  - claim: TransactWriteItems covers up to 100 distinct items and 4 MB in one account and Region, and DynamoDB does two underlying writes per item (prepare and commit).
    source: ddb-txn
    note: TransactWriteItems API and Capacity management sections.
  - claim: GSIs are updated asynchronously, usually within a fraction of a second, so queries can be stale; a GSI short of write capacity throttles table writes.
    source: ddb-gsi
    note: Data synchronization and Write capacity units sections.
  - claim: Cassandra suggests under 100,000 values and 100 MB per partition, with a hard limit of 2 billion cells.
    source: cass-intro
    note: The 2 billion cell limit is on the refining page (cass-refining).
  - claim: DeBrie's main reason for single-table design is fetching several item types "using a single request"; he would avoid it for GraphQL and for new apps that value developer agility.
    source: debrie
    note: Naylor's counterpoint (tables as an outcome; might be one, often more) checked against naylor.
reviewed_by: ai-factcheck
reviewed_on: 2026-11-13
quiz:
  - q: You are starting a new DynamoDB table. What should you write down first?
    options:
      - The entities and their relationships, fully normalised.
      - The list of questions the app will ask, with their keys and rates.
      - The number of tables you want.
    answer: 1
    why: AWS says not to design a DynamoDB schema until you know the questions it must answer. Keys come from those questions.
  - q: A chat table uses channel_id as the only partition key. One busy channel has years of messages. What is the main risk?
    options:
      - One partition grows without a limit and gets too big and too hot.
      - The sort order of messages becomes random.
      - Queries for that channel need a join.
    answer: 0
    why: Everything for one partition key lives together. Discord saw partitions over 100 MB and fixed it by adding a time bucket to the partition key.
  - q: Items have PK = MEMBER#id and SK = CLUB#id. How do you list all members of one club efficiently?
    options:
      - Scan the table with a filter on SK.
      - Add a global secondary index whose partition key is the club ID, for example the sort key swapped into the index key.
      - Use a bigger read capacity on the table.
    answer: 1
    why: The club ID sits in the sort key of many partitions. An inverted index puts all of one club's members in one index partition, so one Query finds them.
  - q: Which statement about DynamoDB transactions is true?
    options:
      - One transaction can touch any number of items in any Region.
      - One transaction can touch up to 100 distinct items in one account and Region, and each item costs two writes.
      - Transactions are free, so you should wrap every write in one.
    answer: 1
    why: TransactWriteItems allows up to 100 distinct items and 4 MB in one account and Region. DynamoDB does a prepare and a commit for each item.
  - q: What is the strongest argument for putting several entity types in one DynamoDB table?
    options:
      - Fewer tables always make every query faster.
      - Items that share a partition key can be read together with one Query.
      - DynamoDB charges per table, not per request.
    answer: 1
    why: The gain comes from the item collection. Items in the same table but different partitions cost the same to read as items in two tables.
incidents: []
glossary_optout: [inverted-index, cell]
related: [data-models, sharding, choosing-database, secondary-indexes, quorums-leaderless, dynamodb, cassandra]
---

## What broke

Discord's 2017 post tells how it moved chat messages from MongoDB to Cassandra. Every query read one channel, so `channel_id` became the partition key. Messages inside it were sorted by `message_id`, a time-ordered ID. [@discord-2017]

The first import showed a problem at once. Cassandra logged warnings about partitions over 100 MB. Under one key, a busy channel's messages would grow without end. Discord wrote that big partitions add heavy garbage collection pressure, and cannot be spread across the cluster. [@discord-2017]

So they changed the key. They added a time **bucket** to the partition key: `((channel_id, bucket), message_id)`. They looked at the largest channels and chose about 10 days per bucket. They expected that to keep partitions comfortably under 100 MB. [@discord-2017]

The model still had a sharp edge. In one public server, someone deleted millions of messages with the API. One message was left in the channel. To return it, Cassandra scanned millions of delete markers, called tombstones. [@discord-2017] The garbage collector ran 10-second stop-the-world pauses again and again. [@discord-2017]

The fix was in table settings and the read path. Discord cut the tombstone lifetime from 10 days to 2. That was safe only because they ran repairs every night. The query code now remembers empty buckets and skips them. [@discord-2017]

Nothing here was a bug in Cassandra. The key decided how big each partition could grow, and how much work one read could cause.

## The idea in simple words

In a relational database, you normalise first and write queries later. The [data models lesson](/learn/data-models-and-query-languages) covers that. DynamoDB and Cassandra flip the order.

AWS says it plainly: you should not design a DynamoDB schema "until you know the questions it will need to answer". [@ddb-nosql] Cassandra's docs say the same: "data modeling is query-driven". [@cass-intro]

The reason is the trade. These stores answer a few kinds of query very fast at any size. Other queries can be "expensive and slow". [@ddb-nosql] You choose which queries are fast when you choose the keys.

> [!key] **The key idea.** Write the list of access patterns before you draw a table. Then pick a partition key and sort key so each pattern is one request to one partition. Copy data where a pattern needs it.

## How it works

### Two keys

The primary key has two parts.

- The **partition key** is hashed to decide which partition, and so which node, stores the item. [@cass-intro] [@discord-2017] The [sharding lesson](/learn/sharding-and-consistent-hashing) explains how the hash ring works.
- The **sort key** orders items inside one partition. Cassandra calls it the clustering key. [@cass-intro]

In DynamoDB, all items with the same partition key form an **item collection**. They are stored together. [@ddb-foundations] One Query reads them in sort-key order.

A sort key can also hold a path. AWS shows `[country]#[region]#[state]#[county]#[city]#[neighborhood]`. With `begins_with` you can read any level of that tree in one range query. [@ddb-sort-keys]

Step through one small table. Use Next and Back, or the ← and → keys.

[[diagram:nosql-item-collection-gsi]]

### Denormalise per query

Neither store has joins. Cassandra's docs say all fields a query needs must sit in one table. Each query gets its own table, so the same data is copied into several tables. [@cass-intro] That copying is **denormalisation**.

In DynamoDB you copy into item collections and indexes instead. In the diagram, the club's name is copied onto each membership item. The "my clubs" screen then needs one read, not one per club. The cost is that a club rename must update every copy.

### One-to-many and many-to-many

**One-to-many** is the easy case. Put the parent and its children under the parent's partition key. Give the children a sort key prefix like `ORDER#` and read them with `begins_with`. [@ddb-sort-keys]

**Many-to-many** needs two directions. AWS calls the usual answer an **adjacency list**. Each entity is a partition. Each link is an item in that partition, with the target's ID as the sort key. [@ddb-adjacency] AWS's example is invoices and bills. One Query reads all bills on an invoice. For the reverse, "which invoices contain this bill?", you add a global secondary index (GSI) on the sort key. [@ddb-adjacency] People call this flipped index an inverted index. [@ddb-nosql]

A GSI is not free. It is updated asynchronously, usually "within a fraction of a second". A query on it can return stale results. [@ddb-gsi] Each GSI also adds write cost, and a GSI short of write capacity throttles writes to the table. [@ddb-gsi]

### Keep partitions bounded

A partition key with a few huge values is a trap. In DynamoDB, each partition can serve at most 3,000 read units and 1,000 write units per second. [@ddb-partition-key] Cassandra suggests under 100,000 values and under 100 MB per partition. [@cass-intro] Its hard limit is 2 billion cells, but it gets slow well before that. [@cass-refining]

The fix is the one Discord used. Add a column to the partition key, such as a month or a date. [@cass-refining] Here is the simplified schema from Discord's post. It keeps the newest messages first. The real table has about 10 more columns. [@discord-2017]

```sql
CREATE TABLE messages (
  channel_id bigint,
  bucket int,
  message_id bigint,
  author_id bigint,
  content text,
  PRIMARY KEY ((channel_id, bucket), message_id)
) WITH CLUSTERING ORDER BY (message_id DESC);
```

The bucket must be computable from the ID or a timestamp. [@discord-2017] Reads then walk buckets from now backwards until they have enough rows. The cost lands on quiet channels: they may need several queries to fill one page. [@discord-2017] Pick the bucket size from your biggest key, not your average one.

### Single-table design, and its critics

AWS's general advice is to keep "as few tables as possible". [@ddb-nosql] **Single-table design** goes further. It stores many entity types in one table, so one Query can return a user and their orders together. [@ddb-foundations] AWS also lists the costs. It has a steep learning curve. All entities share one backup, one encryption setting and one stream. [@ddb-foundations] AWS says multiple tables are "good and sufficient" when you never query entities together. [@ddb-foundations]

Alex DeBrie, who recommends it for most apps, gives the main reason: fetching several item types "using a single request". [@debrie] He lists the downsides too. New access patterns are hard to add, and analytics need extra work. He would skip it for GraphQL, and for new apps that value flexibility. [@debrie]

Pete Naylor, who worked at Amazon, pushes back harder. He says: "Do NOT start with exactly one table as a design constraint…" [@naylor] He agrees the best answer might be one table, but says it often will be more. [@naylor] Items in one table but different partitions are no cheaper to read than items in two tables. [@naylor] The real win is the item collection. So treat the number of tables as an outcome of the modelling. [@naylor]

### What you give up

- **Ad-hoc queries.** A question that the keys do not answer needs a Scan of the whole table, or a new index. [@ddb-gsi]
- **Joins.** Cassandra has none, so you copy data instead. [@cass-intro]
- **Big transactions.** A DynamoDB transaction holds up to 100 distinct items and 4 MB, in one account and Region. [@ddb-txn] Each item costs two writes: one to prepare and one to commit. [@ddb-txn]

## Trade-offs

| Choice | Fast reads | Easy to change later | Watch out for |
|---|---|---|---|
| One table per entity (normalised) | No. Several requests per screen. | Yes. | Code that joins in memory. [@ddb-foundations] |
| Single-table design | Yes, when items share a partition key. [@naylor] | No. New patterns are hard to add. [@debrie] | Shared backups, streams and encryption. [@ddb-foundations] |
| Denormalised copies | Yes. One read per screen. | No. Each copy needs an update path. | Copies that disagree. |
| GSI for the reverse lookup | Yes. One Query. | Yes. You can add one later. | Stale reads and extra write cost. [@ddb-gsi] |
| Time-bucketed partition key | Yes, for recent data. | Partly. Bucket size is hard to change. | Quiet keys need several queries. [@discord-2017] |

**Simple rule:** if you cannot list the access patterns yet, you are not ready for these stores. Start with [a relational database](/learn/choosing-a-database) and come back.

## At work: how a senior uses this

Here is a worked example: a small sports club app. First, the access patterns. Then the keys. Each row is one request.

| Access pattern | Request | PK | SK |
|---|---|---|---|
| Show a member's profile | GetItem | `MEMBER#m1` | `PROFILE` |
| List a member's clubs | Query, `begins_with` | `MEMBER#m1` | `CLUB#` |
| List a club's members | Query on GSI1 | `CLUB#c1` (GSI1PK) | `MEMBER#` (GSI1SK) |
| Show one court's slots for a day | Query | `COURT#c7#2026-11-20` | `SLOT#` |
| List a member's upcoming bookings | Query, `between` | `MEMBER#m1` | `BOOKING#2026-11-20` to `BOOKING#9999` |

The bookings query needs an upper bound. A plain `>=` would also return the `CLUB#` and `PROFILE` items, because they sort after `BOOKING#`.

The court partition is bucketed by day, so it holds at most one item per slot. Booking writes two items in one transaction. The condition stops a double booking.

```js
await ddb.send(new TransactWriteCommand({ TransactItems: [
  { Put: { TableName: "Club",
      Item: { PK: "COURT#c7#2026-11-20", SK: "SLOT#18:00", member: "m1" },
      ConditionExpression: "attribute_not_exists(PK)" } },
  { Put: { TableName: "Club",
      Item: { PK: "MEMBER#m1", SK: "BOOKING#2026-11-20T18:00", court: "c7" } } },
] }));
```

Habits that go with it:

- Write each access pattern with its key, its sort order and its peak rate. AWS says to know data size, shape and velocity first. [@ddb-nosql]
- For each partition key, ask: **"What is the biggest this partition can get?"** If there is no answer, add a bucket.
- For every copied field, name the code that updates it.
- Ask: **"Which question will product ask next year?"** If you can't answer it with a new GSI, say so now.
- Load test with your biggest real tenant or channel, not average data.

## With AI

### What to ask the agent

> "Here are our 8 access patterns with rates. Propose a DynamoDB key design. For each pattern, show the exact GetItem or Query call, the PK and SK values, and how many items it reads."

> "For this Cassandra schema, estimate the largest partition using our biggest customer's data. Show the formula and the numbers."

> "List every field this design copies. For each one, show the code path that must change when the source changes."

### What to check yourself

- Every access pattern maps to one request. None needs a Scan or a filter on a non-key field.
- No partition key value can grow without a limit.
- The agent did not add a GSI where the base table could already answer.
- Code that reads a GSI can handle a missing or stale item.
- Transactions stay under 100 items, and the agent counted the double write cost. [@ddb-txn]

## Check yourself

## Go deeper

- Before this: [Data models](/learn/data-models-and-query-languages), [Sharding](/learn/sharding-and-consistent-hashing) and [Secondary and composite indexes](/learn/secondary-and-composite-indexes).
- [Quorums and leaderless replication](/learn/quorums-and-leaderless-replication) explains how Cassandra reads and writes replicas of each partition.
- Next: [DynamoDB in depth](/learn/dynamodb-in-depth) and [Cassandra in depth](/learn/cassandra-in-depth) go deeper on each store.
