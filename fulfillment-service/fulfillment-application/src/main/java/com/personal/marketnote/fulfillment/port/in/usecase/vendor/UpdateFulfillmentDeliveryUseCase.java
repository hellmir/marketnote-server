package com.personal.marketnote.fulfillment.port.in.usecase.vendor;

import com.personal.marketnote.fulfillment.port.in.command.vendor.UpdateFulfillmentDeliveryCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.RegisterFulfillmentDeliveryResult;

/**
 * 풀필먼트 출고 수정 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-02-17
 * @Description 풀필먼트 출고 수정 기능을 제공합니다.
 */
public interface UpdateFulfillmentDeliveryUseCase {
    RegisterFulfillmentDeliveryResult updateDelivery(UpdateFulfillmentDeliveryCommand command);
}
