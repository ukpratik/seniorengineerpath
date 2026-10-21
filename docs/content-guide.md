# Content guide (for anyone writing or checking a lesson)

> How lessons are written and checked. Updated: 2026-10-21

The site is SeniorPath, for software engineers with 2–3 years of experience who want to work like seniors. Readers are often not native English speakers and read on phones. The lesson file format is enforced by the content gate (`npm run gate`).

## 1. Language rules (NFR-006)
- Short sentences. Average ≤ 20 words. **Never more than 35 words** (build fails).
- Common words. One idea per paragraph. No idioms, no hype. See `src/content/banned-phrases.txt` (build fails on these).
- Use one term for one idea, always the same one. Jargon is fine if it is in the glossary: the build links its first use automatically.
- Speak to the reader as "you". Be concrete: numbers, names of real tools, real situations.
- Target reading grade ≤ 9 (warning above 10).

## 2. Lesson file
`src/content/lessons/<topic-id>.md`. The topic must exist in `src/content/topics/<topic-id>.yaml`.

Front matter:
```yaml
topic: <topic-id>
subtitle: One sentence promise.
status: draft            # published only after the fact-check
illustrative_story: true # true if "What broke" is a made-up example; false if it is a real, cited event
sources:                 # ≥ 2. Original sources: official docs, papers, books (with chapter), postmortems
  - id: short-id
    title: "Author, Title, Publisher"
    url: https://...     # or `where:` for a book chapter
    note: One line on what the reader will find there.
claims_checked: []       # filled by the fact-checker: [{claim, source, note}] ≥ 3
quiz:                    # 3–5 questions
  - q: Question?
    options: [A, B, C]
    answer: 2            # index, from 0
    why: One-line explanation.
incidents: []            # ids from src/content/incidents (must be verified)
related: []              # topic ids
```

Body: exactly these 8 H2 sections, in this order:
1. `## What broke` — a short real or clearly made-up failure story (3–6 short paragraphs). Hook first.
2. `## The idea in simple words` — the concept, plainly. Optionally one `> [!key] **The key idea.** ...` callout.
3. `## How it works` — the mechanism. Use `[[diagram:<id>]]` for a step-through diagram (file in `src/content/diagrams/<id>.yaml`).
4. `## Trade-offs` — a Markdown table. First column = option name. Cells may start with `Yes.` / `No.`.
5. `## At work: how a senior uses this` — bullet list of concrete habits and questions.
6. `## With AI` — `### What to ask the agent` with one `>` quote per prompt, then `### What to check yourself` with a bullet list.
7. `## Check yourself` — leave empty (the quiz comes from front matter).
8. `## Go deeper` — optional extra lines (related lessons). Sources are listed automatically.

Cite facts inline with `[@source-id]`. Every non-obvious fact, number or quote needs a citation.
Internal links: `/learn/<topic-slug>`, `/glossary#<term-id>`, `/incidents#<id>`.

Length: a 10–15 minute read (about 1,200–1,800 words).

## 3. Diagrams
`src/content/diagrams/<id>.yaml`: `title`, `viewBox`, `svg` (inner SVG), `captions` (one per frame). Frames are `<g class="f" data-f="1">…</g>`; parts outside frames are always visible. Reuse the classes from the idempotency diagram: `box`, `bt`, `life`, `ln` (arrow), `lost`, `lb` (label), `sub`, `rec`/`rec-t`, `note`/`note-t`, `okb`/`ok-t`, `xm`, `badt`. Keep text as real `<text>`; width ≈ 340 for phones.

## 4. Glossary
`src/content/glossary/<id>.yaml`: `id`, `term`, `forms` (plurals and variants), `short` (1–2 plain sentences, ≤ 300 chars). Add a term for each new piece of jargon a lesson uses.

## 5. Incidents and case studies
`src/content/incidents/<id>.yaml`: `title`, `date`, `company`, `broke`, `why`, `lesson` (one line each), `url` (the official postmortem), `tags`, `lessons` (topic ids), `verified` (true only after the fact-check read the official source).

## 6. Fact-check
A separate agent that did not write the lesson:
1. Reads the lesson and opens every source.
2. Checks every factual claim, number, date, quote and every quiz answer and "why".
3. Lists problems with a fix. The writer fixes them.
4. Records ≥ 3 `claims_checked` entries (the most important claims, each with its source id), and sets `reviewed_by: ai-factcheck`, `reviewed_on: <date>`. For AI-track lessons also `review_by` (≤ 92 days later).
5. Only then: `status: published`.

## 7. Originality
Write in your own words. Never copy text from books, courses or blogs beyond a short quote in quotation marks with a citation. Never redraw a figure from a book.
