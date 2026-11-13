---
topic: dynamodb
subtitle: Know what DynamoDB does with each request, and you can predict what it costs, when it throttles, and which guarantees you really get.
status: published
illustrative_story: false
sources:
  - id: aws-2015
    title: "Amazon Web Services, “Summary of the Amazon DynamoDB Service Disruption and Related Impacts in the US-East Region” (20 September 2015)"
    url: https://aws.amazon.com/message/5467D2/
    note: How bigger partition lists from new GSIs, a short network blip and retries took DynamoDB to about 55% errors.
  - id: ddb-paper
    title: "Mostafa Elhemali et al., “Amazon DynamoDB: A Scalable, Predictably Performant, and Fully Managed NoSQL Database Service”, USENIX ATC 2022"
    url: https://www.usenix.org/system/files/atc22-elhemali.pdf
    note: Section 3 has request routers, the metadata service and Multi-Paxos replica groups. Section 4 covers admission control, split for consumption and on-demand.
  - id: ddb-rw
    title: "Amazon DynamoDB Developer Guide: DynamoDB read and write operations"
    url: https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/read-write-operations.html
    note: How each read and write is rounded up and turned into capacity units, with examples.
  - id: ddb-ondemand
    title: "Amazon DynamoDB Developer Guide: DynamoDB on-demand capacity mode"
    url: https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/on-demand-capacity-mode.html
    note: The default mode, starting limits for new tables, and the double-the-previous-peak rule.
  - id: ddb-provisioned
    title: "Amazon DynamoDB Developer Guide: DynamoDB provisioned capacity mode"
    url: https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/provisioned-capacity-mode.html
    note: Capacity units, auto scaling with a target utilization, and how fast it reacts.
  - id: ddb-burst
    title: "Amazon DynamoDB Developer Guide: DynamoDB burst and adaptive capacity"
    url: https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/burst-adaptive-capacity.html
    note: Five minutes of saved burst capacity, adaptive capacity, and the per-partition limits.
  - id: ddb-gsi
    title: "Amazon DynamoDB Developer Guide: Using Global Secondary Indexes in DynamoDB"
    url: https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/GSI.html
    note: Asynchronous index updates, eventually consistent reads only, and how a short index throttles table writes.
  - id: ddb-lsi
    title: "Amazon DynamoDB Developer Guide: Local secondary indexes"
    url: https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/LSI.html
    note: Same partition key, strongly consistent reads allowed, and the 10 GB limit per partition key value.
  - id: ddb-concurrency
    title: "Amazon DynamoDB Developer Guide: Best practices for handling concurrent updates in DynamoDB"
    url: https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/BestPractices_ImplementingVersionControl.html
    note: Optimistic locking with a version attribute, transactions, and the lock client, and when to use each.
  - id: ddb-updateitem
    title: "Amazon DynamoDB API Reference: UpdateItem"
    url: https://docs.aws.amazon.com/amazondynamodb/latest/APIReference/API_UpdateItem.html
    note: ConditionExpression, ReturnValuesOnConditionCheckFailure, and the errors UpdateItem can return.
  - id: ddb-txn
    title: "Amazon DynamoDB Developer Guide: Amazon DynamoDB Transactions: How it works"
    url: https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/transaction-apis.html
    note: Limits, isolation, conflicts, the client token, and what a cancelled transaction still costs.
  - id: ddb-ttl
    title: "Amazon DynamoDB Developer Guide: Using time to live (TTL) in DynamoDB"
    url: https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/howitworks-ttl.html
    note: Expiry timestamps in epoch seconds, deletes that come days later, and free deletes.
  - id: ddb-streams
    title: "Amazon DynamoDB Developer Guide: Change data capture for DynamoDB Streams"
    url: https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/Streams.html
    note: 24-hour retention, exactly-once records, and ordering per item.
  - id: ddb-global
    title: "Amazon DynamoDB Developer Guide, “How DynamoDB global tables work”"
    url: https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/V2globaltables_HowItWorks.html
    note: The eventual (MREC) and strong (MRSC) multi-Region modes, conflicts, and what each mode does not support.
  - id: ddb-pagination
    title: "Amazon DynamoDB Developer Guide: Paginating table query results in DynamoDB"
    url: https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/Query.Pagination.html
    note: The 1 MB page, LastEvaluatedKey and ExclusiveStartKey, and empty pages.
  - id: ddb-filter
    title: "Amazon DynamoDB Developer Guide: Filter expressions for the Query operation in DynamoDB"
    url: https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/Query.FilterExpression.html
    note: Why a filter does not lower the read cost of a Query.
claims_checked:
  - claim: "On 20 September 2015, GSIs had grown DynamoDB's membership lists; after a network blip at 2:19 a.m. PDT, metadata timeouts and retries pushed errors to about 55%. AWS paused metadata requests at 5:06 a.m., and most customers recovered by 7:10 a.m."
    source: aws-2015
    note: "Times, 55%, the double-or-triple sentence and the monitoring quote checked on the page; year confirmed from the AWS Post-Event Summaries index."
  - claim: "Reads round up to 4 KB and writes to 1 KB; eventually consistent reads cost half, transactional operations cost double, and a failed conditional write still uses write capacity."
    source: ddb-rw
    note: "All rounding examples (3,500 bytes, 500 bytes) and the conditional-write cost are on the page."
  - claim: "A table write succeeds only if the table and its GSIs have enough write capacity; otherwise the table write is throttled. GSIs support only eventually consistent reads."
    source: ddb-gsi
    note: "Stated in the throughput considerations and data synchronization sections."
  - claim: "With any LSI, one partition key value can hold at most 10 GB of table and index items; past that, writes can fail with ItemCollectionSizeLimitExceededException."
    source: ddb-lsi
    note: "Item collection size limit section."
  - claim: "In MREC, conflicts are settled by last writer wins and conditions are checked against the local Region's copy; MRSC spans exactly three Regions and does not support TTL, LSIs or transactions."
    source: ddb-global
    note: "Consistency modes section and the TTL and transactions sections."
  - claim: "Only the partition leader serves writes and strongly consistent reads, and a write is acknowledged once a quorum, two of three replicas, has persisted the log record."
    source: ddb-paper
    note: "Sections 3 and 6.1 of the USENIX ATC 2022 paper."
reviewed_by: ai-factcheck
reviewed_on: 2026-11-13
quiz:
  - q: Your items are 6 KB each. How many read capacity units does one eventually consistent GetItem use?
    options:
      - "0.75, because 6 KB is three quarters of 8 KB."
      - "1, because 6 KB rounds up to 8 KB, which is 2 units, and eventual reads cost half."
      - "2, because eventual reads cost the same as strong reads."
    answer: 1
    why: Read sizes round up to the next 4 KB, so 6 KB counts as 8 KB, or 2 strongly consistent units. An eventually consistent read costs half of that.
  - q: A Query uses a FilterExpression that keeps 10 of the 1,000 items it reads. What do you pay for?
    options:
      - The 10 items returned.
      - All 1,000 items read, because the filter runs after the read.
      - Nothing extra, because filters are free and reduce cost.
    answer: 1
    why: A filter is applied after the Query reads the items, so the read cost is the same with or without it.
  - q: A table's GSI is short of write capacity, while the table itself has plenty. What happens to writes on the table?
    options:
      - They succeed, and the index catches up later.
      - They can be throttled, because each table write must also fit in the index's write capacity.
      - DynamoDB drops the index entries.
    answer: 1
    why: The docs say a table write succeeds only if the table and all affected GSIs have enough write capacity. Otherwise the write is throttled.
  - q: Your service uses a global table in eventual (MREC) mode in two Regions. It checks a version number with a condition expression before each update. Two users update the same item at the same time, one in each Region. What can happen?
    options:
      - Both condition checks pass locally, and last writer wins drops one update.
      - DynamoDB rejects the second write with a conflict error.
      - The two updates are merged field by field.
    answer: 0
    why: In MREC, a condition is checked against the item in the local Region only. Conflicts across Regions are settled by last writer wins, so the version check does not protect you.
  - q: A Query returns zero items, but the response has a LastEvaluatedKey. What should your code do?
    options:
      - Stop. No items means the end of the results.
      - Send the next Query with ExclusiveStartKey set to that LastEvaluatedKey.
      - Retry the same Query, because the page failed.
    answer: 1
    why: The 1 MB limit applies before the filter, so a page can be empty and still not be the last. You are done only when LastEvaluatedKey is missing.
incidents: [aws-2015-09-dynamodb-metadata]
glossary_optout: [availability]
related: [nosql-modelling, scaling-writes, multi-region, secondary-indexes, sharding, concurrency-control, sagas-outbox-cdc, cassandra]
---

## What broke

On Sunday, 20 September 2015, DynamoDB in AWS's US-East Region began returning errors at 2:19 a.m. Pacific time. [@aws-2015]

Inside DynamoDB, each table is split into partitions spread across storage servers. A server's list of partitions is its "membership". An internal metadata service keeps these lists. Servers check their list from time to time. If one cannot get it in time, it retries and stops taking requests. [@aws-2015]

Customers had rapidly adopted global secondary indexes (GSIs). Each GSI has its own partitions, so membership lists grew. A table with many partitions could see its share of these lists double or triple. [@aws-2015] AWS later wrote: "We did not have detailed enough monitoring for this dimension (membership size)". [@aws-2015]

Then a short network blip made many servers ask for their lists at once. The metadata service slowed past the time limit. Servers that got no answer took themselves out of service and retried, which added more load. Even healthy servers started to fail their next renewals. [@aws-2015] By 2:37 a.m., errors were far above anything seen in three years. They settled at about 55% of requests. [@aws-2015]

At 5:06 a.m., AWS paused requests to the metadata service. That cut the retries and took load off the service, so AWS could add capacity. By 7:10 a.m., errors were low enough for most customers to work normally. SQS, EC2 Auto Scaling, CloudWatch and the AWS Console were hit too. [@aws-2015]

A GSI looks like a small feature. Inside, it meant more partitions, more metadata and more work. That is why it helps to know what DynamoDB does with your table.

## The idea in simple words

DynamoDB is a key-value and document database that AWS runs for you. You never see a server. You see tables, keys, indexes and a bill.

Under that, three simple rules decide almost everything:

- **Every request costs capacity units,** based on item size and read type.
- **Every table is a set of partitions.** Each partition has hard limits per second.
- **Every partition is a small replica group** with one leader, spread across zones.

How to choose keys is covered in [Data modelling for NoSQL](/learn/data-modelling-for-nosql). This lesson is about what happens after that: cost, limits and guarantees.

> [!key] **The key idea.** In DynamoDB, a cheap design is a correct design. Count the units each request uses, keep each partition under its limits, and read the fine print on every guarantee: indexes, transactions, TTL and global tables each have one.

## How it works

### The path of one request

DynamoDB is made of many services. The core ones are request routers, the metadata service and storage nodes. [@ddb-paper] A router authenticates each request, looks up where its key lives, and sends it on. [@ddb-paper]

Each partition has replicas in different Availability Zones. They form a replication group that uses Multi-Paxos to elect a leader. [@ddb-paper] Only the leader serves writes and strongly consistent reads. [@ddb-paper] Step through one write. Use Next and Back, or the ← and → keys.

[[diagram:dynamodb-write-path]]

A write is acknowledged once a quorum, two of the three replicas, has saved its log record. [@ddb-paper] Any replica can serve an eventually consistent read, so that read may miss the newest write. [@ddb-paper] Reads are eventually consistent by default. [@ddb-provisioned]

### Capacity units: the math

A **read capacity unit** is one strongly consistent read per second of an item up to 4 KB. An eventually consistent read costs half. [@ddb-provisioned] A **write capacity unit** is one write per second of an item up to 1 KB. [@ddb-provisioned]

Sizes always round up. A 3,500-byte read counts as 4 KB. A 500-byte write counts as 1 KB. [@ddb-rw] A transactional read or write costs double. [@ddb-rw] An `UpdateItem` pays for the larger of the old and new item, even if you change one attribute. [@ddb-rw]

Worked example: 500 strongly consistent reads per second of 6 KB items. 6 KB rounds to 8 KB, so each read costs 2 units: 1,000 units in total. Make them eventually consistent, and it drops to 500.

A Query adds up the sizes of all items it returns, then rounds once. [@ddb-rw] So a few small items in one Query cost far less than many separate GetItems.

### On-demand or provisioned

**On-demand** is the default and the recommended mode. You pay per request. [@ddb-ondemand] A new on-demand table can take 4,000 writes and 12,000 reads per second. It instantly handles up to double its previous peak. Grow past double within 30 minutes, and you can be throttled. [@ddb-ondemand]

**Provisioned** mode charges per hour for the capacity you set, used or not. [@ddb-provisioned] Auto scaling moves it toward a target utilization. AWS recommends 70%. It acts only after two minutes over target, and alarms can add a few more minutes. [@ddb-provisioned] So it follows ramps, not sudden spikes.

### Hot partitions: burst, adaptive capacity, split

One partition takes at most 3,000 read units and 1,000 write units per second. [@ddb-burst] DynamoDB saves up to 300 seconds of unused capacity for short bursts. [@ddb-burst] **Adaptive capacity** instantly gives more of the table's capacity to a busy partition. It can also move hot items apart, even to one item per partition. [@ddb-burst]

DynamoDB also splits a partition for consumption, at a point chosen from the keys it sees. Splits usually take minutes. A split does not help one hot item or keys accessed in sequence. [@ddb-paper] For those, see [Scaling writes and hot keys](/learn/scaling-writes-and-hot-keys).

### GSI and LSI

A **GSI** has its own partitions [@aws-2015] and, on a provisioned table, its own read and write capacity. [@ddb-gsi] It is updated asynchronously, so it supports only eventually consistent reads. [@ddb-gsi] A table write succeeds only if every affected GSI has enough write capacity. Otherwise the table write is throttled. [@ddb-gsi] That is back-pressure from the index onto your table.

A **local secondary index** (LSI) keeps the table's partition key with a different sort key. You can only create it with the table, and you get up to five. [@ddb-lsi] It allows strongly consistent reads. [@ddb-lsi] The catch: with any LSI, one partition key value can hold at most 10 GB, table and index items together. Past that, writes that grow it can fail with `ItemCollectionSizeLimitExceededException`. [@ddb-lsi] Adaptive capacity will not split such an item collection either. [@ddb-burst]

### Conditional writes and optimistic locking

One `UpdateItem` is atomic. The risk is read, change, write back: someone else can write in between. [@ddb-concurrency] The fix is optimistic locking: keep a version attribute, and write only if it has not changed. [@ddb-concurrency] That check is a conditional write: the condition is tested on the current item, and a false condition stops the write. [@ddb-updateitem]

```js
// ddb is a DynamoDBDocumentClient (@aws-sdk/lib-dynamodb)
try {
  await ddb.send(new UpdateCommand({
    TableName: "Users",
    Key: { PK: "USER#42", SK: "PROFILE" },
    UpdateExpression: "SET #n = :name, #v = :next",
    ConditionExpression: "#v = :seen",
    ExpressionAttributeNames: { "#n": "name", "#v": "version" },
    ExpressionAttributeValues: { ":name": "Asha", ":seen": 7, ":next": 8 },
  }));
} catch (e) {
  if (e.name !== "ConditionalCheckFailedException") throw e;
  // Someone wrote first. Read the item again, redo the change, retry.
}
```

A failed condition still uses write capacity. [@ddb-rw] Set `ReturnValuesOnConditionCheckFailure` to `ALL_OLD` to get the current item with the error, at no read cost. [@ddb-updateitem]

### Transactions

`TransactWriteItems` changes up to 100 items, all or nothing. [@ddb-txn] Each item costs two writes, and a cancelled transaction still pays them. [@ddb-txn] Two transactions on the same item can cancel each other, so keep them small. [@ddb-txn] For safe retries, pass a client token. It is valid for 10 minutes. [@ddb-txn]

### TTL and Streams

With **TTL**, you store an expiry time in epoch seconds. DynamoDB deletes the item "typically within a few days" after it expires, at no write cost. [@ddb-ttl] Until then, reads still return it. Filter expired items out yourself. [@ddb-ttl]

**DynamoDB Streams** records every item change. Each record appears once, in order for each item. [@ddb-streams] Records live for only 24 hours. [@ddb-streams] A consumer that falls a day behind loses changes. See [Sagas, outbox and CDC](/learn/sagas-outbox-and-cdc) for how to use a change feed.

### Global tables

A **global table** keeps replicas in several Regions. The default mode, MREC, copies changes "typically within a second or less". [@ddb-global] If two Regions change one item at once, the latest timestamp wins. AWS calls this "last writer wins". [@ddb-global] Condition checks only see the local Region's copy, [@ddb-global] so version checks do not stop conflicts across Regions. [@ddb-concurrency]

The newer MRSC mode copies each write to at least one other Region before it returns. It always spans exactly three Regions: three replicas, or two replicas and a witness. [@ddb-global] A clashing write fails with `ReplicatedWriteConflictException`, and you retry. [@ddb-global] Writes are slower, and MRSC does not support TTL, LSIs or transactions. [@ddb-global] [Multi-region](/learn/multi-region-and-data-locality) covers when each model fits.

### Pages and filters

A Query returns at most 1 MB per page. If the response has `LastEvaluatedKey`, pass it as `ExclusiveStartKey` in the next call. You are done only when it is missing. [@ddb-pagination]

A filter runs after the read, so it does not lower the cost. [@ddb-filter] The 1 MB limit applies before the filter, so a page can come back empty and still not be the last. [@ddb-pagination] If you throw most items away, fix the key or add an index.

## Trade-offs

| Choice | Good for | Cost | Watch out for |
|---|---|---|---|
| On-demand | Yes. New or spiky traffic. | Pay per request. | Throttles if traffic more than doubles within 30 minutes. [@ddb-ondemand] |
| Provisioned with auto scaling | Yes. Steady, forecastable traffic. | Pay per hour, used or not. | Reacts in minutes, not seconds. [@ddb-provisioned] |
| GSI | A new access pattern, added any time. | Its own writes and storage. | Stale reads, and throttling of table writes. [@ddb-gsi] |
| LSI | Another sort order with strong reads. | Shares the table's capacity. | Must exist at table creation. 10 GB per partition key. [@ddb-lsi] |
| Global table, MREC | Fast local writes in many Regions. | Replicated writes in each Region. | Last writer wins can drop a write. [@ddb-global] |
| Global table, MRSC | No lost writes when a Region fails. | Slower writes and strong reads. | Exactly three Regions. No TTL or transactions. [@ddb-global] |

**Simple rule:** start with on-demand and no LSI. Add a GSI per proven access pattern. Move to provisioned only when traffic is steady and the bill matters.

## At work: how a senior uses this

- Write the unit cost next to each access pattern: item size, read type, calls per second.
- Ask: **"What is the peak per partition key, not per table?"** 1,000 write units a second (1,000 writes of up to 1 KB) is the ceiling.
- Before you add a GSI, check its write capacity can match the table's. AWS says it should be equal or greater. [@ddb-gsi]
- Avoid an LSI unless you need strong reads on another sort key. Ask if any partition key can grow past 10 GB.
- Add a version attribute to every item that users edit, and use a condition on every write.
- Treat TTL as cleanup, not as a timer. Filter expired items in reads.
- Alarm on stream consumer lag well below 24 hours.
- Before a global table, list the items two Regions might change at once.

## With AI

### What to ask the agent

> "Here are our access patterns with item sizes and peak rates. For each one, compute read or write capacity units per second, showing the rounding. Then name the partition key value with the highest load."

> "Rewrite this read-modify-write code to use a version attribute and a ConditionExpression. Handle ConditionalCheckFailedException by reading again and retrying at most 3 times."

> "Find every Query and Scan in this service that uses a FilterExpression. For each, estimate how many items it reads versus returns, and propose a key or index that avoids the filter."

### What to check yourself

- Sizes are rounded up per the rules: 4 KB for reads, 1 KB for writes, and doubled for transactions.
- Pagination loops until `LastEvaluatedKey` is missing, not until a page is empty.
- Code reading a GSI does not expect read-your-writes.
- No condition check is trusted across Regions in an MREC global table.
- Retries on throttling use backoff, as in [Backoff, jitter and retry budgets](/learn/backoff-jitter-retry-budgets).

## Check yourself

## Go deeper

- Before this: [Data modelling for NoSQL](/learn/data-modelling-for-nosql) and [Scaling writes and hot keys](/learn/scaling-writes-and-hot-keys).
- Related: [Secondary and composite indexes](/learn/secondary-and-composite-indexes), [Locking and MVCC in practice](/learn/locking-and-mvcc-in-practice), and [Cassandra in depth](/learn/cassandra-in-depth).
- Another DynamoDB outage, from DNS automation: [AWS, October 2025](/incidents#aws-2025-10-dynamodb-dns).
