---
topic: solid-coupling
subtitle: Learn what coupling, cohesion and the five SOLID principles really say, and when following them blindly makes code harder to change.
status: published
illustrative_story: true
sources:
  - id: smc-1974
    title: "W. P. Stevens, G. J. Myers and L. L. Constantine, “Structured design”, IBM Systems Journal 13(2), 1974 (reprinted in IBM Systems Journal 38(2&3), 1999, pp. 231–256; DOI 10.1147/sj.382.0231)"
    url: https://scrapbox.io/files/607d58d8fb61eb001eee7175.pdf
    note: The link is an unofficial copy of the 1999 reprint; the official version is behind the DOI. The paper that explained coupling and cohesiveness ("binding"), with the scale of binding and the warning about ripple effects.
  - id: liskov-wing-1994
    title: "Barbara H. Liskov and Jeannette M. Wing, “A Behavioral Notion of Subtyping”, ACM Transactions on Programming Languages and Systems 16(6), November 1994, pp. 1811–1841"
    url: https://www.cs.columbia.edu/~wing/publications/LiskovWing94.pdf
    note: Pages 1811–1812 cover behaviour "as far as anyone … can tell", the subtype requirement, and the stack-versus-queue example.
  - id: martin-srp
    title: "Robert C. Martin, “The Single Responsibility Principle”, The Clean Code Blog (8 May 2014)"
    url: https://blog.cleancoder.com/uncle-bob/2014/05/08/SingleReponsibilityPrinciple.html
    note: SRP explained as "one reason to change", where the reasons are people, with links back to Parnas and Constantine.
  - id: martin-ocp
    title: "Robert C. Martin, “The Open Closed Principle”, The Clean Code Blog (12 May 2014)"
    url: https://blog.cleancoder.com/uncle-bob/2014/05/12/TheOpenClosedPrinciple.html
    note: Bertrand Meyer's 1988 wording of open and closed modules, and Martin's plugin reading of it.
  - id: martin-relevance
    title: "Robert C. Martin, “Solid Relevance”, The Clean Code Blog (18 October 2020)"
    url: https://blog.cleancoder.com/uncle-bob/2020/10/18/Solid-Relevance.html
    note: One short line for each principle, and Martin's reply to Dan North's criticism.
  - id: north-cupid
    title: "Dan North, “CUPID: for joyful coding” (10 February 2022)"
    url: https://dannorth.net/blog/cupid-for-joyful-coding/
    note: Why North wanted to replace SOLID, his critique of SRP, and five properties of code instead of rules.
  - id: dip-wild
    title: "Brett L. Schuchert, “DIP in the Wild”, martinfowler.com (May 2013)"
    url: https://martinfowler.com/articles/dipInTheWild.html
    note: Dependency inversion versus dependency injection, domain-relevant abstractions, and the cost of every abstraction.
  - id: connascence
    title: "connascence.io, “What is Connascence?”"
    url: https://connascence.io/
    note: Connascence as a vocabulary for kinds of coupling, judged by strength, degree and locality.
  - id: connascence-position
    title: "connascence.io, “Connascence of Position”"
    url: https://connascence.io/position.html
    note: Two functions that must agree on the order of values in a list, and how naming the fields weakens the coupling.
  - id: ouster-modular
    title: "John Ousterhout, “Modular Design”, CS 190 lecture notes, Stanford (Winter 2018)"
    url: https://web.stanford.edu/~ouster/cgi-bin/cs190-winter18/lecture.php?topic=modularDesign
    note: Deep and shallow classes, and classitis.
  - id: aposd-vs-cc
    title: "John Ousterhout and Robert C. Martin, “A Philosophy of Software Design vs Clean Code” (GitHub, 2024–2025)"
    url: https://github.com/johnousterhout/aposd-vs-clean-code
    note: A written debate on how far to split code, including the "One Thing" rule.
claims_checked:
  - claim: Stevens, Myers and Constantine define coupling as "the measure of the strength of association established by a connection from one module to another", recommend "the weakest possible coupling between modules", and treat binding (cohesiveness) as the second way to reduce coupling, with coincidental binding lowest on the scale.
    source: smc-1974
    note: Quotes checked in the 1999 reprint text, pp. 232-233 and 237; the link is an unofficial copy, official DOI 10.1147/sj.382.0231.
  - claim: Martin states SRP as "each software module should have one and only one reason to change", says "It is people who request changes", and restates it as gathering things that change for the same reasons.
    source: martin-srp
    note: Exact wording checked on the 8 May 2014 post; the "actor" wording is not used there or in the lesson.
  - claim: Subtype objects should behave like supertype objects "as far as anyone or any program using supertype objects can tell"; properties provable about T should hold for subtype S; a stack and a queue may both type-check with put/get, yet code for one is unlikely to work with the other.
    source: liskov-wing-1994
    note: Read from the scanned pages 1811 (introduction quote) and 1812 (subtype requirement, stack/queue, type checking captures only a small part of correctness).
  - claim: Schuchert summarises "DI is about wiring, IoC is about direction, and DIP is about shape", advises capturing low-level dependencies in domain-relevant abstractions, and warns that "all abstractions have a cost".
    source: dip-wild
    note: Exact quotes checked; article dated 21 May 2013.
  - claim: Martin wrote that North's slides "completely miss the point" on SRP, called "Just write simple code" good advice, and added that "simplicity requires disciplines guided by principles"; North says splitting a report's content from its format "creates artificial seams" when both change together.
    source: martin-relevance
    note: The reader's letter cites North's position, not slides; North's example checked in north-cupid.
reviewed_by: ai-factcheck
reviewed_on: 2026-11-08
quiz:
  - q: An Invoice class computes the total, renders a PDF and saves itself to the database. Finance, design and the DBA team all ask for changes to it. What does SRP suggest?
    options:
      - Split it so each method lives in its own class.
      - Split it along the three reasons to change, so each group's changes stay in one place.
      - Leave it, because it is about one thing, invoices.
    answer: 1
    why: Martin defines SRP by reasons to change, which are people. Split by those reasons, not by method count.
  - q: A GiftCardGateway implements PaymentGateway, but its refund method always throws "not supported". PaymentGateway promises that refunds work. The code compiles. Which principle does it break?
    options:
      - Liskov substitution, because callers of PaymentGateway can no longer rely on refund working.
      - None, because the types match.
      - Open-closed, because the class was modified.
    answer: 0
    why: Liskov and Wing say a subtype must behave as its supertype promises. Matching types is not enough.
  - q: What is the difference between dependency inversion and dependency injection?
    options:
      - They are two names for the same thing.
      - Injection is about how an object gets its dependency. Inversion is about the shape of the interface and who owns it.
      - Inversion needs a framework. Injection does not.
    answer: 1
    why: In Schuchert's summary, "DI is about wiring" and "DIP is about shape".
  - q: Your codebase has 60 interfaces. Each one has exactly one class that implements it, and no test uses a fake. What is the likely problem?
    options:
      - Not enough interfaces yet.
      - Shallow layers that add names without hiding anything.
      - Strong cohesion.
    answer: 1
    why: An interface with one implementation and no second user often hides nothing. Ousterhout warns that too many small, shallow classes add complexity.
incidents: []
related: [deep-modules, testing-strategy, tech-debt, code-review]
---

## What broke

This story is made up, but both halves are common. Kiran's team runs the checkout service of a shoe store. The `Checkout` class creates an `SmtpMailer` and calls it to send order emails. Refunds and sign-ups do the same.

Then the company moved to an HTTP email API. The new client took different arguments. Kiran had to edit checkout, refunds and sign-up. Each change needed new tests and a new deploy. Worse, the checkout tests had always opened a real SMTP connection, so they were slow and often failed in CI.

A new tech lead said, "We need SOLID." The team added an interface in front of every class. `OrderService` got `IOrderService`. `PriceCalculator` got `IPriceCalculator`. Six months later there were 60 interfaces, each with exactly one implementation.

Now a new developer had to jump through two files for every call. The vendor problem was solved, but adding a field to an order touched more files than before.

The first design had too much coupling. The second added layers that hid nothing. Both made change slow.

## The idea in simple words

In the [deep modules lesson](/learn/complexity-and-deep-modules), you learned to hide each hard decision behind a small interface. This lesson gives you the older words for the same goal, and five popular rules built on them.

In 1974, Stevens, Myers and Constantine published "Structured design" in the IBM Systems Journal. They called coupling "the measure of the strength of association established by a connection from one module to another". [@smc-1974] Strong coupling makes a module harder to understand, change or fix on its own. They advised "the weakest possible coupling between modules". [@smc-1974]

Fewer connections also mean fewer paths for changes and errors to spread. The paper calls these "ripple" effects. [@smc-1974] Kiran's vendor change was one.

The second idea is cohesion. The paper called it cohesiveness, measured by "binding": how strongly the parts inside one module belong together. [@smc-1974] You lower coupling in two ways at once: fewer links between modules, and stronger links inside each one. [@smc-1974] Their weakest kind is "coincidental" binding, where the parts have no real relationship. [@smc-1974]

> [!key] **The key idea.** Put things that change together in one place. Keep things that change for different reasons apart. SOLID gives five more specific rules that serve this goal, and each one can be overdone.

SOLID names five principles that Robert C. Martin teaches: single responsibility, open-closed, Liskov substitution, interface segregation and dependency inversion. [@martin-relevance] He links his single responsibility principle back to Parnas and to Constantine's coupling and cohesion. [@martin-srp]

## How it works

Step through the vendor change in two designs. Use Next and Back, or the ← and → keys.

[[diagram:dependency-inversion-swap]]

### S: single responsibility

Martin states SRP as "each software module should have one and only one reason to change". [@martin-srp] He says the reasons are people: "It is people who request changes." [@martin-srp] His short form is: "Gather together the things that change for the same reasons. Separate those things that change for different reasons." [@martin-srp]

```ts
class Invoice {            // finance owns the maths
  total(): Money { /* ... */ }
}
class InvoicePdf {         // design owns the layout
  render(inv: Invoice): Buffer { /* ... */ }
}
class InvoiceStore {       // the database team owns the tables
  save(inv: Invoice): void { /* ... */ }
}
```

**Misreading:** "a class should do one thing", so every method gets its own class. That is not how Martin defines it. Ousterhout calls small classes taken to the extreme **classitis**. [@ouster-modular] In their debate, he says the related "One Thing" rule for methods "is simply wrong in many cases". [@aposd-vs-cc]

### O: open-closed

Martin quotes Bertrand Meyer's 1988 book: a module is open if it is "available for extension", and closed if it is "available for use by other modules". [@martin-ocp] Martin reads this as plugins: high-level policy stays apart from low-level detail. [@martin-ocp]

```ts
interface PriceRule { apply(cart: Cart, price: number): number; }

function finalPrice(cart: Cart, rules: PriceRule[]): number {
  return rules.reduce((p, rule) => rule.apply(cart, p), cart.subtotal);
}
```

A new discount is a new `PriceRule`. You do not edit `finalPrice`.

**Misreading:** "never edit old code", or "add plug-in points everywhere". You cannot guess every future change. An extension point that nobody uses is cost with no benefit.

### L: Liskov substitution

Liskov and Wing argue that subtyping is about behaviour. Objects of the subtype should behave like the supertype "as far as anyone or any program using supertype objects can tell." [@liskov-wing-1994] Any property you can prove about type T should also hold for a subtype S. [@liskov-wing-1994]

Their example: a stack and a queue can both have `put` and `get`. The type checker may accept either. But code written for a stack is unlikely to work with a queue. [@liskov-wing-1994]

In the code below, `PaymentGateway` promises that `refund` returns the money to the customer.

```ts
interface PaymentGateway {
  charge(orderId: string, cents: number): Promise<void>;
  refund(orderId: string, cents: number): Promise<void>; // returns the money
}
class GiftCardGateway implements PaymentGateway {
  async charge(orderId: string, cents: number) { /* ... */ }
  async refund(orderId: string, cents: number) {
    throw new Error("not supported");   // compiles, breaks callers
  }
}
```

**Misreading:** "the subclass compiles, so it is fine". Type checking catches only a small part of correctness. [@liskov-wing-1994] Martin's short form: a program that uses an interface "must not be confused by an implementation of that interface." [@martin-relevance]

### I: interface segregation

Martin's line: "Keep interfaces small so that users don't end up depending on things they don't need." [@martin-relevance] The gift card fix is two small interfaces:

```ts
interface Charger  { charge(orderId: string, cents: number): Promise<void>; }
interface Refunder { refund(orderId: string, cents: number): Promise<void>; }
class CardGateway implements Charger, Refunder { /* ... */ }
class GiftCardGateway implements Charger { /* ... */ }
```

**Misreading:** one method per interface, everywhere. Split by what each caller needs, not by method count.

### D: dependency inversion

Martin's line: "High level modules should not depend upon low level details." [@martin-relevance] Schuchert adds: capture low-level dependencies in domain-relevant abstractions. [@dip-wild]

```ts
// orders/order-notifier.ts: owned by the orders module
export interface OrderNotifier { orderPlaced(order: Order): Promise<void>; }

export class Checkout {
  constructor(private notifier: OrderNotifier) {}
  async place(order: Order) { /* save ... */ await this.notifier.orderPlaced(order); }
}

// main.ts: the only line that knows the vendor
const checkout = new Checkout(new EmailApiNotifier(apiClient));
```

The interface speaks the orders language, `orderPlaced`, not `sendSmtp(host, port, mime)`.

**Misreading:** "dependency inversion means a DI framework". The last line is dependency injection. Schuchert puts it this way: "DI is about how one object acquires a dependency." In his summary, "DI is about wiring, IoC is about direction, and DIP is about shape." [@dip-wild] You can inject a concrete class and gain nothing. He also warns that "all abstractions have a cost". [@dip-wild]

### Connascence: finer words for coupling

Connascence is "a taxonomy for different types of coupling". [@connascence] It judges each case by strength, degree (how many things are involved) and locality (how far apart they are). [@connascence] For example, connascence of position means two parts "must agree on the order of values". Reading fields by name instead makes the coupling weaker. [@connascence-position]

### The critiques

Dan North's CUPID started as a light-hearted challenge to SOLID. At first he wanted to replace each principle with something more useful. [@north-cupid] He gives an example: splitting a report's content from its format, as SRP is often explained, "creates artificial seams" when both change together. [@north-cupid] He prefers properties to principles: "Principles are like rules: you are either compliant or you are not." [@north-cupid]

In 2020, Martin answered a reader who cited North's views on SOLID. He wrote that North's slides "completely miss the point" of SRP. [@martin-relevance] He called North's "Just write simple code" good advice. But he added that "simplicity requires disciplines guided by principles." [@martin-relevance] Read both, then judge each rule by one test: does it make the next real change smaller?

## Trade-offs

| Choice | Easy to change? | Cost | Use it when |
|---|---|---|---|
| Call a concrete class directly | Yes, while it is stable. | Vendor or I/O changes ripple into callers. | The detail is stable and in your own code. |
| Interface owned by the caller, injected | Yes. Swap details in one wiring line. | One more name. A small design effort. | The detail is a vendor, I/O, or needs a fake in tests. |
| Interface for every class | No. Each change touches two files. | Shallow layers, more jumping between files. | Rarely. Remove ones with one implementation and no test fake. |
| Split a class by reason to change | Yes. Each group's change stays in one place. | More classes to name. | Different people ask for different changes. |
| Split until each class does "one thing" | No. Code that changes together is spread out. | Classitis. | Rarely. |

## At work: how a senior uses this

- For a class under review, ask: "Who asks for changes to this?" Two different answers suggest a split. One answer suggests keeping it together.
- Before adding an interface, name the second implementation or the test fake. If you cannot, wait.
- Put interfaces next to the code that uses them, in its language. `OrderNotifier` belongs to orders, not to the email package.
- In code review, look for `throw new Error("not supported")` in an implementation. It usually means a broken substitution or an interface that is too big. See [reviewing code](/learn/reviewing-human-and-ai-code).
- Keep the wiring of concrete classes in one place, such as `main`. Then a vendor change is one line.
- When a change touches many files, write down which coupling caused it. Add it to your [tech debt](/learn/technical-debt-management) list.

## With AI

### What to ask the agent

> "List every place where business code creates or calls this vendor client directly. For each, show the arguments that are specific to the vendor."

> "Find interfaces in this repo with exactly one implementation. For each, say whether any test uses a fake for it."

> "Find classes that implement an interface but throw 'not supported' or do nothing in some methods. Explain which callers could break."

### What to check yourself

- The agent did not add an interface and an `Impl` class for every class to "follow SOLID".
- New interfaces use the caller's words, not the vendor's method names.
- A suggested split groups code by who asks for changes, not by method count.
- Behaviour did not change. Run the tests before and after. The [testing strategy lesson](/learn/testing-strategy) shows where fakes help.

## Check yourself

## Go deeper

Before this: [complexity and deep modules](/learn/complexity-and-deep-modules). Next: [testing strategy](/learn/testing-strategy), where these seams make tests fast. Later lessons on design patterns, clean and hexagonal architecture, and low-level object-oriented design build on this one. Related: [managing technical debt](/learn/technical-debt-management) and [reviewing code written by humans and AI](/learn/reviewing-human-and-ai-code).
