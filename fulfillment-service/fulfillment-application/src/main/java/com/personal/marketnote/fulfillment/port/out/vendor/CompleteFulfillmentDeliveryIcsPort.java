package com.personal.marketnote.fulfillment.port.out.vendor;

import com.personal.marketnote.fulfillment.domain.vendor.delivery.FulfillmentDeliveryIcsCompletionMapper;
import com.personal.marketnote.fulfillment.port.in.result.vendor.CompleteFulfillmentDeliveryIcsResult;

/**
 * 파스토 출고 ICS 완료 포트
 *
 * @Author 성효빈
 * @Date 2026-02-18
 * @Description 파스토 출고 ICS 완료 기능을 제공합니다.
 */
public interface CompleteFulfillmentDeliveryIcsPort {

    /**
     * @param request 파스토 출고 ICS 완료 요청 매퍼
     * @return 파스토 출고 ICS 완료 결과 {@link CompleteFulfillmentDeliveryIcsResult}
     * @Date 2026-02-18
     * @Author 성효빈
     * @Description 파스토 출고 ICS를 완료 처리합니다.
     */
    CompleteFulfillmentDeliveryIcsResult completeDeliveryIcs(FulfillmentDeliveryIcsCompletionMapper request);
}
