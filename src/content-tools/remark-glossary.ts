// Links the first use of each glossary term in a lesson (US-011).
// Skips headings, links, code and block quotes. Authors can opt out per term
// with `glossary_optout: [term-id]` in the front matter.
import { visitParents } from 'unist-util-visit-parents';
import type { Root, Text, Link, Parent, Node } from 'mdast';
import type { VFile } from 'vfile';

export interface GlossaryTerm {
  id: string;
  term: string;
  forms?: string[];
  short: string;
}

const SKIP_PARENTS = new Set(['heading', 'link', 'linkReference', 'inlineCode', 'code', 'blockquote', 'tableCell']);

function escapeRe(s: string): string {
  return s.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
}

export function buildMatcher(terms: GlossaryTerm[]): { re: RegExp; idFor: Map<string, string> } | null {
  const idFor = new Map<string, string>();
  for (const t of terms) {
    for (const f of [t.term, ...(t.forms ?? [])]) idFor.set(f.toLowerCase(), t.id);
  }
  if (!idFor.size) return null;
  const alts = [...idFor.keys()].sort((a, b) => b.length - a.length).map(escapeRe);
  return { re: new RegExp(`(?<![\\w-])(${alts.join('|')})(?![\\w-])`, 'gi'), idFor };
}

export function linkGlossary(tree: Root, terms: GlossaryTerm[], optOut: string[] = []): string[] {
  const matcher = buildMatcher(terms);
  if (!matcher) return [];
  const used = new Set<string>(optOut);
  const linked: string[] = [];
  const edits: { parent: Parent; node: Text; replacement: (Text | Link)[] }[] = [];

  visitParents(tree, 'text', (node: Text, ancestors: Node[]) => {
    if (ancestors.some((a) => SKIP_PARENTS.has(a.type))) return;
    const parts: (Text | Link)[] = [];
    let last = 0;
    let changed = false;
    matcher.re.lastIndex = 0;
    for (const m of node.value.matchAll(matcher.re)) {
      const id = matcher.idFor.get(m[1].toLowerCase())!;
      if (used.has(id)) continue;
      used.add(id);
      linked.push(id);
      changed = true;
      const at = m.index!;
      if (at > last) parts.push({ type: 'text', value: node.value.slice(last, at) });
      parts.push({
        type: 'link',
        url: `/glossary#${id}`,
        data: { hProperties: { className: ['term'], 'data-term': id } },
        children: [{ type: 'text', value: m[1] }],
      });
      last = at + m[1].length;
    }
    if (!changed) return;
    if (last < node.value.length) parts.push({ type: 'text', value: node.value.slice(last) });
    edits.push({ parent: ancestors[ancestors.length - 1] as Parent, node, replacement: parts });
  });

  for (const e of edits) {
    const i = e.parent.children.indexOf(e.node as never);
    e.parent.children.splice(i, 1, ...(e.replacement as never[]));
  }
  return linked;
}

export function remarkGlossary(options: { terms: () => GlossaryTerm[] }) {
  return (tree: Root, file: VFile) => {
    const fm = (file.data.astro?.frontmatter ?? {}) as { glossary_optout?: string[] };
    const linked = linkGlossary(tree, options.terms(), fm.glossary_optout ?? []);
    // Lesson page uses this list to emit only the definitions it needs.
    if (file.data.astro?.frontmatter) file.data.astro.frontmatter.glossaryUsed = linked;
  };
}
