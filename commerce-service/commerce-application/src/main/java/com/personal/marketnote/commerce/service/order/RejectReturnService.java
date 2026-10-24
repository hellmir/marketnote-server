package com.personal.marketnote.commerce.service.order;

import com.personal.marketnote.commerce.domain.order.Order;
import com.personal.marketnote.commerce.domain.order.OrderProduct;
import com.personal.marketnote.commerce.domain.order.OrderStatus;
import com.personal.marketnote.commerce.domain.order.OrderStatusHistory;
import com.personal.marketnote.commerce.domain.order.OrderStatusHistoryCreateState;
import com.personal.marketnote.commerce.domain.order.InvalidOrderStatusTransitionException;
import com.personal.marketnote.commerce.exception.OrderStatusAlreadyChangedException;
import com.personal.marketnote.commerce.port.in.command.order.RejectReturnCommand;
import com.personal.marketnote.commerce.port.in.usecase.order.GetOrderUseCase;
import com.personal.marketnote.commerce.port.in.usecase.order.RejectReturnUseCase;
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
@Transactional(isolation = READ_COMMITTED)
public class RejectReturnService implements RejectReturnUseCase {
    private final GetOrderUseCase getOrderUseCase;
    private final UpdateOrderPort updateOrderPort;
    private final PublishOrderEventPort publishOrderEventPort;
    private final Clock clock;

    private static final OrderStatus TARGET_STATUS = OrderStatus.RETURN_REJECTED;

    @Override
    public void rejectReturn(RejectReturnCommand command) {
        Order order = getOrderUseCase.getOrder(command.id());

        validateStatusTransition(order);

        LocalDateTime now = LocalDateTime.now(clock);
        List<Long> returnRequestedPricePolicyIds = collectReturnRequestedPricePolicyIds(order);
        if (returnRequestedPricePolicyIds.isEmpty()) {
            log.warn("반품 요청 상태 상품이 없어 반품 불가 전이를 생략합니다. orderId={}", command.id());
            return;
        }
        order.changeProductsStatus(returnRequestedPricePolicyIds, TARGET_STATUS, now, 0L);

        OrderStatusHistory orderStatusHistory = OrderStatusHistory.from(
                OrderStatusHistoryCreateState.builder()
                        .orderId(command.id())
                        .orderStatus(order.getOrderStatus())
                        .reasonCategory(command.reasonCategory())
                        .reason(command.reason())
                        .build()
        );

        updateOrderPort.update(order, orderStatusHistory);
        publishOrderEventPort.publishReturnRejectedEvent(order.getId(), order.getBuyerId());
    }

    private void validateStatusTransition(Order order) {
        if (TARGET_STATUS.isMe(order.getOrderStatus())) {
            throw new OrderStatusAlreadyChangedException(TARGET_STATUS);
        }

        if (!order.getOrderStatus().canTransitionTo(TARGET_STATUS)) {
            throw new InvalidOrderStatusTransitionException(order.getOrderStatus(), TARGET_STATUS);
        }
    }

    private List<Long> collectReturnRequestedPricePolicyIds(Order order) {
        return order.getOrderProducts().stream()
                .filter(op -> op.getOrderStatus().isReturnRequested())
                .map(OrderProduct::getPricePolicyId)
                .toList();
    }
}
