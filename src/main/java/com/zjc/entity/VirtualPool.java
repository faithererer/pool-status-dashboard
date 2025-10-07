package com.zjc.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 虚拟聚合池实体类
 * 
 * @author zjc
 * @version 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("virtual_pool")
public class VirtualPool extends BaseEntity {

    /**
     * 虚拟池名称
     */
    @TableField("name")
    private String name;

    /**
     * 虚拟池描述
     */
    @TableField("description")
    private String description;

    /**
     * 聚合的号池ID列表（JSON格式）
     */
    @TableField("pool_ids")
    private String poolIds;

    /**
     * 聚合策略 (sum-求和, average-平均值, weighted-加权平均)
     */
    @TableField("aggregate_strategy")
    private String aggregateStrategy;

    /**
     * 权重配置（JSON格式，仅在加权平均时使用）
     */
    @TableField("weight_config")
    private String weightConfig;

    /**
     * 是否启用
     */
    @TableField("enabled")
    private Boolean enabled;

    /**
     * 显示顺序
     */
    @TableField("display_order")
    private Integer displayOrder;
}