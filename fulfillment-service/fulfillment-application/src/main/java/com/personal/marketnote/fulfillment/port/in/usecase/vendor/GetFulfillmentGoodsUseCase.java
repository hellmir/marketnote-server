package com.personal.marketnote.fulfillment.port.in.usecase.vendor;

import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentGoodsCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentGoodsResult;

/**
 * 파스토 상품 목록 조회 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-01-30
 * @Description 파스토 상품 목록 조회 기능을 제공합니다.
 */
public interface GetFulfillmentGoodsUseCase {
    /**
     * @param command 상품 목록 조회 커맨드
     * @return 상품 목록 조회 결과 {@link GetFulfillmentGoodsResult}
     * @Date 2026-01-30
     * @Author 성효빈
     * @Description 파스토 상품 목록을 조회합니다.
     */
    GetFulfillmentGoodsResult getGoods(GetFulfillmentGoodsCommand command);
}
