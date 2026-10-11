package com.personal.marketnote.commerce.service.returntracker;

import com.personal.marketnote.commerce.domain.order.Order;
import com.personal.marketnote.commerce.domain.order.OrderStatus;
import com.personal.marketnote.commerce.domain.order.OrderStatusHistory;
import com.personal.marketnote.commerce.domain.order.OrderStatusHistoryCreateState;
import com.personal.marketnote.commerce.domain.returntracker.ReturnTracker;
import com.personal.marketnote.commerce.port.in.usecase.order.GetOrderUseCase;
import com.personal.marketnote.commerce.port.out.event.PublishOrderEventPort;
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
public class FailReturnInspectionService {

    private static final OrderStatus TARGET_STATUS = OrderStatus.RETURN_REJECTED;

    private final UpdateReturnTrackerPort updateReturnTrackerPort;
    private final GetOrderUseCase getOrderUseCase;
    private final UpdateOrderPort updateOrderPort;
    private final PublishOrderEventPort publishOrderEventPort;

    @Transactional(isolation = READ_COMMITTED)
    public void failInspection(ReturnTracker tracker, LocalDateTime now) {
        tracker.failInspection(now);
        updateReturnTrackerPort.update(tracker);

        Order order = getOrderUseCase.getOrder(tracker.getOrderId());

        if (!order.getOrderStatus().canTransitionTo(TARGET_STATUS)) {
            log.warn("반품 검수 불합격이나 주문 상태 전이 불가. orderId={}, currentStatus={}",
                    tracker.getOrderId(), order.getOrderStatus()
            );
            return;
        }

        order.changeAllProductsStatus(TARGET_STATUS, now);

        OrderStatusHistory orderStatusHistory = OrderStatusHistory.from(
                OrderStatusHistoryCreateState.builder()
                        .orderId(tracker.getOrderId())
                        .orderStatus(TARGET_STATUS)
                        .reason("반품 검수 불합격")
                        .build()
        );

        updateOrderPort.update(order, orderStatusHistory);
        publishOrderEventPort.publishReturnRejectedEvent(order.getId(), order.getBuyerId());

        log.info("반품 검수 불합격 처리 완료. orderId={}", tracker.getOrderId());
    }
}
