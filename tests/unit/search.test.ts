import { describe, it, expect } from 'vitest';
import { rankEntries, weakMatch } from '../../src/scripts/search.ts';

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
});
