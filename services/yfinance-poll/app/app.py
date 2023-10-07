from kafka import KafkaProducer
import yfinance as yf
import sys
import time
import os
import boto3

# Specify the region name
region_name = 'us-east-2'
endpoint_url = os.environ['DYNAMO_URL']  # Specify the URL of your local DynamoDB endpoint

aws_access_key_id = os.environ['AWS_ACCESS_KEY_ID']
aws_secret_access_key = os.environ['AWS_SECRET_ACCESS_KEY']

dynamodb = boto3.client('dynamodb',
                        region_name=region_name,
                        endpoint_url=endpoint_url,
                        aws_access_key_id=aws_access_key_id,
                        aws_secret_access_key=aws_secret_access_key)


# Define the table name
table_name = 'Stock'

# Define the query parameters
query_params = {
    'TableName': table_name,
    'Select': 'ALL_ATTRIBUTES'
}


from protobuf import Quotes_pb2

sys.path.append("protobuf")

# Set up Kafka broker connection details
bootstrap_servers = os.environ.get('KAFKA_BOOSTRAP_URL','127.0.0.1:31090')
security_protocol = os.environ.get('KAFKA_SECURITY_PROTOCOL','SASL_PLAINTEXT')
sasl_mechanism = os.environ.get('KAFKA_SECURITY_MECHANISM','PLAIN')
sasl_plain_username = os.environ.get('KAFKA_CLUSTER_API_KEY','root')
sasl_plain_password = os.environ.get('KAFKA_CLUSTER_API_SECRET','root')

# Create Kafka producer instance
producer = KafkaProducer(security_protocol=security_protocol,
                         bootstrap_servers=bootstrap_servers,
                         key_serializer=str.encode,
                         value_serializer=lambda v: v.SerializeToString(),
                         sasl_mechanism=sasl_mechanism,
                         sasl_plain_username=sasl_plain_username,
                         sasl_plain_password=sasl_plain_password)

# Define the topic to which you want to send the message
topic = 'market.quotes.price'


if __name__ == '__main__':

    response = dynamodb.scan(**query_params)

    # Retrieve the items from the response
    items = response['Items']

    # Process the items
    for item in items:
        # Access the attributes of each item
        attribute_value = item['Stock']['S']
        # Process the attribute value as needed


        msft = yf.Ticker(attribute_value)
        if(msft.info):

            quote = Quotes_pb2.Quote()
            quote.symbol.symbol = msft.info.get('symbol')
            quote.currency = msft.info.get('currency')
            quote.last = msft.info.get('currentPrice') if msft.info.get('currentPrice') is not None else 0
            quote.bid = msft.info.get('bid') if msft.info.get('bid') is not None else 0
            quote.ask = msft.info.get('ask') if msft.info.get('ask') is not None else 0
            quote.transactionTimestamp.seconds = int(time.time())

            producer.send(topic, value=quote, key=quote.symbol.symbol)

    # Close the Kafka producer connection
    producer.close()