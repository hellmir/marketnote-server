package com.personal.marketnote.fulfillment.port.out.vendor;

import com.personal.marketnote.fulfillment.domain.vendor.fassto.supplier.FasstoSupplierQuery;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFasstoSuppliersResult;

/**
 * 파스토 공급사 목록 조회 포트
 *
 * @Author 성효빈
 * @Date 2026-01-26
 * @Description 파스토 공급사 목록 조회 기능을 제공합니다.
 */
public interface GetFasstoSuppliersPort {

    /**
     * @param query 파스토 공급사 조회 쿼리
     * @return 파스토 공급사 목록 조회 결과 {@link GetFasstoSuppliersResult}
     * @Date 2026-01-26
     * @Author 성효빈
     * @Description 파스토 공급사 목록을 조회합니다.
     */
    GetFasstoSuppliersResult getSuppliers(FasstoSupplierQuery query);
}
