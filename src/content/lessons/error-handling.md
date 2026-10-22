---
topic: error-handling
subtitle: Sort errors into the ones you expect and the bugs, handle each kind on purpose, and never hide one.
status: published
reviewed_by: ai-factcheck
reviewed_on: 2026-10-22
illustrative_story: false
sources:
  - id: gcp2025
    title: "Google Cloud, Service Health incident report for 12 June 2025 (official)"
    url: https://status.cloud.google.com/incidents/ow5i3PPK96RduMcb1SsW
    note: How blank fields in policy data hit a null pointer with no error handling and crashed API checks around the world.
  - id: rust-errors
    title: "The Rust Programming Language (official book), chapter 9 “Error Handling”"
    url: https://doc.rust-lang.org/book/ch09-00-error-handling.html
    note: Recoverable and unrecoverable errors, and why bugs should stop the program.
  - id: rust-panic
    title: "The Rust Programming Language (official book), section 9.3 “To panic! or Not to panic!”"
    url: https://doc.rust-lang.org/book/ch09-03-to-panic-or-not-to-panic.html
    note: When to return an error value and when to stop, with examples from parsers and rate limits.
  - id: sre-cascading
    title: "Mike Ulrich, “Addressing Cascading Failures”, Site Reliability Engineering (O'Reilly), chapter 22"
    url: https://sre.google/sre-book/addressing-cascading-failures/
    note: Which errors to retry, deadlines, failing fast and graceful degradation under overload.
  - id: brooker
    title: "Marc Brooker, “Timeouts, retries and backoff with jitter”, Amazon Builders' Library"
    url: https://builder.aws.com/content/3EumjoZascWd1oZiEgL8ORlv3qE/timeouts-retries-and-backoff-with-jitter
    note: Transient errors, why client errors are not worth a retry, how to pick a timeout, and the cost of circuit breakers.
  - id: aws-fallback
    title: "Jacob Gabrielson, “Avoiding fallback in distributed systems”, Amazon Builders' Library"
    url: https://builder.aws.com/content/3EuS9Sakq7L3VLQIF3qzfMfke1Y/avoiding-fallback-in-distributed-systems
    note: Why rarely used fallback code is risky, and what Amazon does instead.
  - id: owasp-errors
    title: "OWASP Cheat Sheet Series, “Error Handling Cheat Sheet”"
    url: https://cheatsheetseries.owasp.org/cheatsheets/Error_Handling_Cheat_Sheet.html
    note: Why detailed errors help attackers, and the generic-response-plus-server-log pattern.
  - id: owasp-input
    title: "OWASP Cheat Sheet Series, “Input Validation Cheat Sheet”"
    url: https://cheatsheetseries.owasp.org/cheatsheets/Input_Validation_Cheat_Sheet.html
    note: Syntax and meaning checks, allowlists, and validating data from internal systems too.
  - id: rfc9457
    title: "IETF RFC 9457, “Problem Details for HTTP APIs” (July 2023)"
    url: https://www.rfc-editor.org/rfc/rfc9457.html
    note: A standard, machine-readable shape for error responses in HTTP APIs.
claims_checked:
  - claim: "Service Control's 29 May 2025 quota-check code had no proper error handling and no feature flag; on 12 June a policy with unintended blank fields replicated globally within seconds and \"without the appropriate error handling, the null pointer caused the binary to crash\" in a crash loop."
    source: gcp2025
    note: Quoted and paraphrased from the official incident report. The rollout was region by region but the failing path "was never exercised" because it needed a policy change. Matches the google-cloud-2025-06-service-control incident card.
  - claim: In larger regions the restarting tasks created a herd effect on the Spanner table they depend on; there was no randomized exponential backoff; us-central1 took up to about 2 h 40 min to fully recover; fixes include better error handling, testing for invalid data and failing open.
    source: gcp2025
    note: Report wording is "up to ~2h 40 mins to fully resolve in us-central-1". Earlier draft said "about"; now "up to about". The overloaded dependency is the Spanner policy table, now named as a database table.
  - claim: Rust splits errors into recoverable and unrecoverable; unrecoverable errors "are always symptoms of bugs". Return a Result when failure is expected (parser with malformed data, HTTP rate limit); panic on a contract violation, which indicates a caller-side bug.
    source: rust-errors
    note: Quote is exact from chapter 9 intro; the Result-vs-panic rule and both examples are from section 9.3 (rust-panic).
  - claim: Under overload "fail early and cheaply"; graceful degradation "shouldn't trigger very often" and should be tested; do not retry permanent errors or malformed requests; use deadline propagation.
    source: sre-cascading
    note: All four points are in chapter 22 (Mike Ulrich) in these words or close to them.
  - claim: Set a timeout on any remote call and generally on calls across processes on the same box; client errors should not be retried; circuit breakers add modal behavior that is hard to test and can add time to recovery.
    source: brooker
    note: Timeout sentence softened to match the source's "generally" for same-machine calls.
  - claim: Amazon usually avoids fallback because it is hard to test and may rarely run; around 2001 a cache-to-database fallback took the whole website down; Amazon prefers a more reliable main path or failover, and exercises essential fallback as often as possible in production.
    source: aws-fallback
    note: Earlier draft said "Amazon is careful with them" and "one 2001 outage"; the article says Amazon avoids fallback and dates the outage "around 2001". Fixed.
  - claim: Validate syntax and semantics (end date after start date), define what you accept and reject the rest, validate on the server, and validate data from internal APIs, partner feeds, queues and stored records when it crosses a trust boundary.
    source: owasp-input
    note: Matches the Input Validation Cheat Sheet wording closely.
  - claim: Detailed errors help attackers in reconnaissance; return a generic response and log the details on the server. Problem details have members type, title, status, detail (and instance).
    source: owasp-errors
    note: OWASP part checked on the Error Handling Cheat Sheet. Problem details fields checked in RFC 9457 (rfc9457). Keeping secrets and personal data out of logs is the lesson's own advice, with no citation.
quiz:
  - q: Your service calls a pricing API and gets HTTP 400 Bad Request. What should it do?
    options:
      - Retry three times with backoff.
      - Do not retry. The same request will fail again, so return or log the error.
      - Switch to a fallback pricing service.
    answer: 1
    why: A client error means the request itself is wrong. Sending it again cannot fix it.
  - q: A function reads a config value that, by design, must always be there. It is missing. What is the best choice?
    options:
      - Use a default value and carry on quietly.
      - Treat it as a bug. Fail fast with a clear error that names the missing value.
      - Catch the error and return an empty result.
    answer: 1
    why: A broken assumption is a bug. Hiding it lets bad state spread and makes it harder to find.
  - q: What should the API send back to a user when a database query fails?
    options:
      - The full stack trace, so the user can report it.
      - The SQL query and the database version.
      - A short, generic message with an error code and a request ID. Details go to the server log.
    answer: 2
    why: Detailed errors help attackers. Keep details in your logs, where engineers can find them by request ID.
  - q: Why does Amazon's Builders' Library warn against fallback code?
    options:
      - It runs rarely, so it is hard to test and may fail or overload something when you finally need it.
      - It is always slower than the main path.
      - Cloud providers do not allow it.
    answer: 0
    why: Code that rarely runs can hide bugs. Amazon prefers to make the main path more reliable.
incidents: [google-cloud-2025-06-service-control, crowdstrike-2024-07-channel-file-291]
related: [idempotency-retries, safe-deploys, observability-slos, owasp-secure-coding, rest-api]
---

## What broke

On 12 June 2025, many Google Cloud APIs started to return errors around the world. The cause was one part of Service Control, the system that checks quota and policy for incoming API requests. [@gcp2025]

On 29 May, Google had added a new quota check to Service Control. It rolled out region by region, but the code path that failed never ran. It needed a certain policy change to trigger it. [@gcp2025]

The report says this change had no proper error handling, and it was not protected by a feature flag. [@gcp2025]

On 12 June, a policy change was copied to every region within seconds. The policy data had blank fields that nobody intended. The new code read a blank field and hit a null pointer. The report says that "without the appropriate error handling, the null pointer caused the binary to crash." [@gcp2025]

The binaries restarted, read the same data and crashed again: a crash loop. In big regions, the restarting tasks all hit the database table they depend on at once. Service Control did not have randomized exponential backoff to prevent this. Full recovery in us-central1 took up to about 2 hours 40 minutes. [@gcp2025]

Google's fixes include better error handling and testing for invalid data. They also say parts of Service Control should "fail open" when they break. [@gcp2025]

## The idea in simple words

Errors come in two kinds, and you should handle them differently.

- **Expected errors** happen in normal life. A user types a wrong date. A file is missing. Another service is busy for a moment. Your code should plan for these and handle them.
- **Bugs** are broken assumptions. A value that "can never be empty" is empty. An index is past the end of a list. Retrying will not fix a bug. A person has to fix the code.

Rust's official book uses the same split. It calls them recoverable and unrecoverable errors. It says unrecoverable errors "are always symptoms of bugs." [@rust-errors]

> [!key] **The key idea.** Decide on purpose what happens for every error. Handle the expected ones close to where you can do something useful. Make bugs loud, so a person sees them and fixes them. Never hide either kind.

**Never swallow errors.** An empty `catch` block, or a "log and return null" with no plan, hides the problem. The program carries on in a bad state, and the real failure shows up later, far away, where it is much harder to debug.

## How it works

Step through a request where one downstream call fails. Watch how each layer decides what to do. Use Next and Back, or the ← and → keys.

[[diagram:downstream-error-layers]]

### Validate input at the boundary

Check data where it enters your system, before any business logic uses it. Check the shape (type and format) and the meaning, for example "the end date is after the start date". Define what you accept and reject everything else. [@owasp-input]

Do this on the server, even if the browser already checks the same field. OWASP also says to validate data from internal APIs, partner feeds, queues and stored records when it crosses a trust boundary. [@owasp-input] The Google Cloud policy data came from inside Google, and it still had blank fields. [@gcp2025]

### Errors are part of your API

Give each error a type or code that tells the caller what it can do next. Three questions cover most cases:

- **Can I retry?** For example, "service busy" or a timeout.
- **Must I change the request?** For example, "invalid email" or "amount too large".
- **Is this a bug on your side?** Then the caller can only report it.

For HTTP APIs, RFC 9457 gives a standard JSON shape for this, called problem details. It has fields such as `type`, `title`, `status` and `detail`. [@rfc9457] The [REST API lesson](/learn/rest-api-design) covers status codes.

### Exceptions or result values

Some languages throw exceptions. Others, like Rust and Go, return an error as a normal value. Rust's book gives a simple rule that works in any language. When failure is expected, return an error value, so the caller must decide what to do. Its examples are a parser given bad data and an HTTP call that hits a rate limit. When a contract is broken, stopping is reasonable, because it shows a bug in the caller. [@rust-panic]

The style matters less than one habit: every error must be visible in the code that calls the function.

### Fail fast or degrade gracefully

**Fail fast** means you stop at the first sign of trouble with a clear error. Do this for bugs, bad config at startup, and requests that are too large. Google's SRE book advises that under overload you should "fail early and cheaply." [@sre-cascading]

**Graceful degradation** means you still serve the user, with less. A product page can show without recommendations. The SRE book says degraded mode "shouldn't trigger very often", and that you should test it. [@sre-cascading]

The choice depends on the feature. A missing recommendation is fine. A missing fraud check on a payment is not.

### Retries, timeouts, circuit breakers and fallbacks

**Retries.** Retry only transient errors, the ones that may pass in a moment. Do not retry client errors or malformed requests, because the same request will fail again. [@sre-cascading] [@brooker] Calls that change data need idempotency first. The [idempotency and retries lesson](/learn/idempotency-retries-timeouts) covers keys, backoff and retry limits.

**Timeouts.** Put a timeout on every remote call. Amazon's advice is to set a timeout on any remote call, and in general on calls between processes on the same machine too. [@brooker] Better still, pass a deadline down the call chain, so lower services stop work the user has already given up on. [@sre-cascading]

**Circuit breakers.** A circuit breaker stops calls to a service that keeps failing, for a short time. This protects the failing service and saves your threads. Amazon notes a cost: breakers add modal behavior that is hard to test, and they can slow recovery. [@brooker]

**Fallbacks.** A fallback is a second way to get the answer, used only when the first way fails. Amazon usually avoids them. Fallback code is hard to test and may rarely run in production, so it can hide bugs. In an Amazon outage around 2001, web servers fell back to a database when their caches failed. They all hit the database at once, and the whole website went down. [@aws-fallback]

Amazon prefers to make the main path more reliable, or to run both paths all the time, which turns the fallback into failover. If a fallback is essential, Amazon exercises it as often as possible in production. [@aws-fallback]

### Messages for users, logs for engineers

Users and engineers need different things. The user needs a short, calm message and a next step: "We could not load delivery dates. Please try again." Engineers need the details: error type, which call failed, how many attempts, and a request ID.

Never send stack traces, SQL errors or server versions to users. They help attackers learn your system. Return a generic response and log the details on the server. [@owasp-errors] Also keep secrets, tokens and personal data out of error messages and logs. The [observability lesson](/learn/observability-slos-error-budgets) covers what to log.

## Trade-offs

| Approach | Good for the user? | Risk | Use it when |
|---|---|---|---|
| Fail fast | No. They see an error now. | Low. The problem is clear and close to its cause. | Bugs, bad config at startup, invalid input. |
| Retry | Yes, if the error was brief. | Retries add load to a struggling service. | Transient errors on idempotent calls, with a limit. |
| Degrade gracefully | Yes. They get most of the page. | The degraded path may be untested. | Optional parts, like recommendations or reviews. |
| Fallback to another source | Yes, if the fallback works. | Rarely run code can fail or overload a shared system. | Only when you run and test it often. |
| Swallow the error | No. Wrong results later. | High. Nobody knows something broke. | Never. |

**Simple rule:** for each call that can fail, write down which errors are expected and what happens for each. Everything else is a bug, and a bug should fail loudly.

## At work: how a senior uses this

- In code review, ask about every `catch`: **"What does the caller see, and what do we log?"** An empty `catch` is a finding.
- Ask **"What happens if this field is empty?"** for data from files, configs and other teams, not only from users.
- Check that every remote call has a timeout, and that retries happen in one layer only.
- Mark each dependency as critical or optional. Optional ones get a degraded mode. Critical ones fail with a clear error.
- Make sure a degraded mode shows up on a dashboard, so it does not stay on for weeks unseen.
- Keep one error format across all your APIs, with a code and a request ID in every response.

## With AI

### What to ask the agent

> "List every place in this module where an error is caught and ignored, logged without being returned, or turned into null. For each one, say what the caller will see."

> "For each remote call in this service, list the timeout, the retry policy and what happens on failure. Mark calls with no timeout."

> "Write tests that send empty, missing and oversized fields to this parser. Each test must expect a clear validation error, not a crash."

### What to check yourself

- The agent did not add broad `catch` blocks that hide errors to make tests pass.
- Retries are only on transient errors and only on idempotent calls.
- Error responses do not contain stack traces, SQL, tokens or personal data.
- New fallback code has a test that makes the main path fail.
- Validation is on the server, at the boundary, not only in the client.

## Check yourself

## Go deeper

Related lessons: [Idempotency and retries](/learn/idempotency-retries-timeouts), [Safe deploys](/learn/safe-deploys-config-changes), [Debugging production](/learn/debugging-production) and [Secure coding](/learn/owasp-top-10-secure-coding).
