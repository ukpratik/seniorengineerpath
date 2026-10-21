// RSS of published lessons (US-015), newest review first.
import rss from '@astrojs/rss';
import type { APIContext } from 'astro';
import { getCollection } from 'astro:content';
import { siteData } from '../lib/site-data.ts';

export async function GET(context: APIContext) {
  const d = await siteData();
  const lessons = (await getCollection('lessons', (l) => l.data.status === 'published')).sort((a, b) =>
    (b.data.reviewed_on ?? '').localeCompare(a.data.reviewed_on ?? ''),
  );
  return rss({
    title: 'SeniorPath: lessons for software engineers',
    description: 'New lessons on SeniorPath: how real systems are built, broken and fixed, in simple English.',
    site: context.site!,
    trailingSlash: false,
    items: lessons.map((l) => {
      const t = d.byId.get(l.id)!;
      return { title: t.title, description: l.data.subtitle, link: `/learn/${t.slug}`, pubDate: new Date(`${l.data.reviewed_on}T00:00:00Z`) };
    }),
  });
}
