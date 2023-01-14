package edu.lantonia.srd.namegenerator.resource;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RandomNameResource {
    private final List<String> names = Arrays.asList("Lukasz", "Robert", "Kinga", "Ania");

    @GetMapping("/random-name")
    public String randomName() {
//        if (Math.random() <= 0.2) {
//            throw new RuntimeException("Simulated error");
//        }
        int idx = ThreadLocalRandom.current().nextInt(names.size());
        return names.get(idx);
    }
}
