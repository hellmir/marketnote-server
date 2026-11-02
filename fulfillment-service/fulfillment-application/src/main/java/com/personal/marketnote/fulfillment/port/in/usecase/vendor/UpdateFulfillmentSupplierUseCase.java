package com.personal.marketnote.fulfillment.port.in.usecase.vendor;

import com.personal.marketnote.fulfillment.port.in.command.vendor.UpdateFulfillmentSupplierCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.UpdateFulfillmentSupplierResult;

/**
 * 풀필먼트 공급사 수정 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-01-28
 * @Description 풀필먼트 공급사 수정 기능을 제공합니다.
 */
public interface UpdateFulfillmentSupplierUseCase {
    /**
     * @param command 공급사 수정 커맨드
     * @return 공급사 수정 결과 {@link UpdateFulfillmentSupplierResult}
     * @Date 2026-01-28
     * @Author 성효빈
     * @Description 풀필먼트 공급사를 수정합니다.
     */
    UpdateFulfillmentSupplierResult updateSupplier(UpdateFulfillmentSupplierCommand command);
}
