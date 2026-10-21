// Partitioning model (lesson sharding): hash mod N vs a consistent hash ring.
// Pure and deterministic. SPEC: see SPEC.md § hash-ring.

// FNV-1a 32-bit: a simple, well-spread, deterministic hash.
export function hash(s: string): number {
  let h = 0x811c9dc5;
  for (let i = 0; i < s.length; i++) {
    h ^= s.charCodeAt(i);
    h = Math.imul(h, 0x01000193) >>> 0;
  }
  // Final mixing (murmur3 fmix32) so similar strings land far apart on the ring.
  h ^= h >>> 16;
  h = Math.imul(h, 0x85ebca6b) >>> 0;
  h ^= h >>> 13;
  h = Math.imul(h, 0xc2b2ae35) >>> 0;
  h ^= h >>> 16;
  return h >>> 0;
}

export function nodeNames(n: number): string[] {
  return Array.from({ length: n }, (_, i) => `server-${i + 1}`);
}

export function keys(n: number): string[] {
  return Array.from({ length: n }, (_, i) => `user:${i}`);
}

export function modN(ks: string[], nodes: string[]): Map<string, string> {
  return new Map(ks.map((k) => [k, nodes[hash(k) % nodes.length]]));
}

export function ring(ks: string[], nodes: string[], vnodes: number): Map<string, string> {
  const points = nodes
    .flatMap((n) => Array.from({ length: vnodes }, (_, v) => ({ at: hash(`${n}#${v}`), node: n })))
    .sort((a, b) => a.at - b.at);
  const owner = (h: number) => {
    // First point clockwise (binary search), wrapping to the start.
    let lo = 0;
    let hi = points.length;
    while (lo < hi) {
      const mid = (lo + hi) >> 1;
      if (points[mid].at < h) lo = mid + 1;
      else hi = mid;
    }
    return points[lo % points.length].node;
  };
  return new Map(ks.map((k) => [k, owner(hash(k))]));
}

export function movedShare(before: Map<string, string>, after: Map<string, string>): number {
  let moved = 0;
  for (const [k, n] of before) if (after.get(k) !== n) moved++;
  return before.size ? moved / before.size : 0;
}

export function loadShare(assign: Map<string, string>, nodes: string[]): { node: string; share: number }[] {
  const counts = new Map(nodes.map((n) => [n, 0]));
  for (const n of assign.values()) counts.set(n, (counts.get(n) ?? 0) + 1);
  return nodes.map((n) => ({ node: n, share: assign.size ? counts.get(n)! / assign.size : 0 }));
}
