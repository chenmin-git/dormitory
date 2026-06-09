import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/user'

// 角色常量
const ADMIN = 'ADMIN'
const MANAGER = 'MANAGER'
const STUDENT = 'STUDENT'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/Login.vue'),
    meta: { public: true }
  },
  {
    path: '/',
    component: () => import('@/layout/Layout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/Dashboard.vue'),
        meta: { title: '数据看板', icon: 'DataLine', roles: [ADMIN, MANAGER] }
      },
      {
        path: 'ai-chat',
        name: 'AiChat',
        component: () => import('@/views/ai/AiChat.vue'),
        meta: { title: '智能问答', icon: 'MagicStick', roles: [ADMIN, MANAGER, STUDENT] }
      },
      {
        path: 'user',
        name: 'User',
        component: () => import('@/views/system/User.vue'),
        meta: { title: '用户管理', icon: 'User', roles: [ADMIN] }
      },
      {
        path: 'building',
        name: 'Building',
        component: () => import('@/views/dorm/Building.vue'),
        meta: { title: '楼栋管理', icon: 'OfficeBuilding', roles: [ADMIN, MANAGER] }
      },
      {
        path: 'room',
        name: 'Room',
        component: () => import('@/views/dorm/Room.vue'),
        meta: { title: '房间床位', icon: 'House', roles: [ADMIN, MANAGER] }
      },
      {
        path: 'student',
        name: 'Student',
        component: () => import('@/views/student/Student.vue'),
        meta: { title: '学生管理', icon: 'Avatar', roles: [ADMIN, MANAGER] }
      },
      {
        path: 'repair',
        name: 'Repair',
        component: () => import('@/views/repair/Repair.vue'),
        meta: { title: '报修管理', icon: 'Tools', roles: [ADMIN, MANAGER, STUDENT] }
      },
      {
        path: 'leave',
        name: 'Leave',
        component: () => import('@/views/leave/Leave.vue'),
        meta: { title: '请假/晚归', icon: 'Calendar', roles: [ADMIN, MANAGER, STUDENT] }
      },
      {
        path: 'announcement',
        name: 'Announcement',
        component: () => import('@/views/announcement/Announcement.vue'),
        meta: { title: '公告通知', icon: 'Bell', roles: [ADMIN, MANAGER, STUDENT] }
      },
      {
        path: 'hygiene',
        name: 'Hygiene',
        component: () => import('@/views/hygiene/Hygiene.vue'),
        meta: { title: '卫生检查', icon: 'Star', roles: [ADMIN, MANAGER, STUDENT] }
      },
      {
        path: 'faq',
        name: 'Faq',
        component: () => import('@/views/faq/Faq.vue'),
        meta: { title: '宿舍指南', icon: 'ChatLineSquare', roles: [ADMIN, MANAGER, STUDENT] }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/profile/Profile.vue'),
        meta: { title: '个人中心', icon: 'Setting', roles: [ADMIN, MANAGER, STUDENT], hideInMenu: true }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 找到当前角色可访问的第一个页面（用于默认落地，避免重定向死循环）
export function firstAccessiblePath(role) {
  const children = routes.find((r) => r.path === '/').children
  const target = children.find(
    (c) => !c.meta.hideInMenu && c.meta.roles && c.meta.roles.includes(role)
  )
  return target ? '/' + target.path : '/profile'
}

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  if (to.meta.public) {
    next()
    return
  }
  if (!userStore.token) {
    next('/login')
    return
  }
  // 根路径或角色无权访问 → 跳转到该角色可访问的首页
  if (to.path === '/' || (to.meta.roles && !to.meta.roles.includes(userStore.role))) {
    const home = firstAccessiblePath(userStore.role)
    if (home !== to.path) {
      next(home)
      return
    }
  }
  next()
})

export default router
