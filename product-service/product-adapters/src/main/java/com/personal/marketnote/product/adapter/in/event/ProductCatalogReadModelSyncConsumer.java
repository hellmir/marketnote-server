package com.personal.marketnote.product.adapter.in.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.domain.file.FileSort;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.EventPayloadValidator;
import com.personal.marketnote.common.kafka.event.ImageChangedEvent;
import com.personal.marketnote.common.kafka.event.InventoryChangedEvent;
import com.personal.marketnote.common.kafka.event.PricePolicyCreatedEvent;
import com.personal.marketnote.common.kafka.event.ProductRegisteredEvent;
import com.personal.marketnote.common.kafka.event.ProductTagChangedEvent;
import com.personal.marketnote.common.kafka.event.ReviewDeletedEvent;
import com.personal.marketnote.common.kafka.event.ReviewRegisteredEvent;
import com.personal.marketnote.common.kafka.event.ReviewUpdatedEvent;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.product.port.in.usecase.pricepolicysync.SyncPricePolicyCreatedUseCase;
import com.personal.marketnote.product.port.in.usecase.tagsync.SyncProductTagChangeUseCase;
import com.personal.marketnote.product.port.out.productcatalog.SaveProductCatalogReadModelPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductCatalogReadModelSyncConsumer {

    private static final String CONSUMER_GROUP_ID = "product-catalog-read-model";

    private final ObjectMapper objectMapper;
    private final SaveProductCatalogReadModelPort saveProductCatalogReadModelPort;
    private final SyncProductTagChangeUseCase syncProductTagChangeUseCase;
    private final SyncPricePolicyCreatedUseCase syncPricePolicyCreatedUseCase;

    @KafkaListener(topics = KafkaTopicConstants.PRODUCT_REGISTERED, groupId = CONSUMER_GROUP_ID)
    public void handleProductRegistered(ConsumerRecord<String, EventEnvelope<?>> record, Acknowledgment ack) {
        EventEnvelope<?> envelope = record.value();
        if (EventPayloadValidator.hasInvalidEnvelope(envelope, record)
                || EventPayloadValidator.hasEventTypeMismatch(envelope, KafkaTopicConstants.PRODUCT_REGISTERED)) {
            ack.acknowledge();
            return;
        }

        ProductRegisteredEvent payload = envelope.getPayloadAs(ProductRegisteredEvent.class, objectMapper);
        if (FormatValidator.hasNoValue(payload)) {
            ack.acknowledge();
            return;
        }

        if (EventPayloadValidator.hasInvalidIds(envelope.eventId(),
                EventPayloadValidator.id("productId", payload.productId()),
                EventPayloadValidator.id("pricePolicyId", payload.pricePolicyId()))) {
            ack.acknowledge();
            return;
        }

        if (FormatValidator.hasNoValue(payload.productName())) {
            log.warn(
                    "ProductRegistered 이벤트 productName 누락 — skip. eventId={}, pricePolicyId={}, productId={}",
                    envelope.eventId(), payload.pricePolicyId(), payload.productId()
            );
            ack.acknowledge();
            return;
        }

        saveProductCatalogReadModelPort.upsertCatalog(
                payload.pricePolicyId(),
                payload.productId(),
                payload.sellerId(),
                null,
                payload.productName(),
                payload.brandName(),
                FormatValidator.hasValue(payload.price()) ? payload.price() : 0L,
                FormatValidator.hasValue(payload.discountPrice()) ? payload.discountPrice() : 0L,
                BigDecimal.ZERO,
                FormatValidator.hasValue(payload.accumulatedPoint()) ? payload.accumulatedPoint() : 0L,
                BigDecimal.ZERO,
                0L,
                0L,
                FormatValidator.hasValue(payload.sales()) ? payload.sales() : 0L,
                serializeProductTags(payload.productTags(), payload.pricePolicyId()),
                serializeSelectedOptions(payload.selectedOptions(), payload.pricePolicyId())
        );

        log.info("상품 카탈로그 Read Model 저장 (상품 등록). pricePolicyId={}", payload.pricePolicyId());
        ack.acknowledge();
    }

    private String serializeProductTags(
            List<ProductRegisteredEvent.ProductTagSnapshot> tags, Long pricePolicyId
    ) {
        if (FormatValidator.hasNoValue(tags)) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(tags);
        } catch (JsonProcessingException jpe) {
            log.warn("상품 카탈로그 Read Model productTags 직렬화 실패. pricePolicyId={}", pricePolicyId, jpe);
            return null;
        }
    }

    private String serializeSelectedOptions(
            List<ProductRegisteredEvent.ProductOptionSnapshot> options, Long pricePolicyId
    ) {
        if (FormatValidator.hasNoValue(options)) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(options);
        } catch (JsonProcessingException jpe) {
            log.warn("상품 카탈로그 Read Model selectedOptions 직렬화 실패. pricePolicyId={}", pricePolicyId, jpe);
            return null;
        }
    }

    @KafkaListener(topics = KafkaTopicConstants.PRODUCT_TAG_CHANGED, groupId = CONSUMER_GROUP_ID)
    public void handleProductTagChanged(ConsumerRecord<String, EventEnvelope<?>> record, Acknowledgment ack) {
        EventEnvelope<?> envelope = record.value();
        if (EventPayloadValidator.hasInvalidEnvelope(envelope, record)
                || EventPayloadValidator.hasEventTypeMismatch(envelope, KafkaTopicConstants.PRODUCT_TAG_CHANGED)) {
            ack.acknowledge();
            return;
        }

        ProductTagChangedEvent payload = envelope.getPayloadAs(ProductTagChangedEvent.class, objectMapper);
        if (FormatValidator.hasNoValue(payload)) {
            ack.acknowledge();
            return;
        }

        if (EventPayloadValidator.hasInvalidIds(envelope.eventId(),
                EventPayloadValidator.id("productId", payload.productId()))) {
            ack.acknowledge();
            return;
        }

        UUID eventId = parseEventId(envelope.eventId());
        if (FormatValidator.hasNoValue(eventId)) {
            ack.acknowledge();
            return;
        }

        syncProductTagChangeUseCase.syncProductTagChange(eventId, payload);
        ack.acknowledge();
    }

    private UUID parseEventId(String eventId) {
        try {
            return UUID.fromString(eventId);
        } catch (IllegalArgumentException iae) {
            log.warn("이벤트 eventId 파싱 실패 — 건너뜀. eventId={}", eventId);
            return null;
        }
    }

    @KafkaListener(topics = KafkaTopicConstants.PRICE_POLICY_CREATED, groupId = CONSUMER_GROUP_ID)
    public void handlePricePolicyCreated(ConsumerRecord<String, EventEnvelope<?>> record, Acknowledgment ack) {
        EventEnvelope<?> envelope = record.value();
        if (EventPayloadValidator.hasInvalidEnvelope(envelope, record)
                || EventPayloadValidator.hasEventTypeMismatch(envelope, KafkaTopicConstants.PRICE_POLICY_CREATED)) {
            ack.acknowledge();
            return;
        }

        PricePolicyCreatedEvent payload = envelope.getPayloadAs(PricePolicyCreatedEvent.class, objectMapper);
        if (FormatValidator.hasNoValue(payload)) {
            ack.acknowledge();
            return;
        }

        if (EventPayloadValidator.hasInvalidIds(envelope.eventId(),
                EventPayloadValidator.id("productId", payload.productId()),
                EventPayloadValidator.id("pricePolicyId", payload.pricePolicyId()))) {
            ack.acknowledge();
            return;
        }

        if (FormatValidator.hasNoValue(payload.productName())) {
            log.warn(
                    "PricePolicyCreated 이벤트 productName 누락 (구 스키마 추정) — skip. eventId={}, pricePolicyId={}, productId={}",
                    envelope.eventId(), payload.pricePolicyId(), payload.productId()
            );
            ack.acknowledge();
            return;
        }

        UUID eventId = parseEventId(envelope.eventId());
        if (FormatValidator.hasNoValue(eventId)) {
            ack.acknowledge();
            return;
        }

        syncPricePolicyCreatedUseCase.syncPricePolicyCreated(eventId, payload);
        ack.acknowledge();
    }

    @KafkaListener(topics = KafkaTopicConstants.INVENTORY_CHANGED, groupId = CONSUMER_GROUP_ID)
    public void handleInventoryChanged(ConsumerRecord<String, EventEnvelope<?>> record, Acknowledgment ack) {
        EventEnvelope<?> envelope = record.value();
        if (EventPayloadValidator.hasInvalidEnvelope(envelope, record)
                || EventPayloadValidator.hasEventTypeMismatch(envelope, KafkaTopicConstants.INVENTORY_CHANGED)) {
            ack.acknowledge();
            return;
        }

        InventoryChangedEvent payload = envelope.getPayloadAs(InventoryChangedEvent.class, objectMapper);
        if (FormatValidator.hasNoValue(payload)) {
            ack.acknowledge();
            return;
        }

        if (EventPayloadValidator.hasInvalidIds(envelope.eventId(),
                EventPayloadValidator.id("pricePolicyId", payload.pricePolicyId()))) {
            ack.acknowledge();
            return;
        }

        saveProductCatalogReadModelPort.updateStockQuantity(payload.pricePolicyId(), payload.stockQuantity());
        log.info("상품 카탈로그 Read Model 재고 업데이트. pricePolicyId={}, stock={}",
                payload.pricePolicyId(), payload.stockQuantity());
        ack.acknowledge();
    }

    @KafkaListener(topics = KafkaTopicConstants.FILE_IMAGE_CHANGED, groupId = CONSUMER_GROUP_ID)
    public void handleImageChanged(ConsumerRecord<String, EventEnvelope<?>> record, Acknowledgment ack) {
        EventEnvelope<?> envelope = record.value();
        if (EventPayloadValidator.hasInvalidEnvelope(envelope, record)
                || EventPayloadValidator.hasEventTypeMismatch(envelope, KafkaTopicConstants.FILE_IMAGE_CHANGED)) {
            ack.acknowledge();
            return;
        }

        ImageChangedEvent payload = envelope.getPayloadAs(ImageChangedEvent.class, objectMapper);
        if (FormatValidator.hasNoValue(payload)) {
            ack.acknowledge();
            return;
        }

        if (EventPayloadValidator.hasInvalidIds(envelope.eventId(),
                EventPayloadValidator.id("targetId", payload.targetId()))) {
            ack.acknowledge();
            return;
        }

        if (!isProductCatalogRepresentativeImage(payload)) {
            ack.acknowledge();
            return;
        }

        saveProductCatalogReadModelPort.updateRepresentativeImage(
                payload.targetId(),
                payload.imageUrl(),
                toResizedImageUrls(payload.resizedImages())
        );
        log.info("상품 카탈로그 Read Model 대표 이미지 업데이트. productId={}", payload.targetId());
        ack.acknowledge();
    }

    private List<String> toResizedImageUrls(List<ImageChangedEvent.ResizedImageInfo> resizedImages) {
        if (FormatValidator.hasNoValue(resizedImages)) {
            return List.of();
        }
        return resizedImages.stream()
                .map(ImageChangedEvent.ResizedImageInfo::storageUrl)
                .filter(FormatValidator::hasValue)
                .toList();
    }

    @KafkaListener(topics = KafkaTopicConstants.REVIEW_REGISTERED, groupId = CONSUMER_GROUP_ID)
    public void handleReviewRegistered(ConsumerRecord<String, EventEnvelope<?>> record, Acknowledgment ack) {
        EventEnvelope<?> envelope = record.value();
        if (EventPayloadValidator.hasInvalidEnvelope(envelope, record)
                || EventPayloadValidator.hasEventTypeMismatch(envelope, KafkaTopicConstants.REVIEW_REGISTERED)) {
            ack.acknowledge();
            return;
        }

        ReviewRegisteredEvent payload = envelope.getPayloadAs(ReviewRegisteredEvent.class, objectMapper);
        if (FormatValidator.hasNoValue(payload)
                || hasInvalidReviewAggregate(payload.productId(), payload.totalCount(), payload.averageRating())) {
            ack.acknowledge();
            return;
        }

        saveProductCatalogReadModelPort.updateReviewAggregate(
                payload.productId(), payload.totalCount(), payload.averageRating()
        );
        log.info("상품 카탈로그 Read Model 리뷰 집계 업데이트 (등록). productId={}", payload.productId());
        ack.acknowledge();
    }

    @KafkaListener(topics = KafkaTopicConstants.REVIEW_UPDATED, groupId = CONSUMER_GROUP_ID)
    public void handleReviewUpdated(ConsumerRecord<String, EventEnvelope<?>> record, Acknowledgment ack) {
        EventEnvelope<?> envelope = record.value();
        if (EventPayloadValidator.hasInvalidEnvelope(envelope, record)
                || EventPayloadValidator.hasEventTypeMismatch(envelope, KafkaTopicConstants.REVIEW_UPDATED)) {
            ack.acknowledge();
            return;
        }

        ReviewUpdatedEvent payload = envelope.getPayloadAs(ReviewUpdatedEvent.class, objectMapper);
        if (FormatValidator.hasNoValue(payload)
                || hasInvalidReviewAggregate(payload.productId(), payload.totalCount(), payload.averageRating())) {
            ack.acknowledge();
            return;
        }

        saveProductCatalogReadModelPort.updateReviewAggregate(
                payload.productId(), payload.totalCount(), payload.averageRating()
        );
        log.info("상품 카탈로그 Read Model 리뷰 집계 업데이트 (수정). productId={}", payload.productId());
        ack.acknowledge();
    }

    @KafkaListener(topics = KafkaTopicConstants.REVIEW_DELETED, groupId = CONSUMER_GROUP_ID)
    public void handleReviewDeleted(ConsumerRecord<String, EventEnvelope<?>> record, Acknowledgment ack) {
        EventEnvelope<?> envelope = record.value();
        if (EventPayloadValidator.hasInvalidEnvelope(envelope, record)
                || EventPayloadValidator.hasEventTypeMismatch(envelope, KafkaTopicConstants.REVIEW_DELETED)) {
            ack.acknowledge();
            return;
        }

        ReviewDeletedEvent payload = envelope.getPayloadAs(ReviewDeletedEvent.class, objectMapper);
        if (FormatValidator.hasNoValue(payload)
                || hasInvalidReviewAggregate(payload.productId(), payload.totalCount(), payload.averageRating())) {
            ack.acknowledge();
            return;
        }

        saveProductCatalogReadModelPort.updateReviewAggregate(
                payload.productId(), payload.totalCount(), payload.averageRating()
        );
        log.info("상품 카탈로그 Read Model 리뷰 집계 업데이트 (삭제). productId={}", payload.productId());
        ack.acknowledge();
    }

    private boolean isProductCatalogRepresentativeImage(ImageChangedEvent payload) {
        return FormatValidator.hasValue(payload.fileSort())
                && FileSort.PRODUCT_CATALOG_IMAGE.name().equals(payload.fileSort())
                && FormatValidator.hasValue(payload.sortOrder())
                && payload.sortOrder() == 0;
    }

    private boolean hasInvalidReviewAggregate(Long productId, Integer totalCount, BigDecimal averageRating) {
        if (FormatValidator.hasNoValue(productId) || productId <= 0) {
            return true;
        }
        return FormatValidator.hasNoValue(totalCount) || FormatValidator.hasNoValue(averageRating);
    }
}
