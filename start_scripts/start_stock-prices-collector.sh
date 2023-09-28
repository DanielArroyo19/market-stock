pushd ./services/stock-prices-collector-process

skaffold dev --profile minikube  --namespace default --force-colors --port-forward
