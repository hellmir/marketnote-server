package com.personal.marketnote.product.port.in.usecase.product;

import com.personal.marketnote.product.port.in.result.product.GetProductKeyResult;

/**
 * 상품 productKey 조회 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-09-20
 * @Description 상품의 productKey를 조회합니다. 관리자 또는 해당 상품 판매자만 조회할 수 있습니다.
 */
public interface GetProductKeyUseCase {
    /**
     * @param id      상품 ID
     * @param userId  요청자 회원 ID
     * @param isAdmin 요청자가 관리자 권한을 가졌는지 여부
     * @return productKey 조회 결과 {@link GetProductKeyResult}
     * @Date 2026-09-20
     * @Author 성효빈
     * @Description 상품 productKey를 조회합니다. 관리자 또는 해당 상품 판매자만 조회 가능합니다.
     */
    GetProductKeyResult getProductKey(Long id, Long userId, boolean isAdmin);
}
