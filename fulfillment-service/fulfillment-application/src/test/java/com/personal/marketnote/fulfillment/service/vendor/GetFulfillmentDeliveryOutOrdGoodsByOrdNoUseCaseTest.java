package com.personal.marketnote.fulfillment.service.vendor;

import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentDeliveryOutOrdGoodsByOrdNoCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentDeliveryOutOrdGoodsByOrdNoResult;
import com.personal.marketnote.fulfillment.port.out.vendor.GetFulfillmentDeliveryOutOrdGoodsByOrdNoPort;
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
@DisplayName("GetFulfillmentDeliveryOutOrdGoodsByOrdNoService 테스트")
class GetFulfillmentDeliveryOutOrdGoodsByOrdNoUseCaseTest {
    @InjectMocks
    private GetFulfillmentDeliveryOutOrdGoodsByOrdNoService getFulfillmentDeliveryOutOrdGoodsByOrdNoService;

    @Mock
    private GetFulfillmentDeliveryOutOrdGoodsByOrdNoPort getFulfillmentDeliveryOutOrdGoodsByOrdNoPort;

    @Test
    @DisplayName("주문번호별 출고 상품 조회 커맨드를 전달하면 포트를 통해 결과를 반환한다")
    void shouldReturnOutOrdGoodsByOrdNoResultFromPort() {
        // given
        GetFulfillmentDeliveryOutOrdGoodsByOrdNoCommand command = GetFulfillmentDeliveryOutOrdGoodsByOrdNoCommand.of(
                "CUST001", "token", "2026-01-01", "2026-01-31", "ORD001"
        );
        GetFulfillmentDeliveryOutOrdGoodsByOrdNoResult expectedResult = GetFulfillmentDeliveryOutOrdGoodsByOrdNoResult.of(0, List.of());
        when(getFulfillmentDeliveryOutOrdGoodsByOrdNoPort.getOutOrdGoodsByOrdNo(any())).thenReturn(expectedResult);

        // when
        GetFulfillmentDeliveryOutOrdGoodsByOrdNoResult result = getFulfillmentDeliveryOutOrdGoodsByOrdNoService.getOutOrdGoodsByOrdNo(command);

        // then
        assertThat(result).isEqualTo(expectedResult);
        verify(getFulfillmentDeliveryOutOrdGoodsByOrdNoPort).getOutOrdGoodsByOrdNo(any());
    }
}
