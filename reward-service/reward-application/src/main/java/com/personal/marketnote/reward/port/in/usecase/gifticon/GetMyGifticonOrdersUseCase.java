package com.personal.marketnote.reward.port.in.usecase.gifticon;

import com.personal.marketnote.reward.port.in.command.gifticon.GetMyGifticonOrdersCommand;
import com.personal.marketnote.reward.port.in.result.gifticon.GetMyGifticonOrdersResult;

public interface GetMyGifticonOrdersUseCase {
    GetMyGifticonOrdersResult getMyGifticonOrders(GetMyGifticonOrdersCommand command);
}
