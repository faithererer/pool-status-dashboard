package com.zjc.datasource.impl;

import com.zjc.datasource.PoolDataSourceService;
import com.zjc.dto.PoolStatusDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Random;

/**
 * 模拟号池数据源服务实现
 * 用于演示和测试，生成随机的号池状态数据
 * 
 * @author zjc
 * @since 2024-01-06
 */
@Slf4j
@Service
public class MockPoolDataSourceService implements PoolDataSourceService {

    private final Random random = new Random();

    @Override
    public PoolStatusDTO getPoolStatus(Long poolId, String poolName, String dataSourceConfig) {
        log.debug("获取模拟号池状态数据, poolId: {}, poolName: {}", poolId, poolName);
        
        try {
            // 模拟网络延迟
            Thread.sleep(random.nextInt(100) + 50);
            
            // 生成随机状态数据
            PoolStatusDTO status = new PoolStatusDTO();
            status.setPoolId(poolId);
            
            // 生成基础数据
            int totalCount = 1000 + random.nextInt(4000); // 1000-5000
            int invalidCount = random.nextInt(totalCount -50); // 0-10%
            int coolingCount = random.nextInt(totalCount / 20); // 0-5%
            int validCount = totalCount - invalidCount - coolingCount;
            
            // 确保有效数量不为负数
            if (validCount < 0) {
                validCount = 0;
                invalidCount = totalCount - coolingCount;
            }
            
            status.setValidCount(validCount);
            status.setInvalidCount(invalidCount);
            status.setCoolingCount(coolingCount);
            status.setTotalCount(totalCount);
            
            // 计算压力值 - 基于无效数量和冷却数量的比例
            double pressure = totalCount > 0 ? (double) (invalidCount + coolingCount) / totalCount * 100 : 0.0;
            // 添加一些随机波动使数据更真实
            pressure = Math.max(0, Math.min(100, pressure + (random.nextGaussian() * 5)));
            status.setPressure(BigDecimal.valueOf(Math.round(pressure * 100.0) / 100.0)); // 保留两位小数
            
            status.setRecordTime(System.currentTimeMillis());
            
            log.debug("生成模拟数据: {}", status);
            return status;
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("获取模拟数据时被中断", e);
            return createErrorStatus(poolId);
        } catch (Exception e) {
            log.error("获取模拟数据失败", e);
            return createErrorStatus(poolId);
        }
    }

    @Override
    public boolean checkConnection(String dataSourceConfig) {
        log.debug("检查模拟数据源连接");
        
        try {
            // 模拟连接检查延迟
            Thread.sleep(random.nextInt(50) + 10);
            
            // 90%的概率返回连接正常
            boolean connected = random.nextDouble() < 0.9;
            log.debug("模拟数据源连接检查结果: {}", connected);
            return connected;
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("连接检查被中断", e);
            return false;
        } catch (Exception e) {
            log.error("连接检查失败", e);
            return false;
        }
    }

    @Override
    public String getDataSourceType() {
        return "mock";
    }

    @Override
    public String getDescription() {
        return "模拟数据源，用于演示和测试，生成随机的号池状态数据";
    }

    /**
     * 创建错误状态数据
     */
    private PoolStatusDTO createErrorStatus(Long poolId) {
        PoolStatusDTO status = new PoolStatusDTO();
        status.setPoolId(poolId);
        status.setValidCount(0);
        status.setInvalidCount(0);
        status.setCoolingCount(0);
        status.setTotalCount(0);
        status.setPressure(BigDecimal.valueOf(0.0));
        status.setRecordTime(System.currentTimeMillis());
        return status;
    }
}