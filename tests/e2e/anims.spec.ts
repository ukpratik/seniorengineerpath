import { test, expect } from '@playwright/test';

// Custom animations (US-007): the widget responds to controls and states the result in text.
test('replication lag: reading after the lag is fresh, before it is stale', async ({ page }) => {
  await page.goto('/learn/database-replication');
  const w = page.locator('[data-anim="replication-lag"]');
  await expect(w.locator('.anim-result')).toContainText('Stale read');
  await w.getByLabel(/You read again after/).fill('1500');
  await expect(w.locator('.anim-result')).toContainText('Fresh read from the follower');
  await w.getByLabel(/You read again after/).fill('100');
  await w.getByRole('switch', { name: /Read-your-writes/ }).click();
  await expect(w.locator('.anim-result')).toContainText('Fresh read from the leader');
});

test('rollout: a staged push limits the damage', async ({ page }) => {
  await page.goto('/learn/safe-deploys-config-changes');
  const w = page.locator('[data-anim="rollout"]');
  await expect(w.locator('.anim-result')).toContainText('Peak: 100%');
  await w.getByRole('button', { name: /In stages/ }).click();
  await expect(w.locator('.anim-result')).toContainText('Peak: 1%');
});

test('animations are hidden without JavaScript (the step-through diagram explains instead)', async ({ browser }) => {
  const ctx = await browser.newContext({ javaScriptEnabled: false });
  const page = await ctx.newPage();
  await page.goto('/learn/database-replication');
  await expect(page.locator('[data-anim]')).toBeHidden();
  await expect(page.locator('.caps-nojs').first()).toBeVisible();
  await ctx.close();
});

test('cache stampede: coalescing turns many queries into one', async ({ page }) => {
  await page.goto('/learn/caching-and-cache-stampedes');
  const w = page.locator('[data-anim="cache-stampede"]');
  await expect(w.locator('.anim-result')).toContainText('Without coalescing: 240 database queries');
  await w.getByRole('switch').click();
  await expect(w.locator('.anim-result')).toContainText('With coalescing: 1 database query from this server');
});
