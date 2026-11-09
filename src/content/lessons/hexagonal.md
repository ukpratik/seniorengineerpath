---
topic: hexagonal
subtitle: Learn how to keep business rules apart from the web framework and the database, why tests get faster, and when the extra layers are not worth it.
status: published
illustrative_story: true
sources:
  - id: cockburn-hex
    title: "Alistair Cockburn, “The Hexagonal (Ports & Adapters) Architecture”, HaT Technical Report 2005.02 (4 September 2005)"
    url: https://alistair.cockburn.us/hexagonal-architecture/
    note: The original article. The intent, ports as conversations, driving and driven sides, and why the hexagon has six sides only for drawing.
  - id: palermo-onion
    title: "Jeffrey Palermo, “The Onion Architecture : part 1” (29 July 2008)"
    url: https://jeffreypalermo.com/2008/07/the-onion-architecture-part-1/
    note: Layers around a domain model, repository interfaces in the core, and the database as an outer layer.
  - id: martin-clean
    title: "Robert C. Martin, “The Clean Architecture”, The Clean Code Blog (13 August 2012)"
    url: https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html
    note: The four circles, the dependency rule, and how data crosses a boundary.
  - id: bogard-vsa
    title: "Jimmy Bogard, “Vertical Slice Architecture” (19 April 2018)"
    url: https://www.jimmybogard.com/vertical-slice-architecture/
    note: A critique of layered designs and an alternative that groups code by request.
  - id: vocke
    title: "Ham Vocke, “The Practical Test Pyramid”, martinfowler.com (26 February 2018)"
    url: https://martinfowler.com/articles/practical-test-pyramid.html
    note: The database integration test section, and why testing against an in-memory database instead of PostgreSQL is a risk.
claims_checked:
  - claim: "Cockburn's intent is that an app can \"equally be driven by users, programs, automated test or batch scripts, and to be developed and tested in isolation from its eventual run-time devices and databases\"; a port \"identifies a purposeful conversation\"; the hexagon is not a hexagon \"because the number six is important\"; adapters come as primary/secondary, also called driving and driven."
    source: cockburn-hex
    note: "Checked word for word on the raw page, HaT Technical Report 2005.02, \"Date: 2005-09-04\"; Adapter pattern credit is in the Acknowledgements."
  - claim: "Martin's dependency rule says \"source code dependencies can only point inwards\"; the four circles are Entities, Use Cases, Interface Adapters, Frameworks and Drivers, with \"no rule\" that there must be only four; data crosses \"in the form that is most convenient for the inner circle\"."
    source: martin-clean
    note: "Post dated 13 August 2012; lists Hexagonal and Onion among the architectures it integrates."
  - claim: "Palermo's Onion Architecture puts the domain model in the centre, \"all coupling is toward the center\", repository interfaces in the core and implementations at the edge, \"The database is not the center. It is external.\", and it \"is not appropriate for small websites\"."
    source: palermo-onion
    note: "Part 1 posted 8:08 am on July 29, 2008; appropriate for long-lived business apps \"as well as\" apps with complex behaviour."
  - claim: "Bogard's team moved away from an onion architecture after cracks showed within a couple of months; he calls layered approaches \"really only appropriate in a minority of the typical requests\", says \"Minimize coupling between slices, and maximize coupling in a slice\", and the style assumes the team \"understands code smells and refactoring\"."
    source: bogard-vsa
    note: "Post dated 19 April 2018; inner quotes around \"layers\" changed to single quotes, harmless."
  - claim: "Vocke's example uses an in-memory H2 database instead of PostgreSQL and warns that \"going with an in-memory database is risky business\" because tests run against a different database than production."
    source: vocke
    note: "Integration tests section of The Practical Test Pyramid, final installment 26 February 2018."
reviewed_by: ai-factcheck
reviewed_on: 2026-11-09
quiz:
  - q: In Clean Architecture, which direction may source code dependencies point?
    options:
      - Outward, from the business rules to the database and framework.
      - Inward, toward the business rules.
      - Both ways, as long as each layer has interfaces.
    answer: 1
    why: Martin's dependency rule says source code dependencies "can only point inwards".
  - q: Your PlaceOrder use case needs to save orders. Where does the OrderRepository interface belong?
    options:
      - In the core, next to PlaceOrder, written in the core's words.
      - In the Postgres package, next to the SQL code.
      - In the HTTP controller, which creates everything.
    answer: 0
    why: The core owns the port. Palermo also puts repository interfaces in the core and their implementations at the edge.
  - q: An HTTP handler and a unit test both call PlaceOrder. In Cockburn's words, what are they?
    options:
      - Driven adapters.
      - Ports.
      - Driving (primary) adapters, because they start the work.
    answer: 2
    why: Cockburn calls an actor that drives the application primary, and also uses the name driving adapters.
  - q: An admin tool only reads and writes rows in 6 tables, with almost no rules. What is a reasonable design?
    options:
      - Full hexagonal layers with mappers for each table.
      - A simple design that talks to the database directly, because there is little core to protect.
      - A separate microservice per table.
    answer: 1
    why: Palermo says his design is "not appropriate for small websites". With few rules, the layers cost more than they save.
  - q: Your unit tests use an in-memory adapter. What else do you still need?
    options:
      - Nothing. The in-memory adapter proves the SQL works.
      - A few integration tests that run the Postgres adapter against a real PostgreSQL.
      - More mocks inside the core.
    answer: 1
    why: The fake does not run your SQL at all. Even an in-memory SQL database is "risky business", Vocke warns, because it differs from production.
incidents: []
related: [solid-coupling, design-patterns, deep-modules, testing-strategy, monolith-microservices]
---

## What broke

This story is made up, but each part is common. Ravi works on the orders service of an online pharmacy. The `POST /orders` handler is 250 lines long. It reads the JSON body, checks the prescription rules, applies discounts and writes SQL to save the order.

The rules for prescription drugs changed. Ravi only needed to change one check. But the check sat between an Express `req` object and a database transaction. To test it, he had to start a server and a real Postgres in Docker.

The test suite took 14 minutes. People stopped running it before they pushed. Two weeks later, a discount bug reached production. Nobody had run the test that would have caught it.

Then the company wanted orders from a nightly CSV import too. The rules lived inside the HTTP handler, so the import team copied them. Now the same rules existed in two places, and they slowly drifted apart.

The business rules were tangled with HTTP and SQL. That made them slow to test and hard to reuse.

## The idea in simple words

In the [SOLID lesson](/learn/solid-coupling-and-cohesion), you learned dependency inversion: business code owns a small interface, and details implement it. This lesson applies that idea to a whole application.

In 2005, Alistair Cockburn described Ports and Adapters, also called the **hexagonal architecture**. [@cockburn-hex] His goal: an app should be "driven by users, programs, automated test or batch scripts" equally. It should also be "developed and tested in isolation from its eventual run-time devices and databases." [@cockburn-hex]

The app sits in the middle. It talks to the outside only through **ports**. Cockburn says a port "identifies a purposeful conversation". [@cockburn-hex] An **adapter** translates between a port and one real technology. There will "typically be multiple adapters for any one port". [@cockburn-hex] Cockburn credits the Adapter pattern from the *Design Patterns* book with helping him understand the hexagon. [@cockburn-hex] The [design patterns lesson](/learn/design-patterns-that-matter) covers that pattern. Here it is used at the edge of the app.

The six sides mean nothing. Cockburn writes that "the number six" is not important. The shape just leaves room to draw more ports. [@cockburn-hex]

> [!key] **The key idea.** Put the business rules in a core that imports no framework and no database driver. The core owns its interfaces. Everything outside depends on the core, never the other way round.

## How it works

Step through one order. Use Next and Back, or the ← and → keys.

[[diagram:ports-adapters-order]]

### Two sides: driving and driven

Cockburn splits the outside world in two. A primary actor "drives the application", such as a user or a test. A secondary actor is one "that the application drives", such as a database. [@cockburn-hex] He says you can also call them driving adapters and driven adapters. [@cockburn-hex] Ports come in the same two kinds.

### The core: a use case and a port

A use case is one thing a user can do, such as "place an order". `PlaceOrder.run` is the driving port: the API the core offers to the outside. Some teams also write it as an interface. The core also defines the driven port it needs, in its own words.

```ts
// core/orders.ts: no imports from express or pg
export type Order = { id: string; userId: string; totalCents: number };

export interface OrderRepository {          // a driven port
  save(order: Order): Promise<void>;
  countToday(userId: string): Promise<number>;
}

export class PlaceOrder {                   // a use case
  constructor(private repo: OrderRepository) {}
  async run(userId: string, totalCents: number): Promise<Order> {
    if (await this.repo.countToday(userId) >= 5) throw new Error("daily limit");
    const order = { id: crypto.randomUUID(), userId, totalCents };
    await this.repo.save(order);
    return order;
  }
}
```

### Driven adapters: Postgres and in-memory

```ts
// adapters/pg-orders.ts
import { Pool } from "pg";
import type { Order, OrderRepository } from "../core/orders"; // points inward

export class PgOrderRepository implements OrderRepository {
  constructor(private db: Pool) {}
  async save(o: Order) {
    await this.db.query("INSERT INTO orders (id, user_id, total_cents) VALUES ($1, $2, $3)",
      [o.id, o.userId, o.totalCents]);
  }
  async countToday(userId: string) {
    const r = await this.db.query("SELECT count(*) FROM orders WHERE user_id = $1 AND created_at >= current_date", [userId]);
    return Number(r.rows[0].count);
  }
}

// test/in-memory-orders.ts
export class InMemoryOrderRepository implements OrderRepository {
  orders: Order[] = [];
  async save(o: Order) { this.orders.push(o); }
  // in a test, every saved order counts as "today"
  async countToday(userId: string) { return this.orders.filter(o => o.userId === userId).length; }
}
```

### A driving adapter: HTTP

```ts
// adapters/http.ts
import type { Express } from "express";
import type { PlaceOrder } from "../core/orders"; // points inward

export function orderRoutes(app: Express, placeOrder: PlaceOrder) {
  app.post("/orders", async (req, res) => {
    try {
      const order = await placeOrder.run(req.body.userId, req.body.totalCents);
      res.status(201).json(order);
    } catch (e) {
      if ((e as Error).message === "daily limit") res.status(409).json({ error: "daily limit" });
      else res.status(500).json({ error: "internal" });
    }
  });
}

// main.ts: the only file that knows every piece
orderRoutes(app, new PlaceOrder(new PgOrderRepository(pool)));
```

The adapter turns HTTP into a call, and a core error into a status code. The CSV import from the story becomes a second driving adapter. It calls the same `run` method. The rules now live in one place.

### Onion and Clean: the same idea

In 2008, Jeffrey Palermo described the **Onion Architecture**. The domain model is the centre, and "all coupling is toward the center." [@palermo-onion] Repository interfaces live in the core. Their implementations live at the edge. "The database is not the center. It is external." [@palermo-onion]

In 2012, Robert C. Martin wrote "The Clean Architecture". He listed hexagonal and onion among the designs it brings together. [@martin-clean] He drew four circles: Entities, Use Cases, Interface Adapters, and Frameworks and Drivers. [@martin-clean] He adds that there is no rule you must have only four. [@martin-clean]

His **dependency rule**: "source code dependencies can only point inwards." [@martin-clean] When the inner code must call outward, you use the dependency inversion principle. [@martin-clean] Data that crosses a boundary takes "the form that is most convenient for the inner circle." [@martin-clean]

So the three names share one idea. Business rules sit in the middle. Frameworks and databases are details at the edge. Dependencies point inward.

### How it helps testing

The unit test drives the use case with the in-memory adapter:

```ts
test("blocks the sixth order of the day", async () => {
  const repo = new InMemoryOrderRepository();
  const place = new PlaceOrder(repo);
  for (let i = 0; i < 5; i++) await place.run("u1", 100);
  await expect(place.run("u1", 100)).rejects.toThrow("daily limit");
});
```

No server, no Docker, no network. Cockburn suggests a mock, in-memory database as the natural test stand-in for a database. [@cockburn-hex] Most rule tests can now be fast unit tests, at the bottom of the [test pyramid](/learn/testing-strategy).

The fake does not run your SQL. Vocke uses an in-memory database in his example, but warns it is "risky business". The tests then run against a different database than production. [@vocke] So keep a few integration tests that run `PgOrderRepository` against a real PostgreSQL.

### The costs and critiques

**More files to open.** One feature now needs a use case, a port, two adapters and the wiring. For simple features, that is a lot of reading.

**Mapping between layers.** Martin wants data to cross in the inner circle's form. [@martin-clean] So you often copy fields from a request object to a core type, and from a core type to a database row. Each copy is code to write and test.

**Too much for simple CRUD.** CRUD means create, read, update and delete: screens that only save and load rows, with few rules. Palermo says his design "is not appropriate for small websites". He aims it at long-lived business apps and at apps with complex behaviour. [@palermo-onion] If a screen just saves a form to a table, the core has nothing to protect.

**Interfaces with one implementation.** The SOLID and [design patterns](/learn/design-patterns-that-matter) lessons warned about these. Here, a driven port is usually worth it, because the test adapter is a real second implementation. A port for every class in the core is not. Add ports only at the edges: the database, the clock, other services, the message queue.

**Too many rules and layers.** Jimmy Bogard says layered, onion and clean designs are "really only appropriate in a minority of the typical requests in a system". [@bogard-vsa] He says he tends to see them as "mock-heavy, with rigid rules around dependency management". [@bogard-vsa] He adds that one feature often means "touching many different 'layers'". [@bogard-vsa]

### A middle path

You do not need the full design everywhere. Start with a clean core only for the part with real rules, such as pricing or order limits. Leave simple CRUD screens simple. Inside a [modular monolith](/learn/monolith-modular-monolith-microservices), each module can make this choice for itself.

### Vertical slices: another option

Bogard's team once started a long project with an onion architecture. Within a couple of months, problems showed, and they moved away from it, towards CQRS. [@bogard-vsa] His vertical slice architecture groups code by request, not by layer. His rule: "Minimize coupling between slices, and maximize coupling in a slice." [@bogard-vsa] Each slice "can decide for itself how to best fulfill the request". [@bogard-vsa] A simple slice can use SQL directly. A complex one can have a rich core.

It has a cost too. Bogard says it assumes "your team understands code smells and refactoring". [@bogard-vsa] Without that, logic can grow messy inside each slice.

## Trade-offs

| Option | Fast tests? | Cost | Use it when |
|---|---|---|---|
| Handler calls the database directly | No. Rules need a server and a database to test. | Low at first. Rules get copied as new entry points appear. | Simple CRUD with few rules. |
| Hexagonal, Onion or Clean core | Yes. Rules run against in-memory adapters. | More files, ports and mapping code. | Complex rules that live for years and have several entry points. |
| Clean core for one complex module only | Yes, where it matters. | Two styles in one codebase. | A good default for many teams. Protect the hard part. |
| Vertical slices | Yes, if each slice keeps its logic testable. | Needs steady refactoring; some code repeats across slices. | Many independent features and a team comfortable with refactoring. |

## At work: how a senior uses this

- Ask: "Can I test this rule without a server or a database?" If not, the rule is tangled with a detail.
- Check the imports. Code in the core should not import the web framework, the ORM or the SQL driver. Tools such as dependency-cruiser or eslint-plugin-boundaries (TypeScript) and ArchUnit (Java) can check this in CI.
- Name ports in business words: `OrderRepository.countToday`, not `executeQuery`.
- Add a port only at a real edge, and name its second adapter, often the test one.
- Keep one integration test per driven adapter against the real tool.
- Before you add layers to a CRUD screen, count its business rules. If there are none, keep it simple.
- When an entry point is added (a queue consumer, a CLI, a cron job), make it a new driving adapter. Do not copy the rules.

## With AI

### What to ask the agent

> "List every file under src/core that imports express, pg, an ORM or an HTTP client. For each import, say what the core uses it for."

> "Find business rules that appear in more than one HTTP handler or job. Show the duplicated lines side by side."

> "Extract the order limit rule from this handler into a PlaceOrder use case with an OrderRepository port. Add an in-memory adapter and one unit test. Do not change the HTTP behaviour."

### What to check yourself

- The core has no imports from framework or database packages.
- The port uses business words, not table or column names.
- The agent did not add an interface for every class inside the core.
- The in-memory adapter behaves like the real one for the cases you test. A real-database integration test still exists.
- The HTTP status codes and JSON shape did not change.

## Check yourself

## Go deeper

Before this: [SOLID, coupling and cohesion](/learn/solid-coupling-and-cohesion). Related: [complexity and deep modules](/learn/complexity-and-deep-modules), [design patterns that still matter](/learn/design-patterns-that-matter), [testing strategy](/learn/testing-strategy) and [monolith, modular monolith or microservices](/learn/monolith-modular-monolith-microservices). A later lesson on domain-driven design builds on this one.
