// Content for the Android app (ADR-007, 09-android/architecture.md §3).
// Turns the gated lessons into JSON with a small, closed block set, and the
// diagrams into standalone SVG + CSS. Any Markdown the app can't draw throws,
// so a lesson never reaches the app half-rendered.
import { createHash } from 'node:crypto';
import { visit, SKIP } from 'unist-util-visit';
import type { Root, RootContent, PhrasingContent, Paragraph, Text, Heading, TableCell } from 'mdast';
import { parseMarkdown, CITE_RE, DIAGRAM_RE, LESSON_SECTIONS, sectionName, headingText } from './markdown.ts';
import { linkGlossary } from './remark-glossary.ts';
import { frameCount } from '../lib/stepper.ts';
import { ANIMS } from '../lib/anims.ts';
import type { Content, LessonFile } from './load.ts';

// Bump when the JSON shape changes; the app refuses a schema it doesn't know.
export const APP_SCHEMA = 1;

export type Inline =
  | { t: 'text'; v: string }
  | { t: 'strong' | 'em'; c: Inline[] }
  | { t: 'code'; v: string }
  | { t: 'link'; url: string; c: Inline[] }
  | { t: 'term'; id: string; v: string }
  | { t: 'cite'; id: string; n: number }
  | { t: 'br' };

export interface Cell {
  c: Inline[];
  mark?: 'yes' | 'no';
}

export interface AiColumn {
  kind: 'ask' | 'check';
  title: Inline[];
  blocks: Block[];
}

export type Block =
  | { t: 'p'; c: Inline[] }
  | { t: 'h'; depth: number; c: Inline[] }
  | { t: 'list'; ordered: boolean; start?: number; items: Block[][] }
  | { t: 'code'; lang?: string; v: string }
  | { t: 'table'; head: Inline[][]; rows: Cell[][] }
  | { t: 'callout'; kind: 'key'; blocks: Block[] }
  | { t: 'quote'; blocks: Block[] }
  | { t: 'diagram'; id: string }
  | { t: 'anim'; id: string }
  | { t: 'prompt'; c: Inline[] }
  | { t: 'checks'; items: Block[][] }
  | { t: 'ai'; cols: AiColumn[] };

export interface Section {
  n: number;
  name: string;
  title: string;
  blocks: Block[];
}

class ExportError extends Error {}

const fail = (lesson: string, msg: string): never => {
  throw new ExportError(`${lesson}: ${msg}`);
};

// ---------- Markdown → blocks ----------

// Same order as the site's plugins: markers and callouts first, then citations, then glossary.
function prepare(tree: Root, l: LessonFile, terms: Content['glossary']): string[] {
  visit(tree, 'paragraph', (p: Paragraph, i, parent) => {
    if (p.children.length !== 1 || p.children[0].type !== 'text' || !parent || i === undefined) return;
    const m = DIAGRAM_RE.exec(p.children[0].value.trim());
    if (!m) return;
    parent.children[i] = { type: m[1] === 'anim' ? 'appAnim' : 'appDiagram', id: m[2] } as never;
    return SKIP;
  });
  const order = l.data.sources.map((s) => s.id);
  visit(tree, 'text', (t: Text, i, parent) => {
    if (!parent || i === undefined || !new RegExp(CITE_RE.source).test(t.value)) return;
    const out: unknown[] = [];
    let last = 0;
    for (const m of t.value.matchAll(CITE_RE)) {
      const n = order.indexOf(m[1]) + 1;
      if (n === 0) fail(l.id, `citation [@${m[1]}] has no matching source`);
      if (m.index! > last) out.push({ type: 'text', value: t.value.slice(last, m.index) });
      out.push({ type: 'appCite', id: m[1], n });
      last = m.index! + m[0].length;
    }
    if (last < t.value.length) out.push({ type: 'text', value: t.value.slice(last) });
    parent.children.splice(i, 1, ...(out as never[]));
    return i + out.length;
  });
  return linkGlossary(tree, terms, l.data.glossary_optout);
}

function inlines(nodes: PhrasingContent[], lesson: string): Inline[] {
  const out: Inline[] = [];
  for (const n of nodes as (PhrasingContent | { type: 'appCite'; id: string; n: number })[]) {
    switch (n.type) {
      case 'text':
        if (n.value) out.push({ t: 'text', v: n.value });
        break;
      case 'strong':
        out.push({ t: 'strong', c: inlines(n.children, lesson) });
        break;
      case 'emphasis':
        out.push({ t: 'em', c: inlines(n.children, lesson) });
        break;
      case 'inlineCode':
        out.push({ t: 'code', v: n.value });
        break;
      case 'break':
        out.push({ t: 'br' });
        break;
      case 'appCite':
        out.push({ t: 'cite', id: n.id, n: n.n });
        break;
      case 'link': {
        const term = /^\/glossary#([a-z0-9-]+)$/.exec(n.url);
        if (term && (n.data as { hProperties?: { 'data-term'?: string } })?.hProperties?.['data-term']) {
          out.push({ t: 'term', id: term[1], v: n.children.map((c) => (c as Text).value).join('') });
        } else {
          out.push({ t: 'link', url: n.url, c: inlines(n.children, lesson) });
        }
        break;
      }
      default:
        fail(lesson, `inline Markdown "${n.type}" is not supported in the app`);
    }
  }
  return out;
}

function tableCell(cell: TableCell, lesson: string): Cell {
  const c = inlines(cell.children, lesson);
  // "Yes …" / "No …" cells get a mark, as on the site (decorateTable).
  const first = c[0];
  if (first?.t === 'text') {
    const m = /^(Yes|No)\b[.:]?\s*/.exec(first.v);
    if (m) {
      first.v = first.v.slice(m[0].length);
      if (!first.v) c.shift();
      return { c, mark: m[1] === 'Yes' ? 'yes' : 'no' };
    }
  }
  return { c };
}

function block(n: RootContent, lesson: string): Block {
  const node = n as RootContent | { type: 'appDiagram' | 'appAnim'; id: string };
  switch (node.type) {
    case 'paragraph':
      return { t: 'p', c: inlines(node.children, lesson) };
    case 'heading':
      return { t: 'h', depth: node.depth, c: inlines(node.children, lesson) };
    case 'list':
      return {
        t: 'list',
        ordered: !!node.ordered,
        ...(node.ordered && node.start != null && node.start !== 1 ? { start: node.start } : {}),
        items: node.children.map((li) => li.children.map((c) => block(c, lesson))),
      };
    case 'code':
      return { t: 'code', ...(node.lang ? { lang: node.lang } : {}), v: node.value };
    case 'table': {
      const [head, ...rows] = node.children;
      return {
        t: 'table',
        head: head.children.map((c) => inlines(c.children, lesson)),
        rows: rows.map((r) => r.children.map((c) => tableCell(c, lesson))),
      };
    }
    case 'blockquote': {
      const first = node.children[0];
      const lead = first?.type === 'paragraph' && first.children[0]?.type === 'text' ? (first.children[0] as Text) : undefined;
      const m = lead ? /^\[!(key)\]\s*/.exec(lead.value) : null;
      if (m && lead) {
        lead.value = lead.value.slice(m[0].length);
        if (!lead.value) (first as Paragraph).children.shift();
        return { t: 'callout', kind: 'key', blocks: node.children.map((c) => block(c, lesson)) };
      }
      return { t: 'quote', blocks: node.children.map((c) => block(c, lesson)) };
    }
    case 'appDiagram':
      return { t: 'diagram', id: node.id };
    case 'appAnim':
      if (!ANIMS.includes(node.id)) fail(lesson, `unknown animation "${node.id}"`);
      return { t: 'anim', id: node.id };
    default:
      return fail(lesson, `block Markdown "${node.type}" is not supported in the app`);
  }
}

// "With AI": h3 "What to ask…" + quotes, h3 "What to check…" + list → columns (site: aiLayout).
function aiBlocks(nodes: RootContent[], lesson: string): Block[] {
  const lead: Block[] = [];
  const cols: AiColumn[] = [];
  let cur: AiColumn | null = null;
  for (const n of nodes) {
    if (n.type === 'heading' && n.depth === 3) {
      cur = { kind: /ask/i.test(headingText(n)) ? 'ask' : 'check', title: inlines(n.children, lesson), blocks: [] };
      cols.push(cur);
    } else if (!cur) {
      lead.push(block(n, lesson));
    } else if (n.type === 'blockquote') {
      for (const c of n.children) {
        if (c.type === 'paragraph') cur.blocks.push({ t: 'prompt', c: inlines(c.children, lesson) });
        else cur.blocks.push(block(c, lesson));
      }
    } else if (n.type === 'list' && !n.ordered) {
      cur.blocks.push({ t: 'checks', items: n.children.map((li) => li.children.map((c) => block(c, lesson))) });
    } else {
      cur.blocks.push(block(n, lesson));
    }
  }
  return cols.length ? [...lead, { t: 'ai', cols }] : lead;
}

export function lessonSections(l: LessonFile, terms: Content['glossary']): { sections: Section[]; glossaryUsed: string[] } {
  const tree = parseMarkdown(l.body);
  const glossaryUsed = prepare(tree, l, terms);
  const sections: Section[] = [];
  let body: RootContent[] | null = null;
  const flush = () => {
    const s = sections[sections.length - 1];
    if (s && body) s.blocks = s.name === 'With AI' ? aiBlocks(body, l.id) : body.map((n) => block(n, l.id));
  };
  for (const n of tree.children) {
    if (n.type === 'heading' && n.depth === 2) {
      flush();
      const title = headingText(n as Heading);
      sections.push({ n: sections.length + 1, name: sectionName(title), title, blocks: [] });
      body = [];
    } else if (!body) {
      fail(l.id, 'content before the first ## section is not supported in the app');
    } else {
      body.push(n);
    }
  }
  flush();
  for (const s of sections) {
    if (!(LESSON_SECTIONS as readonly string[]).includes(s.name)) fail(l.id, `unknown section "${s.name}"`);
  }
  return { sections, glossaryUsed };
}

// Quiz text is plain front matter; `code` spans are its only markup (site: inlineCode).
export function quizInlines(s: string): Inline[] {
  return s
    .split(/(`[^`]+`)/)
    .filter(Boolean)
    .map((p) => (/^`[^`]+`$/.test(p) ? { t: 'code', v: p.slice(1, -1) } : { t: 'text', v: p }));
}

export function plainText(x: Inline[] | Block[] | Section[]): string {
  const parts: string[] = [];
  const walk = (v: unknown): void => {
    if (Array.isArray(v)) return v.forEach(walk);
    if (!v || typeof v !== 'object') return;
    const o = v as Record<string, unknown>;
    if (o.t === 'cite') return;
    if (typeof o.v === 'string') parts.push(o.v);
    for (const k of ['c', 'blocks', 'items', 'head', 'rows', 'cols'] as const) walk(o[k]);
    if (Array.isArray(o.title)) walk(o.title);
  };
  walk(x);
  return parts.join(' ').replace(/\s+/g, ' ').trim();
}

// ---------- Diagrams ----------

const DEFS =
  '<defs><marker id="ah" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="4.5" markerHeight="4.5" orient="auto-start-reverse"><path class="mk" d="M0 0 10 5 0 10z"/></marker>' +
  '<marker id="ahA" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="3.6" markerHeight="3.6" orient="auto-start-reverse"><path class="mkA" d="M0 0 10 5 0 10z"/></marker></defs>';

// Resolved from tokens.css (the --dg-* tokens). Keep in step with it.
export const DIAGRAM_PALETTE = {
  light: { bg: '#EEF2F7', node: '#FFFFFF', nodeStroke: '#7B8799', text: '#0F172A', sub: '#475569', life: '#7B8799', arrow: '#475569', arrowActive: '#4F46E5', labelActive: '#3730A3', rec: '#ECEEFF', recStroke: '#4F46E5', recText: '#3730A3', ok: '#E2F5EC', okStroke: '#047857', okText: '#047857', warn: '#FDF1E1', warnStroke: '#9A4A06', warnText: '#9A4A06', bad: '#B91C1C', site: '#556073' },
  dark: { bg: '#1A2336', node: '#131A2B', nodeStroke: '#5D6B87', text: '#E7ECF5', sub: '#AAB5C8', life: '#5D6B87', arrow: '#AAB5C8', arrowActive: '#8F88FF', labelActive: '#C9C5FF', rec: '#1F2250', recStroke: '#8F88FF', recText: '#C9C5FF', ok: '#0E2A21', okStroke: '#4ADE9A', okText: '#4ADE9A', warn: '#2B2113', warnStroke: '#F5B454', warnText: '#F5B454', bad: '#F87171', site: '#98A4B9' },
} as const;

export type Theme = keyof typeof DIAGRAM_PALETTE;

// The app.css diagram rules with colours filled in. Class selectors only, so AndroidSVG can apply them.
export function diagramCss(theme: Theme): string {
  const p = DIAGRAM_PALETTE[theme];
  return [
    `text{font-family:'Plus Jakarta Sans',sans-serif;fill:${p.text};font-size:12px}`,
    `.box{fill:${p.node};stroke:${p.nodeStroke};stroke-width:1.5}`,
    `.bt{font-weight:800;font-size:13px;text-anchor:middle}`,
    `.life{stroke:${p.life};stroke-width:1.5;stroke-dasharray:3 5}`,
    `.ln{stroke:${p.arrow};stroke-width:2.5;fill:none;marker-end:url(#ah)}`,
    `.lost{stroke-dasharray:6 5}`,
    `.lb{font-weight:800;text-anchor:middle;font-size:13.5px}`,
    `.sub{font-size:12px;fill:${p.sub};text-anchor:middle}`,
    `.badt{fill:${p.bad};font-weight:700}`,
    `.xm{stroke:${p.bad};stroke-width:3;stroke-linecap:round}`,
    `.note{fill:${p.warn};stroke:${p.warnStroke};stroke-width:1.5}`,
    `.note-t{fill:${p.warnText};font-weight:800;text-anchor:middle}`,
    `.rec{fill:${p.rec};stroke:${p.recStroke};stroke-width:1.5}`,
    `.rec-t{fill:${p.recText};font-weight:700;text-anchor:middle}`,
    `.okb{fill:${p.ok};stroke:${p.okStroke};stroke-width:2}`,
    `.ok-t{fill:${p.okText};font-weight:800;text-anchor:middle;font-size:13px}`,
    `.mk{fill:${p.arrow}}`,
    `.mkA{fill:${p.arrowActive}}`,
  ].join('\n');
}

// Frame k of n (1-based): later frames hidden, frame k highlighted (site: .later / .now).
// The app builds the same CSS in Kotlin; tests/unit/app-export.test.ts pins the format.
export function frameCss(k: number, n: number, theme: Theme): string {
  const p = DIAGRAM_PALETTE[theme];
  const hide = Array.from({ length: n - k }, (_, i) => `.f${k + i + 1}`);
  return [
    hide.length ? `${hide.join(',')}{display:none}` : '',
    `.f${k} .ln{stroke:${p.arrowActive};stroke-width:3.5;marker-end:url(#ahA)}`,
    `.f${k} .lb{fill:${p.labelActive}}`,
  ]
    .filter(Boolean)
    .join('\n');
}

export interface AppDiagram {
  id: string;
  title: string;
  viewBox: string;
  frames: number;
  captions: string[];
  svg: string;
}

export function appDiagram(d: Content['diagrams'][number]): AppDiagram {
  const frames = frameCount(d.svg);
  if (frames !== d.captions.length) throw new ExportError(`diagram ${d.id}: ${frames} frames but ${d.captions.length} captions`);
  const inner = d.svg.trim().replace(/class="f" data-f="(\d+)"/g, 'class="f f$1" data-f="$1"');
  if (/class="f"/.test(inner)) throw new ExportError(`diagram ${d.id}: a frame group has attributes in an unexpected order`);
  const svg = `<svg xmlns="http://www.w3.org/2000/svg" viewBox="${d.viewBox}">${DEFS}${inner}</svg>`;
  return { id: d.id, title: d.title, viewBox: d.viewBox, frames, captions: d.captions, svg };
}

// ---------- Everything ----------

export interface AppExport {
  files: Map<string, unknown>; // path under assets/content → JSON value
}

export function readingMinutes(sections: Section[]): number {
  const words = plainText(sections).split(/\s+/).filter(Boolean).length;
  return Math.max(3, Math.round(words / 160)); // same pace as the site
}

export function buildAppExport(c: Content): AppExport {
  const files = new Map<string, unknown>();
  const published = c.lessons.filter((l) => l.data.status === 'published');
  const written = new Set(published.map((l) => l.id));
  const topicById = new Map(c.topics.map((t) => [t.id, t]));
  const search: { type: string; id: string; title: string; text: string; body?: string }[] = [];
  const lessonMeta = new Map<string, { subtitle: string; readingMinutes: number }>();

  for (const l of published) {
    const topic = topicById.get(l.data.topic) ?? fail(l.id, 'topic missing');
    const { sections, glossaryUsed } = lessonSections(l, c.glossary);
    const minutes = l.data.readingMinutes ?? readingMinutes(sections);
    lessonMeta.set(l.id, { subtitle: l.data.subtitle, readingMinutes: minutes });
    files.set(`lessons/${l.id}.json`, {
      id: l.id,
      slug: topic.slug,
      title: topic.title,
      subtitle: l.data.subtitle,
      level: topic.level,
      track: topic.track,
      module: topic.module,
      readingMinutes: minutes,
      illustrativeStory: l.data.illustrative_story,
      reviewedBy: l.data.reviewed_by,
      reviewedOn: l.data.reviewed_on,
      ...(l.data.major_revised_on ? { majorRevisedOn: l.data.major_revised_on } : {}),
      sections,
      quiz: l.data.quiz.map((q) => ({
        q: quizInlines(q.q),
        options: q.options.map(quizInlines),
        answer: q.answer,
        why: quizInlines(q.why),
      })),
      sources: l.data.sources,
      incidents: l.data.incidents,
      cases: l.data.cases,
      related: l.data.related,
      glossaryUsed,
    });
    search.push({ type: 'lesson', id: l.id, title: topic.title, text: l.data.subtitle, body: plainText(sections) });
  }

  // Curriculum order: tracks.yaml order; in a module, written first, then level, then title (site-data.ts).
  const tracks = c.tracks.map((t, i) => ({
    id: t.id,
    title: t.title,
    short: t.short,
    index: i + 1,
    modules: t.modules.map((m) => ({
      id: m.id,
      title: m.title,
      topics: c.topics
        .filter((x) => x.track === t.id && x.module === m.id)
        .sort((a, b) => Number(written.has(b.id)) - Number(written.has(a.id)) || a.level.localeCompare(b.level) || a.title.localeCompare(b.title))
        .map((x) => x.id),
    })),
  }));
  const topics = Object.fromEntries(
    c.topics.map((t) => [
      t.id,
      {
        slug: t.slug,
        title: t.title,
        oneLiner: t.oneLiner,
        level: t.level,
        track: t.track,
        module: t.module,
        prereqs: t.prereqs,
        ...(t.link ? { link: t.link } : {}),
        written: written.has(t.id),
        ...(lessonMeta.get(t.id) ?? {}),
      },
    ]),
  );
  for (const t of c.topics) if (!written.has(t.id)) search.push({ type: 'topic', id: t.id, title: t.title, text: t.oneLiner });
  const paths = [...c.paths].sort((a, b) => Number(b.default) - Number(a.default) || a.title.localeCompare(b.title));

  files.set('glossary.json', c.glossary);
  files.set('incidents.json', c.incidents);
  files.set('cases.json', c.cases);
  for (const g of c.glossary) search.push({ type: 'glossary', id: g.id, title: g.term, text: g.short });
  for (const i of c.incidents) search.push({ type: 'incident', id: i.id, title: `${i.company}: ${i.title}`, text: i.broke });
  for (const k of c.cases) search.push({ type: 'case', id: k.id, title: `${k.company}: ${k.title}`, text: k.lesson });
  files.set('search.json', search);

  // Only diagrams that a published lesson uses.
  const used = new Set<string>();
  for (const [path, v] of files) {
    if (!path.startsWith('lessons/')) continue;
    JSON.stringify((v as { sections: Section[] }).sections, (_k, x) => {
      if (x && x.t === 'diagram') used.add(x.id);
      return x;
    });
  }
  for (const id of [...used].sort()) {
    const d = c.diagrams.find((x) => x.id === id);
    if (!d) throw new ExportError(`unknown diagram "${id}"`);
    files.set(`diagrams/${id}.json`, appDiagram(d));
  }
  files.set('diagram-style.json', { light: diagramCss('light'), dark: diagramCss('dark'), palette: DIAGRAM_PALETTE });

  const hash = createHash('sha256');
  for (const [k, v] of [...files].sort(([a], [b]) => a.localeCompare(b))) hash.update(k).update(JSON.stringify(v));
  files.set('manifest.json', {
    schema: APP_SCHEMA,
    contentHash: hash.digest('hex').slice(0, 16),
    lessonCount: published.length,
    lessons: published.map((l) => l.id).sort(),
    tracks,
    topics,
    paths,
    anims: ANIMS,
  });
  return { files };
}
