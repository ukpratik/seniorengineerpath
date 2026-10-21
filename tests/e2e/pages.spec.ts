import { test, expect } from '@playwright/test';
import AxeBuilder from '@axe-core/playwright';

test('search finds a lesson and a glossary term, and handles no results', async ({ page }) => {
  await page.goto('/search');
  const q = page.getByRole('searchbox');
  await q.fill('idempotency');
  await expect(page.locator('.rgroup h2', { hasText: 'Lessons' })).toBeVisible();
  await expect(page.locator('.rlist a[href*="/learn/idempotency-retries-timeouts"]').first()).toBeVisible();
  await q.fill('jitter');
  await expect(page.locator('.rlist a[href*="/glossary#jitter"]').first()).toBeVisible();
  await q.fill('zzqqxx');
  await expect(page.locator('.none-box')).toContainText('No results');
});

test('map: written-only filter toggles and is remembered', async ({ page }) => {
  await page.goto('/map');
  const sw = page.getByRole('switch', { name: /Written only/ });
  await expect(sw).toHaveAttribute('aria-checked', 'true');
  const soon = page.locator('details[open] .stn.is-soon').first();
  if ((await page.locator('details[open] .stn.is-soon').count()) > 0) await expect(soon).toBeHidden();
  await sw.click();
  await expect(sw).toHaveAttribute('aria-checked', 'false');
  if ((await page.locator('details[open] .stn.is-soon').count()) > 0) await expect(soon).toBeVisible();
  await page.reload();
  await expect(page.getByRole('switch', { name: /Written only/ })).toHaveAttribute('aria-checked', 'false');
});

test('progress: done lessons show up, export and import round-trip', async ({ page }) => {
  await page.goto('/learn/what-senior-really-means');
  await page.locator('[data-done]').click();
  await page.goto('/progress');
  await expect(page.locator('[data-done-list] li')).toHaveCount(1);
  const [download] = await Promise.all([page.waitForEvent('download'), page.locator('[data-export]').click()]);
  const file = await download.path();

  await page.evaluate(() => localStorage.clear());
  await page.reload();
  await expect(page.locator('.e-only')).toBeVisible();
  await page.locator('[data-import]').setInputFiles(file!);
  await expect(page.locator('[data-banner]')).toContainText('Imported');
  await expect(page.locator('[data-done-list] li')).toHaveCount(1);

  await page.locator('[data-import]').setInputFiles({ name: 'bad.json', mimeType: 'application/json', buffer: Buffer.from('{oops') });
  await expect(page.locator('[data-banner]')).toContainText('Could not import');
  await expect(page.locator('[data-done-list] li')).toHaveCount(1);
});

test('home shows Continue for a returning reader', async ({ page }) => {
  await page.goto('/learn/idempotency-retries-timeouts');
  await page.goto('/');
  await expect(page.locator('[data-continue]')).toBeVisible();
  await expect(page.locator('[data-c-title]')).toHaveText('Idempotency, retries and timeouts');
});

test('incidents: tag filter and deep link', async ({ page }) => {
  await page.goto('/incidents');
  const total = await page.locator('li.inc').count();
  expect(total).toBeGreaterThan(0);
  const chip = page.locator('[data-tag]:not([data-tag=""])').first();
  await chip.click();
  await expect(chip).toHaveAttribute('aria-pressed', 'true');
  expect(await page.locator('li.inc:visible').count()).toBeLessThanOrEqual(total);
  await page.goto('/incidents#aws-2021-12-us-east-1');
  await expect(page.locator('li.inc.hl')).toHaveCount(1);
});

test('glossary filter narrows the list', async ({ page }) => {
  await page.goto('/glossary');
  await page.getByPlaceholder('Filter terms').fill('jitter');
  await expect(page.locator('.gentry:visible')).toHaveCount(1);
});

for (const path of ['/map', '/glossary', '/incidents', '/cases', '/progress', '/search', '/about', '/paths/senior-at-work', '/learn/backoff-jitter-retry-budgets']) {
  test(`axe: ${path}`, async ({ page }) => {
    await page.goto(path);
    const r = await new AxeBuilder({ page }).withTags(['wcag2a', 'wcag2aa', 'wcag21aa', 'wcag22aa']).analyze();
    const serious = r.violations.filter((v) => v.impact === 'serious' || v.impact === 'critical');
    expect(serious.map((v) => `${v.id}: ${v.nodes.length} × ${v.nodes[0]?.target}`)).toEqual([]);
  });
}
