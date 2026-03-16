package com.personal.marketnote.commerce.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.SettlementExecutedEvent;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SettlementExecutedLedgerConsumer 테스트")
class SettlementExecutedLedgerConsumerTest {
    @InjectMocks
    private SettlementExecutedLedgerConsumer consumer;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private Acknowledgment acknowledgment;

    private ConsumerRecord<String, EventEnvelope<?>> buildRecord(Long settlementId, Long sellerId) {
        SettlementExecutedEvent event = new SettlementExecutedEvent(
                settlementId, sellerId, 100000L, 3000L, 7000L, 90000L
        );
        EventEnvelope<SettlementExecutedEvent> envelope = new EventEnvelope<>(
                "test-event-id", "commerce.settlement.executed", "commerce-service",
                LocalDateTime.of(2026, 3, 9, 10, 0), event
        );
        return new ConsumerRecord<>("commerce.settlement.executed", 0, 0L, String.valueOf(settlementId), envelope);
    }

    @Test
    @DisplayName("듀얼 라이트 기간 중 정산 실행 이벤트 수신 시 페이로드 검증을 완료하고 acknowledge한다")
    void handleSettlementExecutedEvent_success_validatesAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L, 100L);

        // when
        consumer.handleSettlementExecutedEvent(record, acknowledgment);

        // then
        // TODO: [#929][#1024] RecordLedgerEntryUseCase 활성화 시
        //  recordLedgerEntryUseCase.recordPgSettlement(), recordSellerSettlement() 호출 검증 추가
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("settlementId가 null이면 이벤트를 무시하고 acknowledge한다")
    void handleSettlementExecutedEvent_nullSettlementId_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(null, 100L);

        // when
        consumer.handleSettlementExecutedEvent(record, acknowledgment);

        // then
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("settlementId가 0이면 이벤트를 무시하고 acknowledge한다")
    void handleSettlementExecutedEvent_zeroSettlementId_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(0L, 100L);

        // when
        consumer.handleSettlementExecutedEvent(record, acknowledgment);

        // then
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("settlementId가 음수이면 이벤트를 무시하고 acknowledge한다")
    void handleSettlementExecutedEvent_negativeSettlementId_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(-1L, 100L);

        // when
        consumer.handleSettlementExecutedEvent(record, acknowledgment);

        // then
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("envelope이 null이면 이벤트를 무시하고 acknowledge한다")
    void handleSettlementExecutedEvent_nullEnvelope_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(
                "commerce.settlement.executed", 0, 0L, "1", null
        );

        // when
        consumer.handleSettlementExecutedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(objectMapper);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("eventType이 불일치하면 이벤트를 무시하고 acknowledge한다")
    void handleSettlementExecutedEvent_eventTypeMismatch_skipsAndAcknowledges() {
        // given
        SettlementExecutedEvent event = new SettlementExecutedEvent(
                1L, 100L, 100000L, 3000L, 7000L, 90000L
        );
        EventEnvelope<SettlementExecutedEvent> envelope = new EventEnvelope<>(
                "test-event-id", "wrong.event.type", "commerce-service",
                LocalDateTime.of(2026, 3, 9, 10, 0), event
        );
        ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(
                "commerce.settlement.executed", 0, 0L, "1", envelope
        );

        // when
        consumer.handleSettlementExecutedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(objectMapper);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("페이로드 역직렬화 실패 시 DefaultErrorHandler로 위임되어 예외가 전파된다")
    void handleSettlementExecutedEvent_deserializationFailure_propagatesForRetry() {
        // given
        EventEnvelope<String> envelope = new EventEnvelope<>(
                "test-event-id", "commerce.settlement.executed", "commerce-service",
                LocalDateTime.of(2026, 3, 9, 10, 0), "invalid-payload"
        );
        ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(
                "commerce.settlement.executed", 0, 0L, "1", envelope
        );

        // when & then
        assertThatThrownBy(() -> consumer.handleSettlementExecutedEvent(record, acknowledgment))
                .isInstanceOf(IllegalArgumentException.class);

        verify(acknowledgment, never()).acknowledge();
    }
}
