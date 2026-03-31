package com.personal.marketnote.file.adapter.out.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.ImageChangeAction;
import com.personal.marketnote.common.kafka.event.ImageChangedEvent;
import com.personal.marketnote.common.outbox.OutboxEvent;
import com.personal.marketnote.common.outbox.SaveOutboxEventPort;
import com.personal.marketnote.file.port.out.event.ImageEventCommand;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImageEventKafkaProducerTest {
    @InjectMocks
    private ImageEventKafkaProducer imageEventKafkaProducer;

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
    @DisplayName("이미지 생성 이벤트 발행 시 올바른 토픽과 파티션 키로 Outbox에 저장된다")
    void publishImageCreatedEvents_savesToOutboxWithCorrectTopicAndPartitionKey() throws Exception {
        // given
        setUpClock("2026-03-27T10:00:00Z");
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        ImageEventCommand command = new ImageEventCommand(1L, 100L, "PRODUCT", "https://cdn.example.com/image.png", 1);
        List<ImageEventCommand> commands = List.of(command);

        // when
        imageEventKafkaProducer.publishImageCreatedEvents(commands);

        // then
        ArgumentCaptor<OutboxEvent> outboxCaptor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(saveOutboxEventPort).save(outboxCaptor.capture());

        OutboxEvent captured = outboxCaptor.getValue();
        assertThat(captured.getTopic()).isEqualTo(KafkaTopicConstants.FILE_IMAGE_CHANGED);
        assertThat(captured.getPartitionKey()).isEqualTo("1");
        assertThat(captured.getSource()).isEqualTo("file-service");
        assertThat(captured.getEventId()).isNotNull();
    }

    @Test
    @DisplayName("이미지 생성 이벤트 발행 시 EventEnvelope에 CREATED 액션이 포함된다")
    @SuppressWarnings("unchecked")
    void publishImageCreatedEvents_envelopeContainsCreatedAction() throws Exception {
        // given
        setUpClock("2026-03-27T10:00:00Z");
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        ImageEventCommand command = new ImageEventCommand(10L, 200L, "PRODUCT", "https://cdn.example.com/image.png", 2);
        List<ImageEventCommand> commands = List.of(command);

        // when
        imageEventKafkaProducer.publishImageCreatedEvents(commands);

        // then
        ArgumentCaptor<EventEnvelope> envelopeCaptor = ArgumentCaptor.forClass(EventEnvelope.class);
        verify(objectMapper).writeValueAsString(envelopeCaptor.capture());

        EventEnvelope<?> capturedEnvelope = envelopeCaptor.getValue();
        assertThat(capturedEnvelope.eventType()).isEqualTo(KafkaTopicConstants.FILE_IMAGE_CHANGED);
        assertThat(capturedEnvelope.source()).isEqualTo("file-service");

        ImageChangedEvent payload = (ImageChangedEvent) capturedEnvelope.payload();
        assertThat(payload.imageId()).isEqualTo(10L);
        assertThat(payload.targetId()).isEqualTo(200L);
        assertThat(payload.targetType()).isEqualTo("PRODUCT");
        assertThat(payload.imageUrl()).isEqualTo("https://cdn.example.com/image.png");
        assertThat(payload.sortOrder()).isEqualTo(2);
        assertThat(payload.action()).isEqualTo(ImageChangeAction.CREATED);
    }

    @Test
    @DisplayName("이미지 삭제 이벤트 발행 시 EventEnvelope에 DELETED 액션이 포함된다")
    @SuppressWarnings("unchecked")
    void publishImageDeletedEvents_envelopeContainsDeletedAction() throws Exception {
        // given
        setUpClock("2026-03-27T10:00:00Z");
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        ImageEventCommand command = new ImageEventCommand(5L, 300L, "POST", "https://cdn.example.com/post.png", 0);
        List<ImageEventCommand> commands = List.of(command);

        // when
        imageEventKafkaProducer.publishImageDeletedEvents(commands);

        // then
        ArgumentCaptor<EventEnvelope> envelopeCaptor = ArgumentCaptor.forClass(EventEnvelope.class);
        verify(objectMapper).writeValueAsString(envelopeCaptor.capture());

        EventEnvelope<?> capturedEnvelope = envelopeCaptor.getValue();
        ImageChangedEvent payload = (ImageChangedEvent) capturedEnvelope.payload();
        assertThat(payload.imageId()).isEqualTo(5L);
        assertThat(payload.targetId()).isEqualTo(300L);
        assertThat(payload.targetType()).isEqualTo("POST");
        assertThat(payload.action()).isEqualTo(ImageChangeAction.DELETED);
    }

    @Test
    @DisplayName("여러 이미지 생성 이벤트 발행 시 이벤트 수만큼 Outbox에 저장된다")
    void publishImageCreatedEvents_multipleImages_savesMultipleOutboxEvents() throws Exception {
        // given
        setUpClock("2026-03-27T10:00:00Z");
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        List<ImageEventCommand> commands = List.of(
                new ImageEventCommand(1L, 100L, "PRODUCT", "https://cdn.example.com/1.png", 1),
                new ImageEventCommand(2L, 100L, "PRODUCT", "https://cdn.example.com/2.png", 2),
                new ImageEventCommand(3L, 100L, "PRODUCT", "https://cdn.example.com/3.png", 3)
        );

        // when
        imageEventKafkaProducer.publishImageCreatedEvents(commands);

        // then
        verify(saveOutboxEventPort, times(3)).save(any(OutboxEvent.class));
    }

    @Test
    @DisplayName("빈 이벤트 목록 발행 시 Outbox에 저장하지 않는다")
    void publishImageCreatedEvents_emptyList_doesNotSaveToOutbox() {
        // given
        List<ImageEventCommand> commands = List.of();

        // when
        imageEventKafkaProducer.publishImageCreatedEvents(commands);

        // then
        verifyNoInteractions(saveOutboxEventPort, objectMapper);
    }

    @Test
    @DisplayName("Outbox 저장 중 예외 발생 시 나머지 이벤트를 계속 처리한다")
    void publishImageCreatedEvents_outboxSaveFailure_continuesProcessing() throws Exception {
        // given
        setUpClock("2026-03-27T10:00:00Z");
        when(objectMapper.writeValueAsString(any()))
                .thenThrow(new RuntimeException("직렬화 실패"))
                .thenReturn("{}");

        List<ImageEventCommand> commands = List.of(
                new ImageEventCommand(1L, 100L, "PRODUCT", "https://cdn.example.com/1.png", 1),
                new ImageEventCommand(2L, 100L, "PRODUCT", "https://cdn.example.com/2.png", 2)
        );

        // when
        imageEventKafkaProducer.publishImageCreatedEvents(commands);

        // then
        verify(saveOutboxEventPort, times(1)).save(any(OutboxEvent.class));
    }
}
