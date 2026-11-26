# SeniorPath

**From mid-level to senior: how real systems are built, broken and fixed, in simple English.**

SeniorPath is a free website for software engineers with about 2–3 years of experience who want to work like senior engineers. Every lesson starts from something that really broke, explains the idea in plain words, shows it in a step-through diagram, and ends with what a senior does at work and with AI tools.

**Website:** https://ukpratik.github.io/seniorengineerpath/

**Android app** (offline, no account, no tracking):

<a href="https://play.google.com/store/apps/details?id=io.github.ukpratik.seniorpath.play&utm_source=github&utm_medium=readme&utm_campaign=seniorengineerpath"><img alt="Get it on Google Play" src="https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png" height="80"></a>
<a href="https://f-droid.org/packages/io.github.ukpratik.seniorpath/"><img alt="Get it on F-Droid" src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png" height="80"></a>

- 8 tracks, ~230 topics: foundations, data, distributed systems, building at scale, reliability and security, domain tracks (SaaS, fintech, e-commerce, gaming, low latency), the senior engineer's craft, and AI-native engineering.
- No account. Progress stays in your browser. No cookies.
- Every lesson is checked against its sources before it is published ([how we check content](https://ukpratik.github.io/seniorengineerpath/about#checks)).

## Found a mistake?
Use **Report a mistake** at the end of any lesson, or [open an issue](../../issues/new?labels=content-mistake). Please include a source if you can.

## Run it locally
Needs Node 22 (`nvm use`).
```sh
npm ci
npm run dev        # live site with drafts at http://localhost:4321
npm run gate       # check all content without building
npm run build      # content gate + site + search index → dist/
npm run export:app # content for the Android app → android/app/src/main/assets/content/ (committed: rerun and commit after any lesson change; CI checks)
node scripts/serve.mjs 4322   # serve dist/ with clean URLs
npm test           # unit tests
npx playwright install chromium webkit firefox && npm run test:e2e
```

## How content works
- Lessons: `src/content/lessons/<topic-id>.md` with 8 fixed sections. Topics, glossary, incidents, paths and diagrams are YAML files in `src/content/`.
- The build fails if a published lesson has no sources, no checked claims, broken links, very long sentences or banned idioms.
- See [CONTRIBUTING.md](CONTRIBUTING.md).

## Hosting
The site is static and deployed to GitHub Pages by `.github/workflows/pages.yml` on every push to `main`. Its address is one value, `SITE.url` in `src/config.ts`: the Astro `site` and `base`, canonical URLs, sitemap, RSS, robots.txt and structured data all come from it, so moving to a custom domain is a one-line change (plus a CNAME). Links in pages and lessons are written root-relative (`/learn/x`); the build adds the base path.

More apps from UK Group: https://ukpratik.github.io/awesomeaiapplications/

## Licence
- Content (lessons, diagrams, glossary, incident cards): [CC BY-NC-SA 4.0](LICENSE-CONTENT.md).
- Code: [MIT](LICENSE).
- Font: Plus Jakarta Sans (SIL OFL 1.1, `public/fonts/OFL.txt`). Icons: Lucide (ISC).
