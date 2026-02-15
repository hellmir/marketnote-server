package com.personal.marketnote.commerce.service.order;

import com.personal.marketnote.commerce.domain.order.Order;
import com.personal.marketnote.commerce.domain.order.OrderCreateState;
import com.personal.marketnote.commerce.domain.order.OrderProductCreateState;
import com.personal.marketnote.commerce.port.in.command.order.RegisterOrderCommand;
import com.personal.marketnote.commerce.port.in.result.order.RegisterOrderResult;
import com.personal.marketnote.commerce.port.in.usecase.order.RegisterOrderUseCase;
import com.personal.marketnote.commerce.port.out.order.SaveOrderPort;
import com.personal.marketnote.common.application.UseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED)
public class RegisterOrderService implements RegisterOrderUseCase {
    private final SaveOrderPort saveOrderPort;

    @Override
    public RegisterOrderResult registerOrder(RegisterOrderCommand command) {
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

        return RegisterOrderResult.from(savedOrder);
    }
}

