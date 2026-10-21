// Step-through diagram (US-006). The server renders every frame and caption;
// this shows one frame at a time with Back/Next, ←/→ and an optional Play.
import { icon } from './ui.ts';

const PLAY_MS = 3500;

export function initStepper(fig: HTMLElement, onFirstUse: () => void): void {
  const caps = [...fig.querySelectorAll('.caps-nojs li')].map((li) => li.textContent ?? '');
  const frames = [...fig.querySelectorAll<SVGGElement>('.f')];
  const n = caps.length;
  const prev = fig.querySelector<HTMLButtonElement>('[data-prev]')!;
  const next = fig.querySelector<HTMLButtonElement>('[data-next]')!;
  const play = fig.querySelector<HTMLButtonElement>('[data-play]')!;
  const cap = fig.querySelector<HTMLElement>('[data-cap]')!;
  const count = fig.querySelector<HTMLElement>('[data-count]')!;
  const dots = [...fig.querySelectorAll('.dots i')];
  let k = 1;
  let timer: number | undefined;
  let used = false;

  const show = (to: number) => {
    k = Math.max(1, Math.min(n, to));
    for (const g of frames) {
      const f = Number(g.dataset.f);
      g.classList.toggle('now', f === k);
      g.classList.toggle('later', f > k);
    }
    cap.innerHTML = '';
    const b = document.createElement('b');
    b.textContent = `Step ${k}.`;
    cap.append(b, ` ${caps[k - 1]}`);
    count.textContent = `Step ${k} of ${n}`;
    prev.disabled = k === 1;
    next.disabled = k === n;
    dots.forEach((d, i) => d.classList.toggle('on', i < k));
  };
  const firstUse = () => {
    if (!used) {
      used = true;
      onFirstUse();
    }
  };
  const stop = () => {
    clearInterval(timer);
    timer = undefined;
    play.innerHTML = `${icon('play', true)}<span>Play</span>`;
  };

  prev.addEventListener('click', () => {
    stop();
    show(k - 1);
  });
  next.addEventListener('click', () => {
    stop();
    firstUse();
    show(k + 1);
  });
  play.addEventListener('click', () => {
    if (timer) return stop();
    firstUse();
    if (k === n) show(1);
    play.innerHTML = `${icon('pause', true)}<span>Pause</span>`;
    timer = window.setInterval(() => (k >= n ? stop() : show(k + 1)), PLAY_MS);
  });
  fig.addEventListener('keydown', (e) => {
    if (e.key !== 'ArrowRight' && e.key !== 'ArrowLeft') return;
    e.preventDefault();
    stop();
    if (e.key === 'ArrowRight') firstUse();
    show(k + (e.key === 'ArrowRight' ? 1 : -1));
  });
  show(1);
}
