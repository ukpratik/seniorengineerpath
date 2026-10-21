---
topic: realtime-transports
subtitle: How a server can push live updates to apps, and how to pick between long polling, Server-Sent Events and WebSockets.
status: published
reviewed_by: ai-factcheck
reviewed_on: 2026-10-22
illustrative_story: true
sources:
  - id: rfc6455
    title: "Fette and Melnikov, RFC 6455: The WebSocket Protocol (IETF, 2011)"
    url: https://www.rfc-editor.org/rfc/rfc6455
    note: The opening handshake (HTTP Upgrade and 101), frames, ping and pong, and the closing handshake.
  - id: whatwg-sse
    title: "WHATWG, HTML Living Standard, section 9.2: Server-sent events"
    url: https://html.spec.whatwg.org/multipage/server-sent-events.html
    note: The EventSource API, the text/event-stream format, automatic reconnect and the Last-Event-ID header.
  - id: rfc6202
    title: "Loreto, Saint-Andre, Salsano and Wilkins, RFC 6202: Known Issues and Best Practices for the Use of Long Polling and Streaming in Bidirectional HTTP (IETF, 2011)"
    url: https://www.rfc-editor.org/rfc/rfc6202
    note: How long polling and HTTP streaming work, and their costs in latency, headers, held connections and proxies.
  - id: mdn-sse
    title: "MDN Web Docs, “Using server-sent events”"
    url: https://developer.mozilla.org/en-US/docs/Web/API/Server-sent_events/Using_server-sent_events
    note: Code for the browser and the server, and the six-connection limit without HTTP/2.
  - id: mdn-ws
    title: "MDN Web Docs, “The WebSocket API (WebSockets)”"
    url: https://developer.mozilla.org/en-US/docs/Web/API/WebSockets_API
    note: The browser API, and why the plain WebSocket interface has no backpressure.
  - id: mdn-buffered
    title: "MDN Web Docs, “WebSocket: bufferedAmount property”"
    url: https://developer.mozilla.org/en-US/docs/Web/API/WebSocket/bufferedAmount
    note: How many bytes are queued by send() but not yet sent on the network.
  - id: rfc8441
    title: "McManus, RFC 8441: Bootstrapping WebSockets with HTTP/2 (IETF, 2018)"
    url: https://www.rfc-editor.org/rfc/rfc8441
    note: Running a WebSocket on one stream of an HTTP/2 connection with an extended CONNECT.
  - id: nginx-ws
    title: "nginx documentation, “WebSocket proxying”"
    url: https://nginx.org/en/docs/http/websocket.html
    note: The headers a reverse proxy must pass, and the 60-second idle timeout.
  - id: socketio-nodes
    title: "Socket.IO documentation, “Using multiple nodes” (source on GitHub)"
    url: https://github.com/socketio/socket.io-website/blob/main/docs/categories/02-Server/using-multiple-nodes.md
    note: Why long polling needs sticky sessions, and why many servers need an adapter such as Redis to broadcast.
  - id: slack-rtm
    title: "Sameera Thangudu, “Real-time Messaging”, Slack Engineering, 2023"
    url: https://slack.engineering/real-time-messaging/
    note: Gateway servers that hold WebSockets, channel servers found by consistent hashing, and the path of one message.
  - id: discord-elixir
    title: "Stanislav Vishnevskiy, “How Discord Scaled Elixir to 5,000,000 Concurrent Users”, Discord blog, 2017"
    url: https://discord.com/blog/how-discord-scaled-elixir-to-5-000-000-concurrent-users
    note: One session process per WebSocket, pub/sub inside a guild, and what broke under load.
claims_checked:
  - claim: "A WebSocket starts with an HTTP Upgrade request answered by 101 Switching Protocols. A ping frame can be a keepalive or a liveness check, and the receiver must reply with a pong. Clients should reconnect after a random delay, then with backoff."
    source: rfc6455
    note: "Sections 1.3, 5.5.2 and 7.2.3. 7.2.3 suggests a random first delay (0 to 5 seconds) and then e.g. truncated binary exponential backoff. Wording changed to say RFC 6455 leaves reconnecting to the client."
  - claim: "Long polling: the server holds the request until an update or a timeout; worst-case latency is over three network transits; every poll is a full HTTP message; about 30 seconds is a safer timeout because of proxies; intermediaries may buffer a whole response."
    source: rfc6202
    note: "Sections 2.1, 2.2, 3.2 and 5.5. The RFC says the client sends a new request 'either immediately' or after a pause, so 'at once' became 'usually at once'. RFC 6202 does not mention sticky sessions."
  - claim: "SSE uses text/event-stream; events have ids; the browser reconnects and sends Last-Event-ID; the spec suggests a comment line about every 15 seconds for legacy proxies. MDN says it is one-way and, without HTTP/2, limited to 6 connections per browser and domain (HTTP/2 default 100 streams)."
    source: whatwg-sse
    note: "WHATWG 9.2.1, 9.2.4, 9.2.5, 9.2.7. The one-way statement and the 6-connection limit come from MDN (mdn-sse), quoted with its 'when not used over HTTP/2' and 'per browser + domain' scope. The earlier 'over HTTP/1.1, about six connections per domain' wording was replaced. MDN does not mention POST, so POST is now the lesson's advice."
  - claim: "nginx does not pass the hop-by-hop Upgrade header, so Upgrade and Connection must be set; by default it closes the connection if the proxied server sends no data for 60 seconds, fixed by proxy_read_timeout or server pings."
    source: nginx-ws
    note: "Matches the nginx WebSocket proxying page. Quiz 2 'why' reworded to say the 60 seconds is the default and is about the proxied server."
  - claim: "Long polling needs sticky sessions with many nodes because it sends many HTTP requests; WebSocket-only does not; broadcasting across nodes needs an adapter, such as Redis."
    source: socketio-nodes
    note: "Matches the Socket.IO 'Using multiple nodes' page."
  - claim: "Slack gateway servers in multiple regions hold WebSocket subscriptions; channel servers are found by consistent hashing and send each message to every subscribed gateway, which forwards it to its clients. Discord gives each WebSocket a session process and fans guild events out to every connected session."
    source: slack-rtm
    note: "Slack post by Sameera Thangudu, April 2023. Discord post (discord-elixir, July 2017) checked too. The earlier Discord sentence merged two separate problems (registry requests piling up, and session queues causing an OOM of the whole Erlang VM). It now states them separately."
quiz:
  - q: Your app shows live sports scores. The browser only needs to receive updates. What is the simplest good fit?
    options:
      - Server-Sent Events, because data flows one way and the browser reconnects by itself.
      - WebSocket, because it is always faster.
      - Short polling every second.
    answer: 0
    why: SSE is one-way from server to client, and the EventSource API reconnects on its own with the Last-Event-ID header.
  - q: Your WebSocket connections behind nginx close after one quiet minute. What is a likely cause?
    options:
      - WebSockets cannot stay open longer than 60 seconds.
      - The proxy closes a connection when the server sends nothing for 60 seconds, which is the nginx default.
      - The browser limits each tab to one minute.
    answer: 1
    why: By default, nginx closes the connection if the proxied server sends no data for 60 seconds. Raise proxy_read_timeout, or have the server send ping frames.
  - q: You run 4 WebSocket servers. A user on server 3 does not get a message created on server 1. What is missing?
    options:
      - A bigger load balancer.
      - A pub/sub layer, so every server that holds a subscriber gets a copy of the message.
      - HTTP/2.
    answer: 1
    why: Each server only knows its own connections. Socket.IO calls this part an adapter, and Slack's channel servers do this job.
  - q: Why does long polling across many servers often need sticky sessions?
    options:
      - Each client sends many separate requests, and they must reach the server that holds its session.
      - Long polling encrypts data per server.
      - It does not need them.
    answer: 0
    why: The Socket.IO docs say long polling sends many HTTP requests, so the load balancer must route each client to the same server.
incidents: []
cases: [slack-realtime-messaging, discord-elixir-fanout]
related: [http-versions, load-balancing, presence-fanout, backpressure, webhooks]
glossary_optout: [fan-out]
---

## What broke

A food delivery app adds live order tracking. When the restaurant marks an order as "cooking" or the rider moves, the phone should update at once. The team picks WebSockets and builds it in two weeks. On a laptop, it works well.

In production, two problems appear. First, many phones show "Reconnecting…" again and again. The team finds that their nginx proxy closes any connection that is quiet for 60 seconds. A cooking order can be quiet for ten minutes.

Second, some users never see "rider picked up". The team runs four WebSocket servers. The update is created on server 1, but the user's phone is connected to server 3. Server 1 has no idea that server 3 holds the user.

The fix takes another two weeks: ping frames every 30 seconds, and a Redis pub/sub channel per order. This story is made up, but both problems are real and well documented. [@nginx-ws] [@socketio-nodes]

## The idea in simple words

Normal HTTP is request and response. The client asks, and the server answers. The server cannot speak first. For live updates, like chat, scores or a rider on a map, you need the server to push.

There are four common ways to do this:

- **Short polling**: the client asks "anything new?" every few seconds. Simple, but most answers are empty, and updates wait until the next ask.
- **Long polling**: the client asks, and the server holds the request until there is news or a timeout. Then the client asks again. [@rfc6202]
- **Server-Sent Events (SSE)**: the client opens one HTTP response that never ends. The server keeps writing events into it. Data flows only from server to client. [@whatwg-sse] [@mdn-sse]
- **WebSocket**: the client starts with an HTTP request and asks to "upgrade". After that, one connection stays open, and each side can send at any time. [@rfc6455]

> [!key] **The key idea.** Pushing data means holding a connection open for every user. The hard part is not the protocol. It is keeping millions of open connections alive, routed and fed at the right speed.

## How it works

Step through one chat message, delivered three ways. Use Next and Back, or the ← and → keys.

[[diagram:realtime-new-message]]

**Long polling.** The server answers only when an event happens or a timeout ends. The client then sends a new request, usually at once. [@rfc6202] Every poll is a full HTTP message with headers. In the worst case, a message waits more than three network trips, because it can arrive just after a response left. RFC 6202 says a timeout of about 30 seconds is safer than longer ones, which proxies may cut. [@rfc6202]

**SSE.** The browser calls `new EventSource("/events")`. The server answers with the type `text/event-stream` and writes lines like `data: hello`, with a blank line after each event. [@mdn-sse] Each event can carry an `id`. If the connection drops, the browser reconnects by itself and sends the last id in a `Last-Event-ID` header. The server can then send what the client missed. [@whatwg-sse] The client cannot send events on this connection. [@mdn-sse] To send data, use normal requests, such as POST.

**WebSocket.** The client sends an HTTP Upgrade request. The server answers `101 Switching Protocols`, and from then on, either side can send frames at any time. [@rfc6455] RFC 6455 leaves reconnecting to the client. It says clients should wait a random time before the first reconnect, then use [backoff](/learn/backoff-jitter-retry-budgets). [@rfc6455] There is also no built-in "what did I miss", so you design that yourself.

**Proxies and load balancers.** Anything between the app and your server can break a long-lived connection.

- `Upgrade` is a hop-by-hop header, so a reverse proxy does not pass it on by default. In nginx you must set the `Upgrade` and `Connection` headers yourself. [@nginx-ws]
- nginx closes a proxied connection if the server sends nothing for 60 seconds. You can raise `proxy_read_timeout`, or send ping frames. [@nginx-ws]
- Some old proxies drop quiet HTTP connections too. The SSE spec suggests a comment line, starting with `:`, about every 15 seconds. [@whatwg-sse] Proxies may also buffer a whole response before they send any of it, which breaks streaming. [@rfc6202]

**Heartbeats.** A WebSocket ping frame can work as a keepalive or as a check that the other side still answers. The other side must reply with a pong. [@rfc6455] Heartbeats do two jobs: they keep proxies from closing quiet connections, and they find dead connections, so you can free their memory.

**HTTP/2.** Without HTTP/2, MDN says a browser allows only 6 SSE connections per domain, across all tabs. So a few open tabs can use them all up. Over HTTP/2, the browser and server agree on a stream limit, and the default is 100. [@mdn-sse] RFC 8441 lets a WebSocket run on one HTTP/2 stream instead of its own TCP connection. [@rfc8441]

**Scaling many open connections.** Each user now holds a connection to one server. Two things follow.

- **Routing.** A WebSocket stays on the server where it started. Long polling sends many separate requests, so they need [sticky sessions](/learn/load-balancing) to reach the server that holds the client's state. [@socketio-nodes]
- **Fan-out.** One event must reach every user who cares, but those users sit on many servers. You put a [pub/sub](/learn/presence-and-real-time-fan-out) layer in the middle. Each server subscribes to the topics of its own users, and passes messages down their connections. Socket.IO calls this an adapter, and Redis is one option. [@socketio-nodes]

Slack does this at large scale. Gateway servers in many regions hold the users' WebSockets. A channel server, found by consistent hashing, sends each message to every gateway with a subscriber. Each gateway then sends it to its own clients. [@slack-rtm] At Discord, each WebSocket gets its own session process. Anything published in a guild goes to every session connected to it. [@discord-elixir]

**Backpressure for slow clients.** A phone on a weak network reads slowly. If the server keeps sending, messages pile up in memory. MDN says the plain browser `WebSocket` interface has no backpressure. Fast messages can fill memory or use all the CPU. [@mdn-ws] On the sending side, `bufferedAmount` shows the bytes queued by `send()` that are not yet on the network. [@mdn-buffered] Watch your server's queue per connection. When it grows too large, drop old updates, merge them, or close the connection and let the client resync.

Discord learned how slow parts spread. During peak hours, some guild processes could not keep up with their message queues. In another case, session processes grew their message queues until the whole Erlang VM ran out of memory. [@discord-elixir]

## Trade-offs

| Option | Direction | Reconnect and missed events | Proxies and load balancers | Use it when |
|---|---|---|---|---|
| Short polling | Client asks | Yes. Each ask is fresh. | Easy. Plain requests. | Updates are rare, and a delay of seconds is fine. |
| Long polling | Server to client, one answer per request | Yes. Next request picks up. | Needs sticky sessions with many servers. [@socketio-nodes] Header cost on every message. [@rfc6202] | A fallback for networks that block other options. |
| SSE | Server to client only | Yes. Built in, with `Last-Event-ID`. [@whatwg-sse] | Plain HTTP. Use HTTP/2, or six tabs can use up the browser limit. [@mdn-sse] | Feeds, scores, notifications, streaming AI answers. |
| WebSocket | Both ways | No. You build reconnect and catch-up. | Proxy must pass `Upgrade`. Idle timeouts need pings. [@nginx-ws] | Chat, games, shared editing: the client sends often too. |

**Simple rule:** if only the server talks, start with SSE. Choose WebSocket when the client also sends often. Keep long polling as a fallback.

## At work: how a senior uses this

- Ask first: **"How fast must the update arrive, and who talks?"** A ten-second delay may be fine with plain polling.
- Plan "what did I miss" from day one. Give events an id, and let the client ask for everything after its last id.
- Check every hop: CDN, load balancer, proxy. Ask: **"What is the idle timeout here?"** Set your heartbeat below the smallest one.
- Treat connections as a resource. Count open connections per server, and alert on sudden drops.
- After a deploy or outage, every client reconnects at once. Add random [jitter](/glossary#jitter) to reconnect delays.
- Keep connection servers thin. They hold sockets and forward messages. Business logic lives in normal services.
- Load test with real numbers of idle connections, not only with requests per second.

## With AI

### What to ask the agent

> "Write an SSE endpoint in Node for order status. Send an id with each event, support Last-Event-ID, and send a comment line every 15 seconds."

> "Review this WebSocket client. Does it reconnect with backoff and jitter? Does it fetch missed events after a reconnect?"

> "We run 6 WebSocket servers behind a load balancer. Design the pub/sub layer with Redis so a message reaches users on any server."

### What to check yourself

- The proxy config passes `Upgrade` and `Connection`, and the idle timeout is longer than the heartbeat.
- The server sends heartbeats, and closes connections that stop answering.
- Reconnect has a limit, backoff and jitter. A loop with a fixed one-second delay can flood your servers after an outage.
- Each connection has a send queue with a maximum size. Agents often write `socket.send()` in a loop with no limit.
- The client sends an auth token on connect, and the server checks that the user may see each topic.

## Check yourself

## Go deeper

- [HTTP/1.1, HTTP/2, HTTP/3 and QUIC](/learn/http-1-2-3-and-quic): why many streams on one connection matter.
- [Presence and real-time fan-out](/learn/presence-and-real-time-fan-out): sending one event to millions of users.
- [Backpressure and flow control](/learn/backpressure-and-flow-control): what to do when a reader is slower than a writer.
