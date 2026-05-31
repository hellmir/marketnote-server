package com.personal.marketnote.fulfillment.service;

import com.personal.marketnote.fulfillment.domain.FulfillmentAccessToken;
import com.personal.marketnote.fulfillment.port.in.command.RegisterInternalReturnDeliveryCommand;
import com.personal.marketnote.fulfillment.port.in.command.RegisterInternalReturnDeliveryProductCommand;
import com.personal.marketnote.fulfillment.port.in.result.RegisterInternalReturnDeliveryResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.RegisterFulfillmentDeliveryItemResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.RegisterFulfillmentDeliveryResult;
import com.personal.marketnote.fulfillment.port.out.vendor.DisconnectFulfillmentAuthPort;
import com.personal.marketnote.fulfillment.port.out.vendor.GetFulfillmentCustomerCodePort;
import com.personal.marketnote.fulfillment.port.out.vendor.RegisterFulfillmentReturnDeliveryPort;
import com.personal.marketnote.fulfillment.port.out.vendor.RequestFulfillmentAuthPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RegisterInternalReturnDeliveryService 테스트")
class RegisterInternalReturnDeliveryUseCaseTest {
    @InjectMocks
    private RegisterInternalReturnDeliveryService registerInternalReturnDeliveryService;

    @Mock
    private GetFulfillmentCustomerCodePort getFulfillmentCustomerCodePort;

    @Mock
    private RequestFulfillmentAuthPort requestFulfillmentAuthPort;

    @Mock
    private DisconnectFulfillmentAuthPort disconnectFulfillmentAuthPort;

    @Mock
    private RegisterFulfillmentReturnDeliveryPort registerFulfillmentReturnDeliveryPort;

    @Test
    @DisplayName("반품 등록 요청 시 Fassto 인증 후 반품 등록을 호출하고 returnSlipNumber를 반환한다")
    void shouldRegisterReturnDeliveryAndReturnSlipNumber() {
        // given
        RegisterInternalReturnDeliveryCommand command = createCommand(100L);

        FulfillmentAccessToken accessToken = FulfillmentAccessToken.of("test-token", "20261231235959");
        when(requestFulfillmentAuthPort.requestAccessToken()).thenReturn(accessToken);
        when(getFulfillmentCustomerCodePort.getCustomerCode()).thenReturn("CUST001");

        RegisterFulfillmentDeliveryResult fasstoResult = RegisterFulfillmentDeliveryResult.of(1, List.of(
                RegisterFulfillmentDeliveryItemResult.of("RTN-SLIP-001", "100", "성공", "00", null)
        ));
        when(registerFulfillmentReturnDeliveryPort.registerReturnDelivery(any())).thenReturn(fasstoResult);

        // when
        RegisterInternalReturnDeliveryResult result = registerInternalReturnDeliveryService.registerReturnDelivery(command);

        // then
        assertThat(result.orderId()).isEqualTo(100L);
        assertThat(result.returnSlipNumber()).isEqualTo("RTN-SLIP-001");
        assertThat(result.registered()).isTrue();
        assertThat(result.message()).isEqualTo("성공");
        verify(registerFulfillmentReturnDeliveryPort).registerReturnDelivery(any());
        verify(disconnectFulfillmentAuthPort).disconnectAccessToken("test-token");
    }

    @Test
    @DisplayName("Fassto 반품 등록 실패 시 예외가 전파되고 인증 해제는 호출된다")
    void shouldPropagateExceptionAndDisconnectWhenRegistrationFails() {
        // given
        RegisterInternalReturnDeliveryCommand command = createCommand(100L);

        FulfillmentAccessToken accessToken = FulfillmentAccessToken.of("test-token", "20261231235959");
        when(requestFulfillmentAuthPort.requestAccessToken()).thenReturn(accessToken);
        when(getFulfillmentCustomerCodePort.getCustomerCode()).thenReturn("CUST001");
        when(registerFulfillmentReturnDeliveryPort.registerReturnDelivery(any()))
                .thenThrow(new RuntimeException("Fassto 반품 등록 실패"));

        // when & then
        assertThatThrownBy(() -> registerInternalReturnDeliveryService.registerReturnDelivery(command))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Fassto 반품 등록 실패");

        verify(disconnectFulfillmentAuthPort).disconnectAccessToken("test-token");
    }

    @Test
    @DisplayName("반품 등록 성공 시 인증 발급 → 반품 등록 → 인증 해제 순서로 실행된다")
    void shouldExecuteInCorrectOrder() {
        // given
        RegisterInternalReturnDeliveryCommand command = createCommand(100L);

        FulfillmentAccessToken accessToken = FulfillmentAccessToken.of("test-token", "20261231235959");
        when(requestFulfillmentAuthPort.requestAccessToken()).thenReturn(accessToken);
        when(getFulfillmentCustomerCodePort.getCustomerCode()).thenReturn("CUST001");

        RegisterFulfillmentDeliveryResult fasstoResult = RegisterFulfillmentDeliveryResult.of(1, List.of(
                RegisterFulfillmentDeliveryItemResult.of("RTN-SLIP-001", "100", "성공", "00", null)
        ));
        when(registerFulfillmentReturnDeliveryPort.registerReturnDelivery(any())).thenReturn(fasstoResult);

        // when
        registerInternalReturnDeliveryService.registerReturnDelivery(command);

        // then
        InOrder inOrder = inOrder(requestFulfillmentAuthPort, registerFulfillmentReturnDeliveryPort, disconnectFulfillmentAuthPort);
        inOrder.verify(requestFulfillmentAuthPort).requestAccessToken();
        inOrder.verify(registerFulfillmentReturnDeliveryPort).registerReturnDelivery(any());
        inOrder.verify(disconnectFulfillmentAuthPort).disconnectAccessToken("test-token");
    }

    private RegisterInternalReturnDeliveryCommand createCommand(Long orderId) {
        return RegisterInternalReturnDeliveryCommand.builder()
                .orderId(orderId)
                .orderDate("20260409")
                .recipientName("수령인")
                .recipientPhoneNumber("01012345678")
                .recipientAddress("서울시 강남구 테헤란로")
                .pickupRecipientName("회수 수령인")
                .pickupRecipientPhoneNumber("01099998888")
                .pickupZipCode("06234")
                .pickupAddress("서울시 강남구")
                .pickupAddressDetail("상세주소")
                .returnReason("단순 변심")
                .returnDetailReason("상세 사유")
                .returnShippingRequest("부재시 경비실")
                .products(List.of(
                        RegisterInternalReturnDeliveryProductCommand.of("100", 2)
                ))
                .build();
    }
}
