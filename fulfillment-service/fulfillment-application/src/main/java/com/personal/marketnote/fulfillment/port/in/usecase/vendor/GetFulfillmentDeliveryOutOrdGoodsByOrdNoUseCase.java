package com.personal.marketnote.fulfillment.port.in.usecase.vendor;

import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentDeliveryOutOrdGoodsByOrdNoCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentDeliveryOutOrdGoodsByOrdNoResult;

/**
 * 풀필먼트 출고 주문번호별 상품 조회 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-02-17
 * @Description 풀필먼트 출고 주문번호별 상품 조회 기능을 제공합니다.
 */
public interface GetFulfillmentDeliveryOutOrdGoodsByOrdNoUseCase {
    GetFulfillmentDeliveryOutOrdGoodsByOrdNoResult getOutOrdGoodsByOrdNo(GetFulfillmentDeliveryOutOrdGoodsByOrdNoCommand command);
}
