import { describe, it, expect } from 'vitest';
import { ogPng, wrap } from '../../src/lib/og-image.ts';

describe('share images (US-015, BUG-001)', () => {
  it('draws the text, so two lessons get different images', () => {
    const a = ogPng({ title: 'Rate limiting', subtitle: 'Token buckets', track: 'Building at scale', level: 'L1 Must-know' });
    const b = ogPng({ title: 'Replication', subtitle: 'Leaders and followers', track: 'Data', level: 'L1 Must-know' });
    expect(a.equals(b)).toBe(false);
    expect(a.length).toBeGreaterThan(15_000); // a blank card is about 6 KB
  });

  it('wraps long titles and ends cut text with an ellipsis', () => {
    const lines = wrap('One two three four five six seven eight nine ten eleven twelve', 10, 2);
    expect(lines).toHaveLength(2);
    expect(lines[1].endsWith('…')).toBe(true);
  });
});
