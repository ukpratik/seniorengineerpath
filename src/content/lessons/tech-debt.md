---
topic: tech-debt
subtitle: Find the debt that really slows your team, measure its interest, and pay it down in small steps the business agrees to.
status: published
illustrative_story: true
sources:
  - id: cunningham-1992
    title: "Ward Cunningham, “The WyCash Portfolio Management System”, OOPSLA '92 Experience Report (1992)"
    url: http://c2.com/doc/oopsla92.html
    note: The short report where the debt metaphor first appears, with "interest" on not-quite-right code.
  - id: fowler-debt
    title: "Martin Fowler, “Technical Debt” (bliki, 2019)"
    url: https://martinfowler.com/bliki/TechnicalDebt.html
    note: Cruft, principal and interest, and why stable messy code can be left alone while busy code cannot.
  - id: fowler-quadrant
    title: "Martin Fowler, “Technical Debt Quadrant” (bliki, 2009)"
    url: https://martinfowler.com/bliki/TechnicalDebtQuadrant.html
    note: Reckless or prudent, deliberate or inadvertent, with one quote for each of the four kinds of debt.
  - id: codescene-hotspots
    title: "CodeScene documentation, “Technical Debt” (hotspots guide)"
    url: https://codescene.io/docs/guides/technical/hotspots.html
    note: How a hotspot analysis combines change frequency with code health to rank debt, with a worked example.
  - id: larson-quality
    title: "Will Larson, “Manage technical quality”, StaffEng"
    url: https://staffeng.com/guides/manage-technical-quality/
    note: Seven ways to improve quality, from fixing hot spots up to a quality program, and why to start light.
  - id: cod
    title: "Black Swan Farming, “Cost of Delay”"
    url: https://blackswanfarming.com/cost-of-delay/
    note: Cost of delay as value plus urgency, and CD3 for ranking work.
  - id: boy-scout
    title: "Robert C. Martin, “The Boy Scout Rule”, in 97 Things Every Programmer Should Know (O'Reilly, 2010)"
    url: https://github.com/97-things/97-things-every-programmer-should-know/blob/master/en/thing_08/README.md
    note: Check a module in a little cleaner than you found it, with small examples.
  - id: fowler-opportunistic
    title: "Martin Fowler, “Opportunistic Refactoring” (bliki, 2011)"
    url: https://martinfowler.com/bliki/OpportunisticRefactoring.html
    note: Fixing unclear code as you meet it, and why good teams rarely need to plan refactoring.
  - id: spolsky-rewrite
    title: "Joel Spolsky, “Things You Should Never Do, Part I”, Joel on Software (2000)"
    url: https://www.joelonsoftware.com/2000/04/06/things-you-should-never-do-part-i/
    note: The classic argument against rewriting working code from scratch, with Netscape as the example.
claims_checked:
  - claim: Cunningham wrote "Shipping first time code is like going into debt" and that a little debt helps only "so long as it is paid back promptly with a rewrite".
    source: cunningham-1992
  - claim: Fowler's quadrant quotes are "We don't have time for design", "We must ship now and deal with consequences", "What's Layering?" and "Now we know how we should have done it".
    source: fowler-quadrant
  - claim: Fowler says crufty but stable code can be left alone, while areas of high activity need zero tolerance for cruft.
    source: fowler-debt
  - claim: CodeScene's docs show hotspots at 1.2% of the code holding 45% of the bugs found and fixed, and say low code health in hotspots should be prioritized.
    source: codescene-hotspots
  - claim: Spolsky called rewriting from scratch "the single worst strategic mistake that any software company can make", with Netscape as the example.
    source: spolsky-rewrite
  - claim: The Boy Scout Rule text is by Robert C. Martin (Uncle Bob) and says "Always check a module in cleaner than when you checked it out."
    source: boy-scout
reviewed_by: ai-factcheck
reviewed_on: 2026-10-22
quiz:
  - q: Your team shipped a quick hack to meet a launch date, and wrote a ticket to clean it up next month. Which kind of debt is this in Fowler's quadrant?
    options:
      - Reckless and inadvertent
      - Prudent and deliberate
      - Prudent and inadvertent
    answer: 1
    why: The team knew it was taking on debt and chose it for a real benefit. Fowler's example quote is "We must ship now and deal with consequences".
  - q: Two modules have messy code. Module A changed twice last year. Module B changed 60 times and had 4 incidents. Which should you clean up first?
    options:
      - Module A, because it is older.
      - Module B, because you pay its interest on every change.
      - Both at the same time, in one big project.
    answer: 1
    why: You only pay interest when you work in the code. Messy code that rarely changes can usually wait.
  - q: What is the main risk of rewriting a large working system from scratch?
    options:
      - You lose years of bug fixes hidden in the old code, and ship nothing new for a long time.
      - The new code will use too much memory.
      - Rewrites are always slower to compile.
    answer: 0
    why: Spolsky's point is that old code has been used and tested, and its odd parts are often bug fixes.
  - q: Which way of asking for debt time is most likely to work with your PM?
    options:
      - "The code is ugly. We need a refactoring sprint."
      - "Changes to pricing take 3 days instead of 1, and we plan 8 this quarter. Two days of cleanup a sprint pays back by March."
      - "Senior engineers always refactor."
    answer: 1
    why: It shows the interest in days, the plan, and when the cleanup pays back. That is a business case.
incidents: []
related: [working-with-pms, legacy-code, architecture-tradeoffs, migrations, estimation-planning]
---

## What broke

This story is made up, but it is common. Ravi works on the pricing service of a food delivery app called FoodNow. Three years ago, the team copied the discount logic into three places to hit a festival launch. Everyone agreed to clean it up the next month.

Next month never came. Each new offer now needs changes in three places. Engineers forget one of them about once a quarter. Last Diwali, customers saw one price in the cart and another at checkout. Support got 4,000 tickets in one evening.

The team was frustrated. A senior engineer proposed rewriting the whole pricing service in a new language. "Six weeks," he said. The rewrite started. Six months later it was still not finished. Meanwhile, the old service still needed every offer, so the team was maintaining two systems.

The problem was real. The fix was wrong. Nobody had measured where the debt hurt most, and nobody paid it down in small steps.

## The idea in simple words

Ward Cunningham first used the debt metaphor in 1992. He wrote: "Shipping first time code is like going into debt." [@cunningham-1992] He added that a little debt speeds development "so long as it is paid back promptly with a rewrite." [@cunningham-1992] He did not say that messy code is fine. The danger, he wrote, comes when the debt is not repaid. Then "every minute spent on not-quite-right code counts as interest on that debt." [@cunningham-1992]

Martin Fowler explains the parts. The messy code, which he calls cruft, is the debt. The extra effort it adds to each new feature is the interest. Cleaning up the cruft is paying back the principal. [@fowler-debt]

Debt is not always bad. Fowler sorts it on two axes: **reckless or prudent**, and **deliberate or inadvertent**. [@fowler-quadrant] Each kind has a typical quote:

- Reckless and deliberate: "We don't have time for design".
- Prudent and deliberate: "We must ship now and deal with consequences".
- Reckless and inadvertent: "What's Layering?"
- Prudent and inadvertent: "Now we know how we should have done it".

Fowler writes that "the useful distinction isn't between debt or non-debt, but between prudent and reckless debt." [@fowler-quadrant] He also says the prudent and inadvertent kind is "inevitable" for teams that are excellent designers. They learn the right design while they build. [@fowler-quadrant]

> [!key] **The key idea.** Debt only costs you when you touch the code. Pay down the debt in the code you change often, in small steps, and only when the interest is larger than the cost of fixing it.

## How it works

Step through one debt item, from the day someone notices it to the day its interest drops. Use Next and Back, or the ← and → keys.

[[diagram:tech-debt-register]]

### 1. Write the debt down

Keep a short debt register: a list in your tracker, one line per item. Each line says where the debt is, what it slows down, and who noticed it. A register turns "the code is bad" into items you can compare.

### 2. Measure the interest

Interest is what the debt costs you now. Look for three signals:

- **Time.** How many extra days does each change take? Ask the engineers who made the last few changes.
- **Change frequency.** How often does this code change? Your version control history tells you.
- **Incidents and bugs.** How many incidents or bug fixes touched this code?

Fowler notes that you only pay interest when you work in that part of the code. So "crufty but stable areas of code can be left alone." [@fowler-debt] But "areas of high activity need a zero-tolerance attitude to cruft." [@fowler-debt]

This is the idea behind a **code hotspot**: code that changes often and is hard to work with. CodeScene, a tool for this analysis, says most development activity happens in relatively few modules. [@codescene-hotspots] Its advice: low code health in a hotspot is expensive, so improve it first. Low health in stable code has lower priority. [@codescene-hotspots] In one example in its docs, the hotspots were 1.2% of the code but held 45% of the bugs found and fixed. [@codescene-hotspots]

Will Larson also starts with hot spots when he lists ways to improve quality. First fix the hot spots that cause problems now. Larger programs come later. He advises starting with the lightest solution that works. [@larson-quality]

### 3. Decide what to pay down

Debt work competes with features, so compare them in the same units. **Cost of delay** helps here. It combines how valuable a piece of work is with how urgent it is. [@cod] A related rule, CD3, divides cost of delay by duration. It favours work that is both valuable and quick. [@cod]

For debt, ask: what does each month of waiting cost us in slower changes, bugs and incidents? Compare that with the features you would delay.

### 4. Pay in small steps

There are four common ways to pay:

- **The Boy Scout rule.** Robert C. Martin's version is "Always check a module in cleaner than when you checked it out." [@boy-scout] Rename one variable or split one long function. [@boy-scout]
- **Fix it inside the feature.** Fowler calls this opportunistic refactoring: fix unclear code when you see it. He says a team that refactors well "should hardly ever need to plan refactoring." [@fowler-opportunistic] So clean up the pricing code inside the next pricing feature, and include that time in its estimate.
- **Dedicated time.** For larger items, agree on a fixed share of each sprint with your PM. This is the lesson's advice, not a rule from a source. The share matters less than doing it every sprint.
- **A rewrite.** This is the riskiest choice. Joel Spolsky called rewriting code from scratch "the single worst strategic mistake that any software company can make." [@spolsky-rewrite] His reason: "Old code has been used. It has been tested. Lots of bugs have been found, and they've been fixed." [@spolsky-rewrite] He used Netscape as the example. When he wrote, its last major release had come out almost three years earlier. [@spolsky-rewrite] Treat this as one strong opinion. Some rewrites are needed. But replace the system piece by piece, behind the same interface, and ship each piece.

### 5. Make the case in business terms

Talk about interest, not code style. "Each pricing change takes 3 days instead of 1. We plan 8 this quarter. Two days of cleanup per sprint pays back by March." Bring the incident count as well. See [working with PMs and the business](/learn/working-with-pms-and-the-business) for how to frame options and costs.

## Trade-offs

| Way to pay | Risk | Cost to the roadmap | Use it when |
|---|---|---|---|
| Boy Scout rule | Low. Small, reviewed changes. | Almost none. | Always, in the code you touch. |
| Fix inside a feature | Low. Tests from the feature cover it. | The feature takes a bit longer. | The next feature works in the same code. |
| Dedicated time each sprint | Low to medium. | A fixed, visible share. | Debt in hotspots that no feature will touch soon. |
| Big cleanup project | Medium. It can grow quietly. | Weeks with fewer features. | One measured hotspot, with a clear end. |
| Full rewrite | High. Lost bug fixes, two systems for a long time. | Months with little new value. | Rarely. Prefer replacing piece by piece. |
| Leave it alone | Low, while the code stays stable. | None. | The code is stable and rarely changes. [@fowler-debt] |

**Simple rule:** pay down debt where you change code often, in steps small enough to ship every week.

## At work: how a senior uses this

- Keep a debt register. Review it once a month with your PM. Remove items nobody has touched.
- For each item, write the interest in days, bugs or incidents, not in adjectives.
- Before you propose cleanup, check the change history. If the code rarely changes, leave it.
- When you take on deliberate debt, write the ticket and the payback date in the same pull request.
- Include cleanup time in feature estimates for messy code. Do not hide it, and do not ask for it separately each time.
- When someone proposes a rewrite, ask: "Can we replace one piece first and ship it?"
- After paying down an item, measure again. Show the PM that the change time dropped.

## With AI

### What to ask the agent

> "Here is the git log for the last 12 months as a list of files and change counts. Rank the 10 files that changed most, and for each one, list the size and the number of commits that mention 'fix' or 'bug'."

> "Read this module. List the duplicated logic and the riskiest parts to change. Suggest three refactoring steps, each small enough to ship alone with its own tests."

> "Turn this debt item into a short note for my PM: what it costs us each month, the fix in steps, and when it pays back."

### What to check yourself

- The ranking uses real history, not guesses. Run the git command yourself and compare.
- Each refactoring step keeps behaviour the same. Tests must pass before and after every step.
- The agent did not "clean up" odd code that is really a bug fix. Check the commit history of strange lines before you delete them.
- The cost numbers in the PM note come from your team, not from the agent.
- An agent can rewrite a module fast. That does not make a rewrite less risky. Review it like any other large change.

## Check yourself

## Go deeper

Before this: [architecture trade-offs](/learn/making-architecture-trade-offs). Related: [working with PMs and the business](/learn/working-with-pms-and-the-business), [refactoring and legacy code](/learn/refactoring-and-legacy-code), [estimation and planning under uncertainty](/learn/estimation-planning-uncertainty) and [running large migrations](/learn/running-large-migrations).
