package com.market.stock;

import lombok.Value;
import org.apache.kafka.common.TopicPartition;

@Value
public class PartitionAndOffset {

    private TopicPartition topicPartition;
    private long offset;
}
