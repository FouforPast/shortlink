package com.lyl.shortlink.aggregation;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;


@EnableDiscoveryClient
@MapperScan(value = {
        "com.lyl.shortlink.project.dao.mapper",
        "com.lyl.shortlink.admin.dao.mapper"
})
@SpringBootApplication(scanBasePackages = {
        "com.lyl.shortlink.admin",
        "com.lyl.shortlink.project"
})
public class ShortlinkAggregationApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShortlinkAggregationApplication.class, args);
    }

}
