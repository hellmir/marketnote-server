package com.personal.marketnote.fulfillment.service.vendor;

import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentDeliveryGoodDetailCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentDeliveryGoodDetailResult;
import com.personal.marketnote.fulfillment.port.out.vendor.GetFulfillmentDeliveryGoodDetailPort;
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
@DisplayName("GetFulfillmentDeliveryGoodDetailService 테스트")
class GetFulfillmentDeliveryGoodDetailUseCaseTest {
    @InjectMocks
    private GetFulfillmentDeliveryGoodDetailService getFulfillmentDeliveryGoodDetailService;

    @Mock
    private GetFulfillmentDeliveryGoodDetailPort getFulfillmentDeliveryGoodDetailPort;

    @Test
    @DisplayName("출고 상품 상세 조회 커맨드를 전달하면 포트를 통해 상세 정보를 반환한다")
    void shouldReturnDeliveryGoodDetailResultFromPort() {
        // given
        GetFulfillmentDeliveryGoodDetailCommand command = GetFulfillmentDeliveryGoodDetailCommand.of(
                "CUST001", "token", "2026-01-01", "2026-01-31", "ORD001"
        );
        GetFulfillmentDeliveryGoodDetailResult expectedResult = GetFulfillmentDeliveryGoodDetailResult.of(0, List.of());
        when(getFulfillmentDeliveryGoodDetailPort.getDeliveryGoodDetail(any())).thenReturn(expectedResult);

        // when
        GetFulfillmentDeliveryGoodDetailResult result = getFulfillmentDeliveryGoodDetailService.getDeliveryGoodDetail(command);

        // then
        assertThat(result).isEqualTo(expectedResult);
        verify(getFulfillmentDeliveryGoodDetailPort).getDeliveryGoodDetail(any());
    }
}
