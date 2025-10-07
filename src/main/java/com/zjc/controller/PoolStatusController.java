package com.zjc.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zjc.common.Result;
import com.zjc.dto.PoolStatusDTO;
import com.zjc.service.PoolStatusService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 号池状态控制器
 * 
 * @author zjc
 * @since 2024-01-06
 */
@Slf4j
@RestController
@RequestMapping("/api/pool-status")
@CrossOrigin
public class PoolStatusController {

    @Autowired
    private PoolStatusService poolStatusService;

    /**
     * 分页查询号池状态历史记录
     */
    @GetMapping
    public Result<Page<PoolStatusDTO>> getPoolStatusPage(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) Long poolId,
            @RequestParam(required = false) Long startTime,
            @RequestParam(required = false) Long endTime) {
        
        try {
            Page<PoolStatusDTO> page = poolStatusService.getPoolStatusPage(current, size, poolId, startTime, endTime);
            return Result.success(page);
        } catch (Exception e) {
            log.error("分页查询号池状态历史记录失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 获取号池最新状态
     */
    @GetMapping("/latest/{poolId}")
    public Result<PoolStatusDTO> getLatestPoolStatus(@PathVariable Long poolId) {
        try {
            PoolStatusDTO status = poolStatusService.getLatestPoolStatus(poolId);
            if (status == null) {
                return Result.notFound("未找到号池状态数据");
            }
            return Result.success(status);
        } catch (Exception e) {
            log.error("获取号池最新状态失败, poolId: {}", poolId, e);
            return Result.error("获取失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有号池的最新状态
     */
    @GetMapping("/latest")
    public Result<List<PoolStatusDTO>> getAllLatestPoolStatus() {
        try {
            List<PoolStatusDTO> statusList = poolStatusService.getAllLatestPoolStatus();
            return Result.success(statusList);
        } catch (Exception e) {
            log.error("获取所有号池最新状态失败", e);
            return Result.error("获取失败: " + e.getMessage());
        }
    }

    /**
     * 获取指定号池列表的最新状态
     */
    @PostMapping("/latest/batch")
    public Result<List<PoolStatusDTO>> getLatestPoolStatusByIds(@RequestBody List<Long> poolIds) {
        try {
            List<PoolStatusDTO> statusList = poolStatusService.getLatestPoolStatusByIds(poolIds);
            return Result.success(statusList);
        } catch (Exception e) {
            log.error("批量获取号池最新状态失败, poolIds: {}", poolIds, e);
            return Result.error("获取失败: " + e.getMessage());
        }
    }

    /**
     * 保存号池状态
     */
    @PostMapping
    public Result<PoolStatusDTO> savePoolStatus(@RequestBody PoolStatusDTO poolStatusDTO) {
        try {
            PoolStatusDTO savedStatus = poolStatusService.savePoolStatus(poolStatusDTO);
            return Result.success("保存成功", savedStatus);
        } catch (Exception e) {
            log.error("保存号池状态失败", e);
            return Result.error("保存失败: " + e.getMessage());
        }
    }

    /**
     * 批量保存号池状态
     */
    @PostMapping("/batch")
    public Result<Void> batchSavePoolStatus(@RequestBody List<PoolStatusDTO> poolStatusList) {
        try {
            boolean saved = poolStatusService.batchSavePoolStatus(poolStatusList);
            if (saved) {
                return Result.<Void>success("批量保存成功");
            } else {
                return Result.<Void>error("批量保存失败");
            }
        } catch (Exception e) {
            log.error("批量保存号池状态失败", e);
            return Result.error("批量保存失败: " + e.getMessage());
        }
    }

    /**
     * 获取号池状态历史趋势数据
     */
    @GetMapping("/trend/{poolId}")
    public Result<List<PoolStatusDTO>> getPoolStatusTrend(
            @PathVariable Long poolId,
            @RequestParam(required = false) Long startTime,
            @RequestParam(required = false) Long endTime,
            @RequestParam(required = false) Integer interval) {
        
        try {
            List<PoolStatusDTO> trendData = poolStatusService.getPoolStatusTrend(poolId, startTime, endTime, interval);
            return Result.success(trendData);
        } catch (Exception e) {
            log.error("获取号池状态趋势数据失败, poolId: {}", poolId, e);
            return Result.error("获取失败: " + e.getMessage());
        }
    }

    /**
     * 获取号池状态统计信息
     */
    @GetMapping("/statistics/{poolId}")
    public Result<Map<String, Object>> getPoolStatusStatistics(
            @PathVariable Long poolId,
            @RequestParam(required = false) Long startTime,
            @RequestParam(required = false) Long endTime) {
        
        try {
            Map<String, Object> statistics = poolStatusService.getPoolStatusStatistics(poolId, startTime, endTime);
            return Result.success(statistics);
        } catch (Exception e) {
            log.error("获取号池状态统计信息失败, poolId: {}", poolId, e);
            return Result.error("获取失败: " + e.getMessage());
        }
    }

    /**
     * 获取总览统计数据
     */
    @GetMapping("/overview")
    public Result<Map<String, Object>> getOverviewStatistics() {
        try {
            Map<String, Object> statistics = poolStatusService.getOverviewStatistics();
            return Result.success(statistics);
        } catch (Exception e) {
            log.error("获取总览统计数据失败", e);
            return Result.error("获取失败: " + e.getMessage());
        }
    }

    /**
     * 获取高压力号池列表
     */
    @GetMapping("/high-pressure")
    public Result<List<PoolStatusDTO>> getHighPressurePools(
            @RequestParam(required = false, defaultValue = "80.0") Double pressureThreshold) {
        
        try {
            List<PoolStatusDTO> highPressurePools = poolStatusService.getHighPressurePools(pressureThreshold);
            return Result.success(highPressurePools);
        } catch (Exception e) {
            log.error("获取高压力号池列表失败", e);
            return Result.error("获取失败: " + e.getMessage());
        }
    }

    /**
     * 获取号池压力分布统计
     */
    @GetMapping("/pressure-distribution")
    public Result<Map<String, Integer>> getPressureDistribution() {
        try {
            Map<String, Integer> distribution = poolStatusService.getPressureDistribution();
            return Result.success(distribution);
        } catch (Exception e) {
            log.error("获取号池压力分布统计失败", e);
            return Result.error("获取失败: " + e.getMessage());
        }
    }

    /**
     * 检查号池状态是否异常
     */
    @GetMapping("/check-abnormal/{poolId}")
    public Result<Boolean> isPoolStatusAbnormal(@PathVariable Long poolId) {
        try {
            boolean abnormal = poolStatusService.isPoolStatusAbnormal(poolId);
            return Result.success(abnormal);
        } catch (Exception e) {
            log.error("检查号池状态是否异常失败, poolId: {}", poolId, e);
            return Result.error("检查失败: " + e.getMessage());
        }
    }

    /**
     * 删除过期的历史数据
     */
    @DeleteMapping("/expired")
    public Result<Long> deleteExpiredData(@RequestParam(defaultValue = "30") int retentionDays) {
        try {
            long deletedCount = poolStatusService.deleteExpiredData(retentionDays);
            return Result.success("删除过期数据成功", deletedCount);
        } catch (Exception e) {
            log.error("删除过期数据失败, retentionDays: {}", retentionDays, e);
            return Result.error("删除失败: " + e.getMessage());
        }
    }
}