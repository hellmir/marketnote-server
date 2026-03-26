package com.personal.marketnote.common.saga;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.outbox.OutboxEvent;
import com.personal.marketnote.common.outbox.SaveOutboxEventPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("SagaResponsePublisher 테스트")
class SagaResponsePublisherTest {

    @Mock
    private SaveOutboxEventPort saveOutboxEventPort;

    private SagaResponsePublisher sagaResponsePublisher;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(
                Instant.parse("2026-03-17T01:00:00Z"),
                ZoneId.of("Asia/Seoul")
        );
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        sagaResponsePublisher = new SagaResponsePublisher(saveOutboxEventPort, objectMapper, clock);
    }

    @Nested
    @DisplayName("publishSuccess")
    class PublishSuccess {

        @Test
        @DisplayName("성공 응답을 saga.response 토픽으로 Outbox에 발행한다")
        void shouldPublishSuccessResponseToOutbox() {
            // when
            sagaResponsePublisher.publishSuccess(
                    "saga-001", "ORDER_PAYMENT", "DEDUCT_INVENTORY",
                    SagaStepMessage.ACTION, "{\"success\":true}");

            // then
            ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
            verify(saveOutboxEventPort).save(captor.capture());

            OutboxEvent event = captor.getValue();
            assertThat(event.getTopic()).isEqualTo(KafkaTopicConstants.SAGA_RESPONSE);
            assertThat(event.getPartitionKey()).isEqualTo("saga-001");
            assertThat(event.getSource()).isEqualTo("saga-step-handler");
            assertThat(event.getPayload()).contains("\"success\":true");
            assertThat(event.getPayload()).contains("\"sagaId\":\"saga-001\"");
            assertThat(event.getPayload()).contains("\"stepName\":\"DEDUCT_INVENTORY\"");
        }
    }

    @Nested
    @DisplayName("publishFailure")
    class PublishFailure {

        @Test
        @DisplayName("실패 응답을 saga.response 토픽으로 Outbox에 발행한다")
        void shouldPublishFailureResponseToOutbox() {
            // when
            sagaResponsePublisher.publishFailure(
                    "saga-002", "ORDER_PAYMENT", "RECORD_LEDGER",
                    SagaStepMessage.ACTION, "분개 처리 실패");

            // then
            ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
            verify(saveOutboxEventPort).save(captor.capture());

            OutboxEvent event = captor.getValue();
            assertThat(event.getTopic()).isEqualTo(KafkaTopicConstants.SAGA_RESPONSE);
            assertThat(event.getPartitionKey()).isEqualTo("saga-002");
            assertThat(event.getPayload()).contains("\"success\":false");
            assertThat(event.getPayload()).contains("\"stepName\":\"RECORD_LEDGER\"");
        }

        @Test
        @DisplayName("보상 실패 응답도 saga.response 토픽으로 발행된다")
        void shouldPublishCompensationFailureResponse() {
            // when
            sagaResponsePublisher.publishFailure(
                    "saga-003", "ORDER_PAYMENT", "DEDUCT_INVENTORY",
                    SagaStepMessage.COMPENSATION, "재고 복구 실패");

            // then
            ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
            verify(saveOutboxEventPort).save(captor.capture());

            OutboxEvent event = captor.getValue();
            assertThat(event.getPayload()).contains("\"messageType\":\"COMPENSATION\"");
            assertThat(event.getPayload()).contains("\"success\":false");
        }
    }
}
