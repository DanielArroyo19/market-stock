pushd ./frontend

skaffold dev --profile $1  --namespace default --force-colors --port-forward