package com.personal.marketnote.reward.service.gifticon;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.reward.port.in.result.gifticon.GetGiftishowBizMoneyBalanceResult;
import com.personal.marketnote.reward.port.in.usecase.gifticon.GetGiftishowBizMoneyBalanceUseCase;
import com.personal.marketnote.reward.port.out.gifticon.FetchGiftishowBizMoneyPort;
import lombok.RequiredArgsConstructor;

@UseCase
@RequiredArgsConstructor
public class GetGiftishowBizMoneyBalanceService implements GetGiftishowBizMoneyBalanceUseCase {

    private final FetchGiftishowBizMoneyPort fetchGiftishowBizMoneyPort;

    @Override
    public GetGiftishowBizMoneyBalanceResult getBalance() {
        long balance = fetchGiftishowBizMoneyPort.fetchBalance();
        return GetGiftishowBizMoneyBalanceResult.of(balance);
    }
}
