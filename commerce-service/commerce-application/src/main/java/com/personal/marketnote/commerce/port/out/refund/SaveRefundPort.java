package com.personal.marketnote.commerce.port.out.refund;

import com.personal.marketnote.commerce.domain.refund.Refund;

/**
 * 환불 저장 아웃바운드 포트.
 *
 * @author 성효빈
 * @since 2026-03-02
 */
public interface SaveRefundPort {

    /**
     * 환불 정보를 저장한다.
     *
     * @param refund 저장할 환불 도메인 객체
     * @return 저장된 환불 도메인 객체 (ID 포함)
     */
    Refund save(Refund refund);
}
