<template>
  <div class="application-detail" v-loading="loading">
    <div class="detail-header" v-if="application">
      <el-page-header @back="goBack">
        <template #content>
          <div class="header-content">
            <span class="title">{{ application.title }}</span>
            <el-tag :type="getStatusType(application.status)">
              {{ getStatusText(application.status) }}
            </el-tag>
          </div>
        </template>
      </el-page-header>
    </div>

    <el-row :gutter="20" v-if="application">
      <el-col :span="16">
        <el-card class="info-card">
          <template #header>申请详情</template>

          <el-descriptions :column="2" border>
            <el-descriptions-item label="类型">{{ application.type }}</el-descriptions-item>
            <el-descriptions-item label="优先级">
              <el-tag :type="getPriorityType(application.priority)" size="small">
                {{ getPriorityText(application.priority) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="申请人">
              {{ application.applicant?.displayName }}
            </el-descriptions-item>
            <el-descriptions-item label="提交时间">
              {{ formatDate(application.submittedAt) }}
            </el-descriptions-item>
            <el-descriptions-item label="更新时间" :span="2">
              {{ formatDate(application.updatedAt) }}
            </el-descriptions-item>
          </el-descriptions>

          <el-divider />

          <div class="description-section">
            <h4>申请描述</h4>
            <p>{{ application.description }}</p>
          </div>
        </el-card>

        <!-- Approvals Section -->
        <el-card class="approvals-card" style="margin-top: 20px">
          <template #header>审批历史</template>

          <el-timeline v-if="application.approvals && application.approvals.length > 0">
            <el-timeline-item
              v-for="approval in application.approvals"
              :key="approval.id"
              :type="getApprovalTimelineType(approval.action)"
              :icon="getApprovalIcon(approval.action)"
            >
              <el-card>
                <div class="approval-item">
                  <div class="approval-header">
                    <span class="approver">{{ approval.approverName }}</span>
                    <el-tag :type="getApprovalTagType(approval.action)" size="small">
                      {{ getApprovalActionText(approval.action) }}
                    </el-tag>
                  </div>
                  <div v-if="approval.agentRole" class="agent-role">
                    Agent: {{ approval.agentRole }}
                  </div>
                  <p v-if="approval.comment" class="comment">{{ approval.comment }}</p>
                  <span class="time">{{ formatDate(approval.createdAt) }}</span>
                </div>
              </el-card>
            </el-timeline-item>
          </el-timeline>

          <el-empty v-else description="暂无审批记录" />
        </el-card>
      </el-col>

      <el-col :span="8">
        <!-- Approval Action Card -->
        <el-card v-if="canApprove" class="action-card">
          <template #header>审批操作</template>

          <el-form @submit.prevent="submitApproval">
            <el-form-item label="审批决定">
              <el-radio-group v-model="approvalForm.action">
                <el-radio label="APPROVED">通过</el-radio>
                <el-radio label="REJECTED">驳回</el-radio>
              </el-radio-group>
            </el-form-item>

            <el-form-item label="意见">
              <el-input
                v-model="approvalForm.comment"
                type="textarea"
                :rows="3"
                placeholder="请输入审批意见"
              />
            </el-form-item>

            <el-button
              type="primary"
              @click="submitApproval"
              :loading="submitting"
              style="width: 100%"
            >
              提交审批
            </el-button>
          </el-form>
        </el-card>

        <!-- Agent Actions -->
        <el-card v-if="isAdmin" class="action-card" style="margin-top: 20px">
          <template #header>Agent 操作</template>

          <div class="agent-actions">
            <el-button
              v-for="agent in agentRoles"
              :key="agent"
              @click="agentAction(agent)"
              style="width: 100%; margin-bottom: 10px"
            >
              {{ agent }} 审批
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { applicationApi, approvalApi } from '@/api'
import { useUserStore } from '@/stores/user'
import dayjs from 'dayjs'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const application = ref<any>(null)
const loading = ref(false)
const submitting = ref(false)

const approvalForm = reactive({
  action: 'APPROVED',
  comment: ''
})

const agentRoles = ['PRODUCT_AGENT', 'ARCHITECT_AGENT', 'BACKEND_AGENT', 'FRONTEND_AGENT', 'TEST_AGENT', 'SECURITY_AGENT']

const canApprove = computed(() => userStore.isApprover)
const isAdmin = computed(() => userStore.isAdmin)

const loadApplication = async () => {
  loading.value = true
  try {
    const response = await applicationApi.getById(route.params.id as string)
    application.value = response
  } catch (error) {
    ElMessage.error('加载申请详情失败')
  } finally {
    loading.value = false
  }
}

const submitApproval = async () => {
  submitting.value = true
  try {
    await approvalApi.process(route.params.id as string, approvalForm)
    ElMessage.success('审批已提交')
    loadApplication()
  } catch (error) {
    ElMessage.error('提交审批失败')
  } finally {
    submitting.value = false
  }
}

const agentAction = async (agentRole: string) => {
  try {
    await approvalApi.agentApprove(route.params.id as string, agentRole, `Agent ${agentRole} 已审批`)
    ElMessage.success(`${agentRole} 审批已提交`)
    loadApplication()
  } catch (error) {
    ElMessage.error('Agent 审批失败')
  }
}

const goBack = () => {
  router.back()
}

const getStatusType = (status: string) => {
  const types: Record<string, any> = {
    PENDING: 'warning',
    IN_REVIEW: 'primary',
    APPROVED: 'success',
    REJECTED: 'danger',
    WITHDRAWN: 'info'
  }
  return types[status] || 'info'
}

const getStatusText = (status: string) => {
  const texts: Record<string, string> = {
    PENDING: '待处理',
    IN_REVIEW: '审核中',
    APPROVED: '已通过',
    REJECTED: '已驳回',
    WITHDRAWN: '已撤回'
  }
  return texts[status] || status
}

const getPriorityType = (priority: string) => {
  const types: Record<string, any> = {
    LOW: 'info',
    NORMAL: '',
    HIGH: 'warning',
    URGENT: 'danger'
  }
  return types[priority] || ''
}

const getPriorityText = (priority: string) => {
  const texts: Record<string, string> = {
    LOW: '低',
    NORMAL: '普通',
    HIGH: '高',
    URGENT: '紧急'
  }
  return texts[priority] || priority
}

const getApprovalTagType = (action: string) => {
  const types: Record<string, any> = {
    APPROVED: 'success',
    REJECTED: 'danger',
    PENDING: 'warning'
  }
  return types[action] || 'info'
}

const getApprovalActionText = (action: string) => {
  const texts: Record<string, string> = {
    APPROVED: '已通过',
    REJECTED: '已驳回',
    PENDING: '待审批'
  }
  return texts[action] || action
}

const getApprovalTimelineType = (action: string) => {
  const types: Record<string, any> = {
    APPROVED: 'success',
    REJECTED: 'danger',
    PENDING: 'warning'
  }
  return types[action] || 'primary'
}

const getApprovalIcon = (action: string) => {
  const icons: Record<string, any> = {
    APPROVED: 'CircleCheck',
    REJECTED: 'CircleClose',
    PENDING: 'Clock'
  }
  return icons[action] || 'InfoFilled'
}

const formatDate = (date: string) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

onMounted(() => {
  loadApplication()
})
</script>

<style scoped>
.application-detail {
  padding: 20px;
}

.detail-header {
  margin-bottom: 20px;
}

.header-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.title {
  font-size: 18px;
  font-weight: 500;
}

.description-section {
  margin-top: 16px;
}

.description-section h4 {
  margin-bottom: 8px;
  color: #303133;
}

.description-section p {
  color: #606266;
  line-height: 1.6;
}

.approval-item {
  padding: 8px 0;
}

.approval-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.approver {
  font-weight: 500;
  color: #303133;
}

.agent-role {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}

.comment {
  color: #606266;
  margin: 8px 0;
}

.time {
  font-size: 12px;
  color: #909399;
}

.action-card {
  position: sticky;
  top: 20px;
}

.agent-actions {
  display: flex;
  flex-direction: column;
}
</style>
