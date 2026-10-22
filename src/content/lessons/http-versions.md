---
topic: http-versions
subtitle: Learn what really changed from HTTP/1.1 to HTTP/2 and HTTP/3, so you can pick settings for clients, load balancers and timeouts with care.
status: published
reviewed_by: ai-factcheck
reviewed_on: 2026-10-22
illustrative_story: true
sources:
  - id: rfc9110
    title: "Fielding, Nottingham and Reschke (eds.), RFC 9110: HTTP Semantics (IETF, 2022)"
    url: https://www.rfc-editor.org/rfc/rfc9110
    note: Methods, status codes and headers. Section 1.2 says all three HTTP versions share these semantics.
  - id: rfc9112
    title: "Fielding, Nottingham and Reschke (eds.), RFC 9112: HTTP/1.1 (IETF, 2022)"
    url: https://www.rfc-editor.org/rfc/rfc9112
    note: Section 9 covers persistent connections, pipelining, how many connections to open, and timeouts.
  - id: rfc9113
    title: "Thomson and Benfield (eds.), RFC 9113: HTTP/2 (IETF, 2022)"
    url: https://www.rfc-editor.org/rfc/rfc9113
    note: Section 1 says why HTTP/2 exists. Frames, streams, SETTINGS, GOAWAY and connection rules follow.
  - id: rfc7541
    title: "Peon and Ruellan, RFC 7541: HPACK: Header Compression for HTTP/2 (IETF, 2015)"
    url: https://www.rfc-editor.org/rfc/rfc7541
    note: Section 1 explains why headers needed a new, safer compression format.
  - id: chrome-push
    title: "Barry Pollard, “Remove HTTP/2 Server Push from Chrome”, Chrome for Developers blog, 2022"
    url: https://developer.chrome.com/blog/removing-push
    note: Why Chrome turned off server push, with usage numbers and the suggested alternatives.
  - id: rfc9114
    title: "Bishop (ed.), RFC 9114: HTTP/3 (IETF, 2022)"
    url: https://www.rfc-editor.org/rfc/rfc9114
    note: Section 1 compares the three versions. Section 3.1 explains how a client finds an HTTP/3 server.
  - id: rfc9000
    title: "Iyengar and Thomson (eds.), RFC 9000: QUIC: A UDP-Based Multiplexed and Secure Transport (IETF, 2021)"
    url: https://www.rfc-editor.org/rfc/rfc9000
    note: QUIC streams, and section 9 on connection migration.
  - id: rfc7301
    title: "Friedl, Popov, Langley and Stephan, RFC 7301: TLS Application-Layer Protocol Negotiation Extension (IETF, 2014)"
    url: https://www.rfc-editor.org/rfc/rfc7301
    note: How client and server agree on "h2" or "http/1.1" inside the TLS handshake.
  - id: rfc7838
    title: "Nottingham, McManus and Reschke, RFC 7838: HTTP Alternative Services (IETF, 2016)"
    url: https://www.rfc-editor.org/rfc/rfc7838
    note: The Alt-Svc header, and how long a client may remember it.
  - id: mdn-evolution
    title: "MDN Web Docs, “Evolution of HTTP”"
    url: https://developer.mozilla.org/en-US/docs/Web/HTTP/Guides/Evolution_of_HTTP
    note: A short history from HTTP/0.9 to HTTP/3, in plain words.
  - id: mdn-altsvc
    title: "MDN Web Docs, “Alt-Svc header”"
    url: https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Headers/Alt-Svc
    note: Examples of the header, with the ma and persist parameters.
  - id: aws-alb-tg
    title: "AWS, Elastic Load Balancing User Guide, “Target groups for your Application Load Balancers”"
    url: https://docs.aws.amazon.com/elasticloadbalancing/latest/application/load-balancer-target-groups.html
    note: The "protocol version" setting, which decides if the load balancer talks HTTP/1.1, HTTP/2 or gRPC to your servers.
claims_checked:
  - claim: All three major versions of HTTP rely on the same semantics (methods, status codes, headers).
    source: rfc9110
    note: Section 1.2 says this word for word.
  - claim: HTTP/1.1 has no multiplexing layer, so clients open several TCP connections; TCP does not share congestion control across them. With HTTP/2, a lost or reordered packet stalls all active transactions.
    source: rfc9114
    note: Section 1.1. Section 2 says HTTP/3 streams are independent, so a blocked or lossy stream does not stop others. MDN Evolution of HTTP says the same.
  - claim: Pipelined responses must come back in request order; clients ought to limit connections to a server, and no fixed maximum is set.
    source: rfc9112
    note: Sections 9.3.2 and 9.4. The lesson gives no per-host connection number, because RFC 9112 drops the old ceiling.
  - claim: TCP head-of-line blocking is not addressed by HTTP/2; clients SHOULD NOT open more than one HTTP/2 connection per host and port; SETTINGS_MAX_CONCURRENT_STREAMS is recommended to be at least 100; server push is difficult to use effectively.
    source: rfc9113
    note: Sections 1, 9.1, 6.5.2 and 8.4. GOAWAY last stream ID (6.8) and "h2" ALPN token (3.1, 3.2) also checked.
  - claim: Only 1.25% of HTTP/2 sites used push; studies had mixed results with no clear net gain and many regressions; push is off by default in Chrome 106; 103 Early Hints and preload are the alternatives.
    source: chrome-push
    note: All from the 2022 post. Earlier "often made pages slower" was softened to match the post's wording.
  - claim: SPDY's DEFLATE header compression was exposed by the CRIME attack; HPACK uses header tables so a field can be sent as an index.
    source: rfc7541
    note: Sections 1 and 1.1. RFC 9114 section 2 confirms HTTP/3 replaces HPACK with QPACK because HPACK needs in-order delivery.
  - claim: Connection IDs let a QUIC connection survive address changes; only clients start migration, and not before the handshake is confirmed; QUIC gives no ordering between bytes on different streams.
    source: rfc9000
    note: Section 9 and section 2. "After the handshake" was made exact ("is confirmed").
  - claim: ALPN negotiates the protocol inside the TLS handshake without adding round trips.
    source: rfc7301
    note: Sections 1 and 3.1.
  - claim: An Alt-Svc entry is fresh for 24 hours unless ma sets it; clients MAY try QUIC after seeing h3 in Alt-Svc and SHOULD fall back to TCP when QUIC fails.
    source: rfc7838
    note: RFC 7838 section 3.1 for the 24-hour default (also on the MDN Alt-Svc page, which says the URL and origin do not change). RFC 9114 sections 3.1 and 3.1.1 for h3 and fallback.
  - claim: An ALB sends requests to targets over HTTP/1.1 by default; the HTTP/2 or gRPC protocol version needs an HTTPS listener; an HTTP/1.1 request to an HTTP/2 target group is an error.
    source: aws-alb-tg
    note: The "Protocol version" section and its result table.
quiz:
  - q: You move an API from HTTP/1.1 to HTTP/2. What do your handlers need to change?
    options:
      - Every handler must read binary frames.
      - Nothing in the meaning. Methods, status codes and headers stay the same.
      - All GET requests must become POST.
    answer: 1
    why: RFC 9110 says all three major HTTP versions rely on the same semantics. Only how messages travel on the wire changes.
  - q: Ten requests share one HTTP/2 connection on a phone. One TCP packet is lost. What happens?
    options:
      - Only the request in that packet waits.
      - All requests behind the lost packet wait, because TCP gives bytes only in order.
      - HTTP/2 switches to UDP for that request.
    answer: 1
    why: TCP cannot see HTTP/2 streams, so a lost packet stalls them all. RFC 9113 says HTTP/2 does not fix TCP head-of-line blocking.
  - q: How does a browser first learn that your site also speaks HTTP/3?
    options:
      - From an Alt-Svc header in an earlier response, or another discovery method.
      - The browser always tries UDP first for every site.
      - From the URL, which must start with h3://.
    answer: 0
    why: RFC 9114 describes Alt-Svc as one way to advertise an HTTP/3 endpoint. The URL stays https://.
  - q: Your AWS Application Load Balancer accepts HTTP/2 from browsers. By default, what does it speak to your servers?
    options:
      - HTTP/3.
      - HTTP/2, always.
      - HTTP/1.1, unless you change the target group's protocol version.
    answer: 2
    why: AWS documents that the load balancer sends requests to targets with HTTP/1.1 by default.
incidents: []
related: [tcp-ip, tls, realtime-transports, load-balancing, cdn-http-caching, grpc]
---

## What broke

A shopping app loads its home screen with about forty small API calls. The API runs on HTTP/1.1. On slow networks, the screen takes many seconds to fill, because requests wait in line for free connections.

The team turns on HTTP/2 at the load balancer. Now all forty calls share one connection. On office Wi-Fi, the home screen is much faster, and everyone is happy.

Then support tickets arrive from users on trains. Sometimes the whole screen freezes for a second or two, and then everything appears at once. The team checks the servers, and they answered fast.

The cause was one lost packet. HTTP/2 sends every request over one TCP connection. TCP hands data to the app only in order. So one lost packet stalls every request on that connection, even requests the loss did not touch [@rfc9114]. The fix was to also offer HTTP/3 for clients that can use it.

This story is made up. But the behaviour is real and written in the standards.

## The idea in simple words

HTTP has two parts:

- **Semantics**: what a message means. Methods like GET and POST, status codes like 404, and headers. RFC 9110 says all three major HTTP versions rely on the same semantics [@rfc9110].
- **Wire format**: how those messages travel. This is the part each version changes.

So your handler code does not change when the version changes. What changes is speed, how many connections you hold, and what happens when packets are lost.

- **HTTP/1.1** sends text messages over TCP. It cannot mix requests on one connection, so clients open several connections to work in parallel [@rfc9114].
- **HTTP/2** uses binary frames instead of text, over one TCP connection. Many requests run at the same time, each in its own stream [@rfc9113] [@mdn-evolution].
- **HTTP/3** keeps the idea of streams, but runs on [QUIC](/glossary#quic), over UDP. A lost packet blocks only the streams it carried [@rfc9114] [@mdn-evolution].

> [!key] **The key idea.** Each new HTTP version keeps the same meaning and fixes the waiting of the version before. HTTP/2 removes waiting between requests. HTTP/3 removes waiting caused by TCP itself.

## How it works

Step through three requests, A, B and C, with one lost packet. Use Next and Back, or the ← and → keys.

[[diagram:http-versions-loss]]

### HTTP/1.1: one request at a time

HTTP/1.1 connections are persistent by default. One connection can serve many requests, one after another [@rfc9112]. This is [keep-alive](/glossary#keep-alive), and it saves a new TCP handshake for each request.

Pipelining lets a client send several requests without waiting. But the server must send the responses in the same order [@rfc9112]. So one slow response still blocks the rest [@rfc9113]. In practice, clients open several connections to one server instead [@rfc9113]. RFC 9112 asks clients to limit how many they open, because each one uses server resources. It gives no fixed maximum [@rfc9112].

Many connections also make congestion control work less well, because TCP does not share it across connections [@rfc9114].

### HTTP/2: many streams, one TCP connection

HTTP/2 cuts each message into binary **frames** [@rfc9113]. Each request and response pair gets its own **stream**, and frames from many streams mix on one connection [@rfc9113]. This is called **multiplexing**. RFC 9113 says clients should not open more than one HTTP/2 connection to a given host and port [@rfc9113].

Each side tells the other how many streams it may open at once. RFC 9113 recommends at least 100, so that parallel work is not limited [@rfc9113].

**HPACK.** Requests to one server repeat the same headers again and again, which wastes bandwidth [@rfc7541]. Before HPACK, SPDY compressed headers with DEFLATE. The CRIME attack showed that this could leak secrets [@rfc7541]. HPACK instead keeps tables of headers that both sides already know. A header can then be sent as a small index [@rfc7541].

**Server push.** HTTP/2 added an optional way for a server to send responses the client did not ask for yet [@rfc9113]. It did not work out well. RFC 9113 itself says push is hard to use well, because the server must guess what the client will ask for [@rfc9113]. Only 1.25% of HTTP/2 sites used it. Studies had mixed results, with no clear overall gain and often worse performance [@chrome-push]. Chrome announced that it would turn push off by default in version 106. The post suggests `103 Early Hints` and preload instead [@chrome-push].

**What HTTP/2 did not fix.** TCP still sees one ordered stream of bytes. RFC 9113 says plainly that TCP head-of-line blocking is not addressed [@rfc9113]. A lost packet stalls every stream, even streams the loss did not touch [@rfc9114]. See [TCP/IP and the network stack](/learn/tcp-ip-network-stack) for why TCP works this way.

### HTTP/3: streams that do not block each other

HTTP/3 maps each request and response to its own QUIC stream. If one stream is blocked or loses a packet, other streams still make progress [@rfc9114]. QUIC does not keep any order between bytes on different streams [@rfc9000].

Two other changes matter:

- **Faster setup.** QUIC includes TLS 1.3. RFC 9114 says this gives security like TLS over TCP, with faster connection setup [@rfc9114]. See [TLS and secure connections](/learn/tls-and-secure-connections) for the handshake.
- **Connection migration.** A QUIC connection has connection IDs, not only an address and port. These IDs let a connection survive a change of address [@rfc9000]. Only the client can start a migration, and only after the handshake is confirmed [@rfc9000].

HPACK assumes data arrives in order, which QUIC does not promise across streams. So HTTP/3 uses a new format, QPACK [@rfc9114].

### How a client picks a version

**ALPN.** In the TLS handshake, the client sends a list of protocols it supports. The server picks one. This adds no extra round trips [@rfc7301]. HTTP/2 over TLS uses the name `h2` [@rfc9113], and HTTP/3 uses `h3` [@rfc9114].

**Alt-Svc.** The URL is still `https://`, so the client needs a sign that the server also speaks HTTP/3. The server can send one in a response header, like `Alt-Svc: h3=":443"; ma=86400` [@rfc9114] [@mdn-altsvc]. The `ma` value is how many seconds the client may remember this. Without it, the default is 24 hours [@rfc7838]. The URL and origin do not change [@mdn-altsvc].

Alt-Svc is only a hint. The client may try QUIC after it sees one [@rfc9114]. If UDP is blocked, RFC 9114 tells clients to use HTTP over TCP instead [@rfc9114]. So keep TCP serving too.

### What changes for backend engineers

- **Load balancers often split the versions.** An AWS Application Load Balancer can accept HTTP/2 from browsers. By default it still sends requests to your servers over HTTP/1.1 [@aws-alb-tg]. You can choose HTTP/2 or gRPC per target group, only behind an HTTPS listener. With HTTP/2 chosen, requests that arrive as HTTP/1.1 fail [@aws-alb-tg].
- **One connection means less spread.** An HTTP/2 client should open only one connection per host and port [@rfc9113]. Behind a [layer 4](/glossary#layer-4) balancer, all its requests then reach one server. See [Load balancing](/learn/load-balancing).
- **Closing gracefully.** HTTP/2 has a GOAWAY frame. Its last stream ID tells the client which requests the server may have acted on [@rfc9113]. The rest were not processed, so they can be retried on a new connection [@rfc9113]. In HTTP/1.1, a side that wants to time out should close the connection gracefully [@rfc9112].
- **Retries.** Idempotent requests can be retried automatically after a connection fails [@rfc9112]. Others should not be, unless the client knows the first try was not applied [@rfc9110]. See [Idempotency, retries and timeouts](/learn/idempotency-retries-timeouts).

## Trade-offs

| Option | Requests per connection | A lost packet blocks | Good fit |
|---|---|---|---|
| HTTP/1.1 | One at a time. Clients open several. [@rfc9114] | Only that connection. | Simple service calls, old clients, debugging by hand. |
| HTTP/2 | Many streams on one TCP connection. [@rfc9113] | All streams on the connection. [@rfc9114] | Browsers and APIs with many small requests, gRPC. |
| HTTP/3 | Many streams on one QUIC connection. [@rfc9114] | Only streams with data in that packet. [@mdn-evolution] | Users on phones and lossy networks. |
| Server push | Extra responses the client did not ask for. [@rfc9113] | Not relevant. | Avoid. Chrome turned it off by default. Use `103 Early Hints` or preload. [@chrome-push] |

**Simple rule:** for users on the internet, serve HTTP/2, and add HTTP/3 at the edge if your CDN or load balancer supports it. Inside your data center, the version matters less than reusing connections.

## At work: how a senior uses this

- When someone says "HTTP/2 will make it faster", ask: **"Faster for which clients, on which network?"** Measure before and after.
- Know which version each hop speaks: client to CDN, CDN to load balancer, load balancer to server. They often differ.
- Check stream limits on both sides. A low stream limit on one HTTP/2 connection makes extra requests wait in a queue.
- During deploys, make sure servers send GOAWAY and drain, so clients move to a new connection without errors.
- Retry only idempotent requests after a connection drops. Use an [idempotency key](/glossary#idempotency-key) for payments.
- When you turn on HTTP/3, also watch QUIC errors and UDP traffic. Keep TCP working as the fallback.

## With AI

### What to ask the agent

> "Here is our nginx config. Which HTTP versions does it serve to clients, and which does it use to our upstream servers? Show the exact lines."

> "Our Go service calls another service over HTTP/2. Explain how the client reuses connections, and what happens to in-flight requests when the server sends GOAWAY."

> "Write a load test that compares HTTP/1.1 and HTTP/2 with 2% packet loss. Explain what I should expect and why."

### What to check yourself

- Every setting the agent names exists in your server or library version. Check the docs.
- The agent does not suggest HTTP/2 server push. Chrome turned it off by default [@chrome-push].
- Claims like "HTTP/3 is always faster" are tested on your own traffic, with loss and without.
- Retries are added only for idempotent requests.
- The agent knows that the load balancer may speak a different version to your servers than to clients.

## Check yourself

## Go deeper

- [TCP/IP and the network stack](/learn/tcp-ip-network-stack): handshakes, packet loss and head-of-line blocking.
- [TLS and secure connections](/learn/tls-and-secure-connections): how ALPN fits into the handshake.
- [WebSockets, SSE and long polling](/learn/websockets-sse-long-polling): long-lived connections on top of HTTP.
- [CDNs and HTTP caching](/learn/cdn-and-http-caching): where HTTP/3 is often turned on first.
