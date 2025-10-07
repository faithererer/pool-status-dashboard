package com.zjc.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 号池状态实体类
 * 
 * @author zjc
 * @version 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pool_status")
public class PoolStatus extends BaseEntity {

    /**
     * 号池ID
     */
    @TableField("pool_id")
    private Long poolId;

    /**
     * 有效数量
     */
    @TableField("valid_count")
    private Integer validCount;

    /**
     * 无效数量
     */
    @TableField("invalid_count")
    private Integer invalidCount;

    /**
     * 冷却中数量
     */
    @TableField("cooling_count")
    private Integer coolingCount;

    /**
     * 总数量
     */
    @TableField("total_count")
    private Integer totalCount;

    /**
     * 号池压力（百分比）
     * 计算公式：有效数量/总数量 * 100
     */
    @TableField("pressure")
    private BigDecimal pressure;

    /**
     * 记录时间戳
     */
    @TableField("record_time")
    private Long recordTime;

    /**
     * 数据来源 (system-系统采集, manual-手动录入)
     */
    @TableField("data_source")
    private String dataSource;

    /**
     * 备注信息
     */
    @TableField("remarks")
    private String remarks;
}