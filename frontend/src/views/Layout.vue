<template>
  <div class="app-container">
    <el-container>
      <el-header class="header">
        <div class="header-content">
          <h1 class="logo">学生审批系统</h1>
          <nav class="nav">
            <router-link to="/">工作台</router-link>
            <router-link to="/applications">我的申请</router-link>
            <router-link to="/approvals" v-if="isApprover">审批处理</router-link>
          </nav>
          <div class="user-info">
            <span>{{ userStore.user?.displayName }}</span>
            <el-button text @click="handleLogout">
              <el-icon><SwitchButton /></el-icon>
            </el-button>
          </div>
        </div>
      </el-header>

      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const isApprover = userStore.isApprover

const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.app-container {
  min-height: 100vh;
  background: #f5f7fa;
}

.header {
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  padding: 0 20px;
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 60px;
  max-width: 1400px;
  margin: 0 auto;
}

.logo {
  font-size: 20px;
  font-weight: 600;
  color: #409eff;
  margin: 0;
}

.nav {
  display: flex;
  gap: 24px;
}

.nav a {
  color: #606266;
  text-decoration: none;
  font-size: 14px;
  padding: 8px 12px;
  border-radius: 4px;
  transition: all 0.3s;
}

.nav a:hover,
.nav a.router-link-active {
  color: #409eff;
  background: #ecf5ff;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #606266;
  font-size: 14px;
}

.main {
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px;
}
</style>
