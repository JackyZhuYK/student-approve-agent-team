<template>
  <div class="dashboard">
    <h2>工作台</h2>

    <el-row :gutter="20" class="stats-cards">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon pending">
              <el-icon :size="32"><Clock /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.pending }}</div>
              <div class="stat-label">待处理</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon in-review">
              <el-icon :size="32"><Search /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.inReview }}</div>
              <div class="stat-label">审核中</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon approved">
              <el-icon :size="32"><CircleCheck /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.approved }}</div>
              <div class="stat-label">已通过</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon rejected">
              <el-icon :size="32"><CircleClose /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.rejected }}</div>
              <div class="stat-label">已驳回</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="recent-card">
      <template #header>
        <div class="card-header">
          <span>最近申请</span>
          <router-link to="/applications">
            <el-button>查看全部</el-button>
          </router-link>
        </div>
      </template>

      <el-table :data="recentApplications" style="width: 100%">
        <el-table-column prop="title" label="标题" />
        <el-table-column prop="type" label="类型" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="submittedAt" label="提交时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.submittedAt) }}
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { applicationApi, type PageResponse, type Application } from '@/api'
import { useUserStore } from '@/stores/user'
import dayjs from 'dayjs'

const userStore = useUserStore()
const recentApplications = ref<Application[]>([])
const stats = ref({
  pending: 0,
  inReview: 0,
  approved: 0,
  rejected: 0
})

const isApprover = computed(() => userStore.isApprover)

onMounted(async () => {
  try {
    const response: PageResponse<Application> = isApprover.value
      ? await applicationApi.getPending()
      : await applicationApi.getList()

    recentApplications.value = response.content.slice(0, 5)

    // Calculate stats
    stats.value.pending = response.content.filter(
      (a) => a.status === 'PENDING'
    ).length
    stats.value.inReview = response.content.filter(
      (a) => a.status === 'IN_REVIEW'
    ).length
    stats.value.approved = response.content.filter(
      (a) => a.status === 'APPROVED'
    ).length
    stats.value.rejected = response.content.filter(
      (a) => a.status === 'REJECTED'
    ).length
  } catch (error) {
    console.error('Failed to load applications:', error)
  }
})

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

const formatDate = (date: string) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}
</script>

<style scoped>
.dashboard {
  padding: 20px;
}

.dashboard h2 {
  margin-bottom: 20px;
  color: #303133;
}

.stats-cards {
  margin-bottom: 20px;
}

.stat-card {
  border-radius: 8px;
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 64px;
  height: 64px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-icon.pending {
  background: #fff7e6;
  color: #faad14;
}

.stat-icon.in-review {
  background: #e6f7ff;
  color: #1890ff;
}

.stat-icon.approved {
  background: #f6ffed;
  color: #52c41a;
}

.stat-icon.rejected {
  background: #fff1f0;
  color: #ff4d4f;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
