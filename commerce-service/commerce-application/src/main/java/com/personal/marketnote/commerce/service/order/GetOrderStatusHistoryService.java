package com.personal.marketnote.commerce.service.order;

import com.personal.marketnote.commerce.domain.order.OrderStatusHistory;
import com.personal.marketnote.commerce.exception.OrderNotFoundException;
import com.personal.marketnote.commerce.port.in.result.order.GetOrderStatusHistoryResult;
import com.personal.marketnote.commerce.port.in.usecase.order.GetOrderStatusHistoryUseCase;
import com.personal.marketnote.commerce.port.out.order.FindOrderPort;
import com.personal.marketnote.commerce.port.out.order.FindOrderStatusHistoryPort;
import com.personal.marketnote.common.application.UseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

/**
 * 주문 상태 이력 조회 서비스
 *
 * @Author 성효빈
 * @Date 2026-03-02
 * @Description 관리자가 특정 주문의 상태 변경 이력을 조회합니다.
 */
@UseCase
@RequiredArgsConstructor
@Slf4j
public class GetOrderStatusHistoryService implements GetOrderStatusHistoryUseCase {
    private final FindOrderPort findOrderPort;
    private final FindOrderStatusHistoryPort findOrderStatusHistoryPort;

    @Override
    @Transactional(isolation = READ_COMMITTED, readOnly = true)
    public GetOrderStatusHistoryResult getOrderStatusHistory(Long orderId) {
        log.info("주문 상태 이력 조회 - orderId={}", orderId);

        findOrderPort.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        List<OrderStatusHistory> histories = findOrderStatusHistoryPort.findAllByOrderId(orderId);

        return GetOrderStatusHistoryResult.from(orderId, histories);
    }
}
