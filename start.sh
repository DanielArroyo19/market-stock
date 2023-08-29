./start_minikube.sh

helm repo update

(./start_dynamodb.sh
./load_dynamo_tables.sh) &

#(./start_postgres.sh
#./connect_postgres_local.sh) &

./start_kafka-cluster.sh &

./start_quote-poll.sh &
./start_api.sh &
#./start_front.sh &
./start_stock-prices-collector.sh &
./start_stock-prices-sink.sh &

#kafka-console-consumer.bat --bootstrap-server 127.0.0.1:31090 --topic market.quotes.price
#kafka-console-consumer.bat --bootstrap-server 127.0.0.1:31090 --topic market.stock.price.last
# kafka-topics.bat --list --bootstrap-server 127.0.0.1:31090

#kafka-topics.sh --describe --bootstrap-server 127.0.0.1:31090 --topic market.stock.price.last

#kafka-consumer-groups.bat --bootstrap-server 127.0.0.1:31090 --describe --group stock-prices-collector-process-sink

#kafka-consumer-groups.bat --bootstrap-server 127.0.0.1:31090 --reset-offsets --to-earliest --dry-run --group stock-prices-collector-process-sink --topic market.stock.price.last