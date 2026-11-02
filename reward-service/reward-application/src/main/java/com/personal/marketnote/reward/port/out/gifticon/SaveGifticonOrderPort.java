package com.personal.marketnote.reward.port.out.gifticon;

import com.personal.marketnote.reward.domain.gifticon.GifticonOrder;

public interface SaveGifticonOrderPort {
    GifticonOrder save(GifticonOrder order);
}
