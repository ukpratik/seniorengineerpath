---
topic: estimation-planning
subtitle: Give estimates as honest ranges, shrink them by removing unknowns, and keep your plan true when the facts change.
status: published
reviewed_by: ai-factcheck
reviewed_on: 2026-10-22
illustrative_story: true
sources:
  - id: mcconnell-what
    title: "Steve McConnell, “Demystifying the Black Art of Software Estimation: What Is an ‘Estimate’?”, Microsoft Press Store (2006), excerpt from Software Estimation: Demystifying the Black Art, chapter 1"
    url: https://www.microsoftpressstore.com/articles/article.aspx?p=2191414
    note: Estimates, targets and commitments; why a single number is usually a target; and the real purpose of estimation. Four short web pages.
  - id: mcconnell-cone
    title: "Steve McConnell, “Software Development's Cone of Uncertainty”, Construx white paper (2010)"
    url: https://www.construx.com/wp-content/uploads/2019/02/CxWhitePaper_ConeOfUncertainty.pdf
    note: Why more time alone does not improve an estimate, and how decisions narrow the cone.
  - id: flyvbjerg-rcf
    title: "Bent Flyvbjerg, “From Nobel Prize to Project Management: Getting Risks Right”, Project Management Journal 37(3), 2006"
    url: https://arxiv.org/abs/1302.3642
    note: The planning fallacy, the inside and outside view, the three steps of reference class forecasting, and buffers set by acceptable risk.
  - id: xp-spike
    title: "Don Wells, “Create a Spike Solution”, ExtremeProgramming.org"
    url: http://www.extremeprogramming.org/rules/spike.html
    note: A short page on spikes, small throwaway programs that reduce risk and improve estimates.
  - id: shapeup-hill
    title: "Ryan Singer, Shape Up, chapter 13: “Show Progress” (Basecamp)"
    url: https://basecamp.com/shapeup/3.4-chapter-13
    note: Hill charts, uphill unknowns against downhill execution, and why to do the scariest work first.
  - id: shapeup-appetite
    title: "Ryan Singer, Shape Up, chapter 3: “Set Boundaries” (Basecamp)"
    url: https://basecamp.com/shapeup/1.2-chapter-03
    note: Appetite as a time budget, and fixed time with variable scope.
  - id: mcconnell-theses
    title: "Steve McConnell, “17 Theses on Software Estimation”, Construx blog"
    url: https://www.construx.com/blog/17-theses-on-software-estimation/
    note: McConnell's reply to #NoEstimates, with the business reasons estimates exist.
  - id: heusser-noestimates
    title: "Matt Heusser, “‘No Estimates’ in Action: 5 Ways to Rethink Software Projects”, CIO (2013)"
    url: https://www.cio.com/article/2381167/no-estimates-in-action-5-ways-to-rethink-software-projects.html
    note: Where #NoEstimates came from, what Woody Zuill did and did not claim, and five ways teams work without estimates.
claims_checked:
  - claim: "An estimate is a prediction, a target is a desirable business objective, and a commitment is a promise to deliver defined functionality at a quality by a date; a single-point estimate is usually a target masquerading as an estimate."
    source: mcconnell-what
    note: "Microsoft Press Store excerpt, pages 1 and 3: definitions quoted exactly; 'A single-point estimate is usually a target masquerading as an estimate'; 'no limit to how poorly a project can go'. Page 4: primary purpose and 'within about 20%'."
  - claim: "More time alone does not make an estimate more accurate; the only way to reduce estimate variability is to reduce project variability; the cone is a best case and narrows only through decisions."
    source: mcconnell-cone
    note: "Construx white paper (Jan 2010), p.4-5: 'If I give you another week ... not possible'; 'The only way to reduce the variability in the estimate is to reduce the variability in the project itself'; 'represents the best case accuracy'; Cloud of Uncertainty when the project does not converge."
  - claim: "Kahneman and Tversky (1979) found judgment generally optimistic; Lovallo and Kahneman (2003) call this the planning fallacy, caused by the inside view; reference class forecasting has three steps."
    source: flyvbjerg-rcf
    note: "arXiv 1302.3642, p.6-7: 'Kahneman and Tversky (1979a, b) found human judgment to be generally optimistic'; 'Lovallo and Kahneman (2003: 58) call such common behavior the \"planning fallacy\"'. The paper does not say who coined the term, so the lesson does not claim it."
  - claim: "Kahneman's curriculum team guessed 18 to 30 months; about 40% of similar teams gave up, the rest took 7 to 10 years; the team finished eight years later. UK roads need a 15% uplift at 50% overrun risk and 32% at under 20%."
    source: flyvbjerg-rcf
    note: "arXiv 1302.3642, p.7-8 (story from Lovallo and Kahneman 2003: 61) and p.12-13 and Table 3 (roads 15% / 32%); 'Reserves will be spent simply because they are there'."
  - claim: "A spike solution is a very simple program that explores one problem, is usually thrown away, and reduces risk or makes an estimate more reliable."
    source: xp-spike
    note: "ExtremeProgramming.org spike page (Don Wells, 1999), read in full. 'Time-boxed' is the lesson's own advice, not from this page."
  - claim: "McConnell counts velocity-based projection as 'a really effective form of estimation', lists business uses of estimates, and accepts contexts where estimates add little value; Zuill did not claim estimates are bad in all cases."
    source: mcconnell-theses
    note: "Construx blog (read via web archive; live site blocked automated access): theses 3(c), 4, 5 and 15. Heusser, CIO, 5 Nov 2013: Zuill said estimates are 'not always essential'."

quiz:
  - q: Your director says "We need this live by 1 March for the trade fair." In McConnell's terms, what is 1 March?
    options:
      - An estimate
      - A target
      - A commitment
    answer: 1
    why: A target is a desired business objective. It may be important, but that does not make it achievable.
  - q: Your team has built five similar integrations. They took 4, 5, 5, 7 and 9 weeks. A new one looks "easy, about 3 weeks". What is the best first estimate?
    options:
      - 3 weeks, because this one looks easy.
      - About 5 to 9 weeks, starting from the past results, then adjust for real differences.
      - 9 weeks, the worst case, to be safe.
    answer: 1
    why: This is the outside view. Start from what similar work really took, and move away from it only for clear reasons.
  - q: You do not know if a vendor's API can handle partial refunds. What should you do before you give a firm date?
    options:
      - Pad the estimate by 50% and hope.
      - Run a short, time-boxed spike to test that one question.
      - Ask the PM to decide.
    answer: 1
    why: A spike removes the biggest unknown cheaply. Removing unknowns narrows the range; more time spent thinking does not.
  - q: In week 3 of 6 you see you will miss the date by about two weeks. What is the best move?
    options:
      - Tell the PM now, with options such as cutting scope or moving the date.
      - Work weekends and say nothing.
      - Wait until week 6 in case things improve.
    answer: 0
    why: Early news leaves real choices. With fixed time, cutting scope is often the best one.
incidents: []
related: [working-with-pms, tech-debt, multi-team-projects, latency-estimation, design-docs]
---

## What broke

This story is made up, but it happens often. Meera's team runs checkout for an online pharmacy. In a planning meeting, the PM asks how long it will take to add a second payment provider. Meera has done one before. "About two weeks," she says.

By the evening, "two weeks" is on the roadmap as a launch date. Marketing books a campaign for the day after.

In week 2 Meera finds the problems. The new provider handles refunds in a different way. Its test system is down for days at a time. The finance team needs a new daily report. None of this was in her head when she said "two weeks".

She tells the PM in week 5. The campaign is cancelled. The feature ships in week 7. Seven weeks was a normal result for this kind of work. The real failure was a quick guess that became a promise, and the late warning.

## The idea in simple words

Steve McConnell separates three things that people mix up: [@mcconnell-what]

- An **estimate** is "a prediction of how long a project will take or how much it will cost".
- A **target** is "a statement of a desirable business objective", such as a trade fair date.
- A **commitment** is "a promise to deliver defined functionality at a specific level of quality by a certain date".

When managers ask for an estimate, they often want a commitment, or a plan to meet a target. [@mcconnell-what] McConnell says estimation should be unbiased. Planning, in contrast, is biased on purpose toward a goal. [@mcconnell-what] Mix them, and the target quietly replaces the estimate.

McConnell also says a single-point estimate is "usually a target masquerading as an estimate". [@mcconnell-what] Outcomes have a long tail on the late side. There is a limit to how well a project can go, but no limit to how badly. [@mcconnell-what]

Why are early guesses so optimistic? Bent Flyvbjerg sums up the research. In 1979, Kahneman and Tversky found human judgment to be generally optimistic. So people underestimate the costs, time and risks of their plans. [@flyvbjerg-rcf] Flyvbjerg cites a 2003 paper by Lovallo and Kahneman that calls this behavior the **planning fallacy**. [@flyvbjerg-rcf] They say it comes from the "inside view": you think about the details of your own plan, not about how similar work turned out. [@flyvbjerg-rcf]

> [!key] **The key idea.** An estimate is a range with a level of confidence. You make it smaller by removing unknowns, not by thinking harder. Keep it separate from the target, and decide the commitment only when the range is small enough.

The [working with PMs lesson](/learn/working-with-pms-and-the-business) shows the Cone of Uncertainty and why you give ranges. Here you learn how to make the range, and how to shrink it.

## How it works

Step through one estimate as the team removes unknowns. Use Next and Back, or the ← and → keys.

[[diagram:estimate-range-narrows]]

### 1. Start from the outside view

Before you list tasks, ask: how long did similar work take us before? This is **reference class forecasting**. Flyvbjerg gives three steps. Pick a class of similar past projects. Find how their results were spread. Place your project in that spread. [@flyvbjerg-rcf]

His best-known story is about Kahneman himself. Kahneman's team was writing a school curriculum, and its members guessed 18 to 30 months. Then a curriculum expert on the team was asked about similar teams. About 40% of them never finished. The rest took seven to ten years. The team went on anyway and finished eight years later. [@flyvbjerg-rcf]

For you, the reference class is your ticket history. Look up the last five similar features and what they really took.

### 2. Break the work down

Then use the inside view to check the outside one. Split the work into pieces of a few days. Write the unknowns next to each piece: "Does the vendor support partial refunds?" Shape Up separates imagined tasks, which you list before you start, from discovered tasks, which you find while doing the real work. [@shapeup-hill] So your list will grow. Leave room for that.

### 3. Remove unknowns with spikes

You cannot make an estimate more accurate just by spending another week on it. McConnell says the only way to reduce the variability in an estimate is to reduce the variability in the project itself. [@mcconnell-cone] The cone narrows only as you make decisions that remove variability. [@mcconnell-cone] The cone also shows the best case. In a project that does not remove unknowns, the range can stay wide to the end. [@mcconnell-cone]

A **spike solution** is a very simple program that explores one hard question and ignores everything else. Expect to throw it away. Its goal is to reduce risk or make an estimate more reliable. [@xp-spike] For a hard problem, XP suggests putting a pair of developers on it for a week or two. [@xp-spike] Shape Up gives the same order: push the scariest, most unknown work "uphill" first. [@shapeup-hill]

### 4. Give a range and a confidence

Say it like this: "5 to 9 weeks. I am about 80% sure we finish within 9." Flyvbjerg shows how the safety margin depends on the risk you accept. For UK road projects, a 50% chance of going over budget needed a 15% uplift. A 20% chance needed 32%. [@flyvbjerg-rcf] The lower the risk you accept, the bigger the buffer.

Make the buffer visible, one line in the plan. He also warns that a large reserve can be spent just because it exists. [@flyvbjerg-rcf]

### 5. Fit the plan to the target

McConnell says the main purpose of estimation is not to predict the outcome. It is to check if the targets are realistic enough to control the project to meet them. [@mcconnell-what] If the first target and the first estimate are within about 20% of each other, the project manager can usually adjust features, schedule or team size to meet it. [@mcconnell-what] With a much bigger gap, talk with your PM about changing the target or the scope.

Shape Up's answer is to fix the time and let scope vary. [@shapeup-appetite] Decide with your PM which parts are must-haves before work starts.

### 6. Track and re-estimate

Every week, compare done work with the plan, and update the range. Say what changed and why. McConnell counts using measured team speed from early iterations to project the end date as estimation, and calls it "a really effective form of estimation". [@mcconnell-theses]

### 7. When you will miss the date

Tell people the day you know. Bring options: cut scope, move the date, or both. Agree on the cut list early, so this talk is short. Cutting scope keeps quality; cutting tests does not.

### A debated view: #NoEstimates

Woody Zuill started the #NoEstimates hashtag. He said estimates are not always needed. He did not claim they are bad in all cases. [@heusser-noestimates] Teams in this style deliver small working pieces often and decide based on what they deliver. [@heusser-noestimates] McConnell disagrees. He lists many business uses of estimates, such as budgets, staffing and release-date commitments to the market. [@mcconnell-theses] He says estimation is a skill every true software professional needs to develop. [@mcconnell-theses] He also agrees that in some contexts, estimates add little value. [@mcconnell-theses] A fair middle: slice work small, and still be able to give a range when the business asks.

## Trade-offs

| Approach | Accuracy early on | Effort | Use it when |
|---|---|---|---|
| Single number from a gut feeling | No. Often a hidden target. | Seconds. | Never for a date that others will plan around. |
| Task breakdown only (inside view) | Often too optimistic. | Hours. | You also check it against past data. |
| Past data (outside view) | Yes, if the past work is truly similar. | An hour with your ticket history. | You have done similar work before. |
| Spike first, then estimate | Yes. It removes the biggest unknown. | A few days. | One unknown drives most of the range. |
| Fixed time, variable scope | The date holds; the scope moves. | Needs clear priorities. | A hard date, such as a sale or a law. |
| No estimates, small slices | No long-range date. | Low. | The business can work without a date. |

**Simple rule:** start from past data, remove the biggest unknown first, and commit only when the range is small enough for the decision at hand.

## At work: how a senior uses this

- When asked "how long?", ask back: "Is there a target date? What decision does this estimate support?"
- Never give a number in a meeting. Say: "I will send a range tomorrow."
- Keep a simple sheet of past features: first estimate, real time, and what surprised you.
- Write the top three unknowns into the estimate, each with a plan to remove it.
- Run spikes before you commit. Time-box them, and throw the code away.
- Give a range with a confidence, and a date when the range will shrink.
- Re-estimate every week, and share the new range even when it is worse.
- When a slip appears, tell the PM the same day with options. See [technical debt management](/learn/technical-debt-management) for how debt silently widens your ranges.

## With AI

### What to ask the agent

> "Here is a feature description. Break it into tasks of 1 to 3 days. For each task, list the unknowns and the question a spike should answer."

> "Here are our last 10 features with first estimates and real durations. Show me the ratio of real to estimated time, and the spread. What range would a similar new feature get?"

> "Write a short update for my PM: our range moved from 5–9 weeks to 7–9 weeks. Say why, what we will cut if needed, and when we will know more."

### What to check yourself

- The task list includes the boring work: tests, rollout, monitoring, docs and the review cycle. Agents often skip it.
- The past data really is similar work. A wrong reference class gives a confident, wrong range.
- The agent's numbers come from your data, not from typical figures it invented.
- An agent can make coding faster, but the unknowns stay. Vendor answers, reviews and approvals take the same time.
- The update states the bad news plainly in the first line.

## Check yourself

## Go deeper

Before this: [working with PMs and the business](/learn/working-with-pms-and-the-business). Next: [leading a multi-team project](/learn/leading-multi-team-projects). Related: [technical debt management](/learn/technical-debt-management).
