package com.personal.marketnote.product.adapter.in.scheduler;

import com.personal.marketnote.product.port.in.usecase.popularity.UpdatePopularityUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "popularity.scheduler.enabled", havingValue = "true", matchIfMissing = false)
public class PopularityScheduler {
    private final UpdatePopularityUseCase updatePopularityUseCase;

    @Scheduled(cron = "${popularity.scheduler.cron:0 0 * * * *}", zone = "Asia/Seoul")
    public void updateWeeklyPopularity() {
        log.info("주간 인기도 갱신 스케줄러 실행 시작");

        try {
            updatePopularityUseCase.updateWeeklyPopularity();
            log.info("주간 인기도 갱신 스케줄러 실행 완료");
        } catch (Exception e) {
            log.error("주간 인기도 갱신 스케줄러 실행 실패", e);
        }
    }
}
