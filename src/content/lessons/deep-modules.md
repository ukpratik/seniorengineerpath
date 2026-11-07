---
topic: deep-modules
subtitle: Learn why code gets hard to change, and how to hide each hard decision behind a simple interface so one change stays in one place.
status: published
illustrative_story: true
sources:
  - id: parnas-1972
    title: "D. L. Parnas, “On the Criteria To Be Used in Decomposing Systems into Modules”, Communications of the ACM 15(12), December 1972, pp. 1053–1058"
    url: https://wstomv.win.tue.nl/edu/2ip30/references/criteria_for_modularization.pdf
    note: The classic paper. Two ways to split the same small program, and which changes each one survives.
  - id: ouster-complexity
    title: "John Ousterhout, “The Nature of Complexity”, CS 190 lecture notes, Stanford (Winter 2018)"
    url: https://web.stanford.edu/~ouster/cgi-bin/cs190-winter18/lecture.php?topic=complexity
    note: Short notes on the three symptoms of complexity and its two causes.
  - id: ouster-modular
    title: "John Ousterhout, “Modular Design”, CS 190 lecture notes, Stanford (Winter 2018)"
    url: https://web.stanford.edu/~ouster/cgi-bin/cs190-winter18/lecture.php?topic=modularDesign
    note: Deep and shallow classes, classitis, information hiding and leakage, general-purpose classes, pulling complexity down.
  - id: ouster-exceptions
    title: "John Ousterhout, “Exception Handling”, CS 190 lecture notes, Stanford (Winter 2018)"
    url: https://web.stanford.edu/~ouster/cgi-bin/cs190-winter18/lecture.php?topic=exceptions
    note: Why errors add complexity, and ways to reduce the places that must handle them.
  - id: ouster-aposd-class
    title: "John Ousterhout, “Discussion of A Philosophy of Software Design”, CS 190 class notes, Stanford (Winter 2024)"
    url: https://web.stanford.edu/~ouster/cs190-winter24/lectures/aposd
    note: The book's main ideas as a short checklist, with a warning not to take "define errors out of existence" too far.
  - id: ouster-aposd-slides
    title: "John Ousterhout, “Text Editor Example”, CS 190 lecture slides (APOSD), Stanford (Winter 2024)"
    url: https://web.stanford.edu/~ouster/cs190-winter24/slides/aposd.pdf
    note: Slide 2 puts a special-purpose and a general-purpose text editor API side by side.
  - id: ouster-unix
    title: "John Ousterhout, “The UNIX Time-Sharing System”, CS 190 class notes, Stanford (Winter 2024)"
    url: https://web.stanford.edu/~ouster/cs190-winter24/lectures/unix
    note: What Unix file and device I/O hid from programs, compared with the systems before it.
  - id: man-read
    title: "Linux man-pages project, read(2)"
    url: https://man7.org/linux/man-pages/man2/read.2.html
    note: The read system call, its return value and how it moves the file offset.
  - id: aposd-vs-cc
    title: "John Ousterhout and Robert C. Martin, “A Philosophy of Software Design vs Clean Code” (GitHub, 2024–2025)"
    url: https://github.com/johnousterhout/aposd-vs-clean-code
    note: A long written debate on method length, comments and TDD, with code, and a summary of where they agree.
claims_checked:
  - claim: Parnas proposes beginning with "a list of difficult design decisions or design decisions which are likely to change" and designing each module "to hide such a decision from the others"; in his KWIC example a change to line storage hits every module of the flowchart design but only module 1 of the second design.
    source: parnas-1972
    note: Quotes checked against the scanned CACM pages 1055 and 1058. The efficiency cost and the inline-assembly remedy are on p. 1057.
  - claim: Ousterhout names three symptoms of complexity (change amplification, cognitive load, unknown unknowns) and two causes (dependencies, obscurity); complexity is judged by readers and builds up from thousands of small dependencies and obscurities.
    source: ouster-complexity
    note: All points are in the Winter 2018 lecture notes.
  - claim: A deep class has a small interface and lots of functionality, with Unix file I/O as the example; a shallow class has a complex interface and/or little functionality; classitis is small classes taken to the extreme; 200-2000 line classes are fine; pass-through methods are a red flag; make classes somewhat generic.
    source: ouster-modular
    note: Information leakage is worded as in the notes ("implementation details exposed"), not as in the book.
  - claim: Before Unix there were separate APIs for sequential and random access and different kernel calls per device; Unix used the same calls with an access position kept by the kernel, and each read advances the file offset by the bytes read (0 at end of file, -1 on error).
    source: ouster-unix
    note: The read() details were checked against man-read.
  - claim: Ousterhout and Martin agree that modular design is good and that over-decomposition is possible; Martin would rather err on the side of decomposition, since methods can be inlined later; both value decomposition and avoid entanglement but "disagree on the relative weighting of those two values".
    source: aposd-vs-cc
    note: These come from the Method Length section and its summary. The debate ran from September 2024 to February 2025, partly in person.
  - claim: The text editor example contrasts special-purpose methods (insertChar, backspace, delete, deleteSelection, ...) with general-purpose insert(position, text) and delete(start, end).
    source: ouster-aposd-slides
    note: Slide 2 of the Winter 2024 APOSD slides.
reviewed_by: ai-factcheck
reviewed_on: 2026-11-08
quiz:
  - q: You add one new field to orders. You have to edit the API handler, the database mapper, two reports and a CSV export. Which symptom of complexity is this?
    options:
      - Cognitive load
      - Change amplification
      - Unknown unknowns
    answer: 1
    why: A simple change needs edits in many places. That is change amplification.
  - q: Which class is the deepest?
    options:
      - A class with 12 public methods that each wrap one line of a library.
      - A class with 2 methods, save(order) and load(id), that hides the table layout, caching and retries.
      - A class whose methods take the same arguments as the class below and call it directly.
    answer: 1
    why: A deep module gives a lot of function for a small interface. The other two are shallow, and the third is made of pass-through methods.
  - q: Two classes both split a CSV line by commas and read column 3 as the amount. What is the best fix?
    options:
      - Add a comment in both classes that says "keep in sync".
      - Move all knowledge of the format into one module that returns parsed objects.
      - Add a unit test for each class.
    answer: 1
    why: This is information leakage. Bring the knowledge into one place, so a format change touches one module.
  - q: What do Ousterhout and Robert Martin agree on in their published debate?
    options:
      - Functions should be 2 to 4 lines long.
      - Modular design is good, and it is possible to split code too far.
      - Comments are always a failure.
    answer: 1
    why: Their summary says they agree on both points. They disagree on how far to split code and on comments.
incidents: []
related: [tech-debt, code-review, rest-api, error-handling]
---

## What broke

This story is made up, but it is common. Meera works on the payments team of a grocery app. Every night, a job writes the day's invoices to a CSV file. A second job reads that file and pays the shops.

The writer builds each line with `join(",")`. The payout job splits each line with `split(",")` and reads the amount from column 2. A finance report does the same split in a third place. Nobody planned it this way. Each engineer copied what worked.

Then the tax rules changed. Meera added a tax column after the id, in the writer. Her tests passed. The pull request was small and looked safe.

That night, the payout job read the tax as the amount. Hundreds of shops got paid only their tax. The report showed the wrong totals too. Finance found it the next morning.

The bug was not in Meera's change. It was in the design. The file format was a decision that three classes shared, and nothing in the code said so.

## The idea in simple words

John Ousterhout teaches software design at Stanford. He uses the word complexity for anything in the structure of a system that makes it hard to work on. [@ouster-complexity] He adds that the reader of the code judges it, not the writer. [@ouster-complexity]

He names three symptoms. [@ouster-complexity]

- **Change amplification.** A simple change needs edits in many places. Meera's tax column needed three.
- **Cognitive load.** You must hold a lot of information in your head to make a change.
- **Unknown unknowns.** You need to know something before a change, but it is not clear where to find it. You may not even know you need it. Ousterhout calls the worst case a crucial fact hidden in far-away code you have never heard of. [@aposd-vs-cc]

He names two causes. **Dependencies**: if one piece of code changes, another must change too. **Obscurity**: it is not clear how the code works or why. [@ouster-complexity] No single choice makes a system complex. Thousands of small dependencies and obscurities add up. [@ouster-complexity]

The cure is old. In 1972, David Parnas proposed a way to split a system into modules. Make a list of the "difficult design decisions or design decisions which are likely to change." Then design each module "to hide such a decision from the others." [@parnas-1972] This is **information hiding**.

> [!key] **The key idea.** Give each hard decision one owner. Hide it behind a small, simple interface that does a lot of work. Then a change to that decision stays inside one module.

Ousterhout calls this a **deep module**: a small interface with a lot of function behind it. A **shallow module** has a complex interface, or does little, or both. [@ouster-modular]

## How it works

Step through the same format change in two designs. Use Next and Back, or the ← and → keys.

[[diagram:format-change-ripple]]

### Deep: Unix file I/O

Ousterhout's example of a deep interface is Unix file I/O. [@ouster-modular] You need only a few calls:

```c
int fd = open("invoices.csv", O_RDONLY);
char buf[4096];
ssize_t n = read(fd, buf, sizeof buf);  // bytes read; 0 at end, -1 on error
close(fd);
```

Look at what you do not see. Before Unix, systems often had separate calls for sequential and random access, and different calls for each kind of device. [@ouster-unix] Unix uses the same calls for both kinds of access. The kernel keeps your position in the file. [@ouster-unix] Each `read` moves that position forward by the bytes it returned. [@man-read] The kernel also hides its buffer cache and device differences. [@ouster-unix] Devices use the same basic calls as files: `open`, `read`, `write`, `lseek` (called `seek` in early Unix) and `close`. One extra call, `ioctl`, handles device-specific work. [@ouster-modular]

### Shallow: a wrapper that hides nothing

Now compare a wrapper like this one, written in TypeScript:

```ts
class RawFile {
  constructor(private fd: number) {}
  readBytes(buf: Buffer, off: number, len: number, pos: number) {
    return fs.readSync(this.fd, buf, off, len, pos);
  }
}
```

Every argument goes straight through. You still track the position yourself. You must learn one more name and get nothing back. Ousterhout says a class is shallow when calling it is not much easier than writing its code. [@ouster-modular] He lists **pass-through methods** like `readBytes` as a red flag. [@ouster-modular] Each layer should offer a different idea from the layers above and below it. [@ouster-modular]

### Information leakage

**Information leakage** is the opposite of information hiding. Implementation details are exposed, and other classes depend on them. [@ouster-modular] Meera's CSV format leaked into three classes. Ousterhout calls this kind "back-door leakage": it is not visible in the interface. [@ouster-modular]

A common cause is splitting code by the order things happen: one class reads, the next one parses, the next one saves. [@ouster-modular] Parnas saw the same problem in 1972. His first design made each processing step a module. If the way lines were stored changed, every module had to change. [@parnas-1972] In his second design, "knowledge of the exact way that the lines are stored is entirely hidden from all but module 1." [@parnas-1972]

The fix for the payments team is one module that owns the format:

```ts
interface InvoiceFile {
  write(path: string, invoices: Invoice[]): void;
  read(path: string): Invoice[];
}
```

The column order, quoting and escaping now live in one place. The payout job gets `Invoice` objects and never sees a comma.

### Pull complexity down

When some complexity cannot be removed, put it inside the module, not on its callers. Ousterhout's words: "Let a few module developers suffer, rather than thousands of users." He also writes that a simple API matters more than a simple implementation. [@ouster-modular]

One form of this is to handle a problem rather than pass it up. Each configuration setting you expose is a question for the user. Ousterhout asks: if you do not know the right value, how will a user or administrator find it? [@ouster-modular]

Another form is to **define errors out of existence**. Shape the API so the error case has a normal meaning. For example, "delete this key" can simply succeed when the key is already gone. Fewer callers then need special handling. [@ouster-exceptions] He warns that it is easy to take this idea too far. [@ouster-aposd-class] It is for cases that are not really errors. Real bugs should still fail loudly, as the [error handling lesson](/learn/error-handling-defensive-design) explains.

### General-purpose beats special-purpose

Ousterhout advises making classes "somewhat generic". Build only what you need today, but shape the interface so it could serve other uses. [@ouster-modular] His text editor example compares two APIs. [@ouster-aposd-slides] The special-purpose one has methods such as `insertChar`, `backspace`, `delete` and `deleteSelection`. The general one has two methods: `insert(position, text)` and `delete(start, end)`. Both serve the editor's keys. He says the general approach gives a simpler and deeper interface. [@ouster-modular]

### Size is not the goal

Ousterhout calls small classes taken to the extreme **classitis**. Each class adds as little as it can. [@ouster-modular] He says classes of 200 to 2,000 lines are fine, and depth matters more than size. [@ouster-modular]

This is where the honest debate starts. Robert Martin's *Clean Code* asks for very small functions. Between September 2024 and February 2025, he and Ousterhout discussed it and published the result as a written debate. [@aposd-vs-cc] They agreed that modular design is good and that you can split code too far. [@aposd-vs-cc] Martin argued that small, well-named methods show how an algorithm works, even at the cost of some entanglement. [@aposd-vs-cc] He said that if he must err, he would rather err on the side of splitting, since you can always inline later. [@aposd-vs-cc] Ousterhout worried that tiny methods become shallow and entangled: you must read one to understand another. [@aposd-vs-cc] Martin summed it up: they both value splitting code, and both avoid entanglement, but "we disagree on the relative weighting of those two values." [@aposd-vs-cc]

So "deep" is a judgment, not a metric. Parnas also noted a cost. If each function in his second design were a separate procedure call, it could be much less efficient. He suggested having the assembler insert the code inline instead. [@parnas-1972]

## Trade-offs

| Design choice | Easy to change? | Cost | Use it when |
|---|---|---|---|
| Deep module with a small interface | Yes. Hidden decisions change in one place. | More thought up front. Some calls may be slower. | The decision is hard or likely to change. |
| Many small, shallow classes | No. Each change crosses many interfaces. | More names to learn, more jumping between files. | Rarely. Merge them when they always change together. |
| Special-purpose API | Yes, for today's case only. | Grows a new method for each new need. | Rarely. Ousterhout advises making classes somewhat generic instead. [@ouster-modular] |
| Somewhat general API | Yes. New uses fit the same methods. | A little more design time. | Several callers, or a clear family of uses. |
| Expose every setting | Yes, for experts. | Every caller must learn and choose values. | The right value truly depends on the caller. |

**Simple rule:** before you add a class or a method, ask what it hides. If the answer is "nothing", do not add it.

## At work: how a senior uses this

- For each module, write one sentence: "This module hides ___." If you cannot fill the blank, the module is probably shallow.
- In code review, look for the same knowledge in two places: a format, a table layout, a magic string, an order of steps. Ask: "If this changes, how many files change?" See [reviewing code](/learn/reviewing-human-and-ai-code).
- Count the files in a recent small change. If adding one field touched eight files, that is change amplification. Add it to your [tech debt](/learn/technical-debt-management) list with the count.
- When a class just forwards calls to another class with the same arguments, merge them or give the upper one a real job.
- When a caller must "remember to" do something, move that step inside the module.
- Apply the same thinking to [REST APIs](/learn/rest-api-design): a small set of resources that hide your database layout.

## With AI

### What to ask the agent

> "List every place in this repo that reads or writes the invoice CSV format. For each one, show the line that depends on the column order."

> "Find classes whose public methods only call one other method with the same arguments. List them, and say what each one adds, if anything."

> "Propose one module that hides this file format. Show its interface first, with at most three methods. Then list every caller that must change."

### What to check yourself

- The agent's list of places is complete. Search for the format yourself, including SQL, scripts and tests.
- The new interface really is smaller. An agent can add a wrapper layer that just renames things.
- The agent did not split one clear function into many tiny ones to look "clean". Read the result top to bottom.
- Errors that were removed by design are cases with a natural meaning, not bugs that now pass silently.
- Behaviour is the same. Run the old tests before and after the change.

## Check yourself

## Go deeper

Next: [error handling and defensive design](/learn/error-handling-defensive-design). Later lessons on SOLID, coupling and cohesion and on functional programming ideas build on this one. Related: [managing technical debt](/learn/technical-debt-management), [reviewing code written by humans and AI](/learn/reviewing-human-and-ai-code) and [REST API design](/learn/rest-api-design).
