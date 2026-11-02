package com.personal.marketnote.commerce.port.out.order;

import com.personal.marketnote.commerce.domain.order.Order;

/**
 * 주문 저장 포트
 *
 * @Author 성효빈
 * @Date 2026-01-05
 * @Description 주문 저장 기능을 제공합니다.
 */
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
