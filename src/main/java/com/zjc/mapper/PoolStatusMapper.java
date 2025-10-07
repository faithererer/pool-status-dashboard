package com.zjc.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zjc.entity.PoolStatus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 号池状态Mapper接口
 * 
 * @author zjc
 * @version 1.0.0
 */
@Mapper
public interface PoolStatusMapper extends BaseMapper<PoolStatus> {

    /**
     * 获取指定号池的最新状态
     * 
     * @param poolId 号池ID
     * @return 最新状态
     */
    @Select("SELECT * FROM pool_status WHERE pool_id = #{poolId} AND deleted = 0 ORDER BY record_time DESC LIMIT 1")
    PoolStatus selectLatestByPoolId(@Param("poolId") Long poolId);

    /**
     * 获取指定号池在指定时间范围内的状态历史
     * 
     * @param poolId 号池ID
     * @param startTime 开始时间戳
     * @param endTime 结束时间戳
     * @return 状态历史列表
     */
    @Select("SELECT * FROM pool_status WHERE pool_id = #{poolId} AND record_time BETWEEN #{startTime} AND #{endTime} AND deleted = 0 ORDER BY record_time ASC")
    List<PoolStatus> selectHistoryByPoolId(@Param("poolId") Long poolId, 
                                          @Param("startTime") Long startTime, 
                                          @Param("endTime") Long endTime);

    /**
     * 获取所有号池的最新状态
     * 
     * @return 最新状态列表
     */
    @Select("SELECT ps.* FROM pool_status ps " +
            "INNER JOIN (SELECT pool_id, MAX(record_time) as max_time FROM pool_status WHERE deleted = 0 GROUP BY pool_id) latest " +
            "ON ps.pool_id = latest.pool_id AND ps.record_time = latest.max_time " +
            "WHERE ps.deleted = 0")
    List<PoolStatus> selectAllLatestStatus();

    /**
     * 删除指定时间之前的历史数据
     * 
     * @param beforeTime 时间戳
     * @return 删除的记录数
     */
    @Select("UPDATE pool_status SET deleted = 1 WHERE record_time < #{beforeTime} AND deleted = 0")
    int deleteHistoryBefore(@Param("beforeTime") Long beforeTime);

    /**
     * 获取指定号池列表的最新状态
     * 
     * @param poolIds 号池ID列表
     * @return 最新状态列表
     */
    @Select("<script>" +
            "SELECT ps.* FROM pool_status ps " +
            "INNER JOIN (SELECT pool_id, MAX(record_time) as max_time FROM pool_status " +
            "WHERE pool_id IN " +
            "<foreach collection='poolIds' item='poolId' open='(' separator=',' close=')'>" +
            "#{poolId}" +
            "</foreach>" +
            " AND deleted = 0 GROUP BY pool_id) latest " +
            "ON ps.pool_id = latest.pool_id AND ps.record_time = latest.max_time " +
            "WHERE ps.deleted = 0" +
            "</script>")
    List<PoolStatus> selectLatestByPoolIds(@Param("poolIds") List<Long> poolIds);
}