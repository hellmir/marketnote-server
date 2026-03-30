package com.personal.marketnote.fulfillment.port.out.vendor;

import com.personal.marketnote.fulfillment.domain.vendor.warehousing.FulfillmentWarehousingAbnormalImageQuery;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentWarehousingAbnormalImageResult;

/**
 * 파스토 입고 이상 이미지 조회 포트
 *
 * @Author 성효빈
 * @Date 2026-02-17
 * @Description 파스토 입고 이상 이미지 조회 기능을 제공합니다.
 */
public interface GetFulfillmentWarehousingAbnormalImagePort {

    /**
     * @param query 파스토 입고 이상 이미지 조회 쿼리
     * @return 파스토 입고 이상 이미지 조회 결과 {@link GetFulfillmentWarehousingAbnormalImageResult}
     * @Date 2026-02-17
     * @Author 성효빈
     * @Description 파스토 입고 이상 이미지를 조회합니다.
     */
    GetFulfillmentWarehousingAbnormalImageResult getWarehousingAbnormalImage(FulfillmentWarehousingAbnormalImageQuery query);
}
