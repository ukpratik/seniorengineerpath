// Tag filter shared by the incident and case-study libraries.
export function initTagFilter(noun: string): void {
  const chips = [...document.querySelectorAll<HTMLButtonElement>('[data-tag]')];
  const cards = [...document.querySelectorAll<HTMLElement>('li[data-tags]')];
  const count = document.querySelector<HTMLElement>('[data-count-inc]')!;
  const apply = (tag: string) => {
    chips.forEach((c) => c.setAttribute('aria-pressed', String(c.dataset.tag === tag)));
    let n = 0;
    for (const c of cards) {
      const show = !tag || c.dataset.tags!.split(' ').includes(tag);
      c.hidden = !show;
      if (show) n++;
    }
    count.textContent = tag ? `${n} ${noun} tagged “${tag}”` : `${n} ${noun}`;
  };
  chips.forEach((c) =>
    c.addEventListener('click', () => {
      apply(c.dataset.tag!);
      history.replaceState(null, '', c.dataset.tag ? `?tag=${c.dataset.tag}` : location.pathname);
    }),
  );
  const t = new URLSearchParams(location.search).get('tag');
  if (t && chips.some((c) => c.dataset.tag === t)) apply(t);
  if (location.hash) document.getElementById(decodeURIComponent(location.hash.slice(1)))?.closest('li')?.classList.add('hl');
}
