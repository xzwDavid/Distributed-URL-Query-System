CQL="CREATE KEYSPACE IF NOT EXISTS dev WITH replication = {'class': 'SimpleStrategy', 'replication_factor': '3'} AND durable_writes = true;
USE dev;
CREATE TABLE IF NOT EXISTS urls (id text, url text, PRIMARY KEY (id)) WITH default_time_to_live = 300;"

until echo $CQL | cqlsh; do
  echo "cqlsh: Cassandra is unavailable to initialize - will retry later"
  sleep 5
done &

exec /docker-entrypoint.sh "$@"
