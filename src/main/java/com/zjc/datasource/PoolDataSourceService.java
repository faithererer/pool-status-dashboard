package com.zjc.datasource;

import com.zjc.dto.PoolStatusDTO;
import org.json.JSONException;

/**
 * 号池数据源服务接口
 * 用户需要实现此接口来提供号池状态数据
 * 
 * @author zjc
 * @since 2024-01-06
 */
public interface PoolDataSourceService {

    /**
     * 获取号池状态数据
     * 
     * @param poolId 号池ID
     * @param poolName 号池名称
     * @param dataSourceConfig 数据源配置（JSON字符串）
     * @return 号池状态数据
     */
    PoolStatusDTO getPoolStatus(Long poolId, String poolName, String dataSourceConfig) throws JSONException;

    /**
     * 检查数据源连接是否正常
     * 
     * @param dataSourceConfig 数据源配置（JSON字符串）
     * @return true表示连接正常，false表示连接异常
     */
    boolean checkConnection(String dataSourceConfig);

    /**
     * 获取数据源类型名称
     * 
     * @return 数据源类型名称
     */
    String getDataSourceType();

    /**
     * 获取数据源描述
     * 
     * @return 数据源描述
     */
    String getDescription();

    // 完整全类名
    default String getClassFullName(){
        return this.getClass().getName();
    }
}