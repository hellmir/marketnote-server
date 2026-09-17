package com.personal.marketnote.fulfillment.service.vendor;

import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentWarehousingCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentWarehousingResult;
import com.personal.marketnote.fulfillment.port.out.vendor.GetFulfillmentWarehousingPort;
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
@DisplayName("GetFulfillmentWarehousingService 테스트")
class GetFulfillmentWarehousingUseCaseTest {
    @InjectMocks
    private GetFulfillmentWarehousingService getFulfillmentWarehousingService;

    @Mock
    private GetFulfillmentWarehousingPort getFulfillmentWarehousingPort;

    @Test
    @DisplayName("입고 목록 조회 커맨드를 전달하면 포트를 통해 입고 목록을 반환한다")
    void shouldReturnWarehousingListFromPort() {
        // given
        GetFulfillmentWarehousingCommand command = GetFulfillmentWarehousingCommand.of(
                "CUST001", "token", "2026-01-01", "2026-01-31"
        );
        GetFulfillmentWarehousingResult expectedResult = GetFulfillmentWarehousingResult.of(0, List.of());
        when(getFulfillmentWarehousingPort.getWarehousing(any())).thenReturn(expectedResult);

        // when
        GetFulfillmentWarehousingResult result = getFulfillmentWarehousingService.getWarehousing(command);

        // then
        assertThat(result).isEqualTo(expectedResult);
        verify(getFulfillmentWarehousingPort).getWarehousing(any());
    }
}
