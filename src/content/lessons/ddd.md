---
topic: ddd
subtitle: Learn how to keep code and business words in step, split a big model into bounded contexts, and draw aggregates around real rules, and when all of this is too much.
status: published
reviewed_by: ai-factcheck
reviewed_on: 2026-11-09
illustrative_story: true
sources:
  - id: evans-ref
    title: "Eric Evans, “Domain-Driven Design Reference: Definitions and Pattern Summaries”, Domain Language, Inc. (2015)"
    url: https://www.domainlanguage.com/wp-content/uploads/2016/05/DDD_Reference_2015-03.pdf
    note: Evans's own free summary of every pattern in his 2003 book, plus newer ones such as domain events. Short definitions, one page each.
  - id: fowler-bc
    title: "Martin Fowler, “Bounded Context”, martinfowler.com (15 January 2014)"
    url: https://martinfowler.com/bliki/BoundedContext.html
    note: Why one model for a whole company fails, with the “meter”, “Customer” and “Product” examples.
  - id: fowler-ddd
    title: "Martin Fowler, “Domain Driven Design”, martinfowler.com (22 April 2020)"
    url: https://martinfowler.com/bliki/DomainDrivenDesign.html
    note: A one-page overview of DDD, where it fits, and which books to read next.
  - id: fowler-agg
    title: "Martin Fowler, “DDD_Aggregate”, martinfowler.com (23 April 2013)"
    url: https://martinfowler.com/bliki/DDD_Aggregate.html
    note: Aggregates and aggregate roots in four short paragraphs, with the order and line items example.
  - id: fowler-evans
    title: "Martin Fowler, “Evans Classification”, martinfowler.com (14 December 2005)"
    url: https://martinfowler.com/bliki/EvansClassification.html
    note: Entities, value objects and services, with examples, and why the words “entity” and “service” confuse people.
  - id: vernon-1
    title: "Vaughn Vernon, “Effective Aggregate Design Part I: Modeling a Single Aggregate”, dddcommunity.org (2011)"
    url: https://www.dddcommunity.org/wp-content/uploads/files/pdf_articles/Vernon_2011_1.pdf
    note: A Scrum tool whose one big Product aggregate kept failing to save. The rules on true invariants and small aggregates.
  - id: vernon-2
    title: "Vaughn Vernon, “Effective Aggregate Design Part II: Making Aggregates Work Together”, dddcommunity.org (2011)"
    url: https://www.dddcommunity.org/wp-content/uploads/files/pdf_articles/Vernon_2011_2.pdf
    note: Reference other aggregates by ID, use eventual consistency between them, and when to break the rules.
  - id: ms-boundaries
    title: "Microsoft, “Identifying domain-model boundaries for each microservice”, .NET Microservices Architecture for Containerized .NET Applications, Microsoft Learn"
    url: https://learn.microsoft.com/en-us/dotnet/architecture/microservices/architect-microservice-container-applications/identify-microservice-domain-model-boundaries
    note: How bounded contexts guide service boundaries, with one user who is a Buyer, a Payer and a Customer in different services.
  - id: ms-ddd-service
    title: "Microsoft, “Designing a DDD-oriented microservice”, .NET Microservices Architecture for Containerized .NET Applications, Microsoft Learn"
    url: https://learn.microsoft.com/en-us/dotnet/architecture/microservices/microservice-ddd-cqrs-patterns/ddd-oriented-microservice
    note: When DDD is worth it and when a simple CRUD service is better, and how to balance small services against chatty ones.
claims_checked:
  - claim: "Evans's three-point summary ends with \"speak a ubiquitous language within an explicitly bounded context\", and DDD \"addresses both tactical and strategic design\"."
    source: evans-ref
    note: "Read in the 2015 reference PDF, Pattern Language Overview and section I. Domain events: ordinarily immutable, typically with a timestamp and the identities of the entities involved."
  - claim: "\"total unification of the domain model for a large system will not be feasible or cost-effective\" is Evans's book as quoted by Fowler, with the meter, Customer and Product examples."
    source: fowler-bc
    note: "Exact words on Fowler's page, footnote 1: Eric Evans in Domain-Driven Design. Not in evans-ref, so attributed as quoted by Fowler."
  - claim: "Vernon's Scrum example: Bill commits a backlog item (Product version 2), Joe's release save fails under optimistic concurrency, caused by false invariants; about 70% of aggregates on one project were a root entity with value-typed properties."
    source: vernon-1
    note: "Part I, pages 1-4. The 70% figure is Niclas Hedhman's Qi4j project, as reported by Vernon. Rule names checked: Model True Invariants In Consistency Boundaries; Design Small Aggregates."
  - claim: "Reference other aggregates by identity, use eventual consistency outside the boundary, one transaction per subscriber with retry by redelivery, Evans's whose-job test, and reasons to break the rules."
    source: vernon-2
    note: "Part II rule headings and the Ask Whose Job It Is and Reasons To Break the Rules sections."
  - claim: "Microsoft: if two microservices need to collaborate a lot, they should probably be the same microservice; use DDD only for complex microservices with significant business rules."
    source: ms-ddd-service
    note: "Exact phrases on the Microsoft Learn page (last updated 2022-04-13)."
quiz:
  - q: Sales, billing and shipping all use the word “customer”, but each needs different fields. What does DDD suggest?
    options:
      - One Customer class with every field, shared by all three teams.
      - A separate model in each bounded context, linked by the customer's ID, with translation where they meet.
      - Rename the word everywhere to “user” so there is no confusion.
    answer: 1
    why: Evans's book, quoted by Fowler, says one model for a large system is not feasible or cost-effective. Each bounded context keeps its own model, and you map between them where they connect.
  - q: Which of these is best modelled as a value object?
    options:
      - A customer, who can change their name and address.
      - An order, which moves from draft to placed to shipped.
      - An amount of money, such as ₹499.00.
    answer: 2
    why: You care only about the amount and the currency. Two equal amounts are the same, and you replace a value instead of changing it. A customer and an order keep an identity while they change.
  - q: In Vernon's Scrum example, why did Joe's save fail after Bill added a backlog item?
    options:
      - Both changes went through one large Product aggregate, so optimistic concurrency rejected Joe's save as stale.
      - The database ran out of connections.
      - Joe broke a real business rule about releases.
    answer: 0
    why: Bill's commit raised the Product version. Joe's change was based on the old version, so it was rejected. The rule it protected was false. Planning an item and scheduling a release do not conflict.
  - q: When a customer places an order, your system must also reduce their loyalty points, which live in another aggregate. What do Vernon's rules suggest?
    options:
      - Load both aggregates and change them in one transaction.
      - Merge Order and LoyaltyAccount into one large aggregate.
      - Save the order, publish an OrderPlaced event, and update the points in a separate transaction.
    answer: 2
    why: Change one aggregate per transaction. Rules that span aggregates use eventual consistency, often through a domain event.
  - q: Your team plans to split a monolith into a Product service, a Customer service and an Order service, one per main table. What is the main risk?
    options:
      - The services will be too small to run in containers.
      - Most features need data from several of them, so the services become chatty and must change together.
      - Each service will need its own programming language.
    answer: 1
    why: Splitting by table ignores how the business works. Microsoft notes that if two services must collaborate a lot, they should probably be one service.
incidents: []
glossary_optout: [kernel, process, span]
related: [hexagonal, monolith-microservices, checkout-orders, sagas-outbox-cdc, money-ledgers, billing-metering, solid-coupling]
---

## What broke

This story is made up, but each part is common. An online shop sells kitchenware. One `products` table serves the catalogue team, the billing team and the warehouse team. It has 64 columns and a flag called `active`.

The catalogue team wanted to reshoot photos of a popular steel bottle. So a developer set `active = false` to hide its page for a week. That was the meaning of "active" in catalogue code.

The warehouse restock job also read `active`. To the warehouse team, it meant "keep ordering this from the supplier". So the job stopped buying bottles.

The page came back a week later. Stock was nearly zero. Over the next weekend, 300 orders waited on bottles the shop did not have. Support spent days on apology emails.

Nobody wrote a bug. Each team used the word correctly, in its own sense. The code had one word for two different ideas.

## The idea in simple words

In the [hexagonal architecture lesson](/learn/clean-and-hexagonal-architecture), you put business rules in a core with no framework or database code. That tells you *where* the rules live. **Domain-driven design** (DDD) tells you *how to shape* them.

Fowler describes DDD as building software around a domain model with "a rich understanding of the processes and rules of a domain". [@fowler-ddd] The name comes from Eric Evans's 2003 book. Fowler says it fits complex domains with "a lot of often-messy logic". [@fowler-ddd]

Evans sums DDD up in three points. Focus on the core domain. Explore models together with business experts. And "speak a ubiquitous language within an explicitly bounded context." [@evans-ref]

DDD works at two levels. Evans says it "addresses both tactical and strategic design". [@evans-ref] **Strategic design** splits a large system into bounded contexts and maps how they relate. [@fowler-ddd] [@fowler-bc] **Tactical design** gives you building blocks inside one context: entities, value objects, aggregates and domain events. [@evans-ref] [@vernon-1]

> [!key] **The key idea.** One word should mean one thing inside one part of the system. Draw a boundary around each part, give it its own model, and translate where the parts meet. Inside each part, group objects around the business rules that must always hold.

## How it works

### A shared language

Business experts and developers often use different words for the same things. Evans warns that the words of daily talk are often disconnected from the words in the code. [@evans-ref] So the team agrees on one **ubiquitous language**. It is used in talk, in documents, and "in the code". [@evans-ref]

Evans adds that "a change in the language is a change to the model". [@evans-ref] If the experts start saying "reserve stock", rename `holdItems()` to `reserveStock()`. Experts should object to awkward terms, and developers should watch for unclear ones. [@evans-ref]

### Bounded contexts

One language for the whole company does not work. Fowler saw the word "meter" mean subtly different things to different groups at a power company. He says the same confusion comes up again and again with "Customer" and "Product". [@fowler-bc] Fowler quotes Evans's book: "total unification of the domain model for a large system will not be feasible or cost-effective". [@fowler-bc]

A **bounded context** is a boundary, "typically a subsystem, or the work of a particular team", inside which one model applies. [@evans-ref] Different contexts can model the same idea in completely different ways. [@fowler-bc] Microsoft's example: one person is a User in one service, a Buyer in another, a Payer in a third and a Customer in a fourth. [@ms-boundaries]

Step through the shop from the story. Use Next and Back, or the ← and → keys.

[[diagram:product-three-contexts]]

### Context maps

A **context map** names each bounded context and its points of contact, with "explicit translation for any communication". [@evans-ref] Evans lists common relationships. Three you will meet often:

- **Shared kernel.** Two teams share a small part of the model and its code. Keep it small. Neither team changes it without talking to the other. [@evans-ref]
- **Customer/supplier.** One team depends on another. The downstream team's needs are part of the upstream team's planning. [@evans-ref]
- **Anti-corruption layer.** You depend on a system whose model does not fit yours, such as a legacy app. You build a layer that translates its model into your own words. [@evans-ref]

### Entities and value objects

An **entity** has an identity that lasts while its data changes. An order is still the same order after its address changes. Evans says "the model must define what it means to be the same thing." [@evans-ref]

A **value object** has no identity. You care only about its values, as with an amount of money. [@evans-ref] [@fowler-evans] Treat it as immutable. [@evans-ref] To change one, you replace it with a new one. [@fowler-evans]

Watch the words. Fowler notes that "entity" is often used to mean a database table, which is not the DDD sense. [@fowler-evans]

### Aggregates: consistency boundaries

An aggregate is "a cluster of domain objects that can be treated as a single unit", such as an order and its line items. [@fowler-agg] One entity is the **aggregate root**. Outside code holds references only to the root, and the root enforces the rules. [@evans-ref] Apply rules synchronously inside the boundary and asynchronously across boundaries. [@evans-ref]

Vaughn Vernon shows why size matters. A Scrum tool modelled Product as one big aggregate holding all backlog items, releases and sprints. [@vernon-1] Bill added a backlog item, and the Product version went up. Joe then scheduled a release. His save failed, because his copy was based on the old version. [@vernon-1] No real rule linked the two changes. Vernon calls these "false invariants". [@vernon-1]

His four rules:

1. **Model true invariants in consistency boundaries.** An invariant is a rule that must always be true. Only objects tied by such a rule belong in one aggregate. [@vernon-1]
2. **Design small aggregates.** On one project, about 70% of aggregates were just a root entity with some value properties. [@vernon-1]
3. **Reference other aggregates by identity**, not by object. [@vernon-2]
4. **Use eventual consistency outside the boundary.** [@vernon-2]

He also says to change only one aggregate per transaction in most cases. [@vernon-1] These are guidelines, not laws. He lists reasons to break them, such as batch screens or having no messaging system. [@vernon-2]

How do you pick? Vernon passes on a test from Evans. Is it the job of the user doing this action to make the data consistent? Then try to make it transactional, while still following the other aggregate rules. If it is another user's job, or the system's, let it be eventual. [@vernon-2]

### Domain events

A **domain event** records "something happened that domain experts care about". [@evans-ref] It is usually immutable and often carries a timestamp and the IDs of the entities involved. [@evans-ref] Vernon uses events to update other aggregates, each in its own transaction, with retries if one fails. [@vernon-2]

### A worked example

The Ordering context has an `Order` aggregate. `Money` is a value object. Customers and products are referenced only by ID.

```ts
// ordering/money.ts: a value object
export class Money {
  private constructor(readonly paise: number) {}
  static inr(paise: number): Money {
    if (!Number.isInteger(paise) || paise < 0) throw new Error("bad amount");
    return new Money(paise);
  }
  add(o: Money) { return Money.inr(this.paise + o.paise); }
  times(n: number) { return Money.inr(this.paise * n); }
  equals(o: Money) { return this.paise === o.paise; } // equal values, equal objects
}

// ordering/order.ts: Order is the aggregate root
type Line = { productId: string; qty: number; unitPrice: Money };
export type OrderPlaced = { type: "OrderPlaced"; orderId: string; customerId: string; totalPaise: number; at: string };

export class Order {
  private lines: Line[] = [];
  private status: "draft" | "placed" = "draft";
  readonly events: OrderPlaced[] = [];
  constructor(readonly id: string, readonly customerId: string) {}

  addLine(productId: string, qty: number, unitPrice: Money) {
    if (this.status !== "draft") throw new Error("order already placed");
    if (!Number.isInteger(qty) || qty < 1 || qty > 10) throw new Error("qty must be 1 to 10");
    this.lines.push({ productId, qty, unitPrice });
  }
  total(): Money {
    return this.lines.reduce((sum, l) => sum.add(l.unitPrice.times(l.qty)), Money.inr(0));
  }
  place(now: Date) {
    if (this.status !== "draft" || this.lines.length === 0) throw new Error("cannot place");
    this.status = "placed";
    this.events.push({ type: "OrderPlaced", orderId: this.id, customerId: this.customerId,
      totalPaise: this.total().paise, at: now.toISOString() });
  }
}
```

The use case saves one aggregate and its event in one transaction. The [outbox from the sagas lesson](/learn/sagas-outbox-and-cdc) then publishes the event.

```ts
await db.transaction(async (tx) => {
  const order = await orders.get(tx, orderId);
  order.place(new Date());
  await orders.save(tx, order);       // one aggregate per transaction
  await outbox.add(tx, order.events); // the event commits with it
});
```

Billing calls a customer an "account". Its translation layer turns Ordering's words into its own. Teams often call any translator like this an anti-corruption layer, though Evans keeps that name for the defensive kind.

```ts
// billing/from-ordering.ts: Billing's own copy of the message shape
type OrderPlacedMessage = { orderId: string; customerId: string; totalPaise: number };

export async function onOrderPlaced(e: OrderPlacedMessage) {
  if (await invoices.existsFor(e.orderId)) return; // fast path for repeats
  const accountId = await accounts.idForCustomer(e.customerId); // their word → ours
  // invoices.order_ref is UNIQUE; the insert uses ON CONFLICT (order_ref) DO NOTHING
  await invoices.openOnce({ accountId, orderRef: e.orderId, amountPaise: e.totalPaise });
}
```

The outbox may deliver the same event twice, even at the same moment. The unique `order_ref` column is what stops a second invoice. The early return only saves work.

Line items, prices and quantity limits live in one place. Billing never imports `Order`. The [money and ledgers lesson](/learn/money-and-double-entry-ledgers) explains why amounts are whole paise.

### From contexts to services

Bounded contexts are a good first guide to service boundaries. Microsoft notes that one bounded context may hold several services, "but not vice versa". [@ms-boundaries] You chose well if typical operations rarely need data from several models. [@ms-boundaries]

A common trap is one service per table: Product, Customer, Order. Most features then need all three. Microsoft says that if two services must collaborate a lot, "they should probably be the same microservice". [@ms-ddd-service] Splitting along contexts, not tables, is the idea the [monolith and microservices lesson](/learn/monolith-modular-monolith-microservices) builds on.

### When DDD is too much

Microsoft says to use DDD only for "complex microservices with significant business rules". A CRUD service can stay simple. [@ms-ddd-service] It also notes that people find the patterns hard to learn. The important part is aligning code with the business and its words. [@ms-ddd-service]

Fowler notes the book has "a reputation for being a hard book to read", but says it repays the effort. [@fowler-ddd] A common mistake is the opposite of skipping DDD: using every tactical pattern in a simple app, and skipping the talks with experts. [@ms-ddd-service] Evans himself says to spend the best effort on the core domain. For generic parts, he suggests off-the-shelf solutions. [@evans-ref]

## Trade-offs

| Option | Same word, same meaning? | Cost | Use it when |
|---|---|---|---|
| One shared model for everything | No. Each team bends the shared words. | Low at first. Changes get risky as teams grow. | A small app with one team and few rules. |
| Bounded contexts in a modular monolith | Yes, inside each module. | Translation code where modules meet. | The default for a growing product with several teams. |
| Bounded contexts as separate services | Yes, inside each service. | Network calls, eventual consistency, more to run. | Teams need to deploy on their own, and the boundaries are proven. |
| Full tactical DDD (aggregates, events) | Yes. | More classes and more design time. | The core domain, where rules are complex and change often. |
| Plain CRUD | Not a concern. | Lowest. | Admin screens and generic parts with few rules. |

## At work: how a senior uses this

- When a meeting gets confused, ask: **"Which team's meaning of this word are we using?"** Write the answer down.
- Keep a short glossary per context in the repo. Rename code when the business words change.
- Before you add a field to a shared table, ask which contexts read it, and whether they read it the same way.
- When you design an aggregate, list its true invariants first. If you cannot name one rule that links two objects, keep them apart.
- If a use case changes two aggregates in one transaction, ask whose job the consistency is. Most of the time, an event will do.
- Draw the context map before you draw the service map.
- Skip the tactical patterns for CRUD screens. Spend the effort where the business makes its money.

## With AI

### What to ask the agent

> "List every place in this codebase that reads or writes the products.active column. For each one, say in one line what the code assumes 'active' means."

> "Here are three of our team glossaries. Find words that appear in more than one, and show where the meanings differ."

> "This Order class is saved with its customer and every product in one transaction. List the business rules it enforces. For each rule, say which objects it really needs. Suggest smaller aggregates that reference others by ID."

### What to check yourself

- The agent's rules came from the code or from experts, not from its guesses about your business.
- Each proposed aggregate protects at least one real invariant. It is not just a group of related tables.
- Cross-aggregate updates use events, with retries and idempotent handlers.
- The translation layer turns their words into yours. Your model does not import the other context's types.
- Simple CRUD parts did not get new layers they do not need.

## Check yourself

## Go deeper

Before this: [clean and hexagonal architecture](/learn/clean-and-hexagonal-architecture). Next: [monolith, modular monolith or microservices](/learn/monolith-modular-monolith-microservices). Related: [checkout and the order state machine](/learn/checkout-and-order-state-machine), [sagas, outbox and CDC](/learn/sagas-outbox-and-cdc), [billing, metering and usage pricing](/learn/billing-metering-usage-pricing) and [SOLID, coupling and cohesion](/learn/solid-coupling-and-cohesion).
