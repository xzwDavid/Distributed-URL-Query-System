package edu.lantonia.tinyurl.backend.configuration;

import java.util.Arrays;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(value = "spring.cloud.etcd")
public class EtcdProperties {
    private List<String> endpoints = Arrays.asList("http://localhost:4001");

    public List<String> getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(List<String> endpoints) {
        this.endpoints = endpoints;
    }
}
