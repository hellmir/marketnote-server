package com.personal.marketnote.commerce.service.returntracker;

import com.personal.marketnote.commerce.domain.order.*;
import com.personal.marketnote.commerce.exception.InvalidOrderStatusTransitionException;
import com.personal.marketnote.commerce.port.in.command.order.CalculateReturnShippingFeeCommand;
import com.personal.marketnote.commerce.port.in.command.returntracker.CompleteReturnCommand;
import com.personal.marketnote.commerce.port.in.result.order.CalculateReturnShippingFeeResult;
import com.personal.marketnote.commerce.port.in.usecase.order.CalculateReturnShippingFeeUseCase;
import com.personal.marketnote.commerce.port.in.usecase.order.GetOrderUseCase;
import com.personal.marketnote.commerce.port.in.usecase.returntracker.CompleteReturnUseCase;
import com.personal.marketnote.commerce.port.out.event.PublishOrderEventPort;
import com.personal.marketnote.commerce.port.out.order.UpdateOrderPort;
import com.personal.marketnote.common.application.UseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class CompleteReturnService implements CompleteReturnUseCase {

    private final GetOrderUseCase getOrderUseCase;
    private final UpdateOrderPort updateOrderPort;
    private final PublishOrderEventPort publishOrderEventPort;
    private final CalculateReturnShippingFeeUseCase calculateReturnShippingFeeUseCase;
    private final Clock clock;

    @Override
    @Transactional(isolation = READ_COMMITTED)
    public void completeReturn(CompleteReturnCommand command) {
        Order order = getOrderUseCase.getOrder(command.orderId());

        if (order.getOrderStatus().isReturned()) {
            log.info("이미 반품 완료 처리된 주문 (멱등). orderId={}", command.orderId());
            return;
        }

        validateReturnableStatus(order);

        List<Long> returnPricePolicyIds = collectReturnTargetPricePolicyIds(order);

        LocalDateTime now = LocalDateTime.now(clock);
        transitionToReturned(order, returnPricePolicyIds, now);

        OrderStatusHistory orderStatusHistory = createReturnedHistory(command.orderId(), order);
        updateOrderPort.update(order, orderStatusHistory);

        long returnShippingFee = calculateReturnShippingFee(order, returnPricePolicyIds);
        boolean isFullReturn = resolveIsFullReturn(order);

        List<OrderProduct> returnProducts = filterReturnedProducts(order, returnPricePolicyIds);
        publishReturnedEvent(order, returnProducts, returnShippingFee, isFullReturn);

        log.info("반품 완료 처리. orderId={}, isFullReturn={}", command.orderId(), isFullReturn);
    }

    private void validateReturnableStatus(Order order) {
        if (!order.getOrderStatus().isReturnRequested()
                && !order.getOrderStatus().canTransitionTo(OrderStatus.RETURNED)) {
            throw new InvalidOrderStatusTransitionException(order.getOrderStatus(), OrderStatus.RETURNED);
        }
    }

    private List<Long> collectReturnTargetPricePolicyIds(Order order) {
        return order.getOrderProducts().stream()
                .filter(p -> p.getOrderStatus().isReturnRequested()
                        || p.getOrderStatus().canTransitionTo(OrderStatus.RETURNED))
                .map(OrderProduct::getPricePolicyId)
                .toList();
    }

    private void transitionToReturned(Order order, List<Long> returnPricePolicyIds, LocalDateTime now) {
        order.changeProductsStatus(returnPricePolicyIds, OrderStatus.RETURN_IN_PROGRESS, now);
        order.changeProductsStatus(returnPricePolicyIds, OrderStatus.RETURNED, now);
    }

    private OrderStatusHistory createReturnedHistory(Long orderId, Order order) {
        return OrderStatusHistory.from(
                OrderStatusHistoryCreateState.builder()
                        .orderId(orderId)
                        .orderStatus(order.getOrderStatus())
                        .reasonCategory(order.getStatusChangeReasonCategory())
                        .build()
        );
    }

    private long calculateReturnShippingFee(Order order, List<Long> returnPricePolicyIds) {
        CalculateReturnShippingFeeCommand feeCommand = CalculateReturnShippingFeeCommand.builder()
                .orderId(order.getId())
                .reasonCategory(order.getStatusChangeReasonCategory())
                .returnPricePolicyIds(returnPricePolicyIds)
                .build();

        CalculateReturnShippingFeeResult feeResult =
                calculateReturnShippingFeeUseCase.calculateReturnShippingFee(feeCommand);

        return feeResult.returnShippingFee();
    }

    private boolean resolveIsFullReturn(Order order) {
        return order.getOrderProducts().stream()
                .allMatch(product -> product.getOrderStatus().isReturned());
    }

    private List<OrderProduct> filterReturnedProducts(Order order, List<Long> returnPricePolicyIds) {
        return order.getOrderProducts().stream()
                .filter(p -> returnPricePolicyIds.contains(p.getPricePolicyId()))
                .toList();
    }

    private void publishReturnedEvent(
            Order order, List<OrderProduct> returnProducts,
            long returnShippingFee, boolean isFullReturn
    ) {
        OrderAmount amount = order.getAmount();
        long returnAmount = returnProducts.stream()
                .mapToLong(product -> product.getUnitAmount().multiply(product.getQuantity().getValue()).getValue())
                .reduce(0L, Math::addExact);

        publishOrderEventPort.publishOrderReturnedEvent(
                order.getId(),
                order.getOrderKey().toString(),
                order.getBuyerId(),
                returnAmount,
                amount.getPaidAmount(),
                amount.getPointAmount().getValue(),
                amount.getShippingFee().getValue(),
                isFullReturn,
                returnShippingFee,
                returnProducts
        );
    }
}
