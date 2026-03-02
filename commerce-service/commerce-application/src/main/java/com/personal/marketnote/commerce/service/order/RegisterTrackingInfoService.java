package com.personal.marketnote.commerce.service.order;

import com.personal.marketnote.commerce.domain.order.Order;
import com.personal.marketnote.commerce.port.in.command.order.RegisterTrackingInfoCommand;
import com.personal.marketnote.commerce.port.in.usecase.order.GetOrderUseCase;
import com.personal.marketnote.commerce.port.in.usecase.order.RegisterTrackingInfoUseCase;
import com.personal.marketnote.commerce.port.out.order.UpdateOrderPort;
import com.personal.marketnote.common.application.UseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

/**
 * 송장 정보 등록 서비스
 *
 * @Author 성효빈
 * @Date 2026-03-02
 * @Description 주문의 송장 정보(택배사, 송장번호)를 등록/수정합니다.
 */
@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED)
public class RegisterTrackingInfoService implements RegisterTrackingInfoUseCase {
    private final GetOrderUseCase getOrderUseCase;
    private final UpdateOrderPort updateOrderPort;

    @Override
    public void registerTrackingInfo(RegisterTrackingInfoCommand command) {
        Order order = getOrderUseCase.getOrder(command.orderId());
        order.registerTrackingInfo(command.courierCompany(), command.trackingNumber());
        updateOrderPort.updateTrackingInfo(order);
    }
}
