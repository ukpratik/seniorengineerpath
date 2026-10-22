---
topic: zero-to-millions
subtitle: Most systems grow through the same few steps, from one server to many parts. Learn the usual order, what makes each step worth it, and why you should measure before you add anything.
status: published
reviewed_by: ai-factcheck
reviewed_on: 2026-10-22
illustrative_story: true
sources:
  - id: bbg-zero
    title: "ByteByteGo, “Scale From Zero To Millions Of Users”, System Design Interview course (online chapter)"
    url: https://bytebytego.com/courses/system-design-interview/scale-from-zero-to-millions-of-users
    note: The page names no author. A step-by-step walk from one server to load balancers, replicas, cache, CDN, stateless servers, data centers, queues and sharding.
  - id: aws-rel
    title: "AWS Well-Architected Framework, Reliability Pillar, “Design principles”"
    url: https://docs.aws.amazon.com/wellarchitected/latest/reliability-pillar/design-principles.html
    note: Scale horizontally to limit the impact of one failure, and stop guessing capacity by watching demand.
  - id: sre-monitoring
    title: "Rob Ewaschuk, “Monitoring Distributed Systems”, Site Reliability Engineering (Google, O'Reilly 2016), chapter 6"
    url: https://sre.google/sre-book/monitoring-distributed-systems/
    note: The four golden signals to measure first for any user-facing system.
  - id: figma-scale
    title: "Sammy Steele, “How Figma's databases team lived to tell the scale”, Figma blog (March 2024)"
    url: https://www.figma.com/blog/how-figmas-databases-team-lived-to-tell-the-scale/
    note: A real path from one large Postgres database to caching, read replicas, split databases and then sharding.
claims_checked:
  - claim: One server first; users cannot reach the site if it is offline. Vertical scaling suits low traffic but has a hard limit and "does not have failover and redundancy". Stateless servers keep sessions in a shared store. The chapter ends "Scaling a system is an iterative process."
    source: bbg-zero
    note: All on the ByteByteGo page. The page names no author and no book, so the lesson no longer credits Alex Xu or his book; the citation now names ByteByteGo's course page.
  - claim: CDN serves static content (images, CSS, JavaScript); in the queue example photo processing workers take jobs from the queue; producer and consumer can be scaled independently and keep working when the other side is down; sharding splits data across servers.
    source: bbg-zero
    note: Matches the CDN, message queue and sharding sections.
  - claim: "Scale horizontally: replace one large resource with several small ones to reduce the impact of one failure. Stop guessing capacity: monitor demand and add or remove resources; there are still limits (quotas)."
    source: aws-rel
    note: Both are listed design principles, word for word in meaning.
  - claim: The four golden signals are latency, traffic, errors and saturation; if you can measure only four metrics of a user-facing system, focus on these.
    source: sre-monitoring
    note: Chapter 6, written by Rob Ewaschuk (edited by Betsy Beyer). Quiz answer checked.
  - claim: In 2020 Figma ran one Postgres database on AWS's largest physical instance; by end of 2022 it had caching, read replicas and a dozen vertically partitioned databases; some tables reached several terabytes and billions of rows and writes would soon exceed RDS IOPS; first horizontally sharded table shipped September 2023.
    source: figma-scale
    note: Post by Sammy Steele, March 14, 2024. Earlier wording "close to RDS limits" made specific (IOPS).
quiz:
  - q: Your one database server is at 90% CPU, and most of its queries are reads of the same popular pages. What is a good next step?
    options:
      - Shard the database.
      - Add a cache or a read replica for those reads.
      - Move to microservices.
    answer: 1
    why: The trigger is read load on hot data. A cache or replica fixes that at a much lower cost than sharding.
  - q: Why must app servers be stateless before you put many of them behind a load balancer?
    options:
      - Stateless servers use less memory.
      - So any server can serve any user, and losing one server loses no sessions.
      - Load balancers only work with stateless servers.
    answer: 1
    why: Move sessions to a shared store. Then the load balancer can send each request anywhere, and servers can come and go.
  - q: What is the main limit of vertical scaling?
    options:
      - It is slower to set up than horizontal scaling.
      - There is a largest machine you can buy, and one machine is still one point of failure.
      - It does not work with databases.
    answer: 1
    why: The ByteByteGo chapter says vertical scaling has a hard limit and gives no failover or redundancy.
  - q: Which four signals does the Google SRE book say to measure first for a user-facing system?
    options:
      - CPU, memory, disk and network.
      - Latency, traffic, errors and saturation.
      - Users, revenue, sign-ups and churn.
    answer: 1
    why: These are the four golden signals. They tell you which step you need next.
incidents: []
cases: [figma-postgres-sharding, zerodha-scaling-common-sense]
related: [latency-estimation, load-balancing, replication, caching-stampedes, cdn-http-caching, queues-vs-logs, sharding, observability-slos, capacity-planning, multi-region]
---

## What broke

This story is made up, but it is common. A small team builds a ticket booking app. It runs on one server: the web app and the database together. For a year this works fine.

Then the team plans for growth. In one quarter they add Kafka, a Redis cluster, six microservices and a sharded database. They have 3,000 daily users.

A popular concert goes on sale. The site slows to a crawl. The team looks at the dashboards they have, and none of them show the problem. After two days of searching, they find it. One slow SQL query with no index was reading the whole bookings table on every page load.

The new parts did not help. They made the system harder to debug, and nobody had measured anything before adding them. One index fixed the problem.

Scaling is not a list of tools to install. It is a series of answers to problems you can see in your numbers.

## The idea in simple words

A well-known chapter of ByteByteGo's System Design Interview course walks one system from a single user to millions, one step at a time. [@bbg-zero] The steps are not a law. Treat them as a typical order: in our experience, each step fixes the most likely next bottleneck.

The rule behind every step: **find what is full, then fix only that.** The chapter ends by saying that scaling a system is an iterative process. [@bbg-zero]

> [!key] **The key idea.** Each part you add fixes one kind of limit and adds new ways to fail. Add a part when a measurement says you need it, not because big companies have it.

## How it works

Step through how the system grows. Use Next and Back, or the ← and → keys.

[[diagram:zero-to-millions-growth]]

### 0. Measure from day one

Before any step, you need numbers. The Google SRE book names four golden signals: latency, traffic, errors and saturation. If you can measure only four metrics of a user-facing system, measure these. [@sre-monitoring] Saturation tells you what is full. Latency and errors tell you if users feel it. Use [back-of-envelope estimates](/learn/latency-numbers-and-estimation) to guess what will fill up next.

### 1. One server

At the start, everything runs on one server. [@bbg-zero] It is cheap and easy to debug. Its weakness: if that server goes offline, users cannot reach the site. [@bbg-zero]

### 2. A separate database

Move the database to its own machine. [@bbg-zero] Now the app and the database stop fighting for the same CPU and memory, and you can size each one on its own.

**Trigger (our advice):** the app and the database compete for CPU, memory or disk on one machine.

### 3. Bigger machine, or more machines?

Vertical scaling means a bigger machine: more CPU and RAM. Horizontal scaling means more machines. When traffic is low, vertical scaling is a good choice because it is simple. [@bbg-zero] But it has a hard limit, and one machine gives you no failover. [@bbg-zero]

AWS's reliability advice is to replace one large resource with several small ones, so one failure hurts less. [@aws-rel]

**Trigger (our advice):** scale up first while it is cheap. Scale out when you near the largest machine, or when one machine going down is not acceptable.

### 4. A load balancer and stateless app servers

A load balancer spreads requests across several app servers. [@bbg-zero] For this to work, the app servers must be stateless. Move session data out of server memory into a shared store, so any server can handle any request. [@bbg-zero] See [Load balancing](/learn/load-balancing) for how the balancer picks a server.

**Trigger:** one app server is near its CPU limit, or you need to survive one server dying.

### 5. Read replicas

Many apps read far more than they write. With replication, the main database takes the writes and replicas serve the reads. [@bbg-zero] Replicas can lag behind the main database, so a user may not see their own write at once. See [Replication](/learn/database-replication).

**Trigger:** database CPU is high and most queries are reads. Check first that slow queries are not the real cause.

### 6. A cache

A cache keeps often-read data in memory, so fewer reads reach the database. [@bbg-zero] It adds new problems: stale data and many requests missing at once. See [Caching and cache stampedes](/learn/caching-and-cache-stampedes).

**Trigger:** the same data is read again and again, and it can be a little out of date.

### 7. A CDN for static files

A CDN is a network of servers spread around the world that serves static content, such as images, CSS and JavaScript. [@bbg-zero] Users get files from a nearby server, and your servers do less work. See [CDNs and HTTP caching](/learn/cdn-and-http-caching).

**Trigger:** static files use much of your bandwidth, or users far away see slow page loads.

### 8. Queues for slow work

Some work does not need to finish before you reply to the user. In the chapter's example, web servers put photo processing jobs on a message queue, and workers do them in the background. [@bbg-zero] The queue also lets the two sides scale and fail on their own. See [Queues vs logs](/learn/queues-vs-logs-delivery-guarantees).

**Trigger:** a request waits on slow work, such as sending email or resizing images, that the user does not need to see at once.

### 9. Sharding

Replicas help with reads. They do not help when one database cannot take all the writes or hold all the data. Then you split the data across shards, each with a part of the rows. [@bbg-zero] Sharding is hard to undo, and queries across shards get harder. See [Sharding and consistent hashing](/learn/sharding-and-consistent-hashing).

Figma is a real example of this order. In 2020 it ran one Postgres database on AWS's largest physical instance. [@figma-scale] By the end of 2022 it had added caching, read replicas and a dozen databases split by table. [@figma-scale] It moved to horizontal sharding only when some tables reached several terabytes and billions of rows. Their writes would also soon pass the IOPS limit of Amazon RDS. Its first sharded table shipped in September 2023. [@figma-scale]

**Trigger:** writes or data size on one database near its limits, after indexes, query fixes, replicas and splitting tables are done.

### After that

The chapter goes further, to several data centers. [@bbg-zero] See [Multi-region systems](/learn/multi-region-and-data-locality) when you get there.

## Trade-offs

| Step | What it fixes | What it costs |
|---|---|---|
| Bigger machine | More CPU and memory, with no code change. | A hard size limit. Still one point of failure. |
| Separate database | The app and database stop fighting for resources. | One more machine and one network hop. |
| Load balancer and stateless servers | More traffic. One server can die safely. | Sessions must move to a shared store. |
| Read replicas | Read load on the database. | Replica lag. Users may read old data. |
| Cache | Repeated reads of hot data. | Stale data, stampedes, one more system to run. |
| CDN | Static file load and distance to users. | Purging old files. Cost per GB. |
| Queue | Slow work blocking requests. | Duplicates, retries, a backlog to watch. |
| Sharding | Write load and data size on one database. | Cross-shard queries, resharding, hot keys. Hard to undo. |

**Simple rule (our advice):** go down this table only as far as your numbers force you to.

## At work: how a senior uses this

- Ask **"What is full?"** before any scaling proposal. Ask for the graph that shows it.
- Fix the cheap things first: a missing index, an N+1 query, a bigger instance.
- Write the trigger down. For example: "We add a read replica when database CPU stays above 70% at peak." Then the decision is not a debate later.
- Make app servers stateless early. It is cheap when the code is small and painful later.
- For each new part, ask who runs it, how it fails and how you will see it fail.
- Use the AWS principle "stop guessing capacity": watch demand and add or remove resources to match it. [@aws-rel] Remember that cloud quotas still apply. [@aws-rel]
- Plan for [load tests](/learn/load-testing) and [capacity planning](/learn/capacity-planning) before big events, not after.

## With AI

### What to ask the agent

> "Here are our dashboards for latency, traffic, errors and CPU, memory and disk for each machine. Which resource is closest to full at peak? What is the cheapest change that buys us 3x headroom?"

> "Find any place where this app keeps user state in server memory, such as sessions, uploads on local disk or in-process caches. List what must move before we run more than one server."

> "List the 10 slowest database queries from this log, with how often each runs. Suggest an index or query change for each, before we talk about replicas."

### What to check yourself

- The agent's plan starts from a measured bottleneck, not from a standard diagram.
- It did not suggest sharding or microservices when an index or a bigger machine would do.
- Any proposed cache says how data is kept fresh and what happens on a miss.
- Each new part comes with the metric that shows it works and an alert for when it fails.
- Cost is in the plan. More parts mean more servers to pay for and more on-call work.

## Check yourself

## Go deeper

Every step after the first one turns your app into a distributed system. Read [Why distributed systems are hard](/learn/why-distributed-systems-are-hard) next. For reads and writes in depth, see [Scaling reads](/learn/scaling-reads) and [Scaling writes and hot keys](/learn/scaling-writes-and-hot-keys).
