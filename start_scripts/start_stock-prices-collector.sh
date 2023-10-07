pushd ./services/stock-prices-collector-process

skaffold dev --profile $1  --namespace default --force-colors --port-forward
