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
import com.personal.marketnote.commerce.port.in.usecase.order.ChangeOrderStatusUseCase;
import com.personal.marketnote.commerce.port.in.usecase.order.GetOrderUseCase;
import com.personal.marketnote.commerce.port.out.event.PublishOrderEventPort;
import com.personal.marketnote.commerce.port.out.order.UpdateOrderPort;
import com.personal.marketnote.commerce.port.out.product.FindProductByPricePolicyPort;
import com.personal.marketnote.commerce.port.out.result.product.ProductInfoResult;
import com.personal.marketnote.commerce.port.out.reward.ModifyUserPointPort;
import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.common.utility.FormatValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED)
@Slf4j
public class ChangeOrderStatusService implements ChangeOrderStatusUseCase {
    private final GetOrderUseCase getOrderUseCase;
    private final UpdateOrderPort updateOrderPort;
    private final FindProductByPricePolicyPort findProductByPricePolicyPort;
    private final ModifyUserPointPort modifyUserPointPort;
    private final PublishOrderEventPort publishOrderEventPort;
    private final Clock clock;

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
        applyPickupAddressIfRefundRequested(command, order);
        OrderStatusHistory orderStatusHistory = OrderStatusHistory.from(OrderCommandToStateMapper.mapToState(command));
        updateOrderPort.update(order, orderStatusHistory);

        if (status.isPaid()) {
            updatePaymentSubsequentProcesses(order);
        }

        if (status.isConfirmed() && !command.isPartialProductChange()) {
            updateConfirmSubsequentProcesses(order);
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
        LocalDateTime now = LocalDateTime.now(clock);

        if (command.isPartialProductChange()) {
            order.changeProductsStatus(command.pricePolicyIds(), status, now);
            return;
        }

        order.changeAllProductsStatus(status, now);
    }

    private void applyPickupAddressIfRefundRequested(ChangeOrderStatusCommand command, Order order) {
        if (!command.orderStatus().isRefundRequested()) {
            return;
        }

        order.applyPickupAddress(command.pickupAddress());
    }

    private void updatePaymentSubsequentProcesses(Order order) {
        // [#929] 재고 차감(#932), 장바구니 삭제(#1018), 공유 포인트 적립(#1019), 포인트 차감(#1020)은 Kafka Consumer로 전환 완료
        // [미전환] 상품 적립 포인트 (#1131)

        List<Long> pricePolicyIds = order.getOrderProducts()
                .stream()
                .map(OrderProduct::getPricePolicyId)
                .toList();
        Long orderId = order.getId();
        Long buyerId = order.getBuyerId();
        Long totalAccumulatedPoint = calculateTotalAccumulatedPoint(order.getOrderProducts(), pricePolicyIds);

        // Outbox 이벤트 저장 (트랜잭션 내)
        publishOrderPaymentCompletedEvent(order, totalAccumulatedPoint);

        runAfterCommit(() -> {
            // 상품 적립 포인트를 적립 예정 포인트로 추가
            addPendingProductAccumulationPoints(buyerId, totalAccumulatedPoint, orderId);
        });
    }

    private Long calculateTotalAccumulatedPoint(List<OrderProduct> orderProducts, List<Long> pricePolicyIds) {
        Map<Long, ProductInfoResult> productInfoMap = findProductByPricePolicyPort.findByPricePolicyIds(pricePolicyIds);

        long totalAccumulatedPoint = 0L;
        for (OrderProduct orderProduct : orderProducts) {
            ProductInfoResult productInfo = productInfoMap.get(orderProduct.getPricePolicyId());
            if (FormatValidator.hasNoValue(productInfo)) {
                log.warn("상품 정보 조회 누락 - pricePolicyId: {}", orderProduct.getPricePolicyId());
                continue;
            }
            if (FormatValidator.hasNoValue(productInfo.accumulatedPoint())) {
                continue;
            }
            totalAccumulatedPoint = Math.addExact(totalAccumulatedPoint,
                    Math.multiplyExact(productInfo.accumulatedPoint(), orderProduct.getQuantity()));
        }

        return totalAccumulatedPoint;
    }

    private void addPendingProductAccumulationPoints(Long buyerId, Long totalAccumulatedPoint, Long orderId) {
        if (FormatValidator.hasNoValue(totalAccumulatedPoint) || totalAccumulatedPoint <= 0) {
            return;
        }

        try {
            modifyUserPointPort.addPendingProductAccumulationPoints(
                    buyerId,
                    totalAccumulatedPoint,
                    orderId
            );
        } catch (Exception e) {
            log.error("상품 적립 예정 포인트 추가 실패 - orderId: {}, buyerId: {}, amount: {}, error: {}",
                    orderId, buyerId, totalAccumulatedPoint, e.getMessage(), e);
        }
    }

    private void updateConfirmSubsequentProcesses(Order order) {
        Long orderId = order.getId();
        Long buyerId = order.getBuyerId();
        List<Long> sharerIds = extractSharerIds(order.getOrderProducts());

        // Outbox 이벤트 저장 (트랜잭션 내)
        // [#929][#1190] 적립 예정 포인트 확정은 Kafka Consumer로 전환 완료
        publishOrderPurchaseConfirmedEvent(orderId, buyerId, sharerIds);
    }

    private void publishOrderPurchaseConfirmedEvent(Long orderId, Long buyerId, List<Long> sharerIds) {
        try {
            publishOrderEventPort.publishOrderPurchaseConfirmedEvent(orderId, buyerId, sharerIds);
        } catch (Exception e) {
            log.error("구매 확정 이벤트 발행 실패 - orderId: {}, buyerId: {}, error: {}",
                    orderId, buyerId, e.getMessage(), e);
        }
    }

    private void publishOrderPaymentCompletedEvent(Order order, Long totalAccumulatedPoint) {
        try {
            publishOrderEventPort.publishOrderPaymentCompletedEvent(
                    order.getId(),
                    order.getBuyerId(),
                    order.getAmount().getTotalAmount(),
                    order.getAmount().getPointAmount(),
                    order.getOrderProducts(),
                    totalAccumulatedPoint
            );
        } catch (Exception e) {
            log.error("주문 결제 완료 이벤트 발행 실패 - orderId: {}, error: {}",
                    order.getId(), e.getMessage(), e);
        }
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
