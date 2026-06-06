package com.personal.marketnote.commerce.service.returntracker;

import com.personal.marketnote.commerce.domain.order.Order;
import com.personal.marketnote.commerce.domain.order.OrderStatus;
import com.personal.marketnote.commerce.domain.order.OrderStatusHistory;
import com.personal.marketnote.commerce.domain.order.OrderStatusHistoryCreateState;
import com.personal.marketnote.commerce.domain.returntracker.ReturnTracker;
import com.personal.marketnote.commerce.port.in.usecase.order.GetOrderUseCase;
import com.personal.marketnote.commerce.port.out.order.UpdateOrderPort;
import com.personal.marketnote.commerce.port.out.returntracker.UpdateReturnTrackerPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@Slf4j
@Service
@RequiredArgsConstructor
public class HandleInspectionFailedOrHoldService {

    private static final String INSPECTION_FAILED = "02";
    private static final String INSPECTION_ON_HOLD = "03";

    private final UpdateReturnTrackerPort updateReturnTrackerPort;
    private final GetOrderUseCase getOrderUseCase;
    private final UpdateOrderPort updateOrderPort;

    @Transactional(isolation = READ_COMMITTED)
    public void handleFailedOrHold(ReturnTracker tracker, String overallStatus, LocalDateTime now) {
        if (!INSPECTION_FAILED.equals(overallStatus) && !INSPECTION_ON_HOLD.equals(overallStatus)) {
            log.warn("예상하지 못한 검수 상태 코드: {}, orderId: {}", overallStatus, tracker.getOrderId());
            return;
        }

        applyInspectionStatus(tracker, overallStatus, now);
        updateReturnTrackerPort.update(tracker);

        Order order = getOrderUseCase.getOrder(tracker.getOrderId());
        if (order.isReturnInspecting()) {
            log.info("이미 반품 검수 중 상태 (멱등). orderId={}", tracker.getOrderId());
            return;
        }

        order.changeAllProductsStatus(OrderStatus.RETURN_INSPECTING, now);

        OrderStatusHistory history = OrderStatusHistory.from(
                OrderStatusHistoryCreateState.builder()
                        .orderId(order.getId())
                        .orderStatus(OrderStatus.RETURN_INSPECTING)
                        .build()
        );
        updateOrderPort.update(order, history);

        log.info("반품 검수 불량/보류 처리 완료 - orderId: {}, status: {}", tracker.getOrderId(), overallStatus);
    }

    private void applyInspectionStatus(ReturnTracker tracker, String status, LocalDateTime now) {
        if (INSPECTION_FAILED.equals(status)) {
            tracker.failInspection(now);
            return;
        }
        if (INSPECTION_ON_HOLD.equals(status)) {
            tracker.holdInspection();
        }
    }
}
