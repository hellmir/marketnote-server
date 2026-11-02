package com.personal.marketnote.fulfillment.port.out.vendor;

import com.personal.marketnote.fulfillment.port.in.command.vendor.UpdateFulfillmentDeliveryCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.RegisterFulfillmentDeliveryResult;

/**
 * 풀필먼트 출고 수정 포트
 *
 * @Author 성효빈
 * @Date 2026-02-11
 * @Description 풀필먼트 출고 수정 기능을 제공합니다.
 */
public interface UpdateFulfillmentDeliveryPort {

    /**
     * @param command 풀필먼트 출고 수정 커맨드
     * @return 풀필먼트 출고 수정 결과 {@link RegisterFulfillmentDeliveryResult}
     * @Date 2026-02-11
     * @Author 성효빈
     * @Description 풀필먼트 출고 정보를 수정합니다.
     */
    RegisterFulfillmentDeliveryResult updateDelivery(UpdateFulfillmentDeliveryCommand command);
}
