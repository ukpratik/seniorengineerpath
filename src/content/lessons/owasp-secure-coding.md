---
topic: owasp-secure-coding
subtitle: The few web security bugs that cause most damage, and the plain coding habits that stop them.
status: published
reviewed_by: ai-factcheck
reviewed_on: 2026-10-21
illustrative_story: false
sources:
  - id: gao-equifax
    title: "U.S. Government Accountability Office, GAO-18-559, Data Protection: Actions Taken by Equifax and Federal Agencies in Response to the 2017 Breach (August 2018)"
    url: https://www.gao.gov/assets/gao-18-559.pdf
    note: Pages 11 to 16 explain how attackers got in through Apache Struts, what helped them, and how the breach was found.
  - id: owasp-top10
    title: "OWASP Top 10:2025, home page"
    url: https://top10.owasp.org/2025/
    note: The ten categories of the current edition, with links to each one.
  - id: owasp-intro
    title: "OWASP Top 10:2025, Introduction"
    url: https://top10.owasp.org/2025/0x00_2025-Introduction/
    note: What changed since 2021, how the categories were chosen, and how much data was used.
  - id: owasp-a01
    title: "OWASP Top 10:2025, A01 Broken Access Control"
    url: https://top10.owasp.org/2025/A01_2025-Broken_Access_Control/
    note: Examples like changing an ID in the URL, and the "deny by default" prevention list.
  - id: owasp-a02
    title: "OWASP Top 10:2025, A02 Security Misconfiguration"
    url: https://top10.owasp.org/2025/A02_2025-Security_Misconfiguration/
    note: Default passwords, stack traces shown to users, open cloud storage, and how to harden.
  - id: owasp-a03
    title: "OWASP Top 10:2025, A03 Software Supply Chain Failures"
    url: https://top10.owasp.org/2025/A03_2025-Software_Supply_Chain_Failures/
    note: Real supply chain attacks and a list of defenses, from an SBOM to signed packages.
  - id: owasp-a05
    title: "OWASP Top 10:2025, A05 Injection"
    url: https://top10.owasp.org/2025/A05_2025-Injection/
    note: The classic ' OR '1'='1 example and why a safe API beats escaping.
  - id: owasp-a09
    title: "OWASP Top 10:2025, A09 Security Logging and Alerting Failures"
    url: https://top10.owasp.org/2025/A09_2025-Security_Logging_and_Alerting_Failures/
    note: What to log, what never to log, and why alerts matter.
  - id: owasp-sqli
    title: "OWASP Cheat Sheet Series, SQL Injection Prevention Cheat Sheet"
    url: https://cheatsheetseries.owasp.org/cheatsheets/SQL_Injection_Prevention_Cheat_Sheet.html
    note: Prepared statements first, allow-lists for table and column names, and no admin rights for app accounts.
  - id: owasp-ssrf
    title: "OWASP Cheat Sheet Series, Server Side Request Forgery Prevention Cheat Sheet"
    url: https://cheatsheetseries.owasp.org/cheatsheets/Server_Side_Request_Forgery_Prevention_Cheat_Sheet.html
    note: Allow-lists, no redirects, blocking private addresses and cloud metadata endpoints.
  - id: owasp-input
    title: "OWASP Cheat Sheet Series, Input Validation Cheat Sheet"
    url: https://cheatsheetseries.owasp.org/cheatsheets/Input_Validation_Cheat_Sheet.html
    note: Allow-lists over deny-lists, server-side checks, and why validation alone does not stop injection.
  - id: owasp-xss
    title: "OWASP Cheat Sheet Series, Cross Site Scripting Prevention Cheat Sheet"
    url: https://cheatsheetseries.owasp.org/cheatsheets/Cross_Site_Scripting_Prevention_Cheat_Sheet.html
    note: Output encoding for each context, and the risky escape hatches in React and Angular.
  - id: owasp-secrets
    title: "OWASP Cheat Sheet Series, Secrets Management Cheat Sheet"
    url: https://cheatsheetseries.owasp.org/cheatsheets/Secrets_Management_Cheat_Sheet.html
    note: Secrets managers, rotation, detection before commit, and what to do when a secret leaks.
  - id: dependabot
    title: "GitHub Docs, About Dependabot alerts"
    url: https://docs.github.com/en/code-security/dependabot/dependabot-alerts/about-dependabot-alerts
    note: How a dependency scanner compares your packages with a database of known flaws.
claims_checked:
  - claim: "Attackers scanned Equifax on 10 March 2017, 2 days after US-CERT's Struts warning; the notice went to an out-of-date list, and a scan a week later missed the flaw."
    source: gao-equifax
    note: GAO-18-559 pages 11 and 15.
  - claim: "From 13 May 2017 attackers used unencrypted credentials to reach 48 more databases and ran about 9,000 queries; the attack lasted about 76 days and reached at least 145.5 million people."
    source: gao-equifax
    note: GAO-18-559 pages 2, 11, 12 and 16.
  - claim: "The traffic-inspection device's certificate had expired about 10 months before the breach; it was replaced and the intrusion seen on 29 July 2017."
    source: gao-equifax
    note: GAO-18-559 pages 13 to 15.
  - claim: "OWASP Top 10:2025 is the current edition (top10.owasp.org redirects to it); A01 to A10 names match; SSRF rolled into A01; A03 expands A06:2021."
    source: owasp-intro
    note: Home page says "2025 Release", no release-candidate label. Data from over 2.8 million applications; 8 categories from data, 2 from the survey.
  - claim: "Parameterized queries mean the database always distinguishes code and data; escaping is strongly discouraged; table and column names need an allow-list mapping."
    source: owasp-sqli
    note: Quote checked word for word. The cheat sheet calls escaping "fragile", not "error-prone"; fixed.
  - claim: "Input validation does not replace parameterized queries or output encoding."
    source: owasp-input
    note: The current cheat sheet no longer says "not the primary defense"; quiz why and table reworded to its C3 wording.
  - claim: "SSRF: allow-lists preferred, deny-list is a last resort, block 169.254.169.254, DNS rebinding needs connecting to the validated IP, move to IMDSv2."
    source: owasp-ssrf
    note: All checked against the cheat sheet.
  - claim: "Secret leak response order is revoke, rotate, then delete from code and logs."
    source: owasp-secrets
    note: Section 9.2.
quiz:
  - q: "Your code builds SQL like `\"SELECT * FROM users WHERE email = '\" + email + \"'\"`. What is the best fix?"
    options:
      - Remove quote characters from the email before building the string.
      - Use a parameterized query, so the email is sent as a value, apart from the SQL.
      - Check the email format in the browser before it is sent.
    answer: 1
    why: OWASP's first defense is prepared statements with parameterized queries. The database then always tells code and data apart. OWASP calls escaping fragile, and browser checks can be skipped.
  - q: "An endpoint downloads an image from any URL the user gives. What is the main risk?"
    options:
      - Server-side request forgery. The server can be made to call internal addresses, like a cloud metadata endpoint.
      - Cross-site scripting in the user's browser.
      - The image might be too large.
    answer: 0
    why: SSRF abuses your server to reach the internal network or the machine itself. OWASP lists blocking metadata endpoints such as 169.254.169.254 among the defenses.
  - q: "In the 2017 Equifax breach, why did the patch for the Apache Struts flaw not reach the dispute portal?"
    options:
      - The patch did not exist yet.
      - The warning went to an out-of-date list, and a later scan missed the flaw.
      - The portal was written in a different language.
    answer: 1
    why: GAO reports that the recipient list was out of date, so the people who should patch never got the notice. A scan a week later did not find the flaw.
  - q: "Which statement about input validation is true?"
    options:
      - It is the main defense against SQL injection and XSS.
      - A deny-list of bad strings is safer than an allow-list.
      - It is useful, but you still need parameterized queries and output encoding.
    answer: 2
    why: OWASP separates validation from the defenses you need when you use the data. Use allow-lists on the server, plus parameterized queries for SQL and output encoding for HTML.
  - q: "A teammate commits an API key to a public repo, then deletes it in the next commit. What should happen first?"
    options:
      - Nothing. The key is gone now.
      - Revoke and rotate the key, then remove it from the code and logs.
      - Make the repo private.
    answer: 1
    why: OWASP's leak steps are revoke, rotate, then delete from the exposed system. The old commit still holds the key.
incidents: []
cases: []
related: [authn-authz, secrets-management, supply-chain-security, threat-modelling, rest-api]
---

## What broke

On 8 March 2017, a US government security team publicly warned about a flaw in Apache Struts. Struts is a widely used, open-source framework for web apps. The flaw let an attacker run commands on the server. [@gao-equifax]

Two days later, someone scanned Equifax's systems for it. They found an online dispute portal still running the old version, broke in, and confirmed they could run commands. They took no data that time. [@gao-equifax]

Equifax did send the warning to its system administrators. But the list of people was out of date, so the people who should install the patch never got it. A scan a week later also missed the flaw. [@gao-equifax]

From 13 May 2017, attackers used the portal again. They found a store of usernames and passwords kept unencrypted. With those, they reached 48 more databases. They ran about 9,000 queries, far more than normal work needs, and nothing limited them. [@gao-equifax]

A device that inspected network traffic should have seen this. But its digital certificate had expired about 10 months before the breach, so it was not inspecting encrypted traffic. On 29 July 2017, an administrator replaced the certificate and saw the attack. It had lasted about 76 days. The attackers reached personal data of at least 145.5 million people in the US. [@gao-equifax]

One unpatched framework let them in. Plain-text passwords, databases that were not separated and blind monitoring did the rest. This lesson covers the habits that stop each of these.

## The idea in simple words

The OWASP Top 10 is "a standard awareness document" about the most critical security risks to web applications. [@owasp-top10] Treat it as a list of bug types to learn, not a full checklist.

The current edition is **OWASP Top 10:2025**, the eighth one. For it, OWASP used data from over 2.8 million applications. Eight categories come from testing data and two from a survey of practitioners. [@owasp-intro]

| # | 2025 category [@owasp-top10] |
|---|---|
| A01 | Broken Access Control |
| A02 | Security Misconfiguration |
| A03 | Software Supply Chain Failures |
| A04 | Cryptographic Failures |
| A05 | Injection |
| A06 | Insecure Design |
| A07 | Authentication Failures |
| A08 | Software or Data Integrity Failures |
| A09 | Security Logging and Alerting Failures |
| A10 | Mishandling of Exceptional Conditions |

Two changes since 2021 matter for daily work. Server-side request forgery is now part of A01. A03 grows the old "Vulnerable and Outdated Components" to cover builds and distribution too. [@owasp-intro]

> [!key] **The key idea.** Many of these bugs happen when your code trusts something it should not: user input, a library, a default setting or its own network. Check on the server, keep data apart from code, and give each part the least power it needs.

## How it works

Here are six to start with, each with one example and one fix.

### Broken access control (A01)

A user changes `/accounts/123` to `/accounts/124` and sees someone else's account. The server never checked ownership. [@owasp-a01] OWASP found some form of broken access control in 100% of the applications tested. [@owasp-a01]

**Fix:** deny by default, write the check once and reuse it everywhere, and enforce record ownership. Add access control cases to your unit and integration tests. [@owasp-a01] The full story is in [Authentication and authorization](/learn/authentication-and-authorization).

### Injection (A05)

Injection happens when untrusted input reaches an interpreter, such as a SQL database, as part of a command. [@owasp-a05] Step through the classic attack.

[[diagram:sql-injection-param]]

**Fix:** use a parameterized query. With it, "the database will always distinguish between code and data". [@owasp-sqli] Most drivers and ORMs support it.

```python
# Wrong: input becomes part of the SQL
cur.execute("SELECT * FROM accounts WHERE id = '" + acc_id + "'")
# Right: a placeholder, so the driver passes input only as a value
cur.execute("SELECT * FROM accounts WHERE id = %s", (acc_id,))
```

Placeholders cannot hold table or column names. For a "sort by" field, map the user's choice to a fixed list of allowed names. [@owasp-sqli] Also, never give your app's database account admin rights. [@owasp-sqli]

### Security misconfiguration (A02)

Examples: an admin console with its default password, sample apps left on a production server, or stack traces shown to users. [@owasp-a02] At Equifax, an expired certificate left a traffic monitor misconfigured, so it missed the attack. [@gao-equifax]

**Fix:** build each environment from a script, with the same hardened settings everywhere and different credentials in each. Remove features and samples you do not use. Add one central setting that stops detailed error messages from reaching users. [@owasp-a02]

### Software supply chain failures (A03)

Your app is mostly other people's code. You are exposed if you do not know every version you run, including dependencies of dependencies, or you do not scan them regularly. [@owasp-a03] OWASP lists a 2017 Apache Struts 2 flaw, CVE-2017-5638, and Log4Shell among its example attacks. [@owasp-a03] A CVE is the public ID of one known flaw.

**Fix:** keep a **software bill of materials** (SBOM), remove unused packages, and watch for new flaws in what you use all the time. [@owasp-a03] A tool like Dependabot compares your dependency list with a database of known flaws. It alerts you when a new flaw is published for a version you use. [@dependabot]

### Server-side request forgery (part of A01)

Your app has a feature "import an image from a URL". An attacker gives `http://169.254.169.254/`, a cloud metadata address. Your server, which sits inside your network, fetches it. SSRF often aims at such endpoints to steal cloud credentials. [@owasp-ssrf]

**Fix:** if you know the target hosts, use an allow-list. Do not accept full URLs from users, and turn off redirects. If you must call any address, block private, loopback and link-local ranges and metadata endpoints. Check the address the name resolves to, and connect only to that address. [@owasp-ssrf] On AWS, move to IMDSv2. [@owasp-ssrf]

### Logging and alerting failures (A09)

"Without logging and monitoring, attacks and breaches cannot be detected." [@owasp-a09] A common gap is logging only successful logins, so failed attempts go unseen. [@owasp-a09]

**Fix:** log security events, including failures, with enough user context. Encode log data so attackers cannot inject fake lines. Keep personal data and secrets out of logs, and set alerts that people act on. [@owasp-a09]

### Input validation versus output encoding

These two are often mixed up.

- **Input validation** checks data when it arrives. Use allow-lists ("this field is 1 to 64 letters"), not lists of bad strings. Always validate on the server, because browser checks can be skipped. [@owasp-input]
- **Output encoding** changes data when you display it, so the browser shows it as text and does not run it. HTML, JavaScript, URLs and CSS each need their own encoder. [@owasp-xss]

Validation alone does not stop SQL injection or cross-site scripting. You still need parameterized queries and output encoding. [@owasp-input] Modern frameworks help by escaping output automatically. Escape hatches like React's `dangerouslySetInnerHTML` skip that protection. [@owasp-xss]

### Secrets out of code, and least privilege

Never put passwords or API keys in code, config files or Docker `ENV` lines. Load them at run time from a secrets manager, like AWS Secrets Manager or HashiCorp Vault. [@owasp-secrets] Run a secret scanner in a pre-commit hook. If a secret leaks: revoke it, rotate it, then delete it from code and logs. [@owasp-secrets]

Give each service the smallest access it needs. Equifax's attackers jumped to 48 more databases because they were not separated, and plain-text credentials let them in. [@gao-equifax]

## Trade-offs

| Defense | Stops | Does not stop | Cost |
|---|---|---|---|
| Parameterized queries | Yes. SQL injection through values. [@owasp-sqli] | Bad table or column names. Use an allow-list. [@owasp-sqli] | Low. Most libraries support it. |
| Escaping input by hand | Some injection. | No. OWASP calls it fragile and strongly discourages it. [@owasp-sqli] | Low to write, high to get right. |
| Allow-list input validation | Bad formats, wrong values. [@owasp-input] | No. It does not replace parameterized queries or output encoding. [@owasp-input] | Low. |
| Output encoding | Most XSS when you show user data. [@owasp-xss] | Not every case. "Output encoding is not perfect." [@owasp-xss] | Low with a framework. |
| SSRF allow-list | Calls to any host you did not list. [@owasp-ssrf] | Features that must call any URL. | Medium. You keep the list. |
| SSRF block-list | Known private ranges and metadata. [@owasp-ssrf] | Tricks like DNS rebinding, unless you connect to the address you checked. [@owasp-ssrf] | Medium. A last resort. |
| Dependency scanning | Known flaws in versions you use. [@dependabot] | Flaws not yet in the advisory database. Alerts cannot catch every issue. [@dependabot] | Low to start. Ongoing work to update. |

**Simple rule:** prefer the defense that makes the bug impossible, like a parameterized query or an allow-list. Use filters and block-lists only as extra layers.

## At work: how a senior uses this

- In review, ask of every new endpoint: **"Who may call this, and where do we check it?"**
- Search the diff for string-built SQL, shell commands and HTML. Each one needs a parameter, an argument list or an encoder.
- For any feature that fetches a URL, ask: **"Which hosts may it reach?"** Write the answer as an allow-list.
- Keep dependency alerts at zero open critical items. Agree on a patch deadline with your team, and track it like a bug.
- Give each service its own database user with only the tables it needs.
- Check that failed logins and access denials are logged and alerted. Ask: **"Would we notice this attack within a day?"**
- Treat certificates, keys and secrets as things that expire. Put their dates in monitoring.

## With AI

### What to ask the agent

> "Find every place in this repo that builds SQL, shell commands or HTML with string concatenation or f-strings. For each, show the safe version."

> "List every endpoint that fetches a URL given by the user. For each, say which hosts it can reach, and whether it follows redirects."

> "Scan the repo and its history for hard-coded secrets. List each one with file and commit, but do not print the secret."

### What to check yourself

- Every query uses placeholders. Table and column names come from a fixed list.
- An AI agent may add packages to fix a problem. Check that each new package is real, maintained and pinned, and that it shows up in your scanner.
- No secret appears in code, test fixtures, prompts or logs that the agent wrote.
- New handlers have the ownership check, not only the login check.
- Read the agent's SSRF fix. A check on the host name alone is not enough. It must check the resolved address and connect to that same address.

## Check yourself

## Go deeper

- [Authentication and authorization](/learn/authentication-and-authorization): A01 and A07 in depth.
- Coming next on this line: [Secrets and key management](/learn/secrets-and-key-management), [Dependency and supply-chain security](/learn/dependency-supply-chain-security) and [Threat modelling and zero trust](/learn/threat-modelling-zero-trust).
