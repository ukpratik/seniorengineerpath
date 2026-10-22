---
topic: tls
subtitle: Learn what TLS protects, how a client decides to trust a server, and how to stop certificate expiry from taking your service down.
status: published
reviewed_by: ai-factcheck
reviewed_on: 2026-10-22
illustrative_story: false
sources:
  - id: lucidworks
    title: "Lucidworks status page, “SaaS Platform User Interface and Signals Beacon Disruption” (incident report, July 2026)"
    url: https://status.lucidworks.com/incidents/vxmrbxrlzbcl
    note: A shared certificate expired. The 30-day warning was missed, and later alerts never reached the incident management system.
  - id: improvmx
    title: "ImprovMX, “Post Mortem: Expired SMTP TLS Certificate”, May 2026"
    url: https://improvmx.com/blog/2026-05-15-postmortem-smtp-tls-cert-expiry
    note: Automatic renewal failed silently after a credential change, an outside monitor gave no warning, and SMTP sending failed for about six hours.
  - id: rfc9846
    title: "Rescorla, RFC 9846: The Transport Layer Security (TLS) Protocol Version 1.3 (IETF, July 2026)"
    url: https://www.rfc-editor.org/rfc/rfc9846
    note: The current TLS 1.3 standard. It replaces RFC 8446 and keeps the same version. Sections 1, 2 and 2.3 are the overview.
  - id: rfc5280
    title: "Cooper et al., RFC 5280: Internet X.509 Public Key Infrastructure Certificate and CRL Profile (IETF, 2008)"
    url: https://www.rfc-editor.org/rfc/rfc5280
    note: What a certificate holds, its validity dates, and how a client checks a chain (section 6.1).
  - id: rfc9110
    title: "Fielding, Nottingham and Reschke (eds.), RFC 9110: HTTP Semantics (IETF, 2022), section 4.3.4"
    url: https://www.rfc-editor.org/rfc/rfc9110
    note: An HTTPS client must check that the certificate matches the host, and what to do when it does not.
  - id: le-certs
    title: "Let's Encrypt, “Chains of Trust”"
    url: https://letsencrypt.org/certificates/
    note: Let's Encrypt's root and intermediate certificates, and how the chain is sent to clients.
  - id: rfc6066
    title: "Eastlake, RFC 6066: TLS Extensions: Extension Definitions (IETF, 2011), section 3"
    url: https://www.rfc-editor.org/rfc/rfc6066
    note: Server Name Indication, so one address can serve many sites.
  - id: rfc7301
    title: "Friedl, Popov, Langley and Stephan, RFC 7301: TLS Application-Layer Protocol Negotiation Extension (IETF, 2014)"
    url: https://www.rfc-editor.org/rfc/rfc7301
    note: How the client and server agree on an application protocol, such as HTTP/2, inside the handshake.
  - id: rfc8470
    title: "Thomson, Nottingham and Tarreau, RFC 8470: Using Early Data in HTTP (IETF, 2018)"
    url: https://www.rfc-editor.org/rfc/rfc8470
    note: The replay risk of 0-RTT for HTTP, and the 425 Too Early status code.
  - id: rfc8555
    title: "Barnes, Hoffman-Andrews, McCarney and Kasten, RFC 8555: Automatic Certificate Management Environment (ACME) (IETF, 2019)"
    url: https://www.rfc-editor.org/rfc/rfc8555
    note: The protocol that lets software prove it controls a domain and get a certificate with no human.
  - id: le-faq
    title: "Let's Encrypt, “FAQ”"
    url: https://letsencrypt.org/docs/faq/
    note: Certificate lifetimes, and when to renew.
  - id: le-45
    title: "Matthew McPherrin, “Decreasing Certificate Lifetimes to 45 Days”, Let's Encrypt blog, 2 December 2025"
    url: https://letsencrypt.org/2025/12/02/from-90-to-45
    note: The dates when Let's Encrypt certificates get shorter, and why fixed renewal schedules will break.
  - id: rfc6797
    title: "Hodges, Jackson and Barth, RFC 6797: HTTP Strict Transport Security (HSTS) (IETF, 2012)"
    url: https://www.rfc-editor.org/rfc/rfc6797
    note: The standard for telling browsers to use only HTTPS for a site.
  - id: mdn-hsts
    title: "MDN Web Docs, “Strict-Transport-Security header”"
    url: https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Headers/Strict-Transport-Security
    note: Header examples, the first-visit weakness, and the preload list.
  - id: istio-security
    title: "Istio documentation, “Security” concepts"
    url: https://istio.io/latest/docs/concepts/security/
    note: Mutual TLS between services through sidecar proxies, automatic certificate rotation, and permissive mode.
  - id: aws-alb-https
    title: "AWS, Elastic Load Balancing User Guide, “Create an HTTPS listener for your Application Load Balancer”"
    url: https://docs.aws.amazon.com/elasticloadbalancing/latest/application/create-https-listener.html
    note: How the load balancer ends TLS, and that passing encrypted traffic through needs a Network or Classic Load Balancer with a TCP listener.
  - id: aws-alb-tg
    title: "AWS, Elastic Load Balancing User Guide, “Target groups for your Application Load Balancers”"
    url: https://docs.aws.amazon.com/elasticloadbalancing/latest/application/load-balancer-target-groups.html
    note: HTTPS to your servers, and the note that the load balancer does not validate their certificates.
claims_checked:
  - claim: RFC 9846 (July 2026) is the current TLS 1.3 spec and obsoletes RFC 8446; the server is always authenticated, the client optionally; data is confidential and changes are detected.
    source: rfc9846
    note: Checked on datatracker (Proposed Standard, July 2026, obsoletes 5077, 5246, 6961, 7627, 8422, 8446) and the RFC section 1 text.
  - claim: Everything after the key exchange (ServerHello) is encrypted; CertificateVerify is a signature over the whole handshake; the full handshake is 1-RTT; the server can ask for a client certificate.
    source: rfc9846
    note: Section 2 and 2.3.
  - claim: 0-RTT data has "no guarantees of non-replay between connections".
    source: rfc9846
    note: Exact quote, section 2.3.
  - claim: Clients must not send unsafe methods in early data; a server can answer 425 Too Early when the replay risk is too great.
    source: rfc8470
    note: Sections 3, 4 and 5.2.
  - claim: Each certificate in the path is issued by the next, the path starts at a trust anchor, and the current time must be inside each validity period.
    source: rfc5280
    note: Sections 4.1.2.5 and 6.1.
  - claim: The client must check the certificate matches the host; on failure an automated client must log the error and should end the connection.
    source: rfc9110
    note: Section 4.3.4. Ending the connection is a SHOULD, so the text says "should".
  - claim: SNI puts the host name in the ClientHello so one address can host many sites; ALPN lists protocols in the ClientHello.
    source: rfc6066
    note: RFC 6066 section 3; ALPN checked against RFC 7301. "h2" is not defined in RFC 7301, so the text now says HTTP/2.
  - claim: Webmasters often need 1-3 hours to get and install a certificate by hand; ACME lets software get certificates with no user interaction.
    source: rfc8555
    note: Introduction (based on the authors' informal tests).
  - claim: Let's Encrypt certificates are 90 days by default, renewed every 60 days.
    source: le-faq
    note: FAQ, read 2026-10-22.
  - claim: Default becomes 64 days on 10 February 2027 and 45 days on 16 February 2028; a hardcoded 60-day renewal will no longer be enough.
    source: le-45
    note: Matches the post. Opt-in 45-day tlsserver profile from 13 May 2026, so "changes from 2026 to 2028" holds.
  - claim: Lucidworks shared SAN certificate expired 27 July 2026 17:56 UTC; 30-day alert on 27 June went unanswered; alerts stopped reaching incident management from 1 July; Beacon signals 17:56-19:01 UTC lost; new provider needed DNS validation; resolved 19:24 UTC.
    source: lucidworks
    note: Matches the postmortem. Fixed "renewed" to "made a new certificate", since they changed providers.
  - claim: ImprovMX renewals failed silently after a credential change, a third-party monitor gave no warning, and SMTP sending failed for about 6 hours (14-15 May 2026).
    source: improvmx
    note: Postmortem gives 23:08 to 05:15, about 6 h 7 min. Remediation is first-party expiry checks and alerts on renewal failure.
  - claim: HSTS makes the browser rewrite http to https before sending, blocks click-through on certificate errors, and leaves the first visit exposed unless preloaded.
    source: rfc6797
    note: RFC 6797 sections 8.3, 8.4 and 12.1; MDN for first-visit weakness and preload.
  - claim: Istio client proxy starts the mTLS handshake, rotates keys and certificates automatically, and permissive mode accepts plain text and mTLS.
    source: istio-security
    note: Security concepts page.
  - claim: An ALB HTTPS listener decrypts before forwarding; ALB does not validate target certificates; passthrough needs a Network or Classic Load Balancer with a TCP listener.
    source: aws-alb-https
    note: Text said passthrough on the ALB; fixed, since AWS names NLB or CLB. Target-certificate note is from aws-alb-tg.
quiz:
  - q: What does TLS give you on a connection?
    options:
      - Encryption only.
      - Encryption, protection against changes, and proof of the server's identity.
      - Proof that the user is logged in.
    answer: 1
    why: TLS 1.3 gives confidentiality, integrity, and authentication. The server is always authenticated. The client is only authenticated if the server asks.
  - q: Why is 0-RTT data risky for a "POST /payments" request?
    options:
      - It is not encrypted.
      - An attacker can capture it and send it again, and TLS does not stop replays across connections.
      - It is slower than a normal handshake.
    answer: 1
    why: RFC 9846 says there are no guarantees of non-replay between connections. RFC 8470 says clients must not send unsafe methods in early data.
  - q: Your certificate renewal runs from a cron job every 60 days. Your CA moves to 45-day certificates. What happens?
    options:
      - Nothing. Renewal still works.
      - Certificates expire before the job runs, and clients get errors.
      - The CA renews the certificates for you.
    answer: 1
    why: Let's Encrypt warns that fixed schedules like 60 days will fail. Use ACME Renewal Information, or renew about two thirds of the way through the lifetime.
  - q: Your load balancer ends TLS and sends plain HTTP to your servers. What is true?
    options:
      - Traffic is encrypted all the way to the server.
      - Traffic is encrypted only to the load balancer. Inside, it is readable unless you add TLS to the servers too.
      - TLS cannot be ended at a load balancer.
    answer: 1
    why: AWS says an HTTPS listener decrypts requests before it sends them on. For encryption to the server, use HTTPS to the targets, or a Network Load Balancer with a TCP listener.
  - q: A website sends the HSTS header. What happens next time a user types http:// for it?
    options:
      - The browser changes it to https:// before it sends anything.
      - The server redirects the user.
      - The browser shows a warning and lets the user choose.
    answer: 0
    why: HSTS tells browsers to turn insecure links to that host into secure ones, and not to let users click through certificate errors.
incidents: []
related: [tcp-ip, http-versions, authn-authz, secrets-management, service-mesh, load-balancing, incident-response]
---

## What broke

On 27 July 2026, at 17:56 UTC, one certificate expired at Lucidworks. It covered several names, including `platform.lucidworks.com` and `api.lucidworks.com`. Users of the platform got certificate errors and could not open it [@lucidworks].

Data was lost too. Usage signals sent to the Beacon endpoint on `api.lucidworks.com` between 17:56 and 19:01 UTC were not stored. They cannot be recovered [@lucidworks].

The warning came in time. On 27 June, a monitoring check raised an alert 30 days before expiry. It went to an internal channel, and nobody acted on it. From 1 July, alerts also stopped reaching the incident management system, and nothing watched that path [@lucidworks].

The team made and installed a new certificate by hand. This took longer because they used a new certificate provider, so they first had to prove they owned the domains in DNS. Service was fully back at 19:24 UTC [@lucidworks].

Their action items: remove the manual renewal step, and send expiry alerts to a pager that needs a person to answer [@lucidworks]. A certificate fails on a date you know in advance. Seniors plan for that date.

## The idea in simple words

When your app calls `https://api.shop.example`, the data crosses Wi-Fi, ISPs and routers you do not control. [TLS](/glossary#tls) protects that path. TLS 1.3 gives three things [@rfc9846]:

- **Confidentiality.** Only the two ends can read the data.
- **Integrity.** Nobody can change the data without being noticed.
- **Authentication.** The server always proves who it is. The client proves who it is only if the server asks.

The server proves its identity with a **certificate**. A certificate says "this public key belongs to `api.shop.example`", and is valid between two dates [@rfc5280]. A **certificate authority** (CA) signs it.

Your app does not know every CA. It knows a small set of **root** certificates in its trust store. Roots sign **intermediate** certificates, and intermediates sign site certificates [@le-certs]. This is the **chain of trust**.

> [!key] **The key idea.** TLS does two jobs: it agrees on secret keys, and it proves the server holds the private key for a certificate the client trusts. The outages in this lesson were not about cryptography. They were about a certificate that expired. A chain the client cannot follow fails the same way.

## How it works

Step through one TLS 1.3 handshake and a request. Use Next and Back, or the ← and → keys.

[[diagram:tls13-handshake]]

### The TLS 1.3 handshake in plain words

TLS runs on top of a [TCP](/learn/tcp-ip-network-stack) connection. Then:

1. **ClientHello.** The client sends the options it supports and its part of a key exchange (a "key share") [@rfc9846].
2. **ServerHello.** The server picks the options and sends its own key share. From here, both sides can compute the same keys. Every handshake message after ServerHello is encrypted [@rfc9846].
3. **Certificate and proof.** The server sends its certificate chain. It also sends a signature over the whole handshake, made with the private key that matches the certificate [@rfc9846]. A thief who copied the certificate but not the private key cannot make this signature.
4. **Finished.** The client checks everything and sends Finished. Now application data can flow [@rfc9846].

This takes one round trip, so RFC 9846 calls it the 1-RTT handshake [@rfc9846].

### How the client checks the chain

The client must check that the certificate is for the host it asked for [@rfc9110]. It then follows the chain. Each certificate must be signed by the next one up, and the top must be signed by a trust anchor, such as a root in the trust store [@rfc5280]. Each certificate must be valid now: the current time must be inside its dates [@rfc5280].

When the server sends its own certificate plus the intermediate, the client can check every signature up to a root it already trusts. It does not need to download anything else [@le-certs].

If the check fails, an automated client must log the error and should close the connection [@rfc9110].

### SNI: many sites on one address

One IP address often serves many domains. The server must know which certificate to send before it can send one. So the client puts the host name in the ClientHello. This is **Server Name Indication** (SNI) [@rfc6066]. The ClientHello also lists the application protocols the client speaks, such as HTTP/2. This is ALPN [@rfc7301].

### 0-RTT and replay

When a client has talked to a server before, TLS 1.3 lets it send data in its very first message. This is called 0-RTT, or early data [@rfc9846]. It saves a round trip, but it has weaker protection. RFC 9846 says there are no guarantees of non-replay between connections [@rfc9846]. An attacker can copy that data and send it again.

For HTTP, clients must not send unsafe methods, like POST, in early data [@rfc8470]. A server that does not want to risk a replay can answer `425 Too Early` [@rfc8470].

### Mutual TLS between services

In normal TLS, only the server shows a certificate. With **mutual TLS** (mTLS), the server also asks the client for one [@rfc9846]. Now both sides know who they talk to. This is common between internal services.

A service mesh such as Istio can do this for you. A proxy next to each service starts the mutual TLS handshake [@istio-security]. Istio gives each workload a certificate and rotates keys and certificates automatically [@istio-security]. Its permissive mode accepts both plain text and mTLS, which helps you move services over step by step [@istio-security]. Then switch to strict mode.

### Expiry and automatic renewal

Every certificate has an end date [@rfc5280]. Let's Encrypt certificates are valid for 90 days by default, and it suggests renewing them every 60 days [@le-faq]. They are getting shorter. On 10 February 2027 the default becomes 64 days, and on 16 February 2028 it becomes 45 days [@le-45]. Let's Encrypt warns that a fixed schedule, like a job every 60 days, will then fail [@le-45].

**ACME** is the protocol that makes renewal automatic. Software proves it controls the domain and gets a certificate with no human involved [@rfc8555]. Before ACME, people often needed 1–3 hours to get and install one certificate [@rfc8555].

Automation still fails. At ImprovMX, a changed credential made renewals fail silently. An outside monitor gave no warning. The SMTP certificate expired, and sending mail failed for about six hours [@improvmx]. Their fix: their own expiry checks, and alerts when a renewal fails [@improvmx].

### HSTS

A user who types `shop.example` may first reach your site over plain HTTP. An attacker can act there. The `Strict-Transport-Security` header tells the browser to use only HTTPS for your site from now on [@mdn-hsts]. The browser then turns `http://` links into `https://` before it sends anything [@rfc6797]. It also stops users from clicking through certificate errors [@mdn-hsts]. The first visit is still exposed, and the preload list fixes that [@mdn-hsts].

### Where TLS ends

TLS protects one hop. Often it ends at the [load balancer](/learn/load-balancing). An AWS Application Load Balancer uses its certificate to decrypt requests, then sends them to your servers [@aws-alb-https]. You can send HTTPS to the servers too. But the load balancer does not validate their certificates [@aws-alb-tg]. AWS says this is safe inside the VPC, but traffic that leaves AWS needs more steps [@aws-alb-tg]. To keep traffic encrypted until your server, AWS suggests a Network Load Balancer with a TCP listener, which passes TLS through [@aws-alb-https].

## Trade-offs

| Option | Encrypted until | Who holds certificates | Use it when |
|---|---|---|---|
| End TLS at the load balancer | The load balancer. [@aws-alb-https] | Only the load balancer. | Most web apps on a private network. |
| Re-encrypt to servers | The server, but its certificate is not checked by AWS. [@aws-alb-tg] | Load balancer and servers. | Rules say all traffic must be encrypted. |
| TLS passthrough (Network Load Balancer, TCP) | The server. [@aws-alb-https] | Only the servers. | The load balancer must not see the data. |
| Mutual TLS | Both ends, both proven. [@rfc9846] | Every service. Needs automatic rotation. [@istio-security] | Calls between your own services. |
| 0-RTT | Weaker. It can be replayed. [@rfc9846] | Same as normal TLS. | Only safe, repeatable requests like GET. [@rfc8470] |

**Simple rule:** end TLS at the edge for speed, add mTLS inside when services must prove who they are, and automate every certificate.

## At work: how a senior uses this

- Keep a list of every certificate: where it lives, who owns it, and when it expires. Shared certificates for many names make one expiry hit many services.
- Ask of each certificate: **"What renews it, and what tells us when renewal fails?"** Alert on the renewal, not only on the date.
- Send expiry alerts to the on-call pager, 30 days and 7 days ahead. A chat channel is not enough.
- Check certificates from outside too, with a probe that follows the full chain like a real client.
- When one client fails and another works, compare the chain each one receives and the trust store each one uses.
- Turn off 0-RTT for APIs that change data, unless every request is safe to replay.
- Never ship code that disables certificate checks, even "only in staging".

## With AI

### What to ask the agent

> "Write a script that lists every TLS certificate on these 40 hosts, with issuer, names and expiry date. Sort by expiry and flag anything under 30 days."

> "Review this nginx TLS config. Does it send the full chain? Is 0-RTT on, and is that safe for these endpoints?"

> "Our certificate renewal runs from cron every 60 days. Our CA is moving to 45-day certificates. Rewrite it to use ACME Renewal Information, and alert when renewal fails."

### What to check yourself

- The agent did not add `verify=False`, `InsecureSkipVerify` or a similar switch to make an error go away.
- Expiry dates come from the real certificate the server sends, not from a file on disk that may be old.
- Lifetimes and dates match the CA's current docs. Let's Encrypt changes its lifetimes from 2026 to 2028 [@le-45].
- The mTLS setup checks the client certificate, not only that one exists.
- HSTS is sent only over HTTPS, and `includeSubDomains` does not cover a subdomain that still needs HTTP.

## Check yourself

## Go deeper

- [TCP/IP and the network stack](/learn/tcp-ip-network-stack): the connection TLS runs on.
- [HTTP/1.1, HTTP/2 and HTTP/3](/learn/http-1-2-3-and-quic): ALPN, and TLS inside QUIC.
- [Authentication and authorization](/learn/authentication-and-authorization): who the user is, which TLS does not tell you.
- [Secrets and key management](/learn/secrets-and-key-management) (coming soon): where private keys should live.
- [Service mesh and sidecars](/learn/service-mesh-and-sidecars) (coming soon): mTLS for every service.
