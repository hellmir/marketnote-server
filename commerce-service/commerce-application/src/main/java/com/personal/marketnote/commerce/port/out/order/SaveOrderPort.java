package com.personal.marketnote.commerce.port.out.order;

import com.personal.marketnote.commerce.domain.order.Order;

public interface SaveOrderPort {
    /**
     * @param order 주문 도메인
     * @return 저장된 주문 도메인 {@link Order}
     * @Date 2026-01-06
     * @Author 성효빈
     * @Description 주문 정보를 저장합니다.
     */
    Order save(Order order);
}
