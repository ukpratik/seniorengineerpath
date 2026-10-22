---
topic: api-gateway
subtitle: An API gateway does the shared work for every request at the edge. Learn what belongs there, what does not, and how to stop it taking everything down.
status: published
reviewed_by: ai-factcheck
reviewed_on: 2026-10-22
illustrative_story: false
sources:
  - id: gcp-2025
    title: "Google Cloud, “Multiple GCP products are experiencing Service issues” incident report (official, June 2025)"
    url: https://status.cloud.google.com/incidents/ow5i3PPK96RduMcb1SsW
    note: How a crash in Service Control, the policy and quota check in front of Google's APIs, caused 503 errors worldwide.
  - id: az-offload
    title: "Microsoft, “Gateway Offloading pattern”, Azure Architecture Center"
    url: https://learn.microsoft.com/en-us/azure/architecture/patterns/gateway-offloading
    note: Shared jobs a gateway can take over, and the risks, including keeping business logic out.
  - id: az-routing
    title: "Microsoft, “Gateway Routing pattern”, Azure Architecture Center"
    url: https://learn.microsoft.com/en-us/azure/architecture/patterns/gateway-routing
    note: One endpoint in front of many services and versions, and the single point of failure it adds.
  - id: az-bff
    title: "Microsoft, “Backends for Frontends pattern”, Azure Architecture Center"
    url: https://learn.microsoft.com/en-us/azure/architecture/patterns/backends-for-frontends
    note: A separate backend for each user interface, with a worked example behind a gateway.
  - id: newman-bff
    title: "Sam Newman, “Backends For Frontends”, samnewman.io, 2015"
    url: https://samnewman.io/patterns/architectural/bff/
    note: The original write-up of the BFF pattern, including "one experience, one BFF".
  - id: aws-validate
    title: "AWS documentation, “Request validation for REST APIs in API Gateway”"
    url: https://docs.aws.amazon.com/apigateway/latest/developerguide/api-gateway-method-request-validation.html
    note: Basic checks a managed gateway can run before calling your backend.
  - id: aws-quotas
    title: "AWS documentation, “Amazon API Gateway quotas”"
    url: https://docs.aws.amazon.com/apigateway/latest/developerguide/limits.html
    note: The default request rate limit per account and Region, with its token bucket burst (checked October 2026).
  - id: aws-rest-quotas
    title: "AWS documentation, “Quotas for configuring and running a REST API in API Gateway”"
    url: https://docs.aws.amazon.com/apigateway/latest/developerguide/api-gateway-execution-service-limits-table.html
    note: The integration timeout range and other limits on a managed gateway (checked October 2026).
  - id: envoy-timeouts
    title: "Envoy proxy documentation, “How do I configure timeouts?”"
    url: https://www.envoyproxy.io/docs/envoy/latest/faq/configuration/timeouts
    note: Route timeout, per-try timeout and idle timeouts, with their defaults.
  - id: envoy-routing
    title: "Envoy proxy documentation, “HTTP routing”, section “Retry semantics”"
    url: https://www.envoyproxy.io/docs/envoy/latest/intro/arch_overview/http/http_routing
    note: How a proxy retries, why retry budgets are preferred, and how retries fit inside the overall timeout.
  - id: istio-mesh
    title: "Istio, “What is a service mesh?”"
    url: https://istio.io/latest/about/service-mesh/
    note: What a service mesh adds between services, and how it uses proxies built on Envoy.
claims_checked:
  - claim: 'Google APIs are served through an API management layer that checks each request for authorization, policy and quota; Service Control is the regional core binary for these checks.'
    source: gcp-2025
    note: 'Paraphrased from the report''s opening. Same framing as error-handling.md and the incident card.'
  - claim: 'On June 12, 2025, policy data with blank fields replicated globally within seconds; Service Control crashed in every region and API requests got 503 errors.'
    source: gcp-2025
    note: 'Report says "replicated globally within seconds" and "increased 503 errors in external API requests". Start times differ by source (10:45, 10:49, 10:51 PDT), so no clock time is quoted.'
  - claim: 'Root cause found within 10 minutes; red-button rollout complete within 40 minutes; us-central1 took up to about 2 h 40 min to fully resolve; fixes include fail open and incremental global data propagation.'
    source: gcp-2025
    note: 'Durations quoted as the report gives them. No end time quoted, because the report gives several (12:48, 13:49, 18:18 PDT) for different scopes.'
  - claim: 'Gateway terminates TLS then re-encrypts to the backend; never offload business logic; backend changes can force gateway redeploys; run multiple instances, size for peak bursts, drain; prevent bypass; don''t trust unverified forwarded headers; gateway adds latency.'
    source: az-offload
    note: 'All in "Problems and considerations" and "When to use" (page dated 2026-09-23).'
  - claim: 'A routing gateway can introduce a single point of failure and a bottleneck, should be load tested, routes on IP, port, header or URL, and lets you revert a bad version with a config change without affecting clients.'
    source: az-routing
    note: '"Issues and considerations" and "Multiple versions" sections.'
  - claim: 'A BFF is "typically maintained by the same team as the user interface"; guideline "one experience, one BFF".'
    source: newman-bff
    note: 'Article dated Nov 18 2015. Newman credits the guideline to Stewart Gleadow.'
  - claim: 'Code duplication is a probable outcome of BFFs; Microsoft''s example puts API Management in front of per-client BFFs.'
    source: az-bff
    note: 'Quoted from "Problems and considerations" and the "Example" section.'
  - claim: 'AWS API Gateway default throttle is 10,000 RPS per account per Region with a token bucket of 5,000; some Regions default to 2,500/1,250.'
    source: aws-quotas
    note: 'Checked 2026-10-22. Quota can be increased; lesson dates the number.'
  - claim: 'REST API integration timeout is 50 ms to 29 s by default; can be raised above 29 s for Regional and private APIs.'
    source: aws-rest-quotas
    note: 'Checked 2026-10-22. Raising it may lower the Region-level throttle quota.'
  - claim: 'Basic validation checks required parameters and a JSON schema body; on failure API Gateway returns 400 without calling the backend.'
    source: aws-validate
    note: 'Page says it "immediately fails the request, returns a 400 error response" and "reduces unnecessary calls to the backend".'
  - claim: 'Envoy route timeout defaults to 15 seconds and covers the complete upstream response; all retries fit inside the overall request timeout; retry budgets are preferred to avoid retry storms.'
    source: envoy-timeouts
    note: 'Retry statements are from envoy-routing ("All retries are contained within the overall request timeout"; "configure retry budgets (preferred)").'
  - claim: 'Istio adds mTLS, traffic control and observability without code changes, and is powered by Envoy.'
    source: istio-mesh
    note: 'Page also offers ambient mode, so the lesson says proxies are "often" next to each service, not always.'
quiz:
  - q: Which job should NOT live in the API gateway?
    options:
      - Checking that the access token is valid.
      - Deciding whether an order can be refunded.
      - Rejecting clients that send too many requests.
    answer: 1
    why: A refund rule is business logic. It belongs in the orders service, so changing it does not mean changing and redeploying the shared gateway.
  - q: What is the main difference between an API gateway and a service mesh?
    options:
      - A gateway handles requests coming in from outside; a mesh handles calls between your own services.
      - A gateway only works over HTTP/1.1.
      - A mesh replaces the need for TLS.
    answer: 0
    why: The gateway is the front door for clients. A mesh uses proxies, often one next to each service, to manage service-to-service traffic.
  - q: The gateway's route timeout is 15 seconds. The orders service has its own 20 second timeout to the database. What can happen?
    options:
      - Nothing, the longer timeout wins.
      - The gateway gives up at 15 seconds, but the service keeps working for a client that has already left.
      - The client waits 35 seconds.
    answer: 1
    why: Envoy's route timeout ends the wait at the gateway. Inner timeouts should be shorter than outer ones, or work continues after nobody is waiting.
  - q: Your gateway may retry failed upstream calls. Which requests are safe to retry automatically?
    options:
      - All POST requests.
      - Requests that are idempotent, such as GET, or POST with an idempotency key.
      - Only requests that timed out after the response started.
    answer: 1
    why: A retry can run the request twice. Only retry when running it twice has the same effect as running it once.
  - q: "In June 2025, why did one bad policy record break Google APIs in every region?"
    options:
      - The policy data was copied to all regions within seconds, and every region's check crashed on it.
      - A single data centre lost power.
      - A DNS record expired.
    answer: 0
    why: The data spread to every region within seconds, and the new code had no error handling and no feature flag.
incidents: [google-cloud-2025-06-service-control]
related: [rest-api, load-balancing, rate-limiting, authn-authz, graphql-bff, service-mesh, api-versioning, idempotency-retries]
---

## What broke

This is a real incident, retold from Google's official report. Google and Google Cloud APIs are served through an API management layer. For each incoming API request, it checks that the request is authorized and within policy and quota. [@gcp-2025]

The core program that does these checks is called Service Control. It is a regional service, and it sits in the request path of each API call that comes in. [@gcp-2025]

On June 12, 2025, a policy change with blank fields was copied to every region within seconds. A new quota check in Service Control read the blank fields and crashed, again and again, in every region. API requests got 503 errors around the world. [@gcp-2025]

So one shared check, in front of everything, took down many Google Cloud products at once. Engineers found the cause within 10 minutes and finished turning the check off within 40 minutes. One of the larger regions, us-central1, took up to about 2 hours 40 minutes to recover fully. [@gcp-2025]

Among Google's fixes: make Service Control fail open, so a failing check does not block API requests. And copy global data changes in stages, with time to catch problems. [@gcp-2025] The code-level story (no error handling, no feature flag) is in [Error handling and defensive design](/learn/error-handling-defensive-design).

The lesson for you: the layer that every request passes through is also the layer that can stop every request. See the [incident](/incidents#google-cloud-2025-06-service-control).

## The idea in simple words

An API gateway is one front door for many services. Clients call one address. The gateway does the shared work, then sends each request to the right service.

Without it, every service must do its own TLS, token checks, rate limits and logging. With it, you do those once, in one place. Microsoft calls this "offloading" shared jobs to the gateway. [@az-offload]

> [!key] **The key idea.** Put shared, generic work in the gateway: checks that are the same for every service. Keep business rules in the services. The gateway must be as boring and as reliable as possible, because everything depends on it.

## How it works

### What a gateway does

A request passes through the gateway's steps in order. A cheap check that fails stops the request before any expensive work.

1. **TLS termination.** The gateway decrypts the request. Microsoft's guidance is to then open a new TLS connection to the backend, not plain HTTP. [@az-offload]
2. **Authentication.** Check the token or API key. See [Authentication and authorization](/learn/authentication-and-authorization).
3. **Rate limiting.** Reject clients that send too much. For example, as of October 2026, AWS API Gateway's default limit is 10,000 requests a second per account and Region. A token bucket allows bursts of up to 5,000 requests. Some newer Regions have lower defaults. [@aws-quotas] See [Rate limiting](/learn/rate-limiting).
4. **Request validation.** AWS API Gateway can check that required parameters exist and that the body matches a JSON schema. A bad request gets a 400 error at once, so your backend never sees it. [@aws-validate]
5. **Routing.** Send `/orders` to the orders service and `/search` to search, by URL, header, IP or port. [@az-routing]
6. **Transformation.** Rewrite headers, redirect an old URL to a new one, or add a correlation ID for logs. [@az-offload]
7. **Observability.** Log and measure every request in the same way, even for services that are not well instrumented. [@az-offload]

Step through one request. Use Next and Back, or the ← and → keys.

[[diagram:api-gateway-request]]

### Gateway, load balancer or service mesh?

These three overlap, and one product can play several roles. Ask what each one knows.

- A **load balancer** spreads requests across copies of one service. It knows about servers and their health. See [Load balancing](/learn/load-balancing).
- An **API gateway** sits at the edge, in front of many services. It knows about APIs: keys, users, plans, routes and versions.
- A **service mesh** handles calls between your own services. It uses proxies, often one next to each service. It adds things like mutual TLS, traffic control and metrics without app code changes. Istio is one mesh, and it is built on the Envoy proxy. [@istio-mesh] See [Service mesh and sidecars](/learn/service-mesh-and-sidecars).

### Backend for frontend

A mobile app and a desktop website often need different data. One shared backend that serves both gets pulled in two directions. [@az-bff]

The backend-for-frontend (BFF) pattern gives each interface its own small backend. Sam Newman says a BFF is usually owned by the same team as the user interface. His guide is "one experience, one BFF". [@newman-bff] Expect some duplicated code across BFFs; Microsoft lists that as a likely cost. [@az-bff] Microsoft's worked example puts one gateway in front for shared checks, with a BFF for each client behind it. [@az-bff] See [GraphQL and backend-for-frontend](/learn/graphql-and-backend-for-frontend).

### Keep business logic out

Microsoft's advice is short: never offload business logic to the gateway. [@az-offload] If the gateway holds rules for one service, each change to that service can force a gateway redeploy. If the gateway team releases more slowly, it becomes a bottleneck for every other team. [@az-offload]

A good test: would this logic be the same for every service? Token checks, yes. "Refunds over ₹10,000 need a manager", no.

### A single point of failure, and a bottleneck

Every request passes through the gateway, so it can become a single point of failure and a bottleneck. Microsoft says to plan for that, and to load test it. [@az-routing] Run several instances, size them for peak bursts, not the average, and drain them when you restart. [@az-offload]

Three more traps from the same guide. [@az-offload]

- **Bypass.** If clients can call a backend directly, they skip auth and limits. Let backends accept traffic only from the gateway.
- **Forged identity.** Do not treat an unverified forwarded header as proof of who the caller is. The gateway must also prove its own identity to the backend.
- **Extra latency.** Each hop and each check adds time. Measure it.

Decide in advance what each check does when it fails. Google plans to make Service Control fail open. [@gcp-2025] Our advice: a broken rate limiter can fail open, but a broken token check must fail closed.

### Versioning at the edge

The gateway can send `/v1` and `/v2` to different versions of a service. It can also shift traffic to a new version step by step. If the new version is bad, you roll back with a config change, and clients do not notice. [@az-routing] See [API versioning](/learn/api-versioning-and-lifecycle).

### Timeouts and retries at the edge

The gateway gives up on slow services. Envoy's route timeout defaults to 15 seconds. It covers the wait for the complete response from the service. [@envoy-timeouts] On AWS API Gateway, as of October 2026, a REST API's integration timeout is 50 milliseconds to 29 seconds by default. Regional and private APIs can ask for more. [@aws-rest-quotas]

Our advice: set the gateway's timeout a little longer than the service's own timeouts. Otherwise the service keeps working after the client is gone.

A gateway can also retry. In Envoy, all retries fit inside the overall request timeout. To avoid retry storms, Envoy's docs prefer retry budgets. A retry budget limits the share of active requests that can be retried. [@envoy-routing] Only retry requests that are safe to run twice. See [Idempotency, retries and timeouts](/learn/idempotency-retries-timeouts).

## Trade-offs

| Option | What you get | What it costs | Use it when |
|---|---|---|---|
| No gateway | No extra hop. Nothing shared to fail. | Every service repeats TLS, auth, limits and logs. | One or two services. |
| One shared gateway | Shared checks in one place, one public address. | A single point of failure and a team that can become a queue. | Many services, one kind of client. |
| Gateway plus BFFs | Each client gets an API shaped for it. | More services to run. Some duplicated code. | Mobile and web need different data. |
| Gateway plus service mesh | The same controls for traffic between services. | A proxy next to every service, and more to learn. | Many services calling each other. |

**Our simple rule:** keep the gateway thin and generic. Add BFFs or a mesh only when you feel the pain they fix.

## At work: how a senior uses this

- For every new gateway rule, ask: **"Is this the same for every service?"** If not, it goes in the service.
- Know what happens when each gateway check fails. Write "fail open" or "fail closed" next to it.
- Treat gateway config like code. Review it, test it and roll it out in stages, as Google now plans to do with global data. [@gcp-2025]
- Line up timeouts from the outside in: client, then gateway, then service, then database. Each inner one is shorter.
- Watch gateway latency and error rate per route. A slow gateway makes every service look slow.
- Make sure backends cannot be called around the gateway.

## With AI

### What to ask the agent

> "Read our gateway config. List every route with its upstream, timeout, retry policy and auth rule. Flag any route that retries a POST without an idempotency key."

> "Here is a gateway plugin we wrote. Point out any business logic in it that should move into a service, and explain why."

> "Write a test plan for our gateway: kill one instance under load, make the token service slow, and send a request with a forged user header. Say what users should see in each case."

### What to check yourself

- The agent did not invent config keys. Check them against the docs for your gateway.
- Retries are only on idempotent calls, and they sit inside the overall timeout.
- Auth checks fail closed. Only checks like rate limits fail open, and that choice is written down.
- The gateway's timeout is longer than the service's own timeouts.
- Any new header that carries identity is set by the gateway and cannot come from the client.

## Check yourself

## Go deeper

Read the [Netflix Zuul case](/cases#netflix-zuul-async) about moving a gateway to async I/O. Related lessons: [REST API design](/learn/rest-api-design), [Backoff and jitter](/learn/backoff-jitter-retry-budgets) and [Circuit breakers and bulkheads](/learn/circuit-breakers-and-bulkheads).
