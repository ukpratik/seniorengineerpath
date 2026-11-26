// Sitemap of indexable pages only: coming-soon topics are left out (NFR-012).
import { getCollection } from 'astro:content';
import { siteData } from '../lib/site-data.ts';
import { absUrl } from '../config.ts';

export async function GET() {
  const d = await siteData();
  const lessons = await getCollection('lessons', (l) => l.data.status === 'published');
  const lastmod = new Map(lessons.map((l) => [l.id, l.data.major_revised_on ?? l.data.reviewed_on]));
  const urls: [string, string | undefined][] = [
    ...['/', '/map', '/glossary', '/incidents', '/cases', '/about'].map((u): [string, undefined] => [u, undefined]),
    ...d.paths.map((p): [string, undefined] => [`/paths/${p.slug}`, undefined]),
    ...d.topics.filter((t) => t.written).map((t): [string, string | undefined] => [`/learn/${t.slug}`, lastmod.get(t.id)]),
  ];
  const body = `<?xml version="1.0" encoding="UTF-8"?>\n<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">\n${urls
    .map(([u, m]) => `  <url><loc>${absUrl(u)}</loc>${m ? `<lastmod>${m}</lastmod>` : ''}</url>`)
    .join('\n')}\n</urlset>\n`;
  return new Response(body, { headers: { 'content-type': 'application/xml' } });
}
