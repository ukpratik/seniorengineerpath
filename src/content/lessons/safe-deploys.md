---
topic: safe-deploys
subtitle: Most outages start with a change. Learn to ship code and config in small, watched steps, and to undo them fast.
status: published
reviewed_by: ai-factcheck
reviewed_on: 2026-10-21
illustrative_story: false
sources:
  - id: cf2019
    title: "Cloudflare, “Details of the Cloudflare outage on July 2, 2019” (official postmortem)"
    url: https://blog.cloudflare.com/details-of-the-cloudflare-outage-on-july-2-2019/
    note: The full timeline, why WAF rules skipped the staged rollout, and the list of fixes.
  - id: gcp2025
    title: "Google Cloud, Service Health incident report for 12 June 2025 (official)"
    url: https://status.cloud.google.com/incidents/ow5i3PPK96RduMcb1SsW
    note: How code with no feature flag met policy data that spread to every region in seconds.
  - id: aws-deploys
    title: "Clare Liguori, “Automating safe, hands-off deployments”, Amazon Builders' Library"
    url: https://builder.aws.com/content/3ErTKQOTKc5NIw031UePBPxTQ6I/automating-safe-hands-off-deployments
    note: A real pipeline with one-box stages, waves of regions, bake times and automatic rollback.
  - id: sre-canary
    title: "Google, The Site Reliability Workbook (O'Reilly), chapter “Canarying Releases”"
    url: https://sre.google/workbook/canarying-releases/
    note: What a canary is, how to compare it with the rest of the fleet, and why config changes need it too.
  - id: toggles
    title: "Pete Hodgson, “Feature Toggles (aka Feature Flags)”, martinfowler.com"
    url: https://martinfowler.com/articles/feature-toggles.html
    note: The kinds of feature flags, and why every flag has a cost until you remove it.
claims_checked:
  - claim: "At 13:42 UTC on 2 July 2019 a WAF rule change went global in seconds, CPU hit nearly 100%, users saw 502 errors and Cloudflare lost about 80% of its traffic."
    source: cf2019
    note: Postmortem gives 13:42 deploy, p99 2.29s global distribution, nearly 100% CPU, 502 errors and 80% traffic loss.
  - claim: "WAF rules skipped staged rollout by design; a global WAF terminate at 14:07 restored traffic by 14:09; the fix plan adds staged rule rollouts but keeps emergency global deploys."
    source: cf2019
    note: Matches the postmortem timeline and fix list. Speed tests and the regex engine change were planned (ETA), so the lesson now says planned.
  - claim: "Google Cloud's June 2025 quota-check code had no feature flag or error handling, policy data with blank fields replicated globally within seconds, and the red-button rollout finished within about 40 minutes."
    source: gcp2025
    note: Matches the official incident report, including the remediation on flags disabled by default and incremental data propagation.
  - claim: "AWS deploys to one Availability Zone at a time in the first wave, waits at least one hour after each one-box stage and at least 12 hours after the first regional wave, and rolls back automatically on alarms."
    source: aws-deploys
    note: All figures match the Builders' Library article.
  - claim: "In Google's experience, a majority of incidents are triggered by binary or configuration pushes."
    source: sre-canary
    note: Close paraphrase of the Workbook chapter; no percentage given, and the lesson gives none.
  - claim: "Feature flags change behaviour without a new deploy, add a testing burden, and should be removed when no longer needed."
    source: toggles
    note: Matches Hodgson's article (carrying cost, testing burden, remove stale flags).
quiz:
  - q: Your team changes one line in a shared config file. How should it reach production?
    options:
      - Push it everywhere at once. Config is not code, so it is safe.
      - Validate it, then roll it out in stages with health checks, like code.
      - Ask a teammate to look at it, then push it everywhere at once.
    answer: 1
    why: Config changes the behaviour of running systems, so they cause outages too. Treat them like code.
  - q: The canary runs the new version on 1% of servers. Its error rate doubles. What should happen first?
    options:
      - Roll back the canary and stop the rollout.
      - Continue to 10% to get more data.
      - Start debugging on the canary while it keeps serving users.
    answer: 0
    why: Mitigate first, debug second. A fast rollback limits harm, and you can study the logs afterwards.
  - q: What does bake time mean?
    options:
      - The time it takes to build the release.
      - Time you wait at each stage, watching metrics, before you go wider.
      - The time a feature flag stays in the code.
    answer: 1
    why: Some bugs show up only after minutes or hours of real traffic. Bake time gives them a chance to appear.
  - q: In the 2019 Cloudflare outage, why did one bad rule hit every server?
    options:
      - A hacker changed the rule.
      - Firewall rule changes were allowed to go global in seconds, with no staged rollout.
      - The servers ran out of disk space.
    answer: 1
    why: Code went through staged rollouts, but WAF rules were pushed globally so they could stop new attacks quickly.
incidents:
  - cloudflare-2019-07-waf-regex
  - google-cloud-2025-06-service-control
  - crowdstrike-2024-07-channel-file-291
  - cloudflare-2025-11-bot-management
  - cloudflare-2025-12-waf-config
related: [feature-flags, ci-cd, observability-slos, incident-response]
---

## What broke

On 2 July 2019 at 13:42 UTC, a Cloudflare engineer deployed a small change to a firewall rule. The rule was meant to catch cross-site scripting attacks. It had passed its automatic tests. [@cf2019]

Within seconds the rule was on every Cloudflare server in the world. It contained a regular expression that backtracked badly. CPU use on the servers that handle web traffic jumped to nearly 100%. Visitors to Cloudflare sites got 502 errors, and Cloudflare lost about 80% of its traffic. [@cf2019]

The rule was in "simulate" mode, so it was not meant to block anything. But even in that mode the rule still had to run, and running it was the problem. [@cf2019]

Why did one rule reach everyone at once? Normal Cloudflare code went through staged rollouts. Firewall rules did not, by design, so that the team could respond fast to new attacks. [@cf2019]

At 14:07 the team used a "global terminate" to switch off the firewall everywhere. Traffic came back at 14:09. One of the fixes they listed was to move rule changes to staged rollouts, like code. [@cf2019]

## The idea in simple words

Most outages do not start on their own. They start with a change. Google's SRE team writes that, in its experience, most incidents are triggered by binary or configuration pushes. [@sre-canary]

> [!key] **The key idea.** Config is code. It changes how running systems behave, so it needs the same checks, the same slow rollout and the same fast undo as code.

A safe deploy has three parts:

1. **Check before you ship.** Validate the change before it reaches any real server.
2. **Ship to a few first.** Send the change to a small group, watch it, then go wider in steps.
3. **Undo fast.** Make rollback one click or fully automatic, and practise it.

The goal is a small blast radius. If a change is bad, only a few users should see it, and only for a few minutes.

## How it works

Step through a bad config change. Path A pushes it everywhere at once. Path B uses a staged rollout. Use Next and Back, or the ← and → keys.

[[diagram:staged-rollout]]

Now try it yourself. Push the same bad change both ways, and change how fast alerts notice.

[[anim:rollout]]

### Validate the change first

Before any rollout, a machine should check the change. Does the file parse? Does it match the schema? Is it a normal size? Does a quick test on sample traffic stay fast? After the outage, Cloudflare planned speed tests for every firewall rule. It also planned to switch to a regex engine with run-time guarantees. [@cf2019]

Validation catches simple mistakes. It does not catch every bug, so you still need the next steps.

### Canary, then waves

A canary is a small part of the fleet that gets the change first. The rest of the fleet is the control. You compare the two: error rate, latency, CPU. [@sre-canary]

If the canary looks healthy, you go wider in waves. AWS describes a real pipeline: first one box in one zone, then one region, then groups of regions that get bigger each time. [@aws-deploys] In the first wave, AWS deploys to only one Availability Zone at a time. Its top goal is to never hurt more than one region at once. [@aws-deploys]

A simple percentage rollout follows the same idea: 1%, then 10%, then 50%, then 100% of servers or users.

### Bake time and health checks

Bake time is how long you wait at each stage before going wider. Some bugs need time or real traffic to show up. The AWS pipeline waits at least one hour after each one-box stage. It waits at least 12 hours after the first wave of regions. [@aws-deploys]

During bake time, automated health checks watch the metrics. If an alarm fires, the system rolls back on its own. No human has to wake up and decide. [@aws-deploys]

### Feature flags and kill switches

A feature flag lets you turn code on or off without a new deploy. [@toggles] Ship new code switched off, then turn it on for 1% of users, then more. If it breaks, turn it off in seconds.

Flags also act as kill switches. In June 2025, Google Cloud added new quota-checking code with no feature flag and no proper error handling. A policy with blank fields then spread to every region within seconds. The code crashed on it. The team needed about 40 minutes to finish rolling out its "red button" fix. [@gcp2025] Afterwards, Google said changes to critical binaries must be behind a flag that is off by default. It also said policy data must roll out in steps, with time to check each step. [@gcp2025]

Flags have a cost. Each one adds a branch to test. Remove a flag once the feature is fully on. [@toggles]

## Trade-offs

| Approach | Blast radius | Speed to reach everyone | Use it when |
|---|---|---|---|
| Global push | No. Every user at once. | Seconds. | A real emergency, like an active attack, and you accept the risk. |
| Canary, then waves | Yes. A small group sees a bad change first. | Hours to days. | Most code and config changes. |
| Percentage rollout behind a flag | Yes. You choose the share of users. | Minutes to days, your choice. | New features and risky code paths. |
| Blue-green (two full copies, switch traffic) | No. All traffic moves at once. | Minutes. | You need a very fast undo, and you can pay for two copies. |

**Rule of thumb:** keep a fast global path only for emergencies, and make the slow staged path the default. Cloudflare's planned fix did the same: staged rollouts for rule changes, and a global push kept only for active attacks. [@cf2019]

## At work: how a senior uses this

- Ask about every change: **"How do we undo this, and how long does it take?"** If nobody knows, the change is not ready.
- Treat config, feature flags, firewall rules and data files as code. They need review, validation and a staged rollout.
- Ask "what is the blast radius?" A change that touches every region at once needs a very good reason.
- Name the metric that decides go or no-go before you deploy. Write it in the change ticket, for example "5xx rate above 0.5% on the canary means rollback".
- Make rollback automatic where you can. Test it on a normal day, not for the first time during an outage.
- Check that the tools you use to roll back do not depend on the system you are deploying. During the 2019 outage, Cloudflare engineers had trouble reaching internal tools. [@cf2019]
- Ship big changes as many small ones. A small change is easier to check and easier to undo.

## With AI

### What to ask the agent

> "Here is our deploy pipeline config. List every way a change can reach all production servers at once, including config and feature flags."

> "Write a validation script for this config file. Check the schema, the size limits and the allowed values. Fail the build if any check fails."

> "Add a canary stage to this pipeline: deploy to 1% of hosts, wait 30 minutes, and roll back if the 5xx rate or p99 latency rises."

### What to check yourself

- The canary gets real traffic, and enough of it to show a problem.
- The rollback trigger uses metrics you trust, with clear thresholds.
- Rollback was tested, and it works when the new version is crashing.
- Config and data files go through the same stages as code, not a side path.
- New flags are off by default, and each has an owner and a plan to remove it.

## Check yourself

## Go deeper

Related lessons: [Feature flags](/learn/feature-flags-and-entitlements), [CI/CD](/learn/ci-cd-trunk-based-development) and [Incident response](/learn/incident-response-and-postmortems).
