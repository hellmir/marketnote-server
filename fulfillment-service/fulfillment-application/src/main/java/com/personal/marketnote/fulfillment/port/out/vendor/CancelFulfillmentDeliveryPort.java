package com.personal.marketnote.fulfillment.port.out.vendor;

import com.personal.marketnote.fulfillment.port.in.command.vendor.CancelFulfillmentDeliveryCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.CancelFulfillmentDeliveryResult;

/**
 * 풀필먼트 출고 취소 포트
 *
 * @Author 성효빈
 * @Date 2026-02-12
 * @Description 풀필먼트 출고 취소 기능을 제공합니다.
 */
public interface CancelFulfillmentDeliveryPort {

    /**
     * @param command 풀필먼트 출고 취소 커맨드
     * @return 풀필먼트 출고 취소 결과 {@link CancelFulfillmentDeliveryResult}
     * @Date 2026-02-12
     * @Author 성효빈
     * @Description 풀필먼트 출고를 취소합니다.
     */
    CancelFulfillmentDeliveryResult cancelDelivery(CancelFulfillmentDeliveryCommand command);
}
