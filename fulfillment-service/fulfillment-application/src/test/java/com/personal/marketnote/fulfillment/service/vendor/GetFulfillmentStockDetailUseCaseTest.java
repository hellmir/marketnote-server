package com.personal.marketnote.fulfillment.service.vendor;

import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentStockDetailCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentStocksResult;
import com.personal.marketnote.fulfillment.port.out.vendor.GetFulfillmentStockDetailPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("풀필먼트 재고 상세 조회 테스트")
class GetFulfillmentStockDetailUseCaseTest {
    @InjectMocks
    private GetFulfillmentStockDetailService service;

    @Mock
    private GetFulfillmentStockDetailPort getFulfillmentStockDetailPort;

    @Test
    @DisplayName("재고 상세 조회 커맨드를 포트에 직접 전달하여 결과를 반환한다")
    void shouldDelegateCommandDirectlyToPort() {
        // given
        GetFulfillmentStockDetailCommand command = GetFulfillmentStockDetailCommand.of(
                "CUST001", "token", "GOD001", "Y"
        );
        GetFulfillmentStocksResult expectedResult = GetFulfillmentStocksResult.of(0, List.of());
        when(getFulfillmentStockDetailPort.getStockDetail(command)).thenReturn(expectedResult);

        // when
        GetFulfillmentStocksResult result = service.getStockDetail(command);

        // then
        assertThat(result).isEqualTo(expectedResult);
        verify(getFulfillmentStockDetailPort).getStockDetail(command);
    }
}
