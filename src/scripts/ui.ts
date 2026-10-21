// Shared behaviour on every page: theme, dialogs (bottom sheets), toast,
// glossary sheet and the progress box in the desktop rail.
import { createStore } from './store.ts';

export const store = createStore();

export function icon(name: string, small = false): string {
  return `<svg class="i${small ? ' sm' : ''}" aria-hidden="true"><use href="/icons.svg#i-${name}"/></svg>`;
}

// ---- Theme ----
function currentTheme(): 'light' | 'dark' {
  const t = document.documentElement.getAttribute('data-theme');
  if (t === 'light' || t === 'dark') return t;
  return matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
}
function paintThemeButtons(): void {
  const t = currentTheme();
  document.querySelectorAll<HTMLButtonElement>('.theme-toggle').forEach((b) => {
    b.setAttribute('aria-label', t === 'dark' ? 'Use light theme' : 'Use dark theme');
    b.innerHTML = icon(t === 'dark' ? 'sun' : 'moon');
  });
}
export function toggleTheme(): void {
  const next = currentTheme() === 'dark' ? 'light' : 'dark';
  document.documentElement.setAttribute('data-theme', next);
  store.setTheme(next);
  paintThemeButtons();
}

// ---- Toast ----
let toastTimer: number | undefined;
export function toast(msg: string): void {
  let el = document.getElementById('toast');
  if (!el) {
    el = document.createElement('div');
    el.id = 'toast';
    el.className = 'toast';
    el.setAttribute('role', 'status');
    document.body.appendChild(el);
  }
  el.textContent = msg;
  el.classList.add('on');
  clearTimeout(toastTimer);
  toastTimer = window.setTimeout(() => el!.classList.remove('on'), 2800);
}

// ---- Dialogs: one bottom-sheet pattern, native showModal() ----
let opener: HTMLElement | null = null;
export function openDialog(d: HTMLDialogElement, from?: HTMLElement | null): void {
  opener = from ?? (document.activeElement as HTMLElement | null);
  if (!d.dataset.wired) {
    d.dataset.wired = '1';
    d.addEventListener('click', (e) => {
      if (e.target === d) d.close();
    });
    d.addEventListener('close', () => opener?.focus({ preventScroll: true }));
  }
  d.showModal();
  d.querySelector<HTMLElement>('[data-close]')?.focus();
}
export function closeDialog(returnFocus = true): void {
  const d = document.querySelector<HTMLDialogElement>('dialog[open]');
  if (!d) return;
  if (!returnFocus) opener = null;
  d.close();
}

// ---- Glossary sheet (definitions are embedded per page as <template data-term>) ----
function openTerm(a: HTMLAnchorElement): boolean {
  const d = document.getElementById('gloss') as HTMLDialogElement | null;
  const tpl = document.querySelector<HTMLTemplateElement>(`template[data-term="${a.dataset.term}"]`);
  if (!d || !tpl) return false;
  d.querySelector('.g-title')!.textContent = tpl.dataset.title ?? '';
  d.querySelector('.g-def')!.textContent = tpl.content.textContent?.trim() ?? '';
  d.querySelector<HTMLAnchorElement>('.more')!.href = a.getAttribute('href') ?? '/glossary';
  openDialog(d, a);
  return true;
}

// ---- Rail progress box ----
function paintRail(): void {
  const box = document.querySelector<HTMLElement>('[data-rail-progress]');
  if (!box) return;
  const total = Number(box.dataset.total ?? 0);
  const s = store.load();
  if (!s) {
    box.querySelector('[data-rail-count]')!.textContent = 'Progress can’t be saved in this browser';
    return;
  }
  const done = Object.keys(s.done).length;
  box.querySelector('[data-rail-count]')!.textContent = `${done} of ${total} lessons done`;
  box.querySelector<HTMLElement>('.bar > i')!.style.width = `${total ? Math.round((done / total) * 100) : 0}%`;
}

export function initUi(): void {
  paintThemeButtons();
  paintRail();
  document.addEventListener('click', (e) => {
    const t = e.target as HTMLElement;
    const term = t.closest<HTMLAnchorElement>('a.term');
    if (term && openTerm(term)) {
      e.preventDefault();
      return;
    }
    if (t.closest('[data-close]')) {
      closeDialog();
      return;
    }
    if (t.closest('.theme-toggle')) toggleTheme();
  });
}
