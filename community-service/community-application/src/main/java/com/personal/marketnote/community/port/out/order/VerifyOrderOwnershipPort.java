package com.personal.marketnote.community.port.out.order;

/**
 * 주문 소유권 검증 Out Port
 *
 * @Author 성효빈
 * @Date 2026-04-05
 * @Description 커머스 서비스에 주문 소유권을 검증합니다.
 */
public interface VerifyOrderOwnershipPort {
    /**
     * @param orderId 주문 ID
     * @param buyerId 구매자 ID
     * @throws com.personal.marketnote.community.exception.UnauthorizedOrderAccessException 소유권 검증 실패 시
     * @throws com.personal.marketnote.community.exception.InvalidOrderStatusForReviewException 리뷰 작성 불가능한 주문 상태 시
     */
    void verifyOrderOwnership(Long orderId, Long buyerId);
}
