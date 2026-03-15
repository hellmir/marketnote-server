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
    private Consumer<String, Object> consumer;

    @Test
    @DisplayName("DLT 토픽에서 메시지를 읽어 원본 토픽으로 재전송한다")
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

        // when
        DltReprocessResult result = dltReprocessService.reprocess(originalTopic);

        // then
        assertThat(result.reprocessedCount()).isEqualTo(2);
        assertThat(result.failedCount()).isEqualTo(0);
        verify(kafkaTemplate).send(originalTopic, "key-1", "value-1");
        verify(kafkaTemplate).send(originalTopic, "key-2", "value-2");
        verify(consumer).close();
    }

    @Test
    @DisplayName("DLT 토픽에 메시지가 없으면 reprocessedCount 0, failedCount 0을 반환한다")
    void reprocess_emptyDlt_returnsZero() {
        // given
        String originalTopic = "commerce.payment.approved";
        String dltTopic = originalTopic + ".dlt";

        when(consumerFactory.createConsumer(anyString(), eq(""))).thenReturn(consumer);

        PartitionInfo partitionInfo = new PartitionInfo(dltTopic, 0, null, null, null);
        when(consumer.partitionsFor(dltTopic)).thenReturn(List.of(partitionInfo));

        ConsumerRecords<String, Object> emptyRecords = new ConsumerRecords<>(Collections.emptyMap());
        when(consumer.poll(any(Duration.class))).thenReturn(emptyRecords);

        // when
        DltReprocessResult result = dltReprocessService.reprocess(originalTopic);

        // then
        assertThat(result.reprocessedCount()).isEqualTo(0);
        assertThat(result.failedCount()).isEqualTo(0);
        verify(kafkaTemplate, never()).send(anyString(), anyString(), any());
        verify(consumer).close();
    }

    @Test
    @DisplayName("DLT 토픽 파티션이 없으면 reprocessedCount 0, failedCount 0을 반환한다")
    void reprocess_noPartitions_returnsZero() {
        // given
        String originalTopic = "commerce.settlement.executed";
        String dltTopic = originalTopic + ".dlt";

        when(consumerFactory.createConsumer(anyString(), eq(""))).thenReturn(consumer);
        when(consumer.partitionsFor(dltTopic)).thenReturn(Collections.emptyList());

        // when
        DltReprocessResult result = dltReprocessService.reprocess(originalTopic);

        // then
        assertThat(result.reprocessedCount()).isEqualTo(0);
        assertThat(result.failedCount()).isEqualTo(0);
        verify(kafkaTemplate, never()).send(anyString(), anyString(), any());
        verify(consumer).close();
    }

    @Test
    @DisplayName("허용되지 않은 토픽명으로 재처리 시 InvalidDltTopicException이 발생한다")
    void reprocess_invalidTopic_throwsInvalidDltTopicException() {
        // given
        String invalidTopic = "unknown.topic";

        // when & then
        assertThatThrownBy(() -> dltReprocessService.reprocess(invalidTopic))
                .isInstanceOf(InvalidDltTopicException.class)
                .hasMessageContaining(invalidTopic);
    }

    @Test
    @DisplayName("메시지 재전송 실패 시 failedCount가 증가한다")
    void reprocess_sendFailure_incrementsFailedCount() {
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

        // when
        DltReprocessResult result = dltReprocessService.reprocess(originalTopic);

        // then
        assertThat(result.reprocessedCount()).isEqualTo(0);
        assertThat(result.failedCount()).isEqualTo(1);
        verify(consumer).close();
    }
}
