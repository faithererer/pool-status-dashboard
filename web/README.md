# Pool Status Dashboard - 前端应用

基于Vue3 + Vite构建的号池监控面板前端应用，提供实时监控和管理功能。

## 🚀 技术栈

- **Vue 3** - 渐进式JavaScript框架
- **Vite** - 下一代前端构建工具
- **Pinia** - Vue状态管理库
- **Vue Router 4** - Vue官方路由管理器
- **Axios** - HTTP客户端
- **Chart.js** - 图表库
- **SCSS** - CSS预处理器

## 📦 项目结构

```
web/
├── public/                 # 静态资源
├── src/
│   ├── components/         # 组件
│   │   ├── common/        # 通用组件
│   │   ├── dashboard/     # 仪表板组件
│   │   ├── management/    # 管理组件
│   │   ├── layout/        # 布局组件
│   │   └── charts/        # 图表组件
│   ├── views/             # 页面视图
│   │   ├── dashboard/     # 仪表板页面
│   │   ├── management/    # 管理页面
│   │   └── auth/          # 认证页面
│   ├── stores/            # Pinia状态管理
│   ├── composables/       # 组合式函数
│   ├── services/          # API服务
│   ├── router/            # 路由配置
│   ├── styles/            # 样式文件
│   └── main.js           # 应用入口
├── .env                   # 环境变量
├── .env.development       # 开发环境变量
├── .env.production        # 生产环境变量
├── package.json           # 项目配置
├── vite.config.js         # Vite配置
└── README.md             # 项目说明
```

## 🛠️ 开发环境要求

- Node.js >= 16.0.0
- npm >= 8.0.0 或 yarn >= 1.22.0

## 📋 安装和运行

### 1. 安装依赖

```bash
cd web
npm install
```

### 2. 开发环境运行

```bash
npm run dev
```

应用将在 `http://localhost:5173` 启动

### 3. 生产环境构建

```bash
npm run build
```

构建文件将输出到 `dist/` 目录

### 4. 预览生产构建

```bash
npm run preview
```

## 🔧 环境配置

### 环境变量

项目使用三个环境配置文件：

- `.env` - 所有环境的默认配置
- `.env.development` - 开发环境配置
- `.env.production` - 生产环境配置

主要配置项：

```bash
# API基础URL
VITE_API_BASE_URL=http://localhost:8080/api

# 应用标题
VITE_APP_TITLE=Pool Status Dashboard

# WebSocket URL (如果需要)
VITE_WS_URL=ws://localhost:8080/ws
```

## 🎯 主要功能

### 1. 仪表板监控
- 实时号池状态监控
- 压力仪表盘显示
- 历史趋势图表
- 总览统计信息

### 2. 号池管理
- 号池列表查看
- 添加/编辑/删除号池
- 搜索和过滤功能
- 批量操作支持

### 3. 用户认证
- 登录/登出功能
- JWT令牌管理
- 路由权限控制
- 自动令牌刷新

### 4. 主题系统
- 深色/浅色主题切换
- 主题持久化存储
- 响应式设计适配

## 🧩 核心组件

### 通用组件
- `BaseButton` - 按钮组件
- `BaseModal` - 模态框组件
- `BaseLoading` - 加载组件
- `BaseBadge` - 徽章组件
- `BaseToast` - 通知组件

### 图表组件
- `PressureGauge` - 压力仪表盘
- `TrendChart` - 趋势图表

### 布局组件
- `AppLayout` - 应用布局
- `AppHeader` - 应用头部

## 🔄 状态管理

使用Pinia进行状态管理，主要Store包括：

- `dashboard` - 仪表板数据状态
- `pool` - 号池数据状态
- `auth` - 认证状态管理

## 🌐 API集成

### HTTP客户端配置
- 基于Axios的HTTP客户端
- 请求/响应拦截器
- 自动错误处理
- 令牌自动附加

### API服务
- `poolApi` - 号池相关API
- `authApi` - 认证相关API
- `dashboardApi` - 仪表板数据API

## 🎨 样式系统

### SCSS变量系统
- 颜色变量定义
- 尺寸变量定义
- 断点变量定义
- 主题变量支持

### 响应式设计
- 移动端适配
- 平板端适配
- 桌面端优化

## 🔍 开发工具

### 组合式函数 (Composables)
- `useAuth` - 认证逻辑
- `useTheme` - 主题管理
- `useError` - 错误处理
- `useToast` - 通知管理

### 工具函数
- 数据格式化
- 日期处理
- 验证函数

## 🚦 路由配置

### 路由结构
```
/                    # 重定向到仪表板
/dashboard           # 仪表板页面
/management          # 管理页面
/login               # 登录页面
```

### 路由守卫
- 认证检查
- 权限验证
- 自动重定向

## 📱 移动端适配

- 响应式布局设计
- 触摸友好的交互
- 移动端优化的组件
- 适配不同屏幕尺寸

## 🔒 安全特性

- JWT令牌认证
- 自动令牌刷新
- 路由权限控制
- XSS防护
- CSRF防护

## 🐛 调试和测试

### 开发调试
```bash
# 启用详细日志
npm run dev -- --debug

# 分析构建包大小
npm run build -- --analyze
```

### 错误处理
- 全局错误捕获
- 用户友好的错误提示
- 错误日志记录
- 网络错误重试

## 📈 性能优化

- 组件懒加载
- 路由懒加载
- 图片懒加载
- 代码分割
- 缓存策略

## 🔄 部署

### 构建优化
```bash
# 生产环境构建
npm run build

# 构建分析
npm run build:analyze
```

### 部署配置
- 静态文件服务
- 路由回退配置
- 缓存策略设置
- 环境变量配置

## 📚 开发指南

### 代码规范
- ESLint配置
- Prettier格式化
- 组件命名规范
- 文件组织规范

### 提交规范
- 使用语义化提交信息
- 代码审查流程
- 分支管理策略

## 🤝 贡献指南

1. Fork项目
2. 创建功能分支
3. 提交更改
4. 推送到分支
5. 创建Pull Request

## 📄 许可证

本项目采用MIT许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

## 📞 支持

如有问题或建议，请通过以下方式联系：

- 创建Issue
- 发送邮件
- 项目讨论区

---

**注意**: 确保后端API服务正在运行，并且环境变量配置正确。