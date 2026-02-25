package com.personal.marketnote.fulfillment.port.in.usecase.vendor;

import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFasstoDeliveryCarCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.RegisterFasstoDeliveryResult;

/**
 * 파스토 출고 집차 등록 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-02-17
 * @Description 파스토 출고 집차 등록 기능을 제공합니다.
 */
public interface RegisterFasstoDeliveryCarUseCase {
    RegisterFasstoDeliveryResult registerDeliveryCar(RegisterFasstoDeliveryCarCommand command);
}
