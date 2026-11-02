package com.personal.marketnote.reward.adapter.out.vendor.giftishow;

import com.personal.marketnote.reward.adapter.out.vendor.giftishow.dto.GiftishowApiResponse;
import com.personal.marketnote.reward.adapter.out.vendor.giftishow.dto.GiftishowBrandListResponse;
import com.personal.marketnote.reward.adapter.out.vendor.giftishow.dto.GiftishowBrandListResponse.GiftishowBrandItem;
import com.personal.marketnote.reward.port.out.gifticon.FetchGifticonBrandPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class GifticonBrandFetchAdapter implements FetchGifticonBrandPort {

    private final GiftishowApiClient giftishowApiClient;

    @Override
    public FetchGifticonBrandResult fetchBrandList() {
        GiftishowApiResponse<GiftishowBrandListResponse> response = giftishowApiClient.getBrandList();

        if (!response.isSuccess()) {
            log.warn("기프티쇼 브랜드 조회 실패: code={}, message={}", response.code(), response.message());
            return new FetchGifticonBrandResult(0, List.of());
        }

        GiftishowBrandListResponse result = response.result();
        List<FetchedGifticonBrandItem> items = result.brandList().stream()
                .map(this::mapToFetchedItem)
                .toList();

        return new FetchGifticonBrandResult(result.listTotalCnt(), items);
    }

    private FetchedGifticonBrandItem mapToFetchedItem(GiftishowBrandItem item) {
        return new FetchedGifticonBrandItem(
                item.brandCode(),
                item.brandName(),
                item.brandIconImg(),
                item.category1Seq(),
                item.category1Name()
        );
    }
}
