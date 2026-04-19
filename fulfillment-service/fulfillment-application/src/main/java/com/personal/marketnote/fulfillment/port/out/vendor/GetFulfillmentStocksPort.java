package com.personal.marketnote.fulfillment.port.out.vendor;

import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentStocksCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentStocksResult;

/**
 * 풀필먼트 재고 목록 조회 포트
 *
 * @Author 성효빈
 * @Date 2026-02-03
 * @Description 풀필먼트 재고 목록 조회 기능을 제공합니다.
 */
public interface GetFulfillmentStocksPort {

    /**
     * @param command 재고 목록 조회 커맨드
     * @return 재고 목록 조회 결과 {@link GetFulfillmentStocksResult}
     * @Date 2026-02-03
     * @Author 성효빈
     * @Description 풀필먼트 재고 목록을 조회합니다.
     */
    GetFulfillmentStocksResult getStocks(GetFulfillmentStocksCommand command);
}
