---
topic: lld
subtitle: Learn a repeatable method for designing the classes of a small system, carried through one parking lot, from the first question to the race at two gates.
status: published
illustrative_story: true
sources:
  - id: hi-lld
    title: "Hello Interview, “Low Level Design in a Hurry”"
    url: https://www.hellointerview.com/learn/low-level-design
    note: What a low-level design round tests, how it differs from system design, and the five things interviewers judge.
  - id: hi-delivery
    title: "Hello Interview, “Low Level Design Interview Delivery Framework”"
    url: https://www.hellointerview.com/learn/low-level-design/in-a-hurry/delivery
    note: Five steps with rough times, from requirements to extensibility, and the warning against forcing patterns.
  - id: hi-parking
    title: "Hello Interview, “Parking Lot Low Level Design”"
    url: https://www.hellointerview.com/learn/low-level-design/problem-breakdowns/parking-lot
    note: A sample scope for the parking lot, what is out of scope, and three follow-up questions. The full answer is behind a paywall.
  - id: hi-correctness
    title: "Hello Interview, “Correctness in Low Level Design”"
    url: https://www.hellointerview.com/learn/low-level-design/concurrency/correctness
    note: The two-users-one-seat race, explained as a check and an action done in two separate steps.
  - id: hi-principles
    title: "Hello Interview, “Design Principles”"
    url: https://www.hellointerview.com/learn/low-level-design/in-a-hurry/design-principles
    note: Short principles for interviews, including building only what you need now.
  - id: fowler-tda
    title: "Martin Fowler, “Tell Dont Ask”, martinfowler.com (5 September 2013)"
    url: https://martinfowler.com/bliki/TellDontAsk.html
    note: Telling an object what to do instead of asking for its data, and Fowler's own doubts about the rule.
  - id: fowler-anemic
    title: "Martin Fowler, “Anemic Domain Model”, martinfowler.com (25 November 2003)"
    url: https://martinfowler.com/bliki/AnemicDomainModel.html
    note: Objects that are "bags of getters and setters", with all logic in service classes, and why Fowler calls it an anti-pattern.
  - id: guru-large-class
    title: "Refactoring.Guru, “Large Class”"
    url: https://refactoring.guru/smells/large-class
    note: How classes grow too big over time, and the refactorings that split them.
  - id: gamma-principles
    title: "Bill Venners, “Design Principles from Design Patterns: A Conversation with Erich Gamma, Part III”, Artima Developer (6 June 2005)"
    url: https://www.artima.com/articles/design-principles-from-design-patterns
    note: Gamma on composition over inheritance, and why he calls inheritance brittle.
  - id: java-chm
    title: "Oracle, “ConcurrentHashMap (Java SE 21 & JDK 21)”"
    url: https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/ConcurrentHashMap.html
    note: The thread-safe map, and the exact behaviour of remove and putIfAbsent.
  - id: java-atomicref
    title: "Oracle, “AtomicReference (Java SE 21 & JDK 21)”"
    url: https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/atomic/AtomicReference.html
    note: compareAndSet, which sets a new value only if the current one is still the expected one.
claims_checked:
  - claim: Hello Interview's delivery framework has five steps (requirements ~5 min, entities ~3, class design ~10-15, implementation ~10, extensibility ~5), about 35 minutes in total.
    source: hi-delivery
    note: Times and quotes checked in the freely visible text.
  - claim: Hello Interview's sample parking-lot scope has three vehicle types and hourly pricing rounded up, puts payments, gate hardware, cameras, UI and reservations out of scope, and warns against a pricing engine with surge pricing and discounts "unless they explicitly ask for it".
    source: hi-parking
    note: All quoted text is before the paywall.
  - claim: The seat race is described as "Both users checked availability before either completed their booking", with the check and the action done "as two separate steps".
    source: hi-correctness
    note: Exact quotes in the free Problem section.
  - claim: AtomicReference.compareAndSet sets the new value only if the current value == the expected value and returns false otherwise, so only one gate's tryTake succeeds.
    source: java-atomicref
    note: Javadoc checked; snippet compiled on Java 21 and a two-thread test showed no double booking in 20,000 runs.
  - claim: All ConcurrentHashMap operations are thread-safe and remove returns the previous value or null, so only one concurrent exit gets the ticket.
    source: java-chm
    note: Javadoc checked; confirmed by the same two-thread test.
  - claim: Gamma calls inheritance "a cool way to change behavior" but brittle, and says favouring composition is "still true even after ten years".
    source: gamma-principles
    note: Exact quotes checked; interview dated 6 June 2005.
quiz:
  - q: An LLD interviewer says "Design a parking lot." What should you do in the first few minutes?
    options:
      - Draw the full class diagram with every vehicle type you can think of.
      - Ask questions until the prompt is a small spec, and say what is out of scope.
      - Pick the design patterns you will use, so the interviewer sees them early.
    answer: 1
    why: Hello Interview's framework starts with about 5 minutes of requirements, where you make the prompt unambiguous and name what is out of scope.
  - q: Car, Motorcycle and Truck differ only in which spot size they need. How should you model them?
    options:
      - One Vehicle type with a size field, because there is no different behaviour.
      - An abstract Vehicle class with three subclasses, so you can add more later.
      - A VehicleFactory and a Vehicle interface with three classes.
    answer: 0
    why: Subclasses that add no behaviour are inheritance used as labels. A field holds the difference, and a new size is one enum value.
  - q: Two gates call enter() at the same moment. Both see spot 14 as free and both issue a ticket for it. What is the root problem?
    options:
      - The spot list is not sorted.
      - Checking "is it free?" and taking the spot happen as two separate steps.
      - The ticket IDs are not unique.
    answer: 1
    why: This is check-then-act. Another thread can change the state between the check and the action, so the two must happen as one atomic step.
  - q: The interviewer asks for different prices for cars and trucks. Which change fits best?
    options:
      - Add an if on vehicle size inside ParkingLot.exit().
      - Put the fee rule behind a small PricingPolicy interface that ParkingLot calls.
      - Make CarTicket and TruckTicket subclasses of Ticket.
    answer: 1
    why: Pricing is now a real change pressure, so it earns a seam. ParkingLot keeps one job, and a new price rule is a new class.
glossary_optout: [availability, stock-reservation, deadlock, atomic]
incidents: []
reviewed_by: ai-factcheck
reviewed_on: 2026-11-09
related: [solid-coupling, design-patterns, deep-modules, concurrency-primitives, interview-loops, system-design-method, testing-strategy]
---

## What broke

This story is made up, but the pattern is common. Ravi had a 45-minute low-level design round. The prompt was one line: "Design a parking lot."

He started drawing at once. `Vehicle` got subclasses `Car`, `Bike` and `Truck`. A `ParkingLotManager` class grew 20 methods: find a spot, print tickets, take payment, send SMS. He added a factory, a singleton and two strategies, to show he knew patterns.

At minute 30 the interviewer asked, "Two cars arrive at two gates at the same moment. Can both get spot 14?" Ravi looked at his code. Both gates read the spot as free, then both took it. He had no answer.

Then came the second question: "We want different prices for trucks." The price logic sat in four methods of the manager. He had to edit all four.

The feedback said: "Knows the patterns. Did not clarify scope. Design breaks under change and under concurrent use." Ravi had written more code than the strong candidates. It was the wrong code.

## The idea in simple words

A low-level design (LLD) round gives you a small, self-contained problem. You design its classes, their methods and how they work together. Hello Interview puts it this way: these rounds "test your ability to structure code for a self-contained problem". The round is also called object-oriented design. [@hi-lld]

In a [system design round](/learn/system-design-interview-method), you draw services, databases and caches for scale. In an LLD round, you design the inside of one program. Hello Interview's summary: "System design is the map. Low level design is the blueprint for a building on the map." [@hi-lld]

Interviewers judge five things: how you analyse the problem, your class design, code quality, how easily the design takes a follow-up change, and how you explain it. [@hi-lld] Code quality includes choosing composition or inheritance, managing state and the direction of dependencies. [@hi-lld] Where the round sits in a full loop is in the [interview loops lesson](/learn/big-tech-interview-loops).

> [!key] **The key idea.** Put each rule in the object that owns the data it needs. Add an interface or a pattern only where you can name a change that is coming. Then ask what happens when two callers touch the same object at once.

You already have the tools. [SOLID, coupling and cohesion](/learn/solid-coupling-and-cohesion) told you to group what changes together. [Design patterns that still matter](/learn/design-patterns-that-matter) told you to start from the problem, not the pattern. This lesson is the method that uses both, under time pressure.

## How it works

Step through the parking lot design as it grows. Use Next and Back, or the ← and → keys.

[[diagram:parking-lot-design]]

Hello Interview's framework has five steps, about 35 minutes in total. [@hi-delivery] Here is a version you can repeat.

### 1. Clarify requirements and scope

The prompt is vague on purpose. Your job is to "turn it into a spec you can actually design around". [@hi-delivery] Spend the first minutes asking. [@hi-delivery]

For the parking lot, Hello Interview picks a sample scope. There are three vehicle types: motorcycle, car and large vehicle. Entry gives a free, compatible spot and a ticket. Exit takes the ticket, works out the fee and frees the spot. The price is hourly, rounded up. [@hi-parking] Payments, gate hardware, cameras, the UI and reservations are out of scope. [@hi-parking] Say that list out loud. It protects your time.

### 2. Find the core objects and their responsibilities

Pull out the meaningful nouns from your requirements. [@hi-delivery] For the parking lot: lot, spot, ticket, vehicle. But "don't turn every noun into a class. Some things are just data." [@hi-parking]

Give each object one job, in one sentence:

- `ParkingLot`: takes cars in and out, and owns the spots and the active tickets.
- `Spot`: knows its size and whether it is taken.
- `Ticket`: records which vehicle is in which spot, and since when.
- `Vehicle`: just data, a plate and a size.

The framework's rule: "keep rules with the entity that owns the relevant state." [@hi-delivery] Fowler calls the same habit tell-don't-ask: "rather than asking an object for data and acting on that data, we should instead tell an object what to do." [@fowler-tda] So the lot tells a spot "take this ticket". It does not read a `taken` flag and set it from outside.

### 3. Choose the relationships

The lot **has** spots. That is composition: spots belong to the lot. A ticket **refers to** a spot and a vehicle. That is association: it points at them but does not own them.

Should `Car` and `Truck` be subclasses? Only if they behave differently. Here they differ only in size, so a size field is enough. Gamma calls inheritance "a cool way to change behavior", but brittle. A subclass "can easily make assumptions" about when the parent calls the methods it overrides. [@gamma-principles] Here the variants have no behaviour to change. He still backs the GoF advice to favour composition, ten years on. [@gamma-principles]

### 4. Write the interfaces and the few methods that matter

You have about 10 minutes for code. Start with the happy path, then the edge cases. [@hi-delivery] Ask whether they want real code or pseudo-code. [@hi-delivery] Two methods matter here: `enter` and `exit`. Like a [deep module](/learn/complexity-and-deep-modules), they hide the hard parts.

```java
// imports: java.time.*, java.util.*, java.util.concurrent.*, java.util.concurrent.atomic.*
enum Size { SMALL, MEDIUM, LARGE }
record Vehicle(String plate, Size size) {}
record Ticket(String id, Vehicle vehicle, Spot spot, Instant in) {}

final class Spot {
  private final Size size;
  private final AtomicReference<Ticket> holder = new AtomicReference<>();
  Spot(Size size) { this.size = size; }

  boolean fits(Vehicle v) { return size.compareTo(v.size()) >= 0; }
  boolean tryTake(Ticket t) { return holder.compareAndSet(null, t); }
  void release(Ticket t)    { holder.compareAndSet(t, null); }
}
```

`Spot` has no public setter. Callers cannot put it into a bad state.

### 5. Add a pattern only where a change is coming

A common follow-up asks for different prices by vehicle type. [@hi-parking] Now pricing has a real reason to change, so it earns a small interface:

```java
interface PricingPolicy { long feeCents(Ticket t, Instant out); }

final class ParkingLot {
  private final List<Spot> spots;
  private final Map<String, Ticket> active = new ConcurrentHashMap<>();
  private final PricingPolicy pricing;
  private final Clock clock;
  // constructor that sets all four is left out

  Ticket enter(Vehicle v) {
    for (Spot s : spots) {
      if (!s.fits(v)) continue;
      Ticket t = new Ticket(UUID.randomUUID().toString(), v, s, clock.instant());
      if (s.tryTake(t)) { active.put(t.id(), t); return t; }
    }
    throw new IllegalStateException("no free spot for " + v.size());
  }

  long exit(String ticketId) {
    Ticket t = active.remove(ticketId);
    if (t == null) throw new IllegalArgumentException("unknown or used ticket");
    t.spot().release(t);
    return pricing.feeCents(t, clock.instant());
  }
}
```

This is the Strategy pattern from the patterns lesson, used for one known need. Before the interviewer asked, a plain hourly method on the lot was enough. Hello Interview warns against a pricing engine with surge prices and discounts "unless they explicitly ask for it". [@hi-parking]

The `Clock` is passed in, so a test can fix the time. The [testing strategy lesson](/learn/testing-strategy) shows why that matters.

### 6. Handle state and concurrency

Two gates means two threads. Hello Interview describes the classic race for a seat: "Both users checked availability before either completed their booking." [@hi-correctness] The check and the action "happened as two separate steps". [@hi-correctness] This bug is called check-then-act.

The code above makes them one step. `compareAndSet` sets the new value only if the current value is still the expected one. It returns `false` if another thread got there first. [@java-atomicref] So one gate wins spot 14. The other moves on to the next spot.

Exit has the same risk. Someone might use one ticket twice. All `ConcurrentHashMap` operations are thread-safe, and `remove` returns the old value or `null`. [@java-chm] So only one exit gets the ticket back. Locks, atomics and deadlocks are covered in depth in [locks, atomics and deadlocks](/learn/locks-atomics-and-deadlocks).

### 7. Answer the extensibility questions

Expect follow-ups such as many floors, prices by vehicle type, or many entrances. [@hi-parking] Their goal is to show "that your initial design is extensible and robust". [@hi-delivery] Answer each one by naming the class that changes. Many floors: `ParkingLot` holds floors, and each floor holds spots. A new price: a new `PricingPolicy`. If one answer touches five classes, say so and propose a fix.

## Trade-offs

| Choice | Easy to change? | Cost | Use it when |
|---|---|---|---|
| A field or enum for the variant (`Size`) | Yes. A new size is one value. | No place for behaviour that differs. | Variants differ only in data. |
| Subclass per variant (`Car`, `Truck`) | No. Subclasses depend on the parent's details. | More classes, brittle hierarchy. | Variants really behave differently, and the parent's contract is stable. |
| Interface plus implementations (`PricingPolicy`) | Yes. A new rule is a new class. | One more name to learn. | You can name a second rule, or the interviewer asks. |
| Logic in one manager class | Yes, at first. | It grows into a god class. Every change touches it. | A first sketch only. Split it before you finish. |
| Atomic step on the object (`tryTake`) | Yes. | You must think about which steps must be one. | Two callers can change the same object. |
| One lock around the whole lot | Yes. | All gates wait for each other. | Low traffic, or a quick first answer you then improve. |

## At work: how a senior uses this

- In a design review or a pull request, ask each new class "What is your one job?" If the answer has "and" three times, it needs a split.
- Watch for a god class: a manager or service that keeps growing. Refactoring.Guru notes that classes "usually start small" but bloat as the program grows. [@guru-large-class] Split it with Extract Class. [@guru-large-class]
- Look for objects that are only getters and setters, with all rules in a service. Fowler calls this an anemic domain model. He calls it "really just a procedural style design". [@fowler-anemic] Move the rule next to the data it reads.
- Do not chase getters everywhere, though. Fowler himself says, "personally, I don't use tell-dont-ask". He still puts data and behaviour together, but he has seen the rule push people to remove every getter. [@fowler-tda]
- When you see a new abstract base class, ask what behaviour the subclasses change. If none, suggest a field.
- When you see a new interface, ask for the second implementation. If nobody can name it, wait. Hello Interview says: "Build what you need now, not what you might need later." [@hi-principles]
- For every shared object, ask: "What if two requests call this at once?" Look for a read, a decision and a write as three separate lines.

## With AI

### What to ask the agent

> "Here is my parking lot spec and its out-of-scope list. Propose the classes, with one sentence of responsibility each. Do not add any design pattern I did not ask for."

> "In this class design, find every place where code reads a value, decides, then writes it back in a separate step. For each, say what two threads could do to break it."

> "List the classes in this diff that only have getters and setters. For each, name the service method that holds its rules."

### What to check yourself

- The agent did not turn every noun into a class or add subclasses that change no behaviour.
- Every pattern it added solves a change you can name.
- The concurrency fix makes the check and the action one atomic step. A `synchronized` method that only wraps the check is not a fix.
- The code compiles and a test with two threads passes many times in a row.

## Check yourself

## Go deeper

Before this: [SOLID, coupling and cohesion](/learn/solid-coupling-and-cohesion) and [design patterns that still matter](/learn/design-patterns-that-matter). Related: [complexity and deep modules](/learn/complexity-and-deep-modules), [locks, atomics and deadlocks](/learn/locks-atomics-and-deadlocks), [testing strategy](/learn/testing-strategy), [the system design interview method](/learn/system-design-interview-method) and [big tech interview loops](/learn/big-tech-interview-loops).
