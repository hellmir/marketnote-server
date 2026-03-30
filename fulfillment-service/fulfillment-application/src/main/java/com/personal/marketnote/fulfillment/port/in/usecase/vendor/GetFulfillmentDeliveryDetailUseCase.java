package com.personal.marketnote.fulfillment.port.in.usecase.vendor;

import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentDeliveryDetailCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentDeliveryDetailResult;

/**
 * 파스토 출고 상세 조회 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-02-12
 * @Description 파스토 출고 상세 조회 기능을 제공합니다.
 */
public interface GetFulfillmentDeliveryDetailUseCase {
    GetFulfillmentDeliveryDetailResult getDeliveryDetail(GetFulfillmentDeliveryDetailCommand command);
}
