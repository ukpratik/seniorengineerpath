---
topic: leader-election
subtitle: Many systems need exactly one node in charge. Learn how leases pick that node, why an old leader can still act, and how fencing tokens stop it.
status: published
illustrative_story: false
sources:
  - id: cloudflare-2020
    title: "Tom Lianza and Chris Snook, “A Byzantine failure in the real world”, The Cloudflare Blog (November 2020)"
    url: https://blog.cloudflare.com/a-byzantine-failure-in-the-real-world/
    note: The official write-up. A partly failed switch stalled etcd elections, and two database clusters promoted a new primary.
  - id: kleppmann
    title: "Martin Kleppmann, “How to do distributed locking” (February 2016)"
    url: https://martin.kleppmann.com/2016/02/08/how-to-do-distributed-locking.html
    note: Why a lease can expire while its holder is paused, and how a fencing token makes the storage reject the late write.
  - id: chubby
    title: "Mike Burrows, “The Chubby lock service for loosely-coupled distributed systems”, OSDI 2006"
    url: https://static.googleusercontent.com/media/research.google.com/en//archive/chubby-osdi06.pdf
    note: Google's lock service for electing primaries. Section 2.4 covers sequencers and lock-delay.
  - id: client-go
    title: "leaderelection package - k8s.io/client-go/tools/leaderelection - Go Packages"
    url: https://pkg.go.dev/k8s.io/client-go/tools/leaderelection
    note: The Kubernetes leader election library. Its overview is honest about fencing and clock skew rate.
  - id: kcm-flags
    title: "Kubernetes documentation, “kube-controller-manager”"
    url: https://kubernetes.io/docs/reference/command-line-tools-reference/kube-controller-manager/
    note: The leader election flags, their defaults and what each one means.
  - id: k8s-leases
    title: "Kubernetes documentation, “Leases”"
    url: https://kubernetes.io/docs/concepts/architecture/leases/
    note: How Kubernetes uses Lease objects for node heartbeats and to keep one active copy of control plane parts.
  - id: etcd-concurrency
    title: "concurrency package - go.etcd.io/etcd/client/v3/concurrency - Go Packages"
    url: https://pkg.go.dev/go.etcd.io/etcd/client/v3/concurrency
    note: Sessions, elections and mutexes on top of etcd leases.
  - id: etcd-data-model
    title: "etcd documentation (v3.5), “Data model”"
    url: https://etcd.io/docs/v3.5/learning/data_model/
    note: Every change creates a new revision, and revisions only grow.
  - id: zk-recipes
    title: "Apache ZooKeeper documentation, “ZooKeeper Recipes and Solutions”"
    url: https://zookeeper.apache.org/doc/current/recipes.html
    note: The leader election recipe with sequential ephemeral nodes, and how it avoids the herd effect.
  - id: consul-election
    title: "HashiCorp Consul documentation, “Application leader election”"
    url: https://developer.hashicorp.com/consul/docs/automate/application-leader-election
    note: Leader election for your own app with Consul sessions and a key-value lock.
  - id: dynamodb-lock
    title: "Alexander Patrikalakis and Sasha Slutsker, “Building Distributed Locks with the DynamoDB Lock Client”, AWS Database Blog (August 2017)"
    url: https://aws.amazon.com/blogs/database/building-distributed-locks-with-the-dynamodb-lock-client/
    note: A lease built on conditional writes, with heartbeats that change a record version number.
claims_checked:
  - claim: "On 2 November 2020 a partly failed switch cut etcd node 1 off from leader node 3. Tied elections made etcd read-only, two database clusters promoted a synchronous replica, and a defect forced a rebuild of all replicas. The incident lasted 6 hours 33 minutes; API success fell to 75% and the dashboard was up to 80 times slower."
    source: cloudflare-2020
    note: "Timeline sections 14:43, 14:44 and 14:45 UTC and the opening paragraph."
  - claim: "kube-controller-manager defaults are lease duration 15s, renew deadline 10s and retry period 2s; the renew deadline must be less than the lease duration."
    source: kcm-flags
    note: "--leader-elect-lease-duration, --leader-elect-renew-deadline, --leader-elect-retry-period."
  - claim: "client-go does not guarantee fencing, acts only on locally captured timestamps, and tolerates any clock skew but not any skew rate (60 s / 30 s for a 2x rate)."
    source: client-go
    note: "Package overview."
  - claim: "A Chubby sequencer holds the lock name, mode and lock generation number, and servers reject invalid ones; lock-delay, up to one minute, is an imperfect fallback."
    source: chubby
    note: "Section 2.4; the acquisition-count if-statement is in section 2.1; clock-rate bound in section 2.8."
  - claim: "Election.Rev returns the leader key's creation revision, and etcd revisions increase monotonically over the life of a cluster."
    source: etcd-data-model
    note: "Also etcd-concurrency (Election.Rev doc)."
  - claim: "A client paused past lease expiry later writes with token 33 after another got 34, and the storage rejects it; stop-the-world GC pauses have lasted several minutes."
    source: kleppmann
    note: "Sections on GC pauses and fencing tokens."
reviewed_by: ai-factcheck
reviewed_on: 2026-11-13
quiz:
  - q: "A Kubernetes controller uses the default settings: lease duration 15 s, renew deadline 10 s. Why must the renew deadline be shorter?"
    options:
      - So the old leader stops leading before another candidate is allowed to take over.
      - So the leader renews more often and uses less network.
      - So two leaders can share the work for 5 seconds during a handover.
    answer: 0
    why: The leader gives up after 10 seconds without a renewal. The others wait 15 seconds. The gap is the safety margin.
  - q: "The leader holds a valid lease, then has a 40-second garbage collection pause. The lease lasts 15 seconds. What is true when it wakes up?"
    options:
      - The lease service pauses the other nodes too, so nothing has changed.
      - Another node may now be leader, but the old one still believes it leads, until it checks.
      - The old leader's process is restarted by the lease service.
    answer: 1
    why: A paused process cannot notice that time passed. Unless something rejects it, it carries on as leader.
  - q: "Which check actually stops a late write from an old leader?"
    options:
      - The old leader asks the lease service "am I still leader?" just before it writes.
      - The old leader sets a shorter lease duration.
      - The storage rejects any write whose fencing token is smaller than the biggest one it has seen.
    answer: 2
    why: A pause can fall between the leader's own check and its write. Only a check at the storage closes that gap.
  - q: "Your app needs a fencing token from an etcd election. What can you use?"
    options:
      - The leader's host name.
      - The creation revision of the leader key, from Election.Rev.
      - The lease time to live in seconds.
    answer: 1
    why: etcd revisions only grow over the life of a cluster, so a later leader's key has a bigger revision.
incidents: [cloudflare-2020-11-etcd-failover]
glossary_optout: [ambiguity, leader, follower, process, partition, deadline]
related: [failure-detection, raft, concurrency-primitives, redis, replication, clocks-ordering, coordination-services]
---

## What broke

On 2 November 2020, a network switch in one Cloudflare data centre partly failed. It stayed up enough that other systems did not route around it. But it dropped some of the packets it should have passed on. [@cloudflare-2020]

Cloudflare's control plane used etcd, a store built on Raft, to coordinate its database clusters. One etcd node sat behind the bad switch. It could reach one peer, but not the etcd leader. [@cloudflare-2020]

That node kept starting elections and voting for itself. The other follower kept voting for the existing leader. The votes tied again and again. Cloudflare writes that Raft leader elections block all writes until they finish, so etcd became read-only. [@cloudflare-2020]

Now two database clusters could not record in etcd that they had a healthy primary. So the cluster manager promoted a synchronous replica to be the new primary. [@cloudflare-2020]

The promotion lost no data. But a bug in the manager forced a rebuild of every replica. One cluster handled authentication for the API and the dashboard. Its new primary had to carry all the reads alone, and it was overloaded. [@cloudflare-2020]

The switch recovered by itself after about six minutes. The incident lasted 6 hours and 33 minutes. API success fell as low as 75%, and the dashboard was up to 80 times slower. Cloudflare's conclusion: "we may not want to invoke the promotion process so quickly." [@cloudflare-2020]

## The idea in simple words

Many systems want exactly one node to do a job. A database primary is the only node that takes writes. A scheduler must start each cron job once. In a highly available Kubernetes setup, only one kube-controller-manager and one kube-scheduler are active. The other copies stand by. [@k8s-leases]

Picking that one node is leader election. Keeping it safe takes two more tools.

A **lease** is leadership with an end time. The leader must renew it often. If it stops renewing, for example because it crashed, the lease runs out and another node takes over. Nobody has to prove the old leader is dead. They only have to wait.

But you learned in [failure detection](/learn/failure-detection-and-membership) that silence is ambiguous. The old leader may be paused, not dead. When it wakes up, it still believes it is the leader. Now two leaders act at once. This is split-brain.

So the last check must sit where the damage happens: in the storage. Every new leader gets a bigger number, called a fencing token. The storage rejects a write that carries a smaller number than one it has already seen. [@kleppmann] [@chubby] Raft calls a similar number a term.

> [!key] **The key idea.** A lease tells the old leader when to stop. A fencing token protects your data when it does not stop in time.

## How it works

### Getting a lease

Do not write this yourself on a plain database row and your own timer. Use a coordination service that already runs consensus, or a tested lock library. How these services work inside is the topic of the later lesson on ZooKeeper, etcd and coordination. Here is what you use from the outside.

- **etcd.** A session holds a lease with a time to live. The default is 60 seconds. `Campaign` blocks until you become the leader. `Session.Done` closes when the lease ends, so you know to stop. [@etcd-concurrency]
- **ZooKeeper.** Each candidate creates an ephemeral node with the sequential flag under one path. The one with the smallest number leads. Each other candidate watches only the node just below its own. So one failure wakes one client, not all of them. [@zk-recipes]
- **Consul.** You create a session, then try to acquire a key with it. The answer is `true` if you are now the leader. [@consul-election] Consul warns that this lock "has no enforcement mechanism". Any client can still write the key. [@consul-election]
- **Kubernetes.** Control plane parts hold a Lease object to stay the single active copy. [@k8s-leases] The client-go `leaderelection` package gives your own controllers the same thing. [@client-go]
- **DynamoDB.** The DynamoDB lock client stores each lock as an item. The owner sends heartbeats that change a record version number. A waiting client takes over only if that number did not change for a full lease period. [@dynamodb-lock] The record version number is a random UUID, so it is not a fencing token.

Notice one thing in the DynamoDB design. The waiting client does not trust a timestamp from another machine. It times the wait on its own clock. client-go does the same: it acts only on timestamps it captured itself. [@client-go]

### The timing rules

kube-controller-manager uses three timing settings. [@kcm-flags]

| Setting | Default | Meaning |
|---|---|---|
| Lease duration | 15 s | How long other candidates wait after the last renewal they saw. |
| Renew deadline | 10 s | How long the leader keeps trying to renew before it stops leading. |
| Retry period | 2 s | The wait between tries to get or renew the lease. |

The renew deadline must be less than the lease duration. [@kcm-flags] That gap is your safety margin. The leader gives up after 10 seconds without a renewal. The others wait 15 seconds before they take over. So a running leader stops about 5 seconds before anyone else starts.

This works only if clocks run at about the same speed. client-go says it tolerates any clock skew, but not any skew rate. [@client-go] Its example: to survive a node whose clock runs twice as fast, set the lease to 60 seconds and the renew deadline to 30. [@client-go] Each node measures how long it waited. It never compares its clock reading with another machine's.

The lease duration also sets your failover time. Kubernetes calls it "effectively the maximum duration that a leader can be stopped before it is replaced". [@kcm-flags]

### Why the old leader thinks it is still leader

The margin protects a leader that is running. It does nothing for a leader that is not running at all.

Kleppmann describes a client that pauses while it holds a lock. Its lease expires, another client gets the lock, and then the first one wakes up and writes. [@kleppmann] Stop-the-world GC pauses "have sometimes been known to last for several minutes". [@kleppmann] A clock jump can do it too. If the lock service's clock jumps forward, it ends the lease while the holder still thinks it has time. [@kleppmann] Leases need clocks that run at about the same rate and do not jump. [@chubby] [@client-go] A network partition looks the same from outside: the old leader is healthy, but its renewals never arrive.

In each case, the old leader's belief is out of date. It checked "am I still the leader?" and then acted. A pause, a clock jump or a lost message came between the check and the act. No check inside the leader can close that gap. Step through it.

[[diagram:lease-fencing-token]]

### Fencing: the storage enforces the term

Google's Chubby lock service, described in a 2006 paper, handles this with a **sequencer**. It holds the lock name, the lock mode and a lock generation number. The leader sends it with each request. The server checks it and rejects the request if it is no longer valid. [@chubby] The paper's simplest version is even smaller: pass one extra integer, the lock acquisition count, with each write. The file server adds one if-statement that rejects a count lower than the current one. [@chubby]

Kleppmann calls this number a fencing token. With ZooKeeper, you can use the zxid or the znode version. [@kleppmann] With etcd, `Election.Rev` gives the creation revision of the leader key. [@etcd-concurrency] etcd revisions only grow over the life of a cluster, so a newer leader gets a bigger one. [@etcd-data-model]

In your own database, the check is a conditional write:

```sql
-- Leader with token 34 writes. A paused leader 33 can no longer win.
UPDATE job_state
SET    last_run = now(), token = 34
WHERE  job_id = 'nightly-report' AND token <= 34;
-- 0 rows changed? A newer leader exists. Stop at once.
```

This is the same idea as a Raft term in [Consensus and Raft](/learn/consensus-and-raft). [Locks, atomics and deadlocks](/learn/locks-atomics-and-deadlocks) covers the same fix for a single lock, and [Redis in depth](/learn/redis-in-depth) covers the Redlock debate.

Libraries say plainly that they do not do this for you. client-go "does not guarantee that only one client is acting as a leader (a.k.a. fencing)". [@client-go] If a system cannot check tokens, Chubby offers a weaker fallback, the lock-delay. When a holder fails, nobody may take its lock for a while, at most one minute. Chubby calls it "imperfect". [@chubby]

### Failover timing

A short lease means fast failover. It also means more false failovers, because a GC pause or a slow network looks like a crash. A long lease means fewer mistakes, but a real crash leaves you without a leader for longer. client-go puts it this way: tolerance to clock skew rate "varies inversely to master availability". [@client-go]

Also ask what the lease is really measuring. At Cloudflare, the databases were fine. The coordination service was sick, and the promotion logic read that as a dead primary. [@cloudflare-2020] GitHub saw failover go wrong twice too: [in 2012](/incidents#github-2012-09-mysql-failover) a busy primary was taken for a dead one, and [in 2018](/incidents#github-2018-10-split-brain) a 43-second network cut led to writes on both coasts.

## Trade-offs

| Option | Fast failover? | Safe if the old leader wakes up? | Use it when |
|---|---|---|---|
| Short lease (a few seconds) | Yes. | No. Pauses cause false failovers, and two leaders can overlap. | Duplicate work is cheap, like a cache warmer. |
| Long lease (a minute or more) | No. | Better, but a long pause can still outlast it. | Leader work is slow and rare, like a nightly batch. |
| Lease plus fencing token | Yes. | Yes. The storage rejects old leaders. | Leader writes must be correct: money, orders, primary databases. |
| Chubby-style lock-delay | No. Adds the delay to every failover. | Partly. Chubby calls it imperfect. [@chubby] | The storage cannot check tokens. |
| Human-approved failover | No. Minutes, not seconds. | Yes, if the person fences the old primary first. | Rare, risky moves of a primary database. |

## At work: how a senior uses this

- Ask: **"If two nodes believe they lead for one minute, what breaks?"** If the answer is "nothing much", a lease is enough. If it is "money moves twice", you need fencing.
- Find where the token is checked. It must be in the storage or the downstream service, not in the leader's own code.
- Make leader code stop when it loses the lease. In etcd, watch `Session.Done`. In client-go, stop all work in the callback for lost leadership.
- Measure lease time with a monotonic clock, not the wall clock.
- Check that the renew deadline is clearly shorter than the lease duration, and that both are longer than your worst real GC pause.
- Ask what triggers a failover. A sick coordination service should not, by itself, promote a new database primary.
- Check that the new leader can take the full load before it is promoted. At Cloudflare, it had no local replicas left to help. [@cloudflare-2020]
- Alert on leader changes. Many changes per hour mean the lease is too short or something is pausing.

## With AI

### What to ask the agent

> "Our report job runs on 3 pods and uses client-go leader election. Show the lease settings and explain what happens if the leader has a 30-second GC pause. Which writes could happen twice?"

> "Add a fencing token to this job. Take it from the etcd election revision, send it with every write, and make the database reject writes with a smaller token. Write a test for a paused old leader."

> "List every automatic failover in our system. For each, say what signal triggers it and whether a sick coordination service alone could trigger it."

### What to check yourself

- The token check is a conditional write in the storage, not an "am I leader?" call just before the write.
- The token always grows. A host name, random ID or timestamp from the leader's clock is not a fencing token.
- The leader stops all work on lost leadership. It does not finish the current batch first.
- Timeouts come from your measured worst pauses, not from guesses.
- The agent did not shorten the lease for faster failover without saying how many false failovers that adds.

## Check yourself

## Go deeper

Before this: [failure detection and membership](/learn/failure-detection-and-membership). Related: [Consensus and Raft](/learn/consensus-and-raft), [Database replication](/learn/database-replication), [Time, clocks and ordering](/learn/time-clocks-and-ordering), [Locks, atomics and deadlocks](/learn/locks-atomics-and-deadlocks) and [Redis in depth](/learn/redis-in-depth). Next: [ZooKeeper, etcd and coordination](/learn/zookeeper-etcd-coordination). See [the Cloudflare incident page](/incidents#cloudflare-2020-11-etcd-failover).
