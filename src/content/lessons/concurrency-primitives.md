---
topic: concurrency-primitives
subtitle: Two threads that touch the same data can quietly break it. Locks and atomics stop that, and lock order stops the deadlocks that locks can cause.
status: published
illustrative_story: true
sources:
  - id: ostep-intro
    title: "Remzi and Andrea Arpaci-Dusseau, Operating Systems: Three Easy Pieces, chapter 26, “Concurrency: An Introduction”"
    url: https://pages.cs.wisc.edu/~remzi/OSTEP/threads-intro.pdf
    note: Two threads add to a shared counter 10 million times each and get a different wrong total on each run. Defines race condition and critical section.
  - id: ostep-locks
    title: "Arpaci-Dusseau, Operating Systems: Three Easy Pieces, chapter 28, “Locks”"
    url: https://pages.cs.wisc.edu/~remzi/OSTEP/threads-locks.pdf
    note: How locks are built from hardware instructions such as compare-and-swap, and why simple spin locks can starve a thread.
  - id: ostep-bugs
    title: "Arpaci-Dusseau, Operating Systems: Three Easy Pieces, chapter 32, “Common Concurrency Problems”"
    url: https://pages.cs.wisc.edu/~remzi/OSTEP/threads-bugs.pdf
    note: The four conditions for deadlock from Coffman, Elphick and Shoshani (1971), lock ordering, livelock, and a study of real concurrency bugs.
  - id: java-atomic
    title: "Java SE 21 API: package java.util.concurrent.atomic"
    url: https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/atomic/package-summary.html
    note: AtomicInteger, AtomicLong and compareAndSet for lock-free, thread-safe updates of one variable.
  - id: java-rwlock
    title: "Java SE 21 API: interface ReadWriteLock"
    url: https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/locks/ReadWriteLock.html
    note: Many readers or one writer, and when that really helps.
  - id: java-juc
    title: "Java SE 21 API: package java.util.concurrent"
    url: https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/package-summary.html
    note: Thread pools and blocking queues for producer-consumer designs.
  - id: go-sync
    title: "Go standard library: package sync"
    url: https://pkg.go.dev/sync
    note: Mutex and RWMutex, and the advice to prefer channels for higher-level work.
  - id: go-atomic
    title: "Go standard library: package sync/atomic"
    url: https://pkg.go.dev/sync/atomic
    note: What compare-and-swap and atomic add do, and the warning that they need great care.
  - id: py-threading
    title: "Python documentation: threading — Lock objects"
    url: https://docs.python.org/3/library/threading.html
    note: Lock.acquire with a timeout, and using a lock with the with statement.
  - id: pg-lock
    title: "PostgreSQL documentation: Explicit Locking (Deadlocks)"
    url: https://www.postgresql.org/docs/current/explicit-locking.html
    note: A deadlock between two UPDATEs on two rows, how Postgres breaks it, and why a consistent lock order prevents it.
  - id: pg-timeout
    title: "PostgreSQL documentation: Client Connection Defaults (lock_timeout)"
    url: https://www.postgresql.org/docs/current/runtime-config-client.html
    note: lock_timeout stops a statement that waits too long for a lock.
  - id: kleppmann
    title: "Martin Kleppmann, “How to do distributed locking” (2016)"
    url: https://martin.kleppmann.com/2016/02/08/how-to-do-distributed-locking.html
    note: Locks for efficiency versus correctness, leases that expire during a pause, and fencing tokens.
claims_checked:
  - claim: Two threads each add 1 to a shared counter 10 million times; the right total is 20,000,000, but runs gave 19,345,221 and 19,221,041. The increment is three x86 instructions (load, add, store).
    source: ostep-intro
    note: Numbers and the mov/add/mov sequence match chapter 26 (v1.10 PDF) exactly. The book also shows one correct run, so the lesson now says "sometimes" the answer is right. Race condition and critical section definitions match the chapter.
  - claim: Four conditions must all hold for deadlock (mutual exclusion, hold-and-wait, no preemption, circular wait); a lock order is "probably the most practical" prevention; a random delay helps with livelock.
    source: ostep-bugs
    note: All in chapter 32. OSTEP cites Coffman, Elphick and Shoshani (1971) as [C+71]; we did not read that paper, and the lesson only says OSTEP credits it. The trylock-and-release pattern is under "No Preemption" in the book, so the lesson no longer says timeouts break hold-and-wait.
  - claim: Simple spin locks are not fair and can lead to starvation; locks can be built from compare-and-swap.
    source: ostep-locks
    note: Chapter 28, sections on fairness and 28.9 Compare-And-Swap.
  - claim: Go's CompareAndSwap sets the value to new only if it equals old and returns a bool; atomics "require great care to be used correctly"; Mutex.Lock blocks until the mutex is free; a blocked Lock call excludes new readers.
    source: go-atomic
    note: Pseudocode and warning on pkg.go.dev/sync/atomic. Mutex and RWMutex text checked on pkg.go.dev/sync (go-sync), including "Higher-level synchronization is better done via channels and communication."
  - claim: A read-write lock gains fully only on a multi-processor, depends on the read/write ratio, and short reads can let its overhead dominate.
    source: java-rwlock
    note: Paraphrase of the ReadWriteLock interface docs. The docs say reader preference "can delay the writer indefinitely", which the trade-off table now states instead of a general starvation claim.
  - claim: Postgres detects deadlocks and aborts one transaction, which one is "difficult to predict"; the best defence is a consistent lock order; lock_timeout aborts a statement that waits too long for a lock.
    source: pg-lock
    note: Section 13.3.4 Deadlocks, including the accounts example and the advice to retry. lock_timeout checked in runtime-config-client (pg-timeout).
  - claim: A distributed lock should be a lease with a timeout; a GC pause can outlast the lease; fencing tokens increase on each grant and storage rejects older tokens; for efficiency-only locks a single Redis instance is fine.
    source: kleppmann
    note: All four points are in the 2016 post.
  - claim: Python Lock.acquire with a positive timeout blocks at most that many seconds and returns False if the lock was not acquired; acquire on a locked lock blocks until release().
    source: py-threading
    note: Lock Objects section of the threading docs.
reviewed_by: ai-factcheck
reviewed_on: 2026-10-22
quiz:
  - q: Two threads each run `count = count + 1` once. The counter starts at 7. With no lock, what can the result be?
    options:
      - Always 9.
      - 8 or 9, depending on timing.
      - Always 8.
    answer: 1
    why: Both threads can load 7 before either stores. Then both store 8, and one add is lost.
  - q: Which change removes the circular wait that causes most deadlocks?
    options:
      - Add more threads.
      - Make every thread take locks in the same global order.
      - Use a read-write lock everywhere.
    answer: 1
    why: If everyone takes lock 1 before lock 2, no thread can hold 2 while waiting for 1, so no cycle can form.
  - q: What does compare-and-swap do?
    options:
      - It writes a new value only if the variable still holds the value you expected, and tells you if it worked.
      - It swaps two variables in memory.
      - It locks the variable until you unlock it.
    answer: 0
    why: Go's docs describe it as one atomic step. If the value equals old, set it to new and return true. Otherwise return false.
  - q: Postgres finds that two transactions wait for each other's row locks. What happens?
    options:
      - Both wait forever.
      - Postgres aborts one of them, so the other can finish. The app should retry.
      - Postgres merges the two transactions.
    answer: 1
    why: Postgres detects deadlocks and aborts one transaction. Which one is hard to predict.
  - q: A lease-based distributed lock expires while the holder is stuck in a long GC pause. What stops the old holder's late write?
    options:
      - Nothing. Leases make this impossible.
      - A fencing token that the storage checks on every write.
      - A longer thread sleep.
    answer: 1
    why: Kleppmann shows that the storage must reject writes that carry an older token than one it has already seen.
incidents: []
related: [os-essentials, transactions-isolation, concurrency-control, lock-free, concurrency-models, leader-election]
---

## What broke

This is a made-up example, but the bug in it is real. A team runs a small API that counts video views. Each request starts a thread, and each thread runs `views = views + 1` on a shared counter in memory. In tests, 1,000 requests give 1,000 views.

In production, traffic is high and the server has many CPU cores. The dashboard shows fewer views than the access logs. The gap is small, so nobody notices for weeks. There is no error and no stack trace.

The bug is easy to reproduce. The OSTEP textbook runs two threads that each add 1 to a shared counter 10 million times. The answer should be 20,000,000. [@ostep-intro]

Sometimes the book gets the right answer. But one run gave 19,345,221, and the next gave 19,221,041. [@ostep-intro] Wrong totals that change from run to run are the mark of a race condition. In this lesson you will see why it happens, and the tools that fix it.

## The idea in simple words

`views = views + 1` looks like one step. OSTEP shows that on x86 it becomes three instructions: load the value into a register, add 1, and store it back. [@ostep-intro]

If the scheduler switches threads between those steps, two threads can load the same old value. [@ostep-intro] Both store the same new value. One add is lost.

OSTEP calls this a **race condition**: the result depends on the timing of the threads. The code that touches shared data is a **critical section**. It must not run in more than one thread at a time. [@ostep-intro]

> [!key] **The key idea.** Shared data that changes needs one of three things: a lock, so only one thread is inside at a time; an atomic operation, so the change cannot be split; or no sharing at all.

## How it works

Step through the lost update, then the two fixes. Use Next and Back, or the ← and → keys.

[[diagram:counter-race]]

### Mutexes and locks

A **mutex** (mutual exclusion lock) has two states, locked and unlocked. In Python, `acquire()` on a locked lock blocks until another thread calls `release()`. [@py-threading] Go's `sync.Mutex` works the same way. [@go-sync]

Put the critical section between lock and unlock:

```go
mu.Lock()
views = views + 1
mu.Unlock()
```

Keep the critical section short. Never do a network call or slow disk read while you hold a lock. Every other thread that needs it waits.

### Atomics and compare-and-swap

For one number, a lock is often more than you need. Java's `AtomicLong` and `AtomicInteger` have atomic increment methods. [@java-atomic] Go has `atomic.AddInt64`. [@go-atomic]

The base tool under many of these is **compare-and-swap** (CAS). Go's docs describe it as one atomic step: if the value still equals `old`, set it to `new` and return true; otherwise return false. [@go-atomic] Java calls it `compareAndSet`. [@java-atomic]

With CAS you read the value, compute the new one, and try to swap. If another thread changed it first, the swap fails and you try again. OSTEP shows how locks themselves are built from instructions like this. [@ostep-locks]

Go warns that its atomic functions "require great care to be used correctly". Outside low-level code, it suggests channels or the `sync` package instead. [@go-atomic]

### Read-write locks

Many readers can safely read at the same time. Only writers need to be alone. A **read-write lock** allows many readers or one writer. [@java-rwlock]

Java's docs say the gain is fully seen only on a multi-processor, and only when data is read often and changed rarely. If reads are very short, the extra cost of the lock can eat the gain. [@java-rwlock] Go also warns against taking a read lock again while you hold one. A waiting writer blocks new readers. [@go-sync]

### Deadlock

Locks bring a new risk. Thread 1 holds lock A and waits for lock B. Thread 2 holds B and waits for A. Neither can move. This is a deadlock.

OSTEP lists four conditions, which it credits to a 1971 paper by Coffman, Elphick and Shoshani. All four must hold for a deadlock: [@ostep-bugs]

1. **Mutual exclusion.** A thread has a resource, such as a lock, to itself.
2. **Hold and wait.** A thread holds some locks while it waits for more.
3. **No preemption.** Nobody can take a lock away from its holder.
4. **Circular wait.** There is a cycle of threads, each waiting for a lock the next one holds.

Break any one condition and deadlock cannot happen. [@ostep-bugs]

**Lock ordering** breaks circular wait. Decide a global order, for example "always lock A before B". OSTEP calls this probably the most practical way to prevent deadlock. [@ostep-bugs]

**Try, then back off.** OSTEP shows a thread that cannot get its second lock. It releases the first lock and starts again. The book discusses this under no preemption. [@ostep-bugs] In Python, `acquire(timeout=2)` returns `False` if it cannot get the lock in 2 seconds. [@py-threading]

### Livelock and starvation

- **Livelock:** threads keep trying and backing off, but none makes progress. OSTEP suggests a random delay before each retry. [@ostep-bugs]
- **Starvation:** one thread never gets the lock, because others always win. Simple spin locks are not fair and can cause it. [@ostep-locks]

### The same ideas in a database

A database has the same problems, one level up. Two requests that read a row, add 1 in app code, and write it back can lose an update. The [transactions lesson](/learn/transactions-and-isolation-levels) shows the fixes: one atomic `UPDATE`, or a row lock with `SELECT … FOR UPDATE`.

Row locks can deadlock too. The Postgres docs show two transactions that update the same two accounts in opposite order. Postgres detects the deadlock and aborts one of them. Which one is hard to predict. [@pg-lock]

The fix is the same as in memory. Postgres says the best defence is to take locks on many objects in a consistent order. [@pg-lock] You can also set `lock_timeout`, so a statement stops instead of waiting too long for a lock. [@pg-timeout] More in [locking and MVCC in practice](/learn/locking-and-mvcc-in-practice).

### Across machines

A mutex works only inside one process. When many servers share one job, you need a distributed lock. Kleppmann says it should expire after a time, so a crashed client cannot hold it forever. A lock with a timeout is called a **lease**. [@kleppmann]

Leases have a trap. Martin Kleppmann describes a client that pauses for garbage collection. Its lease expires, another client takes the lock, and then the first client wakes up and writes anyway. [@kleppmann]

His fix is a **fencing token**: a number that grows each time the lock is given out. The storage rejects any write with an older token than one it has already seen. [@kleppmann] He also asks: is the lock for efficiency or for correctness? If two nodes doing the same work only costs a little, a simple lock on one Redis node is enough. [@kleppmann] See [leader election and leases](/learn/leader-election-and-leases) and [Raft](/learn/consensus-and-raft).

### Prefer higher-level tools

Go's `sync` docs say higher-level synchronization is better done with channels. [@go-sync] Java gives you thread pools and blocking queues for producer-consumer designs. [@java-juc]

The best lock is the one you do not need. Give each piece of data one owner, for example one worker reading from a queue. Pass immutable values between threads. Actors, covered in [threads, async and actors](/learn/threads-async-and-actors), follow the same idea.

## Trade-offs

| Tool | Good for | Cost | Watch out for |
|---|---|---|---|
| Mutex | Any critical section, many variables at once. | Threads wait. Slow if held long. | Deadlock, forgotten unlock. |
| Atomic / CAS | One counter or one reference. | Retries under heavy contention. | Two atomics together are not atomic. |
| Read-write lock | Many reads, rare writes. | More complex than a mutex. | Little gain on one core. Busy readers can delay a writer for a long time. |
| Database row lock | Shared rows across many app servers. | Waiting transactions, deadlock aborts. | Lock order, `lock_timeout`, retries. |
| Lease (distributed lock) | One worker per job across machines. | A lock service to run. | Expiry during a pause. Needs fencing for correctness. |
| Queue or single owner | No shared changing data at all. | Extra hop, ordering design. | A slow consumer becomes the bottleneck. |

## At work: how a senior uses this

- When you see shared state that changes, ask "who else writes this?" first. This one question finds many race bugs.
- Prefer no sharing, then atomics for one value, then a mutex. Use a read-write lock only after a profile shows it helps.
- Write the lock order down in a comment or in the design doc, and keep it the same in every code path.
- Never call the network or wait on I/O while you hold a lock.
- Run your tests with a race detector where the language has one.
- In SQL, update rows in a fixed order, for example by ID, and retry on deadlock errors.
- For a distributed lock, ask whether it is for efficiency or for correctness. For correctness, use fencing tokens, or better, a database constraint.
- When a bug "only happens under load" and the numbers are a little off, suspect a race condition.

## With AI

### What to ask the agent

> "Here is our Go service. List every variable that more than one goroutine can write. For each, say how it is protected today, and suggest the simplest safe fix."

> "This Java class uses two locks. Draw the order in which each method takes them, and tell me if any pair of methods can deadlock."

> "Write a test that runs this counter code with 8 threads and 1 million increments each, and fails if the total is wrong."

### What to check yourself

- The agent's fix protects every read and write of the shared data, not just one of them.
- A "thread-safe" claim is tested with many threads and many runs, not one run.
- No lock is held across a network call or a callback.
- Two separate atomic operations were not treated as one atomic step.
- Code that uses a distributed lock for correctness also has fencing or a database check.

## Check yourself

## Go deeper

Next: [Lock-free programming and memory models](/learn/lock-free-programming-memory-models) goes deeper into CAS loops and memory visibility. OSTEP's chapter on common concurrency bugs is free and short.
