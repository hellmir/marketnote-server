package com.personal.marketnote.commerce.service.order;

import com.personal.marketnote.commerce.domain.order.Order;
import com.personal.marketnote.commerce.domain.order.OrderProduct;
import com.personal.marketnote.commerce.domain.order.OrderStatus;
import com.personal.marketnote.commerce.domain.order.OrderStatusHistory;
import com.personal.marketnote.commerce.exception.InvalidOrderStatusTransitionException;
import com.personal.marketnote.commerce.exception.OrderStatusAlreadyChangedException;
import com.personal.marketnote.commerce.exception.UnauthorizedOrderAccessException;
import com.personal.marketnote.commerce.exception.UnauthorizedOrderStatusChangeException;
import com.personal.marketnote.commerce.mapper.OrderCommandToStateMapper;
import com.personal.marketnote.commerce.port.in.command.order.ChangeOrderStatusCommand;
import com.personal.marketnote.commerce.port.in.usecase.inventory.ReduceProductInventoryUseCase;
import com.personal.marketnote.commerce.port.in.usecase.order.ChangeOrderStatusUseCase;
import com.personal.marketnote.commerce.port.in.usecase.order.GetOrderUseCase;
import com.personal.marketnote.commerce.port.out.order.DeleteOrderedCartProductsPort;
import com.personal.marketnote.commerce.port.out.order.UpdateOrderPort;
import com.personal.marketnote.commerce.port.out.reward.ModifyUserPointPort;
import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.common.utility.FormatValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.Objects;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED)
@Slf4j
public class ChangeOrderStatusService implements ChangeOrderStatusUseCase {
    private final ReduceProductInventoryUseCase reduceProductInventoryUseCase;
    private final GetOrderUseCase getOrderUseCase;
    private final UpdateOrderPort updateOrderPort;
    private final DeleteOrderedCartProductsPort deleteOrderedCartProductsPort;
    private final ModifyUserPointPort modifyUserPointPort;

    @Override
    public void changeOrderStatus(ChangeOrderStatusCommand command) {
        Order order = getOrderUseCase.getOrder(command.id());
        OrderStatus status = command.orderStatus();

        validateBuyerRoleRestriction(command);
        validateBuyerOwnership(command, order);

        if (status.isMe(order.getOrderStatus()) && status.isNotPartialChanged()) {
            throw new OrderStatusAlreadyChangedException(status);
        }

        if (!command.isPartialProductChange() && !order.getOrderStatus().canTransitionTo(status)) {
            throw new InvalidOrderStatusTransitionException(order.getOrderStatus(), status);
        }

        changeOrderStatus(command, order);
        OrderStatusHistory orderStatusHistory = OrderStatusHistory.from(OrderCommandToStateMapper.mapToState(command));
        updateOrderPort.update(order, orderStatusHistory);

        // FIXME: Payment Service의 Kafka 이벤트 Consumption으로 변경(주문 상태 PAID로 변경 / 결제 금액 업데이트 / 재고 감소 / 장바구니 상품 삭제)
        if (status.isPaid()) {
            // 결제 완료 시 재고 차감
            reduceProductInventoryUseCase.reduce(order.getOrderProducts(), status.getDescription());

            List<Long> pricePolicyIds = order.getOrderProducts()
                    .stream()
                    .map(OrderProduct::getPricePolicyId)
                    .toList();
            List<Long> sharerIds = extractSharerIds(order.getOrderProducts());
            Long orderId = order.getId();
            Long buyerId = order.getBuyerId();
            Long pointAmount = order.getPointAmount();

            runAfterCommit(() -> {
                // 결제 완료 시 장바구니 상품 삭제
                deleteOrderedCartProductsPort.delete(pricePolicyIds);

                // 링크 공유 회원 포인트 적립
                modifyUserPointPort.accrueSharedPurchasePoints(sharerIds);

                // 포인트 사용 시 차감
                if (FormatValidator.hasValue(pointAmount) && pointAmount > 0) {
                    try {
                        modifyUserPointPort.deductOrderPoints(buyerId, pointAmount, orderId);
                    } catch (Exception e) {
                        log.error("주문 포인트 차감 실패 - orderId: {}, buyerId: {}, pointAmount: {}, error: {}",
                                orderId, buyerId, pointAmount, e.getMessage(), e);
                    }
                }
            });
        }
    }

    private void validateBuyerRoleRestriction(ChangeOrderStatusCommand command) {
        if (command.isBuyerRole() && !command.orderStatus().isBuyerAllowed()) {
            log.warn("구매자 허용되지 않은 상태 변경 시도 - orderId: {}, targetStatus: {}, role: {}",
                    command.id(), command.orderStatus(), command.role());
            throw new UnauthorizedOrderStatusChangeException();
        }
    }

    private void validateBuyerOwnership(ChangeOrderStatusCommand command, Order order) {
        if (command.isInternalCall()) {
            return;
        }

        if (!command.isBuyerRole()) {
            return;
        }

        if (!order.getBuyerId().equals(command.buyerId())) {
            log.warn("주문 소유자 불일치 - orderId: {}, 주문소유자: {}, 요청자: {}",
                    command.id(), order.getBuyerId(), command.buyerId());
            throw new UnauthorizedOrderAccessException();
        }
    }

    private void changeOrderStatus(ChangeOrderStatusCommand command, Order order) {
        OrderStatus status = command.orderStatus();

        if (command.isPartialProductChange()) {
            order.changeProductsStatus(command.pricePolicyIds(), status);
            return;
        }

        order.changeAllProductsStatus(status);
    }

    private List<Long> extractSharerIds(List<OrderProduct> orderProducts) {
        return orderProducts.stream()
                .map(OrderProduct::getSharerId)
                .filter(Objects::nonNull)
                .toList();
    }

    private void runAfterCommit(Runnable action) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    action.run();
                }
            });
            return;
        }

        action.run();
    }
}
