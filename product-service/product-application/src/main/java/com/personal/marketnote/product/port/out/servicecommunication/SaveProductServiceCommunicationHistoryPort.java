package com.personal.marketnote.product.port.out.servicecommunication;

import com.personal.marketnote.product.domain.servicecommunication.ProductServiceCommunicationHistory;

/**
 * 상품 서비스 간 통신 기록 저장 포트
 *
 * @Author 성효빈
 * @Date 2026-01-31
 * @Description 상품 서비스 간 통신 기록 저장 기능을 제공합니다.
 */
public interface SaveProductServiceCommunicationHistoryPort {
    /**
     * @param history 상품 서비스 간 통신 기록 도메인
     * @return 저장된 상품 서비스 간 통신 기록 도메인 {@link ProductServiceCommunicationHistory}
     * @Date 2026-01-31
     * @Author 성효빈
     * @Description 상품 서비스 간 통신 기록을 저장합니다.
     */
    ProductServiceCommunicationHistory save(ProductServiceCommunicationHistory history);
}
