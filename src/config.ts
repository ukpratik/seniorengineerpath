// Site settings. Nothing here is secret (LLD §7).
export const SITE = {
  name: 'SeniorPath',
  tagline: 'for software engineers',
  url: 'https://seniorpath.dev',
  repo: 'ukpratik/seniorengineerpath',
  author: 'Pratik Ukey',
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
