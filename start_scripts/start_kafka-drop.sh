docker run -d -p 9000:9000 \
    -e KAFKA_BROKERCONNECT="cp-helm-charts-cp-kafka-0-nodeport:31090" \
    -e JVM_OPTS="-Xms32M -Xmx64M" \
    obsidiandynamics/kafdrop:latest

#echo "http://$(kubectl get service my-kafkadrop-kafkadrop -o=jsonpath='{.status.loadBalancer.ingress[0].ip}')"
