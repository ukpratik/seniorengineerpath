// Readability checks for lesson prose (US-016, NFR-006).
// Works on plain text that has already had code, tables and headings removed.

export const LIMITS = {
  failSentenceWords: 35,
  warnAvgWords: 20,
  warnGrade: 10,
};

// Abbreviations that end in a dot but do not end a sentence.
const ABBREVIATIONS = ['e.g.', 'i.e.', 'etc.', 'vs.', 'cf.', 'approx.', 'Mr.', 'Ms.', 'Dr.', 'No.', 'Fig.', 'ch.', 'p.'];

export function splitSentences(text: string): string[] {
  let t = text.replace(/\s+/g, ' ').trim();
  if (!t) return [];
  // Protect dots that do not end sentences.
  ABBREVIATIONS.forEach((a, i) => {
    t = t.split(a).join(a.replace(/\./g, `\u0000${i}\u0000`));
  });
  t = t.replace(/(\d)\.(\d)/g, '$1\u0001$2'); // 1.5, 99.9
  t = t.replace(/\b([a-z0-9-]+)\.(com|io|dev|org|net|md|ts|js|yaml|json|html)\b/gi, '$1\u0001$2'); // file and domain names
  const parts = t.split(/(?<=[.!?])["”’)]*\s+(?=["“‘(]?[A-Z0-9₹$])/);
  return parts
    .map((s) =>
      s
        .replace(/\u0000(\d+)\u0000/g, '.')
        .replace(/\u0001/g, '.')
        .trim(),
    )
    .filter((s) => /[A-Za-z0-9]/.test(s));
}

export function words(sentence: string): string[] {
  return sentence.split(/\s+/).filter((w) => /[A-Za-z0-9]/.test(w));
}

// Simple English syllable estimate. Good enough for a grade estimate, not for poetry.
export function syllables(word: string): number {
  let w = word.toLowerCase().replace(/[^a-z]/g, '');
  if (!w) return 1; // numbers and symbols count as one
  if (w.length <= 3) return 1;
  w = w.replace(/(?:[^laeiouy]es|ed|[^laeiouy]e)$/, '').replace(/^y/, '');
  const groups = w.match(/[aeiouy]{1,2}/g);
  return Math.max(1, groups ? groups.length : 1);
}

export interface Readability {
  sentences: number;
  words: number;
  avgWords: number;
  grade: number;
  longSentences: string[];
}

// Flesch–Kincaid grade level. Glossary terms are excluded from syllable scoring
// (R-007): technical words like "idempotency" are explained, not avoided.
export function analyze(text: string, glossaryWords: Set<string> = new Set()): Readability {
  const sents = splitSentences(text);
  let wordCount = 0;
  let syl = 0;
  const longSentences: string[] = [];
  for (const s of sents) {
    const ws = words(s);
    wordCount += ws.length;
    if (ws.length > LIMITS.failSentenceWords) longSentences.push(s);
    for (const w of ws) {
      const bare = w.toLowerCase().replace(/[^a-z-]/g, '');
      syl += glossaryWords.has(bare) ? 2 : syllables(w);
    }
  }
  const n = Math.max(1, sents.length);
  const wc = Math.max(1, wordCount);
  const grade = 0.39 * (wc / n) + 11.8 * (syl / wc) - 15.59;
  return {
    sentences: sents.length,
    words: wordCount,
    avgWords: Math.round((wordCount / n) * 10) / 10,
    grade: Math.round(grade * 10) / 10,
    longSentences,
  };
}

export function findBannedPhrases(text: string, banned: string[]): string[] {
  const lower = text.toLowerCase();
  return banned.filter((p) => p && new RegExp(`\\b${p.toLowerCase().replace(/[.*+?^${}()|[\]\\]/g, '\\$&')}\\b`).test(lower));
}
