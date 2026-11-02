package com.personal.marketnote.user.port.out.shippingaddress;

import com.personal.marketnote.user.domain.shippingaddress.ShippingAddress;

/**
 * 배송지 저장 포트
 *
 * @Author 성효빈
 * @Date 2026-02-19
 * @Description 배송지 저장 기능을 제공합니다.
 */
public interface SaveShippingAddressPort {
    /**
     * @param shippingAddress 배송지
     * @return 저장된 배송지 {@link ShippingAddress}
     * @Date 2026-02-19
     * @Author 성효빈
     * @Description 배송지를 저장합니다.
     */
    ShippingAddress save(ShippingAddress shippingAddress);
}
