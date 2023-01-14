package edu.lantonia.tinyurl.backend.configuration;

import io.etcd.jetcd.Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EtcdConfiguration {
    @Autowired
    private EtcdProperties etcdProperties;

    @Bean
    public Client etcdClient() {
        return Client.builder().endpoints(etcdProperties.getEndpoints().toArray(new String[0])).build();
    }
}
