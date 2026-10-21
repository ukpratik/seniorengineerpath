---
topic: architecture-tradeoffs
subtitle: There is no best architecture, only choices with different costs. Learn how seniors compare options, match the effort to the risk, and write down what they gave up.
status: published
reviewed_by: ai-factcheck
reviewed_on: 2026-10-21
illustrative_story: true
sources:
  - id: amazon-2015
    title: "Jeff Bezos, 2015 Letter to Shareholders, Amazon.com (filed with the US SEC, April 2016)"
    url: https://www.sec.gov/Archives/edgar/data/1018724/000119312516530910/d168744dex991.htm
    note: The original "one-way door" and "two-way door" passage, and why one heavy process for every decision makes a company slow.
  - id: amazon-2016
    title: "Jeff Bezos, 2016 Letter to Shareholders, Amazon"
    url: https://www.aboutamazon.com/news/company-news/2016-letter-to-shareholders
    note: The section on high-velocity decisions. Deciding with about 70% of the information, and "disagree and commit".
  - id: mckinley
    title: "Dan McKinley, “Choose Boring Technology” (2015)"
    url: https://mcfunley.com/choose-boring-technology
    note: Innovation tokens, why known failure modes matter, and the hidden cost of adding one more tool.
  - id: fowler-architect
    title: "Martin Fowler, “Who Needs an Architect?”, IEEE Software, July/August 2003"
    url: https://martinfowler.com/ieeeSoftware/whoNeedsArchitect.pdf
    note: Architecture as the things that are hard to change, and why a good architect removes irreversibility.
  - id: aws-wa
    title: "AWS Well-Architected Framework, “Definitions”"
    url: https://docs.aws.amazon.com/wellarchitected/latest/framework/definitions.html
    note: The six pillars, and how you trade between them based on business context.
  - id: fowler-utility
    title: "Martin Fowler, “Utility Vs Strategic Dichotomy” (2010)"
    url: https://martinfowler.com/bliki/UtilityVsStrategicDichotomy.html
    note: Why software that does not set you apart should usually be bought, and software that does should be built well.
  - id: nygard
    title: "Michael Nygard, “Documenting Architecture Decisions”, Cognitect (2011)"
    url: https://cognitect.com/blog/2011/11/15/documenting-architecture-decisions
    note: The original ADR post. Five short sections, and why reversed decisions are kept and marked superseded.
claims_checked:
  - claim: "One-way doors are hard to undo and need a slow, careful process. Most decisions are two-way doors and should be made quickly by a person or small group with good judgement."
    source: amazon-2015
    note: "Matches the letter: Type 1 \"one-way doors\" made \"methodically, carefully, slowly\"; Type 2 \"two-way doors\" made \"quickly by high judgment individuals or small groups\". The warning about slowness and failure to experiment is also there."
  - claim: "Decide with about 70% of the information; waiting for 90% is probably slow. \"Disagree and commit\", with the \"gamble with me\" quote, and bosses should do it too."
    source: amazon-2016
    note: "Wording checked against the letter. The quote is exact. Writer's \"being wrong cheaper\" paraphrase was tightened to the letter's sentence about recognizing and correcting bad decisions."
  - claim: "Each company gets about three innovation tokens. Boring tools like MySQL, Postgres, Python and cron have well-understood failure modes. Long-term running costs almost always exceed the cost of building."
    source: mckinley
    note: "All points in the essay. \"usually\" changed to \"almost always\" to match \"basically always\". Spending example changed to the essay's own NodeJS example."
  - claim: "Architecture can be seen as things people perceive as hard to change; an architect should find ways to remove irreversibility. Schema example from one of Fowler's projects."
    source: fowler-architect
    note: "Read in the PDF. The schema system was built by DBA Pramod Sadalage, so \"his team\" was corrected; \"no longer architectural\" is Fowler's phrase."
  - claim: "You trade between six pillars based on business context; security and operational excellence are generally not traded off."
    source: aws-wa
    note: "Matches the Definitions page. Example was \"development environments\", so \"test environment\" was changed."
  - claim: "Utility functions like payroll should usually be bought as a package; strategic functions should not use the same software as competitors; the split may be like 95/5."
    source: fowler-utility
    note: Matches the bliki entry. "Perhaps 5 in 100" reworded to Fowler's 95/5 rule of thumb.
  - claim: "An ADR has title, context, decision, status and consequences; list all consequences; keep reversed ADRs marked superseded."
    source: nygard
    note: All quoted in Nygard's post, including the quiz 5 "why".
quiz:
  - q: Your team wants to switch the logging library in one service. It takes a day and is easy to switch back. How should you decide?
    options:
      - Write a 10-page design doc and get approval from every team lead.
      - Treat it as a two-way door. Let a small group decide quickly and move on.
      - Wait until you have all the information you could ever want.
    answer: 1
    why: Bezos says reversible, two-way door decisions should be made quickly by people with good judgement or small groups. A heavy process here only makes you slow.
  - q: What does Dan McKinley mean by "innovation tokens"?
    options:
      - A budget for cloud spending.
      - A small, fixed number of new or unusual technologies a company can afford to adopt.
      - Points engineers earn for shipping features.
    answer: 1
    why: McKinley says each company gets about three. Every new technology spends one, because its failure modes are not yet well known.
  - q: In a scoring table, option C wins on performance but scores low on team skill, operations and ease of undo. What is the senior move?
    options:
      - Pick C, because performance always matters most.
      - Look at the total and the reasons. Choose what the team can run, and name a signal that would make you revisit.
      - Add more criteria until C wins.
    answer: 1
    why: The table is a tool for talking. The point is to see what each option costs you to live with, and when to look again.
  - q: According to Martin Fowler, when should you usually buy a package instead of building your own software?
    options:
      - When the function does not set your business apart from competitors.
      - Never. Building is always cheaper in the long run.
      - When the function is your main advantage over competitors.
    answer: 0
    why: Fowler calls such functions "utility", like payroll. You want them to work at low cost, so a package is the obvious choice.
  - q: Six months after an ADR, the facts that led to the decision have changed. What should you do with the old ADR?
    options:
      - Delete it, so nobody follows the old decision.
      - Edit it to say what you now believe.
      - Write a new ADR, and mark the old one as superseded with a link to the new one.
    answer: 2
    why: Nygard keeps old ADRs. It still matters that this was the decision, even though it is no longer the decision.
incidents: []
cases: [zerodha-scaling-common-sense, shopify-modular-monolith]
related: [design-docs, adrs, build-vs-buy, requirements-nfrs, monolith-microservices, tech-debt]
---

## What broke

This story is made up, but many teams will know it.

A team of four builds a new booking service. In the kickoff, someone says, "Let's do it properly." They choose six microservices, a self-hosted Kafka cluster and a new database nobody on the team has used. Each tool is good. Each one wins a blog post comparison.

The product launches with 300 bookings a day. One Postgres database could hold that many easily. But now the team runs nine separate systems. On-call pages come at night for problems nobody understands. A small feature touches four services and takes three weeks.

A year later, the team spends a full quarter moving back to one service and one database. Nobody wrote down why they chose the first design, so nobody could say when it stopped making sense.

The team did not pick bad tools. They picked tools for a problem they did not have, and they never wrote down what those tools would cost them.

## The idea in simple words

There is no best architecture. Every choice makes some things easier and other things harder. A senior's job is to see both sides, choose on purpose, and say out loud what you give up.

The AWS Well-Architected Framework names six areas: operational excellence, security, reliability, performance efficiency, cost optimization and sustainability. It says you trade between them based on your business context. For example, you might accept lower reliability in a development environment to save cost. For a critical system, you might pay more for reliability. AWS adds that security and operational excellence are usually not traded away. [@aws-wa]

> [!key] **The key idea.** A good decision is not one with no downsides. It is one where you chose the downsides on purpose, matched the effort to the risk, and wrote it down so others can follow, or change it later.

**Not every decision deserves the same effort.** Jeff Bezos split decisions into two kinds. Some are one-way doors: hard or impossible to undo, so you decide slowly and carefully. Most are two-way doors: if you are wrong, you can go back. Those should be made quickly, by a person or a small group with good judgement. [@amazon-2015] He warns that big organisations tend to use the slow process for everything. The result is slowness, too much fear of risk and too little experimenting. [@amazon-2015]

## How it works

### Step 1: Requirements and constraints first

Start with the problem, not the tool. Write down the numbers that matter: load, data size, latency target and budget. Then write the constraints. Who will run this at 3 a.m.? What does the team already know? What must it connect to? Do not skip this step and compare tools in general.

### Step 2: Ask how hard it is to undo

Martin Fowler offers one way to think of architecture: the things people see as hard to change. [@fowler-architect] He writes that one of an architect's most important tasks is to find ways to remove irreversibility from a design. [@fowler-architect] His example: on one of his projects, a database administrator built a way to change the schema and move the data easily. After that, Fowler says, the schema was no longer architectural. [@fowler-architect]

So for each option, ask: if this is wrong in a year, what does leaving cost? A library behind your own interface is cheap to replace. A public API, a data format stored in millions of rows, or a database choice usually is not.

Bezos also gives a rule for speed: most decisions should be made with about 70% of the information you wish you had. If you wait for 90%, in most cases you are probably being slow. Either way, you need to be good at quickly noticing and fixing bad decisions. [@amazon-2016]

### Step 3: Count the cost of running it

Dan McKinley's essay "Choose Boring Technology" gives a simple model. Each company gets about three "innovation tokens". Choosing a new or unusual technology spends one. His example is writing your website in NodeJS. [@mckinley] Boring does not mean bad. His examples of boring tools include MySQL, Postgres, Python and cron. Their strength is that their failure modes are well understood. [@mckinley]

He also points to the hidden cost of each new tool: operations. You have to monitor it, test it and learn it. [@mckinley] The long-term cost of keeping a system working almost always far exceeds the trouble of building it. [@mckinley] His practical test: first ask how you would solve the problem with no new technology at all. [@mckinley]

### Step 4: Compare options in a small table

Step through how a team turns options into a written decision. Use Next and Back, or the ← and → keys.

[[diagram:tradeoff-decision]]

A decision table is useful because it forces you to agree on what matters before you argue about tools. But do not trust the total blindly. If one option scores 0 on something you must have, such as security, it is out, whatever its total. Treat the scores as a way to start a conversation, not to end it.

### Step 5: Build or buy

Fowler splits software into two kinds. "Utility" software, like payroll, must work reliably, but it does not set you apart. "Strategic" software is part of why customers choose you. [@fowler-utility] For utility functions, the obvious choice is to buy a package. For strategic functions, you do not want the same software as your competitors. [@fowler-utility] He adds that only a few projects are truly strategic. He suggests the split may be more like 95 to 5. [@fowler-utility] The lesson [build vs buy vs open source](/learn/build-vs-buy-vs-open-source) goes deeper.

### Step 6: Decide, write it down, and plan to look again

When people still disagree, Bezos suggests the phrase "disagree and commit". In his words, you ask: "I know we disagree on this but will you gamble with me on it?" He says bosses should do this too. [@amazon-2016]

Then write the decision down. For a big change, write a [design doc](/learn/design-docs-and-rfcs). For one decision, write an ADR. Michael Nygard's format has a title, context, decision, status and consequences. Consequences should list all the effects, not only the good ones. [@nygard]

Good ADRs make it clear when to revisit. Nygard says the time to change an old decision becomes clear from changes in the project's context. [@nygard] Help that happen. Write a revisit trigger, such as "if load passes 1,000 jobs a second". When you reverse a decision, keep the old ADR and mark it as superseded, which means replaced by a newer decision. [@nygard]

## Trade-offs

| Approach | Good for | Risk | Use it when |
|---|---|---|---|
| Decide fast, small group | Speed and learning. | You may need to undo it. | Two-way doors: easy and cheap to reverse. |
| Slow, careful review | Fewer costly mistakes. | Slowness if used for everything. | One-way doors: data formats, public APIs, core databases. |
| Boring, well-known tech | Known failure modes. Easy to hire for and run. | May need more effort for a special need. | Most of your system, most of the time. |
| New, exciting tech | A real edge for one hard problem. | Unknown failures. Spends an innovation token. | The problem is core to your business and old tools truly cannot solve it. |
| Buy a product | Fast start. Someone else runs it. | Licence cost, lock-in, less control. | Utility functions that do not set you apart. |
| Build your own | Full control over a key feature. | You own every bug and every on-call page. | Strategic functions that customers choose you for. |

## At work: how a senior uses this

- Before you compare tools, write the problem, the numbers and the constraints in five lines.
- Always show at least two real options. One of them should be "use what we already have".
- Ask: **"Is this a one-way or a two-way door?"** Match the review effort to the answer.
- Name the cost of running each option: who is on call, what to monitor, what the team must learn.
- Say what you give up, in writing. "We accept X to get Y."
- Write a revisit trigger with a number or a date, not "if things change".
- When you lose an argument, disagree and commit. Then help the decision work.
- When you join a team, read the ADRs before you suggest a rewrite. The reason may still be valid.

## With AI

### What to ask the agent

> "Here are our requirements, team size and current stack. Give me three real options for this job queue, including 'use what we already have'. For each, list the cost of running it, the hardest thing to undo, and the main failure mode."

> "Act as a skeptical staff engineer. Argue against my preferred option. What will hurt in a year?"

> "Turn this decision into an ADR with title, context, decision, status and consequences. Include the negative consequences and a revisit trigger."

> "Is this decision a one-way door or a two-way door? List what would make it hard to reverse."

### What to check yourself

- The requirements and numbers are yours. The agent does not know your real load or budget.
- The options are real. Agents tend to suggest popular tools, even when you do not need them.
- Every claim about a tool's limits or price comes from its official docs, not the agent's memory.
- The team's skills and on-call load are in the decision. The agent cannot see who will be paged at night.
- The decision is yours. You can defend it in review without the agent.

## Check yourself

## Go deeper

Next, read [design docs and RFCs](/learn/design-docs-and-rfcs), [architecture decision records](/learn/architecture-decision-records) and [build vs buy vs open source](/learn/build-vs-buy-vs-open-source).
