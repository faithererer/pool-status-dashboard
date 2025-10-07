package com.zjc.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zjc.entity.Pool;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 号池Mapper接口
 * 
 * @author zjc
 * @version 1.0.0
 */
@Mapper
public interface PoolMapper extends BaseMapper<Pool> {

    /**
     * 查询启用的号池列表
     * 
     * @return 启用的号池列表
     */
    @Select("SELECT * FROM pool WHERE enabled = 1 AND deleted = 0 ORDER BY create_time DESC")
    List<Pool> selectEnabledPools();

    /**
     * 根据显示策略查询号池
     * 
     * @param displayStrategy 显示策略
     * @return 号池列表
     */
    @Select("SELECT * FROM pool WHERE display_strategy = #{displayStrategy} AND deleted = 0 ORDER BY create_time DESC")
    List<Pool> selectByDisplayStrategy(String displayStrategy);

    /**
     * 根据名称模糊查询号池
     * 
     * @param name 号池名称
     * @return 号池列表
     */
    @Select("SELECT * FROM pool WHERE name LIKE CONCAT('%', #{name}, '%') AND deleted = 0 ORDER BY create_time DESC")
    List<Pool> selectByNameLike(String name);
}