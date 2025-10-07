import { ref, computed  } from 'vue'

// 主题状态
const isDark = ref(false)
const theme = ref('dark') // 默认暗色主题

// 可用主题
const themes = {
  dark: {
    name: '暗色主题',
    colors: {
      primary: '#3b82f6',
      secondary: '#6b7280',
      success: '#10b981',
      warning: '#f59e0b',
      danger: '#ef4444',
      info: '#06b6d4',
      background: '#0f172a',
      surface: '#1e293b',
      text: '#f8fafc',
      textSecondary: '#94a3b8'
    }
  },
  light: {
    name: '亮色主题',
    colors: {
      primary: '#3b82f6',
      secondary: '#6b7280',
      success: '#10b981',
      warning: '#f59e0b',
      danger: '#ef4444',
      info: '#06b6d4',
      background: '#ffffff',
      surface: '#f8fafc',
      text: '#1f2937',
      textSecondary: '#6b7280'
    }
  }
}

// 从本地存储加载主题设置
const loadTheme = () => {
  const savedTheme = localStorage.getItem('pool-dashboard-theme')
  if (savedTheme && themes[savedTheme]) {
    theme.value = savedTheme
    isDark.value = savedTheme === 'dark'
  } else {
    // 检测系统主题偏好
    const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches
    theme.value = prefersDark ? 'dark' : 'light'
    isDark.value = prefersDark
  }
}

// 保存主题设置
const saveTheme = () => {
  localStorage.setItem('pool-dashboard-theme', theme.value)
}

// 应用主题到DOM
const applyTheme = () => {
  const currentTheme = themes[theme.value]
  const root = document.documentElement
  
  // 设置CSS变量
  Object.entries(currentTheme.colors).forEach(([key, value]) => {
    root.style.setProperty(`--color-${key}`, value)
  })
  
  // 设置主题类名
  root.className = root.className.replace(/theme-\w+/g, '')
  root.classList.add(`theme-${theme.value}`)
  
  // 设置暗色模式类
  if (isDark.value) {
    root.classList.add('dark')
  } else {
    root.classList.remove('dark')
  }
}

// 切换主题
const toggleTheme = () => {
  theme.value = isDark.value ? 'light' : 'dark'
  isDark.value = !isDark.value
  applyTheme()
  saveTheme()
}

// 设置特定主题
const setTheme = (themeName) => {
  if (themes[themeName]) {
    theme.value = themeName
    isDark.value = themeName === 'dark'
  }
}

// 当前主题配置
const currentTheme = computed(() => themes[theme.value])

// 主题颜色
const colors = computed(() => currentTheme.value.colors)

// 监听主题变化

// 监听系统主题变化
const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)')
mediaQuery.addEventListener('change', (e) => {
  if (!localStorage.getItem('pool-dashboard-theme')) {
    theme.value = e.matches ? 'dark' : 'light'
    isDark.value = e.matches
  }
})

// 主题组合式函数
export function useTheme() {
  // 初始化主题
  if (!theme.value) {
    loadTheme()
    applyTheme()
  }

  return {
    // 状态
    isDark: computed(() => isDark.value),
    theme: computed(() => theme.value),
    currentTheme,
    colors,
    themes: Object.keys(themes),
    
    // 方法
    toggleTheme,
    setTheme,
    loadTheme,
    applyTheme
  }
}

// 响应式断点组合式函数
export function useBreakpoints() {
  const windowWidth = ref(window.innerWidth)
  
  const updateWidth = () => {
    windowWidth.value = window.innerWidth
  }
  
  window.addEventListener('resize', updateWidth)
  
  const breakpoints = {
    xs: 480,
    sm: 640,
    md: 768,
    lg: 1024,
    xl: 1280,
    '2xl': 1536
  }
  
  const current = computed(() => {
    const width = windowWidth.value
    if (width < breakpoints.xs) return 'xs'
    if (width < breakpoints.sm) return 'sm'
    if (width < breakpoints.md) return 'md'
    if (width < breakpoints.lg) return 'lg'
    if (width < breakpoints.xl) return 'xl'
    return '2xl'
  })
  
  const isXs = computed(() => windowWidth.value < breakpoints.xs)
  const isSm = computed(() => windowWidth.value >= breakpoints.xs && windowWidth.value < breakpoints.sm)
  const isMd = computed(() => windowWidth.value >= breakpoints.sm && windowWidth.value < breakpoints.md)
  const isLg = computed(() => windowWidth.value >= breakpoints.md && windowWidth.value < breakpoints.lg)
  const isXl = computed(() => windowWidth.value >= breakpoints.lg && windowWidth.value < breakpoints.xl)
  const is2xl = computed(() => windowWidth.value >= breakpoints.xl)
  
  const isMobile = computed(() => windowWidth.value < breakpoints.md)
  const isTablet = computed(() => windowWidth.value >= breakpoints.md && windowWidth.value < breakpoints.lg)
  const isDesktop = computed(() => windowWidth.value >= breakpoints.lg)
  
  return {
    windowWidth: computed(() => windowWidth.value),
    current,
    breakpoints,
    
    // 具体断点
    isXs,
    isSm,
    isMd,
    isLg,
    isXl,
    is2xl,
    
    // 设备类型
    isMobile,
    isTablet,
    isDesktop
  }
}

// 暗色模式工具函数
export function useDarkMode() {
  const { isDark, toggleTheme } = useTheme()
  
  return {
    isDark,
    toggle: toggleTheme
  }
}