package com.personal.marketnote.product.port.out.fulfillment;

/**
 * 풀필먼트 벤더 상품 수정 포트
 *
 * @Author 성효빈
 * @Date 2026-02-03
 * @Description 풀필먼트 벤더 상품 수정 기능을 제공합니다.
 */
public interface UpdateFulfillmentVendorGoodsPort {
    /**
     * @param command 풀필먼트 벤더 상품 수정 커맨드
     * @Date 2026-02-03
     * @Author 성효빈
     * @Description 풀필먼트 벤더 상품을 수정합니다.
     */
    void updateFulfillmentVendorGoods(UpdateFulfillmentVendorGoodsCommand command);
}
