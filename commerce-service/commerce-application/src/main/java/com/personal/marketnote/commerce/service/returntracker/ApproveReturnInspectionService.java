package com.personal.marketnote.commerce.service.returntracker;

import com.personal.marketnote.commerce.domain.order.Order;
import com.personal.marketnote.commerce.domain.order.OrderStatus;
import com.personal.marketnote.commerce.exception.InvalidOrderStatusTransitionException;
import com.personal.marketnote.commerce.port.in.command.returntracker.ApproveReturnInspectionCommand;
import com.personal.marketnote.commerce.port.in.command.returntracker.CompleteReturnCommand;
import com.personal.marketnote.commerce.port.in.usecase.order.GetOrderUseCase;
import com.personal.marketnote.commerce.port.in.usecase.returntracker.ApproveReturnInspectionUseCase;
import com.personal.marketnote.commerce.port.in.usecase.returntracker.CompleteReturnUseCase;
import com.personal.marketnote.common.application.UseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class ApproveReturnInspectionService implements ApproveReturnInspectionUseCase {

    private final GetOrderUseCase getOrderUseCase;
    private final CompleteReturnUseCase completeReturnUseCase;

    @Override
    public void approveReturnInspection(ApproveReturnInspectionCommand command) {
        Order order = getOrderUseCase.getOrder(command.orderId());

        if (order.isReturned()) {
            log.info("이미 반품 완료 처리된 주문 (멱등). orderId={}", command.orderId());
            return;
        }

        if (!order.isReturnInspecting()) {
            throw new InvalidOrderStatusTransitionException(order.getOrderStatus(), OrderStatus.RETURNED);
        }

        completeReturnUseCase.completeReturn(new CompleteReturnCommand(command.orderId()));

        log.info("CS 반품 검수 승인 완료. orderId={}", command.orderId());
    }
}
