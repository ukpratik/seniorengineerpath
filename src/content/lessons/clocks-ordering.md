---
topic: clocks-ordering
subtitle: Server clocks drift, jump and disagree. Learn which clock to use for what, why timestamps can lose data, and how logical clocks put events in order.
status: published
reviewed_by: ai-factcheck
reviewed_on: 2026-10-21
illustrative_story: false
sources:
  - id: cloudflare-2017
    title: "Cloudflare, “How and why the leap second affected Cloudflare DNS” (January 2017)"
    url: https://blog.cloudflare.com/how-and-why-the-leap-second-affected-cloudflare-dns/
    note: The postmortem. A negative time difference reached rand.Int63n, which panicked, and some DNS lookups failed.
  - id: linux-clock
    title: "Linux man-pages: clock_gettime(2)"
    url: https://man7.org/linux/man-pages/man2/clock_gettime.2.html
    note: CLOCK_REALTIME (settable wall clock) and CLOCK_MONOTONIC (cannot be set).
  - id: go-time
    title: "Go standard library documentation: package time, “Monotonic Clocks”"
    url: https://pkg.go.dev/time
    note: The wall clock is for telling time, the monotonic clock for measuring time, with a short code example.
  - id: google-smear
    title: "Google Public NTP: Leap Smear"
    url: https://developers.google.com/time/smear
    note: How Google spreads a leap second over 24 hours, and the smear it recommends to others.
  - id: lamport-1978
    title: "Leslie Lamport, “Time, Clocks, and the Ordering of Events in a Distributed System”, Communications of the ACM 21(7), July 1978"
    url: https://lamport.azurewebsites.net/pubs/time-clocks.pdf
    note: The “happened before” relation, the Clock Condition, the two rules for logical clocks, and how to break ties for a total order.
  - id: dynamo
    title: "Werner Vogels, “Amazon's Dynamo” (2007), with the SOSP paper"
    url: https://www.allthingsdistributed.com/2007/10/amazons_dynamo.html
    note: Shows vector clocks catching conflicting versions, and when the client must merge them. It also mentions last write wins.
  - id: cassandra-dynamo
    title: "Apache Cassandra documentation: Dynamo (architecture)"
    url: https://cassandra.apache.org/doc/latest/cassandra/architecture/dynamo.html
    note: Cassandra resolves conflicts by last write wins, with a timestamp per column, and says its correctness depends on clocks.
  - id: hlc-paper
    title: "Sandeep Kulkarni, Murat Demirbas, Deepak Madeppa, Bharadwaj Avva and Marcelo Leone, “Logical Physical Clocks and Consistent Snapshots in Globally Distributed Databases” (2014)"
    url: https://cse.buffalo.edu/tech-reports/2014-04.pdf
    note: The hybrid logical clock (HLC) paper. Section 1 also compares Lamport clocks, vector clocks, NTP time and TrueTime.
  - id: spanner
    title: "James C. Corbett et al. (Google), “Spanner: Google's Globally-Distributed Database”, OSDI 2012"
    url: https://static.googleusercontent.com/media/research.google.com/en//archive/spanner-osdi2012.pdf
    note: Section 3 explains the TrueTime API, and section 4.1.2 the commit wait rule.
claims_checked:
  - claim: "RRDNS computed a duration with time.Now().Sub(start); when time went backwards the value went negative, and rand.Int63n panicked on it. At peak about 0.2% of DNS queries were affected, and impact ended at 06:45 UTC."
    source: cloudflare-2017
  - claim: "Lamport's Clock Condition: if a happens-before b, then C(a) < C(b). The converse does not hold, because concurrent events can have any order of numbers."
    source: lamport-1978
  - claim: "TrueTime keeps uncertainty generally under 10 ms; in production it varied from about 1 to 7 ms; commit wait was about 5 ms in the 1-replica tests."
    source: spanner
  - claim: "Google recommends a 24-hour linear smear from noon to noon UTC, making each second about 11.6 μs longer."
    source: google-smear
  - claim: "Cassandra resolves conflicts per column by last write wins, with timestamps from the client or the coordinator node, and its correctness depends on clocks."
    source: cassandra-dynamo
  - claim: "NTP usually keeps time within tens of milliseconds over the public internet, but can occasionally be off by 100 ms or more."
    source: hlc-paper
quiz:
  - q: Your code measures how long a database call takes by subtracting two wall-clock readings. What can go wrong?
    options:
      - Nothing. The wall clock always moves forward.
      - NTP or a leap second can move the wall clock, so the result can be wrong or even negative.
      - The result will always be zero.
    answer: 1
    why: The wall clock can be set and adjusted. Cloudflare's 2017 outage came from a negative duration like this. Use the monotonic clock for durations.
  - q: Two servers use last write wins with their own clock timestamps. Server B's clock is 2 seconds slow. A user updates a record on A, then 1 second later on B. What is kept?
    options:
      - The update from B, because it was made later.
      - The update from A, because it has the bigger timestamp. B's newer update is lost.
      - Both updates, merged.
    answer: 1
    why: Last write wins compares timestamps, not real order. B's slow clock gives the newer write the smaller timestamp, so it loses without any error.
  - q: Event a has Lamport timestamp 5 and event b has 9. What can you say for sure?
    options:
      - a happened before b.
      - b did not happen before a.
      - a and b are concurrent.
    answer: 1
    why: If b happened before a, b would have the smaller number. But a smaller number alone does not prove a happened before b. They may be concurrent.
  - q: What does Spanner's commit wait do?
    options:
      - It waits until the clock uncertainty for the commit timestamp has passed, before anyone can see the data.
      - It waits for every replica in the world to answer.
      - It waits for the next leap second.
    answer: 0
    why: Clients cannot see the data until TrueTime says the timestamp has surely passed. This gives external consistency. The paper measured about 5 ms of waiting.
  - q: A mobile app sends a "created at" time with each order. How should the server use it?
    options:
      - Use it to decide which order came first.
      - Store it only as information. Order events with server-assigned sequence numbers or times.
      - Reject any order whose time is in the past.
    answer: 1
    why: The phone's user controls its clock. Order on the server side, where you control the clock or counter.
incidents: [cloudflare-2017-01-leap-second]
cases: [google-spanner]
related: [why-distributed-hard, unique-ids, quorums-leaderless, crdts, leader-election, distributed-transactions]
---

## What broke

At midnight UTC on 1 January 2017, a leap second was added to the world's clocks. At that moment, some DNS lookups through Cloudflare began to fail. [@cloudflare-2017]

Their DNS software, RRDNS, is written in Go. For CNAME records, it asked upstream DNS servers. It measured how long they took to answer, with code like `time.Now().Sub(start)`. Around the leap second, `time.Now()` could return a time earlier than `start`. So the measured time was negative. [@cloudflare-2017]

The negative numbers flowed into the code that picks which server to ask next. That code called `rand.Int63n`, which panics if its argument is negative. Lookups for customers who use CNAME records failed. At the peak, about 0.2% of DNS queries to Cloudflare were affected. [@cloudflare-2017]

The post names the root cause as one belief: time cannot go backwards. The code assumed the difference between two times would be, at worst, zero. At that time, the post notes, Go did not offer a monotonic time source. [@cloudflare-2017]

The fix was live worldwide by 06:45 UTC. [@cloudflare-2017]

## The idea in simple words

Every server has its own clock, and no two agree exactly. NTP keeps them close, but not equal. Over the public internet, NTP can usually keep clocks within tens of milliseconds. Uneven routes and busy networks can sometimes cause errors of 100 ms or more. [@hlc-paper]

So you often cannot tell which of two events came first by comparing their clock times. Leslie Lamport made this point in 1978. In a distributed system, he wrote, it is sometimes impossible to say which of two events occurred first. [@lamport-1978]

> [!key] **The key idea.** Use a clock to measure how long something takes, on one machine, with the monotonic clock. To decide what came first across machines, use counters, messages or one server that assigns the order. Do not trust wall-clock timestamps for that.

## How it works

### Two clocks on every machine

Your operating system gives you two clocks. Linux calls the first one `CLOCK_REALTIME`. It is the wall clock, which shows the real date and time. It can jump if someone sets it, and NTP also adjusts its speed. [@linux-clock]

The second is `CLOCK_MONOTONIC`. Nobody can set it. It counts from some point in the past, and that point has no meaning of its own. [@linux-clock]

Go's documentation puts the rule simply: "the wall clock is for telling time and the monotonic clock is for measuring time." In its example, an operation measured with `time.Now()` and `Sub` gives a positive time of about 20 ms. This holds even if the wall clock changes during the operation. [@go-time]

### Skew, NTP and leap seconds

Clock skew is the gap between two clocks. NTP shrinks it, but cannot remove it. Wall-clock time also has kinks. Leap seconds and jumps in system time can make timestamps go backwards. [@hlc-paper]

A leap second is one extra second added to UTC. A clock that steps back for it can break code like Cloudflare's. Google spreads the extra second over the hours around the leap instead. This is called a leap smear. [@google-smear]

Google recommends a 24-hour linear smear, from noon to noon UTC. During the smear, each second is about 11.6 μs longer than normal. [@google-smear] Our advice: use the same time source for every server in a fleet. Our own arithmetic shows why: halfway through a 24-hour smear, a smeared clock is about half a second away from one that is not smeared.

### Why "last write wins" can lose data

Many stores settle conflicts with last write wins. Cassandra does this, with a separate timestamp on every column. The timestamp comes from the client's clock, or from the clock of the node that coordinates the write. [@cassandra-dynamo] Cassandra says its correctness depends on these clocks, and tells you to run NTP. [@cassandra-dynamo] Dynamo also describes last write wins: the version with the largest physical timestamp is kept. [@dynamo]

Now add skew. If one server's clock is a little behind, a newer write can get an older timestamp. It then loses to the write it should replace. Nothing fails, and no error is logged. The data is just gone.

Step through it.

[[diagram:clock-skew-lww]]

### Ordering without clocks: Lamport clocks

Lamport defined order by what could affect what, not by time. Event a **happens-before** event b if one of three rules holds. Both are in one process and a comes first. Or a sends a message and b receives it. Or a happens-before c, and c happens-before b. [@lamport-1978] If neither event happens-before the other, they are **concurrent**. [@lamport-1978]

A Lamport clock gives each event a number with two rules. Each process adds one to its counter between any two of its events. Each message carries the sender's counter. The receiver then moves its own counter past that value. [@lamport-1978]

These rules give the Clock Condition: if a happens-before b, then a's number is smaller than b's. [@lamport-1978] To get one total order, sort by the number, and break ties by a fixed order of the processes, such as their IDs. [@lamport-1978]

Know the limit. The reverse is not true. A smaller number does not prove that a happened first, because the two events may be concurrent. [@lamport-1978] In the diagram, the counter fixed the order only because Server B read the price before it changed it.

### Vector clocks, in one paragraph

A vector clock is a list of (node, counter) pairs, one list per version of the data. Dynamo uses them to see whether two versions have a causal order or sit on parallel branches. If they are on parallel branches, Dynamo keeps both and the client must merge them. [@dynamo] So vector clocks can find conflicts that a Lamport clock hides. The cost is size: the list can grow with the number of nodes. [@hlc-paper]

### Hybrid logical clocks

A hybrid logical clock (HLC) mixes the two ideas. Like a Lamport clock, it follows the happens-before order. It also keeps its value close to the NTP clock, so the timestamp still means roughly "when". [@hlc-paper]

An HLC fits in the 64-bit NTP timestamp format. It only reads the physical clock and never sets it. It keeps working, and still tracks causality, even when NTP has small problems. [@hlc-paper]

### TrueTime and commit wait

Google's Spanner takes a different path: it makes clock error visible. Its TrueTime API does not return one time. `TT.now()` returns an interval, `[earliest, latest]`. The true time is always inside it. [@spanner]

TrueTime uses GPS and atomic clocks as time sources. [@spanner] This keeps the uncertainty small, generally under 10 ms. [@spanner] In Google's production, the error bound moved between about 1 and 7 ms in each poll interval. Its average was 4 ms most of the time. [@spanner]

For each write, Spanner picks a commit timestamp no smaller than `TT.now().latest`. Then comes **commit wait**. Clients cannot see the new data until `TT.after(s)` is true, that is, until the timestamp has surely passed. [@spanner]

This gives external consistency. If transaction T1 commits before T2 starts, T1 gets the smaller timestamp. [@spanner] The price is waiting: in the paper's tests, commit wait was about 5 ms. [@spanner] The HLC paper notes that TrueTime needs special hardware, which is not possible for many systems, such as those on leased cloud machines. [@hlc-paper]

## Trade-offs

| Option | Orders events across servers? | Cost | Use it when |
|---|---|---|---|
| Wall-clock timestamps | No. Skew can make a newer write lose. | Free and simple. | Showing dates to people. Rough order is fine. |
| Monotonic clock | No. It means nothing outside one machine. | Free. | Timeouts, retries, durations and rate limits. |
| One server assigns sequence numbers | Yes, for everything that goes through it. | Every write goes through one place. | Most apps. Let the database or leader give the order. |
| Lamport clock | Yes, it respects happens-before. It cannot spot concurrent events. [@lamport-1978] | One counter on every message. | You need one total order that respects cause and effect. |
| Vector clock | Yes, and it can find conflicting versions. [@dynamo] | Size grows with the number of nodes. [@hlc-paper] | Many nodes take writes, and the app can merge conflicts. |
| Hybrid logical clock | Yes, and stays close to real time. [@hlc-paper] | A little more logic than a plain counter. | Databases that need timestamps that are both causal and near real time. |
| TrueTime | Yes, with external consistency. [@spanner] | Special hardware, and a short wait on every commit. [@spanner] [@hlc-paper] | You run on a platform that offers it. |

## At work: how a senior uses this

- Search the code for durations computed from wall-clock time. In Go, keep the value from `time.Now()` and use `Sub`, so the monotonic reading is used. [@go-time] In other languages, find the monotonic clock in the official docs.
- Never trust client clocks for order. A phone's clock is set by its user. Store the client time as information only.
- Let one place assign order when you can: a database sequence or a leader. See [unique ID generation](/learn/unique-id-generation).
- In a design review, ask: **"If two of our servers' clocks are one second apart, what breaks?"** If the answer is "the newest write might be lost", you have last write wins on wall-clock time.
- Do not build locks or leases on clocks from different machines. See [leader election and leases](/learn/leader-election-and-leases).
- Watch clock offset on every host, and alert when NTP stops syncing.

## With AI

### What to ask the agent

> "Find every place in this repo that subtracts two wall-clock readings to get a duration or timeout. Show the file and line, and suggest the monotonic clock call for this language. Link the official docs for it."

> "This table uses last write wins with an `updated_at` set by each app server. List the cases where a newer update could be lost, and suggest a fix with a version number or a database sequence."

### What to check yourself

- Every suggested clock call is the monotonic one, checked in the language's official documentation.
- The agent did not "fix" ordering by adding more NTP servers. Smaller skew still allows lost writes.
- Any version check is done in the database, in one atomic step, not read first and compared later in app code.
- Code that runs during a leap second, or a clock jump, still works. Ask for a test that moves the wall clock backwards.

## Check yourself

## Go deeper

Before this: [why distributed systems are hard](/learn/why-distributed-systems-are-hard). Next: [quorums and leaderless replication](/learn/quorums-and-leaderless-replication), [CRDTs and coordination avoidance](/learn/crdts-and-coordination-avoidance), and [distributed transactions](/learn/distributed-transactions-two-phase-commit).
