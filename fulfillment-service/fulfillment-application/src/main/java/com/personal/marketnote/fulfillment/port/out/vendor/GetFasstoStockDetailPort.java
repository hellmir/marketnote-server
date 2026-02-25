package com.personal.marketnote.fulfillment.port.out.vendor;

import com.personal.marketnote.fulfillment.domain.vendor.fassto.stock.FasstoStockDetailQuery;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFasstoStocksResult;

/**
 * 파스토 재고 상세 조회 포트
 *
 * @Author 성효빈
 * @Date 2026-02-03
 * @Description 파스토 재고 상세 조회 기능을 제공합니다.
 */
public interface GetFasstoStockDetailPort {

    /**
     * @param query 파스토 재고 상세 조회 쿼리
     * @return 파스토 재고 상세 조회 결과 {@link GetFasstoStocksResult}
     * @Date 2026-02-03
     * @Author 성효빈
     * @Description 파스토 재고 상세 정보를 조회합니다.
     */
    GetFasstoStocksResult getStockDetail(FasstoStockDetailQuery query);
}
