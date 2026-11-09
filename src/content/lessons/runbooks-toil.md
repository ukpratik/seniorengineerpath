---
topic: runbooks-toil
subtitle: Write runbooks that work at 3 a.m., measure the repeat work your team does by hand, and automate it step by step with safety limits.
status: published
reviewed_by: ai-factcheck
reviewed_on: 2026-11-09
illustrative_story: false
glossary_optout: [thread, career-ladder, context-switch, rate-limit]
sources:
  - id: aws-s3
    title: "Amazon Web Services, “Summary of the Amazon S3 Service Disruption in the Northern Virginia (US-EAST-1) Region”"
    url: https://aws.amazon.com/message/41926/
    note: AWS's own account of the 2017 S3 outage, the playbook command with one wrong input, and the safety limits added to the tool.
  - id: sre-toil
    title: "Vivek Rau, “Eliminating Toil”, Google, Site Reliability Engineering, chapter 5"
    url: https://sre.google/sre-book/eliminating-toil/
    note: Google's definition of toil, its six properties, what is not toil, and the 50% goal for engineering work.
  - id: srew-toil
    title: "Google, “Eliminating Toil”, The Site Reliability Workbook, chapter 6"
    url: https://sre.google/workbook/eliminating-toil/
    note: How to measure and rank toil, when not to automate, how to make automation safe, and two long case studies.
  - id: sre-intro
    title: "Benjamin Treynor Sloss, “Introduction”, Google, Site Reliability Engineering, chapter 1"
    url: https://sre.google/sre-book/introduction/
    note: Why a written playbook makes on-call faster than "winging it", and the Wheel of Misfortune practice.
  - id: sre-automation
    title: "Niall Murphy with John Looney and Michael Kacirek, “The Evolution of Automation at Google”, Google, Site Reliability Engineering, chapter 7"
    url: https://sre.google/sre-book/automation-at-google/
    note: Google's five-step path from no automation to systems that need none, and the Diskerase story.
  - id: do-nothing
    title: "Dan Slimmon, “Do-nothing scripting: the key to gradual automation”"
    url: https://blog.danslimmon.com/2019/07/15/do-nothing-scripting-the-key-to-gradual-automation/
    note: A short post with a full Python 2 example of a script that only prints each manual step.
  - id: pd-alerting
    title: "PagerDuty Incident Response documentation: Alerting Principles"
    url: https://response.pagerduty.com/oncall/alerting_principles/
    note: What every alert must contain, including clear steps or a link to a run book.
claims_checked:
  - claim: "Toil is \"the kind of work tied to running a production service\" that tends to be manual, repetitive, automatable, tactical, without enduring value, and grows with the service. Google aims to keep it below 50% of each SRE's time; surveys show about 33% on average, with outliers at 0% and 80%."
    source: sre-toil
    note: "Definition quoted exactly; six properties and the 'not every task has all' caveat match; 50% goal is 'an advertised goal of keeping operational work (i.e., toil) below 50%'."
  - claim: "At 9:37 AM PST on 28 Feb 2017, a command from an established playbook had one input entered incorrectly and removed servers behind the index and placement subsystems, which needed a full restart; S3 was fully back at 1:54 PM PST. AWS changed the tool to remove capacity more slowly and not below any subsystem's minimum required capacity."
    source: aws-s3
    note: "Matches the summary word for word; 'far more' corrected to 'more than intended'; audit of other tools stated as in progress."
  - claim: "A written playbook gives roughly a 3x improvement in MTTR compared with winging it."
    source: sre-intro
  - claim: "Google SRE teams often track toil in bugs and rank it by the cost to fix and the time saved; rejecting the toil is the first option to consider; eliminating toil isn't always the best solution; automation should default to human operators in an unsafe condition."
    source: srew-toil
  - claim: "Diskerase read an empty machine list as 'everything' and wiped all machines in Google's CDN within minutes; Google then added sanity checks including rate limiting and made the workflow idempotent."
    source: sre-automation
  - claim: "A do-nothing script puts each step of a manual procedure in a function that prints it and waits; it lowers the activation energy for automating tasks."
    source: do-nothing
quiz:
  - q: Which of these tasks is toil, by the SRE book's definition?
    options:
      - Writing the design doc for a new cache.
      - Restarting the same stuck worker by hand every few days, more often as traffic grows.
      - The weekly team planning meeting.
    answer: 1
    why: The restart is manual, repetitive, automatable, has no lasting value and grows with the service. The meeting is overhead, and the design doc is engineering work.
  - q: A script takes 20 minutes to run. You spend 5 minutes starting it and checking the result. You do this 10 times a week. How much toil is that?
    options:
      - None, because a script does the work.
      - "About 50 minutes a week: your hands-on time."
      - "About 200 minutes a week: the time the script runs."
    answer: 1
    why: The SRE book counts the hands-on time a human spends running a script as toil, not the elapsed time.
  - q: What is a do-nothing script?
    options:
      - A script that prints each manual step and waits for you to do it.
      - A script that runs in dry-run mode and changes nothing.
      - A cron job that checks if a task is still needed.
    answer: 0
    why: Dan Slimmon describes it as a script that puts each step of a long manual task in a function, and waits for you to do it. Later you can replace any step with real code.
  - q: In the 2017 S3 outage, what did AWS change in the tool that removed servers?
    options:
      - It now needs two people to approve each command.
      - It removes capacity more slowly and refuses to go below a subsystem's minimum capacity.
      - It was deleted, and servers are now removed by hand.
    answer: 1
    why: AWS's summary says the tool now removes capacity more slowly and has safeguards against going below the minimum required capacity.
  - q: Your team spends 3 hours a week on a task. Automating it would take 2 weeks of work. What does Google's workbook suggest first?
    options:
      - Always automate, because all toil is bad.
      - Compare the cost of the fix with the time it saves, and ask if you can reject or remove the task.
      - Never automate a task under 5 hours a week.
    answer: 1
    why: The workbook says eliminating toil "isn't always the best solution". Rank work by the cost to fix and the time saved, and consider rejecting the toil first.
incidents: [aws-2017-02-s3]
related: [alerting-oncall, incident-response, observability-slos, safe-deploys, coding-agents]
---

## What broke

On 28 February 2017 at 9:37 a.m. PST, an S3 engineer at AWS was debugging a slow billing system. They used "an established playbook" and ran a command to remove a few servers. [@aws-s3]

AWS wrote: "one of the inputs to the command was entered incorrectly". The command removed more servers than intended. [@aws-s3]

Those servers ran two S3 parts. One knows where every object is stored, and every request needs it. The other picks where new objects go. Both had to restart fully. [@aws-s3] S3 was not fully back until 1:54 p.m. [@aws-s3]

The engineer followed a written runbook. AWS's fixes were not to the runbook or the person. They were to the tool, which AWS said "allowed too much capacity to be removed too quickly". [@aws-s3]

AWS changed the tool to remove capacity more slowly. It also refuses to take any subsystem below its minimum capacity. AWS said it was also auditing its other operational tools for similar safety checks. [@aws-s3]

This lesson is about that path: from a written runbook, to a script, to automation. Each step saves time. Each step also needs limits.

## The idea in simple words

You met runbooks in [Alerting and on-call](/learn/alerting-and-on-call). This lesson is about the work behind them.

Google's SRE book defines **toil** as "the kind of work tied to running a production service" that has six properties. [@sre-toil] Not every toil task has all six. The more of them a task has, the more likely it is toil. [@sre-toil]

- **Manual.** Running a script by hand still counts. Your hands-on time is toil, not the time the script runs. [@sre-toil]
- **Repetitive.** The first or second time is not toil. Doing it again and again is. [@sre-toil]
- **Automatable.** A machine could do it as well as you. If it needs human judgement, it is probably not toil. [@sre-toil]
- **Tactical.** It is driven by interrupts, not by a plan. Handling pages is toil. [@sre-toil]
- **No lasting value.** After the task, the service is in the same state as before. [@sre-toil]
- **Grows with the service.** Twice the traffic or users means twice the work. [@sre-toil]

Some work is not toil. Meetings, goal setting and HR paperwork are **overhead**. [@sre-toil] Hard cleanup that makes the service better for good is not toil either. [@sre-toil]

Google's SRE teams aim to keep toil below 50% of each person's time. [@sre-toil] In Google's quarterly surveys, the average was about 33%. Some people reported 0% and some 80%. [@sre-toil] The book warns that too much toil leads to burnout, boredom and discontent. [@sre-toil]

> [!key] **The key idea.** A good runbook makes manual work fast and safe. Then measure that manual work, and turn the biggest pieces into code, one step at a time. Give every piece of automation limits, so one mistake cannot spread.

## How it works

### 1. Write a runbook people can follow at 3 a.m.

Google found that a written "playbook" gives "roughly a 3x improvement" in time to repair, compared with "winging it". [@sre-intro] As you saw in the alerting lesson, PagerDuty says alerts with neither steps nor a runbook link "are useless". [@pd-alerting]

Our advice: a runbook needs these parts, in this order.

```text
# Runbook: DiskFull on app workers
Owner: payments-platform   Last tested: 2026-10-14 (game day)

Impact: uploads fail on this host. Users see "try again".
Quick check (30 s):
  ssh <host> df -h /var/log
  If use < 90%: the alert was a blip. Stop.

Fix:
  1. ssh <host> 'sudo find /var/log/app -type f -exec du -h {} + | sort -h | tail -5'
  2. If the biggest files are old *.gz logs:
       ssh <host> 'sudo find /var/log/app -type f -name "*.gz" -mtime +7 -delete'
  3. Else: do not delete. Escalate to #payments-oncall.

Undo: you cannot undo the delete on the host.
  Copies of these logs stay in the log store for 30 days.
```

- **Impact first.** The reader learns how bad it is before anything else.
- **A quick check.** It tells the reader if they can stop.
- **Commands you can copy and paste.** No "check the disk somehow".
- **Decision points.** Say what to do in each case, including "stop and escalate".
- **Rollback.** Say how to undo each step, or say clearly that you cannot.
- **Owner and last-tested date.** A runbook with no owner goes stale.

### 2. Keep runbooks alive

Runbooks rot. Commands change, hosts get new names, dashboards move. Google's automation chapter calls this "bit rot" when it happens to automation kept apart from its system: the system changes and the automation does not. [@sre-automation] A runbook kept apart from the system has the same risk.

Our advice: test runbooks on a **game day**. Break a test system on purpose and let a new team member follow the runbook alone. Fix every step where they get stuck. Google also uses practice exercises, such as the "Wheel of Misfortune", to prepare engineers for on-call. [@sre-intro]

### 3. Measure toil before you fix it

Google's workbook says intuition is not "repeatable, objective, or transferable". [@srew-toil] So measure.

- **Pick a unit.** Minutes and hours are the natural choice. Count the cost of context switching too. [@srew-toil]
- **Track it over time.** Measure before, during and after a fix. [@srew-toil]
- **Rank it.** Google SRE teams often track toil in bugs and rank it by "the cost to fix it and the time saved". [@srew-toil]

The workbook also says eliminating toil "isn't always the best solution". [@srew-toil] Sometimes the best first move is to reject the task. Or batch it, to cut interrupts and see patterns. [@srew-toil]

### 4. Climb the automation ladder

The ladder below is our own framing. Google's SRE book describes a different five-step path. It is about where automation lives: from "No automation", through scripts kept outside the system, to "Systems that don't need any automation". [@sre-automation]

[[diagram:toil-automation-ladder]]

**Doc to script.** Google's workbook says runbook steps are "essentially pseudocode" to a software engineer. [@srew-toil] A good first script is a **do-nothing script**. Dan Slimmon describes it as a script that puts each step of a manual task in a function. [@do-nothing] At first, each function only prints the step and waits. [@do-nothing]

```python
#!/usr/bin/env python3
"""Do-nothing script: free disk space on an app worker.
Each step only prints what to do, then waits for you."""


def check_disk(host):
    print(f"1. Run: ssh {host} df -h /var/log")
    print("   If use is below 90%, stop here.")


def find_big_logs(host):
    print(f"2. Run: ssh {host} 'sudo find /var/log/app -type f -exec du -h {{}} + | sort -h | tail -5'")


def delete_old_logs(host):
    print("3. Only if the biggest files are old *.gz logs. Otherwise do not delete: escalate to #payments-oncall.")
    print(f"   Run: ssh {host} 'sudo find /var/log/app -type f -name \"*.gz\" -mtime +7 -delete'")


def main():
    host = input("Host name: ")
    for step in (check_disk, find_big_logs, delete_old_logs):
        step(host)
        input("   Done? Press Enter to continue, or Ctrl+C to stop. ")


if __name__ == "__main__":
    main()
```

It looks useless, but you lose your place less often. And you can replace one step at a time with real code. Slimmon says it "lowers the activation energy for automating tasks". [@do-nothing]

**Script to self-service.** Let other teams run the task themselves. The workbook suggests a web form, script, API or documented pull request. [@srew-toil]

**Self-service to auto-remediation.** The alert runs the fix with no human. Our view: this step removes the most human time, and it carries the most risk.

The diagram leaves out self-service, because it moves the work to other teams rather than removing it.

**Fix the cause.** The workbook calls removing toil at its source "the optimal strategy". [@srew-toil]

### 5. Give automation limits

Automation repeats a mistake as fast as it repeats a fix. Google's SRE book tells a story from its own CDN. A disk-erase job got an empty list of machines. The empty list was read as "everything". [@sre-automation] "Within minutes", it wiped the disks of all machines in Google's CDN. [@sre-automation] Afterwards, Google added more sanity checks, including rate limiting, and made the workflow idempotent. [@sre-automation]

The workbook gives clear rules: [@srew-toil]

- Check input carefully, even input from other systems.
- Add safeguards, such as timeouts or checks of current metrics.
- "Automation should default to human operators if it runs into an unsafe condition."

AWS's fix after S3 follows the same idea: go slowly, and refuse to go below a minimum. [@aws-s3]

## Trade-offs

| Step | Human time per run | Cost to build | Main risk |
|---|---|---|---|
| Runbook | High. | Low. | Goes stale. People skip or mistype steps. |
| Do-nothing script | Same as the runbook. | Very low. | Feels like no progress, so people stop. |
| Script run by a person | Low. | Medium. | One wrong input acts on many hosts, as in the S3 outage. |
| Self-service tool | None for your team. | Medium to high. | Other teams use it in ways you did not plan for. |
| Auto-remediation | None, unless it pages a person. | High. | It hides a growing problem, or repeats a mistake at machine speed. |
| Fix the cause | None. The task is gone. | Often highest. | Takes the longest, so the toil continues until it ships. |

## At work: how a senior uses this

- Link a runbook from every page. If an alert has no runbook, write one in the next postmortem.
- Put an owner and a "last tested" date at the top. Our advice: treat a runbook not tested in 6 months as untrusted.
- Keep a toil log for a month. Each entry has the task, minutes spent and what triggered it.
- Rank the log by minutes per month and by cost to fix. Bring the top three to planning.
- Ask "can we stop doing this?" before "can we automate this?". The workbook puts rejecting the task first. [@srew-toil]
- In code review of any automation, ask: what is the most it can change at once? What happens with empty or wrong input? When does it stop and page a person?
- Watch for growth. If a task doubles when traffic doubles, it will not wait for you to have time.

## With AI

### What to ask the agent

> "Here is our Slack thread and timeline from last night's DiskFull incident. Draft a runbook with: impact, a 30-second quick check, copy-paste commands, decision points, rollback, owner and a last-tested field. Mark any command you are not sure about."

> "You are on call with read-only access. For the alert DiskFull on worker-7, run only read commands: df, du, ls, journalctl. Show me each command and its output. Propose a fix, but do not run any command that changes or deletes anything."

> "Turn this runbook into a do-nothing script in Python. Each step is a function that prints the exact command and waits for Enter."

### What to check yourself

- Run every command the agent wrote on a test host first. Agents can invent flags or paths.
- Check that "read-only" is enforced by permissions, not only by the prompt. Our advice: give the agent a read-only account, and approve every change yourself.
- Check delete and restart commands for limits: a path, an age, a host list, never "all".
- Make sure the runbook says how to undo each step.
- See [Working with coding agents](/learn/working-with-coding-agents) for permission settings and review habits.

## Check yourself

## Go deeper

Before this: [Alerting and on-call](/learn/alerting-and-on-call) covers which alerts should page and how to run a fair rota.

Related: [Incident response](/learn/incident-response-and-postmortems) covers the team side of an incident, and postmortem action items are a good source of toil to remove. [Safe deploys](/learn/safe-deploys-config-changes) applies the same "go slowly, with limits" idea to changes. [Observability and SLOs](/learn/observability-slos-error-budgets) helps you decide which work actually matters to users.
