---
topic: observability-slos
subtitle: Logs, metrics and traces show what your system does. SLOs and error budgets tell you when to act.
status: published
reviewed_by: ai-factcheck
reviewed_on: 2026-10-21
illustrative_story: true
sources:
  - id: otel
    title: "OpenTelemetry documentation: Signals"
    url: https://opentelemetry.io/docs/concepts/signals/
    note: Short definitions of traces, metrics, logs and baggage in the most widely used open standard.
  - id: sre-monitoring
    title: "Google, Site Reliability Engineering, chapter 6: Monitoring Distributed Systems"
    url: https://sre.google/sre-book/monitoring-distributed-systems/
    note: The four golden signals, and why averages hide slow requests.
  - id: sre-slo
    title: "Google, Site Reliability Engineering, chapter 4: Service Level Objectives"
    url: https://sre.google/sre-book/service-level-objectives/
    note: What SLIs, SLOs and SLAs are, and why 100% is the wrong target.
  - id: sre-risk
    title: "Google, Site Reliability Engineering, chapter 3: Embracing Risk"
    url: https://sre.google/sre-book/embracing-risk/
    note: Where error budgets come from, and how they settle the fight between speed and safety.
  - id: sre-avail
    title: "Google, Site Reliability Engineering, Appendix A: Availability Table"
    url: https://sre.google/sre-book/availability-table/
    note: How much downtime each number of nines allows per year, quarter, month and day.
  - id: srew-implementing
    title: "Google, The Site Reliability Workbook, chapter 2: Implementing SLOs"
    url: https://sre.google/workbook/implementing-slos/
    note: How to choose SLIs, pick a time window, and write an error budget policy.
  - id: srew-alerting
    title: "Google, The Site Reliability Workbook, chapter 5: Alerting on SLOs"
    url: https://sre.google/workbook/alerting-on-slos/
    note: Burn rates and the multiwindow, multi-burn-rate alerts used in this lesson.
  - id: roblox
    title: "Roblox, “Roblox Return to Service 10/28–10/31, 2021”"
    url: https://about.roblox.com/newsroom/2022/01/roblox-return-to-service-10-28-10-31-2021/
    note: A 73-hour outage where monitoring depended on the system that was failing.
  - id: slack
    title: "Slack Engineering, “Slack's Outage on January 4th 2021”"
    url: https://slack.engineering/slacks-outage-on-january-4th-2021/
    note: Dashboards and alerts failed during the outage because they depended on the broken network.
claims_checked:
  - claim: "The Workbook's recommended alerts for a 99.9% SLO: page at burn rate 14.4 over 1 h and 5 min (2% of budget), page at 6 over 6 h and 30 min (5%), ticket at 1 over 3 days and 6 h (10%)."
    source: srew-alerting
    note: Matches Table 5-8 exactly. Burn rate 10 at 99.9% empties a 30-day budget in 3 days. 14.4 × 0.1% = 1.44% error rate.
  - claim: "99% allows about 7.2 hours a month, 99.9% about 43 minutes, 99.99% about 4.3 minutes, 99.999% about 26 seconds."
    source: sre-avail
    note: Table gives 7.2 h, 43.2 min, 4.32 min, 25.9 s per 30-day month. Re-computed 0.1% × 43,200 min = 43.2 min.
  - claim: "A user on a phone that works 99% of the time cannot tell 99.99% from 99.999%."
    source: sre-risk
    note: Close paraphrase of the chapter. The chapter also says releases halt when the error budget is spent.
  - claim: "The four golden signals are latency, traffic, errors and saturation; a 100 ms average can hide 1% of requests taking 5 seconds."
    source: sre-monitoring
    note: Both stated in chapter 6, which also says to page on symptoms and keep causes for debugging.
  - claim: "Roblox was down for 73 hours, and its monitoring depended on Consul, which slowed diagnosis."
    source: roblox
    note: Postmortem says monitoring relied on Consul and this severely hampered triage.
  - claim: "Slack's dashboards and alerts failed in the January 2021 outage."
    source: slack
    note: They ran in a different VPC from their databases, so traffic went through the overloaded Transit Gateways. Wording made more precise.
quiz:
  - q: Your SLO is 99.9% of requests succeed over 30 days. You serve 10 million requests in that time. How many failed requests can you afford?
    options: ["1,000", "10,000", "100,000"]
    answer: 1
    why: The error budget is 100% − 99.9% = 0.1%. 0.1% of 10 million is 10,000.
  - q: What does a burn rate of 1 mean?
    options:
      - The service is completely down.
      - The error budget will run out exactly at the end of the SLO window.
      - One error happened in the last hour.
    answer: 1
    why: Burn rate compares how fast you spend the budget with the speed that would use it up exactly at the end of the window.
  - q: A checkout needs three services, one after another. Each is 99.9% available, and they fail independently. What is the best availability checkout can have?
    options: ["About 99.97%", "About 99.9%", "About 99.7%"]
    answer: 2
    why: 0.999 × 0.999 × 0.999 ≈ 0.997. Every service in the chain adds its own failures.
  - q: Which alert is most likely to page on real user pain, and not on noise?
    options:
      - CPU above 80% for 5 minutes.
      - The error budget is burning 14.4 times too fast over both the last hour and the last 5 minutes.
      - Any single 500 error.
    answer: 1
    why: It measures what users feel, and the two windows stop it from firing on short blips or long after the problem ends.
  - q: About how much downtime does 99.9% availability allow in a 30-day month?
    options: ["About 4 minutes", "About 43 minutes", "About 7 hours"]
    answer: 1
    why: 0.1% of 30 days is 43.2 minutes. Each extra nine divides the allowed downtime by 10.
incidents: [roblox-2021-10-consul, slack-2021-01-network]
related: [alerting-oncall, distributed-tracing, incident-response]
---

## What broke

Meera's team runs the checkout API for an online shop. Their dashboard has 40 graphs. On Tuesday evening, every graph looks fine. Average latency is 120 ms. CPU is at 45%.

At the same time, one in ten customers cannot pay. A new release broke payments for one card type. Those requests fail fast, so they even pull the average latency down.

Nobody is paged. The team gets 200 alerts a week about CPU and disk, so they have learned to ignore the alert channel. The problem is found the next morning, from angry messages to support.

The team had a lot of data, but no clear answer to one question: **"Are our users getting what they need right now?"** (This story is made up, but the pattern is common.)

## The idea in simple words

**Observability** means you can understand what your system is doing from the data it sends out. That data comes in three main kinds [@otel]:

- **Metrics:** numbers measured over time, like requests per second or error rate. Cheap to keep, good for graphs and alerts.
- **Logs:** a record of one event, like "payment 7f3a failed: card declined". Rich in detail, but large.
- **Traces:** the path of one request through many services. A trace is made of **spans**, one for each step, with its timing.

Data alone does not tell you when to act. For that you need a target that matches what users feel:

- An **SLI** (service level indicator) is a measure of service, best written as *good events ÷ valid events* [@srew-implementing]. For example: the share of checkout requests that succeed in under 500 ms.
- An **SLO** (service level objective) is the target for that SLI over a time window. For example: 99.9% over a rolling 4 weeks [@srew-implementing].
- An **SLA** (service level agreement) is a contract with customers that has a penalty, often money, if you miss it [@sre-slo].

The **error budget** is the part you are allowed to fail: 100% minus the SLO [@srew-implementing]. With a 99.9% SLO and 1 million requests, you can fail 1,000 of them.

> [!key] **The key idea.** Never aim for 100%. It is not realistic [@sre-slo]. A user on a phone that works 99% of the time cannot tell 99.99% from 99.999% [@sre-risk]. Pick a target, then spend the error budget on purpose. When the budget is left, ship. When it is gone, fix reliability first [@sre-risk].

## How it works

### Availability maths: the nines

Each extra "nine" cuts the allowed downtime by 10 [@sre-avail]:

- **99%:** about 7.2 hours a month.
- **99.9%:** about 43 minutes a month.
- **99.99%:** about 4.3 minutes a month.
- **99.999%:** about 26 seconds a month.

Services that depend on each other multiply. If checkout needs three services in a row, each 99.9%, the best it can do is 0.999 × 0.999 × 0.999 ≈ 99.7%. This assumes they fail independently. Two copies in parallel help: if each copy is 99% and they really fail independently, both are down only 0.01 × 0.01 = 0.01% of the time. In real life, copies often fail together, for example after one bad config change.

### What to measure

Google's SRE book suggests four golden signals for any service: **latency, traffic, errors and saturation** (how full the service is) [@sre-monitoring]. It also warns that averages hide slow requests. A service with an average latency of 100 ms can still have 1% of requests take 5 seconds [@sre-monitoring]. So use **percentiles**, like p99, in your SLIs.

### Burn-rate alerts

The **burn rate** is how fast you spend the error budget, compared with the speed that would use it up exactly at the end of the window [@srew-alerting]. At burn rate 1, the budget lasts 30 days. At burn rate 10, it is gone in 3 days [@srew-alerting].

Step through a bad deploy and see how a burn-rate alert reacts. Use Next and Back, or the ← and → keys.

[[diagram:error-budget-burn]]

The Site Reliability Workbook recommends three alerts for a 99.9% SLO [@srew-alerting]:

- **Page** if the burn rate is above 14.4 over the last hour *and* the last 5 minutes. That is 2% of the 30-day budget in one hour.
- **Page** if it is above 6 over the last 6 hours *and* the last 30 minutes. That is 5% of the budget.
- **Open a ticket** if it is above 1 over the last 3 days *and* the last 6 hours. That is 10% of the budget.

The long window proves the problem is real. The short window makes the alert stop a few minutes after the problem stops [@srew-alerting].

## Trade-offs

| Approach | Catches real user pain? | Noise | Use it when |
|---|---|---|---|
| Alert on causes (CPU, disk, memory) | No. Users can suffer while CPU is fine. | High. Many alerts need no action. | Only as tickets or dashboards, to help find a cause. |
| Alert on any error | Yes, but too early. | Very high. Every blip pages someone. | Almost never for paging. |
| Simple SLO threshold (error rate above 0.1% for 10 minutes) | Yes. | Medium. Short spikes can still page. | A first step, while you learn your traffic. |
| Multiwindow, multi-burn-rate SLO alerts | Yes. Fast for big outages, slower for small leaks. | Low. | Most user-facing services, once SLOs exist. |

**Rule of thumb:** page a human only for symptoms that users feel. Keep cause metrics for debugging [@sre-monitoring].

## At work: how a senior uses this

- Start from the user's journey: "Can a customer pay?" Write the SLI as good events ÷ valid events. Then add the SLO.
- Choose the SLO with the product owner. Use past data, and do not promise more than your dependencies give you.
- Write an **error budget policy** before you need it. For example: if the budget for 4 weeks is spent, stop feature launches and work on reliability until the service is back within the SLO [@srew-implementing].
- Put a trace ID in every log line. Then you can jump from a slow trace to the logs of that exact request.
- Keep monitoring separate from what it watches. In 2021, Roblox was down for 73 hours, and its monitoring depended on Consul, the system that was failing. That slowed the team's diagnosis [@roblox]. Slack's dashboards and alerts also failed in its January 2021 outage, because they reached their databases through the overloaded network [@slack].
- Delete or fix every alert that pages without needing action. Alert noise trains people to ignore the real alert.

## With AI

### What to ask the agent

> "Write a Prometheus recording rule for the checkout SLI: the share of requests that return a non-5xx status in under 500 ms. Then write burn-rate alerts for a 99.9% SLO, with 1 hour and 5 minute windows at 14.4, and 6 hour and 30 minute windows at 6."

> "Add OpenTelemetry tracing to this HTTP handler. Add the trace ID to every log line it writes."

### What to check yourself

- The SLI counts only valid requests. Decide on purpose whether health checks and 4xx errors from bad user input count.
- The burn-rate maths matches your SLO. A burn rate of 14.4 for a 99.9% SLO means an error rate of 1.44%.
- Each alert uses both a long and a short window.
- The latency SLI uses a percentile or a threshold count, never an average.
- Logs do not contain passwords, card numbers or other personal data.

## Check yourself

## Go deeper

Next on this line: [Alerting and on-call](/learn/alerting-and-on-call) explains how to page humans only for what matters.
