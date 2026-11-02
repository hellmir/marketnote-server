package com.personal.marketnote.reward.port.in.usecase.gifticon;

import com.personal.marketnote.reward.port.in.command.gifticon.GetMyGifticonOrderDetailCommand;
import com.personal.marketnote.reward.port.in.result.gifticon.GetMyGifticonOrderDetailResult;

public interface GetMyGifticonOrderDetailUseCase {
    GetMyGifticonOrderDetailResult getMyGifticonOrderDetail(GetMyGifticonOrderDetailCommand command);
}
