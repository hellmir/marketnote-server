package com.personal.marketnote.commerce.service.order;

import com.personal.marketnote.commerce.domain.inventory.Inventory;
import com.personal.marketnote.commerce.domain.order.Order;
import com.personal.marketnote.commerce.domain.order.OrderCreateState;
import com.personal.marketnote.commerce.domain.order.OrderProductCreateState;
import com.personal.marketnote.commerce.domain.payment.Payment;
import com.personal.marketnote.commerce.domain.payment.PaymentCreateState;
import com.personal.marketnote.commerce.port.in.command.order.OrderProductItemCommand;
import com.personal.marketnote.commerce.port.in.command.order.RegisterOrderCommand;
import com.personal.marketnote.commerce.port.in.result.order.RegisterOrderResult;
import com.personal.marketnote.commerce.port.in.usecase.inventory.GetInventoryUseCase;
import com.personal.marketnote.commerce.port.in.usecase.order.RegisterOrderUseCase;
import com.personal.marketnote.commerce.port.out.order.SaveOrderPort;
import com.personal.marketnote.commerce.port.out.payment.SavePaymentPort;
import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.common.utility.FormatValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED)
public class RegisterOrderService implements RegisterOrderUseCase {
    private final GetInventoryUseCase getInventoryUseCase;
    private final SaveOrderPort saveOrderPort;
    private final SavePaymentPort savePaymentPort;

    @Override
    public RegisterOrderResult registerOrder(RegisterOrderCommand command) {
        Map<Long, Long> productIdsByPricePolicyId = command.orderProducts().stream()
                .collect(Collectors.toMap(
                        OrderProductItemCommand::pricePolicyId,
                        OrderProductItemCommand::productId,
                        (existing, replacement) -> existing
                ));

        Set<Inventory> inventories = getInventoryUseCase.getOrCreateInventories(productIdsByPricePolicyId);
        inventories.forEach(inventory -> {
            int orderQuantity = command.orderProducts().stream()
                    .filter(item -> item.pricePolicyId().equals(inventory.getPricePolicyId()))
                    .map(OrderProductItemCommand::quantity)
                    .reduce(0, Integer::sum);

            inventory.validateIsSufficient(orderQuantity);
        });

        List<OrderProductCreateState> orderProductStates = command.orderProducts().stream()
                .map(item -> OrderProductCreateState.builder()
                        .sellerId(item.sellerId())
                        .pricePolicyId(item.pricePolicyId())
                        .sharerId(item.sharerId())
                        .quantity(item.quantity())
                        .unitAmount(item.unitAmount())
                        .imageUrl(item.imageUrl())
                        .build())
                .toList();

        Order savedOrder = saveOrderPort.save(
                Order.from(
                        OrderCreateState.builder()
                                .buyerId(command.buyerId())
                                .totalAmount(command.totalAmount())
                                .couponAmount(command.couponAmount())
                                .pointAmount(command.pointAmount())
                                .orderProductStates(orderProductStates)
                                .build()
                )
        );

        long couponAmount = FormatValidator.hasValue(command.couponAmount()) ? command.couponAmount() : 0L;
        long pointAmount = FormatValidator.hasValue(command.pointAmount()) ? command.pointAmount() : 0L;
        long paymentAmount = command.totalAmount() - couponAmount - pointAmount;

        savePaymentPort.save(
                Payment.from(
                        PaymentCreateState.builder()
                                .orderId(savedOrder.getId())
                                .orderKey(savedOrder.getOrderKey())
                                .paymentAmount(paymentAmount)
                                .build()
                )
        );

        return RegisterOrderResult.from(savedOrder);
    }
}

