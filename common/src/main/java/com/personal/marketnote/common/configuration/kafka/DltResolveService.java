package com.personal.marketnote.common.configuration.kafka;

import com.personal.marketnote.common.kafka.DltTopicRegistry;
import com.personal.marketnote.common.utility.FormatValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.TopicPartition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "spring.kafka", name = "bootstrap-servers")
public class DltResolveService {
    private static final Duration POLL_TIMEOUT = Duration.ofSeconds(5);
    private static final long SEND_TIMEOUT_SECONDS = 10L;

    private final ConsumerFactory<String, Object> consumerFactory;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final DltAuditLogger dltAuditLogger;
    private final DltMetricsCollector dltMetricsCollector;
    private final DltMessageResolutionJpaRepository resolutionRepository;

    public DltResolveResult resolve(DltResolveCommand command, String operatorInfo) {
        if (!DltTopicRegistry.isAllowed(command.originalTopic())) {
            throw new InvalidDltTopicException(command.originalTopic());
        }

        String dltTopic = DltTopicRegistry.toDltTopic(command.originalTopic());

        Optional<DltMessageResolutionJpaEntity> existing =
                resolutionRepository.findByDltTopicAndPartitionNumberAndOffsetNumber(
                        dltTopic, command.partition(), command.offset());

        if (existing.isPresent()) {
            dltAuditLogger.logResolveAlreadyResolved(
                    command.originalTopic(), command.partition(), command.offset(),
                    existing.get().getResolution().name(), operatorInfo);
            return new DltResolveResult(existing.get().getResolution(), true);
        }

        DltResolutionStatus resolution = command.action().toResolutionStatus();

        try {
            if (command.action().isRetry()) {
                retryMessage(command.originalTopic(), dltTopic, command.partition(), command.offset());
            }

            DltResolveResult duplicateResult = saveResolution(command.originalTopic(), dltTopic,
                    command.partition(), command.offset(), resolution, command.reason(), operatorInfo);
            if (FormatValidator.hasValue(duplicateResult)) {
                return duplicateResult;
            }

            dltAuditLogger.logResolve(command.originalTopic(), command.partition(), command.offset(),
                    command.action().name(), command.reason(), operatorInfo);
            dltMetricsCollector.incrementDltResolveCount(
                    command.originalTopic(), command.action().name().toLowerCase());

            return new DltResolveResult(resolution, false);
        } catch (DltMessageNotFoundException e) {
            throw e;
        } catch (Exception e) {
            dltAuditLogger.logResolveError(command.originalTopic(), command.partition(), command.offset(),
                    command.action().name(), operatorInfo, e);
            throw new DltResolveFailedException(
                    command.originalTopic(), command.partition(), command.offset());
        }
    }

    private void retryMessage(String originalTopic, String dltTopic,
                              int partition, long offset) throws Exception {
        ConsumerRecord<String, Object> record = readDltMessage(dltTopic, partition, offset);
        kafkaTemplate.send(originalTopic, record.key(), record.value())
                .get(SEND_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    private ConsumerRecord<String, Object> readDltMessage(String dltTopic, int partition, long offset) {
        String groupId = "dlt-resolver-" + UUID.randomUUID();
        try (Consumer<String, Object> consumer = consumerFactory.createConsumer(groupId, "")) {
            TopicPartition topicPartition = new TopicPartition(dltTopic, partition);
            consumer.assign(List.of(topicPartition));
            consumer.seek(topicPartition, offset);

            ConsumerRecords<String, Object> records = consumer.poll(POLL_TIMEOUT);
            for (ConsumerRecord<String, Object> record : records) {
                if (record.offset() == offset) {
                    return record;
                }
            }
            throw new DltMessageNotFoundException(dltTopic, partition, offset);
        }
    }

    private DltResolveResult saveResolution(String originalTopic, String dltTopic,
                                            int partition, long offset,
                                            DltResolutionStatus resolution, String reason,
                                            String operatorInfo) {
        DltMessageResolutionJpaEntity entity = DltMessageResolutionJpaEntity.of(
                originalTopic, dltTopic, partition, offset,
                resolution, operatorInfo, LocalDateTime.now(), reason
        );
        try {
            resolutionRepository.save(entity);
        } catch (DataIntegrityViolationException dive) {
            log.info("동시 요청에 의한 DLT resolution 중복 저장. dltTopic={}, partition={}, offset={}",
                    dltTopic, partition, offset);
            Optional<DltMessageResolutionJpaEntity> existing =
                    resolutionRepository.findByDltTopicAndPartitionNumberAndOffsetNumber(
                            dltTopic, partition, offset);
            if (existing.isPresent()) {
                return new DltResolveResult(existing.get().getResolution(), true);
            }
        }
        return null;
    }
}
