package com.preptrack;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync      // enables @Async on service methods (weekly report generation)
@EnableScheduling // enables @Scheduled (daily revision digest at 8 AM)
public class PreptrackApplication {

    public static void main(String[] args) {
        SpringApplication.run(PreptrackApplication.class, args);
    }
}
