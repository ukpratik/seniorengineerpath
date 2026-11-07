---
topic: requirements-nfrs
subtitle: Ask the questions that turn "fast" and "always up" into numbers for scale, latency, availability, consistency, durability, security and cost, then write them down before you design.
status: published
reviewed_by: ai-factcheck
reviewed_on: 2026-11-07
illustrative_story: true
glossary_optout: [follower, transaction]
sources:
  - id: hi-delivery
    title: "Hello Interview, “System Design Delivery Framework” (System Design in a Hurry)"
    url: https://www.hellointerview.com/learn/system-design/in-a-hurry/delivery
    note: The requirements step, the advice to quantify non-functional requirements, and a checklist of eight areas to ask about.
  - id: sre-slo
    title: "Google, Site Reliability Engineering, chapter 4: “Service Level Objectives” (O'Reilly, 2016)"
    url: https://sre.google/sre-book/service-level-objectives/
    note: Why percentiles beat averages for latency, an example latency target, and advice on choosing targets, such as "avoid absolutes".
  - id: sre-risk
    title: "Google, Site Reliability Engineering, chapter 3: “Embracing Risk” (O'Reilly, 2016)"
    url: https://sre.google/sre-book/embracing-risk/
    note: Time-based and request-based availability, how cost rises with each improvement in reliability, and the questions to ask when you set an availability target.
  - id: aws-availability
    title: "AWS Well-Architected Framework, Reliability Pillar, “Availability”"
    url: https://docs.aws.amazon.com/wellarchitected/latest/reliability-pillar/availability.html
    note: A table of availability goals with downtime per year, how hard dependencies multiply, and why higher availability costs more.
  - id: aws-dr
    title: "AWS Well-Architected Framework, Reliability Pillar, “Disaster Recovery (DR) objectives”"
    url: https://docs.aws.amazon.com/wellarchitected/latest/reliability-pillar/disaster-recovery-dr-objectives.html
    note: The definitions of RTO and RPO, and how disaster recovery differs from availability.
  - id: s3-durability
    title: "Amazon S3 User Guide, “Data protection in Amazon S3”"
    url: https://docs.aws.amazon.com/AmazonS3/latest/userguide/DataDurability.html
    note: S3 Standard's design goals for durability and for availability, stated side by side.
  - id: iso25010
    title: "ISO 25000 portal, “ISO/IEC 25010” product quality model (2023 edition)"
    url: https://iso25000.com/index.php/en/iso-25000-standards/iso-25010
    note: The nine quality characteristics of the product quality model and their sub-characteristics, such as capacity, availability and confidentiality. Reliability and security continue on the later pages (?start=5 and ?start=6).
  - id: gitlab-pm
    title: "GitLab, “Postmortem of database outage of January 31” (10 February 2017)"
    url: https://about.gitlab.com/blog/2017/02/10/postmortem-of-database-outage-of-january-31/
    note: How GitLab.com lost about six hours of database changes, and why most of its backup methods did not work.
claims_checked:
  - claim: "A 99.99% service with hard dependencies on two other 99.99% services can reach only about 99.97%."
    source: aws-availability
    note: "AWS gives this exact example: 99.99% × 99.99% × 99.99% = 99.97%. Recomputed: 0.9999³ = 99.970003%."
  - claim: "Amazon S3 Standard is designed for 99.999999999% durability but 99.99% availability over a year."
    source: s3-durability
    note: "\"Designed to provide 99.999999999% durability and 99.99% availability of objects over a given year.\""
  - claim: "RPO is the most data, in time, you accept to lose; RTO is the longest you accept to be down; the organization defines both."
    source: aws-dr
    note: "Both \"Defined by the organization\". RTO: \"maximum acceptable delay between the interruption of service and restoration of service\". RPO: \"maximum acceptable amount of time since the last data recovery point\"."
  - claim: "A typical request takes about 50 ms, but \"5% of requests are 20 times slower!\"; example SLO: \"99% of Get RPC calls will complete in less than 100 ms.\""
    source: sre-slo
    note: "Both quotes are verbatim from chapter 4."
  - claim: "Each improvement in reliability \"may cost 100x more than the previous increment\"."
    source: sre-risk
    note: "Verbatim: \"an incremental improvement in reliability may cost 100x more than the previous increment.\" The lesson now says improvement in reliability, not \"each nine\"."
  - claim: "Hello Interview lists eight non-functional areas, advises picking the top 3 to 5, and gives the requirements step about 5 minutes."
    source: hi-delivery
    note: "Areas: CAP, environment constraints, scalability, latency, durability, security, fault tolerance, compliance. \"top 3-5\"; heading \"Requirements (~5 minutes)\"."
quiz:
  - q: A product manager says the order page "must be fast". What is the best requirement to write down?
    options:
      - "The order page should be fast."
      - "Average response time under 300 ms."
      - "99% of order page requests answer in under 300 ms, at the peak of 2,000 requests per second."
    answer: 2
    why: A percentile at a stated load can be measured and tested. An average hides the slowest requests, and "fast" cannot be checked at all.
  - q: Your service targets 99.99% and has hard dependencies on two other services, each at 99.99%. What is the best it can do in theory?
    options:
      - 99.99%
      - About 99.97%
      - 99.9999%
    answer: 1
    why: With hard dependencies, availabilities multiply. 99.99% × 99.99% × 99.99% is about 99.97%.
  - q: A storage service is designed for 99.999999999% durability and 99.99% availability. What does that tell you?
    options:
      - Your saved data is very unlikely to be lost, but you may not be able to reach it for a while.
      - The service is never down.
      - You do not need backups or a recovery plan.
    answer: 0
    why: Durability is about not losing data. Availability is about reaching it right now. They are separate needs with separate numbers.
  - q: In a 45-minute design interview, how should you handle non-functional requirements?
    options:
      - List every quality you can think of, so you miss nothing.
      - Pick the 3 to 5 that matter most for this system and put a number on each where you can.
      - Skip them and start drawing. They come up in the deep dives.
    answer: 1
    why: A few quantified needs guide the design. A long list wastes time, and skipping them means you may design for the wrong things.
incidents: [gitlab-2017-01-database]
related: [system-design-method, design-docs, latency-estimation, observability-slos, cap-consistency, tail-latency-queueing]
---

## What broke

A food delivery team got a request from its product manager (PM). Build a live order status page. It "must be fast and always up". Nobody asked what those words meant. The team wrote no numbers down.

In testing, the page answered in 120 ms on average. The team used one Postgres database, with a nightly 20-minute maintenance window. It seemed fine.

On the first Friday night, traffic was eight times the test load. The average was still under 400 ms. But one request in a hundred took over 6 seconds. Those users refreshed again and again, which added more load.

Then a restaurant owner called at 2 a.m. Her kitchen tablets used the same page, and they went blank every night during maintenance. To the PM, "always up" had meant "up whenever a restaurant is open". Some restaurants never close.

The fix took a month. It needed a read replica, a cache and a new way to run maintenance. Three questions in the first week would have changed the first design. This story is made up, but each part of it is common.

## The idea in simple words

**Functional requirements** say what users can do: "see the status of my order". **Non-functional requirements** say how well the system must do it: how fast, for how many people, how often it may fail, and what it may cost.

The functional list tells you which parts to build. The non-functional list tells you how to build them. The same feature can be one server or fifty, depending on these numbers.

People rarely give you these numbers. They give you words like "fast", "scalable" and "always up". Your job is to turn each word into a number that you can design for and later measure.

> [!key] **The key idea.** A need you cannot measure is a wish. Ask questions until each one has a number, a way to measure it and a person who agreed to it. Then design for those numbers, and no more.

## How it works

Step through how one vague ask becomes numbers. Use Next and Back, or the ← and → keys.

[[diagram:vague-ask-to-numbers]]

Start with the functional requirements, as in the [system design method](/learn/system-design-interview-method). Then go through the qualities below. Hello Interview gives a checklist of eight areas, including latency, scalability, durability, security and compliance. It advises picking the top 3 to 5 for your system. [@hi-delivery] ISO/IEC 25010:2023, a standard model of software quality, lists nine quality characteristics with sub-characteristics. [@iso25010] You do not need all of them. Use them as a list of questions you might forget.

### Scale

Ask: how many users, how many requests per second at the busiest time, and how much data after a year? Ask for the peak, not the daily average. ISO 25010 calls this "capacity": how well the system's maximum limits, such as users or data size, meet the requirements. [@iso25010] To turn users into requests per second, see [latency numbers and estimation](/learn/latency-numbers-and-estimation).

### Latency, as percentiles

"Fast" needs a percentile, a load and an operation. An average hides the slow requests. In one example from the Google SRE book, a typical request takes about 50 ms, but "5% of requests are 20 times slower!" [@sre-slo] A high percentile, such as the 99th, shows "a plausible worst-case value". [@sre-slo]

So write: "99% of `GET /orders/:id` calls answer in under 300 ms, at 2,000 requests per second". The book's own example has this shape: "99% of `Get` RPC calls will complete in less than 100 ms." [@sre-slo] Hello Interview makes the same point. "Low latency" alone is not useful. "Low latency search, < 500ms" names the part and the target. [@hi-delivery]

### Availability, as nines

"Always up" is not a real target. The SRE book calls it "unrealistic". A system that comes close would be slow to build and expensive to run. [@sre-slo] Ask instead how much downtime users can live with. Count it per month. A month is a common period for availability targets, and a number of minutes per month is easy to picture. [@aws-availability]

These numbers are simple maths over a 30-day month:

| Target | Downtime allowed per month |
|---|---|
| 99% | about 7 hours 12 minutes |
| 99.9% | about 43 minutes |
| 99.95% | about 22 minutes |
| 99.99% | about 4 minutes 19 seconds |
| 99.999% | about 26 seconds |

AWS gives example uses for each level: internal tools at 99.9%, online shops at 99.95%, and ATM transactions at 99.999%. Its own table shows downtime per year. [@aws-availability] AWS also advises that planned maintenance should count as downtime, because users may want the service then. [@aws-availability]

Two more facts change designs. First, hard dependencies multiply. A 99.99% service that needs two other 99.99% services can reach only about 99.97%. [@aws-availability] Second, more reliability gets expensive fast. The SRE book warns that each improvement in reliability "may cost 100x more than the previous increment". [@sre-risk] It suggests asking what users expect, whether the service ties to revenue, and whether it is free or paid. [@sre-risk] To measure availability over time, see [observability, SLOs and error budgets](/learn/observability-slos-error-budgets).

### Consistency

Ask: after a write, who must see it at once, and who can see old data for a few seconds? A seat sold twice is a real failure. A follower count that is 5 seconds old is not. Hello Interview puts this as the first item on its list: should the system prefer consistency or availability? [@hi-delivery] Answer it for each part, not for the whole system. See [CAP, PACELC and consistency models](/learn/cap-pacelc-consistency-models).

### Durability and recovery

Durability is not the same as availability. Amazon S3 Standard is designed for 99.999999999% durability but 99.99% availability over a year. [@s3-durability] Your data is very safe, but you may not reach it for a while.

For disasters, ask for two numbers. The **RPO** is the most data, in time, you accept to lose. The **RTO** is the longest you accept to be down. [@aws-dr] AWS says the organization defines both, so ask the business, not only engineers. [@aws-dr] In 2017, GitLab lost about six hours of database changes. Most of its backup methods were broken or never set up, and nobody had noticed. It recovered from a copy made six hours earlier for another purpose. [@gitlab-pm] See the [GitLab incident](/incidents#gitlab-2017-01-database).

### Security and compliance

ISO 25010 splits security into parts such as confidentiality, integrity and accountability. [@iso25010] Turn each into a question. Who may read this data? Must every change be traced to a person? Is there personal or card data, and which laws apply? Hello Interview lists compliance as its own area. [@hi-delivery]

### Cost

Ask for a budget, even a rough one: "under $2,000 a month in cloud costs". AWS warns that higher availability needs more testing, more automation and slower releases. It advises finding the "true availability needs" before you design. [@aws-availability]

### Write them down

Put each need in one row: the need, the number, how you will measure it, and who asked for it. Start with a loose target you can tighten later. The SRE book says this is better than a strict target you must relax. [@sre-slo] Put the table near the top of the [design doc](/learn/design-docs-and-rfcs).

In an interview, the requirements step takes about 5 minutes. [@hi-delivery] Say 3 to 5 needs out loud, each with a number, and write them on the board.

## Trade-offs

| Way to state a need | Easy to agree? | Guides the design? | Cost | Use it when |
|---|---|---|---|---|
| A word: "fast", "always up" | Yes. | No. | Rework later, when the words mean different things. | Never as the final version. |
| An average: "200 ms on average" | Yes. | Partly. | Hides the slowest requests. | Only for batch jobs, where the total matters. |
| A percentile at a load: "p99 < 300 ms at 2,000 req/s" | Needs a talk. | Yes. | You must measure it in production. | Any user-facing request path. |
| A very high target: 99.99% or more | Hard. | Yes. | Each nine costs far more. Releases get slower. | Payments, or when the business proves it needs it. |
| A loose target you tighten later | Yes. | Yes. | You may need to rebuild some parts as targets rise. | New systems with no traffic data yet. |

**Simple rule:** every need gets a number, and every number gets a reason from a user or the business.

## At work: how a senior uses this

- Ask "what happens to the user if this is slow, or down for 10 minutes?" The answer sets the target better than any guess.
- Ask for peak load, not average. Ask when the peak happens, such as Friday dinner or a sale at noon.
- Ask what "always" means. Does it include nights, weekends and planned maintenance?
- Split the system. The checkout can need 99.95% while the reports page needs 99%.
- List every hard dependency and its availability. Your target cannot be higher than what they allow.
- Ask the business for the RPO and RTO. Then test a restore, because a backup you never restored may not work.
- Write "out of scope" needs as [non-goals](/glossary#non-goal), for example "no multi-region in version 1".
- In a review, ask "which need does this part serve?" A cache or a queue with no need behind it is extra cost.

## With AI

### What to ask the agent

> "Here is a feature request from my PM. List the questions I should ask about scale, latency, availability, consistency, durability, security and cost. Mark which answers would change the design."

> "Turn these vague needs into measurable ones. For each, give a number, the operation it applies to, the load, and how I would measure it in production."

> "My service needs 99.95% and calls these four services. Their targets are listed below. Can my target be met? Show the maths."

### What to check yourself

- Every number came from a person or from data, not from the agent. An agent may invent targets that look plausible.
- Latency targets name a percentile, an operation and a load, not only an average.
- The monthly downtime maths is right. Check one row by hand: 43,200 minutes × 0.1% = 43.2 minutes.
- Durability, RPO and RTO are separate from availability in the list.
- The list is short. Three to five strong needs beat fifteen weak ones.

## Check yourself

## Go deeper

Before this: [the system design method](/learn/system-design-interview-method). Next: [latency numbers and estimation](/learn/latency-numbers-and-estimation), [observability, SLOs and error budgets](/learn/observability-slos-error-budgets), [design docs and RFCs](/learn/design-docs-and-rfcs) and [tail latency and queueing](/learn/tail-latency-and-queueing).
