import { expect, test } from '@playwright/test'

const email = process.env.TAHA_E2E_ADMIN_EMAIL
const password = process.env.TAHA_E2E_ADMIN_PASSWORD

// The approved harness deliberately uses one ephemeral administrator. Its
// session/login bookkeeping is versioned, so concurrent browser workers must
// not race to authenticate the same principal.
test.describe.configure({ mode: 'serial' })

test.beforeAll(() => {
  if (!process.env.TAHA_E2E_BASE_URL || !email || !password) {
    throw new Error(
      'TAHA_E2E_BASE_URL, TAHA_E2E_ADMIN_EMAIL, and TAHA_E2E_ADMIN_PASSWORD are required for live admin E2E.'
    )
  }
})

async function signIn(page) {
  await page.goto('/admin/login')
  await page.getByRole('textbox', { name: 'Email address' }).fill(email)
  await page.getByLabel('Password').fill(password)
  await page.getByRole('button', { name: 'Sign in' }).click()
}

test('administrator can sign in, reach the dashboard, and log out', async ({ page }) => {
  await signIn(page)
  await expect(page.getByRole('heading', { name: 'Dashboard' })).toBeVisible()
  await page.getByRole('button', { name: 'Log out' }).click()
  await expect(page.getByRole('heading', { name: 'Admin sign in' })).toBeVisible()

  await page.goto('/admin')
  await expect(page).toHaveURL(/\/admin\/login/)
})

test('desktop navigation remains available after selecting an admin destination', async ({ page }) => {
  await signIn(page)
  const navigation = page.getByTestId('admin-navigation')

  await expect(navigation).toBeVisible()
  await navigation.getByText('Site settings', { exact: true }).click()

  await expect(page.getByRole('heading', { name: 'Site settings' })).toBeVisible()
  await expect(navigation).toBeVisible()
  await expect(
    page.getByRole('button', { name: 'Toggle administration navigation' })
  ).toBeVisible()
})
