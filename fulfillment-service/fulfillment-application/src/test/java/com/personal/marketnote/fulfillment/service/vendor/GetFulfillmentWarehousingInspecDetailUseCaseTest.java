package com.personal.marketnote.fulfillment.service.vendor;

import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentWarehousingInspecDetailCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentWarehousingInspecDetailResult;
import com.personal.marketnote.fulfillment.port.out.vendor.GetFulfillmentWarehousingInspecDetailPort;
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
@DisplayName("풀필먼트 입고 검수 상세 조회 테스트")
class GetFulfillmentWarehousingInspecDetailUseCaseTest {
    @InjectMocks
    private GetFulfillmentWarehousingInspecDetailService service;
    @Mock
    private GetFulfillmentWarehousingInspecDetailPort getFulfillmentWarehousingInspecDetailPort;

    @Test
    @DisplayName("Command를 Port에 위임하여 결과를 반환한다")
    void shouldDelegateToPort() {
        // given
        GetFulfillmentWarehousingInspecDetailCommand command = new GetFulfillmentWarehousingInspecDetailCommand("CUST001", "token", "SLIP001", "WH001");
        GetFulfillmentWarehousingInspecDetailResult expectedResult = GetFulfillmentWarehousingInspecDetailResult.of(0, List.of());
        when(getFulfillmentWarehousingInspecDetailPort.getWarehousingInspecDetail(any())).thenReturn(expectedResult);
        // when
        GetFulfillmentWarehousingInspecDetailResult result = service.getWarehousingInspecDetail(command);
        // then
        assertThat(result).isEqualTo(expectedResult);
        verify(getFulfillmentWarehousingInspecDetailPort).getWarehousingInspecDetail(any());
    }
}
