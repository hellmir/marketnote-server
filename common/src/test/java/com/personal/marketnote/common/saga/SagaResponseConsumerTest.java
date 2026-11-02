package com.personal.marketnote.common.saga;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SagaResponseConsumer 테스트")
class SagaResponseConsumerTest {

    private static final String SAGA_ID = "saga-001";
    private static final String SAGA_TYPE = "ORDER_PAYMENT";
    private static final String STEP_NAME = "DEDUCT_INVENTORY";
    private static final String RESPONSE_DATA = "{\"success\":true}";
    private static final String EVENT_TYPE = "saga.response";
    private static final String TOPIC = "saga.response";

    @Mock
    private SagaOrchestrator sagaOrchestrator;

    @Mock
    private Acknowledgment acknowledgment;

    private SagaResponseConsumer sagaResponseConsumer;
    private Clock clock;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(
                Instant.parse("2026-03-16T01:00:00Z"),
                ZoneId.of("Asia/Seoul")
        );
        objectMapper = new ObjectMapper();
        sagaResponseConsumer = new SagaResponseConsumer(sagaOrchestrator, objectMapper);
    }

    @Nested
    @DisplayName("ACTION 응답 처리")
    class HandleActionResponse {

        @Test
        @DisplayName("ACTION 성공 응답을 수신하면 orchestrator.handleStepResponse를 success=true로 호출한다")
        void shouldCallHandleStepResponseWithSuccessTrue() {
            // given
            SagaResponseMessage responseMessage = new SagaResponseMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, true, RESPONSE_DATA);
            ConsumerRecord<String, EventEnvelope<?>> record = createRecord(EVENT_TYPE, responseMessage);

            // when
            sagaResponseConsumer.handleSagaResponse(record, acknowledgment);

            // then
            verify(sagaOrchestrator).handleStepResponse(SAGA_ID, STEP_NAME, true, RESPONSE_DATA);
            verify(acknowledgment).acknowledge();
        }

        @Test
        @DisplayName("ACTION 실패 응답을 수신하면 orchestrator.handleStepResponse를 success=false로 호출한다")
        void shouldCallHandleStepResponseWithSuccessFalse() {
            // given
            String errorResponse = "{\"error\":\"insufficient funds\"}";
            SagaResponseMessage responseMessage = new SagaResponseMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, false, errorResponse);
            ConsumerRecord<String, EventEnvelope<?>> record = createRecord(EVENT_TYPE, responseMessage);

            // when
            sagaResponseConsumer.handleSagaResponse(record, acknowledgment);

            // then
            verify(sagaOrchestrator).handleStepResponse(SAGA_ID, STEP_NAME, false, errorResponse);
            verify(acknowledgment).acknowledge();
        }
    }

    @Nested
    @DisplayName("COMPENSATION 응답 처리")
    class HandleCompensationResponse {

        @Test
        @DisplayName("COMPENSATION 성공 응답을 수신하면 orchestrator.handleCompensationResponse를 success=true로 호출한다")
        void shouldCallHandleCompensationResponseWithSuccessTrue() {
            // given
            SagaResponseMessage responseMessage = new SagaResponseMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.COMPENSATION, true, RESPONSE_DATA);
            ConsumerRecord<String, EventEnvelope<?>> record = createRecord(EVENT_TYPE, responseMessage);

            // when
            sagaResponseConsumer.handleSagaResponse(record, acknowledgment);

            // then
            verify(sagaOrchestrator).handleCompensationResponse(SAGA_ID, STEP_NAME, true, RESPONSE_DATA);
            verify(acknowledgment).acknowledge();
        }

        @Test
        @DisplayName("COMPENSATION 실패 응답을 수신하면 orchestrator.handleCompensationResponse를 success=false로 호출한다")
        void shouldCallHandleCompensationResponseWithSuccessFalse() {
            // given
            String errorResponse = "{\"error\":\"rollback failed\"}";
            SagaResponseMessage responseMessage = new SagaResponseMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.COMPENSATION, false, errorResponse);
            ConsumerRecord<String, EventEnvelope<?>> record = createRecord(EVENT_TYPE, responseMessage);

            // when
            sagaResponseConsumer.handleSagaResponse(record, acknowledgment);

            // then
            verify(sagaOrchestrator).handleCompensationResponse(SAGA_ID, STEP_NAME, false, errorResponse);
            verify(acknowledgment).acknowledge();
        }
    }

    @Nested
    @DisplayName("유효성 검증")
    class Validation {

        @Test
        @DisplayName("envelope이 null이면 오케스트레이터를 호출하지 않고 acknowledge한다")
        void shouldAcknowledgeAndSkipWhenEnvelopeIsNull() {
            // given
            ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(TOPIC, 0, 0L, SAGA_ID, null);

            // when
            sagaResponseConsumer.handleSagaResponse(record, acknowledgment);

            // then
            verifyNoInteractions(sagaOrchestrator);
            verify(acknowledgment).acknowledge();
        }

        @Test
        @DisplayName("eventType이 불일치하면 오케스트레이터를 호출하지 않고 acknowledge한다")
        void shouldAcknowledgeAndSkipWhenEventTypeMismatch() {
            // given
            SagaResponseMessage responseMessage = new SagaResponseMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, true, RESPONSE_DATA);
            ConsumerRecord<String, EventEnvelope<?>> record = createRecord("wrong.event.type", responseMessage);

            // when
            sagaResponseConsumer.handleSagaResponse(record, acknowledgment);

            // then
            verifyNoInteractions(sagaOrchestrator);
            verify(acknowledgment).acknowledge();
        }

        @Test
        @DisplayName("알 수 없는 messageType이면 오케스트레이터를 호출하지 않고 acknowledge한다")
        void shouldAcknowledgeAndSkipWhenUnknownMessageType() {
            // given
            SagaResponseMessage responseMessage = new SagaResponseMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, "UNKNOWN_TYPE", true, RESPONSE_DATA);
            ConsumerRecord<String, EventEnvelope<?>> record = createRecord(EVENT_TYPE, responseMessage);

            // when
            sagaResponseConsumer.handleSagaResponse(record, acknowledgment);

            // then
            verifyNoInteractions(sagaOrchestrator);
            verify(acknowledgment).acknowledge();
        }
    }

    @Nested
    @DisplayName("예외 처리")
    class ExceptionHandling {

        @Test
        @DisplayName("오케스트레이터에서 예외가 발생하면 DefaultErrorHandler에 위임한다")
        void shouldPropagateExceptionForDefaultErrorHandler() {
            // given
            SagaResponseMessage responseMessage = new SagaResponseMessage(
                    SAGA_ID, SAGA_TYPE, STEP_NAME, SagaStepMessage.ACTION, true, RESPONSE_DATA);
            ConsumerRecord<String, EventEnvelope<?>> record = createRecord(EVENT_TYPE, responseMessage);

            doThrow(new RuntimeException("DB connection error"))
                    .when(sagaOrchestrator).handleStepResponse(SAGA_ID, STEP_NAME, true, RESPONSE_DATA);

            // when & then
            assertThatThrownBy(() -> sagaResponseConsumer.handleSagaResponse(record, acknowledgment))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("DB connection error");

            verify(acknowledgment, never()).acknowledge();
        }
    }

    // --- Helper Methods ---

    private ConsumerRecord<String, EventEnvelope<?>> createRecord(String eventType, SagaResponseMessage responseMessage) {
        EventEnvelope<Object> envelope = EventEnvelope.of(eventType, "step-consumer", responseMessage, clock);
        return new ConsumerRecord<>(TOPIC, 0, 0L, SAGA_ID, envelope);
    }
}
