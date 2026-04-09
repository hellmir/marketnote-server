package com.personal.marketnote.common.outbox.adapter;

import com.personal.marketnote.common.outbox.OutboxEventStatus;
import com.personal.marketnote.common.outbox.OutboxResolutionAction;
import com.personal.marketnote.common.outbox.adapter.command.OutboxResolveCommand;
import com.personal.marketnote.common.outbox.adapter.command.OutboxResolveResult;
import com.personal.marketnote.common.outbox.entity.OutboxEventJpaEntity;
import com.personal.marketnote.common.outbox.exception.InvalidOutboxEventStatusTransitionException;
import com.personal.marketnote.common.outbox.exception.OutboxEventNotFoundException;
import com.personal.marketnote.common.outbox.repository.OutboxEventJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OutboxResolveService 테스트")
class OutboxResolveServiceTest {
    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2026-04-05T10:00:00Z"), ZoneId.of("Asia/Seoul")
    );

    @InjectMocks
    private OutboxResolveService outboxResolveService;

    @Mock
    private OutboxEventJpaRepository outboxEventJpaRepository;

    @Mock
    private OutboxAuditLogger outboxAuditLogger;

    @Mock
    private OutboxMetricsCollector outboxMetricsCollector;

    @Mock
    private Clock clock;

    @Test
    @DisplayName("FAILED 이벤트에 RETRY 액션 시 status가 PENDING으로 전이되고 retryCount가 0으로 리셋된다")
    void resolve_retryActionTransitionsFailedToPendingAndResetsRetryCount() {
        // given
        OutboxResolveCommand command = new OutboxResolveCommand(1L, OutboxResolutionAction.RETRY, null);
        OutboxEventJpaEntity entity = createFailedEntity("event-1", "commerce.payment.approved");
        when(outboxEventJpaRepository.findById(1L)).thenReturn(Optional.of(entity));

        // when
        OutboxResolveResult result = outboxResolveService.resolve(command);

        // then
        assertThat(result.alreadyResolved()).isFalse();
        assertThat(result.resolution()).isEqualTo("RETRY");
        verify(entity).resetForRetry();
    }

    @Test
    @DisplayName("FAILED 이벤트에 DISCARD 액션 시 status가 DISCARDED로 전이되고 reason이 기록된다")
    void resolve_discardActionTransitionsFailedToDiscardedAndRecordsReason() {
        // given
        when(clock.instant()).thenReturn(FIXED_CLOCK.instant());
        when(clock.getZone()).thenReturn(FIXED_CLOCK.getZone());
        OutboxResolveCommand command = new OutboxResolveCommand(2L, OutboxResolutionAction.DISCARD, "수동 폐기");
        OutboxEventJpaEntity entity = createFailedEntity("event-2", "commerce.order.created");
        when(outboxEventJpaRepository.findById(2L)).thenReturn(Optional.of(entity));

        // when
        OutboxResolveResult result = outboxResolveService.resolve(command);

        // then
        assertThat(result.alreadyResolved()).isFalse();
        assertThat(result.resolution()).isEqualTo("DISCARD");
        assertThat(result.reason()).isEqualTo("수동 폐기");
        verify(entity).discard("수동 폐기", LocalDateTime.now(FIXED_CLOCK));
    }

    @Test
    @DisplayName("존재하지 않는 ID로 resolve 시 OutboxEventNotFoundException이 발생한다")
    void resolve_throwsOutboxEventNotFoundExceptionWhenIdNotFound() {
        // given
        OutboxResolveCommand command = new OutboxResolveCommand(999L, OutboxResolutionAction.RETRY, null);
        when(outboxEventJpaRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> outboxResolveService.resolve(command))
                .isInstanceOf(OutboxEventNotFoundException.class);
        verifyNoInteractions(outboxAuditLogger, outboxMetricsCollector);
    }

    @Test
    @DisplayName("FAILED가 아닌 상태에서 resolve 시도 시 InvalidOutboxEventStatusTransitionException이 발생한다")
    void resolve_throwsInvalidStatusTransitionExceptionWhenNotFailed() {
        // given
        OutboxResolveCommand command = new OutboxResolveCommand(3L, OutboxResolutionAction.RETRY, null);
        OutboxEventJpaEntity entity = mock(OutboxEventJpaEntity.class);
        doThrow(new InvalidOutboxEventStatusTransitionException(OutboxEventStatus.PENDING))
                .when(entity).resetForRetry();
        when(outboxEventJpaRepository.findById(3L)).thenReturn(Optional.of(entity));

        // when & then
        assertThatThrownBy(() -> outboxResolveService.resolve(command))
                .isInstanceOf(InvalidOutboxEventStatusTransitionException.class);
    }

    @Test
    @DisplayName("이미 DISCARDED인 이벤트에 resolve 시 alreadyResolved가 true로 반환된다")
    void resolve_returnsAlreadyResolvedWhenAlreadyDiscarded() {
        // given
        OutboxResolveCommand command = new OutboxResolveCommand(4L, OutboxResolutionAction.RETRY, null);
        OutboxEventJpaEntity entity = createAlreadyResolvedEntity("event-4", "commerce.payment.approved");
        when(outboxEventJpaRepository.findById(4L)).thenReturn(Optional.of(entity));

        // when
        OutboxResolveResult result = outboxResolveService.resolve(command);

        // then
        assertThat(result.alreadyResolved()).isTrue();
        verifyNoInteractions(outboxAuditLogger, outboxMetricsCollector);
    }

    @Test
    @DisplayName("이미 PUBLISHED인 이벤트에 resolve 시 alreadyResolved가 true로 반환된다")
    void resolve_returnsAlreadyResolvedWhenAlreadyPublished() {
        // given
        OutboxResolveCommand command = new OutboxResolveCommand(5L, OutboxResolutionAction.DISCARD, "폐기");
        OutboxEventJpaEntity entity = createAlreadyResolvedEntity("event-5", "commerce.payment.approved");
        when(outboxEventJpaRepository.findById(5L)).thenReturn(Optional.of(entity));

        // when
        OutboxResolveResult result = outboxResolveService.resolve(command);

        // then
        assertThat(result.alreadyResolved()).isTrue();
        verifyNoInteractions(outboxAuditLogger, outboxMetricsCollector);
    }

    @Test
    @DisplayName("resolve 시 감사 로그가 기록된다")
    void resolve_logsAuditOnSuccessfulResolve() {
        // given
        OutboxResolveCommand command = new OutboxResolveCommand(6L, OutboxResolutionAction.RETRY, null);
        OutboxEventJpaEntity entity = createFailedEntity("event-6", "commerce.payment.approved");
        when(outboxEventJpaRepository.findById(6L)).thenReturn(Optional.of(entity));

        // when
        outboxResolveService.resolve(command);

        // then
        verify(outboxAuditLogger).logResolve("commerce.payment.approved", "event-6", "RETRY", null);
    }

    @Test
    @DisplayName("resolve 시 메트릭 카운터가 증가한다")
    void resolve_incrementsMetricsOnSuccessfulResolve() {
        // given
        when(clock.instant()).thenReturn(FIXED_CLOCK.instant());
        when(clock.getZone()).thenReturn(FIXED_CLOCK.getZone());
        OutboxResolveCommand command = new OutboxResolveCommand(7L, OutboxResolutionAction.DISCARD, "테스트 폐기");
        OutboxEventJpaEntity entity = createFailedEntity("event-7", "commerce.order.created");
        when(outboxEventJpaRepository.findById(7L)).thenReturn(Optional.of(entity));

        // when
        outboxResolveService.resolve(command);

        // then
        verify(outboxMetricsCollector).incrementResolvedCount("commerce.order.created", "DISCARD");
    }

    private OutboxEventJpaEntity createFailedEntity(String eventId, String topic) {
        OutboxEventJpaEntity entity = mock(OutboxEventJpaEntity.class);
        when(entity.getEventId()).thenReturn(eventId);
        when(entity.getTopic()).thenReturn(topic);
        return entity;
    }

    private OutboxEventJpaEntity createAlreadyResolvedEntity(String eventId, String topic) {
        OutboxEventJpaEntity entity = mock(OutboxEventJpaEntity.class);
        when(entity.isAlreadyResolved()).thenReturn(true);
        when(entity.getEventId()).thenReturn(eventId);
        when(entity.getTopic()).thenReturn(topic);
        return entity;
    }
}
