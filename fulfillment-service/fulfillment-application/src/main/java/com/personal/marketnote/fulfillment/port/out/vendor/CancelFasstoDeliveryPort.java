package com.personal.marketnote.fulfillment.port.out.vendor;

import com.personal.marketnote.fulfillment.domain.vendor.fassto.delivery.FasstoDeliveryCancelMapper;
import com.personal.marketnote.fulfillment.port.in.result.vendor.CancelFasstoDeliveryResult;

/**
 * 파스토 출고 취소 포트
 *
 * @Author 성효빈
 * @Date 2026-02-12
 * @Description 파스토 출고 취소 기능을 제공합니다.
 */
public interface CancelFasstoDeliveryPort {

    /**
     * @param request 파스토 출고 취소 요청 매퍼
     * @return 파스토 출고 취소 결과 {@link CancelFasstoDeliveryResult}
     * @Date 2026-02-12
     * @Author 성효빈
     * @Description 파스토 출고를 취소합니다.
     */
    CancelFasstoDeliveryResult cancelDelivery(FasstoDeliveryCancelMapper request);
}
