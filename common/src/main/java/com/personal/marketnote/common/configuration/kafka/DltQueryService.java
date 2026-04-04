package com.personal.marketnote.common.configuration.kafka;

import com.personal.marketnote.common.adapter.in.web.kafka.response.DltMessageResponse;
import com.personal.marketnote.common.adapter.in.web.kafka.response.DltTopicSummaryResponse;
import com.personal.marketnote.common.kafka.DltTopicRegistry;
import com.personal.marketnote.common.utility.FormatValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "spring.kafka", name = "bootstrap-servers")
public class DltQueryService {

    private static final Duration POLL_TIMEOUT = Duration.ofSeconds(2);
    private static final int MAX_POLL_ITERATIONS = 50;

    private final ConsumerFactory<String, Object> consumerFactory;
    private final DltMessageResolutionJpaRepository resolutionRepository;

    public List<DltMessageResponse> queryDltMessages(String originalTopic, int limit) {
        if (!DltTopicRegistry.isAllowed(originalTopic)) {
            throw new InvalidDltTopicException(originalTopic);
        }

        String dltTopic = DltTopicRegistry.toDltTopic(originalTopic);
        String groupId = "dlt-query-" + UUID.randomUUID();
        Map<String, String> resolutionMap = buildResolutionMap(originalTopic);

        try (Consumer<String, Object> consumer = consumerFactory.createConsumer(groupId, "")) {
            List<PartitionInfo> partitionInfos = consumer.partitionsFor(dltTopic);
            if (FormatValidator.hasNoValue(partitionInfos)) {
                return List.of();
            }

            List<TopicPartition> partitions = partitionInfos.stream()
                    .map(pi -> new TopicPartition(dltTopic, pi.partition()))
                    .toList();

            consumer.assign(partitions);
            consumer.seekToBeginning(partitions);

            List<DltMessageResponse> messages = new ArrayList<>();
            int pollIteration = 0;

            ConsumerRecords<String, Object> records = consumer.poll(POLL_TIMEOUT);
            while (!records.isEmpty() && messages.size() < limit && pollIteration < MAX_POLL_ITERATIONS) {
                for (ConsumerRecord<String, Object> record : records) {
                    if (messages.size() >= limit) {
                        break;
                    }
                    String resolution = resolveStatus(resolutionMap, record.partition(), record.offset());
                    messages.add(DltMessageResponse.from(record, resolution));
                }
                pollIteration++;
                if (messages.size() >= limit) {
                    break;
                }
                records = consumer.poll(POLL_TIMEOUT);
            }

            return messages;
        }
    }

    public List<DltTopicSummaryResponse> queryAllDltTopicSummaries() {
        String groupId = "dlt-summary-" + UUID.randomUUID();

        try (Consumer<String, Object> consumer = consumerFactory.createConsumer(groupId, "")) {
            List<DltTopicSummaryResponse> summaries = new ArrayList<>();

            for (String originalTopic : DltTopicRegistry.getAllOriginalTopics()) {
                String dltTopic = DltTopicRegistry.toDltTopic(originalTopic);
                long messageCount = calculateMessageCount(consumer, dltTopic);
                summaries.add(new DltTopicSummaryResponse(originalTopic, dltTopic, messageCount));
            }

            return summaries;
        }
    }

    private Map<String, String> buildResolutionMap(String originalTopic) {
        List<DltMessageResolutionJpaEntity> resolutions = resolutionRepository.findByOriginalTopic(originalTopic);
        Map<String, String> map = new HashMap<>();
        for (DltMessageResolutionJpaEntity entity : resolutions) {
            map.put(entity.toResolutionKey(), entity.getResolution().name());
        }
        return map;
    }

    private String resolveStatus(Map<String, String> resolutionMap, int partition, long offset) {
        String key = partition + ":" + offset;
        return resolutionMap.getOrDefault(key, DltResolutionStatus.UNRESOLVED);
    }

    private long calculateMessageCount(Consumer<String, Object> consumer, String dltTopic) {
        List<PartitionInfo> partitionInfos = consumer.partitionsFor(dltTopic);
        if (FormatValidator.hasNoValue(partitionInfos)) {
            return 0L;
        }

        List<TopicPartition> partitions = partitionInfos.stream()
                .map(pi -> new TopicPartition(dltTopic, pi.partition()))
                .toList();

        Map<TopicPartition, Long> beginOffsets = consumer.beginningOffsets(partitions);
        Map<TopicPartition, Long> endOffsets = consumer.endOffsets(partitions);

        long totalCount = 0L;
        for (TopicPartition partition : partitions) {
            long begin = beginOffsets.getOrDefault(partition, 0L);
            long end = endOffsets.getOrDefault(partition, 0L);
            totalCount += Math.max(0L, end - begin);
        }

        return totalCount;
    }
}
