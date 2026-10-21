// Server-side markup for the step-through diagram (US-006). Every frame is
// rendered, so the diagram reads fully without JavaScript; stepthrough.ts
// then shows one frame at a time.
import { escapeHtml } from './html.ts';

export interface Diagram {
  id: string;
  title: string;
  viewBox: string;
  svg: string; // inner SVG; frames are <g class="f" data-f="1">…</g>
  captions: string[];
  intro?: string;
}

export function frameCount(svg: string): number {
  const nums = new Set([...svg.matchAll(/data-f="(\d+)"/g)].map((m) => Number(m[1])));
  return nums.size;
}

export function renderStepper(d: Diagram): string {
  const n = d.captions.length;
  const tid = `dg-${d.id}-title`;
  const dots = Array.from({ length: n }, () => '<i></i>').join('');
  return `<figure class="stepper" id="dg-${d.id}" data-stepper tabindex="0" aria-roledescription="step-through diagram" aria-labelledby="${tid}">
<svg class="dg" viewBox="${escapeHtml(d.viewBox)}" role="img" aria-labelledby="${tid}"><title id="${tid}">${escapeHtml(d.title)}</title>${d.svg}<text class="site" x="99%" y="99%" text-anchor="end">seniorpath.dev</text></svg>
<ol class="caps-nojs">${d.captions.map((c) => `<li>${escapeHtml(c)}</li>`).join('')}</ol>
<div class="ctrls no-js-hide">
<button type="button" class="btn btn-secondary" data-prev><svg class="i" aria-hidden="true"><use href="/icons.svg#i-chev-left"/></svg>Back</button>
<span class="count" data-count>Step 1 of ${n}</span>
<button type="button" class="btn btn-primary" data-next>Next<svg class="i" aria-hidden="true"><use href="/icons.svg#i-chev-right"/></svg></button>
<button type="button" class="btn btn-ghost play-btn" data-play><svg class="i sm" aria-hidden="true"><use href="/icons.svg#i-play"/></svg><span>Play</span></button>
</div>
<figcaption class="cap no-js-hide" data-cap aria-live="polite"></figcaption>
<div class="dots no-js-hide" aria-hidden="true">${dots}</div>
</figure>`;
}
