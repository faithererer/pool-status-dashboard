package com.zjc.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * 数据库初始化器
 * 在应用启动时执行数据库初始化脚本
 * 
 * @author zjc
 * @since 2024-01-06
 */
@Slf4j
@Component
public class DatabaseInitializer implements CommandLineRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        log.info("开始初始化数据库...");
        
        try {
            // 执行数据库初始化脚本
            executeSqlScript("sql/schema.sql");
            log.info("数据库初始化完成");
        } catch (Exception e) {
            log.error("数据库初始化失败", e);
            throw e;
        }
    }

    /**
     * 执行SQL脚本文件
     * 
     * @param scriptPath 脚本文件路径
     */
    private void executeSqlScript(String scriptPath) throws Exception {
        ClassPathResource resource = new ClassPathResource(scriptPath);
        
        if (!resource.exists()) {
            log.warn("SQL脚本文件不存在: {}", scriptPath);
            return;
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            
            StringBuilder sqlBuilder = new StringBuilder();
            String line;
            
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                
                // 跳过注释和空行
                if (line.isEmpty() || line.startsWith("--")) {
                    continue;
                }
                
                sqlBuilder.append(line).append(" ");
                
                // 如果遇到分号，执行SQL语句
                if (line.endsWith(";")) {
                    String sql = sqlBuilder.toString().trim();
                    if (!sql.isEmpty()) {
                        try {
                            jdbcTemplate.execute(sql);
                            log.debug("执行SQL: {}", sql.substring(0, Math.min(sql.length(), 100)) + "...");
                        } catch (Exception e) {
                            log.warn("执行SQL失败: {}, 错误: {}", sql, e.getMessage());
                            // 继续执行其他SQL，不中断初始化过程
                        }
                    }
                    sqlBuilder.setLength(0);
                }
            }
            
            // 处理最后一条可能没有分号的SQL
            String remainingSql = sqlBuilder.toString().trim();
            if (!remainingSql.isEmpty()) {
                try {
                    jdbcTemplate.execute(remainingSql);
                    log.debug("执行SQL: {}", remainingSql.substring(0, Math.min(remainingSql.length(), 100)) + "...");
                } catch (Exception e) {
                    log.warn("执行SQL失败: {}, 错误: {}", remainingSql, e.getMessage());
                }
            }
            
        } catch (Exception e) {
            log.error("读取SQL脚本文件失败: {}", scriptPath, e);
            throw e;
        }
    }
}