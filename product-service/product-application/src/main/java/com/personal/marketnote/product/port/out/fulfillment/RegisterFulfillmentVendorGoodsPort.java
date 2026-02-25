package com.personal.marketnote.product.port.out.fulfillment;

/**
 * 풀필먼트 벤더 상품 등록 포트
 *
 * @Author 성효빈
 * @Date 2026-01-29
 * @Description 풀필먼트 벤더 상품 등록 기능을 제공합니다.
 */
public interface RegisterFulfillmentVendorGoodsPort {
    /**
     * @param command 풀필먼트 벤더 상품 등록 커맨드
     * @Date 2026-01-29
     * @Author 성효빈
     * @Description 풀필먼트 벤더 상품을 등록합니다.
     */
    void registerFulfillmentVendorGoods(RegisterFulfillmentVendorGoodsCommand command);
}
