package com.personal.marketnote.commerce.port.out.user;

/**
 * 회원 서비스의 배송지 배송 요청사항 수정 포트
 *
 * @Author 성효빈
 * @Date 2026-03-27
 * @Description 커머스 서비스에서 회원 서비스의 배송지 배송 요청사항을 수정합니다.
 */
public interface UpdateUserShippingAddressDeliveryRequestPort {
    /**
     * @param shippingAddressId    배송지 ID
     * @param userId               회원 ID
     * @param deliveryRequestType  배송 요청사항 타입
     * @param deliveryRequestMessage 배송 요청사항 메시지
     * @Date 2026-03-27
     * @Author 성효빈
     * @Description 배송지의 배송 요청사항을 수정합니다.
     */
    void updateDeliveryRequest(Long shippingAddressId, Long userId, String deliveryRequestType, String deliveryRequestMessage);
}
