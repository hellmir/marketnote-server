package com.personal.marketnote.fulfillment.service.vendor;

import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentDeliveriesCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentDeliveriesResult;
import com.personal.marketnote.fulfillment.port.out.vendor.GetFulfillmentDeliveriesPort;
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
@DisplayName("GetFulfillmentDeliveriesService 테스트")
class GetFulfillmentDeliveriesUseCaseTest {
    @InjectMocks
    private GetFulfillmentDeliveriesService getFulfillmentDeliveriesService;

    @Mock
    private GetFulfillmentDeliveriesPort getFulfillmentDeliveriesPort;

    @Test
    @DisplayName("출고 목록 조회 커맨드를 전달하면 포트를 통해 출고 목록을 반환한다")
    void shouldReturnDeliveriesFromPort() {
        // given
        GetFulfillmentDeliveriesCommand command = GetFulfillmentDeliveriesCommand.of(
                "CUST001", "token", "20260401", "20260402", "01", "01"
        );
        GetFulfillmentDeliveriesResult expectedResult = GetFulfillmentDeliveriesResult.of(0, List.of());
        when(getFulfillmentDeliveriesPort.getDeliveries(any())).thenReturn(expectedResult);

        // when
        GetFulfillmentDeliveriesResult result = getFulfillmentDeliveriesService.getDeliveries(command);

        // then
        assertThat(result).isEqualTo(expectedResult);
        verify(getFulfillmentDeliveriesPort).getDeliveries(any());
    }
}
