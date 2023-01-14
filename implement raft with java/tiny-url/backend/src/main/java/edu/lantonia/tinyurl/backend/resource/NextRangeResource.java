package edu.lantonia.tinyurl.backend.resource;

import edu.lantonia.tinyurl.backend.model.NewRangeResponse;
import edu.lantonia.tinyurl.backend.service.LeaderElection;
import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KeyValue;
import io.etcd.jetcd.election.NotLeaderException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Scope("singleton")
public class NextRangeResource {
    private static final ByteSequence counterKey = ByteSequence.from("tiny-url/counter", StandardCharsets.UTF_8);

    @Value("${tiny-url.counter.batch-size:5}")
    private Long batchSize;

    @Autowired
    private LeaderElection leaderElection;

    @Autowired
    private Client etcdClient;

    private volatile Long lastValue = null;

    @GetMapping("/next-range")
    public synchronized NewRangeResponse lookupUrl() throws Exception {
        if (!leaderElection.isLeader()) {
            throw new NotLeaderException();
        }
        lookupLastValueIfNeeded();
        final Long nextValue = lastValue + batchSize;
        etcdClient.getKVClient().put(counterKey, ByteSequence.from(nextValue.toString(), StandardCharsets.UTF_8)).get(1, TimeUnit.SECONDS);
        final NewRangeResponse response = new NewRangeResponse(lastValue, nextValue);
        lastValue = nextValue;
        return response;
    }

    public synchronized void initializeLeader() {
        // reset, so that we look up the most recent value
        lastValue = null;
    }

    private void lookupLastValueIfNeeded() throws Exception {
        if (lastValue != null) {
            return;
        }
        List<KeyValue> keyValues = etcdClient.getKVClient().get(counterKey).get(1, TimeUnit.SECONDS).getKvs();
        if (keyValues.isEmpty() || keyValues.get(0) == null || keyValues.get(0).getValue() == null) {
            lastValue = 0L;
        } else {
            lastValue = Long.parseLong(keyValues.get(0).getValue().toString(StandardCharsets.UTF_8));
        }
    }
}
