package com.personal.marketnote.commerce.service.order;

import com.personal.marketnote.commerce.domain.order.Order;
import com.personal.marketnote.commerce.domain.order.OrderProduct;
import com.personal.marketnote.commerce.domain.order.OrderStatus;
import com.personal.marketnote.commerce.domain.order.OrderStatusHistory;
import com.personal.marketnote.commerce.exception.OrderStatusAlreadyChangedException;
import com.personal.marketnote.commerce.mapper.OrderCommandToStateMapper;
import com.personal.marketnote.commerce.port.in.command.order.ChangeOrderStatusCommand;
import com.personal.marketnote.commerce.port.in.usecase.inventory.ReduceProductInventoryUseCase;
import com.personal.marketnote.commerce.port.in.usecase.order.ChangeOrderStatusUseCase;
import com.personal.marketnote.commerce.port.in.usecase.order.GetOrderUseCase;
import com.personal.marketnote.commerce.port.out.order.DeleteOrderedCartProductsPort;
import com.personal.marketnote.commerce.port.out.order.UpdateOrderPort;
import com.personal.marketnote.commerce.port.out.reward.ModifyUserPointPort;
import com.personal.marketnote.common.application.UseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.Objects;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED)
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

        if (status.isMe(order.getOrderStatus()) && status.isNotPartialChanged()) {
            throw new OrderStatusAlreadyChangedException(status);
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

            runAfterCommit(() -> {
                // 결제 완료 시 장바구니 상품 삭제
                deleteOrderedCartProductsPort.delete(pricePolicyIds);

                // 링크 공유 회원 포인트 적립
                modifyUserPointPort.accrueSharedPurchasePoints(sharerIds);
            });
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
