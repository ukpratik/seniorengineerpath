# SeniorPath

**From mid-level to senior: how real systems are built, broken and fixed, in simple English.**

SeniorPath is a free website for software engineers with about 2–3 years of experience who want to work like senior engineers. Every lesson starts from something that really broke, explains the idea in plain words, shows it in a step-through diagram, and ends with what a senior does at work and with AI tools.

Site: https://seniorpath.dev

- 8 tracks, ~230 topics: foundations, data, distributed systems, building at scale, reliability and security, domain tracks (SaaS, fintech, e-commerce, gaming, low latency), the senior engineer's craft, and AI-native engineering.
- No account. Progress stays in your browser. No cookies.
- Every lesson is checked against its sources before it is published ([how we check content](https://seniorpath.dev/about#checks)).

## Found a mistake?
Use **Report a mistake** at the end of any lesson, or [open an issue](../../issues/new?labels=content-mistake). Please include a source if you can.

## Run it locally
Needs Node 22 (`nvm use`).
```sh
npm ci
npm run dev        # live site with drafts at http://localhost:4321
npm run gate       # check all content without building
npm run build      # content gate + site + search index → dist/
npm run export:app # content for the Android app → android/app/src/main/assets/content/
node scripts/serve.mjs 4322   # serve dist/ with clean URLs
npm test           # unit tests
npx playwright install chromium webkit firefox && npm run test:e2e
```

## How content works
- Lessons: `src/content/lessons/<topic-id>.md` with 8 fixed sections. Topics, glossary, incidents, paths and diagrams are YAML files in `src/content/`.
- The build fails if a published lesson has no sources, no checked claims, broken links, very long sentences or banned idioms.
- See [CONTRIBUTING.md](CONTRIBUTING.md).

## Licence
- Content (lessons, diagrams, glossary, incident cards): [CC BY-NC-SA 4.0](LICENSE-CONTENT.md).
- Code: [MIT](LICENSE).
- Font: Plus Jakarta Sans (SIL OFL 1.1, `public/fonts/OFL.txt`). Icons: Lucide (ISC).
