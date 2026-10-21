---
topic: debugging-production
subtitle: When production breaks, first stop the harm, then find the cause with data, one hypothesis at a time.
status: published
illustrative_story: false
sources:
  - id: cf2025
    title: "Cloudflare, “Cloudflare outage on November 18, 2025” (official postmortem)"
    url: https://blog.cloudflare.com/18-november-2025-outage/
    note: The full timeline, the wrong first theory of a DDoS attack, and why the errors came and went.
  - id: sre-trouble
    title: "Chris Jones, “Effective Troubleshooting”, Google, Site Reliability Engineering, chapter 12"
    url: https://sre.google/sre-book/effective-troubleshooting/
    note: The troubleshooting loop, "stop the bleeding" first, recent changes, bisection and the common traps.
  - id: sre-emergency
    title: "Corey Adam Baye, “Emergency Response”, Google, Site Reliability Engineering, chapter 13"
    url: https://sre.google/sre-book/emergency-response/
    note: Real Google emergencies, including a config change that was fixed by rolling it back.
  - id: sre-incidents
    title: "Andrew Stribblehill, “Managing Incidents”, Google, Site Reliability Engineering, chapter 14"
    url: https://sre.google/sre-book/managing-incidents/
    note: Incident roles, the danger of people changing things on their own, and when to declare an incident.
  - id: sre-postmortem
    title: "John Lunney and Sue Lueder, “Postmortem Culture: Learning from Failure”, Google, Site Reliability Engineering, chapter 15"
    url: https://sre.google/sre-book/postmortem-culture/
    note: What a blameless postmortem is, when to write one, and how to make action items stick.
  - id: use
    title: "Brendan Gregg, “The USE Method”"
    url: https://www.brendangregg.com/usemethod.html
    note: A checklist for resources such as CPU, memory, disks and network.
  - id: red
    title: "Grafana Labs, “The RED Method: How to instrument your services” (Tom Wilkie's method)"
    url: https://grafana.com/blog/2018/08/02/the-red-method-how-to-instrument-your-services/
    note: Rate, errors and duration for every service, and how RED differs from USE.
  - id: otel-prop
    title: "OpenTelemetry documentation: Context propagation"
    url: https://opentelemetry.io/docs/concepts/context-propagation/
    note: How a trace ID moves from one service to the next in the traceparent header.
  - id: otel-logs
    title: "OpenTelemetry documentation: Logs"
    url: https://opentelemetry.io/docs/concepts/signals/logs/
    note: How log records carry the trace ID and span ID, so logs and traces line up.
  - id: pd-roles
    title: "PagerDuty Incident Response documentation: Different Roles"
    url: https://response.pagerduty.com/before/different_roles/
    note: What the incident commander, scribe, experts and liaisons each do.
  - id: pd-blameless
    title: "PagerDuty Postmortem documentation: The Blameless Postmortem"
    url: https://postmortems.pagerduty.com/culture/blameless/
    note: Why blame makes incidents longer, and how to ask questions without blame.
  - id: anthropic-skills
    title: "Anthropic, “How AI assistance impacts the formation of coding skills” (29 January 2026)"
    url: https://www.anthropic.com/news/AI-assistance-coding-skills
    note: A trial with 52 mostly junior engineers learning the Trio library. The AI group scored lower, most of all on debugging questions.
claims_checked:
  - claim: "Cloudflare, 18 Nov 2025: access change 11:05 UTC, core traffic failing from 11:20, first automated test alert 11:31, incident call 11:35, new file creation stopped 14:24, main impact resolved 14:30, all services back 17:06."
    source: cf2025
    note: Every time matches the postmortem timeline. Matches the incident card.
  - claim: "The team first suspected a DDoS (recent Aisuru attacks, and the off-Cloudflare status page went down by coincidence). Errors came and went because the feature file was rebuilt every five minutes on a ClickHouse cluster being updated gradually. Limit 200 features, about 60 in use, file doubled."
    source: cf2025
    note: Verified in the postmortem. Card agrees.
  - claim: "In a big outage, stop the bleeding first: 'Ignore that instinct!' Then use the hypothetico-deductive method, test likely causes first while weighing risk, take notes, and check what changed."
    source: sre-trouble
    note: Quotes checked on the chapter 12 page (Chris Jones). Evidence-saving advice moved from sre-emergency to sre-trouble, where it is.
  - claim: "Only the operations team should change the system during an incident; a freelancing engineer made an outage worse."
    source: sre-incidents
    note: Direct quote from chapter 14 (Andrew Stribblehill). Role duties also match.
  - claim: "RED = rate, errors, duration (Tom Wilkie); USE = utilization, saturation, errors for every resource."
    source: red
    note: Grafana post is by Julie Dam from Tom Wilkie's talk; the method is Wilkie's. USE quoted from Gregg's page.
  - claim: "Anthropic, 29 Jan 2026: 52 mostly junior engineers learned Trio; AI group 50% vs hand-coding 67% on the quiz; biggest gap on debugging questions; small sample, measured shortly after the task."
    source: anthropic-skills
    note: All numbers match the article. Removed "half" (group sizes not stated in the article).
reviewed_by: ai-factcheck
reviewed_on: 2026-10-21
quiz:
  - q: Checkout errors jump from 0.2% to 6% ten minutes after a deploy. What do you do first?
    options:
      - Read the new code until you find the bug.
      - Roll back the deploy, then investigate with the logs you saved.
      - Wait 30 minutes to see if the errors go away.
    answer: 1
    why: Stop the harm first. A rollback is fast and usually safe, and you can find the bug after users are fine.
  - q: Errors come only from servers running version 4.2. Servers on 4.1 are fine. What have you just done?
    options:
      - Bisected the problem by version.
      - Proved the root cause.
      - Used the USE method.
    answer: 0
    why: Splitting the traffic into two groups narrows the search. It does not yet prove the cause, but it tells you where to look.
  - q: Which method gives you rate, errors and duration for each service?
    options: ["The USE method", "The RED method", "A postmortem"]
    answer: 1
    why: RED is for services and their requests. USE checks utilization, saturation and errors for resources like CPU and disks.
  - q: During an incident, three engineers each try a different fix at the same time. Why is this a problem?
    options:
      - It is not a problem. More fixes means a faster recovery.
      - You cannot tell which change helped or hurt, and changes can make the outage worse.
      - It breaks the blameless rule.
    answer: 1
    why: Google's SRE book says only the operations team should change the system during an incident, so every change is known and coordinated.
  - q: An AI assistant reads your logs and says, "The root cause is a memory leak in the cache." What should you do?
    options:
      - Accept it. The AI read more logs than you can.
      - Ask which log lines prove it, then check them and test the idea yourself.
      - Restart the cache to fix it.
    answer: 1
    why: AI can summarise logs well but can also give a cause that sounds right and is wrong. Treat it as one hypothesis to test.
incidents: [cloudflare-2025-11-bot-management, roblox-2021-10-consul]
related: [observability-slos, safe-deploys, incident-response, distributed-tracing, feature-flags, skill-atrophy]
---

## What broke

On 18 November 2025 at 11:05 UTC, Cloudflare changed the access rights on one of its databases. From 11:20, its network began to fail to deliver core traffic. Many websites behind Cloudflare showed error pages. [@cf2025]

An automated test caught the problem at 11:31. At 11:35 the team opened an incident call. [@cf2025]

The team first thought it was a huge DDoS attack. Two things pointed that way. Cloudflare had recently fought a wave of very large attacks. And its status page, which runs outside Cloudflare, went down at the same time. That was a coincidence, but it made some people think an attacker was hitting both. [@cf2025]

The errors were strange, too. The network failed, recovered, then failed again. The real cause was a file of bot features that was rebuilt every five minutes. The database cluster was being updated step by step, so each run could make a good file or a bad one. [@cf2025]

The bad file came from the database change. A query started to return duplicate rows, so the file doubled in size. The proxy had a limit of 200 features and normally used about 60. The bigger file went over the limit, and the proxy failed. [@cf2025]

At 14:24 the team stopped making new files, and a test with an older file worked. By 14:30 core traffic was mostly flowing again. Every service was back at 17:06. [@cf2025] Note what misled them: a coincidence, and a symptom that came and went.

## The idea in simple words

In production, real users are hurting while you debug. So you have two goals, in this order:

1. **Stop the harm.** This is called mitigation.
2. **Find the cause.** Then fix it so it does not come back.

Google's SRE book says that in a big outage, you may want to start hunting for the root cause at once. Its advice: "Ignore that instinct!" Instead, make the system work as well as it can for now. [@sre-trouble]

After that, use a loop. Look at the data. Make one guess, called a hypothesis. Test it. If the test says no, make the next guess. The book calls this the hypothetico-deductive method. [@sre-trouble]

> [!key] **The key idea.** Mitigate first, debug second. Then let data, not the loudest opinion, choose between hypotheses, and test them one at a time.

## How it works

Step through one incident from the alert to the postmortem.

[[diagram:debug-incident-loop]]

### 1. Stop the bleeding

The fastest safe action is usually one of these:

- **Roll back** the last deploy or config change. In one Google emergency, an engineer pushed another config change to roll back the first one, and services began to recover. [@sre-emergency]
- **Turn off a feature flag** for the new code path.
- **Shed load or move traffic.** The SRE book gives examples: send traffic away from a broken cluster, or turn off subsystems to reduce load. [@sre-trouble]

Fast action can still save evidence, such as logs. [@sre-trouble] Take a minute to save logs, a heap dump, or one bad host out of traffic. If you feel overwhelmed, pull in more people. [@sre-emergency]

### 2. Ask "what changed?"

A system tends to keep running until something acts on it, such as a config change or a new kind of load. So start with recent changes. [@sre-trouble] Check four kinds:

- **Deploys:** code, images, OS updates.
- **Config:** settings, feature flags, firewall rules, generated data files.
- **Traffic:** a spike, a new customer, a bot, a new client version.
- **Dependencies:** databases, caches, cloud services, other teams' APIs.

In the Cloudflare story, the change was in a database, not in the proxy that failed. [@cf2025] So look beyond your own service.

### 3. Read the right signals

The [observability lesson](/learn/observability-slos-error-budgets) covers metrics, logs and traces. Two short checklists tell you where to look first.

The **RED method** is for services. For each one, check **rate** (requests per second), **errors** (how many fail) and **duration** (how long they take). [@red]

The **USE method** is for resources like CPU, memory, disks and network. For every resource, check **utilization**, **saturation** and **errors**. [@use] Saturation means extra work the resource cannot handle yet, often waiting in a queue. [@use]

Tom Wilkie, who created RED, says RED cares about how happy your users are, and USE about how happy your machines are. [@red] Start with RED to find the service that hurts, then use USE on its machines.

### 4. Test one hypothesis at a time

Write each hypothesis down, for example: "The new connection pool is too small." Say what data would prove it wrong.

Test the most likely idea first, and think about risk to the system: "when you hear hoofbeats, think of horses not zebras." [@sre-trouble]

Take notes on each idea, each test and each result. Do not ignore a clear "no": negative results are useful too. [@sre-trouble]

### 5. Narrow it down

Split the problem in two, again and again. The SRE book calls this divide and conquer, or bisection. [@sre-trouble] You can split by:

- **Time:** what happened in the minute before the first error?
- **Region or zone:** is one region bad and the others fine?
- **Version:** do errors come only from hosts on the new build?
- **Tenant:** is it one customer, one card type, one app version?

Each good split roughly halves the search.

### 6. Follow one request

Metrics tell you *that* something is wrong. A trace and its logs show *what* happened to one request. When service A calls service B, A sends a trace ID and a span ID with the call, in the W3C `traceparent` header. [@otel-prop] OpenTelemetry can add these IDs to your log records. [@otel-logs] The trace ID then works as a **correlation ID**: search for it and see that request's logs in every service.

### Safe experiments in production

Sometimes you must test in production. Change one thing, on one host or a canary, and know how to undo it. Such tests can also change the results of the next test. [@sre-trouble]

### Common traps

The SRE book lists traps that send teams the wrong way. [@sre-trouble]

- **Wrong symptoms:** looking at metrics that do not matter, or misreading what a metric means.
- **Old causes:** "it happened once, so it must be happening again."
- **Coincidences:** two things that move together by chance.

Add two more. **Confirmation bias** means you see only the data that fits your first idea. **Chasing the loudest symptom** means you fix the error that fills the logs, not the one that came first.

The worst trap is many people changing things at once. Google tells of an engineer whose change, made without telling anyone, made an outage worse. Its rule: only the operations team changes the system during an incident. [@sre-incidents]

### Incident roles

Google uses clear roles. [@sre-incidents]

- The **incident commander** holds the overall picture and gives out the work.
- **Operations** makes the changes.
- **Communication** sends regular updates to people outside the call.
- **Planning** handles longer jobs, like filing bugs and handing over to the next shift.

PagerDuty adds: "the Incident Commander is NOT a resolver." [@pd-roles] The commander leads; experts fix.

### The blameless postmortem

A **postmortem** is a written record of an incident: its impact, the actions taken, the root causes and the follow-up steps. [@sre-postmortem] A blameless postmortem looks for the contributing causes "without indicting any individual or team." [@sre-postmortem] As Google puts it, you cannot fix people, but you can fix systems and processes. [@sre-postmortem]

Blame makes engineers stay silent, and silence makes incidents longer. [@pd-blameless] So ask "what" and "how", not "who" or "why". [@pd-blameless]

Cloudflare's follow-up list is a good example: check its own generated config files like user input, and add more global kill switches. [@cf2025]

## Trade-offs

| Option | Speed | Risk | Use it when |
|---|---|---|---|
| Roll back | Yes. Minutes, if automated. | Low, unless data or schema changed too. | A change lines up with the start of the errors. |
| Roll forward (ship a fix) | No. Write, review, deploy. | Medium. A rushed fix can add a bug. | Rollback is impossible, like after a one-way migration. |
| Turn off a feature flag | Yes. Seconds. | Low, if the "off" path was tested. | The new code is behind a flag. |
| Shed load or move traffic | Yes. Minutes. | Medium. Some users fail on purpose. | Overload, or one region is broken. |
| Wait and investigate | No. Users keep failing. | High for users. | Impact is small and stable, and quick actions could make it worse. |

**Simple rule:** if a change lines up with the start of the errors, undo it first and ask questions later.

## At work: how a senior uses this

- Ask in the first five minutes: "Are users hurting? What changed in the last hour? How fast can we undo it?"
- Make rollback and flag-off a single, tested command. Practise it on a quiet day.
- Keep one timeline in the incident channel: each change, hypothesis and test, with the time.
- Say hypotheses as "I think X, because Y. If I am right, we will see Z." When a theory fits too well, look for data that would break it.
- Write the postmortem within a few days. Name systems and gaps, not people. Give every action item an owner and a date.

## With AI

### What to ask the agent

> "Here are 2,000 log lines from the checkout service between 14:00 and 14:20. Group the errors by type. For each group, give the count, the first time it appeared and three example trace IDs."

> "Here is our deploy log and config history for the last 24 hours. List every change that happened in the 30 minutes before 14:05."

> "Draft a blameless postmortem from this incident channel export. Use 'the on-call engineer', not names. Mark every claim you could not find in the export."

### What to check yourself

- **Every cause the AI gives is only a hypothesis.** It can sound sure and still be wrong. Ask for the exact log lines, then open them yourself.
- The counts and times match your dashboards. Summaries can drop or merge errors.
- The AI saw the whole time window, not just the part that fit in its context.
- No secrets or personal data went into the prompt.
- You still do the reasoning. In Anthropic's January 2026 trial, 52 mostly junior engineers learned a new Python library, Trio. One group had an AI assistant. The AI group scored 50% on the quiz and the hand-coding group 67%. The biggest gap was on debugging questions. [@anthropic-skills] People who asked for explanations and conceptual answers learned more. [@anthropic-skills] The study was small and tested learning only shortly after the task. [@anthropic-skills]

## Check yourself

## Go deeper

Related lessons: [Safe deploys and config changes](/learn/safe-deploys-config-changes), [Incident response and postmortems](/learn/incident-response-and-postmortems) and [Skill atrophy and deliberate practice](/learn/skill-atrophy-deliberate-practice).
