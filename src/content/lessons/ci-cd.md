---
topic: ci-cd
subtitle: Merge small changes every day, let a machine build and test each one, and ship the same tested build to production.
status: published
illustrative_story: false
sources:
  - id: sec-knight
    title: "U.S. Securities and Exchange Commission, Order against Knight Capital Americas LLC, Release No. 34-70694, 16 October 2013"
    url: https://www.sec.gov/litigation/admin/2013/34-70694.pdf
    note: The official findings on the August 2012 deploy that missed one of eight servers, and what it cost.
  - id: fowler-ci
    title: "Martin Fowler, “Continuous Integration”, martinfowler.com (revised 2024)"
    url: https://martinfowler.com/articles/continuousIntegration.html
    note: The classic description of CI, the ten-minute build, fixing broken builds at once, and why long branches hurt.
  - id: fowler-cd
    title: "Martin Fowler, “ContinuousDelivery”, martinfowler.com bliki"
    url: https://martinfowler.com/bliki/ContinuousDelivery.html
    note: A short, exact explanation of continuous delivery and how it differs from continuous deployment.
  - id: cd-patterns
    title: "Jez Humble, “Patterns”, continuousdelivery.com"
    url: https://continuousdelivery.com/implementing/patterns/
    note: The deployment pipeline, the commit stage, and the rule to build packages only once.
  - id: tbd
    title: "Paul Hammant and contributors, trunkbaseddevelopment.com"
    url: https://trunkbaseddevelopment.com/
    note: What trunk-based development is, and the techniques that let a team avoid long-lived branches.
  - id: tbd-branches
    title: "trunkbaseddevelopment.com, “Short-Lived Feature Branches”"
    url: https://trunkbaseddevelopment.com/short-lived-feature-branches/
    note: How long a branch should live, and how many people should work on it.
  - id: swe-ch23
    title: "Rachel Tannenbaum, “Continuous Integration”, chapter 23 of Software Engineering at Google (O'Reilly, 2020)"
    url: https://abseil.io/resources/swe-book/html/ch23.html
    note: Fast feedback, presubmit and post-submit tests, green head, and how flaky tests hurt CI at Google scale.
  - id: dora
    title: "DORA, “DORA's software delivery performance metrics”, dora.dev"
    url: https://dora.dev/guides/dora-metrics-four-keys/
    note: The current names and definitions of the delivery metrics, grouped into throughput and instability.
  - id: gitflow
    title: "Vincent Driessen, “A successful Git branching model”, nvie.com (with a note of reflection, 2020)"
    url: https://nvie.com/posts/a-successful-git-branching-model/
    note: The author of GitFlow explains when it still fits and when a simpler flow is better.
  - id: gh-protected
    title: "GitHub Docs, “About protected branches”"
    url: https://docs.github.com/en/repositories/configuring-branches-and-merges-in-your-repository/managing-protected-branches/about-protected-branches
    note: How required status checks block a merge until CI passes.
  - id: gh-hardening
    title: "GitHub Docs, “Security hardening for GitHub Actions”"
    url: https://docs.github.com/en/actions/security-for-github-actions/security-guides/security-hardening-for-github-actions
    note: Why you pin third-party actions to a full commit SHA, and how to scan workflows.
  - id: slsa
    title: "OpenSSF, SLSA (Supply-chain Levels for Software Artifacts), slsa.dev"
    url: https://slsa.dev/
    note: A checklist of controls that stop people tampering with your builds and packages.
  - id: gh-agent
    title: "GitHub Docs, “Risks and mitigations for GitHub Copilot cloud agent”"
    url: https://docs.github.com/en/copilot/concepts/agents/coding-agent/risks-and-mitigations
    note: How an AI agent's pull requests are limited, and why its CI runs only after a human approves.
reviewed_by: ai-factcheck
reviewed_on: 2026-10-22
claims_checked:
  - claim: Knight deployed new code from 27 July 2012 to eight SMARS servers; a technician missed one; no second technician reviewed it and no written procedure required that.
    source: sec-knight
    note: Paragraph 15 of the SEC order, read in full. Matches the knight-capital-2012-08 incident card.
  - claim: The repurposed flag woke Power Peg on the eighth server (unused since 2003, its fill counting broken by a 2005 change); about 45 minutes, over 4 million executions in 154 stocks, loss of more than $460 million; uninstalling the new code from the seven good servers made it worse.
    source: sec-knight
    note: Paragraphs 1, 13-17 and 19. "Trades" changed to "executions"; Power Peg and the 2005 change added for accuracy.
  - claim: Every developer should commit to the mainline every day; fix a failed build right away; "integration hell"; a ten-minute build is reasonable.
    source: fowler-ci
    note: All four are in the 18 January 2024 revision.
  - claim: Continuous delivery means software can be released at any time with push-button deploys; in continuous deployment every change that passes the pipeline goes to production automatically.
    source: fowler-cd
    note: Fowler says continuous delivery means you are able to deploy often "but may choose not to".
  - claim: Commit stage takes a few minutes; each passing stage triggers the next; only build packages once; deploy the same way everywhere; fix a failed commit stage at once; separate deploy from release.
    source: cd-patterns
    note: All on the Patterns page by Jez Humble.
  - claim: Trunk-based development is "a key enabler of Continuous Integration"; short-lived branches last a couple of days with one developer (two if pairing).
    source: tbd-branches
    note: The "key enabler" quote is on the tbd home page. Writer's "makes CI possible" softened to the quote.
  - claim: Cost of a bug grows almost exponentially the later it is caught; presubmit runs only fast, reliable tests; flaky tests erode confidence similar to a broken test and make rollback targets harder to find.
    source: swe-ch23
    note: Chapter by Rachel Tannenbaum. "Much like" changed to "similar to" to match.
  - claim: DORA lists five metrics. Throughput is change lead time, deployment frequency and failed deployment recovery time. Instability is change fail rate and deployment rework rate.
    source: dora
    note: Checked on dora.dev on 2026-10-22. Definitions match. Lesson rewritten to show the two groups explicitly.
  - claim: The GitFlow author's 2020 note says web apps are usually continuously delivered, suggests a simpler flow like GitHub flow, and keeps git-flow for explicitly versioned software with several versions in the wild.
    source: gitflow
    note: Note of reflection dated 5 March 2020.
  - claim: Required status checks must pass before merging into a protected branch; pin actions to a full-length commit SHA because tags can be moved or deleted.
    source: gh-hardening
    note: Status check claim checked on gh-protected. Lockfile, scanning and signing advice is now labelled as the lesson's own advice.
  - claim: Copilot cloud agent can push to only one branch; by default workflows wait for approval by a user with write access; the requester cannot approve the agent's pull request.
    source: gh-agent
    note: Writer said "only its own branch"; the doc says one branch, which can be an existing PR branch. Fixed.
quiz:
  - q: Your team's pipeline tests every merge and puts each passing build into production with no human step. What is this called?
    options:
      - Continuous integration only.
      - Continuous delivery.
      - Continuous deployment.
    answer: 2
    why: In continuous deployment every change that passes the pipeline goes to production on its own. Continuous delivery means you could deploy at any time, but a person chooses when.
  - q: A feature will take three weeks. How do you keep working on trunk?
    options:
      - Keep one branch for three weeks and merge it at the end.
      - Merge small parts every day, with the unfinished feature behind a flag that is off.
      - Work in a fork and copy the files over when done.
    answer: 1
    why: Small daily merges keep conflicts small. The flag keeps unfinished work hidden from users.
  - q: Why should the pipeline build the artifact once and promote it, instead of building again for production?
    options:
      - It saves a little disk space.
      - So the thing you deploy is exactly the thing you tested.
      - Because production servers cannot run a compiler.
    answer: 1
    why: A new build can pick up different dependencies or settings. Promoting one build means your tests are about the real release.
  - q: Which pair are both DORA throughput metrics?
    options:
      - Change lead time and deployment frequency.
      - Change fail rate and deployment rework rate.
      - Test coverage and build time.
    answer: 0
    why: DORA groups change lead time, deployment frequency and failed deployment recovery time as throughput. Change fail rate and rework rate measure instability.
  - q: An AI agent opens a pull request. Its tests all pass. What is still your job?
    options:
      - Nothing. Green CI means the change is correct.
      - Read the diff, check that the tests test the right thing, and look for changes to the pipeline itself.
      - Re-run CI three times to be sure.
    answer: 1
    why: CI only checks what its tests check. An agent can write weak tests or change the workflow files, so a human still reviews.
incidents: [knight-capital-2012-08]
related: [testing-strategy, flaky-tests, safe-deploys, feature-flags, dora-metrics, supply-chain-security, code-review, coding-agents]
---

## What broke

On 1 August 2012, Knight Capital's trading system sent millions of orders it should not have sent. In about 45 minutes it got over 4 million executions (filled trades) in 154 stocks. Knight lost over $460 million. [@sec-knight]

The cause was a deploy. Starting on 27 July, Knight put new code on its eight order-routing servers, a few at a time. A technician did not copy the new code to one of the eight. No second technician reviewed the deploy, and no written procedure asked for one. [@sec-knight]

The new code reused an old flag. On seven servers the flag turned on the new feature. On the eighth, it woke up old code called Power Peg, unused since 2003. A 2005 change had broken how that code counted filled shares. So it never knew an order was filled, and kept sending more. [@sec-knight]

It got worse. While trying to fix it, the team removed the new code from the seven good servers. Now those servers ran the old code too. [@sec-knight]

The SEC report is about missing controls, not about CI tools. But the pattern is common. A manual, step-by-step deploy means each server can end up different. In our view, an automated pipeline that ships one build to every server, and checks the version, makes this mistake much harder.

## The idea in simple words

Three ideas work together.

**Continuous integration (CI).** Everyone merges small changes into one shared branch, often. Fowler suggests every developer commits to the mainline every day. [@fowler-ci] Each merge is built and tested by a machine. If the build breaks, you fix it right away. [@fowler-ci]

**Continuous delivery.** Your software can be released to production at any time. Deploying any version is one button press. [@fowler-cd]

**Continuous deployment.** One step further. Every change that passes the pipeline goes to production with no human step. With continuous delivery you are able to deploy often, but you may choose not to. [@fowler-cd]

**Trunk-based development** is a branching model and "a key enabler of Continuous Integration". Developers work on one branch, called trunk or main, and avoid long-lived branches. [@tbd]

> [!key] **The key idea.** Small changes, merged often, each one built and tested by a machine. Small changes are easy to review, easy to test and easy to undo.

## How it works

Step through one commit, from trunk to users. Use Next and Back, or the ← and → keys.

[[diagram:trunk-pipeline-flag]]

### Why big merges hurt

When a branch lives for weeks, everyone else keeps changing trunk too. At the end, you face a long merge with many conflicts. Fowler calls the worst case "integration hell": every integration bug you fix shows two more. [@fowler-ci]

Short-lived feature branches avoid this. A branch should live a couple of days at most, with one developer, or two when pairing. You open a pull request, CI runs, a teammate reviews, you merge and delete the branch. [@tbd-branches]

For work that takes weeks, merge it in small parts behind a feature flag that is off. [@tbd] Users see nothing until you turn it on. Deploying code and releasing a feature become two separate decisions. [@cd-patterns]

### The pipeline

A deployment pipeline starts on every change. The first stage, the commit stage, should take only a few minutes. Each passing stage starts the next one. [@cd-patterns] A common shape is:

1. **Build.** Compile the code and resolve dependencies.
2. **Test.** Run unit tests, then slower integration tests.
3. **Package.** Make one build artifact, such as a container image.
4. **Deploy.** Send that artifact to staging, then to production in stages.

**Build once, then promote.** "Only build packages once" is one of the core pipeline practices. You want to be sure the thing you deploy is the thing you tested. [@cd-patterns] Deploy it the same way to every environment. [@cd-patterns] Knight's servers ran different code. With one artifact, promoted everywhere, that is much less likely.

### Keep main green

A red build on trunk blocks everyone. If the commit stage fails, fix it at once, and nobody adds new work on top. [@cd-patterns] Often the fastest fix is to revert your change, then fix it calmly on a branch.

GitHub can enforce part of this. With required status checks, nobody can merge into a protected branch until the checks pass. [@gh-protected]

### Keep feedback fast

Google's CI chapter says the cost of a bug grows almost exponentially the later you catch it. So CI aims for fast feedback. [@swe-ch23] Google runs only fast, reliable tests before submit. Slower tests run after submit. [@swe-ch23] Fowler says a ten-minute build is reasonable. [@fowler-ci]

[Flaky tests](/learn/flaky-tests) hurt CI badly. They erode trust in the build, similar to a broken test. They also make it harder to find which change to roll back. [@swe-ch23] See [Testing strategy](/learn/testing-strategy) for how to keep a suite fast and trusted.

### Measure delivery: the DORA metrics

DORA studies software delivery. Its guide now names five metrics, in two groups. [@dora]

**Throughput:**

- **Change lead time:** time from a commit in version control to that change running in production.
- **Deployment frequency:** how many deploys you do in a period, or the time between them.
- **Failed deployment recovery time:** how long it takes to recover from a deploy that fails and needs immediate action.

**Instability:**

- **Change fail rate:** the share of deploys that need immediate action after they go out.
- **Deployment rework rate:** the share of deploys that are unplanned and happen because of an incident in production.

Note that recovery time counts as throughput here, not instability. [@dora] DORA says speed and stability are not trade-offs. For most teams the metrics are correlated. [@dora]

### Protect the pipeline itself

Your pipeline can run code from strangers, so it is also an attack path. GitHub advises pinning third-party actions to a full-length commit SHA. A tag can be moved or deleted by someone who gets access to that repository. [@gh-hardening] Our advice: pin your dependencies with a lockfile, scan them for known flaws, and sign what you build. SLSA is a checklist of controls that help stop tampering with builds and packages. [@slsa] See [Dependency and supply-chain security](/learn/dependency-supply-chain-security) and [the OWASP lesson](/learn/owasp-top-10-secure-coding).

## Trade-offs

| Option | Merge pain | Needs strong automated tests? | Fits |
|---|---|---|---|
| Trunk-based, short branches | Yes, low. Small daily merges. | Yes. CI is the safety net. | Most teams that deploy a web service often. |
| Long-lived feature branches (GitFlow style) | No, high. Weeks of drift, then a big merge. | Less, but bugs show up late. | Software with several versions in use at once, like installed apps. |
| Continuous delivery | Not about merging. | Yes. Every build must be releasable. | Teams that want a human to choose the release time. |
| Continuous deployment | Not about merging. | Yes, plus good monitoring and fast rollback. | Teams with trusted tests, staged rollouts and feature flags. |

The author of GitFlow wrote in 2020 that web apps are usually delivered continuously. For them he suggests a simpler flow. GitFlow still fits when you must support several versions in use at once. [@gitflow]

**Simple rule:** start with trunk-based development and continuous delivery. Move to continuous deployment once your tests and rollback are trusted.

## At work: how a senior uses this

- Keep pull requests small. If a PR is open for more than two days, ask how to split it.
- Ask "can we ship this unfinished, behind a flag?" before anyone creates a long-lived branch.
- Treat a red main branch as an urgent problem for the whole team. Revert first, debug second.
- Watch the CI time. When it grows past about ten minutes, move slow tests out of the merge check or run them in parallel.
- Quarantine flaky tests with an owner and a date. Never make "just re-run it" the normal fix.
- Check that production runs the exact artifact that passed tests. Ask: "Can anyone deploy a build that did not go through the pipeline?"
- Track change lead time and change fail rate together. Use them to find slow or risky steps, not to rank people.
- Remove dead code and old flags. Knight's old code stayed callable for years. [@sec-knight]

## With AI

AI coding agents now open pull requests on their own. On GitHub, Copilot's cloud agent can push to only one branch. By default, CI does not run on its pull request until a person with write access approves the workflow run. The person who asked the agent cannot approve its pull request. [@gh-agent] In our view, CI becomes the main safety net for agent code. It runs on every change and does not get tired.

### What to ask the agent

> "Here is our CI workflow. Find steps that are slow or run in series but could run in parallel. Estimate the time saved for each."

> "List every third-party action and dependency in this repo that is not pinned to an exact version or commit SHA. Propose the pinned versions."

> "This feature is too big for one PR. Split the plan into PRs of under 200 lines. Each must keep main green, with new behaviour behind a flag that is off."

### What to check yourself

- The agent's tests test real behaviour. A test with no real assertion is always green.
- The pull request does not change the CI workflow, skip tests or loosen checks without a clear reason.
- New dependencies are real, well-known packages at pinned versions.
- The agent did not "fix" a red build by deleting or disabling a failing test.
- A human reviewed the diff before merging, even when every check is green.

## Check yourself

## Go deeper

Related lessons: [Safe deploys](/learn/safe-deploys-config-changes), [Testing strategy](/learn/testing-strategy), [Feature flags](/learn/feature-flags-and-entitlements), [DORA metrics](/learn/dora-metrics-delivery-performance) and [Working with coding agents](/learn/working-with-coding-agents).
