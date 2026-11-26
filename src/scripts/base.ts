// Client-side links and fetches must include the base path the site is served under
// ('' on a root domain, '/seniorengineerpath' on GitHub Pages). Astro sets BASE_URL from astro.config.
export const BASE = (import.meta.env.BASE_URL ?? '/').replace(/\/+$/, '');

/** url('/learn/x') → '/seniorengineerpath/learn/x' under a base path; unchanged at the root. */
export const url = (path: string): string => `${BASE}${path}`;
