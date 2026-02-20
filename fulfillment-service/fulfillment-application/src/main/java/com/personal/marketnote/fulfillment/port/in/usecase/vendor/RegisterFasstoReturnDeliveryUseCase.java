package com.personal.marketnote.fulfillment.port.in.usecase.vendor;

import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFasstoReturnDeliveryCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.RegisterFasstoDeliveryResult;

public interface RegisterFasstoReturnDeliveryUseCase {
    /**
     * @param command 반품 예약 등록 커맨드
     * @return 반품 예약 등록 결과 {@link RegisterFasstoDeliveryResult}
     * @Author 성효빈
     * @Date 2026-02-20
     * @Description 파스토 반품 예약 등록을 요청합니다.
     */
    RegisterFasstoDeliveryResult registerReturnDelivery(RegisterFasstoReturnDeliveryCommand command);
}
