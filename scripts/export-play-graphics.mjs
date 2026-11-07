// Play Store graphics for the Android app (A9): the 512×512 icon and the 1024×500 feature graphic, drawn from the
// site's mark and font with resvg. Run: node scripts/export-play-graphics.mjs
import { Resvg } from '@resvg/resvg-js';
import { writeFileSync } from 'node:fs';
import { join } from 'node:path';
import { tmpdir } from 'node:os';
import { execFileSync } from 'node:child_process';

const OUT = join(process.cwd(), 'android/fastlane/metadata/android/en-US/images');
const FONT = join(process.cwd(), 'src/og/PlusJakartaSans-wght.ttf');
const MARK = (s) =>
  `<path d="M${8 * s} ${22 * s} ${14 * s} ${14 * s}l${5 * s} ${4 * s} ${5 * s}-${8 * s}" fill="none" stroke="#fff" stroke-width="${2.6 * s}" stroke-linecap="round" stroke-linejoin="round"/>` +
  `<circle cx="${8 * s}" cy="${22 * s}" r="${2.6 * s}" fill="#fff"/><circle cx="${24 * s}" cy="${10 * s}" r="${2.6 * s}" fill="#fff"/>`;

const png = (svg, width) =>
  new Resvg(svg, { fitTo: { mode: 'width', value: width }, font: { fontFiles: [FONT], loadSystemFonts: false, defaultFontFamily: 'Plus Jakarta Sans' } })
    .render()
    .asPng();

// Icon: full square (Play adds the rounded mask and shadow), the mark centred at the same proportions as the launcher.
const icon = `<svg xmlns="http://www.w3.org/2000/svg" width="512" height="512" viewBox="0 0 512 512">
<rect width="512" height="512" fill="#4F46E5"/><g transform="translate(96 96)">${MARK(10)}</g></svg>`;
writeFileSync(join(OUT, 'icon.png'), png(icon, 512));

// Feature graphic: dark background, the mark, the name and one line. No badges or claims Play may crop.
const feature = `<svg xmlns="http://www.w3.org/2000/svg" width="1024" height="500" viewBox="0 0 1024 500">
<rect width="1024" height="500" fill="#0B1020"/>
<rect x="88" y="150" width="136" height="136" rx="38" fill="#4F46E5"/><g transform="translate(88 150)">${MARK(4.25)}</g>
<text x="270" y="232" font-size="76" font-weight="800" fill="#F1F5F9">SeniorPath</text>
<text x="272" y="292" font-size="34" font-weight="500" fill="#A5B4FC">From mid-level to senior engineer</text>
<text x="272" y="340" font-size="26" font-weight="500" fill="#94A3B8">System design · real outages · AI at work · offline</text>
</svg>`;
writeFileSync(join(OUT, 'featureGraphic.png'), png(feature, 1024));
// Play wants no alpha channel in the feature graphic; flatten both through JPEG (macOS sips, lossless enough at 100).
if (process.platform === 'darwin') {
  for (const f of ['icon', 'featureGraphic']) {
    const png = join(OUT, `${f}.png`);
    const jpg = join(tmpdir(), `${f}.jpg`);
    execFileSync('sips', ['-s', 'format', 'jpeg', '-s', 'formatOptions', '100', png, '--out', jpg], { stdio: 'ignore' });
    execFileSync('sips', ['-s', 'format', 'png', jpg, '--out', png], { stdio: 'ignore' });
  }
}
console.log(`Play graphics → ${OUT}`);
