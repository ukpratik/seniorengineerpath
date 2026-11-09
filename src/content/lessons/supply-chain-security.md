---
topic: supply-chain-security
subtitle: Most of the code you ship was written by strangers. Here is how to know what you run, and to trust it.
status: published
illustrative_story: false
sources:
  - id: freund-xz
    title: "Andres Freund, “backdoor in upstream xz/liblzma leading to ssh server compromise”, oss-security mailing list (29 March 2024)"
    url: https://www.openwall.com/lists/oss-security/2024/03/29/4
    note: The first public report. How slow SSH logins led to a backdoor hidden in the xz release tarballs.
  - id: pytorch-triton
    title: "PyTorch blog, “Compromised PyTorch-nightly dependency chain between December 25th and December 30th, 2022.”"
    url: https://pytorch.org/blog/compromised-nightly-dependency/
    note: A real dependency confusion attack. Why pip picked the fake package, what it stole, and how PyTorch fixed it.
  - id: gh-tj
    title: "GitHub Advisory Database, “tj-actions changed-files through 45.0.7 allows remote attackers to discover secrets by reading actions logs.” (GHSA-mrrh-fwg8-r2c3)"
    url: https://github.com/advisories/GHSA-mrrh-fwg8-r2c3
    note: How moved tags made thousands of CI workflows print their secrets into logs.
  - id: owasp-a03
    title: "OWASP Top 10:2025, A03:2025 Software Supply Chain Failures"
    url: https://top10.owasp.org/2025/A03_2025-Software_Supply_Chain_Failures/
    note: Real attacks such as Shai-Hulud, and a prevention list from SBOMs to signed builds.
  - id: npm-lock
    title: "npm Docs, “package-lock.json”"
    url: https://docs.npmjs.com/cli/v10/configuring-npm/package-lock-json
    note: What the lockfile records, including where each package came from and its integrity hash.
  - id: npm-ci
    title: "npm Docs, “npm-ci”"
    url: https://docs.npmjs.com/cli/v10/commands/npm-ci
    note: The install command for CI, which fails instead of changing the lockfile.
  - id: dependabot
    title: "GitHub Docs, “Dependabot alerts”"
    url: https://docs.github.com/en/code-security/dependabot/dependabot-alerts/about-dependabot-alerts
    note: How a scanner matches your dependency graph against an advisory database, and its limits.
  - id: slsa-build
    title: "SLSA v1.2, “Build: Track Basics”, slsa.dev"
    url: https://slsa.dev/spec/v1.2/build-track-basics
    note: The four build levels, L0 to L3, and the threats each one stops.
  - id: sigstore
    title: "Sigstore documentation, “Overview”"
    url: https://docs.sigstore.dev/about/overview/
    note: Keyless signing with short-lived certificates, Cosign, Fulcio and the Rekor log.
  - id: npm-provenance
    title: "npm Docs, “Generating provenance statements”"
    url: https://docs.npmjs.com/generating-provenance-statements
    note: How to publish an npm package with provenance from CI, and how users check it.
  - id: gh-hardening
    title: "GitHub Docs, “Secure use reference”"
    url: https://docs.github.com/en/actions/security-for-github-actions/security-guides/security-hardening-for-github-actions
    note: Pinning actions to a commit SHA, token permissions, and risky triggers.
  - id: spracklen
    title: "Spracklen et al., “We Have a Package for You! A Comprehensive Analysis of Package Hallucinations by Code Generating LLMs”, USENIX Security 2025 (arXiv 2406.10279)"
    url: https://arxiv.org/abs/2406.10279
    note: How often code models invent package names that do not exist, across 576,000 code samples.
claims_checked:
  - claim: The backdoor was in xz 5.6.0 and 5.6.1; the build-to-host.m4 line that started it was only in the release tarballs, not in Git, and ran an obfuscated script at the end of configure using data from two test files.
    source: freund-xz
    note: Read the full oss-security post. "Solely in the distributed tarballs"; the test files themselves were in the repository.
  - claim: OpenSSH does not use liblzma directly; Debian and other distributions patch it for systemd notification and libsystemd depends on liblzma. A failed login took about 0.8 s instead of 0.3 s.
    source: freund-xz
    note: Timings 0.807s vs 0.299s for a login as a non-existent user.
  - claim: PyPI took precedence over the PyTorch nightly index, so pip installed the malicious torchtriton (Linux nightly, 25 to 30 December 2022); when imported it read files including $HOME/.ssh/* and uploaded them via DNS queries. PyTorch registered a dummy package on PyPI.
    source: pytorch-triton
    note: Exfiltration via encrypted DNS queries to *.h4ck[.]cfd.
  - claim: Attackers moved several tj-actions/changed-files tags (e.g. v44.5.1) to a malicious commit that dumped Runner Worker memory and printed secrets base64-encoded in logs, live 14 to 15 March 2025; over 23,000 repositories used the action.
    source: gh-tj
    note: Read via gh api. The 23,000 figure counts repositories using the action, not confirmed leaks.
  - claim: SLSA Build L1 provenance "is trivial to bypass or forge"; L2 is signed provenance from a hosted build platform and stops tampering after the build; L3 is a hardened platform and stops tampering during the build.
    source: slsa-build
    note: Build Track Basics, v1.2.
  - claim: npm provenance "does not guarantee the package has no malicious code"; it is published with npm publish --provenance from GitHub Actions or GitLab CI/CD and checked with npm audit signatures.
    source: npm-provenance
    note: Quote exact.
reviewed_by: ai-factcheck
reviewed_on: 2026-11-09
quiz:
  - q: "In the 2024 xz backdoor, where was the line that started the hidden build step?"
    options:
      - In a normal source file that reviewers read in Git.
      - In the release tarballs only. The Git repository did not have the line that started it.
      - In the OpenSSH source code.
    answer: 1
    why: Freund found that the line in build-to-host.m4 that ran the hidden script was only in the distributed tarballs, not in git.
  - q: "Your workflow has `uses: some-org/lint-action@v3`. What is the main supply-chain risk?"
    options:
      - Someone with access to that repo can move the v3 tag to a new, harmful commit.
      - Tags make the workflow slower.
      - Version 3 might have a CVE that Dependabot cannot report.
    answer: 0
    why: GitHub warns that a tag can be moved or deleted. In the tj-actions attack, tags were moved to a malicious commit. A full commit SHA cannot be moved.
  - q: "Your app installs an internal package `acme-utils` from a private index, with the public index as an extra source. What best stops dependency confusion?"
    options:
      - Give the internal package a very high version number.
      - Run a vulnerability scanner every night.
      - Own the name on the public index too, and install internal packages only from the index you control.
    answer: 2
    why: PyTorch was hit because pip preferred the public index. Its fix included a placeholder package on PyPI. A scanner only knows about published flaws.
  - q: "A package's provenance is made by a script on a developer laptop and is not signed. What is the highest SLSA build level it can claim?"
    options:
      - Build L1. Provenance exists, but it is easy to forge.
      - Build L2. Any provenance is enough.
      - Build L3. The build is hardened.
    answer: 0
    why: L2 needs a hosted build platform that signs the provenance. Unsigned provenance from a laptop shows how it was built, but anyone could fake it.
  - q: "npm shows a verified provenance attestation for a package. What does that tell you?"
    options:
      - The package contains no malicious code.
      - Which source repository and CI build produced it.
      - The package has no known CVEs.
    answer: 1
    why: Provenance links the package to its source and build. npm says it does not guarantee the package has no malicious code.
incidents: [xz-utils-2024-03-backdoor]
glossary_optout: [deadline]
related: [owasp-secure-coding, secrets-management, ci-cd, code-review, coding-agents, threat-modelling]
---

## What broke

In March 2024, a developer named Andres Freund noticed something small. SSH logins on his Debian unstable (sid) systems used a lot of CPU. A tool called valgrind also showed odd errors. [@freund-xz]

He measured it. A failed login took about 0.8 seconds instead of 0.3. He traced the delay to liblzma, part of the xz compression tools. Then he found a backdoor. [@freund-xz]

The bad code was in xz versions 5.6.0 and 5.6.1. The part that started it was not in the Git repository that people read. It was only in the release tarballs, the files that Linux distributions download and build. [@freund-xz] There, a line in one build file ran a hidden script at the end of `configure`. That script pulled more code out of two "test" files in the repository and put it into the library. [@freund-xz]

OpenSSH does not use liblzma directly. But Debian and several other distributions patch it to talk to systemd, and libsystemd depends on liblzma. That chain loaded the backdoor into the SSH server. [@freund-xz]

Freund pointed to weeks of activity by the person who committed the changes. He wrote that they were "either directly involved" or their system was badly compromised. [@freund-xz] Luckily, few distributions had shipped 5.6.0 or 5.6.1 yet, and mostly in pre-release versions. [@freund-xz]

Look at what the attacker needed. Not a bug in your code. Just one trusted committer, one build step nobody read, and a dependency of a dependency.

## The idea in simple words

Your app is mostly code that you did not write. Every library, every build tool and every CI action runs with your app's power. OWASP now ranks "Software Supply Chain Failures" third in its Top 10. [@owasp-a03] You met it in the [OWASP lesson](/learn/owasp-top-10-secure-coding).

You face two separate problems:

- **Known flaws.** A library you use has a published bug, a CVE. You need to know you use it, and then upgrade.
- **Malicious code.** Someone put harmful code into a package, a build or a tool on purpose. No scanner knows about it yet.

The defenses answer three questions. *What exactly do we run?* A lockfile and an SBOM answer that. *Is any of it known to be bad?* A scanner answers that. *Did it really come from where we think?* Signatures and provenance answer that.

> [!key] **The key idea.** Treat every dependency, build tool and CI action as code that runs with your permissions. Fix the exact versions, know the full list, check where each piece came from, and give your pipeline the smallest power it needs.

## How it works

### Where attacks get in

Step through the path from source code to your app. Use Next and Back, or the ← and → keys.

[[diagram:supply-chain-attack-points]]

Here are the common ways in:

- **Transitive dependencies.** You add 5 packages. They can bring in hundreds more. OWASP says to track "not just your direct dependencies, but their (transitive) dependencies". [@owasp-a03] xz reached SSH through two hops. [@freund-xz]
- **Typosquatting.** An attacker publishes `reqeusts` and waits for someone to mistype `requests`.
- **Dependency confusion.** Your private package name also exists on a public index. From 25 to 30 December 2022, PyTorch nightly installs on Linux used a package called `torchtriton` from PyTorch's own index. Someone uploaded `torchtriton` to PyPI. Because the PyPI index took precedence, pip installed the fake one. [@pytorch-triton] When imported, it read files such as `$HOME/.ssh/*` and sent them out through DNS queries. [@pytorch-triton]
- **Maintainer takeover.** An attacker gets a maintainer's account or token, or becomes a maintainer, as in xz. The Shai-Hulud worm of 2025 stole npm tokens and used them to publish bad versions of other packages. It reached over 500 package versions. [@owasp-a03]
- **Compromised build systems and CI.** In March 2025, attackers moved several version tags of the GitHub Action `tj-actions/changed-files` to a malicious commit. The code read secrets from the runner's memory and printed them, encoded, in workflow logs. The bad code was live on 14 and 15 March 2025. GitHub's advisory says over 23,000 repositories used the action. [@gh-tj]

### Lockfiles: know exactly what you install

A version range like `^4.17.0` means "any 4.x from 4.17 up". Each install can pick a different version. A lockfile fixes the whole tree. npm's `package-lock.json` lets later installs "generate identical trees, regardless of intermediate dependency updates". [@npm-lock] For each package it stores where it came from and an `integrity` hash. [@npm-lock] So a swapped file fails the install.

In CI, use the strict install. `npm ci` needs a lockfile. If the lockfile and `package.json` disagree, it "will exit with an error, instead of updating the package lock". It never writes to either file. [@npm-ci] Python, Go and Rust tools have the same idea under other names.

Commit the lockfile. Review changes to it like code: a new package in the lockfile is new code in production.

### Scanning: when to upgrade

A scanner such as Dependabot compares your dependency graph with an advisory database. It alerts when a new advisory affects a version you use. [@dependabot] GitHub is clear about the limits: "Alerts can't catch every security issue", and new flaws can take time to reach the database. [@dependabot] A scanner finds known flaws. It does not find a fresh backdoor.

So when should you upgrade? OWASP says to choose versions on purpose and "upgrade only when there is need". It also says to watch for libraries that are no longer maintained. [@owasp-a03] OWASP gives no time frames. Here is our own suggestion:

- **Critical flaw in code you actually call:** upgrade within days, like any production bug.
- **Other alerts:** batch them into a regular update, for example every two weeks.
- **A brand-new release with no security reason:** wait, for example a few days. Others then have time to spot a bad release before you install it.

OWASP also suggests [staged rollouts](/learn/safe-deploys-config-changes) for updates, so one bad vendor release does not reach every system at once. [@owasp-a03]

### SBOM: the full list

A software bill of materials lists every component and version in what you ship. OWASP says to generate and manage it centrally for all your software. [@owasp-a03] When the next Log4Shell appears, you search your SBOMs instead of asking every team. Generate one per build artifact, in CI, and store it next to the artifact.

### Signed builds and provenance

A lockfile says *which* version. It cannot say that the package was built from the source you think. The xz tarballs were not the same as git. [@freund-xz]

**Provenance** is a record of how an artifact was built: the source, the build platform and the inputs. [@slsa-build] SLSA is a framework that grades this in build levels. [@slsa-build]

- **Build L0:** nothing. "L0 represents the lack of SLSA." [@slsa-build]
- **Build L1:** provenance exists. It helps prevent mistakes but "is trivial to bypass or forge". [@slsa-build]
- **Build L2:** a hosted build platform makes and signs the provenance. This stops tampering after the build. [@slsa-build]
- **Build L3:** a hardened build platform. Runs cannot affect each other, and build steps cannot reach the signing secret. This stops tampering during the build. [@slsa-build]

Signing used to mean guarding a long-lived private key. **Sigstore** removes that. The signer proves its identity with an OIDC token. Sigstore's certificate authority, Fulcio, issues a short-lived certificate, and the private key is thrown away after one signing. [@sigstore] Each signature goes into Rekor, a transparency log. It is append-only, and anyone can audit it. [@sigstore]

You can use this today. On npm, `npm publish --provenance` from GitHub Actions or GitLab CI/CD signs provenance through Sigstore. Users check it with `npm audit signatures`. [@npm-provenance] npm is careful here: provenance "does not guarantee the package has no malicious code". [@npm-provenance] It proves where a package came from, not that the source is safe.

### Hardening CI

Your CI runs third-party code with your secrets. The [CI/CD lesson](/learn/ci-cd-trunk-based-development) showed pinning briefly. Here is the full set.

```yaml
permissions:
  contents: read        # default for every job: read-only
jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      # pin to a full commit SHA, with the version as a comment
      - uses: actions/checkout@<full-commit-sha>  # v4
      - run: npm ci
```

- **Pin actions to a full commit SHA.** GitHub warns that "a tag can be moved or deleted" by someone who gets access to the action's repo. [@gh-hardening] That is what happened to tj-actions. [@gh-tj] Check that the SHA comes from the action's own repo, not a fork. [@gh-hardening]
- **Give the token least privilege.** Start with read-only contents. Raise permissions only for the job that needs them. [@gh-hardening] Use short-lived OIDC credentials for deploys, as in [Secrets and key management](/learn/secrets-and-key-management).
- **Be careful with `pull_request_target`.** It runs with privileges. Combined with checking out untrusted pull request code, GitHub says it can lead to repository takeover. [@gh-hardening]
- **Keep pinned actions up to date.** Dependabot can update action references. Its alerts do not cover actions pinned to a SHA, so plan regular updates. [@gh-hardening] [@dependabot]
- **Build once and promote.** OWASP lists separation of duties, signed builds, environment-scoped secrets and promoting artifacts rather than rebuilding. [@owasp-a03]

If a CI dependency is compromised, treat every secret it could read as leaked. Rotate them. [@gh-tj]

## Trade-offs

| Defense | Stops | Does not stop | Cost |
|---|---|---|---|
| Lockfile plus `npm ci` | Yes. Silent version changes and swapped files. [@npm-lock] | A bad version you chose to lock. | Low. Review lockfile diffs. |
| Vulnerability scanning | Yes. Known CVEs in versions you use. [@dependabot] | No. New backdoors and flaws not yet in the database. [@dependabot] | Low to start. Ongoing upgrade work. |
| SBOM per build | Slow answers to "are we affected?" | No. It only lists. It blocks nothing. | Low. One CI step. |
| Provenance and signatures | Yes. Packages not built from the claimed source and workflow. [@slsa-build] | No. Malicious code in the real source. [@npm-provenance] | Medium. Build setup, and verify on install. |
| Actions pinned to SHA | Yes. Moved tags. [@gh-hardening] | A bad commit you pinned yourself. | Medium. You must update pins. |
| Least-privilege CI token | Damage after a CI compromise. [@gh-hardening] | The compromise itself. | Low. |

**Simple rule:** lock and scan everything from day one. Pin CI actions and cut token permissions next. Add provenance for anything you publish.

## At work: how a senior uses this

- In review, treat a lockfile diff as a code diff. Ask: **"Why do we need this new package, and who maintains it?"**
- Before adding a dependency, check its downloads, maintainers, last release and its own dependency count. Prefer the standard library for small tasks.
- Ask: **"If this package turned bad tonight, which secrets could it read?"** Install scripts and CI steps run with real credentials.
- Use scoped or namespaced names for internal packages, and own the same names on public indexes. Point installs at one index you control.
- Keep an SBOM per release, so "are we affected?" takes minutes.
- Set a written patch deadline for critical CVEs, and track it like a bug.
- Pin every third-party action to a SHA. Set `permissions: contents: read` at the top of every workflow.

## With AI

Coding agents add packages to fix problems. They can also suggest package names that do not exist. One study of 576,000 code samples found that, on average, at least 5.2% of the packages from commercial models did not exist. For open-source models it was 21.7%. [@spracklen] Attackers can register those names. This is called slopsquatting.

### What to ask the agent

> "List every direct dependency in this repo with its version, last release date and number of maintainers. Flag any package that is unmaintained or has fewer than 1,000 weekly downloads."

> "Find every GitHub Action in .github/workflows that is not pinned to a full commit SHA. Propose the pinned line with the version as a comment. Do not change behaviour."

> "Add a top-level `permissions: contents: read` to each workflow. Then list which jobs need more, and why."

### What to check yourself

- Every package the agent added really exists, is the one you meant, and is in the lockfile.
- The agent did not change the lockfile in ways the task did not need.
- Each pinned SHA comes from the action's own repo, not a fork.
- The agent did not add `pull_request_target` or wider token permissions to make a workflow pass.
- No new install script runs during the build without a reason.

## Check yourself

## Go deeper

- [OWASP Top 10 and secure coding](/learn/owasp-top-10-secure-coding): where supply-chain failures sit among the common flaws.
- [Secrets and key management](/learn/secrets-and-key-management): what to rotate after a CI compromise.
- [CI/CD and trunk-based development](/learn/ci-cd-trunk-based-development): build once, then promote.
