package com.personal.marketnote.user.port.in.usecase.shippingaddress;

/**
 * 기본 배송지 설정 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-02-19
 * @Description 기본 배송지 설정 기능을 제공합니다.
 */
public interface SetDefaultShippingAddressUseCase {
    /**
     * @param shippingAddressId 배송지 ID
     * @param userId            회원 ID
     * @Date 2026-02-19
     * @Author 성효빈
     * @Description 기본 배송지를 설정합니다.
     */
    void setDefaultShippingAddress(Long shippingAddressId, Long userId);
}
