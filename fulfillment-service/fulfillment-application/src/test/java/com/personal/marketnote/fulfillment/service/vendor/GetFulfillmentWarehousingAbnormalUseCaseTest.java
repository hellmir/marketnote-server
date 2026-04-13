package com.personal.marketnote.fulfillment.service.vendor;

import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentWarehousingAbnormalCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentWarehousingAbnormalResult;
import com.personal.marketnote.fulfillment.port.out.vendor.GetFulfillmentWarehousingAbnormalPort;
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
@DisplayName("풀필먼트 입고 이상 조회 테스트")
class GetFulfillmentWarehousingAbnormalUseCaseTest {
    @InjectMocks
    private GetFulfillmentWarehousingAbnormalService service;
    @Mock
    private GetFulfillmentWarehousingAbnormalPort getFulfillmentWarehousingAbnormalPort;

    @Test
    @DisplayName("Command를 Port에 위임하여 결과를 반환한다")
    void shouldDelegateToPort() {
        // given
        GetFulfillmentWarehousingAbnormalCommand command = new GetFulfillmentWarehousingAbnormalCommand("CUST001", "token", "WH001", "SLIP001");
        GetFulfillmentWarehousingAbnormalResult expectedResult = GetFulfillmentWarehousingAbnormalResult.of(0, List.of());
        when(getFulfillmentWarehousingAbnormalPort.getWarehousingAbnormal(any())).thenReturn(expectedResult);
        // when
        GetFulfillmentWarehousingAbnormalResult result = service.getWarehousingAbnormal(command);
        // then
        assertThat(result).isEqualTo(expectedResult);
        verify(getFulfillmentWarehousingAbnormalPort).getWarehousingAbnormal(any());
    }
}
