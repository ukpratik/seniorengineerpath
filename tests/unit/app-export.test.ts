import { describe, it, expect } from 'vitest';
import { readFileSync } from 'node:fs';
import { join } from 'node:path';
import { Resvg } from '@resvg/resvg-js';
import {
  buildAppExport,
  lessonSections,
  quizInlines,
  frameCss,
  diagramCss,
  appDiagram,
  DIAGRAM_PALETTE,
  APP_SCHEMA,
  type Section,
  type Theme,
} from '../../src/content-tools/app-export.ts';
import { loadContent, type LessonFile } from '../../src/content-tools/load.ts';
import { lessonSchema } from '../../src/content-tools/schemas.ts';
import { LESSON_SECTIONS } from '../../src/content-tools/markdown.ts';
import { FONT } from '../../src/lib/og-image.ts';

const TERMS = [{ id: 'retry', term: 'retry', forms: ['retries'], short: 'Trying again.' }];

function lesson(body: string): LessonFile {
  const data = lessonSchema.parse({
    topic: 'idem',
    subtitle: 'Why retries can charge twice.',
    status: 'published',
    sources: [
      { id: 'stripe', title: 'Stripe docs' },
      { id: 'aws', title: 'AWS' },
    ],
  });
  return { file: 'idem.md', id: 'idem', data, body, bodyLine: 1 };
}

const BODY = `## What broke

A retry sent the payment twice [@stripe][@aws]. Every retry counts.

[[diagram:cache-stampede]]

## The idea in simple words

> [!key] One **key** per payment.

## How it works

| Option | Safe? |
|---|---|
| Key | Yes. Always |
| None | No |

1. First \`step\`
2. Second

## Trade-offs

Some text.

## At work: how a senior uses this

Ask *what happens* twice. See [docs](https://example.com).

## With AI

Lead text.

### What to ask the agent

> "Find every retry."

### What to check in its answer

- It found the queue.

## Check yourself

## Go deeper

Read more.
`;

const sec = (s: Section[], name: string) => s.find((x) => x.name === name)!;

describe('lessonSections', () => {
  const { sections, glossaryUsed } = lessonSections(lesson(BODY), TERMS);

  it('keeps the eight sections in order with their full titles', () => {
    expect(sections.map((s) => s.name)).toEqual([...LESSON_SECTIONS]);
    expect(sec(sections, 'At work').title).toBe('At work: how a senior uses this');
  });

  it('numbers citations by source order and links only the first use of a term', () => {
    const p = sec(sections, 'What broke').blocks[0];
    expect(p).toEqual({
      t: 'p',
      c: [
        { t: 'text', v: 'A ' },
        { t: 'term', id: 'retry', v: 'retry' },
        { t: 'text', v: ' sent the payment twice ' },
        { t: 'cite', id: 'stripe', n: 1 },
        { t: 'cite', id: 'aws', n: 2 },
        { t: 'text', v: '. Every retry counts.' },
      ],
    });
    expect(glossaryUsed).toEqual(['retry']);
    expect(sec(sections, 'What broke').blocks[1]).toEqual({ t: 'diagram', id: 'cache-stampede' });
  });

  it('turns [!key] quotes into callouts and marks Yes/No table cells', () => {
    expect(sec(sections, 'The idea in simple words').blocks[0]).toEqual({
      t: 'callout',
      kind: 'key',
      blocks: [{ t: 'p', c: [{ t: 'text', v: 'One ' }, { t: 'strong', c: [{ t: 'text', v: 'key' }] }, { t: 'text', v: ' per payment.' }] }],
    });
    const [table, list] = sec(sections, 'How it works').blocks;
    expect(table).toMatchObject({ t: 'table', rows: [[{ c: [{ v: 'Key' }] }, { mark: 'yes', c: [{ v: 'Always' }] }], [{}, { mark: 'no', c: [] }]] });
    expect(list).toMatchObject({ t: 'list', ordered: true, items: [[{ t: 'p', c: [{ v: 'First ' }, { t: 'code', v: 'step' }] }], [{}]] });
  });

  it('keeps links and emphasis', () => {
    expect(sec(sections, 'At work').blocks[0]).toMatchObject({
      c: [{ v: 'Ask ' }, { t: 'em', c: [{ v: 'what happens' }] }, { v: ' twice. See ' }, { t: 'link', url: 'https://example.com', c: [{ v: 'docs' }] }, { v: '.' }],
    });
  });

  it('lays out With AI as ask and check columns after the lead text', () => {
    const blocks = sec(sections, 'With AI').blocks;
    expect(blocks[0]).toEqual({ t: 'p', c: [{ t: 'text', v: 'Lead text.' }] });
    expect(blocks[1]).toMatchObject({
      t: 'ai',
      cols: [
        { kind: 'ask', blocks: [{ t: 'prompt', c: [{ v: '"Find every retry."' }] }] },
        { kind: 'check', blocks: [{ t: 'checks', items: [[{ t: 'p' }]] }] },
      ],
    });
  });

  it('fails on Markdown the app cannot draw', () => {
    expect(() => lessonSections(lesson(BODY.replace('Some text.', '![x](/x.png)')), TERMS)).toThrow(/inline Markdown "image"/);
    expect(() => lessonSections(lesson(BODY.replace('Some text.', '<div>x</div>')), TERMS)).toThrow(/block Markdown "html"/);
    expect(() => lessonSections(lesson(BODY.replace('[@aws]', '[@nope]')), TERMS)).toThrow(/\[@nope\] has no matching source/);
    expect(() => lessonSections(lesson('Intro.\n\n' + BODY), TERMS)).toThrow(/before the first ## section/);
  });
});

describe('quizInlines', () => {
  it('turns backtick spans into code runs', () => {
    expect(quizInlines('Which `a/b` file?')).toEqual([
      { t: 'text', v: 'Which ' },
      { t: 'code', v: 'a/b' },
      { t: 'text', v: ' file?' },
    ]);
  });
});

describe('diagram styles', () => {
  it('frameCss hides later frames and highlights the current one (the Kotlin port copies this format)', () => {
    expect(frameCss(2, 4, 'light')).toBe(
      '.f3,.f4{display:none}\n.f2 .ln{stroke:#4F46E5;stroke-width:3.5;marker-end:url(#ahA)}\n.f2 .lb{fill:#3730A3}',
    );
    expect(frameCss(4, 4, 'dark')).not.toContain('display:none');
  });

  it('palette matches the --dg-* tokens in tokens.css, in both themes', () => {
    const css = readFileSync(join(process.cwd(), 'src/styles/tokens.css'), 'utf8');
    const vars = (block: string) => Object.fromEntries([...block.matchAll(/--([\w-]+):\s*([^;]+);/g)].map((m) => [m[1], m[2].trim()]));
    const light = vars(/:root\{([\s\S]*?)\n\}/.exec(css)![1]);
    const dark = { ...light, ...vars(/:root\[data-theme="dark"\]\{([\s\S]*?)\n\}/.exec(css)![1]) };
    const resolve = (v: Record<string, string>, name: string): string => {
      const x = v[name];
      const ref = /^var\(--([\w-]+)\)$/.exec(x);
      return ref ? resolve(v, ref[1]) : x;
    };
    const token = (k: string) => (k === 'site' ? 'text-3' : 'dg-' + k.replace(/[A-Z]/g, (c) => '-' + c.toLowerCase()));
    for (const [theme, v] of [['light', light], ['dark', dark]] as const) {
      for (const [k, hex] of Object.entries(DIAGRAM_PALETTE[theme])) expect([theme, k, resolve(v, token(k))]).toEqual([theme, k, hex]);
    }
  });
});

describe('the real export', () => {
  const content = loadContent();
  const { files } = buildAppExport(content);
  const manifest = files.get('manifest.json') as { schema: number; lessons: string[]; contentHash: string; topics: Record<string, { written: boolean }> };

  it('has every published lesson and a stable hash', () => {
    const published = content.lessons.filter((l) => l.data.status === 'published').map((l) => l.id);
    expect(manifest.schema).toBe(APP_SCHEMA);
    expect(manifest.lessons).toEqual([...published].sort());
    for (const id of published) expect(files.has(`lessons/${id}.json`)).toBe(true);
    expect(Object.values(manifest.topics).filter((t) => t.written)).toHaveLength(published.length);
    expect((buildAppExport(loadContent()).files.get('manifest.json') as { contentHash: string }).contentHash).toBe(manifest.contentHash);
  });

  // Every frame of every diagram, in both themes, must draw: a broken SVG would show a blank box in the app.
  it('renders every diagram frame in both themes', () => {
    const diagrams = [...files].filter(([k]) => k.startsWith('diagrams/')).map(([, v]) => v as ReturnType<typeof appDiagram>);
    expect(diagrams.length).toBeGreaterThan(0);
    const ink = (svg: string) => {
      const png = new Resvg(svg, { font: { fontFiles: [FONT], loadSystemFonts: false, defaultFontFamily: 'Plus Jakarta Sans' }, fitTo: { mode: 'width', value: 170 } }).render();
      const px = png.pixels; // a getter that copies the buffer: read it once
      let n = 0;
      for (let i = 3; i < px.length; i += 4) if (px[i] > 0) n++;
      return n;
    };
    const styled = (d: (typeof diagrams)[number], k: number, theme: Theme) =>
      d.svg.replace('<defs>', `<style>${diagramCss(theme)}\n${frameCss(k, d.frames, theme)}</style><defs>`);
    for (const d of diagrams) {
      for (const theme of ['light', 'dark'] as const) {
        const first = ink(styled(d, 1, theme));
        const last = ink(styled(d, d.frames, theme));
        expect(first, `${d.id} ${theme}`).toBeGreaterThan(200);
        expect(last, `${d.id} ${theme}: last frame shows more than the first`).toBeGreaterThan(first);
      }
    }
  }, 60_000);
});
