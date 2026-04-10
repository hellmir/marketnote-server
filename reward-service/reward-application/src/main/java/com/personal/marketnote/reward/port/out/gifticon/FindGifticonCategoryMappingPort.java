package com.personal.marketnote.reward.port.out.gifticon;

import java.util.Optional;

public interface FindGifticonCategoryMappingPort {

    Optional<Long> findCategoryIdByGiftishowCategorySeq(String giftishowCategorySeq);
}
