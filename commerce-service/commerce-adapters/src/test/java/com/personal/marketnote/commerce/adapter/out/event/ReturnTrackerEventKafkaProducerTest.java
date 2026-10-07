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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReturnTrackerEventKafkaProducer 단위 테스트")
class ReturnTrackerEventKafkaProducerTest {

    @InjectMocks
    private ReturnTrackerEventKafkaProducer producer;

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
    @DisplayName("반품 검수 완료 이벤트를 Outbox에 저장한다")
    void shouldSaveReturnInspectionCompletedEvent() throws JsonProcessingException {
        // given
        when(objectMapper.writeValueAsString(org.mockito.ArgumentMatchers.any())).thenReturn("{}");

        // when
        producer.publishReturnInspectionCompletedEvent(42L);

        // then
        ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(saveOutboxEventPort).save(captor.capture());
        OutboxEvent saved = captor.getValue();
        assertThat(saved.getTopic()).isEqualTo(KafkaTopicConstants.RETURN_INSPECTION_COMPLETED);
        assertThat(saved.getPartitionKey()).isEqualTo("42");
        assertThat(saved.getSource()).isEqualTo("commerce-service");
    }

    @Test
    @DisplayName("ObjectMapper에서 JsonProcessingException 발생 시 IllegalStateException으로 감싸 던진다")
    void shouldWrapJsonProcessingException() throws JsonProcessingException {
        // given
        when(objectMapper.writeValueAsString(org.mockito.ArgumentMatchers.any()))
                .thenThrow(new JsonProcessingException("test error") {});

        // when & then
        assertThatThrownBy(() -> producer.publishReturnInspectionCompletedEvent(42L))
                .isInstanceOf(IllegalStateException.class);

        verifyNoInteractions(saveOutboxEventPort);
    }
}
