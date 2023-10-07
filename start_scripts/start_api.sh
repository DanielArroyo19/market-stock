pushd ./services/market-stock-api

skaffold dev --profile $1  --namespace default --force-colors --port-forward

#http://127.0.0.1:8080/v1/symbol

#Invoke-WebRequest -Method PUT -Uri "http://127.0.0.1:8080/v1/symbol/VTI" -Headers @{"Content-Type"="application/json"} -Body '{"last": 123.45}'

#[market-stock-api] 2023-09-22 06:24:02.212  INFO 1 --- [nio-8080-exec-1] c.m.stock.repository.StockRepository     : Unable to update status for the stockKey: VTI, please activate the site first, error: Invalid UpdateExpression: Syntax error; token: "{", near: "set {0" (Service: AmazonDynamoDBv2; Status Code: 400; Error Code: ValidationException; Request ID: 8c40d852-4433-433e-8b71-7cdd95d55e6c; Proxy: null)