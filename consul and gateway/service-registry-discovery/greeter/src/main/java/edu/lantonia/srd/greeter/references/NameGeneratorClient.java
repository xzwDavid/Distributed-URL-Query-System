package edu.lantonia.srd.greeter.references;

import feign.Retryer;
import feign.codec.ErrorDecoder;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "srd-name-generator", configuration = NameGeneratorClient.Configuration.class)
public interface NameGeneratorClient {
    @GetMapping("/random-name")
    String randomName();

    class Configuration {
        @Bean
        Retryer retryer() {
            return new Retryer.Default(50, 500, 3);
        }

        @Bean
        ErrorDecoder errorDecoder() {
            return new ErrorDecoder.Default();
        }
    }
}
