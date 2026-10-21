import { test, expect } from '@playwright/test';

const LESSON = '/learn/idempotency-retries-timeouts';

test('read a lesson: step through, answer the quiz, mark as done', async ({ page }) => {
  await page.goto(LESSON);
  await expect(page.getByRole('heading', { level: 1 })).toHaveText('Idempotency, retries and timeouts');

  const fig = page.locator('[data-stepper]').first();
  await expect(fig.locator('[data-count]')).toHaveText('Step 1 of 5');
  await expect(fig.locator('[data-prev]')).toBeDisabled();
  await fig.locator('[data-next]').click();
  await expect(fig.locator('[data-count]')).toHaveText('Step 2 of 5');
  await expect(fig.locator('[data-cap]')).toContainText('Step 2.');
  await fig.focus();
  await page.keyboard.press('ArrowRight');
  await expect(fig.locator('[data-count]')).toHaveText('Step 3 of 5');
  await page.keyboard.press('ArrowLeft');
  await expect(fig.locator('[data-count]')).toHaveText('Step 2 of 5');

  const q1 = page.locator('fieldset.q').first();
  await q1.getByLabel('The payment failed.').check();
  await expect(q1.locator('.fb')).toContainText('Not quite');
  await q1.getByLabel(/Nothing\. The server may or may not/).check();
  await expect(q1.locator('.fb')).toContainText('Correct');

  const done = page.locator('[data-done]');
  await done.click();
  await expect(done).toContainText('Done ✓ (undo)');
  await page.reload();
  await expect(page.locator('[data-done]')).toContainText('Done ✓ (undo)');
  await page.locator('[data-done]').click();
  await expect(page.locator('[data-done]')).toContainText('Mark as done');
});

test('glossary term opens a sheet that closes with Esc and returns focus', async ({ page }) => {
  await page.goto(LESSON);
  const term = page.locator('a.term[data-term="idempotent"]');
  await term.click();
  const sheet = page.locator('#gloss');
  await expect(sheet).toBeVisible();
  await expect(sheet.locator('.g-title')).toHaveText('Idempotent');
  await expect(sheet.locator('.g-def')).toContainText('twice');
  await page.keyboard.press('Escape');
  await expect(sheet).toBeHidden();
  await expect(term).toBeFocused();
});

test('sources list opens from a footnote', async ({ page }) => {
  await page.goto(LESSON);
  await page.locator('sup a').first().click();
  await expect(page.locator('details.deeper')).toHaveAttribute('open', '');
  await expect(page.locator('#src-stripe')).toBeVisible();
});

test('report a mistake links to a prefilled GitHub issue', async ({ page }) => {
  await page.goto(LESSON);
  const href = await page.getByRole('link', { name: /Report a mistake/ }).getAttribute('href');
  expect(href).toContain('github.com/ukpratik/seniorengineerpath/issues/new');
  expect(href).toContain('idempotency-retries');
});
