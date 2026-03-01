package com.personal.marketnote.commerce.domain.order;

public class InvalidOrderProductStatusTransitionException extends IllegalStateException {
    public InvalidOrderProductStatusTransitionException(OrderStatus from, OrderStatus to) {
        super(String.format("주문 상품 상태를 %s에서 %s(으)로 변경할 수 없습니다.",
                from.getDescription(), to.getDescription()));
    }
}
