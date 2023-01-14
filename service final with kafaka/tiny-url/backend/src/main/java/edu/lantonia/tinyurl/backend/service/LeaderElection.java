package edu.lantonia.tinyurl.backend.service;

import edu.lantonia.tinyurl.backend.resource.NextRangeResource;
import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.Election;
import io.etcd.jetcd.Lease;
import io.etcd.jetcd.election.CampaignResponse;
import io.etcd.jetcd.election.LeaderResponse;
import io.etcd.jetcd.lease.LeaseKeepAliveResponse;
import io.grpc.stub.StreamObserver;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.Lifecycle;
import org.springframework.util.Assert;

@Slf4j
public class LeaderElection implements Lifecycle, InitializingBean, DisposableBean {
    private final ExecutorService executorService = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "etcd-leader-election");
        thread.setDaemon(true);
        return thread;
    });

    @Autowired
    private Client client;

    @Autowired
    private NextRangeResource nextRangeResource;

    private final String electionName;
    private final String instanceId;

    private Lease leaseClient;
    private Election electionClient;

    private volatile boolean running;
    private volatile String leaderInstanceId;
    private volatile Future<Void> leadershipFuture;
    private volatile Long leaseId;


    public LeaderElection(String electionName, String instanceId) {
        this.electionName = electionName;
        this.instanceId = instanceId;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        leaseClient = client.getLeaseClient();
        electionClient = client.getElectionClient();
        start();
    }

    @Override
    public synchronized void start() {
        if (!running) {
            running = true;
            leadershipFuture = executorService.submit(new LeadershipJob());
        }
    }

    @Override
    public synchronized void stop() {
        if (running) {
            running = false;
            leadershipFuture.cancel(true);
        }
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public void destroy() throws Exception {
        stop();
        executorService.shutdown();
        executorService.awaitTermination(30, TimeUnit.SECONDS);
        electionClient.close();
        electionClient = null;
        client.close();
        client = null;
    }

    public String getLeaderInstanceId() {
        return leaderInstanceId;
    }

    public boolean isLeader() {
        return leaderInstanceId != null && leaderInstanceId.equals(instanceId);
    }

    class LeadershipJob implements Callable<Void> {
        @Override
        public Void call() {
            CampaignResponse response = null;
            initializeObserver(electionName);
            try {
                while (running) {
                    if (isLeader()) {
                        TimeUnit.SECONDS.sleep(10);
                    } else {
                        response = tryAcquire();
                    }
                }
            } catch (InterruptedException e) {
                // thread was asked to stop as part of normal operation
                Thread.currentThread().interrupt();
            } finally {
                resign(response);
                revokeLease(leaseId);
            }
            return null;
        }

        private CampaignResponse tryAcquire() {
            CampaignResponse response = null;
            try {
                ByteSequence name = ByteSequence.from(electionName, StandardCharsets.UTF_8);
                ByteSequence proposal = ByteSequence.from(instanceId, StandardCharsets.UTF_8);
                leaseId = registerLeaseAndKeepAlive();
                response = electionClient.campaign(name, leaseId, proposal).get();
                Assert.isTrue(response.getLeader().getLease() == leaseId, "Leadership contract failed");
                leaderInstanceId = instanceId;
            } catch (Exception e) {
                log.error("Leader acquisition failed: " + e.getMessage(), e);
                leaderInstanceId = null;
                resign(response);
                revokeLease(leaseId);
            }
            return response;
        }

        private void initializeObserver(String electionName) {
            ByteSequence name = ByteSequence.from(electionName, StandardCharsets.UTF_8);
            electionClient.observe(name, new Election.Listener() {
                @Override
                public void onNext(LeaderResponse response) {
                    nextRangeResource.initializeLeader();
                    leaderInstanceId = response.getKv().getValue().toString(StandardCharsets.UTF_8);
                    log.info("Leader elected [{}]: {}", electionName, leaderInstanceId);
                }

                @Override
                public void onError(Throwable error) {
                    log.error("Failed to observe leader process: " + error.getMessage(), error);
                    initializeObserver(electionName);
                }

                @Override
                public void onCompleted() {
                }
            });
        }

        private Long registerLeaseAndKeepAlive() throws Exception {
            leaseId = leaseClient.grant(10).get().getID();
            leaseClient.keepAlive(leaseId, new StreamObserver<>() {
                @Override
                public void onNext(LeaseKeepAliveResponse leaseKeepAliveResponse) {
                }

                @Override
                public void onError(Throwable error) {
                    log.error("Failed to keep-alive lease: " + error.getMessage(), error);
                    Thread.currentThread().interrupt();
                }

                @Override
                public void onCompleted() {
                }
            });
            return leaseId;
        }

        private void revokeLease(Long leaseId) {
            if (leaseId != null) {
                try {
                    leaseClient.revoke(leaseId).get(10, TimeUnit.SECONDS);
                } catch (Exception e) {
                    // ignore
                }
            }
        }

        private void resign(CampaignResponse response) {
            if (response != null) {
                try {
                    electionClient.resign(response.getLeader()).get(10, TimeUnit.SECONDS);
                } catch (Exception e) {
                    // ignore.
                }
            }
        }
    }
}
