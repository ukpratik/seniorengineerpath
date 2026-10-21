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

test('token bucket: a bigger bucket absorbs the burst', async ({ page }) => {
  await page.goto('/learn/rate-limiting');
  const w = page.locator('[data-anim="token-bucket"]');
  await expect(w.locator('.anim-result')).toContainText('rejected');
  await w.getByLabel(/Bucket size/).fill('100');
  await expect(w.locator('.anim-result')).toContainText('All');
});

test('hash ring: a ring moves far fewer keys than hash mod N', async ({ page }) => {
  await page.goto('/learn/sharding-and-consistent-hashing');
  const w = page.locator('[data-anim="hash-ring"]');
  const mod = Number((await w.locator('.cmp.bad b').textContent())!.replace('%', ''));
  const ring = Number((await w.locator('.cmp.ok b').textContent())!.replace('%', ''));
  expect(mod).toBeGreaterThan(60);
  expect(ring).toBeLessThan(30);
});

test('charts draw real bar heights (not the 8 px progress-bar style)', async ({ page }) => {
  await page.goto('/learn/caching-and-cache-stampedes');
  const h = await page.locator('[data-anim="cache-stampede"] rect.abar').evaluateAll((rs) => Math.max(...rs.map((r) => r.getBoundingClientRect().height)));
  expect(h).toBeGreaterThan(40);
});

test('replication lag: labels stay inside their lanes at any setting (BUG-016)', async ({ page }) => {
  await page.setViewportSize({ width: 360, height: 800 });
  await page.goto('/learn/database-replication');
  const w = page.locator('[data-anim="replication-lag"]');
  for (const [lag, after] of [['1000', '1000'], ['2000', '600'], ['0', '2000']]) {
    await w.getByLabel(/Replication lag/).fill(lag);
    await w.getByLabel(/You read again after/).fill(after);
    const outside = await w.evaluate((host) =>
      [...host.querySelectorAll('.lane')].filter((lane) => {
        const tl = lane.querySelector('.tl')!.getBoundingClientRect();
        const ev = lane.querySelector('.ev')!.getBoundingClientRect();
        return ev.left < tl.left - 0.5 || ev.right > tl.right + 0.5;
      }).length,
    );
    expect(outside).toBe(0);
  }
});
