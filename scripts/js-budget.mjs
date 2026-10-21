// NFR-002: gzip size of the JavaScript each built page loads.
// ≤ 50 KB per page; ≤ 150 KB on pages with a custom animation (data-anim).
import { readFileSync, readdirSync, statSync, existsSync } from 'node:fs';
import { join } from 'node:path';
import { gzipSync } from 'node:zlib';

const dist = 'dist';
const LIMIT = 50 * 1024;
const ANIM_LIMIT = 150 * 1024;
const size = new Map();
const gz = (p) => size.get(p) ?? size.set(p, gzipSync(readFileSync(p)).length).get(p);
const html = (d) => readdirSync(d).flatMap((f) => (statSync(join(d, f)).isDirectory() ? html(join(d, f)) : f.endsWith('.html') ? [join(d, f)] : []));

// Follow static imports so a page's total includes shared chunks.
function closure(file, seen = new Set()) {
  if (seen.has(file) || !existsSync(file)) return seen;
  seen.add(file);
  for (const m of readFileSync(file, 'utf8').matchAll(/(?:import|from)\s*["']\.?\.?\/?([^"']+\.js)["']/g)) closure(join(dist, '_astro', m[1].split('/').pop()), seen);
  return seen;
}

let worst = { page: '', kb: 0 };
let failed = 0;
for (const page of html(dist)) {
  const src = readFileSync(page, 'utf8');
  const files = new Set();
  for (const m of src.matchAll(/<script[^>]*\bsrc="(\/_astro\/[^"]+\.js)"/g)) closure(join(dist, m[1]), files);
  const inline = [...src.matchAll(/<script(?![^>]*\bsrc=)[^>]*>([\s\S]*?)<\/script>/g)].reduce((n, m) => n + gzipSync(m[1]).length, 0);
  const total = [...files].reduce((n, f) => n + gz(f), 0) + inline;
  const limit = src.includes('data-anim') ? ANIM_LIMIT : LIMIT;
  if (total > worst.kb * 1024) worst = { page, kb: total / 1024 };
  if (total > limit) {
    failed++;
    console.error(`over budget: ${page} ${(total / 1024).toFixed(1)} KB > ${limit / 1024} KB`);
  }
}
console.log(`JS budget: largest page ${worst.page} at ${worst.kb.toFixed(1)} KB gzip`);
process.exit(failed ? 1 : 0);
