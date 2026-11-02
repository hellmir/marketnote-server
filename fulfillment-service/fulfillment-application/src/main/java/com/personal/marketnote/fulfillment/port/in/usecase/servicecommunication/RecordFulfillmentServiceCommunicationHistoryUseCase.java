package com.personal.marketnote.fulfillment.port.in.usecase.servicecommunication;

import com.personal.marketnote.fulfillment.domain.servicecommunication.FulfillmentServiceCommunicationHistory;
import com.personal.marketnote.fulfillment.port.in.command.servicecommunication.FulfillmentServiceCommunicationHistoryCommand;

/**
 * 풀필먼트 서비스 간 통신 기록 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-01-31
 * @Description 풀필먼트 서비스 간 통신 기록 기능을 제공합니다.
 */
public interface RecordFulfillmentServiceCommunicationHistoryUseCase {
    FulfillmentServiceCommunicationHistory record(FulfillmentServiceCommunicationHistoryCommand command);
}
