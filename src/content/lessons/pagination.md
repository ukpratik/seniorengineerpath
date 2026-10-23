---
topic: pagination
subtitle: Offset and cursor pagination, stable sort orders, filters and page sizes, and why deep offsets get slow on big tables.
status: published
reviewed_by: ai-factcheck
reviewed_on: 2026-10-23
illustrative_story: true
sources:
  - id: pg-limit
    title: "PostgreSQL documentation, section 7.6: LIMIT and OFFSET"
    url: https://www.postgresql.org/docs/current/queries-limit.html
    note: Why LIMIT needs a unique ORDER BY, and why rows skipped by OFFSET still cost work.
  - id: luke-seek
    title: "Markus Winand, “Paging Through Results”, Use The Index, Luke!"
    url: https://use-the-index-luke.com/sql/partial-results/fetch-next-page
    note: The seek method with row values, the index it needs, and its limits.
  - id: pg-count
    title: "PostgreSQL wiki, “Slow Counting”"
    url: https://wiki.postgresql.org/wiki/Slow_Counting
    note: Why an exact count(*) must walk through the rows, and that estimates are much faster.
  - id: slack-pages
    title: "Michael Hahn, “Evolving API Pagination at Slack”, Slack Engineering, 2017"
    url: https://slack.engineering/evolving-api-pagination-at-slack/
    note: Why Slack left offsets, what its Base64 cursors hold, and what cursors cannot do.
  - id: slack-docs
    title: "Slack developer docs, “Pagination”"
    url: https://docs.slack.dev/apis/web-api/pagination
    note: The cursor and limit parameters, next_cursor, page size advice, and cursor expiry.
  - id: stripe-pages
    title: "Stripe API reference: Pagination"
    url: https://docs.stripe.com/api/pagination
    note: limit, starting_after, ending_before and has_more, with object IDs as cursors.
  - id: github-pages
    title: "GitHub REST API docs, “Using pagination in the REST API”"
    url: https://docs.github.com/en/rest/using-the-rest-api/using-pagination-in-the-rest-api
    note: The Link header, per_page and page, and before and after on some endpoints.
  - id: github-search
    title: "GitHub REST API docs, “REST API endpoints for search”"
    url: https://docs.github.com/en/rest/search/search
    note: Search returns at most 1,000 results, with up to 100 per page.
  - id: aip158
    title: "Google, AIP-158: Pagination"
    url: https://google.aip.dev/158
    note: Opaque page tokens, page size limits, optional total_size, and keeping the other parameters the same between pages.
  - id: relay
    title: "GraphQL Cursor Connections Specification (Relay)"
    url: https://relay.dev/graphql/connections.htm
    note: Connections with edges, node, cursor and pageInfo, and the first, after, last and before arguments.
claims_checked:
  - claim: Rows skipped by OFFSET "still have to be computed inside the server", so a large offset can be slow; LIMIT needs an ORDER BY with a unique order, or results are an unpredictable subset and can be inconsistent across LIMIT/OFFSET values.
    source: pg-limit
    note: Quoted and paraphrased from section 7.6 of the current PostgreSQL docs.
  - claim: The seek method uses row values like (sale_date, sale_id) < (?, ?) with a matching index, stays stable when rows are inserted, cannot fetch arbitrary pages, and browsing back needs reversed comparisons and sort; add a unique column and extend the index.
    source: luke-seek
    note: All confirmed on the page. The page does not say deep pages cost the same as page 1, so the lesson now says they "stay fast". The page uses "seek method", not "keyset".
  - claim: Exact count(*) is slow in PostgreSQL because of MVCC visibility checks, normally a sequential scan; estimates are much faster.
    source: pg-count
    note: Matches the wiki wording.
  - claim: Slack left offsets because they do not scale for large datasets and repeat results when 10 new messages arrive after page one; a Base64 cursor decodes to user:W07QCRPA4; cursors give no total count and no page jumps.
    source: slack-pages
    note: Confirmed. The per-shard cursor is a design example in the post ("we could return"), so the lesson no longer says Slack shipped it or that no client changed.
  - claim: Slack uses cursor and limit, returns response_metadata.next_cursor (empty string at the end), recommends 100-200 per call with a maximum of 1,000, warns that fewer results than limit does not mean the end, and says not to keep cursors for hours or days.
    source: slack-docs
    note: All confirmed in the docs.
  - claim: Stripe lists take limit (1 to 100, default 10), starting_after and ending_before (object IDs), return data and has_more, in reverse chronological order.
    source: stripe-pages
    note: Confirmed on the API reference page.
  - claim: GitHub uses per_page and page (or before/after, or since), a Link header with next, prev, first and last, and a per_page maximum of 100 on most endpoints.
    source: github-pages
    note: Confirmed.
  - claim: GitHub search gives up to 1,000 results for each search and up to 100 per page.
    source: github-search
    note: Confirmed.
  - claim: AIP-158 uses page_size, page_token and next_page_token; page_size is optional, too-large values are coerced down, negatives are INVALID_ARGUMENT; tokens are opaque, not user-parseable and grant no authorization; other parameters must match; total_size is optional and may be an estimate if documented; three-day expiry is a rule of thumb.
    source: aip158
    note: Confirmed. AIP-158 also says Base64 alone is not enough obfuscation; the lesson now says this and no longer suggests a plain Base64 cursor.
  - claim: Relay connections must have edges and pageInfo; edges have node and cursor; first/after and last/before; PageInfo has hasNextPage, hasPreviousPage, startCursor and endCursor; clients should treat cursors as opaque; no total count in the spec.
    source: relay
    note: Confirmed. The spec says "should" for opacity, so the lesson now says "should".
quiz:
  - q: Your list endpoint uses ORDER BY created_at with OFFSET and LIMIT. Many rows share the same created_at. What can go wrong?
    options:
      - Nothing. The database keeps the same order every time.
      - Rows with the same time can come back in a different order, so pages can repeat or skip them. Add a unique column like id to the sort.
      - The query will fail with an error.
    answer: 1
    why: PostgreSQL says LIMIT needs an ORDER BY that gives a unique order, or you get an unpredictable subset of rows.
  - q: Why does ?offset=500000&limit=20 get slow on a big table?
    options:
      - The database still has to produce the 500,000 skipped rows before it returns 20.
      - HTTP cannot send large numbers.
      - Offsets turn off all indexes.
    answer: 0
    why: PostgreSQL's docs say rows skipped by OFFSET still have to be computed inside the server.
  - q: Why should a cursor be opaque, instead of a plain id the client builds?
    options:
      - It makes the response smaller.
      - You can change what is inside it later without breaking clients, because they never read it.
      - Browsers require it.
    answer: 1
    why: Google AIP-158 says tokens must not be user-parseable, and the Relay spec says clients should treat cursors as opaque. Slack shows how one cursor could hold a position for each shard.
  - q: In the Relay spec, which fields must a connection type have?
    options:
      - items and total
      - edges and pageInfo
      - data and has_more
    answer: 1
    why: "The spec says connection types must have fields named edges and pageInfo. data and has_more are Stripe's names."
incidents: []
cases: []
related: [rest-api, btree-lsm, search-systems, scaling-reads, sharding, graphql-bff]
glossary_optout: [offset]
---

## What broke

A shop has an admin page for orders. It calls `GET /orders?page=1&per_page=50`, and the server runs `OFFSET (page - 1) * 50`. In the first year, the table holds 20,000 orders, and every page loads fast.

Two years later, the table holds 30 million orders. A finance tool walks through all orders every night, page by page. The first pages take a few milliseconds. Page 100,000 takes seconds, and the whole job never ends before morning.

The support team reports a second problem. During a sale, they scroll the list of new orders. Some orders appear twice, and some never appear at all. New orders arrive every second and push the rest down.

The team moves to a cursor that remembers the last order seen. Every page is now fast, and nothing repeats. This story is made up. Slack, however, left offsets for the same two reasons. [@slack-pages]

## The idea in simple words

You never return a whole list in one answer. You return it in pages. The [REST API lesson](/learn/rest-api-design) explains why to add paging from day one. This lesson is about how to do it well.

There are two main ways.

- **Offset pagination** says "skip 40 rows, then give me 20". The client counts positions.
- **Keyset pagination**, also called the seek method, says "give me 20 rows after the last one I saw". [@luke-seek] Most APIs hide the "last one I saw" inside a token called a cursor.

> [!key] **The key idea.** An offset remembers a position, and positions move when data changes. A cursor remembers a row, and a row stays where it is.

## How it works

Step through two pages of a list, with a new row between them. Use Next and Back, or the ← and → keys.

[[diagram:pagination-insert]]

**Offset, and why deep pages get slow.** In SQL, `LIMIT 20 OFFSET 40` skips 40 rows and returns the next 20. PostgreSQL's docs say the skipped rows "still have to be computed inside the server", so a large offset can be slow. [@pg-limit] Markus Winand shows the same thing. The database must count all rows from the start until it reaches the page, so later pages take longer. [@luke-seek] Page 1 is cheap. Page 100,000 reads millions of rows to throw them away.

Offsets also drift. When a row is inserted above the reader's position, the next page starts one row too early and repeats a row. When a row is deleted, the next page starts one row too late and skips one. Slack's example: you read 10 messages, 10 new ones arrive, and page two shows the same messages again. [@slack-pages]

**Keyset pagination.** Instead of a position, send the sort values of the last row. If the list is sorted by `created_at DESC, id DESC`, the next page is:

```sql
SELECT * FROM orders
WHERE (created_at, id) < ($1, $2)
ORDER BY created_at DESC, id DESC
LIMIT 20;
```

The `(created_at, id) < ($1, $2)` part is a row value comparison. With an index on `(created_at, id)`, the database jumps straight to the right place. [@luke-seek] Deep pages stay fast, while offset pages get slower the further you go. New rows do not move your place, so results stay stable. [@luke-seek]

The cost: you cannot jump to page 7. You can only go next or back, and going back means you reverse the comparison and the sort. [@luke-seek] There is also no total page count. [@slack-pages]

**A stable sort needs a tie-breaker.** Many orders share the same `created_at`. PostgreSQL warns that without an `ORDER BY` that gives a unique order, you get an unpredictable subset of rows. Different `LIMIT` and `OFFSET` values can even give inconsistent results. [@pg-limit] So always end the sort with a unique column, usually the primary key. Winand's advice is the same: add a unique column and extend the index. [@luke-seek] This applies to offsets and cursors alike.

**Opaque cursors.** Do not ask clients to build the cursor from raw fields. Encode it, and say in the docs that clients must not read it. Slack's cursors are Base64 text. One decodes to `user:W07QCRPA4`, which means "start after this user". [@slack-pages] Slack also shows how one cursor could hold a position for each shard when data is spread over shards. [@slack-pages] Clients never read the cursor, so they would not see that change. Google's AIP-158 says page tokens must be opaque and must not be user-parseable. Plain Base64 is not enough to hide what is inside, so Google says to do more than that. It also says a token only marks where to continue. It never grants access, so check permissions on every request. [@aip158]

Cursors can expire. Slack tells clients not to keep cursors for hours or days. [@slack-docs] Google says tokens may expire after a reasonable time and suggests about three days. [@aip158]

**Filtering and sorting.** Filters and sort order are query parameters, for example `GET /orders?status=paid&sort=-created_at`. Our advice: only allow sorts that have an index. Without one, each page may need to sort the whole table. Google says the other parameters must stay the same while a client follows page tokens, and the API should return an error if they change. Only the page size may change. [@aip158] Put the filter and sort inside the cursor too, so the server can check them.

**Page size limits.** Always cap the page size. Google says the page size must be optional. If it is too large, the API should lower it to the maximum, and a negative size is an error. [@aip158] Stripe's `limit` goes from 1 to 100, with a default of 10. [@stripe-pages] Slack recommends 100 to 200 results and allows at most 1,000. [@slack-docs] A page can hold fewer items than the limit and still have more after it. So clients must check the "more" signal, not the item count. [@slack-docs] [@aip158]

**Total counts are expensive.** "Page 3 of 912" needs an exact count. In PostgreSQL, `count(*)` must walk through all rows to check which ones the current transaction can see. Estimates are much faster. [@pg-count] Google makes `total_size` optional, and allows an estimate if the API documents it. [@aip158] GitHub search returns at most 1,000 results per search, at most 100 per page. [@github-search] Ask product: does the screen need "912 pages", or just "more"?

**How well-known APIs name things.**

- **Stripe**: `limit`, `starting_after` and `ending_before`. The answer has `data` and `has_more`. The cursor is just an object ID, and lists are newest first. [@stripe-pages]
- **GitHub REST**: `per_page` and `page`, with up to 100 per page on most endpoints. The `Link` header gives `next`, `prev`, `first` and `last` URLs. Some endpoints use `before` and `after` instead. [@github-pages]
- **Slack**: `cursor` and `limit`. The answer has `response_metadata.next_cursor`, which is empty at the end. [@slack-docs]
- **Google**: `page_size`, `page_token` and `next_page_token`. [@aip158]

**GraphQL.** The Relay cursor connections spec is the common pattern. A connection has `edges` and `pageInfo`. Each edge has a `node` and a `cursor`. You page forward with `first` and `after`, and back with `last` and `before`. `pageInfo` has `hasNextPage`, `hasPreviousPage`, `startCursor` and `endCursor`, and clients should treat cursors as opaque. [@relay] The spec has no total count. Add `totalCount` yourself only if you can afford it.

## Trade-offs

| Option | Speed on deep pages | Stable while rows change? | Jump to page 7? | Use it when |
|---|---|---|---|---|
| Offset and limit | No. Gets slower as the offset grows. [@pg-limit] | No. Rows can repeat or be skipped. [@slack-pages] | Yes. | Small admin tables, or lists that rarely change. |
| Keyset or cursor | Yes. Stays fast on deep pages, with the right index. [@luke-seek] | Yes. [@luke-seek] | No. Only next and back. | Feeds, logs, exports, public APIs, any large list. |
| Exact total count | No. Must walk the rows. [@pg-count] | Not applicable. | Not applicable. | Small tables, or when the screen truly needs it. |
| Estimated or no count | Yes. [@pg-count] | Not applicable. | Not applicable. | Large tables. Show "more" instead of "page 3 of 912". |

**Simple rule:** use opaque cursors over a unique, indexed sort. Cap the page size. Skip exact counts unless a user really needs them.

## At work: how a senior uses this

- Ask: **"How big will this list be in two years?"** Design for that size, not for today's.
- Check every list query with `EXPLAIN`. Look for a sort that uses an index, not a sort over the whole table.
- Make sure every sort ends with a unique column. Ask: **"What happens when two rows have the same value?"**
- Keep cursors opaque from version one. You can then change what is inside them, for example after [sharding](/learn/sharding-and-consistent-hashing).
- Test with inserts and deletes between page requests. Then count duplicates and gaps.
- Push back on "page X of Y" for big tables. Offer "load more", or an estimate.
- For big exports, do not use the public paging API at all. Use a background job or a database snapshot.

## With AI

### What to ask the agent

> "Change this offset-based /orders endpoint to keyset pagination. Sort by created_at DESC, id DESC. Return an opaque cursor that also holds the filters. Add an index for it."

> "Write a test that inserts and deletes rows between page 1 and page 2, then checks that no order is repeated or missed."

> "Design a Relay connection for orders in GraphQL, with first, after, last and before. Explain how the cursor is built and checked."

### What to check yourself

- The sort ends with a unique column, and the cursor holds every sort column, not only the id.
- The `WHERE` comparison matches the sort direction. One flipped `<` repeats or skips rows.
- An index matches the filter and sort. Run `EXPLAIN` on a big table.
- The server caps the page size, and rejects negative values.
- The server checks permissions on every page. A cursor is not a key to the data.
- Agents often add `COUNT(*)` to every list call. Remove it unless the screen needs it.

## Check yourself

## Go deeper

- [REST API design](/learn/rest-api-design): resources, status codes, and adding paging from day one.
- [B-tree vs LSM-tree](/learn/btree-vs-lsm-tree): why an index lets keyset pagination jump straight to a row.
- [Search and inverted indexes](/learn/search-and-inverted-indexes): ranked results and why search APIs cap deep pages.
- [Scaling reads](/learn/scaling-reads): caches and replicas for heavy list endpoints.
