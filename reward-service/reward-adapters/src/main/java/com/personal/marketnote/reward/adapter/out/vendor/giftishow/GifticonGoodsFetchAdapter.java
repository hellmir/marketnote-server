package com.personal.marketnote.reward.adapter.out.vendor.giftishow;

import com.personal.marketnote.reward.adapter.out.vendor.giftishow.dto.GiftishowApiResponse;
import com.personal.marketnote.reward.adapter.out.vendor.giftishow.dto.GiftishowProductListResponse;
import com.personal.marketnote.reward.adapter.out.vendor.giftishow.dto.GiftishowProductListResponse.GiftishowProductItem;
import com.personal.marketnote.reward.port.out.gifticon.FetchGifticonGoodsPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class GifticonGoodsFetchAdapter implements FetchGifticonGoodsPort {

    private final GiftishowApiClient giftishowApiClient;

    @Override
    public FetchGifticonGoodsResult fetchProductList(int start, int size) {
        GiftishowApiResponse<GiftishowProductListResponse> response = giftishowApiClient.getProductList(start, size);

        if (!response.isSuccess()) {
            log.warn("기프티쇼 상품 리스트 조회 실패: code={}, message={}", response.code(), response.message());
            return new FetchGifticonGoodsResult(0, List.of());
        }

        GiftishowProductListResponse result = response.result();
        List<FetchedGifticonGoodsItem> items = result.goodsList().stream()
                .map(this::mapToFetchedItem)
                .toList();

        return new FetchGifticonGoodsResult(result.listTotalCnt(), items);
    }

    private FetchedGifticonGoodsItem mapToFetchedItem(GiftishowProductItem item) {
        return new FetchedGifticonGoodsItem(
                item.goodsCode(),
                item.goodsName(),
                item.goodsImgB(),
                item.brandCode(),
                item.brandName(),
                item.brandIconImg(),
                item.category1Seq(),
                item.salePrice(),
                item.realPrice(),
                item.limitDay(),
                item.content(),
                item.goodsStatus()
        );
    }
}
