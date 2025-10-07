package com.zjc.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 号池状态数据传输对象
 * 
 * @author zjc
 * @since 2024-01-06
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class PoolStatusDTO {
    
    /**
     * 状态ID
     */
    private Long id;
    
    /**
     * 号池ID
     */
    private Long poolId;
    
    /**
     * 号池名称
     */
    private String poolName;
    
    /**
     * 有效数量
     */
    private Integer validCount;
    
    /**
     * 无效数量
     */
    private Integer invalidCount;
    
    /**
     * 冷却中数量
     */
    private Integer coolingCount;
    
    /**
     * 总数量
     */
    private Integer totalCount;
    
    /**
     * 号池压力(百分比)
     */
    private BigDecimal pressure;
    
    /**
     * 记录时间戳
     */
    private Long recordTime;
    
    /**
     * 数据来源
     */
    private String dataSource;
    
    /**
     * 备注信息
     */
    private String remarks;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}