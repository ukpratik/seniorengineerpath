---
topic: incident-response
subtitle: Run an incident with one leader, one channel and regular updates, then write a blameless postmortem whose fixes really get done.
status: published
illustrative_story: false
sources:
  - id: atl-pir
    title: "Atlassian, “Post-Incident Review on the Atlassian April 2022 outage” (official review)"
    url: https://www.atlassian.com/engineering/post-incident-review-april-2022-outage
    note: The full timeline of the outage, and an honest section on what went wrong in talking to customers.
  - id: sre-incidents
    title: "Andrew Stribblehill, “Managing Incidents”, Google, Site Reliability Engineering, chapter 14"
    url: https://sre.google/sre-book/managing-incidents/
    note: Incident roles, the living incident document, clear handoffs, and when to declare an incident.
  - id: wb-response
    title: "Google, “Incident Response”, The Site Reliability Workbook, chapter 9"
    url: https://sre.google/workbook/incident-response/
    note: The three Cs, case studies from Google and PagerDuty, rotating responders, and drills.
  - id: sre-postmortem
    title: "John Lunney and Sue Lueder, “Postmortem Culture: Learning from Failure”, Google, Site Reliability Engineering, chapter 15"
    url: https://sre.google/sre-book/postmortem-culture/
    note: When to write a postmortem, what blameless means, and how Google reviews and shares them.
  - id: wb-postmortem
    title: "Google, “Postmortem Culture: Learning from Failure”, The Site Reliability Workbook, chapter 10"
    url: https://sre.google/workbook/postmortem-culture/
    note: A bad and a good example postmortem, side by side, and how to make action items close.
  - id: sre-oncall
    title: "Andrea Spadaccini, “Being On-Call”, Google, Site Reliability Engineering, chapter 11"
    url: https://sre.google/sre-book/being-on-call/
    note: Limits on incidents per shift, team size for a rotation, and why every page must be actionable.
  - id: pd-sev
    title: "PagerDuty Incident Response documentation: Severity Levels"
    url: https://response.pagerduty.com/before/severity_levels/
    note: Five severity levels with examples, and the rule to pick the higher level when unsure.
  - id: pd-roles
    title: "PagerDuty Incident Response documentation: Different Roles"
    url: https://response.pagerduty.com/before/different_roles/
    note: What the incident commander, deputy, scribe, experts and liaisons each do.
  - id: pd-during
    title: "PagerDuty Incident Response documentation: During an Incident"
    url: https://response.pagerduty.com/during/during_an_incident/
    note: Step-by-step duties for the incident commander, from the first page to ending the call.
  - id: pd-pm
    title: "PagerDuty Postmortem documentation: Step by Step"
    url: https://postmortems.pagerduty.com/how_to_write/writing/
    note: Who owns the postmortem, when to meet, contributing factors, and how to word action items.
  - id: atl-response
    title: "Atlassian Incident Management Handbook: How we respond to an incident"
    url: https://www.atlassian.com/incident-management/handbook/incident-response
    note: Atlassian's steps from detection to resolution, with templates for internal and external updates.
  - id: atl-pm
    title: "Atlassian Incident Management Handbook: Postmortems"
    url: https://www.atlassian.com/incident-management/handbook/postmortems
    note: Postmortem owners and approvers, "priority actions" with a 4 or 8 week deadline, and proximate against root causes.
claims_checked:
  - claim: "5 April 2022, 07:38 UTC: a script deleted 883 sites (775 customers); first ticket 07:46; major incident process 08:17, Critical 08:24; first Statuspage update 09:03; up to 14 days of outage for some customers; first broad external message (a tweet) 7 April 00:56 UTC."
    source: atl-pir
    note: Every number and time matches the review. "Almost two days" replaced with the exact gap (about 41 hours). 450+ support engineers, leaders meeting every three hours, lost contact details and the "broader communications much earlier" quote also checked.
  - claim: "PagerDuty's internal liaison sends updates to the executive team roughly every 30 minutes."
    source: pd-during
    note: The 30-minute cadence is for the internal liaison's updates to executives only. PagerDuty states no cadence for customer updates, so the lesson now labels any customer rhythm as its own advice.
  - claim: "Declare an incident if a second team is needed, customers can see it, or it is unsolved after an hour; ops is the only group changing the system; handoff with 'You're now the incident commander, okay?'"
    source: sre-incidents
    note: All three tests, the ops rule and the handoff quote match chapter 14 (Andrew Stribblehill).
  - claim: "At most 2 incidents per 12-hour shift (about 6 hours each); at least 8 engineers on a single-site rotation, 6 per site on two sites; all paging alerts should be actionable."
    source: sre-oncall
    note: Matches chapter 11 (Andrea Spadaccini).
  - claim: "PagerDuty schedules the postmortem meeting within 3 calendar days for a Sev-1 and 5 business days for a Sev-2; no single root cause; action items start with a verb and are narrow."
    source: pd-pm
    note: The deadline is for scheduling the meeting, not holding it; wording fixed.
  - claim: "Atlassian priority actions have an SLO of 4 or 8 weeks, and managers approve postmortems; root cause is the place in the chain where a change prevents the whole class of incident."
    source: atl-pm
    note: Matches the handbook page.
reviewed_by: ai-factcheck
reviewed_on: 2026-10-21
quiz:
  - q: A small part of checkout fails for some users. Nobody is sure yet how big the problem is. What should you do?
    options:
      - Wait an hour and see if it grows before you tell anyone.
      - Declare an incident now. You can lower the severity later.
      - Fix it yourself quietly, so you do not worry anyone.
    answer: 1
    why: Google advises declaring incidents early, because managed incidents are resolved faster. PagerDuty says to pick the higher severity when unsure.
  - q: During a big incident, what should the incident commander mostly do?
    options:
      - Write the fix, because they are the most senior engineer.
      - Hold the big picture, give out the work and make decisions, while others make the changes.
      - Send all customer updates personally.
    answer: 1
    why: PagerDuty says the incident commander is not a resolver and gives out the repair work. In Google's model, only the operations team changes the system.
  - q: It is 30 minutes after your last internal update and you have learned nothing new. What do you send?
    options:
      - Nothing. Wait until you have news.
      - '"We are still investigating. The cause is not known yet. Next update at 15:30."'
      - A guess at the cause, so the update sounds useful.
    answer: 1
    why: Atlassian's guide says to name the unknowns and to say when and where the next update will come. Without updates, people jump to their own conclusions.
  - q: Which postmortem action item is written best?
    options:
      - '"Be more careful with deploys."'
      - '"Add a check that blocks a bulk delete of more than 10 sites. Owner: Priya. Due: 15 May. Ticket OPS-412."'
      - '"Look into the database problem."'
    answer: 1
    why: PagerDuty asks for a narrow item that starts with a verb. Google asks for an owner, a tracking ticket, a priority and an end state you can check.
  - q: Your on-call engineer was paged nine times last night, mostly for alerts that needed no action. What is the senior response?
    options:
      - Ask them to be tougher. On-call is hard.
      - Fix or delete the noisy alerts, because every page should need a human to act.
      - Add a second engineer so each one gets fewer pages.
    answer: 1
    why: Google's SRE book says all paging alerts should be actionable, and noisy alerts should move toward one alert per incident.
incidents: [atlassian-2022-04-site-deletion]
related: [debugging-production, alerting-oncall, observability-slos, chaos-engineering, technical-writing]
---

## What broke

On 5 April 2022 at 07:38 UTC, a script at Atlassian started to delete customer sites. It was meant to remove one old app. By mistake, it was given the IDs of whole sites. It deleted 883 sites, which belonged to 775 customers. [@atl-pir]

The first customer support ticket came at 07:46 UTC. Atlassian started its major incident process at 08:17 and raised the incident to Critical at 08:24. The first status page update went out at 09:03. [@atl-pir] So far, the response was fast.

Then it got hard. For some customers, the outage lasted up to 14 days. [@atl-pir] Leaders met every three hours, and a global team worked 24/7. More than 450 support engineers checked restored sites in shifts. [@atl-pir]

The talking part went badly. The script had also deleted customer contact details, so Atlassian could not reach many of the customers who were hurt. [@atl-pir] Its first broad public message, a tweet, came on 7 April at 00:56 UTC. That was about 41 hours after the script started. [@atl-pir]

In its review, Atlassian wrote that it "should have implemented broader communications much earlier". It also said it should have admitted sooner that it was not sure when sites would be back. Its fixes included 24/7 backups for each communications role. [@atl-pir]

Fixing the system is only half of an incident. The other half is people.

## The idea in simple words

An incident is a problem too big for one person working alone. The [debugging lesson](/learn/debugging-production) covers how to find and stop the cause. This lesson covers how the team works together, and how it learns afterwards.

Google's SRE workbook names three goals, the "three Cs". **Coordinate** the work. **Communicate** with responders, the company and the outside world. **Control** the response. [@wb-response]

> [!key] **The key idea.** An incident needs one leader, one place to talk and a steady rhythm of updates. Afterwards, it needs a postmortem that blames no one, and fixes that really get done.

## How it works

[[diagram:incident-lifecycle]]

### 1. Declare early

Google's advice is: "Declare incidents early and often." In its experience, managed incidents are resolved faster. [@wb-response] The SRE book gives three tests. If any one is true, it is an incident: [@sre-incidents]

- You need a second team to help fix it.
- Customers can see the problem.
- It is still not solved after an hour of focused work.

### 2. Pick a severity level

A **severity level** tells everyone how bad it is and how to respond. PagerDuty uses five levels. SEV-1 and SEV-2 are major incidents. SEV-5 is a cosmetic bug that becomes a ticket. [@pd-sev] Atlassian pages people for severity 1 and 2. Severity 3 goes to the team during work hours. [@atl-response]

When you are not sure, PagerDuty says to treat it as the higher level. Do not argue about the level during the incident; review it in the postmortem. [@pd-sev]

### 3. Give out roles, with one leader

Google's model has four roles: command, operations, communication and planning. [@sre-incidents] PagerDuty adds a **scribe**, who writes the timeline as it happens. [@pd-roles] The debugging lesson lists what each role does. Two rules matter most:

- **The incident commander (IC) does not fix things.** PagerDuty says the IC is "NOT a resolver". The IC is "the single source of truth" and gives out the work. [@pd-roles]
- **Only operations changes the system.** In Google's model, nobody else touches production during the incident. [@sre-incidents]

Atlassian says it is "very important to be clear who's in charge". [@atl-response]

### 4. One channel, one timeline

Atlassian opens one chat channel per incident, named after its ticket, for example HOT-1234. People write their observations, changes and decisions there as they happen. Atlassian says: "It's okay if it looks like people are talking to themselves!" [@atl-response]

Google calls a living incident document the IC's "most important responsibility". It can be messy, but it must be useful. [@sre-incidents] This record becomes your postmortem timeline.

### 5. Update on a clock, even with no news

At PagerDuty, the internal liaison sends a short update to the executive team "roughly every 30mins". [@pd-during] That rhythm is for internal leaders. PagerDuty gives no fixed rhythm for customer updates. [@pd-during] Our advice: pick a rhythm for each audience, put it in every update, and keep it even when nothing has changed.

Atlassian's internal template is simple: [@atl-response]

1. One or two sentences on the current state and impact.
2. "Current Status": 2–4 bullet points.
3. "Next Steps": 2–4 bullet points.
4. When and where the next update will come.

When you do not know something, say so. Atlassian says it is "far better to say 'the root cause is currently unknown'" than to leave it out. Without a shared source of truth, people jump to their own conclusions. [@atl-response]

For customers, one or two sentences are usually enough. They want to know if it is fixed, and if not, when. [@atl-response] At PagerDuty, a public post is the IC's choice, with the rule: "If in doubt, post it out." [@pd-during]

A sample: *"Checkout fails for about 20% of users since 14:05. We rolled back the 14:00 deploy; errors are falling. Cause unknown. Next update 14:50 in #inc-2231."*

### 6. Hand over, and rest

Google's SRE book says stress hormones can "impair cognitive functions". [@sre-oncall] Atlassian asks incident managers to watch how long people have been awake and to plan who takes over. [@atl-response] In a case in Google's workbook, PagerDuty rotated its responders and its IC every four hours. That gave people rest and brought new ideas. [@wb-response]

Make handovers explicit. The outgoing IC says, "You're now the incident commander, okay?" and waits for a firm yes before leaving. [@sre-incidents]

### 7. End it clearly

Atlassian says an incident is resolved "when the current or imminent business impact has ended". [@atl-response] At PagerDuty, the IC announces the end and moves other talk to Slack. [@pd-during] Atlassian's final update gives the impact and duration. Customers hear that service is back and a postmortem will follow. [@atl-response]

### 8. Write a blameless postmortem

Google writes a postmortem for user-visible downtime above a threshold, any data loss, a rollback or other fix by the on-call engineer, a long resolution time, or a monitoring failure. [@sre-postmortem] At PagerDuty, the postmortem owner schedules the meeting within 3 calendar days for a Sev-1 and 5 business days for a Sev-2. [@pd-pm]

Google's good example includes the impact, causes, timeline, "where we got lucky", and action items. [@wb-postmortem]

**Blameless** means you look for the causes without blaming any person or team. [@sre-postmortem] Ask what went wrong, not who caused it. [@wb-postmortem]

**Contributing factors.** PagerDuty says: "There is no single root cause of major failure in complex systems." A person's action should never be called a root cause. [@pd-pm] Atlassian still looks for root causes, using the "Five Whys". It separates the proximate cause, which led directly to the incident, from the root cause. The root cause is the best place to change so that the whole class of incident stops. [@atl-pm]

**Action items.** Each one needs an owner, a tracking number, a priority and an end state you can check. [@wb-postmortem] PagerDuty asks you to start each one with a verb and keep it narrow. [@pd-pm] Atlassian's "priority actions" must be done in 4 or 8 weeks, and managers approve each postmortem. [@atl-pm] Google quotes: "a postmortem without subsequent action is indistinguishable from no postmortem." [@wb-postmortem]

**Share it.** Google says "an unreviewed postmortem might as well never have existed". It shares good ones widely, for example as a "postmortem of the month". [@sre-postmortem]

### 9. Keep on-call healthy

Google's SRE book sets limits. Allow at most two incidents per 12-hour shift, because one incident takes about 6 hours of work. A rotation on one site needs at least eight engineers, or six per site on two sites. [@sre-oncall] "All paging alerts should also be actionable." [@sre-oncall] The [alerting lesson](/learn/alerting-and-on-call) goes deeper.

### 10. Practise before it is real

Google's DiRT tests create "a controlled emergency". PagerDuty's "Failure Friday" injects failures to learn how systems break. [@wb-response] In a Wheel of Misfortune, engineers act out an old postmortem, each playing a role. [@sre-postmortem] The [chaos engineering lesson](/learn/chaos-engineering-game-days) covers such a **game day**.

## Trade-offs

| Choice | Benefit | Cost | Use it when |
|---|---|---|---|
| Declare early | Yes. Help and roles arrive fast. | Some false alarms. | Any of the three tests is true, or you are unsure. |
| One IC, few hands on the system | Yes. Every change is known and agreed. | The IC can become a bottleneck in a huge incident. | Almost always. Split into sub-teams if it grows. |
| Everyone jumps in | Many ideas at once. | No. Changes clash, and nobody knows what changed. | Never as the default. Experts join when the IC asks. |
| Contributing factors | A fuller picture. | More actions to track. | Large or repeated incidents. |
| One root cause (Five Whys) | Simple and focused. | Can miss other factors. | Small, clear incidents. |

## At work: how a senior uses this

- Say "I am declaring an incident" out loud. Then ask: "Who is IC? Who talks to customers? Who writes notes?"
- If you are the IC, do not open a terminal. Ask each expert, "What do you see? What do you need?"
- Put the next update time in every update, and keep it, even if the update is "no change".
- In long incidents, ask who needs to sleep. Hand over with a written summary and a clear "you are IC now".
- In the postmortem, replace "Ravi ran the wrong command" with "the tool allowed a bulk delete with no check".
- Each quarter, count pages per shift and remove alerts that needed no action.

## With AI

### What to ask the agent

> "Here is the export of #inc-2231. Build a UTC timeline of each observation, decision, change and update. Mark any line where the time is unclear."

> "Draft our next internal update: state and impact, current status, next steps, next update time. Use only facts from the channel. Write 'unknown' where we do not know."

> "Draft a blameless postmortem from this timeline. Use roles, not names. List contributing factors, and propose action items that each start with a verb. Leave owners and dates empty."

### What to check yourself

- **Every time and fact.** Compare the timeline with your logs. A summary can merge or drop events.
- **Customer updates.** Check numbers, scope and promises before they go out.
- **Blame.** Remove any wording that points at a person, even softly, such as "failed to notice".
- **Decisions stay with people.** The IC picks the severity and fixes; the team picks owners and dates.

## Check yourself

## Go deeper

Related lessons: [Debugging production](/learn/debugging-production), [Alerting and on-call](/learn/alerting-and-on-call), [Observability, SLOs and error budgets](/learn/observability-slos-error-budgets), [Chaos engineering and game days](/learn/chaos-engineering-game-days) and [Clear technical writing](/learn/clear-technical-writing).
