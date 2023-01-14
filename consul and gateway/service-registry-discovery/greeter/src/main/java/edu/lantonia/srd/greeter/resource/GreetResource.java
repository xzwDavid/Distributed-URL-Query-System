package edu.lantonia.srd.greeter.resource;

import edu.lantonia.srd.greeter.references.NameGeneratorClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GreetResource {
    @Value("${spring.cloud.consul.discovery.instanceId}")
    private String instanceId;

    @Autowired
    private NameGeneratorClient nameGenerator;

    @GetMapping("/greet")
    public String ping() {
        if (Math.random() <= 0.2) {
            throw new RuntimeException("Simulated error");
        }
        String name = nameGenerator.randomName();
        return String.format("Hello %s from %s", name, instanceId);
    }
}
