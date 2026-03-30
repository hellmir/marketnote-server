package com.personal.marketnote.fulfillment.port.out.vendor;

import com.personal.marketnote.fulfillment.domain.vendor.supplier.FulfillmentSupplierMapper;
import com.personal.marketnote.fulfillment.port.in.result.vendor.RegisterFulfillmentSupplierResult;

/**
 * 파스토 공급사 등록 포트
 *
 * @Author 성효빈
 * @Date 2026-01-26
 * @Description 파스토 공급사 등록 기능을 제공합니다.
 */
public interface RegisterFulfillmentSupplierPort {

    /**
     * @param request 파스토 공급사 등록 요청 매퍼
     * @return 파스토 공급사 등록 결과 {@link RegisterFulfillmentSupplierResult}
     * @Date 2026-01-26
     * @Author 성효빈
     * @Description 파스토 공급사를 등록합니다.
     */
    RegisterFulfillmentSupplierResult registerSupplier(FulfillmentSupplierMapper request);
}
