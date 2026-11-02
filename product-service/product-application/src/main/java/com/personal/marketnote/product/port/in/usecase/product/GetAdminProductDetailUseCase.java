package com.personal.marketnote.product.port.in.usecase.product;

import com.personal.marketnote.product.port.in.result.product.GetAdminProductDetailResult;

import java.util.List;

/**
 * 관리자 상품 상세 조회 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-02-03
 * @Description 관리자 상품 상세 조회 기능을 제공합니다.
 */
public interface GetAdminProductDetailUseCase {
    /**
     * @param id                상품 ID
     * @param selectedOptionIds 선택된 옵션 ID 목록
     * @return 관리자 상품 상세 정보 조회 결과 {@link GetAdminProductDetailResult}
     * @Date 2026-02-03
     * @Author 성효빈
     * @Description 관리자 상품 상세 정보를 조회합니다. 파스토 상품/모음상품 정보를 함께 반환합니다.
     */
    GetAdminProductDetailResult getAdminProductDetail(Long id, List<Long> selectedOptionIds);
}
