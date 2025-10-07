package com.zjc.health;

import com.zjc.service.PoolService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

/**
 * 数据库健康检查指示器
 * 
 * @author zjc
 * @since 2024-01-06
 */
@Slf4j
@Component
public class DatabaseHealthIndicator implements HealthIndicator {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private PoolService poolService;

    @Override
    public Health health() {
        try {
            Map<String, Object> details = new HashMap<>();
            
            // 检查数据库连接
            try (Connection connection = dataSource.getConnection()) {
                details.put("database", "SQLite");
                details.put("url", connection.getMetaData().getURL());
                details.put("driver", connection.getMetaData().getDriverName());
                details.put("version", connection.getMetaData().getDriverVersion());
                
                // 检查数据库是否可读写
                try (PreparedStatement stmt = connection.prepareStatement("SELECT 1")) {
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            details.put("readable", true);
                        }
                    }
                }
                
                // 检查主要表是否存在
                boolean poolTableExists = checkTableExists(connection, "pool");
                boolean poolStatusTableExists = checkTableExists(connection, "pool_status");
                boolean virtualPoolTableExists = checkTableExists(connection, "virtual_pool");
                
                details.put("poolTableExists", poolTableExists);
                details.put("poolStatusTableExists", poolStatusTableExists);
                details.put("virtualPoolTableExists", virtualPoolTableExists);
                
                // 获取表记录数量
                if (poolTableExists) {
                    int poolCount = getTableRowCount(connection, "pool");
                    details.put("poolCount", poolCount);
                }
                
                if (poolStatusTableExists) {
                    int statusCount = getTableRowCount(connection, "pool_status");
                    details.put("statusRecordCount", statusCount);
                }
                
                if (virtualPoolTableExists) {
                    int virtualPoolCount = getTableRowCount(connection, "virtual_pool");
                    details.put("virtualPoolCount", virtualPoolCount);
                }
                
                // 如果所有核心表都存在，返回UP状态
                if (poolTableExists && poolStatusTableExists && virtualPoolTableExists) {
                    return Health.up()
                        .withDetails(details)
                        .build();
                } else {
                    return Health.status("WARNING")
                        .withDetail("message", "部分数据表不存在")
                        .withDetails(details)
                        .build();
                }
                
            }
            
        } catch (Exception e) {
            log.error("数据库健康检查失败", e);
            return Health.down()
                .withException(e)
                .build();
        }
    }

    /**
     * 检查表是否存在
     */
    private boolean checkTableExists(Connection connection, String tableName) {
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT name FROM sqlite_master WHERE type='table' AND name=?")) {
            stmt.setString(1, tableName);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            log.error("检查表是否存在失败, tableName: {}", tableName, e);
            return false;
        }
    }

    /**
     * 获取表记录数量
     */
    private int getTableRowCount(Connection connection, String tableName) {
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT COUNT(*) FROM " + tableName)) {
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (Exception e) {
            log.error("获取表记录数量失败, tableName: {}", tableName, e);
        }
        return 0;
    }
}