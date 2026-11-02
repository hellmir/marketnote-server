package com.personal.marketnote.reward.adapter.out.vendor.giftishow;

import com.personal.marketnote.reward.adapter.out.vendor.giftishow.dto.GiftishowApiResponse;
import com.personal.marketnote.reward.adapter.out.vendor.giftishow.dto.GiftishowBrandListResponse;
import com.personal.marketnote.reward.adapter.out.vendor.giftishow.dto.GiftishowBrandListResponse.GiftishowBrandItem;
import com.personal.marketnote.reward.port.out.gifticon.FetchGifticonBrandPort.FetchGifticonBrandResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("GifticonBrandFetchAdapter 테스트")
class GifticonBrandFetchAdapterTest {

    @Mock
    private GiftishowApiClient giftishowApiClient;

    @InjectMocks
    private GifticonBrandFetchAdapter adapter;

    @Test
    @DisplayName("벤더 응답이 성공이면 브랜드 항목을 매핑한 결과를 반환한다")
    void shouldReturnMappedItemsWhenVendorRespondsSuccess() {
        // given
        GiftishowBrandItem item = new GiftishowBrandItem("B001", "스타벅스", "http://img.test.com/brand.jpg", "1", "카페/음료");
        GiftishowBrandListResponse vendorResult = new GiftishowBrandListResponse(1, List.of(item));
        given(giftishowApiClient.getBrandList())
                .willReturn(new GiftishowApiResponse<>("0000", "success", vendorResult));

        // when
        FetchGifticonBrandResult result = adapter.fetchBrandList();

        // then
        assertThat(result.totalCount()).isEqualTo(1);
        assertThat(result.items()).hasSize(1);
        assertThat(result.items().getFirst().brandCode()).isEqualTo("B001");
        assertThat(result.items().getFirst().brandName()).isEqualTo("스타벅스");
        assertThat(result.items().getFirst().brandIconImg()).isEqualTo("http://img.test.com/brand.jpg");
        assertThat(result.items().getFirst().category1Seq()).isEqualTo("1");
        assertThat(result.items().getFirst().category1Name()).isEqualTo("카페/음료");
        verify(giftishowApiClient).getBrandList();
    }

    @Test
    @DisplayName("벤더 응답이 다건이면 입력 순서를 유지한 채 결과를 반환한다")
    void shouldPreserveOrderWhenVendorReturnsMultipleItems() {
        // given
        GiftishowBrandItem item1 = new GiftishowBrandItem("B001", "스타벅스", "img1", "1", "카페/음료");
        GiftishowBrandItem item2 = new GiftishowBrandItem("B002", "베스킨라빈스", "img2", "2", "디저트");
        given(giftishowApiClient.getBrandList())
                .willReturn(new GiftishowApiResponse<>("0000", "success",
                        new GiftishowBrandListResponse(2, List.of(item1, item2))));

        // when
        FetchGifticonBrandResult result = adapter.fetchBrandList();

        // then
        assertThat(result.totalCount()).isEqualTo(2);
        assertThat(result.items()).extracting("brandCode").containsExactly("B001", "B002");
    }

    @Test
    @DisplayName("벤더 응답 코드가 0000이 아니면 빈 결과를 반환한다")
    void shouldReturnEmptyResultWhenVendorRespondsErrorCode() {
        // given
        given(giftishowApiClient.getBrandList())
                .willReturn(new GiftishowApiResponse<>("9999", "인증 실패", null));

        // when
        FetchGifticonBrandResult result = adapter.fetchBrandList();

        // then
        assertThat(result.totalCount()).isZero();
        assertThat(result.items()).isEmpty();
    }
}
