package com.personal.marketnote.reward.port.out.gifticon;

import com.personal.marketnote.reward.domain.gifticon.GifticonOrder;

import java.util.Optional;

public interface FindGifticonOrderPort {
    Optional<GifticonOrder> findByTrId(String trId);

    boolean existsByTrId(String trId);
}
