package com.personal.marketnote.reward.adapter.in.scheduler;

import com.personal.marketnote.reward.port.in.usecase.gifticon.SyncGifticonCouponStatusUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "gifticon.coupon-sync.scheduler.enabled", havingValue = "true", matchIfMissing = false)
public class GifticonCouponSyncScheduler {

    private final SyncGifticonCouponStatusUseCase syncGifticonCouponStatusUseCase;

    @Scheduled(cron = "${gifticon.coupon-sync.scheduler.cron:0 0 4 * * *}", zone = "Asia/Seoul")
    public void syncGifticonCouponStatuses() {
        log.info("기프티콘 쿠폰 상태 동기화 스케줄러 실행 시작");

        try {
            syncGifticonCouponStatusUseCase.syncCouponStatuses();
            log.info("기프티콘 쿠폰 상태 동기화 스케줄러 실행 완료");
        } catch (Exception e) {
            log.error("기프티콘 쿠폰 상태 동기화 스케줄러 실행 실패", e);
        }
    }
}
