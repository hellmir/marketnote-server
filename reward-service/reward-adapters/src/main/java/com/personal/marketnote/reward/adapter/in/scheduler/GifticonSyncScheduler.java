package com.personal.marketnote.reward.adapter.in.scheduler;

import com.personal.marketnote.reward.port.in.usecase.gifticon.SyncGifticonGoodsAndBrandsUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "gifticon.sync.scheduler.enabled", havingValue = "true", matchIfMissing = false)
public class GifticonSyncScheduler {

    private final SyncGifticonGoodsAndBrandsUseCase syncGifticonGoodsAndBrandsUseCase;

    @Scheduled(cron = "${gifticon.sync.scheduler.cron:0 0 3 * * MON,WED,FRI}", zone = "Asia/Seoul")
    public void syncGifticonGoodsAndBrands() {
        log.info("기프티콘 상품/브랜드 동기화 스케줄러 실행 시작");

        try {
            syncGifticonGoodsAndBrandsUseCase.syncAll();
            log.info("기프티콘 상품/브랜드 동기화 스케줄러 실행 완료");
        } catch (Exception e) {
            log.error("기프티콘 상품/브랜드 동기화 스케줄러 실행 실패", e);
        }
    }
}
