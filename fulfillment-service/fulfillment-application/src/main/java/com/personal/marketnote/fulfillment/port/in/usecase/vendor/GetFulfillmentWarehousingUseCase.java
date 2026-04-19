package com.personal.marketnote.fulfillment.port.in.usecase.vendor;

import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentWarehousingCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentWarehousingResult;

/**
 * 풀필먼트 입고 목록 조회 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-02-03
 * @Description 풀필먼트 입고 목록 조회 기능을 제공합니다.
 */
public interface GetFulfillmentWarehousingUseCase {
    /**
     * @param command 상품 입고 목록 조회 커맨드
     * @return 상품 입고 목록 조회 결과 {@link GetFulfillmentWarehousingResult}
     * @Date 2026-02-03
     * @Author 성효빈
     * @Description 풀필먼트 상품 입고 목록을 조회합니다.
     */
    GetFulfillmentWarehousingResult getWarehousing(GetFulfillmentWarehousingCommand command);
}
