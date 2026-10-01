package com.personal.marketnote.reward.adapter.out.vendor.giftishow;

import com.personal.marketnote.reward.adapter.out.vendor.giftishow.dto.GiftishowApiResponse;
import com.personal.marketnote.reward.adapter.out.vendor.giftishow.dto.GiftishowBizMoneyResponse;
import com.personal.marketnote.reward.configuration.GiftishowApiProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("GiftishowBizMoneyAdapter 테스트")
class GiftishowBizMoneyAdapterTest {

    private static final String VENDOR_USER_ID = "vendor-user-1";

    @Mock
    private GiftishowApiClient giftishowApiClient;

    private GiftishowApiProperties properties;
    private GiftishowBizMoneyAdapter adapter;

    @BeforeEach
    void setUp() {
        properties = new GiftishowApiProperties();
        properties.setUserId(VENDOR_USER_ID);
        adapter = new GiftishowBizMoneyAdapter(giftishowApiClient, properties);
    }

    @Test
    @DisplayName("벤더 응답이 성공이면 비즈머니 잔액을 반환한다")
    void shouldReturnBalanceWhenVendorRespondsSuccess() {
        // given
        given(giftishowApiClient.getBizMoneyBalance(VENDOR_USER_ID))
                .willReturn(new GiftishowApiResponse<>("0000", "success",
                        new GiftishowBizMoneyResponse(VENDOR_USER_ID, 500_000L)));

        // when
        long balance = adapter.fetchBalance();

        // then
        assertThat(balance).isEqualTo(500_000L);
        verify(giftishowApiClient).getBizMoneyBalance(VENDOR_USER_ID);
    }

    @Test
    @DisplayName("벤더 응답 코드가 0000이 아니면 0을 반환한다")
    void shouldReturnZeroWhenVendorRespondsErrorCode() {
        // given
        given(giftishowApiClient.getBizMoneyBalance(VENDOR_USER_ID))
                .willReturn(new GiftishowApiResponse<>("9999", "인증 실패", null));

        // when
        long balance = adapter.fetchBalance();

        // then
        assertThat(balance).isZero();
    }
}
