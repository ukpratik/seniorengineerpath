---
topic: mentoring
subtitle: Grow other engineers by asking more than you tell, and use your name to open doors for them.
status: published
reviewed_by: ai-factcheck
reviewed_on: 2026-10-23
illustrative_story: true
sources:
  - id: batra
    title: "Neha Batra, “Mentor, coach, sponsor: a guide to developing engineers”, LeadDev (November 2020)"
    url: https://leaddev.com/mentoring-coaching-feedback/mentor-coach-sponsor-guide-developing-engineers
    note: Clear one-line definitions of mentoring, coaching and sponsoring, with example coaching questions from real 1:1s.
  - id: hogan
    title: "Lara Hogan, “What does sponsorship look like?” (May 2017)"
    url: https://larahogan.me/blog/what-sponsorship-looks-like/
    note: A list of small, concrete ways to sponsor someone, and why opportunity often matters more than advice.
  - id: ccl-sbi
    title: "Center for Creative Leadership, “Use Situation-Behavior-Impact (SBI) to Understand Intent”"
    url: https://www.ccl.org/articles/leading-effectively-articles/closing-the-gap-between-intent-vs-impact-sbii/
    note: The SBI feedback model, step by step, plus a fourth step that asks about the other person's intent.
  - id: candor
    title: "Radical Candor, “Our approach”"
    url: https://www.radicalcandor.com/our-approach/
    note: Care personally and challenge directly at the same time, and the three patterns that miss this balance.
  - id: g-comments
    title: "Google, “How to write code review comments”, Google Engineering Practices"
    url: https://google.github.io/eng-practices/review/reviewer/comments.html
    note: Why review is also for growing skills, and when to point out a problem instead of giving the fix.
  - id: g-looking
    title: "Google, “What to look for in a code review”, Google Engineering Practices"
    url: https://google.github.io/eng-practices/review/reviewer/looking-for.html
    note: The "Good Things" section on praise as a mentoring tool.
  - id: dbx-ic3
    title: "Dropbox, Engineering Career Framework, IC3 Software Engineer"
    url: https://dropbox.github.io/dbx-career-framework/ic3_software_engineer.html
    note: A real ladder level. See the Mentorship and Team Development lines.
  - id: dbx-ic4
    title: "Dropbox, Engineering Career Framework, IC4 Software Engineer"
    url: https://dropbox.github.io/dbx-career-framework/ic4_software_engineer.html
    note: The next level up. Compare the feedback and culture lines with IC3.
  - id: circleci
    title: "CircleCI, “Why we re-designed our engineering career paths at CircleCI”"
    url: https://circleci.com/blog/why-we-re-designed-our-engineering-career-paths-at-circleci/
    note: How the focus of an engineer's work shifts toward guiding others, with a link to CircleCI's competency matrix.
  - id: anthropic-skills
    title: "Anthropic, “How AI assistance impacts the formation of coding skills” (January 2026)"
    url: https://www.anthropic.com/research/AI-assistance-coding-skills
    note: A randomized study on learning a new library with and without AI help. Read the limits section.
claims_checked:
  - claim: "Mentoring is sharing your experience; coaching is asking the right questions to help an engineer reach a solution that works for them; sponsoring is giving stretch projects or opportunities."
    source: batra
    note: "LeadDev, 30 Nov 2020, Neha Batra (former VP Engineering, GitHub). Coaching and sponsoring definitions quoted exactly; mentoring paraphrased. Example questions and 'What do you think?' match. 'Biggest missed opportunity in leadership' refers to sponsoring. Leans to mentoring when time, connection or trust drop."
  - claim: "Sponsorship is feeling on the hook to help someone get promoted; it is not just advice; underrepresented people often need opportunity and visibility, not advice."
    source: hogan
    note: "larahogan.me, 14 May 2017: all three quotes exact. Examples include suggesting someone as postmortem facilitator, raising names when work is assigned, asking them to give a talk on their work, sharing their work in Slack."
  - claim: "SBI is Situation, Behavior, Impact, with a fourth step that asks about intent; 'You interrupted me', not 'You were rude'."
    source: ccl-sbi
    note: "CCL article: Situation, observable Behavior, Impact; Intent step with 'What were you hoping to accomplish with that?'. Interrupted vs rude example is CCL's. Removed the 'camera' wording, which is not in the source."
  - claim: "Radical Candor is caring personally and challenging directly at the same time; caring but staying quiet is ruinous empathy."
    source: candor
    note: "radicalcandor.com/our-approach: quadrants Radical Candor, Ruinous Empathy, Obnoxious Aggression, Manipulative Insincerity."
  - claim: "Pointing out problems often helps the developer learn, but direct instructions or code are sometimes more helpful; a secondary goal of review is improving developers' skills."
    source: g-comments
    note: "Google eng-practices, comments.html: both quotes exact; 'A secondary goal is improving the skills of developers so that they require less and less review over time.'"
  - claim: "Telling a developer what they did right is sometimes more valuable for mentoring than what they did wrong."
    source: g-looking
    note: "Google eng-practices, looking-for.html, 'Good Things' section."
  - claim: "Dropbox IC3 and IC4 both say 'I actively level up less-experienced members of my team by helping them with their craft'; IC3 says 'I actively look for opportunities to mentor new hires, interns and apprentices'."
    source: dbx-ic3
    note: "Both pages checked (Impact Levers, Mentorship line). The new-hires line is in IC3 Talent > Team Development; IC4 does not have it."
  - claim: "Engineers' focus shifts from execution to include facilitating, guiding, and mentoring others."
    source: circleci
    note: "CircleCI blog, 'Even junior engineers can be leaders' section: exact quote. Post links its E1-E6 competency matrix."
  - claim: "In a randomized study of 52 mostly junior engineers learning Trio, the AI group scored 50% vs 67%, with the biggest gap in debugging; explanation-seeking patterns scored 65% or more, delegation patterns under 40%; small sample, immediate quiz."
    source: anthropic-skills
    note: "Anthropic, 29 Jan 2026. Pattern groups had n=2 to 7 and the authors say the qualitative analysis does not show cause; added that caveat. Manager advice quote exact. Speed gain (~2 min) not significant."
quiz:
  - q: Your mentee asks how to fix a failing test. Which reply is coaching, not mentoring?
    options:
      - '"Here is how I fixed the same bug last year."'
      - '"What does the error tell you, and what have you tried so far?"'
      - '"I will ask the tech lead to give you the next big project."'
    answer: 1
    why: Coaching asks questions so the person finds their own answer. Sharing your experience is mentoring. Creating an opportunity is sponsoring.
  - q: Which of these is sponsorship?
    options:
      - Reviewing your mentee's pull request carefully.
      - Suggesting your mentee's name to lead the next postmortem, and saying why they are ready.
      - Sending your mentee a good book on system design.
    answer: 1
    why: Sponsorship uses your influence to create an opportunity. Hogan lists suggesting someone to lead a meeting, such as a postmortem, as one example.
  - q: Which feedback follows the SBI model?
    options:
      - '"You are careless with deploys."'
      - '"In Tuesday''s deploy, you skipped the canary step. Errors reached all users for ten minutes."'
      - '"Great job, keep it up!"'
    answer: 1
    why: SBI names the situation, the observable behaviour and its impact. "Careless" is a judgement, not a behaviour, and "great job" gives nothing to learn from.
  - q: You give your mentee a stretch task. What makes it safe?
    options:
      - You quietly redo the work if it goes wrong.
      - Agreed check-in points, a reversible way to ship, and a clear "call me if" rule.
      - A deadline far in the future, so there is no pressure.
    answer: 1
    why: A safety net limits the damage of a mistake while the mentee still owns the work and the decisions.
  - q: Your mentee uses an AI agent for every task and ships fast. What is the best next step?
    options:
      - Nothing. Speed is all that matters.
      - Ban AI tools for them.
      - Ask them to explain a recent diff line by line, and pick some tasks where they write and debug the code first.
    answer: 2
    why: In Anthropic's study, the AI group scored lower on a quiz, mostly on debugging, and people who handed the work to AI scored lowest. Explaining code and doing some work by hand supports learning.
incidents: []
related: [what-senior-means, code-review, coding-agents, influence-without-authority, promotion, technical-writing, skill-atrophy]
---

## What broke

This story is made up, but it happens on many teams.

Meera was the senior engineer everyone asked. Karan, a new engineer on her team, asked her about ten questions a day. She was kind and fast. She would roll her chair over, take the keyboard and fix the problem in two minutes.

Karan shipped a lot. In his first six months, he merged more pull requests than anyone else. Meera thought she was a good mentor.

Then Meera took two weeks of leave. The payment retry job Karan "owned" started to send double emails. Karan opened the code. Most of the tricky parts were lines Meera had typed. He did not know why they were there. The fix took him three days, and he was afraid to touch the code the whole time.

At her own review, Meera's manager asked one question: "Who can do your work when you are away?" She had no good answer. She had helped Karan finish tasks. She had not helped him grow.

## The idea in simple words

There are three different ways to help someone grow. Neha Batra, a former engineering leader at GitHub, gives short definitions: [@batra]

- **Mentoring** is sharing your own experience, so the engineer can use it.
- **Coaching** is "asking the right questions to help an engineer reach a solution that works for them".
- **Sponsoring** is "giving stretch projects or opportunities to help an engineer grow".

Most of us only give advice, because it is quick.

Sponsorship is the one people forget. Lara Hogan describes it as feeling "on the hook" to help someone get promoted. She adds that it "is not just giving advice and mentorship". [@hogan] She writes that what people from groups that are less represented in tech often need most is "opportunity and visibility, not advice". [@hogan] Batra calls failing to sponsor "the biggest missed opportunity in leadership". [@batra]

> [!key] **The key idea.** Advice helps someone finish today's task. Questions help them think on their own. Sponsorship gives them a chance to show what they can do. A good mentor uses all three.

## How it works

Step through one full mentoring cycle with Next and Back, or the ← and → keys.

[[diagram:mentoring-cycle]]

### Run 1:1s the mentee owns

A 1:1 is a regular private talk between two people. Meet every week or two. Let the mentee bring the list of topics. If they come with nothing, that is a sign to ask more, not to fill the time with your stories. (This is our advice.)

A simple way to start is to ask, "What do you think?" Batra used this question when she first added coaching to her 1:1s. [@batra] Her article gives more examples. [@batra] One is "What do you think you're doing well in and what do you think needs improvement?" Another is "Given your past few projects, what did you learn that could help someone like you six months ago?" [@batra]

### Ask before you answer

When your mentee is stuck, try questions first:

- "What did you expect to happen, and what happened?"
- "What have you ruled out?"
- "Where would you look next, and why?"

Give the answer when time is short, the risk is high, or the mentee is upset. Batra says she leans toward mentoring, not coaching, when time, connection or trust are low. [@batra] Google's review guide makes the same point about code. Pointing out a problem and letting the developer decide "often helps the developer learn". But sometimes direct instructions or even code are more helpful. [@g-comments]

### Give feedback with SBI

The Center for Creative Leadership teaches the SBI model. [@ccl-sbi]

1. **Situation.** Say when and where. "In this morning's design review…"
2. **Behaviour.** Describe what the person did that you could see or hear, with no judgement. "You interrupted me", not "You were rude". [@ccl-sbi]
3. **Impact.** Say what it caused.

CCL adds a fourth step: ask about intent. "What were you hoping to accomplish with that?" [@ccl-sbi] This turns feedback into a conversation. Your guess about *why* they did it may be wrong.

Radical Candor gives one more test: care personally and challenge directly, at the same time. [@candor] Its name for feedback that is kind but never honest is "ruinous empathy". [@candor] Holding back a hard truth to be nice does not help anyone grow.

Praise counts as feedback too. Google's guide says it is sometimes more valuable for mentoring to tell a developer what they did right. [@g-looking] Use SBI for praise as well, so the person knows exactly what to repeat.

### Delegate stretch work with a safety net

A stretch assignment is work just above someone's current level. It can help someone grow fast, but it is also an easy way to fail in public. So build a safety net before you hand it over (this list is our advice):

- **Agree on the goal and the limits.** What does "done" mean? What may they decide alone?
- **Set check-in points.** For example, review the design before any code, and review the rollout plan before launch.
- **Keep it reversible.** Ship behind a feature flag, so a mistake can be turned off.
- **Say when to call you.** "If customer data might be wrong, call me at once."
- **Do not take the keyboard.** If they go the wrong way, ask a question. Step in only to prevent real harm.

### Use pairing and review to teach

Pairing works best when the mentee types and you talk. If you type, they only watch. (This is our advice.)

Code review is also a teaching tool. Google's guide says a secondary goal of review is "improving the skills of developers so that they require less and less review over time". [@g-comments] For how to review well, see [Reviewing human and AI code](/learn/reviewing-human-and-ai-code).

### Write it down once

If three people ask you the same question, write the answer down. Next time, send a link and spend your time on the follow-up questions. One page can help many people at once, including people you will never meet. (This is our advice. See [Clear technical writing](/learn/clear-technical-writing).)

### Why this counts for your level

Career ladders write this down as an expectation. Dropbox's IC3 and IC4 levels both say: "I actively level up less-experienced members of my team by helping them with their craft." [@dbx-ic3] [@dbx-ic4] IC3 also says: "I actively look for opportunities to mentor new hires, interns and apprentices." [@dbx-ic3] CircleCI writes that as engineers grow, their focus shifts "from execution to include facilitating, guiding, and mentoring others". [@circleci]

So growing others is part of the job at senior level, not extra work. See [What senior really means](/learn/what-senior-really-means).

## Trade-offs

| Way to help | Fast today? | Builds independence? | Best when |
|---|---|---|---|
| Give the answer (mentoring) | Yes. The task moves at once. | No, not on its own. | Time is short, risk is high, or the mentee is new to the topic. |
| Ask questions (coaching) | No. It takes longer. | Yes. They learn to find answers. | Trust is strong and the cost of a slow answer is low. |
| Sponsor them | No. Results take months. | Yes. They get real chances and visibility. | They are close to ready for the next level. |
| Take over the work | Yes. | No. It teaches them to wait for you. | Only in a live incident with real customer harm. |

**Simple rule:** answer the question, but ask one question back each time.

## At work: how a senior uses this

- Before you answer, ask: **"Do they need the answer, or do they need to learn to find it?"**
- Keep a short note per mentee: their goal, the last feedback you gave, and the next stretch task.
- In each 1:1, let them set the agenda. Ask at least one open question before you give advice.
- Give feedback within a day or two, using situation, behaviour and impact. Ask about intent before you decide why.
- Sponsor in small ways every week. Name your mentee when work is assigned. Ask them to present their own project. Credit them by name in public. [@hogan]
- When you get the same question twice, write the answer down and share the link.
- Ask yourself every quarter: who can run each system I own when I am away? If the answer is "nobody", start there.

## With AI

Juniors can now ship working code with an agent before they understand it. That makes mentoring more important.

Anthropic ran a randomized study with 52 mostly junior engineers learning a new Python library. The group with AI help scored 50% on a quiz afterwards. The group without AI scored 67%. The biggest gap was in debugging. [@anthropic-skills] People who asked the AI for explanations or concepts scored 65% or more. People who handed over the work scored under 40%. These groups were tiny, and the authors say the patterns do not prove cause. [@anthropic-skills] The sample was small and the quiz came right after the task. [@anthropic-skills]

The authors advise managers to build in "systems or intentional design choices that ensure engineers continue to learn as they work". [@anthropic-skills] For a mentor, that means:

- Ask your mentee to explain their agent's diff, line by line, before you review it.
- For some tasks, ask them to write and debug the code first, then compare with the agent.
- Review their prompts and plans, not only their code. See [Working with coding agents](/learn/working-with-coding-agents).

### What to ask the agent

> "I am mentoring a junior engineer on this pull request. List three questions I could ask them that would help them find the bugs themselves. Do not tell me the bugs."

> "Turn these notes from a 1:1 into feedback using situation, behaviour and impact. Remove any judgement words and keep it under 100 words."

> "My mentee keeps asking how our deploy works. Draft a one-page guide from this pipeline file, and list anything you are unsure about."

### What to check yourself

- The feedback describes behaviour you saw yourself. An agent cannot know what happened in the room.
- The questions fit this person. You know their level, and the agent does not.
- Every fact in the drafted guide is true for your system. Test the steps before you share it.
- The sponsorship is real. Only you can say a name in the meeting where work is assigned.

## Check yourself

## Go deeper

Before this: [What senior really means](/learn/what-senior-really-means). Next: [Influence without authority](/learn/influence-without-authority) and [Getting promoted](/learn/getting-promoted-brag-document).
