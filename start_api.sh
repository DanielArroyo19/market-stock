pushd ./services/market-stock-api

skaffold dev --profile minikube  --namespace default --force-colors --port-forward
