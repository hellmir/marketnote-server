package com.personal.marketnote.user.port.in.usecase.shippingaddress;

import com.personal.marketnote.user.port.in.command.shippingaddress.UpdateDeliveryRequestCommand;

/**
 * 배송 요청사항 수정 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-03-27
 * @Description 배송지의 배송 요청사항만 부분 수정하는 기능을 제공합니다.
 */
public interface UpdateDeliveryRequestUseCase {
    /**
     * @param shippingAddressId 배송지 ID
     * @param userId            회원 ID
     * @param command           배송 요청사항 수정 커맨드
     * @Date 2026-03-27
     * @Author 성효빈
     * @Description 배송지의 배송 요청사항을 수정합니다.
     */
    void updateDeliveryRequest(Long shippingAddressId, Long userId, UpdateDeliveryRequestCommand command);
}
