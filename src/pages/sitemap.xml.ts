// Sitemap of indexable pages only: coming-soon topics are left out (NFR-012).
import type { APIContext } from 'astro';
import { siteData } from '../lib/site-data.ts';

export async function GET(context: APIContext) {
  const d = await siteData();
  const urls = ['/', '/map', '/glossary', '/incidents', '/cases', '/about', ...d.paths.map((p) => `/paths/${p.slug}`), ...d.topics.filter((t) => t.written).map((t) => `/learn/${t.slug}`)];
  const body = `<?xml version="1.0" encoding="UTF-8"?>\n<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">\n${urls
    .map((u) => `  <url><loc>${new URL(u, context.site)}</loc></url>`)
    .join('\n')}\n</urlset>\n`;
  return new Response(body, { headers: { 'content-type': 'application/xml' } });
}
