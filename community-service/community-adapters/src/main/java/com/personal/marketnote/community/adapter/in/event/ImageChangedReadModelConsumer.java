package com.personal.marketnote.community.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.EventPayloadValidator;
import com.personal.marketnote.common.kafka.event.ImageChangeAction;
import com.personal.marketnote.common.kafka.event.ImageChangedEvent;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.community.port.out.file.SaveImageReadModelPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ImageChangedReadModelConsumer {
    private static final String TARGET_TYPE_POST = "POST";
    private static final String TARGET_TYPE_REVIEW = "REVIEW";

    private final ObjectMapper objectMapper;
    private final SaveImageReadModelPort saveImageReadModelPort;

    @KafkaListener(
            topics = KafkaTopicConstants.FILE_IMAGE_CHANGED,
            groupId = "community-image-read-model"
    )
    public void handleImageChangedEvent(
            ConsumerRecord<String, EventEnvelope<?>> record,
            Acknowledgment acknowledgment
    ) {
        EventEnvelope<?> envelope = record.value();

        if (EventPayloadValidator.hasInvalidEnvelope(envelope, record)) {
            acknowledgment.acknowledge();
            return;
        }

        if (EventPayloadValidator.hasEventTypeMismatch(envelope, KafkaTopicConstants.FILE_IMAGE_CHANGED)) {
            acknowledgment.acknowledge();
            return;
        }

        ImageChangedEvent payload = envelope.getPayloadAs(ImageChangedEvent.class, objectMapper);

        log.info("이미지 변경 이벤트 수신. eventId={}, imageId={}, targetId={}, targetType={}, action={}",
                envelope.eventId(), payload.imageId(), payload.targetId(),
                payload.targetType(), payload.action());

        if (EventPayloadValidator.hasInvalidIds(envelope.eventId(),
                EventPayloadValidator.id("imageId", payload.imageId()),
                EventPayloadValidator.id("targetId", payload.targetId()))) {
            acknowledgment.acknowledge();
            return;
        }

        if (!FormatValidator.equals(payload.targetType(), TARGET_TYPE_POST)
                && !FormatValidator.equals(payload.targetType(), TARGET_TYPE_REVIEW)) {
            log.debug("POST/REVIEW 타입이 아닌 이벤트 무시. targetType={}", payload.targetType());
            acknowledgment.acknowledge();
            return;
        }

        if (payload.action() == ImageChangeAction.CREATED) {
            saveImageReadModelPort.upsert(
                    payload.imageId(), payload.targetId(), payload.targetType(),
                    payload.fileSort(), payload.imageUrl(), payload.sortOrder()
            );
            log.info("이미지 Read Model 저장 완료. imageId={}, targetId={}",
                    payload.imageId(), payload.targetId());
        }

        if (payload.action() == ImageChangeAction.DELETED) {
            saveImageReadModelPort.deactivateByImageId(payload.imageId());
            log.info("이미지 Read Model 비활성화 완료. imageId={}", payload.imageId());
        }

        acknowledgment.acknowledge();
    }
}
