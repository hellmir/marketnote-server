package com.personal.marketnote.fulfillment.port.out.vendor;

import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentDeliveryOutOrdGoodsByOrdNoCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentDeliveryOutOrdGoodsByOrdNoResult;

/**
 * 풀필먼트 출고 주문번호별 상품 조회 포트
 *
 * @Author 성효빈
 * @Date 2026-02-17
 * @Description 풀필먼트 출고 주문번호별 상품 조회 기능을 제공합니다.
 */
public interface GetFulfillmentDeliveryOutOrdGoodsByOrdNoPort {

    /**
     * @param command 풀필먼트 출고 주문번호별 상품 조회 커맨드
     * @return 풀필먼트 출고 주문번호별 상품 조회 결과 {@link GetFulfillmentDeliveryOutOrdGoodsByOrdNoResult}
     * @Date 2026-02-17
     * @Author 성효빈
     * @Description 풀필먼트 출고 주문번호별 상품을 조회합니다.
     */
    GetFulfillmentDeliveryOutOrdGoodsByOrdNoResult getOutOrdGoodsByOrdNo(GetFulfillmentDeliveryOutOrdGoodsByOrdNoCommand command);
}
