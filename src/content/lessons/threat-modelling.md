---
topic: threat-modelling
subtitle: Find what an attacker would target in your design before you build it, and stop trusting a call just because it comes from inside.
status: published
illustrative_story: false
sources:
  - id: csrb
    title: "Cyber Safety Review Board, Review of the Summer 2023 Microsoft Exchange Online Intrusion (20 March 2024)"
    url: https://www.cisa.gov/sites/default/files/2025-03/CSRBReviewOfTheSummer2023MEOIntrusion508.pdf
    note: The official review. Pages 1 to 7 tell how a consumer signing key opened enterprise mailboxes; pages 20 and 21 list the fixes the Board asks of all cloud providers.
  - id: tm-manifesto
    title: "Braiterman, Shostack, Marcil et al., Threat Modeling Manifesto"
    url: https://www.threatmodelingmanifesto.org/
    note: The four questions, plus short values and principles for doing threat modelling as a team habit.
  - id: owasp-tm
    title: "OWASP Cheat Sheet Series, Threat Modeling Cheat Sheet"
    url: https://cheatsheetseries.owasp.org/cheatsheets/Threat_Modeling_Cheat_Sheet.html
    note: Data flow diagrams, STRIDE, how to rank threats, the four ways to respond, and how to review the result.
  - id: ms-stride
    title: "Microsoft Learn, Threats - Microsoft Threat Modeling Tool"
    url: https://learn.microsoft.com/en-us/azure/security/develop/threat-modeling-tool-threats
    note: A table with a plain definition and an example for each STRIDE category.
  - id: nist-zta
    title: "Rose, Borchert, Mitchell and Connelly, NIST SP 800-207: Zero Trust Architecture (NIST, August 2020)"
    url: https://nvlpubs.nist.gov/nistpubs/SpecialPublications/NIST.SP.800-207.pdf
    note: The definition of zero trust, its seven tenets in section 2.1, and the threats to a zero trust design in section 5.
  - id: beyondcorp
    title: "Ward and Beyer, BeyondCorp: A New Approach to Enterprise Security, ;login: vol. 39 no. 6 (USENIX, December 2014)"
    url: https://research.google.com/pubs/archive/43231.pdf
    note: How Google moved its internal apps behind an access proxy that checks user and device on every request.
  - id: spiffe
    title: "SPIFFE, SPIFFE Concepts"
    url: https://spiffe.io/docs/latest/spiffe-about/spiffe-concepts/
    note: The SPIFFE ID format, short-lived identity documents for workloads, and the Workload API.
claims_checked:
  - claim: "The forged tokens were signed with a 2016 consumer MSA key that should only have worked for consumer mail and was meant to be retired in March 2021."
    source: csrb
    note: "Printed page 2 says exactly this."
  - claim: "A common OIDC endpoint listed consumer and enterprise signing keys, the SDKs were not adequately updated to tell them apart, so mail accepted either key; without that flaw no enterprise customer would have been hit."
    source: csrb
    note: "Printed pages 6 and 17. Page 17 also lists the other errors: the key was never removed, and forged tokens were not detected."
  - claim: "22 organizations and 503 personal accounts were hit, about 60,000 State Department emails were taken, and Microsoft could not determine how the key was obtained."
    source: csrb
    note: "Printed pages 1 to 2, page iii and page 5. The lesson does not suggest a cause for the key theft."
  - claim: "The four questions: what are we working on, what can go wrong, what are we going to do about it, did we do a good enough job."
    source: tm-manifesto
    note: "Wording matches the manifesto."
  - claim: "Network location alone does not imply trust; inside requests must meet the same security requirements as outside ones."
    source: nist-zta
    note: "Section 2.1, tenet 2. Section 5.3 supports the quiz about attackers with valid credentials."
  - claim: "Risk transfer does not itself reduce the likelihood or harmful consequences of an event."
    source: owasp-tm
    note: "Response and Mitigations section, near word for word."
reviewed_by: ai-factcheck
reviewed_on: 2026-11-10
quiz:
  - q: "Your team will add a feature where partners send files to a new endpoint. When is the best time for a threat model?"
    options:
      - After launch, when the penetration test report arrives.
      - In the design review, before the code is written, and again when the design changes.
      - Only if the security team asks for one.
    answer: 1
    why: OWASP says threat modelling is ideally done early, in the design phase, and the model is kept up to date as the system changes.
  - q: "In STRIDE, a user edits the total on an expense after a manager approved it. Which category is this?"
    options: ["Repudiation", "Tampering", "Denial of service"]
    answer: 1
    why: Tampering is the malicious change of data, here a stored record. The fix is to block edits after approval and log every change.
  - q: "Service A calls service B inside your private network. What does zero trust ask of service B?"
    options:
      - Nothing. The call comes from inside the network, so it is safe.
      - Check A's identity and authorize the request, the same as for a call from the internet.
      - Block all calls from A and use a VPN instead.
    answer: 1
    why: NIST says network location alone does not imply trust. Inside calls must meet the same security rules as outside calls.
  - q: "You found 12 threats in a design review. Which response does NOT make the event less likely or less harmful?"
    options: ["Mitigate", "Eliminate the feature", "Transfer, for example to an insurer"]
    answer: 2
    why: OWASP notes that transferring risk does not itself reduce the likelihood or harmful consequences of an event. It moves the cost to another party.
  - q: "Your company rolled out a zero trust access proxy. An attacker phishes a support engineer's password and second factor. What still holds?"
    options:
      - Nothing changes. Zero trust stops all attacks with valid credentials.
      - The attacker can still use what that account may access, but should not reach resources outside its normal access.
      - The attacker gets access to every service, as on a flat network.
    answer: 1
    why: NIST says an attacker with valid credentials may still reach resources the account is allowed, but a good zero trust design limits movement beyond them.
incidents: [microsoft-2023-06-storm-0558]
related: [authn-authz, owasp-secure-coding, authorization-models, secrets-management, supply-chain-security, service-mesh, design-docs]
glossary_optout: [mitigation, process]
---

## What broke

On 16 June 2023, the US State Department told Microsoft about strange access to its email. A custom alert on mailbox access logs had fired. [@csrb]

Microsoft found that an attacker it calls Storm-0558 was reading mail with forged login tokens. The tokens were signed with a Microsoft consumer account key from 2016. That key was only meant to sign tokens for consumer email, not enterprise mailboxes. It was also meant to be retired in March 2021. [@csrb]

The key should never have opened enterprise mail. But Microsoft had built one shared endpoint that listed active signing keys for both consumer and enterprise systems. The libraries that check tokens were not properly updated to tell the two kinds of key apart. So mail accepted a token signed by either kind of key. [@csrb]

The attacker reached the mailboxes of 22 organizations and 503 personal accounts. It downloaded about 60,000 emails from the State Department alone. [@csrb] Microsoft still did not know how the key was stolen when the Cyber Safety Review Board finished its review. The Board called the intrusion "preventable". [@csrb]

The Board listed several errors. For example, the old key was never removed, and Microsoft did not detect the forged tokens itself. But it found that without the validation flaw, no enterprise customer would have been hit. [@csrb]

In our reading, the missing check sat on a trust boundary between consumer and enterprise systems. A threat model asks at each such line: what if a token from the other side shows up here? The Board asked cloud providers to design identity systems "informed by a rigorous threat model". [@csrb]

## The idea in simple words

A **threat model** is a structured look at a design to find what could go wrong with its security and privacy. [@tm-manifesto] You do it before you build, and you keep doing it as the design changes. [@owasp-tm]

It comes down to four questions: [@tm-manifesto]

1. What are we working on?
2. What can go wrong?
3. What are we going to do about it?
4. Did we do a good enough job?

**Zero trust** is a related idea. No user, device or service is trusted just because of where it sits on the network. Every request proves who it comes from and gets checked. [@nist-zta]

> [!key] **The key idea.** Draw where data crosses from one level of trust to another. Ask what could go wrong at each crossing. Then check identity and permission at every crossing, including the ones inside your own network.

## How it works

### Draw the system and its trust boundaries

Start with a **data flow diagram**. OWASP says a useful model shows trust boundaries, data flows, data stores, processes and external entities. These often mark the places attackers aim at. [@owasp-tm]

A trust boundary is where data or a call moves between parts you trust differently. Examples: the internet to your API, one tenant to another, a partner's webhook to your code, or a consumer system to an enterprise one.

Step through a small design: an expense app where users upload receipts.

[[diagram:receipt-threat-model]]

### Ask what can go wrong: STRIDE

STRIDE is a list of six prompts. Each letter is a kind of threat, and each one breaks a security property. [@owasp-tm]

| Letter | Threat | Breaks | Receipt app example |
|---|---|---|---|
| S | Spoofing: using someone else's identity [@ms-stride] | Authentication | The body sends another user's `userId`, or a stolen session uploads. |
| T | Tampering: changing data you should not [@ms-stride] | Integrity | A user edits an expense total after it was approved. |
| R | Repudiation: denying an action with no proof [@ms-stride] | Accounting | A user says "I never uploaded that". |
| I | Information disclosure [@ms-stride] | Confidentiality | The bucket is public, or links never expire. |
| D | Denial of service [@ms-stride] | Availability | A 2 GB upload fills the worker's disk. |
| E | Elevation of privilege [@ms-stride] | Authorization | A crafted PDF exploits the parser, and the worker has database admin rights. |

Walk the list at each trust boundary. Spend most time where outside data enters. You will find ideas that are not on the list. That is fine.

### Choose what to do

Rank each threat by how likely it is and how bad it would be. OWASP admits both are hard to measure, and that this ranking ignores the work to fix each one. [@owasp-tm] So weigh the cost of the fix too. A simple high, medium or low for each is enough to start.

Then pick one of four responses: [@owasp-tm]

- **Mitigate**: lower the chance it happens. Lock an expense once it is approved.
- **Eliminate**: remove the feature. Maybe you do not need public receipt links at all.
- **Transfer**: give the cost to someone else, such as an insurer. This does not make the event less likely or less harmful. [@owasp-tm]
- **Accept**: write down why, and who agreed.

For the receipt app, a good list is short. Take the user from the verified session, not the body. Keep the bucket private and use links that expire in minutes. Log every upload with the user ID. Limit the file size. Run the parser with no database rights; it hands results to a small writer with insert-only access.

Each fix should be testable. OWASP asks reviewers to check that agreed mitigations can be tested. [@owasp-tm]

### Keep it lightweight

The manifesto values "people and collaboration" over processes, methodologies and tools. It also values "continuous refinement" over a single delivery. [@tm-manifesto] Threat modelling should fit how your team already works, in small steps scoped to the part that changed. [@tm-manifesto]

In practice: add a "Threats" section to each design doc. Spend 30 minutes with the people who know the design. Turn each decision into a ticket or a test. The fourth question checks the model itself. Does the diagram match the system? Does every threat have a response, and can each fix be tested? [@owasp-tm] Ask it again after launch or after an incident.

### Zero trust: what it means

The old model is a castle. Anything inside the wall is trusted. Google's BeyondCorp paper says that once the wall is breached, the attacker has easy access to the internal network. [@beyondcorp]

NIST's definition: zero trust "assumes there is no implicit trust granted to assets or user accounts based solely on their physical or network location". [@nist-zta] Two of its tenets matter most for daily work:

- Every connection is protected, wherever it comes from. A request from inside must meet the same rules as one from outside. [@nist-zta]
- Access is granted per session, with least privilege. Access to one resource does not grant access to another. [@nist-zta]

At Google, every internal app sits behind an internet-facing access proxy. It checks the user, their groups, the device certificate and the device's state on every request. [@beyondcorp] In a Google office, a laptop without a valid certificate lands on a network with very limited access. [@beyondcorp]

### Zero trust for a service team

You probably do not run the access proxy. You run services. For you, zero trust means three habits.

**Every caller has an identity.** Each workload gets its own identity, proved with a short-lived certificate or token. SPIFFE names workloads with IDs like `spiffe://acme.com/billing/payments`, and rotates their keys often and automatically. [@spiffe] A service mesh can do this for you. See [Service mesh and sidecars](/learn/service-mesh-and-sidecars).

**Every request is checked.** Verify the caller's credential, then check what it may do. Never trust a header because the call came from inside.

```python
# Wrong: any pod that can reach us can claim to be any user
user_id = request.headers["X-User-Id"]
# Right: verify a signed token for this service, then read the user from it
claims = verify_token(request.headers["Authorization"], audience="receipts-api")
user_id = claims["sub"]
```

**Credentials are short-lived and narrow.** Each service gets only the permissions it needs. [@nist-zta] How to issue and rotate them is in [Secrets and key management](/learn/secrets-and-key-management).

### What zero trust does not mean

- **It is not a product.** NIST calls it a set of guiding principles. Moving to it is "a journey", not a swap of technology. [@nist-zta]
- **It does not remove the firewall overnight.** NIST expects most companies to run a hybrid of zero trust and perimeter controls for an indefinite period. [@nist-zta]
- **It does not stop a valid account.** An attacker with real credentials can still reach what that account may reach. Zero trust should stop it from moving further. [@nist-zta]
- **It adds a critical part.** If the policy service fails or is attacked, people may not reach anything. Run it in several places. [@nist-zta]

## Trade-offs

| Option | What it catches | Cost | Main risk | Use it when |
|---|---|---|---|---|
| No threat model, code review only | Bugs in single lines. | None up front. | No. Design flaws, like a missing boundary check, ship. | Small changes with no new data flow. |
| 30-minute four-question session | Many design flaws at trust boundaries. | Low. One meeting per design. | Shallow if the right people are not there. | Every new feature, endpoint or integration. |
| Full STRIDE per element, with a tool | More threats, written down for audits. | High. Days of work. | It goes stale if nobody updates it. [@owasp-tm] | Identity, payments and other high-risk systems. |
| Network perimeter as the main control | Outsiders who never got in. | Low. It already exists. | No. Once inside, an attacker moves freely. [@nist-zta] | Only as one layer among others. |
| Zero trust: per-request identity and policy | Stolen access to one part reaching other parts. [@nist-zta] | Medium to high. Identity for every workload and device. | The policy service becomes critical. [@nist-zta] | New systems, and step by step for old ones. [@nist-zta] |

**Simple rule:** threat model every design that adds a trust boundary. Make every service check its caller, whatever network the call comes from.

## At work: how a senior uses this

- In design review, ask: **"Where are the trust boundaries, and what checks the data at each one?"**
- Draw the data flow on the whiteboard first. Most missed threats hide in arrows nobody drew.
- Ask: **"What if a valid token from the wrong tenant, environment or system shows up here?"** That was the Storm-0558 question.
- Never accept "it is internal" as a reason to skip authentication. Ask who else can reach that network.
- Turn each threat into a ticket, a test or a written acceptance with an owner. A list in a doc is not a fix.
- Revisit the model when the design changes, not on a calendar.
- Prefer shared libraries for token checks. The Board found that many different token libraries made correct, uniform checks harder. [@csrb]

## With AI

### What to ask the agent

> "Here is our design doc. Draw a data flow diagram as a Mermaid chart. Mark every trust boundary and list the data that crosses each one."

> "For each trust boundary in this diagram, list STRIDE threats. For each, give likelihood, impact and one concrete mitigation. Mark the ones you are unsure of."

> "Search this service for code that reads user or tenant IDs from headers or request bodies without verifying a token. List file and line."

### What to check yourself

- The agent's diagram matches the real system. Check it against your deploy config, not only the doc.
- Generic threats like "use strong passwords" add noise. Keep only threats tied to a flow in your design.
- Mitigations name a real place in your code or config. "Add validation" is not a mitigation.
- Each internal endpoint the agent writes checks the caller's identity, not just its IP address.
- The agent cannot judge business impact. You decide the ranking with your team.

## Check yourself

## Go deeper

- [Authentication and authorization](/learn/authentication-and-authorization): how to check a user and their permission on every request.
- [Authorization models: RBAC, ABAC and ReBAC](/learn/rbac-abac-rebac): how to write the "what may this caller do" rules.
- [Dependency and supply-chain security](/learn/dependency-supply-chain-security): threats that arrive in code you did not write.
- [Design docs and RFCs](/learn/design-docs-and-rfcs): where your "Threats" section lives.
