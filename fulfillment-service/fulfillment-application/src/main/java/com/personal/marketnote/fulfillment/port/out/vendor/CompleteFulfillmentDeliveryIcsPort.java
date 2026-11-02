package com.personal.marketnote.fulfillment.port.out.vendor;

import com.personal.marketnote.fulfillment.port.in.command.vendor.CompleteFulfillmentDeliveryIcsCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.CompleteFulfillmentDeliveryIcsResult;

/**
 * 풀필먼트 출고 ICS 완료 포트
 *
 * @Author 성효빈
 * @Date 2026-02-18
 * @Description 풀필먼트 출고 ICS 완료 기능을 제공합니다.
 */
public interface CompleteFulfillmentDeliveryIcsPort {

    /**
     * @param command 풀필먼트 출고 ICS 완료 커맨드
     * @return 풀필먼트 출고 ICS 완료 결과 {@link CompleteFulfillmentDeliveryIcsResult}
     * @Date 2026-02-18
     * @Author 성효빈
     * @Description 풀필먼트 출고 ICS를 완료 처리합니다.
     */
    CompleteFulfillmentDeliveryIcsResult completeDeliveryIcs(CompleteFulfillmentDeliveryIcsCommand command);
}
