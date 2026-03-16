package com.personal.marketnote.common.outbox.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.outbox.OutboxEventStatus;
import com.personal.marketnote.common.outbox.entity.OutboxEventJpaEntity;
import com.personal.marketnote.common.outbox.repository.OutboxEventJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OutboxEventPublisher 테스트")
class OutboxEventPublisherTest {
    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2026-03-15T10:00:00Z"), ZoneId.of("Asia/Seoul")
    );

    @InjectMocks
    private OutboxEventPublisher outboxEventPublisher;

    @Mock
    private OutboxEventJpaRepository outboxEventJpaRepository;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private Clock clock;

    @Test
    @DisplayName("PENDING 이벤트를 Kafka로 발행하고 PUBLISHED 상태로 변경한다")
    void publishPendingEvents_publishesToKafkaAndMarksPublished() throws Exception {
        // given
        OutboxEventJpaEntity entity = createPendingEntity("event-1", "commerce.payment.approved", "order-123", "{\"orderId\":123}");

        when(outboxEventJpaRepository.findTop100ByStatusOrderByCreatedAtAsc(OutboxEventStatus.PENDING))
                .thenReturn(List.of(entity));

        Object deserializedPayload = new Object();
        when(objectMapper.readValue("{\"orderId\":123}", Object.class)).thenReturn(deserializedPayload);

        CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(null);
        when(kafkaTemplate.send("commerce.payment.approved", "order-123", deserializedPayload)).thenReturn(future);

        when(clock.instant()).thenReturn(FIXED_CLOCK.instant());
        when(clock.getZone()).thenReturn(FIXED_CLOCK.getZone());

        // when
        outboxEventPublisher.publishPendingEvents();

        // then
        assertThat(entity.getStatus()).isEqualTo(OutboxEventStatus.PUBLISHED);
        assertThat(entity.getPublishedAt()).isNotNull();
        verify(outboxEventJpaRepository).save(entity);
    }

    @Test
    @DisplayName("PENDING 이벤트가 없으면 Kafka 발행을 수행하지 않는다")
    void publishPendingEvents_noPendingEvents_doesNotPublish() {
        // given
        when(outboxEventJpaRepository.findTop100ByStatusOrderByCreatedAtAsc(OutboxEventStatus.PENDING))
                .thenReturn(Collections.emptyList());

        // when
        outboxEventPublisher.publishPendingEvents();

        // then
        verifyNoInteractions(kafkaTemplate);
        verify(outboxEventJpaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Kafka 발행 실패 시 retryCount를 증가시킨다")
    void publishPendingEvents_kafkaSendFails_incrementsRetryCount() throws Exception {
        // given
        OutboxEventJpaEntity entity = createPendingEntity("event-1", "commerce.payment.approved", "order-123", "{}");

        when(outboxEventJpaRepository.findTop100ByStatusOrderByCreatedAtAsc(OutboxEventStatus.PENDING))
                .thenReturn(List.of(entity));

        when(objectMapper.readValue("{}", Object.class)).thenReturn(new Object());

        CompletableFuture<SendResult<String, Object>> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new RuntimeException("Kafka 전송 실패"));
        when(kafkaTemplate.send(eq("commerce.payment.approved"), eq("order-123"), any())).thenReturn(failedFuture);

        // when
        outboxEventPublisher.publishPendingEvents();

        // then
        assertThat(entity.getRetryCount()).isEqualTo(1);
        assertThat(entity.getStatus()).isEqualTo(OutboxEventStatus.PENDING);
        verify(outboxEventJpaRepository).save(entity);
    }

    @Test
    @DisplayName("최대 재시도 초과 시 FAILED 상태로 변경한다")
    void publishPendingEvents_maxRetriesExceeded_marksAsFailed() throws Exception {
        // given
        OutboxEventJpaEntity entity = createPendingEntity("event-1", "commerce.payment.approved", "order-123", "{}");
        for (int i = 0; i < 4; i++) {
            entity.incrementRetry();
        }

        when(outboxEventJpaRepository.findTop100ByStatusOrderByCreatedAtAsc(OutboxEventStatus.PENDING))
                .thenReturn(List.of(entity));

        when(objectMapper.readValue("{}", Object.class)).thenReturn(new Object());

        CompletableFuture<SendResult<String, Object>> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new RuntimeException("Kafka 전송 실패"));
        when(kafkaTemplate.send(eq("commerce.payment.approved"), eq("order-123"), any())).thenReturn(failedFuture);

        // when
        outboxEventPublisher.publishPendingEvents();

        // then
        assertThat(entity.getRetryCount()).isEqualTo(5);
        assertThat(entity.getStatus()).isEqualTo(OutboxEventStatus.FAILED);
        verify(outboxEventJpaRepository).save(entity);
    }

    @Test
    @DisplayName("payload JSON 역직렬화 실패 시 retryCount를 증가시킨다")
    void publishPendingEvents_invalidPayload_incrementsRetryCount() throws Exception {
        // given
        OutboxEventJpaEntity entity = createPendingEntity("event-1", "commerce.payment.approved", "order-123", "invalid-json");

        when(outboxEventJpaRepository.findTop100ByStatusOrderByCreatedAtAsc(OutboxEventStatus.PENDING))
                .thenReturn(List.of(entity));

        when(objectMapper.readValue("invalid-json", Object.class))
                .thenThrow(new JsonProcessingException("parse error") {
                });

        // when
        outboxEventPublisher.publishPendingEvents();

        // then
        assertThat(entity.getRetryCount()).isEqualTo(1);
        assertThat(entity.getStatus()).isEqualTo(OutboxEventStatus.PENDING);
        verify(outboxEventJpaRepository).save(entity);
        verifyNoInteractions(kafkaTemplate);
    }

    @Test
    @DisplayName("다건 이벤트 처리 시 하나가 실패해도 나머지는 정상 발행된다")
    void publishPendingEvents_multipleEvents_processesIndependently() throws Exception {
        // given
        OutboxEventJpaEntity successEntity = createPendingEntity("event-1", "commerce.payment.approved", "order-1", "{\"orderId\":1}");
        OutboxEventJpaEntity failEntity = createPendingEntity("event-2", "commerce.payment.cancelled", "order-2", "{\"orderId\":2}");

        when(outboxEventJpaRepository.findTop100ByStatusOrderByCreatedAtAsc(OutboxEventStatus.PENDING))
                .thenReturn(List.of(successEntity, failEntity));

        Object successPayload = new Object();
        when(objectMapper.readValue("{\"orderId\":1}", Object.class)).thenReturn(successPayload);
        Object failPayload = new Object();
        when(objectMapper.readValue("{\"orderId\":2}", Object.class)).thenReturn(failPayload);

        CompletableFuture<SendResult<String, Object>> successFuture = CompletableFuture.completedFuture(null);
        when(kafkaTemplate.send("commerce.payment.approved", "order-1", successPayload)).thenReturn(successFuture);

        CompletableFuture<SendResult<String, Object>> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new RuntimeException("Kafka 전송 실패"));
        when(kafkaTemplate.send("commerce.payment.cancelled", "order-2", failPayload)).thenReturn(failedFuture);

        when(clock.instant()).thenReturn(FIXED_CLOCK.instant());
        when(clock.getZone()).thenReturn(FIXED_CLOCK.getZone());

        // when
        outboxEventPublisher.publishPendingEvents();

        // then
        assertThat(successEntity.getStatus()).isEqualTo(OutboxEventStatus.PUBLISHED);
        assertThat(successEntity.getPublishedAt()).isNotNull();
        assertThat(failEntity.getRetryCount()).isEqualTo(1);
        assertThat(failEntity.getStatus()).isEqualTo(OutboxEventStatus.PENDING);
        verify(outboxEventJpaRepository, times(2)).save(any());
    }

    private OutboxEventJpaEntity createPendingEntity(String eventId, String topic, String partitionKey, String payload) {
        com.personal.marketnote.common.outbox.OutboxEvent domainEvent = com.personal.marketnote.common.outbox.OutboxEvent.of(
                eventId, topic, partitionKey, "TestEvent", "test-service", payload, FIXED_CLOCK
        );
        return OutboxEventJpaEntity.from(domainEvent);
    }
}
