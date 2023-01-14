package edu.lantonia.tinyurl.backend.service;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope("singleton")
public class IdGenerator {

    private long nextValue = 0;

    public synchronized long nextId() throws Exception {
        return nextValue++;
    }
}
