package com.imperium.api;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.imperium.api", "com.imperium.domain"})
@MapperScan("com.imperium.domain.mapper")
public class ImperiumApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ImperiumApiApplication.class, args);
    }
}
