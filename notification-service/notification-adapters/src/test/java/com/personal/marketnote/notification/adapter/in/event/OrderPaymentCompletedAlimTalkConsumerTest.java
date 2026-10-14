package com.personal.marketnote.notification.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.OrderPaymentCompletedEvent;
import com.personal.marketnote.notification.configuration.AligoProperties;
import com.personal.marketnote.notification.domain.aligo.AligoTemplateCodes;
import com.personal.marketnote.notification.port.in.command.SendAligoAlimTalkCommand;
import com.personal.marketnote.notification.port.in.usecase.aligo.SendAligoAlimTalkUseCase;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class OrderPaymentCompletedAlimTalkConsumerTest {

    @InjectMocks
    private OrderPaymentCompletedAlimTalkConsumer consumer;

    @Mock
    private SendAligoAlimTalkUseCase sendAligoAlimTalkUseCase;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private AligoProperties aligoProperties;

    @Mock
    private Acknowledgment acknowledgment;

    @BeforeEach
    void setUp() {
        org.mockito.Mockito.lenient().when(aligoProperties.getSender()).thenReturn("0212345678");
    }

    private ConsumerRecord<String, EventEnvelope<?>> buildRecord(
            Long orderId, Long buyerId, String recipientPhoneNumber, String recipientName
    ) {
        OrderPaymentCompletedEvent event = new OrderPaymentCompletedEvent(
                orderId, buyerId, 50000L, 0L,
                recipientName, recipientPhoneNumber,
                "12345", "서울시 강남구", "101동 202호", "문 앞에 놓아주세요",
                List.of(), 500L
        );
        EventEnvelope<OrderPaymentCompletedEvent> envelope = new EventEnvelope<>(
                "test-event-id", "commerce.order.payment-completed", "commerce-service",
                LocalDateTime.of(2026, 4, 16, 10, 0), event
        );
        return new ConsumerRecord<>("commerce.order.payment-completed", 0, 0L,
                String.valueOf(orderId), envelope);
    }

    @Test
    @DisplayName("주문 결제 완료 이벤트 수신 시 알림톡을 발송하고 acknowledge한다")
    void shouldSendAlimTalkAndAcknowledge() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(
                100L, 1L, "01012345678", "홍길동"
        );

        // when
        consumer.handleOrderPaymentCompletedEvent(record, acknowledgment);

        // then
        ArgumentCaptor<SendAligoAlimTalkCommand> captor = ArgumentCaptor.forClass(SendAligoAlimTalkCommand.class);
        verify(sendAligoAlimTalkUseCase).sendAlimTalk(captor.capture());

        SendAligoAlimTalkCommand command = captor.getValue();
        assertThat(command.templateCode()).isEqualTo(AligoTemplateCodes.ORDER_PAYMENT_COMPLETED);
        assertThat(command.sender()).isEqualTo("0212345678");
        assertThat(command.recipients()).hasSize(1);
        assertThat(command.recipients().get(0).receiver()).isEqualTo("01012345678");
        assertThat(command.recipients().get(0).recvName()).isEqualTo("홍길동");
        assertThat(command.variables()).containsEntry("order_id", "100");
        assertThat(command.failover()).isNull();

        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("envelope이 null이면 알림톡을 발송하지 않고 acknowledge한다")
    void shouldSkipWhenEnvelopeIsNull() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(
                "commerce.order.payment-completed", 0, 0L, "1", null
        );

        // when
        consumer.handleOrderPaymentCompletedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(sendAligoAlimTalkUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("eventType이 불일치하면 알림톡을 발송하지 않고 acknowledge한다")
    void shouldSkipWhenEventTypeMismatch() {
        // given
        OrderPaymentCompletedEvent event = new OrderPaymentCompletedEvent(
                100L, 1L, 50000L, 0L,
                "홍길동", "01012345678",
                "12345", "서울시 강남구", "101동 202호", null,
                List.of(), 500L
        );
        EventEnvelope<OrderPaymentCompletedEvent> envelope = new EventEnvelope<>(
                "test-event-id", "wrong.event.type", "commerce-service",
                LocalDateTime.of(2026, 4, 16, 10, 0), event
        );
        ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(
                "commerce.order.payment-completed", 0, 0L, "100", envelope
        );

        // when
        consumer.handleOrderPaymentCompletedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(sendAligoAlimTalkUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("orderId가 null이면 알림톡을 발송하지 않고 acknowledge한다")
    void shouldSkipWhenOrderIdIsNull() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(null, 1L, "01012345678", "홍길동");

        // when
        consumer.handleOrderPaymentCompletedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(sendAligoAlimTalkUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("buyerId가 null이면 알림톡을 발송하지 않고 acknowledge한다")
    void shouldSkipWhenBuyerIdIsNull() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(100L, null, "01012345678", "홍길동");

        // when
        consumer.handleOrderPaymentCompletedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(sendAligoAlimTalkUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("recipientPhoneNumber가 null이면 알림톡을 발송하지 않고 acknowledge한다")
    void shouldSkipWhenRecipientPhoneNumberIsNull() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(100L, 1L, null, "홍길동");

        // when
        consumer.handleOrderPaymentCompletedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(sendAligoAlimTalkUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("알림톡 발송 실패 시 예외를 로그하고 acknowledge한다")
    void shouldLogAndAcknowledgeWhenAlimTalkFails() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = buildRecord(
                100L, 1L, "01012345678", "홍길동"
        );
        org.mockito.Mockito.doThrow(new RuntimeException("알리고 API 호출 실패"))
                .when(sendAligoAlimTalkUseCase).sendAlimTalk(org.mockito.ArgumentMatchers.any());

        // when
        consumer.handleOrderPaymentCompletedEvent(record, acknowledgment);

        // then
        verify(acknowledgment).acknowledge();
    }
}
