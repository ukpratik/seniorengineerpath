// Site settings. Nothing here is secret (LLD §7).
export const SITE = {
  name: 'SeniorPath',
  tagline: 'for software engineers',
  // The one place the site's address lives (D-043). Moving to a custom domain is a change to
  // this line only: astro.config derives `site` and `base` from it, and every absolute URL
  // (canonical, sitemap, RSS, Open Graph, JSON-LD, robots.txt) is built with absUrl().
  url: 'https://ukpratik.github.io/seniorengineerpath',
  repo: 'ukpratik/seniorengineerpath',
  author: 'Pratik Ukey',
  publisher: 'UK Group',
  moreApps: 'https://ukpratik.github.io/awesomeaiapplications/',
};

/** Path prefix the site is served under: '' on a root domain, '/seniorengineerpath' on GitHub Pages. */
export const BASE_PATH = new URL(SITE.url).pathname.replace(/\/+$/, '');

/** Host and path without the scheme, for printed credits ("ukpratik.github.io/seniorengineerpath"). */
export const SITE_HOST = SITE.url.replace(/^https?:\/\//, '').replace(/\/+$/, '');

/** Absolute URL of a site path such as '/learn/x' (no base prefix; it is added here). */
export function absUrl(path: string): string {
  return `${SITE.url.replace(/\/+$/, '')}${path.startsWith('/') ? path : `/${path}`}`;
}

// The Android app (D-040, D-042). Store links carry UTM tags so web-driven installs are visible in Play Console.
export const APP = {
  play: 'https://play.google.com/store/apps/details?id=io.github.ukpratik.seniorpath.play&utm_source=seniorpath-web&utm_medium=referral&utm_campaign=site',
  fdroid: 'https://f-droid.org/packages/io.github.ukpratik.seniorpath/',
};

// Umami Cloud (ADR-004). Empty websiteId = analytics off (e.g. local builds).
export const UMAMI = {
  src: 'https://cloud.umami.is/script.js',
  websiteId: process.env.UMAMI_WEBSITE_ID ?? '',
};

export function issueUrl(lessonId: string, pageUrl: string): string {
  const q = new URLSearchParams({
    title: `Mistake in lesson: ${lessonId}`,
    body: `Lesson: ${lessonId}\nPage: ${pageUrl}\n\nWhat is wrong:\n\nWhat it should say (with a source, if you have one):\n`,
    labels: 'content-mistake',
  });
  return `https://github.com/${SITE.repo}/issues/new?${q}`;
}
