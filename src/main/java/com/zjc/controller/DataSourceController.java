package com.zjc.controller;

import com.zjc.common.Result;
import com.zjc.datasource.DataSourceManager;
import com.zjc.dto.DataSourceTypeDTO;
import com.zjc.dto.PoolStatusDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 数据源控制器
 * 
 * @author zjc
 * @since 2024-01-06
 */
@Slf4j
@RestController
@RequestMapping("/api/datasource")
@CrossOrigin

public class DataSourceController {

    @Autowired
    private DataSourceManager dataSourceManager;

    /**
     * 获取所有可用的数据源类型
     */
    @GetMapping("/types")
    public Result<List<DataSourceTypeDTO>> getAvailableDataSources() {
        try {
            List<DataSourceTypeDTO> dataSources = dataSourceManager.getAvailableDataSources();
            return Result.success(dataSources);
        } catch (Exception e) {
            log.error("获取可用数据源类型失败", e);
            return Result.error("获取失败: " + e.getMessage());
        }
    }

    /**
     * 测试数据源连接
     */
    @PostMapping("/test-connection")
    public Result<Boolean> testConnection(
            @RequestParam String dataSourceType,
            @RequestBody(required = false) Map<String, Object> dataSourceConfig) {
        
        try {
            boolean connected = dataSourceManager.checkConnection(dataSourceType, dataSourceConfig);
            return Result.success("连接测试完成", connected);
        } catch (Exception e) {
            log.error("测试数据源连接失败, dataSourceType: {}", dataSourceType, e);
            return Result.error("测试失败: " + e.getMessage());
        }
    }

    /**
     * 测试获取号池状态数据
     */
    @PostMapping("/test-data")
    public Result<PoolStatusDTO> testGetPoolStatus(
            @RequestParam Long poolId,
            @RequestParam String poolName,
            @RequestParam String dataSourceType,
            @RequestBody(required = false) Map<String, Object> dataSourceConfig) {
        
        try {
            PoolStatusDTO status = dataSourceManager.getPoolStatus(poolId, poolName, dataSourceType, dataSourceConfig);
            return Result.success("数据获取测试完成", status);
        } catch (Exception e) {
            log.error("测试获取号池状态数据失败, poolId: {}, dataSourceType: {}", poolId, dataSourceType, e);
            return Result.error("测试失败: " + e.getMessage());
        }
    }
}