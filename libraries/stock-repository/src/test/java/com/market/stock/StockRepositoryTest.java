package com.market.stock;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Expected;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.market.stock.repository.StockRepository;
import com.market.stock.repository.StockTable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class StockRepositoryTest {

    private StockRepository stockRepository;
    private DynamoDB mockDynamoDB;
    private Table mockTable;

    @BeforeEach
    void setUp() {
        mockDynamoDB = mock(DynamoDB.class);
        mockTable = mock(Table.class);
        stockRepository = new StockRepository(mockDynamoDB, "TestTable");
    }

    @Test
    void testSave() {
        // Prepare test data
        Stock previousValue = new Stock("ABC", ZonedDateTime.now());
        Stock newValue = new Stock("ABC", ZonedDateTime.now());

        // Mock DynamoDB operations
        when(mockDynamoDB.getTable("TestTable")).thenReturn(mockTable);
        when(mockTable.putItem(any(Item.class))).thenReturn(null);

        // Test the save method
        stockRepository.save(previousValue, newValue);

        // Verify that putItem was called with the correct parameters
        verify(mockTable, times(1)).putItem(argThat(item -> {
            Map<String, Object> expectedItem = new HashMap<>();
            expectedItem.put("STOCK", "ABC");
            expectedItem.put("ENABLED", newValue.getEnabled().format(DateTimeFormatter.ISO_DATE_TIME));
            return item.getJSON("STOCK").equals("ABC")
                    && item.getJSON("ENABLED").equals(expectedItem.get("ENABLED"));
        }), any(Expected.class));
    }

    // Add similar test methods for other functions

    @Test
    void testUpdate() {
        // Prepare test data
        String stockKey = "ABC";
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("attribute1", "value1");
        attributes.put("attribute2", "value2");

        // Mock DynamoDB operations
        when(mockDynamoDB.getTable("TestTable")).thenReturn(mockTable);
        when(mockTable.updateItem(any(UpdateItemSpec.class))).thenReturn(null);

        // Test the update method
        stockRepository.update(stockKey, attributes);

        // Verify that updateItem was called with the correct parameters
        ArgumentCaptor<UpdateItemSpec> updateItemSpecCaptor = ArgumentCaptor.forClass(UpdateItemSpec.class);
        verify(mockTable, times(1)).updateItem(updateItemSpecCaptor.capture());

        UpdateItemSpec capturedUpdateItemSpec = updateItemSpecCaptor.getValue();
        assertEquals(stockKey, capturedUpdateItemSpec.getKeyComponents().stream().findAny());
        assertEquals("set attribute1 = :attribute1, attribute2 = :attribute2", capturedUpdateItemSpec.getUpdateExpression());

        Map<String, Object> valueMap = capturedUpdateItemSpec.getValueMap();
        assertEquals(stockKey, valueMap.get(":" + StockTable.PrimaryKey.STOCK));
        assertEquals("value1", valueMap.get(":attribute1"));
        assertEquals("value2", valueMap.get(":attribute2"));
    }

}
