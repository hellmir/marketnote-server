package com.personal.marketnote.commerce.service.order;

import com.personal.marketnote.commerce.domain.order.Order;
import com.personal.marketnote.commerce.domain.order.OrderAmount;
import com.personal.marketnote.commerce.domain.order.OrderStatus;
import com.personal.marketnote.commerce.domain.order.OrderStatusHistory;
import com.personal.marketnote.commerce.domain.order.OrderStatusHistoryCreateState;
import com.personal.marketnote.commerce.domain.order.OrderStatusReasonCategory;
import com.personal.marketnote.commerce.exception.InvalidOrderStatusTransitionException;
import com.personal.marketnote.commerce.exception.InvalidReasonCategoryException;
import com.personal.marketnote.commerce.exception.OrderCancellationNotAllowedException;
import com.personal.marketnote.commerce.exception.OrderStatusAlreadyChangedException;
import com.personal.marketnote.commerce.exception.UnauthorizedOrderAccessException;
import com.personal.marketnote.commerce.port.in.command.order.CancelOrderCommand;
import com.personal.marketnote.commerce.port.in.usecase.order.CancelOrderUseCase;
import com.personal.marketnote.commerce.port.in.usecase.order.GetOrderUseCase;
import com.personal.marketnote.commerce.port.out.event.PublishOrderEventPort;
import com.personal.marketnote.commerce.port.out.fulfillment.CancelFulfillmentReleasePort;
import com.personal.marketnote.commerce.port.out.fulfillment.CancelFulfillmentReleaseResult;
import com.personal.marketnote.commerce.port.out.order.UpdateOrderPort;
import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.common.exception.FulfillmentServiceRequestFailedException;
import com.personal.marketnote.common.utility.FormatValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.time.LocalDateTime;

@UseCase
@RequiredArgsConstructor
@Slf4j
public class CancelOrderService implements CancelOrderUseCase {
    private final GetOrderUseCase getOrderUseCase;
    private final UpdateOrderPort updateOrderPort;
    private final CancelFulfillmentReleasePort cancelFulfillmentReleasePort;
    private final PublishOrderEventPort publishOrderEventPort;
    private final PlatformTransactionManager transactionManager;
    private final Clock clock;

    @Override
    public void cancelOrder(CancelOrderCommand command) {
        Order order = getOrderUseCase.getOrder(command.id());

        validateBuyerOwnership(command, order);
        validateReasonCategory(command);

        OrderStatus targetStatus = resolveTargetStatus(order);
        validateStatusTransition(order, targetStatus);

        OrderStatus originalStatus = order.getOrderStatus();

        cancelFulfillmentIfRequired(order);

        persistCancellation(command, order, originalStatus, targetStatus);
    }

    private OrderStatus resolveTargetStatus(Order order) {
        if (order.getOrderStatus().isPending()) {
            return OrderStatus.CANCELLED;
        }
        return OrderStatus.CANCEL_REQUESTED;
    }

    private void cancelFulfillmentIfRequired(Order order) {
        if (!order.getOrderStatus().requiresFulfillmentCancellation()) {
            return;
        }

        try {
            CancelFulfillmentReleaseResult result = cancelFulfillmentReleasePort.cancelRelease(order.getId());
            if (!result.cancelled()) {
                log.warn("풀필먼트 출고 취소 거부 - orderId: {}, message: {}", order.getId(), result.message());
                throw new OrderCancellationNotAllowedException(order.getId());
            }
        } catch (FulfillmentServiceRequestFailedException e) {
            log.error("풀필먼트 서비스 통신 실패로 주문 취소 거부 - orderId: {}", order.getId(), e);
            throw new OrderCancellationNotAllowedException(order.getId());
        }
    }

    private void persistCancellation(CancelOrderCommand command, Order order, OrderStatus originalStatus, OrderStatus targetStatus) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        transactionTemplate.executeWithoutResult(status -> {
            LocalDateTime now = LocalDateTime.now(clock);
            order.changeAllProductsStatus(targetStatus, now);

            OrderStatusHistory orderStatusHistory = OrderStatusHistory.from(
                    OrderStatusHistoryCreateState.builder()
                            .orderId(command.id())
                            .orderStatus(targetStatus)
                            .reasonCategory(command.reasonCategory())
                            .reason(command.reason())
                            .build()
            );

            updateOrderPort.update(order, orderStatusHistory);

            if (originalStatus.requiresPaymentRefund()) {
                publishOrderCancelledEvent(order);
            }
        });
    }

    private void publishOrderCancelledEvent(Order order) {
        OrderAmount amount = order.getAmount();
        publishOrderEventPort.publishOrderCancelledEvent(
                order.getId(),
                order.getOrderKey().toString(),
                order.getBuyerId(),
                amount.getPaidAmount(),
                amount.getPaidAmount(),
                amount.getPointAmount(),
                amount.getShippingFee(),
                true,
                0L,
                order.getOrderProducts(),
                order.getOrderProducts()
        );
    }

    private void validateReasonCategory(CancelOrderCommand command) {
        OrderStatusReasonCategory reasonCategory = command.reasonCategory();
        if (FormatValidator.hasNoValue(reasonCategory)) {
            return;
        }

        if (!reasonCategory.isCancelReason()) {
            throw new InvalidReasonCategoryException(reasonCategory);
        }
    }

    private void validateBuyerOwnership(CancelOrderCommand command, Order order) {
        if (!order.isBuyer(command.buyerId())) {
            log.warn("주문 소유자 불일치 - orderId: {}, 요청자: {}",
                    command.id(), command.buyerId());
            throw new UnauthorizedOrderAccessException();
        }
    }

    private void validateStatusTransition(Order order, OrderStatus targetStatus) {
        if (targetStatus.isMe(order.getOrderStatus())) {
            throw new OrderStatusAlreadyChangedException(targetStatus);
        }

        if (!order.getOrderStatus().canTransitionTo(targetStatus)) {
            throw new InvalidOrderStatusTransitionException(order.getOrderStatus(), targetStatus);
        }
    }
}
