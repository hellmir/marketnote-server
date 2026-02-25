package com.personal.marketnote.fulfillment.port.in.usecase.vendor;

import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFasstoStocksCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFasstoStocksResult;

/**
 * 파스토 재고 목록 조회 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-02-03
 * @Description 파스토 재고 목록 조회 기능을 제공합니다.
 */
public interface GetFasstoStocksUseCase {
    /**
     * @param command 재고 목록 조회 커맨드
     * @return 재고 목록 조회 결과 {@link GetFasstoStocksResult}
     * @Date 2026-02-03
     * @Author 성효빈
     * @Description 파스토 재고 목록을 조회합니다.
     */
    GetFasstoStocksResult getStocks(GetFasstoStocksCommand command);
}
