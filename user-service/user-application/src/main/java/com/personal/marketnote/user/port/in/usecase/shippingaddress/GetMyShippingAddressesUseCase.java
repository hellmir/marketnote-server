package com.personal.marketnote.user.port.in.usecase.shippingaddress;

import com.personal.marketnote.user.port.in.result.shippingaddress.GetMyShippingAddressesResult;

/**
 * 내 배송지 목록 조회 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-02-19
 * @Description 내 배송지 목록 조회 기능을 제공합니다.
 */
public interface GetMyShippingAddressesUseCase {

    /**
     * @param userId 사용자 ID
     * @return 내 배송지 목록 조회 결과 {@link GetMyShippingAddressesResult}
     * @Date 2026-02-19
     * @Author 성효빈
     * @Description 내 배송지 목록을 조회합니다.
     */
    GetMyShippingAddressesResult getMyShippingAddresses(Long userId);
}
