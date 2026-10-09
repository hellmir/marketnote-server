package com.personal.marketnote.reward.adapter.in.web.gifticon.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.reward.adapter.in.web.gifticon.request.PurchaseGifticonRequest;
import com.personal.marketnote.reward.adapter.in.web.gifticon.response.GetMyGifticonOrderDetailResponse;
import com.personal.marketnote.reward.adapter.in.web.gifticon.response.GetMyGifticonOrdersResponse;
import com.personal.marketnote.reward.adapter.in.web.gifticon.response.PurchaseGifticonResponse;
import com.personal.marketnote.reward.port.in.command.gifticon.GetMyGifticonOrderDetailCommand;
import com.personal.marketnote.reward.port.in.command.gifticon.GetMyGifticonOrdersCommand;
import com.personal.marketnote.reward.port.in.command.gifticon.PurchaseGifticonCommand;
import com.personal.marketnote.reward.port.in.result.gifticon.GetMyGifticonOrderDetailResult;
import com.personal.marketnote.reward.port.in.result.gifticon.GetMyGifticonOrdersResult;
import com.personal.marketnote.reward.port.in.result.gifticon.PurchaseGifticonResult;
import com.personal.marketnote.reward.port.in.usecase.gifticon.GetMyGifticonOrderDetailUseCase;
import com.personal.marketnote.reward.port.in.usecase.gifticon.GetMyGifticonOrdersUseCase;
import com.personal.marketnote.reward.port.in.usecase.gifticon.PurchaseGifticonUseCase;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("GifticonOrderController 테스트")
class GifticonOrderControllerTest {

    @Mock
    private PurchaseGifticonUseCase purchaseGifticonUseCase;
    @Mock
    private GetMyGifticonOrdersUseCase getMyGifticonOrdersUseCase;
    @Mock
    private GetMyGifticonOrderDetailUseCase getMyGifticonOrderDetailUseCase;

    @InjectMocks
    private GifticonOrderController controller;

    private OAuth2AuthenticatedPrincipal buildPrincipal(String userId) {
        return new DefaultOAuth2AuthenticatedPrincipal(
                userId,
                Map.of("name", userId),
                List.of(new SimpleGrantedAuthority("ROLE_BUYER"))
        );
    }

    @Nested
    @DisplayName("POST /api/v1/gifticons/orders")
    class PurchaseGifticon {

        @Test
        @DisplayName("정상 요청 시 인증 사용자 ID와 상품 코드를 UseCase Command에 전달하고 OK를 반환한다")
        void returnsOkAndDelegates() {
            OAuth2AuthenticatedPrincipal principal = buildPrincipal("42");
            PurchaseGifticonRequest request = new PurchaseGifticonRequest("G-01");
            given(purchaseGifticonUseCase.purchase(any(PurchaseGifticonCommand.class)))
                    .willReturn(new PurchaseGifticonResult(100L, "ORD-1", 5000L, "상품명"));

            ResponseEntity<BaseResponse<PurchaseGifticonResponse>> response =
                    controller.purchaseGifticon(principal, request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().getContent()).isNotNull();
            ArgumentCaptor<PurchaseGifticonCommand> captor = ArgumentCaptor.forClass(PurchaseGifticonCommand.class);
            verify(purchaseGifticonUseCase).purchase(captor.capture());
            assertThat(captor.getValue().userId()).isEqualTo(42L);
            assertThat(captor.getValue().goodsCode()).isEqualTo("G-01");
        }
    }

    @Nested
    @DisplayName("GET /api/v1/gifticons/orders/me")
    class GetMyGifticonOrders {

        @Test
        @DisplayName("파라미터를 UseCase Command에 그대로 전달하고 OK를 반환한다")
        void returnsOkAndPassesParams() {
            OAuth2AuthenticatedPrincipal principal = buildPrincipal("42");
            given(getMyGifticonOrdersUseCase.getMyGifticonOrders(any(GetMyGifticonOrdersCommand.class)))
                    .willReturn(new GetMyGifticonOrdersResult(0L, 0L, false, -1L, List.of()));

            ResponseEntity<BaseResponse<GetMyGifticonOrdersResponse>> response =
                    controller.getMyGifticonOrders(principal, "AVAILABLE", "PURCHASE_LATEST", -1L, 10);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().getContent()).isNotNull();
            ArgumentCaptor<GetMyGifticonOrdersCommand> captor =
                    ArgumentCaptor.forClass(GetMyGifticonOrdersCommand.class);
            verify(getMyGifticonOrdersUseCase).getMyGifticonOrders(captor.capture());
            assertThat(captor.getValue().userId()).isEqualTo(42L);
            assertThat(captor.getValue().statusFilter()).isEqualTo("AVAILABLE");
            assertThat(captor.getValue().sortType()).isEqualTo("PURCHASE_LATEST");
            assertThat(captor.getValue().cursor()).isEqualTo(-1L);
            assertThat(captor.getValue().pageSize()).isEqualTo(10);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/gifticons/orders/me/{orderId}")
    class GetMyGifticonOrderDetail {

        @Test
        @DisplayName("인증 사용자 ID와 orderId를 UseCase Command에 전달하고 OK를 반환한다")
        void returnsOkWithDetail() {
            OAuth2AuthenticatedPrincipal principal = buildPrincipal("42");
            GetMyGifticonOrderDetailResult result = new GetMyGifticonOrderDetailResult(
                    100L, "상품", "브랜드", "b.img", "p.img", "설명",
                    5000L, "c.img", "핀", "2026-07-15", 90, "사용가능",
                    "AVAILABLE", LocalDateTime.of(2026, 4, 15, 0, 0)
            );
            given(getMyGifticonOrderDetailUseCase.getMyGifticonOrderDetail(any(GetMyGifticonOrderDetailCommand.class)))
                    .willReturn(result);

            ResponseEntity<BaseResponse<GetMyGifticonOrderDetailResponse>> response =
                    controller.getMyGifticonOrderDetail(principal, 100L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().getContent()).isNotNull();
            ArgumentCaptor<GetMyGifticonOrderDetailCommand> captor =
                    ArgumentCaptor.forClass(GetMyGifticonOrderDetailCommand.class);
            verify(getMyGifticonOrderDetailUseCase).getMyGifticonOrderDetail(captor.capture());
            assertThat(captor.getValue().userId()).isEqualTo(42L);
            assertThat(captor.getValue().orderId()).isEqualTo(100L);
        }
    }
}
