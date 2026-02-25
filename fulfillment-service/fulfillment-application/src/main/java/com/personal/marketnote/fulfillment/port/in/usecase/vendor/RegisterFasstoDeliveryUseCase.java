package com.personal.marketnote.fulfillment.port.in.usecase.vendor;

import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFasstoDeliveryCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.RegisterFasstoDeliveryResult;

/**
 * 파스토 출고 등록 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-02-11
 * @Description 파스토 출고 등록 기능을 제공합니다.
 */
public interface RegisterFasstoDeliveryUseCase {
    /**
     * @param command 출고 등록 커맨드
     * @return 출고 등록 결과 {@link RegisterFasstoDeliveryResult}
     * @Author 성효빈
     * @Date 2026-02-11
     * @Description 파스토 출고(택배) 등록을 요청합니다.
     */
    RegisterFasstoDeliveryResult registerDelivery(RegisterFasstoDeliveryCommand command);
}
