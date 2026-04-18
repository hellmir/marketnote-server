package com.personal.marketnote.reward.service.gifticon;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.reward.port.in.result.gifticon.GetGifticonVendorBalanceResult;
import com.personal.marketnote.reward.port.in.usecase.gifticon.GetGifticonVendorBalanceUseCase;
import com.personal.marketnote.reward.port.out.gifticon.FetchGifticonVendorBalancePort;
import lombok.RequiredArgsConstructor;

@UseCase
@RequiredArgsConstructor
public class GetGifticonVendorBalanceService implements GetGifticonVendorBalanceUseCase {

    private final FetchGifticonVendorBalancePort fetchGifticonVendorBalancePort;

    @Override
    public GetGifticonVendorBalanceResult getBalance() {
        long balance = fetchGifticonVendorBalancePort.fetchBalance();
        return GetGifticonVendorBalanceResult.of(balance);
    }
}
