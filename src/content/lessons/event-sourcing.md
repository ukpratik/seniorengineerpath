---
topic: event-sourcing
subtitle: Events can tell other services what changed, carry the data with them, or become your database. Know which one you are building, and what each costs.
status: published
reviewed_by: ai-factcheck
reviewed_on: 2026-11-13
illustrative_story: false
sources:
  - id: fowler-ed
    title: "Martin Fowler, “What do you mean by “Event-Driven”?”, martinfowler.com (7 February 2017)"
    url: https://martinfowler.com/articles/201701-event-driven.html
    note: The four patterns people mean by "event-driven", with the benefits and costs of each.
  - id: fowler-es
    title: "Martin Fowler, “Event Sourcing”, martinfowler.com (12 December 2005)"
    url: https://martinfowler.com/eaaDev/EventSourcing.html
    note: The original write-up. Complete rebuild, temporal query, snapshots, and how to stop replays from calling outside systems.
  - id: kiehl
    title: "Chris Kiehl, “Don't Let the Internet Dupe You, Event Sourcing is Hard”, Blogomatano (3 February 2019)"
    url: https://chriskiehl.com/article/event-sourcing-is-hard
    note: A first-hand account of a year running an event-sourced system in production, and what hurt. The author is named on the site's About page.
  - id: ms-es
    title: "Microsoft Learn, Azure Architecture Center: “Event Sourcing Pattern”"
    url: https://learn.microsoft.com/en-us/azure/architecture/patterns/event-sourcing
    note: Snapshots, optimistic concurrency, versioning strategies including upcasting, personal data, and when not to use the pattern.
  - id: ms-eda
    title: "Microsoft Learn, Azure Architecture Center: “Event-Driven Architecture Style”"
    url: https://learn.microsoft.com/en-us/azure/architecture/guide/architecture-styles/event-driven
    note: Benefits and challenges of event-driven systems, including eventual consistency and tracing across consumers.
  - id: kurrent-append
    title: "Kurrent documentation (Node.js client): “Appending events”"
    url: https://docs.kurrent.io/clients/node/v1.2/appending-events
    note: How a purpose-built event store checks the expected stream revision on each append.
  - id: verraes
    title: "Mathias Verraes, “Eventsourcing Patterns: Crypto-Shredding” (13 May 2019)"
    url: https://verraes.net/2019/05/eventsourcing-patterns-throw-away-the-key/
    note: One key per customer, delete the key to make the data unreadable, and the caveats.
  - id: pg-errcodes
    title: "PostgreSQL documentation: “Appendix A. PostgreSQL Error Codes”"
    url: https://www.postgresql.org/docs/current/errcodes-appendix.html
    note: Error code 23505 is unique_violation, which the code example uses to detect a conflict.
claims_checked:
  - claim: "Fowler separates four patterns people call event-driven: event notification, event-carried state transfer, event sourcing and CQRS. In event sourcing the event store becomes the principal source of truth."
    source: fowler-ed
    note: "Pattern names and the source-of-truth sentence checked on the page; the flow quote is exact."
  - claim: "Event stores use optimistic concurrency control and reject an append if the stream changed since it was read; the handler reloads, re-evaluates and retries."
    source: ms-es
    note: "Stated in the pattern advantages section, with the seat-reservation example."
  - claim: "Microsoft lists four versioning strategies (tolerant deserialization, event versioning, upcasting, in-place migration), with in-place migration as a last resort because it undermines the audit trail."
    source: ms-es
    note: "Checked in the Versioning events consideration."
  - claim: "About a month in, a shift in focus made some events no longer relevant, and either casting them on read or rewriting the log lost the ability to reproduce the state before the change."
    source: kiehl
    note: "Quote checked on the post (published 2019-02-03); author named on the site's About page."
  - claim: "KurrentDB's Node client appendToStream takes a streamState (any, no_stream, stream_exists or a revision) and throws if the stream is not in that state."
    source: kurrent-append
    note: "Checked in the Handling concurrency section of the v1.2 docs; no error class is named."
  - claim: "PostgreSQL error code 23505 is unique_violation."
    source: pg-errcodes
    note: "Checked in the error code table."
quiz:
  - q: The Orders service publishes "OrderPlaced" with only the order ID. Shipping then calls the Orders API to get the address. Which pattern is this?
    options:
      - Event sourcing
      - Event notification
      - Event-carried state transfer
    answer: 1
    why: The event only says that something changed. The receiver must call back for the data it needs.
  - q: Two requests load the same account stream at version 7. Both append a withdrawal with expected version 7. What should the event store do?
    options:
      - Save both, as versions 8 and 9.
      - Save the first as version 8 and reject the second, which must reload and decide again.
      - Save the second and drop the first.
    answer: 1
    why: The expected-version check is optimistic concurrency. The second writer's view is out of date, so its append fails.
  - q: You renamed a field in the "Deposited" event. Old events in the store still use the old name. What is the usual fix?
    options:
      - Edit every old event in the store to the new shape.
      - Add an upcaster that converts old events to the new shape when they are read.
      - Delete the old events and keep a snapshot.
    answer: 1
    why: Upcasting keeps stored events unchanged. Rewriting history breaks the audit trail and should be a last resort.
  - q: A team wants to event-source its admin settings screen "for the audit log". Nothing else needs history or replay. What should a senior suggest?
    options:
      - Event-source it, since every system benefits from an audit log.
      - Keep it as CRUD and add a history table.
      - Use event-carried state transfer instead.
    answer: 1
    why: Simple CRUD with no need for replay or history is a clear case where event sourcing does not pay off. A history table gives the audit trail cheaply.
incidents: []
related: [queues-vs-logs, sagas-outbox-cdc, ddd, schema-evolution, money-ledgers, cqrs]
---

## What broke

In 2019, engineer Chris Kiehl wrote up a year of running an event-sourced system in production. He had made the design call, and his team built it from scratch in Java. [@kiehl]

The pitch had been simple. Every change is an event in a log. Any service can subscribe and react. You get an audit log and time travel for free.

The real system was different. In his view, letting services read each other's raw events was both "extremely coupled" and "excruciatingly opaque". It became hard to tell which services produced or consumed which events. [@kiehl]

About a month in, a change in focus made some stored events "no longer relevant, at all". The team had to either translate old events on read or rewrite the log. Either way, he writes, they could no longer reproduce exactly how the system looked before the change. [@kiehl]

He also warns about the next stage. Once the data grows, reads must move to projections. Those lag behind writes, so new items can return 404 and deleted items can stay visible for a while. [@kiehl] His advice at the end: ask what core problem event sourcing solves for you. If you cannot answer clearly, a history table or a plain queue may be enough. [@kiehl]

## The idea in simple words

"Event-driven" means at least three different things. Martin Fowler separates them. [@fowler-ed] Kiehl's own closing question points at this mix-up: "Is what you actually want just a plain old queue?" [@kiehl]

1. **Event notification.** A service sends an event to say "something changed". It does not expect a reply. The receiver calls back if it needs details. [@fowler-ed]
2. **Event-carried state transfer.** The event carries the changed data. Receivers keep their own copy, so they never need to call the sender. [@fowler-ed]
3. **Event sourcing.** Every change to your own data is stored as an event. The event store is the main source of truth. Current state is whatever you get by replaying the events. [@fowler-ed]

The first two are about how services talk. The third is about how one service stores its data. You can use the first two without the third.

> [!key] **The key idea.** Events between services are messages. Event sourcing makes events your database. The first is common and useful. The second is a big, hard-to-undo decision for the parts of a system that need full history.

## How it works

### Events between services

Event notification gives low coupling. The sender does not know who listens. [@fowler-ed] Microsoft adds that you can add new consumers without changing the producer. [@ms-eda]

The cost is that the flow is hidden. Fowler warns that "it can be hard to see such a flow as it's not explicit in any program text". [@fowler-ed] Debugging is harder too, because there is no shared call stack. Put a correlation ID in every event so you can link the steps into one trace. [@ms-eda]

Event-carried state transfer gives more resilience. Receivers keep working when the sender is down, and they need no remote call. The cost is many copies of the data. [@fowler-ed] Those copies are eventually consistent. For a while after an event, different services see different states. [@ms-eda]

The [sagas, outbox and CDC lesson](/learn/sagas-outbox-and-cdc) shows how to publish these events safely.

### Event sourcing: the stream is the truth

In event sourcing, each aggregate (an account, an order) has its own stream. A stream is the ordered list of every event for that one object. [@ms-es] You never update or delete an event. To undo a change, you append a new event that reverses it. [@ms-es] This is the same rule as a [double-entry ledger](/learn/money-and-double-entry-ledgers), applied to any kind of data.

To load an aggregate, you start from an empty state and apply every event in order. [@ms-es] Fowler calls the full version a "complete rebuild". You can also stop at a past time to see the state as it was then. [@fowler-es]

Step through one account stream. Use Next and Back, or the ← and → keys.

[[diagram:event-sourcing-append]]

### Appending with an expected version

Two requests can load the same stream at the same moment and both pass your rules. Event stores stop this with optimistic concurrency control. They reject an append if the stream changed since it was read. The loser reloads, checks again, and retries. [@ms-es]

Purpose-built stores support this directly. In KurrentDB you pass the revision you read as the expected stream state. If the stream is not in that state, the append throws an error. [@kurrent-append] In Postgres you can get the same result with a primary key on `(stream_id, version)`. [@ms-es]

```ts
// CREATE TABLE events (stream_id text, version int, type text, data jsonb,
//   PRIMARY KEY (stream_id, version));
type Event =
  | { type: "Opened" }
  | { type: "Deposited"; paise: number }
  | { type: "Withdrawn"; paise: number };
type Account = { open: boolean; balance: number; version: number };

function apply(s: Account, e: Event): Account {
  switch (e.type) {
    case "Opened":    return { ...s, open: true };
    case "Deposited": return { ...s, balance: s.balance + e.paise };
    case "Withdrawn": return { ...s, balance: s.balance - e.paise };
  }
}

async function load(db: Db, id: string): Promise<Account> {
  const rows = await db.query(
    "SELECT version, data FROM events WHERE stream_id = $1 ORDER BY version", [id]);
  let s: Account = { open: false, balance: 0, version: 0 };
  for (const r of rows) s = { ...apply(s, r.data), version: r.version };
  return s;
}

async function append(db: Db, id: string, expected: number, events: Event[]) {
  try {
    await db.transaction(async (tx) => {
      for (const [i, e] of events.entries())
        await tx.query("INSERT INTO events VALUES ($1, $2, $3, $4)",
          [id, expected + 1 + i, e.type, e]);
    });
  } catch (err: any) {
    if (err.code === "23505") throw new ConflictError(id, expected); // someone wrote first
    throw err;
  }
}

async function withdraw(db: Db, id: string, paise: number) {
  const acc = await load(db, id);
  if (!acc.open) throw new Error("account not open");
  if (acc.balance < paise) throw new Error("insufficient funds");
  await append(db, id, acc.version, [{ type: "Withdrawn", paise }]);
}
```

If another writer already saved the next version, the insert breaks the primary key. Postgres reports that as error code 23505, `unique_violation`. [@pg-errcodes] The caller catches `ConflictError`, loads again, and retries. Always pass the version you loaded. This check only proves that the next version is free.

### Snapshots

Long streams are slow to replay. A snapshot is a saved copy of the state at one version, taken every N events. To load, you read the latest snapshot and replay only the events after it. [@ms-es] Snapshots are only a speed-up. The stream stays the truth, and you can rebuild any snapshot from it. [@ms-es]

### Replay and the outside world

Replaying must not repeat side effects. If a replay calls the payment provider or sends emails again, real people are affected. Fowler suggests a gateway in front of each outside system that knows when it is in replay mode and skips the call. [@fowler-es]

### Versioning old events

Events live forever, but your code changes. Microsoft lists four strategies, which you can use alone or together. [@ms-es]

- **Tolerant deserialization.** Ignore unknown fields and default missing ones. This handles fields you add.
- **Event versioning.** A version number in each event, so the reader picks the right logic.
- **Upcasting.** Functions convert old event shapes to the current one when they are read. Stored events stay unchanged.
- **In-place migration.** Rewrite stored events to the new shape. Use this only as a last resort, because it breaks the audit trail.

The general rules for changing data formats are in [schema evolution and encoding](/learn/schema-evolution-and-encoding). Kiehl's story shows the limit. Upcasting keeps your code working, but a change in what an event *means* can still blur the past. [@kiehl]

### Deleting personal data

Laws like the "right to be forgotten" clash with a store that never deletes. Microsoft's first advice is to keep personal data outside the event store and refer to it by ID. [@ms-es] If you cannot, use crypto-shredding. You encrypt each person's data with their own key. To forget them, you delete the key. [@ms-es] [@verraes] It adds encryption work on every read and write, and needs careful key management. [@ms-es] Verraes notes that it only helps if consumers such as projections also keep the data encrypted. It does not cover values that consumers computed from the data. [@verraes]

Verraes also asks whether this counts as deletion under GDPR. An update to his post quotes a reader whose lawyer said it does not. That reader prefers keeping personal data in a separate store you can delete from. [@verraes]

### Read models

Replaying a stream on every query is slow, so you build read models (projections) that are updated from the events. They lag behind writes. [@ms-es] The next lesson, *CQRS and read models*, covers them.

## Trade-offs

| Option | Full history? | Main cost | Use it when |
|---|---|---|---|
| CRUD + history table | Partial. You log what you choose. | Low. | Most services, including admin screens and settings. |
| Event notification | No. | Hidden flows, and callbacks to the sender. [@fowler-ed] | Other services need to react, but can fetch details. |
| Event-carried state transfer | No. | Many copies of data, eventually consistent. [@fowler-ed] [@ms-eda] | Receivers must keep working when the sender is down. |
| Event sourcing | Yes. Every change, in order. | Versioning, snapshots, replay safety, projections, and new skills for the team. [@ms-es] | Ledgers, orders and other areas where history and audit are core business needs. |

Microsoft's summary: for most systems, and most parts of a system, plain data storage is enough. Its poor fits include simple CRUD, short-lived prototypes, systems that need real-time consistent views, mostly static data, and teams new to event-driven design. [@ms-es] Apply event sourcing to the few parts that need it, not to the whole system. [@ms-es]

## At work: how a senior uses this

- When someone says "event-driven", ask: **"Notification, state transfer, or event sourcing?"** Then argue about one thing at a time.
- Before you event-source anything, write down the core problem it solves. If the answer is "audit", ask whether a history table is enough. [@kiehl]
- Name events for business intent, like `SeatsReserved`, not "remaining seats changed to 42". [@ms-es]
- Do not let other services read your raw stored events. Publish separate integration events as your public contract. Microsoft notes that stored events are often too low-level for this. [@ms-es]
- Always append with an expected version. Test what happens when two writers race.
- Plan event versioning and personal data on day one. Both are painful to add later.
- Put a correlation ID in every event, and check that your tracing follows it.

## With AI

### What to ask the agent

> "Here is our event-sourced Account aggregate. Write a test where two writers load version 3 and both append. Assert that exactly one succeeds and the other gets a conflict error."

> "List every event type in this codebase. For each one, say whether it is a notification, carries state, or is a stored domain event. Mark any stored event that another service reads directly."

> "We renamed `amount` to `amount_paise` in the Deposited event. Write an upcaster for old events, and a test that replays a stream with both versions."

### What to check yourself

- The append really checks the expected version. An agent may write a plain insert with no unique key, which quietly allows lost updates.
- Replays and projection rebuilds do not call outside systems or send messages.
- No migration edits stored events in place. Old shapes are handled by upcasters.
- Personal data is either outside the store or encrypted with a per-person key.
- Event handlers are idempotent, since delivery is usually at-least-once. [@ms-es]

## Check yourself

## Go deeper

Before this: [queues vs logs](/learn/queues-vs-logs-delivery-guarantees). Related: [sagas, outbox and CDC](/learn/sagas-outbox-and-cdc), [domain-driven design](/learn/domain-driven-design) for aggregates and domain events, [schema evolution and encoding](/learn/schema-evolution-and-encoding), and [money and double-entry ledgers](/learn/money-and-double-entry-ledgers). Next: CQRS and read models.
