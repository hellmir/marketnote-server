package com.personal.marketnote.reward.service.gifticon;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.reward.domain.gifticon.GifticonOrder;
import com.personal.marketnote.reward.domain.gifticon.GifticonOrderStatus;
import com.personal.marketnote.reward.port.in.usecase.gifticon.SyncGifticonCouponStatusUseCase;
import com.personal.marketnote.reward.port.out.gifticon.FindGifticonOrderPort;
import com.personal.marketnote.reward.port.out.gifticon.QueryGifticonCouponStatusPort;
import com.personal.marketnote.reward.port.out.gifticon.QueryGifticonCouponStatusPort.CouponStatusResult;
import com.personal.marketnote.reward.port.out.gifticon.UpdateGifticonOrderPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class SyncGifticonCouponStatusService implements SyncGifticonCouponStatusUseCase {

    private final FindGifticonOrderPort findGifticonOrderPort;
    private final QueryGifticonCouponStatusPort queryGifticonCouponStatusPort;
    private final UpdateGifticonOrderPort updateGifticonOrderPort;
    private final TransactionTemplate transactionTemplate;

    @Override
    public void syncCouponStatuses() {
        List<GifticonOrder> issuedOrders = findGifticonOrderPort.findAllByOrderStatus(GifticonOrderStatus.ISSUED);
        log.info("쿠폰 상태 동기화 대상: {}건", issuedOrders.size());

        int synced = 0;
        int skipped = 0;
        int failed = 0;

        for (GifticonOrder order : issuedOrders) {
            try {
                SyncResult result = syncSingleCoupon(order);
                if (result == SyncResult.SYNCED) {
                    synced++;
                    continue;
                }
                skipped++;
            } catch (Exception e) {
                log.error("쿠폰 상태 동기화 실패: trId={}", order.getTrId(), e);
                failed++;
            }
        }

        log.info("쿠폰 상태 동기화 완료: total={}, synced={}, skipped={}, failed={}",
                issuedOrders.size(), synced, skipped, failed);
    }

    private SyncResult syncSingleCoupon(GifticonOrder order) {
        CouponStatusResult statusResult = queryGifticonCouponStatusPort.queryStatus(order.getTrId());

        if (!statusResult.success()) {
            log.warn("쿠폰 상태 조회 실패: trId={}, errorCode={}, errorMessage={}",
                    order.getTrId(), statusResult.errorCode(), statusResult.errorMessage());
            return SyncResult.SKIPPED;
        }

        String pinStatusCd = statusResult.pinStatusCd();

        if (!GifticonOrderStatus.hasPinStatusMapping(pinStatusCd)) {
            log.info("매핑되지 않는 핀상태: trId={}, pinStatusCd={}", order.getTrId(), pinStatusCd);
            return SyncResult.SKIPPED;
        }

        GifticonOrderStatus newStatus = GifticonOrderStatus.fromPinStatus(pinStatusCd);
        boolean changed = order.syncStatus(newStatus);

        if (!changed) {
            return SyncResult.SKIPPED;
        }

        transactionTemplate.execute(status -> {
            updateGifticonOrderPort.update(order);
            return null;
        });

        log.info("쿠폰 상태 동기화 성공: trId={}, newStatus={}", order.getTrId(), newStatus);
        return SyncResult.SYNCED;
    }

    private enum SyncResult {
        SYNCED, SKIPPED
    }
}
