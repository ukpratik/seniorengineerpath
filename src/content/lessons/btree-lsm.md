---
topic: btree-lsm
subtitle: Every database pays for keeping data sorted on disk. A B-tree pays when you write. An LSM-tree pays later, in the background and on reads.
status: published
reviewed_by: ai-factcheck
reviewed_on: 2026-10-21
illustrative_story: false
sources:
  - id: oneil
    title: "O'Neil, Cheng, Gawlick, O'Neil, “The Log-Structured Merge-Tree (LSM-Tree)”, Acta Informatica 33, 1996"
    url: https://dsf.berkeley.edu/cs286/papers/lsm-acta1996.pdf
    note: The paper that named the LSM-tree (free copy; the journal version is doi 10.1007/s002360050048). The abstract and section 1 explain why a B-tree index on a busy table doubles the disk I/O.
  - id: discord
    title: "Discord Engineering, “How Discord Stores Trillions of Messages” (2023)"
    url: https://discord.com/blog/how-discord-stores-trillions-of-messages
    note: A real Cassandra cluster that grew from 12 to 177 nodes, and the read and compaction pain that came with it.
  - id: pg-index-types
    title: "PostgreSQL documentation: Index Types"
    url: https://www.postgresql.org/docs/current/indexes-types.html
    note: CREATE INDEX makes a B-tree by default. B-trees handle equality and range queries.
  - id: pg-btree
    title: "PostgreSQL documentation: B-Tree Indexes (Implementation)"
    url: https://www.postgresql.org/docs/current/btree.html
    note: Section on B-tree structure. Leaf and internal pages, page splits that cascade upwards, and root splits.
  - id: pg-wal
    title: "PostgreSQL documentation: Write-Ahead Logging (WAL)"
    url: https://www.postgresql.org/docs/current/wal-intro.html
    note: Why changes are logged before data pages change, and why one log flush can commit many transactions.
  - id: pg-wal-config
    title: "PostgreSQL documentation: Write Ahead Log settings (full_page_writes)"
    url: https://www.postgresql.org/docs/current/runtime-config-wal.html
    note: With full_page_writes on (the default), the first change to a page after a checkpoint writes the whole page to the WAL.
  - id: leveldb-impl
    title: "LevelDB documentation: Implementation notes"
    url: https://github.com/google/leveldb/blob/main/doc/impl.md
    note: LevelDB's log, memtable, sorted tables and levels, and how compactions drop overwritten values.
  - id: innodb
    title: "MySQL 8.4 Reference Manual: The Physical Structure of an InnoDB Index"
    url: https://dev.mysql.com/doc/refman/8.4/en/innodb-physical-structure.html
    note: InnoDB indexes are B-trees with 16 KB pages by default. How full pages get with ordered and random inserts.
  - id: innodb-clustered
    title: "MySQL 8.4 Reference Manual: Clustered and Secondary Indexes"
    url: https://dev.mysql.com/doc/refman/8.4/en/innodb-index-types.html
    note: The InnoDB clustered (primary key) index stores the row data. Secondary indexes store the primary key.
  - id: cassandra-storage
    title: "Apache Cassandra documentation: Storage Engine"
    url: https://cassandra.apache.org/doc/latest/cassandra/architecture/storage-engine.html
    note: Commit log, memtables, flushes to SSTables that never change, and compaction.
  - id: cassandra-compaction
    title: "Apache Cassandra documentation: Compaction overview"
    url: https://cassandra.apache.org/doc/latest/cassandra/managing/operating/compaction/overview.html
    note: The compaction strategies (STCS, LCS, UCS, TWCS), which to use when, and tombstones.
  - id: cassandra-stcs
    title: "Apache Cassandra documentation: Size Tiered Compaction Strategy"
    url: https://cassandra.apache.org/doc/latest/cassandra/managing/operating/compaction/stcs.html
    note: When size-tiered compaction starts, and why it makes reads slower.
  - id: cassandra-lcs
    title: "Apache Cassandra documentation: Leveled Compaction Strategy"
    url: https://cassandra.apache.org/doc/latest/cassandra/managing/operating/compaction/lcs.html
    note: Levels that grow 10 times each, one SSTable per level per read, and the extra I/O it costs.
  - id: rocksdb-compaction
    title: "RocksDB wiki: Compaction"
    url: https://github.com/facebook/rocksdb/wiki/Compaction
    note: A short, clear statement of the read, write and space trade-off between leveled and tiered compaction.
  - id: rocksdb-leveled
    title: "RocksDB wiki: Leveled Compaction"
    url: https://github.com/facebook/rocksdb/wiki/Leveled-Compaction
    note: How files move from level to level, and why write amplification is often larger than 10.
  - id: rocksdb-bloom
    title: "RocksDB wiki: RocksDB Bloom Filter"
    url: https://github.com/facebook/rocksdb/wiki/RocksDB-Bloom-Filter
    note: What the filter answers, and a table of bits per key against false positive rate.
claims_checked:
  - claim: "In Postgres, B-tree leaf pages hold tuples that point to table rows, and usually over 99% of pages are leaf pages."
    source: pg-btree
    note: "QC1-08: \"Each leaf page contains tuples that point to table rows.\" \"Typically, over 99% of all pages are leaf pages.\""
  - claim: "The InnoDB clustered (primary key) index stores the row data itself."
    source: innodb-clustered
    note: "QC1-08: \"Each InnoDB table has a special index called the clustered index that stores row data.\""
  - claim: "A B-tree index by account ID on a History table, with keys arriving in random order, can double each transaction's I/O cost."
    source: oneil
    note: "QC1-09: abstract, \"will effectively double the I/O cost of the transaction to maintain an index such as this\"; Example 1.2 uses an Acct-ID||Timestamp index with randomly chosen Acct-ID values. The lesson now names this example instead of applying it to UUIDs."
  - claim: "Discord's Cassandra cluster grew from 12 nodes (2017) to 177 nodes (early 2022), often fell behind on compactions, and needed a \"gossip dance\" to catch up."
    source: discord
    note: "Matches the post: \"In 2017, we ran 12 Cassandra nodes\", \"177 nodes with trillions of messages\", \"prone to falling behind on compactions\", and the gossip dance description."
  - claim: "InnoDB pages are 16 KB by default; sequential inserts leave pages about 15/16 full, random inserts 1/2 to 15/16 full."
    source: innodb
    note: Exact figures from the MySQL 8.4 manual, Physical Structure of an InnoDB Index.
  - claim: "Leveled compaction keeps space amplification low at the cost of read and write amplification; tiered keeps write amplification low at the cost of read and space amplification."
    source: rocksdb-compaction
    note: "RocksDB wiki wording. Write amplification of leveled compaction \"often larger than 10\" is from rocksdb-leveled."
  - claim: "Cassandra LCS: levels 10x bigger, one SSTable per level per read, about 10% spare disk, a lot of disk I/O and CPU under heavy writes."
    source: cassandra-lcs
    note: All four points quoted on the LCS page.
  - claim: "Each change goes to the WAL before the data page, so a B-tree writes each change at least twice; full_page_writes adds a whole page to the WAL."
    source: pg-wal
    note: "The writer's DDIA attribution was from memory and was removed. Now sourced to pg-wal and pg-wal-config. The 256 TB example is now the lesson's own arithmetic (500^4 x 4 KB)."
  - claim: "About 10 bits per key gives about 1% false positives in a RocksDB Bloom filter, and \"not here\" is never wrong."
    source: rocksdb-bloom
    note: Table row is 9.9 bits per key for 1%.
  - claim: "The LSM-tree is most useful when you add entries to an index more often than you look them up."
    source: oneil
    note: "QA1-18 rewording, re-checked in the abstract: 'the LSM-tree is most useful in applications where index inserts are more common than finds that retrieve the entries.'"
  - claim: "UCS is the Unified Compaction Strategy, suggested for new workloads."
    source: cassandra-compaction
    note: "QC1-30 fix. Compaction overview lists 'Unified Compaction Strategy (UCS)' and says it 'is recommended for new workloads.'"
quiz:
  - q: Why is a write cheap in an LSM-tree?
    options:
      - It is added to the end of a log and put in a sorted table in memory. No file on disk is changed in place.
      - It skips the log, so nothing is written to disk.
      - It updates the right page on disk directly, like a B-tree.
    answer: 0
    why: The slow work, sorting into files on disk and merging them, happens later during flush and compaction.
  - q: An SSTable's Bloom filter says "key 42 is surely not here". What do you know?
    options:
      - Key 42 may still be in that file. Bloom filters are often wrong.
      - Key 42 is not in that file, so the read can skip it.
      - Key 42 was deleted.
    answer: 1
    why: A Bloom filter can wrongly say "may be here", but it never wrongly says "not here".
  - q: You insert random UUIDs as keys into a large B-tree index that no longer fits in memory. What happens?
    options:
      - Inserts stay fast, because new keys always go to the last page.
      - Each insert may need a random page read and write on disk, and pages end up partly empty.
      - The B-tree turns into an LSM-tree.
    answer: 1
    why: Random keys land on random pages. InnoDB says random inserts leave pages between 1/2 and 15/16 full.
  - q: Your Cassandra table is read-heavy and has many updates. Which compaction strategy do the Cassandra docs point to?
    options:
      - Size-tiered (STCS)
      - Leveled (LCS)
      - Time window (TWCS)
    answer: 1
    why: The docs say LCS is optimized for read-heavy workloads, or workloads with lots of updates and deletes.
  - q: What did Discord's Cassandra cluster often fall behind on?
    options:
      - Writing to the commit log.
      - Compaction. Reads got more expensive and busy nodes got slow.
      - Bloom filter memory.
    answer: 1
    why: Discord says they were "prone to falling behind on compactions", and stopped sending traffic to nodes to let them compact.
incidents: []
cases: [discord-trillions-messages]
related: [storage-internals, secondary-indexes, cassandra, postgres, choosing-database]
---

## What broke

In 2017, Discord stored its chat messages in a Cassandra cluster of 12 nodes. By early 2022, the same cluster had 177 nodes and trillions of messages. The on-call team was paged often, and latency was hard to predict. [@discord]

Writes were cheap. In Cassandra, a write is added to a commit log and to a table in memory. Reads cost more, because they may have to look in memory and in several files on disk. [@discord]

Those files must be merged in the background. This is called compaction. Discord's nodes often fell behind on it. Then reads got more expensive. When a node tried to compact, Discord saw latency that cascaded to other parts of the system. [@discord]

So the team did what they called a "gossip dance". They stopped sending traffic to one node, let it compact, brought it back, and repeated this until the backlog was empty. [@discord]

This is not a Cassandra bug. It is the price of the design that makes its writes cheap. In this lesson you will see where each storage engine pays, so you can choose the costs you can accept.

## The idea in simple words

A database keeps data sorted by key, so it can find a row without reading everything. There are two main ways to keep it sorted on disk.

A **B-tree** keeps one sorted tree made of fixed-size pages. It changes those pages in place. To insert a key, it finds the right page and writes that page again. Postgres creates B-tree indexes by default, and MySQL's InnoDB stores its indexes as B-trees. [@pg-index-types] [@innodb]

An **LSM-tree** never changes a file after writing it. It collects new writes in memory, writes them out as a new sorted file, and later merges files in the background. Cassandra works this way, and so do RocksDB and LevelDB. [@cassandra-storage] [@rocksdb-leveled] [@leveldb-impl]

> [!key] **The key idea.** A B-tree does the sorting work when you write, in place. An LSM-tree puts that work off and does it later in big batches. Batched work is cheaper per write, but you still pay for it: in background disk work and in slower reads.

The LSM-tree paper says it is most useful when you add entries to an index more often than you look them up. [@oneil] The real answer for your data comes from your own load test.

## How it works

Step through one write in an LSM-tree, then one insert in a B-tree. Use Next and Back, or the ← and → keys.

[[diagram:btree-lsm-write]]

### B-tree: pages and page splits

Disks are read in blocks, so a B-tree is built from pages. In InnoDB a page is 16 KB by default. [@innodb] In Postgres, leaf pages hold keys that point to the table rows. [@pg-btree] In InnoDB, the primary key index stores the rows themselves. [@innodb-clustered] Pages above the leaves hold keys that guide you down to the right leaf. In Postgres, usually over 99% of all pages are leaf pages. [@pg-btree]

A lookup starts at the root and goes down one page per level. Trees stay shallow. Here is a rough example. Say each page is 4 KB and points to 500 pages below it.

Then four levels of branching reach 500 × 500 × 500 × 500 = 62.5 billion pages. At 4 KB each, that is about 256 TB. So a handful of page reads can find any key.

When a new key does not fit in its leaf page, the page splits. Some keys move to a new page, and the parent page gets a pointer to it. If the parent is full too, it splits as well. When the root splits, the tree gets one level taller. [@pg-btree]

The order of your keys matters. InnoDB tries to leave 1/16 of each page free. If you insert keys in order, pages end up about 15/16 full. If you insert them in random order, pages are between 1/2 and 15/16 full. [@innodb]

Random keys, like random UUIDs, cause a second problem. Each insert lands on a different page. When the index is too big for memory, each insert may need a page read and a page write on disk. The LSM-tree paper shows the same effect for an index whose keys arrive in random order. Its example is an index by account ID on a fast-growing History table. A B-tree there can double the disk input and output (I/O) of each transaction. [@oneil]

### The write-ahead log

Changing pages in place is risky. A crash in the middle can leave a page half written. So the database first writes a record of the change to the write-ahead log (WAL) and flushes it to disk. Only after that may the data pages change. [@pg-wal]

After a crash, the database replays the log to redo the missing changes. The log is written in order, so flushing it costs much less than flushing data pages. One flush can even commit many small transactions at once. [@pg-wal] LSM engines keep a log too. Cassandra calls it the commit log. [@cassandra-storage]

### LSM-tree: memtable, SSTables, compaction

1. A write is added to the commit log on disk. [@cassandra-storage]
2. Then it goes into the memtable, a sorted table in memory. [@cassandra-storage]
3. When the memtable reaches a size limit, it is flushed to disk as an SSTable. This is a sorted file that is never written again. [@cassandra-storage]
4. Over time you get many SSTables. Compaction merges several of them into a new one. Then the old SSTables can be removed. [@cassandra-storage]

While it merges, compaction drops values that were overwritten. LevelDB's docs say this plainly. [@leveldb-impl]

A delete is also a write. Cassandra writes a marker called a tombstone. By default, compaction can remove it after 10 days. [@cassandra-compaction]

A read checks the memtable first, then the SSTables. With many SSTables, that can mean many files. A Bloom filter avoids most of this work. Each SSTable has a small filter that answers "this key may be here" or "this key is surely not here". It is sometimes wrong when it says "may be here". It is never wrong when it says "not here". [@rocksdb-bloom]

The filter uses memory. In RocksDB, about 10 bits per key gives about 1% false positives. [@rocksdb-bloom]

### Two ways to compact

**Size-tiered.** Wait until there are several SSTables of a similar size, then merge them. Cassandra starts when it has 4 by default. [@cassandra-stcs] This rewrites data less often than leveled compaction. [@rocksdb-compaction] But versions of one row can spread over many files, so reads get slower. During a big compaction, the old and new files must fit on disk at the same time. [@cassandra-stcs]

**Leveled.** Keep files in levels, and make each level about 10 times bigger than the one above. Inside one level, files do not overlap. So a read needs to look at only one SSTable per level. Leveled compaction needs less spare disk, about 10%. But it uses much more disk I/O and CPU when there are many writes. [@cassandra-lcs]

### Three kinds of amplification

Engineers compare storage engines with three numbers:

- **Write amplification:** how many bytes go to disk for each byte your app writes.
- **Read amplification:** how many places one read must look.
- **Space amplification:** how much disk is used, compared with the size of the live data.

You cannot make all three small at once. The RocksDB docs say it plainly. Leveled compaction keeps space low, but costs more reads and writes. Tiered compaction keeps writes low, but costs more reads and space. [@rocksdb-compaction] For leveled compaction, write amplification is often larger than 10. [@rocksdb-leveled]

B-trees have write amplification too. In Postgres, each change goes to the WAL first, and later to the data page. [@pg-wal] So each change reaches disk at least twice.

Also, the first change to a page after a checkpoint writes the whole page to the WAL, even if only a few bytes changed. This is on by default. [@pg-wal-config]

## Trade-offs

| Engine | Writes | Reads | Disk space | Watch out for |
|---|---|---|---|---|
| B-tree (Postgres, InnoDB) | WAL plus a page write in place. Slows down when the index no longer fits in memory. | Fast and steady: one walk from root to leaf. | Pages are partly empty, more so with random keys. | Random keys, page splits, big indexes. |
| LSM, size-tiered | Cheapest. Data is rewritten less often. | Slower. One key can be spread over many SSTables. | Needs free disk for old and new files during a compaction. | Compaction backlog. Slow reads after a burst of writes. |
| LSM, leveled | More rewriting. Often over 10x in RocksDB. | Better. One SSTable per level. | Low. About 10% spare in Cassandra. | Heavy compaction I/O and CPU under many writes. |

**Simple rule:** start with a B-tree database like Postgres or MySQL. Choose an LSM store when writes clearly dominate, for example events, logs, metrics or chat messages, and plan for compaction from day one.

## At work: how a senior uses this

- Before you pick a store, write down the read/write ratio, and whether reads are by one key or by a range. That decides more than any benchmark blog.
- On an LSM store, put pending compactions and SSTables per read on a dashboard. Alert when they keep growing. A backlog that keeps growing is a problem, even before anything fails.
- Keep enough free disk for compaction to write new files before it deletes the old ones. Put this in the capacity plan.
- Pick the compaction strategy for the data. The Cassandra docs suggest leveled for read-heavy tables or tables with many updates and deletes. They suggest time window for time-series data with a TTL, and UCS (Unified Compaction Strategy) for new workloads. [@cassandra-compaction]
- For B-tree tables with heavy inserts, prefer keys that grow over time, like time-ordered IDs. Random keys spread writes over many pages. [@innodb]
- Treat deletes on an LSM store as writes. A mass delete creates many tombstones, and they stay until compaction removes them.
- Load test long enough. A B-tree slows down most when the index outgrows memory. A compaction backlog shows only after hours of writes.

## With AI

### What to ask the agent

> "Here is our table schema, our 10 most common queries and our write rate. Tell me whether a B-tree database or an LSM store fits better, and why. For each, say what it costs us in disk space and background work."

> "Write a load test that inserts 50 million rows into Postgres twice: once with a random UUID primary key and once with a time-ordered key. Report the insert rate over time and the final index size."

> "Our Cassandra table has slow reads. Here is the schema and the compaction settings. Explain how many SSTables one read may touch, and suggest a compaction strategy with reasons and risks."

### What to check yourself

- Any number the agent gives, like "LSM is 10x faster", comes from a source or from a test you ran.
- The load test runs long enough for the index to outgrow memory and for compaction to start.
- The test machine has the same disk type and memory size as production.
- Free disk for compaction is in the capacity plan, not left to chance.
- A change of compaction strategy is tried on one node or on a copy first.
- Bloom filter memory is counted when you size the machines.

## Check yourself

## Go deeper

Next: [Secondary, composite and special indexes](/learn/secondary-and-composite-indexes). It builds on the B-tree pages you saw here.
