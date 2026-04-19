package com.personal.marketnote.fulfillment.port.in.usecase.vendor;

import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentDeliveryGoodDetailCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentDeliveryGoodDetailResult;

/**
 * 풀필먼트 출고 상품 상세 조회 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-02-18
 * @Description 풀필먼트 출고 상품 상세 조회 기능을 제공합니다.
 */
public interface GetFulfillmentDeliveryGoodDetailUseCase {
    GetFulfillmentDeliveryGoodDetailResult getDeliveryGoodDetail(GetFulfillmentDeliveryGoodDetailCommand command);
}
