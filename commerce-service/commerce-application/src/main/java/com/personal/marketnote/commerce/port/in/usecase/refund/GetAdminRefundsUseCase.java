package com.personal.marketnote.commerce.port.in.usecase.refund;

import com.personal.marketnote.commerce.port.in.result.refund.GetAdminRefundResult;

import java.util.List;

/**
 * 관리자 환불 목록 조회 유스케이스.
 *
 * @author 성효빈
 * @since 2026-03-02
 */
public interface GetAdminRefundsUseCase {

    /**
     * 주문 ID에 해당하는 환불 목록을 조회한다.
     *
     * @param orderId 주문 ID
     * @return 환불 결과 목록
     */
    List<GetAdminRefundResult> getRefundsByOrderId(Long orderId);
}
