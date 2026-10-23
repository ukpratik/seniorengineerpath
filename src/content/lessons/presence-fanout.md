---
topic: presence-fanout
subtitle: How to show who is online and typing, and push each change only to the people who need it, without the cost growing out of control.
status: published
reviewed_by: ai-factcheck
reviewed_on: 2026-10-23
illustrative_story: false
sources:
  - id: discord-elixir
    title: "Stanislav Vishnevskiy, “How Discord Scaled Elixir to 5,000,000 Concurrent Users”, Discord blog, 2017"
    url: https://discord.com/blog/how-discord-scaled-elixir-to-5-000-000-concurrent-users
    note: Session and guild processes, fan-out to every session in a guild, slow publishes at peak, and the Manifold library.
  - id: discord-maxjourney
    title: "Yuliy Pisetsky, “Maxjourney: Pushing Discord's Limits with a Million+ Online Users in a Single Server”, Discord blog, 2023"
    url: https://discord.com/blog/maxjourney-pushing-discords-limits-with-a-million-plus-online-users-in-a-single-server
    note: Why work grows with the square of server size, passive sessions, and relays that share the fan-out.
  - id: slack-rtm
    title: "Sameera Thangudu, “Real-time Messaging”, Slack Engineering, 2023"
    url: https://slack.engineering/real-time-messaging/
    note: Gateway, channel and presence servers, consistent hashing, and presence only for users visible on screen.
  - id: slack-presence
    title: "Slack developer docs, “User presence and status”"
    url: https://docs.slack.dev/apis/web-api/user-presence-and-status
    note: Active and away, auto-away after 10 minutes, presence subscriptions and batched presence events.
  - id: discord-gateway
    title: "Discord developer docs, “Gateway”"
    url: https://docs.discord.com/developers/events/gateway
    note: Heartbeat interval with jitter, heartbeat ACKs, zombied connections, and presence as an opt-in event.
claims_checked:
  - claim: "Discord had nearly five million concurrent users (2017); it was first built for groups of 25 or fewer, some guilds reached 30,000 concurrent users, and staff turned off message-making features by hand at peak."
    source: discord-elixir
    note: All matched in the 2017 post. "Groups of 25 or less" is the post's wording.
  - claim: "One send took 30 to 70 microseconds, so publishing one event from a large guild took 900 ms to 2.1 s; Manifold groups target processes by remote node so the sender makes at most one send per node, spreading CPU cost and cutting network traffic."
    source: discord-elixir
    note: Numbers and Manifold behaviour match the post. Session queues growing until the Erlang VM ran out of memory, and presence rebuilding within minutes after a crash, are also in the post.
  - claim: "Work grows with the square of server size: 1,000 online users each posting once is 1 million notifications, 10,000 is 100 million, 100,000 is 10 billion."
    source: discord-maxjourney
    note: Exact numbers from the 2023 post (Midjourney example).
  - claim: "About 90% of user-guild connections in large servers were passive and kept in a separate list, making fan-out about 90% cheaper; each relay handles up to 15,000 sessions and does fan-out with permission checks."
    source: discord-maxjourney
    note: Matches the post. Passive users are those in many guilds who may not click into some of them.
  - claim: "Slack gateway servers hold user info and channel subscriptions and are the only servers deployed across regions; channel servers use consistent hashing; users are hashed to presence servers; clients get presence only for users visible on screen."
    source: slack-rtm
    note: Each point matches the April 2023 Slack Engineering post. "Channel" also covers users, teams, enterprises, files and huddles.
  - claim: "Slack presence is active or away, auto-away after 10 minutes; presence_sub must list every user ID each time; since January 2018 presence_change is not sent without subscriptions; batch_presence_aware=1 gives a users array."
    source: slack-presence
    note: Matches the Slack developer docs.
  - claim: "Discord's Hello gives a heartbeat interval (example 45,000 ms); the first beat waits interval times a random jitter; a missed heartbeat closes the connection; no ACK means a zombied connection to close and resume; GUILD_PRESENCES is privileged and off by default."
    source: discord-gateway
    note: Matches the Gateway docs.
  - claim: "Hybrid push-to-viewers, pull-for-the-rest presence design."
    source: discord-maxjourney
    note: Written as the lesson's own advice. Twitter's Timelines at Scale talk could not be opened as text, so it is not cited. Passive sessions are cited only as a close example.
quiz:
  - q: A team chat has 2,000 people online, and each of them watches everyone else's status. One status change goes to how many people, and how does total work grow?
    options:
      - One change goes to about 2,000 people, and total work grows with the square of the team size.
      - One change goes to one server, so team size does not matter.
      - One change goes to about 2,000 people, but total work stays flat as the team grows.
    answer: 0
    why: Each change goes to every watcher, and every member also makes changes. Discord shows 1,000 users making 1 million notifications and 10,000 making 100 million.
  - q: A phone loses signal in a tunnel. No close message reaches the server. How does the server find out the user is gone?
    options:
      - The TCP connection tells the server at once.
      - Heartbeats stop arriving, and after a timeout the server marks the user offline.
      - It cannot ever find out.
    answer: 1
    why: A dead network sends nothing. The server must notice the missing heartbeats and use a timeout. Discord's docs tell clients to reconnect when heartbeat ACKs stop, for the same reason.
  - q: Slack sends presence updates only for users who are visible on the screen. What is the main gain?
    options:
      - Better security.
      - Far fewer messages, because most people in a big team are not on any one screen.
      - Status becomes more exact.
    answer: 1
    why: Slack's post says clients get presence only for users on screen, and its API sends presence_change events only for users a client subscribed to.
  - q: In Discord's largest servers, about 90% of user-guild connections were passive. What did Discord do with them?
    options:
      - It closed them.
      - It kept them in a separate list that does not get every event, which cut fan-out work by about 90%.
      - It moved them to a bigger machine.
    answer: 1
    why: Passive sessions do not receive every action. Discord says this cut fan-out work by about 90% in large servers.
incidents: []
cases: [discord-elixir-fanout, slack-realtime-messaging]
related: [realtime-transports, fan-out, sharding, backpressure, queues-vs-logs]
glossary_optout: [fan-out, request-coalescing]
---

## What broke

In 2017, Discord had nearly five million users online at the same time. Each user had one session process. Each Discord server, called a guild, had its own process. Anything that happened in a guild was sent to every session connected to it. [@discord-elixir]

Discord was first built for groups of 25 or fewer. Then some guilds grew to 30,000 users online at once. At peak hours, the guild processes could not keep up with their message queues. The team had to turn off features that created messages, by hand. [@discord-elixir]

They measured why. One send call took 30 to 70 microseconds, because Erlang could pause the sending process. A large guild had to send one event to every connected session. So one event could take from 900 milliseconds to 2.1 seconds to publish. [@discord-elixir]

Six years later, the same team explained the core problem. The work for a server grows with the square of its size. If 1,000 people are online and each sends one message, that is 1 million notifications. With 10,000 people it is 100 million. With 100,000 people it is 10 billion. [@discord-maxjourney]

## The idea in simple words

**Presence** is the small status next to a name: online, away, or "typing…". Slack, for example, has two presence values, `active` and `away`. A user becomes `away` after 10 minutes with no activity. [@slack-presence]

Presence looks cheap, because each change is tiny. But each change goes to everyone who watches that user. If N people each watch M people, every change creates work for many screens. When a team grows, both N and M grow, so total work grows with the square of the size. [@discord-maxjourney]

**Real-time fan-out** means taking one event and sending a copy to every open connection that needs it. A chat message, a presence change and a "typing" signal all need fan-out.

> [!key] **The key idea.** Presence is cheap to store and expensive to send. Most of the design is about sending each change to fewer people, less often.

## How it works

Step through one user going offline. Use Next and Back, or the ← and → keys.

[[diagram:presence-offline]]

**Connection servers.** Clients hold a [WebSocket](/learn/websockets-sse-long-polling) to a connection server. Slack calls them gateway servers. They keep user data and the list of channels each client subscribed to. They are the only Slack servers spread across many regions. [@slack-rtm] Keep these servers thin. They hold sockets and pass messages on.

**Heartbeats and timeouts.** A phone in a tunnel does not say goodbye. Its connection just goes quiet. So the client sends a small heartbeat on a fixed interval. Discord's gateway sends the client an interval, for example 45,000 ms. The client waits a random part of that interval before its first heartbeat, so clients do not all beat at the same moment. [@discord-gateway]

The server answers each heartbeat with an ACK. If a client misses a heartbeat, Discord closes the connection. If the client gets no ACK, it should treat the connection as dead, close it and reconnect. Discord calls this a "zombied" connection. [@discord-gateway]

Pick the timeout with care. A short timeout marks people offline every time a train enters a tunnel. Status then flickers on everyone's screen, and each flicker is one more fan-out. A long timeout shows people as online after they left. The 30 seconds and 90 seconds in the diagram are only an example.

**One owner per user.** Slack hashes each user to one presence server, and those servers keep in memory who is online. [@slack-rtm] One owner means one place decides "Asha is offline". Also make one rule for many devices. For example, a user is online if any device is connected.

**Pub/sub and sharding.** Every user, channel or team gets a channel in a pub/sub layer. Connection servers subscribe for their own clients. Slack's channel servers each own a share of channels, found by [consistent hashing](/learn/sharding-and-consistent-hashing). The word "channel" there covers users, teams, files and huddles too. [@slack-rtm] Sharding by channel or user spreads the load. One hot channel still lands on one owner, so watch for it.

**Fan-out on write or on read.** There are two ways to deliver an event.

- **Push** ([fan-out on write](/learn/fan-out-on-write-vs-read)): when the event happens, send it to every watcher at once. Reads are cheap, but one event to a huge audience is very costly.
- **Pull** (fan-out on read): store the event once. Each client asks for it when it opens the screen. Writes are cheap, but each open screen pays a little.

Many systems mix the two. Our advice for presence is the same mix: push to people who are looking right now, and let everyone else pull a fresh snapshot when they open the screen. This is this lesson's own advice, not a rule from one source. Discord's passive sessions, below, are close to this idea.

**Limit presence to what the user can see.** This is the biggest win. In Slack, clients get presence updates only for users visible on the screen at that moment. [@slack-rtm] Slack's API works the same way. A client sends a `presence_sub` message with the full list of user IDs it wants. Since January 2018, `presence_change` events are not sent without these subscriptions. [@slack-presence]

Discord found the same at its scale. Some users are in many guilds and may never open some of them. Discord moved those connections to a separate "passive" list that does not get every event. Around 90% of user-guild connections in large servers were passive, so fan-out work dropped by about 90%. [@discord-maxjourney] Bots must even opt in to presence. Discord's `GUILD_PRESENCES` intent is privileged and off by default. [@discord-gateway]

**Batch the updates.** Instead of one message per change, send a few changes together. Slack's API batches presence updates into one `presence_change` event with a `users` array, when the client asks with `batch_presence_aware=1`. [@slack-presence] You can also merge updates for the same user. If Asha goes offline and back online within one second, send only the latest state, or nothing.

**Spread the sending work.** One process that sends to a million connections becomes the bottleneck. Discord built Manifold. It groups the target processes by remote machine, so the sender makes at most one send per machine, and workers there finish the job. It also cut network traffic between machines. [@discord-elixir] Later, Discord added relays between a guild and its sessions. Each relay handles up to 15,000 connected sessions and does the fan-out and permission checks, so one guild can use many machines. [@discord-maxjourney]

**Protect yourself from slow parts.** When the fan-out falls behind, queues grow. Discord's session processes once grew their message queues until the whole Erlang VM ran out of memory. [@discord-elixir] Give each connection a bounded queue. For presence, dropping an old update is fine, because only the newest state matters. See [Backpressure and flow control](/learn/backpressure-and-flow-control).

## Trade-offs

| Option | Cost per change | Freshness | Use it when |
|---|---|---|---|
| Push to every watcher | High. One message per watcher, and it grows with the square of group size. [@discord-maxjourney] | Instant. | Small groups, like a 1:1 chat or a team of 50. |
| Push only to what is on screen | Low. Only active subscribers get it. [@slack-rtm] | Instant for what you see. Others refresh when they open. | Big teams and servers. This is the usual default. |
| Pull when a screen opens | Very low on write. Each open screen pays one read. | As fresh as the last read. | Member lists and huge groups where status is a nice extra. |
| Batch and merge updates | Fewer, larger messages. [@slack-presence] | Slightly late, by the batch window. | Busy groups where status changes often. |
| Short heartbeat timeout | More traffic and more flicker. | Fast to show "offline". | Calls and games, where a gone user matters at once. |
| Long heartbeat timeout | Less traffic. | Slow to show "offline". | Chat, where a few minutes of error is fine. |

**Simple rule:** never send presence to everyone. Send it to what is on screen, batch it, and let the rest pull.

## At work: how a senior uses this

- Do the multiplication first. Ask: **"How many watchers per change, and how many changes per second?"** Multiply them before you pick a design.
- Ask: **"Who actually sees this status right now?"** Design subscriptions around the screen, not around the team list.
- Treat presence as soft state. It can be a few seconds late or wrong after a crash. Discord's presence services once crashed, and sessions stayed up while presence rebuilt in minutes. [@discord-elixir]
- Keep presence apart from messages. Never let a burst of "typing" signals delay real chat messages.
- Set heartbeat and timeout from data. Measure how often your users lose signal for 10, 30 and 60 seconds.
- Load test the biggest group you have, with everyone online. Tests with many small groups hide the square growth.
- Add a kill switch for "typing" and presence in huge groups. Discord had to turn off message-making features by hand when guilds fell behind. [@discord-elixir]

## With AI

### What to ask the agent

> "We have 5,000-person teams. Estimate presence messages per second if each user changes status 20 times a day and every member watches every other member. Then estimate again if only users on screen are watched."

> "Write a WebSocket gateway in Node that keeps a set of presence subscriptions per client, sends updates only for those users, and batches them every 500 ms with only the latest state per user."

> "Review this heartbeat code. Does it add jitter to the first beat, detect missing ACKs, and close zombied connections?"

### What to check yourself

- The code does not loop over all team members for each change. Agents often write exactly that.
- Subscriptions are removed when a client disconnects, or the server keeps sending to ghosts.
- A user is offline only when all devices are gone, not when one tab closes.
- Each connection has a bounded queue, and old presence updates are dropped first.
- The server checks that the client may see each user it subscribes to.

## Check yourself

## Go deeper

- [WebSockets, SSE and long polling](/learn/websockets-sse-long-polling): how the open connections work.
- [Fan-out on write vs read](/learn/fan-out-on-write-vs-read): the same choice for news feeds and timelines.
- [Sharding and consistent hashing](/learn/sharding-and-consistent-hashing): how channels are spread over servers.
- [Queues vs logs](/learn/queues-vs-logs-delivery-guarantees): when an event must not be lost, unlike presence.
