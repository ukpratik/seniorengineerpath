// Lesson page behaviour (US-005, 008, 009, 014, 018). Content is static HTML;
// this only adds progress, the stepper, the quiz and the section states.
import { store, icon, toast, openDialog, closeDialog, initUi } from './ui.ts';
import { initStepper } from './stepthrough.ts';
import { track } from './analytics.ts';
import { nthBucket } from './store.ts';

initUi();

const page = document.querySelector<HTMLElement>('[data-lesson]');
if (page) {
  const id = page.dataset.lesson!;
  const nextTitle = page.dataset.nextTitle ?? '';
  const majorRevised = page.dataset.majorRevised ?? '';
  const reduceMotion = matchMedia('(prefers-reduced-motion: reduce)').matches;
  store.setLastOpened(id);
  track('lesson_view', { lesson_id: id });

  // ---- "Updated since you finished it" ----
  const doneAt = store.load()?.done[id];
  if (doneAt && majorRevised && majorRevised > doneAt.slice(0, 10)) {
    document.querySelector<HTMLElement>('[data-updated]')?.removeAttribute('hidden');
  }

  // ---- Section states and contents (one IntersectionObserver, LLD §4) ----
  const secs = [...document.querySelectorAll<HTMLElement>('.sec')];
  const lists = [...document.querySelectorAll<HTMLOListElement>('.toc-list')];
  const pill = document.querySelector<HTMLElement>('[data-sec-count]');
  const reached = new Set<number>();
  const paint = (current: number) => {
    secs.forEach((s, i) => {
      s.classList.toggle('reached', i <= current);
      s.classList.toggle('read', i < current);
    });
    for (const ol of lists) {
      ol.querySelectorAll('a').forEach((a, i) => {
        a.classList.toggle('is-read', i < current);
        a.setAttribute('aria-current', String(i === current));
        a.querySelector('.rs')!.textContent = i === current ? 'You are here' : i < current ? 'Read' : '';
      });
    }
    if (pill) pill.textContent = `${current + 1} / ${secs.length}`;
  };
  let current = 0;
  const io = new IntersectionObserver(
    (entries) => {
      for (const e of entries) {
        const i = secs.indexOf(e.target as HTMLElement);
        if (e.isIntersecting) reached.add(i);
      }
      const top = Math.max(...[...reached].filter((i) => secs[i].getBoundingClientRect().top < innerHeight * 0.62), 0);
      if (top !== current) {
        current = top;
        paint(current);
      }
      const check = secs.findIndex((s) => s.dataset.section === 'Check yourself');
      if (check >= 0 && reached.has(check) && !page.dataset.reachedCheck) {
        page.dataset.reachedCheck = '1';
        track('reached_check_yourself', { lesson_id: id });
      }
    },
    { rootMargin: '0px 0px -38% 0px' },
  );
  secs.forEach((s) => io.observe(s));
  paint(0);

  const jump = (i: number) => {
    const h = secs[i]?.querySelector<HTMLElement>('h2');
    if (!h) return;
    secs[i].scrollIntoView({ behavior: reduceMotion ? 'auto' : 'smooth', block: 'start' });
    h.setAttribute('tabindex', '-1');
    h.focus({ preventScroll: true });
  };
  document.addEventListener('click', (e) => {
    const t = e.target as HTMLElement;
    if (t.closest('[data-toc-open]')) {
      openDialog(document.getElementById('tocSheet') as HTMLDialogElement, t.closest('button'));
      return;
    }
    const a = t.closest<HTMLAnchorElement>('.toc-list a');
    if (a) {
      e.preventDefault();
      closeDialog(false);
      jump(Number(a.dataset.i));
      return;
    }
    const fn = t.closest<HTMLAnchorElement>('sup a');
    if (fn) {
      e.preventDefault();
      const deeper = document.querySelector<HTMLDetailsElement>('details.deeper');
      if (deeper) deeper.open = true;
      document.querySelector(fn.getAttribute('href')!)?.scrollIntoView({ block: 'center', behavior: reduceMotion ? 'auto' : 'smooth' });
    }
  });

  // ---- Step-through diagrams ----
  document.querySelectorAll<HTMLElement>('[data-stepper]').forEach((fig) =>
    initStepper(fig, () => track('stepthrough_used', { lesson_id: id, diagram_id: fig.id.replace(/^dg-/, '') })),
  );

  // ---- Quiz: instant feedback, nothing stored ----
  document.querySelectorAll<HTMLFieldSetElement>('fieldset.q').forEach((q, qi) => {
    let answered = false;
    q.addEventListener('change', (e) => {
      const input = e.target as HTMLInputElement;
      const ok = input.value === q.dataset.ok;
      q.querySelectorAll('.opt').forEach((o) => o.classList.remove('right', 'wrong'));
      input.closest('.opt')!.classList.add(ok ? 'right' : 'wrong');
      const fb = q.querySelector<HTMLElement>('.fb')!;
      fb.innerHTML = ok ? '<strong class="ok">✓ Correct.</strong> ' : '<strong class="bad">✗ Not quite.</strong> ';
      fb.append(fb.dataset.why ?? '');
      if (!answered) {
        answered = true;
        track('quiz_answer', { lesson_id: id, q: qi + 1, correct: ok });
      }
    });
  });

  // ---- Mark as done ----
  const btn = document.querySelector<HTMLButtonElement>('[data-done]');
  const live = document.querySelector<HTMLElement>('[data-done-live]');
  const setDone = (v: boolean) => {
    btn!.classList.toggle('is-done', v);
    btn!.innerHTML = `${icon(v ? 'check' : 'check-circle')}<span>${v ? 'Done ✓ (undo)' : 'Mark as done'}</span>`;
  };
  if (btn) {
    if (!store.available()) {
      btn.disabled = true;
      btn.innerHTML = '<span>Can’t save progress in this browser</span>';
    } else {
      setDone(store.isDone(id));
      btn.addEventListener('click', () => {
        const v = !store.isDone(id);
        if (v) {
          const r = store.markDone(id);
          if (r?.firstEver) track('lesson_complete', { lesson_id: id, nth_completed_bucket: nthBucket(r.nth) });
          toast(nextTitle ? `Marked as done. Next on your path: ${nextTitle}.` : 'Marked as done.');
          const s = store.load();
          if (s && Object.keys(s.done).length >= 5 && !s.exportPrompted) {
            store.setExportPrompted();
            document.querySelector('[data-export-prompt]')?.removeAttribute('hidden');
          }
        } else {
          store.undoDone(id);
        }
        setDone(v);
        if (live) live.textContent = v ? 'Lesson marked as done.' : 'Lesson marked as not done.';
      });
    }
  }

  // ---- Was this clear? ----
  const clarity = document.querySelector<HTMLElement>('[data-clarity]');
  clarity?.querySelectorAll<HTMLButtonElement>('button').forEach((b) =>
    b.addEventListener('click', () => {
      const yes = b.dataset.v === 'yes';
      track('clarity_vote', { lesson_id: id, value: yes ? 'yes' : 'no' });
      clarity.setAttribute('role', 'status');
      clarity.innerHTML = `${icon('check-circle')}<span>Thanks. ${yes ? 'Glad it helped.' : 'We use these answers to fix unclear lessons.'}</span>`;
    }),
  );
}
