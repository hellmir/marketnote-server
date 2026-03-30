package com.personal.marketnote.fulfillment.port.in.usecase.vendor;

import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentSupplierCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.RegisterFulfillmentSupplierResult;

/**
 * 파스토 공급사 등록 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-01-26
 * @Description 파스토 공급사 등록 기능을 제공합니다.
 */
public interface RegisterFulfillmentSupplierUseCase {
    /**
     * @param command 공급사 등록 커맨드
     * @return 공급사 등록 결과 {@link RegisterFulfillmentSupplierResult}
     * @Date 2026-01-26
     * @Author 성효빈
     * @Description 파스토 공급사를 등록합니다.
     */
    RegisterFulfillmentSupplierResult registerSupplier(RegisterFulfillmentSupplierCommand command);
}
