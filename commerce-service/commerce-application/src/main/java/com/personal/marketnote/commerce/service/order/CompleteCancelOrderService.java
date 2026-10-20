package com.personal.marketnote.commerce.service.order;

import com.personal.marketnote.commerce.domain.order.*;
import com.personal.marketnote.commerce.exception.InvalidOrderStatusTransitionException;
import com.personal.marketnote.commerce.port.in.command.order.CompleteCancelOrderCommand;
import com.personal.marketnote.commerce.port.in.command.order.CompleteCancelOrderCommand.CancelProductItemCommand;
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
import java.util.List;
import java.util.UUID;

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
        applyCancelStatusToProducts(command, order, now);

        OrderStatusReasonCategory reasonCategory = resolveReasonCategory(command.reasonCategory());

        OrderStatusHistory orderStatusHistory = OrderStatusHistory.from(
                OrderStatusHistoryCreateState.builder()
                        .orderId(command.orderId())
                        .orderStatus(order.getOrderStatus())
                        .reasonCategory(reasonCategory)
                        .reason(command.reason())
                        .build()
        );

        updateOrderPort.update(order, orderStatusHistory);

        List<OrderProduct> cancelProducts = mapToOrderProducts(command.cancelProducts());

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
                cancelProducts,
                resolveCancelId(command.isFullCancel())
        );

        log.info("주문 취소 완료 처리. orderId={}, orderKey={}, isFullCancel={}",
                command.orderId(), command.orderKey(), command.isFullCancel()
        );
    }

    private void validateCancelRequestedStatus(Order order) {
        if (!order.getOrderStatus().isCancelRequested()) {
            throw new InvalidOrderStatusTransitionException(order.getOrderStatus(), OrderStatus.CANCELLED);
        }
    }

    private void applyCancelStatusToProducts(CompleteCancelOrderCommand command, Order order, LocalDateTime now) {
        if (command.isFullCancel()) {
            order.changeAllProductsStatus(OrderStatus.CANCELLED, now);
            return;
        }
        List<Long> cancelPricePolicyIds = extractCancelPricePolicyIds(command.cancelProducts());
        order.changeProductsStatus(cancelPricePolicyIds, OrderStatus.CANCELLED, now);
    }

    private List<Long> extractCancelPricePolicyIds(List<CancelProductItemCommand> items) {
        if (FormatValidator.hasNoValue(items)) {
            return List.of();
        }
        return items.stream()
                .map(CancelProductItemCommand::pricePolicyId)
                .toList();
    }

    private List<OrderProduct> mapToOrderProducts(List<CancelProductItemCommand> items) {
        if (FormatValidator.hasNoValue(items)) {
            return List.of();
        }
        return items.stream()
                .map(item -> OrderProduct.from(
                        OrderProductSnapshotState.builder()
                                .pricePolicyId(item.pricePolicyId())
                                .sharerKey(item.sharerKey())
                                .quantity(item.quantity())
                                .unitAmount(item.unitAmount())
                                .accumulatedPoint(0L)
                                .build()
                ))
                .toList();
    }

    private OrderStatusReasonCategory resolveReasonCategory(String reasonCategoryStr) {
        if (FormatValidator.hasNoValue(reasonCategoryStr)) {
            return null;
        }
        return OrderStatusReasonCategory.valueOf(reasonCategoryStr);
    }

    private String resolveCancelId(boolean isFullCancel) {
        if (isFullCancel) {
            return null;
        }
        return UUID.randomUUID().toString();
    }
}
