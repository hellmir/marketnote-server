package com.personal.marketnote.fulfillment.port.out.vendorcommunication;

import com.personal.marketnote.fulfillment.domain.vendorcommunication.FulfillmentVendorCommunicationHistory;

/**
 * 풀필먼트 벤더 통신 기록 저장 포트
 *
 * @Author 성효빈
 * @Date 2026-01-25
 * @Description 풀필먼트 벤더 통신 기록 저장 기능을 제공합니다.
 */
public interface SaveFulfillmentVendorCommunicationHistoryPort {

    /**
     * @param history 풀필먼트 벤더 통신 기록 도메인 객체
     * @return 저장된 풀필먼트 벤더 통신 기록 {@link FulfillmentVendorCommunicationHistory}
     * @Date 2026-01-25
     * @Author 성효빈
     * @Description 풀필먼트 벤더 통신 기록을 저장합니다.
     */
    FulfillmentVendorCommunicationHistory save(FulfillmentVendorCommunicationHistory history);
}
