package com.personal.marketnote.fulfillment.port.in.usecase.vendor;

import com.personal.marketnote.fulfillment.port.in.command.vendor.UpdateFasstoSupplierCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.UpdateFasstoSupplierResult;

/**
 * 파스토 공급사 수정 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-01-28
 * @Description 파스토 공급사 수정 기능을 제공합니다.
 */
public interface UpdateFasstoSupplierUseCase {
    /**
     * @param command 공급사 수정 커맨드
     * @return 공급사 수정 결과 {@link UpdateFasstoSupplierResult}
     * @Date 2026-01-28
     * @Author 성효빈
     * @Description 파스토 공급사를 수정합니다.
     */
    UpdateFasstoSupplierResult updateSupplier(UpdateFasstoSupplierCommand command);
}
