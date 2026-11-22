package com.personal.marketnote.user.adapter.out.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.UserSignupCompletedEvent;
import com.personal.marketnote.common.outbox.OutboxEvent;
import com.personal.marketnote.common.outbox.SaveOutboxEventPort;
import com.personal.marketnote.user.adapter.out.event.exception.UserEventSerializationException;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserEventKafkaProducerTest {
    @InjectMocks
    private UserEventKafkaProducer userEventKafkaProducer;

    @Mock
    private SaveOutboxEventPort saveOutboxEventPort;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private Clock clock;

    private void setUpClock(String instant) {
        Clock fixedClock = Clock.fixed(Instant.parse(instant), ZoneId.of("Asia/Seoul"));
        when(clock.instant()).thenReturn(fixedClock.instant());
        when(clock.getZone()).thenReturn(fixedClock.getZone());
    }

    @Test
    @DisplayName("회원가입 완료 이벤트 발행 시 올바른 토픽과 파티션 키로 Outbox에 저장된다")
    void publishUserSignupCompletedEvent_savesToOutboxWithCorrectTopicAndPartitionKey() throws Exception {
        // given
        setUpClock("2026-03-02T10:00:00Z");
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // when
        userEventKafkaProducer.publishUserSignupCompletedEvent(1L, "user-key-123", "010-1234-5678", "홍길동");

        // then
        ArgumentCaptor<OutboxEvent> outboxCaptor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(saveOutboxEventPort).save(outboxCaptor.capture());

        OutboxEvent captured = outboxCaptor.getValue();
        assertThat(captured.getTopic()).isEqualTo(KafkaTopicConstants.USER_SIGNUP_COMPLETED);
        assertThat(captured.getPartitionKey()).isEqualTo("1");
        assertThat(captured.getSource()).isEqualTo("user-service");
        assertThat(captured.getEventId()).isNotNull();
    }

    @Test
    @DisplayName("회원가입 완료 이벤트 발행 시 EventEnvelope에 userId/userKey가 평문 그대로 포함된다")
    @SuppressWarnings("unchecked")
    void publishUserSignupCompletedEvent_envelopeContainsPlainNonPiiFields() throws Exception {
        // given
        setUpClock("2026-03-02T10:00:00Z");
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // when
        userEventKafkaProducer.publishUserSignupCompletedEvent(10L, "user-key-456", "010-1234-5678", "홍길동");

        // then
        ArgumentCaptor<EventEnvelope> envelopeCaptor = ArgumentCaptor.forClass(EventEnvelope.class);
        verify(objectMapper).writeValueAsString(envelopeCaptor.capture());

        EventEnvelope<?> capturedEnvelope = envelopeCaptor.getValue();
        assertThat(capturedEnvelope.eventType()).isEqualTo(KafkaTopicConstants.USER_SIGNUP_COMPLETED);
        assertThat(capturedEnvelope.source()).isEqualTo("user-service");
        assertThat(capturedEnvelope.eventId()).isNotNull();
        assertThat(capturedEnvelope.timestamp()).isNotNull();

        UserSignupCompletedEvent payload = (UserSignupCompletedEvent) capturedEnvelope.payload();
        assertThat(payload.userId()).isEqualTo(10L);
        assertThat(payload.userKey()).isEqualTo("user-key-456");
    }

    @Test
    @DisplayName("Outbox 저장 실패 시 예외를 전파한다")
    void publishUserSignupCompletedEvent_propagatesExceptionWhenSaveFails() throws Exception {
        // given
        setUpClock("2026-11-21T10:00:00Z");
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        doThrow(new RuntimeException("DB connection failed"))
                .when(saveOutboxEventPort).save(any(OutboxEvent.class));

        // when & then
        assertThatThrownBy(
                () -> userEventKafkaProducer.publishUserSignupCompletedEvent(1L, "user-key-1", null, null)
        )
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("DB connection failed");
    }

    @Test
    @DisplayName("Payload 직렬화 실패 시 UserEventSerializationException을 던진다")
    void publishUserSignupCompletedEvent_throwsUserEventSerializationExceptionOnJsonProcessingFailure() throws Exception {
        // given
        setUpClock("2026-11-21T10:00:00Z");
        when(objectMapper.writeValueAsString(any()))
                .thenThrow(new TestJsonProcessingException("Serialization failed"));

        // when & then
        assertThatThrownBy(
                () -> userEventKafkaProducer.publishUserSignupCompletedEvent(1L, "user-key-1", null, null)
        )
                .isInstanceOf(UserEventSerializationException.class)
                .hasCauseInstanceOf(JsonProcessingException.class);
        verifyNoInteractions(saveOutboxEventPort);
    }

    @Test
    @DisplayName("회원가입 이벤트 발행 시 envelope payload의 phoneNumber가 입력 평문 그대로 발행된다")
    @SuppressWarnings("unchecked")
    void publishUserSignupCompletedEvent_publishesPhoneNumberAsPlaintextInPayload() throws Exception {
        // given
        setUpClock("2026-11-21T10:00:00Z");
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        String plainPhone = "010-1234-5678";

        // when
        userEventKafkaProducer.publishUserSignupCompletedEvent(1L, "user-key-1", plainPhone, "홍길동");

        // then
        ArgumentCaptor<EventEnvelope> envelopeCaptor = ArgumentCaptor.forClass(EventEnvelope.class);
        verify(objectMapper).writeValueAsString(envelopeCaptor.capture());

        UserSignupCompletedEvent payload = (UserSignupCompletedEvent) envelopeCaptor.getValue().payload();
        assertThat(payload.phoneNumber()).isEqualTo(plainPhone);
    }

    @Test
    @DisplayName("회원가입 이벤트 발행 시 envelope payload의 userName이 입력 평문 그대로 발행된다")
    @SuppressWarnings("unchecked")
    void publishUserSignupCompletedEvent_publishesUserNameAsPlaintextInPayload() throws Exception {
        // given
        setUpClock("2026-11-21T10:00:00Z");
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        String plainName = "홍길동";

        // when
        userEventKafkaProducer.publishUserSignupCompletedEvent(1L, "user-key-1", "010-1234-5678", plainName);

        // then
        ArgumentCaptor<EventEnvelope> envelopeCaptor = ArgumentCaptor.forClass(EventEnvelope.class);
        verify(objectMapper).writeValueAsString(envelopeCaptor.capture());

        UserSignupCompletedEvent payload = (UserSignupCompletedEvent) envelopeCaptor.getValue().payload();
        assertThat(payload.userName()).isEqualTo(plainName);
    }

    @Test
    @DisplayName("phoneNumber/userName이 null이면 envelope payload에도 null 그대로 유지된다")
    @SuppressWarnings("unchecked")
    void publishUserSignupCompletedEvent_keepsNullForNullInputs() throws Exception {
        // given
        setUpClock("2026-11-21T10:00:00Z");
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // when
        userEventKafkaProducer.publishUserSignupCompletedEvent(1L, "user-key-1", null, null);

        // then
        ArgumentCaptor<EventEnvelope> envelopeCaptor = ArgumentCaptor.forClass(EventEnvelope.class);
        verify(objectMapper).writeValueAsString(envelopeCaptor.capture());

        UserSignupCompletedEvent payload = (UserSignupCompletedEvent) envelopeCaptor.getValue().payload();
        assertThat(payload.phoneNumber()).isNull();
        assertThat(payload.userName()).isNull();
    }

    private static class TestJsonProcessingException extends JsonProcessingException {
        protected TestJsonProcessingException(String msg) {
            super(msg);
        }
    }
}
