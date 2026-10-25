package com.personal.marketnote.product.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.domain.file.FileSort;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.EventEnvelope;
import com.personal.marketnote.common.kafka.event.ImageChangeAction;
import com.personal.marketnote.common.kafka.event.ImageChangedEvent;
import com.personal.marketnote.common.kafka.event.InventoryChangedEvent;
import com.personal.marketnote.common.kafka.event.InventoryChangeAction;
import com.personal.marketnote.common.kafka.event.PricePolicyCreatedEvent;
import com.personal.marketnote.common.kafka.event.ProductRegisteredEvent;
import com.personal.marketnote.common.kafka.event.ProductTagChangedEvent;
import com.personal.marketnote.common.kafka.event.ReviewDeletedEvent;
import com.personal.marketnote.common.kafka.event.ReviewRegisteredEvent;
import com.personal.marketnote.common.kafka.event.ReviewUpdatedEvent;
import com.personal.marketnote.product.port.in.usecase.pricepolicysync.SyncPricePolicyCreatedUseCase;
import com.personal.marketnote.product.port.in.usecase.tagsync.SyncProductTagChangeUseCase;
import com.personal.marketnote.product.port.out.productcatalog.SaveProductCatalogReadModelPort;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class ProductCatalogReadModelSyncConsumerTest {

    @InjectMocks
    private ProductCatalogReadModelSyncConsumer consumer;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private SaveProductCatalogReadModelPort saveProductCatalogReadModelPort;

    @Mock
    private SyncProductTagChangeUseCase syncProductTagChangeUseCase;

    @Mock
    private SyncPricePolicyCreatedUseCase syncPricePolicyCreatedUseCase;

    @Mock
    private Acknowledgment acknowledgment;

    private static final LocalDateTime FIXED_TIME =
            LocalDateTime.now(Clock.fixed(Instant.parse("2026-10-17T10:00:00Z"), ZoneId.of("Asia/Seoul")));

    private <T> EventEnvelope<T> envelopeOf(String eventType, String source, T payload) {
        return new EventEnvelope<>("test-event-id", eventType, source, FIXED_TIME, payload);
    }

    private ConsumerRecord<String, EventEnvelope<?>> recordOf(String topic, EventEnvelope<?> envelope) {
        return new ConsumerRecord<>(topic, 0, 0, "1", envelope);
    }

    @Test
    @DisplayName("ProductRegistered envelope가 null이면 upsert를 호출하지 않는다")
    void handleProductRegistered_nullEnvelope_acknowledges() {
        ConsumerRecord<String, EventEnvelope<?>> record =
                new ConsumerRecord<>(KafkaTopicConstants.PRODUCT_REGISTERED, 0, 0, "1", null);

        consumer.handleProductRegistered(record, acknowledgment);

        verifyNoInteractions(saveProductCatalogReadModelPort);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("ProductRegistered 이벤트 수신 시 upsertCatalog를 호출한다")
    void handleProductRegistered_validPayload_upsertsCatalog() {
        ProductRegisteredEvent payload = new ProductRegisteredEvent(
                100L, null, 1L, 200L, "테스트 상품", "PRODUCT", "테스트 브랜드", 10000L, 9000L, 100L, 0L, null, null);
        EventEnvelope<ProductRegisteredEvent> envelope =
                envelopeOf(KafkaTopicConstants.PRODUCT_REGISTERED, "product-service", payload);
        ConsumerRecord<String, EventEnvelope<?>> record =
                recordOf(KafkaTopicConstants.PRODUCT_REGISTERED, envelope);

        consumer.handleProductRegistered(record, acknowledgment);

        verify(saveProductCatalogReadModelPort).upsertCatalog(
                eq(1L), eq(100L), eq(200L), eq(null),
                eq("테스트 상품"), eq("테스트 브랜드"),
                eq(10000L), eq(9000L), eq(BigDecimal.ZERO), eq(100L),
                eq(BigDecimal.ZERO), eq(0L), eq(0L),
                eq(0L), eq((String) null), eq((String) null)
        );
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("ProductRegistered 이벤트에 sales/productTags/selectedOptions 가 포함되면 JSON 으로 직렬화하여 upsert 한다")
    void handleProductRegistered_withSalesTagsOptions_serializesAndUpserts() throws Exception {
        List<ProductRegisteredEvent.ProductTagSnapshot> tags = List.of(
                new ProductRegisteredEvent.ProductTagSnapshot(10L, "NEW", 1L),
                new ProductRegisteredEvent.ProductTagSnapshot(11L, "HOT", 2L)
        );
        List<ProductRegisteredEvent.ProductOptionSnapshot> options = List.of(
                new ProductRegisteredEvent.ProductOptionSnapshot(100L, "색상: 빨강"),
                new ProductRegisteredEvent.ProductOptionSnapshot(101L, "사이즈: L")
        );
        ProductRegisteredEvent payload = new ProductRegisteredEvent(
                100L, null, 1L, 200L, "테스트 상품", "PRODUCT", "테스트 브랜드",
                10000L, 9000L, 100L, 50L, tags, options
        );
        EventEnvelope<ProductRegisteredEvent> envelope =
                envelopeOf(KafkaTopicConstants.PRODUCT_REGISTERED, "product-service", payload);
        ConsumerRecord<String, EventEnvelope<?>> record =
                recordOf(KafkaTopicConstants.PRODUCT_REGISTERED, envelope);
        String expectedTagsJson = objectMapper.writeValueAsString(tags);
        String expectedOptionsJson = objectMapper.writeValueAsString(options);

        consumer.handleProductRegistered(record, acknowledgment);

        verify(saveProductCatalogReadModelPort).upsertCatalog(
                eq(1L), eq(100L), eq(200L), eq(null),
                eq("테스트 상품"), eq("테스트 브랜드"),
                eq(10000L), eq(9000L), eq(BigDecimal.ZERO), eq(100L),
                eq(BigDecimal.ZERO), eq(0L), eq(0L),
                eq(50L), eq(expectedTagsJson), eq(expectedOptionsJson)
        );
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("ProductRegistered productId가 null이면 upsert를 호출하지 않는다")
    void handleProductRegistered_nullProductId_acknowledges() {
        ProductRegisteredEvent payload = new ProductRegisteredEvent(
                null, null, 1L, 200L, "테스트 상품", "PRODUCT", "테스트 브랜드", 10000L, 9000L, 100L, 0L, null, null);
        EventEnvelope<ProductRegisteredEvent> envelope =
                envelopeOf(KafkaTopicConstants.PRODUCT_REGISTERED, "product-service", payload);
        ConsumerRecord<String, EventEnvelope<?>> record =
                recordOf(KafkaTopicConstants.PRODUCT_REGISTERED, envelope);

        consumer.handleProductRegistered(record, acknowledgment);

        verifyNoInteractions(saveProductCatalogReadModelPort);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("InventoryChanged 이벤트 수신 시 updateStockQuantity를 호출한다")
    void handleInventoryChanged_validPayload_updatesStock() {
        InventoryChangedEvent payload = new InventoryChangedEvent(
                1L, 100L, 50, InventoryChangeAction.UPDATED
        );
        EventEnvelope<InventoryChangedEvent> envelope =
                envelopeOf(KafkaTopicConstants.INVENTORY_CHANGED, "commerce-service", payload);
        ConsumerRecord<String, EventEnvelope<?>> record =
                recordOf(KafkaTopicConstants.INVENTORY_CHANGED, envelope);

        consumer.handleInventoryChanged(record, acknowledgment);

        verify(saveProductCatalogReadModelPort).updateStockQuantity(1L, 50);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("InventoryChanged pricePolicyId가 null이면 updateStockQuantity를 호출하지 않는다")
    void handleInventoryChanged_nullPricePolicyId_acknowledges() {
        InventoryChangedEvent payload = new InventoryChangedEvent(
                null, 100L, 50, InventoryChangeAction.UPDATED
        );
        EventEnvelope<InventoryChangedEvent> envelope =
                envelopeOf(KafkaTopicConstants.INVENTORY_CHANGED, "commerce-service", payload);
        ConsumerRecord<String, EventEnvelope<?>> record =
                recordOf(KafkaTopicConstants.INVENTORY_CHANGED, envelope);

        consumer.handleInventoryChanged(record, acknowledgment);

        verifyNoInteractions(saveProductCatalogReadModelPort);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("ImageChanged 대표 이미지(sortOrder=0) 이벤트 수신 시 updateRepresentativeImage 를 resized URL 과 함께 호출한다")
    void handleImageChanged_representativeImage_updatesImageWithResized() {
        ImageChangedEvent payload = new ImageChangedEvent(
                10L, 100L, "PRODUCT", FileSort.PRODUCT_CATALOG_IMAGE.name(),
                "https://example.com/catalog.png", 0,
                List.of(
                        new ImageChangedEvent.ResizedImageInfo("SMALL", "https://example.com/catalog-sm.png"),
                        new ImageChangedEvent.ResizedImageInfo("MEDIUM", "https://example.com/catalog-md.png")
                ),
                ImageChangeAction.CREATED
        );
        EventEnvelope<ImageChangedEvent> envelope =
                envelopeOf(KafkaTopicConstants.FILE_IMAGE_CHANGED, "file-service", payload);
        ConsumerRecord<String, EventEnvelope<?>> record =
                recordOf(KafkaTopicConstants.FILE_IMAGE_CHANGED, envelope);

        consumer.handleImageChanged(record, acknowledgment);

        verify(saveProductCatalogReadModelPort)
                .updateRepresentativeImage(
                        eq(100L),
                        eq("https://example.com/catalog.png"),
                        eq(List.of("https://example.com/catalog-sm.png", "https://example.com/catalog-md.png"))
                );
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("ImageChanged resized 이미지가 없으면 빈 리스트로 updateRepresentativeImage 를 호출한다")
    void handleImageChanged_noResizedImages_callsWithEmptyList() {
        ImageChangedEvent payload = new ImageChangedEvent(
                10L, 100L, "PRODUCT", FileSort.PRODUCT_CATALOG_IMAGE.name(),
                "https://example.com/catalog.png", 0, List.of(), ImageChangeAction.CREATED
        );
        EventEnvelope<ImageChangedEvent> envelope =
                envelopeOf(KafkaTopicConstants.FILE_IMAGE_CHANGED, "file-service", payload);
        ConsumerRecord<String, EventEnvelope<?>> record =
                recordOf(KafkaTopicConstants.FILE_IMAGE_CHANGED, envelope);

        consumer.handleImageChanged(record, acknowledgment);

        verify(saveProductCatalogReadModelPort)
                .updateRepresentativeImage(eq(100L), eq("https://example.com/catalog.png"), eq(List.of()));
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("ImageChanged 대표 이미지가 아니면(sortOrder != 0) updateRepresentativeImage 를 호출하지 않는다")
    void handleImageChanged_notRepresentative_acknowledges() {
        ImageChangedEvent payload = new ImageChangedEvent(
                10L, 100L, "PRODUCT", FileSort.PRODUCT_CATALOG_IMAGE.name(),
                "https://example.com/catalog2.png", 1, List.of(), ImageChangeAction.CREATED
        );
        EventEnvelope<ImageChangedEvent> envelope =
                envelopeOf(KafkaTopicConstants.FILE_IMAGE_CHANGED, "file-service", payload);
        ConsumerRecord<String, EventEnvelope<?>> record =
                recordOf(KafkaTopicConstants.FILE_IMAGE_CHANGED, envelope);

        consumer.handleImageChanged(record, acknowledgment);

        verifyNoInteractions(saveProductCatalogReadModelPort);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("ImageChanged fileSort가 PRODUCT_CATALOG_IMAGE가 아니면 updateRepresentativeImage 를 호출하지 않는다")
    void handleImageChanged_wrongFileSort_acknowledges() {
        ImageChangedEvent payload = new ImageChangedEvent(
                10L, 100L, "PRODUCT", FileSort.PRODUCT_REPRESENTATIVE_IMAGE.name(),
                "https://example.com/repr.png", 0, List.of(), ImageChangeAction.CREATED
        );
        EventEnvelope<ImageChangedEvent> envelope =
                envelopeOf(KafkaTopicConstants.FILE_IMAGE_CHANGED, "file-service", payload);
        ConsumerRecord<String, EventEnvelope<?>> record =
                recordOf(KafkaTopicConstants.FILE_IMAGE_CHANGED, envelope);

        consumer.handleImageChanged(record, acknowledgment);

        verifyNoInteractions(saveProductCatalogReadModelPort);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("ReviewRegistered 이벤트 수신 시 updateReviewAggregate를 호출한다")
    void handleReviewRegistered_validPayload_updatesAggregate() {
        ReviewRegisteredEvent payload = new ReviewRegisteredEvent(
                700L, 1L, 100L, 10, new BigDecimal("4.50"), 300L, 500L
        );
        EventEnvelope<ReviewRegisteredEvent> envelope =
                envelopeOf(KafkaTopicConstants.REVIEW_REGISTERED, "community-service", payload);
        ConsumerRecord<String, EventEnvelope<?>> record =
                recordOf(KafkaTopicConstants.REVIEW_REGISTERED, envelope);

        consumer.handleReviewRegistered(record, acknowledgment);

        verify(saveProductCatalogReadModelPort).updateReviewAggregate(100L, 10, new BigDecimal("4.50"));
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("ReviewRegistered totalCount가 null이면 updateReviewAggregate를 호출하지 않는다")
    void handleReviewRegistered_nullTotalCount_acknowledges() {
        ReviewRegisteredEvent payload = new ReviewRegisteredEvent(
                700L, 1L, 100L, null, new BigDecimal("4.50"), 300L, 500L
        );
        EventEnvelope<ReviewRegisteredEvent> envelope =
                envelopeOf(KafkaTopicConstants.REVIEW_REGISTERED, "community-service", payload);
        ConsumerRecord<String, EventEnvelope<?>> record =
                recordOf(KafkaTopicConstants.REVIEW_REGISTERED, envelope);

        consumer.handleReviewRegistered(record, acknowledgment);

        verifyNoInteractions(saveProductCatalogReadModelPort);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("ReviewUpdated 이벤트 수신 시 updateReviewAggregate를 호출한다")
    void handleReviewUpdated_validPayload_updatesAggregate() {
        ReviewUpdatedEvent payload = new ReviewUpdatedEvent(500L, 100L, 12, new BigDecimal("4.30"));
        EventEnvelope<ReviewUpdatedEvent> envelope =
                envelopeOf(KafkaTopicConstants.REVIEW_UPDATED, "community-service", payload);
        ConsumerRecord<String, EventEnvelope<?>> record =
                recordOf(KafkaTopicConstants.REVIEW_UPDATED, envelope);

        consumer.handleReviewUpdated(record, acknowledgment);

        verify(saveProductCatalogReadModelPort).updateReviewAggregate(100L, 12, new BigDecimal("4.30"));
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("ReviewDeleted 이벤트 수신 시 updateReviewAggregate를 호출한다")
    void handleReviewDeleted_validPayload_updatesAggregate() {
        ReviewDeletedEvent payload = new ReviewDeletedEvent(500L, 100L, 8, new BigDecimal("4.20"), 300L);
        EventEnvelope<ReviewDeletedEvent> envelope =
                envelopeOf(KafkaTopicConstants.REVIEW_DELETED, "community-service", payload);
        ConsumerRecord<String, EventEnvelope<?>> record =
                recordOf(KafkaTopicConstants.REVIEW_DELETED, envelope);

        consumer.handleReviewDeleted(record, acknowledgment);

        verify(saveProductCatalogReadModelPort).updateReviewAggregate(100L, 8, new BigDecimal("4.20"));
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("ProductTagChanged 이벤트 수신 시 SyncProductTagChangeUseCase 에 위임한다")
    void handleProductTagChanged_validPayload_delegatesToUseCase() {
        java.util.UUID eventId = java.util.UUID.randomUUID();
        ProductTagChangedEvent payload = new ProductTagChangedEvent(
                777L,
                List.of(new ProductTagChangedEvent.ProductTagSnapshot(1L, "봄", 1L)),
                ProductTagChangedEvent.Action.UPDATED
        );
        EventEnvelope<ProductTagChangedEvent> envelope = new EventEnvelope<>(
                eventId.toString(), KafkaTopicConstants.PRODUCT_TAG_CHANGED,
                "product-service", FIXED_TIME, payload
        );
        ConsumerRecord<String, EventEnvelope<?>> record =
                recordOf(KafkaTopicConstants.PRODUCT_TAG_CHANGED, envelope);

        consumer.handleProductTagChanged(record, acknowledgment);

        verify(syncProductTagChangeUseCase).syncProductTagChange(eq(eventId), eq(payload));
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("ProductTagChanged 이벤트 envelope이 null 이면 UseCase 를 호출하지 않는다")
    void handleProductTagChanged_nullEnvelope_acknowledges() {
        ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(
                KafkaTopicConstants.PRODUCT_TAG_CHANGED, 0, 0, "1", null);

        consumer.handleProductTagChanged(record, acknowledgment);

        verifyNoInteractions(syncProductTagChangeUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("ProductTagChanged 이벤트 productId 가 유효하지 않으면 UseCase 를 호출하지 않는다")
    void handleProductTagChanged_invalidProductId_acknowledges() {
        ProductTagChangedEvent payload = new ProductTagChangedEvent(
                null, List.of(), ProductTagChangedEvent.Action.UPDATED
        );
        EventEnvelope<ProductTagChangedEvent> envelope = envelopeOf(
                KafkaTopicConstants.PRODUCT_TAG_CHANGED, "product-service", payload
        );
        ConsumerRecord<String, EventEnvelope<?>> record =
                recordOf(KafkaTopicConstants.PRODUCT_TAG_CHANGED, envelope);

        consumer.handleProductTagChanged(record, acknowledgment);

        verifyNoInteractions(syncProductTagChangeUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("ProductTagChanged 이벤트 eventId 가 UUID 형식이 아니면 UseCase 를 호출하지 않는다")
    void handleProductTagChanged_invalidEventId_acknowledges() {
        ProductTagChangedEvent payload = new ProductTagChangedEvent(
                1L, List.of(), ProductTagChangedEvent.Action.UPDATED
        );
        EventEnvelope<ProductTagChangedEvent> envelope = new EventEnvelope<>(
                "invalid-uuid", KafkaTopicConstants.PRODUCT_TAG_CHANGED,
                "product-service", FIXED_TIME, payload
        );
        ConsumerRecord<String, EventEnvelope<?>> record =
                recordOf(KafkaTopicConstants.PRODUCT_TAG_CHANGED, envelope);

        consumer.handleProductTagChanged(record, acknowledgment);

        verifyNoInteractions(syncProductTagChangeUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("PricePolicyCreated 이벤트 수신 시 SyncPricePolicyCreatedUseCase 에 위임한다")
    void handlePricePolicyCreated_validPayload_delegatesToUseCase() {
        java.util.UUID eventId = java.util.UUID.randomUUID();
        PricePolicyCreatedEvent payload = new PricePolicyCreatedEvent(
                100L, 200L, 300L, "테스트 상품", "브랜드",
                10000L, 9000L, 100L, 0L,
                List.of(new PricePolicyCreatedEvent.ProductTagSnapshot(1L, "NEW", 1L)),
                List.of(new PricePolicyCreatedEvent.ProductOptionSnapshot(500L, "색상: 빨강"))
        );
        EventEnvelope<PricePolicyCreatedEvent> envelope = new EventEnvelope<>(
                eventId.toString(), KafkaTopicConstants.PRICE_POLICY_CREATED,
                "product-service", FIXED_TIME, payload
        );
        ConsumerRecord<String, EventEnvelope<?>> record =
                recordOf(KafkaTopicConstants.PRICE_POLICY_CREATED, envelope);

        consumer.handlePricePolicyCreated(record, acknowledgment);

        verify(syncPricePolicyCreatedUseCase).syncPricePolicyCreated(eq(eventId), eq(payload));
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("PricePolicyCreated 이벤트 envelope 이 null 이면 UseCase 를 호출하지 않는다")
    void handlePricePolicyCreated_nullEnvelope_acknowledges() {
        ConsumerRecord<String, EventEnvelope<?>> record = new ConsumerRecord<>(
                KafkaTopicConstants.PRICE_POLICY_CREATED, 0, 0, "1", null);

        consumer.handlePricePolicyCreated(record, acknowledgment);

        verifyNoInteractions(syncPricePolicyCreatedUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("PricePolicyCreated 이벤트 productId 가 유효하지 않으면 UseCase 를 호출하지 않는다")
    void handlePricePolicyCreated_invalidProductId_acknowledges() {
        PricePolicyCreatedEvent payload = new PricePolicyCreatedEvent(
                null, 200L, 300L, "상품", "브랜드",
                10000L, 9000L, 100L, 0L, List.of(), List.of()
        );
        EventEnvelope<PricePolicyCreatedEvent> envelope =
                envelopeOf(KafkaTopicConstants.PRICE_POLICY_CREATED, "product-service", payload);
        ConsumerRecord<String, EventEnvelope<?>> record =
                recordOf(KafkaTopicConstants.PRICE_POLICY_CREATED, envelope);

        consumer.handlePricePolicyCreated(record, acknowledgment);

        verifyNoInteractions(syncPricePolicyCreatedUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("PricePolicyCreated 이벤트 pricePolicyId 가 유효하지 않으면 UseCase 를 호출하지 않는다")
    void handlePricePolicyCreated_invalidPricePolicyId_acknowledges() {
        PricePolicyCreatedEvent payload = new PricePolicyCreatedEvent(
                100L, null, 300L, "상품", "브랜드",
                10000L, 9000L, 100L, 0L, List.of(), List.of()
        );
        EventEnvelope<PricePolicyCreatedEvent> envelope =
                envelopeOf(KafkaTopicConstants.PRICE_POLICY_CREATED, "product-service", payload);
        ConsumerRecord<String, EventEnvelope<?>> record =
                recordOf(KafkaTopicConstants.PRICE_POLICY_CREATED, envelope);

        consumer.handlePricePolicyCreated(record, acknowledgment);

        verifyNoInteractions(syncPricePolicyCreatedUseCase);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("PricePolicyCreated 이벤트 eventId 가 UUID 형식이 아니면 UseCase 를 호출하지 않는다")
    void handlePricePolicyCreated_invalidEventId_acknowledges() {
        PricePolicyCreatedEvent payload = new PricePolicyCreatedEvent(
                100L, 200L, 300L, "상품", "브랜드",
                10000L, 9000L, 100L, 0L, List.of(), List.of()
        );
        EventEnvelope<PricePolicyCreatedEvent> envelope = new EventEnvelope<>(
                "invalid-uuid", KafkaTopicConstants.PRICE_POLICY_CREATED,
                "product-service", FIXED_TIME, payload
        );
        ConsumerRecord<String, EventEnvelope<?>> record =
                recordOf(KafkaTopicConstants.PRICE_POLICY_CREATED, envelope);

        consumer.handlePricePolicyCreated(record, acknowledgment);

        verifyNoInteractions(syncPricePolicyCreatedUseCase);
        verify(acknowledgment).acknowledge();
    }
}
