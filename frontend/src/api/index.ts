import http from './request'

export interface Application {
  id: string
  title: string
  description: string
  type: string
  status: string
  priority: string
  attachmentUrls?: string[]
  applicant?: {
    id: string
    username: string
    displayName: string
  }
  submittedAt: string
  updatedAt?: string
  completedAt?: string
  approvals?: Approval[]
}

export interface Approval {
  id: string
  approverName: string
  agentRole?: string
  action: string
  comment?: string
  createdAt: string
}

export interface CreateApplicationRequest {
  title: string
  description: string
  type: string
  priority?: string
  attachmentUrls?: string[]
}

export interface ApprovalRequest {
  action: string
  comment?: string
}

export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}

export const applicationApi = {
  create(data: CreateApplicationRequest): Promise<Application> {
    return http.post('/applications', data)
  },

  getById(id: string): Promise<Application> {
    return http.get(`/applications/${id}`)
  },

  getList(page: number = 0, size: number = 10): Promise<PageResponse<Application>> {
    return http.get('/applications', { params: { page, size } })
  },

  getAll(page: number = 0, size: number = 10): Promise<PageResponse<Application>> {
    return http.get('/applications/all', { params: { page, size } })
  },

  getPending(page: number = 0, size: number = 10): Promise<PageResponse<Application>> {
    return http.get('/applications/pending', { params: { page, size } })
  },

  withdraw(id: string): Promise<Application> {
    return http.post(`/applications/${id}/withdraw`)
  }
}

export const approvalApi = {
  getPending(): Promise<Approval[]> {
    return http.get('/approvals/pending')
  },

  getByApplication(applicationId: string): Promise<Approval[]> {
    return http.get(`/approvals/application/${applicationId}`)
  },

  process(applicationId: string, data: ApprovalRequest): Promise<Application> {
    return http.post(`/approvals/application/${applicationId}`, data)
  },

  agentApprove(applicationId: string, agentRole: string, comment?: string): Promise<Application> {
    return http.post(`/approvals/application/${applicationId}/agent/${agentRole}`, null, { params: { comment } })
  }
}
