package com.personal.marketnote.reward.port.out.gifticon;

import com.personal.marketnote.reward.domain.gifticon.GifticonGoods;

import java.util.List;
import java.util.Optional;

public interface FindGifticonGoodsPort {

    Optional<GifticonGoods> findByGoodsCode(String goodsCode);

    List<GifticonGoods> findAllByGoodsStatus(String goodsStatus);

    FindAllForAdminResult findAllForAdmin(int page, int pageSize, String goodsStatus, Boolean exposed, String keyword);

    record FindAllForAdminResult(List<GifticonGoods> items, long totalElements) {
    }

    List<GifticonGoodsBrandProjection> findDistinctBrandsByCategoryCode(String categoryCode);

    List<GifticonGoods> findAllExposed(String categoryCode, String brandCode, int page, int pageSize);

    long countAllExposed(String categoryCode, String brandCode);

    record GifticonGoodsBrandProjection(
            String brandCode,
            String brandName,
            String brandImageUrl
    ) {
    }
}
