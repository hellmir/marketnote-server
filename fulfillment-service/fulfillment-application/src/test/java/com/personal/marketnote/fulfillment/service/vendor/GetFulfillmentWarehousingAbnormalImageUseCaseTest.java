package com.personal.marketnote.fulfillment.service.vendor;

import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentWarehousingAbnormalImageCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentWarehousingAbnormalImageResult;
import com.personal.marketnote.fulfillment.port.out.vendor.GetFulfillmentWarehousingAbnormalImagePort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("풀필먼트 입고 이상 이미지 조회 테스트")
class GetFulfillmentWarehousingAbnormalImageUseCaseTest {
    @InjectMocks
    private GetFulfillmentWarehousingAbnormalImageService service;
    @Mock
    private GetFulfillmentWarehousingAbnormalImagePort getFulfillmentWarehousingAbnormalImagePort;

    @Test
    @DisplayName("Command를 Port에 위임하여 결과를 반환한다")
    void shouldDelegateToPort() {
        // given
        GetFulfillmentWarehousingAbnormalImageCommand command = new GetFulfillmentWarehousingAbnormalImageCommand("token", "SLIP001", "GOD001", "SN001", "1", "CUST001");
        GetFulfillmentWarehousingAbnormalImageResult expectedResult = GetFulfillmentWarehousingAbnormalImageResult.of(0, null);
        when(getFulfillmentWarehousingAbnormalImagePort.getWarehousingAbnormalImage(any())).thenReturn(expectedResult);
        // when
        GetFulfillmentWarehousingAbnormalImageResult result = service.getWarehousingAbnormalImage(command);
        // then
        assertThat(result).isEqualTo(expectedResult);
        verify(getFulfillmentWarehousingAbnormalImagePort).getWarehousingAbnormalImage(any());
    }
}
