package com.personal.marketnote.commerce.port.out.settlement;

import com.personal.marketnote.commerce.domain.settlement.Settlement;
import com.personal.marketnote.commerce.domain.settlement.SettlementStatus;

import java.util.List;
import java.util.Optional;

public interface FindSettlementPort {
    Optional<Settlement> findById(Long id);

    Optional<Settlement> findBySellerIdAndYearAndMonth(Long sellerId, Integer year, Integer month);

    List<Settlement> findAllByYearAndMonth(Integer year, Integer month);

    boolean existsBySellerIdAndYearAndMonth(Long sellerId, Integer year, Integer month);

    /**
     * @param sellerId 판매자 ID
     * @param year     정산 연도
     * @return 해당 판매자의 해당 연도 정산 목록 {@link List}
     * @Date 2026-03-02
     * @Author 성효빈
     * @Description 판매자 ID와 연도로 정산 목록을 조회합니다.
     */
    List<Settlement> findAllBySellerIdAndYear(Long sellerId, Integer year);

    /**
     * 특정 상태의 정산 목록을 조회한다.
     *
     * @param status 조회할 정산 상태
     * @return 해당 상태의 정산 목록
     * @author 성효빈
     * @since 2026-03-02
     */
    List<Settlement> findAllByStatus(SettlementStatus status);
}
