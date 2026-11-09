---
topic: interview-loops
subtitle: Know the stages of a big-tech loop, what each round checks, how the decision and your level are made, and how to spend six to eight weeks getting ready.
status: published
reviewed_by: ai-factcheck
reviewed_on: 2026-11-08
illustrative_story: true
sources:
  - id: google-hire
    title: "Google Careers, “Our hiring process”"
    url: https://www.google.com/about/careers/applications/how-we-hire/
    note: Google's own steps, from resume and recruiter calls to a panel of structured interviews and the decision, with the usual timeline.
  - id: google-tips
    title: "Google Careers, “Interviewing at Google: Best practices, advice, and tips”"
    url: https://www.google.com/about/careers/applications/interview-tips/
    note: Advice on clarifying questions, showing your thinking, behavioural examples, and how long the decision can take.
  - id: meta-prep
    title: "Meta Careers, “Preparing for your software engineering interview at Meta” (updated 6 October 2022)"
    url: https://www.metacareers.com/blog/preparing-for-your-software-engineering-interview-at-meta/
    note: The screen and the full loop as Meta described them in 2022, with tips such as thinking out loud.
  - id: amazon-sde2
    title: "Amazon Jobs, “SDE II Interview Prep”"
    url: https://www.amazon.jobs/content/en/how-we-hire/sde-ii-interview-prep
    note: The steps for a mid-level software engineer role, the size of the loop, and what the coding, design and behavioural parts look for.
  - id: amazon-loop
    title: "Amazon Jobs, “Interview loop”"
    url: https://www.amazon.jobs/content/en/how-we-hire/interview-loop
    note: Amazon's short description of the loop, with links on its Leadership Principles and the STAR method.
  - id: amazon-bar-raiser
    title: "About Amazon, “What is an Amazon bar raiser?” (30 October 2019)"
    url: https://www.aboutamazon.com/news/workplace/amazon-bar-raiser
    note: What a bar raiser does in the loop and the debrief, and why one person does not decide alone.
  - id: gitlab-interview
    title: "The GitLab Handbook, “Conducting a GitLab Interview”"
    url: https://handbook.gitlab.com/handbook/hiring/conducting-a-gitlab-interview/
    note: The interviewer's side of a public hiring process, with scorecards, rules against bias and a level field.
  - id: hi-google-l5
    title: "Hello Interview, “Google L5 (Senior) Software Engineer Interview Guide”"
    url: https://www.hellointerview.com/guides/google/l5
    note: A third-party guide to Google's senior loop, the hiring committee and how the level is decided.
claims_checked:
  - claim: "As of 2026, Amazon's SDE II loop has four 55-minute interviews, the outcome comes within 5 business days, and each interviewer typically asks two or three behavioural questions tied to the Leadership Principles."
    source: amazon-sde2
    note: "SDE II Interview Prep page: 'four 55-minute interviews', 'Interview Outcome (within 5 business days)', 'two or three behavioral-based questions'. Online assessment 90 + 20 minutes also matches."
  - claim: "A bar raiser is a trained, objective third party who guides the debrief and makes the final decision with the hiring manager; a quoted bar raiser says it 'avoids having one person make the hiring decision'."
    source: amazon-bar-raiser
    note: "Article dated 30 October 2019. Quote is from Caley Anderson, a hiring manager and bar raiser. No veto power is mentioned."
  - claim: "GitLab interviewers (not hiring managers) cannot see each other's scorecards, 'to help remove bias'; scorecards are expected within 24 hours; engineering scorecards have a Level category that can score two levels."
    source: gitlab-interview
    note: "Handbook last modified 30 October 2026. Exact phrases found in Candidate Evaluation and Scorecards, and Level Categories in Engineering Scorecards."
  - claim: "Google lists one or two shorter recruiter conversations, a panel of interviews, the same rubrics for everyone, roughly 6-8 weeks once interviews start, and no AI tools in interviews."
    source: google-hire
    note: "Read from the page's embedded text. Google's page never names a hiring committee; that claim is credited to Hello Interview."
  - claim: "In 2022 Meta described a 45-minute screen with two coding problems and advised to think out loud and find and fix your own bugs."
    source: meta-prep
    note: "Post updated 6 October 2022 (first published 2016). Read with WebFetch."
  - claim: "Hello Interview says Google's hiring committee and behavioural interview carry the most weight for L5 level, and weak leadership signals can mean being down leveled."
    source: hi-google-l5
    note: "Third-party guide; the lesson credits it by name and limits it to Google."
quiz:
  - q: You get a coding problem with an unclear input. What is the best first move?
    options:
      - Start coding at once, to save time.
      - Ask one or two questions about the input and edge cases, then say your plan out loud.
      - Solve it in silence, then explain the full answer at the end.
    answer: 1
    why: Interviewers judge your process, not only the answer. Clarifying first and talking through the plan gives them evidence to write down.
  - q: Why do some companies ask interviewers to write feedback before they discuss the candidate?
    options:
      - So that one strong voice in the room does not change what others saw.
      - So the debrief can be skipped.
      - So the recruiter can choose the level alone.
    answer: 0
    why: Independent notes reduce bias. GitLab, for example, hides each interviewer's scorecard from the other interviewers for this reason.
  - q: You are a mid-level engineer aiming at a senior role. How should your prep change?
    options:
      - Only more coding problems, because coding decides everything.
      - More time on system design and behavioural stories, because they weigh more in setting a senior level.
      - No change. The loop is the same at every level.
    answer: 1
    why: Third-party guides report that at senior level, behaviour and design rounds weigh heavily in the level decision. Coding still has to be solid.
  - q: You did well in four rounds but got an offer one level lower than you hoped. What is the most likely reason?
    options:
      - The company made a mistake in the paperwork.
      - Some rounds showed strong skills, but not the signals the higher level needs, such as leading the design or showing leadership.
      - You asked too many questions.
    answer: 1
    why: The decision covers two things, hire and level. You can pass the bar for one level and miss it for the next.
incidents: []
related: [system-design-method, design-deep-dives, requirements-nfrs, what-senior-means, behavioural-interviews, negotiation]
---

## What broke

Arjun had three years of backend experience. He applied for a senior role at a large tech company. He spent six weeks on coding problems, about 200 of them. He did not practise anything else.

The phone screen went well. In the loop, the first coding round started with a vague problem. Arjun did not ask a single question. He typed in silence for 25 minutes. When the interviewer asked about his approach, he said, "Let me finish first."

The design round went worse. He drew boxes for 40 minutes, but never asked how many users the system had. In the behavioural round, his stories were short and vague. "We fixed the outage. It went fine."

Two weeks later the recruiter called. There was no offer at senior level. There was an offer one level lower. Arjun was surprised, because he had solved every coding problem. He had never asked which level he was being considered for, or how the rounds were weighted.

This story is made up, but each mistake in it is common. This lesson shows how the loop works, so your prep matches it.

## The idea in simple words

A big-tech interview loop is a set of separate interviews. Each interviewer checks a different skill. Then a group reads all the notes and makes one decision.

Amazon's careers site describes it plainly. You meet "individually with current employees" and each one assesses "different aspects of your skills and experience". [@amazon-loop]

The decision has two parts: hire or not, and at which level. Many candidates prepare only for the first part. The second part decides your title and your pay.

> [!key] **The key idea.** Each round is a separate test with its own notes. Find out the rounds and the level before you prepare. Then spend your time where the weight is, and make your thinking visible in every round.

## How it works

Step through one candidate's path. Use Next and Back, or the ← and → keys.

[[diagram:interview-loop-flow]]

### The usual stages

The names change between companies, but the shape is similar.

- **Recruiter call.** As of 2026, Google's page says you usually have "one or two shorter conversations" with a recruiter before deeper interviews. [@google-hire] This is the best moment to ask about the level and the rounds.
- **Technical screen.** In 2022, Meta described a 45-minute screen with two coding problems. [@meta-prep] Amazon's SDE II page instead describes an online assessment: 90 minutes for two technical questions, then 20 minutes of system design scenarios. [@amazon-sde2]
- **The loop.** As of 2026, Amazon's SDE II page says the loop has "four 55-minute interviews". [@amazon-sde2] Google describes "a panel of interviews", over video or in person. [@google-hire]
- **Decision and offer.** Amazon's SDE II page says the outcome comes within 5 business days. [@amazon-sde2] Google says the whole process often takes "roughly 6-8 weeks" once interviews start. [@google-hire]

### What each round looks for

**Coding.** Interviewers watch how you solve, not only what you produce. Meta's advice was to "Think out loud." [@meta-prep] Amazon asks for "syntactically correct code—no pseudo code". It also wants you to check edge cases and bad input. [@amazon-sde2]

**System design.** Amazon says to expect "at least one question" on design. It adds that you should ask questions to complete and check your design. [@amazon-sde2] The method is in [the system design interview method](/learn/system-design-interview-method). Going deep is in [design deep dives](/learn/design-deep-dives-trade-offs).

**Behavioural.** You tell real stories from past work. As of 2026, Amazon's SDE II page says each interviewer typically asks two or three such questions, tied to its Leadership Principles. [@amazon-sde2] Google suggests the STAR method to structure your examples. [@google-tips] A later lesson, behavioural interviews, covers this in depth.

**Domain knowledge.** Some loops add a round on your specialty. Hello Interview's Google L5 guide describes a "Role Related Knowledge" round. It replaces one coding round for candidates on a domain track, such as Android or ML. [@hi-google-l5]

### How the decision is made

Each interviewer writes feedback against a fixed rubric. Google says every candidate is assessed with "the same rubrics for everyone" for that role. [@google-hire]

Feedback is written alone, and early. As of 2026, GitLab's public handbook says interviewers cannot see each other's scorecards, "to help remove bias". Hiring managers can see them all. It asks for scorecards within 24 hours. [@gitlab-interview]

Then the notes meet. In a 2019 article, Amazon described its bar raisers. A bar raiser is a trained interviewer who acts as an objective third party and guides the debrief. The bar raiser and the hiring manager make the final decision. A bar raiser quoted in the article says the program "avoids having one person make the hiring decision". [@amazon-bar-raiser] Hello Interview's guide says Google uses a hiring committee that reviews all feedback before team matching. [@hi-google-l5]

### How the level is set

The level comes from the same notes. As of 2026, GitLab's handbook says its engineering scorecards have a "Level" field. An interviewer can score you at two levels, such as Senior and Intermediate. [@gitlab-interview]

So you can pass at one level and miss the next. Hello Interview says, for Google's senior level, the committee and the behavioural interview "carry the most weight" for the level. Weak leadership signals can mean being "down leveled". [@hi-google-l5]

### How prep differs for senior roles

For a senior role, coding must still be solid. Hello Interview reports that, at least at Google, the behavioural round weighs most for a senior level, and design matters a lot too. [@hi-google-l5] In design, you lead the discussion and choose where to go deep. In behaviour, your stories should show scope: a project you drove, a conflict you resolved, a decision you owned. See [what senior really means](/learn/what-senior-really-means).

### A realistic 6–8 week plan

This is our suggested plan, not advice from any company. Google's "6-8 weeks" above is how long its hiring process takes, not how long to prepare. This plan assumes about 1–2 hours on weekdays and more at weekends. Adjust the weights to your loop.

1. **Week 1.** Ask the recruiter for the round list, the level and the format. Do three timed problems to find your weak spots.
2. **Weeks 1–5.** Coding, most days. Practise common patterns. Solve in a plain editor, without running the code, and speak while you type.
3. **Weeks 2–6.** System design, two or three sessions a week. Do one full problem per session, starting with [requirements](/learn/requirements-and-non-functional-needs).
4. **Weeks 2–6.** Behavioural. Write 8–10 stories. Each needs a number and a clear "I", not "we".
5. **Weeks 5–7.** Mock interviews with a peer, at least one per round type. Ask for blunt notes.
6. **Final week.** Light review only. Check the video setup, the time zone and the schedule.

For a senior role, our own suggestion is to move about a third of the coding time to design and stories.

### Common mistakes

- **Silent coding.** The interviewer cannot write down thinking they did not hear.
- **No clarifying questions.** Google says many questions "leave room for clarification" on purpose. [@google-tips]
- **Not asking about the level.** You cannot aim at a bar you do not know.
- **Waiting for the interviewer to find your bugs.** In 2022, Meta advised finding and fixing them yourself. [@meta-prep]
- **Using AI tools in the interview.** As of 2026, Google says they "are not permitted" during interviews. [@google-hire]

### The honest limits

Every company's loop is different, and loops change. The pages here are snapshots, and Meta's is from 2022. Confirm the format with your recruiter. Outcomes are also noisy. One interviewer's bad day can change a result. Google itself says not getting a role "can often be a matter of timing". [@google-hire] One rejection is data, not a verdict.

## Trade-offs

| Prep strategy | Fits the loop? | Cost | Use it when |
|---|---|---|---|
| Mostly coding problems | Partly. | Design and behaviour rounds go unprepared. | Early-career roles where coding is most of the loop. |
| Weighted by the round list | Yes. | You must ask the recruiter first. | Almost always. |
| Many mock interviews | Yes. | Needs a partner and time. | The last 2–3 weeks. |
| A practice loop at a company you care less about | Yes. | Takes extra weeks, and the format may differ. | If you have not interviewed in years. |

**Simple rule:** ask for the rounds and the level, prepare by weight, and talk through your thinking every time.

## At work: how a senior uses this

Seniors also sit on the other side of the table. The same ideas apply there.

- When you interview, write your notes before you read anyone else's. Then the debrief hears what you saw.
- Write evidence, not feelings. "Found the off-by-one bug herself" is useful. "Seemed smart" is not.
- Score against the rubric for the level, not against the last candidate.
- In a debrief, say which level your evidence supports, and why.
- When you coach a teammate who is job hunting, ask first: "Which level, and which rounds?"
- When you move jobs yourself, use the same 6–8 week plan. Plan the negotiation before the offer arrives.

## With AI

### What to ask the agent

> "Act as an interviewer for a senior backend role. Give me one coding problem with a vague input. Do not answer my questions unless I ask them. At the end, list what I failed to clarify."

> "Here are my five behavioural stories. For each one, tell me if it shows my own actions, a number, and a result. Point out every place where I say 'we' instead of 'I'."

> "Make a 7-week prep plan for a loop with two coding rounds, one system design round and one behavioural round, at senior level. I have 90 minutes on weekdays."

### What to check yourself

- Any claim the agent makes about a company's current loop. Check it against the company's own careers page, or ask your recruiter.
- That mock feedback names specific moments, not general praise.
- That the plan gives design and stories enough weight for the level you want.
- That your stories are true. A made-up story falls apart under follow-up questions.

## Check yourself

## Go deeper

Before this: [what senior really means](/learn/what-senior-really-means). For the design round: [the system design interview method](/learn/system-design-interview-method), [design deep dives](/learn/design-deep-dives-trade-offs) and [requirements and non-functional needs](/learn/requirements-and-non-functional-needs). Next: [behavioural interviews and STAR stories](/learn/behavioural-interviews-star) and [negotiation and compensation](/learn/negotiation-and-compensation).
