package com.personal.marketnote.product.service.pricepolicysync;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.event.PricePolicyCreatedEvent;
import com.personal.marketnote.product.port.out.pricepolicysync.PricePolicyCreatedSyncLogPort;
import com.personal.marketnote.product.port.out.productcatalog.SaveProductCatalogReadModelPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SyncPricePolicyCreatedUseCaseTest {

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private SaveProductCatalogReadModelPort saveProductCatalogReadModelPort;

    @Mock
    private PricePolicyCreatedSyncLogPort pricePolicyCreatedSyncLogPort;

    @InjectMocks
    private SyncPricePolicyCreatedService service;

    @Test
    @DisplayName("미처리 이벤트 수신 시 멱등 로그 기록 후 상품 카탈로그 Read Model upsert 를 호출한다")
    void syncPricePolicyCreated_firstTime_persistsLogAndUpsertsCatalog() {
        UUID eventId = UUID.randomUUID();
        PricePolicyCreatedEvent event = new PricePolicyCreatedEvent(
                10L, 100L, 20L, "테스트 상품", "브랜드",
                10000L, 9000L, 100L, 0L,
                List.of(new PricePolicyCreatedEvent.ProductTagSnapshot(1L, "NEW", 1L)),
                List.of(new PricePolicyCreatedEvent.ProductOptionSnapshot(50L, "색상: 빨강"))
        );
        when(pricePolicyCreatedSyncLogPort.isProcessed(eventId)).thenReturn(false);

        service.syncPricePolicyCreated(eventId, event);

        verify(pricePolicyCreatedSyncLogPort).markProcessed(eventId);

        ArgumentCaptor<String> tagsCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> optionsCaptor = ArgumentCaptor.forClass(String.class);
        verify(saveProductCatalogReadModelPort).upsertCatalog(
                eq(100L), eq(10L), eq(20L), eq(null),
                eq("테스트 상품"), eq("브랜드"),
                eq(10000L), eq(9000L), eq(BigDecimal.ZERO),
                eq(100L), eq(BigDecimal.ZERO),
                eq(0L), eq(0L),
                eq(0L),
                tagsCaptor.capture(), optionsCaptor.capture()
        );
        assertThat(tagsCaptor.getValue()).contains("\"NEW\"");
        assertThat(optionsCaptor.getValue()).contains("색상: 빨강");
    }

    @Test
    @DisplayName("이미 처리된 이벤트 재수신 시 Read Model 을 건드리지 않는다")
    void syncPricePolicyCreated_alreadyProcessed_skipsWork() {
        UUID eventId = UUID.randomUUID();
        PricePolicyCreatedEvent event = new PricePolicyCreatedEvent(
                20L, 200L, 30L, "상품", "브랜드",
                5000L, 4500L, 50L, 0L, List.of(), List.of()
        );
        when(pricePolicyCreatedSyncLogPort.isProcessed(eventId)).thenReturn(true);

        service.syncPricePolicyCreated(eventId, event);

        verify(pricePolicyCreatedSyncLogPort).isProcessed(eventId);
        verifyNoInteractions(saveProductCatalogReadModelPort);
    }

    @Test
    @DisplayName("멱등 로그 insert 동시 충돌 시 Read Model 업데이트 없이 종료한다")
    void syncPricePolicyCreated_logConflict_returnsWithoutUpdate() {
        UUID eventId = UUID.randomUUID();
        PricePolicyCreatedEvent event = new PricePolicyCreatedEvent(
                30L, 300L, 40L, "상품", "브랜드",
                3000L, 2500L, 25L, 0L, List.of(), List.of()
        );
        when(pricePolicyCreatedSyncLogPort.isProcessed(eventId)).thenReturn(false);
        doThrow(new DataIntegrityViolationException("duplicate"))
                .when(pricePolicyCreatedSyncLogPort).markProcessed(eventId);

        service.syncPricePolicyCreated(eventId, event);

        verify(pricePolicyCreatedSyncLogPort).markProcessed(eventId);
        verifyNoInteractions(saveProductCatalogReadModelPort);
    }

    @Test
    @DisplayName("태그/옵션 목록이 비어있으면 JSON 컬럼을 null 로 전달한다")
    void syncPricePolicyCreated_emptyTagsAndOptions_writesNull() {
        UUID eventId = UUID.randomUUID();
        PricePolicyCreatedEvent event = new PricePolicyCreatedEvent(
                40L, 400L, 50L, "상품", "브랜드",
                1000L, 900L, 10L, 0L, List.of(), List.of()
        );
        when(pricePolicyCreatedSyncLogPort.isProcessed(eventId)).thenReturn(false);

        service.syncPricePolicyCreated(eventId, event);

        verify(saveProductCatalogReadModelPort).upsertCatalog(
                eq(400L), eq(40L), eq(50L), eq(null),
                eq("상품"), eq("브랜드"),
                eq(1000L), eq(900L), eq(BigDecimal.ZERO),
                eq(10L), eq(BigDecimal.ZERO),
                eq(0L), eq(0L),
                eq(0L),
                eq(null), eq(null)
        );
    }

    @Test
    @DisplayName("직렬화 실패 시 예외를 전파하지 않고 JSON 을 null 로 전달한다")
    void syncPricePolicyCreated_serializationFails_writesNull() throws Exception {
        UUID eventId = UUID.randomUUID();
        PricePolicyCreatedEvent event = new PricePolicyCreatedEvent(
                50L, 500L, 60L, "상품", "브랜드",
                2000L, 1800L, 20L, 0L,
                List.of(new PricePolicyCreatedEvent.ProductTagSnapshot(1L, "SALE", 1L)),
                List.of(new PricePolicyCreatedEvent.ProductOptionSnapshot(10L, "사이즈: L"))
        );
        when(pricePolicyCreatedSyncLogPort.isProcessed(eventId)).thenReturn(false);
        doThrow(new JsonProcessingException("fail") {
        }).when(objectMapper).writeValueAsString(any());

        service.syncPricePolicyCreated(eventId, event);

        verify(saveProductCatalogReadModelPort).upsertCatalog(
                eq(500L), eq(50L), eq(60L), eq(null),
                eq("상품"), eq("브랜드"),
                eq(2000L), eq(1800L), eq(BigDecimal.ZERO),
                eq(20L), eq(BigDecimal.ZERO),
                eq(0L), eq(0L),
                eq(0L),
                eq(null), eq(null)
        );
    }

    @Test
    @DisplayName("price/discountPrice/accumulatedPoint/sales 가 null 이면 0 으로 대체되어 upsert 된다")
    void syncPricePolicyCreated_nullNumerics_defaultsToZero() {
        UUID eventId = UUID.randomUUID();
        PricePolicyCreatedEvent event = new PricePolicyCreatedEvent(
                60L, 600L, 70L, "상품", "브랜드",
                null, null, null, null, List.of(), List.of()
        );
        when(pricePolicyCreatedSyncLogPort.isProcessed(eventId)).thenReturn(false);

        service.syncPricePolicyCreated(eventId, event);

        verify(saveProductCatalogReadModelPort).upsertCatalog(
                eq(600L), eq(60L), eq(70L), eq(null),
                eq("상품"), eq("브랜드"),
                eq(0L), eq(0L), eq(BigDecimal.ZERO),
                eq(0L), eq(BigDecimal.ZERO),
                eq(0L), eq(0L),
                eq(0L),
                eq(null), eq(null)
        );
    }
}
