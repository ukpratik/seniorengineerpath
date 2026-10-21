---
topic: latency-estimation
subtitle: Know the speed of memory, disk and network, and size a system in five minutes with simple maths.
status: published
reviewed_by: ai-factcheck
reviewed_on: 2026-10-21
illustrative_story: true
sources:
  - id: scott
    title: "Colin Scott, “Latency Numbers Every Programmer Should Know” (interactive page, based on numbers from Jeff Dean and Peter Norvig)"
    url: https://colin-scott.github.io/personal_website/research/interactive_latency.html
    note: The classic latency table with a year slider. Most numbers are estimates projected from older measurements.
  - id: norvig
    title: "Peter Norvig, “Teach Yourself Programming in Ten Years” (table of approximate timings)"
    url: https://norvig.com/21-days.html
    note: The original short table of how long common operations take on a typical computer.
  - id: xu
    title: "Alex Xu, System Design Interview – An Insider's Guide, chapter 2: Back-of-the-envelope estimation (ByteByteGo)"
    url: https://bytebytego.com/courses/system-design-interview/back-of-the-envelope-estimation
    note: Powers of two, latency numbers, a worked QPS and storage estimate, and tips for doing it in an interview.
  - id: sre-monitoring
    title: "Google, Site Reliability Engineering, chapter 6: Monitoring Distributed Systems"
    url: https://sre.google/sre-book/monitoring-distributed-systems/
    note: Why averages hide slow requests, and why you should look at the whole distribution.
  - id: slack
    title: "Slack Engineering, “Slack's Outage on January 4th 2021”"
    url: https://slack.engineering/slacks-outage-on-january-4th-2021/
    note: A real postmortem where the first work day of the year brought a sudden peak of traffic.
claims_checked:
  - claim: "Main memory read about 100 ns, SSD random read about 16 µs, same-data-centre round trip about 0.5 ms, California–Netherlands round trip about 150 ms."
    source: scott
    note: "Interactive page JS: memory 100 ns (flat after 2000), SSD 16,000 ns (flat after 2014), DC round trip 500,000 ns and CA–NL 150,000,000 ns (constants). Values are extrapolated estimates; L1 is about 1 ns there, while Norvig and Xu (Dean 2010) give 0.5 ns."
  - claim: "Powers of two map to KB, MB, GB, TB; Xu's worked example uses a peak factor of 2."
    source: xu
    note: "Chapter 2: 2^10 ≈ thousand ... 2^40 ≈ trillion; Twitter example peak QPS = 2 × QPS. The ×5 dinner peak in this lesson is the writer's assumption and is labelled as one."
  - claim: "A service with a 100 ms average latency can have 1% of requests take 5 seconds; many systems slow down before 100% utilization."
    source: sre-monitoring
    note: "Chapter 6 quotes: '1% of requests might easily take 5 seconds' (at 1,000 RPS) and 'many systems degrade in performance before they achieve 100% utilization'."
  - claim: "On 4 January 2021 Slack went from its quietest time to one of its busiest days, and its AWS Transit Gateways did not scale fast enough."
    source: slack
    note: "Postmortem: 'from our quietest time of the whole year to one of our biggest days quite literally overnight'; 'our TGWs did not scale fast enough'."
  - claim: "Fetch from main memory 100 ns; send packet US to Europe and back 150 ms."
    source: norvig
    note: "Norvig's approximate timing table (undated, page © 2001–2014). It has no SSD row; the SSD number comes from Scott only."
  - claim: "Worked maths: 40 M/100,000 = 400 QPS; ×5 = 2,000; ÷500 = 4 servers; 500,000 × 2 KB = 1 GB/day ≈ 365 GB/year; 2,000 × 20 KB = 40 MB/s ≈ 320 Mbit/s; 150 ms ÷ 100 ns = 1.5 million."
    source: xu
    note: "Re-computed by hand. Exact average is 463 QPS (86,400 s), so rounding to 100,000 s is 13.6% low, as the lesson says."
quiz:
  - q: About how many seconds are in one day, for quick estimates?
    options: ["About 1,000", "About 100,000", "About 10,000,000"]
    answer: 1
    why: A day has 86,400 seconds. Round it to 100,000 (10^5) to make division easy.
  - q: A service gets 10 million requests a day. The busiest hour runs at 3 times the average. What peak QPS should you plan for?
    options: ["About 100 QPS", "About 300 QPS", "About 3,000 QPS"]
    answer: 1
    why: 10 million ÷ 100,000 seconds ≈ 100 QPS on average. Three times that is about 300 QPS.
  - q: A read from RAM takes about 100 ns. A round trip from California to the Netherlands takes about 150 ms. How much slower is the round trip?
    options: ["About 10 times", "About 1,000 times", "About 1,000,000 times"]
    answer: 2
    why: 150 ms is 150,000,000 ns. Divided by 100 ns, that is 1.5 million times slower.
  - q: Your estimate says one server is enough for the daily average. What is the biggest risk?
    options:
      - The estimate used round numbers.
      - Traffic is not spread evenly, so the busy hour can be several times the average.
      - The server will be too fast.
    answer: 1
    why: Users come in waves. Size for the busiest minute, then add spare capacity.
incidents: [slack-2021-01-network]
related: [capacity-planning, tail-latency-queueing, zero-to-millions]
---

## What broke

A food delivery app is about to launch in a new, large city. Arjun owns the order service. His manager asks: "How many servers do we need?"

Arjun does the maths. He expects about 40 million requests a day. He divides by 86,400 seconds and gets about 460 requests per second. A load test shows that one server handles about 500. He runs two servers, to be safe.

On the first Friday, at 8 pm, everyone orders dinner at once. Traffic reaches about five times the daily average: over 2,000 requests per second. Both servers hit 100% CPU. Requests wait in queues, and responses take 8 seconds instead of 200 ms.

The app retries slow calls, which adds even more load. Orders fail for 40 minutes on the busiest night of the week.

Arjun's maths was correct. His question was wrong. He sized the system for the average second of the day, but users do not arrive evenly. (This story is made up, but the pattern is common.)

## The idea in simple words

Before you build, you should know roughly how big and how fast a system must be. You do not need exact numbers. You need the right **order of magnitude**: is it 10, 1,000 or 100,000 requests per second? Each answer leads to a very different design.

A **back-of-envelope estimate** gets you there in a few minutes. It uses two tools:

1. **The latency ladder.** How long common operations take, from a CPU cache read to a call across the ocean.
2. **Simple traffic maths.** Users → requests per second (QPS) → servers, storage and bandwidth.

> [!key] **The key idea.** Round hard and write down your assumptions. Then size for the peak, not the average. A system that is fine on an average day can still fall over in its busiest hour.

## How it works

### The latency ladder

These are rough numbers from the well-known table by Jeff Dean and Peter Norvig, as kept up to date by Colin Scott [@norvig] [@scott]. They are the values his page shows for recent years. Most are estimates, and your hardware will differ. Learn the **steps between them**, not the exact digits.

To make them feel real, pretend that 1 nanosecond (ns) is 1 second:

- **Read from the CPU's L1 cache:** about 1 ns (older tables say 0.5 ns) → *1 second*.
- **Read from main memory (RAM):** about 100 ns → *under 2 minutes*.
- **Random read from an SSD:** about 16 µs (16,000 ns) → *about 4.5 hours*.
- **Round trip inside one data centre:** about 0.5 ms (500,000 ns) → *almost 6 days*.
- **Round trip from California to the Netherlands:** about 150 ms → *almost 5 years*.

Three lessons come from this ladder:

- Memory is fast. A cache in RAM can save you a lot of time [@xu].
- A network call costs far more than local work. Avoid calls in a loop.
- A call across an ocean costs about 300 calls inside the data centre. A page that makes five such calls, one after another, waits about 750 ms for the network alone.

### Numbers to keep in your head

Powers of two are close to powers of ten. 2^10 is about a thousand (KB), 2^20 about a million (MB), 2^30 about a billion (GB) and 2^40 about a trillion (TB) [@xu].

A day has 86,400 seconds. Round it to **100,000** (10^5). Now "per day" to "per second" is just "divide by 100,000". The answer is about 14% too low, which is fine for an estimate.

Network speeds are in **bits**, but file sizes are in **bytes**. One byte is 8 bits, so 100 MB per second needs about 800 Mbit/s.

### A worked estimate

Step through Arjun's estimate, done the right way. Use Next and Back, or the ← and → keys.

[[diagram:latency-estimate]]

The steps are always the same:

1. **Requests per day.** Daily active users × requests per user. Here: 2 million × 20 = 40 million.
2. **Average QPS.** Divide by 100,000. Here: about 400 QPS.
3. **Peak QPS.** Multiply by a peak factor. Alex Xu's book example uses 2 [@xu]. Use your own traffic graphs if you have them. A food app at dinner time can be much more uneven. So this example assumes 5, which is a guess, not a measured number.
4. **Servers.** Divide peak QPS by what one server handles in a load test. Then add spare servers, so you survive losing one.
5. **Storage.** Items per day × size × days you keep them. Here: 500,000 orders × 2 KB ≈ 1 GB a day, or about 365 GB a year. One database can hold that.
6. **Bandwidth.** Peak QPS × response size. Here: 2,000 × 20 KB = 40 MB/s, or about 320 Mbit/s.

### Peak and average, for latency too

Averages hide problems in latency as well as traffic. Google's SRE book gives an example: a service with an average latency of 100 ms can still have 1% of requests take 5 seconds [@sre-monitoring]. So estimate and measure **percentiles** like p99, not only the average.

## Trade-offs

| Approach | Safe at the peak? | Cost | Use it when |
|---|---|---|---|
| Size for the daily average | No. The busy hour can be many times higher. | Cheapest on paper. | Never for user-facing systems. Fine for batch jobs that can run slower. |
| Size for the peak, plus spare | Yes. You survive the busiest hour and one lost server. | You pay for idle servers at night. | Most new services, and any launch you cannot afford to fail. |
| Autoscale from the average | Partly. New servers take minutes to start, and sudden spikes arrive faster. | Lower cost when traffic is calm. | Traffic rises slowly and you keep a minimum number of servers running. |
| Load test with real traffic shapes | Yes. You test the real peak, not a guess. | Takes days to build and run well. | Big launches, sales events and systems that move money. |

**Rule of thumb:** a quick estimate tells you which design to choose. A load test tells you if the design really works.

## At work: how a senior uses this

- When someone proposes a design, ask: **"How many requests per second at the peak, and how much data per year?"** If nobody knows, do the five-minute estimate together.
- Write the assumptions in the design doc: users, requests per user, peak factor, sizes [@xu]. Others can then correct one number instead of arguing about the result.
- Find the peak in real data. Look at the busiest minute of the busiest day, not a daily average. Watch for special days, like a sale, a holiday or a big match.
- Remember that the peak can arrive overnight. On 4 January 2021, Slack went from its quietest time of the year to one of its busiest days. Its cloud network links did not scale fast enough [@slack].
- Count network calls per request. Each call inside the data centre costs about 0.5 ms. Each call across an ocean, such as California to the Netherlands, costs about 150 ms [@scott].
- Plan for servers to run well below 100% CPU at the peak. Many systems slow down before they reach 100% [@sre-monitoring]. Agree on a target with your team.

## With AI

### What to ask the agent

> "Here are my assumptions: 2 million daily users, 20 requests each, a peak of 5 times the average, 2 KB per order, 20 KB per response. Estimate peak QPS, storage per year and peak bandwidth. Show every step with units."

> "List every network call this endpoint makes for one request. Mark which ones run one after another and which run in parallel."

### What to check yourself

- Every number has a unit: per second or per day, bits or bytes, KB or MB.
- The estimate uses a peak, not only the average, and the peak factor comes from real data or is clearly labelled as a guess.
- The answer is the right order of magnitude. Check one step by hand: divide by 100,000, then multiply by the peak factor.
- Latency numbers are treated as rough. AI tools often repeat the old table as if it were exact.
- The server count includes spare capacity, so losing one server does not take you down.

## Check yourself

## Go deeper

Next on this line: [Tail latency and queueing](/learn/tail-latency-and-queueing) explains why servers slow down sharply as they get busy.
