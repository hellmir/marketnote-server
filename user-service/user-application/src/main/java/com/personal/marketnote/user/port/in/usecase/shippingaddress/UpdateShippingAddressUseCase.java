package com.personal.marketnote.user.port.in.usecase.shippingaddress;

import com.personal.marketnote.user.port.in.command.shippingaddress.UpdateShippingAddressCommand;

/**
 * 배송지 수정 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-02-19
 * @Description 배송지 수정 기능을 제공합니다.
 */
public interface UpdateShippingAddressUseCase {
    /**
     * @param shippingAddressId 배송지 ID
     * @param userId            회원 ID
     * @param command           배송지 수정 커맨드
     * @Date 2026-02-19
     * @Author 성효빈
     * @Description 배송지를 수정합니다.
     */
    void updateShippingAddress(Long shippingAddressId, Long userId, UpdateShippingAddressCommand command);
}
