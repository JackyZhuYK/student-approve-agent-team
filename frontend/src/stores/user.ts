import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { UserInfo } from '@/api/auth'

function getLocalStorageItem(key: string): string | null {
  try {
    return localStorage.getItem(key)
  } catch {
    return null
  }
}

export const useUserStore = defineStore('user', () => {
  const user = ref<UserInfo | null>(null)
  const accessToken = ref<string | null>(getLocalStorageItem('accessToken'))
  const refreshToken = ref<string | null>(getLocalStorageItem('refreshToken'))

  const isLoggedIn = computed(() => !!accessToken.value)
  const userRole = computed(() => user.value?.role || '')
  const isApprover = computed(() => {
    const role = userRole.value
    return role === 'APPROVER' || role === 'ADMIN'
  })
  const isAdmin = computed(() => userRole.value === 'ADMIN')

  function setUser(userData: UserInfo, token: string, refresh: string) {
    user.value = userData
    accessToken.value = token
    refreshToken.value = refresh
    localStorage.setItem('accessToken', token)
    localStorage.setItem('refreshToken', refresh)
  }

  function logout() {
    user.value = null
    accessToken.value = null
    refreshToken.value = null
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
  }

  function loadUserFromStorage() {
    const token = localStorage.getItem('accessToken')
    const refresh = localStorage.getItem('refreshToken')
    if (token) {
      accessToken.value = token
    }
    if (refresh) {
      refreshToken.value = refresh
    }
  }

  return {
    user,
    accessToken,
    refreshToken,
    isLoggedIn,
    userRole,
    isApprover,
    isAdmin,
    setUser,
    logout,
    loadUserFromStorage
  }
})
