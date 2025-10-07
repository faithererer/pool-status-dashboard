package com.zjc.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zjc.entity.VirtualPool;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 虚拟聚合池Mapper接口
 * 
 * @author zjc
 * @version 1.0.0
 */
@Mapper
public interface VirtualPoolMapper extends BaseMapper<VirtualPool> {

    /**
     * 查询启用的虚拟池列表，按显示顺序排序
     * 
     * @return 启用的虚拟池列表
     */
    @Select("SELECT * FROM virtual_pool WHERE enabled = 1 AND deleted = 0 ORDER BY display_order ASC, create_time DESC")
    List<VirtualPool> selectEnabledPools();

    /**
     * 根据名称模糊查询虚拟池
     * 
     * @param name 虚拟池名称
     * @return 虚拟池列表
     */
    @Select("SELECT * FROM virtual_pool WHERE name LIKE CONCAT('%', #{name}, '%') AND deleted = 0 ORDER BY display_order ASC, create_time DESC")
    List<VirtualPool> selectByNameLike(String name);

    /**
     * 获取最大显示顺序
     * 
     * @return 最大显示顺序
     */
    @Select("SELECT COALESCE(MAX(display_order), 0) FROM virtual_pool WHERE deleted = 0")
    Integer selectMaxDisplayOrder();
}