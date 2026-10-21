---
topic: what-senior-means
subtitle: Senior is not about years or typing speed. It is about how much you own, and how well you decide.
status: published
reviewed_by: ai-factcheck
reviewed_on: 2026-10-21
illustrative_story: true
sources:
  - id: dropbox
    title: "Dropbox, Engineering Career Framework (public career ladder)"
    url: https://dropbox.github.io/dbx-career-framework/
    note: A real company ladder. Open the IC2, IC3, IC4 and IC5 pages to see how scope and reach grow per level.
  - id: larson-archetypes
    title: "Will Larson, “Staff archetypes”, StaffEng (from the book Staff Engineer: Leadership beyond the management track)"
    url: https://staffeng.com/guides/staff-archetypes/
    note: Four common shapes of staff work, and how they differ.
  - id: larson-matters
    title: "Will Larson, “Work on what matters”, StaffEng"
    url: https://staffeng.com/guides/work-on-what-matters/
    note: How to choose work with real impact, and the traps of easy or showy work.
  - id: reilly
    title: "Tanya Reilly, The Staff Engineer's Path (O'Reilly)"
    url: https://noidea.dog/staff
    note: The author's page for the book. Its three parts are big-picture thinking, project execution and levelling up others.
  - id: orosz
    title: "Gergely Orosz, The Software Engineer's Guidebook"
    url: https://www.engguidebook.com/
    note: A practical book on senior, tech lead and staff roles at tech companies and startups.
  - id: metr
    title: "METR, “Measuring the Impact of Early-2025 AI on Experienced Open-Source Developer Productivity” (July 2025)"
    url: https://metr.org/blog/2025-07-10-early-2025-ai-experienced-os-dev-study/
    note: A randomized controlled trial with 16 experienced developers. Read the caveats section too.
  - id: metr-2026
    title: "METR, follow-up results on AI and developer speed (February 2026)"
    url: https://metr.org/blog/2026-02-24-uplift-update/
    note: Newer data from METR. The results are not conclusive, and the page explains why.
  - id: dora-2025
    title: "DORA (Google Cloud), State of AI-assisted Software Development 2025"
    url: https://dora.dev/research/2025/dora-report/
    note: A large survey on how teams use AI. Its main finding is that AI amplifies what a team already is.
  - id: dora-research
    title: "DeBellis, Storer, Harvey et al., DORA 2025 State of AI-assisted Software Development Report, Google Research"
    url: https://research.google/pubs/dora-2025-state-of-ai-assisted-software-development-report/
    note: The report's abstract, with the survey size and the main finding.
claims_checked:
  - claim: "At Dropbox, IC2 solves \"defined problems\", IC3 solves \"ambiguous, open-ended problems\", and IC5 (staff) delivers \"multi-year, multi-team\" goals."
    source: dropbox
    note: "IC2, IC3 and IC5 Software Engineer pages, Scope section: exact phrases found. Dropbox does not call IC2 'mid-level', so the text now names the IC levels."
  - claim: "Larson describes four common staff archetypes: Tech Lead, Architect, Solver and Right Hand; snacking is easy low-impact work and preening is low-impact, high-visibility work."
    source: larson-archetypes
    note: "Staff archetypes page lists 'the four common archetypes' with these names. Work on what matters page defines snacking and preening; preening wording changed to match 'high-visibility'."
  - claim: "Reilly's book has three parts: big-picture thinking, project execution and levelling up."
    source: reilly
    note: "noidea.dog/staff lists Part One Big Picture Thinking, Part Two Project Execution, Part Three Levelling Up."
  - claim: "In METR's July 2025 RCT, 16 experienced open-source developers took 19% longer with AI tools but believed AI had sped them up by 20%."
    source: metr
    note: "Blog post: RCT, 16 developers, large mature repos, 246 issues, Cursor with Claude 3.5/3.7 Sonnet, 19% longer, believed 20% speedup (expected 24%). Page caveats: not representative of most work; results now 'out of date'. Lesson and quiz now say open-source and early-2025 tools."
  - claim: "METR's February 2026 update is not conclusive; developers are probably sped up now, but the size is unclear."
    source: metr-2026
    note: "Update reports -18% and -4% time change with wide intervals, selection bias and tracking problems; calls the data 'only very weak evidence'. Lesson now states the likely direction, not only 'not conclusive'."
  - claim: "DORA 2025 finds AI acts as an amplifier of a team's existing strengths and weaknesses; it is survey data."
    source: dora-2025
    note: "Report page: 'AI's primary role is as an amplifier, magnifying an organization's existing strengths and weaknesses.' Survey size (nearly 5,000) is from Google Research's page for the report, not the cited page."
  - claim: "The DORA 2025 survey had responses from nearly 5,000 technology professionals."
    source: dora-research
    note: "QC1-22: Google Research page: 'survey responses from nearly 5,000 technology professionals from around the world'. Number now cited to this page, not dora.dev."
  - claim: "Larson's Right Hand helps a senior leader run a large, complex part of the company."
    source: larson-archetypes
    note: "QA1-18 fix. Staff archetypes page: 'The Right Hand extends an executive's attention, borrowing their scope and authority to operate particularly complex organizations.'"
quiz:
  - q: Which of these is the clearest sign that someone works at senior level?
    options:
      - They have more than five years of experience.
      - They take an unclear problem, define it, and own the result until it works in production.
      - They know more frameworks than anyone else on the team.
    answer: 1
    why: Ladders such as Dropbox's describe levels by scope, ambiguity and ownership, not by years or tool count.
  - q: Your manager says "Users complain that search is slow." What would a senior do first?
    options:
      - Add a cache to the search service right away.
      - Ask which searches are slow, for whom, and how slow, then agree on a goal.
      - Wait for a ticket with exact steps.
    answer: 1
    why: The problem is still unclear. A senior defines it and the goal before choosing a fix.
  - q: In METR's 2025 study, experienced open-source developers used AI tools on real tasks in their own projects. What happened?
    options:
      - They finished 20% faster, as they believed.
      - They took about 19% longer, but believed they were faster.
      - There was no difference at all.
    answer: 1
    why: The tasks took about 19% longer, yet the developers believed AI had made them about 20% faster. This was one study of early-2025 tools, not a rule for all work.
  - q: AI agents can now write a lot of code fast. Which skill becomes more valuable, not less?
    options:
      - Typing speed.
      - Remembering API details by heart.
      - Deciding what to build and judging whether the code is right.
    answer: 2
    why: When code is cheap to produce, the scarce skill is judgement about what to build and whether it is correct.
incidents: []
related: [idempotency-retries]
---

## What broke

This story is made up, but it is common. Rahul is a strong mid-level engineer. He writes clean code and he ships fast. His manager asks him to "build a new notification service". Users miss important messages, and the old system is slow.

Rahul starts the next morning. In three weeks he has a fast service with good tests. He is proud of it, and the code review is easy.

Then the problems start. The email team already planned to fix the old system next quarter. Nobody asked them. Support says users miss messages because the app turns off alerts by default. Speed was never the real problem.

Now the company has two notification systems. Nobody wants to move the old messages to the new one. Six months later, Rahul's service is turned off.

His code was good. The project failed anyway. He solved the problem he was given, not the problem that existed. Nobody owned the question "Are we building the right thing?"

## The idea in simple words

Being senior is not about writing harder code. It is about which problems people trust you to solve: how big they are and how unclear. Many company career ladders describe this with a few ideas:

- **Scope:** how big the piece of work is that you own.
- **Ambiguity:** how unclear the problem is when it reaches you.
- **Ownership:** whether you own a task or the final result.
- **Impact:** what changed for users or the business because of your work.
- **Judgement:** how well you choose, for example what to build, what to skip and what to cut.

You can see this in a real, public ladder. Dropbox names its levels IC2, IC3 and so on. IC means individual contributor: an engineer who does not manage people. At Dropbox, an IC2 engineer solves "defined problems" for team-level goals. At IC3, the engineer solves "ambiguous, open-ended problems". At IC5, the staff level, the engineer delivers "multi-year, multi-team" goals. [@dropbox]

> [!key] **The key idea.** A mid-level engineer solves the problem well. A senior engineer first makes sure it is the right problem, then owns the result until it works.

Senior is also **not** some things people often think:

- **Not a number of years.** Some people reach senior in four years. Some never do. Years only give you chances to practise.
- **Not being the best coder.** Strong coding is the base, not the goal. Rahul was a great coder.
- **Not knowing every tool.** Seniors learn tools when they need them. They know which questions to ask about any tool.

Above senior is the staff engineer. Will Larson describes four common shapes of this role: Tech Lead, Architect, Solver and Right Hand. The Right Hand helps a senior leader run a large, complex part of the company. [@larson-archetypes] Tanya Reilly splits staff work into three parts: big-picture thinking, executing projects, and helping other people grow. [@reilly]

## How it works

Step through how scope grows as you move up. Use Next and Back, or the ← and → keys.

[[diagram:senior-scope]]

Each step changes three things at once. The work gets bigger. The problem gets less clear. And you own more of the result.

At mid-level, you get a defined problem and choose how to build it. At senior, you often get a vague problem, like "checkout feels slow". You turn it into a clear goal, a plan and a finished result. That middle step is new: nobody does it for you.

Week to week, a senior engineer often does these things:

- Writes a short design doc before a big change, and asks others to find problems early.
- Says "no" or "not now" to work that does not matter, and explains why.
- Reviews code carefully and teaches through the review, not only approves.
- Notices risks early and tells people, even when the news is bad.
- Helps a newer teammate get unstuck, instead of doing their work for them.
- Checks that the feature really helped users after launch.

Larson warns about two traps here. "Snacking" is easy work with little impact. "Preening" is work that is very visible but has little impact. [@larson-matters]

## Trade-offs

There is more than one way to grow. Each one has a cost.

| Way to grow | Helps you reach senior? | Cost | Watch out for |
|---|---|---|---|
| Go deep on one technology | Yes. You become the person others ask. | You may get stuck as "the database person". | Depth without ownership of results is still mid-level work. |
| Go broad across the stack | Yes. You see how parts connect. | You may feel you know nothing deeply. | Learning many tools is not the same as using judgement. |
| Take ownership of a project | Yes. This is the clearest signal on most ladders. | More stress, more meetings, less coding time. | Do not take on so much that you finish nothing. |
| Aim only for the title | No. A title alone does not change your skills. | You may switch jobs for a title you cannot yet do. | Titles mean different things at different companies. |

**Simple rule:** go deep enough to be trusted, then take ownership of something real. That mix grows judgement fastest.

## At work: how a senior uses this

- When you get a task, ask: **"What problem does this solve, and how will we know it worked?"**
- Before you build, ask: **"Who else does this touch?"** Talk to those teams in week one, not week six.
- Write down your plan in a short doc, even half a page. Writing shows the gaps in your thinking.
- Say what you will **not** do. Clear limits protect the important work.
- Own the result after launch. Look at the numbers, the errors and the support tickets.
- Ask your manager for your company's career ladder. Pick one line from the next level and practise it this month.
- Gergely Orosz's book is a good practical guide to these roles at real companies. [@orosz]

## With AI

AI agents can now write a lot of code quickly. So writing code is no longer the scarce skill. Deciding what to build, and judging if the code is right, matters more.

The evidence is mixed, so read it carefully. In a 2025 randomized study by METR, 16 experienced developers did real tasks in large open-source projects they knew well. With early-2025 AI tools, tasks took about 19% longer. Yet the developers believed AI had made them about 20% faster. [@metr] METR says the result may not apply to all kinds of work, and that these results are now out of date. [@metr] Its 2026 update says developers are probably faster with newer tools, but the data is too weak to say by how much. [@metr-2026]

In 2025, Google's DORA team surveyed nearly 5,000 tech workers. [@dora-research] It found that AI acts as "an amplifier". It makes a team's existing strengths and weaknesses bigger. [@dora-2025] This is survey data, so it shows what people report, not a measured cause.

The lesson for you: speed feels good but can mislead you. Your judgement is what makes the speed safe.

### What to ask the agent

> "Before you write any code, list the questions you would ask about this task. What is unclear? What could go wrong?"

> "Here is my design doc. Act as a careful senior reviewer. List the three biggest risks and anything I left out."

> "Give me two other ways to solve this problem, with the cost of each."

### What to check yourself

- The agent solved the real problem, not just the words in your prompt.
- You can explain every line it wrote. If not, you do not own it yet.
- It did not quietly change other files, add libraries or skip tests.
- You talked to the people the change affects. An agent cannot do that for you.
- You measured if it was faster, instead of trusting how it felt.

## Check yourself

## Go deeper

How to use this curriculum: start with the lessons marked L1 (level 1, the first level). Each lesson starts with a failure, explains the idea, then shows the trade-offs and how seniors use it at work. Do the quiz at the end. Then try one "At work" habit in your real job that week. Reading builds knowledge. Practice builds judgement.

A good first technical lesson: [Idempotency, retries and timeouts](/learn/idempotency-retries-timeouts).
