---
topic: service-mesh
subtitle: A service mesh moves mTLS, retries, traffic control and metrics out of your code and into proxies. Learn what that buys you, what it costs, and when to say no.
status: published
reviewed_by: ai-factcheck
reviewed_on: 2026-11-09
illustrative_story: false
sources:
  - id: monzo
    title: "Monzo Community, “RESOLVED: Current account payments may fail - Major Outage (27/10/2017)”, post-incident write-up by Monzo's Head of Engineering (30 October 2017)"
    url: https://community.monzo.com/t/resolved-current-account-payments-may-fail-major-outage-27-10-2017/26296/95
    note: The full timeline of an outage where linkerd stopped getting routing updates, and a restart of every proxy made it worse.
  - id: istio-arch
    title: "Istio documentation, “Architecture”"
    url: https://istio.io/latest/docs/ops/deployment/architecture/
    note: The data plane of Envoy proxies, the control plane (istiod), and the features each proxy has.
  - id: istio-security
    title: "Istio documentation, “Security”"
    url: https://istio.io/latest/docs/concepts/security/
    note: Workload identity, how certificates are issued and rotated, the mutual TLS steps, and permissive and strict modes.
  - id: istio-traffic
    title: "Istio documentation, “Traffic Management”"
    url: https://istio.io/latest/docs/concepts/traffic-management/
    note: Timeouts, retries, weighted routing for canaries, and how mesh and app retries can conflict.
  - id: istio-perf
    title: "Istio documentation, “Performance and Scalability”"
    url: https://istio.io/latest/docs/ops/deployment/performance-and-scalability/
    note: Measured CPU, memory and latency for sidecars, ztunnel and waypoints in Istio 1.24.
  - id: istio-ambient
    title: "Istio documentation, “Overview” (ambient mode)"
    url: https://istio.io/latest/docs/ambient/overview/
    note: How ambient mode splits the mesh into a per-node L4 proxy and optional L7 waypoint proxies.
  - id: ambient-intro
    title: "John Howard, Ethan J. Jackson, Yuval Kohavi, Idit Levine, Justin Pettit and Lin Sun, “Introducing Ambient Mesh”, Istio blog (7 September 2022)"
    url: https://istio.io/latest/blog/2022/introducing-ambient-mesh/
    note: The problems with sidecars that led Istio to build a mode without them.
  - id: ambient-ga
    title: "Lin Sun, “Fast, Secure, and Simple: Istio’s Ambient Mode Reaches General Availability in v1.24”, Istio blog (7 November 2024)"
    url: https://istio.io/latest/blog/2024/ambient-reaches-ga/
    note: Ambient mode became generally available in Istio 1.24.
  - id: envoy-what
    title: "Envoy documentation, “What is Envoy”"
    url: https://www.envoyproxy.io/docs/envoy/latest/intro/what_is_envoy
    note: Why a proxy outside the app process works with any language and can be upgraded across a fleet.
  - id: linkerd-retries
    title: "Linkerd documentation, “Retries and Timeouts”"
    url: https://linkerd.io/2-edge/features/retries-and-timeouts/
    note: Why mesh retries should be limited to idempotent requests, and how they can take services down.
  - id: istio-tracing
    title: "Istio documentation, “Overview” (distributed tracing)"
    url: https://istio.io/latest/docs/tasks/observability/distributed-tracing/overview/
    note: Proxies send spans, but your app must pass the trace headers along.
claims_checked:
  - claim: "Monzo, 27 October 2017: deploy at 14:10, rolled back at 14:12; at 14:18 linkerd was routing to IPs of processes that no longer existed; at 14:26 all several hundred linkerd instances were restarted; at 15:13 no service could serve requests; at 15:27 a NullPointerException parsing service discovery data; deleting the empty service fixed it at 15:31."
    source: monzo
    note: "Matches the timeline in post #95. The post calls the restart \"an unfortunate and poor decision\". It gives the etcd change as both \"two weeks before\" and \"the week prior\", so the lesson says \"a week or two earlier\"."
  - claim: "In Istio the HTTP timeout is off by default and failed HTTP requests are retried twice, so 3 app tries can become 9 attempts."
    source: istio-traffic
    note: "\"The Envoy timeout for HTTP requests is disabled in Istio by default\"; \"retry twice before returning the error\". 3 x 3 = 9 is the lesson's arithmetic."
  - claim: "With a 2 second app timeout and a 3 second mesh timeout with one retry, the app gives up first and the mesh settings have no effect; the two work independently and can conflict."
    source: istio-traffic
    note: "\"the application's timeout kicks in first, so your Envoy timeout and retry attempt has no effect\"; \"both work independently, and therefore might conflict\"."
  - claim: "Istio 1.24 at 1,000 rps with 1 KB payloads: a sidecar uses about 0.20 vCPU and 60 MB, a ztunnel about 0.06 vCPU and 12 MB, a waypoint about 0.25 vCPU and 60 MB."
    source: istio-perf
    note: "Exact figures from the Performance summary for Istio 1.24. The page says different hardware gives different values."
  - claim: "Istio built ambient mode because sidecars need a pod restart to install or upgrade, are sized for each pod's worst case, and can break apps with non-conformant HTTP."
    source: ambient-intro
    note: "Matches the three problems listed in the 2022 announcement."
  - claim: "Workload identity in Istio comes from the Kubernetes service account; the agent creates the key and CSR and istiod signs it; the client-side Envoy checks the server's service account may run the target service; permissive mode accepts plain text and mTLS, strict only mTLS."
    source: istio-security
    note: "Matches the identity, certificate management, mutual TLS (secure naming) and peer authentication sections."
quiz:
  - q: In a service mesh, which part sits on the path of every request?
    options:
      - The control plane.
      - The data plane, meaning the proxies.
      - The certificate authority.
    answer: 1
    why: The proxies carry all traffic between services. The control plane only configures them and gives them certificates.
  - q: Your code tries each call 3 times. The mesh keeps Istio's default retry policy. A downstream service is down. How many attempts can one call make?
    options:
      - "3 attempts."
      - "5 attempts."
      - "9 attempts."
    answer: 2
    why: Istio retries twice by default, so each of your 3 tries becomes up to 3 attempts. 3 × 3 = 9.
  - q: Your app has a 2 second timeout. The mesh has a 3 second timeout with one retry. What happens when the callee hangs?
    options:
      - The app gives up at 2 seconds, so the mesh retry never helps.
      - The mesh retry runs first, then the app gives up at 6 seconds.
      - The two timeouts add up to 5 seconds.
    answer: 0
    why: Istio's docs use this exact case. The settings work independently, and the shorter app timeout wins.
  - q: What is the main reason Istio built ambient mode?
    options:
      - Sidecars could not do mutual TLS.
      - Sidecars need a pod restart to install or upgrade, and must be sized for each pod's worst case.
      - Envoy is too slow for HTTP traffic.
    answer: 1
    why: The ambient announcement names invasiveness and wasted resources. Waypoints in ambient mode still use Envoy.
  - q: You have six services in one language, a shared HTTP client library, and a two-person platform team. A teammate wants a mesh for retries. What is the best first answer?
    options:
      - Adopt it now, because every service will need one later.
      - Put timeouts and retries in the shared library first, and revisit a mesh when you need mTLS or many languages.
      - Use both, so retries happen twice.
    answer: 1
    why: A mesh is one more system to run and upgrade. A shared library solves this need with far less to operate.
incidents: [monzo-2017-10-linkerd]
related: [load-balancing, tls, resilience-patterns, backoff-jitter, distributed-tracing, dns-discovery, safe-deploys, api-gateway, container-patterns]
---

## What broke

On Friday 27 October 2017, at 14:10, Monzo deployed a change to a payments service. Current account payments started to fail. The team rolled the change back two minutes later. The errors did not stop [@monzo].

Monzo used linkerd to route and load balance the calls between its several hundred services. Linkerd learned where each service ran from Kubernetes. At 14:18, engineers found that linkerd was sending traffic to IP addresses of processes that no longer existed. It had stopped getting updates [@monzo].

At 14:26 they restarted all of the several hundred linkerd proxies. The new proxies could not start until the Kubernetes API servers were restarted too. By 15:13 every proxy was back, but no service got any traffic. Now the whole platform was down, including the app [@monzo].

At 15:27 the logs showed the real blocker. Linkerd crashed with a NullPointerException while it read service discovery data. A new service, deployed the day before and then scaled to zero copies, triggered a bug between the linkerd and Kubernetes versions. Deleting that empty service fixed it at 15:31 [@monzo].

The first fault was a bug in Kubernetes and its etcd client after an etcd cluster upgrade a week or two earlier. Monzo called the restart of every proxy a poor decision that made the outage worse [@monzo]. The proxy layer touched every call. So when it broke, everything broke.

## The idea in simple words

In [microservices](/learn/monolith-modular-monolith-microservices), every call between services needs the same things. It needs a timeout, a retry rule, encryption, a check of who is calling, and metrics. You can write these in every service, in every language. Or you can move them into a proxy.

A **service mesh** is that proxy layer, plus a system that manages it. It has two parts [@istio-arch]:

- **The data plane.** Proxies that carry every request between services. In Istio these are Envoy proxies, often deployed as sidecar proxies [@istio-arch].
- **The control plane.** A service that tells the proxies what to do: routes, endpoints, rules and certificates. In Istio this is istiod [@istio-arch].

The app calls `http://payments` as if the network were simple. The proxies do the hard parts. Because the proxy is a separate process, it works with any language, and you can upgrade it without changing app code [@envoy-what].

> [!key] **The key idea.** A mesh does not remove network work. It moves that work out of your code into proxies that your team must now run. You trade code in many services for one more critical system.

## How it works

Step through one call from an orders service to a payments service. Use Next and Back, or the ← and → keys.

[[diagram:mesh-call-path]]

### mTLS and workload identity

In the [TLS lesson](/learn/tls-and-secure-connections) you saw mutual TLS. A mesh makes it the default. Each workload gets a workload identity. In Istio, it comes from the workload's Kubernetes service account [@istio-security]. An agent next to each proxy makes a private key and asks istiod to sign a certificate. It also renews that certificate before it expires [@istio-security]. Nobody copies certificates by hand.

On each call, the client-side proxy starts the mTLS handshake. It checks that the server's certificate names a service account allowed to run the target service [@istio-security]. The server-side proxy can then apply rules such as "only `orders` may call `payments`" [@istio-security].

To roll this out, start in permissive mode. It accepts both plain text and mTLS while you add proxies. Then switch to strict mode, which accepts only mTLS [@istio-security].

### Timeouts, retries and traffic splitting

Mesh config sets [timeouts and retries](/learn/idempotency-retries-timeouts) per service, with no app change. Check the defaults. In Istio, the HTTP timeout is off by default. Failed HTTP requests are retried twice before the error is returned [@istio-traffic].

That default meets your own code. Say your HTTP client tries each call 3 times. Each of those tries can become 3 attempts in the proxy. So one call can hit a failing service 9 times. This is [retry amplification](/learn/backoff-jitter-retry-budgets). Linkerd warns that services under constant retries can be taken down by them, not given time to recover [@linkerd-retries].

Timeouts clash in the same way. Istio's docs give an example. The app has a 2 second timeout. The mesh has a 3 second timeout with one retry. The app gives up first, so the mesh settings have no effect [@istio-traffic]. Mesh and app settings work on their own, and can conflict [@istio-traffic].

Retry only safe requests. Linkerd says retrying a request that changes state each time "can be disastrous" [@linkerd-retries]. Use [idempotency keys](/learn/idempotency-retries-timeouts) for the rest.

The mesh can also split traffic by weight. Istio's example sends 75% of requests to version 1 and 25% to version 2, for a [canary](/learn/safe-deploys-config-changes) or an A/B test [@istio-traffic]. You raise the weight in config, not with a new deploy.

The same proxies give you per-host limits and ejection of bad hosts. The [circuit breakers lesson](/learn/circuit-breakers-and-bulkheads) covers those.

### Telemetry

Every proxy reports metrics for the traffic it carries [@istio-arch]. So you get request count, errors and latency for every service, in every language, from day one. Proxies can send trace spans too [@istio-tracing]. But your app must still pass the trace headers from each incoming request to its outgoing calls [@istio-tracing]. The [distributed tracing lesson](/learn/distributed-tracing) explains why.

### What it costs

**Resources.** The Istio project tested version 1.24 at 1,000 requests per second with 1 KB payloads. One sidecar used about 0.20 vCPU and 60 MB of memory [@istio-perf]. Multiply that by your pods. For 400 pods, 60 MB each is 24 GB of memory just for proxies. Real numbers depend on your hardware and config [@istio-perf].

**Latency.** In sidecar mode, each call passes through two extra proxies: one next to the caller and one next to the callee [@istio-security]. Each hop adds a little time, and the TLS work adds more. Measure p99 latency before and after, on your own hardware.

**Operational complexity.** The mesh is now on the path of every call. Its upgrades, config pushes and bugs can break every service at once. That is what happened at Monzo [@monzo]. Someone must own it, upgrade it and debug it at 3 AM.

**Confusing failures.** An error may now come from a proxy, not from your app. Engineers must learn to read proxy logs and mesh config.

### Sidecar or sidecar-less

Istio's own team listed the problems with sidecars. You must restart the app pod to add or upgrade a sidecar. Each sidecar is sized for its pod's worst case, so a lot of CPU and memory sits idle. And sidecars can break apps with non-standard HTTP code [@ambient-intro].

**Ambient mode** is Istio's answer. A **ztunnel** proxy runs once per node, not once per pod. It handles mTLS, L4 authorization and telemetry, and it does not read HTTP [@istio-ambient]. For L7 features like HTTP routing and L7 authorization, you add **waypoint proxies**. These are Envoy proxies that run outside your app pods and scale on their own [@istio-ambient]. Ambient mode became generally available in Istio 1.24, in November 2024 [@ambient-ga].

In the same test, ztunnel used about 0.06 vCPU and 12 MB of memory per node. A waypoint used about 0.25 vCPU and 60 MB [@istio-perf]. Ambient and sidecar workloads can share one mesh, so you can move step by step [@istio-ambient].

## Trade-offs

| Option | mTLS between services | Retries and routing | Cost to run | Use it when |
|---|---|---|---|---|
| Shared client library | No. You build it yourself. | Yes, in code. One version per language. | Low. No new system. | Few services, one or two languages. |
| Sidecar mesh | Yes. Automatic, with rotation. [@istio-security] | Yes. Per service, in config. [@istio-traffic] | High. A proxy in every pod, and restarts to upgrade. [@ambient-intro] | Many services and languages, and a team to own the mesh. |
| Sidecar-less mesh (Istio ambient) | Yes, from the per-node ztunnel. [@istio-ambient] | Only where you add waypoint proxies. [@istio-ambient] | Lower per pod. [@istio-perf] Still a system to run. | You mainly need mTLS and metrics, or sidecar cost is the blocker. |
| API gateway only | No. Only at the edge. | Yes, for outside traffic only. | Medium. One layer at the edge. | Your main need is clients outside, not service-to-service calls. |

**Simple rule:** adopt a mesh for a need you can name, like mTLS everywhere or many languages. Do not adopt one because it is popular.

## At work: how a senior uses this

- Ask **"Which problem does the mesh solve that we cannot solve in a library?"** If the answer is only retries, use a library.
- Do not adopt a mesh when you have a few services, one language, no compliance need for mTLS, or nobody to own it.
- Pick one layer for retries. If the mesh retries, turn them off in the app, or the other way round. Write that rule down.
- Set a timeout in the mesh for every route. Make the app timeout a little longer than the mesh timeout × attempts.
- Roll out mTLS in permissive mode first. Watch for plain-text traffic. Then go strict.
- Treat a mesh upgrade like a database upgrade. Canary it on a few workloads, and have a rollback plan.
- During an incident, do not restart every proxy at once. Monzo's restart turned a partial outage into a full one [@monzo].
- Alert on the control plane too: config push errors and stale endpoints, not only request errors.

## With AI

### What to ask the agent

> "Read our Istio VirtualService and DestinationRule files and our HTTP client config. For each route, list the app timeout, app retries, mesh timeout and mesh retries. Show the worst-case attempts per call."

> "We have 30 services in Go and Java. List what we would gain and lose by adopting Istio ambient mode instead of sidecars. Cite the Istio docs for each point."

> "Write a rollout plan to move these 5 namespaces to strict mTLS. Include how to find clients still sending plain text."

### What to check yourself

- Retry counts match the docs for your mesh version. Defaults change between versions.
- Retries apply only to idempotent requests, such as GET, or requests with an idempotency key.
- The agent did not set the mesh timeout longer than the app timeout.
- Resource numbers come from your own load test, not from a blog post.
- Config keys exist in your mesh version. Run the mesh's own config check before you apply anything.

## Check yourself

## Go deeper

- [Load balancing](/learn/load-balancing): the balancing rules each proxy uses.
- [Circuit breakers and bulkheads](/learn/circuit-breakers-and-bulkheads): the limits a mesh can enforce.
- [Backoff, jitter and retry budgets](/learn/backoff-jitter-retry-budgets): how to keep mesh retries from piling up.
- [Distributed tracing](/learn/distributed-tracing): what the mesh can and cannot trace for you.
- Sidecar and container patterns (coming soon): the sidecar pattern beyond meshes.
