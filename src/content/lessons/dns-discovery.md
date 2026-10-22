---
topic: dns-discovery
subtitle: Every request starts with a name. Learn how DNS turns it into an address, why changes take time to spread, and how services find each other inside your system.
status: published
illustrative_story: false
sources:
  - id: aws-2025
    title: "Amazon Web Services, “Summary of the Amazon DynamoDB Service Disruption in the Northern Virginia (US-EAST-1) Region” (October 2025)"
    url: https://aws.amazon.com/message/101925/
    note: The postmortem. A race between two copies of the DNS automation left the DynamoDB endpoint with no IP addresses.
  - id: rfc1034
    title: "P. Mockapetris, RFC 1034, “Domain Names - Concepts and Facilities”, IETF, 1987"
    url: https://www.rfc-editor.org/rfc/rfc1034
    note: The parts of DNS (name space, name servers, resolvers), zones, recursive and iterative queries, the meaning of TTL, and the CNAME rule.
  - id: rfc1035
    title: "P. Mockapetris, RFC 1035, “Domain Names - Implementation and Specification”, IETF, 1987"
    url: https://www.rfc-editor.org/rfc/rfc1035
    note: Section 3.2 lists the record types (A, NS, CNAME, MX, TXT) and defines the TTL field.
  - id: rfc2308
    title: "M. Andrews, RFC 2308, “Negative Caching of DNS Queries (DNS NCACHE)”, IETF, 1998"
    url: https://www.rfc-editor.org/rfc/rfc2308
    note: How resolvers cache “this name does not exist”, and for how long.
  - id: rfc2782
    title: "A. Gulbrandsen, P. Vixie, L. Esibov, RFC 2782, “A DNS RR for specifying the location of services (DNS SRV)”, IETF, 2000"
    url: https://www.rfc-editor.org/rfc/rfc2782
    note: The SRV record format, with priority, weight, port and target.
  - id: iana-root
    title: "IANA, “Root Servers”"
    url: https://www.iana.org/domains/root/servers
    note: The 13 named root server authorities and who runs them.
  - id: r53-how
    title: "Amazon Route 53 Developer Guide: How internet traffic is routed to your website or web application"
    url: https://docs.aws.amazon.com/Route53/latest/DeveloperGuide/welcome-dns-service.html
    note: A lookup step by step, from the browser to the resolver, root, .com and the authoritative name servers.
  - id: r53-ttl
    title: "Amazon Route 53 Developer Guide: Values specific for simple records"
    url: https://docs.aws.amazon.com/Route53/latest/DeveloperGuide/resource-record-sets-values-basic.html
    note: Record types Route 53 supports, and the trade-off between a long and a short TTL.
  - id: r53-failover
    title: "Amazon Route 53 Developer Guide: How health checks work in simple Amazon Route 53 configurations"
    url: https://docs.aws.amazon.com/Route53/latest/DeveloperGuide/dns-failover-simple-configs.html
    note: DNS answers that include only healthy resources, and what happens to records without a health check.
  - id: k8s-service
    title: "Kubernetes documentation: Service"
    url: https://kubernetes.io/docs/concepts/services-networking/service/
    note: Why Services exist. Pods come and go, and a Service gives clients one stable way to reach them.
  - id: k8s-dns
    title: "Kubernetes documentation: DNS for Services and Pods"
    url: https://kubernetes.io/docs/concepts/services-networking/dns-pod-service/
    note: The DNS names and records that Kubernetes creates for normal and headless Services.
  - id: consul-dns
    title: "HashiCorp Consul documentation: Perform static DNS queries"
    url: https://developer.hashicorp.com/consul/docs/discover/service/static
    note: The Consul DNS name format for services. Instances that fail their health check are left out.
  - id: roblox-2021
    title: "Roblox, “Roblox Return to Service 10/28-10/31 2021” (January 2022)"
    url: https://about.roblox.com/newsroom/2022/01/roblox-return-to-service-10-28-10-31-2021/
    note: The postmortem of a 73-hour outage. Services found each other through Consul, and Consul was unhealthy.
reviewed_by: ai-factcheck
reviewed_on: 2026-10-22
claims_checked:
  - claim: "At 11:48 PM PDT on 19 October 2025, a race between a delayed DNS Enactor and another Enactor's clean-up left dynamodb.us-east-1.amazonaws.com with an empty DNS record; cause found 12:38 AM, DNS restored 2:25 AM, clients recovered as caches expired by 2:40 AM; EC2 took many more hours."
    source: aws-2025
    note: Matches the postmortem. It says "unusually high delays", not "very slow", and does not call us-east-1 the biggest region, so both were reworded. Full EC2 recovery was 1:50 PM PDT. Agrees with incident card aws-2025-10-dynamodb-dns.
  - claim: IANA lists 13 named root server authorities, run by 12 operators.
    source: iana-root
    note: The page lists "13 named authorities", A to M. Verisign runs both A and J, so 12 operators.
  - claim: The resolver caches the .com referral to a domain's name servers, typically for two days; a long TTL cuts queries, latency and cost but slows changes; lower to e.g. 300 seconds before a change, raise it after.
    source: r53-ttl
    note: Two-day referral caching is from r53-how step 4. TTL advice matches the simple-records TTL section. "At least one old TTL before" is labelled as our advice.
  - claim: Negative answers are cached for the smaller of the SOA MINIMUM field and the SOA's own TTL; resolvers should cap negative caching, and one to three hours works well.
    source: rfc2308
    note: Sections 3 and 5. The one-to-three-hours figure is the resolver's cap, not the SOA value; the text was corrected, and "fail for an hour" changed.
  - claim: A TTL of zero means use only for the transaction in progress; a CNAME node should have no other data; SRV clients try the lowest-numbered priority first, and bigger weights get a proportionally higher chance.
    source: rfc1035
    note: RFC 1035 section 3.2.1 (TTL zero); CNAME rule from RFC 1034 section 3.6.2; SRV rules from RFC 2782.
  - claim: Route 53 runs health checks periodically, not per query, answers only with healthy records, and treats records without a health check as healthy.
    source: r53-failover
    note: All three stated directly on the page.
  - claim: A normal Service name resolves to its cluster IP; a headless Service name returns the IPs of all selected Pods; queries without a namespace are limited to the Pod's namespace.
    source: k8s-dns
    note: Quoted closely. The docs write the domain as cluster-domain.example; cluster.local is the usual default shown in the search-path example.
  - claim: Consul DNS answers for <service>.service.consul omit instances that fail health checks, and SRV answers include the port.
    source: consul-dns
    note: Both stated on the static DNS queries page. The SRV-port line was re-cited from rfc2782 to consul-dns.
  - claim: Roblox was down 73 hours; with Consul unhealthy, servers struggled to connect; telemetry had a circular dependency on Consul, which Roblox later removed.
    source: roblox-2021
    note: All four points are in the postmortem.
quiz:
  - q: You will move api.example.com to a new IP address next week. Its TTL is 86400 seconds (one day). What should you do first?
    options:
      - Nothing. Change the record on the day. Resolvers check for changes every few seconds.
      - Lower the TTL, for example to 300 seconds, at least a day before the move. Raise it again after the move works.
      - Delete the record for a minute, so that every cache forgets it.
    answer: 1
    why: Resolvers may keep the old answer for the old TTL. Lower it early, so old answers expire fast on the day. Deleting the record can cause cached "does not exist" answers.
  - q: A health check marks one of your two servers as down. Route 53 stops returning its address. Why do some users still reach the dead server for a few minutes?
    options:
      - Route 53 checks health only once a day.
      - Resolvers and clients cached the old answer, and they keep it until its TTL ends.
      - DNS failover only works for MX records.
    answer: 1
    why: A DNS change helps only clients that ask again. Cached answers live until their TTL ends, so failover is never faster than the TTL.
  - q: Your app looked up new-api.example.com before you created the record. You create it now. Why might lookups still fail for a while?
    options:
      - The resolver cached the "name does not exist" answer. This is negative caching.
      - New records only work after 24 hours, by law.
      - CNAME records cannot point to new names.
    answer: 0
    why: Resolvers cache negative answers too. RFC 2308 takes the time limit from the zone's SOA record.
  - q: In Kubernetes, what does the DNS name of a normal (not headless) Service resolve to?
    options:
      - The IP address of every Pod behind the Service.
      - The Service's cluster IP, which stays the same while Pods change.
      - The IP address of the node that runs the DNS server.
    answer: 1
    why: A normal Service gets one stable cluster IP. A headless Service, without a cluster IP, returns the IPs of the Pods instead.
incidents: [aws-2025-10-dynamodb-dns, roblox-2021-10-consul]
related: [tcp-ip, load-balancing, cdn-http-caching, safe-deploys]
---

## What broke

Late on 19 October 2025, AWS services in its Northern Virginia region, us-east-1, started to fail. The cause was one DNS name: `dynamodb.us-east-1.amazonaws.com`, the public address of DynamoDB in that region. [@aws-2025]

DynamoDB keeps hundreds of thousands of DNS records for its load balancers. Automation keeps them up to date. One part, the DNS Planner, writes new plans. Three copies of another part, the DNS Enactor, apply the plans in Route 53, AWS's DNS service. [@aws-2025]

That night, one Enactor hit unusually long delays. Another Enactor applied a newer plan, then started to delete old plans. At that moment, the slow Enactor wrote its much older plan over the new one. It had checked that its plan was newer, but only at the start, and that check was now out of date. The cleanup then deleted the old plan, which was now live. All IP addresses for the endpoint disappeared. [@aws-2025]

From 11:48 PM PDT, every system that used the public endpoint got DNS failures. That included AWS's own services. The servers behind the name were fine. Nobody could find them. [@aws-2025]

Engineers found the cause at 12:38 AM and restored the DNS records at 2:25 AM. Even then, clients came back only as their cached DNS records expired, between 2:25 and 2:40 AM. Other services, like EC2 launches, took many more hours. [@aws-2025]

## The idea in simple words

Computers talk to IP addresses, like `192.0.2.44`. People and code use names, like `www.example.com`. DNS connects the two. [@r53-how]

DNS is not one server. It is a tree of names, spread over many name servers. Each name server is responsible for its own part of the tree, called a zone. Resolvers walk the tree for you and cache what they learn. [@rfc1034]

Inside your own system, services have the same problem. Servers start, stop and move, so their addresses change. Service discovery is how one service finds the current addresses of another.

> [!key] **The key idea.** DNS is a cache spread over the whole internet. Every answer has a TTL, and a change reaches users only after the old answers expire. Plan every DNS change, and every failover, around that delay.

## How it works

### A lookup, step by step

Your app does not walk the tree itself. It asks a recursive resolver, often run by your internet provider or your company network. [@r53-how] The resolver does the work and answers your app. RFC 1034 calls this a recursive query. The resolver itself asks each name server iteratively: each server either answers or points to a server closer to the answer. [@rfc1034]

At the top of the tree are the root name servers. IANA lists 13 named root server authorities, run by 12 operators. [@iana-root] Below them are the servers for top-level domains like `.com`. Below those is the authoritative name server for your zone, which holds your records. [@r53-how]

Step through a lookup from a cold cache.

[[diagram:dns-cold-lookup]]

The second lookup is fast because of caching. In the Route 53 guide's example, the resolver keeps the `.com` referral to a domain's name servers, typically for two days. [@r53-how]

### Record types you will meet

- **A**: an IPv4 address, 32 bits. [@rfc1035]
- **AAAA**: an IPv6 address. [@r53-ttl]
- **CNAME**: an alias. It says "this name is another name for that name". If a name has a CNAME, it should have no other records. [@rfc1034] [@rfc1035]
- **MX**: the mail servers for a domain. [@rfc1035]
- **TXT**: free text. [@rfc1035]
- **NS**: the authoritative name servers for a zone. [@rfc1035]
- **SRV**: where a service runs: a host name and a port, with a priority and a weight. Clients must try the lowest priority number first. Among equal priorities, a bigger weight gets more traffic. [@rfc2782]

### TTL: why changes take time

Every record has a TTL in seconds. It is the time limit for keeping the record in a cache. [@rfc1034] A TTL of zero means "use it for this lookup only, do not cache it". [@rfc1035]

The TTL is a trade-off. The Route 53 guide explains it. A long TTL, such as two days, means fewer queries, lower latency and a smaller bill. But a change, like a new IP address, takes longer to reach users. [@r53-ttl]

So, before you change a record that is in use, lower its TTL first, for example to 300 seconds. Raise it again after you confirm the change works. [@r53-ttl] Our advice: lower it at least one old TTL before the change. Caches that read the record yesterday may still hold the long TTL.

### Negative caching: "does not exist" is cached too

Resolvers also cache negative answers. NXDOMAIN means the name does not exist. NODATA means the name exists, but has no record of the type you asked for. [@rfc2308] The time limit for a negative answer is the smaller of two values in the zone's SOA record. [@rfc2308] RFC 2308 also says resolvers should cap it, and that a cap of one to three hours works well. [@rfc2308]

This causes a classic bug. A deploy script looks up a new name before the record exists. The resolver now remembers "no such name". You create the record, and lookups still fail until that negative answer expires.

### DNS load balancing and failover

A DNS name can return several addresses. Route 53 can attach a health check to each record and answer only with healthy ones. [@r53-failover] Two details matter. Route 53 runs health checks on a schedule, not when a query arrives. And a record without a health check is always treated as healthy. [@r53-failover]

DNS failover has a hard limit: the TTL. A new answer helps only clients that ask again. Everyone else keeps the cached address until its TTL ends. [@rfc1034] AWS saw this in the outage above: clients reconnected only as their cached records expired. [@aws-2025]

DNS also knows nothing about load. It cannot see that one server is busy and another is idle. For that, put a load balancer behind the name. See [load balancing](/learn/load-balancing).

### Service discovery inside your system

Kubernetes explains the problem well. Pods come and go, and each Pod has its own IP address. A client should not need to track which Pods exist right now. [@k8s-service]

There are three common answers.

**Plain DNS.** Put internal names in DNS, with short TTLs. It is simple, and every language can use it. You still get the TTL delay, and DNS alone does not check health.

**A service registry.** Each instance registers itself in a registry like Consul. Health checks keep the list current. In Consul's DNS interface, you ask for `<service>.service.consul`. Instances that fail their health check are left out of the answer. SRV answers also return the port. [@consul-dns]

**Kubernetes Services.** A Service gets a DNS name like `my-svc.my-namespace.svc.cluster.local`. For a normal Service, it resolves to one cluster IP, which does not change when Pods change. [@k8s-dns] Kubernetes updates the Service's list of Pods whenever the set of Pods changes. [@k8s-service] A headless Service has no cluster IP. Its name returns the IP of every Pod, and the client must choose. [@k8s-dns]

A registry is a dependency of everything. In October 2021, Roblox was down for 73 hours. Roblox services used Consul to find each other, and when Consul was unhealthy, servers struggled to connect. Their telemetry also depended on Consul, so they lacked the data to see what was wrong. [@roblox-2021]

## Trade-offs

| Option | Speed of change | Health aware? | Main risk |
|---|---|---|---|
| Public DNS, long TTL | Slow. Old answers can live for the whole TTL. [@r53-ttl] | No. | A bad record stays in caches for a long time. |
| Public DNS, short TTL | Fast. | No, unless you add health checks. | More queries and a higher DNS bill. [@r53-ttl] |
| DNS with health checks (Route 53) | Limited by the TTL. | Yes. [@r53-failover] | Records without a health check always count as healthy. [@r53-failover] |
| Service registry (Consul) | Fast, if clients do not cache for long. | Yes. [@consul-dns] | Every service depends on it. [@roblox-2021] |
| Kubernetes Service (cluster IP) | Fast. The IP stays the same. [@k8s-dns] | Partly. Kubernetes keeps the Pod list current. [@k8s-service] | Only works inside the cluster. |
| Headless Service | Fast. | Partly, like a normal Service. | The client must pick a Pod and handle changes. [@k8s-dns] |

**Simple rule:** put a load balancer or a Kubernetes Service behind a stable name, and let DNS change rarely. Use DNS for "where is the front door", not for "which server is free right now".

## At work: how a senior uses this

- Before a migration, write down the current TTL of every record you will change. Lower each TTL at least one old TTL before the move.
- Never look up a name before you create it. Create the record first, then test it, so no negative answer gets cached.
- Treat DNS automation like code that can break everything. Ask: **"What stops this tool from writing an empty or stale record?"** The AWS outage came from exactly that gap. [@aws-2025]
- Keep the tools you use in an outage independent of the systems they watch. Roblox later removed the circular dependency between its telemetry and Consul. [@roblox-2021]
- Add a health check to every record in a failover group. A record without one counts as healthy. [@r53-failover]
- When a service "cannot connect", check name resolution first: run `dig` against your resolver, and read the TTL in the answer.

## With AI

### What to ask the agent

> "Here is our DNS zone file. List every record with its TTL. For a move of api.example.com to a new load balancer, write a step-by-step plan with the time each step must wait."

> "Search this repo for places where a service address is hard-coded, or where DNS answers are cached forever. Show the file and line, and how to make each one respect the TTL."

### What to check yourself

- Every TTL and wait time in the plan matches the real zone, not a guess. Run `dig` and compare.
- The plan does not depend on deleting and re-creating a record. That can cause negative caching.
- Any claim about caching in your language or HTTP client is backed by its official documentation.
- For Kubernetes, the agent used the right namespace. A short name only finds Services in the Pod's own namespace. [@k8s-dns]

## Check yourself

## Go deeper

Before this: [TCP/IP and the network stack](/learn/tcp-ip-network-stack). Next: [load balancing](/learn/load-balancing), which sits behind most DNS names, and [CDN and HTTP caching](/learn/cdn-and-http-caching), another cache with time limits.
