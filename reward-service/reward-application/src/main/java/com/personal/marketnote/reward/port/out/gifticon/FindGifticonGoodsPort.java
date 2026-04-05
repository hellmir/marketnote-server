package com.personal.marketnote.reward.port.out.gifticon;

import com.personal.marketnote.reward.domain.gifticon.GifticonGoods;

import java.util.List;
import java.util.Optional;

public interface FindGifticonGoodsPort {

    Optional<GifticonGoods> findByGoodsCode(String goodsCode);

    List<GifticonGoods> findAllByGoodsStatus(String goodsStatus);
}
