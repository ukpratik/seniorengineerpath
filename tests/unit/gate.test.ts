import { describe, it, expect } from 'vitest';
import { runGate } from '../../src/content-tools/gate.ts';
import type { Content, LessonFile } from '../../src/content-tools/load.ts';
import { lessonSchema } from '../../src/content-tools/schemas.ts';

const BODY = `## What broke

The app sent the same payment twice [@stripe]. The customer paid two times.

## The idea in simple words

A retry is safe when the action is idempotent [@aws].

## How it works

The server saves a key. See [the glossary](/glossary#retry).

## Trade-offs

Some text.

## At work: how a senior uses this

Ask what happens if the call runs twice.

## With AI

Ask the agent for a test.

## Check yourself

## Go deeper

Read more.
`;

function lesson(over: Partial<Record<string, unknown>> = {}, body = BODY): LessonFile {
  const data = lessonSchema.parse({
    topic: 'idem',
    subtitle: 'Why retries can charge twice.',
    status: 'published',
    sources: [
      { id: 'stripe', title: 'Stripe docs', url: 'https://docs.stripe.com/api/idempotent_requests' },
      { id: 'aws', title: 'AWS Builders Library', url: 'https://aws.amazon.com/builders-library/' },
    ],
    claims_checked: [
      { claim: 'a', source: 'stripe' },
      { claim: 'b', source: 'aws' },
      { claim: 'c', source: 'stripe' },
    ],
    reviewed_by: 'ai-factcheck',
    reviewed_on: '2026-10-21',
    quiz: [
      { q: 'One?', options: ['a', 'b'], answer: 0, why: 'Because a.' },
      { q: 'Two?', options: ['a', 'b'], answer: 1, why: 'Because b.' },
      { q: 'Three?', options: ['a', 'b', 'c'], answer: 2, why: 'Because c.' },
    ],
    ...over,
  });
  return { file: 'lessons/idem.md', id: 'idem', data, body, bodyLine: 20 };
}

function content(l: LessonFile, extra: Partial<Content> = {}): Content {
  return {
    tracks: [
      { id: 'distributed', title: 'Distributed systems', short: 'Distributed', modules: [{ id: 'reliability', title: 'Reliable calls' }] },
      { id: 'ai-native', title: 'AI-native engineering', short: 'AI', modules: [{ id: 'agents', title: 'Agents' }] },
    ],
    topics: [
      { id: 'idem', slug: 'idempotency', aliases: [], track: 'distributed', module: 'reliability', title: 'Idempotency', oneLiner: 'Making retries safe for money.', level: 'L1', prereqs: [] },
    ],
    lessons: [l],
    glossary: [{ id: 'retry', term: 'retry', forms: [], short: 'Try again.' }],
    incidents: [],
    cases: [],
    paths: [],
    diagrams: [],
    banned: ['piece of cake'],
    errors: [],
    ...extra,
  };
}

const run = (c: Content, slugs: string[] = []) => runGate(c, { today: '2026-10-21', publishedSlugs: slugs });
const rules = (r: ReturnType<typeof run>) => r.errors.map((e) => e.rule);

describe('content gate', () => {
  it('passes a complete lesson', () => {
    const r = run(content(lesson()));
    expect(r.errors).toEqual([]);
  });

  it('fails a published lesson with missing sections, sources, claims, review and quiz', () => {
    const l = lesson(
      { sources: [{ id: 'stripe', title: 'Stripe', url: 'https://stripe.com' }], claims_checked: [], reviewed_by: undefined, quiz: [] },
      '## What broke\n\nText [@stripe].\n',
    );
    expect(new Set(rules(run(content(l))))).toEqual(new Set(['sections', 'sources', 'claims', 'review', 'quiz']));
  });

  it('only warns for drafts', () => {
    const l = lesson({ status: 'draft', quiz: [] });
    const r = run(content(l));
    expect(r.errors).toEqual([]);
    expect(r.warnings.some((w) => w.rule === 'quiz')).toBe(true);
  });

  it('fails long sentences and banned phrases with a line number', () => {
    const long = Array.from({ length: 40 }, () => 'word').join(' ');
    const body = BODY.replace('Some text.', `${long}. It is a piece of cake.`);
    const r = run(content(lesson({}, body)));
    const read = r.errors.filter((e) => e.rule === 'readability');
    expect(read).toHaveLength(2);
    expect(read[0].line).toBeGreaterThan(20);
  });

  it('fails broken internal links and unknown citations', () => {
    const body = BODY.replace('/glossary#retry', '/learn/nope').replace('[@aws]', '[@nobody]');
    expect(rules(run(content(lesson({}, body))))).toEqual(expect.arrayContaining(['links', 'citations']));
  });

  it('requires verified incidents', () => {
    const inc = { id: 'aws-2021', title: 't', date: '2021-12-07', company: 'AWS', broke: 'b', why: 'w', lesson: 'l', url: 'https://aws.amazon.com/message/12721/', tags: ['retries'], lessons: ['idem'], verified: false };
    const r = run(content(lesson({ incidents: ['aws-2021'] }), { incidents: [inc] }));
    expect(rules(r)).toContain('incidents');
  });

  it('enforces the quarterly review for AI-track lessons', () => {
    const c = content(lesson());
    c.topics[0] = { ...c.topics[0], track: 'ai-native', module: 'agents' };
    expect(rules(run(c))).toContain('review');
    c.lessons[0] = lesson({ review_by: '2027-03-01' });
    expect(rules(run(c))).toContain('review');
    c.lessons[0] = lesson({ review_by: '2026-12-01' });
    expect(rules(run(c))).not.toContain('review');
  });

  it('fails when a published slug disappears without an alias', () => {
    expect(rules(run(content(lesson()), ['old-slug']))).toContain('slugs');
    const c = content(lesson());
    c.topics[0] = { ...c.topics[0], aliases: ['old-slug'] };
    expect(rules(run(c, ['old-slug']))).not.toContain('slugs');
  });

  it('checks topic, path and diagram cross-references', () => {
    const c = content(lesson(), {
      paths: [{ id: 'p', slug: 'p', title: 'P', description: 'd', default: true, steps: ['idem', 'ghost'] }],
      diagrams: [{ id: 'd', title: 't', viewBox: '0 0 1 1', svg: '<g data-f="1"></g>', captions: ['a', 'b'] }],
    });
    c.topics.push({ ...c.topics[0], id: 'dup', slug: 'idempotency', prereqs: ['ghost'] });
    expect(new Set(rules(run(c)))).toEqual(new Set(['topics', 'paths', 'diagrams']));
  });
});
