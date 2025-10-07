package com.zjc.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis Plus配置类
 * 
 * @author zjc
 * @version 1.0.0
 */
@Configuration
@MapperScan("com.zjc.mapper")
public class MyBatisPlusConfig {

    /**
     * MyBatis Plus拦截器配置
     * 添加分页插件
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        
        // 分页插件
        PaginationInnerInterceptor paginationInterceptor = new PaginationInnerInterceptor(DbType.SQLITE);
        paginationInterceptor.setMaxLimit(1000L); // 最大分页限制
        paginationInterceptor.setOverflow(false); // 溢出总页数后是否进行处理
        
        interceptor.addInnerInterceptor(paginationInterceptor);
        
        return interceptor;
    }
}