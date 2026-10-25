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
    private static final int CHUNK_SIZE = 100;
    private static final long INITIAL_CURSOR = 0L;

    private final FindOrderPort findOrderPort;
    private final ChangeOrderStatusUseCase changeOrderStatusUseCase;
    private final Clock clock;

    @Override
    public void autoConfirmDeliveredOrders() {
        LocalDateTime now = LocalDateTime.now(clock);
        long lastId = INITIAL_CURSOR;
        long totalProcessed = 0L;

        while (true) {
            List<Long> chunk = findOrderPort.findOrderIdsScheduledForAutoConfirm(now, lastId, CHUNK_SIZE);
            if (chunk.isEmpty()) {
                log.info("자동 확정 처리 완료 - 총 {}건", totalProcessed);
                return;
            }

            log.info("자동 확정 청크 처리 시작 - lastId: {}, size: {}", lastId, chunk.size());
            chunk.forEach(this::confirmOrderSafely);
            totalProcessed += chunk.size();
            lastId = chunk.get(chunk.size() - 1);

            if (chunk.size() < CHUNK_SIZE) {
                log.info("자동 확정 처리 완료 - 총 {}건", totalProcessed);
                return;
            }
        }
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
