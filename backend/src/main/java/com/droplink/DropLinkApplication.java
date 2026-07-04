package com.droplink;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DropLinkApplication {
    public static void main(String[] args) {
        SpringApplication.run(DropLinkApplication.class, args);
    }
}
