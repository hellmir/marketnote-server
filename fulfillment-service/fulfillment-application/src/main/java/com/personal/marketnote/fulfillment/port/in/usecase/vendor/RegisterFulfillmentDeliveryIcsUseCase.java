package com.personal.marketnote.fulfillment.port.in.usecase.vendor;

import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentDeliveryIcsCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.RegisterFulfillmentDeliveryResult;

/**
 * 풀필먼트 출고 ICS 등록 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-02-17
 * @Description 풀필먼트 출고 ICS 등록 기능을 제공합니다.
 */
public interface RegisterFulfillmentDeliveryIcsUseCase {
    RegisterFulfillmentDeliveryResult registerDeliveryIcs(RegisterFulfillmentDeliveryIcsCommand command);
}
