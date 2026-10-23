---
topic: project-memory
subtitle: Write down what the agent cannot guess, keep it short, and review it like code.
status: published
illustrative_story: true
sources:
  - id: cc-memory
    title: "Anthropic, “How Claude remembers your project”, Claude Code documentation"
    url: https://code.claude.com/docs/en/memory
    note: Every place a CLAUDE.md file can live, the order they load in, imports, path-scoped rules, AGENTS.md support and /memory.
  - id: cc-best
    title: "Anthropic, “Best practices for Claude Code”, Claude Code documentation"
    url: https://code.claude.com/docs/en/best-practices
    note: The "Write an effective CLAUDE.md" section, with a table of what to include and what to leave out.
  - id: agents-md
    title: "AGENTS.md, “A simple, open format for guiding coding agents”"
    url: https://agents.md/
    note: The shared file format many agents read, with examples, nested files and a list of supporting tools.
  - id: copilot
    title: "GitHub Docs, “Adding repository custom instructions for GitHub Copilot”"
    url: https://docs.github.com/en/copilot/how-tos/configure-custom-instructions/add-repository-instructions
    note: File names and folders for Copilot instructions, path-specific files and which instructions win.
  - id: cursor-rules
    title: "Cursor Docs, “Rules”"
    url: https://cursor.com/docs/context/rules
    note: Project, user and team rules in Cursor, the four rule types, and AGENTS.md support.
  - id: owasp-llm01
    title: "OWASP, “LLM01:2025 Prompt Injection”, OWASP Top 10 for LLM Applications"
    url: https://genai.owasp.org/llmrisk/llm01-prompt-injection/
    note: Direct and indirect prompt injection, with ways to reduce the risk.
  - id: pillar
    title: "Pillar Security, “New Vulnerability in GitHub Copilot and Cursor: How Hackers Can Weaponize Code Agents” (March 2025)"
    url: https://www.pillar.security/blog/new-vulnerability-in-github-copilot-and-cursor-how-hackers-can-weaponize-code-agents
    note: The "Rules File Backdoor" research. Hidden characters in a rules file made an agent add a malicious script.
  - id: cc-security
    title: "Anthropic, “Security”, Claude Code documentation"
    url: https://code.claude.com/docs/en/security
    note: Prompt injection protections, the workspace trust dialog and advice for untrusted content.
claims_checked:
  - claim: "Each Claude Code session begins with a fresh context window; CLAUDE.md files are loaded at the start of every session and treated as context, not enforced configuration."
    source: cc-memory
    note: "Opening lines and 'CLAUDE.md vs auto memory' section; both quotes match the docs as of 2026-10-23."
  - claim: "Locations: managed policy, ~/.claude/CLAUDE.md, ./CLAUDE.md or ./.claude/CLAUDE.md, ./CLAUDE.local.md (add to .gitignore). Load order is broad to specific; managed loads first."
    source: cc-memory
    note: "'Choose where to put CLAUDE.md files' table. Lesson now says the managed file loads first; diagram caption updated to match."
  - claim: "Files in the working folder and above load at launch, concatenated from root down so closer files are read last; subfolder files load when Claude reads, writes or edits a file there; conflicting files may be followed arbitrarily."
    source: cc-memory
    note: "'How CLAUDE.md files load' and 'Write effective instructions'. 'reads or edits' widened to 'reads, writes or edits'."
  - claim: "@path imports recurse up to four hops and still load at launch; aim for under 200 lines per file; .claude/rules files with a paths glob load only for matching files; /init, /memory and /context behave as described."
    source: cc-memory
    note: "'Import additional files', 'Write effective instructions', 'Path-specific rules' and '/memory' sections. No '#' shortcut is mentioned in the lesson or the current docs."
  - claim: "Claude Code reads AGENTS.md directly from v2.1.277, by default only when no CLAUDE.md or CLAUDE.local.md is in the working folder or above; @AGENTS.md import works otherwise."
    source: cc-memory
    note: "'AGENTS.md' section and its note: 'Reading AGENTS.md directly requires Claude Code v2.1.277 or later.' Dated 'as of October 2026' in the text."
  - claim: "External imports in project memory files trigger an approval dialog; Claude Code shows a workspace trust dialog in interactive sessions for untrusted folders."
    source: cc-memory
    note: "Import warning box in cc-memory; trust verification bullet in cc-security. 'In an interactive session' added, since -p sessions show no dialog."
  - claim: "Test for each line: 'Would removing this cause Claude to make mistakes?'; include/exclude table; check CLAUDE.md into git; emphasis like IMPORTANT on one line alone."
    source: cc-best
    note: "'Write an effective CLAUDE.md'. The 'every month' pruning cadence was not in the source and was changed to 'regularly'; pull request review is labelled as our advice."
  - claim: "Copilot reads .github/copilot-instructions.md and .github/instructions/NAME.instructions.md with applyTo globs; nearest AGENTS.md wins; personal > repository > organization."
    source: copilot
    note: "All four points confirmed on the GitHub Docs page."
  - claim: "Cursor project rules are .mdc files in .cursor/rules with four types (always, intelligently, specific files, manually); user and team rules exist; AGENTS.md supported; keep rules under 500 lines."
    source: cursor-rules
    note: "Confirmed on the Cursor Rules page."
  - claim: "AGENTS.md is a 'README for agents', goes in the repo root, and in a monorepo the closest file to the edited file wins."
    source: agents-md
    note: "Home page and FAQ."
  - claim: "Indirect prompt injection occurs when an LLM accepts input from external sources, such as websites or files."
    source: owasp-llm01
    note: "Quote matches the LLM01:2025 page."
  - claim: "Pillar Security's 'Rules File Backdoor' (18 March 2025) hid instructions with invisible Unicode; Cursor demo added an attacker script to an HTML page without mentioning it; invisible in GitHub PR review; GitHub added a hidden-Unicode warning on 1 May 2025."
    source: pillar
    note: "Blog post body and disclosure timeline."
reviewed_by: ai-factcheck
reviewed_on: 2026-10-23
review_by: 2027-01-23
quiz:
  - q: Why does a coding agent need a project memory file?
    options:
      - Because the model forgets the programming language between sessions.
      - Because each session starts with a fresh context, without your team's commands and conventions.
      - Because the file makes the model run faster.
    answer: 1
    why: Claude Code's docs say each session begins with a fresh context window. The memory file brings your team's context back every time.
  - q: Which line belongs in a project CLAUDE.md?
    options:
      - The production database password, so the agent can run migrations.
      - '"Run `pnpm test:unit` for fast tests. The full suite needs Docker."'
      - A copy of the whole 40-page API guide.
    answer: 1
    why: Commands the agent cannot guess belong in the file. Secrets never do, and long docs should be linked, not copied.
  - q: You start Claude Code in `repo/services/billing/`. Which CLAUDE.md files load at launch?
    options:
      - Only `repo/services/billing/CLAUDE.md`.
      - The ones in that folder and every folder above it, plus your user file. Files in deeper subfolders load later, when the agent works there.
      - Every CLAUDE.md in the whole repository, at once.
    answer: 1
    why: Claude Code loads files from the working directory and the folders above it at launch. Subfolder files load on demand.
  - q: The agent must never edit files in `migrations/`. Where does that rule belong?
    options:
      - In CLAUDE.md, written in capital letters.
      - In a hook or permission rule that blocks the edit, with a short note in CLAUDE.md that explains why.
      - In the README.
    answer: 1
    why: Memory files are context, not enforced configuration. A hook or a deny rule is code that runs every time.
  - q: You clone an unknown open-source repo and run an agent in it. What is the main risk from its rules files?
    options:
      - They may slow the agent down.
      - They may contain hidden instructions that change what the agent does, which is a form of prompt injection.
      - They may use the wrong Markdown style.
    answer: 1
    why: The agent reads these files as instructions. OWASP calls content from files that changes a model's behaviour indirect prompt injection.
incidents: []
related: [coding-agents, agent-extensions, ai-security-risks, code-review, ai-tool-landscape, prompt-engineering]
---

## What broke

This story is made up, but you may know it.

Sana's team moved their tests from Jest to Vitest last spring. The agent did not know. Every few days it wrote a new test file that imported from `@jest/globals`. The tests failed, Sana typed "we use Vitest, not Jest", and the agent fixed it.

The next morning, a new session started. The agent made the same mistake again. Two weeks later, a teammate found the same error in a pull request. Each person had corrected the agent many times, but the correction lived only in old chat sessions.

Then Sana opened the project's `CLAUDE.md` and added two lines:

```markdown
- Tests use Vitest. Import from `vitest`, never from `@jest/globals`.
- Run one file with `pnpm vitest run path/to/file.test.ts`.
```

She committed it, and the team reviewed it like any other change. The mistake stopped, for everyone, in every session.

## The idea in simple words

A coding agent does not remember yesterday. Claude Code's docs say it plainly: "Each Claude Code session begins with a fresh context window." [@cc-memory] Your corrections, your team's habits and the trap you found last week are gone.

A memory file fixes this. It is a plain Markdown file that the tool loads at the start of every session. [@cc-memory] Think of it as the note you would give a new teammate on day one: how to build, how to test, and what not to touch.

Every major tool has one. The names differ:

- **Claude Code** reads `CLAUDE.md`. [@cc-memory]
- **GitHub Copilot** reads `.github/copilot-instructions.md`. [@copilot]
- **Cursor** reads rules from `.cursor/rules/`. [@cursor-rules]
- **AGENTS.md** is a shared format that many tools read. Its site calls it a "README for agents". [@agents-md]

> [!key] **The key idea.** A memory file is context, not a law. It makes good behaviour more likely, but it does not force it. Keep it short, so the important lines are not lost, and put hard rules in code.

## How it works

Step through one session with Next and Back, or the ← and → keys.

[[diagram:memory-load-order]]

### Where the files live in Claude Code

As of October 2026, Claude Code reads memory files from several places, from broad to specific: [@cc-memory]

| File | Scope | Shared with |
|---|---|---|
| `~/.claude/CLAUDE.md` | You, in all projects | Only you |
| `./CLAUDE.md` or `./.claude/CLAUDE.md` | This project | The team, through git |
| `./CLAUDE.local.md` | You, in this project | Only you (add it to `.gitignore`) |
| `CLAUDE.md` in a subfolder | One part of the project | The team, through git |

An organization can also install a managed file for every user on a machine. It loads first, before your user file. [@cc-memory]

At launch, Claude Code loads the files in your working folder and in every folder above it. Files in subfolders load later, when the agent reads, writes or edits a file in that subfolder. [@cc-memory] The files are joined together, not replaced. Instructions closer to your working folder are read last. [@cc-memory] If two files disagree, the agent may follow either one. [@cc-memory] So do not let them conflict.

### Useful commands and tricks

- `/init` looks at your codebase and writes a first `CLAUDE.md` with build commands and conventions it finds. [@cc-memory]
- `/memory` lists your memory files and opens one in your editor. [@cc-memory]
- `/context` shows which memory files actually loaded. [@cc-memory]
- `@path/to/file` inside `CLAUDE.md` imports another file, up to four levels deep. [@cc-memory] Imports help you organize, but the imported text still loads at launch, so it still costs context. [@cc-memory]
- Files in `.claude/rules/` can have a `paths` field with a glob pattern such as `src/api/**/*.ts`. That rule loads only when the agent works on matching files. [@cc-memory]

### The same idea in other tools

**GitHub Copilot** reads `.github/copilot-instructions.md` for the whole repository. You can add `NAME.instructions.md` files in `.github/instructions/`, each with an `applyTo` glob for certain paths. [@copilot] Copilot also reads `AGENTS.md`, and the nearest one in the folder tree wins. [@copilot] Personal instructions have the highest priority, then repository, then organization. [@copilot]

**Cursor** keeps project rules as `.mdc` files in `.cursor/rules/`. Each rule can apply always, when the agent thinks it is relevant, for matching files, or only when you mention it. [@cursor-rules] Cursor also has user rules and team rules, and it reads `AGENTS.md`. [@cursor-rules]

**AGENTS.md** goes in the repository root. In a monorepo, you can add one per subproject, and the closest file takes precedence. [@agents-md] As of October 2026, Claude Code reads `AGENTS.md` too, from version 2.1.277. By default it does this only when there is no `CLAUDE.md` in your folder or above it. [@cc-memory] On older versions, put the line `@AGENTS.md` in a `CLAUDE.md` to import it. [@cc-memory]

### What goes in, what stays out

Anthropic's guide gives a clear test for each line: "Would removing this cause Claude to make mistakes?" If not, cut it. [@cc-best]

| Put it in | Leave it out |
|---|---|
| Commands the agent cannot guess | Anything it can learn by reading the code |
| Style rules that differ from the defaults | Standard language conventions |
| How to run tests, and which runner | Long API docs (link to them instead) |
| Branch and pull request rules | Facts that change often |
| Project-specific design decisions | Long tutorials |
| Environment quirks and known traps | "Write clean code" |

This table follows the guide. [@cc-best] Two more things never go in: **secrets**, because the file is shared and read by a model, and **stale facts**, because a wrong rule is worse than no rule. (This is our advice.)

Write rules that can be checked. "Run `npm test` before committing" is better than "Test your changes". [@cc-memory] Aim for under 200 lines per file. Longer files cost more context and the agent follows them less well. [@cc-memory] Cursor's docs suggest keeping each rule under 500 lines. [@cursor-rules]

### Context, not a lock

Claude Code treats memory files "as context, not enforced configuration". [@cc-memory] If an action must be blocked every time, use a hook or a permission rule. [@cc-memory] Write the reason in the memory file, and put the lock in code. [Extending agents](/learn/agent-skills-hooks-mcp) covers hooks.

### Memory files are an attack path

The agent reads memory files as trusted instructions. That makes them a target. OWASP describes indirect prompt injection as an attack where a model "accepts input from external sources, such as websites or files". [@owasp-llm01]

In March 2025, Pillar Security showed an attack on rules files. They called it the "Rules File Backdoor". They hid instructions in the file using invisible Unicode characters, such as zero-width joiners. In their demo, a Cursor agent asked for a simple HTML page added a script from an attacker's site, and did not mention it. [@pillar] The researchers say the hidden characters also stayed invisible in GitHub pull request reviews at the time. In May 2025, GitHub added a warning for files with hidden Unicode text. [@pillar]

In an interactive session, Claude Code shows a trust dialog when you start it in a folder you have not trusted before. [@cc-security] It also asks before it loads imports that point outside the project, to protect you from files other people commit. [@cc-memory] These help, but they do not replace reading the files yourself.

## Trade-offs

| Approach | Agent follows team rules? | Cost | Use it when |
|---|---|---|---|
| No memory file | No. You repeat yourself every session. | Free today, slow every day. | One-off scripts. |
| Short root file (under 200 lines) | Yes, most of the time. | A few minutes to write and review. | Almost every project. |
| Root file plus path-scoped rules or nested files | Yes, with less noise per task. | More files to keep in sync. | Large repos and monorepos. |
| One huge file with everything | No. Real rules get lost. | High context cost every session. | Never. Split or cut it. |
| Hooks and permission rules | Yes, every time. | You must write and test code. | Rules that must never be broken. |

**Simple rule:** advice goes in memory, locks go in code.

## At work: how a senior uses this

- Add a line when the agent makes the same mistake twice, or when a code review catches something the agent should have known. [@cc-memory]
- Commit the project file to git and treat it like code. [@cc-best] Review every change to it in a pull request, like a change to a CI script. (This is our advice.)
- Prune it regularly. For each line, ask "Would removing this cause mistakes?" Run `/context` to see which files loaded. [@cc-best]
- If the agent keeps skipping one rule, add "IMPORTANT" to that line alone. If many lines are marked important, none stands out. [@cc-best]
- Keep personal habits in your user file or `CLAUDE.local.md`, not in the team file. [@cc-memory]
- Before you run an agent in a repo you did not write, read its `CLAUDE.md`, `AGENTS.md`, `.cursor/rules/` and `.github/` instruction files first. (This is our advice.)
- If your team uses more than one tool, keep one shared `AGENTS.md`. Import it into `CLAUDE.md` with `@AGENTS.md` if you also need Claude-only rules. [@cc-memory]

## With AI

### What to ask the agent

> "Read this repo and draft a CLAUDE.md under 60 lines. Include only build and test commands, non-default style rules and known traps. For each line, tell me which file proved it."

> "Here is our CLAUDE.md. Mark each line as keep, cut or move. Cut lines you can learn from the code. Move lines that only matter for one folder."

> "You used Jest again in this session. Write one short rule for CLAUDE.md that would have prevented it."

> "Scan every memory and rules file in this repo for hidden Unicode characters, instructions to fetch URLs, or instructions to hide changes from me. Report them, do not change anything."

### What to check yourself

- Every command in the file runs today. Try each one.
- No secret, token or internal URL that should stay private is in a shared file.
- No line conflicts with another file in a parent or child folder.
- After a change, the agent's behaviour actually changed. Test it in a fresh session.
- You read the rules files of any new repo before letting an agent act there.

## Check yourself

## Go deeper

Before this: [Working with coding agents](/learn/working-with-coding-agents). Next: [Extending agents: skills, hooks and MCP](/learn/agent-skills-hooks-mcp) and [AI code security risks](/learn/ai-code-security-risks).
