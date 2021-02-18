package com.example.upload;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

/**
 * Hello world!
 */
@SpringBootApplication
@EnableEurekaClient
public class UploadGatewayApp {
    public static void main(String[] args) {
        SpringApplication.run(UploadGatewayApp.class, args);
    }
}
