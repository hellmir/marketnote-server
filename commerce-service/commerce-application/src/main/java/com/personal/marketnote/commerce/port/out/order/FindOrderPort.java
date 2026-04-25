package com.personal.marketnote.commerce.port.out.order;

import com.personal.marketnote.commerce.domain.order.Order;
import com.personal.marketnote.commerce.domain.order.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 주문 조회 포트
 *
 * @Author 성효빈
 * @Date 2026-01-05
 * @Description 주문 조회 관련 기능을 제공합니다.
 */
public interface FindOrderPort {
    /**
     * @param id 주문 ID
     * @return 주문 도메인 {@link Order}
     * @Date 2026-01-06
     * @Author 성효빈
     * @Description 주문 ID로 주문 정보를 조회합니다.
     */
    Optional<Order> findById(Long id);

    /**
     * @param buyerId   구매자 ID
     * @param startDate 조회 시작 일시
     * @param endDate   조회 종료 일시
     * @param statuses  주문 상태 목록
     * @return 주문 목록 {@link List}
     * @Date 2026-01-06
     * @Author 성효빈
     * @Description 구매자 ID와 조건으로 주문 목록을 조회합니다.
     */
    List<Order> findByBuyerId(
            Long buyerId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            List<OrderStatus> statuses
    );

    /**
     * @param sellerId    판매자 ID (null이면 전체)
     * @param startDate   조회 시작 일시 (null이면 제한 없음)
     * @param endDate     조회 종료 일시 (null이면 제한 없음)
     * @param orderStatus 주문 상태 (null이면 전체)
     * @return 주문 목록 {@link List}
     * @Date 2026-03-02
     * @Author 성효빈
     * @Description 관리자용 전체 주문 조회. 판매자별, 기간별, 상태별 필터링을 지원합니다.
     */
    List<Order> findAllWithFilters(
            Long sellerId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            OrderStatus orderStatus
    );

    List<Long> findOrderIdsEligibleForAutoConfirm(LocalDateTime deliveredBefore);
}
