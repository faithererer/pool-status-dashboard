package com.zjc.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zjc.dto.VirtualPoolDTO;
import com.zjc.entity.VirtualPool;

import java.util.List;
import java.util.Map;

/**
 * 虚拟聚合池服务接口
 * 
 * @author zjc
 * @since 2024-01-06
 */
public interface VirtualPoolService extends IService<VirtualPool> {
    
    /**
     * 分页查询虚拟聚合池列表
     * 
     * @param current 当前页
     * @param size 页大小
     * @param name 虚拟池名称(模糊查询)
     * @param strategy 聚合策略
     * @return 分页结果
     */
    Page<VirtualPoolDTO> getVirtualPoolPage(long current, long size, String name, String strategy);
    
    /**
     * 根据ID获取虚拟聚合池详情
     * 
     * @param id 虚拟池ID
     * @return 虚拟池详情
     */
    VirtualPoolDTO getVirtualPoolById(Long id);
    
    /**
     * 创建虚拟聚合池
     * 
     * @param virtualPoolDTO 虚拟池信息
     * @return 创建的虚拟池
     */
    VirtualPoolDTO createVirtualPool(VirtualPoolDTO virtualPoolDTO);
    
    /**
     * 更新虚拟聚合池
     * 
     * @param id 虚拟池ID
     * @param virtualPoolDTO 虚拟池信息
     * @return 更新的虚拟池
     */
    VirtualPoolDTO updateVirtualPool(VirtualPoolDTO virtualPoolDTO);
    
    /**
     * 删除虚拟聚合池
     * 
     * @param id 虚拟池ID
     * @return 是否删除成功
     */
    boolean deleteVirtualPool(Long id);
    
    /**
     * 批量删除虚拟聚合池
     * 
     * @param ids 虚拟池ID列表
     * @return 是否删除成功
     */
    boolean batchDeleteVirtualPools(List<Long> ids);
    
    /**
     * 启用/禁用虚拟聚合池
     * 
     * @param id 虚拟池ID
     * @param enabled 是否启用
     * @return 是否操作成功
     */
    boolean toggleVirtualPoolEnabled(Long id, Boolean enabled);
    
    /**
     * 获取所有启用的虚拟聚合池列表
     * 
     * @return 启用的虚拟池列表
     */
    List<VirtualPoolDTO> getAllVirtualPools();
    
    /**
     * 按显示顺序获取虚拟聚合池列表
     * 
     * @return 按显示顺序排序的虚拟池列表
     */
    List<VirtualPoolDTO> getVirtualPoolsByDisplayOrder();
    
    /**
     * 更新虚拟聚合池显示顺序
     * 
     * @param id 虚拟池ID
     * @param displayOrder 显示顺序
     * @return 是否更新成功
     */
    boolean updateVirtualPoolDisplayOrder(Long id, Integer displayOrder);
    
    /**
     * 批量更新虚拟聚合池显示顺序
     * 
     * @param orderMap 虚拟池ID和显示顺序的映射
     * @return 是否更新成功
     */
    boolean batchUpdateVirtualPoolDisplayOrder(List<VirtualPoolDTO> virtualPools);
    
    /**
     * 检查虚拟聚合池名称是否存在
     * 
     * @param name 虚拟池名称
     * @param excludeId 排除的ID(用于更新时检查)
     * @return 是否存在
     */
    boolean isVirtualPoolNameAvailable(String name, Long excludeId);
    
    /**
     * 计算虚拟聚合池的聚合状态
     * 
     * @param id 虚拟池ID
     * @return 聚合后的虚拟池状态
     */
    VirtualPoolDTO calculateVirtualPoolStatus(Long id);
    
    /**
     * 批量计算所有虚拟聚合池的聚合状态
     * 
     * @return 所有虚拟池的聚合状态列表
     */
    List<VirtualPoolDTO> calculateAllVirtualPoolStatus();
    
    /**
     * 验证虚拟聚合池配置是否有效
     * 
     * @param virtualPoolDTO 虚拟池配置
     * @return 验证结果消息，null表示验证通过
     */
    Map<String, Object> validateVirtualPoolConfig(VirtualPoolDTO virtualPoolDTO);
    
    /**
     * 获取虚拟聚合池的聚合状态
     *
     * @param id 虚拟池ID
     * @return 聚合状态
     */
    Map<String, Object> getVirtualPoolStatus(Long id);
    
    /**
     * 获取所有虚拟聚合池的聚合状态
     *
     * @return 所有虚拟池的聚合状态列表
     */
    List<Map<String, Object>> getAllVirtualPoolStatus();
    
    /**
     * 获取虚拟聚合池的历史趋势数据
     *
     * @param id 虚拟池ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param interval 时间间隔
     * @return 趋势数据
     */
    List<Map<String, Object>> getVirtualPoolTrend(Long id, Long startTime, Long endTime, Integer interval);
    
    /**
     * 获取可用的聚合策略列表
     *
     * @return 聚合策略列表
     */
    List<Map<String, String>> getAvailableStrategies();
    
    /**
     * 预览虚拟聚合池效果
     *
     * @param virtualPoolDTO 虚拟池配置
     * @return 预览结果
     */
    Map<String, Object> previewVirtualPool(VirtualPoolDTO virtualPoolDTO);
    
    /**
     * 获取虚拟聚合池统计信息
     *
     * @return 统计信息
     */
    Map<String, Object> getVirtualPoolStatistics();
    
    /**
     * 获取包含指定号池的虚拟聚合池列表
     * 
     * @param poolId 号池ID
     * @return 包含该号池的虚拟池列表
     */
    List<VirtualPoolDTO> getVirtualPoolsContainingPool(Long poolId);
}