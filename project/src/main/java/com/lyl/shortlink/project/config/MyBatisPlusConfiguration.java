package com.lyl.shortlink.project.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class MyBatisPlusConfiguration {

    /**
     * 分页插件
     */
    @Bean
    @ConditionalOnMissingBean(MybatisPlusInterceptor.class)
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        // MybatisPlusInterceptor是MyBatis Plus框架提供的一个拦截器，用于添加各种内部拦截器来扩展MyBatis的功能
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 将PaginationInnerInterceptor添加到MybatisPlusInterceptor中，这样，当执行SQL查询时，
        // MybatisPlusInterceptor就会使用PaginationInnerInterceptor来处理分页
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }

//    /**
//     * 元数据填充
//     */
//    @Bean
//    public MyMetaObjectHandler myMetaObjectHandler() {
//        return new MyMetaObjectHandler();
//    }
//
//    /**
//     * 自定义雪花算法 ID 生成器
//     */
//    @Bean
//    @Primary
//    public IdentifierGenerator idGenerator() {
//        return new CustomIdGenerator();
//    }
}
