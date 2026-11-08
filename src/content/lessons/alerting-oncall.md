---
topic: alerting-oncall
subtitle: Page a human only when users are hurting and a person must act now, and build an on-call rota your team can keep up for years.
status: published
reviewed_by: ai-factcheck
reviewed_on: 2026-11-08
illustrative_story: true
sources:
  - id: sre-intro
    title: "Benjamin Treynor Sloss, “Introduction”, Google, Site Reliability Engineering, chapter 1"
    url: https://sre.google/sre-book/introduction/
    note: The three kinds of monitoring output (alerts, tickets and logging) and what each one is for.
  - id: sre-monitoring
    title: "Rob Ewaschuk, “Monitoring Distributed Systems”, Google, Site Reliability Engineering, chapter 6"
    url: https://sre.google/sre-book/monitoring-distributed-systems/
    note: Symptoms against causes, the questions to ask before you add a page, and two stories of alert noise at Google.
  - id: sre-oncall
    title: "Andrea Spadaccini, “Being On-Call”, Google, Site Reliability Engineering, chapter 11"
    url: https://sre.google/sre-book/being-on-call/
    note: Limits on on-call time and incidents per shift, team sizes, response times, pay, and overload.
  - id: srew-alerting
    title: "Google, The Site Reliability Workbook, chapter 5: Alerting on SLOs"
    url: https://sre.google/workbook/alerting-on-slos/
    note: Six ways to alert on an SLO, ending with multiwindow, multi-burn-rate alerts and their numbers.
  - id: prom-alerting
    title: "Prometheus documentation: Alerting (best practices)"
    url: https://prometheus.io/docs/practices/alerting/
    note: Short advice to alert on symptoms, as high in the stack as possible, and to monitor your monitoring.
  - id: alertmanager
    title: "Prometheus documentation: Alertmanager"
    url: https://prometheus.io/docs/alerting/latest/alertmanager/
    note: What grouping, inhibition and silences do, with a network partition example.
  - id: am-config
    title: "Prometheus documentation: Configuration (Alertmanager)"
    url: https://prometheus.io/docs/alerting/latest/configuration/
    note: The routing tree and the timers group_wait, group_interval and repeat_interval, with their defaults.
  - id: pd-alerting
    title: "PagerDuty Incident Response documentation: Alerting Principles"
    url: https://response.pagerduty.com/oncall/alerting_principles/
    note: PagerDuty's alert priorities, what each alert must contain, and why you must test alerts.
  - id: pd-oncall
    title: "PagerDuty Incident Response documentation: Being On-Call"
    url: https://response.pagerduty.com/oncall/being_oncall/
    note: Escalation timeouts, backup schedules, handoffs and how to treat noisy alerts.
  - id: gitlab
    title: "GitLab, “Postmortem of database outage of January 31”"
    url: https://about.gitlab.com/blog/2017/02/10/postmortem-of-database-outage-of-january-31/
    note: Backup failures were reported only by email, the emails were rejected, and nobody knew.
claims_checked:
  - claim: "For a 99.9% SLO over 30 days, page at burn rate 14.4 over 1 h and 5 min (2% of budget), page at 6 over 6 h and 30 min (5%), ticket at 1 over 3 days and 6 h (10%)."
    source: srew-alerting
    note: "Matches Table 5-8. Short window 1/12 of the long one; the alert stops \"5 minutes after the errors stop\". Diagram timings recomputed: 5.76 min, 4.5 h, 48 h; 2%, 5%, 10%."
  - claim: "On-call is capped at 25% of time and operations at 50%; minimum 8 engineers for one site or 6 per site for two; at most 2 incidents per 12-hour shift, about 6 hours each."
    source: sre-oncall
    note: "Also checked the overload examples: \"number of daily tickets < 5, paging events per shift < 2\" and \"approach a 1:1 alert/incident ratio\"."
  - claim: "Three kinds of monitoring output: alerts (act now), tickets (act within a few days, no damage), logging (for diagnosis only)."
    source: sre-intro
    note: "Chapter 1 definitions of Alerts, Tickets and Logging."
  - claim: "Alertmanager defaults: group_wait 30s, group_interval 5m, repeat_interval 4h."
    source: am-config
    note: Route defaults in the configuration reference.
  - claim: "GitLab's backup failures were reported only by email, and the emails were rejected, so nobody knew."
    source: gitlab
    note: "\"DMARC was not enabled for the cronjob emails, resulting in them being rejected by the receiver.\""
quiz:
  - q: Which of these should page a human at 3 a.m.?
    options:
      - CPU on one database replica is at 92%.
      - 8% of checkout requests have failed for the last 10 minutes.
      - A nightly report job finished 20 minutes late.
    answer: 1
    why: Only the checkout errors are urgent, user-visible and need a human now. The other two can be a ticket or a dashboard.
  - q: Your SLO is 99.9% over 30 days. An alert fires at burn rate 14.4 over 1 hour. How much of the error budget has that hour used?
    options: ["About 0.5%", "About 2%", "About 14%"]
    answer: 1
    why: One hour is 1/720 of 30 days. 14.4 × 1/720 = 0.02, so 2% of the budget.
  - q: Why does a burn-rate page check a 5-minute window as well as a 1-hour window?
    options:
      - So the page stops soon after the errors stop.
      - So it pages for smaller error rates.
      - So it uses less memory in Prometheus.
    answer: 0
    why: The 1-hour average stays high long after a fix. The 5-minute average drops within minutes, so the alert clears fast.
  - q: Last week your on-call got 30 pages. Only 6 needed someone to act. What is the best next step?
    options:
      - Ask the on-call to read alerts faster.
      - Fix, downgrade or delete the 24 alerts that needed no action.
      - Add a third on-call engineer.
    answer: 1
    why: Google's SRE book says every page should be actionable. Noise teaches people to ignore real pages.
  - q: A team in one office wants a 24/7 rota where nobody is on call too often. What is the smallest size Google's SRE book suggests?
    options: ["4 engineers", "8 engineers", "12 engineers"]
    answer: 1
    why: With on-call at most 25% of time and a primary and secondary, the book gets a minimum of eight engineers for one site.
incidents: [gitlab-2017-01-database]
related: [observability-slos, incident-response, runbooks-toil, debugging-production, safe-deploys]
---

## What broke

Ravi is on call for a payments team. His phone rings 23 times on Monday night. "CPU > 80% on db-replica-3." "Disk 85% on worker-7." "Queue length > 1,000." Each alert clears by itself a few minutes later.

By 2 a.m. Ravi mutes his phone. He has learned that these alerts never mean anything.

At 4:10 a.m., a bad config change makes 12% of card payments fail. There is no alert on failed payments, only on machines. The one CPU alert that fires looks like every other one.

The team finds out at 9 a.m. from support tickets. Ravi did nothing wrong. The alerts trained him to ignore them. (This story is made up, but the pattern is common.)

## The idea in simple words

Google's SRE book names three kinds of monitoring output [@sre-intro]. Choose one on purpose for each signal:

- **Page (an alert):** wakes a person up. A human must act now, because the problem is happening or about to happen [@sre-intro].
- **Ticket:** goes to a queue. A human must act, but no harm is done if they act within a few days [@sre-intro].
- **Log:** nobody needs to look. It is kept for when you debug or investigate later [@sre-intro].

Google's SRE book says your monitoring answers two questions: "what's broken, and why?" [@sre-monitoring] The "what" is the **symptom**: payments fail, pages are slow. The "why" is the **cause**: a full disk, a hot CPU, a bad config.

Page on symptoms. Use causes to help you debug [@sre-monitoring], for example on dashboards. The Prometheus docs give the same advice: alert on high latency and errors "as high up in the stack as possible" [@prom-alerting].

The book also says, "Every page should be actionable." If a page only needs a fixed, robotic response, it should not be a page [@sre-monitoring]. Automate that response instead.

> [!key] **The key idea.** A person can react with urgency only a few times a day before they get tired [@sre-monitoring]. Every useless page spends that attention. Too many useless pages cause alert fatigue, and then real pages get missed.

## How it works

### 1. Ask four questions before you add a page

The SRE book gives a checklist. Here it is in short [@sre-monitoring]:

- Does it catch something urgent, actionable and visible to users, that nothing else catches?
- Will I ever ignore it because I know it is harmless?
- Can I actually do something about it?
- Is someone else already paged for the same problem?

If any answer is wrong, make it a ticket, a dashboard, or delete it.

### 2. Page on SLO burn rate

You already know SLOs and burn rate from [Observability and SLOs](/learn/observability-slos-error-budgets). A burn-rate alert is the best symptom alert, because it measures user pain directly.

A good alert has high precision (few false pages) and high recall (few missed problems). It should also fire fast and stop fast after the fix [@srew-alerting]. One window cannot do all four. So the SRE Workbook uses a **multiwindow, multi-burn-rate** design. Each rule fires only when a long and a short window both pass the same burn rate [@srew-alerting]. For a 99.9% SLO over 30 days, it recommends [@srew-alerting]:

| Long window | Short window | Burn rate | Budget spent | Action |
|---|---|---|---|---|
| 1 hour | 5 minutes | 14.4 | 2% | Page |
| 6 hours | 30 minutes | 6 | 5% | Page |
| 3 days | 6 hours | 1 | 10% | Ticket |

Check the first row yourself. One hour is 1/720 of 30 days. At burn rate 14.4, one hour spends 14.4 ÷ 720 = 2% of the budget. In error terms, 14.4 × 0.1% = 1.44% of requests failing.

The short window is 1/12 of the long one [@srew-alerting]. It makes the alert stop about 5 minutes after the errors stop, not an hour later [@srew-alerting].

Step through a fast, a slow and a tiny burn. Use Next and Back, or the ← and → keys.

[[diagram:burn-rate-windows]]

The cost is many numbers to manage [@srew-alerting]. Our advice: put them in shared recording rules, not in every team's copy. With very low traffic, one failure is a large share of requests. The Workbook suggests four fixes: synthetic traffic, combining small services for alerting, changing the product so one failure matters less, or a lower SLO or longer window [@srew-alerting].

### 3. Group, route and silence

One broken network link can fire hundreds of alerts at once. Prometheus Alertmanager can group them by labels such as `cluster` and `alertname`, so you get one page, not hundreds [@alertmanager]. It also has **inhibition**: if a "cluster not reachable" alert fires, it can mute every other alert for that cluster. **Silences** mute alerts for a set time, for example during planned work [@alertmanager].

The routing tree sends each alert to a receiver, such as PagerDuty for pages or email for less urgent alerts [@alertmanager]. By default, Alertmanager waits 30 seconds to gather a new group, 5 minutes before it sends updates to that group, and 4 hours before it repeats a notification [@am-config].

Use a `severity` label to route. PagerDuty's own scheme has High (act now, any hour), Medium (act within 24 hours, work hours only), Low, and Notification, which never pages anyone [@pd-alerting].

### 4. Make each alert useful at 3 a.m.

PagerDuty says alerts with generic titles cause confusion. Each alert should say what is wrong and why it matters. It should also list the steps or link to a runbook [@pd-alerting]. Prometheus adds: link to a console that shows which part is at fault [@prom-alerting].

Test every new alert. PagerDuty says, "An untested alert is equivalent to not having an alert at all" [@pd-alerting]. Also check that the alert resolves by itself when the metric is normal again [@pd-alerting].

### 5. Design the rota

Google caps on-call at 25% of an SRE's time, and operations work in total at 50% [@sre-oncall]. Say two people are always on call, a primary and a secondary, in week-long shifts. Then one site needs at least eight engineers, so each one is on call one week in four. With two sites, Google suggests at least six per site [@sre-oncall].

Two sites far apart can run **follow-the-sun**. Each team covers its own day, so nobody works night shifts, which harm health [@sre-oncall].

Set a load limit. Google aims for at most 2 incidents per 12-hour shift. A real incident, with its cleanup and postmortem, takes about 6 hours [@sre-oncall]. The book's example targets for overload are fewer than 2 paging events per shift and fewer than 5 tickets a day. Noisy alerts should be tuned toward one alert per incident [@sre-oncall].

Typical response times at Google are 5 minutes for user-facing services and 30 minutes for less urgent ones [@sre-oncall]. PagerDuty recommends a 5-minute timeout before a page goes to the backup. It says you sometimes cannot answer in time, and that is why there is a backup [@pd-oncall]. At handoff, tell the next person about issues that are still open [@pd-oncall].

Pay for it. Google gives time off or cash for on-call hours, capped at a share of salary [@sre-oncall].

## Trade-offs

| Rota design | Night pages? | People needed | Main risk |
|---|---|---|---|
| One site, primary only | Yes. | Fewer. | No backup when the primary misses a page. Burnout. |
| One site, primary and secondary | Yes. | At least 8 for 25% on-call [@sre-oncall]. | Night shifts still hurt sleep and health. |
| Two sites, follow-the-sun | No, if the sites cover the clock. | At least 6 per site [@sre-oncall]. | Handoffs between sites lose context. |
| Everyone on call, no rota | Yes. | Whole team. | Nobody owns the page, so everyone waits for someone else. |
| On-call too rare (big team, few pages) | Few. | Many. | People forget the system. Google calls this underload [@sre-oncall]. |

## At work: how a senior uses this

- Count two numbers every week: pages per 12-hour on-call period, and pages per real incident. A week-long rota has 14 such periods. Compare them with Google's numbers: fewer than 2 pages per shift, and close to 1 alert per incident [@sre-oncall].
- Hold a short review after each shift. For each page ask: was it real, did it need a human, was the runbook right? Then fix, downgrade or delete the alert.
- Delete alerts and dashboards nobody uses. Google says rarely used alerting is "up for removal" [@sre-monitoring].
- Turn repeat noise into work. PagerDuty says an alert that keeps firing for a preventable non-issue, like a disk filling up, should become a fix task [@pd-oncall]. Many teams call this toil. A coming lesson on runbooks and toil covers it.
- Monitor your monitoring. Prometheus suggests alerts on Alertmanager itself and an outside blackbox check [@prom-alerting]. At GitLab in 2017, backup failures were sent only by email. The emails were rejected because of an email setting (DMARC), so nobody knew the backups were failing [@gitlab].
- Give the person who was up all night the morning off.

## With AI

### What to ask the agent

> "Here is our Alertmanager config and the last 30 days of alerts as CSV. Group the alerts by name. For each, show how often it fired, how often someone acted, and whether it is a symptom or a cause. Suggest which to delete or downgrade to a ticket."

> "Write Prometheus alert rules for a 99.9% checkout SLO. Use the SRE Workbook's three multiwindow rules. Page at 14.4 over 1 h and 5 m, and at 6 over 6 h and 30 m. Open a ticket at 1 over 3 d and 6 h. Add a runbook_url and summary annotation to each."

### What to check yourself

- The burn-rate thresholds match your SLO. For 99.9%, a burn rate of 14.4 is an error rate of 1.44%. For 99.5%, the same burn rate is 7.2%.
- Both windows in each rule use the same burn rate.
- Every page has a runbook link that really opens, and the runbook names real commands and dashboards.
- `group_by` labels make sense. Grouping by too little merges unrelated problems; grouping by too much sends hundreds of pages.
- The agent did not add new cause alerts (CPU, disk) as pages.

## Check yourself

## Go deeper

Before this: [Observability and SLOs](/learn/observability-slos-error-budgets) explains SLIs, error budgets and burn rate.

Next: [Incident response](/learn/incident-response-and-postmortems) covers what to do after the page. Runbooks and toil (coming soon) covers how to write runbooks and remove repeat work.

Related: [Debugging production](/learn/debugging-production) goes from a symptom to a cause. [Safe deploys](/learn/safe-deploys-config-changes) shows how to watch alerts during a rollout.
