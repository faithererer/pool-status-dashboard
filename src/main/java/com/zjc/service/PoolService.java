package com.zjc.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zjc.dto.PoolDTO;
import com.zjc.entity.Pool;

import java.util.List;

/**
 * 号池服务接口
 * 
 * @author zjc
 * @since 2024-01-06
 */
public interface PoolService extends IService<Pool> {
    
    /**
     * 分页查询号池列表
     * 
     * @param current 当前页
     * @param size 页大小
     * @param name 号池名称(模糊查询)
     * @param displayStrategy 显示策略
     * @param enabled 是否启用
     * @return 分页结果
     */
    Page<PoolDTO> getPoolPage(long current, long size, String name, String displayStrategy, Boolean enabled);
    
    /**
     * 根据ID获取号池详情
     * 
     * @param id 号池ID
     * @return 号池详情
     */
    PoolDTO getPoolById(Long id);
    
    /**
     * 创建号池
     * 
     * @param poolDTO 号池信息
     * @return 创建的号池
     */
    PoolDTO createPool(PoolDTO poolDTO);
    
    /**
     * 更新号池
     * 
     * @param id 号池ID
     * @param poolDTO 号池信息
     * @return 更新的号池
     */
    PoolDTO updatePool(Long id, PoolDTO poolDTO);
    
    /**
     * 删除号池
     * 
     * @param id 号池ID
     * @return 是否删除成功
     */
    boolean deletePool(Long id);
    
    /**
     * 批量删除号池
     * 
     * @param ids 号池ID列表
     * @return 是否删除成功
     */
    boolean deletePoolBatch(List<Long> ids);
    
    /**
     * 启用/禁用号池
     * 
     * @param id 号池ID
     * @param enabled 是否启用
     * @return 是否操作成功
     */
    boolean togglePoolEnabled(Long id, Boolean enabled);
    
    /**
     * 获取所有启用的号池列表
     * 
     * @return 启用的号池列表
     */
    List<PoolDTO> getEnabledPools();
    
    /**
     * 获取公开显示的号池列表
     * 
     * @return 公开显示的号池列表
     */
    List<PoolDTO> getPublicPools();
    
    /**
     * 根据显示策略获取号池列表
     * 
     * @param displayStrategy 显示策略
     * @return 号池列表
     */
    List<PoolDTO> getPoolsByDisplayStrategy(String displayStrategy);
    
    /**
     * 检查号池名称是否存在
     * 
     * @param name 号池名称
     * @param excludeId 排除的ID(用于更新时检查)
     * @return 是否存在
     */
    boolean isPoolNameExists(String name, Long excludeId);
    
    /**
     * 更新号池最后更新状态
     * 
     * @param id 号池ID
     * @param status 更新状态
     * @param errorMessage 错误信息
     */
    void updatePoolLastStatus(Long id, String status, String errorMessage);
}