package com.personal.marketnote.reward.port.out.gifticon;

import com.personal.marketnote.reward.domain.gifticon.GifticonCategory;

import java.util.Optional;

public interface FindGifticonCategoryPort {

    Optional<GifticonCategory> findByCategoryCode(String categoryCode);

    Optional<GifticonCategory> findById(Long id);
}
