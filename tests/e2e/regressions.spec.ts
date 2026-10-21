import { test, expect } from '@playwright/test';
import { readFileSync, readdirSync } from 'node:fs';

// Regression tests for QA run 1 (product ISSUES BUG-xxx / QA1-xx).

test('QA1-02: search shows the exact glossary term first', async ({ page }) => {
  await page.goto('/search');
  for (const [q, anchor] of [['rate limit', 'rate-limit'], ['SLO', 'slo']] as const) {
    await page.getByRole('searchbox').fill(q);
    const first = page.locator('.rgroup', { hasText: 'Glossary terms' }).locator('.rlist a').first();
    await expect(first).toHaveAttribute('href', new RegExp(`/glossary#${anchor}$`));
  }
});

test('QA1-03: AI-track lessons say they are reviewed every quarter', async ({ page }) => {
  await page.goto('/learn/working-with-coding-agents');
  await expect(page.locator('.review')).toContainText('review this lesson every quarter');
});

test('QA1-04: without JavaScript the quiz answer can be shown and dead buttons are hidden', async ({ browser }) => {
  const ctx = await browser.newContext({ javaScriptEnabled: false });
  const page = await ctx.newPage();
  await page.goto('/learn/idempotency-retries-timeouts');
  const ans = page.locator('details.ans').first();
  await ans.locator('summary').click();
  await expect(ans).toContainText('Nothing. The server may or may not have charged the card.');
  await expect(page.locator('[data-done]')).toBeHidden();
  await expect(page.locator('[data-clarity]')).toBeHidden();
  await expect(page.locator('[data-toc-open]')).toBeHidden();
  await ctx.close();
});

test('QA1-05: no inline event handlers anywhere (CSP)', () => {
  const walk = (d: string): string[] =>
    readdirSync(d, { withFileTypes: true }).flatMap((e) => (e.isDirectory() ? walk(`${d}/${e.name}`) : e.name.endsWith('.html') ? [`${d}/${e.name}`] : []));
  const offenders = walk('dist').filter((f) => /\son[a-z]+="/.test(readFileSync(f, 'utf8')));
  expect(offenders).toEqual([]);
});

test('QA1-06: glossary terms that start with a digit have an entry', async ({ page }) => {
  await page.goto('/glossary');
  await expect(page.locator('#http-429')).toHaveCount(1);
});

test('QA1-07: incident cards link back to the lessons that cite them', async ({ page }) => {
  await page.goto('/incidents');
  const card = page.locator('li.inc', { has: page.locator('#aws-2021-12-us-east-1') });
  await expect(card.locator('a[href="/learn/idempotency-retries-timeouts"]')).toHaveCount(1);
});

test('QA1-08: an unknown last-opened lesson falls back to the normal start', async ({ page }) => {
  await page.addInitScript(() => localStorage.setItem('seniorpath:v1', JSON.stringify({ v: 1, done: {}, ever: [], lastOpened: 'removed-lesson' })));
  await page.goto('/');
  await expect(page.locator('.first-only a.start')).toBeVisible();
});

test('QA1-15: Next never points to an unwritten lesson', async ({ page }) => {
  await page.goto('/paths/into-domains');
  await page.locator('[data-active]').click();
  await page.goto('/learn/money-and-double-entry-ledgers');
  const next = page.locator('nav.pn a', { hasText: 'Next' });
  if (await next.count()) {
    const href = (await next.getAttribute('href'))!;
    await page.goto(href);
    await expect(page.locator('[data-lesson]')).toHaveCount(1); // a written lesson, not a coming-soon page
  }
});

test('QA1-16: RSS links match the canonical URLs (no trailing slash)', () => {
  const rss = readFileSync('dist/rss.xml', 'utf8');
  const links = [...rss.matchAll(/<link>([^<]+)<\/link>/g)].map((m) => m[1]).filter((l) => l.includes('/learn/'));
  expect(links.length).toBeGreaterThan(0);
  expect(links.filter((l) => l.endsWith('/'))).toEqual([]);
});
