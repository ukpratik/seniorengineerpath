// Astro integration: runs the content gate before every build and, after a
// successful build, records published slugs and writes alias redirects (US-017).
import { readFileSync, writeFileSync, existsSync, appendFileSync, readdirSync, statSync } from 'node:fs';
import { createHash } from 'node:crypto';
import { join } from 'node:path';
import { fileURLToPath } from 'node:url';
import type { AstroIntegration } from 'astro';
import { UMAMI, BASE_PATH } from '../config.ts';
import { loadContent } from './load.ts';
import { runGate, formatIssues, publishedLessons } from './gate.ts';

export const MANIFEST = join(process.cwd(), 'slug-manifest.json');

export function readManifest(): string[] {
  return existsSync(MANIFEST) ? (JSON.parse(readFileSync(MANIFEST, 'utf8')) as string[]) : [];
}

export function gateNow(): { ok: boolean; report: string } {
  const c = loadContent();
  const r = runGate(c, { today: new Date().toISOString().slice(0, 10), publishedSlugs: readManifest() });
  return { ok: r.errors.length === 0, report: formatIssues(r) };
}

function walk(dir: string, ext: string): string[] {
  return readdirSync(dir).flatMap((f) => {
    const p = join(dir, f);
    return statSync(p).isDirectory() ? walk(p, ext) : f.endsWith(ext) ? [p] : [];
  });
}
const htmlFiles = (dir: string) => walk(dir, '.html');

// Inline scripts that run. JSON-LD (type="application/ld+json") is data, never executed, so it needs no hash.
const INLINE_SCRIPT = /<script(?![^>]*\b(?:src=|type="application\/ld\+json"))[^>]*>([\s\S]*?)<\/script>/g;

function scriptHashes(html: string, into = new Set<string>()): Set<string> {
  for (const m of html.matchAll(INLINE_SCRIPT)) {
    if (m[1].trim()) into.add(`'sha256-${createHash('sha256').update(m[1]).digest('base64')}'`);
  }
  return into;
}

function cspFor(hashes: Set<string>, opts: { meta?: boolean } = {}): string {
  const umami = UMAMI.websiteId ? new URL(UMAMI.src).origin : '';
  return [
    "default-src 'self'",
    `script-src 'self' ${[...hashes].join(' ')} ${umami} 'wasm-unsafe-eval'`.replace(/\s+/g, ' ').trim(),
    "style-src 'self' 'unsafe-inline'",
    "img-src 'self' data:",
    "font-src 'self'",
    `connect-src 'self' ${umami}`.trim(),
    // Browsers ignore frame-ancestors in a <meta> CSP (and warn), so only the header carries it.
    ...(opts.meta ? [] : ["frame-ancestors 'none'"]),
    "base-uri 'self'",
    "form-action 'self'",
    "object-src 'none'",
  ].join('; ');
}

// Content Security Policy with a hash for every inline script in the build (NFR-011).
// Written to _headers for hosts that read it (Cloudflare Pages).
export function securityHeaders(distDir: string): string {
  const hashes = new Set<string>();
  for (const f of htmlFiles(distDir)) scriptHashes(readFileSync(f, 'utf8'), hashes);
  const csp = cspFor(hashes);
  return `/*
  Content-Security-Policy: ${csp}
  Referrer-Policy: strict-origin-when-cross-origin
  X-Content-Type-Options: nosniff
  X-Frame-Options: DENY
  Permissions-Policy: camera=(), microphone=(), geolocation=(), payment=(), usb=(), interest-cohort=()
/fonts/*
  Cache-Control: public, max-age=31536000, immutable
/_astro/*
  Cache-Control: public, max-age=31536000, immutable
/icons.svg
  Cache-Control: public, max-age=86400
`;
}

// GitHub Pages sends no custom headers, so each page also carries its CSP in a <meta> tag,
// with the hashes of only that page's inline scripts (D-043).
export function addMetaCsp(html: string): string {
  const tag = `<meta http-equiv="Content-Security-Policy" content="${cspFor(scriptHashes(html), { meta: true })}">`;
  return html.replace(/<meta charset="utf-8"\s*\/?>/i, (m) => `${m}${tag}`);
}

// The site is written with root-relative links ('/learn/x', '/icons.svg#i-map', and the same in
// lesson Markdown, which the Android export reads too). When the site is served under a path
// (GitHub Pages: /seniorengineerpath), prefix every root-relative URL in HTML attributes and CSS.
// Script bodies are left alone; client scripts use url() from src/scripts/base.ts.
export function prefixBase(text: string, base: string, kind: 'html' | 'css'): string {
  if (!base) return text;
  const skip = (p: string) => p.startsWith('//') || p === base || p.startsWith(`${base}/`) || p.startsWith(`${base}#`) || p.startsWith(`${base}?`);
  if (kind === 'css') return text.replace(/url\((["']?)(\/[^"')]*)\1\)/g, (m, q, p) => (skip(p) ? m : `url(${q}${base}${p}${q})`));
  const parts = text.split(/(<script\b[\s\S]*?<\/script>)/i);
  return parts
    .map((part, i) =>
      i % 2 === 1 && !/^<script\b[^>]*\bsrc=/i.test(part)
        ? part
        : part.replace(/(\s(?:href|src|action|poster|xlink:href)=)(["'])(\/[^"']*)\2/g, (m, attr, q, p) => (skip(p) ? m : `${attr}${q}${base}${p}${q}`)),
    )
    .join('');
}

// Hosts without _redirects support (GitHub Pages) get a small HTML page per redirect instead.
export function redirectPage(to: string): string {
  return `<!doctype html><html lang="en"><head><meta charset="utf-8"><title>Moved</title><link rel="canonical" href="${to}"><meta name="robots" content="noindex"><meta http-equiv="refresh" content="0; url=${to}"></head><body><p>This page moved to <a href="${to}">${to}</a>.</p></body></html>\n`;
}

function writeRedirectPages(dist: string, base: string): number {
  const file = join(dist, '_redirects');
  if (!existsSync(file)) return 0;
  let n = 0;
  for (const line of readFileSync(file, 'utf8').split('\n')) {
    const m = line.trim().match(/^(\/[^\s#]+)\s+(\/\S*)\s+30[12]$/);
    if (!m) continue;
    const page = join(dist, `${m[1].replace(/^\//, '')}.html`);
    if (existsSync(page)) continue; // a real page wins
    writeFileSync(page, redirectPage(`${base}${m[2]}`));
    n++;
  }
  return n;
}

export function contentGate(): AstroIntegration {
  return {
    name: 'seniorpath-content-gate',
    hooks: {
      'astro:build:start': ({ logger }) => {
        const { ok, report } = gateNow();
        if (!ok) {
          logger.error(report);
          throw new Error('Content gate failed. Fix the errors above, then build again.');
        }
        logger.info(report);
      },
      'astro:build:done': ({ dir, logger }) => {
        const c = loadContent();
        const live = new Set(publishedLessons(c).map((l) => l.data.topic));
        const slugs = c.topics.filter((t) => live.has(t.id)).map((t) => t.slug);
        const merged = [...new Set([...readManifest(), ...slugs])].sort();
        writeFileSync(MANIFEST, JSON.stringify(merged, null, 2) + '\n');

        const redirects = c.topics.flatMap((t) => t.aliases.map((a) => `/learn/${a} /learn/${t.slug} 301`));
        if (redirects.length) appendFileSync(join(fileURLToPath(dir), '_redirects'), redirects.join('\n') + '\n');
        logger.info(`slug manifest: ${merged.length} published slugs; ${redirects.length} alias redirects`);
        // Rendering must never drop text: every [@citation] in a published lesson
        // must appear on its page (guards against QA2-01-style bugs).
        const missing: string[] = [];
        for (const l of publishedLessons(c)) {
          const topic = c.topics.find((t) => t.id === l.data.topic);
          const page = join(fileURLToPath(dir), 'learn', `${topic?.slug}.html`);
          if (!topic || !existsSync(page)) continue;
          const want = (l.body.match(/\[@[a-z0-9-]+\]/g) ?? []).length;
          const got = (readFileSync(page, 'utf8').match(/<sup><a href="#src-/g) ?? []).length;
          if (got !== want) missing.push(`${l.id}: ${want} citations in the lesson, ${got} on the page`);
        }
        if (missing.length) throw new Error(`Lesson text was dropped while rendering:\n  ${missing.join('\n  ')}`);
        const dist = fileURLToPath(dir);
        const moved = writeRedirectPages(dist, BASE_PATH);
        for (const f of walk(dist, '.css')) writeFileSync(f, prefixBase(readFileSync(f, 'utf8'), BASE_PATH, 'css'));
        for (const f of htmlFiles(dist)) writeFileSync(f, addMetaCsp(prefixBase(readFileSync(f, 'utf8'), BASE_PATH, 'html')));
        writeFileSync(join(dist, '_headers'), securityHeaders(dist));
        logger.info(`base path '${BASE_PATH || '/'}' applied; ${moved} redirect pages; CSP in _headers and in each page's <meta>`);
      },
    },
  };
}
