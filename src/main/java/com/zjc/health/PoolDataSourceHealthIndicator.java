package com.zjc.health;

import com.zjc.datasource.DataSourceManager;
import com.zjc.dto.PoolDTO;
import com.zjc.service.PoolService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 号池数据源健康检查指示器
 * 
 * @author zjc
 * @since 2024-01-06
 */
@Slf4j
@Component
public class PoolDataSourceHealthIndicator implements HealthIndicator {

    @Autowired
    private PoolService poolService;

    @Autowired
    private DataSourceManager dataSourceManager;

    @Override
    public Health health() {
        try {
            // 获取所有启用的号池
            List<PoolDTO> pools = poolService.getEnabledPools();
            
            Map<String, Object> details = new HashMap<>();
            details.put("totalPools", pools.size());
            
            int healthyCount = 0;
            int unhealthyCount = 0;
            Map<String, String> poolStatus = new HashMap<>();
            
            // 检查每个号池的数据源连接状态
            for (PoolDTO pool : pools) {
                try {
                    String dataSourceClass = pool.getDataSourceClass();
                    Map<String, Object> dataSourceConfig = pool.getDataSourceConfig();
                    
                    if (dataSourceClass == null || dataSourceClass.trim().isEmpty()) {
                        poolStatus.put(pool.getName(), "NO_DATASOURCE");
                        unhealthyCount++;
                        continue;
                    }
                    
                    boolean connected = dataSourceManager.checkConnection(dataSourceClass, dataSourceConfig);
                    
                    if (connected) {
                        poolStatus.put(pool.getName(), "HEALTHY");
                        healthyCount++;
                    } else {
                        poolStatus.put(pool.getName(), "UNHEALTHY");
                        unhealthyCount++;
                    }
                    
                } catch (Exception e) {
                    log.error("检查号池数据源健康状态失败, poolId: {}", pool.getId(), e);
                    poolStatus.put(pool.getName(), "ERROR: " + e.getMessage());
                    unhealthyCount++;
                }
            }
            
            details.put("healthyPools", healthyCount);
            details.put("unhealthyPools", unhealthyCount);
            details.put("poolDetails", poolStatus);
            
            // 如果所有号池都健康，返回UP状态
            if (unhealthyCount == 0) {
                return Health.up()
                    .withDetails(details)
                    .build();
            } else if (healthyCount > 0) {
                // 如果有部分号池健康，返回WARNING状态
                return Health.status("WARNING")
                    .withDetails(details)
                    .build();
            } else {
                // 如果所有号池都不健康，返回DOWN状态
                return Health.down()
                    .withDetails(details)
                    .build();
            }
            
        } catch (Exception e) {
            log.error("号池数据源健康检查失败", e);
            return Health.down()
                .withException(e)
                .build();
        }
    }
}