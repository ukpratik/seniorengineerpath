import { defineConfig } from 'astro/config';
import { unified } from '@astrojs/markdown-remark';
import { loadContent } from './src/content-tools/load.ts';
import { remarkGlossary } from './src/content-tools/remark-glossary.ts';
import { remarkLesson, rehypeLesson } from './src/content-tools/lesson-plugins.ts';
import { contentGate } from './src/content-tools/integration.ts';

// Content is read from disk by the Markdown plugins. A short cache keeps dev edits fresh.
let cache: { at: number; c: ReturnType<typeof loadContent> } | null = null;
const content = () => {
  if (!cache || Date.now() - cache.at > 2000) cache = { at: Date.now(), c: loadContent() };
  return cache.c;
};

export default defineConfig({
  site: 'https://seniorpath.dev',
  output: 'static',
  trailingSlash: 'never',
  build: { format: 'file' },
  integrations: [contentGate()],
  markdown: {
    processor: unified({
      smartypants: false,
      remarkPlugins: [
        [remarkLesson, { diagram: (id: string) => content().diagrams.find((d) => d.id === id) }],
        [remarkGlossary, { terms: () => content().glossary }],
      ],
      rehypePlugins: [rehypeLesson],
    }),
  },
});
