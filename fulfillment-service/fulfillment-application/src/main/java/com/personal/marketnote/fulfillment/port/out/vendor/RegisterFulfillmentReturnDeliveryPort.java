package com.personal.marketnote.fulfillment.port.out.vendor;

import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentReturnDeliveryCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.RegisterFulfillmentDeliveryResult;

/**
 * 풀필먼트 반품 등록 포트
 *
 * @Author 성효빈
 * @Date 2026-02-20
 * @Description 풀필먼트 반품 등록 기능을 제공합니다.
 */
public interface RegisterFulfillmentReturnDeliveryPort {

    /**
     * @param command 풀필먼트 반품 등록 커맨드
     * @return 풀필먼트 반품 등록 결과 {@link RegisterFulfillmentDeliveryResult}
     * @Date 2026-02-20
     * @Author 성효빈
     * @Description 풀필먼트 반품을 등록합니다.
     */
    RegisterFulfillmentDeliveryResult registerReturnDelivery(RegisterFulfillmentReturnDeliveryCommand command);
}
