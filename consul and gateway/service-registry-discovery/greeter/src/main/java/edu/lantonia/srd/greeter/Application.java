package edu.lantonia.srd.greeter;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class Application {
    @Value("${info.app.uuid}")
    private String instanceId;

    public static void main(String[] args) {
        System.setProperty("instanceId", UUID.randomUUID().toString());
        SpringApplication.run(Application.class, args);
    }
}
