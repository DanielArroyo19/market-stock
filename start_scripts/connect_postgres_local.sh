export NODE_IP=127.0.0.1
export NODE_PORT=5432
export POSTGRES_PASSWORD=0QVJ8PPhPH
##export POSTGRES_PASSWORD=$(kubectl get secret --namespace default user-purchase-postgres-postgresql -o jsonpath="{.data.postgres-password}" | base64 -d)
echo "$POSTGRES_PASSWORD"
PGPASSWORD="$POSTGRES_PASSWORD" $psql --host $NODE_IP --port $NODE_PORT -U postgres -d postgres
##0QVJ8PPhPH
##C:/Program\ Files/PostgreSQL/14/bin/psql.exe postgres://purchase_db_user:purchase_db_password@$NODE_IP:$NODE_PORT/user_purchase_development
