// One place that turns content collections into what pages show.
import { getCollection } from 'astro:content';
import { readFileSync } from 'node:fs';
import { join } from 'node:path';
import { parse as parseYaml } from 'yaml';
import type { Topic, Track, Path, Incident, GlossaryEntry } from '../content-tools/schemas.ts';

export const showDrafts = import.meta.env.DEV || process.env.SHOW_DRAFTS === '1';

export const LEVEL_LABEL: Record<Topic['level'], string> = { L1: 'L1 Must-know', L2: 'L2 Senior', L3: 'L3 Staff+' };

export interface TopicView extends Topic {
  written: boolean;
  trackTitle: string;
  moduleTitle: string;
  trackIndex: number; // 1–8, for the line colour
}

export interface TrackView extends Track {
  index: number;
  modules: (Track['modules'][number] & { topics: TopicView[] })[];
  total: number;
  writtenCount: number;
}

let cache: Promise<SiteData> | null = null;

export interface SiteData {
  topics: TopicView[];
  byId: Map<string, TopicView>;
  tracks: TrackView[];
  paths: (Path & { topics: TopicView[] })[];
  defaultPath: Path & { topics: TopicView[] };
  incidents: Incident[];
  glossary: GlossaryEntry[];
  writtenCount: number;
}

async function load(): Promise<SiteData> {
  const lessons = await getCollection('lessons', (l) => l.data.status === 'published' || showDrafts);
  const written = new Set(lessons.map((l) => l.id));
  // The file loader does not keep file order; tracks.yaml order is the curriculum order.
  const fileOrder = (parseYaml(readFileSync(join(process.cwd(), 'src/content/tracks.yaml'), 'utf8')) as { id: string }[]).map((t) => t.id);
  const tracksRaw = (await getCollection('tracks')).map((t) => t.data).sort((a, b) => fileOrder.indexOf(a.id) - fileOrder.indexOf(b.id));

  const topics: TopicView[] = (await getCollection('topics')).map((t) => {
    const ti = tracksRaw.findIndex((x) => x.id === t.data.track);
    const tr = tracksRaw[ti];
    return {
      ...t.data,
      written: written.has(t.id),
      trackTitle: tr.title,
      moduleTitle: tr.modules.find((m) => m.id === t.data.module)!.title,
      trackIndex: ti + 1,
    };
  });
  const byId = new Map(topics.map((t) => [t.id, t]));

  const tracks: TrackView[] = tracksRaw.map((t, i) => {
    const modules = t.modules.map((m) => ({
      ...m,
      // Written lessons first, then by level, then title.
      topics: topics
        .filter((x) => x.track === t.id && x.module === m.id)
        .sort((a, b) => Number(b.written) - Number(a.written) || a.level.localeCompare(b.level) || a.title.localeCompare(b.title)),
    }));
    const all = modules.flatMap((m) => m.topics);
    return { ...t, index: i + 1, modules, total: all.length, writtenCount: all.filter((x) => x.written).length };
  });

  const paths = (await getCollection('paths'))
    .map((p) => ({ ...p.data, topics: p.data.steps.map((id) => byId.get(id)!) }))
    .sort((a, b) => Number(b.default) - Number(a.default) || a.title.localeCompare(b.title));

  return {
    topics,
    byId,
    tracks,
    paths,
    defaultPath: paths.find((p) => p.default)!,
    incidents: (await getCollection('incidents', (i) => i.data.verified)).map((i) => i.data).sort((a, b) => b.date.localeCompare(a.date)),
    glossary: (await getCollection('glossary')).map((g) => g.data).sort((a, b) => a.term.localeCompare(b.term)),
    writtenCount: written.size,
  };
}

export function siteData(): Promise<SiteData> {
  return (cache ??= load());
}

export function minutesFor(topics: TopicView[]): number {
  return topics.filter((t) => t.written).length * 12;
}
