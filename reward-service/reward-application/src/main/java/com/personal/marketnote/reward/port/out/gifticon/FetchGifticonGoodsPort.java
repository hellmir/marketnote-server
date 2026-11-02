package com.personal.marketnote.reward.port.out.gifticon;

import java.util.List;

public interface FetchGifticonGoodsPort {

    FetchGifticonGoodsResult fetchProductList(int start, int size);

    record FetchGifticonGoodsResult(
            int totalCount,
            List<FetchedGifticonGoodsItem> items
    ) {
    }

    record FetchedGifticonGoodsItem(
            String goodsCode,
            String goodsName,
            String goodsImgB,
            String brandCode,
            String brandName,
            String brandIconImg,
            String category1Seq,
            long salePrice,
            long realPrice,
            int limitDay,
            String content,
            String goodsStatus
    ) {
    }
}
