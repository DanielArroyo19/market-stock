package com.market.stock.persistence.dynamodb;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.model.*;

import java.util.List;

public class DynamoDb {
    private static final String REGION = "us-east-2";

    private DynamoDb () {

    }
    public static DynamoDB client(String dynamoDBEndpoint) {
        AmazonDynamoDB dynamoDBClient = AmazonDynamoDBClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(dynamoDBEndpoint, REGION))
                .withCredentials(new DefaultAWSCredentialsProviderChain())
                .build();

        return new DynamoDB(dynamoDBClient);
    }

    public static DynamoDB client(String dynamoDBEndpoint, String accessKey, String secretKey) {
        AmazonDynamoDB dynamoDBClient = AmazonDynamoDBClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(dynamoDBEndpoint, REGION))
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey)))
                .build();
        return new DynamoDB(dynamoDBClient);
    }

    public static void createTable(DynamoDB dynamoDB, String tableName, String key, ScalarAttributeType keyType) {
        dynamoDB.createTable(
                tableName,
                List.of(new KeySchemaElement(key, KeyType.HASH)),
                List.of(new AttributeDefinition(key, keyType)),
                new ProvisionedThroughput(1L, 1L)
        );
    }

    public static void createTable(
            DynamoDB dynamoDB, String tableName, String hashKey, ScalarAttributeType hashKeyType,
            String rangeKey, ScalarAttributeType rangeKeyType
    ) {
        dynamoDB.createTable(
                tableName,
                List.of(
                        new KeySchemaElement(hashKey, KeyType.HASH),
                        new KeySchemaElement(rangeKey, KeyType.RANGE)
                ),
                List.of(
                        new AttributeDefinition(hashKey, hashKeyType),
                        new AttributeDefinition(rangeKey, rangeKeyType)
                ),
                new ProvisionedThroughput(1L, 1L)
        );
    }
}