<template>
  <div class="approval-list">
    <h2>待处理审批</h2>

    <el-card>
      <el-table :data="approvals" v-loading="loading" style="width: 100%">
        <el-table-column prop="applicationId" label="申请" min-width="200">
          <template #default="{ row }">
            {{ row.application?.title || '申请' }}
          </template>
        </el-table-column>

        <el-table-column prop="applicant" label="申请人" width="150">
          <template #default="{ row }">
            {{ row.application?.applicant?.displayName }}
          </template>
        </el-table-column>

        <el-table-column prop="type" label="类型" width="120" />

        <el-table-column prop="submittedAt" label="提交时间" width="160">
          <template #default="{ row }">
            {{ formatDate(row.application?.submittedAt) }}
          </template>
        </el-table-column>

        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              size="small"
              @click="viewApplication(row.application?.id)"
            >
              查看
            </el-button>

            <el-button
              type="success"
              size="small"
              @click="approve(row)"
            >
              通过
            </el-button>

            <el-button
              type="danger"
              size="small"
              @click="reject(row)"
            >
              驳回
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && approvals.length === 0" description="暂无待处理审批" />
    </el-card>

    <!-- Quick Approval Dialog -->
    <el-dialog
      v-model="showDialog"
      :title="dialogAction === 'approve' ? '通过申请' : '驳回申请'"
      width="500px"
      @close="resetDialog"
    >
      <el-form>
        <el-form-item label="审批意见">
          <el-input
            v-model="comment"
            type="textarea"
            :rows="4"
            placeholder="请输入审批意见（可选）"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button
          :type="dialogAction === 'approve' ? 'success' : 'danger'"
          @click="submitApproval"
          :loading="submitting"
        >
          确认
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { applicationApi, approvalApi } from '@/api'
import dayjs from 'dayjs'

const router = useRouter()

const approvals = ref<any[]>([])
const loading = ref(false)
const submitting = ref(false)
const showDialog = ref(false)
const dialogAction = ref<'approve' | 'reject'>('approve')
const currentApplicationId = ref('')
const comment = ref('')

const loadApprovals = async () => {
  loading.value = true
  try {
    const response = await approvalApi.getPending()
    approvals.value = response
  } catch (error) {
    ElMessage.error('加载待处理审批失败')
  } finally {
    loading.value = false
  }
}

const viewApplication = (id: string) => {
  router.push(`/applications/${id}`)
}

const approve = (row: any) => {
  currentApplicationId.value = row.application?.id
  dialogAction.value = 'approve'
  showDialog.value = true
}

const reject = (row: any) => {
  currentApplicationId.value = row.application?.id
  dialogAction.value = 'reject'
  showDialog.value = true
}

const submitApproval = async () => {
  if (!currentApplicationId.value) return

  submitting.value = true
  try {
    await approvalApi.process(currentApplicationId.value, {
      action: dialogAction.value.toUpperCase(),
      comment: comment.value
    })

    ElMessage.success(dialogAction.value === 'approve' ? '已通过申请' : '已驳回申请')
    showDialog.value = false
    loadApprovals()
  } catch (error) {
    ElMessage.error('提交审批失败')
  } finally {
    submitting.value = false
  }
}

const resetDialog = () => {
  comment.value = ''
  currentApplicationId.value = ''
}

const formatDate = (date: string) => {
  return date ? dayjs(date).format('YYYY-MM-DD HH:mm') : '-'
}

onMounted(() => {
  loadApprovals()
})
</script>

<style scoped>
.approval-list {
  padding: 20px;
}

.approval-list h2 {
  margin-bottom: 20px;
  color: #303133;
}
</style>
