package com.personal.marketnote.reward.port.in.result.gifticon;

import com.personal.marketnote.reward.port.out.gifticon.FindGifticonGoodsPort.FindAllForAdminResult;

import java.util.List;

public record GetAdminGifticonGoodsResult(
        int page,
        int pageSize,
        long totalElements,
        int totalPages,
        List<GifticonGoodsItemResult> items
) {
    public static GetAdminGifticonGoodsResult from(int page, int pageSize, FindAllForAdminResult portResult) {
        int totalPages = pageSize == 0 ? 0 : (int) Math.ceil((double) portResult.totalElements() / pageSize);
        List<GifticonGoodsItemResult> items = portResult.items().stream()
                .map(GifticonGoodsItemResult::from)
                .toList();
        return new GetAdminGifticonGoodsResult(page, pageSize, portResult.totalElements(), totalPages, items);
    }
}
