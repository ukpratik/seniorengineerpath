---
topic: behavioural-interviews
subtitle: Build a bank of true stories, tell each one in about two to three minutes with STAR, and make the "I", the trade-offs and the result easy to hear.
status: published
reviewed_by: ai-factcheck
reviewed_on: 2026-11-08
illustrative_story: true
sources:
  - id: amazon-loop
    title: "Amazon Jobs, “Interview loop”"
    url: https://www.amazon.jobs/content/en/how-we-hire/interview-loop
    note: Amazon's sample behavioural questions, its STAR guide and its tips for great answers. Open the sections on the page to read them.
  - id: amazon-lp
    title: "Amazon Jobs, “Leadership Principles”"
    url: https://www.amazon.jobs/content/en/our-workplace/leadership-principles
    note: The full list of Amazon's Leadership Principles, each with a short description.
  - id: rework-structured
    title: "Google re:Work, “A guide to structured interviewing for better hiring practices” (updated March 2026)"
    url: https://rework.withgoogle.com/intl/en/guides/a-guide-to-structured-interviewing-for-better-hiring-practices
    note: Why Google uses structured interviews, behavioural versus hypothetical questions, rubrics, and Google's own results.
  - id: google-tips
    title: "Google Careers, “Interviewing at Google: Best practices, advice, and tips”"
    url: https://www.google.com/about/careers/applications/interview-tips/
    note: Google's version of STAR, its advice to prepare a broad library of examples, and what it looks for on ambiguity.
  - id: google-hire
    title: "Google Careers, “Our hiring process”"
    url: https://www.google.com/about/careers/applications/how-we-hire/
    note: Google's short description of structured interviewing with the same rubrics for everyone, and why it dropped brainteasers.
  - id: tih-behavioral
    title: "Tech Interview Handbook, “Behavioral interviews for Software Engineers: How to prepare” (Yangshun, with Austen McDonald)"
    url: https://www.techinterviewhandbook.org/behavioral-interview/
    note: A free third-party guide on STAR, an extra R for Reflection, and how to pick the projects your stories come from.
claims_checked:
  - claim: "As of November 2026, Amazon lists 16 Leadership Principles, including Ownership ('never say that's not my job') and Have Backbone; Disagree and Commit ('respectfully challenge decisions when they disagree')."
    source: amazon-lp
    note: "Counted the 16 headings in the page HTML on 2026-11-08 (read with curl). Both quotes are exact."
  - claim: "Amazon evaluates all candidates through the lens of its Leadership Principles, calls STAR 'not the only way', asks for 'your individual contributions' and 'I' instead of 'we', says it does not expect 'rehearsed scripts', and avoids brain teasers because they do not reliably predict success."
    source: amazon-loop
    note: "Read from the accordion text embedded as JSON in the page HTML; WebFetch does not render it."
  - claim: "Google's STAR uses 'Activity' for A (the steps you took and your role) and advises 'a broad library of examples' and 'concise narratives'."
    source: google-tips
    note: "Read from the page's embedded text. Google's S, T and R match Amazon's; only the A word differs."
  - claim: "re:Work says behavioural questions 'are good at revealing patterns of behavior', that academic research over 20 years backs structured interviews, and that in Google's data they were more predictive of job performance than unstructured ones."
    source: rework-structured
    note: "The current URL works; the page is 'Updated March 2026'. It also shows a behavioural question with three follow-ups and discusses confirmation bias."
  - claim: "Google uses 'the same rubrics for everyone' considered for a role, and dropped brainteasers because its data showed they did not predict job performance."
    source: google-hire
    note: "Read from the page's embedded text and its FAQ."
quiz:
  - q: Why do companies like Amazon and Google ask so many "Tell me about a time…" questions?
    options:
      - Past questions are easier to score with no rubric.
      - Behavioural questions show patterns in what you really did, and structured interviews with them predict job performance better than unstructured ones.
      - Hypothetical questions are not allowed in interviews.
    answer: 1
    why: Google's re:Work guide says behavioural questions reveal patterns of behaviour, and that structured interviews were more predictive of job performance in its own data.
  - q: In a STAR story, which part should usually take the most time?
    options:
      - Situation, so the interviewer knows every detail of the system.
      - Action, because it shows what you did and why.
      - Result, because only the outcome matters.
    answer: 1
    why: The interviewer is judging your behaviour. The time split is our advice; Amazon asks you to focus on your individual contributions in the Action part.
  - q: An interviewer asks, "What did you do yourself?" right after your story. What does that most likely mean?
    options:
      - Your story used "we" so much that your own part was not clear.
      - The interviewer did not like the project.
      - The interview is going badly and you should change the story.
    answer: 0
    why: Interviewers can only score what you did. Amazon's guide suggests using "I" instead of "we" for this reason.
  - q: You are asked about a failure. Which answer is strongest?
    options:
      - "I care too much about quality, so I sometimes work late."
      - "The release failed because the QA team missed a bug."
      - A real mistake you made, what it cost, what you changed, and how you know the change worked.
    answer: 2
    why: The first is a boast dressed as a failure, and the second blames others. Amazon says what matters in a failure is what you learned and how you grew.
incidents: []
glossary_optout: [leader]
related: [interview-loops, what-senior-means, mentoring, code-review, design-docs, incident-response, negotiation]
---

## What broke

This story is made up, but each mistake in it is common. Kiran is a backend engineer with three years of experience. He passed the coding rounds of a big-tech loop. Then he met the behavioural interviewer.

"Tell me about a time you failed," she said. Kiran answered, "I sometimes care too much about code quality, so I work late." She waited. He had nothing else.

"Tell me about a conflict with a teammate." Kiran talked for eight minutes. Most of it was about the system. He said "we" twenty times. When she asked, "What did you do yourself?", he could not say.

For the third question, he told the same story again. He never gave a result. He never said what he learned.

The feedback said "not enough signal". Kiran had done good work in his job. He just had not prepared to show it.

## The idea in simple words

A behavioural question asks what you really did in the past. "Tell me about a time…" is the usual opening. The interviewer is not looking for a right answer. They want evidence of how you behave.

Google's re:Work guide explains the logic. Behavioural questions "are good at revealing patterns of behavior". Hypothetical questions show how you might handle something new. [@rework-structured]

At Google, these questions are part of a structured interview. Every candidate gets planned questions and is scored on the same scale. [@rework-structured] Google's hiring page says it uses "the same rubrics for everyone" for a role. [@google-hire]

Google's guide says academic research has backed this for about 20 years. In Google's own data, structured interviews predicted job performance better than unstructured ones. [@rework-structured] Google says it stopped asking brainteasers because they did not predict job success. [@google-hire] Amazon says it avoids them for the same reason. [@amazon-loop]

> [!key] **The key idea.** The interviewer can only score what you say clearly. Prepare true stories in advance, tell each one with a clear structure, and make your own actions, choices and results easy to hear.

## How it works

The big-tech loop lesson covered where this round fits. Here we go inside one answer. Step through it with Next and Back, or the ← and → keys. The time split is our advice, not a company rule.

[[diagram:star-story-timing]]

### STAR, as the companies write it

Amazon's guide lists the four parts of the STAR method: Situation, Task, Action and Result. It says to focus on "your individual contributions" and to use "I" instead of "we". It wants numbers in the result if you have them. [@amazon-loop]

Google writes the same idea with one small change. Its A is "Activity": the steps you took and your role. [@google-tips] The meaning is the same.

Amazon also says STAR is "not the only way" to tell a story. [@amazon-loop] The Tech Interview Handbook suggests an extra R, for Reflection: what you learned. It says this can "show seniority". [@tih-behavioral]

### Build a story bank

Do not prepare one answer per question. Prepare stories, then map each story to the themes it can answer. This is a story bank.

Our advice is 8–10 stories, as in the loop lesson's plan. Cover these common themes:

- **Conflict:** you disagreed with someone and worked it out.
- **Failure:** a real mistake, with its cost.
- **Ambiguity:** an unclear problem you turned into a plan.
- **Influence:** you changed a decision you did not own.
- **Ownership:** you fixed something that was not strictly your job.
- **Mentoring:** you helped someone else grow.
- **A hard technical call:** you chose between two options with real costs.

One project can feed several themes. The Tech Interview Handbook suggests starting from 3–5 big projects with high impact, high complexity and high involvement by you. [@tih-behavioral] Google calls this "a broad library of examples". [@google-tips] Amazon asks for range too: risks, successes, failures and growth. [@amazon-loop]

Write each story as five bullet points, not a script. Amazon says it does not expect "rehearsed scripts". [@amazon-loop]

### Leadership principles: Amazon as the public example

Amazon publishes the behaviours it hires for. As of November 2026, its page lists 16 Leadership Principles. [@amazon-lp] Its interview page says "All candidates are evaluated through the lens of these principles". [@amazon-loop]

Some principles map directly to themes. "Have Backbone; Disagree and Commit" asks leaders to "respectfully challenge decisions when they disagree". That is your conflict story. "Ownership" says leaders "never say 'that's not my job.'" [@amazon-lp] The Tech Interview Handbook says most top tech companies judge candidates against their own values. [@tih-behavioral] Read your target company's list before you pick which stories to tell.

### How stories change with level

Google's guide advises judging leadership "with different expectations based on the level of the role". [@rework-structured] The handbook says people skills matter more as an engineer becomes more senior. [@tih-behavioral] In our experience, four things change:

- **Scope.** A mid-level story is often about one feature. A senior story is about a system, a team or several teams.
- **"I" and "we".** Say "we" for the team's result. Say "I" for your part. At senior level, "I" often means "I aligned three teams", not "I wrote the code".
- **Numbers.** "Latency went from 900 ms to 200 ms" beats "it got faster".
- **Trade-offs.** Name the option you did not take, and why. Seniors are judged on choices, not only on effort.

### One story, weak and strong

This example is made up. The question is "Tell me about a hard technical call."

**Weak version:** "We had a refund bug. It was a big problem. We worked hard and fixed it. The launch went well."

There is no "I", no choice, and no number. The interviewer has nothing to score.

**Strong version:**

- **S:** "I was on the payments team. We planned to launch a new refund API in two weeks."
- **T:** "I owned the retry logic for the API."
- **A:** "In a load test, I found 3 duplicate refunds in 2,000 runs. The cause was a client retry after a timeout. I saw two options: launch on time and clean up by hand, or delay one week to add idempotency keys. I wrote a one-page note with the cost of each. I asked to delay and launch to 5% of users first."
- **R:** "The product manager agreed. We had zero duplicate refunds in the first month. I learned to raise this kind of risk at design review, not after it."

Said out loud, this takes under a minute. In a real interview, add more detail to the Action, for about two to three minutes in all. Each line answers a probe before it is asked.

### Follow-up probes

Expect follow-ups. Google says so on its tips page. [@google-tips] Its re:Work guide shows a behavioural question with three planned follow-ups. [@rework-structured] Amazon suggests you pause and ask if the interviewer wants more context. [@amazon-loop]

Common probes are "Why that option?", "What would you do differently?" and "What did your manager think?" Answer them directly. If you do not remember, say so.

### Common mistakes

- **No result.** The story stops at the action.
- **Blaming others.** Others may have made mistakes, but your story is about what you did.
- **A false failure.** "I work too hard" is a boast. Amazon says what matters is "what you learned and how you grew". [@amazon-loop]
- **Too long.** Google advises "concise narratives". [@google-tips] Our advice is about two to three minutes per story.
- **Made-up stories.** Never invent one. Follow-ups test the details, and invented details break. Amazon asks for "real examples from your past". [@amazon-loop]

## Trade-offs

| Approach | Works under follow-up? | Cost | Use it when |
|---|---|---|---|
| Improvise on the day | No. Stories run long and lose the result. | None up front. | Never for a loop you care about. |
| Memorise word-for-word scripts | Partly. You sound rehearsed and stall on new probes. | Many hours. | Never. Learn bullets instead. |
| Story bank of 8–10 bullet-point stories | Yes. | A few evenings of recall and writing. | Almost always. |
| Map stories to the company's principles | Yes. | Reading the company's list and choosing well. | When the company publishes one, as Amazon does. |

**Simple rule:** prepare true stories, not answers. Map them to themes, and practise telling each in about two to three minutes.

## At work: how a senior uses this

- Keep a work log. Each Friday, write one line on a decision, a number or a conflict. Your next story bank writes itself.
- After an incident, note your own part and the result. The [incident review](/learn/incident-response-and-postmortems) gives you the facts.
- Save the doc links. A [design doc](/learn/design-docs-and-rfcs) shows the options you weighed.
- When you interview others, ask for one specific time, then probe: "What did you do?" and "What happened next?"
- Score against the rubric, not your first impression. The re:Work guide warns about confirmation bias in interviews. [@rework-structured]
- When you mentor someone who is job hunting, help them find their stories. See [mentoring and sponsorship](/learn/mentoring-and-sponsorship).

## With AI

AI is a good practice partner. It must never write a story you did not live.

### What to ask the agent

> "Act as a behavioural interviewer for a senior backend role. Ask me one question. After my answer, ask two follow-up questions that test my own part. Then tell me which STAR part was weakest."

> "Here is my story as five bullets. Mark every 'we' where my own action is unclear. Tell me where a number is missing. Do not add any facts."

> "Here are my eight stories. Make a table of which themes each one covers: conflict, failure, ambiguity, influence, ownership, mentoring, technical call. Show the gaps."

### What to check yourself

- Every fact in the story is true. Remove anything the agent added.
- You can answer three follow-ups on each story without notes.
- Each story takes two to three minutes when you say it out loud with a timer.
- The failure story has a real cost and a real change.
- Claims about how a company hires match its current careers page.

## Check yourself

## Go deeper

Before this: [big-tech interview loops](/learn/big-tech-interview-loops) and [what senior really means](/learn/what-senior-really-means). For better stories at work: [reviewing human and AI code](/learn/reviewing-human-and-ai-code) and [idempotency, retries and timeouts](/learn/idempotency-retries-timeouts). Next: negotiation.
