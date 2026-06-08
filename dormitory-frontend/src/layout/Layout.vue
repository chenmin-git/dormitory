<template>
  <el-container class="layout">
    <!-- 侧边栏 -->
    <el-aside width="232px" class="aside">
      <div class="logo">
        <div class="logo-mark">
          <Logo :size="26" />
        </div>
        <div class="logo-text">
          <div class="logo-name">智能宿舍</div>
          <div class="logo-en">DORMITORY</div>
        </div>
      </div>
      <el-menu :default-active="activeMenu" router class="side-menu"
               background-color="transparent" text-color="var(--sidebar-text)"
               active-text-color="#ffffff">
        <el-menu-item v-for="item in menus" :key="item.path" :index="'/' + item.path">
          <el-icon><component :is="item.meta.icon" /></el-icon>
          <span>{{ item.meta.title }}</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <!-- 顶栏 -->
      <el-header class="header">
        <div class="header-title">{{ currentTitle }}</div>
        <div class="header-right">
          <el-tag :type="roleTagType" effect="dark">{{ roleLabel }}</el-tag>
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              {{ userStore.realName }}
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 主内容 -->
      <el-main class="main">
        <router-view v-slot="{ Component }">
          <keep-alive>
            <component :is="Component" />
          </keep-alive>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/user'
import Logo from '@/components/Logo.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 当前角色可见的菜单
const menus = computed(() => {
  const children = router.options.routes.find((r) => r.path === '/').children
  return children.filter(
    (c) => !c.meta.hideInMenu && c.meta.roles && c.meta.roles.includes(userStore.role)
  )
})

const activeMenu = computed(() => route.path)
const currentTitle = computed(() => route.meta.title || '')

const roleLabel = computed(() => {
  return { ADMIN: '超级管理员', MANAGER: '宿管员', STUDENT: '学生' }[userStore.role] || ''
})
const roleTagType = computed(() => {
  return { ADMIN: 'danger', MANAGER: 'warning', STUDENT: 'success' }[userStore.role] || 'info'
})

const handleCommand = (cmd) => {
  if (cmd === 'profile') {
    router.push('/profile')
  } else if (cmd === 'logout') {
    ElMessageBox.confirm('确定退出登录吗？', '提示', { type: 'warning' }).then(() => {
      userStore.logout()
      router.push('/login')
    })
  }
}
</script>

<style scoped>
.layout {
  height: 100vh;
}
.aside {
  background: var(--sidebar);
  overflow-x: hidden;
  border-right: 1px solid rgba(255, 255, 255, 0.04);
}
.logo {
  height: 72px;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 22px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}
.logo-mark {
  width: 40px;
  height: 40px;
  border-radius: 11px;
  background: var(--primary);
  color: #fff;
  font-size: 22px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  flex: none;
}
.logo-name {
  color: #f1f4f1;
  font-size: 17px;
  font-weight: 600;
  letter-spacing: 1px;
}
.logo-en {
  color: var(--sidebar-muted);
  font-size: 11px;
  letter-spacing: 3px;
}
.side-menu {
  border-right: none;
  padding: 14px 12px;
}
.side-menu :deep(.el-menu-item) {
  height: 46px;
  border-radius: 10px;
  margin-bottom: 4px;
  color: var(--sidebar-text);
}
.side-menu :deep(.el-menu-item:hover) {
  background: var(--sidebar-soft);
  color: #fff;
}
.side-menu :deep(.el-menu-item.is-active) {
  background: var(--primary);
  color: #fff;
  font-weight: 500;
  box-shadow: 0 6px 16px rgba(21, 103, 96, 0.4);
}
.header {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: var(--surface);
  border-bottom: 1px solid var(--line);
  padding: 0 24px;
}
.header-title {
  font-size: 19px;
  font-weight: 600;
  color: var(--ink);
}
.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}
.user-info {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
  color: var(--ink);
  font-weight: 500;
}
.main {
  background: var(--bg);
  padding: 22px;
}
</style>
