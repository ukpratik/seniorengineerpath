---
topic: sharding
subtitle: One machine cannot hold everything. Learn how to split data across many, and how to add a machine without moving it all.
status: published
reviewed_by: ai-factcheck
reviewed_on: 2026-10-21
illustrative_story: true
sources:
  - id: ddia
    title: "Martin Kleppmann, Designing Data-Intensive Applications (O'Reilly)"
    where: "1st edition (2017): chapter 6, Partitioning (key-range and hash partitioning, skew and hot spots, secondary indexes, rebalancing, request routing)."
  - id: karger
    title: "Karger, Lehman, Leighton, Levine, Lewin, Panigrahy, “Consistent Hashing and Random Trees: Distributed Caching Protocols for Relieving Hot Spots on the World Wide Web”, ACM STOC 1997"
    url: https://doi.org/10.1145/258533.258660
    note: The paper that named consistent hashing. The introduction explains why a normal hash moves almost every item when the number of caches changes.
  - id: dynamo
    title: "DeCandia et al., “Dynamo: Amazon's Highly Available Key-value Store”, SOSP 2007"
    url: https://www.allthingsdistributed.com/2007/10/amazons_dynamo.html
    note: Section 4.2 shows the hash ring and virtual nodes. Section 6.2 compares three ways to split the ring.
  - id: discord
    title: "Discord Engineering, “How Discord Stores Trillions of Messages” (2023)"
    url: https://discord.com/blog/how-discord-stores-trillions-of-messages
    note: A real hot partition story. One busy channel slowed the whole database cluster.
  - id: helix
    title: "Apache Helix documentation: Architecture"
    url: https://helix.apache.org/Architecture.html
    note: Helix stores cluster state, including which node holds each partition, in Apache ZooKeeper.
  - id: ddb-gsi
    title: "Amazon DynamoDB Developer Guide: Using Global Secondary Indexes"
    url: https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/GSI.html
    note: A global index is updated in the background, so a query on it can return data that is not up to date.
claims_checked:
  - claim: "With a classic hash, when the range changes, \"almost every item would be hashed to a new location.\""
    source: karger
    note: Exact quote from the paper's introduction (STOC 1997 copy). The paper also defines a consistent hash as one that "changes minimally" as its range changes. Arithmetic re-done - N to N+1 servers keeps 1/(N+1) of keys; 3 to 4 keeps 1/4, 4 to 5 keeps 1/5.
  - claim: Each key goes to the first server clockwise; a node joining or leaving only affects its neighbours; virtual nodes even out load, spread a dead node's load, and let bigger machines take more.
    source: dynamo
    note: Section 4.2 says exactly this. Section 6.2 says strategy 3 (Q/S tokens per node, equal-sized partitions) gave the best load balancing.
  - claim: Discord's hot partitions "frequently affected latency across our entire database cluster", and consistent hash routing by channel lets one instance query the database once for many users.
    source: discord
    note: Both quoted or closely paraphrased from the 2023 post. The big-announcement case is Discord's own hypothetical example, so the lesson now presents it as an example.
  - claim: DynamoDB global secondary indexes are updated asynchronously, usually "within a fraction of a second", and queries can return results that are not up to date.
    source: ddb-gsi
    note: Matches the "Data synchronization" section of the GSI docs word for word.
  - claim: Hot key fix with a two-digit random suffix (100 keys, reads must combine all 100); many more partitions than nodes; local index means scatter/gather.
    source: ddia
    note: Book not opened directly. Wording confirmed through reader notes and chapter summaries of DDIA chapter 6.
quiz:
  - q: You have 4 cache servers and pick one with hash(key) mod 4. You add a fifth server and switch to mod 5. About how many keys now map to a different server?
    options:
      - About 1 in 5.
      - About 4 in 5.
      - None. Old keys stay where they are.
    answer: 1
    why: With mod N, a key stays only if both remainders match. Going from 4 to 5 servers keeps about 1 key in 5, so about 4 in 5 move.
  - q: On a consistent hash ring, you add one server. Which keys move?
    options:
      - Every key, because the ring changes.
      - Only the keys in the arc the new server takes over, from its one neighbour.
      - Half the keys on every server.
    answer: 1
    why: Each key belongs to the next server clockwise. Only keys that now have the new server as their next server move.
  - q: Why does Dynamo give each server many virtual nodes on the ring?
    options:
      - To make each lookup faster.
      - To store more copies of each key.
      - To spread keys more evenly, and to spread a dead server's load over many servers.
    answer: 2
    why: With one point per server, the arcs are uneven. Many small arcs even out, and a bigger server can get more of them.
  - q: One celebrity's user ID gets 100 times more writes than anyone else's. What is a common fix?
    options:
      - Add more shards. The hash will spread it out.
      - Split that one key into many keys with a small random suffix, and read all of them back.
      - Use key-range partitioning instead of hash partitioning.
    answer: 1
    why: All writes for one key go to one shard, whatever the number of shards. Splitting the key is what spreads them over many shards.
  - q: Orders are sharded by order_id. Each shard has a local index on customer_id. What happens when you ask for all orders of one customer?
    options:
      - One shard answers, because the index knows where to look.
      - The query goes to every shard, and the results are merged.
      - The query fails. Local indexes cannot be queried.
    answer: 1
    why: A local index only covers its own shard. The customer's orders can be on any shard, so you must ask them all.
incidents: []
related: [replication, quorums-leaderless, dynamodb, cassandra, caching-stampedes]
---

## What broke

A food delivery app keeps menus in a cache cluster with 3 servers. To find the right server, the code runs `hash(key) mod 3`. It worked well for a year.

On a Friday evening, traffic grows and the cache servers run hot. An engineer adds a fourth server and changes the code to `mod 4`. The deploy takes two minutes.

Within seconds, the cache hit rate drops from 95% to about 25%. Almost every request now looks for its menu on a different server than before. The menus are still in the cache, but on the wrong machines. Every miss goes to the database, which was sized for 5% of the traffic. The database slows down, then times out. The app is down for 40 minutes at dinner time.

Adding capacity caused the outage. The fix they needed was not a bigger database. It was a better way to map keys to servers.

## The idea in simple words

Sharding means splitting one big dataset into smaller pieces, called shards, and putting them on different machines. You shard when one machine cannot hold all the data, or cannot handle all the reads and writes. Books often call the same thing partitioning. [@ddia]

Every record needs a rule that says which shard it lives on. That rule uses a shard key, such as the user ID or the order ID. You choose it once, and it is hard to change later.

Sharding is not the same as replication. Replication keeps full copies of the same data. Sharding gives each machine a different part. Real systems usually do both: each shard has a leader and some followers.

> [!key] **The key idea.** A good shard key spreads both data and traffic evenly. A good mapping from keys to machines lets you add a machine and move only a small part of the data.

## How it works

### Two ways to split the data

**Key-range partitioning.** Each shard owns a range of keys, like the volumes of a paper encyclopedia: A–F, G–M, and so on. Range queries are cheap, because nearby keys sit together. The risk is uneven load. If the key is a timestamp, every new write goes to the shard that holds "today". [@ddia]

**Hash partitioning.** You run the key through a hash function and use the hash to pick the shard. A good hash spreads keys evenly, even keys that look alike. The cost: range queries no longer work well, because nearby keys land on different shards. [@ddia]

### Why hash mod N breaks

The simple way to use a hash is `shard = hash(key) mod N`, where N is the number of servers. It spreads keys evenly. The problem comes when N changes.

Step through what happens when you add a fourth server. Use Next and Back, or the ← and → keys.

[[diagram:hash-mod-vs-ring]]

With mod N, a key stays on its server only if its old and new remainders match. Going from N to N + 1 servers, only about 1 key in N + 1 stays. The rest move, and many move between two old servers. The 1997 paper that named consistent hashing says it plainly: when the range of a classic hash changes, "almost every item would be hashed to a new location." [@karger]

Now try it yourself. Add a server and compare how many keys move with each method.

[[anim:hash-ring]]

### Consistent hashing and virtual nodes

Consistent hashing changes as little as possible when servers come and go. [@karger] Amazon's Dynamo database made the ring version famous. Servers and keys are hashed to points on a circle. Each key belongs to the first server you meet when you walk clockwise from the key. [@dynamo]

When a server joins or leaves, only its neighbours are affected. The others keep all their keys. [@dynamo] With N servers, a new one takes about 1/(N + 1) of the keys on average.

One point per server is not enough. Random points make some arcs much longer than others, so some servers get more keys. Dynamo's fix is virtual nodes: each server gets many points on the ring. Small arcs average out. When a server dies, its load spreads over many servers, not just one neighbour. And a bigger machine can get more points. [@dynamo]

### Hot keys: the celebrity problem

A perfect hash cannot fix one very busy key. All requests for one key go to one shard, however many shards you have. Think of a celebrity with millions of followers. A post from them can send huge traffic to a single shard. [@ddia]

Discord stores messages by channel and time window. Discord gives an example: a big announcement on a large server can send many users to the same channel at once. Discord wrote that a hot partition "frequently affected latency across our entire database cluster." [@discord] Part of their fix was routing by consistent hash. All requests for one channel go to the same service instance. It then sends one database query for many users who want the same data. [@discord]

For writes, a common fix is to split the hot key. Add a random two-digit suffix, and one key becomes 100 keys on many shards. The cost: every read must fetch all 100 and combine them. So do this only for the few keys that need it. [@ddia]

### Rebalancing and routing

Moving data between machines is called rebalancing. A common pattern is to create many more shards than machines from the start. When you add a machine, it takes whole shards from the others. Keys never change shards, only shards change machines. [@ddia] For example, you might start with 1,000 shards on 10 machines. Dynamo tried three ways to split its ring. The most even load came from this one: cut the ring into equal, fixed ranges, and give each node the same number of them. [@dynamo]

Some databases rebalance on their own. Kleppmann suggests it can be good to keep a human in the loop. Moving data uses a lot of disk and network. If a node is only slow, an automatic system may think it is dead and start moving data. That adds even more load. [@ddia]

Something must also know which machine holds which shard. There are three common choices. A client can ask any node, which forwards the request. A routing layer can sit in front of all nodes. Or the client can know the map itself. [@ddia] Some systems keep this map in a coordination service. Apache Helix, for example, stores which node holds each partition in ZooKeeper. [@helix]

### Secondary indexes

Say you shard orders by `order_id`, but you also need "all orders of customer 42". There are two kinds of index. [@ddia]

- **Local index.** Each shard indexes only its own rows. Writes are simple. But a query by customer must ask every shard and merge the results. This is called scatter/gather, and it is as slow as the slowest shard.
- **Global index.** The index itself is sharded by the indexed value, so one shard holds all of customer 42's entries. Reads are fast. But one write may need to update several shards, so the index is usually updated in the background.

DynamoDB's global secondary indexes work this way. The docs say updates usually reach the index "within a fraction of a second". Your app must still handle results that are not up to date. [@ddb-gsi]

## Trade-offs

| Choice | Even load? | Range queries? | Adding a machine |
|---|---|---|---|
| Key-range partitioning | No. Busy ranges, like "today", get hot. | Yes. Nearby keys sit together. | Split a range in two and move one half. |
| hash mod N | Yes, for normal keys. | No. | Most keys move. Avoid it for anything you will resize. |
| Hash ring with virtual nodes | Yes, more even with more virtual nodes. | No. | Only about 1/(N + 1) of the keys move. |
| Fixed number of shards (many per machine) | Yes, if shards are similar in size. | Depends on the hash or range inside. | Whole shards move. You must pick the count up front. |

**Rule of thumb:** don't shard until one machine and its replicas truly cannot cope. When you must, pick the shard key from your top queries, and never use plain `mod N` for a cluster that will grow.

## At work: how a senior uses this

- Before sharding, ask: **"Can a bigger machine, read replicas or a cache buy us another year?"** Sharding adds cost to every future feature.
- Choose the shard key from the queries you run most. If most requests are "for one user", shard by user ID, so one request touches one shard.
- List every query that will not include the shard key. Each one is a scatter/gather or a global index. Decide which, on purpose.
- Look for hot keys before launch: big customers, celebrity accounts, "today" timestamps. Plan how to split them.
- Watch load per shard, not just the cluster average. One shard at 95% CPU is an incident, even if the average is 40%.
- Rehearse adding a node in a test environment. Measure how much data moves and how long it takes.

## With AI

### What to ask the agent

> "Here are our 10 most frequent queries and the table schema. Suggest a shard key. For each query, say if it hits one shard or all shards."

> "Find every place in this code that picks a server or shard with `hash(...) % n`. Show what breaks when n changes."

> "Write a script that loads 1 million fake keys, maps them to 10 servers with a hash ring of 100 virtual nodes each, then adds an 11th. Print the share of keys that moved and the load per server."

### What to check yourself

- The shard key appears in your most common queries, and the agent did not pick it just because it is unique.
- The hash function is the same in every service and every language that routes requests. Two hashes mean two different maps.
- The test data has realistic skew, such as one customer with 10% of all rows. Uniform fake data hides hot keys.
- The rebalancing plan says what happens to reads and writes while data is moving.
- Any query without the shard key has a known cost, and someone agreed to it.

## Check yourself

## Go deeper

Before this: [Replication](/learn/database-replication). Each shard is usually replicated too. Next: [Quorums and leaderless replication](/learn/quorums-and-leaderless-replication), which builds on the Dynamo ring.
