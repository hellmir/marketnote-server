package com.personal.marketnote.product.port.in.usecase.product;

import com.personal.marketnote.product.port.in.command.GetMyOrderingProductsQuery;
import com.personal.marketnote.product.port.in.result.product.GetMyOrderProductsResult;

/**
 * 나의 주문 상품 조회 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-01-05
 * @Description 나의 주문 상품 조회 기능을 제공합니다.
 */
public interface GetMyOrderingProductsUseCase {
    /**
     * @param getMyOrderingProductsQuery 주문 대기 상품 목록 조회 쿼리
     * @return 주문 대기 상품 목록 조회 결과 {@link GetMyOrderProductsResult}
     * @Date 2026-01-16
     * @Author 성효빈
     * @Description 주문 대기 상품 목록을 조회합니다.
     */
    GetMyOrderProductsResult getMyOrderingProducts(GetMyOrderingProductsQuery getMyOrderingProductsQuery);
}
