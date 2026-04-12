package com.personal.marketnote.fulfillment.service.vendor;

import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentWarehousingDetailCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentWarehousingDetailResult;
import com.personal.marketnote.fulfillment.port.out.vendor.GetFulfillmentWarehousingDetailPort;
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
@DisplayName("GetFulfillmentWarehousingDetailService 테스트")
class GetFulfillmentWarehousingDetailUseCaseTest {
    @InjectMocks
    private GetFulfillmentWarehousingDetailService getFulfillmentWarehousingDetailService;

    @Mock
    private GetFulfillmentWarehousingDetailPort getFulfillmentWarehousingDetailPort;

    @Test
    @DisplayName("입고 상세 조회 커맨드를 전달하면 포트를 통해 입고 상세 정보를 반환한다")
    void shouldReturnWarehousingDetailFromPort() {
        // given
        GetFulfillmentWarehousingDetailCommand command = GetFulfillmentWarehousingDetailCommand.of(
                "CUST001", "token", "SLIP001", "ORD001"
        );
        GetFulfillmentWarehousingDetailResult expectedResult = GetFulfillmentWarehousingDetailResult.of(0, List.of());
        when(getFulfillmentWarehousingDetailPort.getWarehousingDetail(any())).thenReturn(expectedResult);

        // when
        GetFulfillmentWarehousingDetailResult result = getFulfillmentWarehousingDetailService.getWarehousingDetail(command);

        // then
        assertThat(result).isEqualTo(expectedResult);
        verify(getFulfillmentWarehousingDetailPort).getWarehousingDetail(any());
    }
}
