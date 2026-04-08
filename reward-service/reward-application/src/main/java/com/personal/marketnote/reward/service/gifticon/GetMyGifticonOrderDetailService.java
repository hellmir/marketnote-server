package com.personal.marketnote.reward.service.gifticon;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.reward.domain.exception.GifticonOrderNotFoundException;
import com.personal.marketnote.reward.domain.gifticon.GifticonGoods;
import com.personal.marketnote.reward.domain.gifticon.GifticonOrder;
import com.personal.marketnote.reward.domain.gifticon.GifticonOrderStatus;
import com.personal.marketnote.reward.port.in.command.gifticon.GetMyGifticonOrderDetailCommand;
import com.personal.marketnote.reward.port.in.result.gifticon.GetMyGifticonOrderDetailResult;
import com.personal.marketnote.reward.port.in.usecase.gifticon.GetMyGifticonOrderDetailUseCase;
import com.personal.marketnote.reward.port.out.gifticon.*;
import com.personal.marketnote.reward.port.out.gifticon.QueryGifticonCouponStatusPort.CouponStatusResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Slf4j
public class GetMyGifticonOrderDetailService implements GetMyGifticonOrderDetailUseCase {

    private final FindGifticonOrderPort findGifticonOrderPort;
    private final FindGifticonGoodsPort findGifticonGoodsPort;
    private final QueryGifticonCouponStatusPort queryGifticonCouponStatusPort;
    private final UpdateGifticonOrderPort updateGifticonOrderPort;
    private final DecryptGifticonPinPort decryptGifticonPinPort;
    private final Clock clock;

    @Override
    public GetMyGifticonOrderDetailResult getMyGifticonOrderDetail(GetMyGifticonOrderDetailCommand command) {
        GifticonOrder order = findOrder(command.orderId(), command.userId());

        syncStatusIfIssued(order);

        return buildDetailResult(order);
    }

    @Transactional(isolation = READ_COMMITTED, readOnly = true)
    public GifticonOrder findOrder(Long orderId, Long userId) {
        return findGifticonOrderPort.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new GifticonOrderNotFoundException(orderId));
    }

    private void syncStatusIfIssued(GifticonOrder order) {
        if (!order.isIssued()) {
            return;
        }
        CouponStatusResult statusResult = queryCouponStatus(order.getTrId());
        if (FormatValidator.hasNoValue(statusResult)) {
            return;
        }
        GifticonOrderStatus newStatus = GifticonOrderStatus.fromPinStatus(statusResult.pinStatusCd());
        boolean changed = order.syncStatus(newStatus);
        if (!changed) {
            return;
        }
        persistStatusChange(order, newStatus);
    }

    private CouponStatusResult queryCouponStatus(String trId) {
        try {
            CouponStatusResult statusResult = queryGifticonCouponStatusPort.queryStatus(trId);
            if (!statusResult.success()) {
                log.warn("기프티쇼 쿠폰 상태 조회 실패: trId={}, errorCode={}", trId, statusResult.errorCode());
                return null;
            }
            return statusResult;
        } catch (Exception e) {
            log.warn("기프티쇼 쿠폰 상태 동기화 실패 (fail-open): trId={}, error={}", trId, e.getMessage());
            return null;
        }
    }

    @Transactional(isolation = READ_COMMITTED)
    public void persistStatusChange(GifticonOrder order, GifticonOrderStatus newStatus) {
        updateGifticonOrderPort.update(order);
        log.info("기프티콘 주문 상태 동기화: orderId={}, newStatus={}", order.getId(), newStatus);
    }

    @Transactional(isolation = READ_COMMITTED, readOnly = true)
    public GetMyGifticonOrderDetailResult buildDetailResult(GifticonOrder order) {
        GifticonGoods goods = findGifticonGoodsPort.findByGoodsCode(order.getGoodsCode()).orElse(null);
        String decryptedPin = decryptPin(order.getPinNo());
        LocalDate now = LocalDate.now(clock);

        String description = FormatValidator.hasValue(goods) ? goods.getDescription() : null;
        String brandImageUrl = FormatValidator.hasValue(goods) ? goods.getBrandImageUrl() : null;

        return new GetMyGifticonOrderDetailResult(
                order.getId(),
                order.getGoodsName(),
                order.getBrandName(),
                brandImageUrl,
                order.getProductImageUrl(),
                description,
                order.getCashPrice(),
                order.getCouponImageUrl(),
                decryptedPin,
                order.formatExpiryDate(),
                order.calculateDaysRemaining(now),
                order.resolveStatusLabel(),
                order.getOrderStatus().name(),
                order.getCreatedAt()
        );
    }

    private String decryptPin(String encryptedPin) {
        if (FormatValidator.hasNoValue(encryptedPin)) {
            return null;
        }
        return decryptGifticonPinPort.decrypt(encryptedPin);
    }
}
