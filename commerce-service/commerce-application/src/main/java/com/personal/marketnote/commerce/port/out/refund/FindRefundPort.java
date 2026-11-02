package com.personal.marketnote.commerce.port.out.refund;

import com.personal.marketnote.commerce.domain.refund.Refund;

import java.util.List;

/**
 * 환불 조회 아웃바운드 포트.
 *
 * @author 성효빈
 * @since 2026-03-02
 */
public interface FindRefundPort {

    /**
     * 주문 ID로 환불 목록을 조회한다.
     *
     * @param orderId 주문 ID
     * @return 해당 주문의 환불 목록
     */
    List<Refund> findByOrderId(Long orderId);

    /**
     * 결제 ID로 환불 목록을 조회한다.
     *
     * @param paymentId 결제 ID
     * @return 해당 결제의 환불 목록
     */
    List<Refund> findByPaymentId(Long paymentId);
}
