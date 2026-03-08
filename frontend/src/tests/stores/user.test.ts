import { describe, it, expect, beforeEach, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { useUserStore } from '@/stores/user'

describe('User Store', () => {
  let pinia: ReturnType<typeof createPinia>
  let store: ReturnType<typeof useUserStore>

  beforeEach(() => {
    pinia = createPinia()
    setActivePinia(pinia)
    store = useUserStore(pinia)
  })

  it('should initialize with default values', () => {
    expect(store.user).toBeNull()
    expect(store.isLoggedIn).toBe(false)
  })

  it('should set user and compute properties', () => {
    const userInfo = {
      id: '123',
      username: 'testuser',
      email: 'test@example.com',
      displayName: 'Test User',
      role: 'STUDENT'
    }

    store.setUser(userInfo, 'access-token', 'refresh-token')

    expect(store.user).toEqual(userInfo)
    expect(store.accessToken).toBe('access-token')
    expect(store.isLoggedIn).toBe(true)
  })

  it('should compute isApprover correctly for APPROVER', () => {
    store.setUser(
      { id: '123', username: 'approver', email: 'approver@test.com', displayName: 'Approver', role: 'APPROVER' },
      'token',
      'refresh'
    )

    expect(store.isApprover).toBe(true)
    expect(store.isAdmin).toBe(false)
  })

  it('should compute isAdmin correctly for ADMIN', () => {
    store.setUser(
      { id: '123', username: 'admin', email: 'admin@test.com', displayName: 'Admin', role: 'ADMIN' },
      'token',
      'refresh'
    )

    expect(store.isApprover).toBe(true)
    expect(store.isAdmin).toBe(true)
  })
})
