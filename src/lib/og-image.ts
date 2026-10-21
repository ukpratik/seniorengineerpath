// Share image per lesson (US-015): 1200×630 PNG drawn from an SVG template at build time.
import { Resvg } from '@resvg/resvg-js';
import { existsSync } from 'node:fs';
import { join } from 'node:path';
import { escapeHtml } from './html.ts';

// Resolved from the project root: the build bundles this module elsewhere, so a
// path relative to import.meta.url breaks and resvg silently drops all text (BUG-001).
export const FONT = join(process.cwd(), 'src', 'og', 'PlusJakartaSans-wght.ttf');

// Greedy word wrap by an average character width; good enough for titles.
export function wrap(text: string, maxChars: number, maxLines: number): string[] {
  const lines: string[] = [];
  let cur = '';
  for (const w of text.split(/\s+/)) {
    if ((cur + ' ' + w).trim().length > maxChars && cur) {
      lines.push(cur);
      cur = w;
    } else cur = (cur + ' ' + w).trim();
  }
  if (cur) lines.push(cur);
  if (lines.length > maxLines) {
    const kept = lines.slice(0, maxLines);
    kept[maxLines - 1] = kept[maxLines - 1].replace(/\s*\S*$/, '') + '…';
    return kept;
  }
  return lines;
}

export function ogSvg(o: { title: string; subtitle: string; track: string; level: string }): string {
  const title = wrap(o.title, 26, 3);
  const sub = wrap(o.subtitle, 56, 2);
  const titleY = 250;
  const t = title.map((l, i) => `<text x="80" y="${titleY + i * 78}" font-size="66" font-weight="800" fill="#0F172A">${escapeHtml(l)}</text>`).join('');
  const subY = titleY + title.length * 78 + 24;
  const s = sub.map((l, i) => `<text x="80" y="${subY + i * 42}" font-size="32" font-weight="500" fill="#475569">${escapeHtml(l)}</text>`).join('');
  return `<svg xmlns="http://www.w3.org/2000/svg" width="1200" height="630" viewBox="0 0 1200 630">
<rect width="1200" height="630" fill="#F5F7FA"/>
<rect x="0" y="0" width="1200" height="14" fill="#4F46E5"/>
<g transform="translate(80 78)"><rect width="56" height="56" rx="16" fill="#4F46E5"/><path d="M14 38 24.5 24.5l8.75 7 8.75-14" fill="none" stroke="#fff" stroke-width="4.5" stroke-linecap="round" stroke-linejoin="round"/><circle cx="14" cy="38" r="4.5" fill="#fff"/><circle cx="42" cy="17.5" r="4.5" fill="#fff"/></g>
<text x="152" y="116" font-size="34" font-weight="800" fill="#0F172A">SeniorPath</text>
<text x="1120" y="116" font-size="26" font-weight="700" fill="#3730A3" text-anchor="end">${escapeHtml(o.track)} · ${escapeHtml(o.level)}</text>
${t}${s}
<text x="80" y="580" font-size="26" font-weight="700" fill="#475569">Free lessons for software engineers · seniorpath.dev</text>
</svg>`;
}

export function ogPng(o: Parameters<typeof ogSvg>[0]): Buffer {
  if (!existsSync(FONT)) throw new Error(`OG image font not found: ${FONT}`);
  const r = new Resvg(ogSvg(o), { font: { fontFiles: [FONT], loadSystemFonts: false, defaultFontFamily: 'Plus Jakarta Sans' } });
  return r.render().asPng();
}
