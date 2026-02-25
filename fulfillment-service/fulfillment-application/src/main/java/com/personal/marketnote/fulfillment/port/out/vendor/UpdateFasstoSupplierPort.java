package com.personal.marketnote.fulfillment.port.out.vendor;

import com.personal.marketnote.fulfillment.domain.vendor.fassto.supplier.FasstoSupplierMapper;
import com.personal.marketnote.fulfillment.port.in.result.vendor.UpdateFasstoSupplierResult;

/**
 * 파스토 공급사 수정 포트
 *
 * @Author 성효빈
 * @Date 2026-01-28
 * @Description 파스토 공급사 수정 기능을 제공합니다.
 */
public interface UpdateFasstoSupplierPort {

    /**
     * @param request 파스토 공급사 수정 요청 매퍼
     * @return 파스토 공급사 수정 결과 {@link UpdateFasstoSupplierResult}
     * @Date 2026-01-28
     * @Author 성효빈
     * @Description 파스토 공급사를 수정합니다.
     */
    UpdateFasstoSupplierResult updateSupplier(FasstoSupplierMapper request);
}
