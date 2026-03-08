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
    localStorage.clear()
  })

  it('should initialize with default values', () => {
    expect(store.user).toBeNull()
    expect(store.accessToken).toBeNull()
    expect(store.isLoggedIn).toBe(false)
  })

  it('should set user and tokens', () => {
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
    expect(localStorage.getItem('accessToken')).toBe('access-token')
  })

  it('should logout and clear storage', () => {
    store.setUser(
      { id: '123', username: 'test', email: 'test@test.com', displayName: 'Test', role: 'STUDENT' },
      'access-token',
      'refresh-token'
    )

    store.logout()

    expect(store.user).toBeNull()
    expect(store.accessToken).toBeNull()
    expect(store.isLoggedIn).toBe(false)
    expect(localStorage.getItem('accessToken')).toBeNull()
  })

  it('should compute isApprover correctly', () => {
    store.setUser(
      { id: '123', username: 'approver', email: 'approver@test.com', displayName: 'Approver', role: 'APPROVER' },
      'token',
      'refresh'
    )

    expect(store.isApprover).toBe(true)
    expect(store.isAdmin).toBe(false)
  })

  it('should compute isAdmin correctly', () => {
    store.setUser(
      { id: '123', username: 'admin', email: 'admin@test.com', displayName: 'Admin', role: 'ADMIN' },
      'token',
      'refresh'
    )

    expect(store.isApprover).toBe(true)
    expect(store.isAdmin).toBe(true)
  })

  it('should compute isApprover as false for student', () => {
    store.setUser(
      { id: '123', username: 'student', email: 'student@test.com', displayName: 'Student', role: 'STUDENT' },
      'token',
      'refresh'
    )

    expect(store.isApprover).toBe(false)
  })
})
