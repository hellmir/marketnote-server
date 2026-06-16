package com.personal.marketnote.fulfillment.adapter.out.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.ShippingStatusChangedEvent;
import com.personal.marketnote.common.outbox.OutboxEvent;
import com.personal.marketnote.common.outbox.SaveOutboxEventPort;
import com.personal.marketnote.fulfillment.exception.ShippingStatusEventSerializationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ShippingStatusEventOutboxAdapter 테스트")
class ShippingStatusEventOutboxAdapterTest {

    @InjectMocks
    private ShippingStatusEventOutboxAdapter adapter;

    @Mock
    private SaveOutboxEventPort saveOutboxEventPort;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Spy
    private Clock clock = Clock.fixed(
            Instant.parse("2026-06-03T10:00:00Z"),
            ZoneId.of("Asia/Seoul")
    );

    @Test
    @DisplayName("ShippingStatusChangedEvent를 OutboxEvent로 저장한다")
    void publishSavesOutboxEvent() {
        // given
        ShippingStatusChangedEvent event = new ShippingStatusChangedEvent(
                100L,
                "SHIPPING",
                "INV001",
                "CJ",
                LocalDateTime.of(2026, 4, 9, 10, 0)
        );

        // when
        adapter.publish(event);

        // then
        ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(saveOutboxEventPort).save(captor.capture());

        OutboxEvent outboxEvent = captor.getValue();
        assertThat(outboxEvent.getTopic()).isEqualTo(KafkaTopicConstants.SHIPPING_STATUS_CHANGED);
        assertThat(outboxEvent.getPartitionKey()).isEqualTo("100");
        assertThat(outboxEvent.getEventType()).isEqualTo("ShippingStatusChangedEvent");
        assertThat(outboxEvent.getSource()).isEqualTo("fulfillment-service");
        assertThat(outboxEvent.getEventId()).isNotBlank();
        assertThat(outboxEvent.getPayload()).contains("\"orderId\":100");
        assertThat(outboxEvent.getPayload()).contains("\"shippingStatus\":\"SHIPPING\"");
        assertThat(outboxEvent.getPayload()).contains("\"trackingNumber\":\"INV001\"");
        assertThat(outboxEvent.getPayload()).contains("\"carrierCode\":\"CJ\"");
    }

    @Test
    @DisplayName("페이로드 JSON이 올바르게 직렬화된다")
    void payloadIsValidJson() throws Exception {
        // given
        ShippingStatusChangedEvent event = new ShippingStatusChangedEvent(
                200L,
                "DELIVERED",
                "INV002",
                "HANJIN",
                LocalDateTime.of(2026, 4, 9, 12, 0)
        );

        // when
        adapter.publish(event);

        // then
        ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(saveOutboxEventPort).save(captor.capture());

        JsonNode parsed = objectMapper.readTree(captor.getValue().getPayload());
        assertThat(parsed.get("orderId").asLong()).isEqualTo(200L);
        assertThat(parsed.get("shippingStatus").asText()).isEqualTo("DELIVERED");
        assertThat(parsed.get("trackingNumber").asText()).isEqualTo("INV002");
        assertThat(parsed.get("carrierCode").asText()).isEqualTo("HANJIN");
    }

    @Test
    @DisplayName("직렬화 실패 시 ShippingStatusEventSerializationException을 던진다")
    void throwsOnSerializationFailure() throws Exception {
        // given
        ShippingStatusChangedEvent event = new ShippingStatusChangedEvent(
                300L,
                "DELIVERY_FAILED",
                "INV003",
                "CJ",
                LocalDateTime.of(2026, 4, 9, 14, 0)
        );
        when(objectMapper.writeValueAsString(event))
                .thenThrow(new JsonProcessingException("test failure") {});

        // when & then
        assertThatThrownBy(() -> adapter.publish(event))
                .isInstanceOf(ShippingStatusEventSerializationException.class)
                .hasMessageContaining("orderId=300");
    }
}
