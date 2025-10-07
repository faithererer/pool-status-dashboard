package com.zjc.task;

import com.zjc.datasource.DataSourceManager;
import com.zjc.dto.PoolDTO;
import com.zjc.dto.PoolStatusDTO;
import com.zjc.service.PoolService;
import com.zjc.service.PoolStatusService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 号池数据更新定时任务
 * 
 * @author zjc
 * @since 2024-01-06
 */
@Slf4j
@Component
public class PoolDataUpdateTask {

    @Autowired
    private PoolService poolService;

    @Autowired
    private PoolStatusService poolStatusService;

    @Autowired
    private DataSourceManager dataSourceManager;

    /**
     * 线程池，用于并发更新号池数据
     */
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    /**
     * 定时更新所有号池状态数据
     * 每分钟执行一次
     */
    @Scheduled(fixedRate = 60000) // 60秒
    public void updateAllPoolStatus() {
        log.debug("开始定时更新所有号池状态数据");
        
        try {
            // 获取所有启用的号池
            List<PoolDTO> pools = poolService.getEnabledPools();
            if (pools.isEmpty()) {
                log.debug("没有启用的号池，跳过更新");
                return;
            }
            
            log.info("开始更新 {} 个号池的状态数据", pools.size());
            
            // 并发更新所有号池状态
            CompletableFuture<?>[] futures = pools.stream()
                .map(pool -> CompletableFuture.runAsync(() -> updateSinglePoolStatus(pool), executorService))
                .toArray(CompletableFuture[]::new);
            
            // 等待所有任务完成，最多等待30秒
            CompletableFuture.allOf(futures).get(30, TimeUnit.SECONDS);
            
            log.info("所有号池状态数据更新完成");
            
        } catch (Exception e) {
            log.error("定时更新号池状态数据失败", e);
        }
    }

    /**
     * 更新单个号池状态
     */
    private void updateSinglePoolStatus(PoolDTO pool) {
        try {
            log.debug("开始更新号池状态, poolId: {}, poolName: {}", pool.getId(), pool.getName());
            
            // 检查号池是否启用
            if (!pool.getEnabled()) {
                log.debug("号池已禁用，跳过更新, poolId: {}", pool.getId());
                return;
            }
            
            // 获取数据源配置
            String dataSourceClass = pool.getDataSourceClass();
            Map<String, Object> dataSourceConfigMap = pool.getDataSourceConfig();
            
            if (dataSourceClass == null || dataSourceClass.trim().isEmpty()) {
                log.warn("号池数据源类型为空，跳过更新, poolId: {}", pool.getId());
                return;
            }
            
            // 从数据源获取最新状态
            PoolStatusDTO status = dataSourceManager.getPoolStatus(
                pool.getId(),
                pool.getName(),
                dataSourceClass,
                dataSourceConfigMap
            );
            
            if (status != null) {
                // 保存状态数据
                poolStatusService.savePoolStatus(status);
                log.debug("号池状态更新成功, poolId: {}, validCount: {}, totalCount: {}, pressure: {}%", 
                    pool.getId(), status.getValidCount(), status.getTotalCount(), status.getPressure());
            } else {
                log.warn("获取号池状态数据失败, poolId: {}", pool.getId());
            }
            
        } catch (Exception e) {
            log.error("更新号池状态失败, poolId: {}, poolName: {}", pool.getId(), pool.getName(), e);
        }
    }

    /**
     * 清理过期数据定时任务
     * 每天凌晨2点执行
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanExpiredData() {
        log.info("开始清理过期的号池状态数据");
        
        try {
            // 默认保留30天的数据
            int retentionDays = 30;
            long deletedCount = poolStatusService.deleteExpiredData(retentionDays);
            
            log.info("清理过期数据完成，删除了 {} 条记录", deletedCount);
            
        } catch (Exception e) {
            log.error("清理过期数据失败", e);
        }
    }

    /**
     * 检查数据源连接状态定时任务
     * 每10分钟执行一次
     */
    @Scheduled(fixedRate = 600000) // 10分钟
    public void checkDataSourceConnections() {
        log.debug("开始检查数据源连接状态");
        
        try {
            List<PoolDTO> pools = poolService.getEnabledPools();
            
            for (PoolDTO pool : pools) {
                try {
                    String dataSourceClass = pool.getDataSourceClass();
                    Map<String, Object> dataSourceConfigMap = pool.getDataSourceConfig();
                    
                    if (dataSourceClass == null || dataSourceClass.trim().isEmpty()) {
                        continue;
                    }
                    
                    boolean connected = dataSourceManager.checkConnection(dataSourceClass, dataSourceConfigMap);
                    
                    if (!connected) {
                        log.warn("号池数据源连接异常, poolId: {}, poolName: {}, dataSourceClass: {}",
                            pool.getId(), pool.getName(), dataSourceClass);
                    }
                    
                } catch (Exception e) {
                    log.error("检查号池数据源连接失败, poolId: {}", pool.getId(), e);
                }
            }
            
        } catch (Exception e) {
            log.error("检查数据源连接状态失败", e);
        }
    }

    /**
     * 手动触发更新指定号池状态
     */
    public void updatePoolStatusManually(Long poolId) {
        log.info("手动触发更新号池状态, poolId: {}", poolId);
        
        try {
            PoolDTO pool = poolService.getPoolById(poolId);
            if (pool == null) {
                log.warn("号池不存在, poolId: {}", poolId);
                return;
            }
            
            updateSinglePoolStatus(pool);
            log.info("手动更新号池状态完成, poolId: {}", poolId);
            
        } catch (Exception e) {
            log.error("手动更新号池状态失败, poolId: {}", poolId, e);
        }
    }

    /**
     * 手动触发更新所有号池状态
     */
    public void updateAllPoolStatusManually() {
        log.info("手动触发更新所有号池状态");
        
        try {
            updateAllPoolStatus();
            log.info("手动更新所有号池状态完成");
            
        } catch (Exception e) {
            log.error("手动更新所有号池状态失败", e);
        }
    }

    /**
     * 获取任务执行统计信息
     */
    public String getTaskStatistics() {
        return String.format("线程池状态 - 活跃线程: %d 常驻线程: %d, 队列大小: %d",
            ((java.util.concurrent.ThreadPoolExecutor) executorService).getActiveCount(),
            ((java.util.concurrent.ThreadPoolExecutor) executorService).getCorePoolSize(),
            ((java.util.concurrent.ThreadPoolExecutor) executorService).getQueue().size());
    }
}