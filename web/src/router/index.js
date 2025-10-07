import { createRouter, createWebHistory } from 'vue-router'

// 路由组件懒加载
const DashboardView = () => import('@/views/dashboard/DashboardView.vue')
const ManagementView = () => import('@/views/management/ManagementView.vue')
const LoginView = () => import('@/views/auth/LoginView.vue')
const NotFoundView = () => import('@/views/error/NotFoundView.vue')

const routes = [
  {
    path: '/',
    redirect: '/dashboard'
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: DashboardView,
    meta: {
      title: '监控面板',
      requiresAuth: false
    }
  },
  {
    path: '/management',
    name: 'Management',
    component: ManagementView,
    meta: {
      title: '管理面板',
      requiresAuth: true
    }
  },
  {
    path: '/login',
    name: 'Login',
    component: LoginView,
    meta: {
      title: '登录',
      requiresAuth: false,
      hideLayout: true
    }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: NotFoundView,
    meta: {
      title: '页面未找到',
      hideLayout: true
    }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    } else {
      return { top: 0 }
    }
  }
})

// 路由守卫
router.beforeEach(async (to, from, next) => {
  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - 号池监控面板` : '号池监控面板'
  
  // 检查是否需要认证
  if (to.meta.requiresAuth) {
    try {
      // 动态导入认证组合式函数以避免循环依赖
      const { useAuth } = await import('@/composables/useAuth')
      const { isAuthenticated, checkAuth } = useAuth()
      
      // 检查认证状态
      const isAuthValid = await checkAuth()
      
      // 如果认证无效，重定向到登录页
      if (!isAuthValid) {
        next('/login')
        return
      }
    } catch (error) {
      console.error('认证检查失败:', error)
      next('/login')
      return
    }
  }
  
  // 处理已登录用户访问登录页面
  if (to.name === 'Login') {
    try {
      const { useAuth } = await import('@/composables/useAuth')
      const { isAuthenticated, checkAuth } = useAuth()
      
      await checkAuth()
      
      if (isAuthenticated.value) {
        next('/dashboard')
        return
      }
    } catch (error) {
      // 如果检查失败，继续到登录页
      console.error('认证状态检查失败:', error)
    }
  }
  
  next()
})

export default router