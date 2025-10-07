package com.zjc.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 虚拟聚合池数据传输对象
 * 
 * @author zjc
 * @since 2024-01-06
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class VirtualPoolDTO {
    
    /**
     * 虚拟池ID
     */
    private Long id;
    
    /**
     * 虚拟池名称
     */
    private String name;
    
    /**
     * 虚拟池描述
     */
    private String description;
    
    /**
     * 聚合的号池ID列表
     */
    private List<Long> poolIds;
    
    /**
     * 聚合的号池名称列表
     */
    private List<String> poolNames;
    
    /**
     * 聚合策略
     */
    private String aggregateStrategy;
    
    /**
     * 权重配置
     */
    private Map<String, Object> weightConfig;
    
    /**
     * 是否启用
     */
    private Boolean enabled;
    
    /**
     * 显示顺序
     */
    private Integer displayOrder;
    
    /**
     * 聚合后的有效数量
     */
    private Integer aggregatedValidCount;
    
    /**
     * 聚合后的无效数量
     */
    private Integer aggregatedInvalidCount;
    
    /**
     * 聚合后的冷却中数量
     */
    private Integer aggregatedCoolingCount;
    
    /**
     * 聚合后的总数量
     */
    private Integer aggregatedTotalCount;
    
    /**
     * 聚合后的压力值
     */
    private BigDecimal aggregatedPressure;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 获取聚合的号池列表（兼容性方法）
     * @return 聚合的号池ID列表
     */
    public List<Long> getAggregatedPools() {
        return this.poolIds;
    }
    
    /**
     * 设置聚合的号池列表（兼容性方法）
     * @param aggregatedPools 聚合的号池ID列表
     */
    public void setAggregatedPools(List<Long> aggregatedPools) {
        this.poolIds = aggregatedPools;
    }
    
    /**
     * 获取聚合策略（兼容性方法）
     * @return 聚合策略
     */
    public String getStrategy() {
        return this.aggregateStrategy;
    }
    
    /**
     * 设置聚合策略（兼容性方法）
     * @param strategy 聚合策略
     */
    public void setStrategy(String strategy) {
        this.aggregateStrategy = strategy;
    }
}