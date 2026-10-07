package com.personal.marketnote.commerce.adapter.out.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.outbox.OutboxEvent;
import com.personal.marketnote.common.outbox.SaveOutboxEventPort;
import org.junit.jupiter.api.BeforeEach;
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
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SettlementEventKafkaProducer 단위 테스트")
class SettlementEventKafkaProducerTest {

    @InjectMocks
    private SettlementEventKafkaProducer producer;

    @Mock
    private SaveOutboxEventPort saveOutboxEventPort;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private Clock clock;

    @BeforeEach
    void setUp() {
        when(clock.instant()).thenReturn(Instant.parse("2026-10-07T00:00:00Z"));
        when(clock.getZone()).thenReturn(ZoneId.of("UTC"));
    }

    @Test
    @DisplayName("Outbox 이벤트로 settlement 실행 이벤트를 저장한다")
    void shouldSaveOutboxEvent() throws JsonProcessingException {
        // given
        when(objectMapper.writeValueAsString(org.mockito.ArgumentMatchers.any())).thenReturn("{}");

        // when
        producer.publishSettlementExecutedEvent(1L, 100L, 100000L, 3000L, 1000L, 2000L, 100000L);

        // then
        ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(saveOutboxEventPort).save(captor.capture());
        OutboxEvent saved = captor.getValue();
        assertThat(saved.getTopic()).isEqualTo(KafkaTopicConstants.SETTLEMENT_EXECUTED);
        assertThat(saved.getPartitionKey()).isEqualTo("1");
        assertThat(saved.getSource()).isEqualTo("commerce-service");
    }

    @Test
    @DisplayName("ObjectMapper에서 JsonProcessingException 발생 시 예외를 삼킨다")
    void shouldSwallowJsonProcessingException() throws JsonProcessingException {
        // given
        when(objectMapper.writeValueAsString(org.mockito.ArgumentMatchers.any()))
                .thenThrow(new JsonProcessingException("test error") {});

        // when & then
        assertThatCode(() -> producer.publishSettlementExecutedEvent(
                1L, 100L, 100000L, 3000L, 1000L, 2000L, 100000L
        )).doesNotThrowAnyException();

        verifyNoInteractions(saveOutboxEventPort);
    }

    @Test
    @DisplayName("SaveOutboxEventPort 저장 실패 예외도 삼킨다")
    void shouldSwallowSaveError() throws JsonProcessingException {
        // given
        when(objectMapper.writeValueAsString(org.mockito.ArgumentMatchers.any())).thenReturn("{}");
        doThrow(new RuntimeException("save failed")).when(saveOutboxEventPort).save(org.mockito.ArgumentMatchers.any());

        // when & then
        assertThatCode(() -> producer.publishSettlementExecutedEvent(
                1L, 100L, 100000L, 3000L, 1000L, 2000L, 100000L
        )).doesNotThrowAnyException();
    }
}
