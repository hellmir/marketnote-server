package com.personal.marketnote.product.service.popularity;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.product.port.in.usecase.popularity.UpdatePopularityUseCase;
import com.personal.marketnote.product.port.out.pricepolicy.UpdatePopularityPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED)
public class UpdatePopularityService implements UpdatePopularityUseCase {
    private final UpdatePopularityPort updatePopularityPort;
    private final Clock productClock;

    @Override
    public void updateWeeklyPopularity() {
        LocalDateTime since = LocalDateTime.now(productClock).minusDays(7);
        updatePopularityPort.updateWeeklyPopularity(since);
    }
}
