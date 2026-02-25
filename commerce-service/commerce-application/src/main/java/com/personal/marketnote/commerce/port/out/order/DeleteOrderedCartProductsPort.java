package com.personal.marketnote.commerce.port.out.order;

import java.util.List;

public interface DeleteOrderedCartProductsPort {
    /**
     * @param pricePolicyIds 가격 정책 ID 목록
     * @Date 2026-01-06
     * @Author 성효빈
     * @Description 주문 완료된 장바구니 상품을 삭제합니다.
     */
    void delete(List<Long> pricePolicyIds);
}
