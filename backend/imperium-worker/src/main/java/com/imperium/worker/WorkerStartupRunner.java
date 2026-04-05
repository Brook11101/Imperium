package com.imperium.worker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class WorkerStartupRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(WorkerStartupRunner.class);

    @Override
    public void run(ApplicationArguments args) {
        log.info("Imperium worker started.");
    }
}
