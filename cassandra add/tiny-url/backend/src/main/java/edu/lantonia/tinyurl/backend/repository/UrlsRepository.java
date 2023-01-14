package edu.lantonia.tinyurl.backend.repository;

import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UrlsRepository {
    private final static Integer TTL = 5;

    private final Session session;

    private PreparedStatement insertUrl = null;
    private PreparedStatement selectUrl = null;

    @Autowired
    public UrlsRepository(Session session) {
        this.session = session;
    }

    @PostConstruct
    public void initialize() {
        insertUrl = session.prepare("INSERT INTO urls(id, url) VALUES(?, ?) USING TTL ?;");
        selectUrl = session.prepare("SELECT url FROM urls WHERE id = ? LIMIT 1;");
    }

    public void insertUrl(String id, String url) {
        insertUrl(id, url, TTL);
    }

    public void insertUrl(String id, String url, Integer ttl) {
        session.execute(insertUrl.bind(id, url, ttl));
    }

    public String selectUrl(String id) {
        final Row row = session.execute(selectUrl.bind(id).setConsistencyLevel(ConsistencyLevel.QUORUM)).one();
        if (row == null) {
            return null;
        }
        return row.getString("url");
    }
}
