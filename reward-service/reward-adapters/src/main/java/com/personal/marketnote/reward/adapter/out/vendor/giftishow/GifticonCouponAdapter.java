package com.personal.marketnote.reward.adapter.out.vendor.giftishow;

import com.personal.marketnote.reward.adapter.out.vendor.giftishow.dto.GiftishowApiResponse;
import com.personal.marketnote.reward.adapter.out.vendor.giftishow.dto.GiftishowCouponSendResponse;
import com.personal.marketnote.reward.configuration.GiftishowApiProperties;
import com.personal.marketnote.reward.port.out.gifticon.CancelGifticonSendFailPort;
import com.personal.marketnote.reward.port.out.gifticon.SendGifticonCouponPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class GifticonCouponAdapter implements SendGifticonCouponPort, CancelGifticonSendFailPort {

    private final GiftishowApiClient giftishowApiClient;
    private final GiftishowApiProperties properties;

    @Override
    public SendCouponResult sendCoupon(String trId, String goodsCode, String userId) {
        try {
            GiftishowApiResponse<GiftishowCouponSendResponse> response = giftishowApiClient.sendCoupon(
                    trId, goodsCode, properties.getPhoneNo(), userId,
                    properties.getMmsMsg(), properties.getMmsTitle()
            );

            if (!response.isSuccess()) {
                log.error("기프티쇼 쿠폰 발송 실패: trId={}, code={}, message={}",
                        trId, response.code(), response.message());
                return SendCouponResult.builder()
                        .success(false)
                        .errorCode(response.code())
                        .errorMessage(response.message())
                        .build();
            }

            GiftishowCouponSendResponse result = response.result();
            return SendCouponResult.builder()
                    .success(true)
                    .orderNo(result.orderNo())
                    .pinNo(result.pinNo())
                    .couponImageUrl(result.couponImgUrl())
                    .validEndDate(result.validPrdEndDt())
                    .build();

        } catch (Exception e) {
            log.error("기프티쇼 쿠폰 발송 통신 오류: trId={}, error={}", trId, e.getMessage(), e);
            return SendCouponResult.builder()
                    .success(false)
                    .errorCode("COMM_ERROR")
                    .errorMessage(e.getMessage())
                    .build();
        }
    }

    @Override
    public void cancelSendFailed(String trId, String userId) {
        try {
            giftishowApiClient.cancelSendFailedCoupon(trId, userId);
            log.info("기프티쇼 발송실패 취소 완료: trId={}", trId);
        } catch (Exception e) {
            log.error("기프티쇼 발송실패 취소 API 호출 실패: trId={}, error={}", trId, e.getMessage(), e);
        }
    }
}
