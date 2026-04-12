package com.personal.marketnote.fulfillment.service.vendor;

import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentDeliveryGoodsCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentDeliveryIcsCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentDeliveryIcsItemCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.RegisterFulfillmentDeliveryResult;
import com.personal.marketnote.fulfillment.port.out.vendor.RegisterFulfillmentDeliveryIcsPort;
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
@DisplayName("RegisterFulfillmentDeliveryIcsService 테스트")
class RegisterFulfillmentDeliveryIcsUseCaseTest {
    @InjectMocks
    private RegisterFulfillmentDeliveryIcsService registerFulfillmentDeliveryIcsService;

    @Mock
    private RegisterFulfillmentDeliveryIcsPort registerFulfillmentDeliveryIcsPort;

    @Test
    @DisplayName("출고 ICS 등록 커맨드를 전달하면 포트를 통해 등록 결과를 반환한다")
    void shouldReturnRegisterDeliveryIcsResultFromPort() {
        // given
        RegisterFulfillmentDeliveryIcsItemCommand itemCommand = RegisterFulfillmentDeliveryIcsItemCommand.builder()
                .ordDt("20260402")
                .ordNo("ORD001")
                .platform("PLATFORM01")
                .logiCenter("CENTER01")
                .invoiceNo("INV001")
                .godCds(List.of(RegisterFulfillmentDeliveryGoodsCommand.of("GOD001", "20260430", 1)))
                .build();
        RegisterFulfillmentDeliveryIcsCommand command = RegisterFulfillmentDeliveryIcsCommand.of(
                "CUST001", "token", List.of(itemCommand)
        );
        RegisterFulfillmentDeliveryResult expectedResult = RegisterFulfillmentDeliveryResult.of(0, List.of());
        when(registerFulfillmentDeliveryIcsPort.registerDeliveryIcs(any())).thenReturn(expectedResult);

        // when
        RegisterFulfillmentDeliveryResult result = registerFulfillmentDeliveryIcsService.registerDeliveryIcs(command);

        // then
        assertThat(result).isEqualTo(expectedResult);
        verify(registerFulfillmentDeliveryIcsPort).registerDeliveryIcs(any());
    }
}
