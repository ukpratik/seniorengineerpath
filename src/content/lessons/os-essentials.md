---
topic: os-essentials
subtitle: Processes, threads, memory, files and system calls are where your code meets the machine. Know them, and many "random" production failures stop being random.
status: published
reviewed_by: ai-factcheck
reviewed_on: 2026-11-07
illustrative_story: true
sources:
  - id: k8s-memory
    title: "Kubernetes documentation, “Assign Memory Resources to Containers and Pods”"
    url: https://kubernetes.io/docs/tasks/configure-pod-container/assign-memory-resource/
    note: A container that tries to use 250 MiB under a 100 MiB limit is killed with reason OOMKilled, exit code 137, and restarted again and again.
  - id: k8s-cgroups
    title: "Kubernetes documentation, “About cgroup v2”"
    url: https://kubernetes.io/docs/concepts/architecture/cgroups/
    note: How the kubelet and the container runtime use cgroups to enforce the CPU and memory limits of pods and containers.
  - id: cgroup-v2
    title: "Linux kernel documentation, “Control Group v2” (memory controller)"
    url: https://docs.kernel.org/admin-guide/cgroup-v2.html
    note: memory.max, memory.high and memory.events, what the cgroup counts as memory, and when the OOM killer runs inside a cgroup.
  - id: ostep-lde
    title: "Remzi and Andrea Arpaci-Dusseau, Operating Systems: Three Easy Pieces, chapter 6, “Mechanism: Limited Direct Execution”"
    url: https://pages.cs.wisc.edu/~remzi/OSTEP/cpu-mechanisms.pdf
    note: User mode and kernel mode, the trap that starts a system call, what a context switch saves, and how long both take.
  - id: ostep-vm
    title: "Arpaci-Dusseau, Operating Systems: Three Easy Pieces, chapter 13, “The Abstraction: Address Spaces”"
    url: https://pages.cs.wisc.edu/~remzi/OSTEP/vm-intro.pdf
    note: Why each process gets its own private address space, and why every address your program prints is virtual.
  - id: pthreads
    title: "Linux man-pages, pthreads(7)"
    url: https://man7.org/linux/man-pages/man7/pthreads.7.html
    note: What threads in one process share (memory, open file descriptors, resource limits) and what each thread has alone (its stack).
  - id: open-2
    title: "Linux man-pages, open(2)"
    url: https://man7.org/linux/man-pages/man2/open.2.html
    note: What a file descriptor is, and the EMFILE and ENFILE errors.
  - id: socket-2
    title: "Linux man-pages, socket(2)"
    url: https://man7.org/linux/man-pages/man2/socket.2.html
    note: A new socket is a file descriptor, so it fails with EMFILE when the limit is reached.
  - id: errno-3
    title: "Linux man-pages, errno(3)"
    url: https://man7.org/linux/man-pages/man3/errno.3.html
    note: The text for each error code, including EMFILE, "Too many open files".
  - id: getrlimit
    title: "Linux man-pages, getrlimit(2)"
    url: https://man7.org/linux/man-pages/man2/getrlimit.2.html
    note: RLIMIT_NOFILE, soft and hard limits, and how child processes inherit limits.
  - id: write-2
    title: "Linux man-pages, write(2)"
    url: https://man7.org/linux/man-pages/man2/write.2.html
    note: Why a successful write() does not mean the data is on disk, and where late errors show up.
  - id: fsync-2
    title: "Linux man-pages, fsync(2)"
    url: https://man7.org/linux/man-pages/man2/fsync.2.html
    note: What fsync() and fdatasync() flush, that they block until the device is done, and why a new file also needs a directory fsync.
  - id: meminfo
    title: "Linux man-pages, proc_meminfo(5)"
    url: https://man7.org/linux/man-pages/man5/proc_meminfo.5.html
    note: The fields in /proc/meminfo, including Cached (the page cache), Dirty and MemAvailable.
  - id: pg-async
    title: "PostgreSQL documentation, “Asynchronous Commit”"
    url: https://www.postgresql.org/docs/current/wal-async-commit.html
    note: Why waiting for the WAL flush is a large part of a short transaction, and what you risk when you skip the wait.
  - id: gregg-use
    title: "Brendan Gregg, “USE Method: Linux Performance Checklist”"
    url: https://www.brendangregg.com/USEmethod/use-linux.html
    note: Which Linux command shows each resource, including memory, the OOM killer and file descriptors.
claims_checked:
  - claim: "A container that uses more memory than its limit is killed with reason OOMKilled and exit code 137, and the kubelet restarts it again and again."
    source: k8s-memory
    note: "The docs exercise allocates 250 MiB under a 100 MiB limit. The output shows exitCode 137 and reason OOMKilled, then repeated restarts. The lesson no longer treats exit code 137 alone as proof of an OOM kill."
  - claim: "When a cgroup's memory use reaches memory.max and cannot be reduced, the OOM killer runs inside that cgroup. Going over memory.high throttles the processes but never invokes the OOM killer. The cgroup counts page cache, anonymous memory and some kernel memory such as TCP socket buffers."
    source: cgroup-v2
    note: "memory.max and memory.high entries, and the list of tracked memory types. The draft implied page cache causes the kill; it now says the kernel can free page cache first."
  - claim: "The kubelet and the container runtime use cgroups to enforce the memory limits of containers."
    source: k8s-cgroups
    note: "Added for the draft's uncited 'Linux puts the processes of a container in a cgroup'; the sentence now names the container runtime."
  - claim: "A successful write() does not guarantee the data is on disk. fsync() blocks until the device reports the transfer is complete, and a new file also needs an fsync() on its directory."
    source: fsync-2
    note: "fsync(2) DESCRIPTION, with write(2) NOTES. Late errors are scoped to NFS-like filesystems as write(2) says."
  - claim: "Threads of one process share memory, open file descriptors and resource limits; each thread has its own stack."
    source: pthreads
    note: "pthreads(7) list of process-wide attributes."
  - claim: "In 1996 on a 200 MHz CPU, a system call took about 4 microseconds and a context switch about 6; modern 2-3 GHz systems reach sub-microsecond results."
    source: ostep-lde
    note: "Aside 'How long context switches take' (lmbench). The lesson now says these are direct costs only."
quiz:
  - q: Your Java service has a 512 MiB heap. Its container has a 512 MiB memory limit. Why can it still be OOM-killed?
    options:
      - The limit counts all the memory of the container, such as thread stacks and native buffers, not only the heap.
      - It cannot. The heap is the only memory a process uses.
      - Kubernetes kills any container that uses more than half its limit.
    answer: 0
    why: The cgroup counts the memory of every process in it, not only the heap. Each thread has its own stack, and the JVM also uses native memory. The kernel can free page cache, but not these.
  - q: A service logs "too many open files" (EMFILE). What has it hit?
    options:
      - The disk is full.
      - The per-process limit on open file descriptors, RLIMIT_NOFILE.
      - The maximum file size.
    answer: 1
    why: The open(2) man page defines EMFILE as reaching the per-process limit on open file descriptors. A new socket is a file descriptor too.
  - q: write() returned success. Then the machine lost power. Is the data safe?
    options:
      - Yes. write() returns only after the disk has the data.
      - Yes, if the file was opened for writing.
      - Not for sure. write() may only have copied the data to the page cache. Calling fsync() makes it wait for the device.
    answer: 2
    why: The write(2) man page says a successful write gives no guarantee that the data is on disk. fsync() returns only after the device confirms it has stored the data.
  - q: Two threads in the same process open files. What do they share?
    options:
      - Nothing. Each thread has its own memory and its own file descriptors.
      - Heap memory and the table of open file descriptors. Each thread has its own stack.
      - Only the stack.
    answer: 1
    why: The pthreads(7) man page lists shared memory, open file descriptors and resource limits, and says each thread has its own stack.
incidents: []
related: [concurrency-primitives, gc-tuning, kubernetes-basics, mechanical-sympathy, io-models, storage-internals]
---

## What broke

This is a made-up example, but each part of it is common. A team moves a Java order service into Kubernetes. The container gets a 512 MiB memory limit. To be safe, they set the JVM heap to 512 MiB too.

Under load, the pod restarts every few hours. `kubectl get pod` shows the status `OOMKilled`. The last state has exit code 137. The Kubernetes docs show exactly this pattern: a container goes over its limit, gets killed, restarts, and gets killed again. [@k8s-memory]

The team looks at the JVM dashboard. Heap use is at 400 MiB, well under 512 MiB. So who used the memory?

The answer is: the operating system counted more than the heap. The limit applies to the whole container. It counts every thread stack and every native buffer. [@cgroup-v2] [@pthreads] It even counts file pages in the page cache, but the kernel can free those first. Stacks and native memory cannot be freed that way. [@cgroup-v2]

A week later, the same service fails in a new way. Logs fill with "too many open files". A client library forgot to close sockets. This lesson explains both failures, and the OS ideas behind them.

## The idea in simple words

Your program cannot use devices such as disks and network cards directly. The **kernel** sits in between. It shares the CPU, memory and disks among many programs.

A **process** is a running program with its own private memory. A **thread** is one line of execution inside a process. Threads in one process share memory and open files. Each thread has its own stack. [@pthreads]

When your code needs the kernel, for example to read a file or send bytes on a socket, it makes a **system call**. That call costs time.

The kernel also keeps limits. It limits how much memory a group of processes may use, and how many files one process may hold open. Some limits are hard. When you hit the memory limit or the open-file limit, the kernel does not slow down politely. It kills the process, or the call fails.

> [!key] **The key idea.** Your runtime shows you a small part of the picture, such as the heap. The kernel counts much more: almost all memory, every open file and socket, and time spent waiting. Limits and costs come from the kernel's view, not your runtime's view.

## How it works

### User mode, kernel mode and system calls

Your code runs in **user mode**. In user mode it cannot do I/O or other privileged operations. The kernel runs in kernel mode, with full access to the machine. [@ostep-lde]

To ask the kernel for something, a program runs a special trap instruction. The CPU jumps into the kernel, which does the work and then returns to user mode. [@ostep-lde] `read`, `write`, `open`, `accept` and `fsync` are all system calls.

### Context switches

The kernel shares each CPU among many threads. It runs one for a while, then switches to another. To switch, it saves the registers of the running thread and restores those of the next one. This is a **context switch**. [@ostep-lde]

Both steps cost time. In 1996, on a 200 MHz CPU, a system call took about 4 microseconds and a context switch about 6. [@ostep-lde] OSTEP says modern 2–3 GHz systems reach sub-microsecond results. [@ostep-lde] These are the direct costs. OSTEP also notes that many OS operations depend on memory speed, which has not improved as fast as CPUs. [@ostep-lde] A few microseconds is cheap once. It is not cheap 100,000 times per second, so reading a file 1 byte per call is slow.

### Virtual memory

Every process gets its own **address space**. It looks like a large, private block of memory. The OS and hardware map each address to a real place in RAM. [@ostep-vm]

So every address your program prints is a virtual address. [@ostep-vm] Two processes can use the same address and never see each other's data. This is how the OS keeps one buggy process from damaging another. [@ostep-vm]

### The page cache

The kernel keeps recently read and written file data in RAM. This is the **page cache**. In `/proc/meminfo` you see it as `Cached`. [@meminfo]

The page cache is why the second read of a file is often much faster than the first. It is also why `free` on a busy server can show little free memory. The useful number is `MemAvailable`: how much memory new programs can get without the system swapping. [@meminfo]

### Memory limits and the OOM killer

The container runtime puts the processes of each container in a control group, or **cgroup**. Kubernetes uses it to enforce the container's memory limit. [@k8s-cgroups] In cgroup v2, `memory.max` is the hard memory limit. [@cgroup-v2]

The cgroup counts the page cache, anonymous memory such as heaps and stacks, and some kernel memory, such as TCP socket buffers. [@cgroup-v2] When usage reaches `memory.max` and the kernel cannot reduce it, it runs the **OOM killer** inside that cgroup. [@cgroup-v2] The OOM killer ends a process to get memory back.

`memory.events` counts these events. `oom` counts how often the limit was reached, and `oom_kill` counts processes killed. [@cgroup-v2] There is also a softer limit, `memory.high`. Going over it slows the processes down, but it never calls the OOM killer. [@cgroup-v2]

In Kubernetes, a container may use more than its request if the node has room, but never more than its limit. [@k8s-memory]

### Files and file descriptors

When you open a file, the kernel returns a **file descriptor**. It is a small number, starting from 0, that points into your process's table of file descriptors. [@open-2] A new socket gets a file descriptor too. [@socket-2] So do pipes. [@getrlimit]

Each process has a limit, `RLIMIT_NOFILE`. Calls such as `open`, `pipe` and `dup` that go over it fail with `EMFILE`. [@getrlimit] The open(2) man page describes EMFILE as the per-process limit on open file descriptors. [@open-2] Your logs show it as "Too many open files". [@errno-3] There is also a system-wide limit, which gives `ENFILE`. [@open-2]

The limit has a soft value, which the kernel enforces, and a hard value, which caps the soft one. A child process inherits its parent's limits. [@getrlimit]

All threads in a process share one descriptor table and one limit. [@pthreads] A leak in one library can starve the whole service.

### Writes, fsync and durability

Step through one write, with the ← and → keys or the buttons.

[[diagram:write-fsync-path]]

A successful `write()` does not mean the data is on disk. [@write-2] For normal, buffered writes, the kernel only copied it into the page cache. [@cgroup-v2] `/proc/meminfo` calls this data `Dirty`: memory waiting to be written back to disk. [@meminfo]

`fsync()` sends the file's changed data to the storage device. It blocks, and returns only after the device confirms it has stored the data. [@fsync-2] If you created a new file, you also need an `fsync()` on its directory to be sure the entry is on disk. [@fsync-2]

Errors can come late, too. If the kernel fails to write the data back, `fsync()` reports it. [@fsync-2] On some filesystems, such as NFS, an error may show up only at a later `write()`, `fsync()` or even `close()`. [@write-2] Always check the return value of `fsync()`.

This wait is real. PostgreSQL waits for its log records to reach permanent storage before it reports a commit. For short transactions, the docs call this delay "a major component of the total transaction time". [@pg-async]

### Seeing it on a real server

Brendan Gregg's USE checklist names the tools for each resource. [@gregg-use]

- `free -m` and `vmstat 1` for memory use. `vmstat` columns `si` and `so` show swapping. [@gregg-use]
- `dmesg | grep killed` to find OOM kills. [@gregg-use]
- `/proc/sys/fs/file-nr` for system-wide open files. [@gregg-use]
- `strace` to catch calls that fail with `EMFILE`. [@gregg-use]

## Trade-offs

| Choice | Good for | Cost | Watch out for |
|---|---|---|---|
| Many threads in one process | Sharing memory cheaply. | Each thread needs its own stack. Locks for shared data. | One bad thread can corrupt shared memory or leak shared descriptors. |
| Many processes | Isolation. A crash stays in one process. | More memory. Sharing data needs sockets, pipes or shared memory. | Each process has its own limits. |
| `write()` without `fsync()` | Fast writes. | No. Data may be lost on a power cut. | Fine for caches and logs you can lose. |
| `fsync()` on every commit | Durable data. | Every commit waits for the device. | Check its errors. A new file also needs a directory `fsync()`. |
| Postgres asynchronous commit | Much faster small transactions. | The last few transactions can be lost in a crash. | Data loss, but not corruption. [@pg-async] |
| Memory limit = heap size | Nothing. | OOM kills under load. | Leave room for stacks and native memory. |

## At work: how a senior uses this

- When a pod restarts, check the last state first. The reason `OOMKilled` means the container hit its memory limit. The cause can be a limit that is too low, or a memory leak. Trust the reason, not the exit code alone.
- Set the memory limit above the heap. Measure total container memory under load, not only heap graphs.
- Watch `memory.events`. Rising `high` or `max` counts warn you before a kill. Any `oom_kill` means a process already died.
- Treat "too many open files" as a leak until proven otherwise. Raising the limit only buys time.
- Count open descriptors per process in your dashboards, next to memory.
- Ask "where does this write become durable?" in every design review that stores data.

## With AI

### What to ask the agent

> "Here is our Kubernetes deployment and JVM flags. Estimate total container memory: heap, thread stacks, metaspace and native buffers. Tell me if the memory limit leaves enough room."

> "Search this Go service for every place we open a file, socket or HTTP response body. For each, show where it is closed, and list paths where it is not."

> "This code writes an order to a file and then replies 'saved'. Explain which crash could lose the order, and show the smallest fix with fsync."

### What to check yourself

- Every memory number the agent gives is labelled heap, stack or native. Totals that add only the heap are wrong.
- Each close happens on error paths too, for example in `defer` or `finally`.
- The fsync fix checks the return value, and a new file also gets a directory fsync.

## Check yourself

## Go deeper

- [Locks, atomics and deadlocks](/learn/locks-atomics-and-deadlocks): what to do once two threads share memory.
- [Kubernetes and runtime basics](/learn/kubernetes-and-runtime-basics): requests, limits and how pods are scheduled.
- [Garbage collection and runtime tuning](/learn/garbage-collection-runtime-tuning): heap sizing inside a container limit.
- Next steps on this track: mechanical sympathy (CPU caches and memory), I/O models (blocking, epoll, zero-copy), and storage engine internals.
