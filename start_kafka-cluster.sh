helm repo add confluentinc https://charts.confluent.io
helm install my-kafka-cluster confluentinc/cp-helm-charts --version 0.6.1

pushd ./dependencies/kafka-cluster

skaffold dev --profile minikube  --namespace default --force-colors 