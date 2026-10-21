// Astro integration: runs the content gate before every build and, after a
// successful build, records published slugs and writes alias redirects (US-017).
import { readFileSync, writeFileSync, existsSync, appendFileSync, readdirSync, statSync } from 'node:fs';
import { createHash } from 'node:crypto';
import { join } from 'node:path';
import { fileURLToPath } from 'node:url';
import type { AstroIntegration } from 'astro';
import { UMAMI } from '../config.ts';
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

function htmlFiles(dir: string): string[] {
  return readdirSync(dir).flatMap((f) => {
    const p = join(dir, f);
    return statSync(p).isDirectory() ? htmlFiles(p) : f.endsWith('.html') ? [p] : [];
  });
}

// Content Security Policy with a hash for every inline script in the build (NFR-011).
export function securityHeaders(distDir: string): string {
  const hashes = new Set<string>();
  for (const f of htmlFiles(distDir)) {
    for (const m of readFileSync(f, 'utf8').matchAll(/<script(?![^>]*\bsrc=)[^>]*>([\s\S]*?)<\/script>/g)) {
      if (m[1].trim()) hashes.add(`'sha256-${createHash('sha256').update(m[1]).digest('base64')}'`);
    }
  }
  const umami = UMAMI.websiteId ? new URL(UMAMI.src).origin : '';
  const csp = [
    "default-src 'self'",
    `script-src 'self' ${[...hashes].join(' ')} ${umami} 'wasm-unsafe-eval'`.replace(/\s+/g, ' ').trim(),
    "style-src 'self' 'unsafe-inline'",
    "img-src 'self' data:",
    "font-src 'self'",
    `connect-src 'self' ${umami}`.trim(),
    "frame-ancestors 'none'",
    "base-uri 'self'",
    "form-action 'self'",
    "object-src 'none'",
  ].join('; ');
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
        writeFileSync(join(fileURLToPath(dir), '_headers'), securityHeaders(fileURLToPath(dir)));
        logger.info('wrote _headers with CSP hashes for inline scripts');
      },
    },
  };
}
