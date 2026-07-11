package com.angli.online;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.angli.online.mapper")
public class AngliOnlineApplication {

    public static void main(String[] args) {
        SpringApplication.run(AngliOnlineApplication.class, args);
    }

}