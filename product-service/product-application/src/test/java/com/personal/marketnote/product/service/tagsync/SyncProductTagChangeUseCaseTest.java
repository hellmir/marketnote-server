package com.personal.marketnote.product.service.tagsync;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.kafka.event.ProductTagChangedEvent;
import com.personal.marketnote.product.port.out.productcatalog.SaveProductCatalogReadModelPort;
import com.personal.marketnote.product.port.out.tagsync.ProductTagChangeSyncLogPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

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
class SyncProductTagChangeUseCaseTest {

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private SaveProductCatalogReadModelPort saveProductCatalogReadModelPort;

    @Mock
    private ProductTagChangeSyncLogPort productTagChangeSyncLogPort;

    @InjectMocks
    private SyncProductTagChangeService service;

    @Test
    @DisplayName("미처리 이벤트를 수신하면 멱등 로그 기록 후 태그 JSON 을 Read Model 에 반영한다")
    void syncProductTagChange_firstTime_persistsLogAndUpdatesReadModel() {
        UUID eventId = UUID.randomUUID();
        ProductTagChangedEvent event = new ProductTagChangedEvent(
                10L,
                List.of(
                        new ProductTagChangedEvent.ProductTagSnapshot(1L, "봄", 1L),
                        new ProductTagChangedEvent.ProductTagSnapshot(2L, "세일", 2L)
                ),
                ProductTagChangedEvent.Action.UPDATED
        );
        when(productTagChangeSyncLogPort.isProcessed(eventId)).thenReturn(false);

        service.syncProductTagChange(eventId, event);

        verify(productTagChangeSyncLogPort).markProcessed(eventId);

        ArgumentCaptor<String> jsonCaptor = ArgumentCaptor.forClass(String.class);
        verify(saveProductCatalogReadModelPort).updateProductTags(eq(10L), jsonCaptor.capture());
        assertThat(jsonCaptor.getValue()).contains("\"봄\"").contains("\"세일\"");
    }

    @Test
    @DisplayName("이미 처리된 이벤트를 재수신하면 Read Model 을 건드리지 않는다")
    void syncProductTagChange_alreadyProcessed_skipsWork() {
        UUID eventId = UUID.randomUUID();
        ProductTagChangedEvent event = new ProductTagChangedEvent(
                20L,
                List.of(new ProductTagChangedEvent.ProductTagSnapshot(3L, "겨울", 1L)),
                ProductTagChangedEvent.Action.REORDERED
        );
        when(productTagChangeSyncLogPort.isProcessed(eventId)).thenReturn(true);

        service.syncProductTagChange(eventId, event);

        verify(productTagChangeSyncLogPort).isProcessed(eventId);
        verifyNoInteractions(saveProductCatalogReadModelPort);
    }

    @Test
    @DisplayName("멱등 로그 insert 가 동시 처리로 충돌하면 Read Model 업데이트 없이 종료한다")
    void syncProductTagChange_logConflict_returnsWithoutUpdate() {
        UUID eventId = UUID.randomUUID();
        ProductTagChangedEvent event = new ProductTagChangedEvent(
                30L,
                List.of(new ProductTagChangedEvent.ProductTagSnapshot(4L, "한정", 1L)),
                ProductTagChangedEvent.Action.UPDATED
        );
        when(productTagChangeSyncLogPort.isProcessed(eventId)).thenReturn(false);
        doThrow(new DataIntegrityViolationException("duplicate"))
                .when(productTagChangeSyncLogPort).markProcessed(eventId);

        service.syncProductTagChange(eventId, event);

        verify(productTagChangeSyncLogPort).markProcessed(eventId);
        verifyNoInteractions(saveProductCatalogReadModelPort);
    }

    @Test
    @DisplayName("태그 목록이 비어있으면 productTags 컬럼을 null 로 업데이트한다")
    void syncProductTagChange_emptyTags_writesNull() {
        UUID eventId = UUID.randomUUID();
        ProductTagChangedEvent event = new ProductTagChangedEvent(
                40L, List.of(), ProductTagChangedEvent.Action.UPDATED
        );
        when(productTagChangeSyncLogPort.isProcessed(eventId)).thenReturn(false);

        service.syncProductTagChange(eventId, event);

        verify(saveProductCatalogReadModelPort).updateProductTags(eq(40L), eq(null));
    }

    @Test
    @DisplayName("직렬화 실패 시 예외를 전파하지 않고 null 로 업데이트한다")
    void syncProductTagChange_serializationFails_writesNull() throws Exception {
        UUID eventId = UUID.randomUUID();
        ProductTagChangedEvent event = new ProductTagChangedEvent(
                50L,
                List.of(new ProductTagChangedEvent.ProductTagSnapshot(5L, "가을", 1L)),
                ProductTagChangedEvent.Action.UPDATED
        );
        when(productTagChangeSyncLogPort.isProcessed(eventId)).thenReturn(false);
        doThrow(new JsonProcessingException("failed") {
        }).when(objectMapper).writeValueAsString(any());

        service.syncProductTagChange(eventId, event);

        verify(saveProductCatalogReadModelPort).updateProductTags(eq(50L), eq(null));
    }
}
