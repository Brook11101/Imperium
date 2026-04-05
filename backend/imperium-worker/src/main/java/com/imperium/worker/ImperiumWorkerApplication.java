package com.imperium.worker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class ImperiumWorkerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ImperiumWorkerApplication.class, args);
    }
}
