package com.personal.marketnote.fulfillment.port.out.vendor;

import com.personal.marketnote.fulfillment.domain.vendor.delivery.FulfillmentDeliveryStatusQuery;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentDeliveryStatusesResult;

/**
 * 파스토 출고 상태 목록 조회 포트
 *
 * @Author 성효빈
 * @Date 2026-02-13
 * @Description 파스토 출고 상태 목록 조회 기능을 제공합니다.
 */
public interface GetFulfillmentDeliveryStatusesPort {

    /**
     * @param query 파스토 출고 상태 목록 조회 쿼리
     * @return 파스토 출고 상태 목록 조회 결과 {@link GetFulfillmentDeliveryStatusesResult}
     * @Date 2026-02-13
     * @Author 성효빈
     * @Description 파스토 출고 상태 목록을 조회합니다.
     */
    GetFulfillmentDeliveryStatusesResult getDeliveryStatuses(FulfillmentDeliveryStatusQuery query);
}
