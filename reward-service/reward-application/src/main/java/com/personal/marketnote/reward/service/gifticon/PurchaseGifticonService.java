package com.personal.marketnote.reward.service.gifticon;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.reward.domain.exception.DuplicateGifticonOrderException;
import com.personal.marketnote.reward.domain.exception.GifticonCouponSendFailedException;
import com.personal.marketnote.reward.domain.exception.GifticonGoodsNotFoundException;
import com.personal.marketnote.reward.domain.gifticon.GifticonGoods;
import com.personal.marketnote.reward.port.in.command.gifticon.PurchaseGifticonCommand;
import com.personal.marketnote.reward.port.in.result.gifticon.PurchaseGifticonResult;
import com.personal.marketnote.reward.port.in.usecase.gifticon.PurchaseGifticonUseCase;
import com.personal.marketnote.reward.port.out.gifticon.CancelGifticonSendFailPort;
import com.personal.marketnote.reward.port.out.gifticon.FindGifticonGoodsPort;
import com.personal.marketnote.reward.port.out.gifticon.SendGifticonCouponPort;
import com.personal.marketnote.reward.port.out.gifticon.SendGifticonCouponPort.SendCouponResult;
import com.personal.marketnote.reward.service.gifticon.PurchaseGifticonTransactionHelper.DeductCashAndCreateOrderContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@UseCase
@RequiredArgsConstructor
@Slf4j
public class PurchaseGifticonService implements PurchaseGifticonUseCase {

    private static final int MAX_TR_ID_RETRY = 3;

    private final FindGifticonGoodsPort findGifticonGoodsPort;
    private final PurchaseGifticonTransactionHelper transactionHelper;
    private final SendGifticonCouponPort sendGifticonCouponPort;
    private final CancelGifticonSendFailPort cancelGifticonSendFailPort;

    @Override
    public PurchaseGifticonResult purchase(PurchaseGifticonCommand command) {
        GifticonGoods goods = findAndValidateGoods(command.goodsCode());

        DeductCashAndCreateOrderContext context = deductWithRetry(command.userId(), goods);

        SendCouponResult sendResult = sendGifticonCouponPort.sendCoupon(
                context.trId(), context.goodsCode(), String.valueOf(command.userId())
        );

        if (!sendResult.success()) {
            handleSendFailure(context);
            throw new GifticonCouponSendFailedException(sendResult.errorCode(), sendResult.errorMessage());
        }

        try {
            transactionHelper.commitSuccess(context, sendResult);
        } catch (Exception e) {
            log.error("commitSuccess 실패 — 쿠폰은 발행되었으나 DB 저장 실패. 수동 복구 필요. trId={}, orderNo={}, error={}",
                    context.trId(), sendResult.orderNo(), e.getMessage(), e);
            throw e;
        }

        return new PurchaseGifticonResult(
                context.orderId(),
                sendResult.orderNo(),
                context.cashPrice(),
                context.goodsName()
        );
    }

    // Helper의 REQUIRES_NEW 트랜잭션이 DuplicateGifticonOrderException 발생 시 롤백되어
    // 포인트 차감·주문 저장 모두 원복된다. 재시도 시 중복 차감 없음.
    private DeductCashAndCreateOrderContext deductWithRetry(Long userId, GifticonGoods goods) {
        DuplicateGifticonOrderException lastException = null;
        for (int attempt = 1; attempt <= MAX_TR_ID_RETRY; attempt++) {
            try {
                return transactionHelper.deductCashAndCreateOrder(
                        userId,
                        goods.getGoodsCode().getValue(),
                        goods.getGoodsName(),
                        goods.getBrandName(),
                        goods.getImageUrl(),
                        goods.getCashPrice().getValue()
                );
            } catch (DuplicateGifticonOrderException dgoe) {
                lastException = dgoe;
                log.warn("trId 중복 발생 — 재시도 {}/{}: userId={}, message={}",
                        attempt, MAX_TR_ID_RETRY, userId, dgoe.getMessage());
            }
        }
        log.error("trId 중복이 {}회 연속 발생하여 기프티콘 구매를 중단합니다. userId={}", MAX_TR_ID_RETRY, userId);
        throw lastException;
    }

    private GifticonGoods findAndValidateGoods(String goodsCode) {
        GifticonGoods goods = findGifticonGoodsPort.findByGoodsCode(goodsCode)
                .orElseThrow(() -> new GifticonGoodsNotFoundException(goodsCode));

        if (!goods.isExposed() || !goods.isSale()) {
            throw new GifticonGoodsNotFoundException(goodsCode);
        }

        return goods;
    }

    private void handleSendFailure(DeductCashAndCreateOrderContext context) {
        transactionHelper.commitFailure(context);

        try {
            cancelGifticonSendFailPort.cancelSendFailed(context.trId(), String.valueOf(context.userId()));
        } catch (Exception e) {
            log.error("발송실패 취소 API 호출 실패: trId={}, error={}", context.trId(), e.getMessage(), e);
        }
    }
}
