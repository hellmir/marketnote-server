package com.personal.marketnote.commerce.service.order;

import com.personal.marketnote.commerce.domain.order.*;
import com.personal.marketnote.commerce.exception.*;
import com.personal.marketnote.commerce.port.in.command.order.CancelOrderCommand;
import com.personal.marketnote.commerce.port.in.command.saga.OrderCancelSagaContext;
import com.personal.marketnote.commerce.port.in.command.saga.OrderPaymentSagaContext.OrderProductItem;
import com.personal.marketnote.commerce.port.in.usecase.order.CancelOrderUseCase;
import com.personal.marketnote.commerce.port.in.usecase.order.GetOrderUseCase;
import com.personal.marketnote.commerce.port.out.order.UpdateOrderPort;
import com.personal.marketnote.commerce.service.saga.OrderCancelSagaStarter;
import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.common.utility.FormatValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@UseCase
@RequiredArgsConstructor
@Slf4j
public class CancelOrderService implements CancelOrderUseCase {
    private final GetOrderUseCase getOrderUseCase;
    private final UpdateOrderPort updateOrderPort;
    private final OrderCancelSagaStarter orderCancelSagaStarter;

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
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

        if (originalStatus.requiresPaymentRefund()) {
            cancelOrderViaSaga(command, order, originalStatus);
            return;
        }

        persistFullStatusChange(command, order, targetStatus);
    }

    private void handlePartialCancel(CancelOrderCommand command, Order order, List<Long> pricePolicyIds) {
        OrderStatus originalStatus = order.getOrderStatus();

        if (requiresPaymentRefundForPartialCancel(originalStatus)) {
            partialCancelViaSaga(command, order, originalStatus, pricePolicyIds);
            return;
        }

        persistPartialStatusChange(command, order, pricePolicyIds, OrderStatus.CANCELLED);
    }

    private void cancelOrderViaSaga(CancelOrderCommand command, Order order, OrderStatus originalStatus) {
        persistFullStatusChange(command, order, OrderStatus.CANCEL_REQUESTED);

        OrderAmount amount = order.getAmount();

        List<OrderProductItem> orderProductItems = mapToOrderProductItems(order.getOrderProducts());

        OrderCancelSagaContext context = new OrderCancelSagaContext(
                order.getId(),
                order.getOrderKey().toString(),
                order.getBuyerId(),
                amount.getPaidAmount().getValue(),
                amount.getPaidAmount().getValue(),
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

        orderCancelSagaStarter.start(context);
    }

    private void partialCancelViaSaga(
            CancelOrderCommand command, Order order,
            OrderStatus originalStatus, List<Long> pricePolicyIds
    ) {
        persistPartialStatusChange(command, order, pricePolicyIds, OrderStatus.CANCEL_REQUESTED);

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
                amount.getPaidAmount().getValue(),
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

        orderCancelSagaStarter.start(context);
    }

    private void persistFullStatusChange(CancelOrderCommand command, Order order, OrderStatus targetStatus) {
        order.changeAllProductsStatus(targetStatus, null, 0L);

        OrderStatusHistory history = OrderStatusHistory.from(
                OrderStatusHistoryCreateState.builder()
                        .orderId(command.id())
                        .orderStatus(targetStatus)
                        .reasonCategory(command.reasonCategory())
                        .reason(command.reason())
                        .build()
        );

        updateOrderPort.update(order, history);
    }

    private void persistPartialStatusChange(
            CancelOrderCommand command, Order order,
            List<Long> pricePolicyIds, OrderStatus targetProductStatus
    ) {
        order.changeProductsStatus(pricePolicyIds, targetProductStatus, null, 0L);

        OrderStatusHistory history = OrderStatusHistory.from(
                OrderStatusHistoryCreateState.builder()
                        .orderId(command.id())
                        .orderStatus(order.getOrderStatus())
                        .reasonCategory(command.reasonCategory())
                        .reason(command.reason())
                        .build()
        );

        updateOrderPort.update(order, history);
    }

    private OrderStatus resolveTargetStatus(Order order) {
        if (order.getOrderStatus().isPending()) {
            return OrderStatus.CANCELLED;
        }
        return OrderStatus.CANCEL_REQUESTED;
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
