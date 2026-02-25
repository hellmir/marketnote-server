package com.personal.marketnote.user.port.out.shippingaddress;

import com.personal.marketnote.user.domain.shippingaddress.ShippingAddress;

/**
 * 배송지 수정 포트
 *
 * @Author 성효빈
 * @Date 2026-02-19
 * @Description 배송지 수정 기능을 제공합니다.
 */
public interface UpdateShippingAddressPort {
    /**
     * @param shippingAddress 배송지
     * @Date 2026-02-19
     * @Author 성효빈
     * @Description 배송지를 수정합니다.
     */
    void update(ShippingAddress shippingAddress);
}
