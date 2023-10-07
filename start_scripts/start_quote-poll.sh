pushd ./services/yfinance-poll

skaffold dev --profile $1  --namespace default --force-colors --cache-artifacts=true