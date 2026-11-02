package com.personal.marketnote.fulfillment.service.vendor;

import com.personal.marketnote.fulfillment.port.in.command.vendor.UpdateFulfillmentWarehousingCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.UpdateFulfillmentWarehousingGoodsCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.UpdateFulfillmentWarehousingItemCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.UpdateFulfillmentWarehousingResult;
import com.personal.marketnote.fulfillment.port.out.vendor.UpdateFulfillmentWarehousingPort;
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
@DisplayName("풀필먼트 입고 수정 테스트")
class UpdateFulfillmentWarehousingUseCaseTest {
    @InjectMocks
    private UpdateFulfillmentWarehousingService service;
    @Mock
    private UpdateFulfillmentWarehousingPort updateFulfillmentWarehousingPort;

    @Test
    @DisplayName("Command를 Port에 위임하여 결과를 반환한다")
    void shouldDelegateToPort() {
        // given
        UpdateFulfillmentWarehousingItemCommand item = UpdateFulfillmentWarehousingItemCommand.of(
                "20260402", "ORD001", "01", "SLIP001", null, null, null, null, null, null, null,
                List.of(UpdateFulfillmentWarehousingGoodsCommand.of("GOD001", "20260501", 10))
        );
        UpdateFulfillmentWarehousingCommand command = UpdateFulfillmentWarehousingCommand.of("CUST001", "token", List.of(item));
        UpdateFulfillmentWarehousingResult expectedResult = UpdateFulfillmentWarehousingResult.of(0, List.of());
        when(updateFulfillmentWarehousingPort.updateWarehousing(any())).thenReturn(expectedResult);
        // when
        UpdateFulfillmentWarehousingResult result = service.updateWarehousing(command);
        // then
        assertThat(result).isEqualTo(expectedResult);
        verify(updateFulfillmentWarehousingPort).updateWarehousing(any());
    }
}
