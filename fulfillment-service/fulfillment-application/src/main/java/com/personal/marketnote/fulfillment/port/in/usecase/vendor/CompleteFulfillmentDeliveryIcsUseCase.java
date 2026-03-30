package com.personal.marketnote.fulfillment.port.in.usecase.vendor;

import com.personal.marketnote.fulfillment.port.in.command.vendor.CompleteFulfillmentDeliveryIcsCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.CompleteFulfillmentDeliveryIcsResult;

/**
 * 파스토 출고 ICS 완료 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-02-18
 * @Description 파스토 출고 ICS 완료 기능을 제공합니다.
 */
public interface CompleteFulfillmentDeliveryIcsUseCase {
    /**
     * @param command 해외 배송완료 처리 커맨드
     * @return 해외 배송완료 처리 결과
     * @Author 성효빈
     * @Date 2026-02-18
     * @Description 파스토 배송완료 처리(해외)를 요청합니다.
     */
    CompleteFulfillmentDeliveryIcsResult completeDeliveryIcs(CompleteFulfillmentDeliveryIcsCommand command);
}
