package com.example.ansibleping;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AnsiblePingApplication {
    public static void main(String[] args) {
        SpringApplication.run(AnsiblePingApplication.class, args);
    }
}
