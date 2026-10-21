import { describe, it, expect } from 'vitest';
import { toString } from 'mdast-util-to-string';
import { visit } from 'unist-util-visit';
import { parseMarkdown } from '../../src/content-tools/markdown.ts';
import { linkGlossary, type GlossaryTerm } from '../../src/content-tools/remark-glossary.ts';

const terms: GlossaryTerm[] = [
  { id: 'retry', term: 'retry', forms: ['retries'], short: 'Trying a request again.' },
  { id: 'idempotent', term: 'idempotent', forms: ['idempotency'], short: 'Doing it twice equals once.' },
  { id: 'idempotency-key', term: 'idempotency key', forms: ['idempotency keys'], short: 'A unique id per operation.' },
];

function links(md: string, optOut: string[] = []) {
  const tree = parseMarkdown(md);
  const used = linkGlossary(tree, terms, optOut);
  const found: { url: string; text: string }[] = [];
  visit(tree, 'link', (l: { url: string }) => {
    found.push({ url: l.url, text: toString(l as never) });
  });
  return { used, found, text: toString(tree) };
}

describe('linkGlossary', () => {
  it('links only the first use of each term', () => {
    const r = links('A retry is risky. Another retry is worse.');
    expect(r.found).toEqual([{ url: '/glossary#retry', text: 'retry' }]);
    expect(r.text).toBe('A retry is risky. Another retry is worse.');
  });

  it('matches plural forms and prefers the longest term', () => {
    const r = links('Use idempotency keys for all retries.');
    expect(r.found.map((f) => f.url)).toEqual(['/glossary#idempotency-key', '/glossary#retry']);
    expect(r.found[0].text).toBe('idempotency keys');
  });

  it('skips headings, code, links and quotes', () => {
    const r = links('## Retry\n\n`retry()` and [retry](https://x.dev)\n\n> retry here\n\nNow a retry.');
    expect(r.found.filter((f) => f.url.startsWith('/glossary'))).toEqual([{ url: '/glossary#retry', text: 'retry' }]);
  });

  it('respects opt-outs and word boundaries', () => {
    expect(links('A retry here.', ['retry']).used).toEqual([]);
    expect(links('The retrying loop.').used).toEqual([]);
  });
});
