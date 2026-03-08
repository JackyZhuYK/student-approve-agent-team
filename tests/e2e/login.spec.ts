import { test, expect } from '@playwright/test'

// E2E Test for Login Flow
test.describe('Login Flow', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/')
  })

  test('should display login page when not authenticated', async ({ page }) => {
    // Should be redirected to login page
    await expect(page).toHaveURL('/login')
    await expect(page.locator('h2')).toContainText('学生审批系统')
  })

  test('should show validation errors for empty form', async ({ page }) => {
    await page.goto('/login')

    // Click login without entering credentials
    await page.click('button:has-text("登录")')

    // Should show validation errors
    await expect(page.locator('text=用户名不能为空')).toBeVisible()
    await expect(page.locator('text=密码不能为空')).toBeVisible()
  })

  test('should login successfully with admin credentials', async ({ page }) => {
    await page.goto('/login')

    // Enter credentials
    await page.fill('input[placeholder="请输入用户名"]', 'admin')
    await page.fill('input[type="password"]', 'admin123')

    // Click login
    await page.click('button:has-text("登录")')

    // Wait for navigation
    await page.waitForURL('/')

    // Should redirect to dashboard
    await expect(page.locator('h2')).toContainText('工作台')
  })

  test('should login successfully with approver credentials', async ({ page }) => {
    await page.goto('/login')

    await page.fill('input[placeholder="请输入用户名"]', 'approver')
    await page.fill('input[type="password"]', 'approver123')
    await page.click('button:has-text("登录")')

    await page.waitForURL('/')
    await expect(page.locator('h2')).toContainText('工作台')
  })

  test('should login successfully with student credentials', async ({ page }) => {
    await page.goto('/login')

    await page.fill('input[placeholder="请输入用户名"]', 'student')
    await page.fill('input[type="password"]', 'student123')
    await page.click('button:has-text("登录")')

    await page.waitForURL('/')
    await expect(page.locator('h2')).toContainText('工作台')
  })

  test('should show error message for invalid credentials', async ({ page }) => {
    await page.goto('/login')

    await page.fill('input[placeholder="请输入用户名"]', 'invalid')
    await page.fill('input[type="password"]', 'wrongpassword')
    await page.click('button:has-text("登录")')

    // Should show error message
    await expect(page.locator('.el-message--error')).toBeVisible()
  })

  test('should display test account hints', async ({ page }) => {
    await page.goto('/login')

    await expect(page.locator('text=测试账号')).toBeVisible()
    await expect(page.locator('text=admin / admin123')).toBeVisible()
    await expect(page.locator('text=approver / approver123')).toBeVisible()
    await expect(page.locator('text=student / student123')).toBeVisible()
  })

  test('should logout successfully', async ({ page }) => {
    // Login first
    await page.goto('/login')
    await page.fill('input[placeholder="请输入用户名"]', 'admin')
    await page.fill('input[type="password"]', 'admin123')
    await page.click('button:has-text("登录")')
    await page.waitForURL('/')

    // Click logout button
    await page.click('button:has-text("退出")')

    // Should redirect to login
    await page.waitForURL('/login')
  })
})

// E2E Test for Application Management
test.describe('Application Management', () => {
  test.beforeEach(async ({ page }) => {
    // Login as admin
    await page.goto('/login')
    await page.fill('input[placeholder="请输入用户名"]', 'admin')
    await page.fill('input[type="password"]', 'admin123')
    await page.click('button:has-text("登录")')
    await page.waitForURL('/')
  })

  test('should navigate to application list', async ({ page }) => {
    await page.click('text=我的申请')
    await expect(page.locator('h2')).toContainText('申请列表')
  })

  test('should create new application', async ({ page }) => {
    await page.click('text=我的申请')
    await page.click('text=新建申请')

    // Fill form
    await page.fill('input[placeholder="请输入标题"]', 'E2E 测试申请')
    await page.selectOption('.el-select', 'LEAVE')
    await page.fill('textarea[placeholder="请输入详细描述"]', '这是 E2E 测试创建的申请')

    // Submit
    await page.click('button:has-text("提交")')

    // Should show success message
    await expect(page.locator('.el-message--success')).toBeVisible()
  })

  test('should display applications table', async ({ page }) => {
    await page.click('text=我的申请')
    await expect(page.locator('.el-table')).toBeVisible()
  })
})

// E2E Test for Approval Flow
test.describe('Approval Flow', () => {
  test('should view pending approvals as approver', async ({ page }) => {
    // Login as approver
    await page.goto('/login')
    await page.fill('input[placeholder="请输入用户名"]', 'approver')
    await page.fill('input[type="password"]', 'approver123')
    await page.click('button:has-text("登录")')
    await page.waitForURL('/')

    // Navigate to approvals
    await page.click('text=审批处理')
    await expect(page.locator('h2')).toContainText('待处理审批')
  })
})
