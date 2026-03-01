package com.personal.marketnote.commerce.service.settlement;

import com.personal.marketnote.commerce.domain.settlement.Settlement;
import com.personal.marketnote.commerce.port.in.command.settlement.GetSellerSettlementsQuery;
import com.personal.marketnote.commerce.port.in.result.settlement.GetSettlementsResult;
import com.personal.marketnote.commerce.port.in.usecase.settlement.GetSellerSettlementsUseCase;
import com.personal.marketnote.commerce.port.out.settlement.FindSettlementPort;
import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.common.utility.FormatValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

/**
 * 판매자 정산 내역 조회 서비스
 *
 * @Author 성효빈
 * @Date 2026-03-02
 * @Description 판매자가 본인의 정산 내역을 연도/월 기준으로 조회합니다.
 */
@UseCase
@RequiredArgsConstructor
@Slf4j
public class GetSellerSettlementsService implements GetSellerSettlementsUseCase {
    private final FindSettlementPort findSettlementPort;

    @Override
    @Transactional(isolation = READ_COMMITTED, readOnly = true)
    public GetSettlementsResult getSellerSettlements(GetSellerSettlementsQuery query) {
        log.info("판매자 정산 조회 - sellerId={}, year={}, month={}",
                query.sellerId(), query.year(), query.month());

        List<Settlement> settlements;

        if (FormatValidator.hasValue(query.month())) {
            Optional<Settlement> settlement = findSettlementPort.findBySellerIdAndYearAndMonth(
                    query.sellerId(), query.year(), query.month());
            settlements = settlement.map(List::of).orElse(List.of());
        } else {
            settlements = findSettlementPort.findAllBySellerIdAndYear(
                    query.sellerId(), query.year());
        }

        return GetSettlementsResult.from(settlements);
    }
}
