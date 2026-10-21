---
topic: code-review
subtitle: Review for correctness, design and shared understanding, and check harder when an AI wrote the code.
status: published
reviewed_by: ai-factcheck
reviewed_on: 2026-10-21
review_by: 2027-01-19
illustrative_story: true
sources:
  - id: g-index
    title: "Google, “Google's Code Review Guidelines”, Google Engineering Practices"
    url: https://google.github.io/eng-practices/review/
    note: The start page of Google's public review guide, with one guide for reviewers and one for authors.
  - id: g-standard
    title: "Google, “The Standard of Code Review”, Google Engineering Practices"
    url: https://google.github.io/eng-practices/review/reviewer/standard.html
    note: The main rule of review at Google. Approve when the change makes code health better, even if it is not perfect.
  - id: g-looking
    title: "Google, “What to look for in a code review”, Google Engineering Practices"
    url: https://google.github.io/eng-practices/review/reviewer/looking-for.html
    note: Design, functionality, complexity, tests, naming and more. Also why you read every line.
  - id: g-small
    title: "Google, “Small CLs”, Google Engineering Practices"
    url: https://google.github.io/eng-practices/review/developer/small-cls.html
    note: Why small changes get faster and better reviews, and how to split a big one.
  - id: g-desc
    title: "Google, “Writing good CL descriptions”, Google Engineering Practices"
    url: https://google.github.io/eng-practices/review/developer/cl-descriptions.html
    note: How to write the first line and body of a change description, with bad examples.
  - id: g-comments
    title: "Google, “How to write code review comments”, Google Engineering Practices"
    url: https://google.github.io/eng-practices/review/reviewer/comments.html
    note: Be kind, explain why, label each comment as Nit, Optional or FYI, and give praise.
  - id: g-speed
    title: "Google, “Speed of Code Reviews”, Google Engineering Practices"
    url: https://google.github.io/eng-practices/review/reviewer/speed.html
    note: Answer within one business day, and when to approve with comments.
  - id: gh-help
    title: "GitHub Docs, “Helping others review your changes”"
    url: https://docs.github.com/en/pull-requests/collaborating-with-pull-requests/getting-started/helping-others-review-your-changes
    note: Short advice for authors. Small pull requests, clear descriptions and reviewing your own work first.
  - id: spracklen
    title: "Spracklen et al., “We Have a Package for You! A Comprehensive Analysis of Package Hallucinations by Code Generating LLMs”, USENIX Security 2025 (arXiv 2406.10279)"
    url: https://arxiv.org/abs/2406.10279
    note: A study of 16 models and 576,000 code samples. How often models invent package names, and how attackers can use this.
  - id: owasp-llm01
    title: "OWASP Gen AI Security Project, “LLM01:2025 Prompt Injection”"
    url: https://genai.owasp.org/llmrisk/llm01-prompt-injection/
    note: What prompt injection is, including indirect injection through files and web pages a model reads.
  - id: cc-review
    title: "Anthropic, “Code Review”, Claude Code documentation"
    url: https://code.claude.com/docs/en/code-review
    note: A managed AI review service for GitHub pull requests. It comments but never approves or blocks.
  - id: cc-review-local
    title: "Anthropic, “Review locally with /code-review”, Claude Code documentation"
    url: https://code.claude.com/docs/en/code-review-local
    note: How to run an AI review of your own branch in a separate subagent before you open a pull request.
  - id: cc-security
    title: "Anthropic, “Security”, Claude Code documentation"
    url: https://code.claude.com/docs/en/security
    note: Prompt injection defences, and why you are responsible for reviewing what the agent proposes.
  - id: infosec-slop
    title: "Infosecurity Magazine, “AI Hallucinations Create ‘Slopsquatting’ Supply Chain Threat”, 14 April 2025"
    url: https://www.infosecurity-magazine.com/news/ai-hallucinations-slopsquatting/
    note: News report on the Spracklen et al. study. It says Seth Larson of the Python Software Foundation coined the word "slopsquatting".
claims_checked:
  - claim: "Spracklen et al. tested 16 models on 576,000 code samples; at least 5.2% (commercial) and 21.7% (open-source) of recommended packages were hallucinated; 43% of hallucinated names repeated in all 10 re-runs."
    source: spracklen
    note: "Abstract and Section 5.1/5.3. The rate is per recommended package, not per output, so 'of outputs' was changed to 'of the packages they suggested'. The paper's term is 'package confusion attack'; it does not use 'slopsquatting'."
  - claim: "'Slopsquatting' is a name for this attack, credited to Seth Larson of the PSF."
    source: infosec-slop
    note: "Article (14 Apr 2025) attributes the coinage to Seth Larson, citing Socket. Source added; lesson wording ties the word to that source."
  - claim: "Google: approve once a change definitely improves overall code health; no 'perfect' code, only better code; design is the most important thing to cover; ask whether tests fail when the code is broken; one business day maximum to respond; ~100 lines reasonable, 1000 usually too large."
    source: g-standard
    note: "Checked on g-standard, g-looking, g-speed and g-small. Quote matches. 'Nobody writes tests for the tests' changed to match 'Tests do not test themselves'."
  - claim: "Claude Code Review runs several agents, verifies findings, comments inline, never approves or blocks; a review with no findings doesn't mean code is error-free or secure."
    source: cc-review
    note: "All in the opening section and the NOTE box of the current page."
  - claim: "/code-review reviews your branch's changes and runs as a background subagent with its own context window."
    source: cc-review-local
    note: "Confirmed. It runs in the foreground in -p runs, so the lesson now says 'by default'."
  - claim: "Indirect prompt injection happens when a model takes input from external sources such as websites or files; the user is responsible for reviewing proposed code."
    source: owasp-llm01
    note: "OWASP LLM01:2025 definition; Claude Code security page: 'You're responsible for reviewing proposed code and commands for safety before approval.'"
  - claim: "Google's example first line is 'Delete the FizzBuzz RPC and replace it with the new system.'"
    source: g-desc
    note: "QC1-24: CL descriptions page gives this full sentence as the imperative example. Lesson quote no longer cut short."

quiz:
  - q: A pull request has a clear design and good tests. You would name one variable differently. What should you do?
    options:
      - Request changes until the name is fixed.
      - Approve, and leave a comment that starts with "Nit:".
      - Say nothing, because names do not matter.
    answer: 1
    why: Google's guide says to approve once a change clearly improves code health. Mark small, optional points as "Nit:" so the author can choose.
  - q: An agent's diff imports a package you have never heard of. The tests pass. What do you check first?
    options:
      - That the package really exists in the official registry, is the one you expect, and is maintained.
      - That the import lines are sorted.
      - Nothing. The tests pass, so the package works.
    answer: 0
    why: Models invent package names, and attackers can publish real packages under those names. Passing tests may only mean the package was mocked.
  - q: Which of these tests is a warning sign in an AI-written diff?
    options:
      - A test that calls the real function with an empty list and checks the result.
      - A test that mocks the function under test and then checks that the mock was called.
      - A test that checks an error is raised for a negative amount.
    answer: 1
    why: A test that mocks the code it should test cannot fail when that code is broken. Ask "would this test fail if the code were wrong?"
  - q: Your team turns on an AI reviewer for every pull request. It finds no issues in a change. Is the change safe to merge?
    options:
      - Yes. The AI reviewed it.
      - Not by itself. A review with no findings does not mean the code is correct or secure. A person still decides.
      - Yes, if the AI reviewer ran at its deepest level.
    answer: 1
    why: Anthropic's own docs say an empty AI review does not mean the code is free of errors. Use AI as a second pass, not as the decider.
incidents: []
related: [coding-agents, ai-security-risks, testing-strategy]
---

## What broke

This story is made up, but each part of it has happened to real teams.

Ravi asked his coding agent to add signature checks to the payment webhook. Ten minutes later he had a 600-line pull request. The code looked clean. There were twelve new tests, and all of them passed.

His teammate Lina was busy. She skimmed the main file, saw green checks, and wrote "LGTM" ("looks good to me"). She did not open `package.json`. One new line there added a package called `webhook-signature-verify`.

The agent had invented that name. No such package existed when the model learned to code. But someone had since published a package with that exact name. It had an install script. When CI installed it, the script read the environment variables and sent the payment API key to a server outside the company.

The twelve tests did not catch it. Each one mocked the new package: it replaced the package with a fake version. So the real code never ran. One `catch` block also swallowed every error and returned `true`. So a webhook with a broken signature would have been accepted too.

The team rotated the key, removed the package and added a dependency check to CI. The code had passed review. The review had not really happened.

## The idea in simple words

Code review means someone other than the author reads the code before it is merged. Google's guide says it uses review "to maintain the quality of our code and products". [@g-index]

Review has three jobs:

1. **Correctness.** Does the code do what it should? Does it break on edge cases, such as empty or very large inputs?
2. **Design.** Does the change fit the system? Will the next person understand it? Google calls design the most important thing to cover. [@g-looking]
3. **Shared understanding.** After review, at least two people know how this code works. That helps the team on a bad night months later.

Review is not for spacing, import order or quote style. A linter and a formatter can check those every time, for free. (This is our advice. Google's guide also says reviewers should not block a change only because of their personal style. [@g-looking])

The goal is not perfect code. Google's main rule is to approve a change once it clearly improves the overall health of the code. Its guide says there is "no such thing as 'perfect' code—there is only better code". [@g-standard]

> [!key] **The key idea.** Review is where a team decides "we understand this, and we will own it". With AI, the code arrives faster, so that decision needs more care, not less.

## How it works

A good review has layers. Each one catches different problems. Step through them with Next and Back, or the ← and → keys.

[[diagram:review-flow]]

### As the author

Your job is to make the review easy.

- **Keep it small.** Small changes are reviewed faster and more carefully, and they cause fewer bugs. Google suggests about 100 lines is usually fine and 1,000 lines is usually too large. [@g-small] A 200-line change in one file may be fine. Spread across 50 files, it is usually too large. [@g-small]
- **Write a clear description.** The first line says what the change does, as a short order: "Delete the FizzBuzz RPC and replace it with the new system." The body says why, and what the known weak points are. [@g-desc] "Fix bug" is not a description. [@g-desc]
- **Add tests in the same change.** [@g-small]
- **Review it yourself first.** Read your own diff before you ask anyone else. GitHub's docs say this catches mistakes early. [@gh-help] Tell the reviewer where to look first. [@gh-help]

### As the reviewer

**Start with risk.** Do not read files in alphabetical order. First find the parts where a mistake costs the most: data deletes and migrations, money, login and permissions, secrets, and code that runs in parallel. Concurrency bugs are hard to find by running the code, so you must reason about them while you read. [@g-looking] (The risk list is our advice.)

**Read every line you are asked to review.** Google's guide says you can sometimes skim generated code and data files. But do not skim a human-written function and assume it is fine. [@g-looking] If you cannot understand it, ask the author to make it clearer. [@g-looking]

**Read the tests.** Ask one question: "Will the tests actually fail when the code is broken?" [@g-looking] Tests do not test themselves, so a person must check them. [@g-looking]

**Ask questions.** "What happens if this list is empty?" works better than "This is wrong."

**Write kind, specific comments.** Comment on the code, not the person. Explain why. [@g-comments] Label each comment, so the author knows what is required:

- `Nit:` small, fine to skip.
- `Optional:` a good idea, but not required.
- `FYI:` something for the future. [@g-comments]

When you see good work, say so. [@g-comments]

**Be fast.** Google says one business day is the maximum time to respond. [@g-speed] You can approve with comments when you trust the author to fix small points. [@g-speed]

### What changes when AI wrote the code

The rules above stay the same. What changes is the kind of mistake.

- **More code, faster.** An agent can write 600 lines in minutes. Your reading speed did not change. Ask for smaller changes.
- **Plausible but wrong.** The code has good names and comments, so it *looks* reviewed. Read it anyway. Google's "do not skim" rule names human-written code. [@g-looking] Our advice is to apply it to AI-written code too.
- **Invented APIs and packages.** A study of 16 models and 576,000 code samples found that, on average, at least 5.2% of the packages that commercial models suggested were invented. For open-source models it was 21.7%. [@spracklen] An attacker can publish a real package under an invented name. The paper calls this a form of package confusion attack. [@spracklen] Many people now call it "slopsquatting", a name credited to Seth Larson of the Python Software Foundation. [@infosec-slop] The same invented names often come back: 43% of them appeared again in all 10 repeats of the same prompt. [@spracklen] So an attacker can predict them.
- **Missing edge cases.** Empty lists, time zones, retries, a second request at the same moment.
- **Tests that test nothing.** Tests that mock the code under test, or check only that "no error was thrown".
- **Over-broad changes.** "While I was there, I also cleaned up..." Every extra file is extra risk.
- **Instructions hidden in the input.** An agent may read a README, an issue or a web page with hidden instructions in it. This is indirect prompt injection. [@owasp-llm01] It can steer the code the agent writes. So you are still responsible for reviewing everything the agent proposes. [@cc-security]

### A checklist for AI-written diffs

This list is our advice. Use it on top of a normal review.

1. Does every new package exist, with the exact name, in the official registry? Is it the one you meant, and is it maintained?
2. Does every API, method and config option exist in the version you use?
3. Are there files in the diff that the task did not need?
4. Does any `catch` or `except` hide an error, or return a default value?
5. Would each test fail if the code were wrong? Is the code under test mocked?
6. Are edge cases covered: empty input, nulls, limits, time zones, a repeat call?
7. Are there secrets, tokens or personal data in the code, tests or logs?
8. Can the author explain every line without asking the agent?

### AI as a reviewer

AI review is a good second pass. Anthropic's Code Review service runs several agents over a pull request, checks their findings, and leaves comments on the exact lines. [@cc-review] It never approves or blocks a pull request, so the decision stays with your team. [@cc-review] Its docs also warn that a review with no findings does not mean the code is free of errors or secure. [@cc-review]

You can also run `/code-review` before you open the pull request. It reviews the changes on your branch. By default it runs in a separate subagent with its own context. [@cc-review-local]

## Trade-offs

| Review style | Catches deep bugs? | Cost | Use it when |
|---|---|---|---|
| Skim and "LGTM" | No. It approves what it never read. | Almost none, until something breaks. | Never for real code. |
| Full human review of every line | Yes, if the change is small. | Slow on big diffs. Reviewers get tired. | Risky code: money, data, security. |
| Risk-first human review with automated checks | Yes. Time goes where mistakes cost most. | Needs good CI and a linter set up once. | Most changes. |
| Add an AI second pass | Yes, as an extra layer. Can be noisy or miss things. | Tokens or a paid service, and more comments to read. | Larger diffs, and all AI-written code. |
| AI review only, no human | No. Nobody owns the decision. | Cheapest. | Never as the final check. |

**Simple rule:** the less you wrote yourself, the more carefully you read.

## At work: how a senior uses this

- Before you ask for review, read your own diff on the review page, line by line, as if a stranger wrote it.
- Put "How to review this" in the description: "Start with `ledger.ts`. The rest is renames."
- Split agent output into small pull requests before anyone reviews it. A 1,500-line AI diff is a request to skim.
- Ask: "What is the worst thing this change can do in production?" Review that part first.
- For every new dependency, open its page in the registry yourself. Check the owner, age and downloads.
- Search the diff for `catch`, `except`, `TODO`, `mock` and `skip` before you read the rest.
- Mark every optional comment with `Nit:` and approve. Do not block a good change because of your personal taste. [@g-standard]

## With AI

### What to ask the agent

> "Before I open a pull request, list every new dependency you added, with a link to its registry page. Flag any you are not sure exists."

> "Review this diff as a strict senior reviewer. Focus on data loss, security and error handling. Ignore style. For each issue, give the line and a test that would show the bug."

> "For each test you wrote, tell me what bug it would catch. If a test would still pass with the function body deleted, say so."

### What to check yourself

- Each new package exists, has the exact name you expect, and comes from the official registry. [@spracklen]
- Tests call the real code and would fail if it were wrong. [@g-looking]
- No error is swallowed and turned into a quiet success.
- The diff touches only what the task needed.
- No input the agent read (an issue, a README, a web page) could have carried hidden instructions. [@owasp-llm01]
- You, not the AI reviewer, made the final decision, and you can explain every line. [@cc-review]

## Check yourself

## Go deeper

Before this: [Working with coding agents](/learn/working-with-coding-agents).

Next: [Security risks of AI-written code](/learn/ai-code-security-risks) and [Testing strategy](/learn/testing-strategy).
