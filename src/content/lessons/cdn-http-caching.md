---
topic: cdn-http-caching
subtitle: A CDN answers from a copy near the user. Your HTTP headers decide what it may copy, for how long, and for whom.
status: published
reviewed_by: ai-factcheck
reviewed_on: 2026-10-21
illustrative_story: false
sources:
  - id: steam
    title: "Valve, “Update on Christmas Issues”, Steam news, 30 December 2015"
    url: https://store.steampowered.com/news/19852/
    note: Valve's own account of how a caching setting showed users' store pages to other users.
  - id: rfc9111
    title: "Fielding, Nottingham and Reschke, RFC 9111: HTTP Caching (IETF, 2022)"
    url: https://www.rfc-editor.org/rfc/rfc9111
    note: The standard. Shared and private caches (section 1), the cache key (section 2), Vary (section 4.1) and every Cache-Control directive (section 5.2).
  - id: mdn-cache-control
    title: "MDN Web Docs, “Cache-Control”"
    url: https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Headers/Cache-Control
    note: Each directive with examples, the no-cache versus no-store trap, private for logged-in pages, and the cache-busting pattern.
  - id: mdn-etag
    title: "MDN Web Docs, “ETag”"
    url: https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Headers/ETag
    note: How ETag and If-None-Match lead to a 304 Not Modified answer with no body.
  - id: mdn-vary
    title: "MDN Web Docs, “Vary”"
    url: https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Headers/Vary
    note: How Vary makes caches store a separate copy for each value of the listed request headers.
  - id: cf-cache
    title: "Cloudflare Docs, “Cache”"
    url: https://developers.cloudflare.com/cache/
    note: A short statement of what a CDN cache is for.
  - id: cf-default
    title: "Cloudflare Docs, “Default cache behavior”"
    url: https://developers.cloudflare.com/cache/concepts/default-cache-behavior/
    note: What Cloudflare caches by default, and the headers that stop it from caching.
  - id: cf-cache-keys
    title: "Cloudflare Docs, “Cache keys”"
    url: https://developers.cloudflare.com/cache/how-to/cache-keys/
    note: What the default cache key contains, and the warning that custom keys can lower the hit rate.
  - id: cf-purge
    title: "Cloudflare Docs, “Purge cache”"
    url: https://developers.cloudflare.com/cache/how-to/purge-cache/
    note: All the ways to purge, from one URL to everything.
  - id: cf-tiered
    title: "Cloudflare Docs, “Tiered Cache”"
    url: https://developers.cloudflare.com/cache/how-to/tiered-cache/
    note: Lower and upper tiers of data centers, and why only the upper tier asks the origin.
  - id: fastly-cache
    title: "Fastly Documentation, “Caching” (concepts)"
    url: https://www.fastly.com/documentation/guides/concepts/edge-state/cache/
    note: Fastly's default cache key (URL and Host), Vary as a secondary key, and request collapsing.
  - id: fastly-freshness
    title: "Fastly Documentation, “Cache freshness and TTLs”"
    url: https://www.fastly.com/documentation/guides/concepts/edge-state/cache/cache-freshness/
    note: Which header sets the TTL at Fastly, in which order, and how private responses are treated.
  - id: fastly-purge
    title: "Fastly Documentation, “Purging”"
    url: https://www.fastly.com/documentation/guides/concepts/edge-state/cache/purging/
    note: URL, surrogate key and purge-all, and soft purge versus hard purge.
  - id: fastly-shield
    title: "Fastly Documentation, “Shielding”"
    url: https://www.fastly.com/documentation/guides/concepts/shielding/
    note: How a shield POP sits between edge locations and the origin, and what it costs.
  - id: fastly-2021
    title: "Fastly, “Summary of June 8 outage” (2021)"
    url: https://www.fastly.com/blog/summary-of-june-8-outage
    note: A hidden bug and a valid customer config change that made 85% of Fastly's network return errors.
claims_checked:
  - claim: "Between 11:50 and 13:20 PST on 25 December 2015, store page requests for about 34,000 users may have been shown to other users, after a second caching setting incorrectly cached traffic for authenticated users."
    source: steam
    note: Matches Valve's statement. Exposed fields corrected to Valve's list (last four digits of the Steam Guard phone number). Added that full card numbers and passwords were not exposed.
  - claim: "no-cache allows storing but requires revalidation before each reuse; no-store forbids storing."
    source: rfc9111
    note: RFC 9111 sections 5.2.2.4 and 5.2.2.5, and MDN's explicit warning that no-cache does not mean don't cache.
  - claim: "private means a shared cache must not store the response; s-maxage overrides max-age only in shared caches; public lets a shared cache store responses to requests with Authorization."
    source: rfc9111
    note: Sections 5.2.2.7, 5.2.2.10 and 5.2.2.9. The public bullet was vague and now states the Authorization case.
  - claim: "stale-while-revalidate and stale-if-error (500, 502, 503, 504) let a cache serve a stale copy."
    source: mdn-cache-control
    note: Status codes match MDN. Added that both are defined in RFC 5861, which RFC 9111 cites as an extension.
  - claim: "Cloudflare does not cache HTML or JSON by default, and skips responses with private, no-store, no-cache, max-age=0 or Set-Cookie."
    source: cf-default
    note: Matches Cloudflare's default cache behavior page.
  - claim: "A Fastly request that reaches the origin through a shield counts as two misses, and inter-POP traffic is billed."
    source: fastly-shield
    note: Matches Fastly's shielding guide.
  - claim: "Fastly's June 2021 outage: a valid customer config change triggered a hidden bug, and 85% of the network returned errors."
    source: fastly-2021
    note: Matches Fastly's summary (bug deployed 12 May, triggered 8 June).
quiz:
  - q: "A logged-in \"My account\" page is sent with `Cache-Control: max-age=300` and nothing else. What is the risk?"
    options:
      - The browser will never cache it.
      - A shared cache such as a CDN may store it and show it to other users.
      - The page will always be checked with the origin first.
    answer: 1
    why: Without `private`, a shared cache may store the page. MDN warns this can leak personal data to other users.
  - q: "What does `Cache-Control: no-cache` mean?"
    options:
      - Never store the response anywhere.
      - The response may be stored, but must be checked with the origin before each reuse.
      - Only the browser may store it.
    answer: 1
    why: "`no-cache` means store, but revalidate before reuse. To stop storing, use `no-store`."
  - q: A browser sends `If-None-Match` with the ETag it has, and the file has not changed. What does the server send back?
    options:
      - 304 Not Modified, with no body.
      - 200 OK, with the full file again.
      - 404 Not Found.
    answer: 0
    why: The ETags match, so the server tells the browser to keep using its copy. No body is sent.
  - q: You ship a new `app.js` every day. What is the safest way to make every user get the new file fast?
    options:
      - Purge the whole CDN cache after each deploy.
      - Put a content hash in the file name, and cache each file for a long time.
      - Set max-age=0 on the file.
    answer: 1
    why: A new name is a new cache key, so no old copy can be served. Old names can be cached for a long time because they never change.
incidents: [fastly-2021-06-global]
cases: [zerodha-scaling-common-sense]
related: [caching-stampedes, http-versions, scaling-reads, safe-deploys]
---

## What broke

Early on 25 December 2015 (Pacific time), the Steam Store was under a denial-of-service attack. Traffic to the store rose by 2,000% over the average during the Steam Sale. [@steam]

To reduce the load on its store servers, Valve deployed caching rules managed by a web caching partner. During a second wave of the attack, a second caching setting was deployed. In Valve's words, it "incorrectly cached web traffic for authenticated users". [@steam]

So pages built for logged-in users were cached and returned to other users. Between 11:50 and 13:20 PST, store page requests for about 34,000 users may have been shown to someone else. [@steam]

Those pages could contain a billing address, the last four digits of a Steam Guard phone number, purchase history, the last two digits of a card number, or an email address. Full card numbers and passwords were not exposed. [@steam]

Valve shut down the store and deployed a new caching setting. The store stayed down until all caching settings were checked and cached data was purged from the edge servers. [@steam]

Valve called it a configuration error. The cache did what its settings said, and the settings were wrong. This lesson is about writing those settings well.

## The idea in simple words

A CDN (content delivery network) keeps copies of your content in data centers that are closer to users than your own servers. This makes pages faster and takes load off your servers. [@cf-cache] The data centers near users are called edge locations. Your own server, which has the real content, is the origin server.

An edge location is a shared cache: it stores responses and reuses them for more than one user. A browser cache is a private cache: it serves one user only. [@rfc9111]

The CDN must answer two questions for each response:

1. **May I store this, and for how long?** Your `Cache-Control` header answers this.
2. **Which later requests may get this copy?** The cache key answers this.

> [!key] **The key idea.** A CDN is only as safe as your headers and your cache key. If two different users can produce the same cache key, they can see each other's pages.

## How it works

Step through two users and one deploy. Use Next and Back, or the ← and → keys.

[[diagram:cdn-edge-cache]]

### Cache-Control: the rules you send

You set the rules with the `Cache-Control` response header. The main directives, from RFC 9111 section 5.2: [@rfc9111]

- **`max-age=N`**: the response is fresh for N seconds.
- **`s-maxage=N`**: the same, but only for shared caches like a CDN. It overrides `max-age` there.
- **`public`**: a shared cache may store this, even for a request with an `Authorization` header.
- **`private`**: a shared cache must not store this. A browser may.
- **`no-cache`**: you may store it, but you must check with the origin before each reuse.
- **`no-store`**: do not store any part of the request or the response.

The names `no-cache` and `no-store` confuse many people. MDN says it plainly: `no-cache` does not mean "don't cache". If you mean "don't store", use `no-store`. [@mdn-cache-control]

MDN also says to add `private` to pages made for one user, especially after login. Without it, a shared cache may reuse the page for many users and leak personal data. [@mdn-cache-control] That is what happened to Steam.

CDNs add their own defaults on top. Cloudflare does not cache HTML or JSON by default. It also skips responses with `private`, `no-store`, `no-cache` or `max-age=0`, or with a `Set-Cookie` header. [@cf-default] Fastly does not cache `private` responses, and reads `s-maxage` before `max-age`. [@fastly-freshness] Check your own CDN's docs. They differ.

### ETag and 304: checking without downloading

When a cached copy becomes stale, the cache does not have to download it again. The origin can send an `ETag`, a short ID for this version of the content. Later, the client sends that ID back in an `If-None-Match` header.

If the content has not changed, the server answers `304 Not Modified` with no body. The client keeps using its copy. [@mdn-etag] You save bandwidth, but you still pay one round trip to the origin.

### The cache key, and Vary

The cache key decides which requests share one stored copy. In the standard, it is at least the request method and the URL. [@rfc9111] Fastly uses the URL, including the query string, plus the `Host` header. [@fastly-cache] Cloudflare's default key also uses the full URL, plus a few headers such as `Origin`. [@cf-cache-keys]

Notice what is **not** in the key: the cookie or the logged-in user. Two users who ask for `/account` get the same key. So a shared cache must never store a personal page.

Sometimes one URL can return different versions, chosen by the request headers. The `Vary` header names those request headers. Caches then store a separate copy for each value. [@mdn-vary] A cache may reuse a copy without checking the origin only if those request headers match. [@rfc9111]

Every extra part of the key splits your traffic into more copies. Cloudflare warns that custom cache keys can reduce your cache hit rate. [@cf-cache-keys] The cache hit ratio is the share of requests the edge answers from its own copy. A high ratio means a cheap, fast origin. A low ratio means the CDN mostly forwards traffic.

### Changing content: purge or a new name

When content changes, you have two choices.

**A cache purge.** You tell the CDN to drop copies. Cloudflare can purge one URL, a prefix, a tag, a hostname or everything. It recommends purging single URLs. [@cf-purge] Fastly has URL, surrogate key and purge-all options. A soft purge marks a copy stale, so it can still be served while a new one is fetched. A hard purge removes it, so the next request goes to the origin. Purge-all is always hard. [@fastly-purge]

**A new file name.** Put a version or a hash in the file name, like `app.c3d4.js`. Never change a file at the same name. This is called cache busting. Each file can then use a long `max-age` plus `immutable`, and the HTML can use `no-cache`. [@mdn-cache-control] The HTML is checked on each load, and it points to the new names.

### Stale content and failures

Two more directives help when a copy goes stale. With `stale-while-revalidate`, a cache may serve the stale copy for a short time. Meanwhile it fetches a fresh one in the background. With `stale-if-error`, it may serve the stale copy when the origin returns a 500, 502, 503 or 504 error. [@mdn-cache-control] Both come from a separate standard, RFC 5861, not from RFC 9111. [@rfc9111] The lesson on [caching and cache stampedes](/learn/caching-and-cache-stampedes) shows why this matters for hot keys.

At a CDN, many users may miss on the same URL at once. Fastly's request collapsing sends only one of them to the origin, and the rest share the answer. [@fastly-cache]

### Origin shield

A CDN has many edge locations. Each one that misses goes to your origin. A shield adds one more layer. Edges ask a chosen shield location, and only the shield asks the origin. [@fastly-shield] Cloudflare's Tiered Cache works the same way, with lower tiers and upper tiers. [@cf-tiered]

This reduces origin requests. The cost: traffic between Fastly locations counts toward your bill. Also, a request that reaches the origin counts as two misses, so your reported hit ratio can look lower than you expect. [@fastly-shield]

## Trade-offs

| Choice | Fresh content? | Origin load | Use it when |
|---|---|---|---|
| Long max-age + hashed file names | Yes. A new name is fetched at once. | Very low. | JS, CSS, images and fonts from your build. |
| Short max-age (seconds to minutes) | Mostly. Users may see data that old. | Medium. | Public pages that change often, like a product list. |
| no-cache + ETag | Yes. Every reuse is checked. | One small request per reuse. | HTML pages that point to hashed files. |
| Purge on change | Yes, after the purge. | A burst of misses after each purge. | Content you cannot rename, like an article URL. |
| private or no-store | Yes. | High. The CDN answers none of these requests. | Anything personal: accounts, carts, tokens. |

**Simple rule:** hash your static files and cache them for a long time. Mark every logged-in response `private`. Cache public HTML only for a short time.

## At work: how a senior uses this

- Set `Cache-Control` on **every** response, on purpose. Do not leave the choice to CDN defaults.
- For each new endpoint, ask: **"Is this response the same for every user?"** If not, it is `private` or `no-store`.
- When you add CDN rules, test them with two different logged-in users. Check that user B never sees user A's data.
- Review CDN config changes like code. The Steam leak came from a cache setting deployed in a hurry. [@steam] Fastly's 2021 outage came from a valid customer config change that hit a hidden bug. About 85% of its network returned errors. [@fastly-2021]
- Track the cache hit ratio and origin requests per second. A sudden drop often means a new query string or header split the cache key.
- Avoid purge-all in busy hours. At Fastly, purge-all is a hard purge, so every object must be fetched from the origin again. [@fastly-purge]
- Know your plan B. If the CDN fails, can your origin carry the full traffic?

## With AI

### What to ask the agent

> "List every HTTP endpoint in this service and the Cache-Control header it sends. Mark any response that depends on the logged-in user but is not private or no-store."

> "Change our build so JS and CSS files get a content hash in the name. Serve them with max-age=31536000, immutable, and serve index.html with no-cache."

> "Write a test that logs in as two users, requests /account through the CDN, and fails if the second user sees the first user's email."

### What to check yourself

- No response with personal data can be stored in a shared cache. Look for `Set-Cookie` and `Authorization` paths.
- The agent did not add `Vary: Cookie` or user IDs to the cache key "to be safe". That splits the cache into one copy per user.
- Hashed file names change when the content changes, and only then.
- The CDN config change is reviewed, and you know how to roll it back.

## Check yourself

## Go deeper

Related lessons: [Caching and cache stampedes](/learn/caching-and-cache-stampedes), [HTTP/1.1, HTTP/2 and HTTP/3](/learn/http-1-2-3-and-quic), [Scaling reads](/learn/scaling-reads) and [Safe deploys and config changes](/learn/safe-deploys-config-changes).
