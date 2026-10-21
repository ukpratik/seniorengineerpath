import type { APIRoute } from 'astro';
import { getCollection } from 'astro:content';
import { siteData, LEVEL_LABEL } from '../../lib/site-data.ts';
import { ogPng } from '../../lib/og-image.ts';

export async function getStaticPaths() {
  const d = await siteData();
  const lessons = await getCollection('lessons');
  return d.topics
    .filter((t) => t.written)
    .map((t) => ({
      params: { id: t.id },
      props: { title: t.title, subtitle: lessons.find((l) => l.id === t.id)!.data.subtitle, track: t.trackTitle, level: LEVEL_LABEL[t.level] },
    }));
}

export const GET: APIRoute = ({ props }) =>
  new Response(new Uint8Array(ogPng(props as { title: string; subtitle: string; track: string; level: string })), { headers: { 'content-type': 'image/png' } });
