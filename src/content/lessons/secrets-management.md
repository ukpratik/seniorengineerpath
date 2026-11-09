---
topic: secrets-management
subtitle: Keep secrets out of code, give them a short life, and be ready to rotate any of them in minutes.
status: published
reviewed_by: ai-factcheck
reviewed_on: 2026-11-09
illustrative_story: false
sources:
  - id: circleci
    title: "Rob Zuber, CircleCI Jan 4, 2023 security incident report (CircleCI blog, 12 January 2023)"
    url: https://circleci.com/blog/jan-4-2023-incident-report/
    note: The company's own timeline of the stolen session, the stolen secrets and the mass rotation.
  - id: owasp-secrets
    title: "OWASP Cheat Sheet Series, Secrets Management Cheat Sheet"
    url: https://cheatsheetseries.owasp.org/cheatsheets/Secrets_Management_Cheat_Sheet.html
    note: Central secrets managers, rotation, dynamic secrets, CI/CD, containers, detection and what to do after a leak.
  - id: twelve-factor
    title: "The Twelve-Factor App, III. Config"
    url: https://12factor.net/config
    note: Why config, including credentials, must be strictly separate from code.
  - id: vault-db
    title: "HashiCorp Vault documentation, Database secrets engine"
    url: https://developer.hashicorp.com/vault/docs/secrets/databases
    note: How Vault creates a unique database user for each client, with a lease that ends.
  - id: gh-oidc
    title: "GitHub Docs, OpenID Connect"
    url: https://docs.github.com/en/actions/concepts/security/openid-connect
    note: How a GitHub Actions job gets a short-lived cloud token instead of a stored key.
  - id: gh-oidc-aws
    title: "GitHub Docs, Configuring OpenID Connect in Amazon Web Services"
    url: https://docs.github.com/en/actions/how-tos/secure-your-work/security-harden-deployments/oidc-in-aws
    note: The id-token permission and the configure-aws-credentials step.
  - id: aws-rotation
    title: "AWS Secrets Manager User Guide, Lambda function rotation strategies"
    url: https://docs.aws.amazon.com/secretsmanager/latest/userguide/rotation-strategy.html
    note: Single-user and alternating-users rotation, and when each one fits.
  - id: aws-kms
    title: "AWS Key Management Service Developer Guide, AWS KMS cryptography essentials"
    url: https://docs.aws.amazon.com/kms/latest/developerguide/kms-cryptography.html
    note: Envelope encryption, root keys, and the HSMs that KMS keys never leave unencrypted.
  - id: gcp-envelope
    title: "Google Cloud Documentation, Cloud Key Management Service, Envelope encryption"
    url: https://docs.cloud.google.com/kms/docs/envelope-encryption
    note: DEKs and KEKs, the exact encrypt and decrypt steps, and rules for new DEKs.
  - id: gh-scanning
    title: "GitHub Docs, Secret scanning"
    url: https://docs.github.com/en/code-security/secret-scanning/introduction/about-secret-scanning
    note: Scanning all of Git history, the partner program, and what to do with an alert.
  - id: gh-push
    title: "GitHub Docs, Push protection"
    url: https://docs.github.com/en/code-security/secret-scanning/introduction/about-push-protection
    note: Blocking a push that contains a secret, and what happens on a bypass.
  - id: gitleaks
    title: "Gitleaks, README (github.com/gitleaks/gitleaks)"
    url: https://github.com/gitleaks/gitleaks
    note: An open-source secret scanner for Git history, folders and pre-commit hooks.
claims_checked:
  - claim: "Malware on a CircleCI engineer's laptop stole a valid, 2FA-backed SSO session; the engineer could generate production access tokens, and on 22 December 2022 customer environment variables, tokens and keys were exfiltrated."
    source: circleci
    note: CircleCI's report also says encryption keys were taken from a running process, "enabling them to potentially access the encrypted data".
  - claim: "OWASP says environment variables are generally accessible to all processes and may be included in logs or system dumps, and does not recommend them unless other methods are not possible; after a leak the order is revocation, rotation, deletion."
    source: owasp-secrets
  - claim: "Google Cloud says to store the encrypted data with the wrapped DEK and never a plaintext DEK, that the KEK never leaves Cloud KMS, that Encrypt and Decrypt accept at most 64 KiB, and to make a new DEK for every write."
    source: gcp-envelope
  - claim: "With AWS Secrets Manager alternating-users rotation, both users' credentials are valid after rotation, and AWS says it fits apps that need high availability; single-user rotation has a short window of possible denied calls, mitigated by retries."
    source: aws-rotation
  - claim: "With GitHub OIDC the cloud provider issues a short-lived access token valid for a single job, and id-token: write only lets the workflow request the token."
    source: gh-oidc-aws
  - claim: "GitHub secret scanning scans the entire Git history on all branches, is free and automatic on public repos, and removing a secret from history is often unnecessary once the credential is revoked."
    source: gh-scanning
quiz:
  - q: An engineer pushed an AWS access key to a public repo 10 minutes ago. What do you do first?
    options:
      - Rewrite Git history to remove the commit, then force-push.
      - Revoke or rotate the key, then check its use since the push.
      - Make the repo private, then plan a rotation for next week.
    answer: 1
    why: OWASP puts revocation and rotation first. GitHub says that once a credential is revoked, removing it from history is often not needed.
  - q: In envelope encryption, what do you store next to the encrypted data?
    options:
      - The plain data encryption key, so reads are fast.
      - The key encryption key from KMS.
      - The data encryption key, wrapped by the key encryption key.
    answer: 2
    why: Google Cloud's steps are to store the wrapped DEK with the data and never store a plain DEK. The KEK never leaves Cloud KMS.
  - q: Why does AWS say the alternating-users strategy fits apps that need high availability?
    options:
      - Two database users take turns, so a valid set of credentials exists during the change.
      - It rotates the password less often.
      - It removes the need for a secrets manager.
    answer: 0
    why: After rotation, both users' credentials are valid. An app that reads the secret mid-rotation still gets working credentials.
  - q: Your GitHub Actions workflow deploys to AWS. What removes the need for a stored AWS key?
    options:
      - Putting the key in a GitHub secret instead of the YAML file.
      - OpenID Connect, where the job swaps a GitHub token for a short-lived AWS token.
      - Encrypting the key with base64 before you store it.
    answer: 1
    why: With OIDC, the cloud issues a token that is valid for one job and then expires. No cloud key is stored in GitHub.
incidents: [circleci-2023-01-security-incident]
glossary_optout: [orchestration, scope, trace, lease]
related: [authn-authz, owasp-secure-coding, tls, ci-cd, coding-agents, incident-response, multi-tenancy]
---

## What broke

On 4 January 2023, CircleCI told its customers to rotate every secret they kept in it. CircleCI runs build and deploy jobs. Teams store API keys, tokens and cloud keys there, so those jobs can deploy. [@circleci]

The attack began on 16 December 2022. Malware on one engineer's laptop stole a valid single sign-on session. The session already had two-factor authentication behind it. So the attacker could act as the engineer from far away. [@circleci]

That engineer could create production access tokens. With them, the attacker reached some production databases and stores. On 22 December, they copied customer data, including environment variables, tokens and keys. [@circleci]

The data was encrypted at rest. But the attacker "extracted encryption keys from a running process", so they could potentially read it. [@circleci]

On 29 December, a customer reported odd GitHub OAuth activity. From 31 December, CircleCI rotated customers' GitHub OAuth tokens. On 4 January, it told customers to "assume they have been accessed" and rotate their secrets. The next day it revoked Project API tokens. [@circleci]

One laptop forced every customer into an emergency. The lesson for us: many secrets sat in one store, and the keys to decrypt them were in a running process.

## The idea in simple words

A **secret** is anything that gives access. OWASP lists database credentials, SSH keys, certificates and API keys. [@owasp-secrets] OAuth tokens, signing keys and [TLS](/learn/tls-and-secure-connections) private keys count too.

Three rules cover most of the work:

- **Keep secrets out of code and build output.** The Twelve-Factor App asks for "strict separation of config from code". Its test: could you open-source the code today "without compromising any credentials"? [@twelve-factor]
- **Make secrets short-lived and small.** A stolen secret should work for minutes, not years, and reach one system, not all.
- **Be able to rotate any secret fast.** Leaks will happen. Rotation is how you recover.

> [!key] **The key idea.** Assume every secret will leak one day. Then design so a leak does little harm: short lifetimes, small scopes, an audit log, and a rotation you have already practised.

## How it works

### How secrets leak

Secrets leak through ordinary paths:

- **Git commits.** Deleting the line in a later commit does not help. The old commit still holds it. That is why GitHub's scanner checks "your entire Git history on all branches". [@gh-scanning]
- **Logs and CI output.** A debug line prints a request with its `Authorization` header.
- **Container images.** OWASP says never to put secrets in Docker `ENV` or `ARG` lines. [@owasp-secrets]
- **Client apps.** Anything shipped inside a mobile app or a JavaScript bundle can be read by anyone who downloads it.
- **Chat and AI tools.** Someone may paste a config file into a chat or an AI prompt to get help.

### Environment variables: better than code, not the end

The Twelve-Factor App says to put config in environment variables. They change per deploy without code changes, and they are unlikely to be committed by accident. [@twelve-factor]

OWASP adds a warning. Environment variables "are generally accessible to all processes and may be included in logs or system dumps". It does not recommend them unless the other methods are not possible. [@owasp-secrets] A better option is a file that the orchestrator mounts at run time, never one built into the image. [@owasp-secrets]

### A secrets manager

A **secrets manager**, such as HashiCorp Vault, AWS Secrets Manager or GCP Secret Manager, gives you four things:

1. **One central store.** OWASP says to centralize and standardize secrets management. [@owasp-secrets]
2. **Access policy.** Each service reads only its own secrets. This is [least privilege](/learn/authentication-and-authorization), applied to machines.
3. **An audit log.** It records who asked for which secret, and when. OWASP says the log must resist tampering. [@owasp-secrets]
4. **Rotation.** The store can change the secret on a schedule.

### Short-lived credentials instead of stored keys

The best secret is one that does not exist until you need it.

**Dynamic secrets.** Vault's database engine creates a new database user when a client asks. Each user has a lease, for example 1 hour, which the client can renew up to a maximum, for example 24 hours. When the lease ends, Vault revokes the user. [@vault-db] Every service gets its own username. So you can trace a strange query back to one service instance. [@vault-db]

**OIDC from CI.** A GitHub Actions job can deploy to a cloud with no stored cloud key. The job gets a signed token from GitHub's OIDC provider. The cloud checks it and returns an access token that "is only valid for a single job". [@gh-oidc]

```yaml
jobs:
  deploy:
    runs-on: ubuntu-latest
    permissions:
      id-token: write   # lets the job request an OIDC token
      contents: read
    steps:
      # <pinned-sha> stands for a full commit SHA
      - uses: aws-actions/configure-aws-credentials@<pinned-sha>
        with:
          role-to-assume: arn:aws:iam::123456789012:role/deploy
          aws-region: eu-west-1
```

The `id-token: write` line does not give write access to anything. It only lets the job ask for the token. [@gh-oidc-aws] OWASP gives the same advice for pipelines: avoid static bootstrap credentials and use short-lived OIDC tokens. [@owasp-secrets]

### Rotation with two valid versions

A rotation has a risky moment. If the password changes before every app has the new one, some calls fail.

For rotation by a Lambda function, AWS Secrets Manager offers two strategies for database secrets. "Single user" changes one password. AWS calls it the simplest, and fine for most cases. There is a short window when the database may deny calls, so retry. [@aws-rotation] "Alternating users" keeps two users and changes them in turn. After a rotation, both users' credentials are valid. AWS says it fits apps that need high availability. [@aws-rotation]

The same idea works for API keys and signing keys. Add the new key. Move every client to it. Then remove the old one.

### Encryption keys: KMS and envelope encryption

Encrypting data creates a new problem: you must protect the key. AWS defines **envelope encryption** as "encrypting plaintext data with a data key, and then encrypting the data key under another key". [@aws-kms]

Google Cloud names the two keys. The **data encryption key** (DEK) encrypts the data. The **key encryption key** (KEK) wraps the DEK, and "the KEK never leaves Cloud KMS". [@gcp-envelope] Step through it:

[[diagram:envelope-encryption-steps]]

Why not send all the data to KMS? Cloud KMS's Encrypt and Decrypt calls accept at most 64 KiB, because the service was built to manage KEKs. [@gcp-envelope] Google also says to make a new DEK for every write, and never to share one DEK between two users. [@gcp-envelope] That fits [multi-tenancy](/learn/multi-tenancy-noisy-neighbours) well.

At the top sits a **hardware security module** (HSM). AWS KMS keys never leave the service's FIPS 140-3 Level 3 HSMs unencrypted. [@aws-kms] OWASP says a dedicated HSM of your own is only needed when a law, contract or security policy demands it. [@owasp-secrets] KMS also holds asymmetric keys, for example for signing. Their private keys never leave AWS KMS unencrypted. [@aws-kms]

### Secret scanning

Catch secrets before they land, and find the ones that already did.

- **GitHub secret scanning** checks all history. It is free and automatic on public repos. Private organization repos need GitHub Secret Protection. For partner secrets, GitHub tells the provider, which may revoke the credential. [@gh-scanning]
- **Push protection** blocks a push that contains a secret. For users it is on by default for public repos. For repositories, it needs GitHub Secret Protection, and an admin must turn it on. Anyone with write access can bypass it by giving a reason. The bypass raises an alert. [@gh-push]
- **Gitleaks** scans Git history with `gitleaks git` and folders with `gitleaks dir`. It also runs as a pre-commit hook. It is now in maintenance: security fixes only. [@gitleaks]

### When a secret leaks

OWASP's order is: revoke, rotate, then delete the secret from the exposed system. [@owasp-secrets] Do not start with a history rewrite. GitHub says removing a secret from history is slow and often not needed once the credential is revoked. [@gh-scanning]

Then ask OWASP's questions: who had access, when was it used, and when was it last rotated? [@owasp-secrets] Assume it was used, as CircleCI told its customers. [@circleci] Treat it as an [incident](/learn/incident-response-and-postmortems).

## Trade-offs

| Option | Survives a Git leak? | Rotation effort | Main risk | Use it when |
|---|---|---|---|---|
| Hard-coded in code | No. History keeps it. [@gh-scanning] | High. A code change and a deploy. | Anyone with the repo has it. | Never. |
| Environment variable or CI variable | Yes, if never committed. | Medium. Update and restart. | Readable by other processes, logs and dumps. [@owasp-secrets] | Small apps, with no better option yet. |
| Secrets manager, static secret | Yes. | Low. Scheduled rotation. [@aws-rotation] | The store itself, and its bootstrap credential. | Third-party API keys and passwords you cannot make dynamic. |
| Dynamic or OIDC credential | Yes. | Low. Each credential expires on its own. Vault's own root credential still needs rotating. [@vault-db] | Setup work, and a new runtime dependency. | Databases, cloud access and CI deploys. |

**Simple rule:** use short-lived credentials where the system supports them. Put everything else in a secrets manager with rotation.

## At work: how a senior uses this

- For every secret, ask: **"If this leaks tonight, who can rotate it, and how long does it take?"**
- Keep a list of secrets with owner, scope and last rotation date. Put expiry dates in monitoring.
- Replace stored cloud keys in [CI/CD](/learn/ci-cd-trunk-based-development) with OIDC. Delete the old keys after.
- Turn on push protection and a pre-commit scanner for every repo. On private organization repos, push protection needs GitHub Secret Protection.
- Practise a rotation of your main database password before you need it.
- In review, look for secrets in logs, test fixtures, Dockerfiles and front-end code.
- Give each service its own credentials. Shared credentials make audit logs useless.

## With AI

[Coding agents](/learn/working-with-coding-agents) read your files and run commands. Treat them like any tool that can see secrets.

### What to ask the agent

> "Scan this repo and its Git history for hard-coded secrets. List file, commit and secret type. Do not print the secret values."

> "Rewrite this GitHub Actions workflow to use OIDC with AWS instead of stored access keys. Pin every action to a commit SHA."

> "Add envelope encryption for the `ssn` column using AWS KMS. Use a new data key for every write and store only the wrapped key."

### What to check yourself

- No real secret appears in the prompt, in `.env` files the agent can read, or in its output.
- The agent did not log a secret while debugging, even "just for now".
- New code reads secrets at run time from the secrets manager, not from a constant.
- Envelope encryption code never writes the plain data key to disk or logs.
- OIDC trust policies limit which repo and branch can assume the role.
- If a secret was pasted into a chat, treat it as leaked and rotate it.

## Check yourself

## Go deeper

- [Authentication and authorization](/learn/authentication-and-authorization): tokens, sessions and least privilege.
- [OWASP Top 10 and secure coding](/learn/owasp-top-10-secure-coding): where secrets fit among the common flaws.
- [Dependency and supply-chain security](/learn/dependency-supply-chain-security): trusting the code you did not write. Coming next on this line: Threat modelling and zero trust.
