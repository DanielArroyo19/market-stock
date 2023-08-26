URL=http://dynamodb:8000/

aws dynamodb create-table \
    --table-name Stock\
    --attribute-definitions \
        AttributeName=Stock,AttributeType=S \
    --key-schema AttributeName=Stock,KeyType=HASH \
    --provisioned-throughput ReadCapacityUnits=1,WriteCapacityUnits=1 \
    --table-class STANDARD --endpoint-url $URL  --region us-west-2


aws dynamodb put-item \
    --table-name Stock \
    --item '{
        "Stock": {"S": "AAL"},
        "Enabled": {"S": "2022-01-01T00:00:00Z"}
        }' \
    --return-consumed-capacity TOTAL --endpoint-url $URL --region us-west-2

aws dynamodb put-item \
    --table-name Stock \
    --item '{
        "Stock": {"S": "VTI"},
        "Enabled": {"S": "2022-01-01T00:00:00Z"}
         }' \
    --return-consumed-capacity TOTAL --endpoint-url $URL --region us-west-2

aws dynamodb list-tables --endpoint-url $URL --region us-west-2

aws dynamodb scan --table-name Stock --endpoint-url $URL --region us-west-2