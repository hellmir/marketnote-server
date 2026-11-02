package com.personal.marketnote.reward.port.in.usecase.gifticon;

import com.personal.marketnote.reward.port.in.command.gifticon.PurchaseGifticonCommand;
import com.personal.marketnote.reward.port.in.result.gifticon.PurchaseGifticonResult;

public interface PurchaseGifticonUseCase {
    PurchaseGifticonResult purchase(PurchaseGifticonCommand command);
}
