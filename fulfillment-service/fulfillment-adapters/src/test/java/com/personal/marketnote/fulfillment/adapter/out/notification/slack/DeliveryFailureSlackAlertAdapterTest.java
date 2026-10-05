package com.personal.marketnote.fulfillment.adapter.out.notification.slack;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeliveryFailureSlackAlertAdapter 테스트")
class DeliveryFailureSlackAlertAdapterTest {

    @InjectMocks
    private DeliveryFailureSlackAlertAdapter adapter;

    @Mock
    private FulfillmentSlackProperties fulfillmentSlackProperties;

    @Mock
    private ObjectMapper objectMapper;

    @Nested
    @DisplayName("알림 미전송")
    class NoAlertSent {

        @Test
        @DisplayName("webhookUrl이 null이면 알림을 전송하지 않는다")
        void shouldNotSendAlertWhenWebhookUrlIsNull() {
            // given
            when(fulfillmentSlackProperties.getWebhookUrl()).thenReturn(null);

            // when
            adapter.sendDeliveryFailureAlert(
                    1L, "1234567890", "CJ", LocalDateTime.of(2026, 4, 14, 10, 0)
            );

            // then
            verifyNoInteractions(objectMapper);
        }

        @Test
        @DisplayName("webhookUrl이 빈 문자열이면 알림을 전송하지 않는다")
        void shouldNotSendAlertWhenWebhookUrlIsEmpty() {
            // given
            when(fulfillmentSlackProperties.getWebhookUrl()).thenReturn("");

            // when
            adapter.sendDeliveryFailureAlert(
                    1L, "1234567890", "CJ", LocalDateTime.of(2026, 4, 14, 10, 0)
            );

            // then
            verifyNoInteractions(objectMapper);
        }
    }
}
