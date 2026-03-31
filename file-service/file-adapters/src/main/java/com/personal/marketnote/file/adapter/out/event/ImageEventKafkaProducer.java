package com.personal.marketnote.file.adapter.out.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.adapter.out.ServiceAdapter;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.ImageChangeAction;
import com.personal.marketnote.common.kafka.event.ImageChangedEvent;
import com.personal.marketnote.common.outbox.OutboxEvent;
import com.personal.marketnote.common.outbox.SaveOutboxEventPort;
import com.personal.marketnote.file.port.out.event.ImageEventCommand;
import com.personal.marketnote.file.port.out.event.PublishImageEventPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Clock;
import java.util.List;

@Slf4j
@ServiceAdapter
@RequiredArgsConstructor
public class ImageEventKafkaProducer implements PublishImageEventPort {
    private static final String SOURCE = "file-service";

    private final SaveOutboxEventPort saveOutboxEventPort;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    @Override
    public void publishImageCreatedEvents(List<ImageEventCommand> events) {
        publishEvents(events, ImageChangeAction.CREATED);
    }

    @Override
    public void publishImageDeletedEvents(List<ImageEventCommand> events) {
        publishEvents(events, ImageChangeAction.DELETED);
    }

    private void publishEvents(List<ImageEventCommand> events, ImageChangeAction action) {
        for (ImageEventCommand event : events) {
            ImageChangedEvent payload = new ImageChangedEvent(
                    event.imageId(), event.targetId(), event.targetType(),
                    event.imageUrl(), event.sortOrder(), action
            );
            String topic = KafkaTopicConstants.FILE_IMAGE_CHANGED;
            EventEnvelope<ImageChangedEvent> envelope = EventEnvelope.of(topic, SOURCE, payload, clock);

            saveToOutbox(envelope, topic, event.imageId().toString());
        }
    }

    private <T> void saveToOutbox(EventEnvelope<T> envelope, String topic, String partitionKey) {
        try {
            String payloadJson = objectMapper.writeValueAsString(envelope);
            OutboxEvent outboxEvent = OutboxEvent.of(
                    envelope.eventId(), topic, partitionKey,
                    envelope.eventType(), SOURCE, payloadJson, clock
            );
            saveOutboxEventPort.save(outboxEvent);
            log.info("Outbox 이벤트 저장. topic={}, partitionKey={}, eventId={}",
                    topic, partitionKey, envelope.eventId());
        } catch (Exception e) {
            log.error("Outbox 이벤트 저장 실패. topic={}, partitionKey={}, error={}",
                    topic, partitionKey, e.getMessage(), e);
        }
    }
}
