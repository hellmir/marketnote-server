package com.personal.marketnote.fulfillment.port.out.vendor;

import com.personal.marketnote.fulfillment.domain.vendor.fassto.returndelivery.FasstoReturnDeliveryMapper;
import com.personal.marketnote.fulfillment.port.in.result.vendor.RegisterFasstoDeliveryResult;

/**
 * 파스토 반품 등록 포트
 *
 * @Author 성효빈
 * @Date 2026-02-20
 * @Description 파스토 반품 등록 기능을 제공합니다.
 */
public interface RegisterFasstoReturnDeliveryPort {

    /**
     * @param request 파스토 반품 등록 요청 매퍼
     * @return 파스토 반품 등록 결과 {@link RegisterFasstoDeliveryResult}
     * @Date 2026-02-20
     * @Author 성효빈
     * @Description 파스토 반품을 등록합니다.
     */
    RegisterFasstoDeliveryResult registerReturnDelivery(FasstoReturnDeliveryMapper request);
}
