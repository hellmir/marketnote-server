package com.personal.marketnote.commerce.service.returntracker;

import com.personal.marketnote.commerce.domain.order.Order;
import com.personal.marketnote.commerce.domain.order.OrderStatus;
import com.personal.marketnote.commerce.domain.order.OrderStatusHistory;
import com.personal.marketnote.commerce.domain.order.OrderStatusHistoryCreateState;
import com.personal.marketnote.commerce.exception.InvalidOrderStatusTransitionException;
import com.personal.marketnote.commerce.port.in.command.returntracker.CompleteReturnReshippingCommand;
import com.personal.marketnote.commerce.port.in.usecase.order.GetOrderUseCase;
import com.personal.marketnote.commerce.port.in.usecase.returntracker.CompleteReturnReshippingUseCase;
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
public class CompleteReturnReshippingService implements CompleteReturnReshippingUseCase {

    private final GetOrderUseCase getOrderUseCase;
    private final UpdateOrderPort updateOrderPort;
    private final Clock clock;

    @Override
    @Transactional(isolation = READ_COMMITTED)
    public void completeReturnReshipping(CompleteReturnReshippingCommand command) {
        Order order = getOrderUseCase.getOrder(command.orderId());

        if (order.isReturnReshipped()) {
            log.info("이미 회송 완료된 주문 (멱등). orderId={}", command.orderId());
            return;
        }

        if (!order.isReturnReshipping()) {
            throw new InvalidOrderStatusTransitionException(order.getOrderStatus(), OrderStatus.RETURN_RESHIPPED);
        }

        LocalDateTime now = LocalDateTime.now(clock);
        order.changeAllProductsStatus(OrderStatus.RETURN_RESHIPPED, now);

        OrderStatusHistory history = OrderStatusHistory.from(
                OrderStatusHistoryCreateState.builder()
                        .orderId(order.getId())
                        .orderStatus(OrderStatus.RETURN_RESHIPPED)
                        .reasonCategory(order.getStatusChangeReasonCategory())
                        .build()
        );
        updateOrderPort.update(order, history);

        log.info("CS 회송 완료 처리. orderId={}", command.orderId());
    }
}
