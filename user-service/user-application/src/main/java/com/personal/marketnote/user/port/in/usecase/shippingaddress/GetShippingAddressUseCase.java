package com.personal.marketnote.user.port.in.usecase.shippingaddress;

import com.personal.marketnote.user.port.in.result.shippingaddress.GetShippingAddressResult;

public interface GetShippingAddressUseCase {
    /**
     * @param shippingAddressId 배송지 ID
     * @param userId            회원 ID
     * @return 배송지 조회 결과 {@link GetShippingAddressResult}
     * @Date 2026-02-03
     * @Author 성효빈
     * @Description 배송지를 조회합니다. 본인의 배송지만 조회할 수 있습니다.
     */
    GetShippingAddressResult getShippingAddress(Long shippingAddressId, Long userId);
}
