import { createApp } from 'vue'
import { createPinia } from 'pinia'

// 全局错误处理
const handleError = (error, info) => {
  console.error('Vue应用错误:', error)
  console.error('错误信息:', info)
  
  // 显示错误信息给用户
  const errorDiv = document.createElement('div')
  errorDiv.innerHTML = `
    <div style="
      position: fixed;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: #ef4444;
      color: white;
      display: flex;
      align-items: center;
      justify-content: center;
      font-family: monospace;
      z-index: 9999;
      padding: 20px;
    ">
      <div style="text-align: center; max-width: 600px;">
        <h1>应用启动失败</h1>
        <p style="margin: 20px 0;">错误: ${error.message}</p>
        <details style="margin-top: 20px; text-align: left;">
          <summary>详细信息</summary>
          <pre style="background: rgba(0,0,0,0.2); padding: 10px; border-radius: 4px; margin-top: 10px; overflow: auto;">
${error.stack || '无堆栈信息'}
          </pre>
        </details>
        <button onclick="location.reload()" style="
          background: rgba(255,255,255,0.2);
          border: 1px solid white;
          color: white;
          padding: 10px 20px;
          border-radius: 4px;
          cursor: pointer;
          margin-top: 20px;
        ">重新加载</button>
      </div>
    </div>
  `
  document.body.appendChild(errorDiv)
}

// 尝试逐步初始化应用
try {
  console.log('开始初始化Vue应用...')
  
  // 1. 创建Pinia实例
  const pinia = createPinia()
  console.log('✓ Pinia创建成功')
  
  // 2. 动态导入组件以捕获导入错误
  Promise.all([
    import('./App.vue'),
    import('./router'),
    import('./components/common'),
    import('./styles/main.scss')
  ]).then(async ([
    { default: App },
    { default: router },
    { default: CommonComponents }
  ]) => {
    console.log('✓ 所有模块导入成功')
    
    // 3. 创建Vue应用
    const app = createApp(App)
    console.log('✓ Vue应用创建成功')
    
    // 4. 配置错误处理
    app.config.errorHandler = handleError
    
    // 5. 使用Pinia
    app.use(pinia)
    console.log('✓ Pinia注册成功')
    
    // 6. 使用路由
    app.use(router)
    console.log('✓ 路由注册成功')
    
    // 7. 使用通用组件
    app.use(CommonComponents)
    console.log('✓ 通用组件注册成功')
    
    // 8. 初始化认证store（延迟初始化以避免循环依赖）
    try {
      const { useAuthStore } = await import('./stores/auth')
      const authStore = useAuthStore()
      await authStore.initialize()
      console.log('✓ 认证store初始化成功')
    } catch (authError) {
      console.warn('认证store初始化失败，将使用默认状态:', authError)
    }
    
    // 9. 挂载应用
    app.mount('#app')
    console.log('✓ Vue应用挂载成功')
    
    // 10. 添加全局样式类
    document.documentElement.classList.add('theme-dark')
    
  }).catch(importError => {
    console.error('模块导入失败:', importError)
    handleError(importError, '模块导入阶段')
  })
  
} catch (initError) {
  console.error('应用初始化失败:', initError)
  handleError(initError, '应用初始化阶段')
}

// 全局未捕获错误处理
window.addEventListener('error', (event) => {
  console.error('全局错误:', event.error)
  handleError(event.error || new Error(event.message), '全局错误')
})

window.addEventListener('unhandledrejection', (event) => {
  console.error('未处理的Promise拒绝:', event.reason)
  handleError(new Error(event.reason), '未处理的Promise拒绝')
})