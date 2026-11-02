package com.personal.marketnote.product.service.tagsync;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.common.kafka.event.ProductTagChangedEvent;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.product.port.in.usecase.tagsync.SyncProductTagChangeUseCase;
import com.personal.marketnote.product.port.out.productcatalog.SaveProductCatalogReadModelPort;
import com.personal.marketnote.product.port.out.tagsync.ProductTagChangeSyncLogPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@Slf4j
@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED)
public class SyncProductTagChangeService implements SyncProductTagChangeUseCase {

    private final ObjectMapper objectMapper;
    private final SaveProductCatalogReadModelPort saveProductCatalogReadModelPort;
    private final ProductTagChangeSyncLogPort productTagChangeSyncLogPort;

    @Override
    public void syncProductTagChange(UUID eventId, ProductTagChangedEvent event) {
        Long productId = event.productId();

        if (productTagChangeSyncLogPort.isProcessed(eventId)) {
            log.info("상품 태그 변경 동기화 중복 수신 (멱등 처리). eventId={}, productId={}", eventId, productId);
            return;
        }

        try {
            productTagChangeSyncLogPort.markProcessed(eventId);
        } catch (DataIntegrityViolationException dive) {
            log.info("상품 태그 변경 동기화 멱등 로그 충돌 (동시 처리). eventId={}, productId={}", eventId, productId);
            return;
        }

        String productTagsJson = serializeProductTags(event.productTags(), productId);
        saveProductCatalogReadModelPort.updateProductTags(productId, productTagsJson);
        log.info("상품 카탈로그 Read Model 태그 동기화 완료. productId={}, action={}", productId, event.action());
    }

    private String serializeProductTags(List<ProductTagChangedEvent.ProductTagSnapshot> tags, Long productId) {
        if (FormatValidator.hasNoValue(tags)) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(tags);
        } catch (JsonProcessingException jpe) {
            log.warn("상품 태그 변경 이벤트 직렬화 실패. productId={}", productId, jpe);
            return null;
        }
    }
}
