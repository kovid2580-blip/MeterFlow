package com.meterflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class MeterFlowApplication {
    public static void main(String[] args) {
        SpringApplication.run(MeterFlowApplication.class, args);
    }
}
