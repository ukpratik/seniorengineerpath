---
topic: multi-tenancy
subtitle: Many customers share one system. Learn to keep their data apart and stop one busy customer from slowing down everyone else.
status: published
illustrative_story: true
sources:
  - id: saas-lens
    title: "AWS, “Silo, Pool, and Bridge Models”, SaaS Lens, AWS Well-Architected Framework"
    url: https://docs.aws.amazon.com/wellarchitected/latest/saas-lens/silo-pool-and-bridge-models.html
    note: Short definitions of the three models, and why one service might be siloed while another is pooled.
  - id: saas-noisy
    title: "AWS, “Noisy Neighbor”, SaaS Lens, AWS Well-Architected Framework"
    url: https://docs.aws.amazon.com/wellarchitected/latest/saas-lens/noisy-neighbor.html
    note: What a noisy neighbour is and why it matters more when tenants share resources.
  - id: aws-isolation
    title: "AWS, SaaS Tenant Isolation Strategies whitepaper (2020, now marked historical), introduction"
    url: https://docs.aws.amazon.com/whitepapers/latest/saas-tenant-isolation-strategies/saas-tenant-isolation-strategies.html
    note: Why tenant isolation is a core part of any SaaS design.
  - id: aws-pool
    title: "AWS, “Pool isolation”, SaaS Tenant Isolation Strategies whitepaper (2020, now marked historical)"
    url: https://docs.aws.amazon.com/whitepapers/latest/saas-tenant-isolation-strategies/pool-isolation.html
    note: Pros and cons of the pool model, including noisy neighbours, cost tracking and blast radius.
  - id: aws-fullstack
    title: "AWS, “Full stack silo and pool”, SaaS Architecture Fundamentals whitepaper (now marked historical)"
    url: https://docs.aws.amazon.com/whitepapers/latest/saas-architecture-fundamentals/full-stack-silo-and-pool.html
    note: Basic tier tenants in a pool and premium tenants in their own stacks, all on one version.
  - id: pg-rls
    title: "PostgreSQL documentation, “Row Security Policies” (section 5.9)"
    url: https://www.postgresql.org/docs/current/ddl-rowsecurity.html
    note: How row-level security works, the default-deny rule, and who bypasses it.
  - id: aws-rls
    title: "Michael Beardsley, “Multi-tenant data isolation with PostgreSQL Row Level Security”, AWS Database Blog, 2020"
    url: https://aws.amazon.com/blogs/database/multi-tenant-data-isolation-with-postgresql-row-level-security/
    note: A tenant policy that reads the tenant from a session setting, plus traps with owners and connection pools.
  - id: fairness
    title: "Amazon Builders' Library, “Fairness in multi-tenant systems”, 2020"
    url: https://builder.aws.com/content/3Eupj3d2bo4fEvlzYbICMZNhQ3B/fairness-in-multi-tenant-systems
    note: Per-tenant quotas, token buckets, telling clients their quota, and logging the throttle key.
  - id: shuffle
    title: "Amazon Builders' Library, “Workload isolation using shuffle-sharding”, 2019"
    url: https://builder.aws.com/content/3F06NpJ8YeoIGP8VHTw4n81pFn8/workload-isolation-using-shuffle-sharding
    note: The 8-worker example, the 1 in 28 maths and Route 53's 730 billion shuffle shards.
  - id: aws-cells
    title: "AWS, “What is a cell-based architecture?”, Reducing the Scope of Impact with Cell-Based Architecture whitepaper"
    url: https://docs.aws.amazon.com/wellarchitected/latest/reducing-scope-of-impact-with-cell-based-architecture/what-is-a-cell-based-architecture.html
    note: Cells, the partition key, the thin cell router and how cells limit a failure.
  - id: shopify-pods
    title: "Xavier Denis, “A Pods Architecture To Allow Shopify To Scale”, Shopify Engineering, 2018"
    url: https://shopify.engineering/a-pods-architecture-to-allow-shopify-to-scale
    note: Groups of shops on their own datastores, with a routing layer in front.
claims_checked:
  - claim: "SaaS Architecture Fundamentals (published 3 August 2022, now for historical reference only) says every full-stack silo must run the same configuration and software version as the pool, with no one-off variation."
    source: aws-fullstack
    note: "QC1-06: the page says \"each of these stacks should be running the same configuration of the stack, with the same version of the software\"; the paper's landing page gives the 2022 date. So it is not \"older\" than the 2020 whitepaper."
  - claim: Crossing the tenant isolation boundary is "a significant and potentially un-recoverable event" for a SaaS business.
    source: aws-isolation
    note: Exact quote from the introduction. The whitepaper (August 2020) is marked historical, so the lesson dates it.
  - claim: The pool model scales with the actual load of tenants; its cons include noisy neighbour, tenant cost tracking and a blast radius where an outage will likely hit all tenants.
    source: aws-pool
    note: Matches the Pros/Cons list. Page is marked historical; wording now attributes it to the 2020 whitepaper.
  - claim: With RLS enabled and no policy, default deny applies; superusers and BYPASSRLS roles always bypass; table owners normally bypass unless FORCE ROW LEVEL SECURITY.
    source: pg-rls
    note: Matches section 5.9. The policy SQL matches the AWS blog (current_setting('app.current_tenant')::UUID), which also warns about owners and PgBouncer.
  - claim: With 8 workers there are 28 pairs, so shuffle sharding cuts impact to 1/28, "7 times better" than 4 fixed shards (25%); Route 53 uses 2,048 virtual name servers, 4 per domain, about 730 billion shuffle shards.
    source: shuffle
    note: 8 choose 2 = 28 checked. Source also says at most one of another shuffle shard's workers is affected, given fault-tolerant clients.
  - claim: With 10 cells, a failure in one cell leaves 90% of requests unaffected; cells share no state and a thin router uses a partition key such as customer ID.
    source: aws-cells
    note: Matches the whitepaper text.
  - claim: Fairness means a "single-tenant experience"; quotas per tenant via token buckets; large responses may count as several requests; the throttle key and rate limit are logged on every request.
    source: fairness
    note: All found in the 2020 Builders' Library article.
  - claim: A Shopify pod is a set of shops on fully isolated datastores, routed by Sorting Hat, which stops one shop "sucking up database time across the platform".
    source: shopify-pods
    note: Matches the 2018 post.
reviewed_by: ai-factcheck
reviewed_on: 2026-10-21
quiz:
  - q: Your app runs one shared Postgres database for all tenants. What protects you best if a developer forgets a tenant filter in one query?
    options:
      - A code review checklist.
      - Row-level security on the table, with the app connecting as a role that is not the table owner.
      - A bigger database server.
    answer: 1
    why: Row-level security adds the tenant filter inside the database, for every query. The table owner skips it by default, so the app must use another role.
  - q: Which model gives the strongest isolation, at the highest cost per tenant?
    options:
      - Pool.
      - Bridge.
      - Silo.
    answer: 2
    why: In the silo model each tenant gets its own resources. Nothing is shared, but you pay for idle capacity and run many copies.
  - q: You have 8 workers. Each tenant gets a random pair of 2 workers (shuffle sharding). How many different pairs are there?
    options:
      - "4"
      - "16"
      - "28"
    answer: 2
    why: There are 8 × 7 / 2 = 28 pairs. A bad tenant takes down its own pair. Another tenant loses both workers only if it has the exact same pair.
  - q: One tenant's traffic grows 50 times in an hour. What should stop it from hurting the others first?
    options:
      - A per-tenant quota or rate limit that rejects the extra requests early.
      - Waiting for autoscaling to add servers.
      - Moving every tenant to its own database tonight.
    answer: 0
    why: A per-tenant limit rejects the extra requests at once and affects only that tenant. Autoscaling is slower and shares the new capacity with the noisy tenant.
  - q: Why should you log the tenant id on every request?
    options:
      - So you can see which tenant uses the most capacity, and who is affected in an incident.
      - Because the database needs it to run queries.
      - To make logs smaller.
    answer: 0
    why: Without the tenant id, a dashboard shows "the system is slow" but not "tenant 42 is causing it" or "tenants 7 and 9 are hurt".
incidents: []
cases: [amazon-shuffle-sharding, shopify-pods]
related: [sharding, rate-limiting, cell-architecture, load-shedding, tenant-onboarding, authorization-models, observability-slos]
---

## What broke

This story is made up, but it is common. A small company sells invoicing software to 400 businesses. All of them share one set of app servers and one Postgres database. This is cheap and easy to run.

On the last day of the year, the biggest customer starts a full export of five years of invoices. It runs as hundreds of heavy queries at once. The database CPU goes to 100%.

Every other customer now waits 20 seconds to open an invoice. Support gets messages from 399 businesses that did nothing wrong. The on-call engineer sees "database slow" on the dashboard, but nothing says which customer is causing it.

It takes an hour to find the export and stop it. Then a second question comes: "Could a slow query ever return another customer's invoices?" Nobody is sure. Every query has `WHERE tenant_id = ?`, but only because developers remembered to add it.

## The idea in simple words

A **tenant** is one customer organisation, such as one company. **Multi-tenancy** means one running system serves many tenants. The tenants share code, servers and often databases, but each must feel like it has the system to itself.

You must keep tenants apart in two ways:

1. **Data.** A tenant must never see another tenant's data. A 2020 AWS whitepaper calls crossing this line "a significant and potentially un-recoverable event" for a SaaS (software as a service) business. [@aws-isolation]
2. **Performance.** One tenant must not make the system slow for the rest. This is the **noisy neighbour** problem: one user puts so much load on shared resources that other users suffer. [@saas-noisy]

> [!key] **The key idea.** Sharing makes SaaS cheap. Every shared thing is also a path for a leak or a slowdown. Decide what to share on purpose, and isolate each shared thing.

## How it works

### Silo, pool and bridge

AWS names three models. [@saas-lens]

- **Silo model.** Each tenant gets its own resources, such as its own database or even its own full stack.
- **Pool model.** Tenants share resources. This is classic multi-tenancy, and it lowers the cost per tenant as you grow.
- **Bridge model.** A mix. Some services are siloed and others are pooled. Strict data rules or noisy load can push one service to silo. Cost and agility can push another to pool.

The pool is cheapest, because it scales with the real load of all tenants. The same 2020 whitepaper lists its costs too: noisy neighbours, harder cost tracking per tenant, and a bigger blast radius. One outage will likely hit every tenant. [@aws-pool]

You can mix models by price tier. Basic tenants share a pool, and premium tenants pay for their own stack. A 2022 AWS whitepaper (now archived) adds a warning: every silo should run the same version and setup as the pool, with no one-off changes. [@aws-fullstack] If you skip this, you end up running many different products.

### Tenant id everywhere

In a pool, the tenant id is the only thing that keeps data apart. Our advice:

- Put `tenant_id` on every row of every tenant table, and in every index that starts a lookup.
- Read the tenant from the login token once, at the edge. Pass it down in a request context. Never trust a tenant id from the request body.
- Put the tenant id in every log line, metric label, cache key, queue message and file path.

A cache key like `invoice:991` can leak data between tenants. Use `tenant:42:invoice:991`.

### Row-level security

Remembering `WHERE tenant_id = ?` in every query is not enough. One missed filter leaks data. Row-level security (RLS) moves the rule into the database.

In PostgreSQL, you turn on RLS for a table and add a policy. If RLS is on but no policy exists, the default is to deny: no rows are visible. [@pg-rls] A common tenant policy reads the current tenant from a session setting:

```sql
ALTER TABLE invoices ENABLE ROW LEVEL SECURITY;
CREATE POLICY tenant_isolation ON invoices
  USING (tenant_id = current_setting('app.current_tenant')::uuid);
```

The app sets `app.current_tenant` at the start of each request. Now the database adds the filter to every query. [@aws-rls]

Know the traps:

- Superusers and roles with `BYPASSRLS` always skip the policies. The table owner usually skips them too, unless you use `FORCE ROW LEVEL SECURITY`. [@pg-rls] So the app must connect as a role that does not own the tables. [@aws-rls]
- Session settings may not work with a connection pooler such as PgBouncer, which can share session state. [@aws-rls] Our advice: set the tenant inside each transaction, for example with `SET LOCAL`, and test it.

### Noisy neighbours and per-tenant limits

The first wall is a per-tenant limit. Amazon describes fairness as giving every client "a single-tenant experience". Its services set quotas per tenant. A common way to enforce them is a token bucket. [@fairness] See [rate limiting](/learn/rate-limiting) for how a token bucket works.

Not all requests cost the same. Amazon notes that the cost of a call is not always known up front, and some services count large responses as several requests. [@fairness] Our advice: limit the costly things separately. Cap each tenant's concurrent exports, background jobs and database connections, not just requests per second.

Tell tenants about their limits. Amazon's services show clients their quota and how close they are to it. [@fairness]

### Cells

Limits stop one tenant from using too much. Cells stop one failure from reaching everyone.

A **cell** is a full, independent copy of your workload. It shares no state with other cells and serves only some of the tenants. A thin **cell router** sends each request to the right cell, using a partition key such as the customer id. With 10 cells, a failure in one cell leaves 90% of requests unaffected. [@aws-cells]

Shopify does this with **pods**. Each pod is a set of shops on its own fully isolated datastores. Sorting Hat, software in Shopify's load balancers, sends each request to the right pod. This keeps one shop from "sucking up database time across the platform". [@shopify-pods]

### Shuffle sharding

Shuffle sharding goes one step further. Amazon's example has 8 workers. [@shuffle]

- With no sharding, a bad customer can crash all 8 workers. Everyone is down.
- With 4 fixed shards of 2 workers, a bad customer crashes one shard. That is 25% of customers. [@shuffle]
- With shuffle sharding, each customer gets a random pair of workers. There are 28 possible pairs. A bad customer crashes its own pair. Only customers with that exact pair lose everything. The impact drops to 1/28, which Amazon calls 7 times better than plain sharding. [@shuffle]

Every other customer shares at most one worker with the bad customer. They keep working if their clients retry on the other worker. [@shuffle] Route 53 uses 2,048 virtual name servers and gives each domain 4 of them. That makes about 730 billion possible shuffle shards. [@shuffle]

Step through it with Next and Back, or the ← and → keys.

[[diagram:noisy-neighbour-cells]]

### Big tenants and observability

Some tenants are much bigger than the rest. Our advice: give them their own cell or silo, as a paid tier, before they become a problem. Move them in a planned migration, not during an incident.

Amazon puts the throttle key and the rate limit in its request log on every request. [@fairness] Build dashboards for the top tenants by requests, CPU time and errors. Your first question in an incident should be "Which tenant?", and the answer should take seconds.

## Trade-offs

| Model | Data isolation | Noisy neighbour risk | Cost per tenant | Use it when |
|---|---|---|---|---|
| Silo | Yes. Separate resources. | No. Its resources are its own. | High. Idle capacity, many copies to run. | Strict data rules, or big tenants who pay for it. |
| Pool | Only by tenant id and RLS. | Yes. Needs per-tenant limits. | Low. Scales with real load. | Many small tenants. |
| Bridge | Mixed, per service. | Mixed. | Medium. | Some data or services need a silo, the rest do not. |
| Cells | Yes, between cells. | Only inside one cell. | Medium. Needs a router and a system to manage the cells. | You must limit how many tenants one failure can hit. |
| Shuffle sharding | Not by itself. | Rarely shared fully. | Low to medium. | Many tenants, and clients that can retry on another worker. |

**Our simple rule:** start with a pool, RLS and per-tenant limits. Add cells when one failure would hurt too many tenants. Give the biggest tenants a silo as a paid tier.

## At work: how a senior uses this

- In every design review, ask: **"Where is the tenant id here, and what stops a query without it?"**
- Write a test that logs in as tenant A and tries to read tenant B's data through every API. Run it in CI.
- Ask: **"What is the most one tenant can use?"** Check requests, connections, jobs, storage and export size.
- Put tenant id on every log line and metric, so on-call can see who is noisy and who is hurt.
- Keep a list of your largest tenants and their growth. Plan their move to a bigger tier early.
- Keep every silo on the same version as the pool. Treat a "special build for one customer" as a warning sign.
- Before an incident, know your blast radius: how many tenants share each database, cell and queue.

## With AI

### What to ask the agent

> "Add PostgreSQL row-level security to these tables. Use a policy on tenant_id that reads current_setting('app.current_tenant'). The app role must not own the tables."

> "Find every query, cache key and queue message in this service that does not include the tenant id. List them by file and line."

> "Write an integration test: log in as tenant A, then call every read endpoint with ids that belong to tenant B. Every call must return 404 or 403."

> "Add a per-tenant limit on concurrent export jobs, 2 per tenant, stored in Redis. Log the tenant id and the limit when a job is rejected."

### What to check yourself

- The app connects as a role that does not own the tables and does not have BYPASSRLS. Otherwise RLS does nothing.
- The tenant id comes from the auth token, not from a header or body the client controls.
- The tenant setting is set per transaction and cannot leak to the next request on a pooled connection.
- Cache keys, file paths and search indexes include the tenant id, not only database rows.
- Limits are per tenant, not global. A global limit lets one tenant use it all.

## Check yourself

## Go deeper

Related lessons: [Sharding](/learn/sharding-and-consistent-hashing), [Rate limiting](/learn/rate-limiting), [Cell-based architecture](/learn/cell-based-architecture) and [Load shedding](/learn/load-shedding-graceful-degradation).
