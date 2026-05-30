package com.personal.marketnote.commerce.service.order;

import com.personal.marketnote.commerce.domain.order.*;
import com.personal.marketnote.commerce.exception.InvalidOrderStatusTransitionException;
import com.personal.marketnote.commerce.port.in.command.order.CompleteCancelOrderCommand;
import com.personal.marketnote.commerce.port.in.usecase.order.CompleteCancelOrderUseCase;
import com.personal.marketnote.commerce.port.in.usecase.order.GetOrderUseCase;
import com.personal.marketnote.commerce.port.out.event.PublishOrderEventPort;
import com.personal.marketnote.commerce.port.out.order.UpdateOrderPort;
import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.common.utility.FormatValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

@UseCase
@RequiredArgsConstructor
@Slf4j
public class CompleteCancelOrderService implements CompleteCancelOrderUseCase {

    private final GetOrderUseCase getOrderUseCase;
    private final UpdateOrderPort updateOrderPort;
    private final PublishOrderEventPort publishOrderEventPort;
    private final Clock clock;

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void completeCancellation(CompleteCancelOrderCommand command) {
        Order order = getOrderUseCase.getOrder(command.orderId());

        validateCancelRequestedStatus(order);

        LocalDateTime now = LocalDateTime.now(clock);
        order.changeAllProductsStatus(OrderStatus.CANCELLED, now);

        OrderStatusReasonCategory reasonCategory = resolveReasonCategory(command.reasonCategory());

        OrderStatusHistory orderStatusHistory = OrderStatusHistory.from(
                OrderStatusHistoryCreateState.builder()
                        .orderId(command.orderId())
                        .orderStatus(OrderStatus.CANCELLED)
                        .reasonCategory(reasonCategory)
                        .reason(command.reason())
                        .build()
        );

        updateOrderPort.update(order, orderStatusHistory);

        publishOrderEventPort.publishOrderCancelledEvent(
                command.orderId(),
                command.orderKey(),
                command.buyerId(),
                command.cancelAmount(),
                command.paymentAmount(),
                command.pointAmount(),
                command.shippingFee(),
                command.isFullCancel(),
                command.alreadyRefunded(),
                order.getOrderProducts(),
                order.getOrderProducts()
        );

        log.info("주문 취소 완료 처리. orderId={}, orderKey={}", command.orderId(), command.orderKey());
    }

    private void validateCancelRequestedStatus(Order order) {
        if (!order.getOrderStatus().isCancelRequested()) {
            throw new InvalidOrderStatusTransitionException(order.getOrderStatus(), OrderStatus.CANCELLED);
        }
    }

    private OrderStatusReasonCategory resolveReasonCategory(String reasonCategoryStr) {
        if (FormatValidator.hasNoValue(reasonCategoryStr)) {
            return null;
        }
        return OrderStatusReasonCategory.valueOf(reasonCategoryStr);
    }
}
