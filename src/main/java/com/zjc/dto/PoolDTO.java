package com.zjc.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 号池数据传输对象
 * 
 * @author zjc
 * @since 2024-01-06
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class PoolDTO {
    
    /**
     * 号池ID
     */
    private Long id;
    
    /**
     * 号池名称
     */
    private String name;
    
    /**
     * 号池描述
     */
    private String description;
    
    /**
     * 数据源服务类名
     */
    private String dataSourceClass;
    
    /**
     * 数据源配置
     */
    private Map<String, Object> dataSourceConfig;
    
    /**
     * 更新频率(秒)
     */
    private Integer updateFrequency;
    
    /**
     * 显示字段配置
     */
    private Map<String, Boolean> displayFields;
    
    /**
     * 显示策略
     */
    private String displayStrategy;
    
    /**
     * 是否启用
     */
    private Boolean enabled;
    
    /**
     * 最后更新时间
     */
    private Long lastUpdateTime;
    
    /**
     * 最后更新状态
     */
    private String lastUpdateStatus;
    
    /**
     * 最后更新错误信息
     */
    private String lastErrorMessage;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}