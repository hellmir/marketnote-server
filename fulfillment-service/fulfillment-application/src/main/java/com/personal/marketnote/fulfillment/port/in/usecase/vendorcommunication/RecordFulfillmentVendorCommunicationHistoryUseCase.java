package com.personal.marketnote.fulfillment.port.in.usecase.vendorcommunication;

import com.personal.marketnote.fulfillment.domain.vendorcommunication.FulfillmentVendorCommunicationHistory;
import com.personal.marketnote.fulfillment.port.in.command.vendorcommunication.FulfillmentVendorCommunicationHistoryCommand;

/**
 * 풀필먼트 벤더 통신 기록 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-01-25
 * @Description 풀필먼트 벤더 통신 기록 기능을 제공합니다.
 */
public interface RecordFulfillmentVendorCommunicationHistoryUseCase {
    FulfillmentVendorCommunicationHistory record(FulfillmentVendorCommunicationHistoryCommand command);
}
