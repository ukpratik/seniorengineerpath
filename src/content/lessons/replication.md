---
topic: replication
subtitle: Copies of your data make reads fast and keep you running. They also make users see the past, unless you plan for it.
status: published
reviewed_by: ai-factcheck
reviewed_on: 2026-10-21
illustrative_story: true
sources:
  - id: ddia
    title: "Martin Kleppmann, Designing Data-Intensive Applications (O'Reilly)"
    where: "1st edition (2017): chapter 5, Replication (leaders and followers, replication lag, multi-leader, leaderless and quorums)."
  - id: pg-repl
    title: "PostgreSQL documentation: High Availability, Load Balancing, and Replication (Log-Shipping Standby Servers)"
    url: https://www.postgresql.org/docs/current/warm-standby.html
    note: Sections on streaming replication and synchronous replication. Shows that replication is async by default and what sync costs.
  - id: mysql-semisync
    title: "MySQL 8.4 Reference Manual: Semisynchronous Replication"
    url: https://dev.mysql.com/doc/refman/8.4/en/replication-semisync.html
    note: Why async replication can lose committed writes on failover, and what waiting for one replica buys you.
  - id: github-2018
    title: "GitHub, “October 21 post-incident analysis”"
    url: https://github.blog/2018-10-30-oct21-post-incident-analysis/
    note: A 43-second network cut, an automatic failover across the US, and writes on both coasts that could not be merged.
  - id: jepsen-ryw
    title: "Jepsen, Consistency models: Read Your Writes"
    url: https://jepsen.io/consistency/models/read-your-writes
    note: A short, exact definition of the read-your-writes guarantee.
  - id: jepsen-mr
    title: "Jepsen, Consistency models: Monotonic Reads"
    url: https://jepsen.io/consistency/models/monotonic-reads
    note: A short, exact definition of monotonic reads.
claims_checked:
  - claim: "Postgres streaming replication is async by default; lag is typically under one second; a primary crash can lose committed transactions."
    source: pg-repl
    note: "Verified on postgresql.org warm-standby page (Streaming Replication and Synchronous Replication sections). Sync commit waits for WAL on disk on both servers; minimum wait is one round trip."
  - claim: "MySQL replication is async by default; semisync waits for at least one replica to receive and log the change, and falls back to async on timeout."
    source: mysql-semisync
    note: "Verified on MySQL 8.4 manual, Semisynchronous Replication page. Changed 'received' to 'received and logged'."
  - claim: "GitHub 2018: 43-second network cut, Orchestrator failed over to US West Coast, several seconds of unreplicated East writes, nearly 40 minutes of West writes, 24 h 11 min degraded."
    source: github-2018
    note: "Verified against GitHub's post-incident analysis. Fixed 'a few seconds' to 'several seconds' and 'about 40' to 'nearly 40' to match its wording."
  - claim: "Read-your-writes and monotonic reads definitions."
    source: jepsen-ryw
    note: "Verified on Jepsen pages for read-your-writes and monotonic reads; lesson wording matches."
  - claim: "With n copies, w + r > n means a read overlaps the latest write; n = 3, w = r = 2 is a common choice."
    source: ddia
    note: "Book not opened. Confirmed via reader highlights of DDIA ch. 5 (n = 3, w = 2, r = 2 example; w + r > n) and the overlap follows from counting. Other ch. 5 claims (failover steps, lag fixes) match the book's well-known structure."
quiz:
  - q: A user saves a new address. The page reloads from a follower and shows the old address. What is this?
    options:
      - Data loss on the leader.
      - Replication lag. The follower has not got the change yet.
      - A split-brain.
    answer: 1
    why: The write is safe on the leader. The follower is just behind, so the read shows the past.
  - q: Postgres uses asynchronous replication, and the leader crashes. What can happen after failover?
    options:
      - Nothing. Every committed write is on the follower.
      - Some writes that were committed on the old leader may be missing.
      - The database refuses all writes until the old leader returns.
    answer: 1
    why: With async replication the leader does not wait for followers, so the newest commits may not have reached them.
  - q: You have 3 copies of the data in a leaderless store. You write to 2 and read from 2. Why does the read see the latest write?
    options:
      - Because 2 + 2 is more than 3, so at least one copy you read has the write.
      - Because writes always go to every copy.
      - Because the leader checks every read.
    answer: 0
    why: When w + r > n, the set of copies you read and the set you wrote must share at least one copy.
  - q: What makes split-brain so painful to fix?
    options:
      - The database is down for a long time.
      - Two nodes accepted different writes, and you must merge them without losing either.
      - The followers are too slow.
    answer: 1
    why: Both sides hold writes the other lacks. GitHub spent about 24 hours on this in 2018.
incidents: [github-2018-10-split-brain]
related: [quorums-leaderless, cap-consistency, leader-election, multi-region]
---

## What broke

Ravi changes his delivery city from Pune to Delhi in a shopping app. He taps Save and sees "Saved". The page reloads, and the city still says Pune.

He taps Save again. Same result. He writes to support: "Your app does not save my address." Five seconds later he refreshes once more, and now it says Delhi.

Nothing was lost. The write went to the leader, the one database that takes writes. The page read came from a copy that was 3 seconds behind. The team had added copies last month to handle more reads. Nobody had asked what users would see while a copy catches up.

This bug is common, hard to reproduce on a laptop, and easy to prevent once you know its name.

## The idea in simple words

Replication means keeping the same data on several machines. You do it for three reasons. Reads can be spread across many machines. A copy can take over if one machine dies. And a copy near the user can answer faster.

The most common setup has one leader and some followers. All writes go to the leader. The leader sends each change to every follower, and the followers apply the changes in the same order. Reads can go to the leader or to any follower. [@ddia]

> [!key] **The key idea.** A follower is a copy of the past. How far in the past is the replication lag. Every read from a follower must be fine with seeing that past.

Most of the time the lag is small. Postgres says it is typically under one second when the standby can keep up. [@pg-repl] But that is not a promise. Our advice: plan for the lag to grow much larger under heavy load, a long query, or a network problem.

## How it works

Step through Ravi's bug. Use Next and Back, or the ← and → keys.

[[diagram:replication-stale-read]]

Now try it yourself. Move the sliders to change the lag and the moment you read.

[[anim:replication-lag]]

### Sync or async

The big choice is when the leader says "done" to the client.

With synchronous replication, the leader waits until a follower confirms it has the change. By default, Postgres waits until the commit is written to disk on both servers. The cost is at least one network round trip on every commit. [@pg-repl]

With asynchronous replication, the leader says "done" at once and sends the change later. Writes are fast. But if the leader crashes, its newest commits may never have reached a follower. Postgres and MySQL both replicate asynchronously by default, and both docs warn about this data loss. [@pg-repl] [@mysql-semisync]

MySQL also has a middle option, semisynchronous replication: the leader waits until at least one replica has received and logged the change. If no replica answers before a timeout, it falls back to async. [@mysql-semisync]

### Lag bugs, and their names

Two lag bugs come up again and again. [@ddia]

- **Read-your-writes.** After you save something, your own next read should show it. Ravi's bug breaks this. Other users may see the change a bit later, and that is usually fine. [@jepsen-ryw]
- **Monotonic reads.** Your reads should never go back in time. Imagine you refresh twice. The first read hits a fresh follower and shows a new comment. The second hits a slow follower, and the comment is gone. [@jepsen-mr]

Common fixes: read a user's own recent data from the leader for a short time after they write. Send all of one user's reads to the same follower. Or make the client remember the last version it saw, and skip followers that are behind it.

### Failover and split-brain

When the leader dies, a follower must become the new leader. This is failover. It sounds simple, but each step can go wrong. [@ddia]

First, you must decide the leader is dead. You usually use a timeout. Too short, and a slow leader gets replaced for no reason. Second, you pick the follower with the most data. With async replication, some recent writes may be missing. Third, every client must send writes to the new leader.

The worst case is split-brain: the old leader is still alive and still takes writes. Now two nodes think they are the leader. Their data goes in two directions.

This happened to GitHub in October 2018. A network cut lasted 43 seconds. In that time, their failover tool made database servers on the US West Coast the new leaders. Several seconds of writes on the East Coast never reached the West. Then the West took writes for nearly 40 minutes. Both sides now had writes the other did not have. [@github-2018]

GitHub chose to keep user data correct over coming back fast. The site was degraded for 24 hours and 11 minutes while they put the data back together. [@github-2018]

### Two other shapes, briefly

**Multi-leader.** Each data centre has its own leader, and the leaders copy to each other. Writes are fast everywhere. But two people can change the same row in two places, and you must resolve the conflict. [@ddia]

**Leaderless.** The client writes to several copies and reads from several copies. Amazon's Dynamo made this popular, and Cassandra works this way. With n copies, you write to w of them and read from r. If w + r > n, at least one copy you read has the latest write. With 3 copies, w = 2 and r = 2 is a common quorum. [@ddia]

## Trade-offs

| Setup | Lose writes on crash? | Cost | Use it when |
|---|---|---|---|
| Leader + async followers | Yes. The newest commits may be lost on failover. | Fast writes. Stale reads from followers. | Most web apps. It is the default in Postgres and MySQL. |
| Leader + one sync follower | No, unless both crash at once. | Every commit waits for a round trip. If the sync follower is down, writes can stall. | Money, orders, anything you must not lose. |
| Multi-leader | Yes. Conflicts can drop one side's change. | You must design conflict rules. | Several regions must accept writes, or apps must work offline. |
| Leaderless with quorums | Depends on w and r. | More copies per request. Edge cases with lag still exist. | Very high write volume, and you can live with some stale reads. |

**Simple rule:** start with one leader and async followers. Then decide, for each read, whether it may see stale data.

## At work: how a senior uses this

- For every new read path, ask: **"What does the user see if this read is 5 seconds old?"** If the answer is "a bug report", read from the leader.
- Watch replication lag on a dashboard, and alert on it. A follower that is minutes behind is a quiet incident.
- After a write, send that user's reads to the leader for a short window. Make the window longer than your normal lag.
- Ask how failover works before you need it. Who decides the leader is dead? What stops the old leader from taking writes?
- Practise failover in a test environment. Measure how many writes you lose and how long clients take to find the new leader.
- Know your settings. "Async by default" means a crash can lose your last commits. [@pg-repl]

## With AI

### What to ask the agent

> "We read profiles from a Postgres read replica. Change the code so that for 10 seconds after a user updates their profile, that user's reads go to the primary."

> "Write an integration test with a primary and a replica where replication is paused. Assert that a user's own profile read after a save shows the new value."

> "List every query in this service that reads from a replica. For each, tell me what goes wrong if the data is 30 seconds old."

### What to check yourself

- The "read from leader" window really covers your worst normal lag, not just the average.
- A connection pool or ORM (a library that maps code objects to database rows) did not quietly send writes, or reads inside a transaction, to the replica.
- Reads that check a rule before a write, like "is the coupon still unused?", go to the leader.
- The test really pauses or slows replication. A test with zero lag proves nothing.
- Failover scripts stop the old leader from taking writes before they promote a new one.

## Check yourself

## Go deeper

Next: [Transactions and isolation levels](/learn/transactions-and-isolation-levels). It covers bugs inside one database, even with no copies at all.
