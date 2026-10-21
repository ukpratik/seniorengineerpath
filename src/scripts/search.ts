// Search UI on top of Pagefind (ADR-005). The index loads on first focus.
import { icon } from './ui.ts';
import { track } from './analytics.ts';

interface SubResult { url: string; title: string; excerpt: string }
interface ResultData { url: string; meta: { title?: string }; excerpt: string; filters: { type?: string[] }; sub_results?: SubResult[] }
interface Pagefind {
  init: () => Promise<void>;
  debouncedSearch: (q: string, opts?: object, ms?: number) => Promise<{ results: { data: () => Promise<ResultData> }[] } | null>;
}

const ORDER = ['Lesson', 'Glossary', 'Incident', 'Coming soon'];
const ICON: Record<string, string> = { Lesson: 'book', Glossary: 'text', Incident: 'alert', 'Coming soon': 'dashed' };
const LABEL: Record<string, string> = { Lesson: 'Lessons', Glossary: 'Glossary terms', Incident: 'Incidents', 'Coming soon': 'Coming soon' };

// Pagefind reports file paths; the site uses clean URLs.
const clean = (url: string) => url.replace(/\.html(?=#|$)/, '').replace(/\/index(?=#|$)/, '/');

function bucket(n: number): string {
  return n === 0 ? '0' : n <= 3 ? '1-3' : n <= 10 ? '4-10' : '11+';
}

// Pagefind excerpts contain only <mark> tags; anything else is escaped.
function safeExcerpt(html: string): string {
  return html.replace(/<(?!\/?mark>)/g, '&lt;');
}

export function initSearch(): void {
  const input = document.getElementById('q') as HTMLInputElement;
  const out = document.getElementById('out')!;
  const live = document.getElementById('s-live')!;
  const empty = out.innerHTML;
  let pf: Pagefind | null = null;
  let loading: Promise<Pagefind | null> | null = null;

  const load = () =>
    (loading ??= import(/* @vite-ignore */ '/pagefind/pagefind.js' as string)
      .then(async (m: Pagefind) => {
        await m.init();
        pf = m;
        return m;
      })
      .catch(() => null));

  const showNone = (q: string) => {
    track('search', { result_count_bucket: '0' });
    live.textContent = 'No results.';
    out.innerHTML = '<div class="none-box"><p><strong></strong></p><p class="small">Try a shorter word, or browse the <a href="/map">map</a> and the <a href="/glossary">glossary</a>.</p></div>';
    out.querySelector('strong')!.textContent = `No results for “${q}”.`;
  };

  const render = async (q: string) => {
    if (q.trim().length < 2) {
      out.innerHTML = empty;
      bindSugg();
      live.textContent = '';
      return;
    }
    if (!pf) out.innerHTML = '<div class="skel"></div><div class="skel"></div><div class="skel"></div>';
    const m = await load();
    if (!m) {
      out.innerHTML = '<div class="notice warn">' + icon('alert') + '<p>Search could not load. Check your connection, or browse the <a href="/map">map</a>.</p></div>';
      return;
    }
    const res = await m.debouncedSearch(q, {}, 200);
    if (!res) return; // a newer query replaced this one
    const data = await Promise.all(res.results.slice(0, 30).map((r) => r.data()));
    if (!data.length) {
      showNone(q);
      return;
    }
    const groups = new Map<string, string[]>();
    for (const d of data) {
      const type = d.filters.type?.[0] ?? 'Lesson';
      // Glossary and incident pages hold many entries: list the matching entries.
      const entries = (type === 'Glossary' || type === 'Incident') && d.sub_results ? d.sub_results.filter((x) => x.url.includes('#')).slice(0, 8) : [];
      const items = entries.length ? entries : [{ url: d.url, title: d.meta.title ?? d.url, excerpt: d.excerpt }];
      for (const it of items) {
        const li = `<li><a href="${clean(it.url)}">${icon(ICON[type] ?? 'book')}<span><b>${it.title.replace(/[<>&]/g, '')}</b><span>${safeExcerpt(it.excerpt)}</span></span></a></li>`;
        groups.set(type, [...(groups.get(type) ?? []), li]);
      }
    }
    const types = [...ORDER, ...[...groups.keys()].filter((t) => !ORDER.includes(t))].filter((t) => groups.has(t));
    const shown = types.reduce((n, t) => n + groups.get(t)!.length, 0);
    track('search', { result_count_bucket: bucket(shown) });
    live.textContent = `${shown} results.`;
    out.innerHTML = types
      .map((t) => `<section class="rgroup"><h2>${LABEL[t] ?? t}</h2><ul class="rlist">${groups.get(t)!.join('')}</ul></section>`)
      .join('');
  };

  function bindSugg() {
    out.querySelectorAll<HTMLButtonElement>('[data-sugg]').forEach((b) =>
      b.addEventListener('click', () => {
        input.value = b.textContent ?? '';
        input.focus();
        void render(input.value);
      }),
    );
  }
  bindSugg();
  input.addEventListener('focus', () => void load(), { once: true });
  input.addEventListener('input', () => void render(input.value));
  document.querySelector<HTMLAnchorElement>('[data-close-search]')?.addEventListener('click', (e) => {
    if (history.length > 1 && document.referrer.startsWith(location.origin)) {
      e.preventDefault();
      history.back();
    }
  });
  const q0 = new URLSearchParams(location.search).get('q');
  if (q0) {
    input.value = q0;
    void render(q0);
  }
  input.focus();
}
