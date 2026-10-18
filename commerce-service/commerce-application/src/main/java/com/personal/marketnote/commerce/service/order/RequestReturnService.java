package com.personal.marketnote.commerce.service.order;

import com.personal.marketnote.commerce.domain.order.*;
import com.personal.marketnote.commerce.exception.InvalidOrderStatusTransitionException;
import com.personal.marketnote.commerce.exception.InvalidReasonCategoryException;
import com.personal.marketnote.commerce.exception.InvalidReturnTargetPricePolicyException;
import com.personal.marketnote.commerce.exception.OrderStatusAlreadyChangedException;
import com.personal.marketnote.commerce.exception.UnauthorizedOrderAccessException;
import com.personal.marketnote.commerce.port.in.command.order.RequestReturnCommand;
import com.personal.marketnote.commerce.port.in.usecase.order.GetOrderUseCase;
import com.personal.marketnote.commerce.port.in.usecase.order.RequestReturnUseCase;
import com.personal.marketnote.commerce.port.out.event.PublishOrderEventPort;
import com.personal.marketnote.commerce.port.out.fulfillment.RegisterFulfillmentReturnDeliveryCommand;
import com.personal.marketnote.commerce.port.out.order.UpdateOrderPort;
import com.personal.marketnote.commerce.service.returntracker.CreateReturnTrackerAfterReturnRequestService;
import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.common.utility.FormatValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED)
@Slf4j
public class RequestReturnService implements RequestReturnUseCase {
    private final GetOrderUseCase getOrderUseCase;
    private final UpdateOrderPort updateOrderPort;
    private final PublishOrderEventPort publishOrderEventPort;
    private final CreateReturnTrackerAfterReturnRequestService createReturnTrackerAfterReturnRequestService;
    private final Clock clock;

    private static final DateTimeFormatter ORDER_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private static final OrderStatus TARGET_STATUS = OrderStatus.RETURN_REQUESTED;

    @Override
    public void requestReturn(RequestReturnCommand command) {
        Order order = getOrderUseCase.getOrder(command.id());

        validateBuyerOwnership(command, order);
        validateReasonCategory(command);
        validateReturnTargetPricePolicyIds(command, order);
        validateStatusTransition(command, order);

        LocalDateTime now = LocalDateTime.now(clock);
        transitionOrderStatus(command, order, now);
        order.applyPickupAddress(buildPickupAddress(command));

        OrderStatusHistory orderStatusHistory = OrderStatusHistory.from(
                OrderStatusHistoryCreateState.builder()
                        .orderId(command.id())
                        .orderStatus(order.getOrderStatus())
                        .reasonCategory(command.reasonCategory())
                        .reason(command.reason())
                        .build()
        );

        updateOrderPort.update(order, orderStatusHistory);
        publishOrderEventPort.publishReturnRequestedEvent(order.getId(), order.getBuyerId());

        RegisterFulfillmentReturnDeliveryCommand returnDeliveryCommand = buildReturnDeliveryCommand(command, order);
        runAfterCommit(() -> createReturnTrackerAfterReturnRequestService.createReturnTracker(returnDeliveryCommand));
    }

    private void validateBuyerOwnership(RequestReturnCommand command, Order order) {
        if (!order.getBuyerId().equals(command.buyerId())) {
            log.warn("주문 소유자 불일치 - orderId: {}, 주문소유자: {}, 요청자: {}",
                    command.id(), order.getBuyerId(), command.buyerId()
            );
            throw new UnauthorizedOrderAccessException();
        }
    }

    private void validateReasonCategory(RequestReturnCommand command) {
        OrderStatusReasonCategory reasonCategory = command.reasonCategory();
        if (FormatValidator.hasNoValue(reasonCategory)) {
            return;
        }

        if (!reasonCategory.isReturnReason()) {
            throw new InvalidReasonCategoryException(reasonCategory);
        }
    }

    private void validateReturnTargetPricePolicyIds(RequestReturnCommand command, Order order) {
        if (!command.isPartialReturn()) {
            return;
        }

        Set<Long> orderPricePolicyIds = order.getOrderProducts().stream()
                .map(OrderProduct::getPricePolicyId)
                .collect(Collectors.toSet());

        List<Long> invalidIds = command.pricePolicyIds().stream()
                .filter(id -> !orderPricePolicyIds.contains(id))
                .toList();

        if (!invalidIds.isEmpty()) {
            throw new InvalidReturnTargetPricePolicyException(invalidIds);
        }
    }

    private void validateStatusTransition(RequestReturnCommand command, Order order) {
        OrderStatus currentStatus = order.getOrderStatus();

        if (!command.isPartialReturn() && TARGET_STATUS.isMe(currentStatus)) {
            throw new OrderStatusAlreadyChangedException(TARGET_STATUS);
        }

        if (command.isPartialReturn()) {
            validatePartialReturnTransition(command, order, currentStatus);
            return;
        }

        if (!currentStatus.canTransitionTo(TARGET_STATUS)) {
            throw new InvalidOrderStatusTransitionException(currentStatus, TARGET_STATUS);
        }
    }

    private void validatePartialReturnTransition(
            RequestReturnCommand command,
            Order order,
            OrderStatus currentStatus
    ) {
        boolean allTargetsAlreadyRequested = order.getOrderProducts().stream()
                .filter(op -> command.pricePolicyIds().contains(op.getPricePolicyId()))
                .allMatch(op -> op.getOrderStatus().isReturnRequested());
        if (allTargetsAlreadyRequested) {
            throw new OrderStatusAlreadyChangedException(TARGET_STATUS);
        }

        if (!currentStatus.canTransitionTo(OrderStatus.PARTIALLY_RETURN_REQUESTED)
                && !currentStatus.canTransitionTo(TARGET_STATUS)) {
            throw new InvalidOrderStatusTransitionException(currentStatus, OrderStatus.PARTIALLY_RETURN_REQUESTED);
        }
    }

    private void transitionOrderStatus(RequestReturnCommand command, Order order, LocalDateTime now) {
        if (command.isPartialReturn()) {
            order.changeProductsStatus(command.pricePolicyIds(), TARGET_STATUS, now);
            return;
        }
        order.changeAllProductsStatus(TARGET_STATUS, now);
    }

    private ShippingAddress buildPickupAddress(RequestReturnCommand command) {
        return ShippingAddress.of(
                command.pickupRecipientName(),
                command.pickupRecipientPhoneNumber(),
                command.pickupZipCode(),
                command.pickupAddress(),
                command.pickupAddressDetail(),
                null,
                command.pickupRequestMessage()
        );
    }

    private RegisterFulfillmentReturnDeliveryCommand buildReturnDeliveryCommand(
            RequestReturnCommand command,
            Order order
    ) {
        ShippingAddress shippingAddress = order.getShippingAddress();
        String orderDate = FormatValidator.hasValue(order.getCreatedAt())
                ? order.getCreatedAt().format(ORDER_DATE_FORMATTER)
                : "";

        String fullAddress = buildFullAddress(shippingAddress);

        List<RegisterFulfillmentReturnDeliveryCommand.ProductItem> products = collectReturnDeliveryProducts(command, order);

        String reasonCategory = FormatValidator.hasValue(command.reasonCategory())
                ? command.reasonCategory().getDescription()
                : "";

        return RegisterFulfillmentReturnDeliveryCommand.builder()
                .orderId(command.id())
                .orderDate(orderDate)
                .recipientName(shippingAddress.getRecipientName().getValue())
                .recipientPhoneNumber(shippingAddress.getRecipientPhoneNumber().getValue())
                .recipientAddress(fullAddress)
                .pickupRecipientName(command.pickupRecipientName())
                .pickupRecipientPhoneNumber(command.pickupRecipientPhoneNumber())
                .pickupZipCode(command.pickupZipCode())
                .pickupAddress(command.pickupAddress())
                .pickupAddressDetail(command.pickupAddressDetail())
                .returnReason(reasonCategory)
                .returnDetailReason(command.reason())
                .returnShippingRequest(command.pickupRequestMessage())
                .products(products)
                .build();
    }

    private List<RegisterFulfillmentReturnDeliveryCommand.ProductItem> collectReturnDeliveryProducts(
            RequestReturnCommand command,
            Order order
    ) {
        return order.getOrderProducts().stream()
                .filter(op -> !command.isPartialReturn() || command.pricePolicyIds().contains(op.getPricePolicyId()))
                .map(this::toProductItem)
                .filter(FormatValidator::hasValue)
                .toList();
    }

    private RegisterFulfillmentReturnDeliveryCommand.ProductItem toProductItem(OrderProduct orderProduct) {
        UUID productKey = orderProduct.getProductKey();
        if (FormatValidator.hasNoValue(productKey)) {
            log.warn("OrderProduct productKey 누락으로 Fassto 회수 품목에서 제외 - pricePolicyId: {}",
                    orderProduct.getPricePolicyId()
            );
            return null;
        }
        return RegisterFulfillmentReturnDeliveryCommand.ProductItem.of(
                productKey.toString(),
                orderProduct.getQuantity().getValue()
        );
    }

    private String buildFullAddress(ShippingAddress shippingAddress) {
        String address = FormatValidator.hasValue(shippingAddress.getAddress())
                ? shippingAddress.getAddress()
                : "";
        String detail = FormatValidator.hasValue(shippingAddress.getAddressDetail())
                ? " " + shippingAddress.getAddressDetail()
                : "";
        return address + detail;
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
