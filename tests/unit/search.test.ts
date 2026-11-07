import { describe, it, expect } from 'vitest';
import { rankEntries, weakMatch, latestOnly } from '../../src/scripts/search.ts';

describe('search helpers', () => {
  it('ranks exact title matches first (QA1-02)', () => {
    const items = [
      { title: 'Burn rate', excerpt: 'a <mark>rate</mark>' },
      { title: 'Rate limit', excerpt: '<mark>rate</mark> <mark>limit</mark>' },
      { title: 'Leaky bucket', excerpt: 'a <mark>limit</mark>' },
    ];
    expect(rankEntries(items, 'rate limit')[0].title).toBe('Rate limit');
  });

  it('drops results that only match a lone letter of a long query', () => {
    expect(weakMatch('we will see <mark>Z.</mark>', 'zzqqxx')).toBe(true);
    expect(weakMatch('a <mark>retry</mark> loop', 'retry')).toBe(false);
    expect(weakMatch('<mark>DB</mark> load', 'db')).toBe(false);
  });

  it('a slow answer for an old query is never shown (BUG-052, QA2-03)', async () => {
    const next = latestOnly();
    const shown: string[] = [];
    const search = async (q: string, ms: number) => {
      const current = next();
      if (q.length < 2) return void shown.push('chips');
      await new Promise((r) => setTimeout(r, ms));
      if (current()) shown.push(q);
    };
    // "lea", then the box is cut back to "l" before the "lea" answer arrives.
    await Promise.all([search('lea', 20), search('l', 0)]);
    expect(shown).toEqual(['chips']);
    // A slow old query and a fast new one: only the new one is shown.
    shown.length = 0;
    await Promise.all([search('cache', 30), search('cach', 5)]);
    expect(shown).toEqual(['cach']);
  });
});
