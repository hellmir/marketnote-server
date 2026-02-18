package com.personal.marketnote.fulfillment.port.in.usecase.vendor;

import com.personal.marketnote.fulfillment.port.in.command.vendor.CompleteFasstoDeliveryIcsCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.CompleteFasstoDeliveryIcsResult;

public interface CompleteFasstoDeliveryIcsUseCase {
    /**
     * @param command 해외 배송완료 처리 커맨드
     * @return 해외 배송완료 처리 결과
     * @Author 성효빈
     * @Date 2026-02-18
     * @Description 파스토 배송완료 처리(해외)를 요청합니다.
     */
    CompleteFasstoDeliveryIcsResult completeDeliveryIcs(CompleteFasstoDeliveryIcsCommand command);
}
