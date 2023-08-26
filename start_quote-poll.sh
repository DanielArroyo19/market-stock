pushd ./services/yfinance-poll

skaffold dev --profile minikube  --namespace default --force-colors --cache-artifacts=false