package com.personal.marketnote.common.configuration.kafka;

import com.personal.marketnote.common.adapter.in.web.kafka.response.DltMessageResponse;
import com.personal.marketnote.common.adapter.in.web.kafka.response.DltTopicSummaryResponse;
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

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DltQueryService 테스트")
class DltQueryServiceTest {

    @InjectMocks
    private DltQueryService dltQueryService;

    @Mock
    private ConsumerFactory<String, Object> consumerFactory;

    @Mock
    private Consumer<String, Object> consumer;

    @Test
    @DisplayName("DLT 토픽에서 메시지를 조회한다")
    void queryDltMessages_returnsMessages() {
        // given
        String originalTopic = "commerce.order.payment-completed";
        String dltTopic = originalTopic + ".dlt";

        when(consumerFactory.createConsumer(anyString(), eq(""))).thenReturn(consumer);

        PartitionInfo partitionInfo = new PartitionInfo(dltTopic, 0, null, null, null);
        when(consumer.partitionsFor(dltTopic)).thenReturn(List.of(partitionInfo));

        TopicPartition topicPartition = new TopicPartition(dltTopic, 0);
        ConsumerRecord<String, Object> record = buildDltRecord(dltTopic, "key-1", originalTopic,
                "java.lang.RuntimeException", "DB 연결 오류");
        ConsumerRecords<String, Object> records = new ConsumerRecords<>(
                Map.of(topicPartition, List.of(record))
        );
        ConsumerRecords<String, Object> emptyRecords = new ConsumerRecords<>(Collections.emptyMap());

        when(consumer.poll(any(Duration.class)))
                .thenReturn(records)
                .thenReturn(emptyRecords);

        // when
        List<DltMessageResponse> result = dltQueryService.queryDltMessages(originalTopic, 100);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).dltTopic()).isEqualTo(dltTopic);
        assertThat(result.get(0).key()).isEqualTo("key-1");
        assertThat(result.get(0).originalTopic()).isEqualTo(originalTopic);
        assertThat(result.get(0).errorFqcn()).isEqualTo("RuntimeException");
        assertThat(result.get(0).errorMessage()).isEqualTo("DB 연결 오류");
        verify(consumer).close();
    }

    @Test
    @DisplayName("DLT 토픽에 메시지가 없으면 빈 리스트를 반환한다")
    void queryDltMessages_emptyDlt_returnsEmptyList() {
        // given
        String originalTopic = "commerce.payment.approved";
        String dltTopic = originalTopic + ".dlt";

        when(consumerFactory.createConsumer(anyString(), eq(""))).thenReturn(consumer);

        PartitionInfo partitionInfo = new PartitionInfo(dltTopic, 0, null, null, null);
        when(consumer.partitionsFor(dltTopic)).thenReturn(List.of(partitionInfo));

        ConsumerRecords<String, Object> emptyRecords = new ConsumerRecords<>(Collections.emptyMap());
        when(consumer.poll(any(Duration.class))).thenReturn(emptyRecords);

        // when
        List<DltMessageResponse> result = dltQueryService.queryDltMessages(originalTopic, 100);

        // then
        assertThat(result).isEmpty();
        verify(consumer).close();
    }

    @Test
    @DisplayName("DLT 토픽 파티션이 없으면 빈 리스트를 반환한다")
    void queryDltMessages_noPartitions_returnsEmptyList() {
        // given
        String originalTopic = "commerce.settlement.executed";
        String dltTopic = originalTopic + ".dlt";

        when(consumerFactory.createConsumer(anyString(), eq(""))).thenReturn(consumer);
        when(consumer.partitionsFor(dltTopic)).thenReturn(Collections.emptyList());

        // when
        List<DltMessageResponse> result = dltQueryService.queryDltMessages(originalTopic, 100);

        // then
        assertThat(result).isEmpty();
        verify(consumer).close();
    }

    @Test
    @DisplayName("허용되지 않은 토픽명으로 조회 시 InvalidDltTopicException이 발생한다")
    void queryDltMessages_invalidTopic_throwsException() {
        // given
        String invalidTopic = "unknown.topic";

        // when & then
        assertThatThrownBy(() -> dltQueryService.queryDltMessages(invalidTopic, 100))
                .isInstanceOf(InvalidDltTopicException.class)
                .hasMessageContaining(invalidTopic);
    }

    @Test
    @DisplayName("limit 이하의 메시지만 조회한다")
    void queryDltMessages_respectsLimit() {
        // given
        String originalTopic = "commerce.order.payment-completed";
        String dltTopic = originalTopic + ".dlt";

        when(consumerFactory.createConsumer(anyString(), eq(""))).thenReturn(consumer);

        PartitionInfo partitionInfo = new PartitionInfo(dltTopic, 0, null, null, null);
        when(consumer.partitionsFor(dltTopic)).thenReturn(List.of(partitionInfo));

        TopicPartition topicPartition = new TopicPartition(dltTopic, 0);
        ConsumerRecord<String, Object> record1 = buildDltRecord(dltTopic, "key-1", originalTopic, "Error", "msg1");
        ConsumerRecord<String, Object> record2 = buildDltRecord(dltTopic, "key-2", originalTopic, "Error", "msg2");
        ConsumerRecord<String, Object> record3 = buildDltRecord(dltTopic, "key-3", originalTopic, "Error", "msg3");
        ConsumerRecords<String, Object> records = new ConsumerRecords<>(
                Map.of(topicPartition, List.of(record1, record2, record3))
        );

        when(consumer.poll(any(Duration.class))).thenReturn(records);

        // when
        List<DltMessageResponse> result = dltQueryService.queryDltMessages(originalTopic, 2);

        // then
        assertThat(result).hasSize(2);
        verify(consumer).close();
    }

    @Test
    @DisplayName("전체 DLT 토픽 요약을 조회한다")
    void queryAllDltTopicSummaries_returnsSummaries() {
        // given
        when(consumerFactory.createConsumer(anyString(), eq(""))).thenReturn(consumer);

        TopicPartition partition = new TopicPartition("commerce.order.payment-completed.dlt", 0);
        PartitionInfo partitionInfo = new PartitionInfo("commerce.order.payment-completed.dlt", 0, null, null, null);

        // 모든 DLT 토픽에 대해 partitionsFor 호출 시 반환
        when(consumer.partitionsFor(anyString()))
                .thenReturn(List.of(partitionInfo));

        when(consumer.beginningOffsets(anyCollection()))
                .thenReturn(Map.of(partition, 0L));
        when(consumer.endOffsets(anyCollection()))
                .thenReturn(Map.of(partition, 5L));

        // when
        List<DltTopicSummaryResponse> result = dltQueryService.queryAllDltTopicSummaries();

        // then
        assertThat(result).isNotEmpty();
        assertThat(result).allSatisfy(summary -> {
            assertThat(summary.dltTopic()).endsWith(".dlt");
            assertThat(summary.originalTopic()).doesNotEndWith(".dlt");
        });
        verify(consumer).close();
    }

    private ConsumerRecord<String, Object> buildDltRecord(
            String dltTopic, String key, String originalTopic, String errorFqcn, String errorMessage
    ) {
        ConsumerRecord<String, Object> record = new ConsumerRecord<>(dltTopic, 0, 0L, key, "value");
        record.headers()
                .add("kafka_dlt-original-topic", originalTopic.getBytes(StandardCharsets.UTF_8))
                .add("kafka_dlt-exception-fqcn", errorFqcn.getBytes(StandardCharsets.UTF_8))
                .add("kafka_dlt-exception-message", errorMessage.getBytes(StandardCharsets.UTF_8));
        return record;
    }
}
