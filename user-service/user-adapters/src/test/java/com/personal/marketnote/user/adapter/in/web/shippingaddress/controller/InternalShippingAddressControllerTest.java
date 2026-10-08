package com.personal.marketnote.user.adapter.in.web.shippingaddress.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.common.domain.delivery.DeliveryRequestType;
import com.personal.marketnote.user.adapter.in.web.shippingaddress.request.UpdateDeliveryRequestRequest;
import com.personal.marketnote.user.adapter.in.web.shippingaddress.response.GetShippingAddressResponse;
import com.personal.marketnote.user.domain.shippingaddress.ShippingAddressRegionType;
import com.personal.marketnote.user.domain.shippingaddress.ShippingAddressType;
import com.personal.marketnote.user.port.in.command.shippingaddress.UpdateDeliveryRequestCommand;
import com.personal.marketnote.user.port.in.result.shippingaddress.GetShippingAddressResult;
import com.personal.marketnote.user.port.in.usecase.shippingaddress.GetShippingAddressUseCase;
import com.personal.marketnote.user.port.in.usecase.shippingaddress.UpdateDeliveryRequestUseCase;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("InternalShippingAddressController 내부 배송지 API")
class InternalShippingAddressControllerTest {

    @InjectMocks
    private InternalShippingAddressController controller;

    @Mock
    private GetShippingAddressUseCase getShippingAddressUseCase;

    @Mock
    private UpdateDeliveryRequestUseCase updateDeliveryRequestUseCase;

    @Nested
    @DisplayName("GET /api/v1/internal/shipping-addresses/{id} - 배송지 조회")
    class GetShippingAddress {

        @Test
        @DisplayName("쿼리 파라미터로 전달된 회원 ID를 UseCase에 전달하여 조회한다")
        void getsShippingAddressWithUserIdFromQuery() {
            // given
            GetShippingAddressResult result = new GetShippingAddressResult(
                    10L, ShippingAddressType.HOME, "서울시 강남구", "101호",
                    null, "집", "홍길동", "01012345678",
                    DeliveryRequestType.LEAVE_AT_DOOR, null, true,
                    ShippingAddressRegionType.NORMAL
            );
            when(getShippingAddressUseCase.getShippingAddress(10L, 100L)).thenReturn(result);

            // when
            ResponseEntity<BaseResponse<GetShippingAddressResponse>> response = controller.getShippingAddress(
                    10L, 100L
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            verify(getShippingAddressUseCase).getShippingAddress(10L, 100L);
            verifyNoInteractions(updateDeliveryRequestUseCase);
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/internal/shipping-addresses/{id}/delivery-request - 배송 요청사항 수정")
    class UpdateDeliveryRequest {

        @Test
        @DisplayName("배송 요청사항 타입과 메시지를 Command로 매핑하여 ID와 회원 ID를 전달한다")
        void mapsRequestToCommand() {
            // given
            UpdateDeliveryRequestRequest request = new UpdateDeliveryRequestRequest();
            ReflectionTestUtils.setField(request, "deliveryRequestType", DeliveryRequestType.CUSTOM);
            ReflectionTestUtils.setField(request, "deliveryRequestMessage", "경비실에 맡겨주세요");

            // when
            ResponseEntity<BaseResponse<Void>> response = controller.updateDeliveryRequest(10L, 100L, request);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            ArgumentCaptor<UpdateDeliveryRequestCommand> captor =
                    ArgumentCaptor.forClass(UpdateDeliveryRequestCommand.class);
            verify(updateDeliveryRequestUseCase).updateDeliveryRequest(eq(10L), eq(100L), captor.capture());
            UpdateDeliveryRequestCommand captured = captor.getValue();
            assertThat(captured.deliveryRequestType()).isEqualTo(DeliveryRequestType.CUSTOM);
            assertThat(captured.deliveryRequestMessage()).isEqualTo("경비실에 맡겨주세요");
            verifyNoInteractions(getShippingAddressUseCase);
        }
    }
}
