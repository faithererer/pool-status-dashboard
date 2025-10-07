package com.zjc.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zjc.common.Result;
import com.zjc.dto.VirtualPoolDTO;
import com.zjc.service.VirtualPoolService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 虚拟聚合池控制器
 * 
 * @author zjc
 * @since 2024-01-06
 */
@Slf4j
@RestController
@RequestMapping("/api/virtual-pools")
@CrossOrigin
public class VirtualPoolController {

    @Autowired
    private VirtualPoolService virtualPoolService;

    /**
     * 分页查询虚拟聚合池
     */
    @GetMapping
    public Result<Page<VirtualPoolDTO>> getVirtualPoolPage(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String strategy) {
        
        try {
            Page<VirtualPoolDTO> page = virtualPoolService.getVirtualPoolPage(current, size, name, strategy);
            return Result.success(page);
        } catch (Exception e) {
            log.error("分页查询虚拟聚合池失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有虚拟聚合池
     */
    @GetMapping("/all")
    public Result<List<VirtualPoolDTO>> getAllVirtualPools() {
        try {
            List<VirtualPoolDTO> virtualPools = virtualPoolService.getAllVirtualPools();
            return Result.success(virtualPools);
        } catch (Exception e) {
            log.error("获取所有虚拟聚合池失败", e);
            return Result.error("获取失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID获取虚拟聚合池
     */
    @GetMapping("/{id}")
    public Result<VirtualPoolDTO> getVirtualPoolById(@PathVariable Long id) {
        try {
            VirtualPoolDTO virtualPool = virtualPoolService.getVirtualPoolById(id);
            if (virtualPool == null) {
                return Result.notFound("虚拟聚合池不存在");
            }
            return Result.success(virtualPool);
        } catch (Exception e) {
            log.error("根据ID获取虚拟聚合池失败, id: {}", id, e);
            return Result.error("获取失败: " + e.getMessage());
        }
    }

    /**
     * 创建虚拟聚合池
     */
    @PostMapping
    public Result<VirtualPoolDTO> createVirtualPool(@RequestBody VirtualPoolDTO virtualPoolDTO) {
        try {
            // 验证必填字段
            if (virtualPoolDTO.getName() == null || virtualPoolDTO.getName().trim().isEmpty()) {
                return Result.error("虚拟聚合池名称不能为空");
            }
            if (virtualPoolDTO.getAggregatedPools() == null || virtualPoolDTO.getAggregatedPools().isEmpty()) {
                return Result.error("聚合号池列表不能为空");
            }
            if (virtualPoolDTO.getStrategy() == null || virtualPoolDTO.getStrategy().trim().isEmpty()) {
                return Result.error("聚合策略不能为空");
            }

            VirtualPoolDTO createdVirtualPool = virtualPoolService.createVirtualPool(virtualPoolDTO);
            return Result.success("创建成功", createdVirtualPool);
        } catch (Exception e) {
            log.error("创建虚拟聚合池失败", e);
            return Result.error("创建失败: " + e.getMessage());
        }
    }

    /**
     * 更新虚拟聚合池
     */
    @PutMapping("/{id}")
    public Result<VirtualPoolDTO> updateVirtualPool(@PathVariable Long id, @RequestBody VirtualPoolDTO virtualPoolDTO) {
        try {
            // 验证必填字段
            if (virtualPoolDTO.getName() == null || virtualPoolDTO.getName().trim().isEmpty()) {
                return Result.error("虚拟聚合池名称不能为空");
            }
            if (virtualPoolDTO.getAggregatedPools() == null || virtualPoolDTO.getAggregatedPools().isEmpty()) {
                return Result.error("聚合号池列表不能为空");
            }
            if (virtualPoolDTO.getStrategy() == null || virtualPoolDTO.getStrategy().trim().isEmpty()) {
                return Result.error("聚合策略不能为空");
            }

            virtualPoolDTO.setId(id);
            VirtualPoolDTO updatedVirtualPool = virtualPoolService.updateVirtualPool(virtualPoolDTO);
            if (updatedVirtualPool == null) {
                return Result.notFound("虚拟聚合池不存在");
            }
            return Result.success("更新成功", updatedVirtualPool);
        } catch (Exception e) {
            log.error("更新虚拟聚合池失败, id: {}", id, e);
            return Result.error("更新失败: " + e.getMessage());
        }
    }

    /**
     * 删除虚拟聚合池
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteVirtualPool(@PathVariable Long id) {
        try {
            boolean deleted = virtualPoolService.deleteVirtualPool(id);
            if (deleted) {
                return Result.<Void>success("删除成功");
            } else {
                return Result.notFound("虚拟聚合池不存在");
            }
        } catch (Exception e) {
            log.error("删除虚拟聚合池失败, id: {}", id, e);
            return Result.<Void>error("删除失败: " + e.getMessage());
        }
    }

    /**
     * 批量删除虚拟聚合池
     */
    @DeleteMapping("/batch")
    public Result<String> batchDeleteVirtualPools(@RequestBody List<Long> ids) {
        try {
            if (ids == null || ids.isEmpty()) {
                return Result.<String>error("删除ID列表不能为空");
            }
            
            boolean deleted = virtualPoolService.batchDeleteVirtualPools(ids);
            if (deleted) {
                return Result.success("批量删除成功");
            } else {
                return Result.<String>error("批量删除失败");
            }
        } catch (Exception e) {
            log.error("批量删除虚拟聚合池失败, ids: {}", ids, e);
            return Result.<String>error("批量删除失败: " + e.getMessage());
        }
    }

    /**
     * 获取虚拟聚合池的聚合状态
     */
    @GetMapping("/{id}/status")
    public Result<Map<String, Object>> getVirtualPoolStatus(@PathVariable Long id) {
        try {
            Map<String, Object> status = virtualPoolService.getVirtualPoolStatus(id);
            if (status == null) {
                return Result.notFound("虚拟聚合池不存在");
            }
            return Result.success(status);
        } catch (Exception e) {
            log.error("获取虚拟聚合池状态失败, id: {}", id, e);
            return Result.error("获取失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有虚拟聚合池的聚合状态
     */
    @GetMapping("/status/all")
    public Result<List<Map<String, Object>>> getAllVirtualPoolStatus() {
        try {
            List<Map<String, Object>> statusList = virtualPoolService.getAllVirtualPoolStatus();
            return Result.success(statusList);
        } catch (Exception e) {
            log.error("获取所有虚拟聚合池状态失败", e);
            return Result.error("获取失败: " + e.getMessage());
        }
    }

    /**
     * 获取虚拟聚合池的历史趋势数据
     */
    @GetMapping("/{id}/trend")
    public Result<List<Map<String, Object>>> getVirtualPoolTrend(
            @PathVariable Long id,
            @RequestParam(required = false) Long startTime,
            @RequestParam(required = false) Long endTime,
            @RequestParam(required = false) Integer interval) {
        
        try {
            List<Map<String, Object>> trendData = virtualPoolService.getVirtualPoolTrend(id, startTime, endTime, interval);
            if (trendData == null) {
                return Result.notFound("虚拟聚合池不存在");
            }
            return Result.success(trendData);
        } catch (Exception e) {
            log.error("获取虚拟聚合池趋势数据失败, id: {}", id, e);
            return Result.error("获取失败: " + e.getMessage());
        }
    }

    /**
     * 验证虚拟聚合池配置
     */
    @PostMapping("/validate")
    public Result<Map<String, Object>> validateVirtualPoolConfig(@RequestBody VirtualPoolDTO virtualPoolDTO) {
        try {
            Map<String, Object> validation = virtualPoolService.validateVirtualPoolConfig(virtualPoolDTO);
            return Result.success(validation);
        } catch (Exception e) {
            log.error("验证虚拟聚合池配置失败", e);
            return Result.error("验证失败: " + e.getMessage());
        }
    }

    /**
     * 获取可用的聚合策略列表
     */
    @GetMapping("/strategies")
    public Result<List<Map<String, String>>> getAvailableStrategies() {
        try {
            List<Map<String, String>> strategies = virtualPoolService.getAvailableStrategies();
            return Result.success(strategies);
        } catch (Exception e) {
            log.error("获取可用聚合策略列表失败", e);
            return Result.error("获取失败: " + e.getMessage());
        }
    }

    /**
     * 预览虚拟聚合池效果
     */
    @PostMapping("/preview")
    public Result<Map<String, Object>> previewVirtualPool(@RequestBody VirtualPoolDTO virtualPoolDTO) {
        try {
            Map<String, Object> preview = virtualPoolService.previewVirtualPool(virtualPoolDTO);
            return Result.success(preview);
        } catch (Exception e) {
            log.error("预览虚拟聚合池效果失败", e);
            return Result.error("预览失败: " + e.getMessage());
        }
    }

    /**
     * 检查虚拟聚合池名称是否可用
     */
    @GetMapping("/check-name")
    public Result<Boolean> checkVirtualPoolName(
            @RequestParam String name,
            @RequestParam(required = false) Long excludeId) {
        
        try {
            boolean available = virtualPoolService.isVirtualPoolNameAvailable(name, excludeId);
            return Result.success(available);
        } catch (Exception e) {
            log.error("检查虚拟聚合池名称可用性失败, name: {}", name, e);
            return Result.error("检查失败: " + e.getMessage());
        }
    }

    /**
     * 获取虚拟聚合池统计信息
     */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getVirtualPoolStatistics() {
        try {
            Map<String, Object> statistics = virtualPoolService.getVirtualPoolStatistics();
            return Result.success(statistics);
        } catch (Exception e) {
            log.error("获取虚拟聚合池统计信息失败", e);
            return Result.error("获取失败: " + e.getMessage());
        }
    }
}