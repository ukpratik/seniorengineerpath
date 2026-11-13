---
topic: api-versioning
subtitle: What really breaks a client, how to change an API without breaking it, and how to retire an old version on purpose.
status: published
reviewed_by: ai-factcheck
reviewed_on: 2026-11-09
illustrative_story: false
sources:
  - id: stripe-blog
    title: "Brandur Leach, “APIs as infrastructure: future-proofing Stripe with versioning”, Stripe blog (5 August 2017)"
    url: https://stripe.com/blog/api-versioning
    note: Dated versions, pinning an account on its first request, and version change modules that turn new responses into old ones.
  - id: stripe-versions
    title: "Stripe API reference: Versioning"
    url: https://docs.stripe.com/api/versioning
    note: Monthly releases with no breaking changes, two major releases a year, and the Stripe-Version header.
  - id: stripe-upgrades
    title: "Stripe documentation: Upgrade your integration"
    url: https://docs.stripe.com/upgrades
    note: How a client moves to a new version, sees which versions it uses, and moves webhook endpoints safely.
  - id: aip180
    title: "Google, AIP-180: Backwards compatibility"
    url: https://google.aip.dev/180
    note: Source, wire and semantic compatibility, with rules on enums, defaults and stricter formats.
  - id: hyrum
    title: "Hyrum Wright, “Hyrum's Law” (hyrumslaw.com)"
    url: https://www.hyrumslaw.com/
    note: Why users depend on behaviour you never promised.
  - id: gh-versions
    title: "GitHub Docs: API Versions"
    url: https://docs.github.com/en/rest/about-the-rest-api/api-versions
    note: A date-based version header, a 24-month support window, and a list of breaking and additive changes.
  - id: gh-graphql
    title: "GitHub Docs: Breaking changes (GraphQL API)"
    url: https://docs.github.com/en/graphql/overview/breaking-changes
    note: How a versionless GraphQL API announces breaking changes three months ahead, and what counts as dangerous.
  - id: graphql-design
    title: "GraphQL Foundation, “Schema Design” (graphql.org)"
    url: https://graphql.org/learn/schema-design/
    note: Why GraphQL APIs usually evolve without versions.
  - id: graphql-spec
    title: "GraphQL Specification, October 2021 edition, section 3.13.3 “@deprecated”"
    url: https://spec.graphql.org/October2021/
    note: The built-in directive that marks a field or enum value as deprecated, with a reason.
  - id: msft
    title: "Microsoft, Azure REST API Guidelines (GitHub, microsoft/api-guidelines)"
    url: https://github.com/microsoft/api-guidelines/blob/vNext/azure/Guidelines.md
    note: A required api-version query parameter with a date, no version in the path, and extensible enums.
  - id: rfc8594
    title: "E. Wilde, RFC 8594: The Sunset HTTP Header Field (IETF, May 2019)"
    url: https://www.rfc-editor.org/rfc/rfc8594
    note: A response header that says when a resource is likely to stop responding.
  - id: rfc9745
    title: "S. Dalal and E. Wilde, RFC 9745: The Deprecation HTTP Response Header Field (IETF, March 2025)"
    url: https://www.rfc-editor.org/rfc/rfc9745
    note: A response header that says a resource is or will be deprecated, and how it works with Sunset.
claims_checked:
  - claim: "Stripe builds each response at the current version, then walks back through version change modules until it reaches the client's version."
    source: stripe-blog
    note: "'the API initially formats data by describing an API resource at the current version'; it then 'walks back through time and applies each version change module'."
  - claim: "Since 2024-09-30.acacia, Stripe releases monthly versions with no breaking changes, and breaking changes come in two major releases a year."
    source: stripe-versions
    note: "'we release new API versions monthly with no breaking changes. Twice a year, we issue a new major release ... that starts with an API version containing breaking changes.'"
  - claim: "Changing a default value is breaking and must not be done; user code may not handle new enum values gracefully."
    source: aip180
    note: "'Changing the default value is considered breaking and must not be done'; 'it is possible that user code does not handle new values gracefully'."
  - claim: "GitHub's REST API reads X-GitHub-Api-Version, defaults to 2022-11-28, supports old versions for 24 months after a newer one, and returns 410 Gone for unsupported versions."
    source: gh-versions
    note: "API Versions page: default 2022-11-28; 24-month support window; 410 Gone; adding enum values is additive, removing them is breaking."
  - claim: "Deprecation is an @ Unix-seconds date, and the Sunset timestamp must not be earlier than the Deprecation timestamp."
    source: rfc9745
    note: "Example 'Deprecation: @1688169599'; 'The timestamp given in the Sunset HTTP header field MUST NOT be earlier than the one given in the Deprecation header field.'"
  - claim: "Sunset is an HTTP-date that says when a resource is likely to become unresponsive, and clients should treat it as a hint."
    source: rfc8594
    note: "'Sunset = HTTP-date'; 'likely to become unresponsive at a specified point in the future'; timestamps are 'hints'."
quiz:
  - q: Your response has `status` with values `pending` and `paid`. You add a third value, `refunded`. Is that safe?
    options:
      - Yes. Adding is always safe.
      - Not always. Old client code may not handle a value it has never seen.
      - No. It always fails JSON parsing.
    answer: 1
    why: Google's AIP-180 warns that user code may not handle new enum values gracefully, and GitHub's GraphQL docs call a new enum value a "dangerous" change.
  - q: You change the default page size of a list endpoint from 50 to 20. No field changed. Is this a breaking change?
    options:
      - No, because the schema is the same.
      - Yes. Clients that relied on the old default now get different results.
      - Only for clients that send a page size.
    answer: 1
    why: AIP-180 says changing a default value is breaking and must not be done. Clients that send their own value are not affected.
  - q: In Stripe's 2017 design, how does an old client get the old response shape?
    options:
      - Stripe runs a separate copy of the old code for each version.
      - The client must parse the newest shape itself.
      - Stripe builds the newest shape, then applies version change modules backwards until it reaches the client's version.
    answer: 2
    why: The Stripe post describes building the response for the current version, then walking back through each version change module.
  - q: "A response has `Deprecation: @1798761599` (31 December 2026) and `Sunset: Tue, 30 Jun 2026 23:59:59 GMT`. What is wrong?"
    options:
      - Nothing. Any two dates are fine.
      - The Sunset date is earlier than the Deprecation date, which RFC 9745 does not allow.
      - Deprecation must use the same date format as Sunset.
    answer: 1
    why: RFC 9745 says the Sunset date must not be earlier than the Deprecation date. RFC 9745 uses an `@` timestamp, and RFC 8594 uses an HTTP date.
  - q: Which fact best tells you an old version is safe to switch off?
    options:
      - The deprecation email went out six months ago.
      - Your request logs show almost no traffic on it, and you have contacted the clients that remain.
      - The new version has been live for a year.
    answer: 1
    why: Announcements and age do not prove anyone moved. Measured traffic per client does.
incidents: []
glossary_optout: [rfc]
related: [rest-api, schema-evolution, api-gateway, webhooks, schema-migrations, contract-tests, graphql-bff]
---

## What broke

Stripe's API moves money for many businesses. Once a business connects its code, it expects that code to keep working for years. A Stripe engineer compared the API to a power grid or water supply: after you hook it up, it should run without interruption. [@stripe-blog]

In a 2017 post, Stripe gave an example of a change that would break that promise. A bank account had a true/false field called `verified`. Suppose Stripe replaced it with a text field called `status`. Every client that read `verified` would break, "because it depends on a field that no longer exists." [@stripe-blog]

Stripe still needed to make changes like that. By 2017 it had made "almost a hundred backwards-incompatible upgrades" in six years. It also said it had kept compatibility with every version since the company started in 2011. [@stripe-blog]

How do you change an API a hundred times and still not break code written years ago? That is what this lesson is about. Stripe's answer used dated versions and a translation layer. You will see both, plus the cheaper habit that should come first.

## The idea in simple words

A **breaking change** is any change that makes a working client fail or behave wrongly. [REST API design](/learn/rest-api-design) covered the obvious ones: removing, renaming or retyping a field.

The surprising ones are changes in behaviour. Hyrum Wright put it this way: with enough users, "all observable behaviors of your system will be depended on by somebody." [@hyrum] That is **Hyrum's law**. Your docs do not decide what is breaking. Your users' code does.

So you have three tools, in this order:

1. **Additive change first.** Add new fields and endpoints next to the old ones. Nothing breaks.
2. **Versions** when you must break something. Old clients keep the old contract.
3. **Deprecation and sunset** to retire an old version on purpose, with dates and data.

> [!key] **The key idea.** A version is a promise you have to keep running. Add instead of change, version only when you must, and switch an old version off only when your logs show nobody is left.

## How it works

### The surprising breaking changes

Google's AIP-180 names three kinds of compatibility. Old code must still compile, still talk to the server, and still get "what most reasonable developers would expect." [@aip180] The third kind hides most surprises.

- **New enum values.** You add `refunded` to a `status` field. AIP-180 warns that user code may not handle new values gracefully. [@aip180] GitHub's GraphQL docs call this a "dangerous" change: queries still work, but clients may behave differently. [@gh-graphql] An old `switch` with no default case may crash or do nothing.
- **Tighter validation.** AIP-180 gives an example: if name formats become stricter, a request that used to succeed now fails. [@aip180]
- **Changed defaults.** A client that never sent a page size silently gets a different result. AIP-180 says changing a default value is breaking and must not be done. [@aip180]
- **New required inputs.** New required fields must not be added to existing requests. [@aip180]
- **New error codes.** Microsoft's guidelines say not to add new top-level error codes without a new version. [@msft]

Rules differ between companies. GitHub's REST docs list adding an enum value as additive, and removing one as breaking. [@gh-versions] Microsoft says a new enum value can be handled with an **extensible enum**, and removing a value breaks customer code. [@msft] Our advice: tell clients in your docs to expect new values and to handle unknown ones.

### Additive change first

Most changes do not need a version. Stripe says it is safe to add a new endpoint, or a new field that was never there before. [@stripe-blog] To "rename" a field, add the new one and keep sending the old one. This is [expand and contract](/learn/schema-migrations-zero-downtime) at the API level.

Additive change only works if clients ignore fields they do not know. Say this in your docs, and test it in your own SDKs.

### Versioning schemes

When you must break something, you need a way for each client to ask for the old contract.

- **URL path**, like `/v2/orders`. Easy to see and to route in an [API gateway](/learn/api-gateway-and-edge). But each new major version forces every client to move.
- **Query parameter.** Azure requires `api-version` on every call, as a date like `2021-06-04`. It returns 400 if the value is missing or unknown. It also says not to put a version in the path. [@msft]
- **Header with a date.** GitHub's REST API reads `X-GitHub-Api-Version`. Without it, you get `2022-11-28`. Old versions are supported for 24 months after a newer one ships. [@gh-versions]
- **Dated version pinned to the account.** In its 2017 design, Stripe pinned an account to the newest version on its first request. A client could override it per call with the `Stripe-Version` header. [@stripe-blog] Since `2024-09-30.acacia`, monthly versions have no breaking changes. Breaking changes come in two major releases a year. [@stripe-versions]
- **No versions (GraphQL).** A GraphQL server returns only the fields a query asks for, so adding fields breaks nobody. That is why GraphQL APIs are usually versionless. [@graphql-design] You mark old fields with the built-in `@deprecated(reason: ...)` directive. [@graphql-spec] GitHub announces GraphQL breaking changes at least three months ahead, and makes them only at the start of a quarter. [@gh-graphql]

### The translation layer

Many versions can mean many copies of the code. Stripe avoided that. Each breaking change lives in one small **version change module**: docs, a transformation, and the resource types it touches. [@stripe-blog]

The core code builds every response in the newest shape. Then the layer walks back through the modules, newest first, until it reaches the client's version. [@stripe-blog] Step through it. Use Next and Back, or the ← and → keys.

[[diagram:version-translation-walkback]]

A tiny version of the idea:

```ts
// Newest first. Each module knows how to undo its own change.
const changes = [
  { version: "2025-03-01", undo: (c: any) => { c.email = c.emails[0] ?? null; delete c.emails; } },
  { version: "2024-06-01", undo: (c: any) => { c.verified = c.status === "verified"; delete c.status; } },
];

export function render(body: any, clientVersion: string) {
  for (const ch of changes) {
    if (ch.version > clientVersion) ch.undo(body); // ISO dates sort as strings
  }
  return body;
}
```

Changes that do more than reshape a response are harder. Stripe marks such a module `has_side_effects`. Its transformation then does nothing, and other code checks whether the change is active for this request. Stripe tries to avoid them. [@stripe-blog] Every version is "more code to understand and maintain." [@stripe-blog]

Do not forget [webhooks](/learn/webhooks-and-async-apis). Stripe renders webhook events in the endpoint's version, or the account default. [@stripe-versions] To upgrade, you create a second endpoint on the new version and move over. [@stripe-upgrades]

### Deprecation and sunset

Two response headers tell clients what is coming.

```http
Deprecation: @1767225599
Sunset: Thu, 31 Dec 2026 23:59:59 GMT
Link: <https://api.example.com/docs/v1-retirement>; rel="deprecation"
```

`Deprecation` comes from RFC 9745. Its value is a Unix timestamp after `@`. A past date means "already deprecated", a future date means "will be". A `rel="deprecation"` link points to human docs. [@rfc9745]

`Sunset` comes from RFC 8594. It says when the resource will likely stop responding. It is a hint, not a promise. [@rfc8594] The Sunset date must not be earlier than the Deprecation date. [@rfc9745]

When the day comes, answer with a clear error. GitHub returns `410 Gone` for a version it no longer supports. [@gh-versions]

### Measure, then communicate

Headers only help if someone reads them. So log the version and the client id on every request. Then you can answer: "Who still calls v1, how often, and on which endpoints?" Stripe shows each customer the versions its own integration uses. [@stripe-upgrades]

Our advice: email the owners of the remaining clients by name, not only a mailing list. Before the final date, consider short planned failures in quiet hours. A client that ignored every email will notice those.

## Trade-offs

| Scheme | Easy to see and route? | Cost for clients | Cost for you | Use it when |
|---|---|---|---|---|
| URL path (`/v2/`) | Yes. | High. Each major version is a big move. | Medium. Two code paths. | Rare, large redesigns. |
| Query or header with a date | Partly. You must read the parameter or header. | Low. Small steps. | Medium. One version per release. | Public APIs that change often. |
| Pinned account plus translation layer | No. It is hidden in account settings. | Lowest. Old code keeps working. [@stripe-blog] | High. Every module must be maintained. [@stripe-blog] | Long-lived integrations, like payments. |
| Versionless (GraphQL) | No version to route. | Low, until a field is removed. | Low, if you only add. [@graphql-design] | Clients pick their own fields. |

**Simple rule:** try an additive change first. Version only when you must break something.

## At work: how a senior uses this

- In every API review, ask: **"Could any client see a different result after this change?"** That includes defaults, validation, error codes and enum values.
- Treat new enum values as a change to announce. Tell clients up front to handle unknown values.
- Before you deprecate, ask: **"Who calls this today?"** Answer with request logs grouped by client and version, not guesses.
- Put a date on every deprecation. Send `Deprecation` and `Sunset` headers, with a link to a migration guide.
- Write the support window into your public docs, the way GitHub states 24 months. [@gh-versions]
- Keep old-version rules in one layer. If version checks spread into business code, stop and refactor.
- Add a contract test for each supported version, so nobody breaks an old shape by accident.

## With AI

### What to ask the agent

> "Compare this OpenAPI file with the one on main. List every change an existing client could notice, including new enum values, changed defaults, stricter validation and new error codes. Mark each one breaking, dangerous or safe."

> "Write middleware that reads an API version header, logs it with the client id, and adds Deprecation and Sunset headers for versions older than 2025-01-01."

> "Turn this breaking change into a version change module with a function that converts the new response shape back to the old one. Add a test for both versions."

### What to check yourself

- The agent did not call a new enum value or a changed default "non-breaking" just because the schema shape stayed the same.
- `Deprecation` uses the `@` timestamp format, and `Sunset` uses an HTTP date. [@rfc9745] [@rfc8594]
- Version strings are full ISO dates like `2024-06-01` (not `2024-6-1`), so comparing them as strings sorts them correctly.
- Every translation step has a test with a real old response as the expected output.
- Webhook payloads follow the same version rules as API responses.

## Check yourself

## Go deeper

- [REST API design](/learn/rest-api-design): the basic breaking changes and why old mobile apps matter.
- [Schema migrations](/learn/schema-migrations-zero-downtime): expand and contract, one layer down in the database.
- [API gateway](/learn/api-gateway-and-edge): routing `/v1` and `/v2` at the edge.
- [Webhooks and async APIs](/learn/webhooks-and-async-apis): events your API pushes, which need versions too.
- [Schema evolution and encoding](/learn/schema-evolution-and-encoding): how JSON, Protobuf and Avro let old and new code read each other's data.
- Coming later: contract and integration tests.
