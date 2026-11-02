package com.personal.marketnote.reward.service.gifticon;

import com.personal.marketnote.reward.domain.exception.GifticonInsufficientCashException;
import com.personal.marketnote.reward.domain.exception.GifticonOrderNotFoundException;
import com.personal.marketnote.reward.domain.gifticon.GifticonOrder;
import com.personal.marketnote.reward.domain.gifticon.GifticonOrderCreateState;
import com.personal.marketnote.reward.domain.point.UserPoint;
import com.personal.marketnote.reward.domain.point.UserPointChangeType;
import com.personal.marketnote.reward.domain.point.UserPointSourceType;
import com.personal.marketnote.reward.exception.UserPointNotFoundException;
import com.personal.marketnote.reward.port.in.command.point.ModifyUserPointCommand;
import com.personal.marketnote.reward.port.in.usecase.point.ModifyUserPointUseCase;
import com.personal.marketnote.reward.port.out.gifticon.EncryptGifticonPinPort;
import com.personal.marketnote.reward.port.out.gifticon.FindGifticonOrderPort;
import com.personal.marketnote.reward.port.out.gifticon.SaveGifticonOrderPort;
import com.personal.marketnote.reward.port.out.gifticon.SendGifticonCouponPort.SendCouponResult;
import com.personal.marketnote.reward.port.out.gifticon.UpdateGifticonOrderPort;
import com.personal.marketnote.reward.port.out.point.FindUserPointPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;
import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

@Component
@RequiredArgsConstructor
@Slf4j
public class PurchaseGifticonTransactionHelper {

    private final FindUserPointPort findUserPointPort;
    private final ModifyUserPointUseCase modifyUserPointUseCase;
    private final SaveGifticonOrderPort saveGifticonOrderPort;
    private final FindGifticonOrderPort findGifticonOrderPort;
    private final UpdateGifticonOrderPort updateGifticonOrderPort;
    private final EncryptGifticonPinPort encryptGifticonPinPort;
    private final Clock clock;

    private static final DateTimeFormatter TR_ID_DATE_FORMAT = DateTimeFormatter.ofPattern("yyMMddHHmmss");
    private static final DateTimeFormatter VALID_END_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    public record DeductCashAndCreateOrderContext(
            Long orderId,
            String trId,
            String goodsCode,
            String goodsName,
            Long cashPrice,
            Long userId
    ) {
    }

    @Transactional(propagation = REQUIRES_NEW, isolation = READ_COMMITTED)
    public DeductCashAndCreateOrderContext deductCashAndCreateOrder(
            Long userId, String goodsCode, String goodsName, String brandName,
            String productImageUrl, Long cashPrice
    ) {
        UserPoint userPoint = findUserPointPort.findByUserIdForUpdate(userId)
                .orElseThrow(() -> new UserPointNotFoundException(userId));
        Long currentBalance = userPoint.getAmountValue();

        if (currentBalance < cashPrice) {
            throw new GifticonInsufficientCashException(cashPrice - currentBalance);
        }

        String trId = generateTrId(userId);

        modifyUserPointUseCase.modify(ModifyUserPointCommand.builder()
                .userId(userId)
                .changeType(UserPointChangeType.DEDUCTION)
                .amount(cashPrice)
                .sourceType(UserPointSourceType.GIFTICON_PURCHASE)
                .reason("기프티콘 구매: " + goodsName)
                .build()
        );

        GifticonOrder order = GifticonOrder.from(GifticonOrderCreateState.builder()
                .userId(userId)
                .goodsCode(goodsCode)
                .goodsName(goodsName)
                .brandName(brandName)
                .productImageUrl(productImageUrl)
                .trId(trId)
                .cashPrice(cashPrice)
                .build()
        );

        GifticonOrder savedOrder = saveGifticonOrderPort.save(order);

        return new DeductCashAndCreateOrderContext(
                savedOrder.getId(), trId, goodsCode, goodsName, cashPrice, userId
        );
    }

    @Transactional(propagation = REQUIRES_NEW, isolation = READ_COMMITTED)
    public void commitSuccess(DeductCashAndCreateOrderContext context, SendCouponResult sendResult) {
        GifticonOrder order = findGifticonOrderPort.findByTrId(context.trId())
                .orElseThrow(() -> new GifticonOrderNotFoundException(context.trId()));

        String encryptedPin = encryptGifticonPinPort.encrypt(sendResult.pinNo());
        LocalDate validEndDate = LocalDate.parse(sendResult.validEndDate(), VALID_END_DATE_FORMAT);

        order.issue(sendResult.couponImageUrl(), encryptedPin, sendResult.orderNo(), validEndDate);
        updateGifticonOrderPort.update(order);
    }

    @Transactional(propagation = REQUIRES_NEW, isolation = READ_COMMITTED)
    public void commitFailure(DeductCashAndCreateOrderContext context) {
        modifyUserPointUseCase.modify(ModifyUserPointCommand.builder()
                .userId(context.userId())
                .changeType(UserPointChangeType.ACCRUAL)
                .amount(context.cashPrice())
                .sourceType(UserPointSourceType.GIFTICON_REFUND)
                .reason("기프티콘 구매 실패 환불: " + context.goodsName())
                .build()
        );

        GifticonOrder order = findGifticonOrderPort.findByTrId(context.trId())
                .orElseThrow(() -> new GifticonOrderNotFoundException(context.trId()));

        order.markSendFailed();
        updateGifticonOrderPort.update(order);
    }

    // 기프티쇼 TR_ID 25자 제약 (공식 API 규격서 v1.04 P.26, ERR0807 "TR_ID가 25byte를 초과했습니다") 준수.
    // 포맷: NC{userId}_{yyMMddHHmmss}{ms3} — 총 15 + userId 자리수 → userId 10자리까지 25자 이내.
    // 동일 초 내 동시 요청 TOCTOU 회피는 ms 3자리(0~999) + Service 레이어 재시도로 커버.
    // ⚠️ 포맷 변경 시 25자 제약 재검증 필수.
    private String generateTrId(Long userId) {
        LocalDateTime now = LocalDateTime.now(clock);
        String msSuffix = String.format("%03d", now.getNano() / 1_000_000);
        return "NC" + userId + "_" + now.format(TR_ID_DATE_FORMAT) + msSuffix;
    }
}
