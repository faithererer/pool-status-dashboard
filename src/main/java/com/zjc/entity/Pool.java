package com.zjc.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 号池实体类
 * 
 * @author zjc
 * @version 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pool")
public class Pool extends BaseEntity {

    /**
     * 号池名称
     */
    @TableField("name")
    private String name;

    /**
     * 号池描述
     */
    @TableField("description")
    private String description;

    /**
     * 数据源服务类名
     */
    @TableField("data_source_class")
    private String dataSourceClass;

    /**
     * 数据源配置（JSON格式）
     */
    @TableField("data_source_config")
    private String dataSourceConfig;

    /**
     * 更新频率（秒）
     */
    @TableField("update_frequency")
    private Integer updateFrequency;

    /**
     * 显示字段配置（JSON格式）
     * 控制前端显示哪些字段
     */
    @TableField("display_fields")
    private String displayFields;

    /**
     * 显示策略 (public-公开显示, private-隐藏保护)
     */
    @TableField("display_strategy")
    private String displayStrategy;

    /**
     * 是否启用
     */
    @TableField("enabled")
    private Boolean enabled;

    /**
     * 最后更新时间戳
     */
    @TableField("last_update_time")
    private Long lastUpdateTime;

    /**
     * 最后更新状态 (success-成功, failed-失败, timeout-超时)
     */
    @TableField("last_update_status")
    private String lastUpdateStatus;

    /**
     * 最后更新错误信息
     */
    @TableField("last_error_message")
    private String lastErrorMessage;
}