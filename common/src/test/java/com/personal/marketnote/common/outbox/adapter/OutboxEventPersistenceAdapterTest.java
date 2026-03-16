package com.personal.marketnote.common.outbox.adapter;

import com.personal.marketnote.common.outbox.OutboxEvent;
import com.personal.marketnote.common.outbox.OutboxEventStatus;
import com.personal.marketnote.common.outbox.entity.OutboxEventJpaEntity;
import com.personal.marketnote.common.outbox.repository.OutboxEventJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("OutboxEventPersistenceAdapter 테스트")
class OutboxEventPersistenceAdapterTest {
    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2026-03-15T10:00:00Z"), ZoneId.of("Asia/Seoul")
    );

    @InjectMocks
    private OutboxEventPersistenceAdapter outboxEventPersistenceAdapter;

    @Mock
    private OutboxEventJpaRepository outboxEventJpaRepository;

    @Test
    @DisplayName("save 호출 시 OutboxEvent를 JPA 엔티티로 변환하여 저장한다")
    void save_convertsOutboxEventToJpaEntityAndPersists() {
        // given
        OutboxEvent event = OutboxEvent.of(
                "event-id-1", "commerce.payment.approved", "order-123",
                "PaymentApproved", "commerce-service", "{\"orderId\":123}", FIXED_CLOCK
        );

        // when
        outboxEventPersistenceAdapter.save(event);

        // then
        ArgumentCaptor<OutboxEventJpaEntity> captor = ArgumentCaptor.forClass(OutboxEventJpaEntity.class);
        verify(outboxEventJpaRepository).save(captor.capture());

        OutboxEventJpaEntity savedEntity = captor.getValue();
        assertThat(savedEntity.getEventId()).isEqualTo("event-id-1");
        assertThat(savedEntity.getTopic()).isEqualTo("commerce.payment.approved");
        assertThat(savedEntity.getPartitionKey()).isEqualTo("order-123");
        assertThat(savedEntity.getEventType()).isEqualTo("PaymentApproved");
        assertThat(savedEntity.getSource()).isEqualTo("commerce-service");
        assertThat(savedEntity.getPayload()).isEqualTo("{\"orderId\":123}");
        assertThat(savedEntity.getStatus()).isEqualTo(OutboxEventStatus.PENDING);
        assertThat(savedEntity.getRetryCount()).isZero();
    }
}
