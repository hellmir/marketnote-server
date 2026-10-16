package com.personal.marketnote.commerce.service.order;

import com.personal.marketnote.commerce.domain.order.*;
import com.personal.marketnote.commerce.exception.*;
import com.personal.marketnote.commerce.port.in.command.order.CancelOrderCommand;
import com.personal.marketnote.commerce.port.in.command.order.PartialCancelOrderCommand;
import com.personal.marketnote.commerce.port.in.command.saga.OrderCancelSagaContext;
import com.personal.marketnote.commerce.port.in.command.saga.OrderPaymentSagaContext.OrderProductItem;
import com.personal.marketnote.commerce.port.in.usecase.order.CancelOrderUseCase;
import com.personal.marketnote.commerce.port.in.usecase.order.GetOrderUseCase;
import com.personal.marketnote.commerce.port.in.usecase.order.PartialCancelOrderUseCase;
import com.personal.marketnote.commerce.port.out.event.PublishOrderEventPort;
import com.personal.marketnote.commerce.port.out.fulfillment.CancelFulfillmentReleasePort;
import com.personal.marketnote.commerce.port.out.fulfillment.CancelFulfillmentReleaseResult;
import com.personal.marketnote.commerce.port.out.order.UpdateOrderPort;
import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.common.exception.FulfillmentServiceRequestFailedException;
import com.personal.marketnote.common.saga.SagaDefinition;
import com.personal.marketnote.common.saga.SagaOrchestrator;
import com.personal.marketnote.common.utility.FormatValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@UseCase
@RequiredArgsConstructor
@Slf4j
public class PartialCancelOrderService implements PartialCancelOrderUseCase {
    private final GetOrderUseCase getOrderUseCase;
    private final CancelOrderUseCase cancelOrderUseCase;
    private final UpdateOrderPort updateOrderPort;
    private final CancelFulfillmentReleasePort cancelFulfillmentReleasePort;
    private final PublishOrderEventPort publishOrderEventPort;
    private final PlatformTransactionManager transactionManager;
    private final Clock clock;
    private final Optional<SagaOrchestrator> sagaOrchestrator;
    private final Optional<SagaDefinition<OrderCancelSagaContext>> orderCancelSagaDefinition;

    @Override
    public void partialCancelOrder(PartialCancelOrderCommand command) {
        Order order = getOrderUseCase.getOrder(command.id());

        validateBuyerOwnership(command, order);
        validateReasonCategory(command);
        validatePartialCancelAllowed(order);
        validateCancelProducts(command, order);

        if (shouldDelegateToFullCancel(command, order)) {
            delegateToFullCancel(command);
            return;
        }

        OrderStatus originalStatus = order.getOrderStatus();

        if (useSagaMode() && requiresPaymentRefundForPartialCancel(originalStatus)) {
            partialCancelViaSaga(command, order, originalStatus);
            return;
        }

        partialCancelSync(command, order, originalStatus);
    }

    private boolean useSagaMode() {
        return sagaOrchestrator.isPresent() && orderCancelSagaDefinition.isPresent();
    }

    private void partialCancelViaSaga(PartialCancelOrderCommand command, Order order, OrderStatus originalStatus) {
        persistCancelRequestedForTargetProducts(command, order);

        OrderAmount amount = order.getAmount();
        String sagaId = "ORDER_CANCEL:" + order.getId();

        List<OrderProductItem> allProductItems = mapToOrderProductItems(order.getOrderProducts());
        List<OrderProductItem> cancelProductItems = order.getOrderProducts().stream()
                .filter(op -> command.cancelPricePolicyIds().contains(op.getPricePolicyId()))
                .map(op -> new OrderProductItem(
                        op.getPricePolicyId(), op.getProductId(), op.getSharerKey(),
                        op.getQuantity().getValue(), op.getUnitAmount().getValue()
                ))
                .toList();

        long cancelAmount = calculateCancelAmount(order, command.cancelPricePolicyIds());

        OrderCancelSagaContext context = new OrderCancelSagaContext(
                order.getId(),
                order.getOrderKey().toString(),
                order.getBuyerId(),
                cancelAmount,
                amount.getPaidAmount(),
                amount.getPointAmount().getValue(),
                amount.getShippingFee().getValue(),
                false,
                0L,
                originalStatus.name(),
                resolveReasonCategoryName(command.reasonCategory()),
                command.reason(),
                allProductItems,
                cancelProductItems
        );

        sagaOrchestrator.get().start(orderCancelSagaDefinition.get(), sagaId, context);

        log.info("주문 부분 취소 SAGA 시작. orderId={}, sagaId={}, cancelPricePolicyIds={}",
                order.getId(), sagaId, command.cancelPricePolicyIds()
        );
    }

    private void partialCancelSync(PartialCancelOrderCommand command, Order order, OrderStatus originalStatus) {
        cancelFulfillmentIfRequired(order);

        persistPartialCancelStatusChange(command, order);

        if (requiresPaymentRefundForPartialCancel(originalStatus)) {
            publishPartialCancelEvent(order, command.cancelPricePolicyIds());
        }
    }

    private void persistCancelRequestedForTargetProducts(PartialCancelOrderCommand command, Order order) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        transactionTemplate.executeWithoutResult(status -> {
            LocalDateTime now = LocalDateTime.now(clock);
            order.changeProductsStatus(command.cancelPricePolicyIds(), OrderStatus.CANCEL_REQUESTED, now);

            OrderStatusHistory orderStatusHistory = OrderStatusHistory.from(
                    OrderStatusHistoryCreateState.builder()
                            .orderId(command.id())
                            .orderStatus(OrderStatus.CANCEL_REQUESTED)
                            .reasonCategory(command.reasonCategory())
                            .reason(command.reason())
                            .build()
            );

            updateOrderPort.update(order, orderStatusHistory);
        });
    }

    private void persistPartialCancelStatusChange(PartialCancelOrderCommand command, Order order) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        transactionTemplate.executeWithoutResult(status -> {
            LocalDateTime now = LocalDateTime.now(clock);
            order.changeProductsStatus(command.cancelPricePolicyIds(), OrderStatus.CANCELLED, now);

            OrderStatusHistory orderStatusHistory = OrderStatusHistory.from(
                    OrderStatusHistoryCreateState.builder()
                            .orderId(command.id())
                            .orderStatus(order.getOrderStatus())
                            .reasonCategory(command.reasonCategory())
                            .reason(command.reason())
                            .build()
            );

            updateOrderPort.update(order, orderStatusHistory);
        });
    }

    private void cancelFulfillmentIfRequired(Order order) {
        if (!order.getOrderStatus().requiresFulfillmentCancellation()) {
            return;
        }

        try {
            CancelFulfillmentReleaseResult result = cancelFulfillmentReleasePort.cancelRelease(order.getId());
            if (!result.cancelled()) {
                log.warn("풀필먼트 출고 취소 거부 - orderId: {}, message: {}", order.getId(), result.message());
                publishOrderEventPort.publishOrderCancelFailedEvent(order.getId(), order.getBuyerId());
                throw new OrderCancellationNotAllowedException(order.getId());
            }
        } catch (FulfillmentServiceRequestFailedException e) {
            log.error("풀필먼트 서비스 통신 실패로 주문 부분 취소 거부 - orderId: {}", order.getId(), e);
            publishOrderEventPort.publishOrderCancelFailedEvent(order.getId(), order.getBuyerId());
            throw new OrderCancellationNotAllowedException(order.getId());
        }
    }

    private void publishPartialCancelEvent(Order order, List<Long> cancelPricePolicyIds) {
        OrderAmount amount = order.getAmount();
        long cancelAmount = calculateCancelAmount(order, cancelPricePolicyIds);

        List<OrderProduct> cancelProducts = order.getOrderProducts().stream()
                .filter(op -> cancelPricePolicyIds.contains(op.getPricePolicyId()))
                .toList();

        publishOrderEventPort.publishOrderCancelledEvent(
                order.getId(),
                order.getOrderKey().toString(),
                order.getBuyerId(),
                cancelAmount,
                amount.getPaidAmount(),
                amount.getPointAmount().getValue(),
                amount.getShippingFee().getValue(),
                false,
                0L,
                order.getOrderProducts(),
                cancelProducts
        );
    }

    private void validateBuyerOwnership(PartialCancelOrderCommand command, Order order) {
        if (!order.isBuyer(command.buyerId())) {
            log.warn("주문 소유자 불일치 - orderId: {}, 요청자: {}", command.id(), command.buyerId());
            throw new UnauthorizedOrderAccessException();
        }
    }

    private void validateReasonCategory(PartialCancelOrderCommand command) {
        OrderStatusReasonCategory reasonCategory = command.reasonCategory();
        if (FormatValidator.hasNoValue(reasonCategory)) {
            return;
        }

        if (!reasonCategory.isCancelReason()) {
            throw new InvalidReasonCategoryException(reasonCategory);
        }
    }

    private boolean requiresPaymentRefundForPartialCancel(OrderStatus orderStatus) {
        return orderStatus.requiresPaymentRefund() || orderStatus.isPartiallyCancelled();
    }

    private void validatePartialCancelAllowed(Order order) {
        if (!order.getOrderStatus().canPartiallyCancel()) {
            throw new PartialCancelNotAllowedException(order.getOrderStatus());
        }
    }

    private void validateCancelProducts(PartialCancelOrderCommand command, Order order) {
        List<Long> cancelPricePolicyIds = command.cancelPricePolicyIds();

        if (FormatValidator.hasNoValue(cancelPricePolicyIds) || cancelPricePolicyIds.isEmpty()) {
            throw new InvalidPartialCancelProductException("취소 대상 상품이 지정되지 않았습니다.");
        }

        Set<Long> uniqueCancelIds = new HashSet<>(cancelPricePolicyIds);
        if (uniqueCancelIds.size() != cancelPricePolicyIds.size()) {
            throw new InvalidPartialCancelProductException("취소 대상 상품에 중복된 pricePolicyId가 포함되어 있습니다.");
        }

        Set<Long> orderPricePolicyIds = order.getOrderProducts().stream()
                .map(OrderProduct::getPricePolicyId)
                .collect(Collectors.toSet());

        for (Long cancelPricePolicyId : cancelPricePolicyIds) {
            if (!orderPricePolicyIds.contains(cancelPricePolicyId)) {
                throw new InvalidPartialCancelProductException("취소 대상에 주문에 포함되지 않은 상품이 있습니다.");
            }
        }

        for (Long cancelPricePolicyId : cancelPricePolicyIds) {
            boolean alreadyCancelled = order.getOrderProducts().stream()
                    .filter(op -> op.getPricePolicyId().equals(cancelPricePolicyId))
                    .anyMatch(op -> op.getOrderStatus().isCancelled());

            if (alreadyCancelled) {
                throw new InvalidPartialCancelProductException("이미 취소된 상품이 취소 대상에 포함되어 있습니다.");
            }
        }
    }

    private boolean shouldDelegateToFullCancel(PartialCancelOrderCommand command, Order order) {
        List<Long> nonCancelledPricePolicyIds = order.getOrderProducts().stream()
                .filter(op -> !op.getOrderStatus().isCancelled())
                .map(OrderProduct::getPricePolicyId)
                .toList();

        return command.cancelPricePolicyIds().containsAll(nonCancelledPricePolicyIds);
    }

    private void delegateToFullCancel(PartialCancelOrderCommand command) {
        CancelOrderCommand cancelOrderCommand = CancelOrderCommand.builder()
                .id(command.id())
                .reasonCategory(command.reasonCategory())
                .reason(command.reason())
                .buyerId(command.buyerId())
                .build();

        cancelOrderUseCase.cancelOrder(cancelOrderCommand);
    }

    private long calculateCancelAmount(Order order, List<Long> cancelPricePolicyIds) {
        long cancelAmount = 0L;
        for (OrderProduct orderProduct : order.getOrderProducts()) {
            if (cancelPricePolicyIds.contains(orderProduct.getPricePolicyId())) {
                cancelAmount = Math.addExact(
                        cancelAmount,
                        Math.multiplyExact(orderProduct.getUnitAmount().getValue(), orderProduct.getQuantity().getValue())
                );
            }
        }
        return cancelAmount;
    }

    private List<OrderProductItem> mapToOrderProductItems(List<OrderProduct> orderProducts) {
        return orderProducts.stream()
                .map(op -> new OrderProductItem(
                        op.getPricePolicyId(), op.getProductId(), op.getSharerKey(),
                        op.getQuantity().getValue(), op.getUnitAmount().getValue()
                ))
                .toList();
    }

    private String resolveReasonCategoryName(OrderStatusReasonCategory reasonCategory) {
        if (FormatValidator.hasNoValue(reasonCategory)) {
            return null;
        }
        return reasonCategory.name();
    }
}
