// Content schemas (LLD §2). Used by Astro collections and by the gate script.
import { z } from 'astro/zod';

const id = z.string().regex(/^[a-z0-9][a-z0-9-]*$/, 'use lowercase letters, numbers and dashes');

// YAML front matter may give a Date or a string; we keep ISO day strings.
export const day = z
  .union([z.string(), z.date()])
  .transform((v) => (v instanceof Date ? v.toISOString().slice(0, 10) : v))
  .pipe(z.string().regex(/^\d{4}-\d{2}-\d{2}$/, 'use YYYY-MM-DD'));

export const level = z.enum(['L1', 'L2', 'L3']);

export const trackSchema = z.object({
  id,
  title: z.string(),
  short: z.string(),
  modules: z.array(z.object({ id, title: z.string() })).min(1),
});

export const topicSchema = z.object({
  id,
  slug: id,
  aliases: z.array(id).default([]),
  track: id,
  module: id,
  title: z.string().min(3),
  oneLiner: z.string().min(10).max(140),
  level,
  prereqs: z.array(id).default([]),
  link: z.object({ title: z.string(), url: z.url() }).optional(),
});

export const sourceSchema = z.object({
  id,
  title: z.string(),
  url: z.url().optional(),
  where: z.string().optional(),
  note: z.string().optional(),
});

export const quizSchema = z.object({
  q: z.string(),
  options: z.array(z.string()).min(2).max(5),
  answer: z.number().int().min(0),
  why: z.string().min(5),
});

export const lessonSchema = z.object({
  topic: id,
  subtitle: z.string(),
  status: z.enum(['draft', 'published']).default('draft'),
  illustrative_story: z.boolean().default(false),
  readingMinutes: z.number().int().positive().optional(),
  sources: z.array(sourceSchema).default([]),
  claims_checked: z.array(z.object({ claim: z.string(), source: id, note: z.string().optional() })).default([]),
  reviewed_by: z.enum(['ai-factcheck', 'human']).optional(),
  reviewed_on: day.optional(),
  major_revised_on: day.optional(),
  review_by: day.optional(),
  quiz: z.array(quizSchema).default([]),
  incidents: z.array(id).default([]),
  cases: z.array(id).default([]),
  related: z.array(id).default([]),
  glossary_optout: z.array(id).default([]),
});

export const glossarySchema = z.object({
  id,
  term: z.string(),
  forms: z.array(z.string()).default([]),
  short: z.string().max(300),
});

export const incidentSchema = z.object({
  id,
  title: z.string(),
  date: day,
  company: z.string(),
  broke: z.string(),
  why: z.string(),
  lesson: z.string(),
  url: z.url(),
  tags: z.array(id).min(1),
  lessons: z.array(id).default([]),
  verified: z.boolean().default(false),
});

export const caseSchema = z.object({
  id,
  company: z.string(),
  title: z.string(),
  lesson: z.string(),
  url: z.url(),
  tags: z.array(id).min(1),
  lessons: z.array(id).default([]),
  verified: z.boolean().default(false),
});

export const pathSchema = z.object({
  id,
  slug: id,
  title: z.string(),
  description: z.string(),
  default: z.boolean().default(false),
  steps: z.array(id).min(1),
});

export const diagramSchema = z.object({
  id,
  title: z.string(),
  viewBox: z.string(),
  svg: z.string(),
  captions: z.array(z.string()).min(2),
});

export type Track = z.infer<typeof trackSchema>;
export type Topic = z.infer<typeof topicSchema>;
export type Lesson = z.infer<typeof lessonSchema>;
export type GlossaryEntry = z.infer<typeof glossarySchema>;
export type Incident = z.infer<typeof incidentSchema>;
export type CaseStudy = z.infer<typeof caseSchema>;
export type Path = z.infer<typeof pathSchema>;
export type DiagramEntry = z.infer<typeof diagramSchema>;
