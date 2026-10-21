import { describe, it, expect } from 'vitest';
import { unified } from 'unified';
import remarkParse from 'remark-parse';
import remarkGfm from 'remark-gfm';
import remarkRehype from 'remark-rehype';
import rehypeRaw from 'rehype-raw';
import rehypeStringify from 'rehype-stringify';
import { VFile } from 'vfile';
import { remarkLesson, rehypeLesson } from '../../src/content-tools/lesson-plugins.ts';
import { remarkGlossary } from '../../src/content-tools/remark-glossary.ts';
import type { Diagram } from '../../src/lib/stepper.ts';

const diagram: Diagram = {
  id: 'demo',
  title: 'Demo flow',
  viewBox: '0 0 100 100',
  svg: '<g class="f" data-f="1"><text>a</text></g><g class="f" data-f="2"><text>b</text></g>',
  captions: ['First step.', 'Second step.'],
};

// Same order as Astro's unified() processor: user rehype plugins run before rehype-raw.
async function render(md: string, frontmatter: Record<string, unknown> = {}) {
  const file = new VFile({ value: md, path: 'lesson.md', data: { astro: { frontmatter } } });
  const out = await unified()
    .use(remarkParse)
    .use(remarkGfm)
    .use(remarkLesson, { diagram: (id: string) => (id === 'demo' ? diagram : undefined) })
    .use(remarkGlossary, { terms: () => [{ id: 'retry', term: 'retry', forms: [], short: 'Try again.' }] })
    .use(remarkRehype, { allowDangerousHtml: true })
    .use(rehypeLesson)
    .use(rehypeRaw)
    .use(rehypeStringify)
    .process(file);
  return { html: String(out), fm: file.data.astro!.frontmatter! };
}

const LESSON = `## What broke

The app did a retry.

## How it works

Intro text.

[[diagram:demo]]

After the diagram.

## Trade-offs

| Approach | Safe? | Cost |
|---|---|---|
| Key | Yes. Repeats are safe. | Storage |
| No key | No: double charge. | None |

## With AI

### What to ask the agent

> Add a key header.

### What to check yourself

- The key is made once.

## Check yourself

## Go deeper

More reading.
`;

describe('lesson plugins', () => {
  it('wraps each H2 in a numbered section card', async () => {
    const { html } = await render(LESSON);
    expect(html).toContain('<section class="sec" id="s1" aria-labelledby="h-s1" data-section="What broke">');
    expect(html).toContain('<small>Section 2 of 6</small>');
    expect(html).toContain('<h2 id="h-s2">How it works</h2>');
  });

  it('keeps diagrams outside the prose and renders all frames', async () => {
    const { html } = await render(LESSON);
    expect(html).toMatch(/<div class="prose"><p>Intro text.<\/p><\/div><figure class="stepper"/);
    expect(html).toContain('<li>Second step.</li>');
    expect(html).toContain('<p>After the diagram.</p>');
  });

  it('turns tables into labelled trade-off tables', async () => {
    const { html } = await render(LESSON);
    expect(html).toContain('<table class="to">');
    expect(html).toContain('<th scope="row">Key</th>');
    expect(html).toContain('data-label="Safe?"><span class="yes">');
    expect(html).toContain('<span class="no">');
  });

  it('lays out the With AI section as prompts and checks', async () => {
    const { html } = await render(LESSON);
    expect(html).toContain('<div class="ai">');
    expect(html).toContain('<p class="prompt">Add a key header.</p>');
    expect(html).toContain('<ul class="checks">');
  });

  it('keeps intro text before the first With AI sub-heading (QA2-01)', async () => {
    const md = LESSON.replace('## With AI\n\n', '## With AI\n\nAn intro paragraph with a fact.\n\n');
    const { html } = await render(md);
    expect(html).toMatch(/<div class="prose"><p>An intro paragraph with a fact.<\/p><\/div><div class="ai">/);
  });

  it('adds slots for incidents, the quiz and sources', async () => {
    const { html } = await render(LESSON);
    for (const s of ['incidents', 'quiz', 'deeper']) expect(html).toContain(`data-slot="${s}"`);
  });

  it('numbers citations by source order', async () => {
    const md = '## What broke\n\nA fact [@b]. Another [@a].\n';
    const { html } = await render(md, { sources: [{ id: 'a' }, { id: 'b' }] });
    expect(html).toContain('<sup><a href="#src-b" aria-label="Source 2">2</a></sup>');
    expect(html).toContain('<sup><a href="#src-a" aria-label="Source 1">1</a></sup>');
  });

  it('fails on a citation without a source', async () => {
    await expect(render('## What broke\n\nA fact [@missing].\n', { sources: [] })).rejects.toThrow(/no matching source/);
  });

  it('turns [!key] quotes into key-idea callouts', async () => {
    const { html } = await render('## What broke\n\n> [!key] **The key idea.** A timeout is not a failure.\n');
    expect(html).toMatch(/<div class="key" data-callout="key"><svg class="i"[^>]*><use href="\/icons.svg#i-bulb"><\/use><\/svg>\s*<p><strong>The key idea.<\/strong> A timeout is not a failure.<\/p>/);
  });

  it('links glossary terms and records which ones were used', async () => {
    const { html, fm } = await render(LESSON);
    expect(html).toContain('<a href="/glossary#retry" class="term" data-term="retry">retry</a>');
    expect(fm.glossaryUsed).toEqual(['retry']);
  });
});
