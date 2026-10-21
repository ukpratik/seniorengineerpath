// Turn lesson Markdown into the designed lesson markup (design-system.md, lesson.html).
// remarkLesson: citations [@id], callouts > [!key], diagram markers [[diagram:id]].
// rehypeLesson: section cards, trade-off tables, the "With AI" layout and slots
// that the lesson page fills (incidents, quiz, sources).
import { visit, SKIP } from 'unist-util-visit';
import { h } from 'hastscript';
import type { Root as MdRoot, Paragraph, Blockquote, Text } from 'mdast';
import type { Root as HRoot, Element, ElementContent, RootContent } from 'hast';
import type { VFile } from 'vfile';
import { CITE_RE, DIAGRAM_RE, LESSON_SECTIONS, sectionName } from './markdown.ts';
import { renderStepper, type Diagram } from '../lib/stepper.ts';
import { ANIMS, renderAnimHost } from '../lib/anims.ts';

export const icon = (name: string, small = false): Element =>
  h('svg', { class: small ? 'i sm' : 'i', 'aria-hidden': 'true' }, [h('use', { href: `/icons.svg#i-${name}` })]);

const SECTION_ICONS = ['zap', 'bulb', 'steps', 'scale', 'briefcase', 'sparkles', 'help', 'book'];

// ---------- remark ----------

export function remarkLesson(options: { diagram: (id: string) => Diagram | undefined }) {
  return (tree: MdRoot, file: VFile) => {
    const fm = (file.data.astro?.frontmatter ?? {}) as { sources?: { id: string }[] };
    const order = (fm.sources ?? []).map((s) => s.id);

    // Diagram markers become stepper HTML.
    visit(tree, 'paragraph', (p: Paragraph, i, parent) => {
      if (p.children.length !== 1 || p.children[0].type !== 'text' || !parent || i === undefined) return;
      const m = DIAGRAM_RE.exec((p.children[0] as Text).value.trim());
      if (!m) return;
      if (m[1] === 'anim') {
        if (!ANIMS.includes(m[2])) throw new Error(`Unknown animation "${m[2]}" in ${file.path}`);
        parent.children[i] = { type: 'html', value: renderAnimHost(m[2]) };
        return SKIP;
      }
      const d = options.diagram(m[2]);
      if (!d) throw new Error(`Unknown diagram "${m[2]}" in ${file.path}`);
      parent.children[i] = { type: 'html', value: renderStepper(d) };
      return SKIP;
    });

    // > [!key] callouts.
    visit(tree, 'blockquote', (b: Blockquote) => {
      const first = b.children[0];
      if (first?.type !== 'paragraph' || first.children[0]?.type !== 'text') return;
      const t = first.children[0] as Text;
      const m = /^\[!(key)\]\s*/.exec(t.value);
      if (!m) return;
      t.value = t.value.slice(m[0].length);
      b.data = { hName: 'div', hProperties: { className: ['key'], 'data-callout': m[1] } };
    });

    // [@source-id] → numbered superscript link.
    visit(tree, 'text', (t: Text, i, parent) => {
      if (!parent || i === undefined || !CITE_RE.test(t.value)) return;
      CITE_RE.lastIndex = 0;
      const out: (Text | { type: 'html'; value: string })[] = [];
      let last = 0;
      for (const m of t.value.matchAll(CITE_RE)) {
        const n = order.indexOf(m[1]) + 1;
        if (n === 0) throw new Error(`Citation [@${m[1]}] has no matching source in ${file.path}`);
        if (m.index! > last) out.push({ type: 'text', value: t.value.slice(last, m.index) });
        out.push({ type: 'html', value: `<sup><a href="#src-${m[1]}" aria-label="Source ${n}">${n}</a></sup>` });
        last = m.index! + m[0].length;
      }
      if (last < t.value.length) out.push({ type: 'text', value: t.value.slice(last) });
      parent.children.splice(i, 1, ...(out as never[]));
      return i + out.length;
    });
  };
}

// ---------- rehype ----------

function textOf(n: ElementContent | RootContent): string {
  if (n.type === 'text') return n.value;
  if ('children' in n) return (n.children as ElementContent[]).map(textOf).join('');
  return '';
}

// Returns the node as an element (optionally of one tag), or undefined.
const el = (n: RootContent | ElementContent | undefined, tag?: string): Element | undefined =>
  n?.type === 'element' && (!tag || n.tagName === tag) ? n : undefined;
const isEl = (n: RootContent | ElementContent | undefined, tag?: string): boolean => !!el(n, tag);

// Astro applies rehype-raw after our plugins, so diagrams arrive as raw HTML nodes.
const isRawFigure = (n: RootContent | ElementContent) =>
  (n as { type: string }).type === 'raw' && /^\s*<(figure class="stepper"|div class="anim)/.test((n as unknown as { value: string }).value);

const STANDALONE = (n: RootContent | ElementContent) =>
  isEl(n, 'table') ||
  isRawFigure(n) ||
  String(el(n, 'figure')?.properties?.className ?? '').includes('stepper') ||
  el(n)?.properties?.dataSlot !== undefined;

function decorateTable(t: Element): void {
  t.properties = { ...t.properties, className: ['to'] };
  const thead = el(t.children.find((c) => isEl(c, 'thead')));
  const tbody = el(t.children.find((c) => isEl(c, 'tbody')));
  const labels: string[] = [];
  if (thead) {
    visit(thead, 'element', (th: Element) => {
      if (th.tagName === 'th') {
        th.properties = { ...th.properties, scope: 'col' };
        labels.push(textOf(th).trim());
      }
    });
  }
  if (!tbody) return;
  for (const row of tbody.children) {
    const tr = el(row, 'tr');
    if (!tr) continue;
    let col = 0;
    for (const c of tr.children) {
      const cell = el(c);
      if (!cell) continue;
      if (col === 0) {
        cell.tagName = 'th';
        cell.properties = { ...cell.properties, scope: 'row' };
      } else {
        cell.properties = { ...cell.properties, dataLabel: labels[col] ?? '' };
        // "Yes ..." / "No ..." cells get the same marks as the design.
        const first = cell.children[0];
        if (first?.type === 'text') {
          const m = /^(Yes|No)\b[.:]?\s*/.exec(first.value);
          if (m) {
            first.value = first.value.slice(m[0].length);
            cell.children.unshift(
              h('span', { class: m[1] === 'Yes' ? 'yes' : 'no' }, [icon(m[1] === 'Yes' ? 'check' : 'x', true), m[1]]),
              { type: 'text', value: ' ' },
            );
          }
        }
      }
      col++;
    }
  }
}

// "With AI": h3 "What to ask…" + quotes, h3 "What to check…" + list → two columns.
function aiLayout(nodes: ElementContent[]): ElementContent[] {
  const cols: Element[] = [];
  let cur: Element | null = null;
  for (const n of nodes) {
    const h3 = el(n, 'h3');
    if (h3) {
      const ask = /ask/i.test(textOf(h3));
      h3.children.unshift(icon(ask ? 'sparkles' : 'check-circle', true));
      cur = h('div', [h3]);
      cols.push(cur);
      continue;
    }
    if (!cur) continue;
    const quote = el(n, 'blockquote');
    const list = el(n, 'ul');
    if (quote) {
      for (const c of quote.children) {
        const p = el(c, 'p');
        if (p) cur.children.push(h('p', { class: 'prompt' }, p.children));
      }
    } else if (list) {
      list.properties = { ...list.properties, className: ['checks'] };
      for (const c of list.children) {
        const li = el(c, 'li');
        if (!li) continue;
        const onlyP = li.children.length === 1 ? el(li.children[0], 'p') : undefined;
        li.children = [icon('check', true), h('span', onlyP ? onlyP.children : li.children)];
      }
      cur.children.push(list);
    } else if (n.type !== 'text' || n.value.trim()) {
      cur.children.push(n);
    }
  }
  return [h('div', { class: 'ai' }, cols)];
}

export function rehypeLesson() {
  return (tree: HRoot) => {
    visit(tree, 'element', (el: Element) => {
      if (el.tagName === 'table') decorateTable(el);
      if (el.tagName === 'div' && String(el.properties?.className ?? '') === 'key') el.children.unshift(icon('bulb'));
    });

    const sections: { title: Element; body: ElementContent[] }[] = [];
    const before: RootContent[] = [];
    for (const n of tree.children) {
      const h2 = el(n, 'h2');
      if (h2) sections.push({ title: h2, body: [] });
      else if (sections.length) sections[sections.length - 1].body.push(n as ElementContent);
      else before.push(n);
    }
    const total = sections.length;

    const out: RootContent[] = [...before];
    sections.forEach((s, idx) => {
      const k = idx + 1;
      const name = sectionName(textOf(s.title));
      const known = LESSON_SECTIONS.indexOf(name as (typeof LESSON_SECTIONS)[number]);
      s.title.properties = { ...s.title.properties, id: `h-s${k}` };

      let body = s.body.filter((n) => n.type !== 'text' || n.value.trim());
      if (name === 'With AI') body = aiLayout(body);
      if (name === 'What broke') body.push(h('div', { dataSlot: 'incidents' }));
      if (name === 'Check yourself') body.push(h('div', { dataSlot: 'quiz' }));
      if (name === 'Go deeper') body.unshift(h('div', { dataSlot: 'deeper' }));

      // Group prose; tables, diagrams, the AI grid and slots stand alone.
      const card: ElementContent[] = [
        h('div', { class: 'sh' }, [
          h('span', { class: 'ico' }, [icon(SECTION_ICONS[known] ?? 'bulb')]),
          h('div', [h('small', `Section ${k} of ${total}`), s.title]),
        ]),
      ];
      let prose: Element | null = null;
      for (const n of body) {
        const alone = STANDALONE(n) || String(el(n, 'div')?.properties?.className ?? '').includes('ai');
        if (alone) {
          prose = null;
          card.push(n);
        } else {
          if (!prose) {
            prose = h('div', { class: 'prose' });
            card.push(prose);
          }
          prose.children.push(n);
        }
      }

      out.push(
        h('section', { class: 'sec', id: `s${k}`, 'aria-labelledby': `h-s${k}`, dataSection: name }, [
          h('div', { class: 'node', 'aria-hidden': 'true' }, [h('b', String(k)), icon('check')]),
          h('div', { class: 'card' }, card),
        ]),
      );
    });
    tree.children = out;
  };
}
