package com.personal.marketnote.user.port.in.usecase.shippingaddress;

import com.personal.marketnote.user.port.in.result.shippingaddress.GetDeliveryRequestTypesResult;

import java.util.List;

/**
 * 배송 요청사항 목록 조회 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-03-19
 * @Description 배송 요청사항 타입 목록 조회 기능을 제공합니다.
 */
public interface GetDeliveryRequestTypesUseCase {
    /**
     * @return 배송 요청사항 타입 목록 {@link GetDeliveryRequestTypesResult}
     * @Date 2026-03-19
     * @Author 성효빈
     * @Description 배송 요청사항 타입 목록을 조회합니다.
     */
    List<GetDeliveryRequestTypesResult> getDeliveryRequestTypes();
}
