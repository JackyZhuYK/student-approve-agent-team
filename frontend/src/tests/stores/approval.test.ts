import { describe, it, expect, beforeEach, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { useApprovalStore } from '@/stores/approval'

describe('Approval Store', () => {
  let pinia: ReturnType<typeof createPinia>
  let store: ReturnType<typeof useApprovalStore>

  beforeEach(() => {
    pinia = createPinia()
    setActivePinia(pinia)
    store = useApprovalStore(pinia)
  })

  it('should initialize with default values', () => {
    expect(store.pendingApprovals).toEqual([])
    expect(store.applications).toEqual([])
    expect(store.loading).toBe(false)
  })

  it('should set pending approvals', () => {
    const approvals = [
      { id: '1', approverName: 'Approver 1', action: 'PENDING' },
      { id: '2', approverName: 'Approver 2', action: 'PENDING' }
    ]

    store.setPendingApprovals(approvals)

    expect(store.pendingApprovals).toEqual(approvals)
  })

  it('should add a single approval', () => {
    const approval = { id: '1', approverName: 'Approver 1', action: 'PENDING' }

    store.addApproval(approval)

    expect(store.pendingApprovals).toHaveLength(1)
    expect(store.pendingApprovals[0]).toEqual(approval)
  })

  it('should update existing approval', () => {
    const approval = { id: '1', approverName: 'Approver 1', action: 'PENDING' }
    store.addApproval(approval)

    const updatedApproval = { id: '1', approverName: 'Approver 1', action: 'APPROVED' }
    store.addApproval(updatedApproval)

    expect(store.pendingApprovals).toHaveLength(1)
    expect(store.pendingApprovals[0].action).toBe('APPROVED')
  })

  it('should set applications', () => {
    const applications = [
      { id: '1', title: 'Application 1', status: 'PENDING' },
      { id: '2', title: 'Application 2', status: 'APPROVED' }
    ]

    store.setApplications(applications)

    expect(store.applications).toEqual(applications)
  })

  it('should add application to the beginning', () => {
    const app1 = { id: '1', title: 'Application 1', status: 'PENDING' }
    const app2 = { id: '2', title: 'Application 2', status: 'APPROVED' }

    store.addApplication(app1)
    store.addApplication(app2)

    expect(store.applications).toHaveLength(2)
    expect(store.applications[0]).toEqual(app2)
    expect(store.applications[1]).toEqual(app1)
  })

  it('should update existing application', () => {
    const app = { id: '1', title: 'Application 1', status: 'PENDING' }
    store.addApplication(app)

    const updatedApp = { id: '1', title: 'Application 1', status: 'APPROVED' }
    store.updateApplication(updatedApp)

    expect(store.applications).toHaveLength(1)
    expect(store.applications[0].status).toBe('APPROVED')
  })

  it('should not update non-existing application', () => {
    const app = { id: '1', title: 'Application 1', status: 'PENDING' }
    store.addApplication(app)

    const updatedApp = { id: '2', title: 'Application 2', status: 'APPROVED' }
    store.updateApplication(updatedApp)

    expect(store.applications).toHaveLength(1)
    expect(store.applications[0].id).toBe('1')
  })
})
