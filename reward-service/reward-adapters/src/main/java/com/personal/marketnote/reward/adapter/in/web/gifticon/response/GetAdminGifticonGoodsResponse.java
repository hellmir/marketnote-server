package com.personal.marketnote.reward.adapter.in.web.gifticon.response;

import com.personal.marketnote.common.adapter.in.response.OffsetResponse;
import com.personal.marketnote.reward.port.in.result.gifticon.GetAdminGifticonGoodsResult;
import com.personal.marketnote.reward.port.in.result.gifticon.GifticonGoodsItemResult;

import java.time.LocalDateTime;

public record GetAdminGifticonGoodsResponse(OffsetResponse<GifticonGoodsItem> goods) {

    public static GetAdminGifticonGoodsResponse from(GetAdminGifticonGoodsResult result) {
        return new GetAdminGifticonGoodsResponse(
                new OffsetResponse<>(
                        result.page(),
                        result.pageSize(),
                        result.totalElements(),
                        result.totalPages(),
                        result.items().stream()
                                .map(GifticonGoodsItem::from)
                                .toList()
                )
        );
    }

    public record GifticonGoodsItem(
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
        public static GifticonGoodsItem from(GifticonGoodsItemResult item) {
            return new GifticonGoodsItem(
                    item.goodsCode(),
                    item.goodsName(),
                    item.brandCode(),
                    item.brandName(),
                    item.categoryCode(),
                    item.realPrice(),
                    item.salePrice(),
                    item.cashPrice(),
                    item.goodsStatus(),
                    item.exposed(),
                    item.orderNum(),
                    item.imageUrl(),
                    item.createdAt(),
                    item.modifiedAt()
            );
        }
    }
}
