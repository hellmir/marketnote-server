package com.personal.marketnote.commerce.port.out.vendorcommunication;

import com.personal.marketnote.commerce.domain.vendorcommunication.CommerceVendorCommunicationHistory;

/**
 * 커머스 벤더 통신 기록 저장 포트
 *
 * @Author 성효빈
 * @Date 2026-02-25
 * @Description 커머스 벤더 통신 기록 저장 기능을 제공합니다.
 */
public interface SaveCommerceVendorCommunicationHistoryPort {
    /**
     * @param history 커머스 벤더 통신 기록
     * @return 저장된 커머스 벤더 통신 기록 {@link CommerceVendorCommunicationHistory}
     * @Date 2026-02-25
     * @Author 성효빈
     * @Description 커머스 벤더 통신 기록을 저장합니다.
     */
    CommerceVendorCommunicationHistory save(CommerceVendorCommunicationHistory history);
}
