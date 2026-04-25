package com.personal.marketnote.commerce.service.order;

import com.personal.marketnote.commerce.domain.order.OrderStatus;
import com.personal.marketnote.commerce.port.in.command.order.ChangeOrderStatusCommand;
import com.personal.marketnote.commerce.port.in.usecase.order.AutoConfirmDeliveredOrdersUseCase;
import com.personal.marketnote.commerce.port.in.usecase.order.ChangeOrderStatusUseCase;
import com.personal.marketnote.commerce.port.out.order.FindOrderPort;
import com.personal.marketnote.common.application.UseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@UseCase
@RequiredArgsConstructor
@Slf4j
public class AutoConfirmDeliveredOrdersService implements AutoConfirmDeliveredOrdersUseCase {
    private final FindOrderPort findOrderPort;
    private final ChangeOrderStatusUseCase changeOrderStatusUseCase;
    private final Clock clock;

    @Override
    public void autoConfirmDeliveredOrders(long autoConfirmDays) {
        LocalDateTime deliveredBefore = LocalDateTime.now(clock).minusDays(autoConfirmDays);
        List<Long> orderIds = findOrderPort.findOrderIdsEligibleForAutoConfirm(deliveredBefore);

        if (orderIds.isEmpty()) {
            log.info("자동 확정 대상 주문 없음");
            return;
        }

        log.info("자동 확정 대상 주문 {}건 처리 시작", orderIds.size());

        for (Long orderId : orderIds) {
            confirmOrderSafely(orderId);
        }

        log.info("자동 확정 처리 완료");
    }

    private void confirmOrderSafely(Long orderId) {
        try {
            ChangeOrderStatusCommand command = ChangeOrderStatusCommand.builder()
                    .id(orderId)
                    .orderStatus(OrderStatus.CONFIRMED)
                    .build();
            changeOrderStatusUseCase.changeOrderStatus(command);
            log.info("주문 자동 확정 완료 - orderId: {}", orderId);
        } catch (Exception e) {
            log.error("주문 자동 확정 실패 - orderId: {}, error: {}", orderId, e.getMessage(), e);
        }
    }
}
