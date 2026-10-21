import { describe, it, expect } from 'vitest';
import { splitSentences, analyze, syllables, findBannedPhrases } from '../../src/content-tools/readability.ts';

describe('splitSentences', () => {
  it('splits on sentence ends', () => {
    expect(splitSentences('One idea. Two ideas! Three?')).toEqual(['One idea.', 'Two ideas!', 'Three?']);
  });

  it('keeps abbreviations, decimals and domains inside a sentence', () => {
    const s = splitSentences('Use a cache, e.g. Redis, for 99.9% of reads. Read stripe.com docs. Then stop.');
    expect(s).toEqual(['Use a cache, e.g. Redis, for 99.9% of reads.', 'Read stripe.com docs.', 'Then stop.']);
  });

  it('handles rupee amounts at the start of a sentence', () => {
    expect(splitSentences('She paid twice. ₹1,499 left her account.')).toHaveLength(2);
  });
});

describe('analyze', () => {
  it('flags sentences over 35 words', () => {
    const long = 'Every ' + Array.from({ length: 35 }, () => 'word').join(' ') + '.';
    const r = analyze(`Short one. ${long}`);
    expect(r.longSentences).toHaveLength(1);
    expect(r.sentences).toBe(2);
  });

  it('gives simple text a low grade', () => {
    const r = analyze('The app sends the key. The server saves it. A retry uses the same key.');
    expect(r.grade).toBeLessThan(6);
  });

  it('does not punish glossary terms', () => {
    const text = 'Idempotency makes retries safe. Idempotency keys stop double charges.';
    const plain = analyze(text);
    const withTerms = analyze(text, new Set(['idempotency']));
    expect(withTerms.grade).toBeLessThan(plain.grade);
  });
});

describe('syllables', () => {
  it('estimates common words', () => {
    expect(syllables('cat')).toBe(1);
    expect(syllables('server')).toBe(2);
    expect(syllables('database')).toBe(3);
  });
});

describe('findBannedPhrases', () => {
  it('finds whole phrases only', () => {
    expect(findBannedPhrases('This is a piece of cake.', ['piece of cake'])).toEqual(['piece of cake']);
    expect(findBannedPhrases('Leverage is a lever.', ['leverage'])).toEqual(['leverage']);
    expect(findBannedPhrases('Levers are fine.', ['leverage'])).toEqual([]);
  });
});
