---
topic: coding-agents
subtitle: Explore, plan, build and verify, and treat the agent's context like a budget you can run out of.
status: published
reviewed_by: ai-factcheck
reviewed_on: 2026-10-21
review_by: 2027-01-19
illustrative_story: true
sources:
  - id: cc-best
    title: "Anthropic, “Best practices for Claude Code”, Claude Code documentation (this page replaced the engineering blog post “Claude Code best practices”)"
    url: https://code.claude.com/docs/en/best-practices
    note: The main guide. Context limits, verification, explore-plan-code, CLAUDE.md, subagents, hooks and common failure patterns.
  - id: cc-how
    title: "Anthropic, “How Claude Code works”, Claude Code documentation"
    url: https://code.claude.com/docs/en/how-claude-code-works
    note: The agent loop, the tools, what fills the context window, and what checkpoints can and cannot undo.
  - id: cc-features
    title: "Anthropic, “Extend Claude Code”, Claude Code documentation"
    url: https://code.claude.com/docs/en/features-overview
    note: When to use CLAUDE.md, skills, subagents, hooks, MCP and plugins, and what each one costs in context.
  - id: cc-memory
    title: "Anthropic, “How Claude remembers your project”, Claude Code documentation"
    url: https://code.claude.com/docs/en/memory
    note: Where CLAUDE.md files live, how they load, and why they are context, not enforced rules.
  - id: context-eng
    title: "Anthropic, “Effective context engineering for AI agents” (September 2025)"
    url: https://www.anthropic.com/engineering/effective-context-engineering-for-ai-agents
    note: Why context is a limited resource for any model, and the idea of "context rot".
  - id: mcp
    title: "Model Context Protocol, “What is the Model Context Protocol (MCP)?”"
    url: https://modelcontextprotocol.io/
    note: The open standard that connects AI tools to outside systems. Read the security page too.
  - id: metr
    title: "METR, “Measuring the Impact of Early-2025 AI on Experienced Open-Source Developer Productivity” (July 2025)"
    url: https://metr.org/blog/2025-07-10-early-2025-ai-experienced-os-dev-study/
    note: A randomized controlled trial with 16 experienced developers. METR now marks these results as out of date.
  - id: metr-2026
    title: "METR, follow-up results on AI and developer speed (February 2026)"
    url: https://metr.org/blog/2026-02-24-uplift-update/
    note: Newer data. The results are not conclusive, and the page explains why.
  - id: dora-2025
    title: "DORA (Google Cloud), State of AI-assisted Software Development 2025"
    url: https://dora.dev/research/2025/dora-report/
    note: A large survey on how teams use AI. Its main finding is that AI amplifies what a team already is.
  - id: anthropic-skills
    title: "Anthropic, “How AI assistance impacts the formation of coding skills” (January 2026)"
    url: https://www.anthropic.com/research/AI-assistance-coding-skills
    note: A randomized study on learning a new library with and without AI help. Read the limits section.
claims_checked:
  - claim: "The context window holds every message, file read and command output; as it fills, Claude may forget earlier instructions and make more mistakes. Use /clear between tasks, and after two failed corrections start fresh."
    source: cc-best
    note: "Best practices page, opening section, 'Course-correct early' and 'Avoid common failure patterns': wording matches. The old engineering blog URL now redirects (308) to this page."
  - claim: "Four phases: explore (plan mode via Shift+Tab), plan (Ctrl+G to edit), implement, commit; skip the plan if the diff fits in one sentence."
    source: cc-best
    note: "'Explore first, then plan, then code' section. The guide's fourth phase is Commit, so the H3 heading was changed from 'verify' to 'commit'. Ctrl+G opens the plan in a text editor."
  - claim: "Compaction clears older tool outputs first, then summarizes; early instructions may be lost. Checkpoints undo file edits, not databases, APIs or deployments."
    source: cc-how
    note: "'When context fills up' and 'Undo changes with checkpoints' sections. /context and '/compact focus on the API changes' also confirmed there."
  - claim: "Checkpoints only track edits made with the agent's file-edit tools; shell (Bash) commands and external processes are not captured, so use git as well."
    source: cc-best
    note: "QC1-03. 'Rewind with checkpoints' warning: 'Checkpoints only track changes made through Claude's file editing tools. Changes made through Bash commands or external processes are not captured. This isn't a replacement for git.'"
  - claim: "CLAUDE.md is context, not enforced configuration; keep it under 200 lines; a rule that must hold every time belongs in a hook (e.g. a PreToolUse hook blocking .env edits)."
    source: cc-features
    note: "Features overview: 'Keep CLAUDE.md under 200 lines', 'If a rule must hold every time, make it a hook', '.env' example. Memory page says 'context, not enforced configuration'. Skills load descriptions first, full content on use; plugins bundle skills, hooks, subagents and MCP servers."
  - claim: "METR 2025 RCT: 16 experienced open-source developers took 19% longer with early-2025 AI but believed they were 20% faster; METR marks the results out of date; its Feb 2026 update says developers are likely faster now but gives only very weak evidence on size."
    source: metr
    note: "Both METR pages read; wording matches the fact-checked what-senior-means lesson. 2026 page: 'only very weak evidence for the size of this increase'."
  - claim: "DORA 2025: AI's primary role is as an amplifier of an organization's existing strengths and weaknesses."
    source: dora-2025
    note: "Exact quote on the report page. Survey data, as the lesson says."
  - claim: "Anthropic study (Jan 2026): 52 mostly junior engineers learning Trio; AI group 50% vs 67% on the quiz, biggest gap on debugging, about two minutes faster (not significant); conceptual/explanation patterns 65%+, delegation patterns under 40%."
    source: anthropic-skills
    note: "All numbers and limits (small sample, quiz right after the task, no causal link for patterns) match the research page."
  - claim: "MCP is an open standard that connects AI applications to external systems such as files, databases and tools."
    source: mcp
    note: "modelcontextprotocol.io home page. 'Issue trackers and design tools' were not on this page, so the lesson and glossary now say 'files, databases and other tools'."
  - claim: "Claude Code's recommended workflow has four phases: explore, plan, implement, commit."
    source: cc-best
    note: "QC1-20: Best practices page: 'The recommended workflow has four phases': Explore, Plan, Implement, Commit. Trade-offs table row fixed from 'verify' to 'commit'."
quiz:
  - q: Your session has run for two hours across three different tasks. The agent starts to ignore rules you gave it at the start. What is the most likely cause?
    options:
      - The model is tired and needs a break.
      - The context window is full of old, unrelated work, so early instructions get lost.
      - The agent has a bug and must be reinstalled.
    answer: 1
    why: Everything in the session uses context. As it fills, the agent can forget early instructions and make more mistakes. Use /clear between tasks.
  - q: You have corrected the agent twice on the same problem, and it is still wrong. What should you do?
    options:
      - Correct it a third and fourth time, more firmly.
      - Run /clear and start again with a better first prompt that includes what you learned.
      - Switch off the tests so it can finish.
    answer: 1
    why: Failed attempts fill the context and mislead the agent. A clean session with a better prompt usually wins.
  - q: The agent must never edit the .env file, with no exceptions. Where do you put this rule?
    options:
      - In CLAUDE.md, in capital letters.
      - In a hook that blocks the edit.
      - In the first prompt of every session.
    answer: 1
    why: CLAUDE.md and prompts are advice the model reads. A hook is code that runs every time, so it enforces the rule.
  - q: You want to rename one variable in one file. Should you use plan mode first?
    options:
      - Yes, always plan first.
      - No. If you can describe the change in one sentence, skip the plan.
      - No. Never use plan mode.
    answer: 1
    why: Planning helps when the approach is unclear or many files change. For a tiny, clear change it only adds time.
  - q: In Anthropic's 2026 study, people learned a new library with or without AI help. Which group scored better on the quiz afterwards?
    options:
      - The AI group, because they finished faster.
      - The group without AI. Among AI users, those who asked questions to understand did better than those who handed over the work.
      - Both groups scored the same.
    answer: 1
    why: The AI group scored 50% against 67%. AI users who asked conceptual questions scored much higher than those who only delegated.
incidents: []
related: [project-memory, agent-extensions, skill-atrophy]
---

## What broke

This story is made up, but it may have happened to you. Arjun has a flaky checkout test. His agent session is already long: a CSS fix, a question about logs, a big stack trace.

He types: "fix the flaky checkout test". The agent reads forty files. It finds a timing problem and adds a two-second sleep to the test. On the way, it "cleans up" a helper in the payment code. It reports: "Fixed. The test should pass now."

Arjun did not ask it to run the test 50 times. He did not read the diff closely. The next day, a reviewer finds the changed payment helper. The test is still flaky. It just fails less often.

He tries again: `/clear`, plan mode, and the exact command that shows the failure. The agent finds a missing `await`, writes a plan, fixes it, and runs the test 50 times. All pass. The diff is four lines.

The agent was the same both times. Only the way he worked with it changed.

## The idea in simple words

A coding agent is not a smarter autocomplete. It works in a loop. Claude Code describes three phases: gather context, take action, and verify results. [@cc-how] It searches and reads files, edits code, runs commands, reads the output, and decides the next step. It repeats this until it thinks the job is done.

Two facts shape everything about working with it.

**First, context is a budget.** The context window holds your whole conversation: every message, every file the agent reads, and every command output. [@cc-best] It fills up fast. As it fills, the model can forget earlier instructions and make more mistakes. [@cc-best] This is not only a Claude problem. An Anthropic engineering post calls context "a finite resource with diminishing marginal returns". [@context-eng]

**Second, the agent stops when the work *looks* done.** Without a check it can run, "looks done" is its only signal. Then you become the checker for every mistake. [@cc-best]

> [!key] **The key idea.** Spend context on what matters, and give the agent a way to prove its work. Most good habits with agents come from these two rules.

## How it works

Step through one task and watch the context fill up. Use Next and Back, or the ← and → keys.

[[diagram:agent-loop-context]]

### Explore, plan, implement, commit

The Claude Code guide recommends four phases. [@cc-best]

1. **Explore.** Press `Shift+Tab` until plan mode is on. In plan mode the agent reads files and answers questions, but does not change them.
2. **Plan.** Ask for a plan: which files change, and why. Press `Ctrl+G` to open the plan in your editor and change it. This is the cheapest moment to catch a wrong idea.
3. **Implement.** Approve the plan or leave plan mode, and let it code against the plan. Ask it to write tests and run them.
4. **Commit.** Ask for a clear commit message and a pull request.

Plan mode has a cost, so skip it for small work. The guide's test: if you could describe the diff in one sentence, skip the plan. [@cc-best]

### Give it a check it can run

A check is anything with a clear pass or fail: a test suite, a build, a linter, a script, a screenshot to compare. [@cc-best] Put the check in the prompt. Then ask for evidence, not claims: the command it ran and the output. [@cc-best]

### Manage the budget

- Run `/clear` between unrelated tasks. [@cc-best]
- Run `/context` to see what uses space. [@cc-how]
- When the context gets full, Claude Code compacts it: it clears old tool output and then summarizes. Early instructions may be lost. [@cc-how] You can run `/compact` with a focus, like `/compact focus on the API changes`. [@cc-how]
- Correct early. `Esc` stops the agent. `Esc` twice, or `/rewind`, goes back to an earlier point. [@cc-best]
- If you have corrected it twice on the same issue, run `/clear` and write a better first prompt. [@cc-best]

### Project memory: CLAUDE.md

Each session starts with a fresh context. [@cc-how] CLAUDE.md is a file the agent reads at the start of every session. [@cc-best] Put in it what the agent cannot guess from the code: build and test commands, unusual style rules, branch rules and known traps. Leave out what it can read from the code. `/init` creates a first version for you. [@cc-best]

Keep it short. The docs suggest under 200 lines. [@cc-features] For each line, ask: "Would removing this cause mistakes?" If not, cut it. A long file makes the agent ignore your real rules. [@cc-best] Remember that CLAUDE.md is context, not enforced configuration. [@cc-memory] Check it into git, so your team improves it together like code. [@cc-best]

### Four extensions, one paragraph each

**Subagents** run in their own context window with their own allowed tools, and send back a short summary. [@cc-best] Use them for research that reads many files, or for a review in a fresh context. The reviewer sees only the diff and your criteria, not the reasoning that produced it. [@cc-best]

**Skills** are Markdown files (`SKILL.md`) with knowledge or a repeatable workflow, each in its own folder under `.claude/skills/`. The agent sees only their descriptions at the start. The full text loads when a skill is used, or when you run it with `/skill-name`. [@cc-features]

**Hooks** are scripts that run at fixed points, for example after every file edit. CLAUDE.md instructions are advice. Hooks are deterministic and always run. [@cc-best] Use a hook to run the linter after each edit, or to block writes to a folder. If a rule must hold every time, make it a hook. [@cc-features]

**MCP** (Model Context Protocol) is an open standard that connects AI tools to outside systems, such as files, databases and other tools. [@mcp] An MCP server can read your data and take actions for you. [@mcp] So treat it like any other dependency: install only servers you trust, and give them the least access you can.

**Plugins** package skills, hooks, subagents and MCP servers so a team can share one setup. [@cc-features]

### When not to use an agent

- **The change is faster to type than to explain.**
- **You are learning something new.** Doing it yourself first builds the skill. See "With AI" below.
- **The action touches live systems.** Checkpoints undo edits made with the agent's file-edit tools. They do not undo shell commands, or changes to databases, APIs or deployments. Use git as well. [@cc-best] Run those live steps yourself, on purpose.
- **You cannot check the result.** The guide says it plainly: if you can't verify it, don't ship it. [@cc-best]

### What the evidence says

The evidence is mixed, so read it carefully. In a 2025 randomized study by METR, 16 experienced developers did real tasks in large open-source projects they knew well. With early-2025 AI tools, tasks took about 19% longer. Yet the developers believed AI had made them about 20% faster. [@metr] METR now says these results are out of date. [@metr] Its 2026 update says developers are probably faster with newer tools, but the data is too weak to say by how much. [@metr-2026]

Google's DORA 2025 report found that AI acts as "an amplifier". It makes a team's existing strengths and weaknesses bigger. [@dora-2025] This is survey data, so it shows what people report, not a measured cause.

The lesson: do not trust how fast it feels. Measure it.

## Trade-offs

| Way of working | Quality | Cost | Use it when |
|---|---|---|---|
| Write it yourself | Yes. You understand every line. | Slower for large or boring changes. | Small fixes, new topics you are learning, risky code. |
| One vague prompt, no check | No. It may solve the wrong problem. | Feels fast. Review and rework come later. | Quick experiments you will throw away. |
| Explore → plan → implement → commit | Yes, if the check is good. | A few minutes of planning and setup. | Most real tasks across several files. |
| Add subagents for research and review | Yes. Your main context stays clean. | More tokens in total, and more output to read. | Large codebases, or work you want checked with fresh eyes. |

**Simple rule:** the less you watch the agent, the stronger its checks must be.

## At work: how a senior uses this

- Before you prompt, ask yourself: **"How will the agent know it is done?"** Write that check into the prompt.
- Start every new task with `/clear`. One session, one task.
- Use plan mode for anything that touches more than one or two files. Read the plan as you would read a design doc.
- Keep CLAUDE.md short and in git. When the agent makes the same mistake twice, add one line. When a line stops mattering, delete it. [@cc-features]
- Turn "must always" rules into hooks, and leave advice in CLAUDE.md. [@cc-features]
- Review agent code like code from a new teammate: read every line, run the tests yourself, and check for changes outside the task.
- Ask for a fresh-context review before you open the pull request. The bundled `/code-review` skill reviews your diff in a fresh subagent. [@cc-best]

## With AI

This whole lesson is about AI, so this section covers one risk: using an agent in a way that stops you from learning.

Anthropic ran a randomized study with 52 mostly junior engineers. They learned a new Python library, with or without AI help. On a quiz afterwards, the AI group scored 50% and the group without AI scored 67%. The biggest gap was on debugging questions. The AI group was only about two minutes faster, and that difference was not statistically significant. [@anthropic-skills]

How people used AI mattered. Those who asked conceptual questions, or asked for explanations of the code, scored 65% or more. Those who handed over the work, or let AI do the debugging, scored under 40%. [@anthropic-skills] The authors say the sample was small, the quiz came right after the task, and their pattern analysis does not prove cause. [@anthropic-skills]

So when the goal is to learn, use the agent as a teacher, not as a typist.

### What to ask the agent

> "Don't write the code. Explain how this library handles errors, then give me a small exercise to try myself."

> "Here is my solution. Before you show yours, list the bugs and edge cases I missed, and explain why each one matters."

> "Explain this diff line by line. Then ask me three questions to check that I understood it."

### What to check yourself

- You can explain every line the agent wrote, without asking it again.
- You could debug this code alone at 2 a.m. during an incident.
- The tests check real behaviour, and the agent did not weaken a test to make it pass.
- The diff contains only what the task needed. No quiet changes elsewhere.

## Check yourself

## Go deeper

Next on this line: [Project memory: CLAUDE.md and rules files](/learn/project-memory-claude-md) and [Extending agents: skills, hooks and MCP](/learn/agent-skills-hooks-mcp).

To keep your skills sharp: [Skill atrophy and deliberate practice](/learn/skill-atrophy-deliberate-practice).
