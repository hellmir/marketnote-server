package com.personal.marketnote.reward.adapter.out.vendor.giftishow;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.reward.adapter.out.vendor.giftishow.dto.GiftishowApiResponse;
import com.personal.marketnote.reward.adapter.out.vendor.giftishow.dto.GiftishowCouponDetailResponse;
import com.personal.marketnote.reward.adapter.out.vendor.giftishow.dto.GiftishowCouponSendResponse;
import com.personal.marketnote.reward.configuration.GiftishowApiProperties;
import com.personal.marketnote.reward.port.out.gifticon.CancelGifticonSendFailPort;
import com.personal.marketnote.reward.port.out.gifticon.QueryGifticonCouponStatusPort;
import com.personal.marketnote.reward.port.out.gifticon.SendGifticonCouponPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class GifticonCouponAdapter implements SendGifticonCouponPort, CancelGifticonSendFailPort, QueryGifticonCouponStatusPort {

    private final GiftishowApiClient giftishowApiClient;
    private final GiftishowApiProperties properties;

    @Override
    public SendCouponResult sendCoupon(String trId, String goodsCode, String userId) {
        try {
            GiftishowApiResponse<GiftishowCouponSendResponse> response = giftishowApiClient.sendCoupon(
                trId, goodsCode, properties.getPhoneNo(), userId,
                properties.getMmsMsg(), properties.getMmsTitle(), null
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

            GiftishowCouponSendResponse outer = response.result();
            if (FormatValidator.hasNoValue(outer) || FormatValidator.hasNoValue(outer.result())) {
                log.error("기프티쇼 쿠폰 발송 응답 본문(inner result) 누락: trId={}", trId);
                return SendCouponResult.builder()
                    .success(false)
                    .errorCode("RESPONSE_EMPTY")
                    .errorMessage("기프티쇼 쿠폰 발송 응답 본문이 비어 있습니다.")
                    .build();
            }
            GiftishowCouponSendResponse.InnerResult inner = outer.result();
            return SendCouponResult.builder()
                .success(true)
                .orderNo(inner.orderNo())
                .pinNo(inner.pinNo())
                .couponImageUrl(inner.couponImgUrl())
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
    public CouponStatusResult queryStatus(String trId) {
        try {
            GiftishowApiResponse<List<GiftishowCouponDetailResponse>> response =
                giftishowApiClient.getCouponDetail(trId);

            if (!response.isSuccess()) {
                log.error("기프티쇼 쿠폰 상세 조회 실패: trId={}, code={}, message={}",
                    trId, response.code(), response.message());
                return CouponStatusResult.builder()
                    .success(false)
                    .errorCode(response.code())
                    .errorMessage(response.message())
                    .build();
            }

            GiftishowCouponDetailResponse.CouponInfo info = extractFirstCouponInfo(response.result());
            if (FormatValidator.hasNoValue(info)) {
                log.error("기프티쇼 쿠폰 상세 조회 응답 본문(couponInfoList) 누락: trId={}", trId);
                return CouponStatusResult.builder()
                    .success(false)
                    .errorCode("RESPONSE_EMPTY")
                    .errorMessage("기프티쇼 쿠폰 상세 조회 응답이 비어 있습니다.")
                    .build();
            }
            return CouponStatusResult.builder()
                .success(true)
                .pinStatusCd(info.pinStatusCd())
                .validPrdEndDt(info.validPrdEndDt())
                .build();

        } catch (Exception e) {
            log.error("기프티쇼 쿠폰 상세 조회 통신 오류: trId={}, error={}", trId, e.getMessage(), e);
            return CouponStatusResult.builder()
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

    private GiftishowCouponDetailResponse.CouponInfo extractFirstCouponInfo(
        List<GiftishowCouponDetailResponse> result
    ) {
        if (FormatValidator.hasNoValue(result) || result.isEmpty()) {
            return null;
        }
        GiftishowCouponDetailResponse first = result.getFirst();
        if (FormatValidator.hasNoValue(first) || FormatValidator.hasNoValue(first.couponInfoList())
            || first.couponInfoList().isEmpty()) {
            return null;
        }
        return first.couponInfoList().getFirst();
    }
}
