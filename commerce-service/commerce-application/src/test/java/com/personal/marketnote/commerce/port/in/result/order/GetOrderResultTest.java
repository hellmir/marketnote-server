package com.personal.marketnote.commerce.port.in.result.order;

import com.personal.marketnote.commerce.domain.order.Order;
import com.personal.marketnote.commerce.domain.order.OrderAmount;
import com.personal.marketnote.commerce.domain.order.OrderNumber;
import com.personal.marketnote.commerce.domain.order.OrderProductSnapshotState;
import com.personal.marketnote.commerce.domain.order.OrderSnapshotState;
import com.personal.marketnote.commerce.domain.order.OrderStatus;
import com.personal.marketnote.commerce.domain.order.ShippingAddress;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class GetOrderResultTest {

    @Test
    @DisplayName("주문 정보 조회 결과의 주문 상품 목록에 orderProductKey가 노출된다")
    void shouldExposeOrderProductKeyInOrderProducts() {
        // given
        UUID firstKey = UUID.fromString("018f0000-0000-7000-8000-000000000011");
        UUID secondKey = UUID.fromString("018f0000-0000-7000-8000-000000000012");
        OrderProductSnapshotState first = OrderProductSnapshotState.builder()
                .orderId(1L)
                .sellerId(10L)
                .pricePolicyId(100L)
                .orderProductKey(firstKey)
                .quantity(1)
                .unitAmount(50000L)
                .accumulatedPoint(0L)
                .orderStatus(OrderStatus.PAID)
                .build();
        OrderProductSnapshotState second = OrderProductSnapshotState.builder()
                .orderId(1L)
                .sellerId(10L)
                .pricePolicyId(200L)
                .orderProductKey(secondKey)
                .quantity(2)
                .unitAmount(30000L)
                .accumulatedPoint(0L)
                .orderStatus(OrderStatus.PAID)
                .build();

        Order order = Order.from(OrderSnapshotState.builder()
                .id(1L)
                .buyerId(100L)
                .orderKey(UUID.randomUUID())
                .orderNumber(OrderNumber.of("ORD-1"))
                .orderStatus(OrderStatus.PAID)
                .amount(OrderAmount.of(110000L, null, 0L, 0L, null))
                .shippingAddress(ShippingAddress.of("수령인", "010-1234-5678", "12345", "서울시 강남구", "상세주소", null, null))
                .orderProductStates(List.of(first, second))
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build());

        // when
        GetOrderResult result = GetOrderResult.from(order, Map.of());

        // then
        assertThat(result.orderProducts()).hasSize(2);
        assertThat(result.orderProducts().get(0).orderProductKey()).isEqualTo(firstKey);
        assertThat(result.orderProducts().get(1).orderProductKey()).isEqualTo(secondKey);
    }
}
