// Loads /data/topics.json once, only on pages that need titles for stored ids.
export interface Manifest {
  topics: Record<string, { t: string; s: string; tr: string; ti: number; l: 'L1' | 'L2' | 'L3'; w: 0 | 1 }>;
  paths: Record<string, { t: string; s: string; steps: string[] }>;
  defaultPath: string;
  tracks: [string, string, number][];
}
let p: Promise<Manifest | null> | null = null;
export function manifest(): Promise<Manifest | null> {
  return (p ??= fetch('/data/topics.json')
    .then((r) => (r.ok ? (r.json() as Promise<Manifest>) : null))
    .catch(() => null));
}
export const LEVEL: Record<string, string> = { L1: 'L1 Must-know', L2: 'L2 Senior', L3: 'L3 Staff+' };
