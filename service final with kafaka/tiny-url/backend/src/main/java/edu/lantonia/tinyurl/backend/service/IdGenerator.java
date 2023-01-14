package edu.lantonia.tinyurl.backend.service;

import edu.lantonia.tinyurl.backend.model.NewRangeResponse;
import edu.lantonia.tinyurl.backend.references.LeaderClient;
import io.etcd.jetcd.election.NoLeaderException;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Scope;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

@Service
@Scope("singleton")
public class IdGenerator {
    @Autowired
    private LeaderClient leaderClient;

    @Autowired
    private DiscoveryClient discoveryClient;

    @Autowired
    private LeaderElection leaderElection;

    private long nextValue = -1;
    private long lastValue = -1;

    public synchronized long nextId() throws Exception {
        if (nextValue + 1 == lastValue || nextValue == -1) {
            requestNewIdRange();
        }
        return nextValue++;
    }

    @Retryable(value = NoLeaderException.class, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    private void requestNewIdRange() throws Exception {
        final ServiceInstance leaderInstance = leaderInstance();
        if (leaderInstance == null) {
            throw new NoLeaderException();
        }
        final NewRangeResponse response = leaderClient.nextRange(
                URI.create("http://" + leaderInstance.getHost() + ":" + leaderInstance.getPort())
        );
        nextValue = response.start;
        lastValue = response.end;
    }

    protected ServiceInstance leaderInstance() {
        final List<ServiceInstance> list = discoveryClient.getInstances("tiny-url-backend");
        if (list != null) {
            final Optional<ServiceInstance> instance = list.stream()
            .filter(s -> s.getInstanceId().endsWith(leaderElection.getLeaderInstanceId())).findAny();
            return instance.orElse(null);
        }
        return null;
    }
}
