// Astro integration: runs the content gate before every build and, after a
// successful build, records published slugs and writes alias redirects (US-017).
import { readFileSync, writeFileSync, existsSync, appendFileSync } from 'node:fs';
import { join } from 'node:path';
import { fileURLToPath } from 'node:url';
import type { AstroIntegration } from 'astro';
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
      },
    },
  };
}
