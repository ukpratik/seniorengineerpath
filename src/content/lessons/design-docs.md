---
topic: design-docs
subtitle: A short written plan, shared before you build, finds problems while they are still cheap. Learn when to write one, what goes in it and how to get it agreed.
status: published
reviewed_by: ai-factcheck
reviewed_on: 2026-10-21
illustrative_story: true
sources:
  - id: ubl
    title: "Malte Ubl, “Design Docs at Google”, Industrial Empathy (2020)"
    url: https://www.industrialempathy.com/posts/design-docs-at-google/
    note: Why Google writes design docs, the usual sections, good length, when not to write one, and how reviews work.
  - id: larson
    title: "Will Larson, “Writing engineering strategy”, StaffEng"
    url: https://staffeng.com/guides/engineering-strategy/
    note: When a project needs a design doc, and practical advice on drafting and getting feedback.
  - id: hashicorp
    title: "HashiCorp, “Writing practices and culture”, How HashiCorp Works"
    url: https://www.hashicorp.com/how-hashicorp-works/articles/writing-practices-and-culture
    note: A real company process with two documents, a PRD for the problem and an RFC for the solution, and links to both templates.
  - id: uber
    title: "Gergely Orosz, “Scaling Engineering Teams via RFCs: Writing Things Down”, The Pragmatic Engineer"
    url: https://blog.pragmaticengineer.com/scaling-engineering-teams-via-writing-things-down-rfcs/
    note: His reporting on how Uber's RFC process started as an email to all engineers and grew with the company.
  - id: oxide
    title: "Oxide Computer Company, “RFD 1: Requests for Discussion”"
    url: https://oxide.computer/blog/rfd-1-requests-for-discussion
    note: An RFC process that lives in git, with clear states from first idea to fully built.
  - id: rust-rfcs
    title: "The Rust project, RFC process (README of the rust-lang/rfcs repository)"
    url: https://github.com/rust-lang/rfcs
    note: An open-source RFC process step by step, including the final comment period.
  - id: nygard
    title: "Michael Nygard, “Documenting Architecture Decisions”, Cognitect (2011)"
    url: https://cognitect.com/blog/2011/11/15/documenting-architecture-decisions
    note: The original ADR post. The five sections, why records are kept short, and why old ones are never deleted.
  - id: adr-site
    title: "ADR GitHub organisation, “Architectural Decision Records”"
    url: https://adr.github.io/
    note: Definitions, templates and tools for ADRs, collected by people who use them.
claims_checked:
  - claim: "Design docs find problems early while changes are cheap, build consensus and keep an organisational memory; alternatives considered is one of the most important sections; non-goals are things that could reasonably be goals, not negated goals."
    source: ubl
    note: "Matches the post (6 Jul 2020): 'still cheap', consensus, organizational memory; 'The system shouldn't crash' is its own non-example. Cross-cutting wording changed to 'teams should standardize'."
  - claim: "About 10 to 20 pages for large projects, a 1 to 3 page mini design doc for small work; skip the doc when the solution is not ambiguous; light review by comments or a formal meeting with senior engineers; reviews can become overhead."
    source: ubl
    note: "Post says 'sweet spot ... 10-20ish pages', '1-3 page mini design doc', reviews are 'a dangerous trap of overhead'. The bug/field rule is now labelled as our advice."
  - claim: "Write a design doc for work over a month of engineering time, work many future projects use, or work that meaningfully affects users; gather input widely but write alone; prefer good over perfect."
    source: larson
    note: "All three triggers and both pieces of advice are on the page. Page title corrected to 'Writing engineering strategy'."
  - claim: "At HashiCorp a PRD defines the problem and an RFC proposes the solution; anyone can comment, explicit approval comes from the right team leads; background takes a paragraph or two and the proposal one."
    source: hashicorp
    note: "Matches the article. 'Exit at any time' line reworded to match the source (non-stakeholders read background and proposal)."
  - claim: "Uber's RFC process began as an email to every engineer and moved to mailing lists per domain (backend, mobile, web)."
    source: uber
    note: "Orosz's own reporting: 'What started out as an email to every engineer changed into mailing lists per domain'. Now attributed to him; 'one email' and missing 'web' fixed."
  - claim: "Oxide RFDs live in a git repo, are discussed in a pull request and move through states incl. discussion, published, committed; Rust's final comment period is ten calendar days."
    source: oxide
    note: "RFD 1 states: prediscussion, ideation, discussion, published, committed, abandoned. Rust README: FCP 'lasts ten calendar days' so stakeholders can lodge final objections (source rust-rfcs)."
  - claim: "An ADR has title, context, decision, status and consequences, is one or two pages, lives in the project repo, and reversed decisions are kept and marked superseded; the set of ADRs is the decision log."
    source: nygard
    note: "Nygard 15 Nov 2011 matches; 'blindly accept or blindly reverse' wording aligned. Decision log definition from adr.github.io (adr-site)."
  - claim: "The overhead of writing and reviewing a design doc may not fit prototyping and rapid iteration."
    source: ubl
    note: "QC1-21: 'When not to write a design doc': 'the overhead of creating and reviewing a design doc may not be compatible with prototyping and rapid iteration.' Lesson no longer says a prototype teaches more."
quiz:
  - q: You need to rename one field in an internal API. Only your team uses it, and the fix is clear. Do you need a design doc?
    options:
      - Yes. Every change needs a design doc.
      - No. A clear ticket or pull request description is enough.
      - Yes, and it should go through a company-wide review.
    answer: 1
    why: A design doc pays off when the right answer is unclear or the cost of being wrong is high. Here neither is true.
  - q: Which of these is a good non-goal for a new search service?
    options:
      - The service should not crash.
      - We will not support search in languages other than English in this version.
      - We will write clean code.
    answer: 1
    why: A non-goal is something that could reasonably be a goal, but you chose to leave out. "Should not crash" is just a goal said backwards.
  - q: Why is the "Alternatives considered" section so important?
    options:
      - It shows reviewers why the chosen design is better than the other real options.
      - It makes the doc longer, so it looks more serious.
      - It lists every tool the team has ever used.
    answer: 0
    why: Without alternatives, readers cannot tell if you chose well or just chose the first idea.
  - q: What is an ADR?
    options:
      - A long design doc for a whole system.
      - A one- or two-page record of one decision, its context and its consequences, kept with the code.
      - A meeting where seniors approve designs.
    answer: 1
    why: Nygard's ADRs are short, one per decision, and stored in the repo so future engineers can find the reason.
  - q: An AI agent drafts your design doc and recommends option B. What must you still do yourself?
    options:
      - Nothing. The agent read the whole codebase.
      - Check the facts, own the decision and the trade-offs, and be ready to defend them in review.
      - Only fix the spelling.
    answer: 1
    why: The agent can draft and critique. The decision, and the cost of being wrong, stay with you.
incidents: []
related: [adrs, technical-writing, architecture-tradeoffs, requirements-nfrs]
---

## What broke

This story is made up, but it is common.

A shop has a warehouse system from 2015. It reads each order ID and expects a number with exactly 10 digits. In a kickoff meeting, an engineer named Ravi says, "Do not change the order ID format. The warehouse parses it." Everyone nods. Nobody writes it down.

Four months later, Ravi has moved to another team. A new engineer, Mei, gets a task: make order IDs safe to create on many servers at once. She chooses UUIDs, a common and good choice. The code review looks fine. Tests pass, because the warehouse is not part of the test setup.

On a Friday evening, the change goes live. The warehouse system rejects every new order. For six hours, paid orders pile up and nothing ships. The team rolls back, then spends two days moving the stuck orders by hand.

Mei did nothing wrong. The constraint lived only in the heads of the people at that meeting. One line in a design doc, "Constraint: the warehouse parses order IDs as 10-digit numbers", would have stopped this. So would a short decision record next to the code.

## The idea in simple words

A design doc is a short written plan for a piece of work. You write it before you build. It says what problem you solve, what you will not solve, the options, and why you chose one.

Malte Ubl, who wrote about design docs at Google, lists what they are for. They find design problems early, while changes are still cheap. They build agreement across the organisation. And they become a memory of why decisions were made. [@ubl]

> [!key] **The key idea.** A design doc is not paperwork. It is how you think clearly, get others to find your mistakes early, and leave a record for the people who come after you.

An RFC (request for comments) is the same idea with a more formal process. Many companies use the names in different ways. HashiCorp, for example, uses an RFC to propose a solution, after a separate document has defined the problem. [@hashicorp]

**When to write one.** Will Larson suggests a design doc for any work that takes more than a month of engineering time. He also suggests one for work that other projects will build on, or that strongly affects users. [@larson]

**When not to.** If the right solution is clear, a doc adds little value. If you are still prototyping and changing things fast, writing and reviewing a doc may slow you down. [@ubl] Our advice: fixing a bug or adding a field needs a clear ticket, not a design doc.

## How it works

Step through the life of one design doc. Use Next and Back, or the ← and → keys.

[[diagram:design-doc-life]]

### A good structure

Most good design docs have the same parts. Here is a short template you can copy:

```markdown
# Order IDs that are safe across servers
Author: Mei · Status: In review · Reviewers: Ravi, Ana

## Context
Why now, and what exists today. Two paragraphs.

## Goals and non-goals
Goals: unique IDs from any server; keep the 10-digit number format.
Non-goals: changing IDs of old orders.

## The design
How it works. One diagram. Key APIs and data.

## Alternatives considered
A: UUIDs. B: database sequence. C: Snowflake-style IDs.
Why we chose B, and what we give up.

## Constraints, risks and rollout
Warehouse parses IDs as 10-digit numbers.
Rollout: 1% of orders, then 100%. Rollback: flag off.

## Open questions
Who owns the ID service on-call?
```

A few sections deserve special care:

- **Non-goals.** Ubl explains that a non-goal is something that could reasonably be a goal, but you chose to leave out. "The system should not crash" is not a non-goal. [@ubl] Non-goals stop scope from growing in review.
- **Alternatives considered.** Ubl calls this one of the most important sections. It shows why your choice is better than the others. [@ubl]
- **Cross-cutting concerns.** Security, privacy and observability. Ubl says each team should agree on which of these its docs must cover. [@ubl]
- **Constraints.** Write down the facts you cannot change, like the warehouse format. These are what new people do not know.

Keep it short. Ubl says 10 to 20 pages works for large projects. A small change can use a "mini design doc" of 1 to 3 pages. [@ubl] HashiCorp's guide says the background takes a paragraph or two, and the proposal takes one. [@hashicorp]

### How reviews work

First, share the draft with a few people who know the area. Larson's advice is to gather input together but write alone. A doc written by a group is usually harder to fix. [@larson] Do not wait for perfect. A good doc in front of people now beats a better one next month. [@larson]

Then share more widely. Most review happens as written comments in the doc. Ubl describes this light path, and a heavier one: a formal review meeting with senior engineers. [@ubl] At HashiCorp, anyone can comment, but you need explicit approval only from the right team leads. [@hashicorp] Gergely Orosz reports that at Uber, the process started as an email to every engineer. Later it moved to mailing lists per area, such as backend, mobile and web. [@uber]

Some processes are more formal. Oxide keeps each RFD (its name for an RFC) in a git repo and discusses it in a pull request. Each one moves through states such as discussion, published and committed. [@oxide] The Rust project ends each RFC with a final comment period of ten calendar days, so people have a last chance to object. [@rust-rfcs]

### ADRs: the lightweight cousin

A design doc covers a whole project. An ADR (architecture decision record) covers one decision. Michael Nygard proposed them in a 2011 blog post. Each ADR has a title, context, decision, status and consequences. It is one or two pages, and it lives in the code repo. [@nygard]

Nygard's reason is simple. Without the "why", a new person can only blindly accept an old decision, or blindly reverse it. Both can hurt the project. [@nygard] When you reverse a decision, you do not delete the old ADR. You mark it as superseded (replaced by a newer decision) and link to the new one. [@nygard] The set of ADRs in a project becomes its decision log. [@adr-site]

### Common mistakes

- **Solution first.** The doc opens with "We will use Kafka" before it explains the problem. Readers cannot judge a fix for a problem they do not understand.
- **No real alternatives.** Only one option, or two fake ones that nobody would pick.
- **Too long.** Thirty pages of detail that nobody reads. Put the decision and the trade-offs at the top.
- **No non-goals.** Every reviewer adds a feature, and the project doubles in size.
- **Hidden constraints.** The one fact that matters, like the warehouse format, stays in someone's head.

### Write in simple English

Many of your readers do not speak English as a first language. They read on a phone, between meetings. Use short sentences. Use one word for one thing. Put the conclusion first. HashiCorp designs its RFC template so readers can stop at any time. Someone with a small stake can read only the background and the proposal. [@hashicorp]

## Trade-offs

| Option | Who it helps | Cost to write | Use it when |
|---|---|---|---|
| Just a ticket | Your team, this week. | Minutes. | The fix is clear and easy to undo. |
| ADR | Future engineers who ask "why?" | 30 to 60 minutes. | One decision that is hard to reverse or easy to break by accident. |
| Design doc | Your team and the teams around you. | Days. | More than a month of work, many teams, or the right answer is unclear. |
| Formal RFC process | The whole company or community. | Days to weeks, plus review time. | Changes that affect many teams or public users, like a new API or language feature. |

**Simple rule:** match the weight of the document to the cost of being wrong. Ubl warns that heavy reviews can turn into overhead. [@ubl]

## At work: how a senior uses this

- Before a big task, ask: **"If we get this wrong, how much does it cost to undo?"** If the answer is weeks, write a doc.
- Write the problem and the non-goals first. Share those two parts before you design anything.
- Show at least two real alternatives, with what each one costs.
- Name the decision owner and a decision date in the doc header. Reviews without a deadline never end.
- Ask reviewers for specific help: "Ana, please check the database part."
- Close every comment: accept it, or explain why not. Unanswered comments look like open problems.
- After the decision, write an ADR and link it from the code. Link the design doc too.
- When you review someone else's doc, ask: "What constraint would a new person not know?"

## With AI

### What to ask the agent

> "Here are my notes and the current code. Draft a design doc with these sections: context, goals and non-goals, design, alternatives, risks, rollout, open questions. Mark anything you guessed with [GUESS]."

> "Act as a strict senior reviewer. List the five weakest points of this design doc. Look for missing non-goals, hidden constraints, missing failure cases and a missing rollback plan."

> "Rewrite this section in simple English for non-native readers. Keep every fact. Use sentences under 20 words."

> "Turn this decision into an ADR with title, context, decision, status and consequences, in under one page."

### What to check yourself

- Every fact about your system is true. The agent may invent APIs, limits or numbers.
- The alternatives are real options your team could choose, not weak options added only to be rejected.
- The constraints come from people and systems, not only from the code. The agent cannot know what was said in a meeting.
- The decision and the trade-offs are yours. You must be able to defend them in review without the agent.
- The doc is short. Agents tend to write too much. Cut it.

## Check yourself

## Go deeper

Next, read [Architecture decision records](/learn/architecture-decision-records) and [Clear technical writing](/learn/clear-technical-writing).
