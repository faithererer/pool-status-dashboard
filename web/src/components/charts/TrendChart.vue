<template>
  <div class="trend-chart">
    <div class="chart-header">
      <h3 class="chart-title">{{ title }}</h3>
      <div class="time-selector">
        <button
          v-for="option in timeOptions"
          :key="option.value"
          class="time-btn"
          :class="{ active: selectedTimeRange === option.value }"
          @click="handleTimeRangeChange(option.value)"
        >
          {{ option.label }}
        </button>
      </div>
    </div>
    
    <div class="chart-wrapper" ref="chartContainer">
      <canvas v-if="!loading && !error && data" ref="chartCanvas"></canvas>
      
      <!-- 加载状态 -->
      <div v-if="loading" class="chart-loading">
        <div class="loading-spinner"></div>
        <p>加载中...</p>
      </div>
      
      <!-- 错误状态 -->
      <div v-if="error" class="chart-error">
        <p>{{ error }}</p>
        <button @click="$emit('retry')" class="retry-btn">重试</button>
      </div>
      
      <!-- 无数据状态 -->
      <div v-if="!loading && !error && !data" class="chart-no-data">
        <p>暂无数据</p>
      </div>
    </div>
  </div>
</template>

<script>
import { defineComponent, ref, onMounted, onUnmounted, watch, nextTick, computed } from 'vue'
import {
  Chart,
  CategoryScale,
  LinearScale,
  PointElement,
  LineController,
  LineElement,
  Title,
  Tooltip,
  Legend,
  Filler,
  TimeScale
} from 'chart.js'
import { format } from 'date-fns'
import 'chartjs-adapter-date-fns';

// 注册 Chart.js 组件
Chart.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineController,
  LineElement,
  Title,
  Tooltip,
  Legend,
  Filler,
  TimeScale
)

export default defineComponent({
  name: 'TrendChart',
  props: {
    title: {
      type: String,
      default: '历史趋势分析'
    },
    data: {
      type: Object,
      default: null
    },
    loading: {
      type: Boolean,
      default: false
    },
    error: {
      type: String,
      default: null
    },
    height: {
      type: Number,
      default: 400
    }
  },
  emits: ['time-range-change', 'retry', 'error'],
  setup(props, { emit }) {
    const chartCanvas = ref(null)
    const chartContainer = ref(null)
    let chartInstance = null
    const selectedTimeRange = ref('1h')

    
    // 时间范围选项
    const timeOptions = [
      { value: '1h', label: '1小时' },
      { value: '24h', label: '24小时' },
      { value: '7d', label: '7天' },
      { value: '30d', label: '30天' }
    ]
    
    // Chart.js 配置
    const getChartOptions = () => ({
      responsive: true,
      maintainAspectRatio: false,
      interaction: {
        mode: 'index',
        intersect: false,
      },
      plugins: {
        legend: {
          position: 'top',
          labels: {
            color: 'rgb(148, 163, 184)', // $gray-400
            usePointStyle: true,
            padding: 20,
            font: {
              size: 12,
              weight: '500'
            }
          }
        },
        tooltip: {
          backgroundColor: 'rgba(15, 23, 42, 0.95)', // $slate-900
          titleColor: 'rgb(248, 250, 252)', // $slate-50
          bodyColor: 'rgb(248, 250, 252)', // $slate-50
          borderColor: 'rgba(148, 163, 184, 0.2)', // $border-color
          borderWidth: 1,
          cornerRadius: 8,
          displayColors: true,
          callbacks: {
            title: (context) => {
              const timestamp = context[0].parsed.x
              return `时间: ${format(new Date(timestamp), 'yyyy-MM-dd HH:mm:ss')}`
            },
            label: (context) => {
              const label = context.dataset.label || ''
              const value = context.parsed.y
              
              if (label.includes('压力')) {
                return `${label}: ${value.toFixed(2)}%`
              } else {
                return `${label}: ${value.toLocaleString()}`
              }
            }
          }
        }
      },
      scales: {
        x: {
          type: 'time',
          time: {
            unit: 'hour',
            tooltipFormat: 'yyyy-MM-dd HH:mm:ss',
            displayFormats: {
              hour: 'HH:mm',
              day: 'MM-dd',
            }
          },
          display: true,
          title: {
            display: true,
            text: '时间',
            color: 'rgb(148, 163, 184)', // $gray-400
            font: {
              size: 12,
              weight: '500'
            }
          },
          ticks: {
            color: 'rgb(148, 163, 184)', // $gray-400
            font: {
              size: 11
            },
            source: 'auto',
            maxRotation: 0,
            autoSkip: true,
          },
          grid: {
            color: 'rgba(148, 163, 184, 0.1)', // $border-color
            drawBorder: false
          }
        },
        y: {
          type: 'linear',
          display: true,
          position: 'left',
          title: {
            display: true,
            text: '数量',
            color: 'rgb(148, 163, 184)', // $gray-400
            font: {
              size: 12,
              weight: '500'
            }
          },
          ticks: {
            color: 'rgb(148, 163, 184)', // $gray-400
            font: {
              size: 11
            },
            callback: function(value) {
              if (value >= 10000) {
                return (value / 10000).toFixed(1) + '万'
              } else if (value >= 1000) {
                return (value / 1000).toFixed(1) + 'k'
              }
              return value.toLocaleString()
            }
          },
          grid: {
            color: 'rgba(148, 163, 184, 0.1)', // $border-color
            drawBorder: false
          }
        },
        y1: {
          type: 'linear',
          display: true,
          position: 'right',
          min: 0,
          max: 100,
          title: {
            display: true,
            text: '压力(%)',
            color: 'rgb(148, 163, 184)', // $gray-400
            font: {
              size: 12,
              weight: '500'
            }
          },
          ticks: {
            color: 'rgb(148, 163, 184)', // $gray-400
            font: {
              size: 11
            },
            callback: function(value) {
              return value + '%'
            }
          },
          grid: {
            drawOnChartArea: false,
            color: 'rgba(148, 163, 184, 0.1)', // $border-color
            drawBorder: false
          }
        }
      },
      elements: {
        line: {
          tension: 0.4
        },
        point: {
          radius: 2,
          hoverRadius: 5,
          borderWidth: 1,
          hoverBorderWidth: 2
        }
      }
    })
    
    // 创建图表
    const downsample = (data, maxPoints) => {
      if (data.length <= maxPoints) {
        return data
      }

      const sampledData = []
      const step = (data.length - 1) / (maxPoints - 1)

      for (let i = 0; i < maxPoints; i++) {
        const index = Math.round(i * step)
        sampledData.push(data[index])
      }
      return sampledData
    }

    const createChart = () => {
      if (!chartCanvas.value || !props.data) return
      
      try {
        const ctx = chartCanvas.value.getContext('2d')
        
        if (chartInstance) {
          chartInstance.destroy()
        }
        
        const processedData = JSON.parse(JSON.stringify(props.data))
        processedData.datasets.forEach(dataset => {
          dataset.data = downsample(dataset.data, 500) // 抽样到500个点
        })

        chartInstance = new Chart(ctx, {
          type: 'line',
          data: processedData,
          options: getChartOptions()
        })
      } catch (error) {
        console.error('创建图表失败:', error)
        emit('error', '图表创建失败')
      }
    }
    
    // 更新图表数据
    const updateChart = () => {
      if (chartInstance && props.data) {
        chartInstance.data = props.data
        // 使用 'none' 动画模式以提高性能并避免更新冲突
        chartInstance.update('none')
      }
    }
    
    // 处理时间范围变化
    const handleTimeRangeChange = (timeRange) => {
      selectedTimeRange.value = timeRange
      emit('time-range-change', timeRange)
    }
    
    // 响应式处理
    const handleResize = () => {
      if (chartInstance) {
        chartInstance.resize()
      }
    }
    
    // 监听数据变化
    watch(() => props.data, (newData) => {
      // 如果没有新数据，则销毁现有图表实例并清理
      if (!newData) {
        if (chartInstance) {
          chartInstance.destroy()
          chartInstance = null
        }
        return
      }
      
      // 如果有新数据，则等待 DOM 更新（因为 canvas 是 v-if 的）。
      // 然后总是重新创建图表。这比尝试更新更可靠，
      // 特别是当 canvas 元素本身被销毁和重建时。
      nextTick(() => {
        createChart()
      })
    }, { deep: true })
    
    onMounted(() => {
      window.addEventListener('resize', handleResize)
    })
    
    onUnmounted(() => {
      if (chartInstance) {
        chartInstance.destroy()
      }
      window.removeEventListener('resize', handleResize)
    })
    
    return {
      chartCanvas,
      chartContainer,
      selectedTimeRange,
      timeOptions,
      handleTimeRangeChange
    }
  }
})
</script>

<style lang="scss" scoped>
.trend-chart {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
  flex-shrink: 0;
}

.chart-title {
  font-size: 1.125rem;
  font-weight: 600;
  color: $gray-100;
  margin: 0;
}

.time-selector {
  display: flex;
  gap: 0.5rem;
}

.time-btn {
  padding: 0.5rem 0.75rem;
  background: $bg-card;
  border: 1px solid $border-color;
  border-radius: 0.5rem;
  color: $gray-400;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 0.875rem;
  font-weight: 500;
  
  &:hover {
    background: $bg-card-hover;
    border-color: $border-hover;
    color: $primary-light;
  }
  
  &.active {
    background: $bg-card-hover;
    border-color: $border-active;
    color: $primary-light;
  }
}

.chart-wrapper {
  flex: 1;
  min-height: 0;
  position: relative;
  
  canvas {
    max-height: 100%;
  }
}

.chart-loading {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  flex: 1;
  min-height: 200px;
  color: $gray-400;
  
  .loading-spinner {
    width: 40px;
    height: 40px;
    border: 3px solid $border-color;
    border-top: 3px solid $primary-color;
    border-radius: 50%;
    animation: spin 1s linear infinite;
    margin-bottom: 0.75rem;
  }
  
  p {
    margin: 0;
    font-size: 0.875rem;
  }
}

.chart-error {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  flex: 1;
  min-height: 200px;
  color: $danger-color;
  text-align: center;
  
  p {
    margin: 0 0 1rem 0;
    font-size: 0.875rem;
  }
  
  .retry-btn {
    padding: 0.5rem 1rem;
    background: $danger-color;
    border: none;
    border-radius: 0.5rem;
    color: $white;
    cursor: pointer;
    font-size: 0.875rem;
    font-weight: 500;
    transition: background-color 0.3s ease;
    
    &:hover {
      background: $red-600;
    }
  }
}

.chart-no-data {
  display: flex;
  justify-content: center;
  align-items: center;
  flex: 1;
  min-height: 200px;
  color: $gray-400;
  
  p {
    margin: 0;
    font-size: 0.875rem;
  }
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

// 响应式设计
@media (max-width: 768px) {
  .chart-header {
    flex-direction: column;
    gap: 0.75rem;
    align-items: stretch;
  }
  
  .time-selector {
    justify-content: center;
  }
  
  .time-btn {
    flex: 1;
    text-align: center;
  }
}

@media (max-width: 640px) {
  .time-selector {
    flex-wrap: wrap;
    gap: 0.25rem;
  }
  
  .time-btn {
    flex: 1;
    min-width: calc(50% - 0.125rem);
    padding: 0.25rem 0.5rem;
    font-size: 0.75rem;
  }
}
</style>