# Progress file fixtures

The website (`src/scripts/store.ts`) and the Android app (`android/.../data/ProgressIo.kt`) read and write the same
progress file (US-010). These two files pin that contract from both sides:

- `web-export.json` is what the website's "Export progress" writes. `tests/unit/store.test.ts` checks the website
  still writes exactly this, and `ProgressIoTest.kt` checks the app imports it.
- `app-export.json` is what the app's "Export progress" writes. `ProgressIoTest.kt` checks the app still writes
  exactly this, and `tests/unit/store.test.ts` checks the website imports it.

Change a fixture only together with both tests.
