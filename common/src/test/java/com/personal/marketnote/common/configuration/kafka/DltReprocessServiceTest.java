package com.personal.marketnote.common.configuration.kafka;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DltReprocessService 테스트")
class DltReprocessServiceTest {
    @InjectMocks
    private DltReprocessService dltReprocessService;

    @Mock
    private ConsumerFactory<String, Object> consumerFactory;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private DltAuditLogger dltAuditLogger;

    @Mock
    private DltMetricsCollector dltMetricsCollector;

    @Mock
    private DltMessageResolutionJpaRepository resolutionRepository;

    @Mock
    private Consumer<String, Object> consumer;

    private static final String OPERATOR = "admin@personal.com";

    @Test
    @DisplayName("DLT 토픽에서 메시지를 읽어 원본 토픽으로 재전송하고 RETRIED 상태를 저장한다")
    void reprocess_pollsFromDltAndSendsToOriginalTopic() {
        // given
        String originalTopic = "commerce.order.payment-completed";
        String dltTopic = originalTopic + ".dlt";

        when(consumerFactory.createConsumer(anyString(), eq(""))).thenReturn(consumer);

        PartitionInfo partitionInfo = new PartitionInfo(dltTopic, 0, null, null, null);
        when(consumer.partitionsFor(dltTopic)).thenReturn(List.of(partitionInfo));

        TopicPartition topicPartition = new TopicPartition(dltTopic, 0);
        ConsumerRecord<String, Object> record1 = new ConsumerRecord<>(dltTopic, 0, 0L, "key-1", "value-1");
        ConsumerRecord<String, Object> record2 = new ConsumerRecord<>(dltTopic, 0, 1L, "key-2", "value-2");
        ConsumerRecords<String, Object> records = new ConsumerRecords<>(
                Map.of(topicPartition, List.of(record1, record2))
        );
        ConsumerRecords<String, Object> emptyRecords = new ConsumerRecords<>(Collections.emptyMap());

        when(consumer.poll(any(Duration.class)))
                .thenReturn(records)
                .thenReturn(emptyRecords);

        CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(null);
        when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(future);

        when(resolutionRepository.findByOriginalTopic(originalTopic)).thenReturn(List.of());

        // when
        DltReprocessResult result = dltReprocessService.reprocess(originalTopic, OPERATOR);

        // then
        assertThat(result.reprocessedCount()).isEqualTo(2);
        assertThat(result.failedCount()).isEqualTo(0);
        assertThat(result.skippedCount()).isEqualTo(0);
        verify(kafkaTemplate).send(originalTopic, "key-1", "value-1");
        verify(kafkaTemplate).send(originalTopic, "key-2", "value-2");
        verify(dltAuditLogger).logReprocessStart(originalTopic, OPERATOR);
        verify(dltAuditLogger).logReprocessComplete(originalTopic, OPERATOR, 2, 0);
        verify(dltMetricsCollector, times(2)).incrementDltReprocessCount(originalTopic, "success");
        verify(resolutionRepository, times(2)).save(any(DltMessageResolutionJpaEntity.class));
        verify(consumer).close();
    }

    @Test
    @DisplayName("DLT 토픽에 메시지가 없으면 reprocessedCount 0, failedCount 0, skippedCount 0을 반환한다")
    void reprocess_emptyDlt_returnsZero() {
        // given
        String originalTopic = "commerce.payment.approved";
        String dltTopic = originalTopic + ".dlt";

        when(consumerFactory.createConsumer(anyString(), eq(""))).thenReturn(consumer);

        PartitionInfo partitionInfo = new PartitionInfo(dltTopic, 0, null, null, null);
        when(consumer.partitionsFor(dltTopic)).thenReturn(List.of(partitionInfo));

        ConsumerRecords<String, Object> emptyRecords = new ConsumerRecords<>(Collections.emptyMap());
        when(consumer.poll(any(Duration.class))).thenReturn(emptyRecords);

        when(resolutionRepository.findByOriginalTopic(originalTopic)).thenReturn(List.of());

        // when
        DltReprocessResult result = dltReprocessService.reprocess(originalTopic, OPERATOR);

        // then
        assertThat(result.reprocessedCount()).isEqualTo(0);
        assertThat(result.failedCount()).isEqualTo(0);
        assertThat(result.skippedCount()).isEqualTo(0);
        verify(kafkaTemplate, never()).send(anyString(), anyString(), any());
        verify(dltAuditLogger).logReprocessStart(originalTopic, OPERATOR);
        verify(consumer).close();
    }

    @Test
    @DisplayName("DLT 토픽 파티션이 없으면 reprocessedCount 0, failedCount 0, skippedCount 0을 반환한다")
    void reprocess_noPartitions_returnsZero() {
        // given
        String originalTopic = "commerce.settlement.executed";
        String dltTopic = originalTopic + ".dlt";

        when(consumerFactory.createConsumer(anyString(), eq(""))).thenReturn(consumer);
        when(consumer.partitionsFor(dltTopic)).thenReturn(Collections.emptyList());

        // when
        DltReprocessResult result = dltReprocessService.reprocess(originalTopic, OPERATOR);

        // then
        assertThat(result.reprocessedCount()).isEqualTo(0);
        assertThat(result.failedCount()).isEqualTo(0);
        assertThat(result.skippedCount()).isEqualTo(0);
        verify(kafkaTemplate, never()).send(anyString(), anyString(), any());
        verify(dltAuditLogger).logReprocessStart(originalTopic, OPERATOR);
        verify(dltAuditLogger).logReprocessComplete(originalTopic, OPERATOR, 0, 0);
        verify(consumer).close();
    }

    @Test
    @DisplayName("허용되지 않은 토픽명으로 재처리 시 InvalidDltTopicException이 발생한다")
    void reprocess_invalidTopic_throwsInvalidDltTopicException() {
        // given
        String invalidTopic = "unknown.topic";

        // when & then
        assertThatThrownBy(() -> dltReprocessService.reprocess(invalidTopic, OPERATOR))
                .isInstanceOf(InvalidDltTopicException.class)
                .hasMessageContaining(invalidTopic);
    }

    @Test
    @DisplayName("메시지 재전송 실패 시 failedCount가 증가하고 RETRIED 상태를 저장하지 않는다")
    void reprocess_sendFailure_incrementsFailedCountAndDoesNotSaveResolution() {
        // given
        String originalTopic = "commerce.payment.cancelled";
        String dltTopic = originalTopic + ".dlt";

        when(consumerFactory.createConsumer(anyString(), eq(""))).thenReturn(consumer);

        PartitionInfo partitionInfo = new PartitionInfo(dltTopic, 0, null, null, null);
        when(consumer.partitionsFor(dltTopic)).thenReturn(List.of(partitionInfo));

        TopicPartition topicPartition = new TopicPartition(dltTopic, 0);
        ConsumerRecord<String, Object> record = new ConsumerRecord<>(dltTopic, 0, 0L, "key-1", "value-1");
        ConsumerRecords<String, Object> records = new ConsumerRecords<>(
                Map.of(topicPartition, List.of(record))
        );
        ConsumerRecords<String, Object> emptyRecords = new ConsumerRecords<>(Collections.emptyMap());

        when(consumer.poll(any(Duration.class)))
                .thenReturn(records)
                .thenReturn(emptyRecords);

        CompletableFuture<SendResult<String, Object>> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new RuntimeException("전송 실패"));
        when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(failedFuture);

        when(resolutionRepository.findByOriginalTopic(originalTopic)).thenReturn(List.of());

        // when
        DltReprocessResult result = dltReprocessService.reprocess(originalTopic, OPERATOR);

        // then
        assertThat(result.reprocessedCount()).isEqualTo(0);
        assertThat(result.failedCount()).isEqualTo(1);
        assertThat(result.skippedCount()).isEqualTo(0);
        verify(dltMetricsCollector).incrementDltReprocessCount(originalTopic, "failure");
        verify(dltAuditLogger).logReprocessComplete(originalTopic, OPERATOR, 0, 1);
        verify(resolutionRepository, never()).save(any(DltMessageResolutionJpaEntity.class));
        verify(consumer).close();
    }

    @Test
    @DisplayName("이미 처리된 메시지는 스킵하고 미처리 메시지만 재전송한다")
    void reprocess_skipsAlreadyResolvedMessages() {
        // given
        String originalTopic = "commerce.order.payment-completed";
        String dltTopic = originalTopic + ".dlt";

        when(consumerFactory.createConsumer(anyString(), eq(""))).thenReturn(consumer);

        PartitionInfo partitionInfo = new PartitionInfo(dltTopic, 0, null, null, null);
        when(consumer.partitionsFor(dltTopic)).thenReturn(List.of(partitionInfo));

        TopicPartition topicPartition = new TopicPartition(dltTopic, 0);
        ConsumerRecord<String, Object> record1 = new ConsumerRecord<>(dltTopic, 0, 0L, "key-1", "value-1");
        ConsumerRecord<String, Object> record2 = new ConsumerRecord<>(dltTopic, 0, 1L, "key-2", "value-2");
        ConsumerRecord<String, Object> record3 = new ConsumerRecord<>(dltTopic, 0, 2L, "key-3", "value-3");
        ConsumerRecords<String, Object> records = new ConsumerRecords<>(
                Map.of(topicPartition, List.of(record1, record2, record3))
        );
        ConsumerRecords<String, Object> emptyRecords = new ConsumerRecords<>(Collections.emptyMap());

        when(consumer.poll(any(Duration.class)))
                .thenReturn(records)
                .thenReturn(emptyRecords);

        CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(null);
        when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(future);

        DltMessageResolutionJpaEntity resolvedEntity = DltMessageResolutionJpaEntity.of(
                originalTopic, dltTopic, 0, 0L,
                DltResolutionStatus.RETRIED, OPERATOR, LocalDateTime.now()
        );
        when(resolutionRepository.findByOriginalTopic(originalTopic)).thenReturn(List.of(resolvedEntity));

        // when
        DltReprocessResult result = dltReprocessService.reprocess(originalTopic, OPERATOR);

        // then
        assertThat(result.reprocessedCount()).isEqualTo(2);
        assertThat(result.failedCount()).isEqualTo(0);
        assertThat(result.skippedCount()).isEqualTo(1);
        verify(kafkaTemplate).send(originalTopic, "key-2", "value-2");
        verify(kafkaTemplate).send(originalTopic, "key-3", "value-3");
        verify(kafkaTemplate, never()).send(originalTopic, "key-1", "value-1");
        verify(resolutionRepository, times(2)).save(any(DltMessageResolutionJpaEntity.class));
    }

    @Test
    @DisplayName("모든 메시지가 이미 처리된 경우 skippedCount만 증가한다")
    void reprocess_allMessagesAlreadyResolved_skipsAll() {
        // given
        String originalTopic = "commerce.payment.approved";
        String dltTopic = originalTopic + ".dlt";

        when(consumerFactory.createConsumer(anyString(), eq(""))).thenReturn(consumer);

        PartitionInfo partitionInfo = new PartitionInfo(dltTopic, 0, null, null, null);
        when(consumer.partitionsFor(dltTopic)).thenReturn(List.of(partitionInfo));

        TopicPartition topicPartition = new TopicPartition(dltTopic, 0);
        ConsumerRecord<String, Object> record1 = new ConsumerRecord<>(dltTopic, 0, 0L, "key-1", "value-1");
        ConsumerRecord<String, Object> record2 = new ConsumerRecord<>(dltTopic, 0, 1L, "key-2", "value-2");
        ConsumerRecords<String, Object> records = new ConsumerRecords<>(
                Map.of(topicPartition, List.of(record1, record2))
        );
        ConsumerRecords<String, Object> emptyRecords = new ConsumerRecords<>(Collections.emptyMap());

        when(consumer.poll(any(Duration.class)))
                .thenReturn(records)
                .thenReturn(emptyRecords);

        DltMessageResolutionJpaEntity resolved1 = DltMessageResolutionJpaEntity.of(
                originalTopic, dltTopic, 0, 0L,
                DltResolutionStatus.RETRIED, OPERATOR, LocalDateTime.now()
        );
        DltMessageResolutionJpaEntity resolved2 = DltMessageResolutionJpaEntity.of(
                originalTopic, dltTopic, 0, 1L,
                DltResolutionStatus.DISCARDED, OPERATOR, LocalDateTime.now()
        );
        when(resolutionRepository.findByOriginalTopic(originalTopic))
                .thenReturn(List.of(resolved1, resolved2));

        // when
        DltReprocessResult result = dltReprocessService.reprocess(originalTopic, OPERATOR);

        // then
        assertThat(result.reprocessedCount()).isEqualTo(0);
        assertThat(result.failedCount()).isEqualTo(0);
        assertThat(result.skippedCount()).isEqualTo(2);
        verify(kafkaTemplate, never()).send(anyString(), anyString(), any());
        verify(resolutionRepository, never()).save(any(DltMessageResolutionJpaEntity.class));
    }
}
