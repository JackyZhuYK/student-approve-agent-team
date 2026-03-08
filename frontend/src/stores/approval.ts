import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Application, Approval } from '@/api'

export const useApprovalStore = defineStore('approval', () => {
  const pendingApprovals = ref<Approval[]>([])
  const applications = ref<Application[]>([])
  const loading = ref(false)

  function setPendingApprovals(approvals: Approval[]) {
    pendingApprovals.value = approvals
  }

  function addApproval(approval: Approval) {
    const index = pendingApprovals.value.findIndex(a => a.id === approval.id)
    if (index >= 0) {
      pendingApprovals.value[index] = approval
    } else {
      pendingApprovals.value.push(approval)
    }
  }

  function setApplications(apps: Application[]) {
    applications.value = apps
  }

  function addApplication(app: Application) {
    applications.value.unshift(app)
  }

  function updateApplication(app: Application) {
    const index = applications.value.findIndex(a => a.id === app.id)
    if (index >= 0) {
      applications.value[index] = app
    }
  }

  return {
    pendingApprovals,
    applications,
    loading,
    setPendingApprovals,
    addApproval,
    setApplications,
    addApplication,
    updateApplication
  }
})
