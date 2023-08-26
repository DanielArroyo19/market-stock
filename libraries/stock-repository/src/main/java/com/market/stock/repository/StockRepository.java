package com.market.stock.repository;

import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.ScanSpec;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.model.*;
import com.market.stock.Stock;
import lombok.extern.slf4j.Slf4j;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.market.stock.repository.StockTable.Attributes.ENABLED;
import static com.market.stock.repository.StockTable.PrimaryKey.STOCK;
import static java.lang.String.format;
import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;
import static java.util.stream.StreamSupport.stream;


@Slf4j
public class StockRepository {
    private final DynamoDB dynamoDB;
    private final String tableName;

    public StockRepository(DynamoDB dynamoDB, String tableName) {
        this.dynamoDB = dynamoDB;
        this.tableName = tableName;
    }

    public void save(Stock previousValue, Stock newValue) {
        try {
            stockTable().putItem(Item.fromMap(
                    new HashMap<>() {{
                        put(STOCK, newValue.getStock());
                        put(ENABLED, formatDateTime(newValue.getEnabled()));
                    }}
            ), conditionExpression(previousValue), null, conditionValueMap(previousValue));
        } catch (ConditionalCheckFailedException e) {
            log.info("Handled siteMigrationStatusRepository.save race condition.");
        }
    }

    public void update(String stockKey, Map<String, Object> attributes){
        try {
            UpdateItemSpec updateItemSpec = new UpdateItemSpec().withPrimaryKey(STOCK, stockKey)
                    .withUpdateExpression("set " + updateExpression(attributes))
                    .withValueMap(updateValueMap(stockKey, attributes))
                    .withConditionExpression(STOCK + " = :" + STOCK)
                    .withReturnValues(ReturnValue.NONE);

            stockTable().updateItem(updateItemSpec);
        } catch (Exception e) {
            log.info("Unable to update status for the stockKey: {}, please activate the site first, error: {}", stockKey, e.getMessage());
        }
    }

    private Map<String, Object> updateValueMap(String stockKey, Map<String, Object> attributes){
        var valueMap = attributes.entrySet().stream()
                .collect(Collectors.toMap(entry -> ":" + entry.getKey(), Map.Entry::getValue, (prev, next) -> next, HashMap::new));
        valueMap.put(":" + STOCK, stockKey);
        return  valueMap;
    }

    public void removeAttributes(String stockKey, List<String> nameAttributes){
        try {
            UpdateItemSpec updateItemSpec = new UpdateItemSpec().withPrimaryKey(STOCK, stockKey)
                    .withUpdateExpression("remove " + String.join(", ", nameAttributes))
                    .withReturnValues(ReturnValue.NONE);

            stockTable().updateItem(updateItemSpec);
        } catch (Exception e) {
            log.info("Unable to update status for the stockKey: {}, please activate the site first, error: {}", stockKey, e.getMessage());
        }
    }

    public List<Stock> all() {
        dynamoDB.listTables().forEach(System.out::println);
        var scanOutcomes = stockTable().scan(new ScanSpec()
                .withAttributesToGet(STOCK, ENABLED));

        return stream(scanOutcomes.spliterator(), false)
                .map(this::stockStatus)
                .collect(Collectors.toList());
    }

    public Stock get(String stockKey) {
        var item = stockTable().getItem(new GetItemSpec()
                .withPrimaryKey(new PrimaryKey(STOCK, stockKey))
                .withAttributesToGet(STOCK, ENABLED));
        if (nonNull(item)) return stockStatus(item);
        return null;
    }

    public void delete(String stockKey) {
        stockTable().deleteItem(new KeyAttribute(STOCK, stockKey));
    }

    private String updateExpression(Map<String, Object> updateMap) {
        if (nonNull(updateMap))
            return updateMap.keySet().stream()
                    .map(attribute -> format("{0} = :{0}", attribute))
                    .collect(joining(", "));

        return null;
    }

    private String conditionExpression(Stock previousValue) {
        if (nonNull(previousValue)) return Stream
                .of(STOCK, ENABLED)
                .map(attribute -> format("{0} = :{0}", attribute))
                .collect(joining(" and "));

        return "attribute_not_exists(" + STOCK + ")";
    }

    private Map<String, Object> conditionValueMap(Stock previousValue) {
        if (nonNull(previousValue)) return new HashMap<>() {{
            put(":" + ENABLED, formatDateTime(previousValue.getEnabled()));
        }};

        return null;
    }

    private Stock stockStatus(Item item) {
        return Stock.builder()
                .stock(item.getString(STOCK))
                .enabled(parseDateTime(item.getString(ENABLED)))
                .build();
    }

    private String formatDateTime(ZonedDateTime activationRequested) {
        return ofNullable(activationRequested)
                .map(zonedDateTime -> zonedDateTime.format(ISO_DATE_TIME))
                .orElse(null);
    }

    private ZonedDateTime parseDateTime(String formattedDateTime) {
        return ofNullable(formattedDateTime)
                .map(ZonedDateTime::parse)
                .orElse(null);
    }

    private Table stockTable() {
        return dynamoDB.getTable(tableName);
    }
}
