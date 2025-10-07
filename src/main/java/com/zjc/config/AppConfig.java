package com.zjc.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 应用配置类
 * 
 * @author zjc
 * @version 1.0.0
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "app")
public class AppConfig {

    /**
     * 数据保留天数
     */
    private int dataRetentionDays = 30;

    /**
     * 数据采集配置
     */
    private Collection collection = new Collection();

    /**
     * 缓存配置
     */
    private Cache cache = new Cache();

    @Data
    public static class Collection {
        /**
         * 默认采集频率（秒）
         */
        private int defaultFrequency = 60;

        /**
         * 采集超时时间（秒）
         */
        private int timeout = 30;

        /**
         * 最大并发采集数
         */
        private int maxConcurrent = 10;
    }

    @Data
    public static class Cache {
        /**
         * 缓存TTL（秒）
         */
        private int ttlSeconds = 300;

        /**
         * 缓存最大大小
         */
        private int maxSize = 1000;
    }
}