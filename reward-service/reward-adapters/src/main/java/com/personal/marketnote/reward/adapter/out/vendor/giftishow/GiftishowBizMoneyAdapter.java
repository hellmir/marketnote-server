package com.personal.marketnote.reward.adapter.out.vendor.giftishow;

import com.personal.marketnote.reward.adapter.out.vendor.giftishow.dto.GiftishowApiResponse;
import com.personal.marketnote.reward.adapter.out.vendor.giftishow.dto.GiftishowBizMoneyResponse;
import com.personal.marketnote.reward.configuration.GiftishowApiProperties;
import com.personal.marketnote.reward.port.out.gifticon.FetchGifticonVendorBalancePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GiftishowBizMoneyAdapter implements FetchGifticonVendorBalancePort {

    private final GiftishowApiClient giftishowApiClient;
    private final GiftishowApiProperties properties;

    @Override
    public long fetchBalance() {
        GiftishowApiResponse<GiftishowBizMoneyResponse> response =
                giftishowApiClient.getBizMoneyBalance(properties.getUserId());

        if (!response.isSuccess()) {
            log.error("기프티쇼 비즈머니 잔액 조회 실패: code={}, message={}", response.code(), response.message());
            return 0L;
        }

        return response.result().bizMoney();
    }
}
