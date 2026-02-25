package com.personal.marketnote.fulfillment.port.in.usecase.vendor;

import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFasstoShopsCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFasstoShopsResult;

/**
 * 파스토 출고처 목록 조회 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-01-25
 * @Description 파스토 출고처 목록 조회 기능을 제공합니다.
 */
public interface GetFasstoShopsUseCase {
    /**
     * @param command 출고처 목록 조회 커맨드
     * @return 출고처 목록 조회 결과 {@link GetFasstoShopsResult}
     * @Date 2026-01-25
     * @Author 성효빈
     * @Description 파스토 출고처 목록을 조회합니다.
     */
    GetFasstoShopsResult getShops(GetFasstoShopsCommand command);
}
