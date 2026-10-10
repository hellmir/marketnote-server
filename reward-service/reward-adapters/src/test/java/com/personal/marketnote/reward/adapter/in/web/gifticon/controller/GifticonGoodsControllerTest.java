package com.personal.marketnote.reward.adapter.in.web.gifticon.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.reward.adapter.in.web.gifticon.response.GetGifticonGoodsDetailResponse;
import com.personal.marketnote.reward.adapter.in.web.gifticon.response.GetGifticonGoodsResponse;
import com.personal.marketnote.reward.adapter.in.web.gifticon.response.GetPopularGifticonGoodsResponse;
import com.personal.marketnote.reward.port.in.command.gifticon.GetGifticonGoodsCommand;
import com.personal.marketnote.reward.port.in.command.gifticon.GetGifticonGoodsDetailCommand;
import com.personal.marketnote.reward.port.in.result.gifticon.GetGifticonGoodsDetailResult;
import com.personal.marketnote.reward.port.in.result.gifticon.GetGifticonGoodsResult;
import com.personal.marketnote.reward.port.in.result.gifticon.GetPopularGifticonGoodsResult;
import com.personal.marketnote.reward.port.in.usecase.gifticon.GetGifticonGoodsDetailUseCase;
import com.personal.marketnote.reward.port.in.usecase.gifticon.GetGifticonGoodsUseCase;
import com.personal.marketnote.reward.port.in.usecase.gifticon.GetPopularGifticonGoodsUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("GifticonGoodsController 테스트")
class GifticonGoodsControllerTest {

    @Mock
    private GetGifticonGoodsUseCase getGifticonGoodsUseCase;
    @Mock
    private GetGifticonGoodsDetailUseCase getGifticonGoodsDetailUseCase;
    @Mock
    private GetPopularGifticonGoodsUseCase getPopularGifticonGoodsUseCase;

    @InjectMocks
    private GifticonGoodsController controller;

    private OAuth2AuthenticatedPrincipal buildPrincipal(String userId) {
        return new DefaultOAuth2AuthenticatedPrincipal(
                userId,
                Map.of("name", userId),
                List.of(new SimpleGrantedAuthority("ROLE_BUYER"))
        );
    }

    @Nested
    @DisplayName("GET /api/v1/gifticon/goods")
    class GetGoods {

        @Test
        @DisplayName("카테고리/브랜드/커서/페이지크기 파라미터를 UseCase Command에 그대로 전달하고 OK를 반환한다")
        void returnsOkAndPassesParams() {
            given(getGifticonGoodsUseCase.getGoods(any(GetGifticonGoodsCommand.class)))
                    .willReturn(GetGifticonGoodsResult.from(null, false, null, List.of()));

            ResponseEntity<BaseResponse<GetGifticonGoodsResponse>> response =
                    controller.getGoods("CAT-01", "BRAND-01", 5L, 10);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().getContent()).isNotNull();
            ArgumentCaptor<GetGifticonGoodsCommand> captor = ArgumentCaptor.forClass(GetGifticonGoodsCommand.class);
            verify(getGifticonGoodsUseCase).getGoods(captor.capture());
            assertThat(captor.getValue().categoryCode()).isEqualTo("CAT-01");
            assertThat(captor.getValue().brandCode()).isEqualTo("BRAND-01");
            assertThat(captor.getValue().cursor()).isEqualTo(5L);
            assertThat(captor.getValue().pageSize()).isEqualTo(10);
        }

        @Test
        @DisplayName("카테고리/브랜드가 null이고 커서가 -1(첫 페이지)이어도 OK를 반환한다")
        void returnsOkWhenFiltersNullAndFirstPageCursor() {
            given(getGifticonGoodsUseCase.getGoods(any(GetGifticonGoodsCommand.class)))
                    .willReturn(GetGifticonGoodsResult.from(0L, false, null, List.of()));

            ResponseEntity<BaseResponse<GetGifticonGoodsResponse>> response =
                    controller.getGoods(null, null, -1L, 20);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            ArgumentCaptor<GetGifticonGoodsCommand> captor = ArgumentCaptor.forClass(GetGifticonGoodsCommand.class);
            verify(getGifticonGoodsUseCase).getGoods(captor.capture());
            assertThat(captor.getValue().cursor()).isEqualTo(-1L);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/gifticon/goods/popular")
    class GetPopularGoods {

        @Test
        @DisplayName("OK 상태로 인기 상품 목록을 반환한다")
        void returnsOkWithPopularGoods() {
            given(getPopularGifticonGoodsUseCase.getPopularGoods())
                    .willReturn(new GetPopularGifticonGoodsResult(List.of()));

            ResponseEntity<BaseResponse<GetPopularGifticonGoodsResponse>> response = controller.getPopularGoods();

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().getContent()).isNotNull();
            verify(getPopularGifticonGoodsUseCase).getPopularGoods();
        }
    }

    @Nested
    @DisplayName("GET /api/v1/gifticon/goods/{goodsCode}")
    class GetGoodsDetail {

        @Test
        @DisplayName("인증된 사용자 ID와 상품 코드를 UseCase Command에 전달하고 OK를 반환한다")
        void returnsOkWithDetail() {
            OAuth2AuthenticatedPrincipal principal = buildPrincipal("42");
            GetGifticonGoodsDetailResult result = new GetGifticonGoodsDetailResult(
                    "G-01", "상품명", "B-01", "브랜드", "brandImg",
                    "CAT-01", 5000L, 4500L, 4000L, "img", "설명", 90, 10000L
            );
            given(getGifticonGoodsDetailUseCase.getGoodsDetail(any(GetGifticonGoodsDetailCommand.class)))
                    .willReturn(result);

            ResponseEntity<BaseResponse<GetGifticonGoodsDetailResponse>> response =
                    controller.getGoodsDetail("G-01", principal);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().getContent()).isNotNull();
            ArgumentCaptor<GetGifticonGoodsDetailCommand> captor =
                    ArgumentCaptor.forClass(GetGifticonGoodsDetailCommand.class);
            verify(getGifticonGoodsDetailUseCase).getGoodsDetail(captor.capture());
            assertThat(captor.getValue().goodsCode()).isEqualTo("G-01");
            assertThat(captor.getValue().userId()).isEqualTo(42L);
        }
    }
}
