package com.personal.marketnote.fulfillment.service.vendor;

import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentReturnGodDetailCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentReturnGodDetailResult;
import com.personal.marketnote.fulfillment.port.out.vendor.GetFulfillmentReturnGodDetailPort;
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
@DisplayName("GetFulfillmentReturnGodDetailService 테스트")
class GetFulfillmentReturnGodDetailUseCaseTest {
    @InjectMocks
    private GetFulfillmentReturnGodDetailService getFulfillmentReturnGodDetailService;

    @Mock
    private GetFulfillmentReturnGodDetailPort getFulfillmentReturnGodDetailPort;

    @Test
    @DisplayName("반품 상품 상세 조회 커맨드를 전달하면 포트를 통해 상세 정보를 반환한다")
    void shouldReturnReturnGodDetailResultFromPort() {
        // given
        GetFulfillmentReturnGodDetailCommand command = GetFulfillmentReturnGodDetailCommand.builder()
                .customerCode("CUST001")
                .accessToken("token")
                .startDate("2026-01-01")
                .endDate("2026-01-31")
                .returnSlipNumbers("RTN001")
                .warehouseCode("WH001")
                .build();
        GetFulfillmentReturnGodDetailResult expectedResult = GetFulfillmentReturnGodDetailResult.of(0, List.of());
        when(getFulfillmentReturnGodDetailPort.getReturnGodDetail(any())).thenReturn(expectedResult);

        // when
        GetFulfillmentReturnGodDetailResult result = getFulfillmentReturnGodDetailService.getReturnGodDetail(command);

        // then
        assertThat(result).isEqualTo(expectedResult);
        verify(getFulfillmentReturnGodDetailPort).getReturnGodDetail(any());
    }
}
