package com.personal.marketnote.user.port.in.usecase.shippingaddress;

import com.personal.marketnote.user.port.in.result.shippingaddress.GetMyShippingAddressesResult;

public interface GetMyShippingAddressesUseCase {

    /**
     * @param userId 사용자 ID
     * @return 내 배송지 목록 조회 결과 {@link GetMyShippingAddressesResult}
     */
    GetMyShippingAddressesResult getMyShippingAddresses(Long userId);
}
