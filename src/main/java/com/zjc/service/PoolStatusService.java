package com.zjc.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zjc.dto.PoolStatusDTO;
import com.zjc.entity.PoolStatus;

import java.util.List;
import java.util.Map;

/**
 * 号池状态服务接口
 * 
 * @author zjc
 * @since 2024-01-06
 */
public interface PoolStatusService extends IService<PoolStatus> {
    
    /**
     * 分页查询号池状态历史记录
     * 
     * @param current 当前页
     * @param size 页大小
     * @param poolId 号池ID
     * @param startTime 开始时间戳
     * @param endTime 结束时间戳
     * @return 分页结果
     */
    Page<PoolStatusDTO> getPoolStatusPage(long current, long size, Long poolId, Long startTime, Long endTime);
    
    /**
     * 获取号池最新状态
     * 
     * @param poolId 号池ID
     * @return 最新状态
     */
    PoolStatusDTO getLatestPoolStatus(Long poolId);
    
    /**
     * 获取所有号池的最新状态
     * 
     * @return 所有号池的最新状态列表
     */
    List<PoolStatusDTO> getAllLatestPoolStatus();
    
    /**
     * 获取指定号池列表的最新状态
     * 
     * @param poolIds 号池ID列表
     * @return 号池状态列表
     */
    List<PoolStatusDTO> getLatestPoolStatusByIds(List<Long> poolIds);
    
    /**
     * 保存号池状态
     * 
     * @param poolStatusDTO 号池状态
     * @return 保存的状态
     */
    PoolStatusDTO savePoolStatus(PoolStatusDTO poolStatusDTO);
    
    /**
     * 批量保存号池状态
     * 
     * @param poolStatusList 号池状态列表
     * @return 是否保存成功
     */
    boolean batchSavePoolStatus(List<PoolStatusDTO> poolStatusList);
    
    /**
     * 获取号池状态历史趋势数据
     * 
     * @param poolId 号池ID
     * @param startTime 开始时间戳
     * @param endTime 结束时间戳
     * @param interval 时间间隔(秒)
     * @return 趋势数据
     */
    List<PoolStatusDTO> getPoolStatusTrend(Long poolId, Long startTime, Long endTime, Integer interval);
    
    /**
     * 获取号池状态统计信息
     * 
     * @param poolId 号池ID
     * @param startTime 开始时间戳
     * @param endTime 结束时间戳
     * @return 统计信息
     */
    Map<String, Object> getPoolStatusStatistics(Long poolId, Long startTime, Long endTime);
    
    /**
     * 获取总览统计数据
     * 
     * @return 总览统计数据
     */
    Map<String, Object> getOverviewStatistics();
    
    /**
     * 删除过期的历史数据
     * 
     * @param retentionDays 保留天数
     * @return 删除的记录数
     */
    long deleteExpiredData(int retentionDays);
    
    /**
     * 获取高压力号池列表
     * 
     * @param pressureThreshold 压力阈值
     * @return 高压力号池状态列表
     */
    List<PoolStatusDTO> getHighPressurePools(Double pressureThreshold);
    
    /**
     * 获取号池压力分布统计
     * 
     * @return 压力分布统计
     */
    Map<String, Integer> getPressureDistribution();
    
    /**
     * 检查号池状态是否异常
     * 
     * @param poolId 号池ID
     * @return 是否异常
     */
    boolean isPoolStatusAbnormal(Long poolId);
}