---
topic: search-systems
subtitle: A search engine does not scan your text. It looks words up in an inverted index, ranks the matches, and must be kept in step with your database.
status: published
illustrative_story: true
sources:
  - id: pg-fts-intro
    title: "PostgreSQL documentation: Full Text Search, Introduction"
    url: https://www.postgresql.org/docs/current/textsearch-intro.html
    note: Why LIKE is not enough, and how documents become normalized lexemes (tsvector) and queries become tsquery.
  - id: pg-fts-indexes
    title: "PostgreSQL documentation: Preferred Index Types for Text Search"
    url: https://www.postgresql.org/docs/current/textsearch-indexes.html
    note: GIN is an inverted index and the preferred type for text search. GiST is lossy.
  - id: pg-fts-controls
    title: "PostgreSQL documentation: Controlling Text Search (ranking)"
    url: https://www.postgresql.org/docs/current/textsearch-controls.html
    note: ts_rank and ts_rank_cd, and why ranking many matches can be slow.
  - id: es-analysis
    title: "Elastic Docs, “Text analysis”"
    url: https://www.elastic.co/docs/manage-data/data-store/text-analysis
    note: Tokenization, normalization (lowercase, stemming, synonyms) and analyzers, with examples.
  - id: lucene-index
    title: "Apache Lucene 9.12 API: package org.apache.lucene.index"
    url: https://lucene.apache.org/core/9_12_0/core/org/apache/lucene/index/package-summary.html
    note: Lucene's own description of the inverted index as fields, terms and postings.
  - id: lucene-tfidf
    title: "Apache Lucene 9.12 API: TFIDFSimilarity"
    url: https://lucene.apache.org/core/9_12_0/core/org/apache/lucene/search/similarities/TFIDFSimilarity.html
    note: The ideas behind term frequency, inverse document frequency and length norms.
  - id: lucene-bm25
    title: "Apache Lucene 9.12 API: BM25Similarity"
    url: https://lucene.apache.org/core/9_12_0/core/org/apache/lucene/search/similarities/BM25Similarity.html
    note: Lucene's BM25 class, its origin in Okapi at TREC-3, and its defaults k1 = 1.2 and b = 0.75.
  - id: es-similarity
    title: "Elastic Docs, “Similarity settings”"
    url: https://www.elastic.co/docs/reference/elasticsearch/index-settings/similarity
    note: BM25 is the default similarity in Elasticsearch, and what k1 and b control.
  - id: es-nrt
    title: "Elastic Docs, “Near real-time search”"
    url: https://www.elastic.co/docs/manage-data/data-store/near-real-time-search
    note: Segments, refresh, and the default one-second refresh for indices that are being searched.
  - id: es-refresh
    title: "Elastic Docs, “refresh parameter”"
    url: https://www.elastic.co/docs/reference/elasticsearch/rest-apis/refresh-parameter
    note: refresh=false, true and wait_for, and why forcing a refresh on each write is costly.
  - id: es-shards
    title: "Elastic Docs, “Clusters, nodes, and shards”"
    url: https://www.elastic.co/docs/deploy-manage/distributed-architecture/clusters-nodes-shards
    note: Primary and replica shards, and why the number of primary shards is fixed.
  - id: es-shard-size
    title: "Elastic Docs, “Size your shards”"
    url: https://www.elastic.co/docs/deploy-manage/production-guidance/optimize-performance/size-shards
    note: Shard size targets, one search thread per shard, and the cost of too many shards.
reviewed_by: ai-factcheck
reviewed_on: 2026-10-21
claims_checked:
  - claim: LIKE and ILIKE have no linguistic support, no ranking, and tend to be slow because there is no index support, so they process all documents.
    source: pg-fts-intro
    note: Matches the three bullets in the Postgres intro; wording softened to "tends to be slow".
  - claim: GIN indexes are the preferred text search index type; as inverted indexes they hold an entry per lexeme with a compressed list of locations.
    source: pg-fts-indexes
    note: Verified against current docs.
  - claim: BM25 is the default similarity in Elasticsearch, with k1 = 1.2 (saturation) and b = 0.75 (length normalization).
    source: es-similarity
    note: Same defaults in Lucene 9.12 BM25Similarity, which cites Okapi at TREC-3 (1994). No claim made about Lucene's own default.
  - claim: Elasticsearch refreshes every second by default, only on indices that got a search request in the last 30 seconds.
    source: es-nrt
    note: Verified; refresh=false is the recommended default and refresh=true creates tiny segments.
  - claim: The number of primary shards is fixed at index creation; replicas can be changed at any time; each shard is a self-contained Lucene index.
    source: es-shards
    note: Verified. Shard size 10–50 GB and one CPU thread per shard verified in es-shard-size.
quiz:
  - q: Why is `WHERE title LIKE '%shoe%'` slow on a big table?
    options:
      - LIKE is only slow in MySQL.
      - A normal B-tree index cannot help with a leading %, so every row must be checked.
      - The database caches the wrong rows.
    answer: 1
    why: The Postgres docs say LIKE searches have no index support, so they must process all documents for every search.
  - q: What does an inverted index store?
    options:
      - For each document, the list of its words.
      - For each term, the list of documents that contain it.
      - The documents sorted by date.
    answer: 1
    why: An inverted index maps a term to its postings, the documents that contain the term.
  - q: In BM25 and TF-IDF scoring, which word adds the most to a document's score?
    options:
      - A rare word that appears in only a few documents.
      - A common word like "the".
      - The longest word in the query.
    answer: 0
    why: Inverse document frequency gives rare terms a bigger weight, because they say more about which documents match.
  - q: You save a product, then search for it at once, and it is not found. What is the most likely reason in Elasticsearch?
    options:
      - The document was lost.
      - The index has not refreshed yet, so the new segment is not open for search.
      - Replicas do not hold new documents.
    answer: 1
    why: Search is near real-time. New documents become searchable after a refresh, by default within about a second for indices that are being searched.
  - q: Your service writes to Postgres and then to Elasticsearch. The second write sometimes fails. What is a safer design?
    options:
      - Retry the Elasticsearch write in the request until it works.
      - Write an outbox row in the same database transaction, or use CDC, and let a worker update the index.
      - Write to Elasticsearch first, then Postgres.
    answer: 1
    why: The database stays the source of truth. A worker reads changes from it and updates the index, so no change is lost.
incidents: []
related: [secondary-indexes, sagas-outbox-cdc, product-search, sharding, postgres]
---

## What broke

This story is made up, but it is common. A small shop has a search box. The code runs `SELECT * FROM products WHERE title ILIKE '%shoe%'`. With 5,000 products, it answers fast.

Two years later there are 3 million products. Each search now reads every row, because no normal index can help with a pattern that starts with `%`. During a sale, searches use most of the database CPU, and checkout slows down too.

The results are also poor. A search for "running shoes" does not find "Shoe for runners". The results come back in no useful order.

So the team adds Elasticsearch. The code saves each product to Postgres and then to Elasticsearch. One night the Elasticsearch call times out for some saves. The prices in search no longer match the real prices, and nobody notices for a week.

Both problems have known fixes. This lesson explains them.

## The idea in simple words

The Postgres docs list three problems with `LIKE` search. It has no language support, so `satisfies` does not match `satisfy`. It does not rank results. And it tends to be slow, because there is no index support, so it must process every document for every search. [@pg-fts-intro]

A search engine fixes this in two steps.

1. **When you add a document**, it splits the text into words, cleans each word, and records which documents contain it.
2. **When you search**, it cleans the query words the same way, looks them up, and ranks the matching documents.

The record from step 1 is an inverted index. A book index is a good picture: "replication, pages 40, 112, 305". You do not read the whole book to find a word.

> [!key] **The key idea.** Search is fast because the work happens when you write. Each document is broken into terms once, and each term points to its documents. A query reads only a few short lists.

## How it works

Step through three product titles, the index built from them, and one query. Use Next and Back, or the ← and → keys.

[[diagram:search-inverted-index]]

### Step 1: analysis (tokenize and normalize)

Elasticsearch calls this text analysis. Tokenization breaks text into small pieces called tokens. Normalization makes near matches equal. It can lowercase `Quick` to `quick`, stem `foxes` to `fox`, and index the synonyms `jump` and `leap` as one word. [@es-analysis]

Postgres does the same work with dictionaries. Its normalized words are called lexemes. Very common words, like "the", are stop words and are usually dropped. [@pg-fts-intro]

Elasticsearch can apply the same rules to the query text. [@es-analysis] Always do this. If the index holds `shoe` but the query asks for `Shoes`, nothing matches.

### Step 2: the inverted index

Each Elasticsearch shard is a Lucene index. [@es-shards] Lucene's docs call the inverted index its core. It is made of postings, which let you look up the documents for a given term. [@lucene-index] It can also store the positions of a term inside each document. [@lucene-index] Positions make phrase search possible, like "red shoes" as two words side by side.

Postgres has an inverted index too. Its GIN index has one entry for each lexeme, with a compressed list of where it appears. The docs call GIN the preferred index type for text search. [@pg-fts-indexes]

### Step 3: ranking

A query for "red shoes" may match 40,000 products. Which come first? The engine gives each match a score.

The classic idea is TF-IDF. Lucene's docs explain its parts. A document that contains a term more often scores higher. Rarer terms add more to the score. And a match in a shorter field counts for more. [@lucene-tfidf]

So "jacket" in 10 products tells you more than "red" in 50,000 products.

BM25 is a newer formula built on the same TF-IDF ideas, with built-in term frequency normalization. [@es-similarity] Lucene's BM25 class traces it to the Okapi system at TREC-3, in 1994. [@lucene-bm25] It is the default similarity in Elasticsearch. [@es-similarity] Its two main settings are:

- **k1** (default 1.2) controls saturation: how fast extra repeats of a word stop adding score.
- **b** (default 0.75) controls how much document length affects the score. [@es-similarity]

You rarely change these. Most relevance work is in the analyzer, the fields you search and the boosts you add.

### Near real-time: refresh

New documents are not searchable the moment you write them. Elasticsearch collects them and, on a refresh, writes them to a new segment and opens it for search. A segment is similar to an inverted index. [@es-nrt]

By default, Elasticsearch refreshes every second. It only does this for indices that got at least one search request in the last 30 seconds. [@es-nrt]

You can ask a write to wait. `refresh=true` makes the change visible at once, but it creates tiny segments that cost more at index, search and merge time. `refresh=wait_for` waits for the next refresh instead. The docs say to use the default, `refresh=false`, unless you have a good reason. [@es-refresh]

### Shards and replicas

An Elasticsearch index is split into shards. Each shard is a self-contained Lucene index with part of the documents. Each document belongs to one primary shard. A replica shard is a copy of a primary, for failover and for more read capacity. [@es-shards]

You choose the number of primary shards when you create the index, and you cannot change it later. You can change the number of replicas at any time. [@es-shards]

Each shard runs a search on one CPU thread. Too many small shards slow search and can make the cluster unstable. Elastic suggests shards between 10 GB and 50 GB. [@es-shard-size]

### Keeping the index in sync

The search index is a copy. Your database is the source of truth. The dangerous design is the one from the story: write to the database, then write to the search engine in the same request. If the second write fails, the copy is wrong, and nothing repairs it.

Safer designs read changes from the database itself:

- **Outbox.** In the same transaction as the product change, insert a row into an outbox table. A worker reads it and updates the index.
- **CDC (change data capture).** A tool reads the database's change log and sends each change to the indexer.

Both are explained in [Sagas, outbox and CDC](/learn/sagas-outbox-and-cdc). Make index updates idempotent: send the full document with its ID, so a retry is harmless. Keep a script that rebuilds the whole index from the database, because you will need it after mapping changes.

### When Postgres is enough

You may not need a second system. Postgres gives you `tsvector`, `tsquery`, stemming dictionaries and a GIN index. [@pg-fts-intro] [@pg-fts-indexes] It can rank with `ts_rank` and `ts_rank_cd`. But ranking must read the `tsvector` of each matching document, so it can be slow when a query matches many rows. [@pg-fts-controls]

Start with Postgres when the data fits in one database and the search needs are simple. Move to a search engine when you need better relevance, typo tolerance, facets, or search load that would hurt your main database.

## Trade-offs

| Option | Fast on big data? | Good ranking? | Extra cost |
|---|---|---|---|
| `LIKE '%x%'` | No. Every row is checked. | No. | None at first. |
| Postgres full-text search + GIN | Yes, for many workloads. | Basic: ts_rank. Slow with many matches. | Little. Same database and same transaction. |
| Elasticsearch or OpenSearch | Yes. Shards spread the load. | Yes. BM25, analyzers, boosts. | A second cluster to run and an index to keep in sync. |
| Managed search service | Yes. | Yes, with less tuning. | Money and less control. Still needs sync. |

**Simple rule:** start with Postgres full-text search. Add a search engine when relevance or load needs it, and feed it from an outbox or CDC.

## At work: how a senior uses this

- When you see `LIKE '%...%'` on a growing table, ask: **"How many rows will this read in a year?"**
- Write down the analyzer for each field. Test it with real queries from your users, including plurals, accents and typos.
- Treat the search index as derived data. Ask in design review: **"How do we rebuild it from scratch, and how long does that take?"**
- Monitor sync lag: the time between a database change and when search shows it. Alert on it.
- Do not set `refresh=true` on every write to fix "I can't see my change". Use `wait_for` only where a user must see their own write. [@es-refresh]
- Pick shard counts from expected data size, not from a guess. The primary count is fixed, so changing it means building a new index. [@es-shards]
- Keep a small set of test queries with the results you expect. Run it after each analyzer or scoring change.

## With AI

### What to ask the agent

> "Find every SQL query in this repo that uses LIKE or ILIKE with a leading %. For each, estimate the table size and suggest a full-text index or a search engine."

> "Add a Postgres full-text search on products.title and products.description, with a GIN index on a generated tsvector column, and rank with ts_rank."

> "Write an outbox worker that reads product changes and sends the full document to Elasticsearch by ID. It must be safe to run twice."

### What to check yourself

- The query uses the same language config or analyzer as the index. Otherwise matches are missed.
- The agent did not add a direct database-then-search write in the request path.
- Index writes use the document ID, so retries replace instead of adding duplicates.
- No `refresh=true` on every write.
- Test with real queries, not just the one the agent picked.

## Check yourself

## Go deeper

Related lessons: [Secondary, composite and special indexes](/learn/secondary-and-composite-indexes), [Sagas, outbox and CDC](/learn/sagas-outbox-and-cdc), [Product search and discovery](/learn/product-search-and-discovery) and [PostgreSQL in depth](/learn/postgresql-in-depth).
