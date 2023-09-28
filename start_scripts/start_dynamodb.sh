pushd ./dependencies/dynamodb

skaffold dev --profile minikube  --namespace default --force-colors --port-forward