package com.personal.marketnote.commerce.port.in.usecase.order;

import com.personal.marketnote.commerce.port.in.command.order.RegisterTrackingInfoCommand;

/**
 * 송장 정보 등록 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-03-02
 * @Description 주문의 송장 정보(택배사, 송장번호) 등록/수정 기능을 제공합니다.
 */
public interface RegisterTrackingInfoUseCase {
    /**
     * @param command 송장 정보 등록 커맨드
     * @Date 2026-03-02
     * @Author 성효빈
     * @Description 주문의 송장 정보(택배사, 송장번호)를 등록/수정합니다.
     */
    void registerTrackingInfo(RegisterTrackingInfoCommand command);
}
