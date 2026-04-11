package com.personal.marketnote.fulfillment.service.vendor;

import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentDeliveryCarCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentDeliveryCarItemCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentDeliveryGoodsCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.RegisterFulfillmentDeliveryResult;
import com.personal.marketnote.fulfillment.port.out.vendor.RegisterFulfillmentDeliveryCarPort;
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
@DisplayName("RegisterFulfillmentDeliveryCarService 테스트")
class RegisterFulfillmentDeliveryCarUseCaseTest {
    @InjectMocks
    private RegisterFulfillmentDeliveryCarService registerFulfillmentDeliveryCarService;

    @Mock
    private RegisterFulfillmentDeliveryCarPort registerFulfillmentDeliveryCarPort;

    @Test
    @DisplayName("출고 집차 등록 커맨드를 전달하면 포트를 통해 등록 결과를 반환한다")
    void shouldReturnRegisterDeliveryCarResultFromPort() {
        // given
        RegisterFulfillmentDeliveryCarItemCommand itemCommand = RegisterFulfillmentDeliveryCarItemCommand.builder()
                .ordDt("20260402")
                .ordNo("ORD001")
                .outWay("01")
                .cstShopCd("SHOP001")
                .godCds(List.of(RegisterFulfillmentDeliveryGoodsCommand.of("GOD001", "20260430", 1)))
                .build();
        RegisterFulfillmentDeliveryCarCommand command = RegisterFulfillmentDeliveryCarCommand.of(
                "CUST001", "token", List.of(itemCommand)
        );
        RegisterFulfillmentDeliveryResult expectedResult = RegisterFulfillmentDeliveryResult.of(0, List.of());
        when(registerFulfillmentDeliveryCarPort.registerDeliveryCar(any())).thenReturn(expectedResult);

        // when
        RegisterFulfillmentDeliveryResult result = registerFulfillmentDeliveryCarService.registerDeliveryCar(command);

        // then
        assertThat(result).isEqualTo(expectedResult);
        verify(registerFulfillmentDeliveryCarPort).registerDeliveryCar(any());
    }
}
