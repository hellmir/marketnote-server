package com.personal.marketnote.fulfillment.port.out.vendor;

import com.personal.marketnote.fulfillment.domain.vendor.warehousing.FulfillmentWarehousingMapper;
import com.personal.marketnote.fulfillment.port.in.result.vendor.RegisterFulfillmentWarehousingResult;

/**
 * 파스토 입고 등록 포트
 *
 * @Author 성효빈
 * @Date 2026-01-31
 * @Description 파스토 입고 등록 기능을 제공합니다.
 */
public interface RegisterFulfillmentWarehousingPort {

    /**
     * @param request 파스토 입고 등록 요청 매퍼
     * @return 파스토 입고 등록 결과 {@link RegisterFulfillmentWarehousingResult}
     * @Date 2026-01-31
     * @Author 성효빈
     * @Description 파스토 입고를 등록합니다.
     */
    RegisterFulfillmentWarehousingResult registerWarehousing(FulfillmentWarehousingMapper request);
}
