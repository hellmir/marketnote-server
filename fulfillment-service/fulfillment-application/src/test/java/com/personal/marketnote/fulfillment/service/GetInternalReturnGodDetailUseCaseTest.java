package com.personal.marketnote.fulfillment.service;

import com.personal.marketnote.fulfillment.domain.FulfillmentAccessToken;
import com.personal.marketnote.fulfillment.port.in.command.GetInternalReturnGodDetailCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentReturnGodDetailCommand;
import com.personal.marketnote.fulfillment.port.in.result.GetInternalReturnGodDetailResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.FulfillmentReturnGodDetailGoodsResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.FulfillmentReturnGodDetailInfoResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentReturnGodDetailResult;
import com.personal.marketnote.fulfillment.port.out.vendor.DisconnectFulfillmentAuthPort;
import com.personal.marketnote.fulfillment.port.out.vendor.GetFulfillmentCustomerCodePort;
import com.personal.marketnote.fulfillment.port.out.vendor.GetFulfillmentReturnGodDetailPort;
import com.personal.marketnote.fulfillment.port.out.vendor.RequestFulfillmentAuthPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetInternalReturnGodDetailUseCaseTest {
    @Mock
    private GetFulfillmentCustomerCodePort getFulfillmentCustomerCodePort;
    @Mock
    private RequestFulfillmentAuthPort requestFulfillmentAuthPort;
    @Mock
    private DisconnectFulfillmentAuthPort disconnectFulfillmentAuthPort;
    @Mock
    private GetFulfillmentReturnGodDetailPort getFulfillmentReturnGodDetailPort;

    @InjectMocks
    private GetInternalReturnGodDetailService getInternalReturnGodDetailService;

    @Nested
    @DisplayName("정상 조회")
    class SuccessfulQueryTest {

        @Test
        @DisplayName("반품 슬립 번호로 조회하면 Fassto 인증 후 검수 상세 결과를 반환한다")
        void getReturnGodDetail_withValidSlipNumbers_returnsResult() {
            // given
            String returnSlipNumbers = "SLIP001,SLIP002";
            String customerCode = "CUST001";
            String accessToken = "test-token";

            FulfillmentAccessToken token = mock(FulfillmentAccessToken.class);
            when(token.getValue()).thenReturn(accessToken);
            when(requestFulfillmentAuthPort.requestAccessToken()).thenReturn(token);
            when(getFulfillmentCustomerCodePort.getCustomerCode()).thenReturn(customerCode);

            FulfillmentReturnGodDetailGoodsResult goods = FulfillmentReturnGodDetailGoodsResult.of(
                    "PROD001", "테스트 상품", "20260401", "20270401", "1", null, "01", "정상"
            );
            FulfillmentReturnGodDetailInfoResult info = FulfillmentReturnGodDetailInfoResult.of(
                    "1", null, null, customerCode, null, "SLIP001", null, null,
                    null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null, null, null, null,
                    List.of(goods)
            );
            GetFulfillmentReturnGodDetailResult fasstoResult = GetFulfillmentReturnGodDetailResult.of(1, List.of(info));
            when(getFulfillmentReturnGodDetailPort.getReturnGodDetail(any(GetFulfillmentReturnGodDetailCommand.class)))
                    .thenReturn(fasstoResult);

            GetInternalReturnGodDetailCommand command = GetInternalReturnGodDetailCommand.of(returnSlipNumbers);

            // when
            GetInternalReturnGodDetailResult result = getInternalReturnGodDetailService.getReturnGodDetail(command);

            // then
            assertThat(result.dataCount()).isEqualTo(1);
            assertThat(result.returnGodInfos()).hasSize(1);
            assertThat(result.returnGodInfos().get(0).orderNumber()).isEqualTo("1");
            assertThat(result.returnGodInfos().get(0).products()).hasSize(1);
            assertThat(result.returnGodInfos().get(0).products().get(0).returnProductCheckStatus()).isEqualTo("01");

            verify(requestFulfillmentAuthPort).requestAccessToken();
            verify(disconnectFulfillmentAuthPort).disconnectAccessToken(accessToken);
        }

        @Test
        @DisplayName("Fassto 응답이 빈 목록이면 빈 결과를 반환한다")
        void getReturnGodDetail_emptyResponse_returnsEmptyResult() {
            // given
            String returnSlipNumbers = "SLIP001";
            FulfillmentAccessToken token = mock(FulfillmentAccessToken.class);
            when(token.getValue()).thenReturn("test-token");
            when(requestFulfillmentAuthPort.requestAccessToken()).thenReturn(token);
            when(getFulfillmentCustomerCodePort.getCustomerCode()).thenReturn("CUST001");

            GetFulfillmentReturnGodDetailResult fasstoResult = GetFulfillmentReturnGodDetailResult.of(0, List.of());
            when(getFulfillmentReturnGodDetailPort.getReturnGodDetail(any(GetFulfillmentReturnGodDetailCommand.class)))
                    .thenReturn(fasstoResult);

            GetInternalReturnGodDetailCommand command = GetInternalReturnGodDetailCommand.of(returnSlipNumbers);

            // when
            GetInternalReturnGodDetailResult result = getInternalReturnGodDetailService.getReturnGodDetail(command);

            // then
            assertThat(result.dataCount()).isEqualTo(0);
            assertThat(result.returnGodInfos()).isEmpty();
        }
    }

    @Nested
    @DisplayName("인증 토큰 관리")
    class AuthTokenManagementTest {

        @Test
        @DisplayName("Fassto API 호출 실패 시에도 accessToken이 해제된다")
        void getReturnGodDetail_fasstoFails_disconnectsToken() {
            // given
            String accessToken = "test-token";
            FulfillmentAccessToken token = mock(FulfillmentAccessToken.class);
            when(token.getValue()).thenReturn(accessToken);
            when(requestFulfillmentAuthPort.requestAccessToken()).thenReturn(token);
            when(getFulfillmentCustomerCodePort.getCustomerCode()).thenReturn("CUST001");
            when(getFulfillmentReturnGodDetailPort.getReturnGodDetail(any(GetFulfillmentReturnGodDetailCommand.class)))
                    .thenThrow(new RuntimeException("Fassto API 오류"));

            GetInternalReturnGodDetailCommand command = GetInternalReturnGodDetailCommand.of("SLIP001");

            // when & then
            try {
                getInternalReturnGodDetailService.getReturnGodDetail(command);
            } catch (RuntimeException ignored) {
            }

            verify(disconnectFulfillmentAuthPort).disconnectAccessToken(accessToken);
        }
    }
}
