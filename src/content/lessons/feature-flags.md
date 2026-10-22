---
topic: feature-flags
subtitle: Use flags to release safely and to run experiments, keep paid-plan rules apart, and remove every flag you no longer need.
status: published
reviewed_by: ai-factcheck
reviewed_on: 2026-10-22
illustrative_story: false
sources:
  - id: sec-knight
    title: "U.S. Securities and Exchange Commission, Order in the matter of Knight Capital Americas LLC, Release No. 34-70694 (16 October 2013)"
    url: https://www.sec.gov/litigation/admin/2013/34-70694.pdf
    note: The official account of the reused flag, the missed server and the losses on 1 August 2012 (paragraphs 1 and 12–17).
  - id: toggles
    title: "Pete Hodgson, “Feature Toggles (aka Feature Flags)”, martinfowler.com"
    url: https://martinfowler.com/articles/feature-toggles.html
    note: The four kinds of toggles, toggle routers, testing toggles and treating flags as inventory.
  - id: unleash
    title: "Unleash documentation, “Stickiness”"
    url: https://docs.getunleash.io/reference/stickiness
    note: How a real flag tool hashes a user ID into a number so each user gets a stable result.
  - id: of-intro
    title: "OpenFeature documentation, “Introduction”"
    url: https://openfeature.dev/docs/reference/intro
    note: What OpenFeature is, and what providers and evaluation context are.
  - id: of-glossary
    title: "OpenFeature specification, “Glossary”"
    url: https://openfeature.dev/specification/glossary
    note: Server-side (dynamic context) and client-side (static context) flag evaluation, targeting and variants.
  - id: of-eval
    title: "OpenFeature specification, “Flag Evaluation API”"
    url: https://openfeature.dev/specification/sections/flag-evaluation
    note: Why a flag check must never throw, and must return the default value on errors.
  - id: gcp2025
    title: "Google Cloud, Service Health incident report for 12 June 2025 (official)"
    url: https://status.cloud.google.com/incidents/ow5i3PPK96RduMcb1SsW
    note: A new code path with no flag, and Google's rule afterwards that critical changes ship behind flags that are off by default.
claims_checked:
  - claim: Knight's new RLP code was meant to replace unused Power Peg code (unused since 2003, still callable) and repurposed the flag that used to turn Power Peg on.
    source: sec-knight
    note: Paragraphs 12 and 13 of the SEC order, read in full. "Stock exchange program" made specific (NYSE Retail Liquidity Program); "replaced" changed to "was meant to replace".
  - claim: From 27 July 2012 a technician missed one of eight SMARS servers; no second technician reviewed it and no written procedure required that; the 2005 move of the cumulative quantity function was never retested.
    source: sec-knight
    note: Paragraphs 14 and 15. Wording aligned with the ci-cd lesson and the knight-capital-2012-08 card.
  - claim: 212 parent orders led to millions of child orders, 4 million executions in 154 stocks in about 45 minutes, and a loss of more than $460 million.
    source: sec-knight
    note: Paragraphs 1 and 17. The opening now says the orders came in 45 minutes and the loss followed, instead of "lost $460 million in 45 minutes".
  - claim: Hodgson's four categories are Release, Experiment, Ops and Permissioning toggles; release toggles should "generally not stick around much longer than a week or two"; some ops toggles are long-lived kill switches; premium-user permissioning toggles can live for multiple years.
    source: toggles
    note: Checked in the "Categories of toggles" sections. "Permission flags" renamed "permissioning flags" to match his name.
  - claim: Teams treat toggles as inventory with a carrying cost, add a removal task when a release toggle is created, and use "time bombs" that fail a test after an expiration date; test the expected production setup, and many teams also test with all toggles on.
    source: toggles
    note: Matches the "Working with feature-flagged systems" and testing sections. Removal-task habit narrowed to release flags, as in the article.
  - claim: Unleash hashes a context field (userId, then sessionId) with the strategy's groupId (default, the flag name) into 0-100 using MurmurHash; users at or below the rollout percentage get the feature; raising the percentage keeps existing users; with no ID the result is random.
    source: unleash
    note: Matches the Stickiness page (last updated August 2026) section by section. MurmurHash added.
  - claim: A flag evaluation must not throw and must return the supplied default value on abnormal execution.
    source: of-eval
    note: Requirement 1.4.10 of the Flag Evaluation API.
  - claim: Targeting is rules, user overrides or fractional evaluation; a variant names a value; client-side SDKs fetch and cache flags for one user and must re-evaluate when the context changes, for example on login.
    source: of-glossary
    note: Glossary entries Targeting, Variant and Static-Context Paradigm. Evaluation context example (user ID) now cited to this page too.
  - claim: OpenFeature is "an open specification that provides a vendor-agnostic, community-driven API for feature flagging"; providers are the "translation layer" to the flag tool.
    source: of-intro
    note: Exact quotes from the Introduction page.
  - claim: After the June 2025 outage Google said it will make all changes to critical binaries feature-flag protected and disabled by default.
    source: gcp2025
    note: Matches the remediation list in the official incident report.
  - claim: Keeping entitlements apart from release flags is the lesson's own advice.
    source: toggles
    note: Hodgson treats premium features as permissioning toggles and says to manage each kind differently. No primary source found that requires a separate system, so the text labels it "our advice, not Hodgson's".
quiz:
  - q: Which kind of flag should normally live for days or weeks, not years?
    options:
      - A release flag.
      - A permissioning flag that unlocks a paid feature.
      - A long-lived kill switch for an optional feature.
    answer: 0
    why: Release flags only hide unfinished or new code. Once the feature is fully on, you delete the flag and the old path.
  - q: You roll out a flag to 10% of users. Why hash the user ID instead of picking at random on each request?
    options:
      - Hashing is faster than a random number.
      - The same user gets the same answer every time, so the feature does not flicker on and off.
      - Random numbers are not allowed in production.
    answer: 1
    why: A hash of the same input always gives the same number, so each user stays in or out of the rollout.
  - q: Your team wants to reuse an old, unused flag name for a new feature. What is the safest choice?
    options:
      - Reuse it. The old code is never called.
      - Create a new flag, and delete the old flag and its old code first.
      - Reuse it, but only on half the servers.
    answer: 1
    why: At Knight Capital, a reused flag turned on old code that was still on one server. New flags and deleted dead code avoid this.
  - q: A customer moves from the Free plan to the Pro plan. Where should "Pro customers can export to PDF" live?
    options:
      - In a release flag, turned on for each Pro customer by hand.
      - In the entitlement system, which reads the customer's plan.
      - In a code comment.
    answer: 1
    why: What a plan allows is a business rule that lives as long as the plan. It should come from billing data, not a release flag.
incidents: [knight-capital-2012-08, google-cloud-2025-06-service-control]
related: [safe-deploys, ci-cd, testing-strategy, tech-debt, multi-tenancy]
---

## What broke

On 1 August 2012, Knight Capital, a large US trading firm, sent millions of unwanted orders in about 45 minutes. It lost more than $460 million. The U.S. Securities and Exchange Commission (SEC) later described what happened. [@sec-knight]

Knight was adding new code to SMARS, its order router, for a new New York Stock Exchange program. The new code was meant to replace old code for a feature called "Power Peg". Knight had stopped using Power Peg in 2003, but the code was still there and could still be called. [@sec-knight]

The new code also reused a flag that used to turn on Power Peg. The plan was simple: delete Power Peg, so that the flag would now turn on the new code. [@sec-knight]

From 27 July, Knight deployed the new code to its servers in stages. A technician did not copy it to one of the eight servers. No second technician reviewed the deploy, and no written procedure required a review. [@sec-knight]

On 1 August, orders arrived with the reused flag set. Seven servers ran the new code correctly. The eighth server ran Power Peg. Power Peg's counter of filled shares had been moved in 2005, and the old code was never tested again. So it sent new orders again and again without checking how many shares were already filled. [@sec-knight]

For 212 incoming orders, the system sent millions of orders. That gave 4 million executions in 154 stocks in about 45 minutes. [@sec-knight]

## The idea in simple words

A feature flag is an `if` statement whose answer comes from config, not from code. You can change the answer while the program runs, for some users or for all, without a new deploy.

Flags let you separate two things: **deploying** code and **releasing** a feature. You deploy the code switched off. Later, you turn it on for 1% of users, then more.

Pete Hodgson names four kinds of flags, and they live for very different lengths of time. [@toggles]

- **Release flags** hide new or unfinished code. They should usually not stay "much longer than a week or two."
- **Experiment flags** split users for an A/B test. They must stay stable long enough to give a clear result.
- **Ops flags** let operators switch off a feature, for example under heavy load. Most are short-lived, but some stay as long-lived kill switches.
- **Permissioning flags** turn features on for some users, such as paying customers. They can live for years.

> [!key] **The key idea.** Every flag is a branch you must test and a switch that can break production. Give each one an owner, a reason and an end date, and delete it when its job is done.

## How it works

Step through one flag check for one user. Use Next and Back, or the ← and → keys.

[[diagram:flag-evaluation-user]]

### Context, rules and variants

Your code asks a question like "is `new-checkout` on for this request?". It passes an evaluation context: data about the request, such as the user ID. [@of-glossary] You can add your own fields, such as the plan. [@of-intro] The flag's rules look at that context. OpenFeature calls this targeting: rules, user overrides or a percentage split. The answer is a variant, such as `on`, `off` or `blue`. [@of-glossary]

Keep the check in one place. Hodgson suggests a toggle router that makes the decision, so the rest of your code does not know how the decision is made. [@toggles]

### Percentage rollouts and stable buckets

A 10% rollout must not flip a user between on and off on each request. So tools hash a user field instead of using a random number. Unleash uses MurmurHash on the user ID and a group ID to get a number from 0 to 100. By default the group ID is the flag name, so each flag splits users in its own way. Users whose number is at or below the rollout percentage get the feature. [@unleash]

The same input always gives the same number. [@unleash] So raising 10% to 20% keeps the first 10% and adds more users. If there is no user ID or session ID, Unleash falls back to a random number, and the result is not stable. [@unleash]

### Server or client evaluation

On a server, one process checks flags for many users, each with their own context. In a browser or phone app there is one user. The app usually fetches all its flag results at once and caches them. When the context changes, for example after login, the cached results are stale and must be fetched again. [@of-glossary]

Two habits follow from this. First, never trust a flag result that a client sends you for anything that matters, like access to a paid feature. Check it again on the server. Second, decide one flag per request, early, and pass the result down. Otherwise one request can see the flag as on in one service and off in another.

### When the flag system fails

Your flag service can be slow or down. OpenFeature says a flag check must never throw an error, and must return the default value you supplied when something goes wrong. [@of-eval] So choose defaults with care. For a new feature, the safe default is usually off.

### Entitlements are not release flags

An entitlement is what a customer's plan allows: "Pro can export to PDF", "Free gets 3 projects". It looks like a permissioning flag. Hodgson even uses paid features as an example of one, and says to manage each kind of flag differently. [@toggles] We go one step further: treat entitlements as their own system.

This is our advice, not Hodgson's, for three reasons. Entitlements come from billing data, not from engineers. They last as long as the plan, so the "delete old flags" rule does not apply. And a wrong value can give away paid features or block a paying customer. Read the plan from your billing or account data, behind one function like `canUse(account, "pdf-export")`. You can still put a release flag around a new paid feature while you build it. The [multi-tenancy lesson](/learn/multi-tenancy-noisy-neighbours) covers per-customer limits.

### Flags are config changes, and they cost you

Turning a flag on for everyone changes production in seconds. Treat flag changes like config: review them, roll them out in steps and watch the metrics. The [safe deploys lesson](/learn/safe-deploys-config-changes) covers this. After its June 2025 outage, Google said changes to critical binaries must be behind flags that are off by default. [@gcp2025]

Each flag doubles the paths through one piece of code. Hodgson says you do not need to test every combination, because most flags do not interact. Test the setup you expect in production. Many teams also test with all flags on. [@toggles]

Old flags are flag debt. Hodgson describes teams that treat flags as inventory with a cost. Some add a removal task when they create a release flag. Some add "time bombs": tests that fail when a flag is past its end date. [@toggles] The [tech debt lesson](/learn/technical-debt-management) shows how to track this kind of work.

### A standard API: OpenFeature

OpenFeature is "an open specification that provides a vendor-agnostic, community-driven API for feature flagging". A provider is the "translation layer" between this API and your flag tool. [@of-intro] Your code calls one API, so you can change tools without rewriting every flag check.

## Trade-offs

| Where the flag lives | Change without deploy? | Risk | Use it when |
|---|---|---|---|
| Constant in code | No. A deploy is needed. | Low. Easy to find and delete. | A small team hides unfinished work on trunk. |
| Config file or env variable | No. A restart or redeploy is needed. | A typo can reach every server at once. | Rare changes, like ops limits. |
| Flag service with SDK | Yes. Seconds. | A bad change is global in seconds. The service can fail. | Gradual rollouts, kill switches and experiments. |
| Entitlement system | Yes. Driven by billing data. | Wrong data gives away paid features or blocks customers. | What each plan allows. |

**Simple rule:** a release flag is temporary, so plan its removal on the day you create it. A rule that lasts as long as a plan is not a release flag.

## At work: how a senior uses this

- Give each flag a clear name, an owner, a kind (release, experiment, ops or permissioning) and an end date.
- Never reuse a flag name. Create a new flag, and delete the old flag and its old code.
- Ask in review: **"What does this do when the flag service is down?"** The default must be safe.
- Roll out by percentage with a stable user ID, and watch errors at each step before going wider.
- When a release flag reaches 100%, open the cleanup pull request within a few weeks. Delete the flag and the old code path.
- Keep a list of flags with their age. Bring old ones to the team's planning meeting.
- Keep plan rules in an entitlement check, and check them on the server.

## With AI

### What to ask the agent

> "Find every feature flag in this repository. For each one, list the name, where it is checked, its default value, and whether one branch is now dead code."

> "Remove the flag `new-checkout`, which is now on for 100% of users. Keep the 'on' path, delete the 'off' path and its tests, and remove the flag from config."

> "Write tests for this function with the flag on and with it off. Also add one test with all flags on."

### What to check yourself

- The agent kept the correct branch when it removed a flag, and removed no other flag.
- The default value used when the flag service is down is safe.
- No paid feature depends only on a flag value sent from the client.
- Rollouts use a stable user ID, not a random number per request.
- The removed flag is also gone from the flag tool's config, so nobody can turn it on again by mistake.

## Check yourself

## Go deeper

Related lessons: [Safe deploys](/learn/safe-deploys-config-changes), [CI/CD and trunk-based development](/learn/ci-cd-trunk-based-development) and [Testing strategy](/learn/testing-strategy).
