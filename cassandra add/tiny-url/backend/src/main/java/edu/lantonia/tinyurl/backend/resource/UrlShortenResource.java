package edu.lantonia.tinyurl.backend.resource;

import edu.lantonia.tinyurl.backend.repository.UrlsRepository;
import edu.lantonia.tinyurl.backend.service.IdGenerator;
import java.net.URI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UrlShortenResource {
    @Autowired
    private UrlsRepository urlsRepository;

    @Autowired
    private IdGenerator idGenerator;

    @PostMapping("/short-url")
    public String shortenUrl(@RequestBody String longUrl) throws Exception {
        String id = Long.toString(idGenerator.nextId(), 36);
        urlsRepository.insertUrl(id, longUrl);
        return String.format("http://localhost:8080/url/%s", id);
    }

    @GetMapping("/url/{id}")
    public ResponseEntity<Void> lookupUrl(@PathVariable String id) {
        String longUrl = urlsRepository.selectUrl(id);
        if (longUrl == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(longUrl)).build();
    }
}
