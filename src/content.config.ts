import { defineCollection } from 'astro:content';
import { glob, file } from 'astro/loaders';
import {
  trackSchema,
  topicSchema,
  lessonSchema,
  glossarySchema,
  incidentSchema,
  caseSchema,
  pathSchema,
} from './content-tools/schemas.ts';

// The file name is the permanent id (never the slug, which can change: US-017).
const byFileName = ({ entry }: { entry: string }) => entry.replace(/\.(ya?ml|md)$/, '');
const yaml = (dir: string) => glob({ pattern: '*.yaml', base: `./src/content/${dir}`, generateId: byFileName });

export const collections = {
  tracks: defineCollection({ loader: file('./src/content/tracks.yaml'), schema: trackSchema }),
  topics: defineCollection({ loader: yaml('topics'), schema: topicSchema }),
  lessons: defineCollection({ loader: glob({ pattern: '*.md', base: './src/content/lessons', generateId: byFileName }), schema: lessonSchema }),
  glossary: defineCollection({ loader: yaml('glossary'), schema: glossarySchema }),
  incidents: defineCollection({ loader: yaml('incidents'), schema: incidentSchema }),
  cases: defineCollection({ loader: yaml('cases'), schema: caseSchema }),
  paths: defineCollection({ loader: yaml('paths'), schema: pathSchema }),
};
