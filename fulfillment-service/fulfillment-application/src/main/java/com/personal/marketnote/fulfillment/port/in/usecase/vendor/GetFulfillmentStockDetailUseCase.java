package com.personal.marketnote.fulfillment.port.in.usecase.vendor;

import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentStockDetailCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentStocksResult;

/**
 * 파스토 재고 상세 조회 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-02-05
 * @Description 파스토 재고 상세 조회 기능을 제공합니다.
 */
public interface GetFulfillmentStockDetailUseCase {
    /**
     * @param command 단일 상품 재고 정보 조회 커맨드
     * @return 단일 상품 재고 정보 조회 결과 {@link GetFulfillmentStocksResult}
     * @Date 2026-02-05
     * @Author 성효빈
     * @Description 파스토 단일 상품 재고 정보를 조회합니다.
     */
    GetFulfillmentStocksResult getStockDetail(GetFulfillmentStockDetailCommand command);
}
