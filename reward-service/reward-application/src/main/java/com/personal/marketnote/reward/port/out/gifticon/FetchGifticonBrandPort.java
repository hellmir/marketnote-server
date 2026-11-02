package com.personal.marketnote.reward.port.out.gifticon;

import java.util.List;

public interface FetchGifticonBrandPort {

    FetchGifticonBrandResult fetchBrandList();

    record FetchGifticonBrandResult(
            int totalCount,
            List<FetchedGifticonBrandItem> items
    ) {
    }

    record FetchedGifticonBrandItem(
            String brandCode,
            String brandName,
            String brandIconImg,
            String category1Seq,
            String category1Name
    ) {
    }
}
