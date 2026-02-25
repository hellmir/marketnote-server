package com.personal.marketnote.product.port.in.usecase.servicecommunication;

import com.personal.marketnote.product.domain.servicecommunication.ProductServiceCommunicationHistory;
import com.personal.marketnote.product.port.in.command.servicecommunication.ProductServiceCommunicationHistoryCommand;

/**
 * 상품 서비스 간 통신 기록 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-01-31
 * @Description 상품 서비스 간 통신 기록 기능을 제공합니다.
 */
public interface RecordProductServiceCommunicationHistoryUseCase {
    /**
     * @param command 상품 서비스 통신 기록 커맨드
     * @return 상품 서비스 통신 기록 {@link ProductServiceCommunicationHistory}
     * @Date 2026-02-18
     * @Author 성효빈
     * @Description 상품 서비스 통신 기록을 저장합니다.
     */
    ProductServiceCommunicationHistory record(ProductServiceCommunicationHistoryCommand command);
}
