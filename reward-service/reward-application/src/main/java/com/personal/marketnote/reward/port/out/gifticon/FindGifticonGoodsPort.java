package com.personal.marketnote.reward.port.out.gifticon;

import com.personal.marketnote.reward.domain.gifticon.GifticonGoods;
import com.personal.marketnote.reward.domain.gifticon.GoodsStatus;

import java.util.List;
import java.util.Optional;

public interface FindGifticonGoodsPort {

    Optional<GifticonGoods> findByGoodsCode(String goodsCode);

    List<GifticonGoods> findAllByGoodsStatus(GoodsStatus goodsStatus);

    FindAllForAdminResult findAllForAdmin(int page, int pageSize, GoodsStatus goodsStatus, Boolean exposed, String keyword);

    record FindAllForAdminResult(List<GifticonGoods> items, long totalElements) {
    }

    List<GifticonGoodsBrandProjection> findDistinctBrandsByCategoryCode(String categoryCode);

    List<GifticonGoods> findAllExposed(String categoryCode, String brandCode, int page, int pageSize);

    long countAllExposed(String categoryCode, String brandCode);

    List<GifticonGoods> findAllPopularAndExposed(int limit);

    record GifticonGoodsBrandProjection(
            String brandCode,
            String brandName,
            String brandImageUrl
    ) {
    }
}
