// Views for the custom animations (US-007). The models in ./sims are pure and
// unit-tested; these views only draw them. Nothing plays by itself: the reader
// moves sliders and switches, so reduced-motion users get the same experience.
import { simulateStampede, summary } from './sims/cache-stampede.ts';
import { simulateRead } from './sims/replication-lag.ts';
import { simulateRollout } from './sims/rollout.ts';
import { track } from './analytics.ts';

const NS = 'http://www.w3.org/2000/svg';
let uid = 0;

function el<K extends keyof HTMLElementTagNameMap>(tag: K, attrs: Record<string, string> = {}, text?: string): HTMLElementTagNameMap[K] {
  const e = document.createElement(tag);
  for (const [k, v] of Object.entries(attrs)) e.setAttribute(k, v);
  if (text) e.textContent = text;
  return e;
}

function slider(label: string, min: number, max: number, step: number, value: number, unit: string) {
  const id = `anim-${++uid}`;
  const wrap = el('div', { class: 'anim-ctrl' });
  const lab = el('label', { for: id });
  const out = el('output', { for: id });
  lab.append(label, ' ', out);
  const input = el('input', { type: 'range', id, min: String(min), max: String(max), step: String(step), value: String(value) });
  const show = () => (out.textContent = `${input.value} ${unit}`);
  input.addEventListener('input', show);
  show();
  wrap.append(lab, input);
  return { wrap, input, value: () => Number(input.value) };
}

function toggle(label: string, on: boolean) {
  const b = el('button', { type: 'button', class: 'switch', role: 'switch', 'aria-checked': String(on) });
  b.innerHTML = '<span class="track" aria-hidden="true"></span>';
  b.append(label);
  b.addEventListener('click', () => b.setAttribute('aria-checked', String(b.getAttribute('aria-checked') !== 'true')));
  return { el: b, on: () => b.getAttribute('aria-checked') === 'true' };
}

// Simple bar chart: one bar per value, scaled to the largest value seen.
function bars(values: number[], opts: { max: number; label: string; peak: string; markAt?: number; markLabel?: string; cls?: string }): SVGSVGElement {
  const W = 340;
  const H = 140;
  const pad = 22;
  const svg = document.createElementNS(NS, 'svg');
  svg.setAttribute('viewBox', `0 0 ${W} ${H}`);
  svg.setAttribute('class', 'anim-chart');
  svg.setAttribute('aria-hidden', 'true');
  const bw = (W - 8) / values.length;
  values.forEach((v, i) => {
    if (v <= 0) return;
    // Non-zero values stay visible (at least 3 px) even next to a large maximum.
    const h = Math.max(3, opts.max ? (v / opts.max) * (H - pad - 8) : 0);
    const r = document.createElementNS(NS, 'rect');
    r.setAttribute('x', String(4 + i * bw));
    r.setAttribute('y', String(H - pad - h));
    r.setAttribute('width', String(Math.max(1, bw - 1.5)));
    r.setAttribute('height', String(h));
    r.setAttribute('class', opts.cls ?? 'bar');
    svg.appendChild(r);
  });
  const axis = document.createElementNS(NS, 'line');
  Object.entries({ x1: '4', x2: String(W - 4), y1: String(H - pad), y2: String(H - pad), class: 'axis' }).forEach(([k, v]) => axis.setAttribute(k, v));
  svg.appendChild(axis);
  const lab = document.createElementNS(NS, 'text');
  Object.entries({ x: '4', y: String(H - 6), class: 'lbl' }).forEach(([k, v]) => lab.setAttribute(k, v));
  lab.textContent = opts.label;
  svg.appendChild(lab);
  const pk = document.createElementNS(NS, 'text');
  Object.entries({ x: String(W - 4), y: '14', class: 'lbl pk', 'text-anchor': 'end' }).forEach(([k, v]) => pk.setAttribute(k, v));
  pk.textContent = opts.peak;
  svg.appendChild(pk);
  if (opts.markAt !== undefined) {
    const x = 4 + opts.markAt * bw;
    const m = document.createElementNS(NS, 'line');
    Object.entries({ x1: String(x), x2: String(x), y1: '4', y2: String(H - pad), class: 'mark' }).forEach(([k, v]) => m.setAttribute(k, v));
    svg.appendChild(m);
    const t = document.createElementNS(NS, 'text');
    Object.entries({ x: String(x + 4), y: '14', class: 'lbl' }).forEach(([k, v]) => t.setAttribute(k, v));
    t.textContent = opts.markLabel ?? '';
    svg.appendChild(t);
  }
  return svg;
}

function result(): HTMLParagraphElement {
  return el('p', { class: 'anim-result', 'aria-live': 'polite' });
}

// ---- cache-stampede ----
function cacheStampede(body: HTMLElement, used: () => void): void {
  const rate = slider('Requests to this server every 100 ms:', 5, 100, 5, 40, 'requests');
  const coalesce = toggle('Request coalescing (one query per key, on this server)', false);
  const chart = el('div', { class: 'anim-chart-wrap' });
  const res = result();
  const draw = () => {
    const ticks = simulateStampede({ ticks: 30, requestsPerTick: rate.value(), expireAt: 8, recomputeTicks: 6, coalesce: coalesce.on() });
    const s = summary(ticks);
    chart.replaceChildren(
      bars(
        ticks.map((t) => t.inFlight),
        { max: 100 * 6, label: 'Queries from one app server (3 seconds)', peak: `peak: ${s.peakInFlight} at once`, markAt: 8, markLabel: 'key expires', cls: coalesce.on() ? 'bar ok' : 'bar bad' },
      ),
    );
    res.textContent = coalesce.on()
      ? `With coalescing: 1 database query from this server. ${rate.value() * 6 - 1} requests wait a moment for its answer. (With many servers, each sends one; a lease can make it one in total.)`
      : `Without coalescing: ${s.totalQueries} database queries for the same value, up to ${s.peakInFlight} at once.`;
  };
  for (const c of [rate.input, coalesce.el]) c.addEventListener(c === rate.input ? 'input' : 'click', () => (used(), draw()));
  const ctrls = el('div', { class: 'anim-ctrls' });
  ctrls.append(rate.wrap, coalesce.el);
  body.append(ctrls, chart, res);
  draw();
}

// ---- replication-lag ----
function replicationLag(body: HTMLElement, used: () => void): void {
  const lag = slider('Replication lag:', 0, 2000, 100, 800, 'ms');
  const after = slider('You read again after:', 0, 2000, 100, 300, 'ms');
  const ryw = toggle('Read-your-writes (send your reads to the leader for 1 s)', false);
  const lanes = el('div', { class: 'anim-lanes' });
  const res = result();
  const draw = () => {
    const r = simulateRead({ lagMs: lag.value(), readAfterMs: after.value(), readYourWrites: ryw.on(), rywWindowMs: 1000 });
    const pct = (ms: number) => `${Math.min(100, (ms / 2000) * 100)}%`;
    lanes.innerHTML = `
      <div class="lane"><span class="who">Leader</span><div class="tl"><i class="ev w" style="left:0%">write: name = "Asha"</i></div></div>
      <div class="lane"><span class="who">Follower</span><div class="tl"><i class="ev a" style="left:${pct(lag.value())}">gets the write</i></div></div>
      <div class="lane"><span class="who">You</span><div class="tl"><i class="ev r ${r.stale ? 'bad' : 'ok'}" style="left:${pct(after.value())}">read from ${r.readFrom}</i></div></div>`;
    res.textContent = r.stale
      ? `Stale read: the follower still shows the old name. You just changed it, so it looks like your change was lost.`
      : `Fresh read from the ${r.readFrom}: you see "Asha".`;
    res.className = `anim-result ${r.stale ? 'bad' : 'ok'}`;
  };
  for (const c of [lag.input, after.input]) c.addEventListener('input', () => (used(), draw()));
  ryw.el.addEventListener('click', () => (used(), draw()));
  const ctrls = el('div', { class: 'anim-ctrls' });
  ctrls.append(lag.wrap, after.wrap, ryw.el);
  body.append(ctrls, lanes, res);
  draw();
}

// ---- rollout ----
function rollout(body: HTMLElement, used: () => void): void {
  const seg = el('div', { class: 'btn-row', role: 'group', 'aria-label': 'How to push the change' });
  const g = el('button', { type: 'button', class: 'chip', 'aria-pressed': 'true' }, 'Everywhere at once');
  const s = el('button', { type: 'button', class: 'chip', 'aria-pressed': 'false' }, 'In stages: 1%, 10%, 50%, 100%');
  seg.append(g, s);
  const detect = slider('Alerts notice errors after:', 1, 30, 1, 5, 'min');
  const chart = el('div', { class: 'anim-chart-wrap' });
  const res = result();
  let strategy: 'global' | 'staged' = 'global';
  const draw = () => {
    const r = simulateRollout({ strategy, stages: [1, 10, 50, 100], bakeMinutes: 10, detectMinutes: detect.value(), rollbackMinutes: 5, minutes: 60 });
    chart.replaceChildren(bars(r.timeline.map((x) => x.affectedPct), { max: 100, label: 'Users getting errors, per minute (1 hour)', peak: `peak: ${r.peakPct}% of users`, markAt: r.fixedAt, markLabel: 'fixed', cls: strategy === 'global' ? 'bar bad' : 'bar ok' }));
    res.textContent = `Peak: ${r.peakPct}% of users get errors. Total harm: ${r.userMinutes.toLocaleString('en')} “user-percent minutes”. Fixed at minute ${r.fixedAt}.`;
  };
  const pick = (to: 'global' | 'staged') => {
    strategy = to;
    g.setAttribute('aria-pressed', String(to === 'global'));
    s.setAttribute('aria-pressed', String(to === 'staged'));
    used();
    draw();
  };
  g.addEventListener('click', () => pick('global'));
  s.addEventListener('click', () => pick('staged'));
  detect.input.addEventListener('input', () => (used(), draw()));
  const ctrls = el('div', { class: 'anim-ctrls' });
  ctrls.append(seg, detect.wrap);
  body.append(ctrls, chart, res);
  draw();
}

const VIEWS: Record<string, (body: HTMLElement, used: () => void) => void> = {
  'cache-stampede': cacheStampede,
  'replication-lag': replicationLag,
  rollout,
};

export function initAnims(lessonId: string): void {
  document.querySelectorAll<HTMLElement>('[data-anim]').forEach((host) => {
    const id = host.dataset.anim!;
    const view = VIEWS[id];
    if (!view) return;
    let used = false;
    view(host.querySelector('.anim-body')!, () => {
      if (!used) {
        used = true;
        track('stepthrough_used', { lesson_id: lessonId, diagram_id: `anim-${id}` });
      }
    });
  });
}
