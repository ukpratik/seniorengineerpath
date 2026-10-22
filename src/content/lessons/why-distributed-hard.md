---
topic: why-distributed-hard
subtitle: Once your system runs on two or more machines, parts fail on their own, messages get lost and clocks disagree. Learn what changes and where each problem is solved.
status: published
reviewed_by: ai-factcheck
reviewed_on: 2026-10-22
illustrative_story: false
sources:
  - id: slack-2021
    title: "Laura Nolan, “Slack's Outage on January 4th 2021”, Slack Engineering (February 2021)"
    url: https://slack.engineering/slacks-outage-on-january-4th-2021/
    note: The official postmortem. A cloud network link dropped packets, monitoring failed, and autoscaling read the signs wrong.
  - id: kleppmann-notes
    title: "Martin Kleppmann, “Distributed Systems” lecture notes, University of Cambridge, 2021/22, sections 1–3"
    url: https://www.cl.cam.ac.uk/teaching/2122/ConcDisSys/dist-sys-notes.pdf
    note: Free notes. Partial failure, RPC with no response, the two generals problem, system models, pauses and quartz clock drift.
  - id: aws-challenges
    title: "Jacob Gabrielson, “Challenges with distributed systems”, Amazon Builders' Library (2019)"
    url: https://builder.aws.com/content/3F08f7GPFiZMCgXD8gny6OjxR0Z/challenges-with-distributed-systems
    note: The eight steps of one request and reply, why a timeout means the result is unknown, and why distributed bugs hide.
  - id: fallacies
    title: "Arnon Rotem-Gal-Oz, “Fallacies of Distributed Computing Explained” (white paper, based on his Dr. Dobb's articles)"
    url: https://arnon.me/wp-content/uploads/Files/fallacies.pdf
    note: The eight fallacies, who first wrote them down, and what each one means for your design.
claims_checked:
  - claim: "On 4 January 2021, cold client caches and a traffic surge meant Slack's AWS Transit Gateways did not scale fast enough; part of the network \"had indeed become saturated and was dropping packets\"."
    source: slack-2021
    note: Matches the official postmortem (Laura Nolan, 1 Feb 2021). Quote is exact.
  - claim: At 6:57 AM PST about 99% of messages were sent, against a normal rate above 99.999%; dashboards and alerts failed because they ran in a different VPC from their databases.
    source: slack-2021
    note: Both numbers and the VPC cause are stated in the postmortem.
  - claim: Waiting threads lowered CPU use, which first triggered some downscaling; Slack then tried to add 1,200 servers between 7:01 and 7:15 AM PST and hit limits; panic mode spread traffic across all instances; the network was normal by 10:40 AM PST.
    source: slack-2021
    note: All confirmed in the postmortem. Earlier draft said "nothing was fully down", but the post says Slack became unavailable, so that line was changed.
  - claim: A crashed node while others keep working is a partial failure; faults happen "at any moment, without warning"; a retried RPC can charge a credit card twice.
    source: kleppmann-notes
    note: Sections 1.1 and 1.3 of the 2021/22 notes.
  - claim: TCP gives up after a timeout "typically on the order of one minute"; packets inside one data center have been delayed over a minute; stop-the-world GC pauses can last several minutes; quartz drift varies with temperature; NTP accuracy is limited by network latency.
    source: kleppmann-notes
    note: Sections 2.3, 3.1 and 3.2. Timeout-based failure detectors "cannot tell the difference" between a crashed node, a slow node and a lost or delayed message. "Only tool" was softened to "usual way".
  - claim: One request and reply has eight steps; client, server and network fail independently; on a timeout the result is UNKNOWN and "may or may not have happened"; distributed bugs often show up long after deployment.
    source: aws-challenges
    note: All in Gabrielson's article (2019), close to word for word.
  - claim: According to Rotem-Gal-Oz, Peter Deutsch (a Sun fellow) drafted seven assumptions in 1994 and James Gosling added an eighth in 1997; the list of eight; for an unreliable network, retry, acknowledge important messages and ignore duplicates or use idempotent messages.
    source: fallacies
    note: Read from the PDF text. Other accounts credit the list differently, so the lesson attributes the history to Rotem-Gal-Oz.
quiz:
  - q: Your service calls another service and gets no reply within the timeout. What do you know?
    options:
      - The other service is down.
      - The request was lost on the network.
      - Nothing for sure. The request may or may not have been done.
    answer: 2
    why: The request could be lost, the server could be dead or slow, or the reply could be lost. From the caller's side these look the same.
  - q: What is a partial failure?
    options:
      - Some parts of the system fail while other parts keep working.
      - A server that returns only half of a response.
      - A failure that fixes itself after a restart.
    answer: 0
    why: On one computer, a fault usually stops everything. In a distributed system, one node or link can fail while the rest keep running.
  - q: Why is it risky to retry a "charge card" call that timed out?
    options:
      - Retries are always slower than the first call.
      - The first call may have worked, so a retry can charge the card twice.
      - The network does not allow the same message twice.
    answer: 1
    why: A timeout means the result is unknown. Make the call safe to repeat, for example with an idempotency key.
  - q: "In Slack's January 2021 outage, why did autoscaling first remove servers?"
    options:
      - An engineer turned autoscaling off by mistake.
      - Threads were waiting on the slow network, so CPU use dropped and looked like low load.
      - The cloud provider removed the servers to save money.
    answer: 1
    why: The postmortem says waiting threads lowered CPU use, which first started some automatic downscaling.
  - q: Which of these is one of the eight fallacies of distributed computing?
    options:
      - The network is reliable.
      - Servers are never slow.
      - Databases never lose data.
    answer: 0
    why: '"The network is reliable" is the first fallacy. The others include "latency is zero" and "bandwidth is infinite".'
incidents: [slack-2021-01-network]
related: [tcp-ip, idempotency-retries, clocks-ordering, failure-detection, cap-consistency, replication, raft, resilience-patterns]
---

## What broke

January 4th, 2021 was the first working day of the year for many people. Slack's traffic jumped. Client caches were cold, so apps pulled down more data than usual on their first connection. [@slack-2021]

Slack's networks on AWS were linked by Transit Gateways. Under the new load, these gateways did not scale fast enough. Part of the network "had indeed become saturated and was dropping packets". [@slack-2021]

The problem was in the network between the servers. Many calls between them were slow or lost. At 6:57 AM PST, about 99% of messages were sent. On a normal day the rate is above 99.999%. [@slack-2021]

Then the tools for seeing the problem broke too. The dashboards and alerts ran in a different network from their databases. So they also needed the broken gateways, and they failed. [@slack-2021]

The machines also read the signs wrong. Threads spent more time waiting on the network, so CPU use dropped. Autoscaling saw low CPU and removed servers. Later, as waiting threads piled up, the team tried to add 1,200 servers between 7:01 and 7:15 AM. That hit other limits. Health checks also failed at a very high rate. The load balancers have a "panic mode" for this: they spread requests across all servers anyway. That, with retries and circuit breakers, got traffic flowing again. [@slack-2021]

No single machine broke. Some parts worked, some failed, and the automatic tools read the signs wrong. AWS raised the gateway capacity by hand, and the network was normal again by 10:40 AM PST. [@slack-2021] This is what distributed systems feel like.

## The idea in simple words

A system is distributed when it runs on more than one machine and the machines talk over a network. Your app server and its database on another host already count. So does a phone app that calls your API.

On one computer, if a part fails, you usually expect the whole computer to stop. In a distributed system, one node can crash while the others keep working. Kleppmann calls this a partial failure. [@kleppmann-notes] Faults in the network or a node can happen at any moment, with no warning. [@kleppmann-notes]

Partial failure comes with other hard problems:

- The network can lose, delay, duplicate or reorder messages.
- A caller cannot tell a slow server from a dead one.
- There is no shared clock that all machines agree on.

> [!key] **The key idea.** In a distributed system, "I got no answer" does not mean "it did not happen". Design every call so that the caller can live with not knowing.

## How it works

### One call, many places to fail

Gabrielson, from AWS, splits one request and reply into eight steps. The client sends the request, the network delivers it, the server checks it and updates its state. Then the same happens in reverse for the reply. [@aws-challenges] Any of the eight steps can fail. The client, the server and the network fail independently of each other. [@aws-challenges]

Step through what the caller sees when no reply comes. Use Next and Back, or the ← and → keys.

[[diagram:no-reply-four-causes]]

When the client times out, the result is UNKNOWN: the work "may or may not have happened". [@aws-challenges] Kleppmann gives the classic example. If a client resends a request after no reply, the server may do it twice, such as charging a credit card twice. [@kleppmann-notes]

### The network is unreliable

Messages can be lost, delayed, duplicated and reordered. [@kleppmann-notes] TCP retries lost packets and removes duplicates for you. But TCP usually gives up after a timeout, typically about a minute. If a network split lasts longer, your code needs its own retries and duplicate checks. [@kleppmann-notes]

Delays can be huge. Kleppmann notes documented cases where packets inside one data center were delayed for more than a minute. [@kleppmann-notes]

### You cannot tell slow from dead

The usual way to spot a dead server is a timeout: send a message and wait for a reply. But silence can also mean that the message was lost or delayed. [@kleppmann-notes] The server may also be paused. Stop-the-world garbage collection in languages such as Java can pause all threads, and on large heaps the pause can last several minutes. [@kleppmann-notes]

So a timeout is a guess. Too short, and you mark healthy servers as dead. Too long, and users wait on a server that is gone. In Slack's outage, low CPU looked like low load, but it really meant threads were waiting on the network. [@slack-2021]

### There is no shared clock

Each machine has its own clock, usually a quartz crystal. Some run a bit fast and some a bit slow, and their speed changes with temperature. [@kleppmann-notes] Machines sync their clocks over the network, but the network adds its own error. [@kleppmann-notes] So two machines can disagree on "now", and timestamps from different machines may not show the true order of events.

### The eight fallacies

These problems are old. According to Rotem-Gal-Oz, Peter Deutsch, then a Sun fellow, wrote down seven false assumptions in 1994. James Gosling added an eighth in 1997. [@fallacies] Designers often assume:

1. The network is reliable.
2. Latency is zero.
3. Bandwidth is infinite.
4. The network is secure.
5. Topology doesn't change.
6. There is one administrator.
7. Transport cost is zero.
8. The network is homogeneous.

Each one feels true on your laptop. Each one is false in production. For the first one, Rotem-Gal-Oz says you must plan for lost calls: retry, acknowledge important messages, and ignore duplicates or make messages idempotent. [@fallacies]

### Where each problem is solved

This lesson is a map. Each problem has its own lesson:

- **Lost and duplicated messages:** [Idempotency, retries and timeouts](/learn/idempotency-retries-timeouts), [Backoff, jitter and retry budgets](/learn/backoff-jitter-retry-budgets) and [Queues vs logs](/learn/queues-vs-logs-delivery-guarantees).
- **Slow or dead:** [Failure detection and membership](/learn/failure-detection-and-membership) and [Tail latency and queueing](/learn/tail-latency-and-queueing).
- **No shared clock:** [Time, clocks and ordering](/learn/time-clocks-and-ordering).
- **Copies of data that disagree:** [Replication](/learn/database-replication) and [CAP, PACELC and consistency models](/learn/cap-pacelc-consistency-models).
- **Agreeing on one leader or value:** [Consensus and Raft](/learn/consensus-and-raft).
- **One failure spreading to others:** [Circuit breakers and bulkheads](/learn/circuit-breakers-and-bulkheads).

## Trade-offs

When a call gets no reply, you must choose what to do. Each choice has a cost.

| Choice | Safe if the first call worked? | User waits? | Use it when |
|---|---|---|---|
| Short timeout, then retry | No, unless the call is idempotent. | No. Fast failover. | Reads, or writes with an idempotency key. |
| Long timeout | Yes. You do not resend. | Yes. A dead server holds users for the full time. | Slow but rare work, such as a big report. |
| Fail and show an error | Yes, but the work may still have happened. | No. | The user can safely try again later. |
| Check the status later | Yes. You ask "did order 42 go through?" | Some. Needs a status API. | Payments and other money moves. |
| Hand it to a queue | Yes, if consumers drop duplicates. | No. The work is done in the background. | Work that does not need an answer now. |

**Simple rule (our advice):** every remote call needs a timeout, and every retry needs a way to drop duplicates.

## At work: how a senior uses this

- In design reviews, point at each arrow between two boxes and ask: **"What happens if this call is lost, slow or done twice?"**
- Check that every network call has a timeout you chose on purpose. Do not trust the client library's default; look it up.
- Treat a timeout as "unknown", not "failed". Log it as unknown, and reconcile later if money or stock is involved.
- Keep monitoring away from the thing it monitors. Slack's dashboards failed because they needed the same broken network. [@slack-2021]
- Be careful with automatic actions based on one signal, such as CPU. A stuck system can look idle.
- Test failure, not just success. Gabrielson writes that engineers must assume each step of a call could fail. [@aws-challenges] He also warns that distributed bugs often show up long after they are deployed. [@aws-challenges]
- When you add a new service, list which of the eight fallacies it depends on.

## With AI

### What to ask the agent

> "List every network call in this code path. For each one, show the timeout, the retry rule, and whether the call is safe to repeat. Mark any call with no timeout."

> "This handler calls the payment service and then saves the order. Walk through what happens if the payment call times out but the payment really worked."

> "Write a test that makes the stock service reply after 5 seconds, and one where the reply is lost after the work is done. Show what our code does in each case."

### What to check yourself

- The agent did not treat a timeout as "the call failed". Look for code that undoes work it is not sure about.
- Retries reuse the same request ID or idempotency key. They do not make a new one each time.
- Timeouts are set in the real client config, not only in comments or docs.
- No logic compares timestamps from two different machines to decide which event came first.
- Tests cover a lost reply, not just a lost request. A lost reply is the case where the work was really done.

## Check yourself

## Go deeper

Start with the lessons in the map above. For the network basics underneath, see [TCP/IP and the network stack](/learn/tcp-ip-network-stack). For the full Slack story, read the postmortem, and see [the incident page](/incidents#slack-2021-01-network).
