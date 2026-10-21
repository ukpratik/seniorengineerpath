---
topic: testing-strategy
subtitle: Which tests to write at each level, how to keep a growing suite fast and trusted, and how to spot tests that check nothing.
status: published
reviewed_by: ai-factcheck
reviewed_on: 2026-10-22
illustrative_story: true
sources:
  - id: swe-ch11
    title: "Adam Bender, “Testing Overview”, chapter 11 of Software Engineering at Google (O'Reilly, 2020)"
    url: https://abseil.io/resources/swe-book/html/ch11.html
    note: Why we test, test size against test scope, the 80/15/5 mix, the ice cream cone, and the cost of flaky tests.
  - id: swe-ch14
    title: "Joseph Graves, “Larger Testing”, chapter 14 of Software Engineering at Google (O'Reilly, 2020)"
    url: https://abseil.io/resources/swe-book/html/ch14.html
    note: What unit tests cannot see, test data, probers and canary analysis in production.
  - id: gtb-sizes
    title: "Simon Stewart, “Test Sizes”, Google Testing Blog, 2010"
    url: https://testing.googleblog.com/2010/12/test-sizes.html
    note: A table of what small, medium and large tests may do, with time limits.
  - id: gtb-flaky
    title: "John Micco, “Flaky Tests at Google and How We Mitigate Them”, Google Testing Blog, 2016"
    url: https://testing.googleblog.com/2016/05/flaky-tests-at-google-and-how-we.html
    note: How often tests flake at Google, why it costs so much, and re-runs and quarantine.
  - id: fowler-pyramid
    title: "Martin Fowler, “TestPyramid”, martinfowler.com, 2012"
    url: https://martinfowler.com/bliki/TestPyramid.html
    note: Where the test pyramid comes from, and why tests through the UI are slow and brittle.
  - id: vocke
    title: "Ham Vocke, “The Practical Test Pyramid”, martinfowler.com, 2018"
    url: https://martinfowler.com/articles/practical-test-pyramid.html
    note: The two rules of the pyramid, consumer-driven contract tests with Pact, and why end-to-end tests should be few.
  - id: dodds
    title: "Kent C. Dodds, “The Testing Trophy and Testing Classifications”, 2021"
    url: https://kentcdodds.com/blog/the-testing-trophy-and-testing-classifications
    note: The testing trophy, and the idea that tests should resemble how the software is used.
  - id: pact
    title: "Pact documentation, “Introduction”"
    url: https://docs.pact.io/
    note: What contract testing is, and how a contract is generated from the consumer's tests.
  - id: pitest
    title: "PIT mutation testing for Java, pitest.org"
    url: https://pitest.org/
    note: A short, clear explanation of mutation testing and why line coverage is not enough.
claims_checked:
  - claim: Google aims, as a very rough guide, for about 80% unit, 15% integration and 5% end-to-end tests.
    source: swe-ch11
  - claim: "Small, medium and large tests: network none, localhost only, or any; time limits 60, 300 and 900+ seconds."
    source: gtb-sizes
  - claim: "In 2016 Google saw about 1.5% of all test runs flaky, almost 16% of tests with some flakiness, and about 84% of post-submit pass-to-fail transitions involving a flaky test."
    source: gtb-flaky
  - claim: Google's book says tests begin to lose value as flakiness nears 1%.
    source: swe-ch11
  - claim: Configuration changes are the number one reason for Google's major outages, and unit tests alone cannot verify them.
    source: swe-ch14
  - claim: "Vocke: write tests at different levels; the higher the level, the fewer tests; push tests as far down as you can."
    source: vocke
  - claim: In Pact, the contract is generated while the consumer's tests run, so only parts the consumer uses get tested.
    source: pact
quiz:
  - q: Your end-to-end suite takes 40 minutes and fails randomly. Most bugs it finds are logic bugs in one function. What should you do?
    options:
      - Add more end-to-end tests to catch more bugs.
      - Write unit tests for those cases, and delete end-to-end tests that only repeat them.
      - Run the suite three times and take the best result.
    answer: 1
    why: Vocke advises pushing tests as far down the pyramid as you can. He deletes high-level tests that lower ones already cover, if they add no extra value.
  - q: In Google's terms, a test starts a database on the same machine and talks to it on localhost. What size is it?
    options: [Small, Medium, Large]
    answer: 1
    why: In Google's terms, small tests run in one process with no network. Medium tests stay on one machine and may use localhost. Large tests may span many machines.
  - q: Why is a test that fails 1 time in 100 so costly?
    options:
      - It wastes a little CPU.
      - People learn to ignore red builds, so they also ignore real failures.
      - It makes the code slower in production.
    answer: 1
    why: Google saw developers dismiss real failures as flaky, and its book says tests lose value as flakiness nears 1%.
  - q: An AI writes 20 tests for a function. Coverage is 100%, but none of the tests has an assertion. What tool shows the problem best?
    options:
      - A line coverage report.
      - Mutation testing, because changed code still passes every test.
      - A linter.
    answer: 1
    why: Coverage only shows which lines ran. Mutation testing changes the code, and if no test fails, the tests check nothing.
  - q: Two teams own an orders service and a mobile backend that calls it. They want to know if a change breaks the caller, without starting both systems. What fits?
    options: [A contract test, A load test, A canary]
    answer: 0
    why: A contract test checks each side alone against a shared contract. In Pact, the consumer's tests generate that contract.
incidents: []
cases: []
related: [flaky-tests, contract-tests, property-tests, ci-cd, safe-deploys, code-review]
---

## What broke

A team runs an online shop. Its main test suite clicks through the site in a real browser: sign in, search, add to cart, pay. There are 300 of these tests, and the suite takes 40 minutes.

Every week a few tests fail with no change to the code. A timeout here, a slow page there. The team learns a habit: when a test fails, press "re-run". Usually it turns green.

One Friday, the payment test fails. A developer re-runs it twice, and on the third run it passes. He merges. The failure was real: a race in the new payment code that showed up only some of the time. Customers were charged twice over the weekend.

This story is made up. But Google has seen the same thing: developers dismiss a failure as flaky and later find it was a real bug. [@gtb-flaky] The tests were not the problem. The strategy was.

## The idea in simple words

Tests are not there to find bugs once. They are there so you can **change code with confidence**, again and again, for years. [@swe-ch11] A good suite tells you fast, and truthfully, if your change broke something.

Tests come in three common scopes:

- **Unit tests** check one small piece, like a function or a class.
- **Integration tests** check a few parts working together, like your code and a real database.
- **End-to-end tests** run the whole system the way a user does. [@swe-ch11]

Lower tests are fast and cheap, and a failure points close to the cause. [@swe-ch11] Tests through the UI are slow, brittle and expensive to write, but they see the real system. [@fowler-pyramid] A strategy is the mix you choose, and the rules that keep it fast and trusted.

> [!key] **The key idea.** Write many small, fast tests and only a few large ones. In our view, a test that people do not trust does harm, because it teaches them to ignore red.

## How it works

Step through one change as it passes four layers of checks. Use Next and Back, or the ← and → keys.

[[diagram:test-layers-change]]

**The test pyramid.** Mike Cohn made the pyramid popular in his 2009 book *Succeeding with Agile*. The bottom is many unit tests. The top is a few tests through the UI. [@fowler-pyramid] Ham Vocke sums it up in two rules: write tests at different levels, and the higher the level, the fewer tests you should have. [@vocke] As a very rough guide, Google aims for about 80% unit, 15% integration and 5% end-to-end tests. [@swe-ch11]

The opposite shape is the **ice cream cone**: many end-to-end tests and few unit tests. Google says such suites tend to be slow, unreliable and hard to work with. [@swe-ch11] The team in the story had an ice cream cone.

**The critics: the testing trophy.** Kent C. Dodds draws a trophy instead. From the bottom, it has static checks, unit tests, integration tests and end-to-end tests. He builds on Guillermo Rauch's advice: "Write tests. Not too many. Mostly integration." His rule: "The more your tests resemble the way your software is used, the more confidence they can give you." [@dodds] He made it for JavaScript apps, mostly in one codebase. [@dodds] In our view, both shapes agree on the main point: keep end-to-end tests few. They differ on how much weight integration tests get.

**Size is not the same as scope.** "Unit" and "integration" mean different things to different teams. So Google also sorts tests by the resources they use. [@gtb-sizes]

- **Small**: one process, no network, no disk, no sleep. Time limit 60 seconds.
- **Medium**: one machine. Network to localhost only, so a database in a container is fine. Limit 300 seconds.
- **Large**: anything, across many machines. Limit 900 seconds or more. [@gtb-sizes] [@swe-ch11]

Google cares most about speed and determinism, and size predicts both. [@swe-ch11] A test that cannot touch the network cannot fail because the network was slow.

**Flaky tests.** A [flaky test](/learn/flaky-tests) passes and fails on the same code. In 2016, Google reported that about 1.5% of all its test runs gave a flaky result. Almost 16% of its tests had some flakiness. [@gtb-flaky] In post-submit CI, about 84% of pass-to-fail changes involved a flaky test. [@gtb-flaky] The book says tests begin to lose value as flakiness nears 1%. [@swe-ch11]

The real cost is trust. People ignore alarms that are often false, so real failures get dismissed. [@gtb-flaky] Google's tools re-run failing tests and quarantine very flaky ones. Quarantine has a cost: it can hide a real race in the code. [@gtb-flaky] Treat a flaky test as a bug with an owner, not as noise.

**Test data.** Large tests need data that looks real. Data written by hand covers a narrow range of cases and reflects the author's biases. Copied production data is more real, and a sample of it keeps the size down. [@swe-ch14] Google's book says tests should not depend on the order they run in, or on a shared database. [@swe-ch11] So let each test create its own data.

**Contract tests between services.** End-to-end tests across many services are slow and brittle. A [contract test](/learn/contract-and-integration-tests) checks each service alone against a shared contract. [@pact] In Pact, the consumer's tests generate the contract, so it holds only the requests the consumer really uses. The provider then checks that it can answer them. [@pact] [@vocke]

**Testing in production.** Some bugs appear only with real traffic, real data and real config. Google's book says config changes are the number one cause of its major outages. Unit tests alone cannot check them. [@swe-ch14] So the last check runs in production. A **prober** is a test that checks production, usually with read-only actions. During a staged rollout, canary analysis compares the canary's health with the rest of the fleet. [@swe-ch14] See [Safe deploys and config changes](/learn/safe-deploys-config-changes) for how a [canary](/glossary#canary) stops a bad release.

**Mutation testing.** Line coverage only shows which code ran during tests. It does not show that a test would notice a bug. Mutation testing makes small changes to your code, such as flipping a condition, and runs the tests again. If a test fails, the change is "killed". If all tests still pass, the change "lived", and your tests missed it. [@pitest] It runs your tests many times, once per change. So we suggest you start with the code that matters most. See [Property-based and mutation testing](/learn/property-based-and-mutation-testing).

**What to test first.** You cannot test everything, so start with risk. Ask: what costs the most if it breaks, and what changes most often? Money, auth, data loss and shared libraries come first. Google calls this the Beyoncé Rule: test everything you do not want to break. In its words, "If you liked it, then you shoulda put a test on it." [@swe-ch11]

## Trade-offs

| Option | Speed | What it catches | What it misses | Keep it |
|---|---|---|---|---|
| Unit (small) | Fast. Run them on every change. | Logic, edge cases, rounding. Points close to the cause. | Wrong SQL, config, how parts fit together. | Many. Most of the suite. [@swe-ch11] |
| Integration (medium) | Slower. Limit 300 s per test. [@gtb-sizes] | Real database, queues, serialisation. | Other teams' services, the full user flow. | A good number, for each boundary you own. |
| Contract | Each side runs alone, without the full system. [@pact] | Broken requests or responses between services. | Whether the whole flow works for a user. | One per consumer and provider pair. |
| End-to-end (large) | Slow. Often flaky. [@vocke] | A broken user journey across the system. | Exact cause of a failure. | Few. Only key journeys, like sign-up and pay. |
| Canary in production | Slow. It waits for real traffic. | Real data, real load, real config. [@swe-ch14] | Bugs that hit only rare users. Some users see errors. | On every deploy. |

**Simple rule:** test each thing at the lowest level that can catch it. When a higher test finds a bug, add a lower test for it. [@vocke]

## At work: how a senior uses this

- Ask in design reviews: **"How will we know this works, and how fast?"** Plan the tests with the feature.
- Watch suite time. Ask: **"How long from push to green?"** If it grows, move tests down the pyramid.
- Track flaky tests on a list with owners. Fix or delete them. Do not let "re-run" become a habit.
- Use a real database in a container for data code. Use a test double for slow or paid services, like a payment provider.
- When a bug reaches production, write the lowest test that would have caught it, before you fix it.
- In reviews, read the assertions first. Ask: **"If I broke this code, would this test fail?"**

## With AI

### What to ask the agent

> "Write unit tests for `applyDiscount`. Cover zero, negative, rounding and the maximum value. Every test must assert the exact result."

> "Here is our test suite. Which tests are end-to-end checks of logic that a unit test could cover? List them with a lower-level replacement."

> "This test is flaky. Read it and list every source of non-determinism: time, order, randomness, network and shared state."

### What to check yourself

- Every test has an assertion that would fail if the code were wrong. AI tools sometimes write tests that only call the code, or assert that a mock was called. See [Reviewing human and AI code](/learn/reviewing-human-and-ai-code).
- The test does not copy the code's logic into the expected value. Then a wrong formula passes.
- Break the code on purpose, or run a mutation tool. If the tests stay green, they test nothing.
- No `sleep` calls, real clocks or real network in unit tests.
- The agent did not change or delete a failing test to make the build green.

## Check yourself

## Go deeper

- [Flaky tests](/learn/flaky-tests): finding and fixing the causes.
- [Contract and integration tests](/learn/contract-and-integration-tests): Pact and testing service boundaries.
- [CI/CD and trunk-based development](/learn/ci-cd-trunk-based-development): where these tests run.
