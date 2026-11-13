---
topic: disaster-recovery
subtitle: Pick an RPO and an RTO, keep backups that a bad script cannot reach, and time real restores until you know you can get your data back.
status: published
illustrative_story: false
sources:
  - id: atl-pir
    title: "Atlassian, “Post-Incident Review on the Atlassian April 2022 outage”"
    url: https://www.atlassian.com/engineering/post-incident-review-april-2022-outage
    note: How 883 deleted sites came back with little data loss, and why it still took up to 14 days.
  - id: gitlab-pm
    title: "GitLab, “Postmortem of database outage of January 31”"
    url: https://about.gitlab.com/blog/2017/02/10/postmortem-of-database-outage-of-january-31/
    note: Backup methods that were broken or never set up, and an 18-hour copy over slow disks.
  - id: pg-pitr
    title: "PostgreSQL documentation: 25.3. Continuous Archiving and Point-in-Time Recovery (PITR)"
    url: https://www.postgresql.org/docs/current/continuous-archiving.html
    note: Base backups, WAL archiving, recovery targets and incremental backups, with their limits.
  - id: pg-delay
    title: "PostgreSQL documentation: 19.6. Replication"
    url: https://www.postgresql.org/docs/current/runtime-config-replication.html
    note: The recovery_min_apply_delay setting for a time-delayed standby, and what it costs.
  - id: mssql-diff
    title: "Microsoft Learn, “Differential Backups (SQL Server)”"
    url: https://learn.microsoft.com/en-us/sql/relational-databases/backup-restore/differential-backups-sql-server
    note: What a differential backup holds, why it grows, and what you need to restore it.
  - id: aws-rel13
    title: "AWS Well-Architected Framework, Reliability Pillar, “REL13-BP02 Use defined recovery strategies to meet the recovery objectives”"
    url: https://docs.aws.amazon.com/wellarchitected/latest/reliability-pillar/rel_planning_for_recovery_disaster_recovery.html
    note: The four DR strategies with their RPO and RTO ranges, in order of cost and complexity.
  - id: aws-dr-options
    title: "AWS Whitepaper, Disaster Recovery of Workloads on AWS: Recovery in the Cloud, “Disaster recovery options in the cloud”"
    url: https://docs.aws.amazon.com/whitepapers/latest/disaster-recovery-workloads-on-aws/disaster-recovery-options-in-the-cloud.html
    note: Each strategy in detail, cross-account backups, scheduled restores, and why active/active still needs backups.
  - id: uscert-backup
    title: "Paul Ruggiero and Matthew A. Heckathorn, “Data Backup Options”, US-CERT (Carnegie Mellon University, 2012)"
    url: https://www.cisa.gov/sites/default/files/publications/data_backup_options.pdf
    note: A short paper that states the 3-2-1 rule and credits it to Peter Krogh's The DAM Book.
  - id: cisa-ransomware
    title: "CISA, “#StopRansomware Guide”"
    url: https://www.cisa.gov/stopransomware/ransomware-guide
    note: Why backups must be offline and tested, and a warning about immutable storage.
  - id: rds-delete
    title: "Amazon RDS User Guide, “Deleting a DB instance”"
    url: https://docs.aws.amazon.com/AmazonRDS/latest/UserGuide/USER_DeleteInstance.html
    note: Deletion protection, final snapshots, and what happens to automated backups when you delete a database.
claims_checked:
  - claim: "5 April 2022: a script deleted 883 sites (775 customers); first restored 8 April, all by 18 April, up to 14 days; most restored to five minutes before deletion, 57 customers first restored further back and data recovered later; Atlassian 'missed our RTO but met our RPO'."
    source: atl-pir
    note: All numbers, the three-week estimate, 30-day retention, immutable backups and the planned DR exercises match the review. Wording fixed so Atlassian 'could' (not 'had tested') restore all customers to a new environment.
  - claim: "GitLab restored from an LVM snapshot made about 6 hours before the outage; copying it from staging took around 18 hours over disks throttled to about 60 Mbps; 'nobody was responsible for testing this procedure'."
    source: gitlab-pm
    note: Also checked that the secondary had already been wiped, pg_dump backups were failing (9.2 vs 9.6) and Azure snapshots were off for database servers.
  - claim: "AWS lists backup and restore (RPO hours, RTO 24 h or less), pilot light (minutes / tens of minutes), warm standby (seconds / minutes) and active-active (near zero / potentially zero), in rising cost and complexity; replication alone does not protect against corruption or destruction without PITR."
    source: aws-rel13
    note: Ranges and the quote match REL13-BP02 exactly.
  - claim: "Logical dumps cannot be used for WAL replay; PITR restores a whole cluster; it needs an unbroken WAL sequence; incremental backups need every earlier backup and Postgres does not track the chain."
    source: pg-pitr
    note: Checked against PostgreSQL 18 docs, section 25.3. Incremental backup needs version 17 or later, now stated.
  - claim: "CISA: 'many ransomware variants attempt to find and subsequently delete or encrypt accessible backups'; keep offline, encrypted backups tested 'in a disaster recovery scenario'; use immutable storage 'with caution'."
    source: cisa-ransomware
    note: Exact wording checked with curl on the CISA page.
reviewed_by: ai-factcheck
reviewed_on: 2026-11-08
quiz:
  - q: Your team has a Postgres primary and two replicas in other zones. Someone runs a DELETE with no WHERE clause. Why do the replicas not save you?
    options:
      - Replicas are read-only, so they cannot be promoted.
      - The replicas copy the DELETE too, a moment after the primary.
      - Replicas only keep the last hour of data.
    answer: 1
    why: Replication copies every change, good or bad. You need a backup that can go back to a moment before the mistake.
  - q: Your RPO is 15 minutes. You take one full backup every night and nothing else. What is wrong?
    options:
      - Nothing. A nightly backup meets any RPO.
      - A crash just before the next backup could lose almost a day of writes, far more than 15 minutes.
      - The RTO will be too short.
    answer: 1
    why: With only nightly backups, you can lose up to 24 hours of writes. Archiving the WAL for point-in-time recovery closes that gap.
  - q: Atlassian met its RPO in April 2022 but missed its RTO. What was the main reason?
    options:
      - Its backups were older than 30 days.
      - It had never tested its backups at all.
      - Its restore process was not built for restoring many sites out of shared data stores at once.
    answer: 2
    why: Atlassian regularly restored one customer or a small set, and could restore all customers into a new environment. It had no tested way to restore hundreds of sites into the live, shared environment.
  - q: Which DR strategy keeps data copied to a second site, but leaves the app servers there switched off until a disaster?
    options:
      - Backup and restore
      - Pilot light
      - Multi-site active/active
    answer: 1
    why: In a pilot light setup, the databases and storage are always on, but app servers are created or started only when you fail over.
incidents: [atlassian-2022-04-site-deletion]
related: [replication, incident-response, schema-migrations, safe-deploys, multi-region]
---

## What broke

On 5 April 2022, a cleanup script at Atlassian deleted 883 sites. They belonged to 775 customers. [@atl-pir] You met this outage in [incident response](/learn/incident-response-and-postmortems), from the communication side. Here is the restore side.

Atlassian was not careless about backups. It kept them for 30 days. It could restore any data store to a chosen point in time. It also kept immutable backups and tested its restores. [@atl-pir]

The data did come back. For most customers, Atlassian restored to five minutes before the deletion. For 57 customers restored early, some databases first went back further, and Atlassian recovered the missing data later. It says no customer lost more than five minutes of data. [@atl-pir]

Time was the problem. The first customers got their sites back on 8 April. The last sites came back on 18 April. Some customers waited up to 14 days. [@atl-pir]

Why so slow? Atlassian regularly restored one customer or a small set of customers, and it could restore all customers into a new environment. It had no tested way to restore hundreds of sites into the live, shared environment. Each data store held data for many customers. So the team had to pull each deleted site out of the backups, piece by piece. With its first method, Atlassian estimated three weeks to restore everyone. [@atl-pir]

In its own words: "we missed our RTO but met our RPO". [@atl-pir]

## The idea in simple words

Disaster recovery (DR) is your plan to get data and service back after something big goes wrong. That can be a lost data centre, a bad script, or ransomware.

Two numbers set the goal. The **RPO** (recovery point objective) is how much data, measured in time, you accept to lose. The **RTO** (recovery time objective) is how long you accept to be down. Atlassian describes them with two questions. How fresh is the data after you restore it? How quickly can you give it back to the customer? [@atl-pir]

Smaller numbers cost more. AWS calls the choice a trade-off between RTO and RPO on one side, and cost and complexity on the other. It also warns against a strategy that is stricter than you need. [@aws-rel13]

You saw in the [replication lesson](/learn/database-replication) that a follower copies every change from the leader. That includes a wrong DELETE, so a replica is not a backup. AWS says continuous replication "will not protect you against data corruption or destruction" unless you also have point-in-time recovery. [@aws-rel13]

> [!key] **The key idea.** A backup counts only after you have restored it and timed the restore, at the size and shape of the disaster you fear. Until then, it is a hope.

## How it works

### Kinds of backup

A **full backup** copies everything. It is simple to restore, but big and slow.

An **incremental backup** holds only what changed since the previous backup. Since version 17, Postgres can take one with `pg_basebackup --incremental`. To restore it, you need it and every earlier backup it depends on. Postgres does not track that chain for you, so you must not delete a backup that a later one needs. [@pg-pitr]

A **differential backup** holds everything that changed since the last full backup. To restore, you load the full backup, then only the newest differential. Differentials grow every day, so Microsoft suggests a new full backup at set times, for example weekly. [@mssql-diff]

There is a second split. A **logical backup**, like `pg_dump`, writes rows as SQL. A physical backup copies the database's own files, or a disk snapshot of them. Postgres says logical dumps "do not contain enough information to be used by WAL replay". [@pg-pitr] So they cannot give you point-in-time recovery.

### Point-in-time recovery

Point-in-time recovery (PITR) needs two things: a **base backup** of the data files, and an archive of every write-ahead log (WAL) file since then. To recover, you load the base backup and replay the WAL. You can stop the replay at any moment after the base backup ended. [@pg-pitr]

Step through a recovery from a bad DELETE. Use Next and Back, or the ← and → keys.

[[diagram:pitr-before-delete]]

You choose the stop point by time, by a named restore point, or by a transaction ID. [@pg-pitr] You need an unbroken chain of WAL files back to the base backup. One missing file breaks it. Postgres advises you to set up and test WAL archiving before you take your first base backup. [@pg-pitr]

A physical PITR restores the whole database cluster, not one table or one customer. [@pg-pitr] Atlassian hit a similar problem at a much larger scale: its data stores were shared by many customers. Restoring one customer from a shared store means a restore to a side server, then a careful copy back.

### Where the copies live

The **3-2-1 rule** says: keep 3 copies of important data (1 primary and 2 backups), on 2 different media types, with 1 copy offsite. A 2012 US-CERT paper states it and credits it to the second edition (2009) of *The DAM Book*, a book for photographers by Peter Krogh. [@uscert-backup]

In the cloud, "offsite" should also mean "outside your blast radius". AWS says copying backups to another account helps against insider threats or a stolen account. [@aws-dr-options]

Attackers know where backups are. CISA says "many ransomware variants attempt to find and subsequently delete or encrypt accessible backups". It asks you to keep offline, encrypted backups. [@cisa-ransomware] An **immutable backup** cannot be changed or deleted until its retention time ends. CISA notes that some cloud vendors offer this, but says to use it "with caution". It may not meet some compliance rules, and a wrong setting can be costly. [@cisa-ransomware]

### Seatbelts against your own mistakes

- **Deletion protection.** Amazon RDS will not delete a database while deletion protection is on. It is on by default for databases created in the console. If you delete without keeping automated backups, RDS deletes those backups in the same region. [@rds-delete]
- **A delayed replica.** In Postgres, `recovery_min_apply_delay = '1h'` makes a standby apply each commit only an hour after the primary made it. That gives you time to stop it before a bad change arrives. Longer delays fill its disk with waiting WAL files. [@pg-delay]
- **Soft delete.** Mark data as deleted and remove it only later. After the outage, Atlassian committed to soft deletes, keeping deleted data until a retention period ends. [@atl-pir]

### Test the restore, and time it

GitLab learned this in 2017. An engineer wiped the primary database while fixing replication, and the replica's data had already been wiped. The team restored from a disk snapshot made six hours earlier, which sat on a staging server. Copying it took around 18 hours, because the disks were throttled to about 60 Mbps. [@gitlab-pm] GitLab also wrote that nobody owned the backups, so "nobody was responsible for testing this procedure". [@gitlab-pm]

AWS says your backup strategy "must include testing your backups", and suggests restoring on a schedule. [@aws-dr-options] CISA also asks you to test backups "in a disaster recovery scenario". [@cisa-ransomware]

A good restore test is a job, not a promise. Every week it restores the newest backup to a fresh server. It checks row counts and a few known records. It records how long the restore took, and it pages someone when any step fails.

A **game day** goes further. The team acts out a whole disaster, with people, runbooks and the clock running. After the outage, Atlassian committed to regular DR exercises that restore every product for a large set of sites at once. [@atl-pir]

## Trade-offs

AWS lists four strategies in order of rising cost and complexity, and falling RPO and RTO. The middle two, **pilot light** and **warm standby**, keep a second site partly ready. The ranges below are AWS's own. [@aws-rel13]

| Strategy | RPO and RTO (AWS) | What runs at the second site | Use it when |
|---|---|---|---|
| Backup and restore | RPO in hours, RTO in 24 hours or less. PITR can bring RPO as low as 5 minutes in some cases. | Only backups. You rebuild servers and restore data after the disaster. | Most internal tools and young products. It is cheap and simple. |
| Pilot light | RPO in minutes, RTO in tens of minutes. | Data is replicated and always on. App servers are off until you need them. | Downtime of an hour hurts, but a second full stack is too costly. |
| Warm standby | RPO in seconds, RTO in minutes. | A smaller but working copy of the whole system. You scale it up. | Customer-facing systems where minutes matter. |
| Multi-site active/active | RPO near zero, RTO potentially zero. | Full production in every site, all taking traffic. | You already need several regions for users or law. |

**Simple rule:** every strategy still needs point-in-time backups. Even with active/active, a data disaster's recovery point "will always be at some point before the disaster was discovered". [@aws-dr-options]

## At work: how a senior uses this

- Ask the business for the RPO and RTO of each system, and write them down.
- Ask, **"Which restore have we timed this quarter, and how long did it take?"** If nobody knows, the RTO is a guess.
- List the disasters you plan for: a lost zone, a bad migration, a deleted customer, ransomware. Check that each one has a tested restore of the right shape.
- Keep at least one backup copy in a separate account, with different credentials, that production scripts cannot delete.
- Before a risky migration or bulk delete, take a fresh backup and know the restore time. See [schema migrations](/learn/schema-migrations-zero-downtime).
- Alert when a backup job fails or the newest backup is too old.

## With AI

### What to ask the agent

> "Here is our Postgres setup. Write a weekly job that restores the newest base backup and WAL to a fresh server, runs these five checks, records the restore time, and fails loudly if any step fails."

> "Read our Terraform. List every database, bucket and backup vault. For each, tell me if deletion protection, versioning or object lock is on, and who can delete it."

### What to check yourself

- The restore test runs against a real, recent backup, not a tiny test database. Restore time grows with data size.
- The checks look at data, not only at "the server started".
- The backup credentials are different from the production credentials the agent saw. Code that can reach both is a single point of failure.
- Retention is long enough to reach back past the time a bug could go unnoticed.

## Check yourself

## Go deeper

Before this: [Database replication](/learn/database-replication), to see how followers copy every change. Next: [Multi-region systems and data locality](/learn/multi-region-and-data-locality), for running in more than one region every day, not only after a disaster.

Related lessons: [Incident response and postmortems](/learn/incident-response-and-postmortems), [Safe deploys and config changes](/learn/safe-deploys-config-changes) and [Requirements and non-functional needs](/learn/requirements-and-non-functional-needs). Case study: the [GitLab incident](/incidents#gitlab-2017-01-database).
