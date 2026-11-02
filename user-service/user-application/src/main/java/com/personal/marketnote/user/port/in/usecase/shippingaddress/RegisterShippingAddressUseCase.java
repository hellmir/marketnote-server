package com.personal.marketnote.user.port.in.usecase.shippingaddress;

import com.personal.marketnote.user.port.in.command.shippingaddress.RegisterShippingAddressCommand;
import com.personal.marketnote.user.port.in.result.shippingaddress.RegisterShippingAddressResult;

/**
 * 배송지 등록 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-02-19
 * @Description 배송지 등록 기능을 제공합니다.
 */
public interface RegisterShippingAddressUseCase {
    /**
     * @param command 배송지 등록 커맨드
     * @return 배송지 등록 결과 {@link RegisterShippingAddressResult}
     * @Date 2026-02-19
     * @Author 성효빈
     * @Description 배송지를 등록합니다.
     */
    RegisterShippingAddressResult registerShippingAddress(RegisterShippingAddressCommand command);
}
