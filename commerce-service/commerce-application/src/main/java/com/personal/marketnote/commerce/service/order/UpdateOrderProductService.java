package com.personal.marketnote.commerce.service.order;

import com.personal.marketnote.commerce.domain.order.OrderProduct;
import com.personal.marketnote.commerce.port.in.command.order.UpdateOrderProductReviewStatusCommand;
import com.personal.marketnote.commerce.port.in.usecase.order.GetOrderUseCase;
import com.personal.marketnote.commerce.port.in.usecase.order.UpdateOrderProductUseCase;
import com.personal.marketnote.commerce.port.out.order.UpdateOrderProductPort;
import com.personal.marketnote.common.application.UseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED)
public class UpdateOrderProductService implements UpdateOrderProductUseCase {
    private final GetOrderUseCase getOrderUseCase;
    private final UpdateOrderProductPort updateOrderProductPort;

    @Override
    public void updateReviewStatus(UpdateOrderProductReviewStatusCommand command) {
        OrderProduct orderProduct = getOrderUseCase.getOrderProduct(command.orderId(), command.pricePolicyId());
        orderProduct.updateReviewStatus(command.isReviewed());
        updateOrderProductPort.update(orderProduct);
    }
}
