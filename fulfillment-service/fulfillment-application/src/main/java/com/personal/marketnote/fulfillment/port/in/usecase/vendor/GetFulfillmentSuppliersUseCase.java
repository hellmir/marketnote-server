package com.personal.marketnote.fulfillment.port.in.usecase.vendor;

import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentSuppliersCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentSuppliersResult;

/**
 * 풀필먼트 공급사 목록 조회 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-01-26
 * @Description 풀필먼트 공급사 목록 조회 기능을 제공합니다.
 */
public interface GetFulfillmentSuppliersUseCase {
    /**
     * @param command 공급사 목록 조회 커맨드
     * @return 공급사 목록 조회 결과 {@link GetFulfillmentSuppliersResult}
     * @Date 2026-01-25
     * @Author 성효빈
     * @Description 풀필먼트 공급사 목록을 조회합니다.
     */
    GetFulfillmentSuppliersResult getSuppliers(GetFulfillmentSuppliersCommand command);
}
