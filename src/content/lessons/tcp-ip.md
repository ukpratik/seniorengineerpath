---
topic: tcp-ip
subtitle: Learn how bytes really move between two machines, so you can read network errors and stop paying for handshakes you do not need.
status: published
reviewed_by: ai-factcheck
reviewed_on: 2026-10-22
illustrative_story: true
sources:
  - id: rfc9293
    title: "Eddy (ed.), RFC 9293: Transmission Control Protocol (TCP) (IETF, 2022)"
    url: https://www.rfc-editor.org/rfc/rfc9293
    note: The current TCP standard. Section 2.2 lists what TCP gives you; 3.5 and 3.6 show the handshake and the close; 3.8 covers retransmission, congestion control, failures and keep-alives.
  - id: rfc768
    title: "Postel, RFC 768: User Datagram Protocol (IETF, 1980)"
    url: https://www.rfc-editor.org/rfc/rfc768
    note: All of UDP on three pages. The header has only two 16-bit ports, a length and a checksum. Delivery and duplicate protection are not guaranteed.
  - id: rfc791
    title: "Postel (ed.), RFC 791: Internet Protocol (IETF, 1981)"
    url: https://www.rfc-editor.org/rfc/rfc791
    note: IPv4. Section 1.2 says what IP does not do; section 3.1 shows the header with 32-bit addresses and the time-to-live field.
  - id: rfc8200
    title: "Deering and Hinden, RFC 8200: Internet Protocol, Version 6 (IPv6) Specification (IETF, 2017)"
    url: https://www.rfc-editor.org/rfc/rfc8200
    note: IPv6, which grows addresses from 32 to 128 bits.
  - id: rfc1122
    title: "Braden (ed.), RFC 1122: Requirements for Internet Hosts – Communication Layers (IETF, 1989), section 1.1.3"
    url: https://www.rfc-editor.org/rfc/rfc1122
    note: The four layers of the internet protocol suite, each described in a paragraph.
  - id: rfc1918
    title: "Rekhter et al., RFC 1918: Address Allocation for Private Internets (IETF, 1996)"
    url: https://www.rfc-editor.org/rfc/rfc1918
    note: The three private address blocks, and why they are not routed on the public internet.
  - id: rfc3022
    title: "Srisuresh and Egevang, RFC 3022: Traditional IP Network Address Translator (Traditional NAT) (IETF, 2001)"
    url: https://www.rfc-editor.org/rfc/rfc3022
    note: How NAT lets many private machines share one public address, by also translating ports.
  - id: rfc9000
    title: "Iyengar and Thomson (eds.), RFC 9000: QUIC: A UDP-Based Multiplexed and Secure Transport (IETF, 2021)"
    url: https://www.rfc-editor.org/rfc/rfc9000
    note: QUIC streams, the built-in TLS handshake, and why it runs over UDP (section 1). Section 13 explains how it avoids head-of-line blocking across streams.
  - id: rfc9114
    title: "Bishop (ed.), RFC 9114: HTTP/3 (IETF, 2022), section 1"
    url: https://www.rfc-editor.org/rfc/rfc9114
    note: A short history of HTTP/1.1 and HTTP/2 over TCP, and why HTTP/3 moved to QUIC.
  - id: mdn-conn
    title: "MDN Web Docs, “Connection management in HTTP/1.x”"
    url: https://developer.mozilla.org/en-US/docs/Web/HTTP/Guides/Connection_management_in_HTTP_1.x
    note: Short-lived and persistent connections, pipelining, and why browsers open about six connections per domain.
  - id: aws-nat
    title: "AWS, Amazon VPC User Guide, “Troubleshoot NAT gateways”"
    url: https://docs.aws.amazon.com/vpc/latest/userguide/nat-gateway-troubleshooting.html
    note: Section “Internet connection drops after 350 seconds”. A connection idle for 350 seconds or more times out; the gateway then returns an RST, not a FIN. The fix is more traffic or TCP keepalive under 350 seconds.
  - id: man-ping
    title: "Linux man-pages project, ping(8)"
    url: https://man7.org/linux/man-pages/man8/ping.8.html
    note: How ping uses ICMP echo requests, and its options.
  - id: man-traceroute
    title: "Linux man-pages project, traceroute(8)"
    url: https://man7.org/linux/man-pages/man8/traceroute.8.html
    note: How traceroute uses the IP time-to-live field to find each router on the path.
  - id: man-tcpdump
    title: "The Tcpdump Group, tcpdump(1) manual page"
    url: https://www.tcpdump.org/manpages/tcpdump.1.html
    note: Capturing packets on an interface, with filters, and saving them to a file.
  - id: man-curl
    title: "curl project, curl man page (option -v, --verbose)"
    url: https://curl.se/docs/manpage.html
    note: What each line of verbose output means.
claims_checked:
  - claim: "An AWS NAT gateway times out a connection idle for 350 seconds or more. It then returns an RST (not a FIN) to anything behind it that keeps using the connection. TCP keepalive under 350 seconds prevents it."
    source: aws-nat
    note: "Section 'Internet connection drops after 350 seconds'. Scope is the AWS NAT gateway only. The second fix in the story (closing idle pooled connections sooner) is the lesson's own advice, not AWS's."
  - claim: "A SYN that matches no existing connection is rejected with a reset (RST). The user is told 'connection reset' when an RST arrives. A SYN is retried for at least 3 minutes. Keep-alives must default to off, with an interval of at least two hours."
    source: rfc9293
    note: "Sections 3.5.2, 3.10.7, 3.8.3 (MUST-23) and 3.8.4 (MUST-25, MUST-28). The draft said TCP 'keeps resending a SYN for at least 3 minutes'; the RFC allows the application to give up sooner, so it now says what the standard requires. The 'crashed server process' cause of RST was unsourced; replaced with a server that lost the connection."
  - claim: "Over HTTP/2 on TCP, a lost or reordered packet stalls all active transactions. In QUIC, a lost packet blocks only the streams with data in it."
    source: rfc9114
    note: "RFC 9114 section 1.1 for HTTP/2; RFC 9000 section 13 for QUIC (the draft pointed to section 2.2, which does not say this). RFC 9000 section 1: QUIC uses UDP 'to better facilitate deployment' and integrates the TLS handshake."
  - claim: "The private blocks are 10.0.0.0/8, 172.16.0.0/12 and 192.168.0.0/16. NAPT maps many private addresses and ports to one public address and its ports."
    source: rfc1918
    note: "RFC 1918 section 3 and RFC 3022 abstract and section 2.2. RFC 3022 does not mention idle timeouts, so 'NAT forgets idle ones' is now tied to the AWS doc only."
  - claim: "IP has no reliability, flow control or sequencing. IP datagrams may arrive damaged, duplicated, out of order, or not at all."
    source: rfc791
    note: "RFC 791 section 1.2 and RFC 1122 section 1.1.3. RFC 768 names the Internet Name Server (IEN 116), not DNS, as a UDP user, so the DNS example and its citation were removed from the table."
quiz:
  - q: Your service calls `10.0.3.8:8080` and gets "connection refused" at once. What is the most likely cause?
    options:
      - The network dropped every packet on the way.
      - The machine answered, but no program is listening on port 8080.
      - The server is up but slow, and took too long to answer.
    answer: 1
    why: A refusal is a fast reset (RST) from the machine. TCP answers a SYN that matches no connection with an RST. Dropped packets give a timeout instead.
  - q: Before an HTTP request can go out on a brand-new TCP connection, what must happen first?
    options:
      - Nothing. TCP sends the request in the first packet.
      - The three-way handshake, which costs one round trip.
      - A DNS lookup for every packet.
    answer: 1
    why: SYN, SYN-ACK and ACK must finish before the request is sent. With TLS on top, there is more waiting. Reusing an open connection skips all of it.
  - q: Ten requests share one HTTP/2 connection over TCP. One packet is lost. What happens?
    options:
      - Only the request in that packet waits.
      - All ten requests stall until the lost packet is sent again.
      - The connection closes and all requests fail.
    answer: 1
    why: TCP delivers bytes in order and cannot see HTTP/2 streams, so every stream waits. QUIC (used by HTTP/3) blocks only the streams in the lost packet.
  - q: Your pooled connections go through an AWS NAT gateway. After a quiet night, the first calls fail with "connection reset". What is a good fix?
    options:
      - Turn on TCP keep-alive with an interval under 350 seconds, or close idle pooled connections sooner.
      - Open a new connection for every request forever.
      - Raise the request timeout to 5 minutes.
    answer: 0
    why: The NAT gateway forgets connections idle for 350 seconds and answers later packets with RST. Keep-alives or a shorter pool idle time stop you from using a forgotten connection.
  - q: Which of these does TCP give you, but plain UDP does not?
    options:
      - Port numbers.
      - Ordered delivery and sending lost data again.
      - A checksum.
    answer: 1
    why: UDP also has ports and a checksum, but it does not promise delivery or order. TCP adds connections, ordering, retransmission, flow control and congestion control.
incidents: []
related: [http-versions, tls, dns-discovery, load-balancing, connection-pooling, latency-estimation]
glossary_optout: [ttl]
---

## What broke

Meera's team runs an order service on AWS. Its servers sit in a private subnet. To call a partner's payment API on the internet, every request goes through a NAT gateway. The HTTP client keeps a pool of open connections, so most calls skip the handshake.

Every morning, the first few payment calls fail with `connection reset by peer`. A retry works. During the day, all is fine. The team blames the partner, and the partner sees nothing in its logs.

An engineer leaves `tcpdump` running on one server overnight. In the morning, the failed requests went out on connections that had been silent for over six minutes. Each one got an RST straight back. AWS documents that a NAT gateway times out a connection that is idle for 350 seconds or more. If you then send on it, the gateway answers with a reset (RST), not a clean close (FIN) [@aws-nat].

The partner never saw those calls because they never left Meera's network. The fix was small. They turned on TCP keep-alive with an interval under 350 seconds, as AWS suggests [@aws-nat]. They also made the pool close connections that sit idle for long.

This story is made up, but the timeout and the RST come from the AWS docs. The lesson: you cannot debug this without knowing what a connection really is.

## The idea in simple words

The internet is built in layers. Each layer does one job and uses the layer below [@rfc1122]:

- **Application**: your protocol, like HTTP or DNS.
- **Transport**: TCP or UDP. It moves data between two programs.
- **Internet**: IP. It moves single packets from one machine to another, through many routers.
- **Link**: the local network, like Ethernet or Wi-Fi, to the next device.

An **IP address** finds the machine. IPv4 addresses are 32 bits, like `10.0.4.17` [@rfc791]. IPv6 addresses are 128 bits [@rfc8200]. A **port number** finds the program on that machine. TCP uses ports to pick a service [@rfc9293]. In UDP, each port is a 16-bit field [@rfc768].

A **packet** is a small chunk of data with a header. The header says where it comes from and where it goes. IP only tries to deliver packets. It has no rules for reliability, order or flow control [@rfc791]. Packets can arrive damaged, twice, out of order, or not at all [@rfc1122].

So the transport layer decides how much to fix:

- **UDP** adds almost nothing: ports, a length and a checksum. It does not promise delivery or protect against duplicates [@rfc768].
- **TCP** gives you a reliable, in-order stream of bytes over a connection [@rfc9293].

> [!key] **The key idea.** IP can lose, repeat and reorder packets. TCP hides this from you, but the hiding costs time: a handshake to start, and waiting when a packet is lost. Most "network problems" in a backend are about those costs.

## How it works

Step through the life of one TCP connection. Use Next and Back, or the ← and → keys.

[[diagram:tcp-connection-life]]

### What TCP adds on top of IP

- **Connections.** Both sides agree to talk with the **three-way handshake**: SYN, SYN-ACK, ACK [@rfc9293].
- **Ordering and retransmission.** Every byte has a sequence number. TCP finds lost data through these numbers and errors through checksums, then sends the data again [@rfc9293].
- **Flow control.** Each packet TCP sends carries a window: how many more bytes the receiver will accept right now [@rfc9293]. A slow reader makes a fast sender wait.
- **Congestion control.** TCP must slow down when the network looks overloaded, so the network does not collapse [@rfc9293].

### Why a new connection costs a round trip

Your app cannot send the request until the handshake ends. That is one full round trip with no useful data. If a round trip to the server takes 150 ms, you wait about 150 ms before the request even leaves. [TLS](/learn/tls-and-secure-connections) (coming soon) then runs its own handshake on top. See [Latency numbers](/learn/latency-numbers-and-estimation) for typical round-trip times.

This is why **connection reuse** matters. MDN explains it for HTTP/1.x. A short-lived connection does a TCP handshake before each request. A **persistent** (keep-alive) connection stays open and serves many requests, with no new handshake [@mdn-conn]. Idle connections are still closed after some time [@mdn-conn]. A **pool** keeps a few open connections ready for many callers.

### Head-of-line blocking

TCP gives bytes to your app only in order [@rfc9293]. If one packet is lost, every byte after it waits.

HTTP/2 sends many requests over one TCP connection. But TCP cannot see those requests. So one lost or reordered packet stalls all of them, even ones the loss did not touch [@rfc9114]. This is **head-of-line blocking**.

HTTP/3 fixes this by moving to **QUIC**. QUIC runs over UDP and builds its own reliable streams. A lost packet blocks only the streams with data in it [@rfc9000]. QUIC also includes the TLS handshake inside its own handshake [@rfc9000]. It uses UDP to make it easier to deploy on existing networks [@rfc9000]. [HTTP/1.1, HTTP/2 and HTTP/3](/learn/http-1-2-3-and-quic) (coming soon) goes deeper.

### Reading the three common errors

- **Connection refused.** Your SYN reached the machine, but nothing listens on that port. TCP answers a SYN that matches no connection with a reset (RST) [@rfc9293]. It is fast. Check the port, and check that the process is running.
- **Connection reset.** The connection was open, then the other side, or a device in the middle, sent RST. TCP tells your app "connection reset" [@rfc9293]. A NAT gateway after an idle timeout is one cause [@aws-nat]. A server that restarted and lost the connection is another. TCP answers packets for a connection it does not know with RST [@rfc9293].
- **Timeout.** No answer in time. Packets were dropped, a firewall ignores you, the host is down, or the server is too slow to reply. The TCP standard says a SYN must be retried for at least 3 minutes [@rfc9293]. So always set your own connect and read [timeouts](/learn/idempotency-retries-timeouts).

### Private addresses and NAT

Some addresses are private: `10.0.0.0/8`, `172.16.0.0/12` and `192.168.0.0/16`. Anyone can use them inside their own network, and they are not routed on the public internet [@rfc1918]. To reach the internet, these machines go through **NAT**. A NAT router swaps the private address and port for its own public address and port. Many machines then share one public address [@rfc3022]. The NAT keeps state for each connection [@rfc3022]. An AWS NAT gateway drops that state after 350 idle seconds [@aws-nat]. That is how Meera's resets happened.

### Tools to see the network

- `ping host`: sends ICMP echo requests and shows round-trip times and packet loss [@man-ping]. If a host does not allow ICMP, you get no reply, even when the host is up [@aws-nat].
- `traceroute host`: uses the IP time-to-live field to find each router on the path [@man-traceroute].
- `curl -v https://host/path`: shows the headers sent and received, plus extra lines about what curl does [@man-curl].
- `tcpdump -i any port 443`: prints the packets that match a filter. The interface `any` means all interfaces on Linux and recent macOS [@man-tcpdump]. Add `-w file` to save the raw packets and read them later [@man-tcpdump].

## Trade-offs

| Option | Reliable and ordered? | Setup cost | Use it when |
|---|---|---|---|
| UDP | No. Messages can be lost or repeated. [@rfc768] IP can reorder them, and UDP does not fix it. [@rfc1122] | None. No connection. | Small messages where late data is useless, like game state or live voice. |
| TCP | Yes. One ordered byte stream. [@rfc9293] | One round trip, plus TLS. | Almost all service-to-service calls and databases. |
| QUIC | Yes, per stream. Loss blocks only its streams. [@rfc9000] | TLS is part of its handshake. [@rfc9000] | HTTP/3 to users on lossy mobile networks. |
| New connection per request | Yes. | A handshake on every request. [@mdn-conn] | Rare calls, or scripts. |
| Keep-alive connection | Yes. | Once, then reused. [@mdn-conn] | Repeated calls to the same host. |
| Connection pool | Yes. | Once per connection. Idle ones can die in the middle. [@aws-nat] | Busy services. Set the pool idle time below every timeout on the path. |

**Simple rule:** use TCP, reuse connections, and set your own timeouts. Reach for UDP or QUIC only when you can name the problem they solve.

## At work: how a senior uses this

- When a call fails, ask first: **"Refused, reset or timeout?"** Each points to a different layer and a different owner.
- List every box between client and server: load balancer, proxy, NAT, firewall. Ask: **"What is the idle timeout here?"** Keep your pool idle time and keep-alive interval below the smallest one.
- Remember that the TCP standard says keep-alives must be off by default, with a default interval of at least two hours [@rfc9293]. That is far longer than the 350 seconds of an AWS NAT gateway [@aws-nat]. Set the interval yourself.
- Count new connections per request in a trace. A new TLS connection to a far region on every call is a common, hidden cost.
- Set a connect timeout and a read timeout on every client. Retry only [idempotent](/glossary#idempotent) calls, with backoff.
- When two teams blame each other, capture packets on both sides with `tcpdump`. Packets end the argument.
- See [Load balancing](/learn/load-balancing) for layer 4 and layer 7 balancers, and [DNS and service discovery](/learn/dns-and-service-discovery) (coming soon) for how a name becomes an address.

## With AI

### What to ask the agent

> "Here is our HTTP client setup in Go. List the connect timeout, read timeout, pool size, pool idle timeout and TCP keep-alive interval. Flag any value that is missing or longer than 350 seconds."

> "Here is a `curl -v` output and a `tcpdump` capture of one failed call. Explain step by step who sent the RST and when."

> "Our service opens a new connection for each call to the billing API. Show how to reuse connections in this code, and what can go wrong."

### What to check yourself

- The agent names the real setting for your library and version. Check each default in the library's docs. Do not assume there is a timeout.
- Pool idle time is shorter than every idle timeout on the path: NAT, load balancer and server.
- Retries are added only for idempotent requests. A retried payment can charge twice.
- Any claim like "HTTP/3 is always faster" is tested on your own traffic before you act on it.
- In a packet capture, find who really sent the RST. A NAT gateway can send one too, not only the server [@aws-nat].

## Check yourself

## Go deeper

- [TLS and secure connections](/learn/tls-and-secure-connections) (coming soon): the handshake that runs on top of TCP.
- [HTTP/1.1, HTTP/2 and HTTP/3](/learn/http-1-2-3-and-quic) (coming soon): multiplexing, and why QUIC exists.
- [WebSockets, SSE and long polling](/learn/websockets-sse-long-polling): keeping one connection open for a long time.
- [CDNs and HTTP caching](/learn/cdn-and-http-caching): moving content closer, so round trips get shorter.
