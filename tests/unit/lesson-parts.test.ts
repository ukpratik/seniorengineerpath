import { describe, it, expect } from 'vitest';
import { inlineCode, quizHtml } from '../../src/lib/lesson-parts.ts';

describe('quiz text', () => {
  it('turns `code` into <code> after escaping', () => {
    expect(inlineCode('Run `a<b>` & `c`')).toBe('Run <code>a&lt;b&gt;</code> &amp; <code>c</code>');
    expect(inlineCode('no `closing')).toBe('no `closing');
  });

  it('renders code spans in questions, options and answers', () => {
    const html = quizHtml([{ q: 'Which `x`?', options: ['`a/b`', 'c'], answer: 0, why: 'Because `y`.' }], '#');
    expect(html).toContain('<legend>1. Which <code>x</code>?</legend>');
    expect(html).toContain('<span><code>a/b</code></span>');
    expect(html).toContain('<template class="why">Because <code>y</code>.</template>');
    expect(html).not.toContain('`');
  });
});
