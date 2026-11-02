package com.personal.marketnote.fulfillment.service.vendor;

import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentSettlementDailyCostsCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentSettlementDailyCostsResult;
import com.personal.marketnote.fulfillment.port.out.vendor.GetFulfillmentSettlementDailyCostsPort;
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
@DisplayName("풀필먼트 정산 일별 비용 조회 테스트")
class GetFulfillmentSettlementDailyCostsUseCaseTest {
    @InjectMocks
    private GetFulfillmentSettlementDailyCostsService getFulfillmentSettlementDailyCostsService;

    @Mock
    private GetFulfillmentSettlementDailyCostsPort getFulfillmentSettlementDailyCostsPort;

    @Test
    @DisplayName("정산 일별 비용 조회 커맨드를 포트에 직접 전달하여 결과를 반환한다")
    void shouldDelegateCommandDirectlyToPort() {
        // given
        GetFulfillmentSettlementDailyCostsCommand command = GetFulfillmentSettlementDailyCostsCommand.of(
                "202604", "WH001", "CUST001", "token"
        );
        GetFulfillmentSettlementDailyCostsResult expectedResult = GetFulfillmentSettlementDailyCostsResult.of(0, List.of());
        when(getFulfillmentSettlementDailyCostsPort.getDailyCosts(command)).thenReturn(expectedResult);

        // when
        GetFulfillmentSettlementDailyCostsResult result = getFulfillmentSettlementDailyCostsService.getDailyCosts(command);

        // then
        assertThat(result).isEqualTo(expectedResult);
        verify(getFulfillmentSettlementDailyCostsPort).getDailyCosts(command);
    }
}
