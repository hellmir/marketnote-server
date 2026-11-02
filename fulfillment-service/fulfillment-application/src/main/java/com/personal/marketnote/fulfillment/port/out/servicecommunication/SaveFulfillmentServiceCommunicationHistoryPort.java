package com.personal.marketnote.fulfillment.port.out.servicecommunication;

import com.personal.marketnote.fulfillment.domain.servicecommunication.FulfillmentServiceCommunicationHistory;

/**
 * 풀필먼트 서비스 간 통신 기록 저장 포트
 *
 * @Author 성효빈
 * @Date 2026-01-31
 * @Description 풀필먼트 서비스 간 통신 기록 저장 기능을 제공합니다.
 */
public interface SaveFulfillmentServiceCommunicationHistoryPort {

    /**
     * @param history 풀필먼트 서비스 간 통신 기록 도메인 객체
     * @return 저장된 풀필먼트 서비스 간 통신 기록 {@link FulfillmentServiceCommunicationHistory}
     * @Date 2026-01-31
     * @Author 성효빈
     * @Description 풀필먼트 서비스 간 통신 기록을 저장합니다.
     */
    FulfillmentServiceCommunicationHistory save(FulfillmentServiceCommunicationHistory history);
}
