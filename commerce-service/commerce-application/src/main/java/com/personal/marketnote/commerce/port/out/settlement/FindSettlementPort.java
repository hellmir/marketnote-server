package com.personal.marketnote.commerce.port.out.settlement;

import com.personal.marketnote.commerce.domain.settlement.Settlement;

import java.util.List;
import java.util.Optional;

public interface FindSettlementPort {
    Optional<Settlement> findById(Long id);

    Optional<Settlement> findBySellerIdAndYearAndMonth(Long sellerId, Integer year, Integer month);

    List<Settlement> findAllByYearAndMonth(Integer year, Integer month);

    boolean existsBySellerIdAndYearAndMonth(Long sellerId, Integer year, Integer month);
}
