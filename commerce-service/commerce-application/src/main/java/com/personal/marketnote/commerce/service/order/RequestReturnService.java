package com.personal.marketnote.commerce.service.order;

import com.personal.marketnote.commerce.domain.order.*;
import com.personal.marketnote.commerce.exception.InvalidOrderStatusTransitionException;
import com.personal.marketnote.commerce.exception.InvalidReasonCategoryException;
import com.personal.marketnote.commerce.exception.OrderStatusAlreadyChangedException;
import com.personal.marketnote.commerce.exception.UnauthorizedOrderAccessException;
import com.personal.marketnote.commerce.port.in.command.order.RequestReturnCommand;
import com.personal.marketnote.commerce.port.in.usecase.order.GetOrderUseCase;
import com.personal.marketnote.commerce.port.in.usecase.order.RequestReturnUseCase;
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

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED)
@Slf4j
public class RequestReturnService implements RequestReturnUseCase {
    private final GetOrderUseCase getOrderUseCase;
    private final UpdateOrderPort updateOrderPort;
    private final CreateReturnTrackerAfterReturnRequestService createReturnTrackerAfterReturnRequestService;
    private final Clock clock;

    private static final DateTimeFormatter ORDER_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private static final OrderStatus TARGET_STATUS = OrderStatus.RETURN_REQUESTED;

    @Override
    public void requestReturn(RequestReturnCommand command) {
        Order order = getOrderUseCase.getOrder(command.id());

        validateBuyerOwnership(command, order);
        validateReasonCategory(command);
        validateStatusTransition(order);

        LocalDateTime now = LocalDateTime.now(clock);
        order.changeAllProductsStatus(TARGET_STATUS, now);
        order.applyPickupAddress(buildPickupAddress(command));

        OrderStatusHistory orderStatusHistory = OrderStatusHistory.from(
                OrderStatusHistoryCreateState.builder()
                        .orderId(command.id())
                        .orderStatus(TARGET_STATUS)
                        .reasonCategory(command.reasonCategory())
                        .reason(command.reason())
                        .build()
        );

        updateOrderPort.update(order, orderStatusHistory);

        RegisterFulfillmentReturnDeliveryCommand returnDeliveryCommand = buildReturnDeliveryCommand(command, order);
        runAfterCommit(() -> createReturnTrackerAfterReturnRequestService.createReturnTracker(returnDeliveryCommand));
    }

    private void validateBuyerOwnership(RequestReturnCommand command, Order order) {
        if (!order.getBuyerId().equals(command.buyerId())) {
            log.warn("주문 소유자 불일치 - orderId: {}, 주문소유자: {}, 요청자: {}",
                    command.id(), order.getBuyerId(), command.buyerId());
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

    private void validateStatusTransition(Order order) {
        if (TARGET_STATUS.isMe(order.getOrderStatus())) {
            throw new OrderStatusAlreadyChangedException(TARGET_STATUS);
        }

        if (!order.getOrderStatus().canTransitionTo(TARGET_STATUS)) {
            throw new InvalidOrderStatusTransitionException(order.getOrderStatus(), TARGET_STATUS);
        }
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

        List<RegisterFulfillmentReturnDeliveryCommand.ProductItem> products = order.getOrderProducts().stream()
                .map(op -> RegisterFulfillmentReturnDeliveryCommand.ProductItem.of(
                        String.valueOf(op.getPricePolicyId()),
                        op.getQuantity()
                ))
                .toList();

        String reasonCategory = FormatValidator.hasValue(command.reasonCategory())
                ? command.reasonCategory().getDescription()
                : "";

        return RegisterFulfillmentReturnDeliveryCommand.builder()
                .orderId(command.id())
                .orderDate(orderDate)
                .recipientName(shippingAddress.getRecipientName())
                .recipientPhoneNumber(shippingAddress.getRecipientPhoneNumber())
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
