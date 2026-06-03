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

        LocalDateTime now = LocalDateTime.now(clock);
        transitionToReturned(order, now);

        OrderStatusHistory orderStatusHistory = createReturnedHistory(command.orderId(), order);
        updateOrderPort.update(order, orderStatusHistory);

        long returnShippingFee = calculateReturnShippingFee(order);
        boolean isFullReturn = resolveIsFullReturn(order);

        publishReturnedEvent(order, returnShippingFee, isFullReturn);

        log.info("반품 완료 처리. orderId={}", command.orderId());
    }

    private void validateReturnableStatus(Order order) {
        if (!order.getOrderStatus().isReturnRequested()
                && !order.getOrderStatus().canTransitionTo(OrderStatus.RETURNED)) {
            throw new InvalidOrderStatusTransitionException(order.getOrderStatus(), OrderStatus.RETURNED);
        }
    }

    private void transitionToReturned(Order order, LocalDateTime now) {
        if (order.getOrderStatus().isReturnRequested()) {
            order.changeAllProductsStatus(OrderStatus.RETURN_IN_PROGRESS, now);
        }
        order.changeAllProductsStatus(OrderStatus.RETURNED, now);
    }

    private OrderStatusHistory createReturnedHistory(Long orderId, Order order) {
        return OrderStatusHistory.from(
                OrderStatusHistoryCreateState.builder()
                        .orderId(orderId)
                        .orderStatus(OrderStatus.RETURNED)
                        .reasonCategory(order.getStatusChangeReasonCategory())
                        .build()
        );
    }

    private long calculateReturnShippingFee(Order order) {
        List<Long> pricePolicyIds = order.getOrderProducts().stream()
                .map(OrderProduct::getPricePolicyId)
                .toList();

        CalculateReturnShippingFeeCommand feeCommand = CalculateReturnShippingFeeCommand.builder()
                .orderId(order.getId())
                .reasonCategory(order.getStatusChangeReasonCategory())
                .returnPricePolicyIds(pricePolicyIds)
                .build();

        CalculateReturnShippingFeeResult feeResult =
                calculateReturnShippingFeeUseCase.calculateReturnShippingFee(feeCommand);

        return feeResult.returnShippingFee();
    }

    private boolean resolveIsFullReturn(Order order) {
        return order.getOrderProducts().stream()
                .allMatch(product -> product.getOrderStatus().isReturned());
    }

    private void publishReturnedEvent(Order order, long returnShippingFee, boolean isFullReturn) {
        OrderAmount amount = order.getAmount();
        long returnAmount = order.getOrderProducts().stream()
                .mapToLong(product -> Math.multiplyExact(product.getUnitAmount(), product.getQuantity().longValue()))
                .reduce(0L, Math::addExact);

        publishOrderEventPort.publishOrderReturnedEvent(
                order.getId(),
                order.getOrderKey().toString(),
                order.getBuyerId(),
                returnAmount,
                amount.getPaidAmount(),
                amount.getPointAmount(),
                amount.getShippingFee(),
                isFullReturn,
                returnShippingFee,
                order.getOrderProducts()
        );
    }
}
