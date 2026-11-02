package com.personal.marketnote.fulfillment.adapter.in.web.vendor.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.response.GetFulfillmentSettlementDailyCostsResponse;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentSettlementDailyCostsCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentSettlementDailyCostsResult;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.GetFulfillmentSettlementDailyCostsUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("FulfillmentSettlementController 풀필먼트 정산 조회")
class FulfillmentSettlementControllerTest {

    private static final String CUSTOMER_CODE = "CUST001";
    private static final String ACCESS_TOKEN = "TOKEN";

    @InjectMocks
    private FulfillmentSettlementController controller;

    @Mock
    private GetFulfillmentSettlementDailyCostsUseCase getFulfillmentSettlementDailyCostsUseCase;

    @Nested
    @DisplayName("GET /api/v1/vendors/fassto/settlements/daily-costs/{yearMonth}/{whCd}/{customerCode} - 물류비 일별 비용 조회")
    class GetDailyCosts {

        @Test
        @DisplayName("경로 파라미터를 Command로 매핑하여 UseCase에 위임하고 OK를 반환한다")
        void returnsOk() {
            // given
            when(getFulfillmentSettlementDailyCostsUseCase.getDailyCosts(any(GetFulfillmentSettlementDailyCostsCommand.class)))
                    .thenReturn(GetFulfillmentSettlementDailyCostsResult.of(0, List.of()));

            // when
            ResponseEntity<BaseResponse<GetFulfillmentSettlementDailyCostsResponse>> response =
                    controller.getDailyCosts("202604", "WH01", CUSTOMER_CODE, ACCESS_TOKEN);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            ArgumentCaptor<GetFulfillmentSettlementDailyCostsCommand> captor =
                    ArgumentCaptor.forClass(GetFulfillmentSettlementDailyCostsCommand.class);
            verify(getFulfillmentSettlementDailyCostsUseCase).getDailyCosts(captor.capture());
            assertThat(captor.getValue().customerCode()).isEqualTo(CUSTOMER_CODE);
            assertThat(captor.getValue().accessToken()).isEqualTo(ACCESS_TOKEN);
        }
    }
}
