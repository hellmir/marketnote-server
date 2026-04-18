package com.personal.marketnote.fulfillment.service.vendor;

import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentReturnDeliveryCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentReturnDeliveryItemCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.RegisterFulfillmentDeliveryResult;
import com.personal.marketnote.fulfillment.port.out.vendor.RegisterFulfillmentReturnDeliveryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("RegisterFulfillmentReturnDeliveryService 테스트")
class RegisterFulfillmentReturnDeliveryUseCaseTest {
    @InjectMocks
    private RegisterFulfillmentReturnDeliveryService registerFulfillmentReturnDeliveryService;

    @Mock
    private RegisterFulfillmentReturnDeliveryPort registerFulfillmentReturnDeliveryPort;

    @Test
    @DisplayName("반품 등록 커맨드를 전달하면 포트를 통해 등록 결과를 반환한다")
    void shouldReturnRegisterReturnDeliveryResultFromPort() {
        // given
        RegisterFulfillmentReturnDeliveryItemCommand itemCommand = RegisterFulfillmentReturnDeliveryItemCommand.builder()
                .orderDate("20260402")
                .orderNumber("ORD001")
                .courierCode("PARCEL001")
                .invoiceNumber("INV001")
                .recipientName("테스트고객")
                .recipientPhoneNumber("01012345678")
                .recipientAddress("서울시 강남구")
                .build();
        RegisterFulfillmentReturnDeliveryCommand command = RegisterFulfillmentReturnDeliveryCommand.of(
                "CUST001", "token", List.of(itemCommand)
        );
        RegisterFulfillmentDeliveryResult expectedResult = RegisterFulfillmentDeliveryResult.of(0, List.of());
        when(registerFulfillmentReturnDeliveryPort.registerReturnDelivery(any())).thenReturn(expectedResult);

        // when
        RegisterFulfillmentDeliveryResult result = registerFulfillmentReturnDeliveryService.registerReturnDelivery(command);

        // then
        assertThat(result).isEqualTo(expectedResult);
        verify(registerFulfillmentReturnDeliveryPort).registerReturnDelivery(any());
    }
}
