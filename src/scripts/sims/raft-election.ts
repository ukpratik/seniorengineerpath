// Raft leader election model (lesson raft). Pure and deterministic for a given seed.
// It models elections only (no log replication): all logs are assumed equally up to date.
// SPEC: see SPEC.md § raft-election.
export interface RaftParams {
  nodes: number; // cluster size, e.g. 3 or 5
  stopped: number[]; // node indexes that are stopped at `stopAt` (node 0 starts as leader)
  stopAt: number; // ms
  untilMs: number; // how long to simulate
  seed: number; // changes the random election timeouts
  minTimeout?: number; // ms, default 150 (Raft paper suggests 150–300 ms)
  maxTimeout?: number; // ms, default 300
  heartbeatMs?: number; // ms, default 50
  latencyMs?: number; // one-way message delay, default 10
}

export type RaftEventKind = 'stopped' | 'timeout' | 'vote' | 'leader';
export interface RaftEvent {
  t: number;
  node: number;
  kind: RaftEventKind;
  term: number;
  votes?: number; // for 'leader'
  for?: number; // for 'vote': who the vote went to
}

export interface RaftResult {
  events: RaftEvent[];
  leader: number | null; // leader at the end, if any
  term: number; // highest term reached
  majority: number;
}

// Small seeded PRNG (mulberry32) so a run can be replayed.
function rng(seed: number): () => number {
  let a = seed >>> 0;
  return () => {
    a = (a + 0x6d2b79f5) >>> 0;
    let t = a;
    t = Math.imul(t ^ (t >>> 15), t | 1);
    t ^= t + Math.imul(t ^ (t >>> 7), t | 61);
    return ((t ^ (t >>> 14)) >>> 0) / 4294967296;
  };
}

interface Msg {
  at: number;
  to: number;
  from: number;
  type: 'heartbeat' | 'requestVote' | 'grant';
  term: number;
}

export function simulateRaft(p: RaftParams): RaftResult {
  const min = p.minTimeout ?? 150;
  const max = p.maxTimeout ?? 300;
  const hb = p.heartbeatMs ?? 50;
  const lat = p.latencyMs ?? 10;
  const rand = rng(p.seed);
  const majority = Math.floor(p.nodes / 2) + 1;
  const timeout = () => min + Math.floor(rand() * (max - min + 1));

  const term = Array(p.nodes).fill(1);
  const role: ('follower' | 'candidate' | 'leader')[] = Array(p.nodes).fill('follower');
  role[0] = 'leader';
  const votedFor: (number | null)[] = Array(p.nodes).fill(null);
  votedFor[0] = 0;
  const votes = Array(p.nodes).fill(0);
  const alive = Array(p.nodes).fill(true);
  const deadline = Array.from({ length: p.nodes }, () => timeout());
  let nextHeartbeat = 0;
  const queue: Msg[] = [];
  const events: RaftEvent[] = [];
  const send = (from: number, to: number, type: Msg['type'], t: number, tm: number) => {
    if (to !== from) queue.push({ at: t + lat, to, from, type, term: tm });
  };

  for (let t = 0; t <= p.untilMs; t++) {
    if (t === p.stopAt) {
      for (const n of p.stopped) {
        alive[n] = false;
        events.push({ t, node: n, kind: 'stopped', term: term[n] });
      }
    }
    // Deliver messages due now (messages to stopped nodes are lost).
    for (let i = queue.length - 1; i >= 0; i--) {
      const m = queue[i];
      if (m.at !== t) continue;
      queue.splice(i, 1);
      const n = m.to;
      if (!alive[n]) continue;
      if (m.term > term[n]) {
        // Rule for all servers: a higher term makes you a follower in that term.
        term[n] = m.term;
        role[n] = 'follower';
        votedFor[n] = null;
      }
      if (m.type === 'heartbeat' && m.term >= term[n]) {
        role[n] = 'follower';
        deadline[n] = t + timeout();
      } else if (m.type === 'requestVote' && m.term === term[n] && (votedFor[n] === null || votedFor[n] === m.from)) {
        votedFor[n] = m.from;
        deadline[n] = t + timeout();
        events.push({ t, node: n, kind: 'vote', term: term[n], for: m.from });
        send(n, m.from, 'grant', t, term[n]);
      } else if (m.type === 'grant' && role[n] === 'candidate' && m.term === term[n]) {
        votes[n]++;
        if (votes[n] >= majority) {
          role[n] = 'leader';
          events.push({ t, node: n, kind: 'leader', term: term[n], votes: votes[n] });
          nextHeartbeat = t; // assert leadership at once
        }
      }
    }
    // Leaders send heartbeats; followers and candidates time out.
    for (let n = 0; n < p.nodes; n++) {
      if (!alive[n]) continue;
      if (role[n] === 'leader') {
        if (t >= nextHeartbeat) for (let o = 0; o < p.nodes; o++) send(n, o, 'heartbeat', t, term[n]);
        continue;
      }
      if (t >= deadline[n]) {
        term[n]++;
        role[n] = 'candidate';
        votedFor[n] = n;
        votes[n] = 1;
        deadline[n] = t + timeout();
        events.push({ t, node: n, kind: 'timeout', term: term[n] });
        for (let o = 0; o < p.nodes; o++) send(n, o, 'requestVote', t, term[n]);
      }
    }
    if (role.some((r, n) => r === 'leader' && alive[n]) && t >= nextHeartbeat) nextHeartbeat = t + hb;
  }
  const leaders = role.map((r, n) => (r === 'leader' && alive[n] ? n : -1)).filter((n) => n >= 0);
  return { events, leader: leaders.length ? leaders[leaders.length - 1] : null, term: Math.max(...term), majority };
}
