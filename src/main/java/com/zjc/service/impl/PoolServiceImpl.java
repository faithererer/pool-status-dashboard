package com.zjc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zjc.dto.PoolDTO;
import com.zjc.entity.Pool;
import com.zjc.mapper.PoolMapper;
import com.zjc.service.PoolService;
import com.zjc.util.BeanUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 号池服务实现类
 * 
 * @author zjc
 * @since 2024-01-06
 */
@Slf4j
@Service
public class PoolServiceImpl extends ServiceImpl<PoolMapper, Pool> implements PoolService {

    @Override
    public Page<PoolDTO> getPoolPage(long current, long size, String name, String displayStrategy, Boolean enabled) {
        LambdaQueryWrapper<Pool> queryWrapper = new LambdaQueryWrapper<>();

        // 添加查询条件
        if (StringUtils.hasText(name)) {
            // 使用嵌套条件，将OR逻辑限制在名称和描述的查询中
            queryWrapper.and(qw -> qw
                    .like(Pool::getName, name)
                    .or()
                    .like(Pool::getDescription, name)
            );
        }

        if (StringUtils.hasText(displayStrategy)) {
            queryWrapper.eq(Pool::getDisplayStrategy, displayStrategy);
        }

        if (enabled != null) {
            queryWrapper.eq(Pool::getEnabled, enabled);
        }

        // 按创建时间倒序
        queryWrapper.orderByDesc(Pool::getCreateTime);

        Page<Pool> poolPage = this.page(new Page<>(current, size), queryWrapper);

        // 转换为DTO
        Page<PoolDTO> dtoPage = new Page<>(current, size, poolPage.getTotal());
        List<PoolDTO> dtoList = poolPage.getRecords().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        dtoPage.setRecords(dtoList);

        return dtoPage;
    }

    @Override
    public PoolDTO getPoolById(Long id) {
        Pool pool = this.getById(id);
        return pool != null ? convertToDTO(pool) : null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PoolDTO createPool(PoolDTO poolDTO) {
        // 检查名称是否重复
        if (isPoolNameExists(poolDTO.getName(), null)) {
            throw new RuntimeException("号池名称已存在: " + poolDTO.getName());
        }
        
        Pool pool = convertToEntity(poolDTO);
        pool.setId(null); // 确保是新建
        
        // 设置默认值
        if (pool.getUpdateFrequency() == null) {
            pool.setUpdateFrequency(60);
        }
        if (pool.getDisplayStrategy() == null) {
            pool.setDisplayStrategy("public");
        }
        if (pool.getEnabled() == null) {
            pool.setEnabled(true);
        }
        
        boolean saved = this.save(pool);
        if (!saved) {
            throw new RuntimeException("保存号池失败");
        }
        
        log.info("创建号池成功: {}", pool.getName());
        return convertToDTO(pool);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PoolDTO updatePool(Long id, PoolDTO poolDTO) {
        Pool existingPool = this.getById(id);
        if (existingPool == null) {
            throw new RuntimeException("号池不存在: " + id);
        }
        
        // 检查名称是否重复（排除自己）
        if (isPoolNameExists(poolDTO.getName(), id)) {
            throw new RuntimeException("号池名称已存在: " + poolDTO.getName());
        }
        
        Pool updatePool = convertToEntity(poolDTO);
        updatePool.setId(id);
        
        // 保留原有的创建时间
        updatePool.setCreateTime(existingPool.getCreateTime());
        
        boolean updated = this.updateById(updatePool);
        if (!updated) {
            throw new RuntimeException("更新号池失败");
        }
        
        log.info("更新号池成功: {}", updatePool.getName());
        return convertToDTO(updatePool);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deletePool(Long id) {
        Pool pool = this.getById(id);
        if (pool == null) {
            throw new RuntimeException("号池不存在: " + id);
        }
        
        boolean deleted = this.removeById(id);
        if (deleted) {
            log.info("删除号池成功: {}", pool.getName());
        }
        
        return deleted;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deletePoolBatch(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return true;
        }
        
        boolean deleted = this.removeByIds(ids);
        if (deleted) {
            log.info("批量删除号池成功，数量: {}", ids.size());
        }
        
        return deleted;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean togglePoolEnabled(Long id, Boolean enabled) {
        LambdaUpdateWrapper<Pool> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Pool::getId, id)
                    .set(Pool::getEnabled, enabled);
        
        boolean updated = this.update(updateWrapper);
        if (updated) {
            log.info("切换号池启用状态成功: id={}, enabled={}", id, enabled);
        }
        
        return updated;
    }

    @Override
    public List<PoolDTO> getEnabledPools() {
        LambdaQueryWrapper<Pool> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Pool::getEnabled, true)
                   .orderByDesc(Pool::getCreateTime);
        
        List<Pool> pools = this.list(queryWrapper);
        return pools.stream()
                   .map(this::convertToDTO)
                   .collect(Collectors.toList());
    }

    @Override
    public List<PoolDTO> getPublicPools() {
        LambdaQueryWrapper<Pool> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Pool::getDisplayStrategy, "public")
                   .eq(Pool::getEnabled, true)
                   .orderByDesc(Pool::getCreateTime);
        
        List<Pool> pools = this.list(queryWrapper);
        return pools.stream()
                   .map(this::convertToDTO)
                   .collect(Collectors.toList());
    }

    @Override
    public List<PoolDTO> getPoolsByDisplayStrategy(String displayStrategy) {
        LambdaQueryWrapper<Pool> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Pool::getDisplayStrategy, displayStrategy)
                   .eq(Pool::getEnabled, true)
                   .orderByDesc(Pool::getCreateTime);
        
        List<Pool> pools = this.list(queryWrapper);
        return pools.stream()
                   .map(this::convertToDTO)
                   .collect(Collectors.toList());
    }

    @Override
    public boolean isPoolNameExists(String name, Long excludeId) {
        LambdaQueryWrapper<Pool> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Pool::getName, name);
        
        if (excludeId != null) {
            queryWrapper.ne(Pool::getId, excludeId);
        }
        
        return this.count(queryWrapper) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePoolLastStatus(Long id, String status, String errorMessage) {
        LambdaUpdateWrapper<Pool> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Pool::getId, id)
                    .set(Pool::getLastUpdateTime, System.currentTimeMillis())
                    .set(Pool::getLastUpdateStatus, status);
        
        if (StringUtils.hasText(errorMessage)) {
            updateWrapper.set(Pool::getLastErrorMessage, errorMessage);
        } else {
            updateWrapper.set(Pool::getLastErrorMessage, null);
        }
        
        this.update(updateWrapper);
    }

    /**
     * 将实体转换为DTO
     */
    private PoolDTO convertToDTO(Pool pool) {
        if (pool == null) {
            return null;
        }
        
        PoolDTO dto = new PoolDTO();
        org.springframework.beans.BeanUtils.copyProperties(pool, dto);
        
        // 转换JSON字段
        dto.setDataSourceConfig(BeanUtils.jsonToMap(pool.getDataSourceConfig()));
        dto.setDisplayFields(BeanUtils.jsonToBooleanMap(pool.getDisplayFields()));
        
        return dto;
    }

    /**
     * 将DTO转换为实体
     */
    private Pool convertToEntity(PoolDTO dto) {
        if (dto == null) {
            return null;
        }
        
        Pool pool = new Pool();
        org.springframework.beans.BeanUtils.copyProperties(dto, pool);
        
        // 转换JSON字段
        pool.setDataSourceConfig(BeanUtils.mapToJson(dto.getDataSourceConfig()));
        pool.setDisplayFields(BeanUtils.booleanMapToJson(dto.getDisplayFields()));
        
        return pool;
    }
}