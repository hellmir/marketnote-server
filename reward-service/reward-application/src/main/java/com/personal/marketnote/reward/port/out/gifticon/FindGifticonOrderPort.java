package com.personal.marketnote.reward.port.out.gifticon;

import com.personal.marketnote.reward.domain.gifticon.GifticonOrder;
import com.personal.marketnote.reward.domain.gifticon.GifticonOrderSortType;
import com.personal.marketnote.reward.domain.gifticon.GifticonOrderStatus;

import java.util.List;
import java.util.Optional;

public interface FindGifticonOrderPort {
    Optional<GifticonOrder> findByTrId(String trId);

    boolean existsByTrId(String trId);

    List<GifticonOrder> findByUserIdAndStatuses(Long userId, List<GifticonOrderStatus> statuses,
                                                GifticonOrderSortType sortType, Long cursor, int pageSize);

    long countByUserIdAndStatuses(Long userId, List<GifticonOrderStatus> statuses);

    Optional<GifticonOrder> findByIdAndUserId(Long id, Long userId);
}
