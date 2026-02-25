package com.personal.marketnote.commerce.port.out.order;

import com.personal.marketnote.commerce.domain.order.OrderProduct;
import com.personal.marketnote.commerce.exception.OrderProductNotFoundException;

public interface UpdateOrderProductPort {
    /**
     * @param orderProduct 주문 상품 도메인
     * @throws OrderProductNotFoundException 주문 상품을 찾을 수 없는 경우
     * @Date 2026-01-12
     * @Author 성효빈
     * @Description 주문 상품 정보를 업데이트합니다.
     */
    void update(OrderProduct orderProduct) throws OrderProductNotFoundException;
}
