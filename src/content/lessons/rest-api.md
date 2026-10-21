---
topic: rest-api
subtitle: Nouns, methods, status codes and errors that clients can trust, and changes that never break an app already in use.
status: published
reviewed_by: ai-factcheck
reviewed_on: 2026-10-21
illustrative_story: true
sources:
  - id: rfc9110
    title: "Fielding, Nottingham and Reschke, RFC 9110: HTTP Semantics (IETF, 2022), sections 9.2 and 15"
    url: https://www.rfc-editor.org/rfc/rfc9110
    note: Which methods are safe and idempotent, and what each status code means.
  - id: rfc5789
    title: "Dusseault and Snell, RFC 5789: PATCH Method for HTTP (IETF, 2010)"
    url: https://www.rfc-editor.org/rfc/rfc5789
    note: The PATCH method, and why it is not idempotent by default.
  - id: rfc9457
    title: "Nottingham, Wilde and Dalal, RFC 9457: Problem Details for HTTP APIs (IETF, 2023)"
    url: https://www.rfc-editor.org/rfc/rfc9457
    note: A standard JSON shape for error bodies, with type, title, status, detail and instance.
  - id: rfc6585
    title: "Nottingham and Fielding, RFC 6585: Additional HTTP Status Codes (IETF, 2012), section 4"
    url: https://www.rfc-editor.org/rfc/rfc6585#section-4
    note: The definition of 429 Too Many Requests.
  - id: aip121
    title: "Google, AIP-121: Resource-oriented design"
    url: https://google.aip.dev/121
    note: Resources as nouns, collections, and a small set of standard methods.
  - id: aip158
    title: "Google, AIP-158: Pagination"
    url: https://google.aip.dev/158
    note: Page tokens, page size, and why you add pagination from the first version.
  - id: aip180
    title: "Google, AIP-180: Backwards compatibility"
    url: https://google.aip.dev/180
    note: Which changes break old clients, such as removing or renaming a field.
  - id: msft
    title: "Microsoft, Azure REST API Guidelines (GitHub, microsoft/api-guidelines)"
    url: https://github.com/microsoft/api-guidelines/blob/vNext/azure/Guidelines.md
    note: Naming, paging with nextLink, the api-version parameter and breaking changes.
  - id: stripe-idem
    title: "Stripe API reference: Idempotent requests"
    url: https://docs.stripe.com/api/idempotent_requests
    note: How a real API accepts an Idempotency-Key header on POST requests.
  - id: stripe-pages
    title: "Stripe API reference: Pagination"
    url: https://docs.stripe.com/api/pagination
    note: Cursor pagination with limit, starting_after and has_more.
  - id: stripe-versions
    title: "Stripe API reference: Versioning"
    url: https://docs.stripe.com/api/versioning
    note: Dated API versions, the Stripe-Version header, and monthly releases with no breaking changes.
  - id: slack-pages
    title: "Michael Hahn, “Evolving API Pagination at Slack”, Slack Engineering, 2017"
    url: https://slack.engineering/evolving-api-pagination-at-slack/
    note: Why Slack moved from offset to cursor pagination, and what cursors cannot do.
  - id: openapi
    title: "OpenAPI Initiative, OpenAPI Specification"
    url: https://spec.openapis.org/oas/latest.html
    note: The standard file format that describes an HTTP API for people and tools.
  - id: grpc
    title: "gRPC documentation, “Introduction to gRPC”"
    url: https://grpc.io/docs/what-is-grpc/introduction/
    note: Calling a method on a remote server, with Protocol Buffers as the contract.
  - id: graphql
    title: "GraphQL Foundation, graphql.org"
    url: https://graphql.org/
    note: A query language where the client asks for exactly the fields it needs.
  - id: stripe-webhooks
    title: "Stripe documentation: Receive Stripe events in your webhook endpoint"
    url: https://docs.stripe.com/webhooks
    note: How a server pushes events to your HTTPS endpoint, with retries and duplicates.
claims_checked:
  - claim: "GET, HEAD, OPTIONS and TRACE are safe; PUT, DELETE and the safe methods are idempotent; a client should not automatically retry a non-idempotent request."
    source: rfc9110
    note: "Sections 9.2.1 and 9.2.2 say exactly this. PATCH: RFC 5789 section 2 says it is 'neither safe nor idempotent'."
  - claim: "201 points to the new resource with Location; 404 may hide a resource; 422 means the syntax is correct but the instructions cannot be processed."
    source: rfc9110
    note: "Sections 15.3.2, 15.5.4/15.5.5 and 15.5.21 match. 403 wording changed to the RFC meaning: the server understood but refuses."
  - claim: "Problem details use application/problem+json with type, title, status, detail and instance; clients must ignore unknown extensions."
    source: rfc9457
    note: "Section 3.1 lists the five members; detail 'ought to focus on helping the client correct the problem'. Diagram caption changed from 'user' to 'client'."
  - claim: "Renaming a field is 'remove and add'; fields must not be removed or retyped; new required request fields must not be added."
    source: aip180
    note: "AIP-180 says all of this. AIP-158 says adding pagination to an existing RPC is backwards-incompatible; Azure guidelines say the same."
  - claim: "Stripe v1 accepts Idempotency-Key on all POST requests and errors if a reused key comes with different parameters."
    source: stripe-idem
    note: "Stated for API v1. Lesson now says 'In its v1 API'. Pagination page confirms limit, starting_after, ending_before and has_more."
  - claim: "Since 2024-09-30.acacia Stripe ships monthly versions with no breaking changes and two major releases a year; Stripe-Version overrides the account default."
    source: stripe-versions
    note: "Matches the versioning page."
  - claim: "Offset pagination skips or repeats items when data changes and gets slow on deep pages; cursors cannot jump to a page or give a total count."
    source: slack-pages
    note: "Michael Hahn, 2017. All points are in the post."
quiz:
  - q: A client sends DELETE /orders/42 and the connection drops before the answer arrives. Is it safe to send it again?
    options:
      - No. DELETE is never safe to repeat.
      - Yes. DELETE is idempotent, so a second identical request has the same intended effect.
      - Only if the server returned 204 the first time.
    answer: 1
    why: RFC 9110 defines PUT, DELETE and the safe methods as idempotent. The answer may differ, but the effect is the same.
  - q: A new release of your API renames the field `total` to `totalAmount`. What happens?
    options:
      - Nothing. JSON clients ignore names.
      - Old apps that read `total` break, because a rename is a remove plus an add.
      - Only new apps break.
    answer: 1
    why: Google's AIP-180 treats a rename like removing the old field, which breaks existing clients.
  - q: Rows are added to a list while a user scrolls it with `?offset=20&limit=20`. What can go wrong?
    options:
      - The user can see the same item twice or miss items.
      - Nothing. Offsets are stable.
      - The server returns 409 Conflict.
    answer: 0
    why: When data changes, the offset window moves. Slack saw skipped and repeated results and moved to cursors.
  - q: The request JSON is valid, but the quantity is -1. Which status code fits best?
    options: ["400 Bad Request", "422 Unprocessable Content", "500 Internal Server Error"]
    answer: 1
    why: RFC 9110 uses 422 when the syntax is correct but the server cannot process the instructions inside.
  - q: Why add pagination to a list endpoint in the first version, even if the list is small today?
    options:
      - Adding it later is a breaking change for existing clients.
      - It makes the database faster today.
      - HTTP requires it.
    answer: 0
    why: Google AIP-158 and Microsoft's guidelines both say adding paging later breaks existing users.
incidents: []
cases: [stripe-idempotency-keys]
related: [idempotency-retries, rate-limiting, pagination, api-versioning, schema-evolution, grpc, graphql-bff, webhooks]
glossary_optout: [offset, rfc]
---

## What broke

A food delivery app has an orders API. The mobile app shows a list of past orders with a price on each one. The API returns each order with a field called `total`.

A backend developer tidies up the code. He renames `total` to `total_amount`, because it is clearer. The web app is updated in the same release. All the tests pass.

The next morning, support gets angry messages. On the Android app, every order shows a price of ₹0. Some old iPhone versions crash on the orders screen.

The team checks the data. Many users had not updated the app in months. Those versions still read `total`, and now the field is gone. The team cannot force an update on every phone, so they put `total` back and send both fields.

This story is made up, but the mistake is common. A web page updates on every reload. A mobile app on a phone may be months old. Your API must serve all of them.

## The idea in simple words

A REST API is built around **resources**: the nouns of your product. Orders, users and payments are resources. Each one has an address, like `/orders/42`. A group of them, like `/orders`, is a **collection**. [@aip121]

You act on resources with a few standard HTTP methods: GET, POST, PUT, PATCH and DELETE. You do not invent a new URL for every action. Google's guide says APIs should prefer standard methods over custom ones. [@aip121]

The server answers with a **status code** that tells the client what happened. When something goes wrong, it sends an error body in one fixed shape.

> [!key] **The key idea.** An API is a promise to every client that uses it, including old ones you cannot update. Design it so you can add to it for years without breaking that promise.

## How it works

Step through one order, from creation to an error. Use Next and Back, or the ← and → keys.

[[diagram:rest-order-flow]]

**Methods.** RFC 9110 defines GET, HEAD, OPTIONS and TRACE as **safe**: the client does not ask for any change. PUT, DELETE and the safe methods are also **idempotent**: sending the same request twice has the same intended effect as sending it once. [@rfc9110] POST is neither. PATCH is not idempotent by default either. [@rfc5789]

This matters for retries. RFC 9110 says a client should not retry a non-idempotent request automatically, unless it has some other way to know it is safe. [@rfc9110] For POST, that way is an [idempotency key](/learn/idempotency-retries-timeouts). In its v1 API, Stripe accepts an `Idempotency-Key` header on every POST. If the same key comes with different parameters, it returns an error. [@stripe-idem]

**Status codes that matter.** These cover most APIs. [@rfc9110]

- **200 OK**: it worked, here is the result.
- **201 Created**: a new resource exists. The `Location` header points to it.
- **204 No Content**: it worked, and there is no body.
- **400 Bad Request**: the request is broken, for example bad JSON.
- **401 Unauthorized**: no valid login. **403 Forbidden**: the server understood the request but refuses it, for example because the user lacks permission.
- **404 Not Found**: no such resource. A server may also use it to hide a resource the user may not see.
- **409 Conflict**: the request clashes with the current state, for example cancelling an order that has shipped.
- **422 Unprocessable Content**: the syntax is fine, but the content makes no sense, like quantity -1.
- **500 Internal Server Error**: a bug or surprise on the server.
- **503 Service Unavailable**: overloaded or in maintenance, try later.

There is one more: **429 Too Many Requests** means the client sent too many requests in a given time. It may carry a `Retry-After` header. [@rfc6585] See [Rate limiting](/learn/rate-limiting).

**One error format.** RFC 9457 defines "problem details": a JSON body with the media type `application/problem+json`. It has five standard fields:

- `type`: a URI that names the kind of problem.
- `title`: a short label for that kind of problem.
- `status`: the HTTP status code.
- `detail`: an explanation of this case, to help the client fix it.
- `instance`: a URI for this one occurrence.

You may add your own fields, and clients must ignore fields they do not know. [@rfc9457] Pick one format and use it on every endpoint. Then clients write the error handling once.

**Pagination and filtering.** Never return a whole collection in one answer. Stripe uses cursor pagination: you send `limit` and `starting_after`, and the answer has `has_more`. [@stripe-pages] Google uses `page_size`, `page_token` and `next_page_token`, and its page tokens must be opaque. [@aip158] Filters are query parameters, like `GET /orders?status=shipped`.

Add pagination from day one. Both Google and Microsoft say adding it later is a breaking change. [@aip158] [@msft]

**Versioning and compatibility.** Adding something new, like a field or an endpoint, is usually safe. Removing a field breaks clients. Google treats renaming as "remove and add", so it breaks them too. Changing a field's type also breaks them. [@aip180]

When you must break something, release a new version and deprecate the old one slowly. Stripe puts `/v1/` in the URL and sets a dated version per account. A client can choose another one with the `Stripe-Version` header. Since September 2024, its monthly versions have no breaking changes, and breaking ones come only twice a year. [@stripe-versions] Microsoft uses a required `api-version` query parameter with a date, like `2021-06-04`. [@msft]

**Naming.** Use plural nouns for collections, like `/orders`. Pick one style for field names and keep it everywhere. Microsoft uses camelCase for JSON fields. [@msft] Stripe uses snake_case, like `has_more`. [@stripe-pages] Both work. Mixing them does not.

**Document it.** The OpenAPI Specification is a standard way to describe an HTTP API in a file. People and tools can then understand the API without reading its code. Tools use the file to generate docs, client and server code, and tests. [@openapi]

**When not REST.** REST is a good default for public and mobile APIs, but not the only choice.

- **gRPC**: calls a method on another server as if it were local, with Protocol Buffers as the contract. It suits calls between your own services. [@grpc]
- **GraphQL**: the client asks for exactly the fields it needs, often in one request. It suits screens that need data from many resources. [@graphql]
- **Webhooks**: the server pushes events to your HTTPS endpoint when they happen. It retries failed deliveries, and the same event can arrive twice, so handle duplicates. [@stripe-webhooks]

## Trade-offs

| Pagination | How it works | Jump to page 7? | Stable while rows are added? | Use it when |
|---|---|---|---|---|
| Offset (`?offset=40&limit=20`) | Skip 40 rows, return the next 20. | Yes. Easy page numbers. | No. Items can repeat or be skipped. Deep pages get slow, because the database still reads the skipped rows. [@slack-pages] | Small lists that rarely change, like an admin table. |
| Cursor (`?cursor=c_7Hq&limit=20`) | Return 20 rows after the item the cursor marks. | No. You can only go next or back. There is no total page count. [@slack-pages] | Yes. The window does not move when new rows arrive. [@slack-pages] | Feeds, messages, orders, any large or busy list. |

**Simple rule:** start with cursors and opaque tokens. You can always add filters later. You cannot easily switch a public API from offset to cursor without a new version.

## At work: how a senior uses this

- Design the API from the client's screens first. Ask: **"What does the app need to show, and in how many calls?"**
- Treat every response field as a promise. Before you remove or rename one, ask: **"Which app versions still read it?"**
- Make new request fields optional. Google says new required fields must not be added to existing requests. [@aip180]
- For every POST that creates something or moves money, ask: **"What happens if this runs twice?"** Then add an idempotency key.
- Return 4xx for client mistakes and 5xx for server faults. Alerts on 5xx then mean real server problems.
- Use one error format on every endpoint. Put a stable error `type` in it, so clients never parse the message text.
- Write the OpenAPI file in the same pull request as the code. Review the file like code.
- Plan deprecation: tell clients early, measure who still calls the old version, and remove it only when traffic is near zero.

## With AI

### What to ask the agent

> "Write an OpenAPI spec for an orders API: create, get, list with cursor pagination, and update. Use RFC 9457 problem details for every error. Use camelCase field names."

> "Compare this OpenAPI file with the version on main. List every change that could break an existing client, like a removed, renamed or retyped field."

> "Add an Idempotency-Key header to POST /orders. Store the key with the order in one transaction, and reject a reused key with a different body."

### What to check yourself

- Field names, casing and plural collection names are the same on every endpoint.
- Every error uses the same problem details shape, with the right status code. No 200 with `"error": true` in the body.
- POST endpoints that create things take an idempotency key. GET never changes data.
- List endpoints are paginated, with a limit and a maximum page size.
- Nothing was removed, renamed or made required compared with the last version. An agent may "tidy up" names, just like the developer in the story.
- The status codes the code really returns match the ones in the OpenAPI file.

## Check yourself

## Go deeper

- [Idempotency, retries and timeouts](/learn/idempotency-retries-timeouts): how one retry can charge twice.
- [Rate limiting](/learn/rate-limiting): what to do before you send 429.
- Coming next on this line: [API pagination and filtering](/learn/api-pagination-and-filtering) and [API versioning and lifecycle](/learn/api-versioning-and-lifecycle).
