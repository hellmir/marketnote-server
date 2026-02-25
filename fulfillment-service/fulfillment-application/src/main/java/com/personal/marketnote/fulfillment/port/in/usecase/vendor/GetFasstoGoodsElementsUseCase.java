package com.personal.marketnote.fulfillment.port.in.usecase.vendor;

import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFasstoGoodsElementsCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFasstoGoodsElementsResult;

/**
 * 파스토 상품 구성 요소 조회 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-01-31
 * @Description 파스토 상품 구성 요소 조회 기능을 제공합니다.
 */
public interface GetFasstoGoodsElementsUseCase {
    /**
     * @param command 모음상품 상세 정보 조회 커맨드
     * @return 모음상품 상세 정보 조회 결과 {@link GetFasstoGoodsElementsResult}
     * @Date 2026-01-31
     * @Author 성효빈
     * @Description 파스토 모음상품 상세 정보를 조회합니다.
     */
    GetFasstoGoodsElementsResult getGoodsElements(GetFasstoGoodsElementsCommand command);
}
