pushd ./dependencies/kafkadrop

skaffold dev --profile minikube  --namespace default --force-colors
