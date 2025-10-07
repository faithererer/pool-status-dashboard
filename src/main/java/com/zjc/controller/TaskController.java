package com.zjc.controller;

import com.zjc.common.Result;
import com.zjc.task.PoolDataUpdateTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 任务管理控制器
 * 
 * @author zjc
 * @since 2024-01-06
 */
@Slf4j
@RestController
@RequestMapping("/api/tasks")
@CrossOrigin
public class TaskController {

    @Autowired
    private PoolDataUpdateTask poolDataUpdateTask;

    /**
     * 手动触发更新所有号池状态
     */
    @PostMapping("/update-all-pools")
    public Result<Void> updateAllPools() {
        try {
            log.info("手动触发更新所有号池状态");
            poolDataUpdateTask.updateAllPoolStatusManually();
            return Result.<Void>success("更新任务已启动");
        } catch (Exception e) {
            log.error("手动触发更新所有号池状态失败", e);
            return Result.<Void>error("更新失败: " + e.getMessage());
        }
    }

    /**
     * 手动触发更新指定号池状态
     */
    @PostMapping("/update-pool/{poolId}")
    public Result<Void> updatePool(@PathVariable Long poolId) {
        try {
            log.info("手动触发更新号池状态, poolId: {}", poolId);
            poolDataUpdateTask.updatePoolStatusManually(poolId);
            return Result.<Void>success("更新任务已启动");
        } catch (Exception e) {
            log.error("手动触发更新号池状态失败, poolId: {}", poolId, e);
            return Result.<Void>error("更新失败: " + e.getMessage());
        }
    }

    /**
     * 获取任务执行统计信息
     */
    @GetMapping("/statistics")
    public Result<String> getTaskStatistics() {
        try {
            String statistics = poolDataUpdateTask.getTaskStatistics();
            return Result.success(statistics);
        } catch (Exception e) {
            log.error("获取任务统计信息失败", e);
            return Result.error("获取失败: " + e.getMessage());
        }
    }
}