package com.personal.marketnote.common.configuration.kafka;

import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "spring.kafka", name = "bootstrap-servers")
public class DltReprocessService {
    private static final Duration POLL_TIMEOUT = Duration.ofSeconds(5);
    private static final int MAX_POLL_ITERATIONS = 100;
    private static final long SEND_TIMEOUT_SECONDS = 10L;

    private static final Set<String> ALLOWED_TOPICS = Set.of(
            KafkaTopicConstants.ORDER_PAYMENT_COMPLETED,
            KafkaTopicConstants.PAYMENT_APPROVED,
            KafkaTopicConstants.PAYMENT_FAILED,
            KafkaTopicConstants.PAYMENT_CANCELLED,
            KafkaTopicConstants.SETTLEMENT_EXECUTED,
            KafkaTopicConstants.ORDER_PURCHASE_CONFIRMED,
            KafkaTopicConstants.PRODUCT_REGISTERED,
            KafkaTopicConstants.PRICE_POLICY_CREATED,
            KafkaTopicConstants.PRODUCT_UPDATED,
            KafkaTopicConstants.USER_SIGNUP_COMPLETED,
            KafkaTopicConstants.USER_REFERRAL_COMPLETED,
            KafkaTopicConstants.REVIEW_REGISTERED
    );

    private final ConsumerFactory<String, Object> consumerFactory;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public DltReprocessResult reprocess(String originalTopic) {
        if (!ALLOWED_TOPICS.contains(originalTopic)) {
            throw new InvalidDltTopicException(originalTopic);
        }

        String dltTopic = originalTopic + KafkaTopicConstants.DLT_SUFFIX;
        String groupId = "dlt-reprocessor-" + UUID.randomUUID();

        try (Consumer<String, Object> consumer = consumerFactory.createConsumer(groupId, "")) {
            List<PartitionInfo> partitionInfos = consumer.partitionsFor(dltTopic);
            if (partitionInfos == null || partitionInfos.isEmpty()) {
                log.info("DLT 토픽 파티션 없음. dltTopic={}", dltTopic);
                return new DltReprocessResult(0, 0);
            }

            List<TopicPartition> partitions = partitionInfos.stream()
                    .map(pi -> new TopicPartition(dltTopic, pi.partition()))
                    .toList();

            consumer.assign(partitions);
            consumer.seekToBeginning(partitions);

            int reprocessedCount = 0;
            int failedCount = 0;
            int pollIteration = 0;

            ConsumerRecords<String, Object> records = consumer.poll(POLL_TIMEOUT);
            while (!records.isEmpty() && pollIteration < MAX_POLL_ITERATIONS) {
                for (ConsumerRecord<String, Object> record : records) {
                    try {
                        kafkaTemplate.send(originalTopic, record.key(), record.value())
                                .get(SEND_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                        reprocessedCount++;
                        log.info("DLT 메시지 재처리 성공. originalTopic={}, key={}, offset={}",
                                originalTopic, record.key(), record.offset());
                    } catch (Exception e) {
                        failedCount++;
                        log.error("DLT 메시지 재처리 실패. originalTopic={}, key={}, offset={}",
                                originalTopic, record.key(), record.offset(), e);
                    }
                }
                pollIteration++;
                records = consumer.poll(POLL_TIMEOUT);
            }

            log.info("DLT 재처리 완료. dltTopic={}, reprocessed={}, failed={}",
                    dltTopic, reprocessedCount, failedCount);
            return new DltReprocessResult(reprocessedCount, failedCount);
        }
    }
}
