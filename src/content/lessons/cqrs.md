---
topic: cqrs
subtitle: Split the model you write with from the models you read from, keep each read model rebuildable, and design the screen for the moments when it lags.
status: published
illustrative_story: false
sources:
  - id: gh-apr-2026
    title: "GitHub, “GitHub availability report: April 2026”, The GitHub Blog (14 May 2026)"
    url: https://github.blog/news-insights/company-news/github-availability-report-april-2026/
    note: The April 1 code search incident. Indexing stopped, results went stale, and the index was reset and rebuilt from Git.
  - id: fowler-cqrs
    title: "Martin Fowler, “CQRS”, martinfowler.com (14 July 2011)"
    url: https://martinfowler.com/bliki/CQRS.html
    note: The short definition, credited to Greg Young, and the warning that most systems should not use it.
  - id: ms-cqrs
    title: "Microsoft Learn, Azure Architecture Center: “CQRS Pattern”"
    url: https://learn.microsoft.com/en-us/azure/architecture/patterns/cqrs
    note: Commands as business tasks, one store or two, the outbox and idempotent consumers, and when not to use the pattern.
  - id: ms-mv
    title: "Microsoft Learn, Azure Architecture Center: “Materialized View Pattern”"
    url: https://learn.microsoft.com/en-us/azure/architecture/patterns/materialized-view
    note: Views are disposable and rebuildable. Refresh choices, staleness alerts, and swapping in a rebuilt view.
  - id: kurrent-sub
    title: "Kurrent documentation (Node.js client): “Catch-up Subscriptions”"
    url: https://docs.kurrent.io/clients/node/v1.2/subscriptions
    note: Subscribing from a stored position, and saving checkpoints so a projection resumes after a restart.
  - id: marten-daemon
    title: "Marten documentation: “Async Projections Daemon”"
    url: https://martendb.io/events/projections/async-daemon.html
    note: A real projection runner. Progress rows, the high water mark, rebuilds, at-least-once delivery, and waiting for non-stale data with a timeout.
claims_checked:
  - claim: "GitHub code search failed 100% of queries from 14:40 to 17:00 UTC on 1 April 2026, came back missing changes after about 07:00 UTC, and was current at 23:45. The index was reset to a point before the disruption and re-indexed; it is a secondary index derived from Git, and no repository data was lost."
    source: gh-apr-2026
    note: "Timeline, cause wording ('applied too aggressively') and recovery checked against the report body; its header lists 15:02, which the lesson notes."
  - claim: "Fowler first heard CQRS described by Greg Young, says the models may share a database or use separate ones, and warns that for most systems CQRS adds risky complexity."
    source: fowler-cqrs
    note: "Quote exact; ReportingDatabase offered as an alternative and bounded-context scope checked."
  - claim: "Commands should be business tasks such as 'Book hotel room'; separate stores need a transactional outbox and an idempotent read-model consumer; the pattern may not suit simple domains or CRUD-style UIs."
    source: ms-cqrs
    note: "All three points checked on the page."
  - claim: "A materialized view is completely disposable and can be rebuilt from the source; build and validate a new view, then swap; a lost refresh signal makes it silently serve stale data, so alert on view age."
    source: ms-mv
    note: "Wording checked in Solution and Problems and considerations."
  - claim: "Marten's daemon delivers at least once, flushes its progress row when a shard stops, and QueryForNonStaleData throws TimeoutException by default, with a ReturnStaleData mode."
    source: marten-daemon
    note: "Drain-timeout section and non-stale query section checked."
  - claim: "KurrentDB catch-up subscriptions start from a stored position, and you store the last processed position as a checkpoint to resume after a restart."
    source: kurrent-sub
    note: "subscribeToStream/subscribeToAll with fromRevision/fromPosition checked in the v1.2 Node docs."
reviewed_by: ai-factcheck
reviewed_on: 2026-11-13
quiz:
  - q: A projection handler receives event version 9 for order #7. The read-model row for #7 already stores version 9. What should the handler do?
    options:
      - Apply the event again, since the newest event always wins.
      - Skip it. The event was already applied, so applying it again could double-count.
      - Throw an error, because a duplicate means the event store is broken.
    answer: 1
    why: Delivery is at least once, so duplicates are normal. A stored version that is equal or higher means the event is already in the row.
  - q: A user clicks "Rename project". The API replies 200, but the list page still shows the old name for a second. Which fix keeps CQRS and removes the confusion?
    options:
      - Write to the read model directly from the command handler, in a second call.
      - Return the new state and version from the command, and show it until the read model reaches that version.
      - Turn off the projection and read from the write model everywhere.
    answer: 1
    why: The command already knows the new state. Showing it, then waiting for the matching version, gives read-your-writes without a dual write.
  - q: You must add a new field to a read model fed by events. What is the safest way to fill it for old data?
    options:
      - Build a new copy of the read model from the first event, then switch readers to it once it has caught up.
      - Edit old events in the event store so they include the new field.
      - Run an UPDATE on the live read-model table and accept a short gap.
    answer: 0
    why: A read model is derived data. Rebuilding it next to the old one, then swapping, keeps readers on a complete view the whole time.
  - q: A team wants CQRS with a separate read database for a settings page with simple create, read, update and delete screens. What should a senior say?
    options:
      - Yes, CQRS makes every part of a system faster.
      - No. Simple CRUD fits one model, and CQRS would add lag and moving parts for no gain.
      - Yes, but only with event sourcing.
    answer: 1
    why: Fowler and Microsoft both say CQRS is a poor fit when the domain is simple and CRUD is enough.
incidents: [github-2026-04-code-search-stale]
related: [event-sourcing, sagas-outbox-cdc, search-systems, replication, scaling-reads, ddd, queues-vs-logs, exactly-once]
---

## What broke

On 1 April 2026, GitHub code search failed every query from 14:40 to 17:00 UTC. That is the report's timeline. Its header lists the incident from 15:02. [@gh-apr-2026]

It started with a routine upgrade to the messaging system behind search. An automated change was applied too aggressively. Internal services lost coordination, search indexing stopped, and results began to go stale. [@gh-apr-2026] During recovery, an unintended deployment cleared internal routing state. That turned stale search into no search at all. [@gh-apr-2026]

At 17:00 search came back, but in a degraded state. Results did not show any repository change made after about 07:00 UTC. Search only showed current data again at 23:45. [@gh-apr-2026]

The fix is the interesting part. The team reset the search index to a point before the problem, and indexed again from there. [@gh-apr-2026]

No repository data was lost. The report explains why: the search index is a secondary index derived from the Git repositories, and those were not affected. [@gh-apr-2026] The index was a read model. It could be wrong for hours, and then be rebuilt from the source of truth.

## The idea in simple words

Most code uses one model for everything. The same `Order` class and table handle "ship this order" and "show my last 20 orders".

CQRS, Command Query Responsibility Segregation, splits that. You use one model to change data and a different model to read it. [@fowler-cqrs] Fowler first heard the idea from Greg Young. [@fowler-cqrs]

- A **command** asks for a business task, like "Book hotel room". It is not a raw field update like "set status to Reserved". [@ms-cqrs] The write model checks the rules and saves the change.
- A **query** never changes data. It returns data shaped for one screen. [@ms-cqrs]

The two models may share one database, or use separate ones. [@fowler-cqrs] With separate stores, you can pick a different technology for each, such as a relational write store and a document read store. [@ms-cqrs]

CQRS does not need [event sourcing](/learn/event-driven-architecture-event-sourcing). [@ms-cqrs] The read side can be fed by events, by [change data capture](/learn/sagas-outbox-and-cdc), or by a plain database view. Event sourcing usually needs read models, because replaying a stream for every query is too slow. [@ms-mv]

> [!key] **The key idea.** The write model is the truth. Every read model is a disposable copy, shaped for one kind of read. Make each copy easy to rebuild, and design the screen for the time when it is behind.

## How it works

### Feeding the read side

When the stores are separate, the write side must tell the read side what changed. A common way is to publish events. You usually cannot put the database and the broker in one transaction, so use an outbox and make the read-side consumer idempotent. [@ms-cqrs] The [sagas, outbox and CDC lesson](/learn/sagas-outbox-and-cdc) covers that pipe.

The code that turns those events into a read model is a projection. Microsoft calls such a view "completely disposable", because it can be rebuilt from the source data. [@ms-mv]

One write model can feed several read stores, each with its own projection:

- **A SQL table** shaped like the screen, for example `order_view` with status and total, so the list page needs no joins.
- **A search index** for text and filters. See [search systems](/learn/search-and-inverted-indexes).
- **A cache** of the pages people read most, such as recent orders.

Each one lags by a different amount, and each needs its own rebuild plan.

### Position, order and duplicates

A projection reads events in order and remembers how far it got. In KurrentDB you subscribe from a stored position. To resume after a restart, you must save the position of the last event you handled. That saved position is a checkpoint. [@kurrent-sub] Marten's projection daemon stores its progress too. When a shard stops, it flushes its progress row, so the next start "picks up exactly where this one left off". [@marten-daemon]

Delivery is still at least once. Marten says so directly. [@marten-daemon] A crash after you apply an event, but before you save the checkpoint, replays that event. So the handler must be idempotent.

Order matters within one aggregate. "Shipped" before "Paid" would leave the wrong status. Route all events of one order to one handler at a time. With Kafka, you key messages by order ID, as in [queues vs logs](/learn/queues-vs-logs-delivery-guarantees). Order across different orders rarely matters.

Store the last applied version on each read-model row. Then a duplicate or an old event is easy to spot. Step through it. Use Next and Back, or the ← and → keys.

[[diagram:cqrs-projection-version]]

Here is a handler for an event-sourced order. Each event carries its stream version and its global position.

```ts
// CREATE TABLE order_view (order_id text PRIMARY KEY, status text, version int NOT NULL);
// CREATE TABLE checkpoints (name text PRIMARY KEY, position bigint NOT NULL);
// One runner reads the log in order. Seed: INSERT INTO checkpoints VALUES ('order_view', 0);
// version is 1 for a stream's first event (add 1 to a KurrentDB revision, which starts at 0).
type Stored = { streamId: string; version: number; position: bigint;
                type: "OrderPlaced" | "OrderPaid" | "OrderShipped" };
const statusAfter = { OrderPlaced: "placed", OrderPaid: "paid", OrderShipped: "shipped" };

async function project(db: Db, e: Stored): Promise<void> {
  await db.transaction(async (tx) => {
    const rows = await tx.query(
      "SELECT version FROM order_view WHERE order_id = $1 FOR UPDATE", [e.streamId]);
    const have = rows[0]?.version ?? 0;
    if (e.version <= have) return;            // already applied: skip
    if (e.version > have + 1)                 // an earlier event is missing
      throw new Error(`gap in ${e.streamId}: have ${have}, got ${e.version}`);
    await tx.query(
      `INSERT INTO order_view (order_id, status, version) VALUES ($1, $2, $3)
       ON CONFLICT (order_id) DO UPDATE SET status = $2, version = $3`,
      [e.streamId, statusAfter[e.type], e.version]);
    await tx.query(
      "UPDATE checkpoints SET position = $1 WHERE name = 'order_view'", [e.position]);
  });
}
```

The row update and the checkpoint commit together, so they never disagree. A gap throws and stops the projection on that event. It will not heal by itself, so you get an alert instead of a wrong status. With several partitions and workers, keep one checkpoint row per partition. With CDC instead of events, use a version column or the source log position in the same way.

### Rebuilding a projection

You will rebuild. A bug corrupts rows, or a screen needs a new field. Run the projection from the first event into a new table, such as `order_view_v2`. Tools help with the replay: Marten's daemon has `RebuildProjectionAsync`. [@marten-daemon] Building next to the old table and swapping is your job.

Keep readers on the old view while the new one builds. Microsoft's advice is to build and validate the new view, then swap to it. [@ms-mv] GitHub's recovery was the same idea at scale: go back to a known good point and index forward. [@gh-apr-2026]

Time a full rebuild before you need one. If it takes two days, you need snapshots or a faster path. Microsoft notes that building views from many events can cost a lot of time and compute. [@ms-cqrs]

### When the read model lags

After a user's command, the read model is behind for a moment. Microsoft calls this out: the read side may not show the newest changes at once. [@ms-cqrs] The user clicks "Ship" and the list still says "Paid". The general fixes are in [read-your-writes](/learn/database-replication). For CQRS, use these:

1. **Return the new state from the command.** The write side knows it. The UI shows it at once, without asking the read side.
2. **Return a version token.** The command replies "order 42 is at version 5". The next query passes it. The read side answers only once it has applied version 5.
3. **Wait with a timeout.** Marten's `QueryForNonStaleData` waits until a projection reaches the latest events. By default it throws a `TimeoutException` if they cannot catch up in time. A mode to return stale data instead also exists. [@marten-daemon] In a UI, wait a second or two, then show "updating…".

Also watch lag in production. Microsoft warns that a lost or late refresh signal makes a view serve stale results with no error. Alert when its age passes your limit. [@ms-mv]

## Trade-offs

| Option | Reads fast for every screen? | Main cost | Use it when |
|---|---|---|---|
| One model, one store (CRUD) | No. Complex screens need joins. | Low. | Most services. Fowler says many systems fit CRUD. [@fowler-cqrs] |
| CRUD plus a reporting database | Yes, for reports. | One copy job to run. | A few heavy queries. Fowler points to this when the domain does not suit CQRS. [@fowler-cqrs] |
| CQRS, one store | Partly. Separate read code and views. | More code, no lag. | Read and write logic differ, but one database copes. |
| CQRS, separate read stores | Yes. Each store fits its reads. | Lag, rebuilds, idempotent handlers, more systems. [@ms-cqrs] | Reads outnumber writes [@ms-cqrs], or reads need search, caches or other shapes. |

Fowler's warning is blunt: "for most systems CQRS adds risky complexity". Use it only on specific parts of a system. [@fowler-cqrs] Microsoft adds that it may not suit simple domains, or apps where CRUD screens are enough. [@ms-cqrs] Apply it per [bounded context](/learn/domain-driven-design), not to a whole system. [@fowler-cqrs]

## At work: how a senior uses this

- Ask first: **"Which screen is slow or awkward, and why?"** Often an index or a reporting replica fixes it without CQRS.
- For every read model, write down its owner, its source, its usual lag and how to rebuild it. Time the rebuild.
- Put a version on every read-model row and on every command reply. It costs one column and makes duplicates and stale reads easy to spot.
- Never let a command handler write the read model in a second call. That is a dual write. Let the projection do it.
- Alert on projection lag in seconds, and on handlers stuck on one event.
- Design each screen for lag. Decide what the user sees during the first second after they act.
- When a read model is wrong, rebuild it next to the old one and swap. Do not patch rows by hand.

## With AI

### What to ask the agent

> "Here is our `OrderShipped` projection handler. Write a test that delivers the same event twice and an older event after a newer one. Assert the row ends at the right status and version."

> "List every read model fed from the orders service. For each, show its source, how its checkpoint is stored, and the command to rebuild it from scratch."

> "Change the ship-order endpoint to return the new status and stream version. Then change the order page to show that status until a query returns the same version or higher, with a 2-second timeout."

### What to check yourself

- The row update and the checkpoint save are in one transaction.
- The handler compares versions. A plain upsert with no version check lets an old event overwrite a new one.
- Events for one aggregate cannot be handled by two workers at the same time.
- The rebuild path writes to a new table and swaps. It does not truncate the live one.
- The rebuild does not send emails or call other services. Those belong to other handlers.
- No code in the command path writes to a read store directly.

## Check yourself

## Go deeper

Before this: [event-driven architecture and event sourcing](/learn/event-driven-architecture-event-sourcing). Related: [sagas, outbox and CDC](/learn/sagas-outbox-and-cdc), [scaling reads](/learn/scaling-reads), [search systems](/learn/search-and-inverted-indexes), [database replication](/learn/database-replication), [exactly-once and effectively-once](/learn/exactly-once-and-effectively-once) and [domain-driven design](/learn/domain-driven-design).
