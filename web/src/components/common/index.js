// 通用组件入口文件
import BaseButton from './BaseButton.vue'
import BaseBadge from './BaseBadge.vue'
import BaseModal from './BaseModal.vue'
import BaseLoading from './BaseLoading.vue'
import BaseToast from './BaseToast.vue'

// 组件列表
const components = [
  BaseButton,
  BaseBadge,
  BaseModal,
  BaseLoading,
  BaseToast
]

// 安装函数
const install = (app) => {
  components.forEach(component => {
    app.component(component.name, component)
  })
}

// 导出单个组件
export {
  BaseButton,
  BaseBadge,
  BaseModal,
  BaseLoading,
  BaseToast
}

// 导出安装函数
export default {
  install
}