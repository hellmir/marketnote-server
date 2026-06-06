package com.personal.marketnote.commerce.service.returntracker;

import com.personal.marketnote.commerce.domain.order.Order;
import com.personal.marketnote.commerce.domain.order.OrderStatus;
import com.personal.marketnote.commerce.domain.order.OrderStatusHistory;
import com.personal.marketnote.commerce.domain.order.OrderStatusHistoryCreateState;
import com.personal.marketnote.commerce.exception.InvalidOrderStatusTransitionException;
import com.personal.marketnote.commerce.port.in.command.returntracker.RejectReturnInspectionCommand;
import com.personal.marketnote.commerce.port.in.usecase.order.GetOrderUseCase;
import com.personal.marketnote.commerce.port.in.usecase.returntracker.RejectReturnInspectionUseCase;
import com.personal.marketnote.commerce.port.out.order.UpdateOrderPort;
import com.personal.marketnote.common.application.UseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class RejectReturnInspectionService implements RejectReturnInspectionUseCase {

    private final GetOrderUseCase getOrderUseCase;
    private final UpdateOrderPort updateOrderPort;
    private final Clock clock;

    @Override
    @Transactional(isolation = READ_COMMITTED)
    public void rejectReturnInspection(RejectReturnInspectionCommand command) {
        Order order = getOrderUseCase.getOrder(command.orderId());

        if (order.isReturnRejected()) {
            log.info("이미 반품 불가 처리된 주문 (멱등). orderId={}", command.orderId());
            return;
        }

        if (!order.isReturnInspecting()) {
            throw new InvalidOrderStatusTransitionException(order.getOrderStatus(), OrderStatus.RETURN_REJECTED);
        }

        LocalDateTime now = LocalDateTime.now(clock);
        order.changeAllProductsStatus(OrderStatus.RETURN_REJECTED, now);

        OrderStatusHistory history = OrderStatusHistory.from(
                OrderStatusHistoryCreateState.builder()
                        .orderId(order.getId())
                        .orderStatus(OrderStatus.RETURN_REJECTED)
                        .reasonCategory(order.getStatusChangeReasonCategory())
                        .build()
        );
        updateOrderPort.update(order, history);

        log.info("CS 반품 불가 판정 완료. orderId={}", command.orderId());
    }
}
