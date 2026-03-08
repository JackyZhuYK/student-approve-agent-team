<template>
  <div class="application-list">
    <div class="list-header">
      <h2>申请列表</h2>
      <el-button type="primary" @click="showCreateDialog = true">
        <el-icon><Plus /></el-icon>
        新建申请
      </el-button>
    </div>

    <el-card>
      <el-table :data="applications" v-loading="loading" style="width: 100%">
        <el-table-column prop="title" label="标题" min-width="200" />
        <el-table-column prop="type" label="类型" width="120" />
        <el-table-column prop="priority" label="优先级" width="100">
          <template #default="{ row }">
            <el-tag :type="getPriorityType(row.priority)" size="small">
              {{ getPriorityText(row.priority) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="applicant" label="申请人" width="150">
          <template #default="{ row }">
            {{ row.applicant?.displayName || '未知' }}
          </template>
        </el-table-column>
        <el-table-column prop="submittedAt" label="提交时间" width="160">
          <template #default="{ row }">
            {{ formatDate(row.submittedAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              link
              @click="viewDetail(row.id)"
            >
              查看
            </el-button>
            <el-button
              v-if="row.status === 'PENDING' && isCurrentUser(row.applicant?.id)"
              type="danger"
              link
              @click="withdraw(row)"
            >
              撤回
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="totalElements"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @current-change="loadApplications"
        @size-change="loadApplications"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>

    <!-- Create Application Dialog -->
    <el-dialog
      v-model="showCreateDialog"
      title="新建申请"
      width="600px"
      @close="resetForm"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入标题" />
        </el-form-item>

        <el-form-item label="类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择类型" style="width: 100%">
            <el-option label="请假申请" value="LEAVE" />
            <el-option label="课程选择" value="COURSE" />
            <el-option label="项目审批" value="PROJECT" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>

        <el-form-item label="优先级" prop="priority">
          <el-select v-model="form.priority" placeholder="请选择优先级" style="width: 100%">
            <el-option label="低" value="LOW" />
            <el-option label="普通" value="NORMAL" />
            <el-option label="高" value="HIGH" />
            <el-option label="紧急" value="URGENT" />
          </el-select>
        </el-form-item>

        <el-form-item label="描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="4"
            placeholder="请输入详细描述"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitting">
          提交
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { applicationApi } from '@/api'
import { useUserStore } from '@/stores/user'
import dayjs from 'dayjs'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref<FormInstance>()

const applications = ref([])
const loading = ref(false)
const submitting = ref(false)
const showCreateDialog = ref(false)
const currentPage = ref(0)
const pageSize = ref(10)
const totalElements = ref(0)

const form = reactive({
  title: '',
  type: '',
  priority: 'NORMAL',
  description: ''
})

const rules: FormRules = {
  title: [{ required: true, message: '标题不能为空', trigger: 'blur' }],
  type: [{ required: true, message: '类型不能为空', trigger: 'change' }],
  description: [{ required: true, message: '描述不能为空', trigger: 'blur' }]
}

const loadApplications = async () => {
  loading.value = true
  try {
    const response = await applicationApi.getList(currentPage.value - 1, pageSize.value)
    applications.value = response.content
    totalElements.value = response.totalElements
  } catch (error) {
    ElMessage.error('加载申请列表失败')
  } finally {
    loading.value = false
  }
}

const viewDetail = (id: string) => {
  router.push(`/applications/${id}`)
}

const isCurrentUser = (applicantId?: string) => {
  return applicantId === userStore.user?.id
}

const withdraw = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定要撤回此申请吗？', '确认', {
      type: 'warning'
    })
    await applicationApi.withdraw(row.id)
    ElMessage.success('申请已撤回')
    loadApplications()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error('撤回申请失败')
    }
  }
}

const submitForm = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    submitting.value = true
    try {
      await applicationApi.create(form)
      ElMessage.success('申请创建成功')
      showCreateDialog.value = false
      loadApplications()
    } catch (error) {
      ElMessage.error('创建申请失败')
    } finally {
      submitting.value = false
    }
  })
}

const resetForm = () => {
  form.title = ''
  form.type = ''
  form.priority = 'NORMAL'
  form.description = ''
  formRef.value?.resetFields()
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

onMounted(() => {
  loadApplications()
})
</script>

<style scoped>
.application-list {
  padding: 20px;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.list-header h2 {
  margin: 0;
  color: #303133;
}
</style>
