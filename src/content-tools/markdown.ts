// Shared helpers for reading lesson Markdown at build time.
import { unified } from 'unified';
import remarkParse from 'remark-parse';
import remarkGfm from 'remark-gfm';
import { parse as parseYaml } from 'yaml';
import { visit, SKIP } from 'unist-util-visit';
import type { Root, Heading, Node } from 'mdast';

export const LESSON_SECTIONS = [
  'What broke',
  'The idea in simple words',
  'How it works',
  'Trade-offs',
  'At work',
  'With AI',
  'Check yourself',
  'Go deeper',
] as const;

export const CITE_RE = /\[@([a-z0-9-]+)\]/g;
export const DIAGRAM_RE = /^\[\[(diagram|anim):([a-z0-9-]+)\]\]$/;

export function splitFrontmatter(source: string): { data: Record<string, unknown>; body: string; bodyLine: number } {
  const m = /^---\r?\n([\s\S]*?)\r?\n---\r?\n?/.exec(source);
  if (!m) return { data: {}, body: source, bodyLine: 1 };
  const bodyLine = m[0].split('\n').length;
  return { data: (parseYaml(m[1]) ?? {}) as Record<string, unknown>, body: source.slice(m[0].length), bodyLine };
}

export function parseMarkdown(body: string): Root {
  return unified().use(remarkParse).use(remarkGfm).parse(body) as Root;
}

export function headingText(h: Heading): string {
  let t = '';
  visit(h, 'text', (n: { value: string }) => {
    t += n.value;
  });
  return t.trim();
}

// "At work: how a senior uses this" counts as the "At work" section.
export function sectionName(text: string): string {
  return text.split(':')[0].trim();
}

export function h2Headings(tree: Root): { text: string; line: number }[] {
  const out: { text: string; line: number }[] = [];
  visit(tree, 'heading', (h: Heading) => {
    if (h.depth === 2) out.push({ text: headingText(h), line: h.position?.start.line ?? 0 });
  });
  return out;
}

// Plain prose for readability scoring: paragraphs and list items only.
// Skips headings, tables, code, HTML, block quotes used as AI prompts, and our markers.
export function extractProse(tree: Root): { text: string; line: number }[] {
  const out: { text: string; line: number }[] = [];
  visit(tree, (node: Node, _i, parent) => {
    const t = node.type;
    if (t === 'heading' || t === 'table' || t === 'code' || t === 'html' || t === 'blockquote') return SKIP;
    if (t === 'paragraph') {
      let s = '';
      visit(node, (c: Node & { value?: string }) => {
        if (c.type === 'text') s += c.value;
        else if (c.type === 'inlineCode') s += 'code';
      });
      s = s.replace(CITE_RE, '').trim();
      if (s && !DIAGRAM_RE.test(s)) {
        // A list item without final punctuation is still one sentence.
        if (parent && (parent as Node).type === 'listItem' && !/[.!?:]$/.test(s)) s += '.';
        out.push({ text: s, line: node.position?.start.line ?? 0 });
      }
      return SKIP;
    }
    return undefined;
  });
  return out;
}

export function findCitations(body: string): string[] {
  return [...body.matchAll(CITE_RE)].map((m) => m[1]);
}

export function findDiagrams(tree: Root): { kind: string; id: string; line: number }[] {
  const out: { kind: string; id: string; line: number }[] = [];
  visit(tree, 'paragraph', (p: Node & { children: (Node & { value?: string })[] }) => {
    if (p.children.length === 1 && p.children[0].type === 'text') {
      const m = DIAGRAM_RE.exec(p.children[0].value!.trim());
      if (m) out.push({ kind: m[1], id: m[2], line: p.position?.start.line ?? 0 });
    }
  });
  return out;
}

export function findLinks(tree: Root): { url: string; line: number }[] {
  const out: { url: string; line: number }[] = [];
  visit(tree, 'link', (l: Node & { url: string }) => {
    out.push({ url: l.url, line: l.position?.start.line ?? 0 });
  });
  return out;
}
