package com.personal.marketnote.fulfillment.service.vendor;

import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentDeliveryOutOrdGoodsDetailCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentDeliveryOutOrdGoodsDetailResult;
import com.personal.marketnote.fulfillment.port.out.vendor.GetFulfillmentDeliveryOutOrdGoodsDetailPort;
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
@DisplayName("GetFulfillmentDeliveryOutOrdGoodsDetailService 테스트")
class GetFulfillmentDeliveryOutOrdGoodsDetailUseCaseTest {
    @InjectMocks
    private GetFulfillmentDeliveryOutOrdGoodsDetailService getFulfillmentDeliveryOutOrdGoodsDetailService;

    @Mock
    private GetFulfillmentDeliveryOutOrdGoodsDetailPort getFulfillmentDeliveryOutOrdGoodsDetailPort;

    @Test
    @DisplayName("출고 주문 상품 상세 조회 커맨드를 전달하면 포트를 통해 상세 정보를 반환한다")
    void shouldReturnOutOrdGoodsDetailResultFromPort() {
        // given
        GetFulfillmentDeliveryOutOrdGoodsDetailCommand command = GetFulfillmentDeliveryOutOrdGoodsDetailCommand.of(
                "CUST001", "token", "SLIP001"
        );
        GetFulfillmentDeliveryOutOrdGoodsDetailResult expectedResult = GetFulfillmentDeliveryOutOrdGoodsDetailResult.of(0, List.of());
        when(getFulfillmentDeliveryOutOrdGoodsDetailPort.getOutOrdGoodsDetail(any())).thenReturn(expectedResult);

        // when
        GetFulfillmentDeliveryOutOrdGoodsDetailResult result = getFulfillmentDeliveryOutOrdGoodsDetailService.getOutOrdGoodsDetail(command);

        // then
        assertThat(result).isEqualTo(expectedResult);
        verify(getFulfillmentDeliveryOutOrdGoodsDetailPort).getOutOrdGoodsDetail(any());
    }
}
