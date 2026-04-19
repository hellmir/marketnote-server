package com.personal.marketnote.commerce.mapper;

import com.personal.marketnote.commerce.domain.order.*;
import com.personal.marketnote.commerce.domain.payment.PaymentCreateState;
import com.personal.marketnote.commerce.domain.settlement.PaymentAllocationCreateState;
import com.personal.marketnote.commerce.domain.settlement.PaymentAllocationTargetType;
import com.personal.marketnote.commerce.domain.settlement.PaymentAllocationTransactionType;
import com.personal.marketnote.commerce.port.in.command.order.ChangeOrderStatusCommand;
import com.personal.marketnote.commerce.port.in.command.order.OrderAmountCommand;
import com.personal.marketnote.commerce.port.in.command.order.OrderProductItemCommand;
import com.personal.marketnote.commerce.port.out.result.product.ProductInfoResult;
import com.personal.marketnote.common.utility.FormatValidator;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class OrderCommandToStateMapper {
    private OrderCommandToStateMapper() {
    }

    public static OrderStatusHistoryCreateState mapToState(ChangeOrderStatusCommand command) {
        return OrderStatusHistoryCreateState.builder()
                .orderId(command.id())
                .orderStatus(command.orderStatus())
                .reasonCategory(command.reasonCategory())
                .reason(command.reason())
                .build();
    }

    public static List<OrderProductCreateState> mapToOrderProductStates(
            List<OrderProductItemCommand> orderProducts,
            Map<Long, ProductInfoResult> productInfoMap
    ) {
        return orderProducts.stream()
                .map(item -> {
                    Long accumulatedPoint = resolveAccumulatedPoint(item.pricePolicyId(), productInfoMap);
                    return OrderProductCreateState.builder()
                            .sellerId(item.sellerId())
                            .pricePolicyId(item.pricePolicyId())
                            .sharerKey(item.sharerKey())
                            .quantity(item.quantity())
                            .unitAmount(item.unitAmount())
                            .imageUrl(item.imageUrl())
                            .accumulatedPoint(accumulatedPoint)
                            .build();
                })
                .toList();
    }

    private static Long resolveAccumulatedPoint(Long pricePolicyId, Map<Long, ProductInfoResult> productInfoMap) {
        if (FormatValidator.hasNoValue(productInfoMap)) {
            return null;
        }
        ProductInfoResult productInfo = productInfoMap.get(pricePolicyId);
        if (FormatValidator.hasNoValue(productInfo)) {
            return null;
        }
        return productInfo.accumulatedPoint();
    }

    public static OrderAmountCreateState mapToOrderAmountState(OrderAmountCommand amountCommand) {
        return OrderAmountCreateState.builder()
                .totalAmount(amountCommand.totalAmount())
                .couponAmount(amountCommand.couponAmount())
                .pointAmount(amountCommand.pointAmount())
                .shippingFee(amountCommand.shippingFee())
                .build();
    }

    public static OrderCreateState mapToOrderState(Long buyerId,
                                                   OrderAmount orderAmount,
                                                   ShippingAddress shippingAddress,
                                                   List<OrderProductCreateState> orderProductStates) {
        return OrderCreateState.builder()
                .buyerId(buyerId)
                .amount(orderAmount)
                .shippingAddress(shippingAddress)
                .orderProductStates(orderProductStates)
                .build();
    }

    public static PaymentCreateState mapToPaymentState(Long orderId, UUID orderKey, long paymentAmount) {
        return PaymentCreateState.builder()
                .orderId(orderId)
                .orderKey(orderKey)
                .paymentAmount(paymentAmount)
                .build();
    }

    public static PaymentAllocationCreateState mapToPaymentAllocationState(Long orderId,
                                                                           Long sellerId,
                                                                           Long allocatedAmount) {
        return PaymentAllocationCreateState.builder()
                .orderId(orderId)
                .sellerId(sellerId)
                .allocatedAmount(allocatedAmount)
                .transactionType(PaymentAllocationTransactionType.ORDER_REGISTRATION)
                .targetType(PaymentAllocationTargetType.ORDER)
                .idempotencyKey("ORDER_ALLOCATION:" + orderId + ":" + sellerId)
                .build();
    }
}
