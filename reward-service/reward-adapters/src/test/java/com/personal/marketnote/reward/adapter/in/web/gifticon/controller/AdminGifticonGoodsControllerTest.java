package com.personal.marketnote.reward.adapter.in.web.gifticon.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.reward.adapter.in.web.gifticon.request.ManageFeaturedGifticonGoodsRequest;
import com.personal.marketnote.reward.adapter.in.web.gifticon.request.ManageGifticonGoodsExposureRequest;
import com.personal.marketnote.reward.adapter.in.web.gifticon.request.ManageGifticonGoodsOrderRequest;
import com.personal.marketnote.reward.adapter.in.web.gifticon.response.GetAdminGifticonGoodsResponse;
import com.personal.marketnote.reward.port.in.command.gifticon.GetAdminGifticonGoodsCommand;
import com.personal.marketnote.reward.port.in.command.gifticon.ManageFeaturedGifticonGoodsCommand;
import com.personal.marketnote.reward.port.in.command.gifticon.ManageGifticonGoodsExposureCommand;
import com.personal.marketnote.reward.port.in.command.gifticon.ManageGifticonGoodsOrderCommand;
import com.personal.marketnote.reward.port.in.result.gifticon.GetAdminGifticonGoodsResult;
import com.personal.marketnote.reward.port.in.usecase.gifticon.GetAdminGifticonGoodsUseCase;
import com.personal.marketnote.reward.port.in.usecase.gifticon.ManageFeaturedGifticonGoodsUseCase;
import com.personal.marketnote.reward.port.in.usecase.gifticon.ManageGifticonGoodsExposureUseCase;
import com.personal.marketnote.reward.port.in.usecase.gifticon.ManageGifticonGoodsOrderUseCase;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminGifticonGoodsController 테스트")
class AdminGifticonGoodsControllerTest {

    @Mock
    private GetAdminGifticonGoodsUseCase getAdminGifticonGoodsUseCase;
    @Mock
    private ManageGifticonGoodsExposureUseCase manageGifticonGoodsExposureUseCase;
    @Mock
    private ManageGifticonGoodsOrderUseCase manageGifticonGoodsOrderUseCase;
    @Mock
    private ManageFeaturedGifticonGoodsUseCase manageFeaturedGifticonGoodsUseCase;

    @InjectMocks
    private AdminGifticonGoodsController controller;

    @Nested
    @DisplayName("GET /api/v1/admin/gifticon/goods")
    class GetAdminGifticonGoods {

        @Test
        @DisplayName("페이지/필터 파라미터를 UseCase Command에 전달하고 OK를 반환한다")
        void returnsOkAndPassesParams() {
            given(getAdminGifticonGoodsUseCase.getAdminGifticonGoods(any(GetAdminGifticonGoodsCommand.class)))
                    .willReturn(new GetAdminGifticonGoodsResult(1, 20, 0L, 0, List.of()));

            ResponseEntity<BaseResponse<GetAdminGifticonGoodsResponse>> response =
                    controller.getAdminGifticonGoods(2, 50, "SELLING", true, "키워드");

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().getContent()).isNotNull();
            ArgumentCaptor<GetAdminGifticonGoodsCommand> captor =
                    ArgumentCaptor.forClass(GetAdminGifticonGoodsCommand.class);
            verify(getAdminGifticonGoodsUseCase).getAdminGifticonGoods(captor.capture());
            assertThat(captor.getValue().page()).isEqualTo(2);
            assertThat(captor.getValue().pageSize()).isEqualTo(50);
            assertThat(captor.getValue().goodsStatus()).isEqualTo("SELLING");
            assertThat(captor.getValue().exposed()).isTrue();
            assertThat(captor.getValue().keyword()).isEqualTo("키워드");
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/admin/gifticon/goods/exposure")
    class ManageGifticonGoodsExposure {

        @Test
        @DisplayName("정상 요청 시 OK 상태를 반환하고 UseCase를 호출한다")
        void returnsOkAndDelegates() {
            ManageGifticonGoodsExposureRequest request = new ManageGifticonGoodsExposureRequest(
                    List.of(new ManageGifticonGoodsExposureRequest.ExposureItem("G-01", true))
            );

            ResponseEntity<BaseResponse<Void>> response = controller.manageGifticonGoodsExposure(request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(manageGifticonGoodsExposureUseCase).manageExposure(any(ManageGifticonGoodsExposureCommand.class));
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/admin/gifticon/goods/order")
    class ManageGifticonGoodsOrder {

        @Test
        @DisplayName("정상 요청 시 OK 상태를 반환하고 UseCase를 호출한다")
        void returnsOkAndDelegates() {
            ManageGifticonGoodsOrderRequest request = new ManageGifticonGoodsOrderRequest(
                    List.of(new ManageGifticonGoodsOrderRequest.OrderItem("G-01", 5))
            );

            ResponseEntity<BaseResponse<Void>> response = controller.manageGifticonGoodsOrder(request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(manageGifticonGoodsOrderUseCase).manageOrder(any(ManageGifticonGoodsOrderCommand.class));
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/admin/gifticon/goods/featured")
    class ManageFeaturedGifticonGoods {

        @Test
        @DisplayName("정상 요청 시 OK 상태를 반환하고 UseCase를 호출한다")
        void returnsOkAndDelegates() {
            ManageFeaturedGifticonGoodsRequest request = new ManageFeaturedGifticonGoodsRequest(
                    List.of(new ManageFeaturedGifticonGoodsRequest.FeaturedGoodsItem("G-01", true, 1))
            );

            ResponseEntity<BaseResponse<Void>> response = controller.manageFeaturedGifticonGoods(request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(manageFeaturedGifticonGoodsUseCase).manageFeatured(any(ManageFeaturedGifticonGoodsCommand.class));
        }
    }
}
