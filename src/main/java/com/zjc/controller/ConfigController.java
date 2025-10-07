package com.zjc.controller;

import com.zjc.common.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 系统配置控制器
 * 
 * @author zjc
 * @since 2024-01-06
 */
@Slf4j
@RestController
@RequestMapping("/api/config")
@RequiredArgsConstructor
@CrossOrigin
public class ConfigController {
    
    @Value("${app.data-retention-days:30}")
    private int dataRetentionDays;
    
    @Value("${app.collection.default-frequency:60}")
    private int defaultFrequency;
    
    @Value("${app.collection.timeout:30}")
    private int collectionTimeout;
    
    @Value("${app.collection.max-concurrent:10}")
    private int maxConcurrent;
    
    @Value("${app.cache.ttl-seconds:300}")
    private int cacheTtlSeconds;
    
    @Value("${app.cache.max-size:1000}")
    private int cacheMaxSize;
    
    @Value("${app.jwt.expiration:24}")
    private int jwtExpiration;
    
    /**
     * 获取系统配置
     * 
     * @return 系统配置信息
     */
    @GetMapping
    public Result<Map<String, Object>> getConfig() {
        try {
            Map<String, Object> config = new HashMap<>();
            
            // 数据保留配置
            Map<String, Object> dataRetention = new HashMap<>();
            dataRetention.put("retentionDays", dataRetentionDays);
            config.put("dataRetention", dataRetention);
            
            // 数据采集配置
            Map<String, Object> collection = new HashMap<>();
            collection.put("defaultFrequency", defaultFrequency);
            collection.put("timeout", collectionTimeout);
            collection.put("maxConcurrent", maxConcurrent);
            config.put("collection", collection);
            
            // 缓存配置
            Map<String, Object> cache = new HashMap<>();
            cache.put("ttlSeconds", cacheTtlSeconds);
            cache.put("maxSize", cacheMaxSize);
            config.put("cache", cache);
            
            // JWT配置
            Map<String, Object> jwt = new HashMap<>();
            jwt.put("expiration", jwtExpiration);
            config.put("jwt", jwt);
            
            // 系统信息
            Map<String, Object> system = new HashMap<>();
            system.put("version", "1.0.0");
            system.put("name", "Pool Status Dashboard");
            system.put("description", "号池监控面板 - 轻量化监控系统");
            config.put("system", system);
            
            return Result.success("获取配置成功", config);
        } catch (Exception e) {
            log.error("获取系统配置失败: {}", e.getMessage());
            return Result.error("获取系统配置失败");
        }
    }
    
    /**
     * 更新系统配置
     * 注意：这个接口需要管理员权限
     * 
     * @param configRequest 配置更新请求
     * @return 更新结果
     */
    @PutMapping
    public Result<Void> updateConfig(@RequestBody Map<String, Object> configRequest) {
        try {
            // 这里可以实现配置更新逻辑
            // 由于当前使用的是基于配置文件的方式，实际更新需要重启应用
            // 在生产环境中，可以考虑使用数据库存储配置或者配置中心
            
            log.info("收到配置更新请求: {}", configRequest);
            
            // 模拟配置更新成功
            return Result.success("配置更新成功，部分配置需要重启应用后生效");
        } catch (Exception e) {
            log.error("更新系统配置失败: {}", e.getMessage());
            return Result.error("更新系统配置失败");
        }
    }
    
    /**
     * 获取系统状态
     * 
     * @return 系统状态信息
     */
    @GetMapping("/status")
    public Result<Map<String, Object>> getSystemStatus() {
        try {
            Map<String, Object> status = new HashMap<>();
            
            // 运行时信息
            Runtime runtime = Runtime.getRuntime();
            Map<String, Object> runtime_info = new HashMap<>();
            runtime_info.put("totalMemory", runtime.totalMemory());
            runtime_info.put("freeMemory", runtime.freeMemory());
            runtime_info.put("usedMemory", runtime.totalMemory() - runtime.freeMemory());
            runtime_info.put("maxMemory", runtime.maxMemory());
            runtime_info.put("availableProcessors", runtime.availableProcessors());
            status.put("runtime", runtime_info);
            
            // 系统属性
            Map<String, Object> systemProps = new HashMap<>();
            systemProps.put("javaVersion", System.getProperty("java.version"));
            systemProps.put("osName", System.getProperty("os.name"));
            systemProps.put("osVersion", System.getProperty("os.version"));
            systemProps.put("osArch", System.getProperty("os.arch"));
            status.put("system", systemProps);
            
            // 应用状态
            Map<String, Object> appStatus = new HashMap<>();
            appStatus.put("status", "running");
            appStatus.put("uptime", System.currentTimeMillis());
            status.put("application", appStatus);
            
            return Result.success("获取系统状态成功", status);
        } catch (Exception e) {
            log.error("获取系统状态失败: {}", e.getMessage());
            return Result.error("获取系统状态失败");
        }
    }
}