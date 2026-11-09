---
topic: authorization-models
subtitle: Roles, attributes and relationships. Pick a permission model that still works when your product grows.
status: published
reviewed_by: ai-factcheck
reviewed_on: 2026-11-09
illustrative_story: false
sources:
  - id: gh-npm
    title: "Mike Hanley, “GitHub’s commitment to npm ecosystem security”, The GitHub Blog (15 November 2021)"
    url: https://github.blog/open-source/githubs-commitment-to-npm-ecosystem-security/
    note: GitHub's own disclosure of the npm publish bug, its root cause across microservices, and the fix.
  - id: nist-rbac
    title: "NIST Computer Security Resource Center, “Role Based Access Control” (project page)"
    url: https://csrc.nist.gov/projects/role-based-access-control
    note: The history of RBAC, the 1992 Ferraiolo and Kuhn paper, and the ANSI/INCITS 359 standard.
  - id: nist-abac
    title: "Hu, Ferraiolo, Kuhn et al., NIST SP 800-162: Guide to Attribute Based Access Control (ABAC) Definition and Considerations (January 2014, updated 2019)"
    url: https://nvlpubs.nist.gov/nistpubs/specialpublications/NIST.SP.800-162.pdf
    note: The ABAC definition, role explosion, the policy decision and enforcement points, and why "who has access?" audits are hard (section 3.1.2.3).
  - id: zanzibar
    title: "Pang et al., Zanzibar: Google’s Consistent, Global Authorization System, USENIX ATC 2019"
    url: https://www.usenix.org/system/files/atc19-pang.pdf
    note: Relation tuples, userset rewrites, the Check and Expand APIs, the new enemy problem and zookies (sections 2.1 to 2.4).
  - id: owasp-authz
    title: "OWASP Cheat Sheet Series, Authorization Cheat Sheet"
    url: https://cheatsheetseries.owasp.org/cheatsheets/Authorization_Cheat_Sheet.html
    note: Why OWASP prefers ABAC and ReBAC over RBAC for apps, deny by default, and testing and logging advice.
  - id: owasp-bola
    title: "OWASP API Security Top 10 (2023), API1:2023 Broken Object Level Authorization"
    url: https://api-security.owasp.org/editions/2023/en/0xa1-broken-object-level-authorization
    note: Number one on the OWASP API list, why it is "extremely common", three attack scenarios, and how to prevent it.
  - id: opa
    title: "Open Policy Agent documentation, “Open Policy Agent (OPA)”"
    url: https://www.openpolicyagent.org/docs
    note: A general-purpose policy engine that keeps policy decisions apart from enforcement. Policies are written in Rego.
  - id: cedar
    title: "Cedar Policy Language Reference Guide, “Basic policy construction in Cedar”"
    url: https://docs.cedarpolicy.com/policies/syntax-policy.html
    note: Permit and forbid policies, the default deny, and why one forbid beats any permit.
  - id: openfga
    title: "OpenFGA documentation, “Introduction to OpenFGA”"
    url: https://openfga.dev/docs/fga
    note: An open source, Zanzibar-inspired ReBAC system owned by the CNCF.
  - id: openfga-lang
    title: "OpenFGA documentation, “Configuration Language”"
    url: https://openfga.dev/docs/configuration-language
    note: Type restrictions, the "or" operator and "X from Y" rules used in the worked example.
  - id: openfga-queries
    title: "OpenFGA documentation, “Relationship Queries: Check, Read, Expand, ListObjects and ListUsers”"
    url: https://openfga.dev/docs/interacting/relationship-queries
    note: When to use Check, Read, ListObjects and ListUsers, and the limits on ListObjects.
  - id: spicedb
    title: "AuthZed, “SpiceDB Documentation”"
    url: https://authzed.com/docs/spicedb/getting-started/discovering-spicedb
    note: Another open source, Zanzibar-inspired permissions database.
claims_checked:
  - claim: "The npm registry's authorization service checked the package named in the request URL path, but the publishing service chose the package from the uploaded file's contents; reported on 2 November 2021 and patched within six hours."
    source: gh-npm
    note: "Matches the post. Telemetry goes back to September 2020; GitHub says \"high confidence\" it was not exploited maliciously in that window."
  - claim: "Zanzibar prevents the new enemy problem with zookies: a content-change check returns a zookie, the client stores it with the content version, and later checks run at a snapshot at least as fresh."
    source: zanzibar
    note: "Sections 2.2 and 2.4.4. Both new enemy examples (A and B) match the paper."
  - claim: "Zanzibar responds to more than 95% of checks within 10 milliseconds and handles hot spots by caching and deduplicating simultaneous requests."
    source: zanzibar
    note: "Section 1. Expand returns a userset tree and is used to build access-aware search indices (section 2.4.5)."
  - claim: "Finding every subject with access to one object in ABAC may mean simulating the request for every known subject."
    source: nist-abac
    note: "Section 3.1.2.3. Role explosion wording (\"often termed\") and the ABAC definition also checked."
  - claim: "Cedar denies by default, and an explicit forbid overrides any permit."
    source: cedar
    note: "Both sentences are on the page. The OpenFGA model was validated with @openfga/syntax-transformer, and the fgaClient.check call and { allowed } result match the JS SDK README."
quiz:
  - q: Your docs app has roles "viewer", "editor" and "admin". A customer wants to share one doc with one outside person. What happens if you only use RBAC?
    options:
      - Nothing changes. Give the person the "viewer" role.
      - You need a new role per doc, or the person can view every doc. Roles start to multiply.
      - RBAC cannot store outside users at all.
    answer: 1
    why: A role applies to every object. Sharing one object needs a narrow role per object. This is often called "role explosion".
  - q: Which question is hardest to answer in a pure ABAC system?
    options:
      - May this user read this file right now?
      - Who can read this file?
      - Which attributes does this request carry?
    answer: 1
    why: NIST SP 800-162 says finding every subject with access to one object may mean simulating the request for every known subject.
  - q: In Zanzibar, Alice removes Bob from a doc's ACL, then adds secret text. What stops a check from using the old ACL for the new text?
    options:
      - A cache TTL of 10 seconds.
      - The zookie stored with the content version. Checks for it run on a snapshot at least as fresh.
      - Bob's session expires.
    answer: 1
    why: The client stores a zookie with each content change and sends it with later checks. The check snapshot must be at least as fresh as that zookie.
  - q: In the npm bug, the authorization service checked the package in the URL. The publisher used the package inside the uploaded file. What is the lesson?
    options:
      - Use longer package names.
      - Move the check to the client.
      - Check permission on the exact object the action will change, using the same data.
    answer: 2
    why: The check passed for one package, and the write happened to another. GitHub fixed it by making both services use the same package.
incidents: [github-2021-11-npm-publish-authz]
cases: [google-zanzibar]
related: [authn-authz, owasp-secure-coding, multi-tenancy, api-gateway, hexagonal, caching-stampedes]
---

## What broke

npm is a package registry for JavaScript code. On 2 November 2021, GitHub, which runs npm, got a report through its bug bounty program. [@gh-npm]

The report showed that an attacker could publish new versions of any npm package from an account without proper authorization. GitHub patched it within six hours. [@gh-npm]

The cause was not a missing check. The check existed. The npm registry ran on several microservices. The authorization service checked the user's rights for the package named in the request URL. [@gh-npm]

But the service that wrote to the registry did not read the URL. It chose the package from the contents of the uploaded file. So a request could be authorized for one package and then applied to a different one. [@gh-npm]

GitHub fixed it by making both services use the same package for the check and for the publish. Its telemetry went back only to September 2020. The bug was older than that. For that window, GitHub said "with high confidence" that it was not exploited maliciously. [@gh-npm]

The lesson for this page: an authorization check is only as good as its model and its placement. You must check the right object, in the right place, with rules that match how the product shares things.

## The idea in simple words

The [Authentication and authorization](/learn/authentication-and-authorization) lesson showed one check: "Does this order belong to this user?" Real products need more. People share folders, become members of teams, and become admins of a whole company account.

There are three common ways to model this.

- **RBAC (role-based access control).** Permissions belong to roles. Users get roles. "Editors may edit docs."
- **ABAC (attribute-based access control).** A policy reads facts about the user, the object, the action and the environment. "Users in finance may read invoices during work hours."
- **ReBAC (relationship-based access control).** Access comes from links between users and objects. "You may view this doc because you are in a group that can view its folder."

Most real systems mix them. Roles for staff tools, relationships for sharing, and a few attribute rules on top.

> [!key] **The key idea.** Choose the model that matches how your product shares things. Then put the check in one place, on the server, for the exact object the action will touch.

## How it works

### RBAC: users, roles, permissions

In RBAC, each user gets one or more roles. Each role gets one or more privileges. David Ferraiolo and Rick Kuhn formalized it in 1992. The NIST model became the standard ANSI/INCITS 359-2004. [@nist-rbac]

RBAC is easy to audit. You can list who has the "admin" role. Its weak spot is per-object rules. A role applies to every object. To share one doc with one person, you need a narrow new role. Many small, one-off roles pile up. This is often called "role explosion". NIST SP 800-162 describes it for rules that depend on context. [@nist-abac]

OWASP adds a practical problem. With many roles, code fills up with checks like `hasAnyRole("SUPERUSER", "ADMIN", "ACCT_MANAGER")`. It is easy to miss one. [@owasp-authz]

### ABAC: rules over attributes

NIST SP 800-162 defines ABAC as allowing or denying each request based on attributes of the subject and the object, environment conditions, and a set of policies. [@nist-abac] An attribute is a name-value pair, like `department=finance` or `time=21:00`. [@owasp-authz]

NIST splits the work into parts. The **policy enforcement point** sits in your app and blocks or allows. The **policy decision point** evaluates the rules and returns the answer. [@nist-abac]

ABAC is flexible, but it has a real cost. "Who can see this file?" becomes a hard question. NIST says it may mean simulating the request for every known subject. [@nist-abac] Auditors often ask exactly that question.

### ReBAC and Zanzibar

Google built one authorization system, Zanzibar, for hundreds of services, including Drive, Calendar and YouTube. [@zanzibar] It stores permissions as **relation tuples**: `object#relation@user`. For example, `doc:readme#owner@10` means user 10 owns doc:readme. [@zanzibar]

A user in a tuple can also be a set, such as "members of group:eng". So groups can contain groups. [@zanzibar] A schema adds rules. One rule says "all owners are editors, and all editors are viewers". Another says viewers of the parent folder are viewers of the doc. [@zanzibar]

A check asks: "Does user U have relation R to object O?" [@zanzibar] Step through one check. Use Next and Back, or the ← and → keys.

[[diagram:rebac-check-walk]]

Zanzibar also had to solve the **"new enemy" problem**. The paper gives two examples. [@zanzibar]

1. Alice removes Bob from a folder's ACL. Then Charlie moves new documents into it. Bob must not see them. He may, if the check ignores the order of the two ACL changes.
2. Alice removes Bob from a document's ACL. Then Charlie adds new content. Bob must not see it. He may, if the check uses a stale ACL from before his removal.

The fix is a consistency token called a **zookie**. When content is about to be saved, the client sends a content-change check. If it is allowed, the response holds a zookie, and the client stores it with that content version. Later checks send the zookie. The check snapshot must then be "at least as fresh" as it. [@zanzibar]

This is still fast. Zanzibar answers more than 95% of checks within 10 milliseconds. It caches results and merges duplicate requests. [@zanzibar] See [Caching and cache stampedes](/learn/caching-and-cache-stampedes) for the same ideas.

### One rule, two models

Take a docs app. Docs have an owner, editors and viewers. Docs live in shared folders. Org admins may edit every doc in their org.

In RBAC, the rule looks like this:

```ts
// Any "editor" may edit any doc in the org.
function canEdit(user: User, doc: Doc): boolean {
  return user.orgId === doc.orgId &&
    (user.roles.includes("editor") || user.roles.includes("org_admin"));
}
```

This is simple, but it cannot say "Asha may edit this one doc". In ReBAC, the same rules live in a schema. Here it is in OpenFGA's language: [@openfga-lang]

```text
model
  schema 1.1

type user

type org
  relations
    define admin: [user]

type group
  relations
    define member: [user, group#member]

type folder
  relations
    define parent: [folder]
    define editor: [user, group#member] or editor from parent
    define viewer: [user, group#member] or editor or viewer from parent

type doc
  relations
    define org: [org]
    define parent: [folder]
    define owner: [user]
    define editor: [user, group#member] or owner or editor from parent or admin from org
    define viewer: [user, group#member] or editor or viewer from parent
```

Each doc also needs a tuple that links it to its org, like `doc:plan#org@org:acme`. Without it, the admin rule never matches.

Sharing a doc is now one tuple, like `doc:plan#editor@user:asha`. No new role. The app asks one question:

```ts
// fgaClient is an OpenFgaClient from @openfga/sdk
const { allowed } = await fgaClient.check({
  user: "user:asha", relation: "editor", object: "doc:plan",
});
if (!allowed) throw new ForbiddenError();
```

### Where to enforce

Check on the server, on every request. OWASP says to deny by default and never trust checks on the client. [@owasp-authz] The check must be for the exact object. Missing it is broken object level authorization (BOLA). OWASP calls it "extremely common in API-based applications". [@owasp-bola]

The npm bug adds one more rule. The check and the action must use the same object ID, from the same source. [@gh-npm] Load the object once, check it, then act on that loaded object.

A gateway can check tokens. Object checks need your data, so they belong in the service. See [API gateways and edge concerns](/learn/api-gateway-and-edge). Put the check behind one interface, so the domain code does not care which engine answers. See [Clean and hexagonal architecture](/learn/clean-and-hexagonal-architecture).

### Policy engines and permission services

A **policy engine** keeps rules apart from app code. Your code sends a question with JSON input and gets a decision back.

- **OPA** is a general-purpose engine. Policies are written in a language called Rego. [@opa]
- **Cedar** uses `permit` and `forbid` policies. If no policy matches, the answer is deny. One matching `forbid` beats any `permit`. [@cedar]
- **OpenFGA** and **SpiceDB** are not policy engines in this sense. They are open source permission services inspired by Zanzibar. They store the relationships and answer checks. [@openfga] [@spicedb]

Keep policies in Git, reviewed like code. Pick a tool for your model, not for its marketing page.

### Checking versus listing

"May Asha open doc:plan?" is one check. "Show me every doc Asha can see" is a different problem. Checking each doc one by one is too slow.

OpenFGA's ListObjects API answers it, but its docs aim it at small collections. It has a time limit and a maximum number of results. [@openfga-queries] Zanzibar offers an Expand API, which returns the tree of users and groups behind a relation. Google's clients use it to build search indexes that respect access. [@zanzibar]

### Tenants, tests and audits

In a multi-tenant app, every query and every object ID must carry the tenant. Never let a tuple or a role from tenant A match an object in tenant B. See [Multi-tenancy and noisy neighbours](/learn/multi-tenancy-noisy-neighbours).

Write unit and integration tests for authorization logic. Log decisions, but not too much and not too little. [@owasp-authz]

## Trade-offs

| Model | Easy to answer "who has access?" | Per-object sharing | Main cost | Use it when |
|---|---|---|---|---|
| RBAC | Yes. List the role's members. | No. Needs a role per object. | Role explosion. [@nist-abac] | Staff tools and admin panels with a few fixed jobs. |
| ABAC | No. May need a simulation per subject. [@nist-abac] | Yes, through attributes like `owner`. | Hard to audit and to debug. | Rules depend on time, place, data labels or training. |
| ReBAC (Zanzibar style) | Yes. Expand or list the relation. [@zanzibar] | Yes. One tuple per share. | A new store to run and keep in sync. | Sharing, folders, teams and nested groups. |
| Policy engine on top | Depends on the model inside. | Depends. | Another service in the request path. | Many services must apply the same rules. |

**Simple rule:** start with a few roles. Add relationships when users start sharing things. Add attributes only for rules that truly depend on context.

## At work: how a senior uses this

- Before you add a role, ask: **"Is this really a relationship, like owner or member?"**
- In code review, ask: **"Is the object we check the same object we change?"**
- Keep one `authorize(user, action, object)` entry point. Ban role checks scattered in handlers.
- Write a permission matrix test: for each role or relationship, try each action, and expect allow or deny.
- Prepare for the auditor's question, "Who can see customer X's data?", before they ask.
- Plan for removal. When someone leaves a team, how fast does access end everywhere?
- Load-test the check path. It runs on every request.

## With AI

### What to ask the agent

> "Find every place in this repo that checks a role or permission. Group them by resource, and show any handler that reads or writes an object without a check."

> "For each endpoint that takes an object ID, show where the ID used for the authorization check comes from, and where the ID used for the write comes from. Flag any that differ."

> "Write an OpenFGA model for this app: users, groups, folders with parents, docs, and org admins. Then write tests for owner, shared folder, removed member and other-tenant cases."

### What to check yourself

- The check runs on the server and denies by default.
- The check and the action use the same loaded object, not two copies of the request.
- Every query and tuple includes the tenant.
- Removing a user from a group removes their access in a test.
- Listing pages filter by permission, not only by a hidden button.
- The AI did not invent APIs. Compare its calls with the engine's docs.

## Check yourself

## Go deeper

- [OWASP Top 10 and secure coding](/learn/owasp-top-10-secure-coding): broken access control in the wider list.
- [Scaling reads](/learn/scaling-reads): when the check service gets most of your traffic.
- Coming later: Enterprise SSO and SCIM, which feed users and groups into these models.
