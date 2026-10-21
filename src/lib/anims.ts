// Custom interactive animations (US-007). Each id has a pure model in
// src/scripts/sims/ and a view in src/scripts/anims.ts.
export const ANIMS = ['cache-stampede', 'replication-lag', 'rollout', 'token-bucket', 'hash-ring', 'raft-election'];

export const ANIM_TITLES: Record<string, string> = {
  'cache-stampede': 'Try it: what happens when a hot key expires',
  'replication-lag': 'Try it: read from a follower that is behind',
  rollout: 'Try it: push a bad config everywhere, or in stages',
  'token-bucket': 'Try it: send a burst into a token bucket',
  'hash-ring': 'Try it: add a server and see how many keys move',
  'raft-election': 'Try it: stop the leader and watch the cluster elect a new one',
};

export function renderAnimHost(id: string): string {
  return `<div class="anim no-js-hide" data-anim="${id}" data-pagefind-ignore role="group" aria-label="${ANIM_TITLES[id]}"><p class="anim-title">${ANIM_TITLES[id]}</p><div class="anim-body"></div></div>`;
}
