package com.personal.marketnote.reward.port.in.result.gifticon;

import com.personal.marketnote.reward.domain.gifticon.GifticonGoods;

import java.time.LocalDateTime;

public record GifticonGoodsItemResult(
        String goodsCode,
        String goodsName,
        String brandCode,
        String brandName,
        String categoryCode,
        Long realPrice,
        Long salePrice,
        Long cashPrice,
        String goodsStatus,
        boolean exposed,
        Integer orderNum,
        String imageUrl,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt
) {
    public static GifticonGoodsItemResult from(GifticonGoods goods) {
        return new GifticonGoodsItemResult(
                goods.getGoodsCode(),
                goods.getGoodsName(),
                goods.getBrandCode(),
                goods.getBrandName(),
                goods.getCategoryCode(),
                goods.getRealPrice(),
                goods.getSalePrice(),
                goods.getCashPrice(),
                goods.getGoodsStatus(),
                goods.isExposed(),
                goods.getOrderNum(),
                goods.getImageUrl(),
                goods.getCreatedAt(),
                goods.getModifiedAt()
        );
    }
}
