package com.zjc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zjc.dto.PoolStatusDTO;
import com.zjc.dto.VirtualPoolDTO;
import com.zjc.entity.Pool;
import com.zjc.entity.VirtualPool;
import com.zjc.mapper.PoolMapper;
import com.zjc.mapper.VirtualPoolMapper;
import com.zjc.service.PoolStatusService;
import com.zjc.service.VirtualPoolService;
import com.zjc.util.BeanUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 虚拟聚合池服务实现类
 * 
 * @author zjc
 * @since 2024-01-06
 */
@Slf4j
@Service
public class VirtualPoolServiceImpl extends ServiceImpl<VirtualPoolMapper, VirtualPool> implements VirtualPoolService {

    @Autowired
    private PoolMapper poolMapper;
    
    @Autowired
    private PoolStatusService poolStatusService;

    @Override
    public Page<VirtualPoolDTO> getVirtualPoolPage(long current, long size, String name, String strategy) {
        LambdaQueryWrapper<VirtualPool> queryWrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(name)) {
            queryWrapper.like(VirtualPool::getName, name)
                       .or()
                       .like(VirtualPool::getDescription, name);
        }
        
        if (strategy != null && !strategy.trim().isEmpty()) {
            queryWrapper.eq(VirtualPool::getAggregateStrategy, strategy);
        }
        
        // 按显示顺序和创建时间排序
        queryWrapper.orderByAsc(VirtualPool::getDisplayOrder)
                   .orderByDesc(VirtualPool::getCreateTime);
        
        Page<VirtualPool> virtualPoolPage = this.page(new Page<>(current, size), queryWrapper);
        
        // 转换为DTO
        Page<VirtualPoolDTO> dtoPage = new Page<>(current, size, virtualPoolPage.getTotal());
        List<VirtualPoolDTO> dtoList = virtualPoolPage.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        dtoPage.setRecords(dtoList);
        
        return dtoPage;
    }

    @Override
    public VirtualPoolDTO getVirtualPoolById(Long id) {
        VirtualPool virtualPool = this.getById(id);
        return virtualPool != null ? convertToDTO(virtualPool) : null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public VirtualPoolDTO createVirtualPool(VirtualPoolDTO virtualPoolDTO) {
        // 检查名称是否重复
        if (!isVirtualPoolNameAvailable(virtualPoolDTO.getName(), null)) {
            throw new RuntimeException("虚拟聚合池名称已存在: " + virtualPoolDTO.getName());
        }
        
        // 验证配置
        Map<String, Object> validation = validateVirtualPoolConfig(virtualPoolDTO);
        if (!(Boolean) validation.get("valid")) {
            throw new RuntimeException((String) validation.get("message"));
        }
        
        VirtualPool virtualPool = convertToEntity(virtualPoolDTO);
        virtualPool.setId(null); // 确保是新建
        
        // 设置默认值
        if (virtualPool.getAggregateStrategy() == null) {
            virtualPool.setAggregateStrategy("sum");
        }
        if (virtualPool.getEnabled() == null) {
            virtualPool.setEnabled(true);
        }
        if (virtualPool.getDisplayOrder() == null) {
            virtualPool.setDisplayOrder(0);
        }
        
        boolean saved = this.save(virtualPool);
        if (!saved) {
            throw new RuntimeException("保存虚拟聚合池失败");
        }
        
        log.info("创建虚拟聚合池成功: {}", virtualPool.getName());
        return convertToDTO(virtualPool);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public VirtualPoolDTO updateVirtualPool(VirtualPoolDTO virtualPoolDTO) {
        Long id = virtualPoolDTO.getId();
        if (id == null) {
            throw new RuntimeException("更新时ID不能为空");
        }
        
        VirtualPool existingVirtualPool = this.getById(id);
        if (existingVirtualPool == null) {
            throw new RuntimeException("虚拟聚合池不存在: " + id);
        }
        
        // 检查名称是否重复（排除自己）
        if (!isVirtualPoolNameAvailable(virtualPoolDTO.getName(), id)) {
            throw new RuntimeException("虚拟聚合池名称已存在: " + virtualPoolDTO.getName());
        }
        
        // 验证配置
        Map<String, Object> validation = validateVirtualPoolConfig(virtualPoolDTO);
        if (!(Boolean) validation.get("valid")) {
            throw new RuntimeException((String) validation.get("message"));
        }
        
        VirtualPool updateVirtualPool = convertToEntity(virtualPoolDTO);
        updateVirtualPool.setId(id);
        
        // 保留原有的创建时间
        updateVirtualPool.setCreateTime(existingVirtualPool.getCreateTime());
        
        boolean updated = this.updateById(updateVirtualPool);
        if (!updated) {
            throw new RuntimeException("更新虚拟聚合池失败");
        }
        
        log.info("更新虚拟聚合池成功: {}", updateVirtualPool.getName());
        return convertToDTO(updateVirtualPool);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteVirtualPool(Long id) {
        VirtualPool virtualPool = this.getById(id);
        if (virtualPool == null) {
            throw new RuntimeException("虚拟聚合池不存在: " + id);
        }
        
        boolean deleted = this.removeById(id);
        if (deleted) {
            log.info("删除虚拟聚合池成功: {}", virtualPool.getName());
        }
        
        return deleted;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchDeleteVirtualPools(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return true;
        }
        
        boolean deleted = this.removeByIds(ids);
        if (deleted) {
            log.info("批量删除虚拟聚合池成功，数量: {}", ids.size());
        }
        
        return deleted;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean toggleVirtualPoolEnabled(Long id, Boolean enabled) {
        LambdaUpdateWrapper<VirtualPool> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(VirtualPool::getId, id)
                    .set(VirtualPool::getEnabled, enabled);
        
        boolean updated = this.update(updateWrapper);
        if (updated) {
            log.info("切换虚拟聚合池启用状态成功: id={}, enabled={}", id, enabled);
        }
        
        return updated;
    }

    @Override
    public List<VirtualPoolDTO> getAllVirtualPools() {
        LambdaQueryWrapper<VirtualPool> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(VirtualPool::getEnabled, true)
                   .orderByAsc(VirtualPool::getDisplayOrder)
                   .orderByDesc(VirtualPool::getCreateTime);
        
        List<VirtualPool> virtualPools = this.list(queryWrapper);
        return virtualPools.stream()
                          .map(this::convertToDTO)
                          .collect(Collectors.toList());
    }

    @Override
    public List<VirtualPoolDTO> getVirtualPoolsByDisplayOrder() {
        LambdaQueryWrapper<VirtualPool> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(VirtualPool::getEnabled, true)
                   .orderByAsc(VirtualPool::getDisplayOrder)
                   .orderByDesc(VirtualPool::getCreateTime);
        
        List<VirtualPool> virtualPools = this.list(queryWrapper);
        return virtualPools.stream()
                          .map(this::convertToDTO)
                          .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateVirtualPoolDisplayOrder(Long id, Integer displayOrder) {
        LambdaUpdateWrapper<VirtualPool> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(VirtualPool::getId, id)
                    .set(VirtualPool::getDisplayOrder, displayOrder);
        
        return this.update(updateWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchUpdateVirtualPoolDisplayOrder(List<VirtualPoolDTO> virtualPools) {
        if (virtualPools == null || virtualPools.isEmpty()) {
            return true;
        }
        
        for (VirtualPoolDTO dto : virtualPools) {
            if (dto.getId() != null && dto.getDisplayOrder() != null) {
                updateVirtualPoolDisplayOrder(dto.getId(), dto.getDisplayOrder());
            }
        }
        
        return true;
    }

    @Override
    public boolean isVirtualPoolNameAvailable(String name, Long excludeId) {
        LambdaQueryWrapper<VirtualPool> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(VirtualPool::getName, name);
        
        if (excludeId != null) {
            queryWrapper.ne(VirtualPool::getId, excludeId);
        }
        
        return this.count(queryWrapper) == 0;
    }

    @Override
    public VirtualPoolDTO calculateVirtualPoolStatus(Long id) {
        VirtualPoolDTO virtualPool = getVirtualPoolById(id);
        if (virtualPool == null || virtualPool.getPoolIds() == null || virtualPool.getPoolIds().isEmpty()) {
            return virtualPool;
        }
        
        // 获取聚合号池的最新状态
        List<PoolStatusDTO> poolStatuses = poolStatusService.getLatestPoolStatusByIds(virtualPool.getPoolIds());
        
        if (poolStatuses.isEmpty()) {
            return virtualPool;
        }
        
        // 根据聚合策略计算聚合值
        String strategy = virtualPool.getAggregateStrategy();
        
        if ("sum".equals(strategy)) {
            // 求和策略
            virtualPool.setAggregatedValidCount(
                poolStatuses.stream().mapToInt(s -> s.getValidCount() != null ? s.getValidCount() : 0).sum()
            );
            virtualPool.setAggregatedInvalidCount(
                poolStatuses.stream().mapToInt(s -> s.getInvalidCount() != null ? s.getInvalidCount() : 0).sum()
            );
            virtualPool.setAggregatedCoolingCount(
                poolStatuses.stream().mapToInt(s -> s.getCoolingCount() != null ? s.getCoolingCount() : 0).sum()
            );
            virtualPool.setAggregatedTotalCount(
                poolStatuses.stream().mapToInt(s -> s.getTotalCount() != null ? s.getTotalCount() : 0).sum()
            );
            
        } else if ("average".equals(strategy)) {
            // 平均值策略
            virtualPool.setAggregatedValidCount(
                (int) poolStatuses.stream().mapToInt(s -> s.getValidCount() != null ? s.getValidCount() : 0).average().orElse(0)
            );
            virtualPool.setAggregatedInvalidCount(
                (int) poolStatuses.stream().mapToInt(s -> s.getInvalidCount() != null ? s.getInvalidCount() : 0).average().orElse(0)
            );
            virtualPool.setAggregatedCoolingCount(
                (int) poolStatuses.stream().mapToInt(s -> s.getCoolingCount() != null ? s.getCoolingCount() : 0).average().orElse(0)
            );
            virtualPool.setAggregatedTotalCount(
                (int) poolStatuses.stream().mapToInt(s -> s.getTotalCount() != null ? s.getTotalCount() : 0).average().orElse(0)
            );
            
        } else if ("weighted".equals(strategy)) {
            // 加权平均策略（暂时使用简单平均，后续可扩展权重配置）
            virtualPool.setAggregatedValidCount(
                (int) poolStatuses.stream().mapToInt(s -> s.getValidCount() != null ? s.getValidCount() : 0).average().orElse(0)
            );
            virtualPool.setAggregatedInvalidCount(
                (int) poolStatuses.stream().mapToInt(s -> s.getInvalidCount() != null ? s.getInvalidCount() : 0).average().orElse(0)
            );
            virtualPool.setAggregatedCoolingCount(
                (int) poolStatuses.stream().mapToInt(s -> s.getCoolingCount() != null ? s.getCoolingCount() : 0).average().orElse(0)
            );
            virtualPool.setAggregatedTotalCount(
                (int) poolStatuses.stream().mapToInt(s -> s.getTotalCount() != null ? s.getTotalCount() : 0).average().orElse(0)
            );
        }
        
        // 计算聚合压力值
        if (virtualPool.getAggregatedTotalCount() != null && virtualPool.getAggregatedTotalCount() > 0 
            && virtualPool.getAggregatedValidCount() != null) {
            BigDecimal pressure = BigDecimal.valueOf(virtualPool.getAggregatedValidCount())
                    .divide(BigDecimal.valueOf(virtualPool.getAggregatedTotalCount()), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            virtualPool.setAggregatedPressure(pressure);
        }
        
        return virtualPool;
    }

    @Override
    public List<VirtualPoolDTO> calculateAllVirtualPoolStatus() {
        List<VirtualPoolDTO> enabledVirtualPools = getAllVirtualPools();
        
        return enabledVirtualPools.stream()
                .map(vp -> calculateVirtualPoolStatus(vp.getId()))
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getVirtualPoolStatus(Long id) {
        VirtualPoolDTO virtualPool = calculateVirtualPoolStatus(id);
        if (virtualPool == null) {
            return null;
        }
        
        Map<String, Object> status = new java.util.HashMap<>();
        status.put("id", virtualPool.getId());
        status.put("name", virtualPool.getName());
        status.put("validCount", virtualPool.getAggregatedValidCount());
        status.put("invalidCount", virtualPool.getAggregatedInvalidCount());
        status.put("coolingCount", virtualPool.getAggregatedCoolingCount());
        status.put("totalCount", virtualPool.getAggregatedTotalCount());
        status.put("pressure", virtualPool.getAggregatedPressure());
        status.put("strategy", virtualPool.getAggregateStrategy());
        
        return status;
    }

    @Override
    public List<Map<String, Object>> getAllVirtualPoolStatus() {
        List<VirtualPoolDTO> virtualPools = calculateAllVirtualPoolStatus();
        
        return virtualPools.stream()
                .map(vp -> {
                    Map<String, Object> status = new java.util.HashMap<>();
                    status.put("id", vp.getId());
                    status.put("name", vp.getName());
                    status.put("validCount", vp.getAggregatedValidCount());
                    status.put("invalidCount", vp.getAggregatedInvalidCount());
                    status.put("coolingCount", vp.getAggregatedCoolingCount());
                    status.put("totalCount", vp.getAggregatedTotalCount());
                    status.put("pressure", vp.getAggregatedPressure());
                    status.put("strategy", vp.getAggregateStrategy());
                    return status;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getVirtualPoolTrend(Long id, Long startTime, Long endTime, Integer interval) {
        // 暂时返回空列表，后续可以实现历史数据查询
        return new ArrayList<>();
    }

    @Override
    public List<Map<String, String>> getAvailableStrategies() {
        List<Map<String, String>> strategies = new ArrayList<>();
        
        Map<String, String> sumStrategy = new java.util.HashMap<>();
        sumStrategy.put("value", "sum");
        sumStrategy.put("label", "求和");
        sumStrategy.put("description", "将所有号池的数值相加");
        strategies.add(sumStrategy);
        
        Map<String, String> avgStrategy = new java.util.HashMap<>();
        avgStrategy.put("value", "average");
        avgStrategy.put("label", "平均值");
        avgStrategy.put("description", "计算所有号池的平均值");
        strategies.add(avgStrategy);
        
        Map<String, String> weightedStrategy = new java.util.HashMap<>();
        weightedStrategy.put("value", "weighted");
        weightedStrategy.put("label", "加权平均");
        weightedStrategy.put("description", "根据权重配置计算加权平均值");
        strategies.add(weightedStrategy);
        
        return strategies;
    }

    @Override
    public Map<String, Object> previewVirtualPool(VirtualPoolDTO virtualPoolDTO) {
        Map<String, Object> preview = new java.util.HashMap<>();
        
        if (virtualPoolDTO.getPoolIds() == null || virtualPoolDTO.getPoolIds().isEmpty()) {
            preview.put("valid", false);
            preview.put("message", "请选择要聚合的号池");
            return preview;
        }
        
        // 获取聚合号池的最新状态进行预览
        List<PoolStatusDTO> poolStatuses = poolStatusService.getLatestPoolStatusByIds(virtualPoolDTO.getPoolIds());
        
        if (poolStatuses.isEmpty()) {
            preview.put("valid", false);
            preview.put("message", "无法获取号池状态数据");
            return preview;
        }
        
        // 模拟聚合计算
        String strategy = virtualPoolDTO.getAggregateStrategy();
        int validCount = 0;
        int invalidCount = 0;
        int coolingCount = 0;
        int totalCount = 0;
        
        if ("sum".equals(strategy)) {
            validCount = poolStatuses.stream().mapToInt(s -> s.getValidCount() != null ? s.getValidCount() : 0).sum();
            invalidCount = poolStatuses.stream().mapToInt(s -> s.getInvalidCount() != null ? s.getInvalidCount() : 0).sum();
            coolingCount = poolStatuses.stream().mapToInt(s -> s.getCoolingCount() != null ? s.getCoolingCount() : 0).sum();
            totalCount = poolStatuses.stream().mapToInt(s -> s.getTotalCount() != null ? s.getTotalCount() : 0).sum();
        } else if ("average".equals(strategy)) {
            validCount = (int) poolStatuses.stream().mapToInt(s -> s.getValidCount() != null ? s.getValidCount() : 0).average().orElse(0);
            invalidCount = (int) poolStatuses.stream().mapToInt(s -> s.getInvalidCount() != null ? s.getInvalidCount() : 0).average().orElse(0);
            coolingCount = (int) poolStatuses.stream().mapToInt(s -> s.getCoolingCount() != null ? s.getCoolingCount() : 0).average().orElse(0);
            totalCount = (int) poolStatuses.stream().mapToInt(s -> s.getTotalCount() != null ? s.getTotalCount() : 0).average().orElse(0);
        }
        
        BigDecimal pressure = BigDecimal.ZERO;
        if (totalCount > 0) {
            pressure = BigDecimal.valueOf(validCount)
                    .divide(BigDecimal.valueOf(totalCount), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }
        
        preview.put("valid", true);
        preview.put("validCount", validCount);
        preview.put("invalidCount", invalidCount);
        preview.put("coolingCount", coolingCount);
        preview.put("totalCount", totalCount);
        preview.put("pressure", pressure);
        preview.put("poolCount", poolStatuses.size());
        
        return preview;
    }

    @Override
    public Map<String, Object> getVirtualPoolStatistics() {
        Map<String, Object> statistics = new java.util.HashMap<>();
        
        // 统计虚拟池总数
        long totalCount = this.count();
        statistics.put("totalCount", totalCount);
        
        // 统计启用的虚拟池数量
        LambdaQueryWrapper<VirtualPool> enabledQuery = new LambdaQueryWrapper<>();
        enabledQuery.eq(VirtualPool::getEnabled, true);
        long enabledCount = this.count(enabledQuery);
        statistics.put("enabledCount", enabledCount);
        
        // 统计各种聚合策略的使用情况
        Map<String, Long> strategyStats = new java.util.HashMap<>();
        
        LambdaQueryWrapper<VirtualPool> sumQuery = new LambdaQueryWrapper<>();
        sumQuery.eq(VirtualPool::getAggregateStrategy, "sum");
        strategyStats.put("sum", this.count(sumQuery));
        
        LambdaQueryWrapper<VirtualPool> avgQuery = new LambdaQueryWrapper<>();
        avgQuery.eq(VirtualPool::getAggregateStrategy, "average");
        strategyStats.put("average", this.count(avgQuery));
        
        LambdaQueryWrapper<VirtualPool> weightedQuery = new LambdaQueryWrapper<>();
        weightedQuery.eq(VirtualPool::getAggregateStrategy, "weighted");
        strategyStats.put("weighted", this.count(weightedQuery));
        
        statistics.put("strategyStats", strategyStats);
        
        return statistics;
    }

    @Override
    public Map<String, Object> validateVirtualPoolConfig(VirtualPoolDTO virtualPoolDTO) {
        Map<String, Object> result = new java.util.HashMap<>();
        
        if (virtualPoolDTO == null) {
            result.put("valid", false);
            result.put("message", "虚拟聚合池配置不能为空");
            return result;
        }
        
        if (!StringUtils.hasText(virtualPoolDTO.getName())) {
            result.put("valid", false);
            result.put("message", "虚拟聚合池名称不能为空");
            return result;
        }
        
        if (virtualPoolDTO.getPoolIds() == null || virtualPoolDTO.getPoolIds().isEmpty()) {
            result.put("valid", false);
            result.put("message", "必须选择至少一个号池进行聚合");
            return result;
        }
        
        // 检查聚合的号池是否存在且启用
        for (Long poolId : virtualPoolDTO.getPoolIds()) {
            Pool pool = poolMapper.selectById(poolId);
            if (pool == null) {
                result.put("valid", false);
                result.put("message", "号池不存在: " + poolId);
                return result;
            }
            if (!pool.getEnabled()) {
                result.put("valid", false);
                result.put("message", "号池未启用: " + pool.getName());
                return result;
            }
        }
        
        // 检查聚合策略
        String strategy = virtualPoolDTO.getAggregateStrategy();
        if (StringUtils.hasText(strategy) &&
            !("sum".equals(strategy) || "average".equals(strategy) || "weighted".equals(strategy))) {
            result.put("valid", false);
            result.put("message", "不支持的聚合策略: " + strategy);
            return result;
        }
        
        result.put("valid", true);
        result.put("message", "验证通过");
        return result;
    }

    @Override
    public List<VirtualPoolDTO> getVirtualPoolsContainingPool(Long poolId) {
        LambdaQueryWrapper<VirtualPool> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(VirtualPool::getEnabled, true);
        
        List<VirtualPool> allVirtualPools = this.list(queryWrapper);
        
        return allVirtualPools.stream()
                .filter(vp -> {
                    List<Long> poolIds = BeanUtils.jsonToLongList(vp.getPoolIds());
                    return poolIds.contains(poolId);
                })
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 将实体转换为DTO
     */
    private VirtualPoolDTO convertToDTO(VirtualPool virtualPool) {
        if (virtualPool == null) {
            return null;
        }
        
        VirtualPoolDTO dto = new VirtualPoolDTO();
        org.springframework.beans.BeanUtils.copyProperties(virtualPool, dto);
        
        // 转换JSON字段
        dto.setPoolIds(BeanUtils.jsonToLongList(virtualPool.getPoolIds()));
        dto.setWeightConfig(BeanUtils.jsonToMap(virtualPool.getWeightConfig()));
        
        // 获取号池名称列表
        if (dto.getPoolIds() != null && !dto.getPoolIds().isEmpty()) {
            List<String> poolNames = new ArrayList<>();
            for (Long poolId : dto.getPoolIds()) {
                Pool pool = poolMapper.selectById(poolId);
                if (pool != null) {
                    poolNames.add(pool.getName());
                }
            }
            dto.setPoolNames(poolNames);
        }
        
        return dto;
    }

    /**
     * 将DTO转换为实体
     */
    private VirtualPool convertToEntity(VirtualPoolDTO dto) {
        if (dto == null) {
            return null;
        }
        
        VirtualPool virtualPool = new VirtualPool();
        org.springframework.beans.BeanUtils.copyProperties(dto, virtualPool);
        
        // 转换JSON字段
        virtualPool.setPoolIds(BeanUtils.longListToJson(dto.getPoolIds()));
        virtualPool.setWeightConfig(BeanUtils.mapToJson(dto.getWeightConfig()));
        
        return virtualPool;
    }
}