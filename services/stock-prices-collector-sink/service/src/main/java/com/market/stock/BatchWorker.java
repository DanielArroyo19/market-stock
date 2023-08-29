package com.market.stock;

import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;

import java.time.Duration;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static org.apache.kafka.clients.CommonClientConfigs.SECURITY_PROTOCOL_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.*;
import static org.apache.kafka.clients.consumer.OffsetResetStrategy.EARLIEST;
import static org.apache.kafka.common.config.SaslConfigs.SASL_JAAS_CONFIG;
import static org.apache.kafka.common.config.SaslConfigs.SASL_MECHANISM;
import static org.apache.kafka.common.config.SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG;

@Slf4j
public class BatchWorker<Key, Value> {
    private static final int POLL_DURATION_SECONDS = 2;
    private static final int SHUTDOWN_TIMEOUT_MILLISECONDS = 10000;

    // Retry
    private static final long RETRY_INTERVAL_START = 100;
    private static final long MAX_RETRY_INTERVAL = 10_000;
    private static final int RETRY_INTERVAL_MULTIPLIER = 2;

    private final AtomicBoolean stopped = new AtomicBoolean(false);

    private final BatchWorkerDefinition<Key, Value> batchWorkerDefinition;
    private final StreamProcessorConfiguration processorConfiguration;
    private Thread thread;
    private final Properties batchProperties;

    public BatchWorker(
            BatchWorkerDefinition<Key, Value> batchWorkerDefinition,
            StreamProcessorConfiguration processorConfiguration,
            Properties batchProperties
    ) {
        this.batchWorkerDefinition = batchWorkerDefinition;
        this.processorConfiguration = processorConfiguration;
        this.batchProperties = batchProperties;
    }

    public void start() {

        thread = new Thread(() -> {
            var kafkaConsumer = kafkaConsumer(batchProperties);

            kafkaConsumer.subscribe(
                    singletonList(batchWorkerDefinition.topic().name(processorConfiguration.getKafkaEnvironmentPrefix()))
            );

            try {
                poll(kafkaConsumer);
            } catch (InterruptedException e) {
                log.error("consumer poll interrupted: {}", Throwables.getStackTraceAsString(e));
            }
            kafkaConsumer.unsubscribe();
        });

        thread.start();
    }

    private KafkaConsumer<Key, Value> kafkaConsumer(Properties batchProperties) {
        return new KafkaConsumer<>(
                batchProperties,
                batchWorkerDefinition.topic().keySerde().deserializer(),
                batchWorkerDefinition.topic().valueSerde().deserializer()
        );
    }

    private void poll(KafkaConsumer<Key, Value> consumer) throws InterruptedException {
        boolean retryInProgress = false;
        long sleepInterval = RETRY_INTERVAL_START;

        while (!stopped.get()) {
            // get consumer's current assigned partitions and offsets
            Set<TopicPartition> assignment = consumer.assignment();
            List<PartitionAndOffset> partitionsAndOffsets = assignment.stream()
                    .map(topicPartition -> new PartitionAndOffset(topicPartition, consumer.position(topicPartition)))
                    .collect(Collectors.toList());

            boolean exceptionOccurred = false;

            try {
                var records = consumer.poll(Duration.ofSeconds(POLL_DURATION_SECONDS));
                if (!records.isEmpty()) {
                    batchWorkerDefinition.processBatch(records);
                }
            } catch (Exception exception) {
                exceptionOccurred = true;
                log.error("Failed to write batch: {}", Throwables.getStackTraceAsString(exception));
                if (batchWorkerDefinition.exceptionsToRetry().stream().anyMatch(retryType -> retryType.isInstance(exception))) {
                    sleepInterval = getNextSleepInterval(sleepInterval, retryInProgress);
                    if (!retryInProgress) {
                        retryInProgress = true;
                    }
                    partitionsAndOffsets.forEach(partitionAndOffset ->
                            consumer.seek(partitionAndOffset.getTopicPartition(), partitionAndOffset.getOffset()));
                    Thread.sleep(sleepInterval);
                } else {
                    retryInProgress = false;
                }
            } finally {
                if (!(exceptionOccurred && retryInProgress)) {
                    retryInProgress = false;
                    try {
                        consumer.commitSync();
                    } catch (Exception exception) {
                        log.error("Failed to commit consumer offset: {}", Throwables.getStackTraceAsString(exception));
                    }
                }
            }
        }
    }

    public void terminate() throws InterruptedException {
        stopped.set(true);
        thread.join(SHUTDOWN_TIMEOUT_MILLISECONDS);
    }

    public boolean isAlive() {
        return thread.isAlive();
    }

    private static long getNextSleepInterval(long currSleepInterval, boolean retryInProgress) {
        long nextSleepInterval = RETRY_INTERVAL_START;
        if (retryInProgress) {
            nextSleepInterval = currSleepInterval * RETRY_INTERVAL_MULTIPLIER;
            if (nextSleepInterval > MAX_RETRY_INTERVAL) {
                nextSleepInterval = MAX_RETRY_INTERVAL;
            }
        }
        return nextSleepInterval;
    }

    public static <Key, Value> Properties batchProperties(
            StreamProcessorConfiguration streamProcessorConfiguration,
            BatchWorkerDefinition<Key, Value> batchWorkerDefinition
    ) {
        Properties properties = sharedProperties(streamProcessorConfiguration);
        properties.put(GROUP_ID_CONFIG, batchWorkerDefinition.groupId(streamProcessorConfiguration.getKafkaEnvironmentPrefix()));
        properties.put(ENABLE_AUTO_COMMIT_CONFIG, false);
        properties.put(SESSION_TIMEOUT_MS_CONFIG, 30_000);
        properties.put(MAX_POLL_RECORDS_CONFIG, 2_000);

        return properties;
    }

    private static Properties sharedProperties(StreamProcessorConfiguration streamProcessorConfiguration) {
        return new Properties() {{
            put(BOOTSTRAP_SERVERS_CONFIG, streamProcessorConfiguration.getKafkaUrl());
            put(AUTO_OFFSET_RESET_CONFIG, EARLIEST);

            if (streamProcessorConfiguration.getKafkaSecurityProtocol().equals("SASL_SSL")) {
                put(SECURITY_PROTOCOL_CONFIG, streamProcessorConfiguration.getKafkaSecurityProtocol());
                put(SASL_JAAS_CONFIG, "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"" + streamProcessorConfiguration.getConfluentCloudApiKey() + "\" password=\"" + streamProcessorConfiguration.getConfluentCloudApiSecret() + "\";");
                put(SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, "https");
                put(SASL_MECHANISM, "PLAIN");
            }
        }};
    }
}
