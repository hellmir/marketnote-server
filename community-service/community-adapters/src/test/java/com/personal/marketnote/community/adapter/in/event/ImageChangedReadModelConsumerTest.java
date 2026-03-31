package com.personal.marketnote.community.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.ImageChangeAction;
import com.personal.marketnote.common.kafka.event.ImageChangedEvent;
import com.personal.marketnote.community.port.out.file.SaveImageReadModelPort;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImageChangedReadModelConsumerTest {

    @InjectMocks
    private ImageChangedReadModelConsumer consumer;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private SaveImageReadModelPort saveImageReadModelPort;

    @Mock
    private Acknowledgment acknowledgment;

    private ConsumerRecord<String, EventEnvelope<?>> createRecord(EventEnvelope<?> envelope) {
        return new ConsumerRecord<>(
                KafkaTopicConstants.FILE_IMAGE_CHANGED, 0, 0, "1", envelope
        );
    }

    private EventEnvelope<ImageChangedEvent> createEnvelope(ImageChangedEvent payload) {
        return new EventEnvelope<>(
                "test-event-id",
                KafkaTopicConstants.FILE_IMAGE_CHANGED,
                "file-service",
                LocalDateTime.now(Clock.fixed(Instant.parse("2026-03-27T10:00:00Z"), ZoneId.of("Asia/Seoul"))),
                payload
        );
    }

    @Test
    @DisplayName("POST 타입 CREATED 이벤트 수신 시 Read Model을 upsert한다")
    void handleImageChangedEvent_createdPost_upsertsReadModel() {
        // given
        ImageChangedEvent payload = new ImageChangedEvent(
                1L, 100L, "POST", "POST_IMAGE",
                "https://cdn.example.com/post.png", 1, ImageChangeAction.CREATED
        );
        EventEnvelope<ImageChangedEvent> envelope = createEnvelope(payload);
        ConsumerRecord<String, EventEnvelope<?>> record = createRecord(envelope);

        // when
        consumer.handleImageChangedEvent(record, acknowledgment);

        // then
        verify(saveImageReadModelPort).upsert(
                1L, 100L, "POST", "POST_IMAGE",
                "https://cdn.example.com/post.png", 1
        );
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("REVIEW 타입 CREATED 이벤트 수신 시 Read Model을 upsert한다")
    void handleImageChangedEvent_createdReview_upsertsReadModel() {
        // given
        ImageChangedEvent payload = new ImageChangedEvent(
                2L, 200L, "REVIEW", "REVIEW_IMAGE",
                "https://cdn.example.com/review.png", 1, ImageChangeAction.CREATED
        );
        EventEnvelope<ImageChangedEvent> envelope = createEnvelope(payload);
        ConsumerRecord<String, EventEnvelope<?>> record = createRecord(envelope);

        // when
        consumer.handleImageChangedEvent(record, acknowledgment);

        // then
        verify(saveImageReadModelPort).upsert(
                2L, 200L, "REVIEW", "REVIEW_IMAGE",
                "https://cdn.example.com/review.png", 1
        );
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("POST 타입 DELETED 이벤트 수신 시 Read Model을 비활성화한다")
    void handleImageChangedEvent_deletedPost_deactivatesReadModel() {
        // given
        ImageChangedEvent payload = new ImageChangedEvent(
                3L, 100L, "POST", "POST_IMAGE",
                "https://cdn.example.com/post.png", 1, ImageChangeAction.DELETED
        );
        EventEnvelope<ImageChangedEvent> envelope = createEnvelope(payload);
        ConsumerRecord<String, EventEnvelope<?>> record = createRecord(envelope);

        // when
        consumer.handleImageChangedEvent(record, acknowledgment);

        // then
        verify(saveImageReadModelPort).deactivateByImageId(3L);
        verifyNoMoreInteractions(saveImageReadModelPort);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("REVIEW 타입 DELETED 이벤트 수신 시 Read Model을 비활성화한다")
    void handleImageChangedEvent_deletedReview_deactivatesReadModel() {
        // given
        ImageChangedEvent payload = new ImageChangedEvent(
                4L, 200L, "REVIEW", "REVIEW_IMAGE",
                "https://cdn.example.com/review.png", 1, ImageChangeAction.DELETED
        );
        EventEnvelope<ImageChangedEvent> envelope = createEnvelope(payload);
        ConsumerRecord<String, EventEnvelope<?>> record = createRecord(envelope);

        // when
        consumer.handleImageChangedEvent(record, acknowledgment);

        // then
        verify(saveImageReadModelPort).deactivateByImageId(4L);
        verifyNoMoreInteractions(saveImageReadModelPort);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("POST/REVIEW 타입이 아닌 이벤트는 무시한다")
    void handleImageChangedEvent_nonCommunityType_ignoresEvent() {
        // given
        ImageChangedEvent payload = new ImageChangedEvent(
                5L, 300L, "PRODUCT", "PRODUCT_CATALOG_IMAGE",
                "https://cdn.example.com/product.png", 1, ImageChangeAction.CREATED
        );
        EventEnvelope<ImageChangedEvent> envelope = createEnvelope(payload);
        ConsumerRecord<String, EventEnvelope<?>> record = createRecord(envelope);

        // when
        consumer.handleImageChangedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(saveImageReadModelPort);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("envelope가 null이면 즉시 acknowledge한다")
    void handleImageChangedEvent_nullEnvelope_acknowledges() {
        // given
        ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(
                KafkaTopicConstants.FILE_IMAGE_CHANGED, 0, 0, "1", null
        );

        // when
        consumer.handleImageChangedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(saveImageReadModelPort);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("이벤트 타입이 불일치하면 즉시 acknowledge한다")
    void handleImageChangedEvent_eventTypeMismatch_acknowledges() {
        // given
        ImageChangedEvent payload = new ImageChangedEvent(
                6L, 100L, "POST", "POST_IMAGE",
                "https://cdn.example.com/post.png", 1, ImageChangeAction.CREATED
        );
        EventEnvelope<ImageChangedEvent> envelope = new EventEnvelope<>(
                "test-event-id",
                "wrong.event.type",
                "file-service",
                LocalDateTime.now(),
                payload
        );
        ConsumerRecord<String, EventEnvelope<?>> record = createRecord(envelope);

        // when
        consumer.handleImageChangedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(saveImageReadModelPort);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("imageId가 유효하지 않으면 즉시 acknowledge한다")
    void handleImageChangedEvent_invalidImageId_acknowledges() {
        // given
        ImageChangedEvent payload = new ImageChangedEvent(
                -1L, 100L, "POST", "POST_IMAGE",
                "https://cdn.example.com/post.png", 1, ImageChangeAction.CREATED
        );
        EventEnvelope<ImageChangedEvent> envelope = createEnvelope(payload);
        ConsumerRecord<String, EventEnvelope<?>> record = createRecord(envelope);

        // when
        consumer.handleImageChangedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(saveImageReadModelPort);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("targetId가 유효하지 않으면 즉시 acknowledge한다")
    void handleImageChangedEvent_invalidTargetId_acknowledges() {
        // given
        ImageChangedEvent payload = new ImageChangedEvent(
                7L, 0L, "POST", "POST_IMAGE",
                "https://cdn.example.com/post.png", 1, ImageChangeAction.CREATED
        );
        EventEnvelope<ImageChangedEvent> envelope = createEnvelope(payload);
        ConsumerRecord<String, EventEnvelope<?>> record = createRecord(envelope);

        // when
        consumer.handleImageChangedEvent(record, acknowledgment);

        // then
        verifyNoInteractions(saveImageReadModelPort);
        verify(acknowledgment).acknowledge();
    }
}
