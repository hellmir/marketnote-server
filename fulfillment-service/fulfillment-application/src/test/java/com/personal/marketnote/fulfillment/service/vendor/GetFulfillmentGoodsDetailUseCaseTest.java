package com.personal.marketnote.fulfillment.service.vendor;

import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentGoodsDetailCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentGoodsResult;
import com.personal.marketnote.fulfillment.port.out.vendor.GetFulfillmentGoodsDetailPort;
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
@DisplayName("풀필먼트 상품 상세 조회 테스트")
class GetFulfillmentGoodsDetailUseCaseTest {
    @InjectMocks
    private GetFulfillmentGoodsDetailService service;
    @Mock
    private GetFulfillmentGoodsDetailPort getFulfillmentGoodsDetailPort;

    @Test
    @DisplayName("Command를 Port에 위임하여 결과를 반환한다")
    void shouldDelegateToPort() {
        // given
        GetFulfillmentGoodsDetailCommand command = new GetFulfillmentGoodsDetailCommand("CUST001", "token", "GOODS001");
        GetFulfillmentGoodsResult expectedResult = GetFulfillmentGoodsResult.of(0, List.of());
        when(getFulfillmentGoodsDetailPort.getGoodsDetail(any())).thenReturn(expectedResult);
        // when
        GetFulfillmentGoodsResult result = service.getGoodsDetail(command);
        // then
        assertThat(result).isEqualTo(expectedResult);
        verify(getFulfillmentGoodsDetailPort).getGoodsDetail(any());
    }
}
