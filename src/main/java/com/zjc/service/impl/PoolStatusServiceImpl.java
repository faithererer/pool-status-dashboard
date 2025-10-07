package com.zjc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zjc.dto.PoolStatusDTO;
import com.zjc.entity.Pool;
import com.zjc.entity.PoolStatus;
import com.zjc.mapper.PoolMapper;
import com.zjc.mapper.PoolStatusMapper;
import com.zjc.service.PoolStatusService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 号池状态服务实现类
 * 
 * @author zjc
 * @since 2024-01-06
 */
@Slf4j
@Service
public class PoolStatusServiceImpl extends ServiceImpl<PoolStatusMapper, PoolStatus> implements PoolStatusService {

    @Autowired
    private PoolMapper poolMapper;
    @Autowired
    private PoolStatusMapper poolStatusMapper;

    @Override
    public Page<PoolStatusDTO> getPoolStatusPage(long current, long size, Long poolId, Long startTime, Long endTime) {
        LambdaQueryWrapper<PoolStatus> queryWrapper = new LambdaQueryWrapper<>();
        
        if (poolId != null) {
            queryWrapper.eq(PoolStatus::getPoolId, poolId);
        }
        
        if (startTime != null) {
            queryWrapper.ge(PoolStatus::getRecordTime, startTime);
        }
        
        if (endTime != null) {
            queryWrapper.le(PoolStatus::getRecordTime, endTime);
        }
        
        // 按记录时间倒序
        queryWrapper.orderByDesc(PoolStatus::getRecordTime);
        
        Page<PoolStatus> statusPage = this.page(new Page<>(current, size), queryWrapper);
        
        // 转换为DTO
        Page<PoolStatusDTO> dtoPage = new Page<>(current, size, statusPage.getTotal());
        List<PoolStatusDTO> dtoList = statusPage.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        dtoPage.setRecords(dtoList);
        
        return dtoPage;
    }

    @Override
    public PoolStatusDTO getLatestPoolStatus(Long poolId) {
        LambdaQueryWrapper<PoolStatus> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PoolStatus::getPoolId, poolId)
                   .orderByDesc(PoolStatus::getRecordTime)
                   .last("LIMIT 1");
        
        PoolStatus status = this.getOne(queryWrapper);
        return convertToDTO(status);
    }

    @Override
    public List<PoolStatusDTO> getAllLatestPoolStatus() {
        // 获取所有启用的号池
        LambdaQueryWrapper<Pool> poolQuery = new LambdaQueryWrapper<>();
        poolQuery.eq(Pool::getEnabled, true);
        List<Pool> pools = poolMapper.selectList(poolQuery);
        
        if (pools.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<PoolStatusDTO> result = new ArrayList<>();
        
        for (Pool pool : pools) {
            PoolStatusDTO latestStatus = getLatestPoolStatus(pool.getId());
            if (latestStatus != null) {
                latestStatus.setPoolName(pool.getName());
                result.add(latestStatus);
            }
        }
        
        return result;
    }

    @Override
    public List<PoolStatusDTO> getLatestPoolStatusByIds(List<Long> poolIds) {
        if (poolIds == null || poolIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<PoolStatusDTO> result = new ArrayList<>();
        
        for (Long poolId : poolIds) {
            PoolStatusDTO latestStatus = getLatestPoolStatus(poolId);
            if (latestStatus != null) {
                // 获取号池名称
                Pool pool = poolMapper.selectById(poolId);
                if (pool != null) {
                    latestStatus.setPoolName(pool.getName());
                }
                result.add(latestStatus);
            }
        }
        
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PoolStatusDTO savePoolStatus(PoolStatusDTO poolStatusDTO) {
        PoolStatus status = convertToEntity(poolStatusDTO);
        
        // 设置记录时间为当前时间戳
        if (status.getRecordTime() == null) {
            status.setRecordTime(System.currentTimeMillis());
        }
        
        // 计算压力值 - 基于无效数量和冷却数量的比例
        if (status.getTotalCount() != null && status.getTotalCount() > 0) {
            int invalidCount = status.getInvalidCount() != null ? status.getInvalidCount() : 0;
            int coolingCount = status.getCoolingCount() != null ? status.getCoolingCount() : 0;
            BigDecimal pressure = BigDecimal.valueOf(invalidCount + coolingCount)
                    .divide(BigDecimal.valueOf(status.getTotalCount()), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            status.setPressure(pressure);
        }
        
        boolean saved = this.save(status);
        if (!saved) {
            throw new RuntimeException("保存号池状态失败");
        }
        
        return convertToDTO(status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchSavePoolStatus(List<PoolStatusDTO> poolStatusList) {
        if (poolStatusList == null || poolStatusList.isEmpty()) {
            return true;
        }
        
        List<PoolStatus> statusList = poolStatusList.stream()
                .map(this::convertToEntity)
                .collect(Collectors.toList());
        
        // 设置记录时间和计算压力值
        long currentTime = System.currentTimeMillis();
        for (PoolStatus status : statusList) {
            if (status.getRecordTime() == null) {
                status.setRecordTime(currentTime);
            }
            
            // 计算压力值 - 基于无效数量和冷却数量的比例
            if (status.getTotalCount() != null && status.getTotalCount() > 0) {
                int invalidCount = status.getInvalidCount() != null ? status.getInvalidCount() : 0;
                int coolingCount = status.getCoolingCount() != null ? status.getCoolingCount() : 0;
                BigDecimal pressure = BigDecimal.valueOf(invalidCount + coolingCount)
                        .divide(BigDecimal.valueOf(status.getTotalCount()), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
                status.setPressure(pressure);
            }
        }
        
        boolean saved = this.saveBatch(statusList);
        if (saved) {
            log.info("批量保存号池状态成功，数量: {}", statusList.size());
        }
        
        return saved;
    }

    @Override
    public List<PoolStatusDTO> getPoolStatusTrend(Long poolId, Long startTime, Long endTime, Integer interval) {
        LambdaQueryWrapper<PoolStatus> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PoolStatus::getPoolId, poolId);
        
        if (startTime != null) {
            queryWrapper.ge(PoolStatus::getRecordTime, startTime);
        }
        
        if (endTime != null) {
            queryWrapper.le(PoolStatus::getRecordTime, endTime);
        }
        
        queryWrapper.orderByAsc(PoolStatus::getRecordTime);
        
        List<PoolStatus> statusList = this.list(queryWrapper);
        
        // 如果没有指定间隔，直接返回所有数据
        if (interval == null || interval <= 0) {
            return statusList.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        }
        
        // 按时间间隔采样数据
        List<PoolStatus> sampledList = new ArrayList<>();
        long intervalMs = interval * 1000L;
        long lastSampleTime = 0;
        
        for (PoolStatus status : statusList) {
            if (status.getRecordTime() - lastSampleTime >= intervalMs) {
                sampledList.add(status);
                lastSampleTime = status.getRecordTime();
            }
        }
        
        return sampledList.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getPoolStatusStatistics(Long poolId, Long startTime, Long endTime) {
        LambdaQueryWrapper<PoolStatus> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PoolStatus::getPoolId, poolId);
        
        if (startTime != null) {
            queryWrapper.ge(PoolStatus::getRecordTime, startTime);
        }
        
        if (endTime != null) {
            queryWrapper.le(PoolStatus::getRecordTime, endTime);
        }
        
        List<PoolStatus> statusList = this.list(queryWrapper);
        
        if (statusList.isEmpty()) {
            return new HashMap<>();
        }
        
        Map<String, Object> statistics = new HashMap<>();
        
        // 计算统计数据
        OptionalDouble avgPressure = statusList.stream()
                .filter(s -> s.getPressure() != null)
                .mapToDouble(s -> s.getPressure().doubleValue())
                .average();
        
        OptionalInt maxValidCount = statusList.stream()
                .filter(s -> s.getValidCount() != null)
                .mapToInt(PoolStatus::getValidCount)
                .max();
        
        OptionalInt minValidCount = statusList.stream()
                .filter(s -> s.getValidCount() != null)
                .mapToInt(PoolStatus::getValidCount)
                .min();
        
        statistics.put("recordCount", statusList.size());
        statistics.put("avgPressure", avgPressure.isPresent() ? 
                BigDecimal.valueOf(avgPressure.getAsDouble()).setScale(2, RoundingMode.HALF_UP) : null);
        statistics.put("maxValidCount", maxValidCount.isPresent() ? maxValidCount.getAsInt() : null);
        statistics.put("minValidCount", minValidCount.isPresent() ? minValidCount.getAsInt() : null);
        
        return statistics;
    }

    @Override
    public Map<String, Object> getOverviewStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        // 获取所有启用的号池数量
        LambdaQueryWrapper<Pool> poolQuery = new LambdaQueryWrapper<>   ();
        poolQuery.eq(Pool::getEnabled, true);
        long totalPools = poolMapper.selectCount(poolQuery);
        
        // 获取活跃号池数量（最近有状态更新的）
        long oneHourAgo = System.currentTimeMillis() - 3600000; // 1小时前
        LambdaQueryWrapper<PoolStatus> activeQuery = new LambdaQueryWrapper<>();
        activeQuery.ge(PoolStatus::getRecordTime, oneHourAgo)
                  .groupBy(PoolStatus::getPoolId);
// 获取去重后的poolId列表，其大小就是活跃号池数量
        List<Object> activePoolIds = poolStatusMapper.selectObjs(activeQuery);
        long activePools = activePoolIds.size();
        // 获取所有最新状态
        List<PoolStatusDTO> latestStatuses = getAllLatestPoolStatus();
        
        // 计算总计数据
        int totalValidCount = latestStatuses.stream()
                .mapToInt(s -> s.getValidCount() != null ? s.getValidCount() : 0)
                .sum();
        
        int totalInvalidCount = latestStatuses.stream()
                .mapToInt(s -> s.getInvalidCount() != null ? s.getInvalidCount() : 0)
                .sum();
        
        int totalCoolingCount = latestStatuses.stream()
                .mapToInt(s -> s.getCoolingCount() != null ? s.getCoolingCount() : 0)
                .sum();
        
        int totalCount = latestStatuses.stream()
                .mapToInt(s -> s.getTotalCount() != null ? s.getTotalCount() : 0)
                .sum();
        
        // 计算平均压力
        OptionalDouble avgPressure = latestStatuses.stream()
                .filter(s -> s.getPressure() != null)
                .mapToDouble(s -> s.getPressure().doubleValue())
                .average();
        
        // 计算高压力号池数量（压力 > 80%）
        long highPressurePools = latestStatuses.stream()
                .filter(s -> s.getPressure() != null && s.getPressure().doubleValue() > 80.0)
                .count();
        
        statistics.put("totalPools", totalPools);
        statistics.put("activePools", activePools);
        statistics.put("totalValidCount", totalValidCount);
        statistics.put("totalInvalidCount", totalInvalidCount);
        statistics.put("totalCoolingCount", totalCoolingCount);
        statistics.put("totalCount", totalCount);
        statistics.put("avgPressure", avgPressure.isPresent() ? 
                BigDecimal.valueOf(avgPressure.getAsDouble()).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO);
        statistics.put("highPressurePools", highPressurePools);
        
        return statistics;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public long deleteExpiredData(int retentionDays) {
        long expiredTime = System.currentTimeMillis() - (retentionDays * 24L * 60L * 60L * 1000L);
        
        LambdaQueryWrapper<PoolStatus> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.lt(PoolStatus::getRecordTime, expiredTime);
        
        long deletedCount = this.count(queryWrapper);
        boolean deleted = this.remove(queryWrapper);
        
        if (deleted) {
            log.info("删除过期数据成功，删除记录数: {}, 保留天数: {}", deletedCount, retentionDays);
        }
        
        return deletedCount;
    }

    @Override
    public List<PoolStatusDTO> getHighPressurePools(Double pressureThreshold) {
        if (pressureThreshold == null) {
            pressureThreshold = 80.0;
        }
        
        List<PoolStatusDTO> allLatestStatus = getAllLatestPoolStatus();

        Double finalPressureThreshold = pressureThreshold;
        return allLatestStatus.stream()
                .filter(s -> s.getPressure() != null && s.getPressure().doubleValue() > finalPressureThreshold)
                .sorted((a, b) -> b.getPressure().compareTo(a.getPressure()))
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Integer> getPressureDistribution() {
        List<PoolStatusDTO> allLatestStatus = getAllLatestPoolStatus();
        
        Map<String, Integer> distribution = new HashMap<>();
        distribution.put("low", 0);      // 0-50%
        distribution.put("medium", 0);   // 50-80%
        distribution.put("high", 0);     // 80-100%
        
        for (PoolStatusDTO status : allLatestStatus) {
            if (status.getPressure() != null) {
                double pressure = status.getPressure().doubleValue();
                if (pressure <= 50.0) {
                    distribution.put("low", distribution.get("low") + 1);
                } else if (pressure <= 80.0) {
                    distribution.put("medium", distribution.get("medium") + 1);
                } else {
                    distribution.put("high", distribution.get("high") + 1);
                }
            }
        }
        
        return distribution;
    }

    @Override
    public boolean isPoolStatusAbnormal(Long poolId) {
        PoolStatusDTO latestStatus = getLatestPoolStatus(poolId);
        
        if (latestStatus == null) {
            return true; // 没有状态数据认为是异常
        }
        
        // 检查数据是否过期（超过5分钟没有更新）
        long fiveMinutesAgo = System.currentTimeMillis() - 300000;
        if (latestStatus.getRecordTime() < fiveMinutesAgo) {
            return true;
        }
        
        // 检查压力是否过高（超过95%）
        if (latestStatus.getPressure() != null && latestStatus.getPressure().doubleValue() > 95.0) {
            return true;
        }
        
        // 检查是否有有效数量
        if (latestStatus.getValidCount() == null || latestStatus.getValidCount() <= 0) {
            return true;
        }
        
        return false;
    }

    /**
     * 将实体转换为DTO
     */
    private PoolStatusDTO convertToDTO(PoolStatus status) {
        if (status == null) {
            return null;
        }
        
        PoolStatusDTO dto = new PoolStatusDTO();
        org.springframework.beans.BeanUtils.copyProperties(status, dto);
        
        return dto;
    }

    /**
     * 将DTO转换为实体
     */
    private PoolStatus convertToEntity(PoolStatusDTO dto) {
        if (dto == null) {
            return null;
        }
        
        PoolStatus status = new PoolStatus();
        org.springframework.beans.BeanUtils.copyProperties(dto, status);
        
        return status;
    }
}