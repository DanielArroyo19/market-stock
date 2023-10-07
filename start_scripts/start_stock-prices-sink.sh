pushd ./services/stock-prices-collector-processor-sink

skaffold dev --profile $1  --namespace default --force-colors --port-forward
