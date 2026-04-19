package com.personal.marketnote.fulfillment.port.in.usecase.vendor;

import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentDeliveryStatusesCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentDeliveryStatusesResult;

/**
 * 풀필먼트 출고 상태 목록 조회 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-02-13
 * @Description 풀필먼트 출고 상태 목록 조회 기능을 제공합니다.
 */
public interface GetFulfillmentDeliveryStatusesUseCase {
    GetFulfillmentDeliveryStatusesResult getDeliveryStatuses(GetFulfillmentDeliveryStatusesCommand command);
}
