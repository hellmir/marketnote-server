package com.personal.marketnote.fulfillment.service.vendor;

import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentDeliveryGoodsCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.UpdateFulfillmentDeliveryCarCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.UpdateFulfillmentDeliveryCarItemCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.RegisterFulfillmentDeliveryResult;
import com.personal.marketnote.fulfillment.port.out.vendor.UpdateFulfillmentDeliveryCarPort;
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
@DisplayName("UpdateFulfillmentDeliveryCarService 테스트")
class UpdateFulfillmentDeliveryCarUseCaseTest {
    @InjectMocks
    private UpdateFulfillmentDeliveryCarService updateFulfillmentDeliveryCarService;

    @Mock
    private UpdateFulfillmentDeliveryCarPort updateFulfillmentDeliveryCarPort;

    @Test
    @DisplayName("출고 집차 수정 커맨드를 전달하면 포트를 통해 수정 결과를 반환한다")
    void shouldReturnUpdateDeliveryCarResultFromPort() {
        // given
        UpdateFulfillmentDeliveryCarItemCommand itemCommand = UpdateFulfillmentDeliveryCarItemCommand.builder()
                .orderDate("20260402")
                .orderNumber("ORD001")
                .slipNumber("SLIP001")
                .releaseMethod("01")
                .shopCode("SHOP001")
                .products(List.of(RegisterFulfillmentDeliveryGoodsCommand.of("GOD001", "20260430", 1)))
                .build();
        UpdateFulfillmentDeliveryCarCommand command = UpdateFulfillmentDeliveryCarCommand.of(
                "CUST001", "token", List.of(itemCommand)
        );
        RegisterFulfillmentDeliveryResult expectedResult = RegisterFulfillmentDeliveryResult.of(0, List.of());
        when(updateFulfillmentDeliveryCarPort.updateDeliveryCar(any())).thenReturn(expectedResult);

        // when
        RegisterFulfillmentDeliveryResult result = updateFulfillmentDeliveryCarService.updateDeliveryCar(command);

        // then
        assertThat(result).isEqualTo(expectedResult);
        verify(updateFulfillmentDeliveryCarPort).updateDeliveryCar(any());
    }
}
