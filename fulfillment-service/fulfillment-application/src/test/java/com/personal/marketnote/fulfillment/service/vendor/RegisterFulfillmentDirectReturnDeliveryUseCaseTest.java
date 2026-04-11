package com.personal.marketnote.fulfillment.service.vendor;

import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentDirectReturnDeliveryCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentDirectReturnDeliveryItemCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.RegisterFulfillmentDeliveryResult;
import com.personal.marketnote.fulfillment.port.out.vendor.RegisterFulfillmentDirectReturnDeliveryPort;
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
@DisplayName("RegisterFulfillmentDirectReturnDeliveryService 테스트")
class RegisterFulfillmentDirectReturnDeliveryUseCaseTest {
    @InjectMocks
    private RegisterFulfillmentDirectReturnDeliveryService registerFulfillmentDirectReturnDeliveryService;

    @Mock
    private RegisterFulfillmentDirectReturnDeliveryPort registerFulfillmentDirectReturnDeliveryPort;

    @Test
    @DisplayName("직접 반품 등록 커맨드를 전달하면 포트를 통해 등록 결과를 반환한다")
    void shouldReturnRegisterDirectReturnDeliveryResultFromPort() {
        // given
        RegisterFulfillmentDirectReturnDeliveryItemCommand itemCommand = RegisterFulfillmentDirectReturnDeliveryItemCommand.builder()
                .ordDt("20260402")
                .orgParcelCd("PARCEL001")
                .orgInvoiceNo("INV001")
                .inWay("01")
                .custNm("테스트고객")
                .rtnGubun("01")
                .rtnReason("단순변심")
                .build();
        RegisterFulfillmentDirectReturnDeliveryCommand command = RegisterFulfillmentDirectReturnDeliveryCommand.of(
                "CUST001", "token", List.of(itemCommand)
        );
        RegisterFulfillmentDeliveryResult expectedResult = RegisterFulfillmentDeliveryResult.of(0, List.of());
        when(registerFulfillmentDirectReturnDeliveryPort.registerDirectReturnDelivery(any())).thenReturn(expectedResult);

        // when
        RegisterFulfillmentDeliveryResult result = registerFulfillmentDirectReturnDeliveryService.registerDirectReturnDelivery(command);

        // then
        assertThat(result).isEqualTo(expectedResult);
        verify(registerFulfillmentDirectReturnDeliveryPort).registerDirectReturnDelivery(any());
    }
}
