package com.imperium.worker;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication(scanBasePackages = {"com.imperium.worker", "com.imperium.domain"})
@MapperScan("com.imperium.domain.mapper")
public class ImperiumWorkerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ImperiumWorkerApplication.class, args);
    }
}
