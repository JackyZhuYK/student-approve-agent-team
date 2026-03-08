// Test setup file
import { config } from '@vue/test-utils'
import { vi } from 'vitest'

// Mock global components if needed
config.global.mocks = {}

// Mock localStorage
const localStorageMock = {
  getItem: vi.fn(),
  setItem: vi.fn(),
  removeItem: vi.fn(),
  clear: vi.fn()
}

Object.defineProperty(global, 'localStorage', {
  value: localStorageMock
})

// Mock window.location
Object.defineProperty(global, 'location', {
  value: {
    href: 'http://localhost/',
    origin: 'http://localhost/'
  }
})
