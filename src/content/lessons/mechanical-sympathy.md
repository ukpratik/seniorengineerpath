---
topic: mechanical-sympathy
subtitle: The same loop can run ten times faster or slower depending on how its data sits in memory. Learn how CPU caches work, so you can lay out data the way the hardware likes it.
status: published
reviewed_by: ai-factcheck
reviewed_on: 2026-11-08
illustrative_story: true
sources:
  - id: drepper-caches
    title: "Ulrich Drepper, “What every programmer should know about memory, Part 2: CPU caches”, LWN.net, 2007"
    url: https://lwn.net/Articles/252125/
    note: Cache levels, access times in cycles, 64-byte cache lines, prefetching, and measured sequential versus random list walks (section 3).
  - id: drepper-numa
    title: "Ulrich Drepper, “What every programmer should know about memory, Part 4: NUMA support”, LWN.net, 2007"
    url: https://lwn.net/Articles/254445/
    note: What NUMA is, the NUMA factor, and measured costs of reading memory on another node (section 5).
  - id: drepper-code
    title: "Ulrich Drepper, “What every programmer should know about memory, Part 5: What programmers can do”, LWN.net, 2007"
    url: https://lwn.net/Articles/255364/
    note: Struct layout, hot and cold data, the matrix multiplication speed-ups, and branch prediction hints (section 6).
  - id: disruptor
    title: "Martin Thompson, Dave Farley, Michael Barker, Patricia Gee and Andrew Stewart, “Disruptor: High performance alternative to bounded queues for exchanging data between concurrent threads”, LMAX, 2011"
    url: https://lmax-exchange.github.io/disruptor/disruptor.html
    note: Why linked queues are slow on modern CPUs, cache lines, false sharing, and prefetching on arrays.
  - id: thompson-why
    title: "Martin Thompson, “Why Mechanical Sympathy?”, Mechanical Sympathy blog, 15 July 2011"
    url: https://mechanical-sympathy.blogspot.com/2011/07/why-mechanical-sympathy.html
    note: Where the term comes from (the racing driver Jackie Stewart) and why it matters for software.
  - id: thompson-fs
    title: "Martin Thompson, “False Sharing”, Mechanical Sympathy blog, 30 July 2011"
    url: https://mechanical-sympathy.blogspot.com/2011/07/false-sharing.html
    note: A short Java benchmark with and without padding, and why cores pass a cache line back and forth.
  - id: contended
    title: "OpenJDK source, jdk.internal.vm.annotation.Contended (Javadoc)"
    url: https://github.com/openjdk/jdk/blob/master/src/java.base/share/classes/jdk/internal/vm/annotation/Contended.java
    note: The JDK's annotation for fields that suffer false sharing, and its warning about extra memory.
  - id: hotspot-globals
    title: "OpenJDK source, HotSpot runtime/globals.hpp (JVM flags)"
    url: https://github.com/openjdk/jdk/blob/master/src/hotspot/share/runtime/globals.hpp
    note: The flags ContendedPaddingWidth (128 bytes by default) and RestrictContended (@Contended only for trusted classes by default).
  - id: striped64
    title: "OpenJDK source, java.util.concurrent.atomic.Striped64 (the base of LongAdder)"
    url: https://github.com/openjdk/jdk/blob/master/src/java.base/share/classes/java/util/concurrent/atomic/Striped64.java
    note: How LongAdder keeps a table of counter cells, each padded with @Contended to avoid false sharing.
  - id: go-cacheline
    title: "Go project, golang.org/x/sys commit “cpu: fix cpu cacheLineSize for arm64 darwin(a.k.a. M1)”, 2022"
    url: https://go.googlesource.com/sys/+/20c2bfdbfe249b02ea905477d7cff52673dadfa2%5E%21/
    note: Go pads for 128-byte lines on arm64 because Apple M1 reports 128-byte cache lines.
  - id: gregg-perf
    title: "Brendan Gregg, “Linux perf Examples”"
    url: https://www.brendangregg.com/perf.html
    note: perf stat commands for cache misses, how to read instructions per cycle, and the warning that VMs may hide hardware counters.
  - id: criterion
    title: "Criterion.rs, README"
    url: https://github.com/criterion-rs/criterion.rs
    note: Statistics-driven microbenchmarking in Rust that detects performance improvements and regressions.
  - id: jmh
    title: "OpenJDK, JMH (Java Microbenchmark Harness), README"
    url: https://github.com/openjdk/jmh
    note: The standard Java benchmark harness, with samples of common benchmarking mistakes.
claims_checked:
  - claim: "Early caches used 32-byte lines. The norm is now 64 bytes."
    source: drepper-caches
    note: "Section 3.2: \"In early caches these lines were 32 bytes long; now the norm is 64 bytes.\""
  - claim: "Pentium M access times: L1d about 3 cycles, L2 about 14, main memory about 240, so main memory costs about 80 L1 reads."
    source: drepper-caches
    note: "\"These are the numbers Intel lists for a Pentium M\": Register <= 1, L1d ~3, L2 ~14, Main Memory ~240. 240 / 3 = 80."
  - claim: "Pentium 4 list walk: about 9 cycles per element in memory order, 450 cycles and more in random order, about 50 times slower."
    source: drepper-caches
    note: "Section 3.3.2, NPAD=0 in both runs (Figure 3.15): \"keep the access times as low as 9 cycles\"; random: \"here we reach 450 cycles and more\". 450 / 9 = 50."
  - claim: "Matrix multiplication: changing the read order cut run time to 23.4%; with blocking and vector instructions it reached about 10%."
    source: drepper-code
    note: "Table 6.2: transposed 23.4%, sub-matrix 17.3%, plus SSE2 9.47%: \"runs in 10% of the time of the original code\"."
  - claim: "Pointer chasing can make memory access \"more than 2 orders of magnitude less efficient\"."
    source: disruptor
    note: "Linked lists and trees have no predictable stride, \"resulting in main memory accesses which can be more than 2 orders of magnitude less efficient\"."
  - claim: "The term mechanical sympathy comes from the racing driver Jackie Stewart."
    source: thompson-why
    note: "\"The name 'Mechanical Sympathy' comes from the great racing driver Jackie Stewart\"."
  - claim: "The JDK pads @Contended fields by 128 bytes by default and, by default, honours @Contended only in trusted classes."
    source: hotspot-globals
    note: "ContendedPaddingWidth = 128; RestrictContended = true, \"Restrict @Contended to trusted classes\"."
quiz:
  - q: Two threads each add to their own counter, A and B. The counters are next to each other in memory. Adding a second thread made the program slower. What is the most likely cause?
    options:
      - A data race. The threads overwrite each other's counter.
      - False sharing. A and B are in the same cache line, so the cores keep taking the line from each other.
      - The garbage collector runs more often with two threads.
    answer: 1
    why: Each thread writes only its own counter, so no data is lost. But a core must own the whole cache line to write to it, so the line moves between the cores on every write.
  - q: You walk 10 million items. Which layout is usually fastest?
    options:
      - A linked list, because inserting is cheap.
      - A tree, because it is sorted.
      - An array, read from start to end.
    answer: 2
    why: An array read in order has a fixed stride. The CPU detects it and prefetches the next cache lines. In a linked list, the CPU must load each node to find the next one.
  - q: About how much slower is a read from main memory than a read from the L1 cache?
    options:
      - About 2 times.
      - About 10 to 100 times.
      - About 1 million times.
    answer: 1
    why: Drepper's table for a Pentium M gives about 3 cycles for L1 and about 240 cycles for main memory, roughly 80 times. Exact numbers vary by CPU.
  - q: Your benchmark says a new data layout is 3 times faster. What should you check before you trust it?
    options:
      - Nothing. A benchmark is a measurement, so it is correct.
      - Whether it ran in a proper harness such as JMH, with warm-up and several runs, and whether perf stat shows fewer cache misses.
      - Whether the code compiles with no warnings.
    answer: 1
    why: Micro-benchmarks are easy to get wrong. JMH asks you to study its samples of common mistakes. Hardware counters show whether the cause is really fewer cache misses.
incidents: []
cases: [lmax-architecture]
related: [latency-estimation, concurrency-primitives, tail-latency-queueing, os-essentials]
---

## What broke

This is a made-up example. A team runs a Java service that counts events. One thread did all the counting. It was too slow, so they split the work across 4 threads.

To avoid locks, each thread got its own counter. They used an `AtomicLongArray` of size 4. Thread 0 adds to slot 0, thread 1 to slot 1, and so on, once per event. The main thread sums the slots at the end.

No thread touched another thread's counter. But with 4 threads, the job ran slower than with 1.

The cause was not in the code. It was in the hardware. The four counters took 32 bytes, side by side. A CPU cache moves memory in blocks of 64 bytes on most machines. [@drepper-caches] So the four counters shared one block, or at most two. Each write by one core took the block away from the other cores.

The fix was to space the counters 128 bytes apart. The code did the same work, and now it scaled with the number of threads. Martin Thompson shows the same effect in a short Java benchmark. [@thompson-fs]

## The idea in simple words

**Mechanical sympathy** means writing code that suits how the machine really works. Martin Thompson took the term from the racing driver Jackie Stewart. [@thompson-why] Stewart's point was that a great driver knows how the car works, not only how to steer it.

For you, the "car" is the CPU and its memory. A modern CPU is very fast. Main memory is slow next to it. To hide that gap, the CPU keeps small, fast caches close to each core.

The hardware decides what goes in the caches. But your data layout and access order decide whether they help or hurt.

> [!key] **The key idea.** Memory is not one flat, equal-speed array. Data that is close together and read in order is fast. Data that is scattered, or written by many cores at once, is slow. The same algorithm can run ten times faster or slower because of this.

## How it works

### The memory hierarchy

A CPU has several levels of cache. L1 is the smallest and fastest. L2 is bigger and slower. Many CPUs also add a third level, L3. [@drepper-caches] Behind them all is main memory (DRAM).

The table below is old. It shows Intel's figures for the Pentium M, a laptop CPU from the early 2000s, as given in Drepper's 2007 paper. [@drepper-caches] Newer CPUs differ, so read them as orders of magnitude only.

| Where the data is | About how long |
|---|---|
| Register | 1 cycle or less |
| L1 data cache | 3 cycles |
| L2 cache | 14 cycles |
| Main memory | 240 cycles |

This chip had no L3. Most CPUs today have an L3 between L2 and main memory. [@drepper-caches]

So a read from main memory can cost as much as about 80 reads from L1. The [latency numbers lesson](/learn/latency-numbers-and-estimation) puts a main memory read at about 100 ns.

### Cache lines

The cache does not load single bytes. It loads a whole **cache line**. Early caches used 32-byte lines. The norm is now 64 bytes. [@drepper-caches] Some CPUs use 128 bytes, such as Apple's M1. [@go-cacheline] When you read one 8-byte `long`, you also get the 56 bytes around it.

This rewards **spatial locality**: data you use together should sit together. It also rewards **temporal locality**: data you used a moment ago is probably still in the cache.

### Prefetching: why sequential beats random

When you read memory in order, the CPU notices. It loads the next cache line before you ask for it. This is **hardware prefetching**. [@drepper-caches] Walking an array has a fixed stride, so the CPU can predict it. [@disruptor]

Drepper measured this on a Pentium 4. He walked a large list of small elements, bigger than every cache. In memory order, each element cost about 9 cycles, thanks to prefetching and because 8 elements fit in one line. In random order, it cost 450 cycles and more. [@drepper-caches] That is about 50 times slower, for the same work.

### Arrays versus linked structures

A linked list, tree or hash map with chained nodes puts each node somewhere in memory. To find the next node, the CPU must first load the current one. This is **pointer chasing**. The CPU cannot fetch ahead, because it does not know the next address yet.

The Disruptor paper says this can make memory access "more than 2 orders of magnitude less efficient". [@disruptor] That is one reason the LMAX team built the Disruptor on a pre-allocated ring buffer, not a linked list. [@disruptor]

### Array of structs versus struct of arrays

A worked example: say each `Order` object has 10 fields, about 80 bytes. A report sums only `price`. With an array of `Order` objects, each 64-byte line brings in at most one price. The other 56 bytes are wasted. In Java, an array of objects holds references, so each item also costs a pointer hop.

Now store each field in its own array: `double[] prices`, `long[] ids`, and so on. One line now holds 8 prices. The loop touches about 8 times fewer lines. This is a **struct of arrays**.

Drepper gives a related example. A loop needs only two fields of an `order` struct, but the other fields are loaded too. He says the program could run "up to 5 times worse than it could". His advice is to split the struct. [@drepper-code]

The order of the loop matters as much as the layout. In Drepper's matrix multiplication, changing the order of memory reads alone cut the run time to 23.4% of the original. With blocking and vector instructions too, it reached about 10%. [@drepper-code] That is a ten-times gain with the same maths.

### False sharing and padding

Step through the bug from the story.

[[diagram:false-sharing-padding]]

Each core can keep its own copy of a cache line in its cache. Before a core writes to a line, the other cores' copies are marked invalid. [@drepper-caches] So if two threads write two different variables in one line, the line moves between the cores on every write. This is **false sharing**. [@disruptor]

The fix is **padding**: unused bytes that push hot variables onto separate lines. [@thompson-fs] The JDK pads by 128 bytes by default, not 64. [@hotspot-globals] In the story, the fix looks like this:

```java
// 16 longs = 128 bytes between slots; the first block stays empty
AtomicLongArray counts = new AtomicLongArray((threads + 2) * 16);
// thread i: counts.incrementAndGet((i + 1) * 16)
```

A Java array is not aligned to a cache line. Slot 0 sits next to the array header, so the code leaves the first block empty.

The JDK uses an internal annotation for this, `@Contended`. Its docs warn that it "will nearly always add significant space overhead to objects". [@contended] By default, the JVM obeys it only in trusted JDK classes. Your own code needs the flag `-XX:-RestrictContended`. [@hotspot-globals]

In everyday Java, use `LongAdder` for a shared counter. It keeps a table of counter cells, each padded with `@Contended`. [@striped64]

### Branch prediction and NUMA, briefly

CPUs "invest heavily in branch prediction". They guess which way an `if` will go and start work early. A wrong guess wastes that work. [@drepper-code] If a branch goes one way or the other at random, the CPU often guesses wrong, and a hot loop slows down.

Large servers have several CPU sockets, each with its own memory. This is **NUMA**. Reading memory attached to another socket costs more. [@drepper-numa] In Drepper's test, reading memory one hop away was about 20% slower. [@drepper-numa] Bigger machines have more hops, so the cost can be higher. [@drepper-numa]

### Measure, do not guess

Count the events with `perf stat`. Brendan Gregg shows commands such as: [@gregg-perf]

```
perf stat -e cycles,instructions,cache-references,cache-misses ./app
```

With cycles and instructions, perf also prints instructions per cycle (IPC). Low IPC with many cache misses usually means the CPU is waiting for memory. [@gregg-perf] Many hardware counters may not be available inside a virtual machine. [@gregg-perf]

Compare layouts with a real benchmark harness: JMH for Java [@jmh], or Criterion.rs for Rust. [@criterion] JMH's own README says a good harness does not free you from benchmarking pitfalls. [@jmh]

## Trade-offs

| Choice | Good for | Cost | Watch out for |
|---|---|---|---|
| Array, read in order | Fast scans. The CPU prefetches. | Inserting in the middle is slow. | Fixed size or costly resizing. |
| Linked list or tree | Cheap inserts and deletes. | Pointer chasing. Each step may wait for memory. | Fine for small or cold data. |
| Struct of arrays | Loops that read a few fields of many items. | Code is harder to read. One item is spread across arrays. | Only worth it on hot loops you measured. |
| Padding hot fields | Stops false sharing between threads. | More memory per object. [@contended] | Pad only fields that different threads write often. |
| Ignore layout | Simple, readable code. | Can be many times slower on hot paths. | Fine for most code. Measure first. |

## At work: how a senior uses this

- Ask "is this on the hot path?" first. Most code is not. Do not trade clarity for cache tricks there.
- Run `perf stat` on the real workload before you change layout. Check cache misses and IPC.
- When adding threads makes a job slower, suspect false sharing. Look for counters, flags or indexes that different threads write, sitting side by side.
- In Java, try `LongAdder` for hot shared counters before you pad an array by hand.
- Prefer arrays for big collections you scan often.
- For big scans, ask which fields the loop really reads. Move cold fields to a separate object.
- Prove each change with a benchmark on hardware like production.

## With AI

### What to ask the agent

> "Here is our hot loop and the class it reads. List each field the loop uses. Suggest a layout that puts those fields in fewer cache lines, and explain the trade-off."

> "Write a JMH benchmark that compares an array of objects with a struct of arrays for summing one field over 10 million items. Include warm-up and use a Blackhole."

### What to check yourself

- Every speed claim the agent makes is backed by a benchmark you ran, not by reasoning alone.
- The benchmark uses JMH or criterion, with warm-up and several runs. A loop timed with `System.nanoTime()` is not enough.
- Any padding uses at least 128 bytes between hot fields, like the JDK default, and is limited to fields that really cause contention.

## Check yourself

## Go deeper

- [OS essentials for backend engineers](/learn/os-essentials-for-backend)
- [Tail latency and queueing](/learn/tail-latency-and-queueing)
- [Latency numbers and back-of-envelope maths](/learn/latency-numbers-and-estimation)
- [Locks, atomics and deadlocks](/learn/locks-atomics-and-deadlocks)
- Next on this track: lock-free programming and memory models, and the LMAX Disruptor and the single-writer rule.
