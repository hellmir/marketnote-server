package com.personal.marketnote.commerce.service.order;

import com.personal.marketnote.commerce.domain.order.Order;
import com.personal.marketnote.commerce.domain.order.OrderStatus;
import com.personal.marketnote.commerce.domain.order.OrderStatusHistory;
import com.personal.marketnote.commerce.domain.order.OrderStatusHistoryCreateState;
import com.personal.marketnote.commerce.exception.InvalidOrderStatusTransitionException;
import com.personal.marketnote.commerce.exception.OrderStatusAlreadyChangedException;
import com.personal.marketnote.commerce.exception.UnauthorizedOrderAccessException;
import com.personal.marketnote.commerce.port.in.command.order.CancelOrderCommand;
import com.personal.marketnote.commerce.port.in.usecase.order.CancelOrderUseCase;
import com.personal.marketnote.commerce.port.in.usecase.order.GetOrderUseCase;
import com.personal.marketnote.commerce.port.out.order.UpdateOrderPort;
import com.personal.marketnote.common.application.UseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED)
@Slf4j
public class CancelOrderService implements CancelOrderUseCase {
    private final GetOrderUseCase getOrderUseCase;
    private final UpdateOrderPort updateOrderPort;
    private final Clock clock;

    private static final OrderStatus TARGET_STATUS = OrderStatus.CANCELLED;

    @Override
    public void cancelOrder(CancelOrderCommand command) {
        Order order = getOrderUseCase.getOrder(command.id());

        validateBuyerOwnership(command, order);
        validateStatusTransition(order);

        LocalDateTime now = LocalDateTime.now(clock);
        order.changeAllProductsStatus(TARGET_STATUS, now);

        OrderStatusHistory orderStatusHistory = OrderStatusHistory.from(
                OrderStatusHistoryCreateState.builder()
                        .orderId(command.id())
                        .orderStatus(TARGET_STATUS)
                        .reasonCategory(command.reasonCategory())
                        .reason(command.reason())
                        .build()
        );

        updateOrderPort.update(order, orderStatusHistory);
    }

    private void validateBuyerOwnership(CancelOrderCommand command, Order order) {
        if (!order.getBuyerId().equals(command.buyerId())) {
            log.warn("주문 소유자 불일치 - orderId: {}, 주문소유자: {}, 요청자: {}",
                    command.id(), order.getBuyerId(), command.buyerId());
            throw new UnauthorizedOrderAccessException();
        }
    }

    private void validateStatusTransition(Order order) {
        if (TARGET_STATUS.isMe(order.getOrderStatus())) {
            throw new OrderStatusAlreadyChangedException(TARGET_STATUS);
        }

        if (!order.getOrderStatus().canTransitionTo(TARGET_STATUS)) {
            throw new InvalidOrderStatusTransitionException(order.getOrderStatus(), TARGET_STATUS);
        }
    }
}
