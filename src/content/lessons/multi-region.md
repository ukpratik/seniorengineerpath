---
topic: multi-region
subtitle: A second region buys speed, survival and legal room. You pay in money, in lag between copies, and in a failover you must practise.
status: published
illustrative_story: false
sources:
  - id: cf-2023
    title: "Cloudflare, “Post mortem on the Cloudflare Control Plane and Analytics Outage”"
    url: https://blog.cloudflare.com/post-mortem-on-cloudflare-control-plane-and-analytics-outage/
    note: One data centre lost power. Services built to survive that still depended on it, and the failover to Europe hit a flood of retried API calls.
  - id: azure-rtt
    title: "Microsoft Learn, “Azure network round-trip latency statistics”"
    url: https://learn.microsoft.com/en-us/azure/networking/azure-network-latency
    note: Measured median round-trip times between Azure regions, in tables you can search by region.
  - id: itu-g114
    title: "ITU-T Recommendation G.114, “One-way transmission time” (05/2003)"
    url: https://www.itu.int/rec/T-REC-G.114-200305-I/en
    note: Table A.1 gives planning values for delay, including 5 µs per km for optical fibre.
  - id: r53-routing
    title: "Amazon Route 53 Developer Guide, “Choosing a routing policy”"
    url: https://docs.aws.amazon.com/Route53/latest/DeveloperGuide/routing-policy.html
    note: Failover, geolocation, latency and weighted DNS routing, each in one line.
  - id: aurora-global
    title: "Amazon Aurora User Guide, “Using Amazon Aurora Global Database”"
    url: https://docs.aws.amazon.com/AmazonRDS/latest/AuroraUserGuide/aurora-global-database.html
    note: One writer region, up to 10 read-only regions, and replication that is typically under a second behind.
  - id: aurora-dr
    title: "Amazon Aurora User Guide, “Using switchover or failover in Amazon Aurora Global Database”"
    url: https://docs.aws.amazon.com/AmazonRDS/latest/AuroraUserGuide/aurora-global-database-disaster-recovery.html
    note: Failover versus switchover, lost writes, write fencing, split-brain and failing back.
  - id: ddb-global
    title: "Amazon DynamoDB Developer Guide, “How DynamoDB global tables work”"
    url: https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/V2globaltables_HowItWorks.html
    note: Writes in every region, last-writer-wins conflicts, and a strongly consistent mode that costs latency.
  - id: spanner-configs
    title: "Google Cloud Spanner documentation, “Regional, dual-region, and multi-region configurations”"
    url: https://docs.cloud.google.com/spanner/docs/instance-configurations
    note: How a multi-region Spanner database forms a write quorum across regions, and where to put the leader.
  - id: aws-fib
    title: "AWS Whitepaper, AWS Fault Isolation Boundaries, “Global services”"
    url: https://docs.aws.amazon.com/whitepapers/latest/aws-fault-isolation-boundaries/global-services.html
    note: Which AWS control planes live in one region, and how to keep them out of your recovery path.
  - id: gcp-2025
    title: "Google Cloud Service Health, “Multiple GCP products are experiencing Service issues.” (12 June 2025)"
    url: https://status.cloud.google.com/incidents/ow5i3PPK96RduMcb1SsW
    note: A regional service read policy data that was copied to every region within seconds.
claims_checked:
  - claim: "PDX-04 lost power between 11:44 and 12:01 UTC on 2 November 2023; at 13:40 UTC Cloudflare chose to fail over to its European DR sites, first services up at 13:43, failing API calls arrived at once so rate limits were added; all services restored 4 November 04:25 UTC."
    source: cf-2023
    note: Both quotes are exact. Cloudflare had tested taking each of its other two sites offline, but never the whole of PDX-04; the sites were independent, the software dependencies (Kafka, ClickHouse only in PDX-04) were not.
  - claim: "DynamoDB global tables default (MREC) mode replicates 'typically within a second or less' and resolves concurrent writes per item by latest internal timestamp ('last writer wins'); MRSC runs in exactly three Regions (or two plus a witness) and fails clashing writes with ReplicatedWriteConflictException."
    source: ddb-global
    note: Also checked stale strongly consistent reads, local-only conditional writes and Region-local transaction atomicity in MREC.
  - claim: "Aurora Global Database failover 'can result in a loss of write transaction data', RPO 'typically measured in seconds'; write fencing is best effort and can allow split-brain; switchover synchronises first (RPO 0) and is the way to fail back."
    source: aurora-dr
    note: Snapshot of the old volume is attempted on managed failover only; AWS recommends taking apps offline and a DNS TTL such as 5 seconds.
  - claim: "Azure P50 round trips (30 days to 30 July 2026): East US–East US 2 8–9 ms, East US–West US 67–69, Central India–Southeast Asia 53, East US–West Europe 83–85, Central India–West Europe 139–140, Central India–East US 198–201."
    source: azure-rtt
    note: Both directions checked for every pair.
  - claim: "ITU-T G.114 Table A.1 gives 5 µs/km one way for optical fibre (provisional), which works out to about 200 km per ms."
    source: itu-g114
    note: Read from the 05/2003 PDF; the lesson frames 200 km/ms as derived.
  - claim: "IAM, Route 53 and CloudFront control planes are in us-east-1; AWS advises removing global control plane dependencies from the recovery path to be statically stable."
    source: aws-fib
    note: Glossary 'statically stable' matches (no control plane changes needed).
reviewed_by: ai-factcheck
reviewed_on: 2026-11-13
quiz:
  - q: Your users are in India. Your only database is in a US East region. Each page makes 3 database calls, one after another. Roughly how long do those calls take, before any work is done?
    options:
      - About 20 ms, if the database is fast.
      - About 600 ms, because each call pays a round trip of about 200 ms.
      - It depends only on the size of the query.
    answer: 1
    why: Azure measures about 198–201 ms between Central India and East US. Three calls in a row pay that three times, before any work is done.
  - q: Two regions write the same DynamoDB item at the same time, in the default global table mode. What happens?
    options:
      - Both writes fail with an error.
      - The write with the latest timestamp is kept everywhere. The other is dropped without an error.
      - DynamoDB keeps both versions and asks your app to merge them.
    answer: 1
    why: The default mode uses "last writer wins" per item. The losing write is simply gone.
  - q: Your primary region is down, and you fail over an async replica in another region. What should you expect?
    options:
      - No data loss, because the replica is a full copy.
      - The writes from the last few seconds before the failure may be missing.
      - The old region will refuse writes forever.
    answer: 1
    why: Async replication lags behind. Aurora says failover RPO is typically measured in seconds, and only a planned switchover loses no data.
  - q: Your service runs in three regions. To fail over, your runbook calls an API whose control plane runs only in one of those regions. What is the risk?
    options:
      - None, because the data plane is in every region.
      - If that one region is the one that fails, your recovery step fails too.
      - The API will be slower, but it will work.
    answer: 1
    why: AWS advises you to keep global control planes out of your recovery path, and to depend only on data plane operations there.
incidents: [cloudflare-2023-11-control-plane, google-cloud-2025-06-service-control]
related: [replication, cap-consistency, quorums-leaderless, disaster-recovery, load-balancing, multi-tenancy, safe-deploys]
glossary_optout: [failover, leader, follower]
---

## What broke

On 2 November 2023, between 11:44 and 12:01 UTC, a data centre near Hillsboro, Oregon, lost all power. It was called PDX-04. [@cf-2023]

Cloudflare ran its control plane (the dashboard and APIs) and analytics mainly across three data centres in that area. They were built so that the control plane would keep running if any one of them failed. In Cloudflare's words, "The three data centers are independent of one another". [@cf-2023]

The buildings were independent. Some of the software was not. Some services that were meant to survive still used Kafka and ClickHouse clusters that ran only in PDX-04. Some services, mostly for newer products, had not moved to the shared, highly available setup yet. [@cf-2023]

At 13:40 UTC the team chose to fail over to its disaster recovery sites in Europe. The first services came up 3 minutes later. Then the API calls that had been failing all arrived at once, so the team added rate limits. A few newer products did not start properly in Europe. The services that did move were stable by 17:57 UTC. But some log processing stayed down, and every service was back only on 4 November. [@cf-2023]

Two lines from the report stand out. "Throughout the incident, Cloudflare's network and security services continued to work as expected." And: "we had never tested fully taking the entire PDX-04 facility offline." [@cf-2023] The traffic part was spread across the world. The control part was not.

## The idea in simple words

A cloud region is a group of data centres in one area, such as Mumbai or Frankfurt. Running in more than one region gives you three things:

- **Speed.** Users near a region get faster answers.
- **Survival.** If a whole region fails, another can serve.
- **Data residency.** Some laws say certain data must stay in a country. A later lesson on privacy covers this.

The price is real. You run and pay for two or more copies of everything. Each copy of the data lags behind the others. And the failover that should save you is a rare, risky event that nobody has practised.

> [!key] **The key idea.** The network between regions is slow, and sometimes it breaks. So for each piece of data, decide which region owns its writes, and what users see while the copies disagree.

## How it works

### The speed-of-light floor

ITU's planning value for optical fibre is 5 µs per km, one way. [@itu-g114] That works out to about 200 km per millisecond. So every 100 km of cable adds about 1 ms to a round trip. No code change can beat this.

Real paths are longer than a straight line. Azure publishes measured median round-trip times between its regions. These are for the 30 days to 30 July 2026, in both directions: [@azure-rtt]

| Region | Region | Median round trip |
|---|---|---|
| East US | East US 2 | 8–9 ms |
| East US | West US | 67–69 ms |
| Central India | Southeast Asia | 53 ms |
| East US | West Europe | 83–85 ms |
| Central India | West Europe | 139–140 ms |
| Central India | East US | 198–201 ms |

These are medians. Your slow requests pay more. Now count the round trips. A page that makes 3 database calls in a row, from India to US East, waits about 600 ms before it does any work. Cross-region calls must be few, and never inside a loop.

### Active-passive or active-active

In **active-passive**, one region serves all traffic. The other keeps a copy of the data and waits. Failover is simple to reason about, but you pay for a region that does little, and you only learn if it works on the bad day.

In **active-active**, every region serves users all the time. Each region is tested by real traffic every day. But now writes can happen in two places, and you must handle conflicts.

The [disaster recovery lesson](/learn/disaster-recovery-and-backups) lists the steps in between, like pilot light and warm standby.

### Routing users to a region

DNS is the usual tool. Amazon Route 53, for example, can answer with the region that gives the best latency, by the user's location, or by a failover rule that switches from active to passive. [@r53-routing] DNS answers are cached, so some clients keep using the old region for a while after a change.

Anycast is the other tool: many sites announce one IP address, and the network picks the nearest. See [load balancing](/learn/load-balancing) for how it works. A [CDN](/learn/cdn-and-http-caching) uses the same idea to serve cached files near users.

### Where the writes go

This is the hard part. You have four main choices.

**One writer region, read copies elsewhere.** Aurora Global Database has one primary region for writes and up to 10 read-only regions. Copies are typically under a second behind. [@aurora-global] Reads are local and fast. Writes from far away pay the long round trip. All the lag bugs from [replication](/learn/database-replication) come back, now with bigger numbers.

**A home region per user or tenant.** Each user's data lives in one region, often the one nearest to them. All their writes go there. A user in Delhi writes to Mumbai. A user in Berlin writes to Frankfurt. No row has two writers, so there are no conflicts. The cost: you need a lookup from user to region, and moving a user is a data migration. The [multi-tenancy lesson](/learn/multi-tenancy-noisy-neighbours) shows the same idea with cells.

**Many writers, conflicts resolved later.** DynamoDB global tables accept writes in every region. In the default mode, changes reach other regions "typically within a second or less". [@ddb-global] If two regions change the same item at once, the one with the latest internal timestamp wins. AWS calls this "last writer wins". [@ddb-global] The other write is silently gone.

Other surprises follow. A strongly consistent read may return stale data if the item last changed in another region. A conditional write checks only the local copy. A transaction is atomic only in the region that ran it. [@ddb-global]

**One logical database with cross-region quorums.** DynamoDB also has a strong mode. It runs in exactly three regions: three copies, or two copies and a "witness" that only votes. Each write is copied to at least one other region before it returns. [@ddb-global] Spanner works in a similar way. Each write needs a majority of five voting copies, one of them in the leader region, so every write crosses at least two regions. [@spanner-configs] No write is silently lost. In DynamoDB, a write that clashes with one in another region fails and must be retried. [@ddb-global] Every write waits for regions to talk. Spanner advises putting write-heavy work in or near the leader region. [@spanner-configs] This is the [PACELC](/learn/cap-pacelc-consistency-models) trade, paid on every write.

### Conflicts in active-active

Picture a stock count of 5. A buyer in Mumbai takes 1 and writes 4. At the same moment, a buyer in Singapore takes 1 and writes 4. With last write wins, the final count is 4, but two items left the shelf. Neither region saw an error. The [quorums lesson](/learn/quorums-and-leaderless-replication) explains why timestamps make this worse when clocks drift.

There are three common fixes. Send all writes for one item to one region. Store changes, like "minus 1", that can be added up in any order. Or use the strong mode for the few items that need it.

### Failing over and failing back

Step through a region failover. Use Next and Back, or the ← and → keys.

[[diagram:region-failover-failback]]

Aurora's docs show the real costs. An unplanned failover "can result in a loss of write transaction data" that had not been copied yet. The RPO is "typically measured in seconds". [@aurora-dr] Aurora tries to stop writes in the old region, but this is best effort. Writes may still land there, which gives split-brain. [@aurora-dr] So Aurora asks you to take apps offline first, and to keep DNS cache times short, such as 5 seconds. [@aurora-dr]

Failing back is a separate step. With a managed failover, Aurora adds the old region back as a copy. It first tries to snapshot the old data, so you can look for lost writes. Copy that snapshot if you need it longer than the backup retention period. Later you run a switchover, which waits until the copies match, so no data is lost. [@aurora-dr]

Capacity matters too. The surviving region must take all the traffic, plus a burst of retries. Cloudflare hit this. When its services came up in Europe, the API calls that had been failing all arrived at once, and it had to add rate limits. [@cf-2023]

### Keep regions from sharing fate

Regions only help if they fail alone. A global control plane can tie them together without anyone noticing.

AWS says some global services have their control plane in one region, while their data plane runs everywhere. IAM, Route 53 and CloudFront have their control planes in us-east-1. [@aws-fib] AWS advises you to keep these control planes out of your recovery path, so that recovery uses only data plane actions. It calls this being statically stable. [@aws-fib]

Shared data can do the same. In June 2025, Google Cloud's Service Control ran in each region, with its own regional datastore. But a bad quota policy "was replicated globally within seconds", and the crash spread with it. [@gcp-2025] Copy config and policy to one region at a time, as [safe deploys](/learn/safe-deploys-config-changes) explains.

## Trade-offs

| Design | Conflicts? | Write latency | Data lost on region failure | Use it when |
|---|---|---|---|---|
| One writer region, read copies | No. | Far users pay a long round trip. | Yes. The last seconds of async writes. | Most apps that go global first for reads or survival. |
| Home region per user or tenant | No. Each row has one writer. | Low for users near home. | Yes, for that region's recent writes. | Users or tenants map cleanly to a place. |
| Many writers, last write wins | Yes. One write is dropped. | Low everywhere. | Small. Seconds of writes not yet copied. | Data where losing one of two writes at once is fine, like settings or sessions. |
| Cross-region quorum (Spanner, strong mode) | No silent conflicts. A clashing write fails and you retry. | Higher. Every write waits for other regions. How much depends on how far apart they are. | No. | Money, stock and unique names, and you accept slower writes. |

**Simple rule:** start with one writer region and read copies. Move to home regions when far users complain about write speed. Allow many writers only for data you have checked can lose a write.

## At work: how a senior uses this

- Ask first: **"Which problem are we solving: speed, survival or law?"** Each one leads to a different design.
- For every table, write down which region owns its writes and what a conflict does.
- Count cross-region round trips per request, and multiply by the real numbers for your regions.
- Run a failover drill each quarter. Turn off each whole site or region in a test, not one server. Cloudflare had switched off its other two sites in tests, but never the whole of PDX-04, the one that failed. [@cf-2023]
- Before a drill, check that the standby region has quotas and capacity for full load, plus retries.
- List every dependency your failover runbook calls. Remove any that run in only one region.
- Alert on replication lag between regions. It is your RPO, moving in real time.

## With AI

### What to ask the agent

> "Here is our request handler and its database calls. Our users are in India and our database is in US East. Count the sequential cross-region round trips per request, and estimate the latency with a 200 ms round trip."

> "We use DynamoDB global tables in two regions. List every write in this service that could conflict if two regions change the same item, and say what last-writer-wins would drop."

> "Write a failover runbook for our Aurora global database. Fence writes first, then fail over, then update DNS. Mark every step that calls a control plane in another region."

### What to check yourself

- The latency numbers come from your provider's measured tables, not a guess.
- The runbook fences the old writer before it promotes the new one.
- No step in the recovery path needs a console or API that lives only in the failed region.
- Any code that checks then writes, like "is stock above zero?", runs in one region only.
- The test really cuts a whole region, including its queues and caches.

## Check yourself

## Go deeper

Before this: [Database replication](/learn/database-replication) and [CAP, PACELC and consistency models](/learn/cap-pacelc-consistency-models).

Related lessons: [Disaster recovery and backups](/learn/disaster-recovery-and-backups), [Quorums and leaderless replication](/learn/quorums-and-leaderless-replication), [DNS and service discovery](/learn/dns-and-service-discovery) and [CDN and HTTP caching](/learn/cdn-and-http-caching). Case study: the [Cloudflare outage](/incidents#cloudflare-2023-11-control-plane).
