// Reads content files straight from disk for the gate script and Markdown plugins.
// (Pages read content through astro:content instead.)
import { readdirSync, readFileSync, existsSync } from 'node:fs';
import { join, basename } from 'node:path';
import { parse as parseYaml } from 'yaml';
import type { z } from 'astro/zod';
import { splitFrontmatter } from './markdown.ts';
import {
  trackSchema,
  topicSchema,
  lessonSchema,
  glossarySchema,
  incidentSchema,
  caseSchema,
  pathSchema,
  diagramSchema,
  type Track,
  type Topic,
  type Lesson,
  type GlossaryEntry,
  type Incident,
  type CaseStudy,
  type Path,
  type DiagramEntry,
} from './schemas.ts';

export const CONTENT_DIR = join(process.cwd(), 'src', 'content');

export interface LoadError {
  file: string;
  message: string;
}

export interface LessonFile {
  file: string;
  id: string;
  data: Lesson;
  body: string;
  bodyLine: number;
}

export interface Content {
  tracks: Track[];
  topics: Topic[];
  lessons: LessonFile[];
  glossary: GlossaryEntry[];
  incidents: Incident[];
  cases: CaseStudy[];
  paths: Path[];
  diagrams: DiagramEntry[];
  banned: string[];
  errors: LoadError[];
}

function files(dir: string, ext: string): string[] {
  const d = join(CONTENT_DIR, dir);
  if (!existsSync(d)) return [];
  return readdirSync(d)
    .filter((f) => f.endsWith(ext))
    .sort()
    .map((f) => join(d, f));
}

function check<T>(schema: z.ZodType<T>, raw: unknown, file: string, errors: LoadError[]): T | undefined {
  const r = schema.safeParse(raw);
  if (r.success) return r.data;
  for (const issue of r.error.issues) errors.push({ file, message: `${issue.path.join('.') || '(root)'}: ${issue.message}` });
  return undefined;
}

function yamlDir<T>(dir: string, schema: z.ZodType<T>, errors: LoadError[]): T[] {
  const out: T[] = [];
  for (const f of files(dir, '.yaml')) {
    const raw = parseYaml(readFileSync(f, 'utf8'));
    const v = check(schema, raw, f, errors);
    if (v) {
      const fileId = basename(f, '.yaml');
      if ((v as { id?: string }).id !== fileId) errors.push({ file: f, message: `id must match the file name "${fileId}"` });
      out.push(v);
    }
  }
  return out;
}

export function loadContent(): Content {
  const errors: LoadError[] = [];
  const tracksFile = join(CONTENT_DIR, 'tracks.yaml');
  const tracks = existsSync(tracksFile)
    ? ((parseYaml(readFileSync(tracksFile, 'utf8')) as unknown[]) ?? [])
        .map((t) => check(trackSchema, t, tracksFile, errors))
        .filter((t): t is Track => !!t)
    : [];

  const lessons: LessonFile[] = [];
  for (const f of files('lessons', '.md')) {
    const { data, body, bodyLine } = splitFrontmatter(readFileSync(f, 'utf8'));
    const v = check(lessonSchema, data, f, errors);
    if (v) lessons.push({ file: f, id: basename(f, '.md'), data: v, body, bodyLine });
  }

  const bannedFile = join(CONTENT_DIR, 'banned-phrases.txt');
  const banned = existsSync(bannedFile)
    ? readFileSync(bannedFile, 'utf8')
        .split('\n')
        .map((l) => l.trim())
        .filter((l) => l && !l.startsWith('#'))
    : [];

  return {
    tracks,
    topics: yamlDir('topics', topicSchema, errors),
    lessons,
    glossary: yamlDir('glossary', glossarySchema, errors),
    incidents: yamlDir('incidents', incidentSchema, errors),
    cases: yamlDir('cases', caseSchema, errors),
    paths: yamlDir('paths', pathSchema, errors),
    diagrams: yamlDir('diagrams', diagramSchema, errors),
    banned,
    errors,
  };
}
