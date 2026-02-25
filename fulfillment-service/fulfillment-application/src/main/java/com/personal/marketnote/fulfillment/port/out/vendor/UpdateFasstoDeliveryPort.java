package com.personal.marketnote.fulfillment.port.out.vendor;

import com.personal.marketnote.fulfillment.domain.vendor.fassto.delivery.FasstoDeliveryMapper;
import com.personal.marketnote.fulfillment.port.in.result.vendor.RegisterFasstoDeliveryResult;

/**
 * 파스토 출고 수정 포트
 *
 * @Author 성효빈
 * @Date 2026-02-11
 * @Description 파스토 출고 수정 기능을 제공합니다.
 */
public interface UpdateFasstoDeliveryPort {

    /**
     * @param request 파스토 출고 수정 요청 매퍼
     * @return 파스토 출고 수정 결과 {@link RegisterFasstoDeliveryResult}
     * @Date 2026-02-11
     * @Author 성효빈
     * @Description 파스토 출고 정보를 수정합니다.
     */
    RegisterFasstoDeliveryResult updateDelivery(FasstoDeliveryMapper request);
}
