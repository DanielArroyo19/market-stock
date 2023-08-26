pushd ./dependencies/user_purchase-postgres

skaffold dev --profile minikube  --namespace default --force-colors --port-forward