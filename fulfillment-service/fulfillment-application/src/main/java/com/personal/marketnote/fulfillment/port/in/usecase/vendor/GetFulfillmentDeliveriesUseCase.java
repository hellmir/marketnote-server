package com.personal.marketnote.fulfillment.port.in.usecase.vendor;

import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentDeliveriesCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentDeliveriesResult;

/**
 * 풀필먼트 출고 목록 조회 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-02-11
 * @Description 풀필먼트 출고 목록 조회 기능을 제공합니다.
 */
public interface GetFulfillmentDeliveriesUseCase {
    GetFulfillmentDeliveriesResult getDeliveries(GetFulfillmentDeliveriesCommand command);
}
