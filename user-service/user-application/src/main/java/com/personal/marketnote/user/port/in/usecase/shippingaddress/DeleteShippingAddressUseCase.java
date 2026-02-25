package com.personal.marketnote.user.port.in.usecase.shippingaddress;

/**
 * 배송지 삭제 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-02-19
 * @Description 배송지 삭제 기능을 제공합니다.
 */
public interface DeleteShippingAddressUseCase {
    /**
     * @param shippingAddressId 배송지 ID
     * @param userId            회원 ID
     * @Date 2026-02-19
     * @Author 성효빈
     * @Description 배송지를 삭제합니다.
     */
    void deleteShippingAddress(Long shippingAddressId, Long userId);
}
