package com.personal.marketnote.fulfillment.port.in.usecase.vendor;

import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentWarehousingInspecDetailCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentWarehousingInspecDetailResult;

/**
 * 풀필먼트 입고 검수 상세 조회 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-02-17
 * @Description 풀필먼트 입고 검수 상세 조회 기능을 제공합니다.
 */
public interface GetFulfillmentWarehousingInspecDetailUseCase {
    GetFulfillmentWarehousingInspecDetailResult getWarehousingInspecDetail(GetFulfillmentWarehousingInspecDetailCommand command);
}
