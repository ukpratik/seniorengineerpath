---
topic: distributed-tracing
subtitle: A trace shows where one request spent its time across many services. Learn to pass the trace ID along and read the waterfall.
status: published
reviewed_by: ai-factcheck
reviewed_on: 2026-11-09
illustrative_story: false
sources:
  - id: logzio
    title: "Avi Froelich, “Cracking Production Performance Issues with Open Source Observability”, Logz.io blog (23 September 2020)"
    url: https://logz.io/blog/high-latency-performance-observability
    note: A real slowdown at peak hours, and how traces ruled out the obvious suspect before the team found a cache setting.
  - id: w3c-tc
    title: "W3C, Trace Context, W3C Recommendation (23 November 2021)"
    url: https://www.w3.org/TR/trace-context/
    note: The exact format of the traceparent and tracestate headers, and what each service must do when it receives them.
  - id: otel-traces
    title: "OpenTelemetry documentation: Traces"
    url: https://opentelemetry.io/docs/concepts/signals/traces/
    note: Spans, parent IDs, attributes, events, links, status and span kinds, with JSON examples.
  - id: otel-prop
    title: "OpenTelemetry documentation: Context propagation"
    url: https://opentelemetry.io/docs/concepts/context-propagation/
    note: How the trace ID and span ID travel from service A to service B, and how logs pick them up.
  - id: otel-inst
    title: "OpenTelemetry documentation: Instrumentation"
    url: https://opentelemetry.io/docs/concepts/instrumentation/
    note: Zero-code and code-based instrumentation, and why libraries depend only on the API.
  - id: otel-collector
    title: "OpenTelemetry documentation: Collector"
    url: https://opentelemetry.io/docs/collector/
    note: What the Collector does and why the project recommends running one next to your services.
  - id: otel-sampling
    title: "OpenTelemetry documentation: Sampling"
    url: https://opentelemetry.io/docs/concepts/sampling/
    note: Head sampling versus tail sampling, with the costs and limits of each.
  - id: otel-msg
    title: "OpenTelemetry semantic conventions: Semantic conventions for messaging spans"
    url: https://opentelemetry.io/docs/specs/semconv/messaging/messaging-spans/
    note: How a producer attaches context to a message, and why consumers use span links by default.
  - id: otel-metrics-dm
    title: "OpenTelemetry specification: Metrics Data Model"
    url: https://opentelemetry.io/docs/specs/otel/metrics/data-model/
    note: The Exemplars section explains how a metric point can carry a trace ID.
  - id: prom-naming
    title: "Prometheus documentation: Metric and label naming"
    url: https://prometheus.io/docs/practices/naming/
    note: Why every new label value makes a new time series, and why user IDs do not belong in labels.
  - id: dapper
    title: "Benjamin H. Sigelman et al., “Dapper, a Large-Scale Distributed Systems Tracing Infrastructure”, Google Technical Report dapper-2010-1 (April 2010)"
    url: https://research.google/pubs/dapper-a-large-scale-distributed-systems-tracing-infrastructure/
    note: The Google paper behind most modern tracing tools. Read sections 2 and 4 for span trees, thread pools, clock skew and sampling.
claims_checked:
  - claim: "traceparent is version-traceid-parentid-flags: trace-id is 16 bytes as 32 lowercase hex (all zeros invalid), parent-id 8 bytes as 16 hex, version ff invalid, and only the sampled flag is defined; the example header is the spec's own."
    source: w3c-tc
    note: "W3C REC 23 Nov 2021, sections 3.2.2 and 3.2.3; section 7.2 also warns an attacker could 'run up your tracing bill' via the sampled flag."
  - claim: "Head sampling cannot ensure every error trace is kept; tail sampling can keep errors and slow traces but needs stateful components; head-then-tail protects the pipeline at high volume."
    source: otel-sampling
    note: "'cannot ensure that all traces with an error within them are sampled with head sampling alone'; tail samplers 'must be stateful systems'; 'protecting the telemetry pipeline from being overloaded'."
  - claim: "Messaging consumers link to the message creation context by default; links are needed for batches because a span has one parent; using it as parent MAY be done for single messages."
    source: otel-msg
    note: "'the Process or Receive span SHOULD link to the message's creation context'; 'a span can only have a single parent'; 'Exclusively for single messages scenarios ... MAY use the message's creation context as its parent'."
  - claim: "Dapper's first production version sampled about one trace per 1,024, which still gave enough data for high-volume services; trace context lived in thread-local storage and was carried into callbacks; clock skew between hosts."
    source: dapper
    note: "Technical Report dapper-2010-1, April 2010, sections 2.1, 2.2, 4.3 and 4.4."
  - claim: "Logz.io's own peak-hour slowdown (2-7 PM EU/US overlap): traces showed the Elasticsearch query took 15 ms; a two-line change to the session token cache eviction policy cut requests to that service by more than 80%."
    source: logzio
    note: "First-person case study of Logz.io's own production system (not hypothetical): 'the actual Elasticsearch query only took 15ms'; 'two lines of code'; 'cut off by more than 80%'."
  - claim: "Every unique combination of label values is a new time series, so user IDs do not belong in metric labels."
    source: prom-naming
    note: "Labels section: 'every unique combination of key-value label pairs represents a new time series'; 'Do not use labels to store dimensions with high cardinality ... such as user IDs'."
quiz:
  - q: "A service receives `traceparent: 00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01` and calls the next service. What should it send?"
    options:
      - The same header, unchanged.
      - The same trace ID, a new parent ID (its own span ID), and the flags.
      - A new trace ID, because each service starts its own trace.
    answer: 1
    why: The trace ID stays the same for the whole request. The parent ID must change to the ID of the current operation, so the next span knows its parent.
  - q: A trace shows 40 ms between the start of a client span and the start of the matching server span. Nothing else is recorded there. What is the most likely cause?
    options:
      - The database was slow.
      - Waiting that has no span, such as a queue, a proxy or a full thread pool.
      - The trace was sampled badly.
    answer: 1
    why: A gap means time passed that nobody recorded. Queue wait and code without instrumentation are the usual causes. A slow database call would show up as its own span. Clock skew can also shift a server span, so check that if the gap looks odd.
  - q: You want to keep every trace that has an error, while storing only 5% of normal traces. Which approach can do this?
    options:
      - Head-based sampling at 5%.
      - Tail-based sampling.
      - Turning off sampling for errors in the sampled flag.
    answer: 1
    why: Head-based sampling decides at the start, before anyone knows the request will fail. Only a decision made after the trace ends can look for errors.
  - q: In a waterfall, a request takes 500 ms. A cache call takes 20 ms and runs in parallel with a 300 ms pricing call. Nothing else runs at the same time. Which change makes the request faster?
    options:
      - Making the cache call 10 ms faster.
      - Making the pricing call 100 ms faster.
      - Both help the same.
    answer: 1
    why: The pricing call is on the critical path. The cache call already finishes while pricing is still running, so making it faster changes nothing.
incidents: []
related: [observability-slos, debugging-production, tail-latency-queueing, incident-response, queues-vs-logs]
glossary_optout: [process]
---

## What broke

In 2020, Logz.io received reports that its product was slow. It happened mostly between 2 PM and 7 PM, when European and American customers were online together. Their own staff saw the same slowness [@logzio].

Logs showed slow endpoints in that window. They also showed a growing number of calls to an internal service that serves user session tokens from the database. Metrics showed big database commits and replication lag at the same times [@logzio]. Both said *that* something was wrong. Neither said *where*.

The team then filtered traces to show only requests slower than 5 seconds. At peak times, almost every endpoint was slow when it fired a search request [@logzio]. The obvious suspect was Elasticsearch, the search database.

The trace said no. Matched with its logs, it showed that the Elasticsearch query took only 15 ms [@logzio]. With Elasticsearch ruled out, the team found the cause in the session token service: too many requests reached it [@logzio].

The fix was a two-line change to the session token cache's eviction policy. Calls to that service dropped by more than 80%, and latency fell on all affected endpoints [@logzio]. The trace ruled out Elasticsearch, so the team could look elsewhere.

## The idea in simple words

A **trace** is the story of one request as it moves through your services. It is made of **spans**. Each span is one piece of work with a name, a start time and an end time [@otel-traces].

Spans form a tree. The first span is the **root span** and has no parent. Every other span stores its **parent span ID**. All spans in the tree share one **trace ID** [@otel-traces]. Google described this tree of spans in its Dapper paper in 2010 [@dapper]. OpenTelemetry uses the same model today [@otel-traces].

A span can carry more detail [@otel-traces]:

- **Attributes:** key-value pairs, like `cart.items = 5` or `http.route = /checkout`.
- **Events:** a named moment with a timestamp inside the span, like "retry started".
- **Status:** `Unset` (the default, meaning no error), `Error`, or `Ok` when the developer marks success on purpose.
- **Links:** a pointer to another span, often in a different trace, that is causally related to this one.

Your tool draws the spans as a **waterfall**: one bar per span, placed on a time line. Child bars sit under their parents.

> [!key] **The key idea.** Metrics and logs tell you that a request is slow. A trace shows which span used the time. Read the critical path first: it is the only place where saving time makes the request faster.

## How it works

### Passing the trace ID along

For one trace to cover many services, each call must carry the trace ID and the caller's span ID. This is **context propagation** [@otel-prop]. Instrumentation libraries usually do it for you. OpenTelemetry's default uses the W3C Trace Context headers [@otel-prop].

The main header is `traceparent` [@w3c-tc]:

```text
traceparent: 00-4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-01
             │  │                                │                └ trace-flags
             │  │                                └ parent-id (16 hex)
             │  └ trace-id (32 hex)
             └ version
```

- **version** is `00` today. The value `ff` is invalid [@w3c-tc].
- **trace-id** is 16 bytes as 32 lowercase hex characters. All zeros is invalid [@w3c-tc].
- **parent-id** is 8 bytes as 16 lowercase hex characters: the caller's span ID [@w3c-tc].
- **trace-flags** is 8 bits. Only the lowest bit is defined: `01` means the caller may have recorded this trace ("sampled") [@w3c-tc].

When a service receives a valid `traceparent`, it keeps the trace ID. Before it calls the next service, it sets a new parent-id: the ID of its own current span. This is the normal case [@w3c-tc]. If the header is invalid, the service starts a new trace [@w3c-tc]. A second header, `tracestate`, carries vendor-specific data [@w3c-tc].

Step through one checkout request. Use Next and Back, or the ← and → keys.

[[diagram:trace-waterfall-critical-path]]

### Across a queue

A message may wait in a queue for minutes. One consumer may also process many messages in one batch. So OpenTelemetry's messaging rules say the producer should attach a **creation context** to each message, for example in the message headers [@otel-msg]. The consumer then adds a **span link** to it by default, not a parent. A span can have only one parent, but it can link to every message in a batch [@otel-msg]. For one message at a time, using the producer as parent is allowed [@otel-msg]. See [Queues vs logs](/learn/queues-vs-logs-delivery-guarantees) for how messages move.

```python
from opentelemetry import trace
from opentelemetry.propagate import inject, extract

tracer = trace.get_tracer("orders")

with tracer.start_as_current_span("publish order", kind=trace.SpanKind.PRODUCER):
    headers = {}
    inject(headers)                 # adds traceparent of this span
    queue.publish(body, headers=headers)

# consumer
ctx = trace.get_current_span(extract(msg.headers)).get_span_context()
with tracer.start_as_current_span("process order", kind=trace.SpanKind.CONSUMER,
                                  links=[trace.Link(ctx)]):
    handle(msg)
```

### Where the spans come from

OpenTelemetry has two ways to instrument code [@otel-inst]. **Zero-code** instrumentation records spans from the libraries you already use, such as web frameworks and database clients. **Code-based** instrumentation uses the API to add spans for your own business logic. You can use both at once [@otel-inst].

The **API** is what libraries depend on. It has no effect until the application also adds the **SDK** [@otel-inst]. The SDK is the part that records and exports spans. OpenTelemetry recommends sending spans to an **OpenTelemetry Collector** running next to the service [@otel-collector]. It can retry, batch, filter sensitive data and send to one or more backends [@otel-collector].

### Reading the waterfall

Open a slow trace from your [tail latency](/learn/tail-latency-and-queueing) graph and look for four shapes:

1. **The critical path.** Follow the chain of spans that ends last. Only shortening these spans shortens the request.
2. **Calls in a row that do not depend on each other.** They could run in parallel.
3. **N+1 calls.** The same short span repeats many times, once per item. Replace them with one batched call.
4. **Gaps.** Time where no span is recorded. It is often queue wait, a busy thread pool, or code without spans.

To estimate if a span's time is reasonable, compare it with [latency numbers](/learn/latency-numbers-and-estimation).

## Trade-offs

You rarely keep every trace. There are two ways to choose: **head sampling** decides when the request starts, and **tail sampling** decides after the trace ends [@otel-sampling].

| Choice | Keeps rare slow and error traces? | Cost | Use it when |
|---|---|---|---|
| Keep every trace | Yes. | Very high at scale. | Low traffic: tens of traces per second or fewer [@otel-sampling]. |
| Head-based sampling (for example 5%) | Not reliably. It decides at the start [@otel-sampling]. | Low. Easy to set up. | You need a cheap first step. Dapper kept about 1 trace in 1,024 and still had enough data for busy services [@dapper]. |
| Tail-based sampling | Yes. It can keep every error and every slow trace [@otel-sampling]. | High. Needs stateful collectors that hold spans until the trace ends [@otel-sampling]. | You must catch rare failures, and you can run the extra servers. |
| Head first, then tail | Only among the traces the head sampler kept. | Medium. | Very high volume: head sampling protects the pipeline, tail sampling picks the useful traces [@otel-sampling]. |

The `sampled` flag in `traceparent` carries the caller's decision to the next service, so all services keep or drop the same trace [@w3c-tc].

## At work: how a senior uses this

- Put the trace ID in every log line. Then a slow trace leads you straight to that request's logs. OpenTelemetry SDKs can add it for you [@otel-prop]. The [debugging lesson](/learn/debugging-production) shows how to use it during an incident.
- Turn on **exemplars** on latency histograms. An exemplar stores the trace ID of one real request with the metric point [@otel-metrics-dm]. In tools that support it, you click a spike on the p99 graph and land on a slow trace.
- Put high-cardinality data, like user ID or order ID, on span attributes. Never put it in metric labels. Each new combination of label values creates a new time series, so storage and cost grow [@prom-naming].
- Check every hop for broken propagation. A proxy or [API gateway](/learn/api-gateway-and-edge) that drops unknown headers cuts the trace in two. Test it: send a request and confirm one trace ID in all services.
- Watch thread pools and async code. Dapper kept the trace context in thread-local storage. It had to copy that context into every callback sent to a thread pool [@dapper]. Most OpenTelemetry SDKs, or the language's own context tools, can carry the context into a task. Check yours.
- Expect clock skew. Spans from different hosts use different clocks [@dapper]. A child that seems to start before its parent is usually a clock problem, not a bug.
- Do not trust incoming trace headers from the internet. An attacker could set `sampled` on every request and run up your tracing bill [@w3c-tc]. Start a new trace at your edge.
- During an [incident](/learn/incident-response-and-postmortems), compare a slow trace with a fast one for the same endpoint. The span that differs is your best lead.

## With AI

### What to ask the agent

> "Add OpenTelemetry to this Python service. Use zero-code instrumentation for Flask, requests and psycopg. Add one manual span around `price_cart` with the item count as an attribute. Export to a local Collector over OTLP."

> "Here is a trace as JSON. List the spans on the critical path, any repeated spans that look like N+1 calls, and any gaps longer than 10 ms between a parent and its first child."

> "This worker reads from a queue. Show how to inject trace context into the message headers on publish and add a span link on consume."

### What to check yourself

- The `traceparent` header really reaches the next service. Agents often forget proxies, queues and thread pools.
- The trace ID is in your logs, in the same format your tracing tool shows.
- No user ID, email or order ID ends up as a metric label.
- Span attributes contain no passwords, tokens or personal data.
- The sampling setup matches your goal. Head sampling alone will not keep every error trace.
- The service ignores or restarts trace context from untrusted callers.

## Check yourself

## Go deeper

Before this lesson: [Observability and SLOs](/learn/observability-slos-error-budgets) covers metrics, logs and error budgets. Next, use traces to find slow requests in [Tail latency and queueing](/learn/tail-latency-and-queueing).
