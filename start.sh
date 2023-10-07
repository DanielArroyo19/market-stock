echo "Profile $1";

./start_scripts/start_minikube.sh

helm repo update

if test $1 == "minikube"; then
   (./start_scripts/start_dynamodb.sh
   ./start_scripts/load_dynamo_tables.sh) &

   ./start_scripts/start_kafka-cluster.sh $1 &
fi

if test $1 == "ephemeral"; then
  kubectl create secret generic dynamodb-secret \
    --from-literal=AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID} \
    --from-literal=AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY};

  kubectl create secret generic kafka-cluster-secret \
    --from-literal=KAFKA_CLUSTER_API_KEY=${KAFKA_CLUSTER_API_KEY} \
    --from-literal=KAFKA_CLUSTER_API_SECRET=${KAFKA_CLUSTER_API_SECRET};
fi

#(./start_scripts/start_postgres.sh
#./start_scripts/connect_postgres_local.sh) &


./start_scripts/start_quote-poll.sh $1 &
./start_scripts/start_api.sh $1 &
#./start_scripts/start_front.sh &
./start_scripts/start_stock-prices-collector.sh $1 &
./start_scripts/start_stock-prices-sink.sh $1 &

#kafka-console-consumer.bat --bootstrap-server 127.0.0.1:31090 --topic market.quotes.price
#kafka-console-consumer.bat --bootstrap-server 127.0.0.1:31090 --topic market.stock.price.last
# kafka-topics.bat --list --bootstrap-server 127.0.0.1:31090

#kafka-topics.sh --describe --bootstrap-server 127.0.0.1:31090 --topic market.stock.price.last

#kafka-consumer-groups.bat --bootstrap-server 127.0.0.1:31090 --describe --group stock-prices-collector-process-sink

#kafka-consumer-groups.bat --bootstrap-server 127.0.0.1:31090 --reset-offsets --to-earliest --dry-run --group stock-prices-collector-process-sink --topic market.stock.price.last