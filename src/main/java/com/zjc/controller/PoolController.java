package com.zjc.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zjc.common.Result;
import com.zjc.dto.PoolDTO;
import com.zjc.service.PoolService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 号池管理控制器
 * 
 * @author zjc
 * @since 2024-01-06
 */
@Slf4j
@RestController
@RequestMapping("/api/pools")
@CrossOrigin
public class PoolController {

    @Autowired
    private PoolService poolService;

    /**
     * 分页查询号池列表
     */
    @GetMapping
    public Result<Page<PoolDTO>> getPoolPage(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String displayStrategy,
            @RequestParam(required = false) Boolean enabled) {
        
        try {
            Page<PoolDTO> page = poolService.getPoolPage(current, size, name, displayStrategy, enabled);
            return Result.success(page);
        } catch (Exception e) {
            log.error("分页查询号池列表失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID获取号池详情
     */
    @GetMapping("/{id}")
    public Result<PoolDTO> getPoolById(@PathVariable Long id) {
        try {
            PoolDTO pool = poolService.getPoolById(id);
            if (pool == null) {
                return Result.notFound("号池不存在");
            }
            return Result.success(pool);
        } catch (Exception e) {
            log.error("获取号池详情失败, id: {}", id, e);
            return Result.error("获取失败: " + e.getMessage());
        }
    }

    /**
     * 创建号池
     */
    @PostMapping
    public Result<PoolDTO> createPool(@RequestBody PoolDTO poolDTO) {
        try {
            PoolDTO createdPool = poolService.createPool(poolDTO);
            return Result.success("创建成功", createdPool);
        } catch (Exception e) {
            log.error("创建号池失败", e);
            return Result.error("创建失败: " + e.getMessage());
        }
    }

    /**
     * 更新号池
     */
    @PutMapping("/{id}")
    public Result<PoolDTO> updatePool(@PathVariable Long id, @RequestBody PoolDTO poolDTO) {
        try {
            PoolDTO updatedPool = poolService.updatePool(id, poolDTO);
            return Result.success("更新成功", updatedPool);
        } catch (Exception e) {
            log.error("更新号池失败, id: {}", id, e);
            return Result.error("更新失败: " + e.getMessage());
        }
    }

    /**
     * 删除号池
     */
    @DeleteMapping("/{id}")
    public Result<Void> deletePool(@PathVariable Long id) {
        try {
            boolean deleted = poolService.deletePool(id);
            if (deleted) {
                return Result.<Void>success("删除成功");
            } else {
                return Result.<Void>error("删除失败");
            }
        } catch (Exception e) {
            log.error("删除号池失败, id: {}", id, e);
            return Result.<Void>error("删除失败: " + e.getMessage());
        }
    }

    /**
     * 批量删除号池
     */
    @DeleteMapping("/batch")
    public Result<Void> deletePoolBatch(@RequestBody List<Long> ids) {
        try {
            boolean deleted = poolService.deletePoolBatch(ids);
            if (deleted) {
                return Result.<Void>success("批量删除成功");
            } else {
                return Result.<Void>error("批量删除失败");
            }
        } catch (Exception e) {
            log.error("批量删除号池失败, ids: {}", ids, e);
            return Result.<Void>error("批量删除失败: " + e.getMessage());
        }
    }

    /**
     * 启用/禁用号池
     */
    @PutMapping("/{id}/toggle")
    public Result<Void> togglePoolEnabled(@PathVariable Long id, @RequestParam Boolean enabled) {
        try {
            boolean updated = poolService.togglePoolEnabled(id, enabled);
            if (updated) {
                return Result.<Void>success(enabled ? "启用成功" : "禁用成功");
            } else {
                return Result.<Void>error("操作失败");
            }
        } catch (Exception e) {
            log.error("切换号池启用状态失败, id: {}, enabled: {}", id, enabled, e);
            return Result.<Void>error("操作失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有启用的号池列表
     */
    @GetMapping("/enabled")
    public Result<List<PoolDTO>> getEnabledPools() {
        try {
            List<PoolDTO> pools = poolService.getEnabledPools();
            return Result.success(pools);
        } catch (Exception e) {
            log.error("获取启用号池列表失败", e);
            return Result.error("获取失败: " + e.getMessage());
        }
    }

    /**
     * 获取公开显示的号池列表
     */
    @GetMapping("/public")
    public Result<List<PoolDTO>> getPublicPools() {
        try {
            List<PoolDTO> pools = poolService.getPublicPools();
            return Result.success(pools);
        } catch (Exception e) {
            log.error("获取公开号池列表失败", e);
            return Result.error("获取失败: " + e.getMessage());
        }
    }

    /**
     * 根据显示策略获取号池列表
     */
    @GetMapping("/strategy/{displayStrategy}")
    public Result<List<PoolDTO>> getPoolsByDisplayStrategy(@PathVariable String displayStrategy) {
        try {
            List<PoolDTO> pools = poolService.getPoolsByDisplayStrategy(displayStrategy);
            return Result.success(pools);
        } catch (Exception e) {
            log.error("根据显示策略获取号池列表失败, strategy: {}", displayStrategy, e);
            return Result.error("获取失败: " + e.getMessage());
        }
    }

    /**
     * 检查号池名称是否存在
     */
    @GetMapping("/check-name")
    public Result<Boolean> checkPoolNameExists(
            @RequestParam String name,
            @RequestParam(required = false) Long excludeId) {
        try {
            boolean exists = poolService.isPoolNameExists(name, excludeId);
            return Result.success(exists);
        } catch (Exception e) {
            log.error("检查号池名称是否存在失败, name: {}", name, e);
            return Result.error("检查失败: " + e.getMessage());
        }
    }
}