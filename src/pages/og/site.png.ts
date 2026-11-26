// Share image for pages that are not lessons (home, map, paths, glossary, incidents, cases).
import type { APIRoute } from 'astro';
import { siteData } from '../../lib/site-data.ts';
import { ogPng } from '../../lib/og-image.ts';

export const GET: APIRoute = async () => {
  const d = await siteData();
  const png = ogPng({
    title: 'From mid-level to senior software engineer',
    subtitle: `${d.writtenCount} free lessons: how real systems are built, broken and fixed, in simple English.`,
    track: 'Free',
    level: 'No sign-up',
  });
  return new Response(new Uint8Array(png), { headers: { 'content-type': 'image/png' } });
};
