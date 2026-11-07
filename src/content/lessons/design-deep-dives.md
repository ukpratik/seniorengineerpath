---
topic: design-deep-dives
subtitle: Pick the riskiest part of a design, size it with numbers, compare a few options on your requirements, and say your trade-off in one clear sentence.
status: published
reviewed_by: ai-factcheck
reviewed_on: 2026-11-08
illustrative_story: true
sources:
  - id: hi-delivery
    title: "Hello Interview, “System Design Delivery Framework” (System Design in a Hurry)"
    url: https://www.hellointerview.com/learn/system-design/in-a-hurry/delivery
    note: The deep-dive step, its time box, who should lead it at each level, and the warning about talking over the interviewer.
  - id: hi-levels
    title: "Hello Interview, “The System Design Interview: What is Expected at Each Level”"
    url: https://www.hellointerview.com/blog/the-system-design-interview-what-is-expected-at-each-level
    note: How much depth, and how much leading, interviewers expect from mid-level, senior and staff candidates.
  - id: xu-framework
    title: "Alex Xu, System Design Interview – An Insider's Guide, “A Framework for System Design Interviews” (ByteByteGo)"
    url: https://bytebytego.com/courses/system-design-interview/a-framework-for-system-design-interviews
    note: The design deep-dive and wrap-up steps, with advice on time, on unhelpful detail and on never calling a design perfect.
  - id: ubl
    title: "Malte Ubl, “Design Docs at Google” (Industrial Empathy)"
    url: https://www.industrialempathy.com/posts/design-docs-at-google/
    note: Why a design doc should focus on trade-offs, and what the "alternatives considered" section is for.
  - id: ddb-partition
    title: "Amazon DynamoDB Developer Guide, “Best practices for designing and using partition keys effectively”"
    url: https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/bp-partition-key-design.html
    note: The read and write limits of one DynamoDB partition, and what a read unit and a write unit are.
  - id: ddb-sharding
    title: "Amazon DynamoDB Developer Guide, “Using write sharding to distribute workloads evenly”"
    url: https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/bp-partition-key-sharding.html
    note: Adding a random or calculated suffix to a key to spread writes, and the extra work it adds to reads.
  - id: ddb-partitions-how
    title: "Amazon DynamoDB Developer Guide, “Partitions and data distribution in DynamoDB”"
    url: https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/HowItWorks.Partitions.html
    note: How DynamoDB hashes the partition key of each item to choose the one partition that stores it.
  - id: firestore-best
    title: "Firebase documentation, “Best practices for Cloud Firestore”"
    url: https://firebase.google.com/docs/firestore/best-practices
    note: Why the highest update rate for one document depends on the workload, and the advice to load test.
  - id: firestore-counters
    title: "Firebase documentation, “Distributed counters” (Cloud Firestore)"
    url: https://firebase.google.com/docs/firestore/solutions/counters
    note: How a counter split into shards works, how read cost grows with the number of shards, and the roll-up alternative.
claims_checked:
  - claim: "Hello Interview gives the deep dive about 10 minutes, to meet non-functional requirements, handle edge cases, fix bottlenecks and respond to probes; more senior candidates should find these places themselves and lead."
    source: hi-delivery
    note: "\"use the remaining 10 or so minutes of the interview to harden your design\"; \"More senior candidates should be able to identify these places themselves and lead the discussion.\""
  - claim: "Hello Interview expects limited depth at mid-level, depth in around 2 places at senior, and depth in multiple areas at staff, where the candidate leads almost the entire interview."
    source: hi-levels
    note: "Exact quotes: \"the expectation for depth is limited\", \"go deep in around 2 places\", \"depth is expected in multiple areas\", \"lead almost the entire interview\"."
  - claim: "A DynamoDB partition is designed for at most 1,000 write units per second; one write unit is one write per second of an item up to 1 KB."
    source: ddb-partition
    note: "\"designed to deliver a maximum capacity of 3,000 read units per second and 1,000 write units per second\"; write unit = one write per second for an item up to 1 KB."
  - claim: "DynamoDB stores each item in one partition, chosen by a hash of its partition key."
    source: ddb-partitions-how
    note: "\"The output value from the hash function determines the partition in which the item will be stored.\""
  - claim: "Reading a sharded counter costs more as shards grow; a roll-up document updated less often makes reads cheap but lags behind."
    source: firestore-counters
    note: "\"The cost of reading a counter value increases linearly with the number of shards\"; roll-up document \"updated at a slower cadence\", clients wait for it."
  - claim: "Firestore gives no fixed maximum update rate for one document; it depends on the workload and should be load tested."
    source: firestore-best
    note: "\"The exact maximum rate that an app can update a single document depends highly on the workload.\""
quiz:
  - q: You have 10 minutes left in a design interview. Where should you go deep?
    options:
      - On the part you know best, even if it is low risk.
      - On the part most likely to break a non-functional requirement, after checking with the interviewer.
      - On every part, for about two minutes each.
    answer: 1
    why: A deep dive should prove the design meets its hardest requirement. Checking first means you spend the time where the interviewer wants it.
  - q: Which sentence is the best way to state a trade-off?
    options:
      - "We use Cassandra because it scales."
      - "50 shards cut each key to about 400 writes per second, at the cost of reading 50 rows. The count may lag 5 seconds, so a job can sum them once a second and pages read one total."
      - "There are many options, and each has pros and cons."
    answer: 1
    why: It names the gain, the cost and the requirement that decides. A listener can check each part.
  - q: One DynamoDB partition is designed for at most 1,000 write units per second. A viral post gets 20,000 likes per second on one counter row. What is the problem?
    options:
      - The row is a hot key. All writes go to one partition, about 20 times its limit.
      - The table needs more total capacity, and that alone fixes it.
      - There is no problem, because the database scales.
    answer: 0
    why: DynamoDB stores each item in one partition, so its limit applies no matter how big the table is. You need to spread the writes, for example with shards.
  - q: The interviewer asks, "What if a server crashes while it holds a second of likes in memory?" What is the best response?
    options:
      - Explain again why your design is correct.
      - Say it is an edge case and move on.
      - Agree that those likes would be lost, check it against the requirements, and change the design if needed.
    answer: 2
    why: Probes are part of the deep dive. Updating with a reason shows judgement. Defending a weak point does not.
incidents: []
related: [system-design-method, architecture-tradeoffs, requirements-nfrs, design-docs, latency-estimation, level-calibration]
---

## What broke

Ravi had three years of backend experience. He was in a system design interview for a senior role. The task was "design a photo-sharing app". After 20 minutes, his high-level design was fine.

Then he tried to cover everything. Feed: "Kafka and Redis." Storage: "Cassandra, because it scales." Search: "Elasticsearch." He spent about two minutes on each box. None of them had a number.

With 8 minutes left, the interviewer asked a question. "A famous user's post gets tens of millions of likes in its first hour. What happens to your like count?" Ravi said Cassandra handles it. The interviewer asked how many writes one row could take. Ravi did not know, so he said again that the database scales.

The feedback said: "Good breadth, no depth. Could not explain why he chose anything. Defended answers instead of reasoning about them." He got an offer, but at the mid level.

This story is made up, but each mistake in it is common. This lesson shows how those 8 minutes could have gone.

## The idea in simple words

A deep dive is the part where you take one hard piece of your design and work it out properly. Hello Interview gives it about 10 minutes of a design interview. You use them to meet the non-functional requirements, handle edge cases and fix bottlenecks. [@hi-delivery]

Depth is not knowing many tools. It is sizing one real problem, comparing a few fixes, and picking one for a reason others can check.

That reason is the trade-off. Every fix gives you something and costs you something. Malte Ubl, describing design docs at Google, calls the doc "the place to write down the trade-offs you made". [@ubl] The same habit works when you speak.

> [!key] **The key idea.** Pick one risky part. State the problem with a number. Give two or three options. Compare them on the requirements you agreed. Pick one, say what it costs, and say what would change your mind.

## How it works

Step through one deep dive on the like counter from the story. Use Next and Back, or the ← and → keys.

[[diagram:deep-dive-counter]]

### Choose where to go deep

You cannot go deep everywhere, so choose the part most likely to break the design. Good places are:

- **The hardest non-functional requirement**, such as a write rate or a latency target. Hello Interview's examples, for a Twitter-like app, are scaling to over 100 million daily users and fast feed loads. [@hi-delivery]
- **A bottleneck you noticed** and parked during the high-level design.
- **Where the interviewer steers.** Xu advises working with the interviewer to choose and rank the parts to go deep on. [@xu-framework]

Skip parts that are interesting but prove little. Xu's example: going into Facebook's EdgeRank feed ranking in detail "is not ideal". [@xu-framework]

Say your choice out loud and check it. "The like counter on viral posts worries me most. Shall I go deep there?"

### Step 1. State the problem with numbers

A problem without a number is an opinion. Make a quick back-of-envelope estimate, and label guesses as guesses. See [latency numbers and estimation](/learn/latency-numbers-and-estimation).

Suppose the counter lives in DynamoDB. Most likes on a viral post come in the first few minutes, so guess a peak of 20,000 likes per second. Each like adds 1 to one row. Now find the limit. A DynamoDB partition is designed for at most 1,000 write units per second. One write unit is one write of up to 1 KB per second. [@ddb-partition]

DynamoDB stores each item in one partition, chosen by a hash of its key. [@ddb-partitions-how] So one counter row can never get more than one partition's limit. It is about 20 times too slow. It is a hot key.

The limit depends on the store. Firestore's docs give no fixed limit for one document. They say it depends on the workload, and advise load testing. [@firestore-best] Their counter sample assumes about one increment per second for each shard. [@firestore-counters] Name the store you assume, then use its number.

### Step 2. Give two or three options

One option is not a choice. Six options is a list, not a decision. Two or three is enough.

- **A. One counter row.** Simple and exact. It fails at our peak.
- **B. Sharded counter.** Keep 50 rows for the post, such as `post42#1` to `post42#50`. Each like updates a random one. AWS calls this write sharding with a random suffix. [@ddb-sharding] To read the total, you read all the shards and add them up. [@firestore-counters]
- **C. Batch in the app.** Each app server counts likes in memory and writes the sum once a second. That means few writes. But a crash loses the likes the server has counted and not yet written, about a second's worth. If the app has already shown those likes as saved, they vanish.

### Step 3. Compare on the requirements

Do not compare options in general. Compare them on the requirements you wrote at the start. See [requirements and non-functional needs](/learn/requirements-and-non-functional-needs). Suppose you agreed on three:

1. Handle 20,000 likes per second on one post.
2. The count on the page may lag up to 5 seconds.
3. A like the user saw succeed must never vanish.

A fails the first. C fails the third. B meets all three, but it moves the cost to reads. Firestore's docs say read cost grows in step with the number of shards. Too many shards make reads slow and expensive. [@firestore-counters]

So add one small piece. A job sums the shards each second and stores the total. Pages read that one value. Firestore's docs describe this kind of roll-up, at the cost of a total that lags behind. [@firestore-counters] Requirement 2 allows that lag.

### Step 4. Pick, with a trade-off sentence

Use one sentence shape every time:

> "X gives us A at the cost of B. Given requirement R, that is the right call."

For the counter: "50 shards cut each key to about 400 writes per second, well under the 1,000 limit, at the cost of reading 50 rows for a total. The count may lag 5 seconds, so we read a cached total, and that cost is fine."

The numbers are simple: 20,000 ÷ 50 = 400 writes per second for each shard key. That leaves room even if two shard keys land on the same partition.

The sentence names the gain, the cost and the deciding requirement. A listener can check, or challenge, each part.

### Step 5. Say what you would watch, and what would change your mind

End with how you would know it is going wrong. For the counter, watch throttled writes and how stale the total gets.

Then name what would flip the decision. If counts had to be exact, like votes in an election, a cached total would not be enough. If only a few posts ever go viral, you could shard only those keys and keep one row for the rest.

Xu's advice fits here: never say your design is perfect, because "there is always something to improve upon". [@xu-framework]

### When the interviewer pushes back

A question is usually a hint, not an attack. Improving the design based on the interviewer's probes is part of the deep dive. [@hi-delivery] Hello Interview warns against talking over the interviewer, and says to leave room for questions. [@hi-delivery]

So update, do not defend. "Good point. In that case B breaks here, and I would change this part." Changing your mind for a stated reason is a strong signal. Repeating "it scales" is a weak one.

### Common failure modes

Defending instead of updating is one. Here are three more.

- **Listing technologies.** "Kafka and Redis" is not a design. Say what each one does, and for which requirement.
- **Going deep everywhere.** Two minutes on each of six parts shows no depth at all.
- **No numbers.** Without a rate and a limit, nobody can check your choice.

### How depth is judged at each level

Hello Interview describes what interviewers expect at each level. [@hi-levels]

- **Mid-level:** "the expectation for depth is limited". You lead the early parts, and the interviewer may lead more later.
- **Senior:** go deep in "around 2 places", using your real experience. You spot limits in your own design and discuss alternatives.
- **Staff:** depth "in multiple areas". You foresee problems and lead almost the whole interview.

Hello Interview also says senior candidates should find the weak spots themselves and lead the discussion. [@hi-delivery] A later lesson, level calibration, covers levels in more detail.

## Trade-offs

| Way to spend the deep-dive time | Shows depth? | Cost | Use it when |
|---|---|---|---|
| Go deep on 1–2 risky parts | Yes. | Other parts stay at high level. | Almost always. |
| Cover every part briefly | No. | Sounds like a list of tools. | Never as the main plan. |
| Go deep on a part you know well but that is low risk | Partly. | Proves skill, but not that the design works. | Only if the interviewer asks for it. |
| Propose a part, then check with the interviewer | Yes. | A few seconds. | Every time. |

**Simple rule:** one risky part, with numbers, two or three options, one trade-off sentence, and one thing you would watch.

## At work: how a senior uses this

A design review is a slow deep dive. The same shape works there.

- Ask "what is the riskiest part of this design, and how deep did we go on it?"
- Ask for the number behind each worry: the rate, the limit and the room left.
- Write the trade-off sentence in the "alternatives considered" section of the [design doc](/learn/design-docs-and-rfcs). Ubl says this section shows "why the selected solution is the best given the project goals". [@ubl]
- Turn "what I would watch" into a dashboard or an alert before launch.
- When a reviewer pushes back, repeat their point in your words. Then ask if it changes a requirement or only the design.
- Keep one review meeting to one or two deep dives. Park the rest as open questions.

## With AI

### What to ask the agent

> "Here is my high-level design and my requirements. Which one part is most likely to break first, and why? Give me the number that shows it."

> "Give me exactly three options for this part. Compare them only on these three requirements, in a table. Do not add new requirements."

> "Act as an interviewer. Probe my sharded counter design with one hard question at a time, and tell me if I defend instead of updating."

### What to check yourself

- Every limit the agent quotes matches the docs of the store you chose. Agents often mix up stores and units.
- The comparison uses your requirements, not general pros and cons.
- You can say the trade-off sentence without notes: gain, cost, deciding requirement.
- You know what would change your mind. If you cannot say it, you have not finished the deep dive.

## Check yourself

## Go deeper

Before this: [the system design interview method](/learn/system-design-interview-method) and [making architecture trade-offs](/learn/making-architecture-trade-offs). Also useful: [requirements and non-functional needs](/learn/requirements-and-non-functional-needs), [design docs and RFCs](/learn/design-docs-and-rfcs) and [latency numbers and estimation](/learn/latency-numbers-and-estimation). Next: level calibration.
