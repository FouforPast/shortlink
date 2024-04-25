package com.lyl.shortlink.project;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.lyl.shortlink.project.dao.mapper")
public class ShortlinkProjectApplication {
    public static void main(String[] args) {
        SpringApplication.run(ShortlinkProjectApplication.class, args);
    }
}
