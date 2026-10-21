// The content gate (US-016, US-017, ADR-006). Published lessons that break a rule
// fail the build. Drafts are only checked for structure, and only warn.
import type { Content, LessonFile } from './load.ts';
import {
  LESSON_SECTIONS,
  parseMarkdown,
  h2Headings,
  sectionName,
  extractProse,
  findCitations,
  findDiagrams,
  findLinks,
} from './markdown.ts';
import { analyze, findBannedPhrases, LIMITS } from './readability.ts';
import { frameCount } from '../lib/stepper.ts';

export interface Issue {
  file: string;
  line?: number;
  rule: string;
  message: string;
}

export interface GateResult {
  errors: Issue[];
  warnings: Issue[];
}

export const AI_TRACK = 'ai-native';
export const AI_REVIEW_DAYS = 92;
const STATIC_ROUTES = ['/', '/map', '/glossary', '/incidents', '/cases', '/progress', '/search', '/about', '/privacy', '/paths'];

function addDays(day: string, n: number): string {
  const d = new Date(`${day}T00:00:00Z`);
  d.setUTCDate(d.getUTCDate() + n);
  return d.toISOString().slice(0, 10);
}

export function knownRoutes(c: Content): Set<string> {
  const r = new Set(STATIC_ROUTES);
  for (const t of c.topics) r.add(`/learn/${t.slug}`);
  for (const p of c.paths) r.add(`/paths/${p.slug}`);
  for (const g of c.glossary) r.add(`/glossary#${g.id}`);
  for (const i of c.incidents) r.add(`/incidents#${i.id}`);
  for (const k of c.cases) r.add(`/cases#${k.id}`);
  return r;
}

function checkLesson(l: LessonFile, c: Content, routes: Set<string>, today: string, res: GateResult): void {
  const published = l.data.status === 'published';
  const err = (rule: string, message: string, line?: number) =>
    (published ? res.errors : res.warnings).push({ file: l.file, line: line ? line + l.bodyLine - 1 : undefined, rule, message });
  const warn = (rule: string, message: string, line?: number) =>
    res.warnings.push({ file: l.file, line: line ? line + l.bodyLine - 1 : undefined, rule, message });

  const topic = c.topics.find((t) => t.id === l.data.topic);
  if (!topic) err('topic', `topic "${l.data.topic}" does not exist`);
  if (l.id !== l.data.topic) err('topic', `file name must be the topic id "${l.data.topic}.md"`);

  const tree = parseMarkdown(l.body);

  // 1. The eight sections, in order.
  const heads = h2Headings(tree);
  const names = heads.map((h) => sectionName(h.text));
  if (names.join('|') !== LESSON_SECTIONS.join('|')) {
    err('sections', `need exactly these H2 sections in order: ${LESSON_SECTIONS.join(', ')}. Found: ${names.join(', ') || 'none'}`);
  }

  // 2–3. Sources and checked claims.
  const sourceIds = new Set(l.data.sources.map((s) => s.id));
  if (l.data.sources.length < 2) err('sources', 'needs at least 2 sources');
  for (const s of l.data.sources) if (!s.url && !s.where) err('sources', `source "${s.id}" needs a url or a "where" (book chapter)`);
  if (l.data.claims_checked.length < 3) err('claims', 'needs at least 3 claims_checked');
  for (const cl of l.data.claims_checked) if (!sourceIds.has(cl.source)) err('claims', `claim cites unknown source "${cl.source}"`);
  const cited = findCitations(l.body);
  for (const id of cited) if (!sourceIds.has(id)) err('citations', `[@${id}] has no matching source`);
  for (const s of l.data.sources) if (!cited.includes(s.id)) warn('citations', `source "${s.id}" is never cited in the text`);

  // 4. Review record.
  if (!l.data.reviewed_by || !l.data.reviewed_on) err('review', 'reviewed_by and reviewed_on are required');

  // 5. Readability: fail on very long sentences and banned phrases; warn on averages.
  const prose = extractProse(tree);
  const glossaryWords = new Set(
    c.glossary.flatMap((g) => [g.term, ...g.forms]).flatMap((t) => t.toLowerCase().split(/\s+/)),
  );
  for (const p of prose) {
    const a = analyze(p.text, glossaryWords);
    for (const s of a.longSentences) err('readability', `sentence over ${LIMITS.failSentenceWords} words: "${s.slice(0, 80)}…"`, p.line);
    for (const b of findBannedPhrases(p.text, c.banned)) err('readability', `banned phrase "${b}"`, p.line);
  }
  const all = analyze(prose.map((p) => p.text).join(' '), glossaryWords);
  if (all.avgWords > LIMITS.warnAvgWords) warn('readability', `average sentence is ${all.avgWords} words (target ≤ ${LIMITS.warnAvgWords})`);
  if (all.grade > LIMITS.warnGrade) warn('readability', `reading grade is ${all.grade} (target ≤ ${LIMITS.warnGrade})`);

  // 6. Internal links resolve.
  for (const link of findLinks(tree)) {
    if (/^(https?:|mailto:)/.test(link.url) || link.url.startsWith('#')) continue;
    const path = link.url.replace(/\/$/, '') || '/';
    if (!routes.has(path) && !routes.has(path.split('#')[0])) err('links', `internal link "${link.url}" does not resolve`, link.line);
  }

  // Diagrams exist and frames match captions.
  for (const d of findDiagrams(tree)) {
    const dg = c.diagrams.find((x) => x.id === d.id);
    if (!dg) err('diagrams', `unknown ${d.kind} "${d.id}"`, d.line);
  }

  // 7. Quiz.
  if (l.data.quiz.length < 3 || l.data.quiz.length > 5) err('quiz', `needs 3–5 questions, has ${l.data.quiz.length}`);
  l.data.quiz.forEach((q, i) => {
    if (q.answer >= q.options.length) err('quiz', `question ${i + 1}: answer ${q.answer} is not one of the options`);
  });

  // Incidents and case studies must exist and be verified before they appear.
  for (const id of l.data.incidents) {
    const inc = c.incidents.find((x) => x.id === id);
    if (!inc) err('incidents', `unknown incident "${id}"`);
    else if (!inc.verified) err('incidents', `incident "${id}" is not verified yet`);
  }
  for (const id of l.data.cases) {
    const cs = c.cases.find((x) => x.id === id);
    if (!cs) err('cases', `unknown case study "${id}"`);
    else if (!cs.verified) err('cases', `case study "${id}" is not verified yet`);
  }
  for (const id of l.data.related) if (!c.topics.some((t) => t.id === id)) err('related', `unknown related topic "${id}"`);

  // 8. AI-track lessons are re-reviewed every quarter.
  if (topic?.track === AI_TRACK && l.data.reviewed_on) {
    if (!l.data.review_by) err('review', `AI-track lessons need review_by (≤ ${AI_REVIEW_DAYS} days after reviewed_on)`);
    else if (l.data.review_by > addDays(l.data.reviewed_on, AI_REVIEW_DAYS)) err('review', `review_by must be ≤ ${AI_REVIEW_DAYS} days after reviewed_on`);
    else if (l.data.review_by < today) warn('review', `review is overdue since ${l.data.review_by}`);
  }
}

export function runGate(c: Content, opts: { today: string; publishedSlugs?: string[] }): GateResult {
  const res: GateResult = { errors: c.errors.map((e) => ({ file: e.file, rule: 'schema', message: e.message })), warnings: [] };
  const e = (file: string, rule: string, message: string) => res.errors.push({ file, rule, message });

  // Topics.
  const topicIds = new Set<string>();
  const slugs = new Map<string, string>();
  for (const t of c.topics) {
    const f = `topics/${t.id}.yaml`;
    if (topicIds.has(t.id)) e(f, 'topics', `duplicate id "${t.id}"`);
    topicIds.add(t.id);
    for (const s of [t.slug, ...t.aliases]) {
      if (slugs.has(s)) e(f, 'topics', `slug "${s}" is also used by "${slugs.get(s)}"`);
      slugs.set(s, t.id);
    }
    const track = c.tracks.find((x) => x.id === t.track);
    if (!track) e(f, 'topics', `unknown track "${t.track}"`);
    else if (!track.modules.some((m) => m.id === t.module)) e(f, 'topics', `unknown module "${t.module}" in track "${t.track}"`);
  }
  for (const t of c.topics) for (const p of t.prereqs) if (!topicIds.has(p)) e(`topics/${t.id}.yaml`, 'topics', `unknown prerequisite "${p}"`);

  // Paths.
  for (const p of c.paths) for (const s of p.steps) if (!topicIds.has(s)) e(`paths/${p.id}.yaml`, 'paths', `unknown step "${s}"`);
  if (c.paths.length && c.paths.filter((p) => p.default).length !== 1) e('paths', 'paths', 'exactly one path must have default: true');

  // Library cross-links.
  for (const i of c.incidents) for (const l of i.lessons) if (!topicIds.has(l)) e(`incidents/${i.id}.yaml`, 'incidents', `unknown lesson "${l}"`);
  for (const k of c.cases) for (const l of k.lessons) if (!topicIds.has(l)) e(`cases/${k.id}.yaml`, 'cases', `unknown lesson "${l}"`);

  // Glossary forms must point to one term only.
  const forms = new Map<string, string>();
  for (const g of c.glossary) {
    for (const f of [g.term, ...g.forms].map((x) => x.toLowerCase())) {
      if (forms.has(f) && forms.get(f) !== g.id) e(`glossary/${g.id}.yaml`, 'glossary', `"${f}" is also a form of "${forms.get(f)}"`);
      forms.set(f, g.id);
    }
  }

  // Diagrams: one caption per frame.
  for (const d of c.diagrams) {
    const n = frameCount(d.svg);
    if (n !== d.captions.length) e(`diagrams/${d.id}.yaml`, 'diagrams', `${n} frames but ${d.captions.length} captions`);
  }

  // Lessons.
  const routes = knownRoutes(c);
  for (const l of c.lessons) checkLesson(l, c, routes, opts.today, res);

  // A published slug may never disappear without an alias (US-017).
  for (const s of opts.publishedSlugs ?? []) {
    if (!slugs.has(s)) e('slug-manifest.json', 'slugs', `published slug "${s}" was removed; add it to the topic's aliases`);
  }
  return res;
}

export function publishedLessons(c: Content): LessonFile[] {
  return c.lessons.filter((l) => l.data.status === 'published');
}

export function formatIssues(r: GateResult): string {
  const line = (i: Issue) => `  ${i.file}${i.line ? `:${i.line}` : ''}  [${i.rule}] ${i.message}`;
  const out: string[] = [];
  if (r.errors.length) out.push(`Content gate: ${r.errors.length} error(s)`, ...r.errors.map(line));
  if (r.warnings.length) out.push(`Content gate: ${r.warnings.length} warning(s)`, ...r.warnings.map(line));
  if (!r.errors.length && !r.warnings.length) out.push('Content gate: all checks passed');
  return out.join('\n');
}
