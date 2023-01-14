package edu.lantonia.tinyurl.backend.configuration;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.HostDistance;
import com.datastax.driver.core.PoolingOptions;
import com.datastax.driver.core.QueryOptions;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.policies.ConstantSpeculativeExecutionPolicy;
import com.datastax.driver.core.policies.DCAwareRoundRobinPolicy;
import com.datastax.driver.core.policies.IdentityTranslator;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CassandraConfiguration {
    @Value("${spring.cassandra.contact-points}")
    private List<String> contactPoints;

    @Value("${spring.cassandra.data-center}")
    private String datacenter;

    @Value("${spring.cassandra.keyspace}")
    private String keyspace;

    @Bean
    public Cluster cassandraCluster() {
        return new Cluster.Builder().addContactPointsWithPorts(
                        contactPoints.stream().map(p -> {
                            String[] parts = p.split(":");
                            return InetSocketAddress.createUnresolved(parts[0], Integer.parseInt(parts[1]));
                        }).collect(Collectors.toList()))
                .withLoadBalancingPolicy(new DCAwareRoundRobinPolicy.Builder().withLocalDc(datacenter).withUsedHostsPerRemoteDc(3).build())
                .withQueryOptions(new QueryOptions().setConsistencyLevel(ConsistencyLevel.QUORUM).setDefaultIdempotence(true))
                .withSpeculativeExecutionPolicy(new ConstantSpeculativeExecutionPolicy(50, 2))
                .withoutJMXReporting()
                .withPoolingOptions(new PoolingOptions()
                        .setConnectionsPerHost(HostDistance.LOCAL,  2, 5)
                        .setConnectionsPerHost(HostDistance.REMOTE, 2, 5))
                .withAddressTranslator(new IdentityTranslator() {
                    @Override
                    public InetSocketAddress translate(InetSocketAddress address) {
                        return InetSocketAddress.createUnresolved("localhost", address.getPort());
                    }
                })
                .build();
    }

    @Bean
    public Session cassandraSession(Cluster cassandraCluster) {
        return cassandraCluster.connect(keyspace);
    }
}
