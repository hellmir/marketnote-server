package com.personal.marketnote.commerce.port.out.reward;

import java.util.List;

/**
 * 회원 포인트 변경 포트
 *
 * @Author 성효빈
 * @Date 2026-01-18
 * @Description 회원 포인트 변경 관련 기능을 제공합니다.
 */
public interface ModifyUserPointPort {
    /**
     * @param sharerIds   공유자 ID 목록
     * @param totalAmount 총 결제 금액
     * @Date 2026-01-18
     * @Author 성효빈
     * @Description 공유 구매 포인트를 적립합니다.
     */
    void accrueSharedPurchasePoints(List<Long> sharerIds, Long totalAmount);

    /**
     * @param userId 회원 ID
     * @return 사용 가능한 포인트 잔액
     * @Date 2026-03-02
     * @Author 성효빈
     * @Description 회원의 사용 가능한 포인트 잔액을 조회합니다.
     */
    Long getAvailablePoints(Long userId);

    /**
     * @param userId  회원 ID
     * @param amount  차감할 포인트
     * @param orderId 주문 ID
     * @Date 2026-03-02
     * @Author 성효빈
     * @Description 주문 결제 시 포인트를 차감합니다.
     */
    void deductOrderPoints(Long userId, Long amount, Long orderId);

    /**
     * @param userId  회원 ID
     * @param amount  환불할 포인트
     * @param orderId 주문 ID
     * @Date 2026-03-02
     * @Author 성효빈
     * @Description 주문 취소 시 포인트를 환불합니다.
     */
    void refundOrderPoints(Long userId, Long amount, Long orderId);
}
