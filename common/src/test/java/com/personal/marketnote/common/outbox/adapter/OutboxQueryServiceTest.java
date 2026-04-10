package com.personal.marketnote.common.outbox.adapter;

import com.personal.marketnote.common.outbox.OutboxEventStatus;
import com.personal.marketnote.common.outbox.adapter.in.web.response.OutboxEventResponse;
import com.personal.marketnote.common.outbox.adapter.in.web.response.OutboxTopicSummaryResponse;
import com.personal.marketnote.common.outbox.entity.OutboxEventJpaEntity;
import com.personal.marketnote.common.outbox.repository.OutboxEventJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OutboxQueryService 테스트")
class OutboxQueryServiceTest {
    private static final LocalDateTime FIXED_TIME = LocalDateTime.of(2026, 4, 2, 10, 0, 0);

    @InjectMocks
    private OutboxQueryService outboxQueryService;

    @Mock
    private OutboxEventJpaRepository outboxEventJpaRepository;

    @Test
    @DisplayName("FAILED 이벤트가 존재하면 목록을 반환한다")
    void queryFailedEvents_returnsListWhenFailedEventsExist() {
        // given
        OutboxEventJpaEntity entity = createFailedEntity(
                1L, "event-1", "commerce.payment.approved", "order-123",
                "PaymentApproved", "commerce-service",
                5, 5, FIXED_TIME, FIXED_TIME, "Kafka send failed"
        );
        when(outboxEventJpaRepository.findByStatusOrderByCreatedAtAsc(OutboxEventStatus.FAILED))
                .thenReturn(List.of(entity));

        // when
        List<OutboxEventResponse> result = outboxQueryService.queryFailedEvents(null);

        // then
        assertThat(result).hasSize(1);
        OutboxEventResponse response = result.get(0);
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.eventId()).isEqualTo("event-1");
        assertThat(response.topic()).isEqualTo("commerce.payment.approved");
        assertThat(response.partitionKey()).isEqualTo("order-123");
        assertThat(response.eventType()).isEqualTo("PaymentApproved");
        assertThat(response.source()).isEqualTo("commerce-service");
        assertThat(response.retryCount()).isEqualTo(5);
        assertThat(response.maxRetries()).isEqualTo(5);
        assertThat(response.createdAt()).isEqualTo(FIXED_TIME);
        assertThat(response.failedAt()).isEqualTo(FIXED_TIME);
        assertThat(response.lastErrorMessage()).isEqualTo("Kafka send failed");
        verify(outboxEventJpaRepository).findByStatusOrderByCreatedAtAsc(OutboxEventStatus.FAILED);
    }

    @Test
    @DisplayName("토픽 필터를 적용하면 해당 토픽의 FAILED 이벤트만 반환한다")
    void queryFailedEvents_returnsFilteredListWhenTopicProvided() {
        // given
        String targetTopic = "commerce.payment.approved";
        OutboxEventJpaEntity entity = createFailedEntity(
                2L, "event-2", targetTopic, "order-456",
                "PaymentApproved", "commerce-service",
                3, 5, FIXED_TIME, FIXED_TIME, "Connection timeout"
        );
        when(outboxEventJpaRepository.findByStatusAndTopicOrderByCreatedAtAsc(OutboxEventStatus.FAILED, targetTopic))
                .thenReturn(List.of(entity));

        // when
        List<OutboxEventResponse> result = outboxQueryService.queryFailedEvents(targetTopic);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).topic()).isEqualTo(targetTopic);
        verify(outboxEventJpaRepository).findByStatusAndTopicOrderByCreatedAtAsc(OutboxEventStatus.FAILED, targetTopic);
        verifyNoMoreInteractions(outboxEventJpaRepository);
    }

    @Test
    @DisplayName("FAILED 이벤트가 없으면 빈 목록을 반환한다")
    void queryFailedEvents_returnsEmptyListWhenNoFailedEvents() {
        // given
        when(outboxEventJpaRepository.findByStatusOrderByCreatedAtAsc(OutboxEventStatus.FAILED))
                .thenReturn(List.of());

        // when
        List<OutboxEventResponse> result = outboxQueryService.queryFailedEvents(null);

        // then
        assertThat(result).isEmpty();
        verify(outboxEventJpaRepository).findByStatusOrderByCreatedAtAsc(OutboxEventStatus.FAILED);
    }

    @Test
    @DisplayName("토픽별 FAILED 요약을 반환한다")
    void queryFailedSummary_returnsTopicSummaries() {
        // given
        List<Object[]> rows = List.of(
                new Object[]{"commerce.order.created", 1L},
                new Object[]{"commerce.payment.approved", 3L}
        );
        when(outboxEventJpaRepository.countByStatusGroupByTopic(OutboxEventStatus.FAILED))
                .thenReturn(rows);

        // when
        List<OutboxTopicSummaryResponse> result = outboxQueryService.queryFailedSummary();

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).topic()).isEqualTo("commerce.order.created");
        assertThat(result.get(0).failedCount()).isEqualTo(1L);
        assertThat(result.get(1).topic()).isEqualTo("commerce.payment.approved");
        assertThat(result.get(1).failedCount()).isEqualTo(3L);
        verify(outboxEventJpaRepository).countByStatusGroupByTopic(OutboxEventStatus.FAILED);
    }

    @Test
    @DisplayName("FAILED 이벤트가 없으면 빈 요약을 반환한다")
    void queryFailedSummary_returnsEmptyWhenNoFailedEvents() {
        // given
        when(outboxEventJpaRepository.countByStatusGroupByTopic(OutboxEventStatus.FAILED))
                .thenReturn(List.of());

        // when
        List<OutboxTopicSummaryResponse> result = outboxQueryService.queryFailedSummary();

        // then
        assertThat(result).isEmpty();
        verify(outboxEventJpaRepository).countByStatusGroupByTopic(OutboxEventStatus.FAILED);
        verifyNoMoreInteractions(outboxEventJpaRepository);
    }

    private OutboxEventJpaEntity createFailedEntity(Long id, String eventId, String topic, String partitionKey,
                                                    String eventType, String source,
                                                    int retryCount, int maxRetries,
                                                    LocalDateTime createdAt, LocalDateTime failedAt,
                                                    String lastErrorMessage) {
        OutboxEventJpaEntity entity = mock(OutboxEventJpaEntity.class);
        when(entity.getId()).thenReturn(id);
        when(entity.getEventId()).thenReturn(eventId);
        when(entity.getTopic()).thenReturn(topic);
        when(entity.getPartitionKey()).thenReturn(partitionKey);
        when(entity.getEventType()).thenReturn(eventType);
        when(entity.getSource()).thenReturn(source);
        when(entity.getRetryCount()).thenReturn(retryCount);
        when(entity.getMaxRetries()).thenReturn(maxRetries);
        when(entity.getCreatedAt()).thenReturn(createdAt);
        when(entity.getFailedAt()).thenReturn(failedAt);
        when(entity.getLastErrorMessage()).thenReturn(lastErrorMessage);
        return entity;
    }
}
