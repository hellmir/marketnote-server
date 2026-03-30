package com.personal.marketnote.fulfillment.port.in.usecase.vendor;

import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentDirectReturnDeliveryCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.RegisterFulfillmentDeliveryResult;

/**
 * 파스토 직접 반품 등록 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-02-20
 * @Description 파스토 직접 반품 등록 기능을 제공합니다.
 */
public interface RegisterFulfillmentDirectReturnDeliveryUseCase {
    /**
     * @param command 반품 택배사 미지정 등록 커맨드
     * @return 반품 택배사 미지정 등록 결과 {@link RegisterFulfillmentDeliveryResult}
     * @Author 성효빈
     * @Date 2026-02-20
     * @Description 파스토 반품 택배사 미지정 등록을 요청합니다.
     */
    RegisterFulfillmentDeliveryResult registerDirectReturnDelivery(RegisterFulfillmentDirectReturnDeliveryCommand command);
}
