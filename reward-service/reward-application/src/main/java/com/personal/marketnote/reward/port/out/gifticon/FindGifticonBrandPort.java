package com.personal.marketnote.reward.port.out.gifticon;

import com.personal.marketnote.reward.domain.gifticon.GifticonBrand;

import java.util.Optional;

public interface FindGifticonBrandPort {

    Optional<GifticonBrand> findByBrandCode(String brandCode);
}
