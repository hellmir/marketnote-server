package com.personal.marketnote.common.configuration.kafka;

import com.personal.marketnote.common.kafka.DltTopicRegistry;
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
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "spring.kafka", name = "bootstrap-servers")
public class DltReprocessService {
    private static final Duration POLL_TIMEOUT = Duration.ofSeconds(5);
    private static final int MAX_POLL_ITERATIONS = 100;
    private static final long SEND_TIMEOUT_SECONDS = 10L;

    private final ConcurrentHashMap<String, Boolean> reprocessingTopics = new ConcurrentHashMap<>();

    private final ConsumerFactory<String, Object> consumerFactory;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final DltAuditLogger dltAuditLogger;
    private final DltMetricsCollector dltMetricsCollector;
    private final DltMessageResolutionJpaRepository resolutionRepository;

    public DltReprocessResult reprocess(String originalTopic, String operatorInfo) {
        if (!DltTopicRegistry.isAllowed(originalTopic)) {
            throw new InvalidDltTopicException(originalTopic);
        }

        if (reprocessingTopics.putIfAbsent(originalTopic, Boolean.TRUE) != null) {
            throw new DltReprocessAlreadyInProgressException(originalTopic);
        }

        try {
            return doReprocess(originalTopic, operatorInfo);
        } finally {
            reprocessingTopics.remove(originalTopic);
        }
    }

    private DltReprocessResult doReprocess(String originalTopic, String operatorInfo) {
        String dltTopic = DltTopicRegistry.toDltTopic(originalTopic);
        String groupId = "dlt-reprocessor-" + UUID.randomUUID();
        Set<String> resolvedKeys = buildResolvedKeySet(originalTopic);

        dltAuditLogger.logReprocessStart(originalTopic, operatorInfo);

        try (Consumer<String, Object> consumer = consumerFactory.createConsumer(groupId, "")) {
            List<PartitionInfo> partitionInfos = consumer.partitionsFor(dltTopic);
            if (partitionInfos == null || partitionInfos.isEmpty()) {
                log.info("DLT 토픽 파티션 없음. dltTopic={}", dltTopic);
                dltAuditLogger.logReprocessComplete(originalTopic, operatorInfo, 0, 0);
                return new DltReprocessResult(0, 0, 0);
            }

            List<TopicPartition> partitions = partitionInfos.stream()
                    .map(pi -> new TopicPartition(dltTopic, pi.partition()))
                    .toList();

            consumer.assign(partitions);
            consumer.seekToBeginning(partitions);

            int reprocessedCount = 0;
            int failedCount = 0;
            int skippedCount = 0;
            int pollIteration = 0;

            ConsumerRecords<String, Object> records = consumer.poll(POLL_TIMEOUT);
            while (!records.isEmpty() && pollIteration < MAX_POLL_ITERATIONS) {
                for (ConsumerRecord<String, Object> record : records) {
                    String key = record.partition() + ":" + record.offset();
                    if (resolvedKeys.contains(key)) {
                        skippedCount++;
                        log.info("이미 처리된 DLT 메시지 스킵. originalTopic={}, partition={}, offset={}",
                                originalTopic, record.partition(), record.offset());
                        continue;
                    }

                    try {
                        kafkaTemplate.send(originalTopic, record.key(), record.value())
                                .get(SEND_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                        reprocessedCount++;
                        dltMetricsCollector.incrementDltReprocessCount(originalTopic, "success");
                        try {
                            saveResolution(originalTopic, dltTopic, record.partition(), record.offset(),
                                    DltResolutionStatus.RETRIED, operatorInfo);
                        } catch (Exception se) {
                            log.warn("DLT resolution 저장 실패. originalTopic={}, partition={}, offset={}",
                                    originalTopic, record.partition(), record.offset(), se);
                        }
                        log.info("DLT 메시지 재처리 성공. originalTopic={}, key={}, offset={}",
                                originalTopic, record.key(), record.offset());
                    } catch (Exception e) {
                        failedCount++;
                        dltMetricsCollector.incrementDltReprocessCount(originalTopic, "failure");
                        log.error("DLT 메시지 재처리 실패. originalTopic={}, key={}, offset={}",
                                originalTopic, record.key(), record.offset(), e);
                    }
                }
                pollIteration++;
                records = consumer.poll(POLL_TIMEOUT);
            }

            log.info("DLT 재처리 완료. dltTopic={}, reprocessed={}, failed={}, skipped={}",
                    dltTopic, reprocessedCount, failedCount, skippedCount);
            dltAuditLogger.logReprocessComplete(originalTopic, operatorInfo, reprocessedCount, failedCount);
            return new DltReprocessResult(reprocessedCount, failedCount, skippedCount);
        } catch (InvalidDltTopicException e) {
            throw e;
        } catch (Exception e) {
            dltAuditLogger.logReprocessError(originalTopic, operatorInfo, e);
            throw new DltReprocessFailedException(originalTopic);
        }
    }

    private Set<String> buildResolvedKeySet(String originalTopic) {
        List<DltMessageResolutionJpaEntity> resolutions = resolutionRepository.findByOriginalTopic(originalTopic);
        Set<String> keys = new HashSet<>();
        for (DltMessageResolutionJpaEntity entity : resolutions) {
            keys.add(entity.toResolutionKey());
        }
        return keys;
    }

    private void saveResolution(String originalTopic, String dltTopic,
                                int partition, long offset,
                                DltResolutionStatus resolution, String operatorInfo) {
        DltMessageResolutionJpaEntity entity = DltMessageResolutionJpaEntity.of(
                originalTopic, dltTopic, partition, offset,
                resolution, operatorInfo, LocalDateTime.now()
        );
        resolutionRepository.save(entity);
    }
}
