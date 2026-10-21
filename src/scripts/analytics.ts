// The only way pages send analytics (PRD §8, NFR-010). Unknown events or
// properties are dropped, so free text or ids can never leak by accident.
type Props = Record<string, string | number | boolean>;

const ALLOWED: Record<string, string[]> = {
  lesson_view: ['lesson_id'],
  reached_check_yourself: ['lesson_id'],
  lesson_complete: ['lesson_id', 'nth_completed_bucket'],
  quiz_answer: ['lesson_id', 'q', 'correct'],
  clarity_vote: ['lesson_id', 'value'],
  stepthrough_used: ['lesson_id', 'diagram_id'],
  search: ['result_count_bucket'],
  progress_export: ['result'],
  progress_import: ['result'],
  map_filter_toggle: ['value'],
};

declare global {
  interface Window {
    umami?: { track: (name: string, data?: Props) => void };
  }
}

export function clean(name: string, props: Props = {}): Props | null {
  const keys = ALLOWED[name];
  if (!keys) return null;
  const out: Props = {};
  for (const k of keys) if (k in props) out[k] = props[k];
  return out;
}

export function track(name: string, props: Props = {}): void {
  const data = clean(name, props);
  if (!data) return;
  try {
    window.umami?.track(name, data);
  } catch {
    /* analytics must never break a page */
  }
}
