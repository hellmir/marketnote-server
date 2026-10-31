package com.personal.marketnote.commerce.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.commerce.port.in.command.returntracker.CompleteReturnCommand;
import com.personal.marketnote.commerce.port.in.usecase.returntracker.CompleteReturnUseCase;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.ReturnInspectionCompletedEvent;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReturnInspectionCompletedConsumer 테스트")
class ReturnInspectionCompletedConsumerTest {

    @InjectMocks
    private ReturnInspectionCompletedConsumer consumer;

    @Mock
    private CompleteReturnUseCase completeReturnUseCase;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private Acknowledgment acknowledgment;

    private ConsumerRecord<String, EventEnvelope<?>> buildRecord(Long orderId) {
        ReturnInspectionCompletedEvent event = new ReturnInspectionCompletedEvent(orderId);
        EventEnvelope<ReturnInspectionCompletedEvent> envelope = new EventEnvelope<>(
                "test-event-id", "commerce.return-inspection.completed", "fulfillment-service",
                LocalDateTime.of(2026, 4, 20, 10, 0), event
        );
        return new ConsumerRecord<>(
                "commerce.return-inspection.completed", 0, 0L, String.valueOf(orderId), envelope
        );
    }

    @Test
    @DisplayName("정상 반품 검수 완료 이벤트 수신 시 CompleteReturnCommand를 생성하여 UseCase를 호출하고 acknowledge한다")
    void handleReturnInspectionCompletedEvent_validEvent_invokesUseCaseAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L);

        // when
        consumer.handleReturnInspectionCompletedEvent(record, acknowledgment);

        // then
        ArgumentCaptor<CompleteReturnCommand> captor = ArgumentCaptor.forClass(CompleteReturnCommand.class);
        verify(completeReturnUseCase).completeReturn(captor.capture());

        CompleteReturnCommand command = captor.getValue();
        assertThat(command.orderId()).isEqualTo(1L);

        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("envelope이 null이면 UseCase를 호출하지 않고 acknowledge한다")
    void handleReturnInspectionCompletedEvent_nullEnvelope_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(
                "commerce.return-inspection.completed", 0, 0L, "1", null
        );

        // when
        consumer.handleReturnInspectionCompletedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(completeReturnUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("eventType이 불일치하면 UseCase를 호출하지 않고 acknowledge한다")
    void handleReturnInspectionCompletedEvent_eventTypeMismatch_skipsAndAcknowledges() {
        // given
        ReturnInspectionCompletedEvent event = new ReturnInspectionCompletedEvent(1L);
        EventEnvelope<ReturnInspectionCompletedEvent> envelope = new EventEnvelope<>(
                "test-event-id", "wrong.event.type", "fulfillment-service",
                LocalDateTime.of(2026, 4, 20, 10, 0), event
        );
        ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(
                "commerce.return-inspection.completed", 0, 0L, "1", envelope
        );

        // when
        consumer.handleReturnInspectionCompletedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(completeReturnUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("orderId가 null이면 UseCase를 호출하지 않고 acknowledge한다")
    void handleReturnInspectionCompletedEvent_nullOrderId_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(null);

        // when
        consumer.handleReturnInspectionCompletedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(completeReturnUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("orderId가 0이면 UseCase를 호출하지 않고 acknowledge한다")
    void handleReturnInspectionCompletedEvent_zeroOrderId_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(0L);

        // when
        consumer.handleReturnInspectionCompletedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(completeReturnUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("orderId가 음수이면 UseCase를 호출하지 않고 acknowledge한다")
    void handleReturnInspectionCompletedEvent_negativeOrderId_skipsAndAcknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(-1L);

        // when
        consumer.handleReturnInspectionCompletedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(completeReturnUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("UseCase에서 예외가 발생해도 catch 후 acknowledge한다")
    void handleReturnInspectionCompletedEvent_useCaseThrows_acknowledgesGracefully() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(1L);
        doThrow(new RuntimeException("DB 연결 오류"))
                .when(completeReturnUseCase).completeReturn(any(CompleteReturnCommand.class));

        // when
        consumer.handleReturnInspectionCompletedEvent(record, acknowledgment);

        // then
        verify(completeReturnUseCase).completeReturn(any(CompleteReturnCommand.class));
        verify(acknowledgment).acknowledge();
    }
}
