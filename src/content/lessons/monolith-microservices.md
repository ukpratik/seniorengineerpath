---
topic: monolith-microservices
subtitle: Microservices let teams ship on their own, but every network call has a cost. Learn what each style buys, what it costs, and why a modular monolith is often the best start.
status: published
reviewed_by: ai-factcheck
reviewed_on: 2026-10-21
illustrative_story: true
sources:
  - id: fowler-microservices
    title: "James Lewis and Martin Fowler, “Microservices” (martinfowler.com, March 2014)"
    url: https://martinfowler.com/articles/microservices.html
    note: The common definition of the style, the comparison with a monolith, Conway's law, decentralised data and design for failure.
  - id: fowler-monolith-first
    title: "Martin Fowler, “MonolithFirst” (June 2015)"
    url: https://martinfowler.com/bliki/MonolithFirst.html
    note: Why most successful microservice stories began as a monolith, and why boundaries are hard to get right early.
  - id: fowler-premium
    title: "Martin Fowler, “MicroservicePremium” (May 2015)"
    url: https://martinfowler.com/bliki/MicroservicePremium.html
    note: The extra cost and risk of microservices, and when not to consider them.
  - id: fowler-strangler
    title: "Martin Fowler, “Strangler Fig” (August 2024 version)"
    url: https://martinfowler.com/bliki/StranglerFigApplication.html
    note: How to replace an old system bit by bit instead of in one big rewrite.
  - id: conway
    title: "Melvin E. Conway, “How Do Committees Invent?”, Datamation, April 1968"
    url: https://www.melconway.com/Home/Committees_Paper.html
    note: The original paper behind Conway's law. The main claim is in the conclusion.
  - id: shopify
    title: "Kirsten Westeinde, “Deconstructing the Monolith: Designing Software that Maximizes Developer Productivity”, Shopify Engineering (February 2019)"
    url: https://shopify.engineering/deconstructing-monolith-designing-software-maximizes-developer-productivity
    note: Why Shopify chose a modular monolith over microservices, how they split the code, and the Wedge tool that checks boundaries.
  - id: prime-video
    title: "Marcin Kolny, “Scaling up the Prime Video audio/video monitoring service and reducing costs by 90%”, Prime Video Tech (March 2023, archived copy)"
    url: https://web.archive.org/web/20230601000000/https://www.primevideotech.com/video-streaming/scaling-up-the-prime-video-audio-video-monitoring-service-and-reducing-costs-by-90
    note: One team moved a service from distributed serverless parts into one process, and cut its infrastructure cost by over 90%.
  - id: segment
    title: "Alexandra Noonan, “Goodbye Microservices: From 100s of problem children to 1 superstar”, Segment (July 2018)"
    url: https://www.twilio.com/en-us/blog/developers/best-practices/goodbye-microservices/
    note: Segment merged over 140 small services back into one, and lists what got better and what got worse.
  - id: newman
    title: "Sam Newman, “Monolith to Microservices” (O'Reilly), book page"
    url: https://samnewman.io/books/monolith-to-microservices/
    note: A book on whether to migrate at all, migration patterns such as the strangler fig, and splitting the database.
claims_checked:
  - claim: "A monolith ties change cycles together and must be scaled as a whole. Microservices are independently deployable and scalable. Distributed transactions are \"notoriously difficult to implement\", so systems accept eventual consistency and compensating operations."
    source: fowler-microservices
    note: "Checked against Lewis and Fowler (25 March 2014). Both authors credited. Quote is exact."
  - claim: "Conway: organisations which design systems are \"constrained to produce designs which are copies of the communication structures of these organizations\" (Datamation, April 1968)."
    source: conway
    note: "Quote matches the paper's thesis sentence on melconway.com. Publication and date match the reprint note."
  - claim: "Almost all successful microservice stories began as a monolith that got too big; experienced architects find boundaries hard to get right early; the view is not unanimous."
    source: fowler-monolith-first
    note: "Matches MonolithFirst (3 June 2015), including \"by no means unanimous\"."
  - claim: "Shopify's modular monolith has \"strictly enforced boundaries between different domains\"; components by business area; Wedge flags boundary violations using call data gathered in CI."
    source: shopify
    note: "Definition quote exact. Writer said Wedge itself \"checks, in CI\"; the post says a separate tool builds the call graph during CI and Wedge scores it. Reworded."
  - claim: "Prime Video's monitoring service hit a hard scaling limit at about 5% of expected load; moving to one process cut infrastructure cost by over 90%; the choice is \"case-by-case\"."
    source: prime-video
    note: "Checked on the archived copy (Marcin Kolny, 22 March 2023). Numbers exact. Scoped to this one service; limit came from \"the way we used some components\"."
  - claim: "Segment merged over 140 services into one; 3 full-time engineers spent most of their time keeping the system alive; a bug in one destination can now crash the whole service."
    source: segment
    note: "Matches the post (Alexandra Noonan, 10 July 2018)."
quiz:
  - q: According to Lewis and Fowler, what does a monolith force on you that microservices avoid?
    options:
      - You must use one programming language for ever.
      - Change cycles are tied together, and you scale the whole app, not just the busy part.
      - You cannot use a database.
    answer: 1
    why: In a monolith, a small change means building and deploying the whole app. Scaling also means scaling all of it.
  - q: What is a modular monolith, in Shopify's words?
    options:
      - One application with strictly enforced boundaries between domains.
      - Many small services that share one database.
      - A monolith that runs in several data centres.
    answer: 0
    why: All the code powers one application, but the boundaries between domains are checked. Shopify wanted more modularity without more deployment units.
  - q: Your team of 6 engineers is starting a new product. The domain is still unclear. What does Fowler's MonolithFirst suggest?
    options:
      - Start with one service per feature, so you never have to split later.
      - Start with a monolith, and split it once you understand the boundaries.
      - Wait until the design is perfect before writing code.
    answer: 1
    why: Fowler notes that even experienced architects find it hard to get boundaries right at the start. Almost all successful microservice stories began with a monolith.
  - q: Four services must always be deployed together, and they read the same database tables. What is the best description?
    options:
      - Well-designed microservices.
      - A system with the network costs of microservices but without the independent deploys.
      - A modular monolith.
    answer: 1
    why: Services that must ship together and share tables cannot change on their own. You pay for network calls and get little back.
  - q: Why did Prime Video's monitoring team move to one process?
    options:
      - Microservices never work at high scale.
      - Orchestration and passing data between parts were the biggest costs, and their design hit a scaling limit at about 5% of the expected load.
      - Their company banned AWS Lambda.
    answer: 1
    why: Moving the parts into one process kept the data in memory and cut this service's infrastructure cost by over 90%. The post says the choice must be made case by case.
incidents: []
cases: [shopify-modular-monolith]
related: [ddd, team-topologies, sagas-outbox-cdc, evolutionary-architecture, architecture-tradeoffs, observability-slos]
---

## What broke

This story is made up, but the pattern is common.

A shop has one large app and 40 engineers. Deploys queue up, and one team's bug can block everyone's release. So the company decides to "move to microservices". In six months, the app becomes nine services.

But the services still read and write the same database tables. A new checkout feature needs changes in five services. The five teams must deploy them in the right order, on the same night.

Then a customer is charged, but gets no order. The request crossed four services. Each one has its own logs, and nothing ties them together. It takes two days to find a timeout between two of them.

The team kept the old pain, the shared release. They added new pain: network calls, partial failures and lost traces. They split the code, but not the ownership or the data.

## The idea in simple words

A **monolith** is one application that you build and deploy as one unit. Lewis and Fowler point to two limits. Change cycles are tied together, so a small change means you build and deploy the whole app. And you must scale the whole app, not just the part that needs more resources. [@fowler-microservices]

**Microservices** build one application as a set of small services. [@fowler-microservices] Each service can be deployed and scaled on its own. [@fowler-microservices]

A **modular monolith** sits between them. Shopify describes it as one application in which "there are strictly enforced boundaries between different domains". [@shopify] You get clear parts, but still one deploy.

> [!key] **The key idea.** Microservices mostly buy independence for teams: each team deploys and scales its part on its own. You pay for it with network calls, data spread across databases and more to run. Pay only when the independence is worth more than the cost.

## How it works

### What microservices cost

Step through one order request in both styles. Use Next and Back, or the ← and → keys.

[[diagram:monolith-vs-services-request]]

**Network calls.** A function call in memory becomes a request over the network. Shopify lists this as one reason they did not pick microservices. Network calls add latency and reduce reliability. [@shopify] Lewis and Fowler say services must be designed to cope when other services fail. [@fowler-microservices] So every call needs a [timeout, retries and idempotency](/learn/idempotency-retries-timeouts).

**Data and transactions.** Each service usually owns its data. Lewis and Fowler write that distributed transactions "are notoriously difficult to implement". So microservice systems tend to accept eventual consistency and fix problems with compensating operations. [@fowler-microservices] In practice, that means [sagas, outbox and CDC](/learn/sagas-outbox-and-cdc).

**Running it.** Fowler calls this the "microservice premium". The style adds cost and risk that "often gets projects into serious trouble". You need deployment automation, monitoring, failure handling and a way to deal with eventual consistency. [@fowler-premium] Shopify also lists a test and deploy pipeline for each service, and infrastructure for each one. Large changes across services need coordinated deploys. [@shopify]

To follow one request across services, you also need good [observability](/learn/observability-slos-error-budgets), with traces that cross every hop.

### Conway's law

In 1968, Melvin Conway argued that organisations which design systems are "constrained to produce designs which are copies of the communication structures of these organizations". [@conway] This is Conway's law. Lewis and Fowler give an example. When teams are split by technical layer, such as UI, server and database, even simple changes become projects across teams. They call this "Conway's Law in action". Their answer is cross-functional teams and services organised around business capabilities. [@fowler-microservices]

Our advice: draw the team chart next to the service map. If one team owns six services, it gets all the costs and little of the independence. If one service needs three teams to change it, its boundary is wrong. The lesson on [team topologies and Conway's law](/learn/team-topologies-conways-law) goes deeper.

### The modular monolith: a common middle path

Shopify runs "one of the largest Ruby on Rails codebases in existence". More than a thousand developers have worked on it for over a decade. [@shopify] They judged that microservices "would bring their own set of challenges". [@shopify]

So they kept one app and changed how the code was organised. They moved from folders by software type (models, views, controllers) to components by business area, such as orders, shipping, inventory and billing. [@shopify] A tool records the calls made while tests run in CI. An internal tool called Wedge uses this data to flag calls that cross component boundaries. Calls are allowed only to things a component makes public. [@shopify]

Fowler gives the same default. Do not even consider microservices unless the system is too complex to manage as a monolith. Build most systems as one application with good internal modularity. [@fowler-premium]

### Monolith first, then the strangler fig

Fowler noticed that "almost all the successful microservice stories have started with a monolith that got too big and was broken up". Systems built as microservices from scratch often "ended up in serious trouble". [@fowler-monolith-first] One reason: even experienced architects find it hard to get boundaries right at the start. [@fowler-monolith-first] He also notes that this view is not shared by everyone. [@fowler-monolith-first]

When it is time to split, do not rewrite everything at once. Fowler's **strangler fig** approach grows the new system around the old one. You add new parts next to the old code, then move behaviour across one piece at a time. [@fowler-strangler] Each step is small, so the risk is lower and value arrives sooner. [@fowler-strangler] Sam Newman's book covers this pattern, and how to split the database when you break up a monolith. It also asks a harder question: should you migrate at all? [@newman]

### Going back is allowed

In 2023, a Prime Video team wrote about a monitoring service built from distributed serverless parts. The way the team used some components hit a hard scaling limit at about 5% of the expected load. Its orchestration and the data passed between parts cost the most. [@prime-video] The team moved all the parts into one process, so data moved in memory. The infrastructure cost of this one service fell by over 90%. [@prime-video] The post adds that the choice between microservices and a monolith "has to be made on a case-by-case basis". [@prime-video]

Segment had over 140 small services. Three full-time engineers spent most of their time keeping the system running. [@segment] They merged the services into one. Deploys and tests became much faster. [@segment] The post is honest about the cost: one bad part can now crash the whole service. [@segment]

### When to split, and signs you split too early

Our rules for when one part should become its own service:

- A separate team owns it, and the shared release truly blocks that team.
- It needs very different scaling, hardware or reliability from the rest.
- Its boundary is already clean inside the modular monolith, with few calls across it.

Signs you split too early:

- Most features change several services, which must deploy together.
- Services share database tables.
- You have more services than teams.
- Nobody can follow one request from end to end.

## Trade-offs

| Option | Independent deploys? | Main cost | Use it when |
|---|---|---|---|
| Monolith | No. Change cycles are tied together. [@fowler-microservices] | Shared release. You scale all of it. [@fowler-microservices] | A small team, a new product, a domain still being learned. |
| Modular monolith | No, but teams work in clear parts. [@shopify] | You must check the boundaries, for example with a CI tool. [@shopify] | The default for most growing teams. |
| Microservices | Yes. Each can deploy and scale on its own. [@fowler-microservices] | Network calls, no shared transactions, more to run. [@fowler-premium] [@shopify] | Many teams that block each other, or parts with very different needs. |
| Services with shared tables and lockstep deploys | No. | All the costs of microservices, with few of the gains. | Never on purpose. Merge back or fix the boundary. |

## At work: how a senior uses this

- When someone says "let's use microservices", ask: **"Which team will deploy on its own because of this, and what does it stop waiting for?"**
- Start with a modular monolith. Make each module own its tables, and check the boundaries in CI.
- Before you extract a service, count the calls across that boundary. If it is chatty, it is not ready.
- Split one service at a time, with the strangler fig approach. Keep a way to send traffic back.
- Give each service one owning team. More services than teams is a warning.
- Budget the premium: tracing, dashboards, on-call, a pipeline for each service, and a plan for data that crosses services.

## With AI

### What to ask the agent

> "Here is our module list and the calls between modules from the last month. Group modules that call each other a lot. Suggest boundaries where cross-module calls are rare."

> "We want to extract the Payments module into a service. List every database table it reads or writes that other modules also use, and every call into it. What must change first?"

> "Act as a skeptical staff engineer. Argue against splitting this app into services. What new failures and costs would we take on?"

### What to check yourself

- The call data is real, from traces or code analysis, not guessed by the agent.
- Every table in the split plan has one owner. No two services write the same table.
- Every new network call has a timeout, a retry rule and an idempotency key where it changes data.
- The plan says which team owns each new service, and who is on call for it.
- Claims about a tool's limits come from its official docs.

## Check yourself

## Go deeper

Before this: [domain-driven design](/learn/domain-driven-design). Next: [evolutionary architecture and the strangler fig](/learn/evolutionary-architecture-strangler-fig), [sagas, outbox and CDC](/learn/sagas-outbox-and-cdc), and [making architecture trade-offs](/learn/making-architecture-trade-offs).
