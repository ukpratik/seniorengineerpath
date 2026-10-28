// `npm run export:app`: write the Android app's content (ADR-007) after the content gate passes.
import { mkdirSync, rmSync, writeFileSync } from 'node:fs';
import { dirname, join } from 'node:path';
import { gateNow } from '../src/content-tools/integration.ts';
import { loadContent } from '../src/content-tools/load.ts';
import { buildAppExport } from '../src/content-tools/app-export.ts';

const OUT = join(process.cwd(), 'android', 'app', 'src', 'main', 'assets', 'content');

const { ok, report } = gateNow();
if (!ok) {
  console.log(report);
  console.error('Content gate failed; nothing exported.');
  process.exit(1);
}

const { files } = buildAppExport(loadContent());
rmSync(OUT, { recursive: true, force: true });
let bytes = 0;
for (const [path, value] of files) {
  const file = join(OUT, path);
  mkdirSync(dirname(file), { recursive: true });
  const text = JSON.stringify(value);
  writeFileSync(file, text);
  bytes += text.length;
}
console.log(`App content: ${files.size} files, ${(bytes / 1024).toFixed(0)} KB → ${OUT}`);
