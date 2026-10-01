package com.personal.marketnote.reward.adapter.out.vendor.giftishow;

import com.personal.marketnote.reward.adapter.out.vendor.giftishow.dto.GiftishowApiResponse;
import com.personal.marketnote.reward.adapter.out.vendor.giftishow.dto.GiftishowCouponDetailResponse;
import com.personal.marketnote.reward.adapter.out.vendor.giftishow.dto.GiftishowCouponSendResponse;
import com.personal.marketnote.reward.configuration.GiftishowApiProperties;
import com.personal.marketnote.reward.port.out.gifticon.QueryGifticonCouponStatusPort.CouponStatusResult;
import com.personal.marketnote.reward.port.out.gifticon.SendGifticonCouponPort.SendCouponResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("GifticonCouponAdapter 테스트")
class GifticonCouponAdapterTest {

    private static final String TR_ID = "NTCASH_1_20260403120000";
    private static final String GOODS_CODE = "G001";
    private static final String USER_ID = "user1";
    private static final String PHONE_NO = "0000000000";
    private static final String MMS_MSG = "기프티콘 발송 안내";
    private static final String MMS_TITLE = "마켓노트";

    @Mock
    private GiftishowApiClient giftishowApiClient;

    private GiftishowApiProperties properties;
    private GifticonCouponAdapter adapter;

    @BeforeEach
    void setUp() {
        properties = new GiftishowApiProperties();
        properties.setPhoneNo(PHONE_NO);
        properties.setMmsMsg(MMS_MSG);
        properties.setMmsTitle(MMS_TITLE);
        adapter = new GifticonCouponAdapter(giftishowApiClient, properties);
    }

    @Nested
    @DisplayName("sendCoupon")
    class SendCouponTest {

        @Test
        @DisplayName("벤더 응답이 성공이면 핀번호와 발급 정보를 담은 SendCouponResult를 반환한다")
        void shouldReturnSuccessResultWhenVendorRespondsSuccess() {
            // given
            GiftishowCouponSendResponse vendorResult = new GiftishowCouponSendResponse(
                    TR_ID,
                    "ORD001",
                    "1234-5678-9012",
                    "http://img.test.com/coupon.jpg",
                    "20260503"
            );
            given(giftishowApiClient.sendCoupon(eq(TR_ID), eq(GOODS_CODE), eq(PHONE_NO), eq(USER_ID), eq(MMS_MSG), eq(MMS_TITLE)))
                    .willReturn(new GiftishowApiResponse<>("0000", "success", vendorResult));

            // when
            SendCouponResult result = adapter.sendCoupon(TR_ID, GOODS_CODE, USER_ID);

            // then
            assertThat(result.success()).isTrue();
            assertThat(result.orderNo()).isEqualTo("ORD001");
            assertThat(result.pinNo()).isEqualTo("1234-5678-9012");
            assertThat(result.couponImageUrl()).isEqualTo("http://img.test.com/coupon.jpg");
            assertThat(result.validEndDate()).isEqualTo("20260503");
            assertThat(result.errorCode()).isNull();
            assertThat(result.errorMessage()).isNull();
            verify(giftishowApiClient).sendCoupon(TR_ID, GOODS_CODE, PHONE_NO, USER_ID, MMS_MSG, MMS_TITLE);
        }

        @Test
        @DisplayName("벤더 응답 코드가 0000이 아니면 실패 코드와 메시지를 담은 결과를 반환한다")
        void shouldReturnFailResultWhenVendorRespondsErrorCode() {
            // given
            given(giftishowApiClient.sendCoupon(any(), any(), any(), any(), any(), any()))
                    .willReturn(new GiftishowApiResponse<>("9999", "잔액 부족", null));

            // when
            SendCouponResult result = adapter.sendCoupon(TR_ID, GOODS_CODE, USER_ID);

            // then
            assertThat(result.success()).isFalse();
            assertThat(result.errorCode()).isEqualTo("9999");
            assertThat(result.errorMessage()).isEqualTo("잔액 부족");
            assertThat(result.orderNo()).isNull();
            assertThat(result.pinNo()).isNull();
        }

        @Test
        @DisplayName("벤더 호출 중 예외가 발생하면 COMM_ERROR 코드를 담은 실패 결과를 반환한다")
        void shouldReturnCommErrorWhenVendorThrowsException() {
            // given
            given(giftishowApiClient.sendCoupon(any(), any(), any(), any(), any(), any()))
                    .willThrow(new RuntimeException("connection refused"));

            // when
            SendCouponResult result = adapter.sendCoupon(TR_ID, GOODS_CODE, USER_ID);

            // then
            assertThat(result.success()).isFalse();
            assertThat(result.errorCode()).isEqualTo("COMM_ERROR");
            assertThat(result.errorMessage()).isEqualTo("connection refused");
        }
    }

    @Nested
    @DisplayName("queryStatus")
    class QueryStatusTest {

        @Test
        @DisplayName("벤더 응답이 성공이면 핀 상태와 유효기간을 담은 CouponStatusResult를 반환한다")
        void shouldReturnSuccessResultWhenVendorRespondsSuccess() {
            // given
            GiftishowCouponDetailResponse detail = new GiftishowCouponDetailResponse(
                    TR_ID, "ORD001", "01", "발행", "20260503", GOODS_CODE, "테스트 상품"
            );
            given(giftishowApiClient.getCouponDetail(TR_ID))
                    .willReturn(new GiftishowApiResponse<>("0000", "success", detail));

            // when
            CouponStatusResult result = adapter.queryStatus(TR_ID);

            // then
            assertThat(result.success()).isTrue();
            assertThat(result.pinStatusCd()).isEqualTo("01");
            assertThat(result.validPrdEndDt()).isEqualTo("20260503");
            assertThat(result.errorCode()).isNull();
            verify(giftishowApiClient).getCouponDetail(TR_ID);
        }

        @Test
        @DisplayName("벤더 응답 코드가 0000이 아니면 실패 결과를 반환한다")
        void shouldReturnFailResultWhenVendorRespondsErrorCode() {
            // given
            given(giftishowApiClient.getCouponDetail(TR_ID))
                    .willReturn(new GiftishowApiResponse<>("8888", "조회 실패", null));

            // when
            CouponStatusResult result = adapter.queryStatus(TR_ID);

            // then
            assertThat(result.success()).isFalse();
            assertThat(result.errorCode()).isEqualTo("8888");
            assertThat(result.errorMessage()).isEqualTo("조회 실패");
            assertThat(result.pinStatusCd()).isNull();
        }

        @Test
        @DisplayName("벤더 호출 중 예외가 발생하면 COMM_ERROR 코드를 담은 실패 결과를 반환한다")
        void shouldReturnCommErrorWhenVendorThrowsException() {
            // given
            given(giftishowApiClient.getCouponDetail(TR_ID))
                    .willThrow(new RuntimeException("timeout"));

            // when
            CouponStatusResult result = adapter.queryStatus(TR_ID);

            // then
            assertThat(result.success()).isFalse();
            assertThat(result.errorCode()).isEqualTo("COMM_ERROR");
            assertThat(result.errorMessage()).isEqualTo("timeout");
        }
    }

    @Nested
    @DisplayName("cancelSendFailed")
    class CancelSendFailedTest {

        @Test
        @DisplayName("벤더 호출이 정상이면 예외 없이 종료한다")
        void shouldCallVendorWhenSucceeds() {
            // when & then
            assertThatCode(() -> adapter.cancelSendFailed(TR_ID, USER_ID)).doesNotThrowAnyException();
            verify(giftishowApiClient).cancelSendFailedCoupon(TR_ID, USER_ID);
        }

        @Test
        @DisplayName("벤더 호출 중 예외가 발생해도 호출자에게 예외를 전파하지 않는다")
        void shouldSwallowExceptionWhenVendorThrows() {
            // given
            org.mockito.Mockito.doThrow(new RuntimeException("api down"))
                    .when(giftishowApiClient).cancelSendFailedCoupon(TR_ID, USER_ID);

            // when & then
            assertThatCode(() -> adapter.cancelSendFailed(TR_ID, USER_ID)).doesNotThrowAnyException();
        }
    }
}
