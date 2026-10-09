package com.personal.marketnote.fulfillment.adapter.in.web.vendor.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.request.CompleteFulfillmentDeliveryIcsRequest;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.response.CancelFulfillmentDeliveryResponse;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.response.CompleteFulfillmentDeliveryIcsResponse;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.response.GetFulfillmentDeliveriesResponse;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.response.GetFulfillmentDeliveryDetailResponse;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.response.GetFulfillmentDeliveryGoodDetailResponse;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.response.GetFulfillmentDeliveryOutOrdGoodsByOrdNoResponse;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.response.GetFulfillmentDeliveryOutOrdGoodsDetailResponse;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.response.GetFulfillmentDeliveryStatusesResponse;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.response.RegisterFulfillmentDeliveryResponse;
import com.personal.marketnote.fulfillment.port.in.command.vendor.CancelFulfillmentDeliveryCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.CompleteFulfillmentDeliveryIcsCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentDeliveriesCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentDeliveryDetailCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentDeliveryGoodDetailCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentDeliveryOutOrdGoodsByOrdNoCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentDeliveryOutOrdGoodsDetailCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentDeliveryStatusesCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentDeliveryCarCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentDeliveryCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentDeliveryIcsCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.UpdateFulfillmentDeliveryCarCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.UpdateFulfillmentDeliveryCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.CancelFulfillmentDeliveryResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.CompleteFulfillmentDeliveryIcsResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentDeliveriesResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentDeliveryDetailResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentDeliveryGoodDetailResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentDeliveryOutOrdGoodsByOrdNoResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentDeliveryOutOrdGoodsDetailResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentDeliveryStatusesResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.RegisterFulfillmentDeliveryResult;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.CancelFulfillmentDeliveryUseCase;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.CompleteFulfillmentDeliveryIcsUseCase;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.GetFulfillmentDeliveriesUseCase;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.GetFulfillmentDeliveryDetailUseCase;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.GetFulfillmentDeliveryGoodDetailUseCase;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.GetFulfillmentDeliveryOutOrdGoodsByOrdNoUseCase;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.GetFulfillmentDeliveryOutOrdGoodsDetailUseCase;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.GetFulfillmentDeliveryStatusesUseCase;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.RegisterFulfillmentDeliveryCarUseCase;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.RegisterFulfillmentDeliveryIcsUseCase;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.RegisterFulfillmentDeliveryUseCase;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.UpdateFulfillmentDeliveryCarUseCase;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.UpdateFulfillmentDeliveryUseCase;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("FulfillmentDeliveryController 풀필먼트 배송 관리")
class FulfillmentDeliveryControllerTest {

    private static final String CUSTOMER_CODE = "CUST001";
    private static final String ACCESS_TOKEN = "TOKEN";

    @InjectMocks
    private FulfillmentDeliveryController controller;

    @Mock
    private RegisterFulfillmentDeliveryUseCase registerFulfillmentDeliveryUseCase;
    @Mock
    private UpdateFulfillmentDeliveryUseCase updateFulfillmentDeliveryUseCase;
    @Mock
    private RegisterFulfillmentDeliveryCarUseCase registerFulfillmentDeliveryCarUseCase;
    @Mock
    private RegisterFulfillmentDeliveryIcsUseCase registerFulfillmentDeliveryIcsUseCase;
    @Mock
    private UpdateFulfillmentDeliveryCarUseCase updateFulfillmentDeliveryCarUseCase;
    @Mock
    private GetFulfillmentDeliveriesUseCase getFulfillmentDeliveriesUseCase;
    @Mock
    private GetFulfillmentDeliveryStatusesUseCase getFulfillmentDeliveryStatusesUseCase;
    @Mock
    private GetFulfillmentDeliveryDetailUseCase getFulfillmentDeliveryDetailUseCase;
    @Mock
    private GetFulfillmentDeliveryOutOrdGoodsDetailUseCase getFulfillmentDeliveryOutOrdGoodsDetailUseCase;
    @Mock
    private GetFulfillmentDeliveryOutOrdGoodsByOrdNoUseCase getFulfillmentDeliveryOutOrdGoodsByOrdNoUseCase;
    @Mock
    private GetFulfillmentDeliveryGoodDetailUseCase getFulfillmentDeliveryGoodDetailUseCase;
    @Mock
    private CancelFulfillmentDeliveryUseCase cancelFulfillmentDeliveryUseCase;
    @Mock
    private CompleteFulfillmentDeliveryIcsUseCase completeFulfillmentDeliveryIcsUseCase;

    @Nested
    @DisplayName("POST /api/v1/vendors/fassto/deliveries/{customerCode} - 출고 등록(택배)")
    class RegisterDelivery {

        @Test
        @DisplayName("정상 요청 시 Created 상태로 응답하고 UseCase에 위임한다")
        void returnsCreated() {
            // given
            when(registerFulfillmentDeliveryUseCase.registerDelivery(any(RegisterFulfillmentDeliveryCommand.class)))
                    .thenReturn(RegisterFulfillmentDeliveryResult.of(0, List.of()));

            // when
            ResponseEntity<BaseResponse<RegisterFulfillmentDeliveryResponse>> response =
                    controller.registerDelivery(CUSTOMER_CODE, ACCESS_TOKEN, List.of());

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            ArgumentCaptor<RegisterFulfillmentDeliveryCommand> captor =
                    ArgumentCaptor.forClass(RegisterFulfillmentDeliveryCommand.class);
            verify(registerFulfillmentDeliveryUseCase).registerDelivery(captor.capture());
            assertThat(captor.getValue().customerCode()).isEqualTo(CUSTOMER_CODE);
            assertThat(captor.getValue().accessToken()).isEqualTo(ACCESS_TOKEN);
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/vendors/fassto/deliveries/{customerCode} - 출고 수정(택배)")
    class UpdateDelivery {

        @Test
        @DisplayName("정상 요청 시 OK를 반환하고 수정 UseCase에 위임한다")
        void returnsOk() {
            // given
            when(updateFulfillmentDeliveryUseCase.updateDelivery(any(UpdateFulfillmentDeliveryCommand.class)))
                    .thenReturn(RegisterFulfillmentDeliveryResult.of(0, List.of()));

            // when
            ResponseEntity<BaseResponse<RegisterFulfillmentDeliveryResponse>> response =
                    controller.updateDelivery(CUSTOMER_CODE, ACCESS_TOKEN, List.of());

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(updateFulfillmentDeliveryUseCase).updateDelivery(any(UpdateFulfillmentDeliveryCommand.class));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/vendors/fassto/deliveries/car/{customerCode} - 출고 등록(차량)")
    class RegisterDeliveryCar {

        @Test
        @DisplayName("정상 요청 시 Created 상태로 응답하고 차량 등록 UseCase에 위임한다")
        void returnsCreated() {
            // given
            when(registerFulfillmentDeliveryCarUseCase.registerDeliveryCar(any(RegisterFulfillmentDeliveryCarCommand.class)))
                    .thenReturn(RegisterFulfillmentDeliveryResult.of(0, List.of()));

            // when
            ResponseEntity<BaseResponse<RegisterFulfillmentDeliveryResponse>> response =
                    controller.registerDeliveryCar(CUSTOMER_CODE, ACCESS_TOKEN, List.of());

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            verify(registerFulfillmentDeliveryCarUseCase).registerDeliveryCar(any(RegisterFulfillmentDeliveryCarCommand.class));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/vendors/fassto/deliveries/ics/{customerCode} - 출고 등록(해외)")
    class RegisterDeliveryIcs {

        @Test
        @DisplayName("정상 요청 시 Created 상태로 응답하고 해외 등록 UseCase에 위임한다")
        void returnsCreated() {
            // given
            when(registerFulfillmentDeliveryIcsUseCase.registerDeliveryIcs(any(RegisterFulfillmentDeliveryIcsCommand.class)))
                    .thenReturn(RegisterFulfillmentDeliveryResult.of(0, List.of()));

            // when
            ResponseEntity<BaseResponse<RegisterFulfillmentDeliveryResponse>> response =
                    controller.registerDeliveryIcs(CUSTOMER_CODE, ACCESS_TOKEN, List.of());

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            verify(registerFulfillmentDeliveryIcsUseCase).registerDeliveryIcs(any(RegisterFulfillmentDeliveryIcsCommand.class));
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/vendors/fassto/deliveries/car/{customerCode} - 출고 수정(차량)")
    class UpdateDeliveryCar {

        @Test
        @DisplayName("정상 요청 시 OK를 반환하고 차량 수정 UseCase에 위임한다")
        void returnsOk() {
            // given
            when(updateFulfillmentDeliveryCarUseCase.updateDeliveryCar(any(UpdateFulfillmentDeliveryCarCommand.class)))
                    .thenReturn(RegisterFulfillmentDeliveryResult.of(0, List.of()));

            // when
            ResponseEntity<BaseResponse<RegisterFulfillmentDeliveryResponse>> response =
                    controller.updateDeliveryCar(CUSTOMER_CODE, ACCESS_TOKEN, List.of());

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(updateFulfillmentDeliveryCarUseCase).updateDeliveryCar(any(UpdateFulfillmentDeliveryCarCommand.class));
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/vendors/fassto/deliveries/cancel/{customerCode} - 출고 취소")
    class CancelDelivery {

        @Test
        @DisplayName("정상 요청 시 OK를 반환하고 취소 UseCase에 위임한다")
        void returnsOk() {
            // given
            when(cancelFulfillmentDeliveryUseCase.cancelDelivery(any(CancelFulfillmentDeliveryCommand.class)))
                    .thenReturn(CancelFulfillmentDeliveryResult.of(0, List.of()));

            // when
            ResponseEntity<BaseResponse<CancelFulfillmentDeliveryResponse>> response =
                    controller.cancelDelivery(CUSTOMER_CODE, ACCESS_TOKEN, List.of());

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(cancelFulfillmentDeliveryUseCase).cancelDelivery(any(CancelFulfillmentDeliveryCommand.class));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/vendors/fassto/deliveries/{customerCode}/{startDate}/{endDate}/{status}/{outDiv} - 출고 목록 조회")
    class GetDeliveries {

        @Test
        @DisplayName("경로/쿼리 파라미터를 Command로 매핑하여 UseCase에 위임하고 OK를 반환한다")
        void returnsOk() {
            // given
            when(getFulfillmentDeliveriesUseCase.getDeliveries(any(GetFulfillmentDeliveriesCommand.class)))
                    .thenReturn(GetFulfillmentDeliveriesResult.of(0, List.of()));

            // when
            ResponseEntity<BaseResponse<GetFulfillmentDeliveriesResponse>> response = controller.getDeliveries(
                    CUSTOMER_CODE, "2026-04-01", "2026-10-07", "ALL", "1", ACCESS_TOKEN, "ORD-1"
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            ArgumentCaptor<GetFulfillmentDeliveriesCommand> captor =
                    ArgumentCaptor.forClass(GetFulfillmentDeliveriesCommand.class);
            verify(getFulfillmentDeliveriesUseCase).getDeliveries(captor.capture());
            assertThat(captor.getValue().customerCode()).isEqualTo(CUSTOMER_CODE);
            assertThat(captor.getValue().startDate()).isEqualTo("2026-04-01");
            assertThat(captor.getValue().endDate()).isEqualTo("2026-10-07");
            assertThat(captor.getValue().status()).isEqualTo("ALL");
            assertThat(captor.getValue().releaseType()).isEqualTo("1");
        }
    }

    @Nested
    @DisplayName("GET /api/v1/vendors/fassto/deliveries/parcel/{customerCode}/{startDate}/{endDate}/{outDiv} - 출고 배송 조회")
    class GetDeliveryStatuses {

        @Test
        @DisplayName("경로 파라미터를 Command로 매핑하여 UseCase에 위임하고 OK를 반환한다")
        void returnsOk() {
            // given
            when(getFulfillmentDeliveryStatusesUseCase.getDeliveryStatuses(any(GetFulfillmentDeliveryStatusesCommand.class)))
                    .thenReturn(GetFulfillmentDeliveryStatusesResult.of(0, List.of()));

            // when
            ResponseEntity<BaseResponse<GetFulfillmentDeliveryStatusesResponse>> response =
                    controller.getDeliveryStatuses(CUSTOMER_CODE, "2026-04-01", "2026-10-07", "ALL", ACCESS_TOKEN);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(getFulfillmentDeliveryStatusesUseCase).getDeliveryStatuses(any(GetFulfillmentDeliveryStatusesCommand.class));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/vendors/fassto/deliveries/detail/{customerCode}/{slipNo} - 출고 상세 조회")
    class GetDeliveryDetail {

        @Test
        @DisplayName("경로/쿼리 파라미터를 Command로 매핑하여 UseCase에 위임하고 OK를 반환한다")
        void returnsOk() {
            // given
            when(getFulfillmentDeliveryDetailUseCase.getDeliveryDetail(any(GetFulfillmentDeliveryDetailCommand.class)))
                    .thenReturn(GetFulfillmentDeliveryDetailResult.of(0, List.of()));

            // when
            ResponseEntity<BaseResponse<GetFulfillmentDeliveryDetailResponse>> response =
                    controller.getDeliveryDetail(CUSTOMER_CODE, "SLIP-001", ACCESS_TOKEN, "ORD-1");

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            ArgumentCaptor<GetFulfillmentDeliveryDetailCommand> captor =
                    ArgumentCaptor.forClass(GetFulfillmentDeliveryDetailCommand.class);
            verify(getFulfillmentDeliveryDetailUseCase).getDeliveryDetail(captor.capture());
            assertThat(captor.getValue().slipNumber()).isEqualTo("SLIP-001");
        }
    }

    @Nested
    @DisplayName("GET /api/v1/vendors/fassto/deliveries/out-ord/goods-detail/{customerCode} - 송장별 상품 조회")
    class GetOutOrdGoodsDetail {

        @Test
        @DisplayName("출고요청번호와 함께 UseCase에 위임하고 OK를 반환한다")
        void returnsOk() {
            // given
            when(getFulfillmentDeliveryOutOrdGoodsDetailUseCase.getOutOrdGoodsDetail(
                    any(GetFulfillmentDeliveryOutOrdGoodsDetailCommand.class)
            )).thenReturn(GetFulfillmentDeliveryOutOrdGoodsDetailResult.of(0, List.of()));

            // when
            ResponseEntity<BaseResponse<GetFulfillmentDeliveryOutOrdGoodsDetailResponse>> response =
                    controller.getOutOrdGoodsDetail(CUSTOMER_CODE, ACCESS_TOKEN, "SLIP-002");

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            ArgumentCaptor<GetFulfillmentDeliveryOutOrdGoodsDetailCommand> captor =
                    ArgumentCaptor.forClass(GetFulfillmentDeliveryOutOrdGoodsDetailCommand.class);
            verify(getFulfillmentDeliveryOutOrdGoodsDetailUseCase).getOutOrdGoodsDetail(captor.capture());
            assertThat(captor.getValue().releaseOrderSlipNumber()).isEqualTo("SLIP-002");
        }
    }

    @Nested
    @DisplayName("GET /api/v1/vendors/fassto/deliveries/out-ord/goods-ord-no/... - 주문번호 기반 조회")
    class GetOutOrdGoodsByOrdNo {

        @Test
        @DisplayName("경로 파라미터를 Command로 매핑하여 UseCase에 위임하고 OK를 반환한다")
        void returnsOk() {
            // given
            when(getFulfillmentDeliveryOutOrdGoodsByOrdNoUseCase.getOutOrdGoodsByOrdNo(
                    any(GetFulfillmentDeliveryOutOrdGoodsByOrdNoCommand.class)
            )).thenReturn(GetFulfillmentDeliveryOutOrdGoodsByOrdNoResult.of(0, List.of()));

            // when
            ResponseEntity<BaseResponse<GetFulfillmentDeliveryOutOrdGoodsByOrdNoResponse>> response =
                    controller.getOutOrdGoodsByOrdNo(CUSTOMER_CODE, "2026-04-01", "2026-10-07", ACCESS_TOKEN, "ORD-99");

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(getFulfillmentDeliveryOutOrdGoodsByOrdNoUseCase)
                    .getOutOrdGoodsByOrdNo(any(GetFulfillmentDeliveryOutOrdGoodsByOrdNoCommand.class));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/vendors/fassto/deliveries/good-detail/... - 상품 상세 목록 조회")
    class GetDeliveryGoodDetail {

        @Test
        @DisplayName("경로 파라미터를 Command로 매핑하여 UseCase에 위임하고 OK를 반환한다")
        void returnsOk() {
            // given
            when(getFulfillmentDeliveryGoodDetailUseCase.getDeliveryGoodDetail(
                    any(GetFulfillmentDeliveryGoodDetailCommand.class)
            )).thenReturn(GetFulfillmentDeliveryGoodDetailResult.of(0, List.of()));

            // when
            ResponseEntity<BaseResponse<GetFulfillmentDeliveryGoodDetailResponse>> response =
                    controller.getDeliveryGoodDetail(CUSTOMER_CODE, "2026-04-01", "2026-10-07", ACCESS_TOKEN, null);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(getFulfillmentDeliveryGoodDetailUseCase)
                    .getDeliveryGoodDetail(any(GetFulfillmentDeliveryGoodDetailCommand.class));
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/vendors/fassto/deliveries/ics/completed/{customerCode} - 해외 배송완료 처리")
    class CompleteDeliveryIcs {

        @Test
        @DisplayName("정상 요청 시 OK를 반환하고 해외 배송완료 UseCase에 위임한다")
        void returnsOk() {
            // given
            CompleteFulfillmentDeliveryIcsRequest request = new CompleteFulfillmentDeliveryIcsRequest();
            ReflectionTestUtils.setField(request, "ordNoList", List.of("ORD-1", "ORD-2"));
            when(completeFulfillmentDeliveryIcsUseCase.completeDeliveryIcs(
                    any(CompleteFulfillmentDeliveryIcsCommand.class)
            )).thenReturn(CompleteFulfillmentDeliveryIcsResult.of(0, List.of()));

            // when
            ResponseEntity<BaseResponse<CompleteFulfillmentDeliveryIcsResponse>> response =
                    controller.completeDeliveryIcs(CUSTOMER_CODE, ACCESS_TOKEN, request);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(completeFulfillmentDeliveryIcsUseCase)
                    .completeDeliveryIcs(any(CompleteFulfillmentDeliveryIcsCommand.class));
        }
    }
}
