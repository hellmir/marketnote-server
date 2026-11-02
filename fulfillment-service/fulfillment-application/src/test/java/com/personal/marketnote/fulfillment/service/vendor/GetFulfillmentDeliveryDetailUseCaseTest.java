package com.personal.marketnote.fulfillment.service.vendor;

import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentDeliveryDetailCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentDeliveryDetailResult;
import com.personal.marketnote.fulfillment.port.out.vendor.GetFulfillmentDeliveryDetailPort;
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
@DisplayName("GetFulfillmentDeliveryDetailService 테스트")
class GetFulfillmentDeliveryDetailUseCaseTest {
    @InjectMocks
    private GetFulfillmentDeliveryDetailService getFulfillmentDeliveryDetailService;

    @Mock
    private GetFulfillmentDeliveryDetailPort getFulfillmentDeliveryDetailPort;

    @Test
    @DisplayName("출고 상세 조회 커맨드를 전달하면 포트를 통해 상세 정보를 반환한다")
    void shouldReturnDeliveryDetailResultFromPort() {
        // given
        GetFulfillmentDeliveryDetailCommand command = GetFulfillmentDeliveryDetailCommand.of(
                "CUST001", "token", "SLIP001"
        );
        GetFulfillmentDeliveryDetailResult expectedResult = GetFulfillmentDeliveryDetailResult.of(0, List.of());
        when(getFulfillmentDeliveryDetailPort.getDeliveryDetail(any())).thenReturn(expectedResult);

        // when
        GetFulfillmentDeliveryDetailResult result = getFulfillmentDeliveryDetailService.getDeliveryDetail(command);

        // then
        assertThat(result).isEqualTo(expectedResult);
        verify(getFulfillmentDeliveryDetailPort).getDeliveryDetail(any());
    }
}
