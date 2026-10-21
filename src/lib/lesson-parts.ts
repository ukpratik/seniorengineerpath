// HTML for the lesson slots that come from front matter and libraries:
// incident cards (What broke), quiz + done row (Check yourself), sources (Go deeper).
import { escapeHtml as e } from './html.ts';
import type { Incident, Lesson } from '../content-tools/schemas.ts';

const ico = (n: string, sm = false) => `<svg class="i${sm ? ' sm' : ''}" aria-hidden="true"><use href="/icons.svg#i-${n}"/></svg>`;

export function formatDay(day: string): string {
  return new Date(`${day}T00:00:00Z`).toLocaleDateString('en-GB', { day: 'numeric', month: 'short', year: 'numeric', timeZone: 'UTC' });
}

export function incidentsHtml(incidents: Incident[]): string {
  if (!incidents.length) return '';
  const tag = incidents[0].tags[0];
  return `<div class="real-inc"><h3>${ico('alert', true)}It happens for real</h3>
${incidents
  .map(
    (i) => `<a class="card inc-link" href="/incidents#${i.id}"><span class="when">${e(i.company)} · ${formatDay(i.date)}</span><b>${e(i.title)}</b><span>${e(i.broke)}</span></a>`,
  )
  .join('\n')}
<a class="more btn btn-ghost" style="padding-left:0" href="/incidents?tag=${tag}">All incidents tagged “${e(tag)}” ${ico('arrow-right', true)}</a></div>`;
}

export function quizHtml(quiz: Lesson['quiz'], reportUrl: string): string {
  const letters = 'abcde';
  const qs = quiz
    .map(
      (q, qi) => `<fieldset class="q" data-ok="${letters[q.answer]}"><legend>${qi + 1}. ${e(q.q)}</legend>
${q.options.map((o, oi) => `<label class="opt"><input type="radio" name="q${qi + 1}" value="${letters[oi]}">${e(o)}</label>`).join('\n')}
<p class="fb" aria-live="polite" data-why="${e(q.why)}"></p></fieldset>`,
    )
    .join('\n');
  return `<form class="quiz" onsubmit="return false">${qs}</form>
<div class="done-row">
<button type="button" class="btn btn-primary done-btn" data-done>${ico('check-circle')}<span>Mark as done</span></button>
<p class="sr" data-done-live aria-live="polite"></p>
<p class="small">Your progress is saved only in this browser. <a href="/progress">Export it</a> to keep it safe.</p>
<div class="notice info" data-export-prompt hidden>${ico('info')}<p><strong>You have finished 5 lessons.</strong> Your progress lives only in this browser. <a href="/progress">Export a copy</a> so you don’t lose it.</p></div>
<div class="fbk" data-clarity><span class="lbl">Was this clear?</span><button type="button" class="btn btn-secondary" data-v="yes">Yes</button><button type="button" class="btn btn-secondary" data-v="no">No</button></div>
<a class="btn btn-ghost" href="${e(reportUrl)}" target="_blank" rel="noopener" style="align-self:flex-start;padding-left:0">${ico('flag', true)}Report a mistake<span class="sr"> (opens GitHub in a new tab)</span></a>
</div>`;
}

export function deeperHtml(
  sources: Lesson['sources'],
  incidents: Incident[],
  related: { title: string; slug: string; written: boolean }[],
): string {
  const items = sources
    .map((s) => {
      const title = s.url
        ? `<a href="${e(s.url)}" target="_blank" rel="noopener">${e(s.title)}<span class="sr"> (opens in a new tab)</span></a>`
        : `<span style="font-weight:700">${e(s.title)}</span>`;
      const extra = [s.where, s.note].filter(Boolean).map((x) => e(x!)).join(' ');
      return `<li id="src-${s.id}">${title}${extra ? `<br><span class="muted">${extra}</span>` : ''}</li>`;
    })
    .join('\n');
  const inc = incidents
    .map((i) => `<p class="small">Real incident: <a href="/incidents#${i.id}">${e(i.company)}, ${formatDay(i.date)}: ${e(i.title)}</a></p>`)
    .join('');
  const rel = related.length
    ? `<p class="small">Related: ${related
        .map((r) => `<a href="/learn/${r.slug}">${e(r.title)}</a>${r.written ? '' : ' (coming soon)'}`)
        .join(' · ')}</p>`
    : '';
  const n = sources.length;
  return `<details class="deeper"><summary>${ico('chev-down', true)}Show ${n} source${n === 1 ? '' : 's'} and related lessons</summary>
<ol class="srcs prose">${items}</ol>${inc}${rel}</details>`;
}

// Replace the empty slot <div>s that rehypeLesson left in the rendered lesson.
export function fillSlots(html: string, parts: { incidents: string; quiz: string; deeper: string }): string {
  return html
    .replace('<div data-slot="incidents"></div>', parts.incidents)
    .replace('<div data-slot="quiz"></div>', parts.quiz)
    .replace('<div data-slot="deeper"></div>', parts.deeper);
}

export function readingMinutes(html: string): number {
  const words = html.replace(/<[^>]+>/g, ' ').split(/\s+/).filter(Boolean).length;
  return Math.max(3, Math.round(words / 160)); // slower pace for non-native readers
}
