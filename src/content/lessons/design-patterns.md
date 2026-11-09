---
topic: design-patterns
subtitle: Learn the few design patterns you will meet every week, how modern languages made them smaller, and how to spot a pattern that makes code worse.
status: published
illustrative_story: true
sources:
  - id: alexander-1977
    title: "Christopher Alexander, Sara Ishikawa and Murray Silverstein, with Max Jacobson, Ingrid Fiksdahl-King and Shlomo Angel, A Pattern Language: Towns, Buildings, Construction, Oxford University Press, 1977 (catalogue record, Canadian Centre for Architecture)"
    url: https://www.cca.qc.ca/en/search/details/library/publication/03132495
    note: The library record of the book that started the pattern idea, with a summary of how each pattern is built.
  - id: gof-book
    title: "Erich Gamma, Richard Helm, Ralph Johnson and John Vlissides, Design Patterns: Elements of Reusable Object-Oriented Software, Addison-Wesley Professional, published 31 October 1994 (copyright 1995) (publisher page, InformIT)"
    url: https://www.informit.com/store/design-patterns-elements-of-reusable-object-oriented-9780201633610
    note: The publisher page of the "Gang of Four" book, with its description of the 23 patterns.
  - id: norvig-1996
    title: "Peter Norvig, “Design Patterns in Dynamic Languages” (slides, first presented 5 May 1996)"
    url: https://norvig.com/design-patterns/
    note: Slides 7–16, 20–21 and 37–39. The three levels of a pattern, the "16 of 23" count, and Strategy and Iterator as plain functions.
  - id: gamma-use
    title: "Bill Venners, “How to Use Design Patterns: A Conversation with Erich Gamma, Part I”, Artima Developer (23 May 2005)"
    url: https://www.artima.com/articles/how-to-use-design-patterns
    note: Gamma on patterns as a vocabulary, why trying to use all of them leads to bad designs, and Alexander as the starting point.
  - id: gamma-principles
    title: "Bill Venners, “Design Principles from Design Patterns: A Conversation with Erich Gamma, Part III”, Artima Developer (6 June 2005)"
    url: https://www.artima.com/articles/design-principles-from-design-patterns
    note: Gamma on composition over inheritance, and Strategy as his main example of it.
  - id: hevery-singleton
    title: "Miško Hevery, “Singletons are Pathological Liars”, Google Testing Blog (17 August 2008)"
    url: https://testing.googleblog.com/2008/08/by-miko-hevery-so-you-join-new-project.html
    note: A test that charges a real credit card shows how singletons hide dependencies and make tests depend on order.
  - id: fowler-yagni
    title: "Martin Fowler, “Yagni”, martinfowler.com (26 May 2015)"
    url: https://martinfowler.com/bliki/Yagni.html
    note: The costs of building a feature before you need it, and why refactoring is not a yagni violation.
  - id: fowler-antipattern
    title: "Martin Fowler, “Anti Pattern”, martinfowler.com (25 August 2015)"
    url: https://martinfowler.com/bliki/AntiPattern.html
    note: Andrew Koenig's definition of an anti-pattern.
  - id: fowler-ioc
    title: "Martin Fowler, “Inversion Of Control”, martinfowler.com (26 June 2005)"
    url: https://martinfowler.com/bliki/InversionOfControl.html
    note: How Template Method and callbacks both let a framework call your code.
  - id: mdn-eventtarget
    title: "MDN Web Docs, “EventTarget”"
    url: https://developer.mozilla.org/en-US/docs/Web/API/EventTarget
    note: The browser's built-in observer, with addEventListener, removeEventListener and dispatchEvent.
  - id: mdn-iteration
    title: "MDN Web Docs, “Iteration protocols”"
    url: https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Iteration_protocols
    note: How any JavaScript object becomes iterable, and which language features use it.
  - id: java-function
    title: "Oracle, “java.util.function (Java SE 21 & JDK 21)”"
    url: https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/function/package-summary.html
    note: Function, Supplier, Predicate and the other interfaces that lambdas fit into.
  - id: java-httprequest-builder
    title: "Oracle, “HttpRequest.Builder (Java SE 21 & JDK 21)”"
    url: https://docs.oracle.com/en/java/javase/21/docs/api/java.net.http/java/net/http/HttpRequest.Builder.html
    note: A builder in the Java standard library, with uri, timeout, header, GET and build.
  - id: express-middleware
    title: "Express, “Using middleware”"
    url: https://expressjs.com/en/guide/using-middleware.html
    note: What a middleware function can do, and why it must call next().
claims_checked:
  - claim: Norvig found "16 of 23 patterns are either invisible or simpler" in Dylan or Lisp than in C++, for at least some uses of each pattern; first-class functions cover four, including Strategy and Template Method.
    source: norvig-1996
    note: Checked in the slide outline (slides 9 and 10) linked from the cited page.
  - claim: Gamma calls Strategy his "prototypical example for the flexibility of composition over inheritance", calls inheritance "brittle", and of "Favor object composition over class inheritance" says "I still think it's true even after ten years."
    source: gamma-principles
    note: Exact quotes checked; interview dated 6 June 2005. The principle is quoted by Venners from the GoF introduction.
  - claim: Gamma warns that "Trying to use all the patterns is a bad thing, because you will end up with synthetic designs" and advises "Do not start immediately throwing patterns into a design", using them "after the fact, refactoring to patterns".
    source: gamma-use
    note: Exact quotes checked; interview dated 23 May 2005.
  - claim: Hevery says "Singletons are nothing more than global state"; in his example, reordering the init() calls breaks the test.
    source: hevery-singleton
    note: Article dated 17 August 2008.
  - claim: Koenig, quoted by Fowler, says an antipattern gives "something that looks superficially like a solution but isn't one"; Fowler names four costs of presumptive features (build, delay, carry, repair).
    source: fowler-antipattern
    note: Yagni costs checked in fowler-yagni ("four kinds of costs").
  - claim: The publisher describes the GoF book as "a catalog of simple and succinct solutions to commonly occurring design problems" with 23 patterns, published 31 October 1994 (copyright 1995).
    source: gof-book
    note: InformIT page; preface dated August 1994.
reviewed_by: ai-factcheck
reviewed_on: 2026-11-08
quiz:
  - q: A function picks a shipping price with a switch over 12 carriers. New carriers arrive every month, and a change for one carrier once broke another. What is the simplest good fix in TypeScript?
    options:
      - A map from carrier name to a pricing function, with a clear error for unknown carriers.
      - An abstract base class, a factory and a singleton registry of carrier classes.
      - Keep the switch and add more comments.
    answer: 0
    why: The problem is one decision with many variants. With first-class functions, a Strategy can be just a function value.
  - q: Your tests pass alone but fail when run together in a different order. The code reads its config through Config.getInstance(). What is the likely cause?
    options:
      - The test runner is flaky.
      - A singleton keeps global state between tests, and the code hides that it depends on it.
      - The tests need more mocks.
    answer: 1
    why: Hevery calls singletons "nothing more than global state". Classes then use things their APIs do not declare.
  - q: You must add timing around many fee rules, without editing each rule. Which approach fits?
    options:
      - Builder. Give each rule a builder object.
      - Singleton. Put one global timer inside every rule.
      - Decorator. A function takes a rule and returns a timed rule with the same signature.
    answer: 2
    why: A decorator wraps a call and keeps the same interface, so callers and rules do not change.
  - q: What did Erich Gamma advise about when to apply patterns?
    options:
      - Use them as you go, once the problem shows up, by refactoring toward them.
      - Plan all patterns at the start, before you write code.
      - Use as many as you can, so the design is flexible.
    answer: 0
    why: Gamma prefers "refactoring to patterns" and warns that trying to use all of them gives "synthetic designs".
incidents: []
related: [solid-coupling, deep-modules, testing-strategy, tech-debt, error-handling]
---

## What broke

This story is made up, but both halves are common. Ana works on a food delivery app. The delivery fee lives in one function, `deliveryFee(order)`. It started with one `if` for each of three cities.

Two years later it had nine cities, a rain surcharge and a late-night rule. The function was 300 lines long. A change for Pune once broke the fee in Delhi, because both cases used the same variable.

A new teammate had just read about design patterns. He rewrote the code with a `FeeStrategy` interface, a `FeeStrategyFactory`, an `AbstractFeeRule` base class and a `FeeRegistry` singleton. That was 11 new files.

The Delhi bug was gone. But now the tests failed only when they ran in a certain order. One test put a fake rule into the shared `FeeRegistry`. The next test still saw it.

They needed one simple pattern. They got four, and one added hidden global state.

## The idea in simple words

A design pattern is a named answer to a problem that keeps coming back in a certain situation. The idea comes from building design. Venners describes Alexander's architecture work as having "inspired the software patterns movement". [@gamma-use] In 1977, Christopher Alexander and his co-authors published *A Pattern Language*. It has more than 250 patterns for towns and buildings. Each has a problem, a discussion and a solution. [@alexander-1977]

In 1994, Erich Gamma, Richard Helm, Ralph Johnson and John Vlissides published *Design Patterns*. People call them the "Gang of Four" (GoF). The publisher describes it as "a catalog of simple and succinct solutions to commonly occurring design problems", with 23 patterns. [@gof-book]

A pattern gives you two things. The first is a shared name. Gamma says patterns "provide you with a vocabulary to describe and discuss a particular design." [@gamma-use] The second is a set of trade-offs, not "a pat solution" (a ready-made answer). [@gamma-use]

Many patterns have become smaller since 1994. In 1996, Peter Norvig found that "16 of 23 patterns are either invisible or simpler" in Lisp or Dylan. He compared them with C++, for at least some uses of each pattern. [@norvig-1996] He called a pattern invisible when it is "so much a part of language that you don't notice". [@norvig-1996] One reason is first-class functions, which simplify four of them, including Strategy and Template Method. [@norvig-1996] TypeScript, Java and Python all have first-class functions today.

> [!key] **The key idea.** Start from the problem, not the pattern. Name the pain first: a growing `if`, a vendor API that does not fit, a step you must add around every call. Then use the smallest form of the pattern that removes it. Often that is a function, not a class.

In the [SOLID lesson](/learn/solid-coupling-and-cohesion), you learned to hide details behind small interfaces that the caller owns. Patterns are common shapes for doing that.

## How it works

Step through Ana's fee code. Use Next and Back, or the ← and → keys.

[[diagram:strategy-fee-rules]]

### Strategy: swap an algorithm

**Problem:** one decision has many variants, and a branch grows with each one. Norvig's slides give the GoF intent: "Define a family of interchangeable algorithms". [@norvig-1996]

In classic Java, the Strategy pattern means an interface and one class per variant. With first-class functions, Norvig writes, "the strategy is a variable whose value is a function". [@norvig-1996]

```ts
type FeeRule = (o: Order) => number;

const rules: Record<string, FeeRule> = {
  blr: () => 30,
  pune: (o) => 20 + 5 * o.km,
};

function deliveryFee(o: Order): number {
  const rule = rules[o.city];
  if (!rule) throw new Error(`no fee rule for ${o.city}`);
  return rule(o);
}
```

Java has had this since lambdas arrived. Interfaces in `java.util.function`, such as `Function<T,R>`, are targets for lambda expressions. [@java-function] Gamma calls Strategy his "prototypical example for the flexibility of composition over inheritance". [@gamma-principles]

### Observer: tell others that something changed

**Problem:** when one object changes, several others must react. The first object should not need to know them all. The intent on Norvig's slide: "When an object changes, notify all interested". [@norvig-1996]

The Observer pattern is now built into most platforms. In the browser, `EventTarget` gives you `addEventListener`, `removeEventListener` and `dispatchEvent`. [@mdn-eventtarget]

```ts
const orders = new EventTarget();
orders.addEventListener("placed", (e) => sendReceipt((e as CustomEvent).detail));
orders.dispatchEvent(new CustomEvent("placed", { detail: order }));
```

Node's event emitters and reactive streams use the same idea. Pub/sub applies it across services. Remove listeners you no longer need. Forgotten ones keep objects in memory.

### Decorator: add a step around a call

**Problem:** you want timing, logging, retries or an auth check around many calls, without editing each one. The Decorator pattern wraps a function or object and keeps the same interface.

```ts
const withTiming = (rule: FeeRule): FeeRule => (o) => {
  const start = performance.now();
  try { return rule(o); }
  finally { metrics.record("fee_ms", performance.now() - start); }
};
rules.pune = withTiming(rules.pune);
```

Norvig's slides show Lisp "around methods" used the same way, for example to add tracing. [@norvig-1996]

Web frameworks give you a related tool, but it is a chain, not a wrapper. In Express, a middleware function gets the request, the response and `next`. If it does not end the request, it "must call `next()`". [@express-middleware] Each middleware runs in turn, so it adds a step before your handler without editing it.

### Adapter: fit an API you do not own

**Problem:** a vendor client has the wrong shape for your code. The Adapter pattern uses a small class that implements your interface by calling theirs. In the SOLID lesson, `EmailApiNotifier` was an adapter. It turned `orderPlaced(order)` into the vendor's call. Keep all vendor types inside the adapter. Then a vendor change touches one file.

### Factory and Builder: create objects in one place

**Factory problem:** callers should not choose the concrete class. In a dynamic language, a class is a value, so the class itself can be the factory. [@norvig-1996] Today a factory is often one function in your wiring code:

```ts
function makeGateway(cfg: Config): PaymentGateway {
  return cfg.env === "test" ? new FakeGateway() : new StripeGateway(cfg.key);
}
```

**Builder problem:** an object has many optional fields. A constructor with eight arguments is easy to call in the wrong order. Java's HTTP client uses a builder. [@java-httprequest-builder]

```java
HttpRequest req = HttpRequest.newBuilder()
    .uri(URI.create("https://api.example.com/orders"))
    .timeout(Duration.ofSeconds(2))
    .header("Accept", "application/json")
    .GET()
    .build();
```

In TypeScript, an options object with named fields usually does the same job.

### Iterator: walk a collection

**Problem:** go through the items without knowing how they are stored. This pattern is now part of most languages. In JavaScript, an object with a `[Symbol.iterator]()` method works with `for...of`, spread and destructuring. [@mdn-iteration] Norvig noted that closures remove the need for iterator classes when you pass a function to each item, as `forEach` does. [@norvig-1996] When you need to step through items yourself, write a generator function, not an `Iterator` class. [@mdn-iteration]

### Template Method or composition

**Problem:** many jobs share the same steps, and only one step differs. Template Method puts the steps in a base class. Subclasses fill in the step that differs. Fowler notes that here "the super-class defines the flow of control". [@fowler-ioc]

This ties each subclass to its parent. Gamma says inheritance is "brittle, because the subclass can easily make assumptions about the context". [@gamma-principles] The GoF book's introduction gives the principle "Favor object composition over class inheritance." [@gamma-principles] In 2005, Gamma said: "I still think it's true even after ten years." [@gamma-principles] So pass the step that differs as a value:

```ts
async function runImport(lines: AsyncIterable<string>, parse: (line: string) => Row) {
  for await (const line of lines) await save(parse(line));
}
```

### When a pattern makes code worse

**Too many patterns.** Gamma warns: "Trying to use all the patterns is a bad thing, because you will end up with synthetic designs." [@gamma-use] He advises: "Do not start immediately throwing patterns into a design". He likes to use them "after the fact, refactoring to patterns". [@gamma-use] Ana's teammate wrote 11 files to do the job of a map of functions.

**Singleton as global state.** Hevery puts it plainly: "Singletons are nothing more than global state." Objects can then reach things their APIs do not declare. [@hevery-singleton] In his example, tests broke when the order of setup calls changed. [@hevery-singleton] If you need one instance, create it once in `main` and pass it in. That is dependency injection.

**Speculative abstraction.** A factory for one class, or a strategy with one variant, is often built for a future that may never come. Fowler names four costs of building for a future need: the cost of build, delay, carry and repair. He says the same argument applies to abstractions built for future flexibility. [@fowler-yagni]

An anti-pattern is the result. Andrew Koenig, quoted by Fowler, calls it "something that looks superficially like a solution but isn't one." [@fowler-antipattern]

## Trade-offs

| Option | Easy to read? | Cost | Use it when |
|---|---|---|---|
| Plain `if` or `switch` | Yes, while it is short. | Grows; cases start to share variables. | Two or three stable cases. |
| Map of functions (Strategy) | Yes. | One lookup, and names for each rule. | One decision with many variants that change often. |
| Interface plus a class per variant | Mostly. | More files and names. | Each variant has its own state or several methods. |
| Base class with steps to fill (Template Method) | No. You read two classes to follow one flow. | Subclasses depend on the parent's details. | A framework you do not own asks for it. |
| Singleton | Yes, at first. | Hidden global state, tests that depend on order. | Rarely. Create one instance and pass it in. |

## At work: how a senior uses this

- Before you name a pattern in a design review, state the problem in one sentence. If you cannot, you do not need the pattern yet.
- Use pattern names to save words in reviews: "This is an adapter. Keep the vendor's types inside it."
- Wait until a branch really grows before you extract a Strategy.
- Prefer the language feature first: a function, an event emitter, a generator.
- Search for `getInstance()` and static mutable fields. Put them on your [tech debt](/learn/technical-debt-management) list, and pass the instance in when you next touch that code.
- In a decorator, keep the error behaviour the same. Rethrow, do not swallow. See [error handling](/learn/error-handling-defensive-design).
- If a factory or interface still has one implementation and no test fake after months, delete it.

## With AI

### What to ask the agent

> "Find switch or if/else chains in this module that choose between variants of one decision. For each, say how often it changed in the last year, using git log."

> "Find classes that use getInstance() or static mutable state. List the callers that depend on them without a constructor parameter."

> "Refactor this switch into a map of functions. Do not add new classes, factories or interfaces. Keep the behaviour and the tests the same."

### What to check yourself

- The agent did not add an interface, a factory and a base class where one function would do.
- Unknown keys in a strategy map fail with a clear error, not `undefined`.
- Decorators keep the same return value and the same errors as the wrapped call.
- The tests pass alone and in a random order.

## Check yourself

## Go deeper

Before this: [SOLID, coupling and cohesion](/learn/solid-coupling-and-cohesion). Related: [complexity and deep modules](/learn/complexity-and-deep-modules). Next: [testing strategy](/learn/testing-strategy). [Low-level and object-oriented design](/learn/low-level-object-oriented-design) builds on this one.
