---
topic: sso-scim
subtitle: Let each customer's own identity provider log people in and remove them, and check every answer it sends you.
status: published
reviewed_by: ai-factcheck
reviewed_on: 2026-11-10
illustrative_story: false
sources:
  - id: gh-saml
    title: "Peter Stöckli, Sign in as anyone: Bypassing SAML SSO authentication with parser differentials, The GitHub Blog (12 March 2025)"
    url: https://github.blog/security/sign-in-as-anyone-bypassing-saml-sso-authentication-with-parser-differentials/
    note: How two XML parsers in one library disagreed about which signature to check, and how that let an attacker log in as any user.
  - id: gitlab-patch
    title: "GitLab Critical Patch Release: 17.9.2, 17.8.5, 17.7.7"
    url: https://docs.gitlab.com/releases/patches/patch-release-gitlab-17-9-2-released/
    note: The vendor's advisory for the same flaw, who was affected, and the stopgaps for customers who could not upgrade at once.
  - id: owasp-saml
    title: "OWASP Cheat Sheet Series, SAML Security Cheat Sheet"
    url: https://cheatsheetseries.owasp.org/cheatsheets/SAML_Security_Cheat_Sheet.html
    note: The checks a service provider must run on every SAML response, and why IdP-initiated login is weaker.
  - id: oidc
    title: "OpenID Foundation, OpenID Connect Core 1.0"
    url: https://openid.net/specs/openid-connect-core-1_0.html
    note: Clock skew leeway, the email_verified claim, and why only iss plus sub identifies a user.
  - id: msrc-email
    title: "Microsoft Security Response Center, Potential Risk of Privilege Escalation in Azure AD Applications (20 June 2023)"
    url: https://www.microsoft.com/en-us/msrc/blog/2023/06/potential-risk-of-privilege-escalation-in-azure-ad-applications/
    note: A real case of apps trusting an email claim that nobody had verified.
  - id: rfc7643
    title: "Hunt, Grizzle, Wahlstroem and Mortimore, RFC 7643: System for Cross-domain Identity Management: Core Schema (IETF, 2015)"
    url: https://www.rfc-editor.org/rfc/rfc7643
    note: The User and Group resources, and the userName, externalId, active and members attributes.
  - id: rfc7644
    title: "Hunt, Grizzle, Ansari, Wahlstroem and Mortimore, RFC 7644: System for Cross-domain Identity Management: Protocol (IETF, 2015)"
    url: https://www.rfc-editor.org/rfc/rfc7644
    note: The SCIM HTTP API, with create, filter, PATCH and DELETE, and their status codes.
  - id: entra-prov
    title: "Microsoft Learn, What is automated app user provisioning in Microsoft Entra ID"
    url: https://learn.microsoft.com/en-us/entra/identity/app-provisioning/user-provisioning
    note: Why companies automate provisioning and deprovisioning, and where SCIM, SAML and OIDC fit.
  - id: entra-scim
    title: "Microsoft Learn, Tutorial - Develop a SCIM endpoint for user provisioning to apps from Microsoft Entra ID"
    url: https://learn.microsoft.com/en-us/entra/identity/app-provisioning/use-scim-to-provision-users-and-groups
    note: What one big identity provider actually sends to your SCIM endpoint, with request and response examples.
  - id: docker-prov
    title: "Docker Docs, User provisioning overview"
    url: https://docs.docker.com/security/provisioning/
    note: A short, real comparison of JIT provisioning and SCIM in one SaaS product.
  - id: docker-sso
    title: "Docker Docs, Set up single sign-on"
    url: https://docs.docker.com/security/authentication/single-sign-on/connect/
    note: Domain verification with a DNS TXT record, and what changes when SSO is enforced.
claims_checked:
  - claim: "Critical bugs in ruby-saml up to 1.17.0 came from two XML parsers disagreeing during signature checking; with one valid signature from the target's IdP, an attacker could forge an assertion for any user."
    source: gh-saml
    note: 'Verbatim "ruby-saml was using two different XML parsers during the code path of signature verification" (REXML and Nokogiri). CVE-2025-25291 and CVE-2025-25292, fixed in 1.18.0. Published 12 March 2025.'
  - claim: "GitLab released its critical patch on 12 March 2025, the same day as the write-up; IdP multi-factor authentication does not mitigate the flaw, and the stopgaps are GitLab 2FA and admin approval for auto-created users."
    source: gitlab-patch
    note: Verbatim "Enabling identity provider multi-factor authentication does not mitigate this vulnerability". Applies to GitLab instances using SAML, including GitLab.com. Earlier draft said "self-managed" only; reworded.
  - claim: "OWASP calls IdP-initiated (unsolicited) SAML login inherently less secure because it lacks login CSRF protection, and says to reject assertions outside the validated signed content."
    source: owasp-saml
    note: Also confirms InResponseTo, local schemas, ignoring KeyInfo, never getElementsByTagName, Destination, Audience, NotBefore/NotOnOrAfter, and XSW "described in 2012".
  - claim: "Only iss plus sub is a stable user identifier, email must not be used as a unique identifier, and clock-skew leeway is usually no more than a few minutes."
    source: oidc
    note: OIDC Core 1.0 section 5.7 (Claim Stability and Uniqueness) and section 2 (exp claim).
  - claim: "SCIM: a duplicate userName on POST gets 409, DELETE returns 204 and later operations 404, the meaning of active is defined by the service provider, and User.groups is read-only."
    source: rfc7644
    note: RFC 7644 sections 3.3, 3.5.2 and 3.6; active and groups are from RFC 7643 sections 4.1.1 and 4.1.2.
  - claim: "Entra disables a user with PATCH op Replace on active=false and expects the user to still be returned until a hard delete."
    source: entra-scim
    note: '"Disable User" example; Entra emits op as Add, Replace and Remove and asks for case-insensitive matching.'
quiz:
  - q: Your SAML library finds the signature and the assertion with two different lookups. An attacker adds a second, unsigned assertion. What is the risk?
    options:
      - None. The response still has a valid signature.
      - The library may check the signed assertion but read the user from the unsigned one.
      - The IdP will reject the response before it reaches you.
    answer: 1
    why: This is XML signature wrapping. You must read data only from the element the signature covers. OWASP says to reject assertions outside the signed content.
  - q: Two users sign in with SSO. Both IdPs send the email bob@acme.com. How should your app pick the account?
    options:
      - By email, because it is the same person.
      - By the tenant's IdP plus the IdP's stable subject ID.
      - By the display name and the email together.
    answer: 1
    why: OpenID Connect says only iss and sub together are a stable identifier. Email must not be used as a unique ID. Any IdP can send any email.
  - q: An employee leaves Acme. Acme uses JIT provisioning only, with no SCIM. What happens to their account in your app?
    options:
      - It is deleted at once.
      - It stays active in your app. They cannot log in through the IdP, but old sessions and API tokens can keep working.
      - The IdP sends your app a SAML logout for every user.
    answer: 1
    why: JIT creates and updates users at login. It does not deprovision. Without SCIM, nothing tells your app that the person left.
  - q: Microsoft Entra sends PATCH /Users/123 with active set to false. What should your app do?
    options:
      - Delete the user and all their data.
      - Ignore it, because the user can no longer log in through the IdP anyway.
      - Block the user, end their sessions and tokens, and keep returning the user from GET.
    answer: 2
    why: This is a soft delete. Entra expects the user to still come back from queries. Your app decides what inactive means, so make it end all access.
  - q: Why should you verify that a tenant owns a domain before you route its emails to that tenant's IdP?
    options:
      - So that one tenant cannot claim gmail.com or a rival's domain and capture other people's logins.
      - Because DNS is faster than a database lookup.
      - Because SAML does not work without DNS.
    answer: 0
    why: Domain routing decides which IdP a user trusts. An unverified claim would let any tenant send other companies' users to an IdP it controls.
glossary_optout: [acme, mitigation]
incidents: [github-2025-03-ruby-saml]
related: [authn-authz, multi-tenancy, authorization-models, owasp-secure-coding]
---

## What broke

On 12 March 2025, GitHub's security lab published a write-up called "Sign in as anyone". It described critical bugs in ruby-saml, a Ruby library that apps use to accept SAML logins. [@gh-saml]

The library used two different XML parsers while it checked a signature. An attacker could build a document that the two parsers read differently. One parser found one signature. The other parser found a different one. So the check passed on content that was never really signed. [@gh-saml]

The attacker needed one valid signature from the target company's identity provider. That could come from an ordinary user's login response. In some cases, it could come from the provider's public metadata. With it, they could forge an assertion for any user and log in as that user. [@gh-saml]

The researchers found a working instance in GitLab. GitLab released a critical patch on 12 March 2025. That was the same day as the write-up. On a GitLab instance with SAML login, "an attacker with access to a valid signed SAML document from the IdP could authenticate as another valid user". [@gitlab-patch]

GitLab's stopgaps show where the flaw was. Turn on two-factor login inside GitLab. Require admin approval for new users created on the fly. Multi-factor login at the identity provider "does not mitigate this vulnerability". [@gitlab-patch] The flaw was in the SAML library inside the app. The forged login never passed through the provider, so nothing the provider did could stop it.

The lesson: when a customer's identity provider logs users in to your app, your app must check every answer it receives. One weak check, and anyone can be anyone.

## The idea in simple words

A large customer has thousands of staff. It keeps them all in one directory, such as Okta or Microsoft Entra ID. It wants three things from every SaaS app it buys:

1. **Single sign-on.** Staff log in with their company account and company MFA. No separate password for your app.
2. **Provisioning.** New staff get an account on day one, with the right role.
3. **Deprovisioning.** When someone leaves, their access to your app ends the same day.

Two kinds of standards do this. SAML 2.0 and OpenID Connect (OIDC) handle the login. SCIM 2.0 handles creating, changing and removing users. Microsoft says SCIM "is becoming the de facto standard for provisioning". [@entra-prov]

The customer's system is the **identity provider (IdP)**. Your app is the **service provider (SP)** in SAML, or the **relying party (RP)** in OIDC. Each customer is a tenant with its own IdP. See [Multi-tenancy and noisy neighbours](/learn/multi-tenancy-noisy-neighbours).

> [!key] **The key idea.** In enterprise SSO, someone else's system tells you who the user is. Find the right IdP for the tenant, check every signed answer strictly, and let the IdP remove users, not only add them.

## How it works

Step through one SAML login. Use Next and Back, or the ← and → keys.

[[diagram:saml-sp-initiated-login]]

### SAML and OIDC in one table

The [authentication lesson](/learn/authentication-and-authorization) covered OIDC ID tokens. SAML does the same job with older tools.

| | SAML 2.0 | OIDC |
|---|---|---|
| The proof of login | An XML assertion, inside a Response | An ID token, which is a JWT |
| How it is signed | XML Signature | JSON Web Signature |
| How it reaches you | The browser posts it to your assertion consumer service (ACS) URL | Usually, your server swaps a code for it |
| Who it is for | `Audience` must be your entity ID [@owasp-saml] | `aud` must hold your client ID [@oidc] |

Our advice: support both if you sell to enterprises. For new work, prefer OIDC. It is simpler to check, because there is no XML.

### SP-initiated and IdP-initiated login

In **SP-initiated** login, the user starts at your app. You send an AuthnRequest with an ID. The response must return that ID in `InResponseTo`. [@owasp-saml]

In **IdP-initiated** login, the user clicks your app's tile in the IdP's portal. A response arrives that you never asked for. OWASP says this is "inherently less secure by design", because it has no login CSRF protection. [@owasp-saml] If customers need it, add replay detection, and check `RelayState` against an allowlist. [@owasp-saml]

### The checks on every SAML response

OWASP lists them. [@owasp-saml] In short:

- **Schema first.** Validate the XML against trusted local schema copies.
- **The right key.** Trust the X.509 certificate stored for this tenant. Ignore any `KeyInfo` in the message, so an attacker cannot send their own key.
- **Signed means used.** The signature's reference must cover the assertion you read. Never use `getElementsByTagName` to find security elements. Breaking this rule allows **XML signature wrapping**: the attacker keeps the signed part and adds an unsigned part that your code reads. These attacks on SAML were described in 2012. [@owasp-saml]
- **Destination and recipient.** `Destination` and `Recipient` must match your ACS URL exactly.
- **Audience.** `Audience` must be your entity ID. Otherwise a response meant for another app works on yours.
- **Time.** Check `NotBefore` and `NotOnOrAfter`.

Clocks on two systems never agree exactly. This is clock skew. The OIDC spec allows "some small leeway, usually no more than a few minutes". [@oidc] Use the same small window for SAML. Never turn off the time check to fix a skew error.

Do not write these checks yourself. Use a well-known library, keep it patched, and test it with bad responses.

### Routing users to the right IdP

Your login page has one email box. Many SaaS apps route by email domain:

```ts
const domain = email.split("@")[1].toLowerCase();
const conn = await ssoConnections.findVerified(domain);
if (conn) return redirectToIdp(conn); // tenant's SAML or OIDC
return showPasswordLogin();
```

The word that matters is "verified". A tenant proves it owns `acme.com` by adding a DNS TXT record with a value you give it. [@docker-sso] Without this, a tenant could claim `gmail.com` or a rival's domain.

Store one connection per tenant: IdP entity ID, SSO URL, signing certificates and your ACS URL. Allow two certificates at once, so the customer can rotate keys without downtime. Once SSO works, let the tenant admin **enforce** it. Docker, for example, then requires SSO for every login. [@docker-sso]

### JIT provisioning versus SCIM

**Just-in-time (JIT) provisioning** creates the user at their first login, from the assertion's attributes. It is easy. But Docker's docs say it plainly: "It doesn't deprovision users." [@docker-prov]

**SCIM** is a REST API that you host and the IdP calls. RFC 7643 defines two core resources, `User` and `Group`. [@rfc7643] RFC 7644 defines the calls: [@rfc7644]

- `POST /Users` creates a user. Return 201. A duplicate `userName` gets 409.
- `GET /Users?filter=userName eq "asha@acme.com"` finds one.
- `PATCH` changes some attributes. The body lists `add`, `remove` and `replace` operations.
- `DELETE` returns 204. Later GETs must return 404.

Store `externalId`. It is the IdP's own ID for the user. [@rfc7643]

### Offboarding

When someone leaves, Microsoft Entra can soft-delete the user with a PATCH like this: [@entra-scim]

```json
{
  "schemas": ["urn:ietf:params:scim:api:messages:2.0:PatchOp"],
  "Operations": [{ "op": "Replace", "path": "active", "value": false }]
}
```

Note `Replace` with a capital R. Entra sends op values that way, so match them without regard to case. [@entra-scim]

Entra expects GET to keep returning an inactive user. Only a hard delete removes it. [@entra-scim] The spec leaves the meaning of `active` to you. [@rfc7643] So make `active: false` end everything: sessions, API tokens and pending invites. If old sessions survive, the IdP believes the person is gone while they are still inside.

### Groups to roles

A user's `groups` attribute is read-only. Membership changes arrive on the `Group` resource. [@rfc7643] Entra sends a PATCH that adds or removes `members`. [@entra-scim] Let each tenant admin map their groups to your roles, such as "Acme-Finance" to "billing-admin". Then your normal authorization model takes over. See [Authorization models](/learn/rbac-abac-rebac).

### Never link accounts by unverified email

Any IdP can put any email in an assertion. OIDC says only `iss` and `sub` together are stable. Email "MUST NOT be used" as a unique ID. [@oidc] In 2023, Microsoft warned that apps using the email claim for authorization risk privilege escalation, because some emails were not verified. [@msrc-email]

Key each SSO identity on the tenant, the IdP and its subject ID. In SAML, ask for a persistent NameID or a fixed, never-reassigned user ID attribute. An email-format or transient NameID is not stable. Link an SSO identity to an existing password account only after that user proves they own the account.

## Trade-offs

| Option | Removes users when they leave? | Setup cost | Main risk | Use it when |
|---|---|---|---|---|
| Passwords plus invites | No. An admin must remember. | Low | Former staff keep access. | Small customers with no IdP. |
| SSO plus JIT | No. It only creates and updates at login. [@docker-prov] | Medium | Accounts and sessions outlive the job. | A first enterprise tier. |
| SSO plus SCIM | Yes. The IdP sends `active: false` or DELETE. [@entra-scim] | High. You host a SCIM API. | Partial PATCH support breaks syncs. | Customers with security reviews and audits. |
| IdP-initiated login | Not related. | Low | No `InResponseTo`, so replay and login CSRF. [@owasp-saml] | Only when customers need portal tiles. |

**Simple rule:** offer SSO with JIT first. Add SCIM before your first large customer's security review. Make deactivation end every session.

## At work: how a senior uses this

- Ask: **"Which element did we verify, and which element did we read the user from?"** They must be the same.
- Keep a test suite of bad SAML responses: unsigned, wrong audience, wrong recipient, expired and wrapped. Run it on every library upgrade.
- Pin each tenant's certificate. Alert two weeks before it expires.
- Ask: **"When SCIM sets active to false, what still works?"** Test sessions, API tokens and mobile refresh tokens.
- Log every SCIM call with tenant, operation and result. Customers will ask why a user still had access.
- Never let a tenant claim a domain without DNS proof.
- Subscribe to security advisories for your SAML and OIDC libraries.

## With AI

### What to ask the agent

> "List every check our SAML handler runs, in order. For each, show the line. Compare with the OWASP SAML Security Cheat Sheet and mark what is missing."

> "Write tests that post these SAML responses to our ACS endpoint: unsigned, signed for another audience, expired, and with a second unsigned assertion added. Each must fail."

> "Implement PATCH /scim/v2/Users/{id} per RFC 7644. When active becomes false, revoke all sessions and API tokens for that user in that tenant."

### What to check yourself

- The code does not parse SAML with a hand-written XML lookup. It calls a maintained library.
- Users are found by tenant plus IdP subject ID, never by email alone.
- Domain routing reads only verified domains.
- Every SCIM query and write is scoped to the tenant that owns the bearer token.
- Deactivation revokes tokens, not only the login.
- The AI did not invent SCIM fields. Compare them with RFC 7643.

## Check yourself

## Go deeper

- [Authentication and authorization](/learn/authentication-and-authorization): sessions, OAuth and ID token checks.
- [Authorization models](/learn/rbac-abac-rebac): where SCIM groups end up.
- [Multi-tenancy and noisy neighbours](/learn/multi-tenancy-noisy-neighbours): keeping each tenant's IdP config and users apart.
