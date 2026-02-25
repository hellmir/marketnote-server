package com.personal.marketnote.fulfillment.port.out.vendor;

import com.personal.marketnote.fulfillment.domain.vendor.fassto.supplier.FasstoSupplierMapper;
import com.personal.marketnote.fulfillment.port.in.result.vendor.RegisterFasstoSupplierResult;

/**
 * 파스토 공급사 등록 포트
 *
 * @Author 성효빈
 * @Date 2026-01-26
 * @Description 파스토 공급사 등록 기능을 제공합니다.
 */
public interface RegisterFasstoSupplierPort {

    /**
     * @param request 파스토 공급사 등록 요청 매퍼
     * @return 파스토 공급사 등록 결과 {@link RegisterFasstoSupplierResult}
     * @Date 2026-01-26
     * @Author 성효빈
     * @Description 파스토 공급사를 등록합니다.
     */
    RegisterFasstoSupplierResult registerSupplier(FasstoSupplierMapper request);
}
