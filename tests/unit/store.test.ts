import { describe, it, expect } from 'vitest';
import { createStore, KEY, nthBucket, type Storage } from '../../src/scripts/store.ts';

function memory(initial: Record<string, string> = {}): Storage & { data: Record<string, string> } {
  const data = { ...initial };
  return { data, getItem: (k) => data[k] ?? null, setItem: (k, v) => void (data[k] = v) };
}

describe('store', () => {
  it('marks done and reports the first completion only once', () => {
    const s = createStore(memory());
    expect(s.markDone('a')).toEqual({ firstEver: true, nth: 1 });
    s.undoDone('a');
    expect(s.isDone('a')).toBe(false);
    expect(s.markDone('a')).toEqual({ firstEver: false, nth: 1 });
    expect(s.markDone('b')).toEqual({ firstEver: true, nth: 2 });
  });

  it('works without storage and never throws', () => {
    const s = createStore(null);
    expect(s.available()).toBe(false);
    expect(s.load()).toBeNull();
    expect(s.markDone('a')).toBeNull();
    expect(s.isDone('a')).toBe(false);
    expect(s.importJson('{}')).toEqual({ ok: false, reason: 'invalid' });
  });

  it('survives a storage backend that throws on write', () => {
    const s = createStore({ getItem: () => null, setItem: () => { throw new Error('quota'); } });
    expect(s.markDone('a')).toBeNull();
  });

  it('recovers from corrupt stored data', () => {
    const s = createStore(memory({ [KEY]: '{not json' }));
    expect(s.load()).toEqual({ v: 1, done: {}, ever: [] });
  });

  it('round-trips export and import, keeping the earlier done date', () => {
    const a = createStore(memory());
    a.markDone('x', new Date('2026-10-01T00:00:00Z'));
    a.setLastOpened('x', new Date('2026-10-02T00:00:00Z'));
    const file = a.exportJson();

    const b = createStore(memory());
    b.markDone('x', new Date('2026-10-05T00:00:00Z'));
    b.markDone('y');
    expect(b.importJson(file)).toEqual({ ok: true, added: 0 });
    expect(b.load()!.done.x).toBe('2026-10-01T00:00:00.000Z');
    expect(b.load()!.done.y).toBeDefined();
  });

  it('lets the newer last-opened lesson win on import', () => {
    const a = createStore(memory());
    a.setLastOpened('old', new Date('2026-10-01T00:00:00Z'));
    const b = createStore(memory());
    b.setLastOpened('new', new Date('2026-10-09T00:00:00Z'));
    b.importJson(a.exportJson());
    expect(b.load()!.lastOpened).toBe('new');
    a.importJson(b.exportJson());
    expect(a.load()!.lastOpened).toBe('new');
  });

  it('rejects bad import files without changing progress', () => {
    const s = createStore(memory());
    s.markDone('keep');
    expect(s.importJson('nope')).toEqual({ ok: false, reason: 'invalid' });
    expect(s.importJson(JSON.stringify({ schema_version: 2, done: {} }))).toEqual({ ok: false, reason: 'newer' });
    expect(s.importJson(JSON.stringify({ schema_version: 1, done: [] }))).toEqual({ ok: false, reason: 'invalid' });
    expect(s.importJson('x'.repeat(100_001))).toEqual({ ok: false, reason: 'too_big' });
    expect(Object.keys(s.load()!.done)).toEqual(['keep']);
  });

  it('ignores bad ids and dates inside an import file', () => {
    const s = createStore(memory());
    const r = s.importJson(JSON.stringify({ schema_version: 1, done: { good: '2026-10-01T00:00:00Z', 'Bad Id': '2026-10-01', worse: 'yesterday' } }));
    expect(r).toEqual({ ok: true, added: 1 });
  });

  it('buckets completion counts', () => {
    expect([1, 2, 5, 6, 40].map(nthBucket)).toEqual(['1', '2-5', '2-5', '6+', '6+']);
  });
});
