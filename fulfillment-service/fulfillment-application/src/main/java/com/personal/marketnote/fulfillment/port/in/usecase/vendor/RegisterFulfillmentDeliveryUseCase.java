package com.personal.marketnote.fulfillment.port.in.usecase.vendor;

import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentDeliveryCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.RegisterFulfillmentDeliveryResult;

/**
 * 풀필먼트 출고 등록 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-02-11
 * @Description 풀필먼트 출고 등록 기능을 제공합니다.
 */
public interface RegisterFulfillmentDeliveryUseCase {
    /**
     * @param command 출고 등록 커맨드
     * @return 출고 등록 결과 {@link RegisterFulfillmentDeliveryResult}
     * @Author 성효빈
     * @Date 2026-02-11
     * @Description 풀필먼트 출고(택배) 등록을 요청합니다.
     */
    RegisterFulfillmentDeliveryResult registerDelivery(RegisterFulfillmentDeliveryCommand command);

    /**
     * @param command 출고 등록 커맨드
     * @return 출고 등록 결과 {@link RegisterFulfillmentDeliveryResult}
     * @Author 성효빈
     * @Date 2026-03-15
     * @Description 풀필먼트 출고(택배) 등록을 멱등하게 요청합니다.
     * orderId 기반 출고 이력을 먼저 저장하여 중복 출고 요청을 방지합니다.
     * Kafka Consumer에서 사용합니다.
     */
    RegisterFulfillmentDeliveryResult registerDeliveryIdempotent(RegisterFulfillmentDeliveryCommand command);
}
