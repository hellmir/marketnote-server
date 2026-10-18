package com.personal.marketnote.product.service.pricepolicysync;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.common.kafka.event.PricePolicyCreatedEvent;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.product.port.in.usecase.pricepolicysync.SyncPricePolicyCreatedUseCase;
import com.personal.marketnote.product.port.out.pricepolicysync.PricePolicyCreatedSyncLogPort;
import com.personal.marketnote.product.port.out.productcatalog.SaveProductCatalogReadModelPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@Slf4j
@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED)
public class SyncPricePolicyCreatedService implements SyncPricePolicyCreatedUseCase {

    private final ObjectMapper objectMapper;
    private final SaveProductCatalogReadModelPort saveProductCatalogReadModelPort;
    private final PricePolicyCreatedSyncLogPort pricePolicyCreatedSyncLogPort;

    @Override
    public void syncPricePolicyCreated(UUID eventId, PricePolicyCreatedEvent event) {
        Long pricePolicyId = event.pricePolicyId();
        Long productId = event.productId();

        if (pricePolicyCreatedSyncLogPort.isProcessed(eventId)) {
            log.info("가격 정책 생성 동기화 중복 수신 (멱등 처리). eventId={}, pricePolicyId={}", eventId, pricePolicyId);
            return;
        }

        try {
            pricePolicyCreatedSyncLogPort.markProcessed(eventId);
        } catch (DataIntegrityViolationException dive) {
            log.info("가격 정책 생성 동기화 멱등 로그 충돌 (동시 처리). eventId={}, pricePolicyId={}", eventId, pricePolicyId);
            return;
        }

        String productTagsJson = serializeProductTags(event.productTags(), pricePolicyId);
        String selectedOptionsJson = serializeSelectedOptions(event.selectedOptions(), pricePolicyId);

        saveProductCatalogReadModelPort.upsertCatalog(
                pricePolicyId,
                productId,
                event.sellerId(),
                null,
                event.productName(),
                event.brandName(),
                FormatValidator.hasValue(event.price()) ? event.price() : 0L,
                FormatValidator.hasValue(event.discountPrice()) ? event.discountPrice() : 0L,
                BigDecimal.ZERO,
                FormatValidator.hasValue(event.accumulatedPoint()) ? event.accumulatedPoint() : 0L,
                BigDecimal.ZERO,
                0L,
                0L,
                FormatValidator.hasValue(event.sales()) ? event.sales() : 0L,
                productTagsJson,
                selectedOptionsJson
        );
        log.info("상품 카탈로그 Read Model 가격 정책 생성 동기화 완료. pricePolicyId={}, productId={}",
                pricePolicyId, productId);
    }

    private String serializeProductTags(
            List<PricePolicyCreatedEvent.ProductTagSnapshot> tags, Long pricePolicyId
    ) {
        if (FormatValidator.hasNoValue(tags)) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(tags);
        } catch (JsonProcessingException jpe) {
            log.warn("가격 정책 생성 이벤트 productTags 직렬화 실패. pricePolicyId={}", pricePolicyId, jpe);
            return null;
        }
    }

    private String serializeSelectedOptions(
            List<PricePolicyCreatedEvent.ProductOptionSnapshot> options, Long pricePolicyId
    ) {
        if (FormatValidator.hasNoValue(options)) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(options);
        } catch (JsonProcessingException jpe) {
            log.warn("가격 정책 생성 이벤트 selectedOptions 직렬화 실패. pricePolicyId={}", pricePolicyId, jpe);
            return null;
        }
    }
}
