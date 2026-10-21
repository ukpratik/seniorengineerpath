---
topic: load-balancing
subtitle: A load balancer decides which server gets each request. Learn to pick the rule, check health and remove servers without dropping users.
status: published
illustrative_story: true
sources:
  - id: sre-dc
    title: "Alejandro Forero Cuervo, “Load Balancing in the Datacenter”, Site Reliability Engineering (Google, O'Reilly 2016), chapter 20"
    url: https://sre.google/sre-book/load-balancing-datacenter/
    note: Why round-robin wastes capacity, least-loaded round-robin and its traps, lame duck state and weighted round-robin.
  - id: sre-fe
    title: "Piotr Lewandowski, “Load Balancing at the Frontend”, Site Reliability Engineering (Google, O'Reilly 2016), chapter 19"
    url: https://sre.google/sre-book/load-balancing-frontend/
    note: DNS load balancing and its limits, anycast, virtual IPs and consistent hashing in network load balancers.
  - id: p2c
    title: "Michael Mitzenmacher, “The Power of Two Choices in Randomized Load Balancing”, IEEE Transactions on Parallel and Distributed Systems, 2001"
    url: https://www.eecs.harvard.edu/~michaelm/abstracts/tpds2001.html
    note: The paper behind power of two choices. Two random choices beat one by far; a third choice adds little.
  - id: envoy-lb
    title: "Envoy proxy documentation, “Supported load balancers”"
    url: https://www.envoyproxy.io/docs/envoy/latest/intro/arch_overview/upstream/load_balancing/load_balancers
    note: Round-robin, least request with two random choices (P2C), ring hash and Maglev, in a real proxy.
  - id: aws-nlb
    title: "AWS documentation, “What is a Network Load Balancer?”"
    url: https://docs.aws.amazon.com/elasticloadbalancing/latest/network/introduction.html
    note: A layer 4 load balancer that picks a target per TCP connection with a flow hash.
  - id: aws-alb
    title: "AWS documentation, “What is an Application Load Balancer?”"
    url: https://docs.aws.amazon.com/elasticloadbalancing/latest/application/introduction.html
    note: A layer 7 load balancer that routes by path, host and headers.
  - id: alb-attrs
    title: "AWS documentation, “Edit target group attributes for your Application Load Balancer”"
    url: https://docs.aws.amazon.com/elasticloadbalancing/latest/application/edit-target-group-attributes.html
    note: Deregistration delay (draining), routing algorithms, slow start and sticky sessions, with defaults.
  - id: alb-health
    title: "AWS documentation, “Health checks for Application Load Balancer target groups”"
    url: https://docs.aws.amazon.com/elasticloadbalancing/latest/application/target-group-health-checks.html
    note: Every health check setting with its default, and what happens when all targets fail.
  - id: aws-oct2025
    title: "AWS, “Summary of the Amazon DynamoDB Service Disruption in the Northern Virginia (US-EAST-1) Region” (official, October 2025)"
    url: https://aws.amazon.com/message/101925/
    note: The section on Network Load Balancer shows health checks that flapped and took healthy capacity out of service.
claims_checked:
  - claim: "Round-robin gave up to a 2x spread in CPU use, and the most expensive requests can use 1,000 times more CPU than the cheapest."
    source: sre-dc
    note: "Chapter 20 says \"1000x (or more)\" and \"a spread of up to 2x in CPU consumption\"."
  - claim: "Two random choices are far better than one; a third choice helps only a little."
    source: p2c
    note: "Abstract: d=2 gives exponential improvement in expected time over d=1; d=3 is only a constant factor better than d=2."
  - claim: "ALB health check defaults are 30 s interval, 5 s timeout, 2 failures to mark unhealthy, 5 passes to bring back; if all targets fail, it fails open."
    source: alb-health
    note: "Defaults for instance and ip targets. Lambda targets use 35 s interval and 30 s timeout."
  - claim: "ALB deregistration delay defaults to 300 seconds; a target that closes connections early causes 500-level errors."
    source: alb-attrs
    note: "Deregistration finishes at once if the target has no in-flight requests."
  - claim: "In October 2025, NLB health checks in us-east-1 flapped, took capacity out of service, and engineers turned off automatic health check failover."
    source: aws-oct2025
    note: "Connection errors 5:30 AM to 2:09 PM PDT; failover turned off at 9:36 AM and re-enabled at 2:09 PM."
  - claim: "Envoy's least request balancer picks two random hosts by default and is nearly as good as a full scan."
    source: envoy-lb
    note: "Envoy cites Mitzenmacher for the \"nearly as good as an O(N) full scan\" claim."
reviewed_by: ai-factcheck
reviewed_on: 2026-10-21
quiz:
  - q: One of your six servers becomes very slow, but its health check still passes. Which rule sends it the fewest new requests?
    options:
      - Round-robin.
      - Least connections.
      - Sticky sessions.
    answer: 1
    why: A slow server keeps many requests open, so least connections sends new requests elsewhere. Round-robin still gives it one in six.
  - q: What does power of two choices do?
    options:
      - Sends each request to two servers and keeps the faster answer.
      - Picks two servers at random and sends the request to the less busy one.
      - Sends half the traffic to each of two data centres.
    answer: 1
    why: It checks only two servers, so it is cheap, and it still avoids most busy servers.
  - q: You remove a server from the load balancer during a deploy. What should happen to requests already running on it?
    options:
      - They are cut off at once.
      - The server stops getting new requests and finishes the open ones first.
      - They move to another server in the middle of the request.
    answer: 1
    why: That is connection draining. On an AWS Application Load Balancer the default wait is up to 300 seconds.
  - q: Why do seniors try to avoid sticky sessions?
    options:
      - They make HTTPS slower.
      - If the server dies, its users lose their session, and load can become uneven.
      - Load balancers cannot support them.
    answer: 1
    why: Stickiness ties users to one server. Keep session data in a shared store instead, so any server can serve any user.
  - q: Why can a DNS change take a long time to move all users to a new region?
    options:
      - Resolvers cache answers for the TTL, and some ignore the TTL.
      - DNS works only over TCP.
      - Browsers never cache DNS.
    answer: 0
    why: The authoritative server cannot flush resolver caches, and not all resolvers respect the TTL.
incidents: [aws-2025-10-dynamodb-dns]
related: [tcp-ip, http-versions, dns-discovery, api-gateway, sharding, multi-region]
---

## What broke

This story is made up, but it is common. A food delivery app runs six API servers behind one load balancer. The load balancer uses round-robin: each server gets one request in turn.

On a Friday evening, one server starts to read from a failing disk. Its requests take 3 seconds instead of 80 ms. Its health check is a simple `/health` page, and it still answers "OK" in 2 ms.

The load balancer sees a healthy server. It keeps sending it one request in six. Users who land there wait, give up and tap "Order" again.

The dashboards show normal average latency, because five servers are fine. But p99 latency is over 3 seconds, and support gets angry messages. It takes the team an hour to find the one bad server.

Nothing was down. The load balancer did exactly what it was told. It just did not know that "healthy" and "fast" are not the same thing.

## The idea in simple words

A load balancer is one address in front of many servers. Clients talk to the load balancer. For each new connection or request, it picks a server.

It has three jobs:

1. **Pick a server.** Follow a rule, such as "next in turn" or "least busy".
2. **Skip bad servers.** Use health checks to stop sending traffic to servers that fail.
3. **Change the pool safely.** Add and remove servers without breaking requests that are still running.

> [!key] **The key idea.** The goal is not to spread requests evenly. The goal is that no server is overloaded and no user waits on a failing server. Equal counts are not equal work.

## How it works

### L4 or L7

A layer 4 (L4) load balancer sees TCP connections, not HTTP requests. AWS's Network Load Balancer works this way. It picks a server with a hash of the protocol, addresses, ports and TCP sequence number. Each TCP connection stays on one server for its whole life. [@aws-nlb] It is fast and simple, but it cannot read URLs or headers.

A layer 7 (L7) load balancer reads each HTTP request. AWS's Application Load Balancer can send `/api` to one group of servers and `/images` to another, by path, host or header. [@aws-alb] It does more work per request, but it can balance requests, not just connections. This matters with HTTP/2 or gRPC, where one connection carries many requests.

### Choosing a server

**Round-robin** gives each server the next request in turn. It is the default on the AWS Application Load Balancer. [@alb-attrs] It works well when requests cost about the same. In practice they often do not. Google found that the most expensive requests can use 1,000 times more CPU than the cheapest. Round-robin gave a spread of up to 2x in CPU use between the least and most loaded servers. [@sre-dc]

**Least connections** sends the request to the server with the fewest requests in progress. AWS calls this "least outstanding requests". It suggests it when requests vary in cost or servers vary in power. [@alb-attrs]

Step through what happens to one slow server under each rule. Use Next and Back, or the ← and → keys.

[[diagram:lb-slow-backend]]

Least connections has one trap. A broken server that returns errors very fast has few open requests. So it looks "least busy" and gets even more traffic. Google calls this a sinkhole. Its fix was to count recent errors as if they were open requests. [@sre-dc]

**Power of two choices (P2C)** picks two servers at random and sends the request to the less busy one. Mitzenmacher showed that two random choices are far better than one, while a third choice helps only a little. [@p2c] Envoy uses this for its least request balancer. Its docs say it is nearly as good as checking every server. [@envoy-lb] It is cheap, because it looks at only two servers per request.

**Consistent hashing** sends the same key, such as a user ID, to the same server. A simple `hash mod N` breaks when N changes: almost every key moves to a new server. With consistent hashing, adding or removing a server moves only a small share of keys. [@sre-fe] Envoy offers it as "ring hash" and "Maglev". [@envoy-lb] Use it when a server keeps a local cache per key.

### Health checks

The load balancer tests each server on a schedule. On an AWS Application Load Balancer, the defaults are a check every 30 seconds, with a 5 second timeout. Two failures in a row mark a server unhealthy. Five passes in a row bring it back. [@alb-health]

With those defaults, a dead server fails two checks 30 seconds apart. So it can get traffic for about a minute before it is removed. Our advice: shorten the interval for important services.

What should the check test? A shallow check ("the process is up") misses the slow disk in our story. A deep check that calls the database can mark every server unhealthy at once when the database has a short problem. Our advice is a middle path: check the server's own parts, and watch latency and errors with alerts.

Health checks can also hurt you. If every target fails, an Application Load Balancer "fails open" and sends traffic to all of them anyway. [@alb-health] In October 2025, health checks for AWS's Network Load Balancer in us-east-1 kept flipping between failing and healthy. Healthy capacity was removed, and customers saw connection errors. Engineers turned off automatic health check failover to stop it. [@aws-oct2025]

### Connection draining

You remove servers all the time: deploys, scale-in, repairs. If you cut a server off at once, its open requests fail.

Connection draining fixes this. The load balancer stops sending new requests to the server, but lets open ones finish. On an AWS Application Load Balancer, this is the "deregistration delay", and the default is 300 seconds. If the server closes connections before the delay ends, clients get 500-level errors. [@alb-attrs] Google's version is a "lame duck" state: the server still serves, but asks clients to stop sending new requests. [@sre-dc]

New servers can need the opposite: a slow start. AWS can raise traffic to a new target step by step, so it can warm up first. [@alb-attrs]

### Sticky sessions, and why to avoid them

Sticky sessions send all requests from one user to the same server, often with a cookie. On AWS, the load balancer's own cookie is named `AWSALB`, and it skips the normal algorithm after the first request. [@alb-attrs]

This is useful when a server keeps session data in memory. But it has costs. If that server dies, those users lose their session. Load becomes uneven: AWS warns that after a big scale-up, stickiness can spread load unequally. [@alb-attrs] Deploys get slower, because you must drain long sessions.

The better fix is to make servers stateless. Keep sessions in a shared store, such as Redis or a database, so any server can serve any user.

### Global load balancing

To spread users across regions, you balance before traffic reaches any one data centre.

**DNS.** The DNS server returns a different IP address for each region. It is simple, but slow to change. Resolvers cache answers for the TTL, and not all resolvers respect it. The DNS server also sees the resolver's address, not the user's, so it can guess the user's location wrong. [@sre-fe]

**Anycast.** Many sites announce the same IP address. The network sends each user to the nearest one. Google's book describes giving DNS servers an anycast address, so DNS queries flow to the closest one. [@sre-fe]

## Trade-offs

| Rule | Handles a slow server? | Cost | Use it when |
|---|---|---|---|
| Round-robin | No. Every server gets the same share. | Almost none. | Requests cost about the same and servers are the same size. |
| Least connections | Yes. Busy servers get less. | Must track open requests. Watch for fast-failing sinkholes. | Request cost varies a lot. |
| Power of two choices | Yes, mostly. | Checks only two servers per request. | Many load balancers or proxies, each with its own view. |
| Consistent hashing | No. The key decides, not the load. | Hot keys can overload one server. | Each server caches data per key. |
| Sticky sessions | No. Users stay on the same server even when it is busy. | Lost sessions when a server dies. Uneven load. | Old apps that keep state in memory, while you move it out. |

**Simple rule:** start with least connections or P2C for HTTP services. Use hashing only when local data needs it.

## At work: how a senior uses this

- Ask: **"What does our health check really test?"** A check that passes while users wait is not a health check.
- Watch p99 latency and errors per server, not just the average. One bad server hides inside a good average.
- Set the draining time longer than your slowest normal request. Make sure the app stops cleanly on SIGTERM, the signal that asks a process to shut down.
- Know what happens when every server fails its check. Does the load balancer fail open, or send nothing?
- Remove sticky sessions when you touch old code. Move session data to a shared store.
- For long-lived connections, like gRPC or WebSockets, check that load is spread by request, not just by connection.
- Test it: kill one server during a load test, and slow one down. Watch what the users see.

## With AI

### What to ask the agent

> "Read this load balancer config. List the algorithm, health check path, interval, thresholds and draining time. Point out anything that could keep a slow server in the pool."

> "Change our /health endpoint so it fails when the local disk or thread pool is unhealthy, but not when a shared database is slow. Explain each check."

> "Write a shutdown handler for this service: on SIGTERM, fail the health check, stop taking new requests, finish open ones within 30 seconds, then exit."

### What to check yourself

- The health check is cheap and fast, and cannot overload the server itself.
- A shared dependency failure does not mark every server unhealthy at once.
- The draining delay in the load balancer matches the shutdown timeout in the app.
- Any change to sticky sessions or hashing was tested with servers added and removed.
- The agent did not invent config keys. Check them against the docs for your load balancer.

## Check yourself

## Go deeper

Related lessons: [DNS and service discovery](/learn/dns-and-service-discovery), [API gateways](/learn/api-gateway-and-edge), [Sharding and consistent hashing](/learn/sharding-and-consistent-hashing) and [Multi-region](/learn/multi-region-and-data-locality).
