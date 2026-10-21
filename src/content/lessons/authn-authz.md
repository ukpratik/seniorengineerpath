---
topic: authn-authz
subtitle: Prove who the user is, then check what they may do, on every request and for every object.
status: published
reviewed_by: ai-factcheck
reviewed_on: 2026-10-21
illustrative_story: false
sources:
  - id: sec-fa
    title: "U.S. Securities and Exchange Commission, Order in the Matter of First American Financial Corporation, Release No. 92176, 14 June 2021"
    url: https://www.sec.gov/files/litigation/admin/2021/34-92176.pdf
    note: The official findings on the EaglePro flaw, the January 2019 test that found it, and why it stayed unfixed.
  - id: sec-fa-press
    title: "U.S. Securities and Exchange Commission, press release 2021-102: SEC charges issuer with cybersecurity disclosure controls failures"
    url: https://www.sec.gov/news/press-release/2021-102
    note: A short summary of the case, the 800 million images and the penalty.
  - id: owasp-bola
    title: "OWASP API Security Top 10 (2023), API1:2023 Broken Object Level Authorization"
    url: https://api-security.owasp.org/editions/2023/en/0xa1-broken-object-level-authorization
    note: The most common API flaw, with attack examples and how to prevent it.
  - id: owasp-authz
    title: "OWASP Cheat Sheet Series, Authorization Cheat Sheet"
    url: https://cheatsheetseries.owasp.org/cheatsheets/Authorization_Cheat_Sheet.html
    note: Least privilege, deny by default, and checks on every request, on the server.
  - id: owasp-pw
    title: "OWASP Cheat Sheet Series, Password Storage Cheat Sheet"
    url: https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html
    note: Hashing versus encryption, salts, and the settings for Argon2id, scrypt, bcrypt and PBKDF2.
  - id: owasp-authn
    title: "OWASP Cheat Sheet Series, Authentication Cheat Sheet"
    url: https://cheatsheetseries.owasp.org/cheatsheets/Authentication_Cheat_Sheet.html
    note: Password length, breached passwords, generic login errors and multi-factor authentication.
  - id: owasp-session
    title: "OWASP Cheat Sheet Series, Session Management Cheat Sheet"
    url: https://cheatsheetseries.owasp.org/cheatsheets/Session_Management_Cheat_Sheet.html
    note: Session ID strength, cookie attributes, new IDs after login, timeouts and logout.
  - id: mdn-cookies
    title: "MDN Web Docs, Using HTTP cookies"
    url: https://developer.mozilla.org/en-US/docs/Web/HTTP/Guides/Cookies
    note: How browsers send cookies, and what HttpOnly, Secure and SameSite do.
  - id: rfc7519
    title: "Jones, Bradley and Sakimura, RFC 7519: JSON Web Token (JWT) (IETF, 2015)"
    url: https://www.rfc-editor.org/rfc/rfc7519
    note: The JWT format, signed versus encrypted tokens, and the exp and jti claims.
  - id: owasp-jwt
    title: "OWASP Cheat Sheet Series, JSON Web Token Cheat Sheet"
    url: https://cheatsheetseries.owasp.org/cheatsheets/JSON_Web_Token_Cheat_Sheet.html
    note: Common JWT mistakes, such as accepting "none", secrets in the payload and no way to revoke.
  - id: rfc6749
    title: "Hardt, RFC 6749: The OAuth 2.0 Authorization Framework (IETF, 2012)"
    url: https://www.rfc-editor.org/rfc/rfc6749
    note: The four OAuth roles and the authorization code grant.
  - id: zanzibar
    title: "Pang et al., Zanzibar: Google's Consistent, Global Authorization System, USENIX ATC 2019"
    url: https://research.google/pubs/zanzibar-googles-consistent-global-authorization-system/
    note: How Google runs one authorization system, with one data model, for hundreds of services.
  - id: rfc7636
    title: "Sakimura, Bradley and Agarwal, RFC 7636: Proof Key for Code Exchange (PKCE) (IETF, 2015)"
    url: https://www.rfc-editor.org/rfc/rfc7636
    note: The code interception attack and how a code verifier and challenge stop it.
  - id: rfc9700
    title: "Lodderstedt, Bradley, Labunets and Fett, RFC 9700: Best Current Practice for OAuth 2.0 Security (IETF, 2025)"
    url: https://www.rfc-editor.org/rfc/rfc9700
    note: Today's OAuth rules, such as PKCE, exact redirect URIs and no implicit or password grants.
  - id: oidc
    title: "OpenID Foundation, OpenID Connect Core 1.0"
    url: https://openid.net/specs/openid-connect-core-1_0.html
    note: The ID token, its required claims, and how a client must validate it.
claims_checked:
  - claim: "From 2014, an EaglePro user could change digits in a document URL to see other people's images; a January 2019 internal test found it, a clerical error set a 90-day deadline instead of 45, and it was not fixed."
    source: sec-fa
    note: Order paragraphs 9 and 11 to 16. Report finalized 11 January 2019; level 2 meant 90 days, deadline 8 May 2019.
  - claim: "A journalist reported the flaw on 24 May 2019; over 800 million images back to 2003 were exposed; the SEC charged Rule 13a-15(a) disclosure controls failures and the penalty was $487,616."
    source: sec-fa-press
    note: Press release 2021-102. Settled without admitting or denying. Earlier draft said "fined for failures in how it reported"; reworded to the actual charge.
  - claim: "OWASP's first choice is Argon2id with at least 19 MiB memory, 2 iterations and parallelism 1; scrypt next; bcrypt only for legacy, work factor at least 10, 72-byte limit in most implementations."
    source: owasp-pw
    note: Matches the cheat sheet. Added scrypt and the "most implementations" qualifier, and that encryption is allowed only in rare edge cases.
  - claim: "Session IDs need at least 64 bits of entropy; session cookies set SameSite=Strict (preferred) or Lax; the session is invalidated on the server at logout."
    source: owasp-session
    note: Matches the cheat sheet wording.
  - claim: "RFC 9700 says public clients must use PKCE, clients should not use the implicit grant, the password grant must not be used, and public-client refresh tokens must be sender-constrained or rotated."
    source: rfc9700
    note: Sections 2.1.1, 2.1.2, 2.4 and 2.2.2. Implicit grant is SHOULD NOT, password grant is MUST NOT.
  - claim: "The ID token's iss, aud and exp must be checked, and its signature too, except that TLS may be relied on when it comes straight from the token endpoint."
    source: oidc
    note: Section 3.1.3.7. Added the TLS exception; the draft said the signature must always be checked.
  - claim: "JWT denylists should be keyed on jti and issuer, and parsers must reject alg none."
    source: owasp-jwt
    note: The cheat sheet warns that keying on the raw token or its SHA-256 hash is not safe.
quiz:
  - q: A user changes `/orders/123` to `/orders/124` in the URL and sees someone else's order. What is missing?
    options:
      - Authentication. The user was not logged in.
      - An object-level authorization check that order 124 belongs to this user.
      - HTTPS on the API.
    answer: 1
    why: The user was logged in. The server never checked that the user may see that exact object. OWASP calls this broken object level authorization.
  - q: How should a server store user passwords?
    options:
      - Encrypted with AES, so support staff can read them when needed.
      - Hashed with a slow password hashing function, such as Argon2id, with a salt.
      - Hashed once with SHA-256, because it is fast.
    answer: 1
    why: OWASP says to hash passwords, because hashing is one-way. Encryption can be reversed. Argon2id is OWASP's first choice.
  - q: Your API issues signed JWTs that are valid for 7 days. A user's laptop is stolen. What is the problem?
    options:
      - None. A signed JWT cannot be used by anyone else.
      - The thief can read the password inside the token.
      - The token keeps working until it expires, unless you build a denylist or other revocation.
    answer: 2
    why: The server checks the signature and expiry, not a session store. Short lifetimes and a denylist keyed on the jti and issuer limit the damage.
  - q: What is the main difference between OAuth 2.0 and OpenID Connect?
    options:
      - OAuth gives an app limited access to an API. OpenID Connect adds an ID token that tells the app who the user is.
      - OpenID Connect is the old version of OAuth.
      - OAuth is for mobile apps, and OpenID Connect is for websites.
    answer: 0
    why: OpenID Connect is an identity layer on top of OAuth 2.0. Its ID token is a JWT about the user's login.
  - q: Which session cookie attribute stops JavaScript on the page from reading the cookie?
    options: ["Secure", "HttpOnly", "SameSite=Lax"]
    answer: 1
    why: MDN says an HttpOnly cookie cannot be read through document.cookie. This limits damage from cross-site scripting.
incidents: []
cases: [google-zanzibar]
related: [tls, owasp-secure-coding, authorization-models, secrets-management, sso-scim, rest-api]
---

## What broke

First American is a large US title insurance company. In 2013 it launched EaglePro, an app that sent document images to customers through unique links. [@sec-fa]

From 2014, the app had a defect. A user could take the URL of their own document and change some digits in it. The server then showed other people's documents. The user should not have had access to those images. [@sec-fa]

The company's own security team found this in a manual test. Their report was finished on 11 January 2019. It said that replacing a number in the URL with another sequential number gave access to other documents. A clerical error gave the flaw a lower severity, so its deadline became 90 days instead of 45. That deadline was 8 May 2019, and the flaw was still not fixed after it. [@sec-fa]

On 24 May 2019, a cybersecurity journalist told the company about it. The flaw exposed over 800 million title and escrow document images, going back to 2003. Some held social security numbers and financial information. [@sec-fa-press]

The executives who answered the press did not know about the January report. [@sec-fa] In 2021, the SEC charged the company with failing to keep proper disclosure controls. It agreed to pay a $487,616 penalty, without admitting or denying the findings. [@sec-fa-press]

The bug was one missing check: "Does this document belong to the person asking?" The same mistake is number one on the OWASP API Security Top 10 today. [@owasp-bola]

## The idea in simple words

Two questions protect every request.

- **Authentication**: who are you? The user proves it with a password, a second factor or a login at another provider.
- **Authorization**: what may you do? The server checks if this user may do this action on this object.

Authentication happens once at login, and then a session or token carries the answer on each request. Authorization must happen on every request, for the exact object asked for. OWASP says to check permissions on every request, on the server, and to deny by default. [@owasp-authz]

> [!key] **The key idea.** Logging in proves who you are. It never proves you may touch this order. Check ownership or permission on the server, every time, for every object.

## How it works

Step through one login and one request for an order. Use Next and Back, or the ← and → keys.

[[diagram:authn-order-check]]

### Passwords done right

Never store a password as it is. Hash it. A hash is one-way, so even you cannot get the password back. [@owasp-pw] Encryption is two-way, so anyone with the key can. OWASP says to encrypt passwords only in rare edge cases. [@owasp-pw]

Use a slow password hashing function, not a fast one like plain SHA-256. OWASP's first choice is Argon2id with at least 19 MiB of memory, 2 iterations and a parallelism of 1. If Argon2id is not available, use scrypt. [@owasp-pw] Use bcrypt only for older systems where neither is available, with a work factor of at least 10. Most bcrypt libraries read at most 72 bytes of a password. [@owasp-pw] Each password gets its own random **salt**, so two users with the same password get different hashes. Good libraries handle the salt for you. [@owasp-pw]

At the login form, OWASP says to:

- Treat passwords shorter than 8 characters as weak when multi-factor authentication (MFA) is on, and shorter than 15 when it is off. [@owasp-authn]
- Allow at least 64 characters, so people can use passphrases. [@owasp-authn]
- Block common and already breached passwords. [@owasp-authn]
- Show one generic error, like "Invalid user ID or password", so attackers cannot learn which accounts exist. [@owasp-authn]

Then add MFA. OWASP calls it "by far the best defense against the majority of password-related attacks". It cites a Microsoft analysis that MFA would have stopped 99.9% of account compromises. [@owasp-authn]

### Sessions and cookies

After login, the server must remember the user. The classic way is a **session**. The server stores the session and sends the browser a random session ID in a cookie. The browser sends the cookie back with each request on its own. [@mdn-cookies]

Set three cookie attributes:

- **HttpOnly**: JavaScript cannot read the cookie. This limits the damage of cross-site scripting (XSS). [@mdn-cookies]
- **Secure**: the browser sends it only over HTTPS. [@mdn-cookies]
- **SameSite**: controls whether the cookie goes with requests from other sites. OWASP says to set `Strict` (preferred) or `Lax` on session cookies. [@owasp-session]

The session ID needs at least 64 bits of entropy, so nobody can guess it. Give the user a new session ID after login. On logout, delete the session on the server, not only the cookie. [@owasp-session]

### Tokens and JWTs

A **JWT** (JSON Web Token) is a compact, URL-safe string that carries claims, such as a user ID. It can be signed, as a JWS, or encrypted, as a JWE. [@rfc7519] This lesson is about signed JWTs.

Signed is not secret. The payload is only base64url encoded, so anyone with the token can read every claim. [@owasp-jwt] Never put private data in it. The signature only proves that nobody changed it. [@owasp-jwt]

Give each JWT an `exp` claim. The `exp` claim is optional in the standard, but once it is set, the token must not be accepted on or after that time. [@rfc7519] Your parser must reject tokens with `"alg":"none"`, which have no signature. [@owasp-jwt]

The big catch is **revocation**. The server checks a JWT by its signature, with no lookup. So a stolen token works until it expires. Fixes: keep lifetimes short, and if you must revoke early, keep a denylist keyed on the token's `jti` and issuer. [@owasp-jwt] Once you add that lookup, you have a session store again.

### OAuth 2.0 and OpenID Connect

**OAuth 2.0** is about delegation. It lets an app get limited access to an API for a user, without the user's password. [@rfc6749] Example: a printing app wants to read your photos from a cloud drive.

It has four roles: the user (resource owner), the app (client), the **authorization server** that issues tokens, and the API (resource server). [@rfc6749]

**OpenID Connect** (OIDC) is about login. It is an identity layer on top of OAuth 2.0. It adds an **ID token**, a signed JWT with claims about how the user logged in. [@oidc] The app must check that `iss` is the expected provider, that `aud` holds its own client ID, and that `exp` has not passed. [@oidc]

The app must also check the signature. One exception: if the app got the ID token straight from the token endpoint over TLS, the spec allows it to trust TLS instead. [@oidc]

The normal flow today is the **authorization code flow with PKCE**:

1. The app makes a random secret, the code verifier. It sends its SHA-256 hash, the code challenge, with the `S256` method. [@rfc7636]
2. The user logs in at the authorization server. The server redirects back to the app with a short-lived code. [@rfc6749]
3. The app swaps the code for tokens and sends the original verifier. The server hashes the verifier and compares it with the challenge. [@rfc7636]

PKCE stops an attacker who steals the code on its way back, because they do not have the verifier. [@rfc7636] RFC 9700 says public clients, such as mobile and browser apps, must use PKCE. It also says the implicit grant should not be used, and the password grant must not be used. [@rfc9700]

### Check every object, with the least privilege

Broken object level authorization (BOLA) happens when an API takes an object ID from the client and does not check that the caller may use that object. OWASP calls it "extremely common". [@owasp-bola] The fix is the check in the diagram, in every function that reads a record using an ID from the client. Random IDs make guessing harder, and authorization tests stop regressions. [@owasp-bola] Random IDs alone are not enough. [@owasp-authz]

Give each user, service and token only the access it needs. OWASP asks for least privilege. [@owasp-authz] RFC 9700 says OAuth access tokens should get the minimum privileges for the use case. [@rfc9700]

Roles, like "admin" and "viewer", are a good start. Larger products need richer rules. Google's Zanzibar, for example, serves hundreds of services from one shared permission model. [@zanzibar] See [Authorization models: RBAC, ABAC and ReBAC](/learn/rbac-abac-rebac) (coming soon).

## Trade-offs

| Option | Revoke at once? | Server lookup per request? | Main risk | Use it when |
|---|---|---|---|---|
| Session ID in a cookie | Yes. Delete the session on the server. [@owasp-session] | Yes. One read from the session store. | Cross-site requests. Set SameSite. [@owasp-session] | A web app with its own backend. A good default. |
| Long-lived signed JWT | No. It works until `exp`. [@owasp-jwt] | No. Only a signature check. | Stolen tokens stay valid. | Rarely. Prefer the next row. |
| Short-lived JWT plus a refresh token | Mostly. The access token dies soon, and you revoke the refresh token. | Only when refreshing. | More moving parts. Public clients must rotate or bind refresh tokens. [@rfc9700] | Mobile apps and APIs used by many services. |
| Login with OIDC (Google, Okta and others) | Depends on the provider and your session. | Only at login. | Skipping ID token checks. [@oidc] | Users already have an account elsewhere, or a company wants single sign-on. |

**Simple rule:** for your own web app, use a server session in an HttpOnly, Secure cookie. Use tokens when you truly need them, and keep them short-lived.

## At work: how a senior uses this

- In every review of an endpoint that takes an ID, ask: **"Where do we check that this user may access this object?"**
- Put the authorization check in one place, like a policy function or middleware. Do not copy it into each handler.
- Write a test for each endpoint where user A asks for user B's object. It must fail with 403 or 404.
- Never write your own password hashing or JWT code. Use a well-known library, and read its defaults.
- Ask: **"If this token leaks, how long does it work, and how do we kill it?"**
- Give services and tokens the smallest scopes that work. Review admin roles every quarter.

## With AI

### What to ask the agent

> "List every API endpoint in this repo that takes an object ID. For each one, show where it checks that the current user may access that object, or say that it does not."

> "Write tests where user A tries to read, update and delete user B's orders. Every test must expect 403 or 404."

> "Review this login code against the OWASP Password Storage and Session Management cheat sheets. List each gap with the line number."

### What to check yourself

- Passwords use Argon2id, scrypt or bcrypt from a known library. Never MD5, SHA-256 alone or encryption.
- Every query by ID also filters by the current user or tenant, or calls a permission check first.
- JWT code checks the signature, `exp`, `iss` and `aud`, and rejects `"alg":"none"`.
- Session cookies have HttpOnly, Secure and SameSite. A new session ID is created at login.
- OAuth code uses the authorization code flow with PKCE, not the implicit flow.
- No secrets, tokens or passwords appear in logs, URLs or test fixtures.
- AI agents often copy a handler and forget the ownership check. Read each new handler for that line.

## Check yourself

## Go deeper

- [TLS](/learn/tls-and-secure-connections): why all of this needs HTTPS first.
- Coming next on this line: [OWASP Top 10 and secure coding](/learn/owasp-top-10-secure-coding), [Secrets and key management](/learn/secrets-and-key-management) and [Enterprise SSO and SCIM](/learn/enterprise-sso-and-scim).
