// Progress that lives only in this browser (US-009, US-010, LLD §3).
// Every storage access is wrapped: private mode or blocked storage must never crash a page.

export const KEY = 'seniorpath:v1';
export const SCHEMA_VERSION = 1;
export const MAX_IMPORT_BYTES = 100_000;

export interface State {
  v: 1;
  done: Record<string, string>; // lesson id → ISO time it was marked done
  ever: string[]; // lessons ever completed in this browser (analytics: first completion only)
  lastOpened?: string;
  lastOpenedAt?: string;
  activePath?: string;
  theme?: 'light' | 'dark';
  exportPrompted?: boolean;
  mapAll?: boolean; // map shows coming-soon topics too
}

export interface Storage {
  getItem(k: string): string | null;
  setItem(k: string, v: string): void;
}

const empty = (): State => ({ v: 1, done: {}, ever: [] });

function storage(): Storage | null {
  try {
    const s = globalThis.localStorage;
    const probe = '__sp_probe__';
    s.setItem(probe, '1');
    s.removeItem(probe);
    return s;
  } catch {
    return null;
  }
}

export function createStore(backend: Storage | null = storage()) {
  const read = (): State | null => {
    if (!backend) return null;
    try {
      const raw = backend.getItem(KEY);
      if (!raw) return empty();
      const s = JSON.parse(raw) as Partial<State>;
      if (s.v !== 1 || typeof s.done !== 'object' || !s.done) return empty();
      return { ...empty(), ...s, ever: Array.isArray(s.ever) ? s.ever : Object.keys(s.done) } as State;
    } catch {
      return empty();
    }
  };

  const write = (s: State): boolean => {
    if (!backend) return false;
    try {
      backend.setItem(KEY, JSON.stringify(s));
      return true;
    } catch {
      return false;
    }
  };

  const update = (fn: (s: State) => void): State | null => {
    const s = read();
    if (!s) return null;
    fn(s);
    return write(s) ? s : null;
  };

  return {
    available: () => backend !== null,
    load: read,

    /** Returns whether this is the first completion ever in this browser, and how many there are now. */
    markDone(id: string, now = new Date()): { firstEver: boolean; nth: number } | null {
      let firstEver = false;
      const s = update((st) => {
        st.done[id] = now.toISOString();
        if (!st.ever.includes(id)) {
          st.ever.push(id);
          firstEver = true;
        }
      });
      return s ? { firstEver, nth: s.ever.length } : null;
    },

    undoDone(id: string): void {
      update((st) => {
        delete st.done[id];
      });
    },

    isDone(id: string): boolean {
      return !!read()?.done[id];
    },

    setLastOpened: (id: string, now = new Date()) =>
      update((s) => {
        s.lastOpened = id;
        s.lastOpenedAt = now.toISOString();
      }),
    setActivePath: (id: string) => update((s) => void (s.activePath = id)),
    setTheme: (t: 'light' | 'dark') => update((s) => void (s.theme = t)),
    setExportPrompted: () => update((s) => void (s.exportPrompted = true)),
    setMapAll: (all: boolean) => update((s) => void (s.mapAll = all)),

    exportJson(now = new Date()): string {
      const s = read() ?? empty();
      return JSON.stringify(
        {
          schema_version: SCHEMA_VERSION,
          exported_at: now.toISOString(),
          done: s.done,
          last_opened: s.lastOpened ?? null,
          last_opened_at: s.lastOpenedAt ?? null,
        },
        null,
        2,
      );
    },

    importJson(text: string): { ok: true; added: number } | { ok: false; reason: 'invalid' | 'newer' | 'too_big' | 'unavailable' } {
      if (text.length > MAX_IMPORT_BYTES) return { ok: false, reason: 'too_big' };
      let data: { schema_version?: unknown; done?: unknown; last_opened?: unknown; last_opened_at?: unknown };
      try {
        data = JSON.parse(text);
      } catch {
        return { ok: false, reason: 'invalid' };
      }
      if (typeof data !== 'object' || data === null || typeof data.schema_version !== 'number') return { ok: false, reason: 'invalid' };
      if (data.schema_version > SCHEMA_VERSION) return { ok: false, reason: 'newer' };
      if (typeof data.done !== 'object' || data.done === null || Array.isArray(data.done)) return { ok: false, reason: 'invalid' };
      const incoming = Object.entries(data.done as Record<string, unknown>).filter(
        ([k, v]) => /^[a-z0-9-]+$/.test(k) && typeof v === 'string' && !Number.isNaN(Date.parse(v)),
      ) as [string, string][];
      if (!read()) return { ok: false, reason: 'unavailable' };
      let added = 0;
      update((s) => {
        for (const [id, at] of incoming) {
          if (!s.done[id]) added++;
          // Keep the earlier finish date.
          if (!s.done[id] || at < s.done[id]) s.done[id] = at;
          if (!s.ever.includes(id)) s.ever.push(id);
        }
        // The newer "last opened" wins.
        const at = typeof data.last_opened_at === 'string' ? data.last_opened_at : '';
        if (typeof data.last_opened === 'string' && /^[a-z0-9-]+$/.test(data.last_opened) && (!s.lastOpenedAt || at > s.lastOpenedAt)) {
          s.lastOpened = data.last_opened;
          s.lastOpenedAt = at || s.lastOpenedAt;
        }
      });
      return { ok: true, added };
    },
  };
}

export type Store = ReturnType<typeof createStore>;

export function nthBucket(n: number): '1' | '2-5' | '6+' {
  return n <= 1 ? '1' : n <= 5 ? '2-5' : '6+';
}
