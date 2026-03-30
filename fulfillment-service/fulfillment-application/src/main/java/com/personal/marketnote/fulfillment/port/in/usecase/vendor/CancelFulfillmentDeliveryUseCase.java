package com.personal.marketnote.fulfillment.port.in.usecase.vendor;

import com.personal.marketnote.fulfillment.port.in.command.vendor.CancelFulfillmentDeliveryCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.CancelFulfillmentDeliveryResult;

/**
 * 파스토 출고 취소 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-02-12
 * @Description 파스토 출고 취소 기능을 제공합니다.
 */
public interface CancelFulfillmentDeliveryUseCase {
    CancelFulfillmentDeliveryResult cancelDelivery(CancelFulfillmentDeliveryCommand command);
}
