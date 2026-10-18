package com.personal.marketnote.product.service.sales;

import com.personal.marketnote.common.kafka.event.OrderPurchaseConfirmedEvent;
import com.personal.marketnote.product.port.out.productcatalog.SaveProductCatalogReadModelPort;
import com.personal.marketnote.product.port.out.sales.OrderSalesSyncLogPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SyncConfirmedOrderSalesUseCaseTest {

    @InjectMocks
    private SyncConfirmedOrderSalesService service;

    @Mock
    private SaveProductCatalogReadModelPort saveProductCatalogReadModelPort;

    @Mock
    private OrderSalesSyncLogPort orderSalesSyncLogPort;

    @Test
    @DisplayName("주문 상품 목록이 없으면 판매량 동기화를 건너뛴다")
    void skipsWhenOrderProductsEmpty() {
        OrderPurchaseConfirmedEvent event = new OrderPurchaseConfirmedEvent(
                1L, 10L, List.of(), false, List.of()
        );

        service.syncSales(event);

        verifyNoInteractions(orderSalesSyncLogPort, saveProductCatalogReadModelPort);
    }

    @Test
    @DisplayName("이미 처리된 주문이면 판매량 증가를 건너뛴다")
    void skipsWhenAlreadyProcessed() {
        OrderPurchaseConfirmedEvent event = buildEventWithSingleItem(100L, 1L, 3);
        when(orderSalesSyncLogPort.isProcessed(100L)).thenReturn(true);

        service.syncSales(event);

        verify(orderSalesSyncLogPort).isProcessed(100L);
        verifyNoInteractions(saveProductCatalogReadModelPort);
        verifyNoMoreInteractions(orderSalesSyncLogPort);
    }

    @Test
    @DisplayName("멱등 로그 저장 시 유니크 충돌이 발생하면 판매량 증가를 건너뛴다")
    void skipsWhenLogInsertViolatesUnique() {
        OrderPurchaseConfirmedEvent event = buildEventWithSingleItem(100L, 1L, 3);
        when(orderSalesSyncLogPort.isProcessed(100L)).thenReturn(false);
        doThrow(new DataIntegrityViolationException("duplicate"))
                .when(orderSalesSyncLogPort).markProcessed(100L);

        service.syncSales(event);

        verify(orderSalesSyncLogPort).markProcessed(100L);
        verifyNoInteractions(saveProductCatalogReadModelPort);
    }

    @Test
    @DisplayName("신규 주문이면 각 주문 상품의 pricePolicyId 에 quantity 만큼 판매량을 증가시킨다")
    void increasesSalesForEachItem() {
        OrderPurchaseConfirmedEvent event = new OrderPurchaseConfirmedEvent(
                100L, 10L, List.of(), false,
                List.of(
                        new OrderPurchaseConfirmedEvent.OrderProductItem(1L, 11L, 3),
                        new OrderPurchaseConfirmedEvent.OrderProductItem(2L, 12L, 5)
                )
        );
        when(orderSalesSyncLogPort.isProcessed(100L)).thenReturn(false);

        service.syncSales(event);

        verify(orderSalesSyncLogPort).markProcessed(100L);
        verify(saveProductCatalogReadModelPort).increaseSales(1L, 3L);
        verify(saveProductCatalogReadModelPort).increaseSales(2L, 5L);
        verifyNoMoreInteractions(saveProductCatalogReadModelPort);
    }

    @Test
    @DisplayName("pricePolicyId 또는 quantity 가 null 인 항목은 건너뛴다")
    void skipsItemsWithMissingFields() {
        OrderPurchaseConfirmedEvent event = new OrderPurchaseConfirmedEvent(
                100L, 10L, List.of(), false,
                List.of(
                        new OrderPurchaseConfirmedEvent.OrderProductItem(null, 11L, 3),
                        new OrderPurchaseConfirmedEvent.OrderProductItem(2L, 12L, null),
                        new OrderPurchaseConfirmedEvent.OrderProductItem(3L, 13L, 7)
                )
        );
        when(orderSalesSyncLogPort.isProcessed(100L)).thenReturn(false);

        service.syncSales(event);

        verify(saveProductCatalogReadModelPort).increaseSales(3L, 7L);
        verifyNoMoreInteractions(saveProductCatalogReadModelPort);
    }

    private OrderPurchaseConfirmedEvent buildEventWithSingleItem(Long orderId, Long pricePolicyId, int quantity) {
        return new OrderPurchaseConfirmedEvent(
                orderId, 10L, List.of(), false,
                List.of(new OrderPurchaseConfirmedEvent.OrderProductItem(pricePolicyId, 99L, quantity))
        );
    }
}
