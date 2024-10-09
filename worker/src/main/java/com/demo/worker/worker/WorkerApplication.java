package com.demo.worker.worker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication
public class WorkerApplication {

    private static final Logger logger = LoggerFactory.getLogger(WorkerApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(WorkerApplication.class, args);
        logger.info("La aplicación se ha iniciado correctamente.");
    }
}
