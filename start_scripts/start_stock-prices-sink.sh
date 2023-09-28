pushd ./services/stock-prices-collector-processor-sink

skaffold dev --profile minikube  --namespace default --force-colors --port-forward
