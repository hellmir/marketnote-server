package com.personal.marketnote.reward.adapter.out.vendor.giftishow;

import com.personal.marketnote.reward.adapter.out.vendor.giftishow.dto.GiftishowApiResponse;
import com.personal.marketnote.reward.adapter.out.vendor.giftishow.dto.GiftishowProductListResponse;
import com.personal.marketnote.reward.adapter.out.vendor.giftishow.dto.GiftishowProductListResponse.GiftishowProductItem;
import com.personal.marketnote.reward.port.out.gifticon.FetchGifticonGoodsPort.FetchGifticonGoodsResult;
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
@DisplayName("GifticonGoodsFetchAdapter 테스트")
class GifticonGoodsFetchAdapterTest {

    @Mock
    private GiftishowApiClient giftishowApiClient;

    @InjectMocks
    private GifticonGoodsFetchAdapter adapter;

    @Test
    @DisplayName("벤더 응답이 성공이면 상품 항목을 매핑한 결과를 반환한다")
    void shouldReturnMappedItemsWhenVendorRespondsSuccess() {
        // given
        GiftishowProductItem item = new GiftishowProductItem(
                "G001", "테스트 상품", "http://img.test.com/goods.jpg",
                "B001", "스타벅스", "http://img.test.com/brand.jpg",
                "1", 10000L, 12000L, 30, "테스트 상품 설명", "SALE"
        );
        given(giftishowApiClient.getProductList(0, 20))
                .willReturn(new GiftishowApiResponse<>("0000", "success",
                        new GiftishowProductListResponse(1, List.of(item))));

        // when
        FetchGifticonGoodsResult result = adapter.fetchProductList(0, 20);

        // then
        assertThat(result.totalCount()).isEqualTo(1);
        assertThat(result.items()).hasSize(1);
        assertThat(result.items().getFirst().goodsCode()).isEqualTo("G001");
        assertThat(result.items().getFirst().goodsName()).isEqualTo("테스트 상품");
        assertThat(result.items().getFirst().brandCode()).isEqualTo("B001");
        assertThat(result.items().getFirst().salePrice()).isEqualTo(10000L);
        assertThat(result.items().getFirst().realPrice()).isEqualTo(12000L);
        assertThat(result.items().getFirst().limitDay()).isEqualTo(30);
        assertThat(result.items().getFirst().goodsStatus()).isEqualTo("SALE");
        verify(giftishowApiClient).getProductList(0, 20);
    }

    @Test
    @DisplayName("페이징 파라미터를 그대로 벤더 호출에 전달한다")
    void shouldPassPagingParametersToVendor() {
        // given
        given(giftishowApiClient.getProductList(40, 10))
                .willReturn(new GiftishowApiResponse<>("0000", "success",
                        new GiftishowProductListResponse(0, List.of())));

        // when
        adapter.fetchProductList(40, 10);

        // then
        verify(giftishowApiClient).getProductList(40, 10);
    }

    @Test
    @DisplayName("벤더 응답 코드가 0000이 아니면 빈 결과를 반환한다")
    void shouldReturnEmptyResultWhenVendorRespondsErrorCode() {
        // given
        given(giftishowApiClient.getProductList(0, 20))
                .willReturn(new GiftishowApiResponse<>("9999", "인증 실패", null));

        // when
        FetchGifticonGoodsResult result = adapter.fetchProductList(0, 20);

        // then
        assertThat(result.totalCount()).isZero();
        assertThat(result.items()).isEmpty();
    }
}
