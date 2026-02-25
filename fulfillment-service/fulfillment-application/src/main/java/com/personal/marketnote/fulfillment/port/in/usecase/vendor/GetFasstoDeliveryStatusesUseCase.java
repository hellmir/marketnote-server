package com.personal.marketnote.fulfillment.port.in.usecase.vendor;

import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFasstoDeliveryStatusesCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFasstoDeliveryStatusesResult;

/**
 * 파스토 출고 상태 목록 조회 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-02-13
 * @Description 파스토 출고 상태 목록 조회 기능을 제공합니다.
 */
public interface GetFasstoDeliveryStatusesUseCase {
    GetFasstoDeliveryStatusesResult getDeliveryStatuses(GetFasstoDeliveryStatusesCommand command);
}
