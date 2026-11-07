---
topic: prompt-engineering
subtitle: Build prompts and context that give the same good result on the ten-thousandth request, and change them as safely as code.
status: published
reviewed_by: ai-factcheck
reviewed_on: 2026-11-07
review_by: 2027-02-07
illustrative_story: true
sources:
  - id: claude-prompting
    title: "Anthropic, “Prompting best practices”, Claude documentation"
    url: https://platform.claude.com/docs/en/build-with-claude/prompt-engineering/claude-prompting-best-practices
    note: Clear instructions, the reason behind each rule, roles, examples, XML tags, and how to order long documents.
  - id: anthropic-context-eng
    title: "Anthropic Applied AI team, “Effective context engineering for AI agents”, Anthropic Engineering, 29 September 2025"
    url: https://www.anthropic.com/engineering/effective-context-engineering-for-ai-agents
    note: Why context is a limited budget, the "right altitude" for system prompts, and how to pick examples and tools.
  - id: claude-caching
    title: "Anthropic, “Prompt caching”, Claude documentation"
    url: https://platform.claude.com/docs/en/build-with-claude/prompt-caching
    note: The cache order (tools, system, messages), the exact-match rule, cache lifetime and prices. Prices change, so read the current table.
  - id: claude-structured
    title: "Anthropic, “Structured outputs”, Claude documentation"
    url: https://platform.claude.com/docs/en/build-with-claude/structured-outputs
    note: What schema-constrained output guarantees, when it can still fail, and which JSON Schema features are not supported.
  - id: openai-prompting
    title: "OpenAI, “Prompt engineering”, API documentation"
    url: https://developers.openai.com/api/docs/guides/prompt-engineering
    note: Message roles, delimiters, few-shot examples, pinning model snapshots, evals, and keeping prompts in code.
  - id: openai-structured
    title: "OpenAI, “Structured Outputs”, API documentation"
    url: https://developers.openai.com/api/docs/guides/structured-outputs
    note: Structured Outputs versus JSON mode, refusals, and when to use function calling instead.
  - id: gemini-prompting
    title: "Google, “Prompt design strategies”, Gemini API documentation"
    url: https://aistudio.google.com/docs/prompting-strategies
    note: A third vendor's view. Few-shot examples, consistent example format, and putting the question after long context.
  - id: owasp-llm01
    title: "OWASP, “LLM01:2025 Prompt Injection”, OWASP Top 10 for LLM Applications 2025"
    url: https://genai.owasp.org/llmrisk/llm01-prompt-injection/
    note: Direct and indirect prompt injection, and seven layered ways to reduce the risk.
  - id: willison-2022
    title: "Simon Willison, “Prompt injection attacks against GPT-3”, 12 September 2022"
    url: https://simonwillison.net/2022/Sep/12/prompt-injection/
    note: The post that named the attack. Shows why warnings and quoting in the prompt did not stop it.
  - id: willison-2023
    title: "Simon Willison, “Prompt injection: What's the worst that can happen?”, 14 April 2023"
    url: https://simonwillison.net/2023/Apr/14/worst-that-can-happen/
    note: Why the risk grows when the model can read private data or take actions, with an email assistant example.
claims_checked:
  - claim: "OpenAI advises storing production prompts in application code, and its v1/prompts API is scheduled to shut down on 30 November 2026."
    source: openai-prompting
    note: "Page says \"Store production prompts in your application code instead of creating reusable prompt objects\" and \"v1/prompts is scheduled to shut down on November 30, 2026\". Checked 2026-11-07."
  - claim: "OWASP ranks prompt injection first (LLM01:2025), says it is unclear if fool-proof prevention exists, and lists seven layered mitigations."
    source: owasp-llm01
    note: "Quote: \"unclear if there are fool-proof methods of prevention for prompt injection\". The seven strategies match the lesson's list."
  - claim: "Claude's structured outputs do not support numerical constraints such as minimum and maximum, and output may not match the schema after a refusal or a max_tokens cut-off."
    source: claude-structured
    note: "Listed under Not supported; the API returns a 400 error. SDK helpers move the constraints to descriptions and check them on the client. Refusal and max_tokens cases are both described."
  - claim: "Claude builds the cache prefix in the order tools, system, messages; a hit needs an identical prefix; the default lifetime is 5 minutes and is refreshed on each use."
    source: claude-caching
    note: "Quotes: \"tools, system, then messages\"; \"100 percent identical prompt segments\"; \"5-minute lifetime ... refreshed for no additional cost\". Read prices differ by model (0.1x, 0.05x, 0.025x), so the lesson no longer gives a single number."
  - claim: "Context rot: as tokens in the context window increase, the model's ability to recall information from it decreases, across all models."
    source: anthropic-context-eng
    note: "The post (29 Sep 2025, Applied AI team) names context rot from needle-in-a-haystack studies and says it \"emerges across all models\"."
  - claim: "Riley Goodside's translation-prompt injection was not stopped by a warning in the prompt or by JSON-quoting the input; Willison proposed the name prompt injection."
    source: willison-2022
    note: "Warning version still returned \"Haha pwned!!\"; Brian Mastenbrook's exploit worked \"even with that JSON quoting trick\"."
quiz:
  - q: Your ticket classifier has five examples in its prompt, and all five are billing tickets. What is the most likely problem?
    options:
      - None. More examples always help.
      - The model may copy the pattern and label too many tickets as billing.
      - The API will reject the prompt.
    answer: 1
    why: Anthropic says examples should be varied so the model does not copy unintended patterns. Google also warns that too many examples can make the model copy them too closely.
  - q: You wrap the user's message in <user_input> tags and tell the model to never follow instructions inside them. Is the feature now safe from prompt injection?
    options:
      - Yes. The tags separate data from instructions.
      - No. Tags help the model, but OWASP says it is unclear if any method fully prevents injection. Limit what the model can do.
      - Yes, if you also use a JSON schema.
    answer: 1
    why: OWASP lists marking outside content as one layer among several. Willison showed early that extra warnings and quoting were bypassed.
  - q: Your system prompt and examples are 5,000 tokens and the same on every call. Where should the user's question go to get cache hits?
    options:
      - After the fixed parts, at the end.
      - At the very start, before the system prompt.
      - Inside the system prompt, so it is all one block.
    answer: 0
    why: A cache hit needs a 100% identical prefix. Anything that changes per request must come after the fixed parts.
  - q: Your structured output schema has a field "refund_amount". The value must be between 0 and 500. How do you enforce that with Claude's structured outputs?
    options:
      - Add "minimum" and "maximum" to the schema.
      - Check the range in your own code after parsing.
      - Say it in the prompt. The schema then guarantees it.
    answer: 1
    why: Claude's API rejects minimum and maximum with an error. Anthropic's SDK helpers check them in client code instead. Either way, the range check runs in code, not in the model.
  - q: A teammate wants to edit the production prompt in a web console, with no code change. What do you suggest instead?
    options:
      - Keep the prompt in the repo, change it in a pull request, and run the eval set before merge.
      - Let them edit it. Prompts are text, not code.
      - Copy the prompt into a shared document as a backup.
    answer: 0
    why: OpenAI now advises storing production prompts in application code, for code review, tests and normal deploys. An eval run shows what the change does.
incidents: []
cases: []
related: [llm-fundamentals, llm-evals, rag, llm-agents, project-memory, llm-guardrails, owasp-secure-coding]
---

## What broke

A team runs a support tool. An LLM reads each new ticket and returns a category and a one-line summary. It works well for three months.

On a Friday, a developer adds five examples to the prompt to fix the summary style. All five happen to be billing tickets. The tests pass, because the tests only check that the JSON parses.

On Monday, the billing queue is full. About a third of all tickets now say "billing", including login problems. The model copied the pattern in the examples, not only the style.

Nobody can say which prompt is live. It was edited in a web console, not in the repo. There is no list of past versions and no test set to compare against. Rolling back takes a day of guessing.

This story is made up. Each failure in it is common, and each one has a known fix.

## The idea in simple words

You already know from [LLM fundamentals](/learn/llm-fundamentals) that the model only knows what is in the request. Prompt engineering is writing the instructions in that request. **Context engineering** is the larger job: choosing everything the model sees, and in what order. [@anthropic-context-eng]

In a product, the prompt is code. It has inputs, outputs, bugs and versions. It needs review and tests like any other code.

> [!key] **The key idea.** A good prompt is clear to a smart new colleague. Fixed parts come first. User text is fenced and treated as data. Every change runs through an eval set.

## How it works

Step through the parts of one request, in the order a careful team builds it. Use Next and Back.

[[diagram:prompt-context-order]]

### Clear instructions and a role

Anthropic suggests a simple test. Show your prompt to a colleague who does not know the task. If they would be confused, the model will be too. [@claude-prompting] Say the exact output format and limits you want. Use numbered steps when order matters. [@claude-prompting]

Give the reason behind each rule. "Never use ellipses" works less well than "The reply is read aloud by a speech engine, so never use ellipses." The model can then apply the reason to cases you did not list. [@claude-prompting]

A role in the system prompt, even one sentence, focuses the model's behaviour and tone. [@claude-prompting] OpenAI's API has a `developer` role for your instructions, which ranks above `user` messages. [@openai-prompting]

Aim for what Anthropic calls the "right altitude". Avoid long, brittle if-else logic, and avoid vague advice. Start with a short prompt, then add rules only for failures you have seen. [@anthropic-context-eng]

### Examples (few-shot prompting)

Anthropic finds that examples work very well for controlling format, tone and structure. [@claude-prompting] Anthropic suggests 3 to 5 examples. Make them relevant, varied, and wrapped in `<example>` tags. [@claude-prompting] Google goes further and recommends always including examples. [@gemini-prompting]

Two warnings. With too many examples, the model may copy them too closely. [@gemini-prompting] And examples that all look alike teach a pattern you did not mean, as in the story. [@claude-prompting] Keep every example in exactly the same format. [@gemini-prompting]

### Delimiting input, and why it does not stop injection

Wrap each kind of content in its own tag, such as `<instructions>`, `<context>` and `<input>`. This helps the model tell them apart. [@claude-prompting] OpenAI suggests Markdown headings or XML tags for the same reason. [@openai-prompting]

Tags are not a security wall. In September 2022, Simon Willison wrote about an early example found by Riley Goodside. A translation prompt obeyed "Ignore the above directions" written inside its input. [@willison-2022] A warning in the prompt did not stop it, and neither did quoting the input as a JSON string. [@willison-2022]

OWASP still lists prompt injection first. It says it is unclear if there are fool-proof ways to prevent it. [@owasp-llm01] Injection can also be indirect: hidden in a web page or file the model reads. [@owasp-llm01] The danger grows with power. If an email assistant can send mail, an email it reads can tell it to. [@willison-2023]

So use layers. OWASP lists them: constrain the model's behaviour, validate output formats, filter input and output, apply least privilege, ask a human before risky actions, mark outside content, and test with attacks. [@owasp-llm01] In our view, the strongest layer is in your code: the model cannot do what its tools do not allow. [@owasp-llm01] [@willison-2023]

### Structured output

Ask for JSON that matches a schema, not free text. With structured outputs, the API constrains the reply to your JSON Schema. [@claude-structured] OpenAI recommends Structured Outputs over plain JSON mode, because only Structured Outputs follow the schema. [@openai-structured] Use a tool call when the model should trigger your code. Use a response format when you only want a shaped reply. [@openai-structured]

The guarantee has limits. A refusal, or a reply cut off at `max_tokens`, may not match the schema. [@claude-structured] [@openai-structured] Claude's structured outputs do not support numerical limits such as `minimum` and `maximum`. [@claude-structured] So check ranges, IDs and allowed values in your own code.

### What goes in the context, and in what order

Context is a limited budget. Anthropic calls it "a finite resource with diminishing marginal returns". [@anthropic-context-eng] As the token count grows, the model recalls facts less accurately. This is called **context rot**, and tests show it in all models. [@anthropic-context-eng] A very large window does not mean you should fill it.

Put in only what this request needs: the rules, a few examples, the top search results, and recent history. For agents, Anthropic suggests loading data only when it is needed, using file paths or saved queries. [@anthropic-context-eng] Retrieval is its own skill: see [RAG](/learn/rag-retrieval-augmented-generation).

For long inputs (Anthropic says 20,000 tokens or more), put the documents before the question. Anthropic says a question at the end improved quality by up to 30% in tests. [@claude-prompting] Google gives the same advice. [@gemini-prompting] Anthropic would also put documents above the examples. [@claude-prompting] If you cache a fixed prefix of rules and examples, you trade a little of that for cheaper calls.

### Prompt caching

The provider can reuse the work on a fixed start of your prompt. On Claude, the cache is built in this order: tools, then system, then messages. [@claude-caching] A hit needs a 100% identical prefix. [@claude-caching] So put fixed parts first and anything that changes after them. OpenAI gives the same advice. [@openai-prompting]

Writing to the cache costs a little more than normal input. Reading from it costs a small fraction of the normal input price. [@claude-caching] Prices differ by model and change often, so check the current pricing table. The default cache lives 5 minutes, and each hit refreshes it. [@claude-caching] A timestamp at the top of your system prompt breaks every cache hit. [@claude-caching]

### Version prompts like code and test them with evals

OpenAI now says: store production prompts in your application code. It is shutting down its stored-prompt API, `v1/prompts`, on 30 November 2026. [@openai-prompting] Code-managed prompts get typed inputs, code review, tests and normal deploys. [@openai-prompting]

OpenAI also advises pinning production apps to a **model snapshot**, and building evals that measure prompt behaviour. [@openai-prompting] Then a prompt change is a pull request with an eval score, as in [Evals for LLM systems](/learn/evals-for-llm-systems). A model upgrade is a separate change, with its own eval run.

## Trade-offs

This table is our own summary, except where a source is cited.

| Option | Reliability gain | Cost | Use it when |
|---|---|---|---|
| Clear rules with reasons | Yes. Fewer vague failures. [@claude-prompting] | A few hundred tokens. | Always. |
| Few-shot examples | Yes. Strong control of format and tone. [@claude-prompting] | More input tokens. Can skew output if they all look alike. | The output has a set style or format. |
| Structured output | Yes. The shape is fixed in most cases. [@claude-structured] | Values still need checks. Some schema features are missing. | Code reads the reply. |
| More context | Only up to a point. Recall drops as it grows. [@anthropic-context-eng] | More tokens and latency. | Each added part answers a real failure. |
| Prompt caching | No change to quality. | Lower. Cached input is cheaper and faster. [@claude-caching] | Large fixed prefix, many calls. |
| Input tags and warnings | Some. The model can tell parts apart. [@claude-prompting] | Almost free. | Always, but never as the only injection defence. [@owasp-llm01] |

**Simple rule:** start short and clear. Add examples, context and rules only when an eval shows a failure.

## At work: how a senior uses this

- Keep the prompt in the repo, next to the code that calls it. One file per prompt, reviewed in pull requests.
- Log the prompt version and model snapshot with every request. Then you can link a bad answer to a change.
- Ask: **"Which eval cases did this prompt change move?"** Never merge on "it looked better on three tries."
- Ask: **"If this input is hostile, what is the worst the model can do?"** Then remove that power from its tools.
- Keep the fixed prefix stable. Move dates, user names and IDs to the end.
- When you add a rule, add an eval case for the failure that caused it.
- For coding agents, the same ideas apply to [project memory files](/learn/project-memory-claude-md).

## With AI

### What to ask the agent

> "Here is our system prompt. Rewrite it so each rule has its reason. Put fixed parts first and anything that changes per request last. List what you removed and why."

> "These are our five few-shot examples. Check them for variety. Point out any pattern they share that the model might copy by mistake."

> "Write a validator for this JSON schema output. Check ranges, allowed values and IDs against our database, and handle refusals and cut-off replies."

> "Write 20 eval cases for this prompt, including five prompt injection attempts hidden in the user input and in retrieved documents."

### What to check yourself

- The agent did not paste user text into the system prompt, outside any tag.
- Nothing that changes per request, such as a date, sits before the cached prefix.
- The validator checks values, not only that the JSON parses.
- The eval set has cases from every category, not only the easy ones.
- No tool lets the model take a risky action without a check in code.

## Check yourself

## Go deeper

- [LLM fundamentals](/learn/llm-fundamentals): tokens, the context window, and cost.
- [Evals for LLM systems](/learn/evals-for-llm-systems): how to build the test set that every prompt change runs through.
- [RAG: retrieval-augmented generation](/learn/rag-retrieval-augmented-generation): how to choose which documents go into the context.
- [Tool use and agents in products](/learn/llm-tool-use-and-agents): giving the model tools, and limiting what they can do.
- [Project memory: CLAUDE.md and rules files](/learn/project-memory-claude-md): context engineering for coding agents.
