---
topic: sagas-outbox-cdc
subtitle: When one order touches three services, there is no single "undo". Learn to undo step by step, and to send events that never get lost.
status: published
reviewed_by: ai-factcheck
reviewed_on: 2026-10-21
illustrative_story: true
sources:
  - id: pg-logical
    title: "PostgreSQL documentation, “Logical Decoding Concepts”"
    url: https://www.postgresql.org/docs/current/logicaldecoding-explanation.html
    note: What logical decoding and replication slots are, and how a slot keeps changes until the client has read them.
  - id: sagas-paper
    title: "Hector Garcia-Molina and Kenneth Salem, “Sagas”, Princeton University technical report TR-070-87 (1987); also in the ACM SIGMOD 1987 proceedings"
    url: https://www.cs.princeton.edu/research/techreps/598
    note: The original paper. A long transaction is split into a chain of short ones, and compensating transactions undo a partial run.
  - id: ms-saga
    title: "Chris Richardson, “Pattern: Saga”, microservices.io"
    url: https://microservices.io/patterns/data/saga.html
    note: Sagas across services, compensating transactions, choreography vs orchestration, and the lack of isolation.
  - id: ms-outbox
    title: "Chris Richardson, “Pattern: Transactional outbox”, microservices.io"
    url: https://microservices.io/patterns/data/transactional-outbox.html
    note: The outbox table, the message relay, and why the relay can publish a message more than once.
  - id: aws-orch
    title: "AWS Prescriptive Guidance, “Saga orchestration pattern”"
    url: https://docs.aws.amazon.com/prescriptive-guidance/latest/cloud-design-patterns/saga-orchestration.html
    note: Why 2PC does not fit a database-per-service design, an order-inventory-payment example, and a Step Functions version.
  - id: aws-chor
    title: "AWS Prescriptive Guidance, “Saga choreography pattern”"
    url: https://docs.aws.amazon.com/prescriptive-guidance/latest/cloud-design-patterns/saga-choreography.html
    note: Event-driven sagas, and their costs - harder tracking, cyclic dependencies and the dual-write issue.
  - id: dbz-blog
    title: "Gunnar Morling, “Reliable Microservices Data Exchange With the Outbox Pattern”, Debezium blog (2019)"
    url: https://debezium.io/blog/2019/02/19/reliable-microservices-data-exchange-with-the-outbox-pattern/
    note: The dual-write problem with Kafka, an outbox read by CDC, and a consumer that skips duplicate event IDs.
  - id: dbz-features
    title: "Debezium documentation, “Debezium Features”"
    url: https://debezium.io/documentation/reference/stable/features.html
    note: Log-based change data capture compared with polling, including low delay and capturing deletes.
  - id: dbz-pg
    title: "Debezium documentation, “Debezium connector for PostgreSQL”"
    url: https://debezium.io/documentation/reference/stable/connectors/postgresql.html
    note: How the connector uses logical decoding and a replication slot, and resumes reading the WAL after a restart.
  - id: dbz-router
    title: "Debezium documentation, “Outbox Event Router”"
    url: https://debezium.io/documentation/reference/stable/transformations/outbox-event-router.html
    note: The outbox table columns, how rows are routed to topics, and using the event id to remove duplicates.
claims_checked:
  - claim: "In a database-per-service design, 2PC is not an option because there is no single controller to coordinate it; on a payment failure the orchestrator runs compensations for earlier steps."
    source: aws-orch
    note: "Motivation and High-level architecture sections. Step Functions sample has Revert Payment, Revert Inventory and Remove Order. Also lists the orchestrator as a possible single point of failure and requires idempotent participants."
  - claim: "Choreography suits few participants; it gets harder to track as participants grow, risks cyclic dependencies, and makes timeouts and retries harder to apply globally."
    source: aws-chor
    note: "Issues and considerations: Complexity, Resilient implementation, Cyclic dependencies, Dual writes issue."
  - claim: "The relay might publish a message more than once, e.g. if it crashes after publishing but before recording it, so consumers must be idempotent, e.g. by tracking processed message IDs."
    source: ms-outbox
    note: "Resulting context section, near-verbatim. Saga page confirms sagas lack isolation and need compensating transactions."
  - claim: "There is no shared transaction spanning the service database and Kafka; with CDC the outbox row can be deleted right after insert; consumers skip already-processed event IDs; the aggregate ID is the Kafka key, keeping per-aggregate order."
    source: dbz-blog
    note: "Morling, 2019-02-19. Dedup marker is written in the handler's transaction (@Transactional, TxType.MANDATORY). Router docs confirm aggregateid is the default message key and id can be used to drop duplicates."
  - claim: "Log-based CDC captures all changes with millisecond-range delay, needs no data model change, and captures deletes; the Postgres connector uses logical decoding and resumes reading the WAL where it left off."
    source: dbz-features
    note: "Features page bullet list; Postgres connector page (dbz-pg) for logical decoding, replication slot and restart behaviour."
  - claim: "Sagas (1987): a long-lived transaction written as a sequence of transactions; if a saga does not complete, compensating transactions amend the partial execution."
    source: sagas-paper
    note: "Checked against the Princeton TR-070-87 abstract only; no further claims are attributed to the paper."
quiz:
  - q: Your order service saves an order, then publishes "OrderPlaced" to Kafka in a second call. What can go wrong?
    options:
      - Nothing, if both calls use retries.
      - The service can crash between the two calls, so the order exists but no event is ever sent.
      - Kafka will refuse the message because the order already exists.
    answer: 1
    why: These are two writes to two systems with no shared transaction. A crash between them leaves them out of step. This is the dual-write problem.
  - q: In an order saga, the payment step fails after stock was reserved. What should happen?
    options:
      - The database rolls back all three services automatically.
      - The saga runs compensating actions, such as releasing the stock and cancelling the order.
      - The saga retries the payment forever.
    answer: 1
    why: Each step already committed in its own database. Only new actions written by you can undo them.
  - q: Why must a consumer of outbox events check for duplicates?
    options:
      - Because the relay can publish the same row twice, for example if it crashes before saving its progress.
      - Because Kafka always sends every message twice.
      - It does not need to. The outbox gives exactly-once delivery.
    answer: 0
    why: The outbox makes sure an event is sent if the data was saved. It does not stop the relay from sending it again.
  - q: When does orchestration fit better than choreography?
    options:
      - When there are only two services and one simple event.
      - When the flow has many steps and you need one place to see its state, timeouts and retries.
      - Never. Choreography is always simpler.
    answer: 1
    why: AWS notes that choreography gets hard to track as participants grow, and timeouts and retries are harder to apply across the whole flow.
incidents: []
related: [queues-vs-logs, idempotency-retries, transactions-isolation, distributed-transactions, checkout-orders, workflow-engines]
---

## What broke

A shop splits checkout into three services: Orders, Inventory and Payments. Each one has its own database.

A new engineer writes the order flow in the Orders service. It creates the order, calls Inventory to hold the item, and then calls Payments to charge the card. If the charge fails, it marks the order "failed".

It looks fine in testing. In production, cards fail every day. Each time, the order is marked "failed", but the held item stays held. Nobody tells Inventory to let it go.

After a month, popular items show "out of stock" on the site. The warehouse has them on the shelf. Hundreds of units are held for orders that will never ship.

A second bug hides in the same code. Orders saves an order, then sends an "OrderPlaced" event to Kafka. One night the service restarts between those two steps. Thirty orders exist, but the warehouse never hears about them. Customers wait for parcels that nobody packs.

## The idea in simple words

Inside one database, a transaction gives you "all or nothing". Across three services with three databases, you do not get that for free.

The classic tool for "all or nothing" across databases is two-phase commit, or 2PC. A coordinator asks every database to prepare, then tells all of them to commit. AWS says 2PC is not an option in a database-per-service design. There is no single controller that can coordinate the services that way. [@aws-orch] Message brokers add another gap. You cannot have one transaction that spans your database and Kafka. [@dbz-blog]

So teams use a **saga**. A saga is a chain of small local transactions. Each step commits on its own. If a later step fails, the saga runs a **compensating transaction** for each earlier step, to undo its effect. [@ms-saga] The idea comes from a 1987 paper by Garcia-Molina and Salem. They split long database transactions into short ones, with compensations that undo a partial run. [@sagas-paper]

Each step must also tell the next service what happened. Writing to your database and to a broker in two calls is the **dual-write** problem. The **transactional outbox** fixes it. You save the event in your own database, in the same transaction as your data. A separate relay publishes it later. [@ms-outbox]

> [!key] **The key idea.** Across services, you cannot roll back. You can only move forward with new actions that undo old ones. And you never write to two systems at once: write to one, and copy from it.

## How it works

Step through an order saga that fails at payment. Then see how the outbox sends each event. Use Next and Back, or the ← and → keys.

[[diagram:saga-outbox]]

### Choreography or orchestration

There are two ways to run a saga. [@ms-saga]

- **Choreography.** No one is in charge. Each service listens for events and reacts. Orders emits "OrderPlaced". Inventory hears it, holds stock, and emits "StockReserved". Payments hears that, and so on.
- **Orchestration.** One component, the orchestrator, tells each service what to do next. It keeps the state of the whole saga in one place.

AWS lists the costs of each. Choreography has no central part to fail. But as services grow, the flow gets hard to follow, and services can end up depending on each other in a cycle. Timeouts and retries must be built into every service. [@aws-chor] An orchestrator keeps the flow in one place, but it can become a single point of failure. [@aws-orch] The AWS guide shows an orchestrator built on Step Functions. It undoes payment, inventory and the order when a step fails. [@aws-orch]

### Writing good compensations

A compensation is not a database rollback. The first step already committed, and other people may have seen it. So the compensation is a new business action: "release the item", "refund the charge", "cancel the order".

Sagas also have no isolation. Two sagas can run at the same time and see each other's half-done work. [@ms-saga] For example, a customer may see stock as held for a minute, then free again. Design for this. Show states like "pending" to users, and do the steps that are hard to undo, like shipping, last.

Compensations can fail and run twice too. Make every step and every compensation idempotent, so a retry does no harm. [@aws-orch] See [idempotency, retries and timeouts](/learn/idempotency-retries-timeouts).

### The outbox, step by step

In the same database transaction, you insert the order row and one outbox row. The outbox row holds the event: an ID, the aggregate type and ID (here, the order and its ID), an event type like "OrderCreated", and a payload. [@dbz-router] If the transaction commits, both rows exist. If it rolls back, neither does. So the event is sent if and only if the data was saved. [@ms-outbox]

A relay then moves outbox rows to the broker. There are two common ways. [@ms-outbox]

- **Polling.** A job reads new rows from the outbox table every second or so, publishes them, and marks them as sent.
- **Log tailing.** A tool reads the database's own change log and publishes every new outbox row.

### Change data capture

Log tailing is a kind of **change data capture**, or CDC. Most databases keep a log of changes so it can recover from a crash. In Postgres this is the WAL. The Debezium Postgres connector reads it through logical decoding and a replication slot. Logical decoding turns the WAL into plain row changes. A slot is a stream of those changes that Postgres keeps until the reader has them [@pg-logical]. After a restart, it carries on from where it stopped. [@dbz-pg]

Debezium lists the benefits over polling. It captures every change, with very low delay. It needs no "last updated" column. It can also see deletes. [@dbz-features] With CDC, you can even delete each outbox row right after you insert it. The insert is already in the log, so the table stays empty. [@dbz-blog]

### The inbox: dedup on the consumer side

The relay can publish a message more than once. For example, it might crash after it publishes but before it records that fact. [@ms-outbox] So every consumer must expect duplicates.

The fix is an inbox. The consumer keeps a table of event IDs it has already handled. In one transaction, it does the work and saves the ID. If the ID is already there, it skips the event. [@dbz-blog] This is at-least-once delivery plus an idempotent handler, the same pattern as in [queues vs logs](/learn/queues-vs-logs-delivery-guarantees).

Order matters too. By default, Debezium uses the outbox row's aggregate ID as the Kafka message key, so events for one order stay in order inside a partition. [@dbz-router]

## Trade-offs

| Option | One place to see the flow? | Main risk | Use when |
|---|---|---|---|
| 2PC across services | Yes. | Every service must support it and wait on a coordinator. | Rarely. Only inside one system that supports it well. |
| Saga with choreography | No. The flow is spread across services. | Hard to trace, cycles between services. | Two or three steps with a simple flow. |
| Saga with orchestration | Yes. | The orchestrator is critical and must be reliable. | Many steps, timeouts, human steps, frequent changes. |
| Outbox with a polling relay | n/a | Delay and load from polling the table. | A simple start, with no CDC tooling. |
| Outbox with CDC (Debezium) | n/a | More systems to run and watch. | High volume, low delay, a team that can run Kafka Connect. |
| Dual write (DB, then broker) | n/a | Lost or phantom events after a crash. | Never for events that matter. |

**Simple rule:** start with an orchestrated saga and an outbox. Move to choreography only for short flows where each team owns its part.

## At work: how a senior uses this

- For every multi-service flow, write the table: step, compensation, and "can this be undone?". Put steps that cannot be undone, like shipping or sending money out, last.
- Search the code for "save, then publish". Each one is a dual write. Replace it with an outbox.
- Give every event a unique ID, and make every consumer store the IDs it has handled.
- Make the saga's state visible. Store it in a table or a workflow engine, with a dashboard of stuck sagas older than, say, 15 minutes.
- Add a timeout to every saga step. A step that never answers must lead to a compensation, not a forever "pending".
- Watch the relay. Alert on outbox rows that are old and still unsent, or on CDC lag.

## With AI

### What to ask the agent

> "Here is our checkout flow across Orders, Inventory and Payments. List each step, what commits where, and what compensation undoes it. Mark any step with no compensation."

> "Find every place in this service where we write to the database and then publish to Kafka. Replace each one with an insert into an outbox table in the same transaction."

> "Write a consumer for OrderCreated that stores the event ID and its result in one transaction, and skips IDs it has seen. Add a test that delivers the same event twice."

### What to check yourself

- The outbox insert really is inside the same transaction as the business write. Check the transaction boundaries, not just the function name.
- Every compensation is idempotent. Run it twice in a test.
- Compensations run in reverse order, and a failure in a compensation is retried or raised to a person. It is not silently logged.
- The agent did not add a "publish directly if the outbox is slow" shortcut. That brings the dual write back.
- The consumer's dedup check and its work share one transaction.

## Check yourself

## Go deeper

Before this: [queues vs logs](/learn/queues-vs-logs-delivery-guarantees) and [idempotency, retries and timeouts](/learn/idempotency-retries-timeouts). Next: [checkout and the order state machine](/learn/checkout-and-order-state-machine).
