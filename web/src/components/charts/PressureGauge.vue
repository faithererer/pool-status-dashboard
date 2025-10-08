<template>
  <div class="pressure-gauge">
    <div 
      class="gauge-wrapper"
      :style="{ width: size + 'px', height: (size / 2) + 'px' }"
    >
      <!-- SVG 仪表盘背景 -->
      <svg class="gauge-svg" viewBox="0 0 400 200">
        <defs>
          <linearGradient id="gaugeGradient" x1="0%" y1="0%" x2="100%" y2="0%">
            <stop offset="0%" style="stop-color:#10b981;" />
            <stop offset="25%" style="stop-color:#34d399;" />
            <stop offset="50%" style="stop-color:#fbbf24;" />
            <stop offset="75%" style="stop-color:#fb923c;" />
            <stop offset="100%" style="stop-color:#ef4444;" />
          </linearGradient>

          <filter id="glow">
            <feGaussianBlur stdDeviation="4" result="coloredBlur"/>
            <feMerge>
              <feMergeNode in="coloredBlur"/>
              <feMergeNode in="SourceGraphic"/>
            </feMerge>
          </filter>

          <mask id="gaugeMask">
            <path
                d="M 60 175 A 140 140 0 0 1 340 175"
                fill="none"
                stroke="white" stroke-width="50"
                stroke-linecap="butt"
            />
          </mask>
        </defs>

        <rect
            x="0"
            y="0"
            width="400"
            height="200"
            fill="url(#gaugeGradient)"
            mask="url(#gaugeMask)"
            filter="url(#glow)"
        />
      </svg>
      
      <!-- 指针 -->
      <div
        class="needle-pivot"
        :style="{
          bottom: pivotBottom + 'px',
          left: '50%',
          transform: 'translateX(-50%)'
        }"
      >
        <div
          class="needle"
          :style="{
            height: needleLength + 'px',
            transform: `translateX(-1px) rotate(${needleRotation}deg)`,
            width: needleWidth + 'px'
          }"
        />
      </div>
      
      <!-- 百分比文本现在是flex项目，不再绝对定位 -->
    </div>
    <!-- 百分比文本移到这里，作为flex项目的兄弟元素 -->
    <div
      class="pressure-text"
      :style="{
        fontSize: textFontSize + 'px',
        color: pressureColor
      }"
    >
      {{ formattedPressure }}
    </div>
  </div>
</template>

<script>
import { defineComponent, computed } from 'vue'

export default defineComponent({
  name: 'PressureGauge',
  props: {
    pressure: {
      type: Number,
      default: 0,
      validator: (value) => value >= 0 && value <= 100
    },
    size: {
      type: Number,
      default: 140 // 减小默认尺寸以适应新布局
    },
    animated: {
      type: Boolean,
      default: true
    }
  },
  setup(props) {
    // 计算相关尺寸
    const radius = computed(() => props.size * 0.35)
    const strokeWidth = computed(() => props.size * 0.12)
    const centerX = computed(() => props.size / 2)
    const centerY = computed(() => props.size / 2)
    const needleLength = computed(() => radius.value * 0.8)
    const needleWidth = computed(() => Math.max(2, props.size * 0.007))
    const pivotBottom = computed(() => props.size * 0.06)
    const fontSize = computed(() => Math.max(10, props.size * 0.04))
    const textFontSize = computed(() => Math.max(16, props.size * 0.15))
    
    // 计算弧形路径
    const arcPath = computed(() => {
      const startAngle = Math.PI // 180度
      const endAngle = 0 // 0度
      const outerRadius = radius.value
      
      const startX = centerX.value + outerRadius * Math.cos(startAngle)
      const startY = centerY.value + outerRadius * Math.sin(startAngle)
      const endX = centerX.value + outerRadius * Math.cos(endAngle)
      const endY = centerY.value + outerRadius * Math.sin(endAngle)
      
      return `M ${startX} ${startY} A ${outerRadius} ${outerRadius} 0 0 1 ${endX} ${endY}`
    })
    
    // 计算指针旋转角度
    const needleRotation = computed(() => {
      const clampedValue = Math.max(0, Math.min(100, props.pressure))
      return -90 + (clampedValue * 1.8) // -90度到90度的范围
    })

    // 格式化显示的百分比
    const formattedPressure = computed(() => {
      return `${Math.round(props.pressure)}%`
    })

    // 根据压力值计算颜色
    const pressureColor = computed(() => {
      const p = props.pressure
      if (p >= 80) return 'var(--color-danger)'
      if (p >= 60) return 'var(--color-warning)'
      if (p >= 40) return 'var(--color-info)'
      return 'var(--color-success)'
    })
    
    // 计算刻度线
    const ticks = computed(() => {
      const ticksArray = []
      const totalTicks = 21 // 0-100，每5一个主刻度，每1一个小刻度
      
      for (let i = 0; i <= totalTicks; i++) {
        const value = i * 5
        const isMajor = i % 2 === 0 // 每10一个主刻度
        const angle = Math.PI - (i / totalTicks) * Math.PI // 从180度到0度
        
        const innerRadius = radius.value - strokeWidth.value / 2 - (isMajor ? 15 : 8)
        const outerRadius = radius.value - strokeWidth.value / 2 - 3
        const textRadius = radius.value - strokeWidth.value / 2 - 25
        
        const x1 = centerX.value + innerRadius * Math.cos(angle)
        const y1 = centerY.value + innerRadius * Math.sin(angle)
        const x2 = centerX.value + outerRadius * Math.cos(angle)
        const y2 = centerY.value + outerRadius * Math.sin(angle)
        const textX = centerX.value + textRadius * Math.cos(angle)
        const textY = centerY.value + textRadius * Math.sin(angle) + fontSize.value / 3
        
        ticksArray.push({
          value,
          isMajor,
          x1,
          y1,
          x2,
          y2,
          textX,
          textY
        })
      }
      
      return ticksArray
    })
    
    return {
      arcPath,
      needleRotation,
      needleLength,
      needleWidth,
      pivotBottom,
      strokeWidth,
      fontSize,
      ticks,
      formattedPressure,
      textFontSize,
      pressureColor
    }
  }
})
</script>

<style lang="scss" scoped>
.pressure-gauge {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 1rem; /* 在仪表和文本之间添加间距 */
}

.gauge-wrapper {
  position: relative;
  overflow: visible;
  display: flex;
  justify-content: center;
  align-items: center;
}

.pressure-text {
  font-weight: 700; /* $font-weight-bold */
  line-height: 1;
  z-index: 10;
  text-shadow: 0 1px 2px rgba(0,0,0,0.3);
  font-size: 2.5rem; /* 增大字体以匹配新的布局 */
}

.gauge-svg {
  position: absolute;
  top: 0;
  left: 0;
  overflow: visible;
}

.needle-pivot {
  position: absolute;
  width: 0;
  height: 0;
  z-index: 5;
}

.needle {
  position: absolute;
  bottom: 0;
  left: 0;
  background: $gray-50;
  transform-origin: bottom center;
  border-radius: 2px 2px 0 0;
  box-shadow: 0 0 10px rgba(255, 255, 255, 0.6);
  transition: transform 1.5s cubic-bezier(0.4, 0.0, 0.2, 1);
  
  &::after {
    content: '';
    position: absolute;
    bottom: -4px; /* 调整枢轴位置 */
    left: 50%;
    transform: translateX(-50%);
    width: 8px; /* 减小枢轴尺寸 */
    height: 8px; /* 减小枢轴尺寸 */
    background: $gray-400;
    border-radius: 50%;
    box-shadow:
      0 0 5px rgba(255, 255, 255, 0.5),
      inset 0 1px 2px rgba(0, 0, 0, 0.3);
  }
}

// 响应式调整
@media (max-width: 768px) {
  .needle::after {
    width: 20px;
    height: 20px;
    bottom: -10px;
  }
}

@media (max-width: 640px) {
  .needle::after {
    width: 15px;
    height: 15px;
    bottom: -8px;
  }
}
</style>