package com.personal.marketnote.common.configuration.kafka;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DltResolveService 테스트")
class DltResolveServiceTest {
    @InjectMocks
    private DltResolveService dltResolveService;

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
    @DisplayName("RETRY 액션으로 DLT 메시지를 원본 토픽에 재발행하고 RETRIED 상태와 reason을 저장한다")
    void resolve_retryAction_sendsToOriginalTopicAndSavesRetriedWithReason() {
        // given
        String originalTopic = "commerce.order.payment-completed";
        String dltTopic = originalTopic + ".dlt";
        String reason = "일시적 DB 타임아웃 복구";
        DltResolveCommand command = new DltResolveCommand(
                originalTopic, 0, 5L, DltResolutionAction.RETRY, reason);

        when(resolutionRepository.findByDltTopicAndPartitionNumberAndOffsetNumber(dltTopic, 0, 5L))
                .thenReturn(Optional.empty());
        when(consumerFactory.createConsumer(anyString(), eq(""))).thenReturn(consumer);

        TopicPartition topicPartition = new TopicPartition(dltTopic, 0);
        ConsumerRecord<String, Object> record = new ConsumerRecord<>(dltTopic, 0, 5L, "key-1", "value-1");
        ConsumerRecords<String, Object> records = new ConsumerRecords<>(
                Map.of(topicPartition, List.of(record)));
        when(consumer.poll(any(Duration.class))).thenReturn(records);

        CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(null);
        when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(future);

        // when
        DltResolveResult result = dltResolveService.resolve(command, OPERATOR);

        // then
        assertThat(result.resolution()).isEqualTo(DltResolutionStatus.RETRIED);
        assertThat(result.alreadyResolved()).isFalse();
        verify(kafkaTemplate).send(originalTopic, "key-1", "value-1");

        ArgumentCaptor<DltMessageResolutionJpaEntity> captor =
                ArgumentCaptor.forClass(DltMessageResolutionJpaEntity.class);
        verify(resolutionRepository).save(captor.capture());
        DltMessageResolutionJpaEntity saved = captor.getValue();
        assertThat(saved.getOriginalTopic()).isEqualTo(originalTopic);
        assertThat(saved.getDltTopic()).isEqualTo(dltTopic);
        assertThat(saved.getPartitionNumber()).isEqualTo(0);
        assertThat(saved.getOffsetNumber()).isEqualTo(5L);
        assertThat(saved.getResolution()).isEqualTo(DltResolutionStatus.RETRIED);
        assertThat(saved.getResolvedBy()).isEqualTo(OPERATOR);
        assertThat(saved.getReason()).isEqualTo(reason);

        verify(dltAuditLogger).logResolve(originalTopic, 0, 5L, "RETRY", reason, OPERATOR);
        verify(dltMetricsCollector).incrementDltResolveCount(originalTopic, "retry");
        verify(consumer).close();
    }

    @Test
    @DisplayName("DISCARD 액션으로 재발행 없이 DISCARDED 상태와 reason을 저장한다")
    void resolve_discardAction_savesDiscardedWithoutSending() {
        // given
        String originalTopic = "commerce.payment.approved";
        String dltTopic = originalTopic + ".dlt";
        String reason = "데이터 정합성 오류 — 수동 보정 완료";
        DltResolveCommand command = new DltResolveCommand(
                originalTopic, 1, 10L, DltResolutionAction.DISCARD, reason);

        when(resolutionRepository.findByDltTopicAndPartitionNumberAndOffsetNumber(dltTopic, 1, 10L))
                .thenReturn(Optional.empty());

        // when
        DltResolveResult result = dltResolveService.resolve(command, OPERATOR);

        // then
        assertThat(result.resolution()).isEqualTo(DltResolutionStatus.DISCARDED);
        assertThat(result.alreadyResolved()).isFalse();
        verify(kafkaTemplate, never()).send(anyString(), anyString(), any());
        verify(consumerFactory, never()).createConsumer(anyString(), anyString());

        ArgumentCaptor<DltMessageResolutionJpaEntity> captor =
                ArgumentCaptor.forClass(DltMessageResolutionJpaEntity.class);
        verify(resolutionRepository).save(captor.capture());
        DltMessageResolutionJpaEntity saved = captor.getValue();
        assertThat(saved.getResolution()).isEqualTo(DltResolutionStatus.DISCARDED);
        assertThat(saved.getReason()).isEqualTo(reason);

        verify(dltAuditLogger).logResolve(originalTopic, 1, 10L, "DISCARD", reason, OPERATOR);
        verify(dltMetricsCollector).incrementDltResolveCount(originalTopic, "discard");
    }

    @Test
    @DisplayName("이미 처리된 메시지는 기존 상태를 반환하고 alreadyResolved가 true이다")
    void resolve_alreadyResolved_returnsExistingResolution() {
        // given
        String originalTopic = "commerce.payment.cancelled";
        String dltTopic = originalTopic + ".dlt";
        DltResolveCommand command = new DltResolveCommand(
                originalTopic, 0, 3L, DltResolutionAction.RETRY, "재시도");

        DltMessageResolutionJpaEntity existingEntity = DltMessageResolutionJpaEntity.of(
                originalTopic, dltTopic, 0, 3L,
                DltResolutionStatus.DISCARDED, "other-admin", LocalDateTime.now(), "이전 폐기 사유");
        when(resolutionRepository.findByDltTopicAndPartitionNumberAndOffsetNumber(dltTopic, 0, 3L))
                .thenReturn(Optional.of(existingEntity));

        // when
        DltResolveResult result = dltResolveService.resolve(command, OPERATOR);

        // then
        assertThat(result.resolution()).isEqualTo(DltResolutionStatus.DISCARDED);
        assertThat(result.alreadyResolved()).isTrue();
        verify(kafkaTemplate, never()).send(anyString(), anyString(), any());
        verify(resolutionRepository, never()).save(any(DltMessageResolutionJpaEntity.class));
        verify(dltAuditLogger).logResolveAlreadyResolved(originalTopic, 0, 3L, "DISCARDED", OPERATOR);
    }

    @Test
    @DisplayName("허용되지 않은 토픽명으로 해결 시 InvalidDltTopicException이 발생한다")
    void resolve_invalidTopic_throwsInvalidDltTopicException() {
        // given
        DltResolveCommand command = new DltResolveCommand(
                "unknown.topic", 0, 0L, DltResolutionAction.RETRY, null);

        // when & then
        assertThatThrownBy(() -> dltResolveService.resolve(command, OPERATOR))
                .isInstanceOf(InvalidDltTopicException.class)
                .hasMessageContaining("unknown.topic");
    }

    @Test
    @DisplayName("RETRY 시 DLT 토픽에서 메시지를 찾을 수 없으면 DltMessageNotFoundException이 발생한다")
    void resolve_retryMessageNotFound_throwsDltMessageNotFoundException() {
        // given
        String originalTopic = "commerce.settlement.executed";
        String dltTopic = originalTopic + ".dlt";
        DltResolveCommand command = new DltResolveCommand(
                originalTopic, 0, 99L, DltResolutionAction.RETRY, null);

        when(resolutionRepository.findByDltTopicAndPartitionNumberAndOffsetNumber(dltTopic, 0, 99L))
                .thenReturn(Optional.empty());
        when(consumerFactory.createConsumer(anyString(), eq(""))).thenReturn(consumer);

        ConsumerRecords<String, Object> emptyRecords = new ConsumerRecords<>(Collections.emptyMap());
        when(consumer.poll(any(Duration.class))).thenReturn(emptyRecords);

        // when & then
        assertThatThrownBy(() -> dltResolveService.resolve(command, OPERATOR))
                .isInstanceOf(DltMessageNotFoundException.class)
                .hasMessageContaining(dltTopic)
                .hasMessageContaining("99");
        verify(resolutionRepository, never()).save(any(DltMessageResolutionJpaEntity.class));
        verify(consumer).close();
    }

    @Test
    @DisplayName("RETRY 시 메시지 재전송 실패하면 DltResolveFailedException이 발생하고 감사 에러 로그를 기록한다")
    void resolve_retrySendFailure_throwsDltResolveFailedExceptionAndLogsError() {
        // given
        String originalTopic = "commerce.order.payment-completed";
        String dltTopic = originalTopic + ".dlt";
        DltResolveCommand command = new DltResolveCommand(
                originalTopic, 0, 7L, DltResolutionAction.RETRY, "재시도");

        when(resolutionRepository.findByDltTopicAndPartitionNumberAndOffsetNumber(dltTopic, 0, 7L))
                .thenReturn(Optional.empty());
        when(consumerFactory.createConsumer(anyString(), eq(""))).thenReturn(consumer);

        TopicPartition topicPartition = new TopicPartition(dltTopic, 0);
        ConsumerRecord<String, Object> record = new ConsumerRecord<>(dltTopic, 0, 7L, "key-1", "value-1");
        ConsumerRecords<String, Object> records = new ConsumerRecords<>(
                Map.of(topicPartition, List.of(record)));
        when(consumer.poll(any(Duration.class))).thenReturn(records);

        CompletableFuture<SendResult<String, Object>> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new RuntimeException("전송 실패"));
        when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(failedFuture);

        // when & then
        assertThatThrownBy(() -> dltResolveService.resolve(command, OPERATOR))
                .isInstanceOf(DltResolveFailedException.class)
                .hasMessageContaining(originalTopic);
        verify(resolutionRepository, never()).save(any(DltMessageResolutionJpaEntity.class));
        verify(dltAuditLogger).logResolveError(
                eq(originalTopic), eq(0), eq(7L), eq("RETRY"), eq(OPERATOR), any(Exception.class));
        verify(consumer).close();
    }

    @Test
    @DisplayName("DISCARD 시 reason이 null이어도 정상 처리된다")
    void resolve_discardWithNullReason_savesSuccessfully() {
        // given
        String originalTopic = "user.user.signup-completed";
        String dltTopic = originalTopic + ".dlt";
        DltResolveCommand command = new DltResolveCommand(
                originalTopic, 0, 0L, DltResolutionAction.DISCARD, null);

        when(resolutionRepository.findByDltTopicAndPartitionNumberAndOffsetNumber(dltTopic, 0, 0L))
                .thenReturn(Optional.empty());

        // when
        DltResolveResult result = dltResolveService.resolve(command, OPERATOR);

        // then
        assertThat(result.resolution()).isEqualTo(DltResolutionStatus.DISCARDED);
        assertThat(result.alreadyResolved()).isFalse();

        ArgumentCaptor<DltMessageResolutionJpaEntity> captor =
                ArgumentCaptor.forClass(DltMessageResolutionJpaEntity.class);
        verify(resolutionRepository).save(captor.capture());
        assertThat(captor.getValue().getReason()).isNull();
    }
}
