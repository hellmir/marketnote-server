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
     * @param orderId     주문 ID (sourceId)
     * @Date 2026-03-07
     * @Author 성효빈
     * @Description 공유 구매 포인트를 적립 예정 포인트로 추가합니다.
     */
    void addPendingSharedPurchasePoints(List<Long> sharerIds, Long totalAmount, Long orderId);

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

    /**
     * @param userId  회원 ID
     * @param amount  적립 예정 포인트
     * @param orderId 주문 ID (sourceId)
     * @Date 2026-03-07
     * @Author 성효빈
     * @Description 주문 결제 완료 시 상품 적립 포인트를 적립 예정 포인트로 추가합니다.
     */
    void addPendingProductAccumulationPoints(Long userId, Long amount, Long orderId);

    /**
     * @param userId  회원 ID
     * @param orderId 주문 ID (sourceId)
     * @Date 2026-03-07
     * @Author 성효빈
     * @Description 구매 확정 시 적립 예정 포인트를 실제 포인트로 확정합니다.
     */
    void confirmPendingPoints(Long userId, Long orderId);

    /**
     * @param userId  회원 ID
     * @param orderId 주문 ID (sourceId)
     * @Date 2026-03-07
     * @Author 성효빈
     * @Description 결제 취소 시 적립 예정 포인트를 회수(취소)합니다.
     */
    void revokePendingPoints(Long userId, Long orderId);

    /**
     * @param sharerIds 공유자 ID 목록
     * @param orderId   주문 ID (sourceId)
     * @Date 2026-03-07
     * @Author 성효빈
     * @Description 결제 취소 시 공유자들의 적립 예정 포인트를 회수(취소)합니다.
     */
    void revokePendingSharedPurchasePoints(List<Long> sharerIds, Long orderId);

    /**
     * @param userId  회원 ID
     * @param amount  차감할 적립 예정 포인트
     * @param orderId 주문 ID (sourceId)
     * @Date 2026-03-07
     * @Author 성효빈
     * @Description 부분 결제 취소 시 취소 금액에 비례한 적립 예정 포인트를 차감합니다.
     */
    void reducePartialPendingPoints(Long userId, Long amount, Long orderId);

    /**
     * @param sharerIds     공유자 ID 목록
     * @param paymentAmount 원 결제 금액
     * @param cancelAmount  부분 취소 금액
     * @param orderId       주문 ID (sourceId)
     * @Date 2026-03-07
     * @Author 성효빈
     * @Description 부분 결제 취소 시 공유자들의 적립 예정 포인트를 취소 금액에 비례하여 차감합니다.
     */
    void reducePartialPendingSharedPurchasePoints(List<Long> sharerIds, Long paymentAmount, Long cancelAmount, Long orderId);
}
