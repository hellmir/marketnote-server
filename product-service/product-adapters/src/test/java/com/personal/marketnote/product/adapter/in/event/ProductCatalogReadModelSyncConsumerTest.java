package com.personal.marketnote.product.adapter.in.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.domain.file.FileSort;
import com.personal.marketnote.common.kafka.KafkaTopicConstants;
import com.personal.marketnote.common.kafka.event.*;
import com.personal.marketnote.product.port.in.usecase.pricepolicysync.SyncPricePolicyCreatedUseCase;
import com.personal.marketnote.product.port.in.usecase.tagsync.SyncProductTagChangeUseCase;
import com.personal.marketnote.product.port.out.productcatalog.SaveProductCatalogReadModelPort;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

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
    private CacheManager cacheManager;

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
        UUID eventId = UUID.randomUUID();
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
        UUID eventId = UUID.randomUUID();
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

    @Test
    @DisplayName("PricePolicyCreated 이벤트 productName 이 null 이면 ack skip 하고 ReadModel 수정 호출을 하지 않는다")
    void handlePricePolicyCreated_nullProductName_acknowledgesAndSkips() {
        PricePolicyCreatedEvent payload = new PricePolicyCreatedEvent(
                100L, 200L, 300L, null, "브랜드",
                10000L, 9000L, 100L, 0L, List.of(), List.of()
        );
        EventEnvelope<PricePolicyCreatedEvent> envelope = new EventEnvelope<>(
                UUID.randomUUID().toString(), KafkaTopicConstants.PRICE_POLICY_CREATED,
                "product-service", FIXED_TIME, payload
        );
        ConsumerRecord<String, EventEnvelope<?>> record =
                recordOf(KafkaTopicConstants.PRICE_POLICY_CREATED, envelope);

        consumer.handlePricePolicyCreated(record, acknowledgment);

        verifyNoInteractions(syncPricePolicyCreatedUseCase);
        verifyNoInteractions(saveProductCatalogReadModelPort);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("PricePolicyDeleted 이벤트 수신 시 deleteByPricePolicyId 를 pricePolicyId 기준으로 호출한다")
    void handlePricePolicyDeleted_validPayload_deletesReadModelByPricePolicyId() {
        PricePolicyDeletedEvent payload = new PricePolicyDeletedEvent(100L, 200L);
        EventEnvelope<PricePolicyDeletedEvent> envelope =
                envelopeOf(KafkaTopicConstants.PRICE_POLICY_DELETED, "product-service", payload);
        ConsumerRecord<String, EventEnvelope<?>> record =
                recordOf(KafkaTopicConstants.PRICE_POLICY_DELETED, envelope);

        consumer.handlePricePolicyDeleted(record, acknowledgment);

        verify(saveProductCatalogReadModelPort).deleteByPricePolicyId(200L);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("PricePolicyDeleted envelope 가 null 이면 deleteByPricePolicyId 를 호출하지 않는다")
    void handlePricePolicyDeleted_nullEnvelope_acknowledges() {
        ConsumerRecord<String, EventEnvelope<?>> record =
                new ConsumerRecord<>(KafkaTopicConstants.PRICE_POLICY_DELETED, 0, 0, "1", null);

        consumer.handlePricePolicyDeleted(record, acknowledgment);

        verifyNoInteractions(saveProductCatalogReadModelPort);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("PricePolicyDeleted pricePolicyId 가 null 이면 deleteByPricePolicyId 를 호출하지 않는다")
    void handlePricePolicyDeleted_nullPricePolicyId_acknowledges() {
        PricePolicyDeletedEvent payload = new PricePolicyDeletedEvent(100L, null);
        EventEnvelope<PricePolicyDeletedEvent> envelope =
                envelopeOf(KafkaTopicConstants.PRICE_POLICY_DELETED, "product-service", payload);
        ConsumerRecord<String, EventEnvelope<?>> record =
                recordOf(KafkaTopicConstants.PRICE_POLICY_DELETED, envelope);

        consumer.handlePricePolicyDeleted(record, acknowledgment);

        verifyNoInteractions(saveProductCatalogReadModelPort);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("ProductDeleted 이벤트 수신 시 deleteByProductId 를 호출한다")
    void handleProductDeleted_validPayload_deletesReadModel() {
        ProductDeletedEvent payload = new ProductDeletedEvent(100L);
        EventEnvelope<ProductDeletedEvent> envelope =
                envelopeOf(KafkaTopicConstants.PRODUCT_DELETED, "product-service", payload);
        ConsumerRecord<String, EventEnvelope<?>> record =
                recordOf(KafkaTopicConstants.PRODUCT_DELETED, envelope);

        consumer.handleProductDeleted(record, acknowledgment);

        verify(saveProductCatalogReadModelPort).deleteByProductId(100L);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("ProductDeleted envelope 가 null 이면 deleteByProductId 를 호출하지 않는다")
    void handleProductDeleted_nullEnvelope_acknowledges() {
        ConsumerRecord<String, EventEnvelope<?>> record =
                new ConsumerRecord<>(KafkaTopicConstants.PRODUCT_DELETED, 0, 0, "1", null);

        consumer.handleProductDeleted(record, acknowledgment);

        verifyNoInteractions(saveProductCatalogReadModelPort);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("ProductDeleted productId 가 null 이면 deleteByProductId 를 호출하지 않는다")
    void handleProductDeleted_nullProductId_acknowledges() {
        ProductDeletedEvent payload = new ProductDeletedEvent(null);
        EventEnvelope<ProductDeletedEvent> envelope =
                envelopeOf(KafkaTopicConstants.PRODUCT_DELETED, "product-service", payload);
        ConsumerRecord<String, EventEnvelope<?>> record =
                recordOf(KafkaTopicConstants.PRODUCT_DELETED, envelope);

        consumer.handleProductDeleted(record, acknowledgment);

        verifyNoInteractions(saveProductCatalogReadModelPort);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("ProductUpdated 이벤트 수신 시 updateProductInfo를 productId 기준으로 호출한다")
    void handleProductUpdated_validPayload_updatesProductInfo() {
        ProductUpdatedEvent payload = productUpdatedPayload(100L, "수정된 상품", "수정된 브랜드");
        EventEnvelope<ProductUpdatedEvent> envelope =
                envelopeOf(KafkaTopicConstants.PRODUCT_UPDATED, "product-service", payload);
        ConsumerRecord<String, EventEnvelope<?>> record =
                recordOf(KafkaTopicConstants.PRODUCT_UPDATED, envelope);

        consumer.handleProductUpdated(record, acknowledgment);

        verify(saveProductCatalogReadModelPort).updateProductInfo(100L, "수정된 상품", "수정된 브랜드");
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("ProductUpdated envelope가 null이면 updateProductInfo를 호출하지 않는다")
    void handleProductUpdated_nullEnvelope_acknowledges() {
        ConsumerRecord<String, EventEnvelope<?>> record =
                new ConsumerRecord<>(KafkaTopicConstants.PRODUCT_UPDATED, 0, 0, "1", null);

        consumer.handleProductUpdated(record, acknowledgment);

        verifyNoInteractions(saveProductCatalogReadModelPort);
        verify(acknowledgment).acknowledge();
    }

    @Test
    @DisplayName("ProductUpdated productId가 null이면 updateProductInfo를 호출하지 않는다")
    void handleProductUpdated_nullProductId_acknowledges() {
        ProductUpdatedEvent payload = productUpdatedPayload(null, "수정된 상품", "수정된 브랜드");
        EventEnvelope<ProductUpdatedEvent> envelope =
                envelopeOf(KafkaTopicConstants.PRODUCT_UPDATED, "product-service", payload);
        ConsumerRecord<String, EventEnvelope<?>> record =
                recordOf(KafkaTopicConstants.PRODUCT_UPDATED, envelope);

        consumer.handleProductUpdated(record, acknowledgment);

        verifyNoInteractions(saveProductCatalogReadModelPort);
        verify(acknowledgment).acknowledge();
    }

    private ProductUpdatedEvent productUpdatedPayload(Long productId, String productName, String brandName) {
        return new ProductUpdatedEvent(
                productId, null, productName, brandName,
                null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null
        );
    }

    @Nested
    @DisplayName("CacheEviction")
    class CacheEviction {

        @Test
        @DisplayName("9개 핸들러(Product Registered/Updated/Deleted, PricePolicy Created/Deleted, Inventory, Review Registered/Updated/Deleted)에 product:list:first allEntries=true CacheEvict가 적용되어 있다")
        void nineHandlers_haveCacheEvictAnnotation() {
            List<String> targetHandlers = List.of(
                    "handleProductRegistered",
                    "handleProductUpdated",
                    "handleProductDeleted",
                    "handlePricePolicyCreated",
                    "handlePricePolicyDeleted",
                    "handleInventoryChanged",
                    "handleReviewRegistered",
                    "handleReviewUpdated",
                    "handleReviewDeleted"
            );

            for (String handlerName : targetHandlers) {
                Method method = findKafkaListenerMethod(handlerName);
                CacheEvict cacheEvict = method.getAnnotation(CacheEvict.class);

                assertThat(cacheEvict)
                        .as("%s 에 @CacheEvict 애노테이션이 적용되어야 한다", handlerName)
                        .isNotNull();
                assertThat(cacheEvict.value())
                        .as("%s @CacheEvict value", handlerName)
                        .containsExactly("product:list:first");
                assertThat(cacheEvict.allEntries())
                        .as("%s @CacheEvict allEntries", handlerName)
                        .isTrue();
            }
        }

        @Test
        @DisplayName("handleImageChanged 는 @CacheEvict 애노테이션 대신 수동 캐시 clear 전략을 사용한다")
        void handleImageChanged_doesNotHaveCacheEvictAnnotation() {
            Method method = findKafkaListenerMethod("handleImageChanged");

            CacheEvict cacheEvict = method.getAnnotation(CacheEvict.class);

            assertThat(cacheEvict)
                    .as("handleImageChanged 는 대표 이미지 조건부 clear 를 위해 @CacheEvict 미적용")
                    .isNull();
        }

        @Test
        @DisplayName("handleImageChanged 대표 이미지(sortOrder=0) 수신 시 product:list:first 캐시를 clear한다")
        void handleImageChanged_representative_clearsCache() {
            Cache cache = mock(Cache.class);
            when(cacheManager.getCache("product:list:first")).thenReturn(cache);

            ImageChangedEvent payload = new ImageChangedEvent(
                    10L, 100L, "PRODUCT", FileSort.PRODUCT_CATALOG_IMAGE.name(),
                    "https://example.com/catalog.png", 0, List.of(), ImageChangeAction.CREATED
            );
            EventEnvelope<ImageChangedEvent> envelope =
                    envelopeOf(KafkaTopicConstants.FILE_IMAGE_CHANGED, "file-service", payload);
            ConsumerRecord<String, EventEnvelope<?>> record =
                    recordOf(KafkaTopicConstants.FILE_IMAGE_CHANGED, envelope);

            consumer.handleImageChanged(record, acknowledgment);

            verify(cache).clear();
            verify(acknowledgment).acknowledge();
        }

        @Test
        @DisplayName("handleImageChanged 비대표 이미지(sortOrder!=0) 수신 시 product:list:first 캐시를 clear하지 않는다")
        void handleImageChanged_notRepresentative_doesNotClearCache() {
            ImageChangedEvent payload = new ImageChangedEvent(
                    10L, 100L, "PRODUCT", FileSort.PRODUCT_CATALOG_IMAGE.name(),
                    "https://example.com/catalog.png", 1, List.of(), ImageChangeAction.CREATED
            );
            EventEnvelope<ImageChangedEvent> envelope =
                    envelopeOf(KafkaTopicConstants.FILE_IMAGE_CHANGED, "file-service", payload);
            ConsumerRecord<String, EventEnvelope<?>> record =
                    recordOf(KafkaTopicConstants.FILE_IMAGE_CHANGED, envelope);

            consumer.handleImageChanged(record, acknowledgment);

            verifyNoInteractions(cacheManager);
            verify(acknowledgment).acknowledge();
        }

        @Test
        @DisplayName("handleImageChanged 대표 이미지 수신 시 CacheManager가 null Cache 를 반환해도 예외 없이 동작한다")
        void handleImageChanged_nullCache_doesNotThrow() {
            when(cacheManager.getCache("product:list:first")).thenReturn(null);

            ImageChangedEvent payload = new ImageChangedEvent(
                    10L, 100L, "PRODUCT", FileSort.PRODUCT_CATALOG_IMAGE.name(),
                    "https://example.com/catalog.png", 0, List.of(), ImageChangeAction.CREATED
            );
            EventEnvelope<ImageChangedEvent> envelope =
                    envelopeOf(KafkaTopicConstants.FILE_IMAGE_CHANGED, "file-service", payload);
            ConsumerRecord<String, EventEnvelope<?>> record =
                    recordOf(KafkaTopicConstants.FILE_IMAGE_CHANGED, envelope);

            consumer.handleImageChanged(record, acknowledgment);

            verify(cacheManager).getCache("product:list:first");
            verify(acknowledgment).acknowledge();
        }

        private Method findKafkaListenerMethod(String methodName) {
            return Arrays.stream(ProductCatalogReadModelSyncConsumer.class.getDeclaredMethods())
                    .filter(m -> m.getName().equals(methodName))
                    .filter(m -> m.isAnnotationPresent(KafkaListener.class))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError(methodName + " 메서드를 찾을 수 없음"));
        }
    }
}
