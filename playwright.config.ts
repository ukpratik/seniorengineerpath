import { defineConfig, devices } from '@playwright/test';

// Runs against the built site (npm run build first). LLD §8.
export default defineConfig({
  testDir: 'tests/e2e',
  fullyParallel: true,
  retries: process.env.CI ? 1 : 0,
  reporter: process.env.CI ? 'github' : 'list',
  use: { baseURL: 'http://127.0.0.1:4322' },
  webServer: {
    command: 'node scripts/serve.mjs 4322',
    url: 'http://127.0.0.1:4322',
    reuseExistingServer: !process.env.CI,
  },
  projects: [
    { name: 'chromium', use: { ...devices['Pixel 7'] } },
    { name: 'webkit', use: { ...devices['iPhone 14'] }, testMatch: /smoke/ },
    { name: 'firefox', use: { ...devices['Desktop Firefox'] }, testMatch: /smoke/ },
  ],
});
