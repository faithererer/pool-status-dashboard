<template>
  <div class="not-found-container">
    <div class="not-found-content">
      <div class="error-code">404</div>
      <div class="error-message">页面未找到</div>
      <div class="error-description">
        抱歉，您访问的页面不存在或已被移除。
      </div>
      <div class="error-actions">
        <BaseButton 
          type="primary" 
          @click="goHome"
          class="home-btn"
        >
          返回首页
        </BaseButton>
        <BaseButton 
          type="secondary" 
          @click="goBack"
          class="back-btn"
        >
          返回上页
        </BaseButton>
      </div>
    </div>
    
    <!-- 装饰性背景 -->
    <div class="background-decoration">
      <div class="floating-shape shape-1"></div>
      <div class="floating-shape shape-2"></div>
      <div class="floating-shape shape-3"></div>
    </div>
  </div>
</template>

<script>
import { defineComponent } from 'vue'
import { useRouter } from 'vue-router'
import BaseButton from '@/components/common/BaseButton.vue'

export default defineComponent({
  name: 'NotFoundView',
  components: {
    BaseButton
  },
  setup() {
    const router = useRouter()
    
    const goHome = () => {
      router.push('/')
    }
    
    const goBack = () => {
      if (window.history.length > 1) {
        router.go(-1)
      } else {
        router.push('/')
      }
    }
    
    return {
      goHome,
      goBack
    }
  }
})
</script>

<style lang="scss" scoped>
.not-found-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #0f172a 0%, #1e293b 50%, #334155 100%);
  color: #f8fafc;
  position: relative;
  overflow: hidden;
}

.not-found-content {
  text-align: center;
  z-index: 2;
  position: relative;
  max-width: 600px;
  padding: 2rem;
}

.error-code {
  font-size: 8rem;
  font-weight: 700;
  background: linear-gradient(45deg, #06b6d4, #3b82f6, #8b5cf6);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 1rem;
  line-height: 1;
  animation: glow 2s ease-in-out infinite alternate;
}

@keyframes glow {
  from { 
    filter: drop-shadow(0 0 10px rgba(59, 130, 246, 0.5)); 
  }
  to { 
    filter: drop-shadow(0 0 30px rgba(59, 130, 246, 0.8)); 
  }
}

.error-message {
  font-size: 2.5rem;
  font-weight: 600;
  margin-bottom: 1rem;
  color: #f1f5f9;
}

.error-description {
  font-size: 1.2rem;
  color: #94a3b8;
  margin-bottom: 3rem;
  line-height: 1.6;
}

.error-actions {
  display: flex;
  gap: 1rem;
  justify-content: center;
  flex-wrap: wrap;
}

.home-btn,
.back-btn {
  min-width: 120px;
}

// 背景装饰
.background-decoration {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 1;
  overflow: hidden;
}

.floating-shape {
  position: absolute;
  border-radius: 50%;
  background: rgba(59, 130, 246, 0.1);
  animation: float 6s ease-in-out infinite;
}

.shape-1 {
  width: 200px;
  height: 200px;
  top: 10%;
  left: 10%;
  animation-delay: 0s;
}

.shape-2 {
  width: 150px;
  height: 150px;
  top: 60%;
  right: 15%;
  animation-delay: 2s;
}

.shape-3 {
  width: 100px;
  height: 100px;
  bottom: 20%;
  left: 20%;
  animation-delay: 4s;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0px) rotate(0deg);
    opacity: 0.3;
  }
  50% {
    transform: translateY(-20px) rotate(180deg);
    opacity: 0.6;
  }
}

// 响应式设计
@media (max-width: 768px) {
  .not-found-content {
    padding: 1rem;
  }
  
  .error-code {
    font-size: 6rem;
  }
  
  .error-message {
    font-size: 2rem;
  }
  
  .error-description {
    font-size: 1rem;
    margin-bottom: 2rem;
  }
  
  .error-actions {
    flex-direction: column;
    align-items: center;
  }
  
  .home-btn,
  .back-btn {
    width: 100%;
    max-width: 200px;
  }
  
  .floating-shape {
    display: none; // 在移动端隐藏装饰元素
  }
}

@media (max-width: 480px) {
  .error-code {
    font-size: 4rem;
  }
  
  .error-message {
    font-size: 1.5rem;
  }
}
</style>