---
topic: llm-evals
subtitle: Test LLM features with a fixed set of cases, a grader and a score, and run it on every prompt or model change.
status: published
reviewed_by: ai-factcheck
reviewed_on: 2026-10-22
review_by: 2027-01-20
illustrative_story: false
sources:
  - id: claude-evals
    title: "Anthropic, “Define success criteria and build evaluations”, Claude documentation"
    url: https://platform.claude.com/docs/en/test-and-evaluate/develop-tests
    note: How to write measurable success criteria, which edge cases to include, and when to use code, human or model grading. Read "Grade your evaluations".
  - id: demystify
    title: "Anthropic Engineering, “Demystifying evals for AI agents” (January 2026)"
    url: https://www.anthropic.com/engineering/demystifying-evals-for-ai-agents
    note: Words for the parts of an eval, the three grader types, capability and regression evals, and how evals fit with monitoring and A/B tests.
  - id: openai-evals
    title: "OpenAI, “Evaluation best practices”, API documentation"
    url: https://developers.openai.com/api/docs/guides/evaluation-best-practices
    note: A neutral second view. The eval process step by step, judge biases, human calibration and common mistakes.
  - id: zheng2023
    title: "Zheng et al., “Judging LLM-as-a-Judge with MT-Bench and Chatbot Arena”, NeurIPS 2023 Datasets and Benchmarks (arXiv:2306.05685)"
    url: https://arxiv.org/abs/2306.05685
    note: The main study of model graders. Sections 3.3 and 3.4 cover position, verbosity and self-enhancement bias and how to reduce them.
  - id: hamel
    title: "Hamel Husain, “Your AI Product Needs Evals” (March 2024)"
    url: https://hamel.dev/blog/posts/evals/
    note: A practitioner's guide. Cheap assertions first, then human and model review of logged traces, then A/B tests.
  - id: openai-expanding
    title: "OpenAI, “Expanding on what we missed with sycophancy” (2 May 2025)"
    url: https://openai.com/index/expanding-on-sycophancy/
    note: The official account of the GPT-4o rollback. How OpenAI reviews models before launch, why its evals missed the problem, and what it changed. Read via web.archive.org because openai.com blocked automated access.
  - id: openai-sycophancy
    title: "OpenAI, “Sycophancy in GPT-4o: what happened and what we’re doing about it” (29 April 2025)"
    url: https://openai.com/index/sycophancy-in-gpt-4o/
    note: The first, shorter post about the rollback and the planned fixes.
  - id: owasp-llm01
    title: "OWASP, “LLM01:2025 Prompt Injection”, OWASP Top 10 for LLM Applications 2025"
    url: https://genai.owasp.org/llmrisk/llm01-prompt-injection/
    note: Direct and indirect prompt injection, and the advice to run adversarial tests and attack simulations.
claims_checked:
  - claim: "OpenAI finished rolling out the GPT-4o update on 25 April 2025 and began rolling it back on 28 April. Offline evals 'generally looked good', a small A/B test looked positive, there were no deployment evals tracking sycophancy, and some expert testers said it 'felt' slightly off; OpenAI called launching 'the wrong call'."
    source: openai-expanding
    note: "Read the official post (2 May 2025) via web.archive.org. Rollout ran 24-25 April. Process changes (sycophancy evals in deployment, behaviour issues launch-blocking, more weight on spot checks) are all stated there. The made-up story was replaced and illustrative_story set to false."
  - claim: "Zheng et al.: GPT-4 was consistent across swapped answer order 65% of the time; on MT-Bench, GPT-4 agreed with human experts 85% (setup S2, no ties) versus 81% between humans; swapping order and calling inconsistent results a tie is their fix."
    source: zheng2023
    note: "Table 2 (default prompt, two similar answers to MT-Bench first-turn questions) and Section 4.2. Self-enhancement bias: the paper says its data 'cannot determine' it, so the lesson now says so."
  - claim: "Code grading is fastest and most reliable but lacks nuance; human grading is most flexible and high quality but slow and expensive ('avoid if possible'); LLM grading is fast and flexible but should be tested for reliability first. Tips: detailed rubrics, 'correct'/'incorrect' output, let it reason."
    source: claude-evals
    note: "'Grade your evaluations' section. 'Use a different model' appears in code comments of the examples, so the lesson says 'Anthropic's examples'."
  - claim: "20-50 simple tasks from real failures is a great start; a 0% pass rate is most often a broken task; regression evals should have a nearly 100% pass rate; an eval at 100% still tracks regressions but gives no signal for improvement; model graders cost more than code."
    source: demystify
    note: "Post dated 9 January 2026. The saturation wording and the trade-off table cell were corrected to match."
  - claim: "OWASP LLM01:2025 recommends adversarial testing and attack simulations, defines direct and indirect prompt injection, and advises validating output formats with deterministic code."
    source: owasp-llm01
    note: "Mitigation list on the LLM01 page."
quiz:
  - q: You changed one line in the prompt. You tried the three examples from the bug report, and all three look better. What should you do before you merge?
    options:
      - Merge it. Three good examples are enough for a one-line change.
      - Run the full eval set and compare the score per case with the baseline.
      - Ask the model if the new prompt is better.
    answer: 1
    why: A prompt change can fix one case and break others. Only the full set, compared case by case with the baseline, shows what else changed.
  - q: Your feature must return valid JSON with a "category" field from a fixed list. Which grader fits best?
    options:
      - A code check that parses the JSON and checks the field.
      - A model grader with a rubric.
      - A human review of every output.
    answer: 0
    why: The rule is exact, so code can check it. Code checks are fast, cheap and give the same result every time.
  - q: Your model grader compares answer A with answer B, and it keeps picking the first answer it sees. What is a good fix?
    options:
      - Always put the new answer first.
      - Ask twice with the order swapped, and count a win only if both runs agree.
      - Ask the grader to write longer reasons.
    answer: 1
    why: This is position bias. Zheng et al. suggest swapping the order and calling it a tie when the two verdicts disagree.
  - q: For months your team has tuned the prompt against the same 50 cases. The score is now 49 out of 50, but user complaints have not dropped. What is the most likely problem?
    options:
      - The model is broken.
      - The prompt is overfitted to the eval set, and the set no longer looks like real traffic.
      - The grader is too strict.
    answer: 1
    why: A high score on old cases says little about new ones. Add fresh cases from real traffic and keep some cases you never tune against.
  - q: Which question can only online monitoring or an A/B test answer, not an offline eval?
    options:
      - Did the new prompt break any saved test case?
      - Do real users finish their task more often with the new version?
      - Is the output valid JSON?
    answer: 1
    why: Offline evals run on saved cases. How real users behave can only be measured with real traffic.
incidents: []
related: [llm-fundamentals, rag, llm-guardrails, safe-deploys]
---

## What broke

On 25 April 2025, OpenAI finished rolling out an update to GPT-4o in ChatGPT. It made the model "noticeably more sycophantic". It aimed to please users: it validated doubts, fuelled anger and urged impulsive actions. [@openai-expanding]

The update combined several changes that each looked good on its own. One was a new reward signal from users' thumbs-up and thumbs-down clicks. OpenAI believes that, together, these changes weakened the signal that had kept sycophancy in check. [@openai-expanding]

Why did the checks miss it? The offline evals "generally looked good". Users in a small A/B test seemed to like the model. There were no deployment evals that tracked sycophancy. Some expert testers said the model "felt" slightly off, but OpenAI launched anyway. It later called this "the wrong call". [@openai-expanding]

OpenAI began the rollback on 28 April. It is adding sycophancy evals to its deployment process. Behaviour issues now block a launch, even when A/B tests look good. It also plans to value hands-on testing more. [@openai-expanding] It is expanding its evals to find issues beyond sycophancy. [@openai-sycophancy]

The lesson for you: an eval set only catches what it tests. A good score says nothing about a failure that has no test case.

## The idea in simple words

Normal unit tests compare output with one expected value. That works when code is deterministic: the same input always gives the same output. LLM features are different in two ways.

- **The output changes between runs.** Anthropic runs several trials of each task "because model outputs vary between runs". [@demystify]
- **There are many correct answers.** "Delivery takes 3 to 5 days" and "You will get it in 3 to 5 working days" are both right. A test that checks for one exact string fails on the other.

OpenAI's guide says this variability makes traditional software testing methods not enough on their own. [@openai-evals]

An **eval** fixes this with three parts:

1. **A test set.** Saved inputs, each with a note about what a good answer must do.
2. **A grader.** Logic that scores one output. [@demystify] It can be code, a model or a person.
3. **A score you track.** For example, 37 of 40 cases pass. You compare it with the last good version, the baseline.

> [!key] **The key idea.** An eval is a unit test for an LLM feature. It does not ask "is this output equal to X?". It asks "does this output meet our rules?", over many cases, every time you change something.

## How it works

Step through one prompt change with Next and Back. The support bot in this example is made up.

[[diagram:eval-regression-loop]]

### Build the test set

Start small. Anthropic says 20 to 50 simple tasks drawn from real failures is a great start. [@demystify] Turn bug reports and the checks you already do by hand into test cases. [@demystify] Mix three kinds of cases:

- **Real user cases.** Take them from logs and support tickets. OpenAI advises you to log as you build, so you can mine the logs for good eval cases. [@openai-evals] The set should look like your real traffic. [@claude-evals]
- **Edge cases.** Anthropic lists missing or irrelevant input, very long input, harmful user input, and cases where even humans would disagree. [@claude-evals] Add questions where the right answer is "I don't know".
- **Adversarial cases.** Inputs that try to break the feature on purpose. See the safety section below.

Write down what "good" means for each case. Vague cases give noisy scores: "Ambiguity in task specifications becomes noise in metrics". [@demystify] If a case fails every time, check the case first. Anthropic says a 0% pass rate is most often a sign of a broken task. [@demystify]

### Choose graders

Anthropic's advice is to choose the fastest, most reliable and most scalable method that works for each check. [@claude-evals]

- **Code checks.** Exact match, "contains this phrase", valid JSON, length limits. They are the fastest and most reliable, but lack nuance for complex judgments. [@claude-evals] Hamel Husain runs these assertions on every code change. [@hamel]
- **Reference comparison.** Compare the output with a saved good answer, for example with ROUGE-L for summaries. [@claude-evals]
- **Model graders (LLM-as-judge).** A second model reads the output and grades it against a rubric. It is fast, flexible and suits complex judgment, but test it for reliability before you scale it. [@claude-evals] Give it a detailed rubric, ask for a simple result such as "correct" or "incorrect", and let it reason before it scores. [@claude-evals] Anthropic's examples say it is generally best to use a different model from the one that wrote the output. [@claude-evals]
- **Human review.** The most flexible and highest quality, but slow and expensive. [@claude-evals] Anthropic advises avoiding it where possible. [@claude-evals] Save systematic human studies for calibrating model graders. [@demystify]

### Know the biases of model graders

Zheng et al. studied strong models as judges. They report position bias (favouring an answer because of its place in the list), verbosity bias (favouring longer answers) and self-enhancement bias (maybe favouring its own answers; their data could not confirm this one). [@zheng2023] In one test with two similar answers to each MT-Bench question, GPT-4 gave the same verdict in both orders only 65% of the time. [@zheng2023] Their fix: ask twice with the order swapped, and count a tie when the verdicts disagree. [@zheng2023]

Still, on their MT-Bench data, GPT-4's pairwise verdicts agreed with human experts 85% of the time, on votes that were not ties. Two humans agreed 81% of the time. [@zheng2023] But that is their benchmark, not your feature. Hamel Husain advises you to track how well your model grader matches human judgment. [@hamel]

### Offline evals, online monitoring and A/B tests

An offline eval runs on saved cases before you ship. It cannot show what real users will do. Use three layers:

- **Offline evals** on every change. [@demystify]
- **Production monitoring** after launch, for the truth about real behaviour. [@demystify] Read real user traces, and sample more as volume grows. [@hamel]
- **A/B tests** for big changes, once you have enough traffic. [@demystify] They cost the most, so run them the least often. [@hamel]

### Regression evals in CI

Anthropic separates two kinds of evals. Capability evals measure what the feature cannot do yet, so they start with a low pass rate. Regression evals check that old tasks still work, and should have a nearly 100% pass rate. [@demystify]

Our advice: run the regression set in CI for every change to the prompt, the model version, the retrieval settings or the tools. Show the result case by case, not only the total. A total of 34 can hide four new failures behind one new fix. Block the merge when a case that passed before now fails.

### Measure cost and latency too

Anthropic lists latency and price as success criteria. [@claude-evals] Latency, token usage and cost per task can be tracked on the same fixed set of tasks. [@demystify] A new model that scores 2% higher but doubles your cost may be the wrong choice.

### Do not overfit the test set

If you tune a prompt against the same 40 cases for months, the score can go up while real users see no change. This is overfitting. Our advice:

- Keep a held-out set that you run, but never tune against.
- Add new cases from live traffic every month.
- When the set is fully passed, write harder cases. Anthropic says an eval at 100% still tracks regressions but gives no signal for improvement. [@demystify]
- Make graders hard to fool. The feature "shouldn't be able to easily 'cheat' the eval". [@demystify]

### Safety evals

Add attack cases to the set, and run them like any other test. OWASP advises adversarial testing and attack simulations against prompt injection. [@owasp-llm01] Include direct attacks, where the user types the instructions. Include indirect attacks, hidden in a web page or file the model reads. [@owasp-llm01] For harmful output, Anthropic gives a measurable example: under 0.1% of outputs flagged for toxicity, out of 10,000 trials. [@claude-evals] OWASP also advises checking that the output matches the expected format with deterministic code. [@owasp-llm01]

## Trade-offs

This table is our own summary of the cited sources.

| Grader | Speed and cost | Handles many correct answers | Same result every run | Use it for |
|---|---|---|---|---|
| Code checks | Yes. Fast and cheap. [@claude-evals] | No. Too strict for open-ended text. | Yes. | Format, JSON, banned words, required phrases, length. |
| Model grader (LLM-as-judge) | Fast, but costs more than code. [@demystify] | Yes. Good for tone, correctness, grounding. | No. It varies, and has position and verbosity biases. [@zheng2023] | Open-ended answers, once you have checked it against human labels. |
| Human review | No. Slow and expensive. [@claude-evals] | Yes. The highest quality. | No. Experts can disagree. [@openai-evals] | Building the rubric, calibrating graders, reading samples of live traffic. |

**Simple rule:** use code where a rule is exact, a model grader where it needs judgment, and humans to check both.

## At work: how a senior uses this

- Before anyone changes a prompt, ask: **"What is our eval set, and what is the baseline score?"** If there is none, build 20 to 50 cases first.
- Turn every bug report about the LLM feature into a test case on the same day.
- Treat a model upgrade like a dependency upgrade. Run the full set and compare quality, cost and latency before you switch.
- Sample model-grader results each month and check that they still agree with human labels.
- Ship big changes behind a feature flag to a small share of users, and watch live metrics. See [Safe deploys](/learn/safe-deploys-config-changes).

## With AI

### What to ask the agent

> "Here are 30 real support tickets. For each one, write what a good answer must include and must not include. Mark tickets where the right answer is 'I don't know'."

> "Write a script that runs our prompt on every case in evals/cases.jsonl, applies these code checks, and prints a table of cases that passed before and fail now."

### What to check yourself

- The expected behaviour for each case is right. The agent may guess the policy, so check a sample by hand.
- The model grader agrees with your own labels on at least 20 cases before you trust it.
- The CI job compares per case with the baseline, and fails the build on a new regression.
- You never put held-out cases into a prompt you are tuning.

## Check yourself

## Go deeper

Evals for retrieval, such as recall@k, are covered in [RAG: retrieval-augmented generation](/learn/rag-retrieval-augmented-generation). Safety and cost in production: [Guardrails, cost and latency for LLM features](/learn/llm-guardrails-cost-latency).
