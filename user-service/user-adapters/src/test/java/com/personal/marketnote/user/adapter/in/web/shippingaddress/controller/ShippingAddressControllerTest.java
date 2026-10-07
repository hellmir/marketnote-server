package com.personal.marketnote.user.adapter.in.web.shippingaddress.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.common.domain.delivery.DeliveryRequestType;
import com.personal.marketnote.user.adapter.in.web.shippingaddress.request.RegisterShippingAddressRequest;
import com.personal.marketnote.user.adapter.in.web.shippingaddress.request.UpdateShippingAddressRequest;
import com.personal.marketnote.user.adapter.in.web.shippingaddress.response.GetMyShippingAddressesResponse;
import com.personal.marketnote.user.adapter.in.web.shippingaddress.response.GetShippingAddressResponse;
import com.personal.marketnote.user.domain.shippingaddress.ShippingAddressRegionType;
import com.personal.marketnote.user.domain.shippingaddress.ShippingAddressType;
import com.personal.marketnote.user.port.in.command.shippingaddress.RegisterShippingAddressCommand;
import com.personal.marketnote.user.port.in.command.shippingaddress.UpdateShippingAddressCommand;
import com.personal.marketnote.user.port.in.result.shippingaddress.GetMyShippingAddressesResult;
import com.personal.marketnote.user.port.in.result.shippingaddress.GetShippingAddressResult;
import com.personal.marketnote.user.port.in.result.shippingaddress.RegisterShippingAddressResult;
import com.personal.marketnote.user.port.in.usecase.shippingaddress.DeleteShippingAddressUseCase;
import com.personal.marketnote.user.port.in.usecase.shippingaddress.GetMyShippingAddressesUseCase;
import com.personal.marketnote.user.port.in.usecase.shippingaddress.GetShippingAddressUseCase;
import com.personal.marketnote.user.port.in.usecase.shippingaddress.RegisterShippingAddressUseCase;
import com.personal.marketnote.user.port.in.usecase.shippingaddress.SetDefaultShippingAddressUseCase;
import com.personal.marketnote.user.port.in.usecase.shippingaddress.UpdateShippingAddressUseCase;
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
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ShippingAddressController 배송지 관리")
class ShippingAddressControllerTest {

    @InjectMocks
    private ShippingAddressController controller;

    @Mock
    private DeleteShippingAddressUseCase deleteShippingAddressUseCase;

    @Mock
    private RegisterShippingAddressUseCase registerShippingAddressUseCase;

    @Mock
    private GetShippingAddressUseCase getShippingAddressUseCase;

    @Mock
    private GetMyShippingAddressesUseCase getMyShippingAddressesUseCase;

    @Mock
    private SetDefaultShippingAddressUseCase setDefaultShippingAddressUseCase;

    @Mock
    private UpdateShippingAddressUseCase updateShippingAddressUseCase;

    private OAuth2AuthenticatedPrincipal principalOf(Long userId) {
        return new DefaultOAuth2AuthenticatedPrincipal(
                String.valueOf(userId), Map.of("name", String.valueOf(userId)), List.of()
        );
    }

    private RegisterShippingAddressResult buildRegisterResult(Long id) {
        return new RegisterShippingAddressResult(
                id, ShippingAddressType.HOME, true, ShippingAddressRegionType.NORMAL
        );
    }

    private GetShippingAddressResult buildGetResult(Long id) {
        return new GetShippingAddressResult(
                id, ShippingAddressType.HOME, "서울시 강남구", "101호",
                null, "집", "홍길동", "01012345678",
                DeliveryRequestType.LEAVE_AT_DOOR, null, true,
                ShippingAddressRegionType.NORMAL
        );
    }

    @Nested
    @DisplayName("POST /api/v1/shipping-addresses - 배송지 등록")
    class RegisterShippingAddress {

        @Test
        @DisplayName("정상 요청 시 CREATED 상태와 결과를 반환하고 userId를 Command에 담는다")
        void registersAndReturnsCreated() {
            // given
            RegisterShippingAddressRequest request = new RegisterShippingAddressRequest();
            ReflectionTestUtils.setField(request, "addressType", ShippingAddressType.HOME);
            ReflectionTestUtils.setField(request, "address", "서울시 강남구");
            ReflectionTestUtils.setField(request, "addressDetail", "101호");
            ReflectionTestUtils.setField(request, "recipientName", "홍길동");
            ReflectionTestUtils.setField(request, "recipientPhoneNumber", "01012345678");
            ReflectionTestUtils.setField(request, "isDefault", Boolean.TRUE);
            when(registerShippingAddressUseCase.registerShippingAddress(any(RegisterShippingAddressCommand.class)))
                    .thenReturn(buildRegisterResult(10L));

            // when
            ResponseEntity<BaseResponse<RegisterShippingAddressResult>> response = controller.registerShippingAddress(
                    request, principalOf(100L)
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            ArgumentCaptor<RegisterShippingAddressCommand> captor =
                    ArgumentCaptor.forClass(RegisterShippingAddressCommand.class);
            verify(registerShippingAddressUseCase).registerShippingAddress(captor.capture());
            RegisterShippingAddressCommand captured = captor.getValue();
            assertThat(captured.userId()).isEqualTo(100L);
            assertThat(captured.addressType()).isEqualTo(ShippingAddressType.HOME);
            assertThat(captured.address()).isEqualTo("서울시 강남구");
            assertThat(captured.isDefault()).isTrue();
        }
    }

    @Nested
    @DisplayName("GET /api/v1/shipping-addresses/{id} - 배송지 조회")
    class GetShippingAddress {

        @Test
        @DisplayName("배송지 ID와 인증된 회원 ID를 전달하여 조회한다")
        void getsShippingAddressByIdAndUserId() {
            // given
            when(getShippingAddressUseCase.getShippingAddress(10L, 100L)).thenReturn(buildGetResult(10L));

            // when
            ResponseEntity<BaseResponse<GetShippingAddressResponse>> response = controller.getShippingAddress(
                    10L, principalOf(100L)
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(getShippingAddressUseCase).getShippingAddress(10L, 100L);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/shipping-addresses/me - 내 배송지 목록 조회")
    class GetMyShippingAddresses {

        @Test
        @DisplayName("내 배송지 목록 조회 시 빈 결과도 OK로 반환한다")
        void returnsEmptyListAsOk() {
            // given
            when(getMyShippingAddressesUseCase.getMyShippingAddresses(100L))
                    .thenReturn(new GetMyShippingAddressesResult(List.of()));

            // when
            ResponseEntity<BaseResponse<GetMyShippingAddressesResponse>> response = controller.getMyShippingAddresses(
                    principalOf(100L)
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(getMyShippingAddressesUseCase).getMyShippingAddresses(100L);
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/shipping-addresses/{id}/default - 기본 배송지 설정")
    class SetDefaultShippingAddress {

        @Test
        @DisplayName("기본 배송지 설정 시 UseCase에 ID와 회원 ID를 위임한다")
        void delegatesToSetDefaultUseCase() {
            // when
            ResponseEntity<BaseResponse<Void>> response = controller.setDefaultShippingAddress(10L, principalOf(100L));

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(setDefaultShippingAddressUseCase).setDefaultShippingAddress(10L, 100L);
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/shipping-addresses/{id} - 배송지 수정")
    class UpdateShippingAddress {

        @Test
        @DisplayName("배송지 수정 요청을 Command로 매핑하여 ID와 회원 ID를 전달한다")
        void mapsUpdateRequestToCommand() {
            // given
            UpdateShippingAddressRequest request = new UpdateShippingAddressRequest();
            ReflectionTestUtils.setField(request, "address", "서울시 서초구");
            ReflectionTestUtils.setField(request, "addressDetail", "202호");
            ReflectionTestUtils.setField(request, "recipientName", "홍길동");
            ReflectionTestUtils.setField(request, "recipientPhoneNumber", "01087654321");
            ReflectionTestUtils.setField(request, "deliveryRequestType", DeliveryRequestType.LEAVE_AT_DOOR);

            // when
            ResponseEntity<BaseResponse<Void>> response = controller.updateShippingAddress(
                    10L, request, principalOf(100L)
            );

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            ArgumentCaptor<UpdateShippingAddressCommand> captor =
                    ArgumentCaptor.forClass(UpdateShippingAddressCommand.class);
            verify(updateShippingAddressUseCase).updateShippingAddress(
                    org.mockito.ArgumentMatchers.eq(10L),
                    org.mockito.ArgumentMatchers.eq(100L),
                    captor.capture()
            );
            UpdateShippingAddressCommand captured = captor.getValue();
            assertThat(captured.address()).isEqualTo("서울시 서초구");
            assertThat(captured.addressDetail()).isEqualTo("202호");
            assertThat(captured.deliveryRequestType()).isEqualTo(DeliveryRequestType.LEAVE_AT_DOOR);
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/shipping-addresses/{id} - 배송지 삭제")
    class DeleteShippingAddress {

        @Test
        @DisplayName("배송지 삭제 시 나머지 UseCase는 호출되지 않는다")
        void invokesOnlyDeleteUseCase() {
            // when
            ResponseEntity<BaseResponse<Void>> response = controller.deleteShippingAddress(10L, principalOf(100L));

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(deleteShippingAddressUseCase).deleteShippingAddress(10L, 100L);
            verifyNoInteractions(
                    registerShippingAddressUseCase, getShippingAddressUseCase,
                    getMyShippingAddressesUseCase, setDefaultShippingAddressUseCase,
                    updateShippingAddressUseCase
            );
        }
    }
}
