minikube start    --ports=30001:30001,30002:30002,30003:30003,30004:30004,30005:30005,30006:30006,30007:30007,30008:30008,30010:30010,31000:31000,31086:31086,31083:31083,31090:31090,32000:32000,32010:32010 \
 --extra-config=apiserver.service-node-port-range=30000-32010

eval minikube docker-env
echo MINIKUBE_IP_ADDRESS=127.0.0.1 > .env_minikube_ip
ssh-keygen -R $(minikube ip)
ssh -i $(minikube ssh-key) -oStrictHostKeyChecking=no -R 8789:localhost:8789 -R 8125:localhost:8125 -R 8000:localhost:8000 -fNT -M -S minikube_ports docker@127.0.0.1 -p $(docker container inspect -f '{{(index (index .NetworkSettings.Ports "22/tcp") 0).HostPort}}' minikube)