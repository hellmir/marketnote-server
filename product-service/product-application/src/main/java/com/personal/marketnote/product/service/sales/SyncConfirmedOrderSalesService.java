package com.personal.marketnote.product.service.sales;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.common.kafka.event.OrderPurchaseConfirmedEvent;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.product.port.in.usecase.sales.SyncConfirmedOrderSalesUseCase;
import com.personal.marketnote.product.port.out.productcatalog.SaveProductCatalogReadModelPort;
import com.personal.marketnote.product.port.out.sales.OrderSalesSyncLogPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@Slf4j
@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED)
public class SyncConfirmedOrderSalesService implements SyncConfirmedOrderSalesUseCase {

    private static final int MAX_ITEMS_PER_ORDER = 1000;

    private final SaveProductCatalogReadModelPort saveProductCatalogReadModelPort;
    private final OrderSalesSyncLogPort orderSalesSyncLogPort;

    @Override
    public void syncSales(OrderPurchaseConfirmedEvent event) {
        Long orderId = event.orderId();
        List<OrderPurchaseConfirmedEvent.OrderProductItem> items = event.orderProducts();
        if (FormatValidator.hasNoValue(items) || items.isEmpty()) {
            log.info("구매 확정 이벤트에 주문 상품 없음. 판매량 동기화 건너뜀. orderId={}", orderId);
            return;
        }
        if (items.size() > MAX_ITEMS_PER_ORDER) {
            log.warn("구매 확정 이벤트 주문 상품 수 상한 초과 — 판매량 동기화 건너뜀. orderId={}, size={}", orderId, items.size());
            return;
        }

        if (orderSalesSyncLogPort.isProcessed(orderId)) {
            log.info("구매 확정 판매량 동기화 중복 수신 (멱등 처리). orderId={}", orderId);
            return;
        }

        try {
            orderSalesSyncLogPort.markProcessed(orderId);
        } catch (DataIntegrityViolationException dive) {
            log.info("구매 확정 판매량 동기화 멱등 로그 충돌 (동시 처리). orderId={}", orderId);
            return;
        }

        int synced = 0;
        for (OrderPurchaseConfirmedEvent.OrderProductItem item : items) {
            if (isInvalidItem(item)) {
                continue;
            }
            saveProductCatalogReadModelPort.increaseSales(item.pricePolicyId(), item.quantity().longValue());
            synced++;
        }
        log.info("구매 확정 판매량 동기화 완료. orderId={}, items={}", orderId, synced);
    }

    private boolean isInvalidItem(OrderPurchaseConfirmedEvent.OrderProductItem item) {
        if (FormatValidator.hasNoValue(item.pricePolicyId()) || item.pricePolicyId() <= 0L) {
            return true;
        }
        return FormatValidator.hasNoValue(item.quantity()) || item.quantity() <= 0;
    }
}
