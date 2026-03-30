package com.personal.marketnote.fulfillment.port.out.vendor;

import com.personal.marketnote.fulfillment.domain.vendor.supplier.FulfillmentSupplierQuery;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentSuppliersResult;

/**
 * 파스토 공급사 목록 조회 포트
 *
 * @Author 성효빈
 * @Date 2026-01-26
 * @Description 파스토 공급사 목록 조회 기능을 제공합니다.
 */
public interface GetFulfillmentSuppliersPort {

    /**
     * @param query 파스토 공급사 조회 쿼리
     * @return 파스토 공급사 목록 조회 결과 {@link GetFulfillmentSuppliersResult}
     * @Date 2026-01-26
     * @Author 성효빈
     * @Description 파스토 공급사 목록을 조회합니다.
     */
    GetFulfillmentSuppliersResult getSuppliers(FulfillmentSupplierQuery query);
}
