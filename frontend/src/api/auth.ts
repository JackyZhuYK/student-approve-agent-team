import http from './request'

export interface LoginRequest {
  username: string
  password: string
}

export interface UserInfo {
  id: string
  username: string
  email: string
  displayName: string
  role: string
}

export interface AuthResponse {
  accessToken: string
  refreshToken: string
  tokenType: string
  user: UserInfo
}

export const authApi = {
  login(data: LoginRequest): Promise<AuthResponse> {
    return http.post('/auth/login', data)
  },

  refreshToken(refreshToken: string): Promise<AuthResponse> {
    return http.post('/auth/refresh', null, { params: { refreshToken } })
  }
}
