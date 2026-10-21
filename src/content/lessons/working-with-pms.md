---
topic: working-with-pms
subtitle: Turn "the business wants X" into a clear goal, honest options and a plan you can keep, and say "no, but…" when you need to.
status: published
reviewed_by: ai-factcheck
reviewed_on: 2026-10-21
illustrative_story: true
sources:
  - id: cagan-risks
    title: "Marty Cagan, “The Four Big Risks”, SVPG (2017)"
    url: https://www.svpg.com/four-big-risks/
    note: Value, usability, feasibility and business viability risk, and which role owns each one.
  - id: cagan-pm
    title: "Marty Cagan, “Behind Every Great Product”, SVPG"
    url: https://www.svpg.com/behind-every-great-product/
    note: What a strong product manager is accountable for, and why it is more than writing a backlog.
  - id: cagan-teams
    title: "Marty Cagan, “Product vs. Feature Teams”, SVPG (2019)"
    url: https://www.svpg.com/product-vs-feature-teams/
    note: Output against outcomes, and what is lost when engineers only deliver what others decided.
  - id: shapeup-appetite
    title: "Ryan Singer, Shape Up, chapter 3: “Set Boundaries” (Basecamp)"
    url: https://basecamp.com/shapeup/1.2-chapter-03
    note: Appetite as a time budget, and fixed time with variable scope.
  - id: shapeup-stop
    title: "Ryan Singer, Shape Up, chapter 14: “Decide When to Stop” (Basecamp)"
    url: https://basecamp.com/shapeup/3.5-chapter-14
    note: Scope hammering, comparing to what customers do today, and why cutting scope is not cutting quality.
  - id: mcconnell-cone
    title: "Steve McConnell, “Software Development's Cone of Uncertainty”, Construx white paper (2010)"
    url: https://www.construx.com/wp-content/uploads/2019/02/CxWhitePaper_ConeOfUncertainty.pdf
    note: Why early estimates can be off by 4 times in either direction, and when a commitment becomes meaningful.
  - id: fowler-debt
    title: "Martin Fowler, “Technical Debt” (bliki, 2019)"
    url: https://martinfowler.com/bliki/TechnicalDebt.html
    note: Cruft, principal and interest, with a worked example in days of work.
  - id: orosz-product
    title: "Gergely Orosz, “The Product-Minded Software Engineer”, The Pragmatic Engineer (2019)"
    url: https://blog.pragmaticengineer.com/the-product-minded-engineer/
    note: Nine traits of engineers who care about the product, from asking why to following up after launch.
claims_checked:
  - claim: "The PM owns value and viability risk, the designer owns usability, and engineers own feasibility."
    source: cagan-risks
    note: "SVPG, 4 Dec 2017: PM responsible for value and viability, designer for usability, lead engineer for feasibility. Feasibility wording ('time, skills and technology we have') matches. Viability examples include sales channel, legal, monetization."
  - claim: "Feature teams get only about half of the engineers' value; product teams are measured by outcomes, not output."
    source: cagan-teams
    note: "SVPG, 29 Aug 2019: 'we're only getting about half their real value'; empowered teams 'measured by outcomes (rather than output)'."
  - claim: "Estimates at the first idea can be 4 times too high or 4 times too low; the cone narrows only as decisions are made; delay commitments until it narrows."
    source: mcconnell-cone
    note: "Construx white paper v1, Jan 2010, p.4-6: 4x high / 0.25x low at Initial Concept; 'narrows only as you make decisions', including what you will not do; 'Effective organizations delay their commitments'."
  - claim: "An appetite is completely different from an estimate; Shape Up uses fixed time, variable scope."
    source: shapeup-appetite
    note: "Shape Up ch. 3 'Set Boundaries': exact quote 'An appetite is completely different from an estimate.' and 'fixed time, variable scope'."
  - claim: "Variable scope is not about sacrificing quality; compare a cut to what customers do today."
    source: shapeup-stop
    note: "Shape Up ch. 14 'Decide When to Stop': exact quote; 'compare to baseline' means comparing to the customers' current workaround."
  - claim: "Feature takes 4 days on clean code, 6 days with cruft; cleanup costs 5 days; not worth it for one feature, faster for three; rarely changed crufty code can be left alone."
    source: fowler-debt
    note: "Fowler bliki, 21 May 2019: numbers match; 'two more similar features coming up' = three in total (17 vs 18 days); 'crufty but stable areas of code can be left alone'."
  - claim: "Asking why, talking with non-engineers, and following up on results after launch are traits of product-minded engineers."
    source: orosz-product
    note: "Pragmatic Engineer, Sep 2019: traits 3 (curiosity about why), 4 (communication with non-engineers) and 8 (done only after results on user behavior and business metrics)."
quiz:
  - q: Your PM says "Sales wants a bulk export feature by the end of the month." What should you do first?
    options:
      - Start building, because the date is short.
      - Ask what problem the customers have, who they are, and how you will know it worked.
      - Say no, because the date is too short.
    answer: 1
    why: The request names a solution, not a goal. Knowing the goal lets you offer cheaper options that solve the same problem.
  - q: Early in a project, your PM asks for a single delivery date. What is the most honest answer?
    options:
      - One exact date, so the PM can plan.
      - A range, such as "4 to 8 weeks", plus what you will learn soon that will narrow it.
      - "I can't know until it's done."
    answer: 1
    why: Early estimates carry large uncertainty. A range shows it, and the plan to narrow it makes the range useful.
  - q: The date is fixed and the work is too big. Which cut is usually best?
    options:
      - Skip the tests and code review.
      - Cut or delay the least important parts of the feature, and keep quality.
      - Ask the team to work every weekend.
    answer: 1
    why: Shape Up's rule is fixed time, variable scope. Cutting quality creates bugs and debt that cost more later.
  - q: How do you best explain why you want two weeks to clean up the payments module?
    options:
      - "The code is ugly and I want to refactor it."
      - "Each change here takes us about 2 extra days. We plan 6 changes this quarter, so cleaning up first saves time."
      - "All good teams refactor."
    answer: 1
    why: The business case is the interest you pay on every change, compared with the one-time cost of cleaning up.
  - q: In week 2 you find a risk that could move the launch by 3 weeks. When should you tell your PM?
    options:
      - The same day, with what you know and what you will check next.
      - After you have tried to fix it yourself.
      - At the next monthly review.
    answer: 0
    why: Early news gives the PM time to cut scope, move the date or warn others. Late news removes those choices.
incidents: []
glossary_optout: [scope]
related: [influence-without-authority, estimation-planning, tech-debt, requirements-nfrs, design-docs, technical-writing]
---

## What broke

This story is made up, but it happens in many companies. Kavya is a backend engineer at a grocery delivery app. Her PM says: "Customers must be able to reschedule a delivery in the app. The business wants it by 1 March."

Kavya does exactly that. She builds a screen to pick any day and any time slot in the next two weeks. The code is clean and well tested.

In week 2 she finds a problem. The warehouse system can only change orders up to 48 hours before delivery. Fixing that needs work from another team. Kavya does not tell anyone. She hopes to find a workaround.

She does not find one. The launch is three weeks late. When it finally ships, only 3% of customers use it. Most people who called support did not want a new day. They wanted to know where their order was.

Nobody asked why the business wanted the feature. Nobody raised the risk while there was still time to act. Kavya's code was good. The way she worked with her PM was not.

## The idea in simple words

A **product manager** (PM) decides what the team should build and why. Marty Cagan of SVPG says the PM owns two big risks. **Value**: will customers want it? **Viability**: does it work for the business, for example sales, legal and money? [@cagan-risks] The designer owns usability. The engineers own **feasibility**: can we build it with the time, skills and technology we have? [@cagan-risks]

So you and your PM are not on opposite sides. You share one goal: a result that matters to customers and the business. Cagan says a PM must ensure "a business outcome", not just that a product gets defined. [@cagan-pm]

Your job is to bring what only an engineer knows. What is cheap and what is costly. What could go wrong. Which simpler idea solves the same problem.

> [!key] **The key idea.** Never answer a request with only "yes" or "no". Ask what problem it solves. Then offer options, each with a cost, a risk and the value it brings. Let the PM choose with you.

Cagan warns about "feature teams", where engineers only build what others decided. He says you then get only about half of the engineers' value. [@cagan-teams] Product teams are measured by outcomes, not output. [@cagan-teams]

## How it works

Step through one request, from a vague ask to a plan with check-ins. Use Next and Back, or the ← and → keys.

[[diagram:pm-request-to-scope]]

### 1. Turn the request into a goal

"The business wants X" usually names a solution. Ask questions until you find the problem behind it:

- Why now? What happens if we do nothing?
- Who asks for this, and how many of them?
- How will we know it worked? Which number should move?

Gergely Orosz lists asking "why" again and again as a trait of product-minded engineers. [@orosz-product] Write the answers down. A goal like "30% fewer support calls about delivery times" lets you judge every option.

### 2. Say "no, but…" with options

Rarely say a flat no. Say "not like that, but here is what we can do". Give two or three options, each with a cost, a risk and how much of the goal it reaches.

Shape Up, Basecamp's book on product work, calls the time you are willing to spend the **appetite**. It says an appetite is "completely different from an estimate". [@shapeup-appetite] You start from the time the problem is worth, then shape a solution that fits. So ask your PM: "How much is this worth to us? Two weeks or two months?"

### 3. Give estimates as ranges

Early in a project, nobody knows the real size of the work. Steve McConnell's "Cone of Uncertainty" shows that estimates made at the first idea can be 4 times too high or 4 times too low. [@mcconnell-cone] The range narrows only as you make decisions, such as writing down what you will not do. [@mcconnell-cone]

So say "4 to 8 weeks", not "6 weeks". Then add what will narrow the range: "After I test the warehouse API on Friday, I can give you 5 to 6 weeks." McConnell advises waiting to commit until the range has narrowed. [@mcconnell-cone]

### 4. Cut scope, not quality

Here, scope means the list of features in the project. When time is short, you can move the date, add people or cut scope. Shape Up fixes the time and lets scope change. [@shapeup-appetite] It also says "Variable scope is not about sacrificing quality." [@shapeup-stop] Its test for a cut is useful: compare the result with what customers do today, not with a perfect version. [@shapeup-stop]

Good cuts are parts that few users need, cases you can handle by hand at first, or a second phase after launch.

### 5. Explain technical debt in business terms

"The code is messy" means little to a PM. Martin Fowler explains **technical debt** with money words. The messy code is the debt. The extra effort it adds to every change is the interest. Cleaning it up is paying back the principal. [@fowler-debt]

His example: a feature takes 4 days on clean code but 6 days on messy code. The cleanup costs 5 days. For one feature it is not worth it. For three similar features, cleaning up first is faster. [@fowler-debt] Messy code that you rarely change can stay as it is. [@fowler-debt] So bring numbers: "Each change here costs about 2 extra days. We plan 6 this quarter."

### 6. Raise risks early

Share bad news early. It costs more the longer you wait. In week 2, the PM can still cut scope, move the date or warn sales. In week 8, the only choice left is to miss the date.

Use a simple format: what you found, what it could cost, what you are doing next, and when you will know more. Say it the same day, even if you are not sure yet.

### 7. Take part in discovery

**Product discovery** is the work of finding out what to build before you build it. Join it. Talk to users and read support tickets. Attend the meetings where ideas are judged. Feasibility is your risk. [@cagan-risks] If you join early, you can raise a feasibility problem before the team commits to a plan.

### 8. Write for people who are not engineers

When you write to the PM, sales or leaders, lead with the decision you need and the date. Then give two or three options in plain words. Put the costs in days, money or customers. Leave out class names and database terms. Orosz lists good communication with non-engineers as one of the traits. [@orosz-product]

## Trade-offs

| Way to respond | Keeps trust? | Cost | Use it when |
|---|---|---|---|
| Say yes to everything | No. Dates are missed and quality drops. | Hidden: overtime, bugs, debt. | Never as a habit. |
| Flat "no" | No. People see you as the one who stops work. | The business goal stays unsolved. | Only for unsafe or illegal requests, with a reason. |
| "No, but…" with options and costs | Yes. The PM can make a real choice. | An hour or two of thinking. | Most requests that do not fit the time. |
| Fixed date, cut scope | Yes, if cuts are agreed early. | Some features wait for phase 2. | Hard dates, such as a sale or a law. |
| Fixed scope, move the date | Yes, if you say so early. | Others who planned around the date must adjust. | Every part is a must-have, such as a compliance rule. |
| One exact estimate early | No. It will likely be wrong. | Lost trust when the date is missed. | Avoid. Give a range and a date to narrow it. |

**Simple rule:** bring options, costs and risks early. The PM decides the trade-off with you, not after you.

## At work: how a senior uses this

- For every new request, ask: **"What problem does this solve, and which number should move?"**
- Answer big asks with two or three options. Give each one a cost range, the main risk and how much of the goal it reaches.
- Ask your PM for the appetite: "How much time is this worth?" Then design to fit it.
- Write the non-goals into the plan, so the scope does not grow quietly.
- Give estimates as ranges, and say when and how you will narrow them.
- Raise a risk the day you find it, with what you know and what you will check next.
- Keep a short list of technical debt with its "interest": extra days per change. Bring it to planning with numbers.
- After launch, check the number you agreed on. Share the result with your PM, even if it is bad. [@orosz-product]

## With AI

### What to ask the agent

> "Here is a feature request from my PM. List the questions I should ask before I estimate it: the problem, the users, the success metric and the hidden risks."

> "Give me three ways to deliver this, from smallest to fullest. For each one, list what is in, what is out, the main risk and a rough size."

> "Rewrite this update for a non-technical PM. Lead with the decision I need, keep it under 150 words, and remove all code terms."

### What to check yourself

- The options are real. Check each size against your own codebase. An agent does not know your warehouse API is slow.
- The estimate is a range, and you can explain both ends.
- The business goal comes from your PM or real data. The agent may invent a plausible goal.
- The rewrite still says the risk clearly. Plain words must not hide bad news.
- You talked to the PM in person. An agent can draft the message but cannot build the trust.

## Check yourself

## Go deeper

Before this: [What "senior" really means](/learn/what-senior-really-means). Next: [estimation and planning under uncertainty](/learn/estimation-planning-uncertainty), [technical debt management](/learn/technical-debt-management) and [influence without authority](/learn/influence-without-authority).
