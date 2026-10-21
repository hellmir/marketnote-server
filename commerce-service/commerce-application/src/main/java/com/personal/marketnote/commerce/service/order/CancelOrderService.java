package com.personal.marketnote.commerce.service.order;

import com.personal.marketnote.commerce.domain.order.*;
import com.personal.marketnote.commerce.exception.*;
import com.personal.marketnote.commerce.port.in.command.order.CancelOrderCommand;
import com.personal.marketnote.commerce.port.in.command.saga.OrderCancelSagaContext;
import com.personal.marketnote.commerce.port.in.command.saga.OrderPaymentSagaContext.OrderProductItem;
import com.personal.marketnote.commerce.port.in.usecase.order.CancelOrderUseCase;
import com.personal.marketnote.commerce.port.in.usecase.order.GetOrderUseCase;
import com.personal.marketnote.commerce.port.out.event.PublishOrderEventPort;
import com.personal.marketnote.commerce.port.out.fulfillment.CancelFulfillmentReleasePort;
import com.personal.marketnote.commerce.port.out.fulfillment.CancelFulfillmentReleaseResult;
import com.personal.marketnote.commerce.port.out.order.UpdateOrderPort;
import com.personal.marketnote.commerce.service.saga.OrderCancelSagaStarter;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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
    private final Optional<OrderCancelSagaStarter> orderCancelSagaStarter;

    @Override
    public void cancelOrder(CancelOrderCommand command) {
        Order order = getOrderUseCase.getOrder(command.id());

        validateBuyerOwnership(command, order);
        validateReasonCategory(command);

        List<Long> pricePolicyIds = resolvePricePolicyIds(command, order);

        if (isPartialCancel(pricePolicyIds)) {
            handlePartialCancel(command, order, pricePolicyIds);
            return;
        }

        handleFullCancel(command, order);
    }

    private List<Long> resolvePricePolicyIds(CancelOrderCommand command, Order order) {
        if (FormatValidator.hasNoValue(command.pricePolicyIds()) || command.pricePolicyIds().isEmpty()) {
            return List.of();
        }
        validatePartialCancelAllowed(order);
        validateCancelProducts(command.pricePolicyIds(), order);

        if (coversAllRemainingProducts(command.pricePolicyIds(), order)) {
            return List.of();
        }
        return command.pricePolicyIds();
    }

    private boolean isPartialCancel(List<Long> pricePolicyIds) {
        return !pricePolicyIds.isEmpty();
    }

    private void handleFullCancel(CancelOrderCommand command, Order order) {
        OrderStatus targetStatus = resolveTargetStatus(order);
        validateStatusTransition(order, targetStatus);

        OrderStatus originalStatus = order.getOrderStatus();

        if (useSagaMode() && originalStatus.requiresPaymentRefund()) {
            cancelOrderViaSaga(command, order, originalStatus);
            return;
        }

        cancelOrderSync(command, order, originalStatus, targetStatus);
    }

    private void handlePartialCancel(CancelOrderCommand command, Order order, List<Long> pricePolicyIds) {
        OrderStatus originalStatus = order.getOrderStatus();

        if (useSagaMode() && requiresPaymentRefundForPartialCancel(originalStatus)) {
            partialCancelViaSaga(command, order, originalStatus, pricePolicyIds);
            return;
        }

        partialCancelSync(command, order, originalStatus, pricePolicyIds);
    }

    private boolean useSagaMode() {
        return orderCancelSagaStarter.isPresent();
    }

    private void cancelOrderViaSaga(CancelOrderCommand command, Order order, OrderStatus originalStatus) {
        persistStatusChange(command, order, OrderStatus.CANCEL_REQUESTED);

        OrderAmount amount = order.getAmount();

        List<OrderProductItem> orderProductItems = mapToOrderProductItems(order.getOrderProducts());

        OrderCancelSagaContext context = new OrderCancelSagaContext(
                order.getId(),
                order.getOrderKey().toString(),
                order.getBuyerId(),
                amount.getPaidAmount(),
                amount.getPaidAmount(),
                amount.getPointAmount().getValue(),
                amount.getShippingFee().getValue(),
                true,
                0L,
                originalStatus.name(),
                resolveReasonCategoryName(command.reasonCategory()),
                command.reason(),
                orderProductItems,
                orderProductItems,
                List.of(),
                UUID.randomUUID().toString()
        );

        orderCancelSagaStarter.get().start(context);
    }

    private void partialCancelViaSaga(
            CancelOrderCommand command, Order order,
            OrderStatus originalStatus, List<Long> pricePolicyIds
    ) {
        persistCancelRequestedForTargetProducts(command, order, pricePolicyIds);

        OrderAmount amount = order.getAmount();

        List<OrderProductItem> allProductItems = mapToOrderProductItems(order.getOrderProducts());
        List<OrderProductItem> cancelProductItems = order.getOrderProducts().stream()
                .filter(op -> pricePolicyIds.contains(op.getPricePolicyId()))
                .map(op -> new OrderProductItem(
                        op.getPricePolicyId(), op.getProductId(), op.getProductKey(), op.getSharerKey(),
                        op.getQuantity().getValue(), op.getUnitAmount().getValue()
                ))
                .toList();

        long cancelAmount = calculateCancelAmount(order, pricePolicyIds);
        List<String> productKeys = extractProductKeys(cancelProductItems);

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
                cancelProductItems,
                productKeys,
                UUID.randomUUID().toString()
        );

        orderCancelSagaStarter.get().start(context);
    }

    private void cancelOrderSync(
            CancelOrderCommand command, Order order,
            OrderStatus originalStatus, OrderStatus targetStatus
    ) {
        cancelFulfillmentIfRequired(order, List.of());
        persistStatusChange(command, order, targetStatus);

        if (originalStatus.requiresPaymentRefund()) {
            publishOrderCancelledEvent(order);
        }
    }

    private void partialCancelSync(
            CancelOrderCommand command, Order order,
            OrderStatus originalStatus, List<Long> pricePolicyIds
    ) {
        List<String> productKeys = order.getOrderProducts().stream()
                .filter(op -> pricePolicyIds.contains(op.getPricePolicyId()))
                .map(OrderProduct::getProductKey)
                .filter(FormatValidator::hasValue)
                .map(Object::toString)
                .toList();

        cancelFulfillmentIfRequired(order, productKeys);

        persistPartialCancelStatusChange(command, order, pricePolicyIds);

        if (requiresPaymentRefundForPartialCancel(originalStatus)) {
            publishPartialCancelEvent(order, pricePolicyIds);
        }
    }

    private void persistStatusChange(CancelOrderCommand command, Order order, OrderStatus targetStatus) {
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
        });
    }

    private void persistCancelRequestedForTargetProducts(
            CancelOrderCommand command, Order order, List<Long> pricePolicyIds
    ) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        transactionTemplate.executeWithoutResult(status -> {
            LocalDateTime now = LocalDateTime.now(clock);
            order.changeProductsStatus(pricePolicyIds, OrderStatus.CANCEL_REQUESTED, now);

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

    private void persistPartialCancelStatusChange(
            CancelOrderCommand command, Order order, List<Long> pricePolicyIds
    ) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        transactionTemplate.executeWithoutResult(status -> {
            LocalDateTime now = LocalDateTime.now(clock);
            order.changeProductsStatus(pricePolicyIds, OrderStatus.CANCELLED, now);

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

    private OrderStatus resolveTargetStatus(Order order) {
        if (order.getOrderStatus().isPending()) {
            return OrderStatus.CANCELLED;
        }
        return OrderStatus.CANCEL_REQUESTED;
    }

    private void cancelFulfillmentIfRequired(Order order, List<String> productKeys) {
        if (!order.getOrderStatus().requiresFulfillmentCancellation()) {
            return;
        }

        try {
            CancelFulfillmentReleaseResult result = cancelFulfillmentReleasePort.cancelRelease(order.getId(), productKeys);
            if (!result.cancelled()) {
                log.warn("풀필먼트 출고 취소 거부 - orderId: {}, message: {}", order.getId(), result.message());
                publishOrderEventPort.publishOrderCancelFailedEvent(order.getId(), order.getBuyerId());
                throw new OrderCancellationNotAllowedException(order.getId());
            }
        } catch (FulfillmentServiceRequestFailedException e) {
            log.error("풀필먼트 서비스 통신 실패로 주문 취소 거부 - orderId: {}", order.getId(), e);
            publishOrderEventPort.publishOrderCancelFailedEvent(order.getId(), order.getBuyerId());
            throw new OrderCancellationNotAllowedException(order.getId());
        }
    }

    private void publishOrderCancelledEvent(Order order) {
        OrderAmount amount = order.getAmount();
        publishOrderEventPort.publishOrderCancelledEvent(
                order.getId(),
                order.getOrderKey().toString(),
                order.getBuyerId(),
                amount.getPaidAmount(),
                amount.getPaidAmount(),
                amount.getPointAmount().getValue(),
                amount.getShippingFee().getValue(),
                true,
                0L,
                order.getOrderProducts(),
                order.getOrderProducts(),
                null
        );
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
                cancelProducts,
                UUID.randomUUID().toString()
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
            log.warn("주문 소유자 불일치 - orderId: {}, 요청자: {}", command.id(), command.buyerId());
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

    private void validatePartialCancelAllowed(Order order) {
        if (!order.getOrderStatus().canPartiallyCancel()) {
            throw new PartialCancelNotAllowedException(order.getOrderStatus());
        }
    }

    private void validateCancelProducts(List<Long> cancelPricePolicyIds, Order order) {
        Set<Long> uniqueCancelIds = new HashSet<>(cancelPricePolicyIds);
        if (uniqueCancelIds.size() != cancelPricePolicyIds.size()) {
            throw new InvalidPartialCancelProductException("취소 대상 상품에 중복된 pricePolicyId가 포함되어 있습니다.");
        }

        Map<Long, OrderProduct> orderProductByPolicyId = order.getOrderProducts().stream()
                .collect(Collectors.toMap(OrderProduct::getPricePolicyId, op -> op));

        for (Long cancelPricePolicyId : cancelPricePolicyIds) {
            OrderProduct orderProduct = orderProductByPolicyId.get(cancelPricePolicyId);
            if (FormatValidator.hasNoValue(orderProduct)) {
                throw new InvalidPartialCancelProductException("취소 대상에 주문에 포함되지 않은 상품이 있습니다.");
            }
            if (orderProduct.isCancelled()) {
                throw new InvalidPartialCancelProductException("이미 취소된 상품이 취소 대상에 포함되어 있습니다.");
            }
        }
    }

    private boolean requiresPaymentRefundForPartialCancel(OrderStatus orderStatus) {
        return orderStatus.requiresPaymentRefund() || orderStatus.isPartiallyCancelled();
    }

    private boolean coversAllRemainingProducts(List<Long> pricePolicyIds, Order order) {
        List<Long> nonCancelledPricePolicyIds = order.getOrderProducts().stream()
                .filter(op -> !op.isCancelled())
                .map(OrderProduct::getPricePolicyId)
                .toList();

        return pricePolicyIds.containsAll(nonCancelledPricePolicyIds);
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
                        op.getPricePolicyId(), op.getProductId(), op.getProductKey(), op.getSharerKey(),
                        op.getQuantity().getValue(), op.getUnitAmount().getValue()
                ))
                .toList();
    }

    private List<String> extractProductKeys(List<OrderProductItem> items) {
        return items.stream()
                .map(OrderProductItem::productKey)
                .filter(FormatValidator::hasValue)
                .map(Object::toString)
                .toList();
    }

    private String resolveReasonCategoryName(OrderStatusReasonCategory reasonCategory) {
        if (FormatValidator.hasNoValue(reasonCategory)) {
            return null;
        }
        return reasonCategory.name();
    }
}
