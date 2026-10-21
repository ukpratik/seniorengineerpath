---
topic: system-design-method
subtitle: A repeatable five-step method for a 45-minute design interview, worked through on a ticket booking system like Ticketmaster.
status: published
reviewed_by: ai-factcheck
reviewed_on: 2026-10-21
illustrative_story: false
sources:
  - id: hi-delivery
    title: "Hello Interview, “System Design Delivery Framework” (System Design in a Hurry)"
    url: https://www.hellointerview.com/learn/system-design/in-a-hurry/delivery
    note: The five steps with time boxes, what each step should produce, and tips for each.
  - id: hi-ticketmaster
    title: "Hello Interview, “Design a Ticket Booking Site Like Ticketmaster” (problem breakdown)"
    url: https://www.hellointerview.com/learn/system-design/problem-breakdowns/ticketmaster
    note: A full worked answer, with requirements, API, seat holds, a virtual queue, search and what each level is expected to show.
  - id: xu-framework
    title: "Alex Xu, System Design Interview – An Insider's Guide, “A Framework for System Design Interviews” (ByteByteGo)"
    url: https://bytebytego.com/courses/system-design-interview/a-framework-for-system-design-interviews
    note: A four-step framework with time for each step in a 45-minute interview, and a list of dos and don'ts.
  - id: tm-statement
    title: "Ticketmaster, “Taylor Swift | The Eras Tour Onsale Explained” (19 November 2022)"
    url: https://business.ticketmaster.com/press-release/taylor-swift-the-eras-tour-onsale-explained/
    note: Ticketmaster's own account of the presale, with registrations, traffic, errors and tickets sold.
  - id: nbc-cancel
    title: "NBC News, “Ticketmaster cancels public sale of Taylor Swift tour, citing high demand for tickets” (17 November 2022)"
    url: https://www.nbcnews.com/pop-culture/pop-culture-news/ticketmaster-cancels-public-sale-taylor-swift-tour-citing-high-demand-rcna57758
    note: News report on the cancelled general sale, with Ticketmaster's stated reason.
  - id: redis-set
    title: "Redis documentation, “SET” command"
    url: https://redis.io/docs/latest/commands/set/
    note: The NX and EX options, the simple lock pattern built on them, and its limits.
  - id: cf-waiting-room
    title: "Cloudflare documentation, “About Waiting Room”"
    url: https://developers.cloudflare.com/waiting-room/about/
    note: How a waiting room admits users by total active users and new users per minute, in first-in-first-out order.
claims_checked:
  - claim: "Over 3.5 million registered; about 1.5 million got codes and 2 million were waitlisted; bots and fans without codes drove 3.5 billion system requests, 4x the previous peak; about 15% of interactions had issues; over 2 million tickets sold on 15 Nov."
    source: tm-statement
    note: "All numbers match the 19 Nov 2022 statement word for word. 'Codes failed to validate' = 'passcode validation errors that caused fans to lose tickets they had carted'."
  - claim: "Ticketmaster cancelled the general public sale, citing 'extraordinarily high demands on ticketing systems and insufficient remaining ticket inventory'."
    source: nbc-cancel
    note: "NBC News, 17 Nov 2022, two days after the 15 Nov sale. Quote is the start of the full sentence ('...to meet that demand'). Fixed the headline in the source title."
  - claim: "Hello Interview's steps and time boxes: requirements ~5 min, core entities ~2, API ~5, high-level design 10–15, deep dives ~10; top 3 functional requirements; REST by default; user from the auth token."
    source: hi-delivery
    note: "Matches. The page also has an optional data flow step (~5 min); added a mention. It gives no total interview length, so '45–60 minute' was changed to Xu's 45 minutes."
  - claim: "Ticketmaster breakdown: view, search, book; consistency for booking, availability for search, 10M users per event, search < 500 ms, 100:1 reads; Redis seat lock with a 10-minute TTL; admitted users checked by the booking service."
    source: hi-ticketmaster
    note: "Matches. Added User to entities and Performer to the event response. Long DB locks are rejected for resource strain and deadlock risk, not 'never released'; fixed. Queue admission is 'periodically or based on criteria', not batches; fixed."
  - claim: "SET key value NX EX 600 sets the key only if it does not exist and expires it after 600 s; the docs discourage this lock pattern in favour of Redlock."
    source: redis-set
    note: "NX, EX and the Patterns note match. Added the docs' advice to use a random token as the value."
  - claim: "Cloudflare Waiting Room admits by total active users and new users per minute, queues in FIFO order and shows an estimated wait."
    source: cf-waiting-room
    note: "Matches 'About Waiting Room'."
  - claim: "Xu's framework has four steps for a 45-minute interview, says to ask for clarification, and warns 'Don't think in silence.'"
    source: xu-framework
    note: "Matches the ByteByteGo chapter 'A Framework For System Design Interviews'. The quote is exact."
  - claim: "10M users in 10 minutes ≈ 17,000/s; 10 reads each ≈ 170,000 reads/s; 70,000 seats."
    source: hi-ticketmaster
    note: "Only the 10M figure is from the source. The 10 minutes, 10 reloads and 70,000 seats are the lesson's labelled guesses. Arithmetic re-checked: 10,000,000/600 = 16,667; x10 = 166,667."

quiz:
  - q: The interviewer says "Design Ticketmaster." What should you do in the first five minutes?
    options:
      - Draw the services and databases you will need.
      - Agree on the top functional requirements and the key non-functional ones, such as no double booking.
      - Start with the hardest part, the seat lock.
    answer: 1
    why: Requirements decide everything that follows. Without them you may design the wrong system, or spend your time on parts nobody asked for.
  - q: Which requirement is the main reason the booking path needs strong consistency?
    options:
      - Search must return results in under 500 ms.
      - Two people must never buy the same seat.
      - The system is read-heavy.
    answer: 1
    why: Search can show slightly old data, but a seat sold twice is a real failure. So booking chooses consistency and search chooses availability.
  - q: A user picks a seat and starts paying. What stops another user from buying it during payment?
    options:
      - A hold on the seat that expires after a set time, such as 10 minutes.
      - A database lock held until payment finishes.
      - Nothing. The first payment to finish wins.
    answer: 0
    why: A hold with a TTL keeps the seat for one user and frees it on its own if they leave. A database lock held for minutes ties up the database and raises the risk of deadlocks.
  - q: Ten million fans arrive for one event at the same minute. Which part protects the booking service most directly?
    options:
      - A bigger cache for event pages.
      - A virtual waiting room that admits users at a rate the system can handle.
      - More search servers.
    answer: 1
    why: The waiting room limits how many people reach seat selection and booking at once. Caching helps reads, but not the write rush.
  - q: You have 10 minutes left and your high-level design works. What is the best use of the time?
    options:
      - Redraw the diagram more neatly.
      - Go deep on the hardest non-functional requirement and explain the trade-offs.
      - List every technology you know.
    answer: 1
    why: Deep dives show seniority. They prove the design meets the hard requirements, and they show you can compare options.
incidents: []
related: [inventory-flash-sales, latency-estimation, requirements-nfrs, design-deep-dives, level-calibration, zero-to-millions, interview-loops]
---

## What broke

On 15 November 2022, Ticketmaster opened a presale for Taylor Swift's Eras Tour. Over 3.5 million people had pre-registered as Verified Fans. About 1.5 million of them got codes to join the sale. The other 2 million were put on a waiting list. [@tm-statement]

On the day, Ticketmaster says, "the staggering number of bot attacks" and fans without codes drove huge traffic. The site got 3.5 billion total system requests, four times its previous peak. [@tm-statement]

To keep the systems up, it slowed down some sales and pushed others back. Fans waited longer in the queue. Ticketmaster estimates that about 15% of interactions on the site had issues. Some fans lost tickets they had already put in their cart, because codes failed to validate. [@tm-statement]

Over 2 million tickets were still sold that day. [@tm-statement] Two days later, Ticketmaster cancelled the general public sale. The reason it gave was "extraordinarily high demands on ticketing systems and insufficient remaining ticket inventory". [@nbc-cancel]

Hello Interview has a full worked answer for "design Ticketmaster". [@hi-ticketmaster] The story shows why the problem is hard. It tests the whole method: what to build, what must never go wrong, and what happens when demand is far bigger than supply.

## The idea in simple words

A system design interview is a conversation of about 45 minutes about a big, vague problem. [@xu-framework] There is no single right answer. The interviewer wants to see how you think: how you clarify the problem, choose a design and explain trade-offs.

Without a method, people jump to drawing boxes. Twenty minutes later, they find they solved the wrong problem. A method gives you a path, and it lets you drive the conversation.

Hello Interview's framework has five main steps, plus an optional data flow step that we skip here. [@hi-delivery] Alex Xu's book uses four steps that cover the same ground: understand the problem, propose a high-level design, deep dive, and wrap up. [@xu-framework]

> [!key] **The key idea.** First agree on what to build. Then build the simplest design that works. Then spend your best time on the parts that are hard. Each step leaves an artefact on the board that the next step uses.

## How it works

Step through the five steps for a ticket booking system, with what each one puts on the board. Use Next and Back, or the ← and → keys.

[[diagram:design-method-steps]]

### Step 1. Requirements (about 5 minutes)

Split them into two lists. **Functional requirements** are what users can do. **Non-functional requirements** are the qualities the system must have, such as speed, scale and consistency. Hello Interview advises picking the top 3 functional ones. [@hi-delivery]

For Ticketmaster, the breakdown chooses three: view events, search for events, and book tickets. [@hi-ticketmaster] The key non-functional ones are: [@hi-ticketmaster]

- **Consistency for booking.** No seat is ever sold twice.
- **Availability for search and viewing.** Slightly old data is fine here.
- **Scale for popular events.** For example, 10 million users for one event.
- **Fast search**, under 500 ms. Reads far outnumber writes, about 100 to 1.

Say out loud what is out of scope, for example dynamic pricing or an admin tool. Xu's first rule is to ask, not assume. [@xu-framework]

### Back-of-envelope numbers

Do maths only where it changes the design. [@hi-delivery] Here it does. The 10 million comes from the breakdown. [@hi-ticketmaster] The other numbers are our own guesses, so label them as guesses in the interview:

- 10 million fans arrive in the first 10 minutes. That is 10,000,000 ÷ 600 s ≈ **17,000 new users per second**.
- Each one reloads the seat map about 10 times in those 10 minutes. That is roughly **170,000 reads per second** on one event.
- A stadium has about 70,000 seats. So at most 70,000 bookings can succeed, while millions of people try.

The lesson from the numbers: reads need caching, and the write path needs a gate in front. See [latency numbers and estimation](/learn/latency-numbers-and-estimation) for the method.

### Step 2. Core entities (about 2 minutes)

List the main nouns. Do not design every column yet. [@hi-delivery] For Ticketmaster: Event, User, Venue (with its seat map), Performer, Ticket (one per seat, with a status) and Booking (one order of tickets). [@hi-ticketmaster]

### Step 3. API (about 5 minutes)

Write one endpoint per main user action. Hello Interview suggests REST unless you have a reason not to. Take the user from the login token, not from the request body. [@hi-delivery]

```
GET  /events/search?keyword=&start=&end=&page=   → Event[]
GET  /events/:eventId                            → Event, Venue, Performer, Ticket[]
POST /bookings/:eventId  { ticketIds, paymentDetails }  → bookingId
```

These follow the breakdown's own API. [@hi-ticketmaster]

### Step 4. High-level design (10–15 minutes)

Draw the simplest design that meets the functional requirements. [@hi-delivery] Clients reach an API gateway. Behind it sit an event service, a search service and a booking service. The breakdown keeps bookings and tickets in one Postgres database, so a transaction can update both. A payment provider takes the money. [@hi-ticketmaster]

Stay simple here. When you see a problem, name it and say you will come back to it. [@hi-delivery]

### Step 5. Deep dives (about 10 minutes)

Now go back to the hard non-functional requirements. Each deep dive should answer one.

**Holding a seat during payment.** Payment can take minutes. Holding a database lock that long is a bad idea. It ties up database resources and raises the risk of deadlocks. [@hi-ticketmaster] Instead, put a hold on the seat that expires on its own. The breakdown uses Redis with a 10-minute TTL. [@hi-ticketmaster] In Redis, `SET seat:42 user7 NX EX 600` sets the key only if it does not exist yet, and it expires after 600 seconds. [@redis-set] The docs advise a random token as the value, so a client can only delete its own lock. [@redis-set]

Two warnings make a senior answer. The Redis docs discourage this simple lock pattern in favour of their Redlock algorithm. [@redis-set] And the database must still be the final check when the seat is sold. Use the conditional update from [inventory and flash sales](/learn/inventory-overselling-flash-sales). Make the payment step safe to retry, as in [idempotency, retries and timeouts](/learn/idempotency-retries-timeouts).

**Surviving the surge.** Put a **virtual waiting room** in front of seat selection. In the breakdown, users join a queue and are let in from the front, from time to time or as tickets sell. The booking service rejects users who were not let in. [@hi-ticketmaster] Products exist for this. Cloudflare's Waiting Room admits visitors by a limit on total active users and on new users per minute. Others wait in first-in-first-out order and see an estimated wait. [@cf-waiting-room] Add [rate limiting](/learn/rate-limiting) per user to slow down bots.

**Fast search and seat maps.** Use a search index instead of `LIKE '%…%'` queries on the database. [@hi-ticketmaster] Cache event and venue data, which rarely change, with long TTLs. Cache seat availability only with a short TTL. [@hi-ticketmaster] See [caching and cache stampedes](/learn/caching-and-cache-stampedes).

### Timeboxing and driving

Keep a clock in view. If requirements take 15 minutes, you will have no time for deep dives. Talk while you think. Xu warns: "Don't think in silence." [@xu-framework]

Drive the conversation, but check in. Say "I plan to go deep on the seat hold next. Is that where you want to spend time?" Hello Interview says senior candidates should find the hard parts and lead the deep dives. It also says to give the interviewer room to ask questions. [@hi-delivery] Its breakdown expects a senior to go deep on topics such as a distributed lock for holds, and to explain trade-offs. A mid-level answer can use a simpler fix, such as a ticket status with a timeout. [@hi-ticketmaster]

## Trade-offs

| Choice | Prevents double booking? | Cost | Use it when |
|---|---|---|---|
| Database lock held during payment | Yes. | Locks last minutes, tie up the database and risk deadlocks. | Avoid for long user steps. |
| Ticket status with an expiry time, checked in short transactions | Yes. | Simple. Expired holds need a cleanup job, or a check at read time. | Most systems, and a solid mid-level answer. |
| Redis hold with a TTL, plus a final database check | Yes. | One more system to run. | Big on-sales with many holds per second. |
| Virtual waiting room | Not directly. It limits load. | Fans wait, and you must keep the queue fair. | Known surges, such as a big presale. |
| Showing live seat maps to everyone | No. It only shows state. | Heavy reads, and maps go stale in seconds. | Normal events. Hide them behind the queue for huge ones. |

**Rule of thumb:** make the database the final judge of who gets a seat. Use holds and the waiting room to keep the database calm.

## At work: how a senior uses this

The same method runs a good design review at work. Only the clock changes, from 45 minutes to a few weeks.

- Start every design doc with functional and non-functional requirements, and a list of non-goals. See [design docs and RFCs](/learn/design-docs-and-rfcs).
- Ask for the numbers that change the design: peak users, reads to writes, data size. Write the assumptions down.
- In a review, ask the author: **"Which requirement does this part serve?"** A box with no requirement is extra cost.
- Spend review time on the riskiest parts, not on naming or the parts everyone agrees on.
- For each deep dive, ask for two options and why one was chosen.
- Before launch, ask what happens at 4 times the expected peak. Ticketmaster's presale reached four times its previous peak. [@tm-statement]

## With AI

### What to ask the agent

> "Act as a system design interviewer. Ask me to design a ticket booking system, then stop me if I skip requirements or run over time on any step."

> "Here are my requirements and my design for seat booking. List every way two users could end up with the same seat, and which part of my design stops each one."

> "Check my back-of-envelope estimate: 10 million users in 10 minutes, 10 seat-map reads each. Show each step with units."

### What to check yourself

- Every part of the design maps to a requirement you wrote first.
- The agent did not quietly add requirements or skip the non-functional ones.
- Double booking is stopped by the database, not only by a cache or a lock that can expire.
- Numbers come with units and labelled assumptions. Agents often state guesses as facts.
- You can explain every trade-off in your own words. An interviewer will ask "why?"

## Check yourself

## Go deeper

Before this: [latency numbers and estimation](/learn/latency-numbers-and-estimation) and [scaling from zero to millions](/learn/scaling-from-zero-to-millions). Next: [requirements and non-functional needs](/learn/requirements-and-non-functional-needs) and [design deep dives and trade-offs](/learn/design-deep-dives-trade-offs).
