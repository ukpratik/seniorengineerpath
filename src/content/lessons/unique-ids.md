---
topic: unique-ids
subtitle: Every row, request and event needs a name that no other has. Learn the main ways to create IDs, and what each one costs your database, your clocks and your security.
status: published
reviewed_by: ai-factcheck
reviewed_on: 2026-10-22
illustrative_story: true
sources:
  - id: rfc9562
    title: "K. Davis, B. Peabody, P. Leach, RFC 9562, “Universally Unique IDentifiers (UUIDs)”, IETF, May 2024"
    url: https://www.rfc-editor.org/rfc/rfc9562
    note: The UUID standard. Section 5.4 is UUID v4 (random), section 5.7 is UUID v7 (time-ordered), section 6.2 covers clocks and counters, section 8 covers security.
  - id: snowflake-blog
    title: "Twitter Engineering, “Announcing Snowflake”, 1 June 2010 (archived copy on archive.org, because blog.x.com blocks automated readers)"
    url: https://web.archive.org/web/20170609203244/https://blog.twitter.com/engineering/en_us/a/2010/announcing-snowflake.html
    note: Why Twitter needed a new ID service when it moved to Cassandra and sharded MySQL, and the three parts of each ID.
  - id: snowflake-repo
    title: "Twitter, snowflake repository README (snowflake-2010 branch), GitHub"
    url: https://github.com/twitter-archive/snowflake/tree/snowflake-2010
    note: The requirements, the 41 + 10 + 12 bit layout, and how it handles a clock that runs backwards.
  - id: ulid-spec
    title: "ULID specification, GitHub (ulid/spec)"
    url: https://github.com/ulid/spec
    note: The ULID layout, its text encoding, and the optional monotonic rule for IDs made in the same millisecond.
  - id: pg-sequence
    title: "PostgreSQL documentation: Sequence Manipulation Functions"
    url: https://www.postgresql.org/docs/current/functions-sequence.html
    note: Why values from nextval are not given back when a transaction aborts, so sequences can have gaps.
  - id: pg-uuid
    title: "PostgreSQL 18 documentation: UUID Functions"
    url: https://www.postgresql.org/docs/current/functions-uuid.html
    note: The built-in gen_random_uuid, uuidv4 and uuidv7 functions.
  - id: mysql-autoinc
    title: "MySQL 8.4 Reference Manual: Replication Source Options and Variables (auto_increment_increment, auto_increment_offset)"
    url: https://dev.mysql.com/doc/refman/8.4/en/replication-options-source.html
    note: How the two settings control the step and the start of AUTO_INCREMENT values, with examples.
  - id: flickr
    title: "Flickr Engineering, “Ticket Servers: Distributed Unique Primary Keys on the Cheap” (February 2010)"
    url: https://code.flickr.net/2010/02/08/ticket-servers-distributed-unique-primary-keys-on-the-cheap/
    note: Two MySQL servers that hand out IDs, one odd and one even.
  - id: innodb
    title: "MySQL 8.4 Reference Manual: The Physical Structure of an InnoDB Index"
    url: https://dev.mysql.com/doc/refman/8.4/en/innodb-physical-structure.html
    note: How full index pages get when keys arrive in order, and when they arrive in random order.
  - id: owasp-authz
    title: "OWASP Cheat Sheet Series, Authorization Cheat Sheet"
    url: https://cheatsheetseries.owasp.org/cheatsheets/Authorization_Cheat_Sheet.html
    note: Predictable IDs in URLs, insecure direct object references, and why random IDs alone are not enough.
claims_checked:
  - claim: UUID v4 has 122 random bits; the other 6 bits are the version and variant.
    source: rfc9562
    note: Section 5.4 says an implementation may generate exactly the 122 bits of random_a, random_b and random_c and add the 4-bit version and 2-bit variant.
  - claim: UUID v7 is a 48-bit Unix millisecond timestamp, 4-bit version, 12 bits rand_a, 2-bit variant, 62 bits rand_b; implementations should use v7 instead of v1 and v6 if possible.
    source: rfc9562
    note: Section 5.7 and Figure 11. The random fields may instead hold a counter or sub-millisecond fraction; the lesson now says so.
  - claim: UUID v4 has "poor database-index locality"; time-ordered UUIDs give better locality, and the difference "can be one order of magnitude or more".
    source: rfc9562
    note: Section 2.1 item 1 and section 6.11, quoted exactly.
  - claim: RFC 9562 gives no collision numbers and says true global uniqueness cannot be guaranteed without shared knowledge.
    source: rfc9562
    note: Sections 6.7 and 6.8. The earlier quote "with very high probability" was from the name-based UUID rules, not v4, so it was replaced. Birthday numbers recomputed - 2^122 = 5.3 x 10^36; 2.3 x 10^18 IDs gives 1 - e^(-n^2/2N) = 39%; 50% needs about 2.7 x 10^18. Labelled as our own arithmetic.
  - claim: UUIDs must not be assumed hard to guess or used as security capabilities.
    source: rfc9562
    note: Section 8, SHOULD NOT / MUST NOT.
  - claim: Twitter was replacing MySQL systems with Cassandra or sharded MySQL, needed tens of thousands of IDs per second, chose an uncoordinated approach, and made "roughly sortable" 64-bit IDs from timestamp, worker number and sequence; worker numbers chosen at startup via ZooKeeper (or a config file).
    source: snowflake-blog
    note: Archived copy of the 1 June 2010 post read in full. Config-file override added to the lesson.
  - claim: Snowflake layout is 41-bit millisecond time with a custom epoch (69 years), 10-bit machine ID (1,024 machines), 12-bit sequence (rolls over every 4,096 per machine); IDs are k-sorted, promising 1 s and aiming for tens of ms; it refuses to generate IDs when the clock runs backwards until time passes the last ID.
    source: snowflake-repo
    note: README on the snowflake-2010 branch. Diagram arithmetic re-done - 1000 x 2^22 + 7 x 2^12 = 4,194,332,672; 1001 gives 4,198,526,976.
  - claim: ULID is a 48-bit ms timestamp plus 80 random bits, 26 characters, 1.21e+24 IDs per ms; same-millisecond order is not guaranteed unless the optional monotonic mode increments the random part, which fails on overflow.
    source: ulid-spec
    note: The lesson had presented the increment as the default; now marked as optional.
  - claim: A value from nextval is not reused if the transaction aborts, so sequences have gaps and cannot give gapless numbers.
    source: pg-sequence
    note: Matches the Caution box on the page.
  - claim: PostgreSQL 18 has gen_random_uuid(), uuidv4() and uuidv7().
    source: pg-uuid
    note: Table 9.45 of the version 18 docs.
  - claim: auto_increment_increment is the interval between values and auto_increment_offset is the starting point.
    source: mysql-autoinc
    note: Wording matches the variable descriptions.
  - claim: Flickr ran two ticket servers with increment 2 and offsets 1 and 2 (odd and even), and the two sides drift out of sync.
    source: flickr
    note: SPOFs section of the 8 February 2010 post. The table row for ordering now cites the drift.
  - claim: InnoDB pages are about 15/16 full with sequential inserts and 1/2 to 15/16 full with random inserts.
    source: innodb
    note: Exact figures from the page (about the clustered index).
  - claim: OWASP's IDOR example uses a "fairly predictable, perhaps sequential" ID, and randomizing IDs is "generally not sufficient by itself".
    source: owasp-authz
    note: Both quotes match the cheat sheet text.
quiz:
  - q: You split an orders table over two MySQL shards. Both still use AUTO_INCREMENT with default settings. What happens?
    options:
      - Nothing. MySQL makes IDs unique across servers.
      - Both shards start at 1, so two different orders get the same ID.
      - MySQL refuses to insert on the second shard.
    answer: 1
    why: Each server counts on its own. You must give them different offsets, or generate IDs outside the databases.
  - q: Why can random UUID v4 primary keys slow down inserts into a large B-tree index?
    options:
      - Each new key lands on a random page, so the index loses locality and pages end up less full.
      - UUIDs cannot be stored in a B-tree.
      - Random keys force the database to rebuild the index after each insert.
    answer: 0
    why: RFC 9562 says UUID v4 has poor database-index locality. InnoDB says random inserts leave pages 1/2 to 15/16 full.
  - q: In a Snowflake-style ID, why do IDs from one machine sort by time?
    options:
      - Because the machine number is in the top bits.
      - Because the timestamp is in the top bits, so a later millisecond always gives a bigger number.
      - Because a central server hands them out in order.
    answer: 1
    why: The time sits above the machine and the sequence bits. Any later millisecond beats any value in the lower bits.
  - q: Your order page URL is /orders/10452, using an auto-increment ID. What is the main risk?
    options:
      - The URL is too long.
      - Someone can try 10451, 10453 and so on. If the server does not check ownership, they see other people's orders.
      - The browser cannot cache the page.
    answer: 1
    why: Sequential IDs are easy to guess. The real fix is an authorization check on every request. Random IDs only make guessing harder.
  - q: A Snowflake-style generator sees that the clock moved back by 2 ms. What should it do?
    options:
      - Keep going with the new time.
      - Refuse to create IDs until the clock passes the last time it used.
      - Pick a random machine number.
    answer: 1
    why: An older time could repeat earlier IDs or break the order. Twitter's Snowflake refuses until the time is after the last one it used.
incidents: []
related: [clocks-ordering, btree-lsm, sharding, idempotency-retries, authn-authz]
---

## What broke

This story is made up, but each part of it is common.

A shop app keeps orders in one PostgreSQL table. The `id` column is an auto-increment number. It has worked for three years.

The table grows too big, so the team splits it into two shards. Each shard is a copy of the old schema, and each still counts its own IDs. On day one, both shards create order 1,000,001.

Support searches for order 1,000,001 and finds two. A refund goes to the wrong customer. Payment webhooks carry only the order ID, so some payments are marked on the wrong order.

There is a second problem. The order page URL is `/orders/1000001`. A curious customer changes the number by one and sees a stranger's address. The endpoint did not check who owns the order.

Both bugs come from the same choice: an ID that was only unique inside one database, and easy to guess.

## The idea in simple words

An ID names one thing, forever. You use IDs as primary keys. You use them as idempotency keys, so a retried request is not done twice. You often sort by them, and you may use them as shard keys.

A good ID scheme answers four questions. Can many machines create IDs without asking each other? Do IDs sort by time? How big are they? Can outsiders guess them?

> [!key] **The key idea.** Pick the ID scheme for the scale you will have, not the scale you have today. Changing it later means changing every table, API and URL that holds an ID.

## How it works

### Auto-increment and sequences

The database hands out 1, 2, 3 and so on. IDs are small, ordered and easy to read. In PostgreSQL, a sequence never takes a number back, even when the transaction fails. So you will see gaps, and you cannot use a sequence for "gapless" numbers like invoice numbers. [@pg-sequence]

The limit is that one database owns the counter. With two databases, you must split the number space. MySQL has two settings for this. `auto_increment_increment` is the step between values, and `auto_increment_offset` is the start. [@mysql-autoinc] Flickr ran two MySQL ticket servers this way. Both used a step of 2. One started at 1 and gave odd IDs, the other started at 2 and gave even IDs. [@flickr] This works, but adding a third server means planning the numbers again.

### UUID v4: random

A UUID is 128 bits. Version 4 fills 122 of them with random bits. The other 6 bits mark the version and variant. [@rfc9562] Any machine can make one, with no coordination.

Random keys have a cost in a [B-tree](/learn/btree-vs-lsm-tree) index. RFC 9562 says UUID v4 has "poor database-index locality". [@rfc9562] Each new key lands on a random page. InnoDB reports that keys inserted in order leave index pages about 15/16 full. Keys inserted in random order leave them between 1/2 and 15/16 full. [@innodb]

### UUID v7: time first

UUID v7 keeps the UUID format but puts time first. The first 48 bits are the Unix time in milliseconds. Then come the version, 12 random bits, the variant, and 62 more random bits. A generator may put a counter in some of the random bits, to keep order within one millisecond. [@rfc9562]

So new IDs sort after older ones, and new values sit near each other in the index. The RFC says this gives better database-index locality. It says the difference from random inserts "can be one order of magnitude or more". [@rfc9562] It also says implementations should use v7 instead of the older time-based versions, v1 and v6, if possible. [@rfc9562]

PostgreSQL 18 has a built-in `uuidv7()` function, next to `gen_random_uuid()` for v4. [@pg-uuid]

### Snowflake: 64 bits, built from parts

In 2010, Twitter was replacing many MySQL systems with Cassandra or sharded MySQL. Neither had a built-in way to create unique IDs. Twitter needed tens of thousands of IDs per second, from a highly available service. So it chose an approach with no coordination. [@snowflake-blog]

Each ID has three parts: a timestamp, a worker number and a sequence number. [@snowflake-blog] The README gives the sizes. The time takes 41 bits, in milliseconds from a custom epoch, which lasts 69 years. The machine ID takes 10 bits, for up to 1,024 machines. The sequence takes 12 bits, so it rolls over every 4,096 IDs per machine. [@snowflake-repo] That is 63 bits, so the ID fits in a signed 64-bit integer. Worker numbers were chosen at startup through ZooKeeper, or set in a config file. [@snowflake-blog]

Step through how one ID is built.

[[diagram:snowflake-id-parts]]

IDs from one machine sort by time, because time sits in the top bits. Across machines, the order is only rough. Twitter called its IDs "roughly sortable". [@snowflake-blog] The README says they are "k-sorted" within a bound: it promised 1 second and aimed for tens of milliseconds. [@snowflake-repo]

### ULID, in one paragraph

A ULID is 128 bits, like a UUID: a 48-bit millisecond timestamp, then 80 random bits. It is written as 26 characters, not the 36 of a UUID, and the text sorts in time order. The spec says it allows 1.21e+24 unique IDs per millisecond. Within one millisecond, order is not guaranteed. An optional monotonic mode adds one to the random part instead. If that overflows, generation fails. [@ulid-spec] In our view, UUID v7 now gives you most of the same benefits in the standard UUID format.

### Clocks can break your IDs

Any time-based ID depends on the [wall clock](/learn/time-clocks-and-ordering), and wall clocks can jump back. RFC 9562 lists clock rollbacks, leap seconds and counter rollovers as causes of out-of-order IDs. It says generators should check that each new ID is bigger than the last. [@rfc9562] Snowflake protects itself. If the clock goes back, it refuses to create IDs until the time passes the last time it used. [@snowflake-repo] Its README suggests running NTP in a mode that never moves the clock back. [@snowflake-repo]

### Do not hand out guessable IDs

Sequential IDs in URLs invite people to try the next number. OWASP's example of this bug uses a "fairly predictable, perhaps sequential" ID. It is a form of insecure direct object reference. [@owasp-authz] Random IDs make guessing harder, but OWASP says that is "generally not sufficient by itself". [@owasp-authz] The fix is an access check on every request. See [authentication and authorization](/learn/authentication-and-authorization). RFC 9562 also warns: do not assume UUIDs are hard to guess, and never use one as a secret that grants access. [@rfc9562]

### How likely is a clash?

For UUID v4, think of 122 random bits. That is about 5.3 × 10^36 possible values. By our own arithmetic, with the standard birthday estimate, you would need about 2.3 × 10^18 IDs before the chance of even one clash reaches about 40%. RFC 9562 itself gives no numbers. It says true global uniqueness cannot be guaranteed without shared knowledge. It asks you to weigh what a clash would cost your application. [@rfc9562] Our view: in practice, a clash is far more likely to come from a bug, like a copied seed or a reused machine number, than from bad luck.

## Trade-offs

| Scheme | Needs coordination? | Sorts by time? | Size | Index-friendly? |
|---|---|---|---|---|
| Auto-increment | Yes. One database owns the counter. | Yes. | Small: one integer column. | Yes. Inserts go to the end. |
| Ticket servers (odd and even) | A little: you plan the offsets. [@flickr] | Only roughly. The two servers drift apart. [@flickr] | 64 bits. [@flickr] | Yes. |
| UUID v4 | No. | No. | 128 bits. [@rfc9562] | No. Poor locality. [@rfc9562] |
| UUID v7 | No. | Yes, by millisecond. [@rfc9562] | 128 bits. | Yes. [@rfc9562] |
| Snowflake-style | Only to assign machine IDs. [@snowflake-blog] | Yes, roughly. [@snowflake-repo] | 64 bits. [@snowflake-repo] | Yes. |
| ULID | No. | Yes, by millisecond. [@ulid-spec] | 128 bits, 26 characters. [@ulid-spec] | Yes. |

**Simple rule:** one database and private IDs? Auto-increment is fine. Many writers, or IDs created on clients? Use UUID v7. Need 64-bit IDs at very high rates? Use a Snowflake-style generator, and treat machine IDs and clocks as part of the design.

## At work: how a senior uses this

- Ask early: **"Will this table ever live on more than one database?"** If yes, do not depend on auto-increment.
- Separate the internal key from the public ID if you must. A table can keep a small integer key and also store a random public ID for URLs.
- Never let an ID replace an authorization check. Test that user A cannot read user B's object by changing the ID.
- For Snowflake-style IDs, write down how machine IDs are assigned and freed. Two generators with the same machine ID can create the same IDs.
- Alert when an ID generator refuses to work because of the clock. It means NTP or the host has a problem.
- Use 64-bit integer columns, or the database's UUID type. Do not store UUIDs as long strings without a reason.
- Never promise gapless IDs from a sequence. If finance needs gapless numbers, design that separately.

## With AI

### What to ask the agent

> "List every table in this schema with its primary key type. For each one, say whether it would still be unique if we split the table across two databases."

> "Find every API route that takes an ID from the URL or body. For each, show where the code checks that the caller may access that object. Flag any route without such a check."

> "Write a test for our ID generator. Make 1 million IDs on 8 threads, check that none repeat, and check that IDs from one thread always go up. Then move the fake clock back by 5 ms and show what happens."

### What to check yourself

- The UUID version the agent's code produces is the one you want. Check the library docs, and do not assume.
- Any bit layout adds up to 64 or fewer bits, and the time part lasts long enough from your chosen epoch.
- The agent did not treat random IDs as security. Every object access still has an owner check.
- Generated IDs are not used as the only sort key across machines when exact order matters.
- Database functions, like `uuidv7()`, exist in the version you run in production.

## Check yourself

## Go deeper

Before this: [time, clocks and ordering](/learn/time-clocks-and-ordering). Related: [B-tree vs LSM-tree](/learn/btree-vs-lsm-tree) for why key order matters, [sharding](/learn/sharding-and-consistent-hashing) for shard keys, and [idempotency and retries](/learn/idempotency-retries-timeouts) for idempotency keys.
