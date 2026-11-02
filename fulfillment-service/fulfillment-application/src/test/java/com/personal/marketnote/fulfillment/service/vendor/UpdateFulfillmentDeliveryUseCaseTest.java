package com.personal.marketnote.fulfillment.service.vendor;

import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentDeliveryGoodsCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.UpdateFulfillmentDeliveryCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.UpdateFulfillmentDeliveryItemCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.RegisterFulfillmentDeliveryResult;
import com.personal.marketnote.fulfillment.port.out.vendor.UpdateFulfillmentDeliveryPort;
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
@DisplayName("UpdateFulfillmentDeliveryService 테스트")
class UpdateFulfillmentDeliveryUseCaseTest {
    @InjectMocks
    private UpdateFulfillmentDeliveryService updateFulfillmentDeliveryService;

    @Mock
    private UpdateFulfillmentDeliveryPort updateFulfillmentDeliveryPort;

    @Test
    @DisplayName("출고 수정 커맨드를 전달하면 포트를 통해 수정 결과를 반환한다")
    void shouldReturnUpdateDeliveryResultFromPort() {
        // given
        UpdateFulfillmentDeliveryItemCommand itemCommand = UpdateFulfillmentDeliveryItemCommand.builder()
                .orderDate("20260402")
                .orderNumber("ORD001")
                .slipNumber("SLIP001")
                .recipientName("테스트고객")
                .recipientPhoneNumber("01012345678")
                .recipientAddress("서울시 강남구")
                .releaseMethod("01")
                .products(List.of(RegisterFulfillmentDeliveryGoodsCommand.of("GOD001", "20260430", 1)))
                .build();
        UpdateFulfillmentDeliveryCommand command = UpdateFulfillmentDeliveryCommand.of(
                "CUST001", "token", List.of(itemCommand)
        );
        RegisterFulfillmentDeliveryResult expectedResult = RegisterFulfillmentDeliveryResult.of(0, List.of());
        when(updateFulfillmentDeliveryPort.updateDelivery(any())).thenReturn(expectedResult);

        // when
        RegisterFulfillmentDeliveryResult result = updateFulfillmentDeliveryService.updateDelivery(command);

        // then
        assertThat(result).isEqualTo(expectedResult);
        verify(updateFulfillmentDeliveryPort).updateDelivery(any());
    }
}
