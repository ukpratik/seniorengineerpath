import { test, expect } from '@playwright/test';
import AxeBuilder from '@axe-core/playwright';
import { readdirSync, statSync } from 'node:fs';
import { join, relative } from 'node:path';

// Every built page (NFR-005).
function pages(dir = 'dist'): string[] {
  return readdirSync(dir).flatMap((f) => {
    const p = join(dir, f);
    if (statSync(p).isDirectory()) return f === '_astro' || f === 'pagefind' ? [] : pages(p);
    return f.endsWith('.html') ? ['/' + relative('dist', p).replace(/index\.html$/, '').replace(/\.html$/, '')] : [];
  });
}

const LESSON = '/learn/idempotency-retries-timeouts';

test.describe('360 px phone', () => {
  test.use({ viewport: { width: 360, height: 780 } });
  for (const path of pages()) {
    test(`no horizontal scroll: ${path}`, async ({ page }) => {
      await page.goto(path);
      const [sw, cw] = await page.evaluate(() => [document.documentElement.scrollWidth, document.documentElement.clientWidth]);
      expect(sw).toBeLessThanOrEqual(cw);
    });
  }
});

test('reduced motion hides Play and never autoplays', async ({ browser }) => {
  const ctx = await browser.newContext({ reducedMotion: 'reduce' });
  const page = await ctx.newPage();
  await page.goto(LESSON);
  await expect(page.locator('[data-play]').first()).toBeHidden();
  await page.waitForTimeout(4000);
  await expect(page.locator('[data-count]').first()).toHaveText('Step 1 of 5');
  await ctx.close();
});

test('without JavaScript every frame and caption is readable', async ({ browser }) => {
  const ctx = await browser.newContext({ javaScriptEnabled: false });
  const page = await ctx.newPage();
  await page.goto(LESSON);
  await expect(page.locator('.caps-nojs li')).toHaveCount(5);
  await expect(page.locator('.caps-nojs')).toBeVisible();
  await expect(page.locator('.ctrls')).toBeHidden();
  await expect(page.locator('.dg .f[data-f="5"]')).toBeVisible();
  await expect(page.locator('#s8 h2')).toHaveText('Go deeper');
  await ctx.close();
});

test('blocked storage: the page still works and says progress cannot be saved', async ({ page }) => {
  await page.addInitScript(() => {
    Object.defineProperty(window, 'localStorage', { get() { throw new Error('blocked'); } });
  });
  await page.goto(LESSON);
  await expect(page.locator('[data-done]')).toBeDisabled();
  await expect(page.locator('[data-done]')).toContainText('Can’t save progress');
  await page.locator('[data-stepper] [data-next]').first().click();
  await expect(page.locator('[data-count]').first()).toHaveText('Step 2 of 5');
});

for (const path of ['/', LESSON]) {
  for (const theme of ['light', 'dark'] as const) {
    test(`axe: ${path} (${theme})`, async ({ browser }) => {
      const ctx = await browser.newContext({ colorScheme: theme });
      const page = await ctx.newPage();
      await page.goto(path);
      const r = await new AxeBuilder({ page }).withTags(['wcag2a', 'wcag2aa', 'wcag21aa', 'wcag22aa']).analyze();
      const serious = r.violations.filter((v) => v.impact === 'serious' || v.impact === 'critical');
      expect(serious.map((v) => `${v.id}: ${v.nodes.length} × ${v.nodes[0]?.target}`)).toEqual([]);
      await ctx.close();
    });
  }
}
