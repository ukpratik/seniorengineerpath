---
topic: chaos-engineering
subtitle: Break your system on purpose, a little at a time and with a stop button, so you find its weak spots before a real outage does.
status: published
reviewed_by: ai-factcheck
reviewed_on: 2026-11-09
illustrative_story: false
glossary_optout: [cgroup, leader, transaction, availability, hypothesis, mitigation, rollback]
sources:
  - id: slack-dt
    title: "Richard Crowley, “Disasterpiece Theater: Slack’s process for approachable Chaos Engineering”, Slack Engineering (1 August 2019)"
    url: https://slack.engineering/disasterpiece-theater-slacks-process-for-approachable-chaos-engineering/
    note: How Slack plans and runs failure exercises, the questions it asks afterwards, and three real findings.
  - id: poc
    title: "Principles of Chaos Engineering (principlesofchaos.org, last updated March 2019)"
    url: https://principlesofchaos.org/
    note: The one-page definition, the four steps of an experiment and the five advanced principles.
  - id: nf-simian
    title: "Yury Izrailevsky and Ariel Tseitlin, “The Netflix Simian Army”, The Netflix Tech Blog (19 July 2011), Wayback Machine copy"
    url: https://web.archive.org/web/2012/http://techblog.netflix.com/2011/07/netflix-simian-army.html
    note: Why Netflix built Chaos Monkey, and the other "monkeys" it planned, from Latency Monkey to Chaos Gorilla.
  - id: nf-aws2011
    title: "Adrian Cockcroft, Cory Hicks and Greg Orzell, “Lessons Netflix Learned from the AWS Outage”, The Netflix Tech Blog (29 April 2011), Wayback Machine copy"
    url: https://web.archive.org/web/2012/http://techblog.netflix.com/2011/04/lessons-netflix-learned-from-aws-outage.html
    note: How Netflix got through the April 2011 AWS outage, what was still manual, and the first idea of Chaos Gorilla.
  - id: nf-cm-gh
    title: "Netflix/chaosmonkey, README (GitHub)"
    url: https://github.com/Netflix/chaosmonkey
    note: What Chaos Monkey does today and why. It needs Spinnaker to run.
  - id: aws-chaos
    title: "AWS Well-Architected Framework, Reliability Pillar, “REL12-BP04 Test resiliency using chaos engineering”"
    url: https://docs.aws.amazon.com/wellarchitected/latest/reliability-pillar/rel_testing_resiliency_failure_injection_resiliency.html
    note: A step-by-step method with a hypothesis template, stop conditions, faults to try and a list of tools.
  - id: aws-gameday
    title: "AWS Well-Architected Framework, Reliability Pillar, “REL12-BP05 Conduct game days regularly”"
    url: https://docs.aws.amazon.com/wellarchitected/latest/reliability-pillar/rel_testing_resiliency_game_days_resiliency.html
    note: How to prepare, run and review a game day, with common mistakes.
  - id: fis-stop
    title: "AWS Fault Injection Service User Guide, “Stop conditions for AWS FIS”"
    url: https://docs.aws.amazon.com/fis/latest/userguide/stop-conditions.html
    note: How an alarm stops an experiment, with the JSON to set it up.
  - id: gremlin-exp
    title: "Gremlin Docs, “Experiments” (Fault Injection)"
    url: https://www.gremlin.com/docs/fault-injection-experiments
    note: The resource, state and network faults one commercial tool offers, and how its agent undoes them.
  - id: dirt
    title: "Kripa Krishnan, “Weathering the Unexpected”, ACM Queue vol. 10 no. 9 (2012), Wayback Machine copy"
    url: https://web.archive.org/web/2020/https://queue.acm.org/detail.cfm?id=2371516
    note: How Google runs DiRT, its yearly disaster test, with many real findings. Includes Tom Limoncelli's sidebar from the point of view of an engineer being tested.
claims_checked:
  - claim: "Confabulator's normal and emergency modes both posted a notice to a Slack channel with no timeout, while the overall API action had one, so even emergency mode could not finish a change while Slack was down. A real incident showed the symptom; a planned exercise that partitioned Confabulator from Slack reproduced it and found the cause."
    source: slack-dt
    note: "Article (1 August 2019): \"There was no timeout on this action but there was a timeout on the overall configuration API action. As a result, even in emergency mode, the request could never make it as far as making the requested configuration change if Slack itself was down.\" Read from the original page with curl."
  - claim: "Chaos engineering is 'the discipline of experimenting on a system in order to build confidence in the system's capability to withstand turbulent conditions in production', with four steps: steady state, hypothesis over control and experimental groups, real-world events, try to disprove."
    source: poc
    note: "Definition and four steps match principlesofchaos.org (Last Update: 2019 March)."
  - claim: "AWS: 'If you know that the workload will fail, do not run the experiment'; try faults in non-production first and check stop conditions fire; keep passing experiments as automated regression tests in CI/CD."
    source: aws-chaos
    note: "REL12-BP04 implementation steps, quote exact."
  - claim: "In AWS FIS a stop condition is a CloudWatch alarm; if it is triggered, FIS stops the experiment and a stopped experiment cannot be resumed."
    source: fis-stop
    note: "\"If a stop condition is triggered during an experiment, AWS FIS stops the experiment. You cannot resume a stopped experiment.\""
  - claim: "In DiRT, exactly one person found the emergency phone bridge in the first exercise; in the next, more than 100 did, but the bridge held at most 40 callers. Services can 'prefail' a test."
    source: dirt
    note: "ACM Queue vol. 10 no. 9 (16 September 2012), read via Wayback."
  - claim: "After the April 2011 US-East outage Netflix moved out of the failed zone with manual work by many teams; Chaos Monkey did not simulate a whole zone failing, and engineers began calling such a tool 'Chaos Gorilla'."
    source: nf-aws2011
    note: "Netflix Tech Blog, 29 April 2011, read via Wayback."
quiz:
  - q: In a chaos experiment, what is the steady state?
    options:
      - The state of each server's CPU and memory before the test.
      - A measurable output that shows the system works normally, such as successful orders per second.
      - The moment when the experiment ends and the system is back to normal.
    answer: 1
    why: The principles say to focus on the system's measurable output, not its internal parts. You compare that output with and without the fault.
  - q: You know your service has no fallback when the cache is down. Should you run a chaos experiment that kills the cache in production?
    options:
      - Yes, because the experiment will prove the problem to your manager.
      - No. Fix the known weak spot first. An experiment you know will fail teaches you nothing new.
      - Yes, but only on a Friday evening, when traffic is low.
    answer: 1
    why: AWS says not to run an experiment when you know the workload will fail. Google lets such services "prefail" and skip the test.
  - q: Your first production experiment adds latency for 1% of users. What should end it automatically if users are hurt?
    options:
      - A stop condition, an alarm on a steady-state metric that stops the experiment.
      - The on-call engineer, who checks the dashboard the next morning.
      - Nothing. Experiments must always run to the end, or the data is useless.
    answer: 0
    why: AWS FIS stops an experiment when a CloudWatch alarm you chose fires. A person can also press stop, but the alarm does not wait for anyone.
  - q: Google's first DiRT test of its emergency phone bridge found that only one person joined. What did the next drill find?
    options:
      - Everyone joined, and the plan worked.
      - More than 100 people found the bridge, but it held only 40 callers.
      - Nobody joined, so the bridge was removed.
    answer: 1
    why: Each drill fixed one problem and showed the next one. That is why you repeat game days.
incidents: []
related: [incident-response, resilience-patterns, load-shedding, disaster-recovery, observability-slos, safe-deploys, feature-flags, runbooks-toil, distributed-tracing]
---

## What broke

Slack has an internal tool called Confabulator. Engineers use it to change configuration, including during incidents. In one bad incident, it did not work, and the team had to deploy the change by hand. [@slack-dt]

The tool had an emergency mode. When Slack itself was down, emergency mode skipped the check that needed Slack. On paper, that was the fallback. [@slack-dt]

So the team rebuilt the failure on purpose, in a planned exercise. They cut Confabulator off from Slack and tried a change that did nothing. The error came back. [@slack-dt]

The cause was small. Both modes posted a notice of each change to a Slack channel. That post had no timeout. With Slack down, the post hung. The whole request then hit its own time limit before the change was made. So even emergency mode could never finish a change while Slack was down. [@slack-dt]

Slack then improved its deploy tools and checked the timeout and retry settings of its critical systems. [@slack-dt] A real incident showed the symptom. A planned exercise reproduced it safely and found the cause.

## The idea in simple words

**Chaos engineering** is "the discipline of experimenting on a system in order to build confidence in the system's capability to withstand turbulent conditions in production." [@poc]

In plain words: you cause a small, real failure on purpose. Tools call this **fault injection**. You watch whether users still get what they need. If they do, your confidence grows. If they do not, you found a weak spot while you were ready for it.

A **game day** is the team version. You plan a failure, everyone who would respond takes part, and you review what happened afterwards.

> [!key] **The key idea.** Every timeout, failover and fallback is a guess until it has run. A chaos experiment runs it on purpose: small, watched, with a stop button, at a time you pick.

## How it works

### Where it came from

Netflix built Chaos Monkey for its move to the AWS cloud. It "randomly disables our production instances", to check that Netflix survives that common failure. [@nf-simian] Netflix ran it during business days, "with engineers standing by". [@nf-simian] Today the tool still randomly ends virtual machines and containers. Its idea: if engineers see failures often, they build services that survive them. [@nf-cm-gh]

In April 2011, AWS had a large outage in US-East. Netflix kept running, with more errors and latency than usual. But leaving the bad zone took manual work by many teams. Chaos Monkey killed single services, but Netflix did not simulate a whole zone going down. [@nf-aws2011]

Engineers started to talk about a "Chaos Gorilla" for that. [@nf-aws2011] By July 2011, Netflix described a whole Simian Army, some of it still only planned. Latency Monkey adds delays between services. Chaos Gorilla would simulate the loss of a whole availability zone. [@nf-simian]

### The principles

The site principlesofchaos.org describes an experiment in four steps: [@poc]

1. Define the **steady state**: a measurable output that shows normal behaviour.
2. Guess that this steady state will continue in a control group and in an experimental group.
3. Add real-world events, such as servers that crash or network links that are cut.
4. Try to prove the guess wrong, by looking for a difference between the two groups.

It then gives five advanced principles: [@poc]

- **Build a hypothesis around steady state behaviour.** Measure the system's outputs, such as throughput or errors, not its internal attributes.
- **Vary real-world events.** Pick events by impact or by how often they happen. Include non-failures, like a traffic spike.
- **Run experiments in production.** Real traffic is "the only way to reliably capture the request path".
- **Automate experiments to run continuously.** Manual runs do not last.
- **Minimise blast radius.** Some short-term harm is allowed, but the chaos engineer must keep it small and contained.

### An experiment is not a test

A test checks a behaviour you already know, and it passes or fails. An experiment asks a question about the whole system under real conditions. The principles say chaos "verifies that the system does work, rather than trying to validate how it works." [@poc]

AWS asks you to do both. Keep your normal resilience tests, and add chaos experiments to find what you did not know. [@aws-chaos] Once an experiment passes, AWS suggests keeping it as an automated regression test in your pipeline. [@aws-chaos]

### Run one experiment safely

Step through an example experiment on a checkout service. Use Next and Back, or the ← and → keys.

[[diagram:chaos-experiment-abort]]

1. **Measure normal first.** AWS gives an example: a payments system that handles 300 transactions a second, with 99% success and a 500 ms round trip. [@aws-chaos] Use the metrics from [Observability, SLOs and error budgets](/learn/observability-slos-error-budgets).
2. **Write the hypothesis.** AWS's template: "If {specific fault} occurs, the {workload name} workload will {describe mitigating controls} to maintain {business or technical metric impact}." [@aws-chaos]
3. **Start outside production.** AWS recommends trying each fault in a non-production environment first. There you also check that your stop conditions fire. [@aws-chaos] Slack runs every exercise in dev first. Then it decides whether to go on to production. It seriously considers stopping if automatic repair did not work, did not work perfectly or was slow. Most of all, it considers stopping if customers would see more than a short, minor rise in latency. [@slack-dt]
4. **Keep the blast radius small.** In production, use a control group and an experiment group, as in a [canary release](/learn/safe-deploys-config-changes). [@aws-chaos] A [feature flag](/learn/feature-flags-and-entitlements) can pick the 1%.
5. **Set a stop condition.** In AWS Fault Injection Service (FIS), a stop condition is a CloudWatch alarm. If it fires, FIS stops the experiment, and you cannot resume it. [@fis-stop]
6. **Pick the time and the people.** AWS suggests off-peak hours for your first production runs. [@aws-chaos] Netflix ran Chaos Monkey on business days, with engineers standing by. [@nf-simian] Pick a quiet time of day when the team is at work and watching. Slack picks a well-announced time, with the experts in one room or video call. [@slack-dt] Tell support and operations teams, and give them a way to report trouble. [@aws-chaos]
7. **Restore, record, repeat.** Bring the system back to a known-good state. Save dashboards and notes with the time and conditions. [@aws-chaos]

Here is an FIS stop condition, linked to an alarm you created: [@fis-stop]

```json
{
  "stopConditions": [
    {
      "source": "aws:cloudwatch:alarm",
      "value": "arn:aws:cloudwatch:eu-west-1:123456789012:alarm:checkout-p99-high"
    }
  ]
}
```

### What to inject, and with what

Start with the failures your design claims to survive. AWS lists: lost instances, a database failover, CPU and memory stress, added latency, dropped packets and DNS failures. To test a fallback, block a third-party provider for seconds or hours. [@aws-chaos] Slack's Confabulator exercise shows that a config tool can be a weak spot too. [@slack-dt]

Gremlin groups its faults into three kinds. Resource faults use up CPU, memory or disk. State faults shut down hosts, kill processes or move the clock ("Time Travel"). Network faults add latency, drop packets or block traffic. [@gremlin-exp]

Tools at a high level: [@aws-chaos]

- **Chaos Monkey** ends instances at random. It needs Spinnaker. [@nf-cm-gh]
- **AWS FIS** is a managed service with stop conditions built in.
- **Chaos Mesh** and **Litmus** are open-source tools. FIS can run their faults too, for example on a Kubernetes pod. **Gremlin** is commercial.

Any tool should track the experiment's state, log what it does and undo it. AWS discourages home-made scripts that cannot do these. [@aws-chaos] Gremlin's agent undoes its faults if it loses its connection. But a shutdown or a killed process cannot be rolled back. [@gremlin-exp]

### Game days

A game day tests people and processes, not only machines. AWS says to involve the same teams who would handle a real event. Business decision makers should take part too. [@aws-gameday]

Google's yearly test is called DiRT. It breaks live systems, and it also keeps key people and leaders from helping. [@dirt] The [incident response lesson](/learn/incident-response-and-postmortems) mentioned it; here is what it found. In one test, exactly one person found the emergency phone bridge. In the next drill, more than 100 people found it, but the bridge held only 40 callers. [@dirt]

Give the game day clear roles. Slack has one or two hosts who write a detailed plan, with exact commands and a hypothesis. [@slack-dt] Google's DiRT has a technical team, a coordination team and a command center ready to undo a test. [@dirt]

Run the scenario, and then watch. Slack's advice is to hold back and let the system heal itself. [@slack-dt] Use the [incident process](/learn/incident-response-and-postmortems) you would use for real. End with a blameless review. Slack asks: [@slack-dt]

- How long did it take to detect and to resolve?
- Did any users notice? Did any humans have to step in?
- Was any documentation wrong? Were any dashboards out of date?

### What you learn

- **Wrong runbooks.** Tom Limoncelli wrote a fictionalized account of a DiRT drill. A restore command needed new parameters, and the restore area had become too small. [@dirt] Fix [runbooks](/learn/runbooks-and-reducing-toil) on the spot.
- **Missing alerts.** AWS says experiments should check that alerts fire and teams get notified. [@aws-chaos] See [Alerting and on-call](/learn/alerting-and-on-call).
- **Fallbacks that fail.** Like Confabulator's emergency mode. [@slack-dt] Test the timeouts, breakers and fallbacks from [Circuit breakers and bulkheads](/learn/circuit-breakers-and-bulkheads) this way.
- **Hidden dependencies.** In one DiRT test, teams moved services to their own workstations. The test had also cut off their office, so those moves failed. Login systems failed in unexpected ways too. [@dirt]
- **Retries that amplify.** Watch the load on the failing service during the test. See [Backoff, jitter and retry budgets](/learn/backoff-jitter-retry-budgets).

### When not to do it

- **You cannot see the effect.** Slack lists its logs, metrics and alerts before each exercise. [@slack-dt] Without them, you cannot tell when to stop. Add [traces](/learn/distributed-tracing) and dashboards first.
- **You cannot undo it.** AWS asks for a rollback plan for every experiment. [@aws-chaos] Have a way back, as in [disaster recovery](/learn/disaster-recovery-and-backups).
- **You already know it will break.** "If you know that the workload will fail, do not run the experiment." [@aws-chaos] At Google, services can "prefail" a test and skip it. [@dirt] Fix the weak spot first.

## Trade-offs

| Option | Finds real problems? | Risk to users | Use it when |
|---|---|---|---|
| Tabletop walk-through | Partly. It finds gaps in docs and plans. | No. Nothing breaks. | Before any real fault, and for a new team. |
| Staging experiment | Partly. Staging may not match production. | No. | First run of every new fault. |
| Production, 1% of traffic | Yes. Real traffic, real config. | Small, if stop conditions work. | Staging passed and you can see and undo. |
| Automated, continuous | Yes, and it catches new regressions. | Small, every day. | The experiment passes reliably. |
| Company-wide game day | Yes, including people and processes. | Can be large. Google says DiRT has caused outages. [@dirt] | Smaller tests already pass. |

## At work: how a senior uses this

- Ask of every fallback: **"When did this last run?"** If nobody knows, plan an experiment.
- Write the hypothesis and the stop condition before the fault. Get one other engineer to review it.
- Start with one instance in staging. Then 1% in production, at a quiet time when people are at work and watching.
- Pick faults from your past postmortems. AWS suggests the same. [@aws-chaos]
- Turn every finding into a tracked action item with an owner, then rerun the experiment.
- After a pass, add the experiment to the pipeline so it keeps passing.

## With AI

### What to ask the agent

> "Here is our service's dependency list and the timeouts in our config. For each dependency, suggest one chaos experiment: the fault, the steady-state metric, the hypothesis and a stop condition."

> "Write an AWS FIS experiment template that adds 3 seconds of latency to 1% of tasks in the checkout ECS service for 10 minutes. Use the CloudWatch alarm checkout-p99-high as the stop condition."

> "Draft a game day plan for losing our primary database: roles, timeline, the exact commands, what we watch, when we abort, and the debrief questions."

### What to check yourself

- **Targets.** Check the selected resources. A wrong tag or filter can hit all instances, not 1%.
- **The stop condition.** Make the alarm fire in staging before you trust it.
- **The undo.** Know how to stop the fault by hand, and whether it can be undone at all.
- **Action names.** Check every fault name against the tool's docs. An agent can invent ones that do not exist.
- **People.** Tell support and on-call before you start, not after.

## Check yourself

## Go deeper

Related lessons: [Incident response and postmortems](/learn/incident-response-and-postmortems), [Circuit breakers and bulkheads](/learn/circuit-breakers-and-bulkheads), [Load shedding and graceful degradation](/learn/load-shedding-graceful-degradation), [Disaster recovery and backups](/learn/disaster-recovery-and-backups) and [Runbooks and reducing toil](/learn/runbooks-and-reducing-toil).
