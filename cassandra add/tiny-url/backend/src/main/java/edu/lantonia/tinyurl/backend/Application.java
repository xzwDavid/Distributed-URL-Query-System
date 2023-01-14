package edu.lantonia.tinyurl.backend;

import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableRetry
public class Application {
    @Value("${info.app.uuid}")
    private String instanceId;

    public static void main(String[] args) {
        System.setProperty("instanceId", UUID.randomUUID().toString());
        SpringApplication.run(Application.class, args);
    }
}
