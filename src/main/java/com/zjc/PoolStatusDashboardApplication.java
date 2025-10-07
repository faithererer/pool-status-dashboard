package com.zjc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 号池监控面板主启动类
 * 
 * @author zjc
 * @version 1.0.0
 */
@SpringBootApplication
@EnableScheduling
public class PoolStatusDashboardApplication {

    public static void main(String[] args) {
        SpringApplication.run(PoolStatusDashboardApplication.class, args);
    }
}