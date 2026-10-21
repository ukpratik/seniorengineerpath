// Small id → title manifest for pages that show stored progress (Home, Progress, Map). LLD §4.
import type { APIRoute } from 'astro';
import { siteData } from '../../lib/site-data.ts';

export const GET: APIRoute = async () => {
  const d = await siteData();
  const topics = Object.fromEntries(
    d.topics.map((t) => [t.id, { t: t.title, s: t.slug, tr: t.trackTitle, ti: t.trackIndex, l: t.level, w: t.written ? 1 : 0 }]),
  );
  const paths = Object.fromEntries(d.paths.map((p) => [p.id, { t: p.title, s: p.slug, steps: p.steps }]));
  return new Response(JSON.stringify({ topics, paths, defaultPath: d.defaultPath.id, tracks: d.tracks.map((t) => [t.id, t.title, t.writtenCount]) }), {
    headers: { 'content-type': 'application/json' },
  });
};
